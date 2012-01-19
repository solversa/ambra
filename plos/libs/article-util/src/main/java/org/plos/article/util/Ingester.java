/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;
import org.topazproject.xml.transform.cache.CachedSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The article ingestor.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class Ingester {
  private static final Log    log = LogFactory.getLog(Ingester.class);

  private static final String OUT_LOC_P    = "output-loc";
  private static final String DS_LOC_P     = "datastream-loc";

  private static final String OL_LOG_A     = "logMessage";
  private static final String OL_AID_A     = "articleId";
  private static final String OBJECT       = "Object";
  private static final String O_PID_A      = "pid";
  private static final String O_INDEX_A    = "doIndex";
  private static final String DATASTREAM   = "Datastream";
  private static final String DS_CONTLOC_A = "contLoc";
  private static final String DS_ID_A      = "id";
  /*
  private static final String DS_ST_A      = "state";
  private static final String DS_CGRP_A    = "controlGroup";
  private static final String DS_MIME_A    = "mimeType";
  private static final String DS_LBL_A     = "label";
  private static final String DS_ALTID_A   = "altIds";
  private static final String DS_FMT_A     = "formatUri";
  private static final String DSCRPTN      = "Description";
  */
  private static final String RDF          = "RDF";
  
  private static final String RDFNS        = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String RDF_MDL_A    = "model";

  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static final String FGS_REPO     = CONF.getString("topaz.fedoragsearch.repository");

  private final TransformerFactory tFactory;
  private final Session            sess;
  private final FedoraAPIM         apim;
  private final Uploader           uploader;
  private final FgsOperations[]    fgs;

  /**
   * Create a new ingester pointing to the given fedora server.
   *
   * @param sess     the OTM session to use for RDF updates
   * @param apim     the fedora management object to use
   * @param uploader the uploader to use to upload to fedora
   * @param fgs      an array of fedoragsearch clients
   */
  public Ingester(Session sess, FedoraAPIM apim, Uploader uploader, FgsOperations[] fgs) {
    this.sess = sess;
    this.apim = apim;
    this.uploader = uploader;
    this.fgs = fgs;

    tFactory = new TransformerFactoryImpl();
    tFactory.setURIResolver(new URLResolver());
    tFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
  }

  /**
   * Ingest a new article.
   *
   * @param zip  the zip archive containing the article and it's related objects
   * @param exclude ZipEntry name exclusion Pattern
   * @return the URI of the new article
   * @throws DuplicateArticleIdException if an article or other object already exists with any
   *                                     of the URI's specified in the zip
   * @throws IngestException if there's any other problem ingesting the article
   * @see Zip2Xml#describeZip(Zip, Pattern)
   */
  public String ingest(Zip zip, Pattern exclude) throws DuplicateArticleIdException, IngestException {
    File tmpDir = createTempDir();

    try {
      // get zip info
      String zipInfo = Zip2Xml.describeZip(zip, exclude);
      if (log.isDebugEnabled())
        log.debug("Extracted zip-description: " + zipInfo);

      // find ingest format handler
      String handler = findIngestHandler(zipInfo);
      if (log.isDebugEnabled())
        log.debug("Using ingest handler '" + handler + "'");

      // use handler to convert zip to fedora-object and RDF descriptions
      Document objInfo = zip2Obj(zip, zipInfo, handler, tmpDir);
      if (log.isDebugEnabled())
        log.debug("Got object-info '" + dom2String(objInfo) + "'");

      // get the article id
      Element objList = objInfo.getDocumentElement();
      String uri = objList.getAttribute(OL_AID_A);

      RollbackBuffer rb = new RollbackBuffer();
      try {
        /* Note that the rollback buffer is far from perfect: if mulgara already contains any
         * of the new statements we're creating, then a rollback will delete those existing
         * statements...
         *
         * For this reason fedoraIngest must be first, since that will catch duplicate articles,
         * and we really don't want to delete existing RDF or permissions in this case.
         */
        fedoraIngest(zip, objInfo, apim, uploader, rb);
        mulgaraInsert(objInfo, sess, rb);
        rb = null;
      } finally {
        if (rb != null) {
          log.info("Rolling back failed ingest for '" + uri + "'");
          rb.doRollback(sess, apim, fgs);
        }
      }

      log.info("Successfully ingested '" + uri + "'");
      return uri;
    } catch (RemoteException re) {
      throw new IngestException("Error ingesting into fedora", re);
    } catch (IOException ioe) {
      throw new IngestException("Error talking to fedora", ioe);
    } catch (OtmException oe) {
      throw new IngestException("Error talking to mulgara", oe);
    } catch (URISyntaxException use) {
      throw new IngestException("Zip format error", use);
    } catch (TransformerException te) {
      throw new IngestException("Zip format error", te);
    } finally {
      try {
        FileUtils.deleteDirectory(tmpDir);
      } catch (IOException ioe) {
        log.warn("Failed to delete tmp-dir '" + tmpDir + "'", ioe);
      }
    }
  }

  private static final File createTempDir() throws IngestException {
    try {
      for (int idx = 0; idx < 10; idx++) {
        File f = File.createTempFile("ingest_", ".d");
        FileUtils.forceDelete(f);
        if (f.mkdir())
          return f;
      }
      throw new IngestException("Failed to create temporary directory - tried 10 times");
    } catch (IOException ioe) {
      throw new IngestException("Failed to create or delete temporary file", ioe);
    }
  }

  /**
   * Look up the appropriate stylesheet to handle the zip. This stylesheet is responsible
   * for converting a zip-info doc (zip.dtd) to a fedora-objects doc (fedora.dtd).
   *
   * @param zipInfo the zip archive description (zip.dtd)
   * @return the URL of the stylesheet
   */
  private String findIngestHandler(String zipInfo) {
    // FIXME: make this configurable, and allow for some sort of lookup
    return getClass().getResource("pmc2obj.xslt").toString();
  }

  /**
   * Get the stylesheet that generates the triples from an RDF/XML doc.
   *
   * @return the URL of the stylesheet
   */
  private String findRDFXML2TriplesConverter() {
    // FIXME: make this configurable
    return getClass().getResource("RdfXmlToTriples.xslt").toString();
  }

  /**
   * Get the stylesheet that generates the foxml doc from an object description (fedora.dtd).
   *
   * @return the URL of the stylesheet
   */
  private String findFoxmlGenerator() {
    // FIXME: make this configurable
    return getClass().getResource("obj2foxml.xslt").toString();
  }

  /**
   * Run the main ingest script.
   *
   * @param zip     the zip archive containing the items to ingest
   * @param zipInfo the document describing the zip archive (adheres to zip.dtd)
   * @param handler the stylesheet to run on <var>zipInfo</var>; this is the main script
   * @param tmpDir  the temporary directory to use for storing files
   * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
   * @throws TransformerException if an error occurs during the processing
   */
  private Document zip2Obj(Zip zip, String zipInfo, String handler, File tmpDir)
      throws TransformerException {
    Transformer t = tFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setURIResolver(new ZipURIResolver(zip));
    ((Controller) t).setBaseOutputURI(tmpDir.toURI().toString());
    t.setParameter(OUT_LOC_P, tmpDir.toURI().toString());

    /* Note: it would be preferable (and correct according to latest JAXP specs) to use
     * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
     * Hence we need to use Saxon's API's in order to get at those messages.
     */
    final StringWriter msgs = new StringWriter();
    ((Controller) t).makeMessageEmitter();
    ((Controller) t).getMessageEmitter().setWriter(msgs);
    t.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing zip", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }
    });

    Source    inp = new StreamSource(new StringReader(zipInfo), "zip:/");
    DOMResult res = new DOMResult();

    try {
      t.transform(inp, res);
    } catch (TransformerException te) {
      if (msgs.getBuffer().length() > 0)
        throw new TransformerException(msgs.toString(), te);
      else
        throw te;
    }
    if (msgs.getBuffer().length() > 0)
      throw new TransformerException(msgs.toString());

    return (Document) res.getNode();
  }

  /**
   * Insert the RDF into the triple-store according to the given object-info doc.
   *
   * @param objInfo the document describing the RDF to insert
   * @param sess    the session to access the RDF store
   * @param rb      where to store the list of completed operations that need to be undone
   *                in case of a rollback
   */
  private void mulgaraInsert(Document objInfo, Session sess, final RollbackBuffer rb)
      throws IngestException, TransformerException, OtmException {
    final Element objList = objInfo.getDocumentElement();

    // set up the transformer to generate the triples
    final Transformer tf = tFactory.newTransformer(new StreamSource(findRDFXML2TriplesConverter()));

    // do the inserts
    boolean done = false;
    try {
      done = TransactionHelper.doInTxE(sess, new TransactionHelper.ActionE<Boolean, Exception>() {
        public Boolean run(Transaction tx) throws Exception {
          NodeList objs = objList.getElementsByTagNameNS(RDFNS, RDF);
          for (int idx = 0; idx < objs.getLength(); idx++) {
            Element obj = (Element) objs.item(idx);
            mulgaraInsertOneRDF(tx.getSession(), obj, tf, rb);
          }

          return true;
        }
      });
    } catch (IngestException ie) {
      throw ie;
    } catch (TransformerException te) {
      throw te;
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("Impossible", e);
    } finally {
      if (!done && (sess.getTransaction() == null))
        rb.rdfStmts.clear();    // tx was rolled back
    }
  }

  private void mulgaraInsertOneRDF(Session sess, Element obj, Transformer t, RollbackBuffer rb)
      throws IngestException, TransformerException, OtmException {
    // figure out the model
    String m = "ri";
    if (obj.hasAttribute(RDF_MDL_A)) {
      m = obj.getAttribute(RDF_MDL_A);
      obj.removeAttribute(RDF_MDL_A);
    }

    ModelConfig mc = sess.getSessionFactory().getModel(m);
    if (mc == null)
      throw new IngestException("Internal error: model '" + mc + "' not found in configuration");
    String model = mc.getUri().toString();

    // create the iTQL insert statements
    StringWriter sw = new StringWriter(500);
    sw.write("insert ");

    t.transform(new DOMSource(obj), new StreamResult(sw));
    if (!hasNonWS(sw.getBuffer(), 7))
      return;

    int stmtLen = sw.getBuffer().length();
    sw.write(" into <" + model + ">;");

    // insert
    sess.doNativeUpdate(sw.toString());
    rb.addRDFStatements(model, sw.getBuffer().substring(7, stmtLen));
  }

  private static final boolean hasNonWS(CharSequence seq, int idx) {
    for (; idx < seq.length(); idx++) {
      if (!Character.isWhitespace(seq.charAt(idx)))
        return true;
    }

    return false;
  }

  /**
   * Create the objects in Fedora according to the given object-info doc.
   *
   * @param zip      the zip archive containing the data for the objects to ingest
   * @param objInfo  the document describing the objects and their datastreams to create
   * @param apim     the handle to access the fedora management api
   * @param uploader the handle to access the fedora upload api
   * @param rb       where to store the list of completed operations that need to be undone
   *                 in case of a rollback
   */
  private void fedoraIngest(Zip zip, Document objInfo, FedoraAPIM apim, Uploader uploader,
                            RollbackBuffer rb)
      throws DuplicateArticleIdException, TransformerException, IOException, RemoteException,
             URISyntaxException {
    // set up the transformer to generate the foxml docs
    Transformer t = tFactory.newTransformer(new StreamSource(findFoxmlGenerator()));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    // get the log message to use
    Element objList = objInfo.getDocumentElement();
    String logMsg = objList.getAttribute(OL_LOG_A);
    if (logMsg == null)
      logMsg = "Ingest";

    // create the fedora objects
    NodeList objs = objList.getElementsByTagName(OBJECT);
    for (int idx = 0; idx < objs.getLength(); idx++) {
      Element obj = (Element) objs.item(idx);
      fedoraIngestOneObj(apim, uploader, obj, t, zip, logMsg, rb);
    }
  }

  private void fedoraIngestOneObj(FedoraAPIM apim, Uploader uploader, Element obj, Transformer t,
                                  Zip zip, String logMsg, RollbackBuffer rb)
      throws DuplicateArticleIdException, TransformerException, IOException, RemoteException,
             URISyntaxException {
    // upload all (non-DC/non-RDF) datastreams
    NodeList dss = obj.getElementsByTagName(DATASTREAM);
    String[] dsLoc = new String[dss.getLength()];

    for (int idx = 0; idx < dss.getLength(); idx++) {
      Element ds = (Element) dss.item(idx);
      dsLoc[idx] = fedoraUploadDS(uploader, ds, zip);
    }

    // create the foxml doc
    StringWriter sw = new StringWriter(200);
    t.setParameter(DS_LOC_P, dsLoc);
    t.transform(new DOMSource(obj), new StreamResult(sw));

    // create the fedora object
    String pid = obj.getAttribute(O_PID_A);
    fedoraCreateObject(apim, pid, sw.toString(), logMsg);
    rb.addFedoraObject(pid);

    // index object with fedoragsearch (lucene)
    String doIndex = obj.getAttribute(O_INDEX_A);
    if (doIndex != null && doIndex.equals("true")) {
      fgsIndex(pid);
      rb.addFGSObject(pid);
    }
  }

  private void fedoraCreateObject(FedoraAPIM apim, String pid, String foxml, String logMsg)
      throws DuplicateArticleIdException, IOException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Ingesting fedora object '" + pid + "'; foxml = " + foxml);

    try {
      apim.ingest(foxml.getBytes("UTF-8"), "foxml1.0", logMsg);
    } catch (RemoteException re) {
      FedoraUtil.detectDuplicateArticleIdException(re, pidToURI(pid));
    }
  }

  private static final String pidToURI(String pid) throws IOException {
    String doi = URLDecoder.decode(pid.substring(pid.indexOf(":") + 1), "UTF-8");
    return "info:doi/" + doi;
  }

  private String fedoraUploadDS(Uploader uploader, Element ds, Zip zip)
      throws IOException, URISyntaxException {
    log.debug("Uploading datastream '" + ds.getAttribute(DS_CONTLOC_A) + "', id = '" +
              ds.getAttribute(DS_ID_A) + "'");

    long[] size = new long[1];
    InputStream is = getContent(ds, zip, size);
    return (size[0] >= 0) ? uploader.upload(is, size[0]) : uploader.upload(is);
  }

  private InputStream getContent(Element ds, Zip zip, long[] size)
      throws IOException, URISyntaxException {
    InputStream is;

    if (ds.hasAttribute(DS_CONTLOC_A)) {
      URI contLoc = new URI(ds.getAttribute(DS_CONTLOC_A));
      if (contLoc.getScheme() == null || contLoc.getScheme().equals("zip")) {
        is = zip.getStream(contLoc.getSchemeSpecificPart(), size);
      } else {
        URLConnection con = contLoc.toURL().openConnection();

        size[0] = con.getContentLength();
        is      = con.getInputStream();
      }
    } else {
      StringBuffer sb = new StringBuffer(500);
      for (Node n = ds.getFirstChild(); n != null; n = n.getNextSibling())
        sb.append(dom2String(n));

      byte[] cont = sb.toString().getBytes("UTF-8");

      size[0] = cont.length;
      is      = new ByteArrayInputStream(cont);
    }

    return is;
  }

  private void fgsIndex(String pid) throws RemoteException {
    String result = "";
    int i = 0;
    try {
      for (i = 0; i < fgs.length; i++)
        result = fgs[i].updateIndex("fromPid", pid, FGS_REPO, null, null, null);
      if (log.isDebugEnabled())
        log.debug("Indexed '" + pid + "':\n" + result);
    } catch (RemoteException re) {
      log.info("Error ingesting to fgs server " + i + " unwinding ingests to servers 0-" + i +
               " (topaz.xml:<fedoragsearch><urls><url>)");
      for (int j = 0; j < i; j++) {
        try {
          result = fgs[j].updateIndex("deletePid", pid, FGS_REPO, null, null, null);
          log.info("Deleted pid '" + pid + "' from fgs server " + j + ":\n" + result);
        } catch (RemoteException re2) {
          log.warn("Unable to delete pid '" + pid + "' from fgs server " + j +
                   " after indexing failed on server " + i +
                   " (topaz.xml:<fedoragsearch><urls><url>)", re2);
        }
      }
      throw re;
    }
  }

  private String dom2String(Node dom) {
    try {
      StringWriter sw = new StringWriter(500);
      Transformer t = tFactory.newTransformer();
      t.transform(new DOMSource(dom), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException te) {
      log.error("Error converting dom to string", te);
      return "";
    }
  }

  private static class RollbackBuffer {
    List fedoraObjects = new ArrayList();
    List fgsObjects    = new ArrayList();
    Map  rdfStmts      = new HashMap();

    void addFedoraObject(String pid) {
      fedoraObjects.add(pid);
    }

    void addFGSObject(String pid) {
      fgsObjects.add(pid);
    }

    void addRDFStatements(String model, String statements) {
      List s = (List) rdfStmts.get(model);
      if (s == null)
        rdfStmts.put(model, s = new ArrayList());

      s.add(statements);
    }

    void doRollback(Session sess, FedoraAPIM apim, FgsOperations[] fgs) {
      // clear out RDF statements
      log.debug("Rolling back RDF statements");
      for (Iterator iter = rdfStmts.keySet().iterator(); iter.hasNext(); ) {
        String model = (String) iter.next();

        for (Iterator iter2 = ((List) rdfStmts.get(model)).iterator(); iter2.hasNext(); ) {
          String stmts = (String) iter2.next();
          String cmd   = "delete " + stmts + " from <" + model + ">;";
          try {
            sess.doNativeUpdate(cmd);
          } catch (Exception e) {
            log.error("Error while rolling back failed ingest: failed itql command was '" + cmd +
                      "'", e);
          }
        }
      }

      // clear out search index
      log.debug("Rolling back fgs index");
      for (Iterator iter = fgsObjects.iterator(); iter.hasNext(); ) {
        String pid = (String) iter.next();
        for (int i = 0; i < fgs.length; i++) {
          try {
            fgs[i].updateIndex("deletePid", pid, FGS_REPO, null, null, null);
          } catch (Exception e) {
            log.error("Error while rolling back failed ingest: fgs deletePid failed for pid '" +
                      pid + "' on repo '" + FGS_REPO + "' on server " + i +
                      " (topaz.xml:<fedoragsearch><urls><url>)", e);
          }
        }
      }

      // delete fedora objects
      log.debug("Rolling back fedora objects");
      for (Iterator iter = fedoraObjects.iterator(); iter.hasNext(); ) {
        String pid = (String) iter.next();
        try {
          apim.purgeObject(pid, "Rolling back failed ingest", false);
        } catch (Exception e) {
          log.error("Error while rolling back failed ingest: fedora purge object failed for pid '" +
                    pid + "'", e);
        }
      }
    }
  }

  /**
   * Saxon's default resolver uses URI.resolve() to resolve the relative URI's. However, that
   * doesn't understand 'jar' URL's and treats them as opaque (because of the "jar:file:"
   * prefix). That in turn prevents us from using relative URI's for things like xsl:include
   * and xsl:import . Hence this class here, which just uses URL to do the resolution.
   */
  private static class URLResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
      if (href.length() == 0)
        return null;  // URL doesn't handle this case properly, so let default resolver handle it

      try {
        URL url = new URL(new URL(base), href);
        return new StreamSource(url.toString());
      } catch (MalformedURLException mue) {
        log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
                 "default URIResolver", mue);
        return null;
      }
    }
  }

  /**
   * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
   */
  private static class ZipURIResolver extends URLResolver {
    private final Zip zip;

    /**
     * Create a new resolver that returns documents from the given zip.
     *
     * @param zip the zip archive to return documents from
     */
    public ZipURIResolver(Zip zip) {
      this.zip = zip;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      if (log.isDebugEnabled())
        log.debug("resolving: base='" + base + "', href='" + href + "'");

      if (!base.startsWith("zip:"))
        return super.resolve(href, base);

      try {
        URI uri = URI.create(base).resolve(href);
        InputStream is = zip.getStream(uri.getPath().substring(1), new long[1]);
        if (is == null)         // hack to deal with broken AP zip's that contain absolute paths
          is = zip.getStream(uri.getPath(), new long[1]);

        if (log.isDebugEnabled())
          log.debug("resolved: uri='" + uri + "', found=" + (is != null));

        if (is == null)
          return null;

        InputSource src = new InputSource(is);
        src.setSystemId(uri.toString());

        return new CachedSource(src);
      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      } catch (SAXException se) {
        throw new TransformerException(se);
      }
    }
  }
}

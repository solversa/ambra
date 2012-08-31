<!-- begin : main content -->
<div id="content">
  <!-- begin : home page wrapper -->
  <div id="wrap">
    <div id="home">
      <!-- begin : layout wrapper -->
      <div class="col">
        <!-- begin : wrapper for cols 1 & 2 -->
        <div id="first" class="col">
        <!-- SWT removed col 1 -->
        <!-- begin : col 2 -->
          <div class="col last">
            <div class="horizontalTabs">
              <ul id="tabsContainer">
                <!-- Tabs generated by global file horizontalTabs.js - parameters are set in local file config_home.js -->
              </ul>
							<div id="tabPaneSet" class="contentwrap">
                  <#include "article/recentArticles.ftl">
              </div>
            </div><!-- end : horizontalTabs -->
            <!-- begin : calls to action blocks -->
            <div class="ctaWrap">
              <div id="cta1">
                <strong>Publish with PLoS</strong>
                <a href="${checklist}">We want to publish your work</a>
              </div>
              <div id="cta2">
                <strong>Have Your Say</strong>
                <a href="${comment}">Add ratings and discussions</a>
              </div>
              <div class="clearer">&nbsp;</div>
            </div>
            <!-- end : calls to action blocks -->
            <div class="block">
              <h2>Perspective</h2>
              <@s.url id="featured" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000065"/>
              <div class="article section lastSection">
                <h3><@s.a href="${featured}" title="Read Open-Access Article">Computational Biology in Argentina</em></@s.a></h3>
                <img src="images/imgPlaceholder_90px.gif" alt="article image" />
                <p>Sebastian Bassi and colleagues from the Universidad Nacional de Quilmes, Buenos Aires, reflect on the identity of the interdisciplinary field of computational biology both generally and specifically in their country, Argentina.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end block -->
            <div class="block">
              <h2>Featured Research</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000056"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000001"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000067"/>
              <div class="article section">
                <h3><@s.a href="${newNoted1}" title="Read Open-Access Article">Genes and (Common) Pathways Underlying Drug Addiction</@s.a></h3>
                <img src="images/imgPlaceholder_90px.gif" alt="article image" />
                <p>In order to explore genes and pathways underlying addiction, Wei et al. from Beijing University integrated data derived from multiple technology platforms and collected 2,343 items of evidence linking genes and chromosome regions to addiction.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3><@s.a href="${newNoted2}" title="Read Open-Access Article">Most <em>Caenorhabditis elegans</em> microRNAs Are Individually Not Essential for Development or Viability</@s.a></h3>
                <img src="images/imgPlaceholder_90px.gif" alt="article image" />
                <p>This study identifies a new role for miRNAs in <em>Caenorhabditis elegans</em> and also demonstrates that most miRNAs are not essential for development.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3><@s.a href="${newNoted3}" title="Read Open-Access Article">Social Interactions in Myxobacterial Swarming</@s.a></h3>
                <img src="images/imgPlaceholder_90px.gif" alt="article image" />
                <p>Alber and colleagues introduce a cell-based model to study how interactions between neighboring cells facilitate bacterial swarming.</p>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3><@s.a href="${newNoted3}" title="Read Open-Access Article">Density Dependence Triggers Runaway Selection of Reduced Senescence</@s.a></h3>
                <img src="images/imgPlaceholder_90px.gif" alt="article image" />
                <p>Seymour and Doncaster explore the invasion of mutations that increase/decrease the rate of senescence into populations with explicit density dependence. Their model reveals circumstances in which senescence is not inevitable.</p>
                <div class="clearer">&nbsp;</div>
              </div>
            </div><!-- end : block -->
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <div class="section lastSection">
                <h3><a href="http://www.plosone.org/"><em>PLoS ONE</em></a></h3>
                <ul class="articles">
                  <li><a href="#" title="Read Open Access Article">Linking Gene Expression and Functional Network Data in Human Heart Failure and Some Additional Text to Make a Longer Title for Testing</a>
                  <!-- I dont think these should get blurbs <p>The authors, from the University of Ulster, assembled a global PPI network in human heart failure, establishing the significance of relationships between the differentiation of gene expression and connectivity degrees.</p>--></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div id="issue" class="block"><h3><a href="${tocStatic}">October 2007 Issue</a></h3><a href="${tocStatic}"><img src="images/imgPlaceholder_251px.gif" alt="issue cover image" /></a></div><!-- keep div#issue hmtl all on one line to avoid extra space below issue image in IE -->
            <!-- end : issue block -->
            <!-- begin : mission block -->
            <div id="mission" class="block">
              <p><strong><em><a href="${info}">PLoS Computational Biology</a></em></strong> is a peer-reviewed, open-access journal featuring works of exceptional significance that further our understanding of living systems at all scales through the application of computational methods. It is the official journal of the <a href="http://www.iscb.org/">International Society for Computational Biology.</a></p>
            </div>
            <!-- end : mission block -->
            <!-- begin : iscb block -->
            <div id="iscb" class="block">
              <h3><a href="#"></a>ISCB</h3>
              <ul class="articles">
                  <li><a href="#">ISMB 2008 Toronto Calls for Participation</a></li>
                  <li><a href="#">Special Session Abstracts Now Online for ISMB 2008 Toronto</a></li>
                  <li><a href="#">Announcing C-SHALS, Conference on Semantics in Healthcare and Life Sciences - Registration now open</a></li>
                  <li><a href="#">Society Membership - Join Now</a></li>
              </ul>
            </div>
            <!-- end : iscb block -->
            <!-- begin : advocacy blocks -->
            <div id="adWrap">
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:156&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a341eeed' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:156&#38;source=CBI&#38;n=a341eeed' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:155&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=ae158f67' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:155&#38;source=CBI&#38;n=ae158f67' border='0' alt=''/></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:154&#38;source=CBI&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=ae53cfec' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:154&#38;source=CBI&#38;n=ae53cfec' border='0' alt=''/></a></noscript>
            </div>
            <!-- end : advocacy blocks -->
            <!-- begin : stay-connected block -->
            <div id="connect" class="block">
              <h3>Stay Connected</h3>
              <ul>
                  <li><img src="images/icon_alerts_small.gif" alt="email alerts icon" /><a href="${freemarker_config.registrationURL}"><strong>E-mail Alerts</strong></a><br />Sign up for alerts by e-mail</li>
                  <li><img src="images/icon_rss_small.gif" alt="rss icon" /><@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/><a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><strong>RSS</strong></a> (<a href="${rssinfo}">What is RSS?</a>)<br />Subscribe to content feed</li>
                  <li><img src="images/icon_join.gif" alt="join PLoS icon" /><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show Your Support"><strong>Join PLoS</strong></a><br />Support the open-access movement!</li>
              </ul>
            </div>
            <!-- end : stay-connected block -->
            <!-- begin : blog block -->
            <div id="blog" class="block">
              <h3>From the PLoS Blog</h3>
              <p>Read the <a href="http://www.plos.org/cms/blog" title="PLoS Blog">PLoS Blog</a> <a href="http://feeds.feedburner.com/plos/Blog"><img alt="RSS" src="images/feed-icon-inline.gif" /></a> and contribute your views on scientific research and open-access publishing.</p>
              <ul class="articles">
                  <li><a href="http://www.plos.org/cms/node/274">Oops we missed our own birthday</a></li>
                  <li><a href="http://www.plos.org/cms/node/272">Journal Clubs - think of the future!</a></li>
                  <li><a href="http://www.plos.org/cms/node/271">Using open-access articles for student projects</a></li>
              </ul>
            </div>
            <!-- end : blog block -->
          </div><!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner"><!--skyscraper-->
              <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                 
                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&#38;what=zone:24&#38;source=CBI&#38;block=1&#38;blockcampaign=1");
                 document.write ("&#38;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&#38;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a92cb003' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:24&#38;source=CBI&#38;n=a92cb003' border='0' alt=''/></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/pone_home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
				<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/pone_home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
				<a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
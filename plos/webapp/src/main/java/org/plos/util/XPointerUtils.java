/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * XPointer related utility methods.
 * 
 * @author jkirton
 * @see http://www.w3.org/TR/xpath
 * @see http://www.w3.org/TR/xptr-xpointer
 */
public abstract class XPointerUtils {

  /**
   * Creates a string-range xpointer fragment.
   * 
   * @param location
   * @param string
   * @param offset
   * @param length
   * @return string-range xpointer fragment
   */
  public static String createStringRangeFragment(String location, String string, int offset,
      int length) {
    return "string-range(" + location + ", '" + string + "', " + offset + ", " + length + ")[1]";
  }

  /**
   * Creates a string-range xpointer fragment.
   * 
   * @param location
   * @param string
   * @param offset
   * @return string-range xpointer fragment
   */
  public static String createStringRangeFragment(String location, String string, int offset) {
    return "string-range(" + location + ", '" + string + "')[" + offset + "]";
  }

  /**
   * @param startPoint
   * @param endPoint
   * @return range-to xpointer fragment
   */
  public static String createRangeToFragment(String startPoint, String endPoint) {
    return startPoint + "/range-to(" + endPoint + ")";
  }

  /**
   * Creates an xpointer string.
   * 
   * @param prefix
   * @param localPart
   * @param encoding The encoding to employ for encoding the local part URI
   * @return xpointer String
   * @throws UnsupportedEncodingException
   */
  public static String createXPointer(String prefix, String localPart, String encoding)
      throws UnsupportedEncodingException {
    return prefix + "#xpointer(" + URLEncoder.encode(localPart, encoding) + ")";
  }
}
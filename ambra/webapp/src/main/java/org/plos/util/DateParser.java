/*
 * $HeadURL::   $Id$
 * 
 * Copyright (c) 2006-2008 by Topaz, Inc. http://topazproject.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.plos.util;

// DateParser.java
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

/* W3C® SOFTWARE NOTICE AND LICENSE
http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
This work (and included software, documentation such as READMEs, or other related items) is being 
provided by the copyright holders under the following license. By obtaining, using and/or copying 
this work, you (the licensee) agree that you have read, understood, and will comply with the 
following terms and conditions.

Permission to copy, modify, and distribute this software and its documentation, with or without 
modification, for any purpose and without fee or royalty is hereby granted, provided that you 
include the following on ALL copies of the software and documentation or portions thereof, 
including modifications:

The full text of this NOTICE in a location viewable to users of the redistributed or derivative work. 
Any pre-existing intellectual property disclaimers, notices, or terms and conditions. If none exist,
the W3C Software Short Notice should be included (hypertext is preferred, text is permitted) within 
the body of any redistributed or derivative code. 
Notice of any changes or modifications to the files, including the date changes were made. 
(We recommend you provide URIs to the location from which the code is derived.) 
THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE NO REPRESENTATIONS 
OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY OR 
FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE 
ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.

COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES 
ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.

The name and trademarks of copyright holders may NOT be used in advertising or publicity pertaining 
to the software without specific, written prior permission. Title to copyright in this software and 
any associated documentation will at all times remain with copyright holders.


____________________________________

This formulation of W3C's notice and license became active on December 31 2002. This version 
removes the copyright ownership notice such that this license can be used with materials other than 
those owned by the W3C, reflects that ERCIM is now a host of the W3C, includes references to this 
specific dated version of the license, and removes the ambiguous grant of "use". Otherwise, this 
version is the same as the previous version and is written so as to preserve the Free Software 
Foundation's assessment of GPL compatibility and OSI's certification under the Open Source 
Definition. Please see our Copyright FAQ for common questions about using materials from our site,
including specific terms and conditions for packages like libwww, Amaya, and Jigsaw. Other 
questions about this notice can be directed to site-policy@w3.org.

Joseph Reagle <site-policy@w3.org> 
*/

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Date parser for ISO 8601 format http://www.w3.org/TR/1998/NOTE-datetime-19980827
 * 
 * @version $Revision$
 * @author Benoît Mahé (bmahe@w3.org)
 * @author Yves Lafon (ylafon@w3.org)
 */
public class DateParser {

  private static boolean check(StringTokenizer st, String token) throws InvalidDateException {
    try {
      if (st.nextToken().equals(token)) {
        return true;
      } else {
        throw new InvalidDateException("Missing [" + token + "]");
      }
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  private static Calendar getCalendar(String isodate) throws InvalidDateException {
    // YYYY-MM-DDThh:mm:ss.sTZD
    StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    try {
      // Year
      if (st.hasMoreTokens()) {
        int year = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.YEAR, year);
      } else {
        return calendar;
      }
      // Month
      if (check(st, "-") && (st.hasMoreTokens())) {
        int month = Integer.parseInt(st.nextToken()) - 1;
        calendar.set(Calendar.MONTH, month);
      } else {
        return calendar;
      }
      // Day
      if (check(st, "-") && (st.hasMoreTokens())) {
        int day = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.DAY_OF_MONTH, day);
      } else {
        return calendar;
      }
      // Hour
      if (check(st, "T") && (st.hasMoreTokens())) {
        int hour = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
      } else {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
      }
      // Minutes
      if (check(st, ":") && (st.hasMoreTokens())) {
        int minutes = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.MINUTE, minutes);
      } else {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
      }

      //
      // Not mandatory now
      //

      // Secondes
      if (!st.hasMoreTokens()) {
        return calendar;
      }
      String tok = st.nextToken();
      if (tok.equals(":")) { // secondes
        if (st.hasMoreTokens()) {
          int secondes = Integer.parseInt(st.nextToken());
          calendar.set(Calendar.SECOND, secondes);
          if (!st.hasMoreTokens()) {
            return calendar;
          }
          // frac sec
          tok = st.nextToken();
          if (tok.equals(".")) {
            // bug fixed, thx to Martin Bottcher
            String nt = st.nextToken();
            while (nt.length() < 3) {
              nt += "0";
            }
            nt = nt.substring(0, 3); // Cut trailing chars..
            int millisec = Integer.parseInt(nt);
            // int millisec = Integer.parseInt(st.nextToken()) * 10;
            calendar.set(Calendar.MILLISECOND, millisec);
            if (!st.hasMoreTokens()) {
              return calendar;
            }
            tok = st.nextToken();
          } else {
            calendar.set(Calendar.MILLISECOND, 0);
          }
        } else {
          throw new InvalidDateException("No secondes specified");
        }
      } else {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
      }
      // Timezone
      if (!tok.equals("Z")) { // UTC
        if (!(tok.equals("+") || tok.equals("-"))) {
          throw new InvalidDateException("only Z, + or - allowed");
        }
        boolean plus = tok.equals("+");
        if (!st.hasMoreTokens()) {
          throw new InvalidDateException("Missing hour field");
        }
        int tzhour = Integer.parseInt(st.nextToken());
        int tzmin = 0;
        if (check(st, ":") && (st.hasMoreTokens())) {
          tzmin = Integer.parseInt(st.nextToken());
        } else {
          throw new InvalidDateException("Missing minute field");
        }
        if (plus) {
          calendar.add(Calendar.HOUR, -tzhour);
          calendar.add(Calendar.MINUTE, -tzmin);
        } else {
          calendar.add(Calendar.HOUR, tzhour);
          calendar.add(Calendar.MINUTE, tzmin);
        }
      }
    } catch (NumberFormatException ex) {
      throw new InvalidDateException("[" + ex.getMessage() + "] is not an integer");
    }
    return calendar;
  }

  /**
   * Parse the given string in ISO 8601 format and build a Date object.
   * 
   * @param isodate
   *          the date in ISO 8601 format
   * @return a Date instance
   * @exception InvalidDateException
   *              if the date is not valid
   */
  public static Date parse(String isodate) throws InvalidDateException {
    if (isodate == null) {
      return null;
    } else {
      return getCalendar(isodate).getTime();
    }
  }

  private static String twoDigit(int i) {
    if (i >= 0 && i < 10) {
      return "0" + String.valueOf(i);
    }
    return String.valueOf(i);
  }

  /**
   * Generate a ISO 8601 date
   * 
   * @param date
   *          a Date instance
   * @return a string representing the date in the ISO 8601 format
   */
  public static String getIsoDate(Date date) {
    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.setTime(date);
    StringBuffer buffer = new StringBuffer();
    buffer.append(calendar.get(Calendar.YEAR));
    buffer.append("-");
    buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
    buffer.append("-");
    buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
    buffer.append("T");
    buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
    buffer.append(":");
    buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
    buffer.append(":");
    buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
    buffer.append(".");
    buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
    buffer.append("Z");
    return buffer.toString();
  }

  /**
   * Generate a ISO 8601 date
   * 
   * @param date
   *          a Date instance
   * @return a string representing the date in the ISO 8601 format
   */
  public static String getIsoDateNoMillis(Date date) {
    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.setTime(date);
    StringBuffer buffer = new StringBuffer();
    buffer.append(calendar.get(Calendar.YEAR));
    buffer.append("-");
    buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
    buffer.append("-");
    buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
    buffer.append("T");
    buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
    buffer.append(":");
    buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
    buffer.append(":");
    buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
    buffer.append("Z");
    return buffer.toString();
  }

  public static void test(String isodate) {
    System.out.println("----------------------------------");
    try {
      Date date = parse(isodate);
      System.out.println(">> " + isodate);
      System.out.println(">> " + date.toString() + " [" + date.getTime() + "]");
      System.out.println(">> " + getIsoDate(date));
    } catch (InvalidDateException ex) {
      System.err.println(isodate + " is invalid");
      System.err.println(ex.getMessage());
    }
    System.out.println("----------------------------------");
  }

  public static void test(Date date) {
    String isodate = null;
    System.out.println("----------------------------------");
    try {
      System.out.println(">> " + date.toString() + " [" + date.getTime() + "]");
      isodate = getIsoDate(date);
      System.out.println(">> " + isodate);
      date = parse(isodate);
      System.out.println(">> " + date.toString() + " [" + date.getTime() + "]");
    } catch (InvalidDateException ex) {
      System.err.println(isodate + " is invalid");
      System.err.println(ex.getMessage());
    }
    System.out.println("----------------------------------");
  }

  public static void main(String args[]) {
    test("1997-07-16T19:20:30.45-02:00");
    test("1997-07-16T19:20:30+01:00");
    test("1997-07-16T19:20:30+01:00");
    test("1997-07-16T12:20:30-06:00");
    test("1997-07-16T19:20");
    test("1997-07-16");
    test("1997-07");
    test("1997");
    test(new Date());
  }

}

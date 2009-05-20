/*
 * Copyright (c) 2008, 2009 by Xuggle Incorporated.  All rights reserved.
 * 
 * This file is part of Xuggler.
 * 
 * You can redistribute Xuggler and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Xuggler is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


import com.xuggle.test_utils.NameAwareTestClassRunner;
import com.xuggle.utils.TimeValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(NameAwareTestClassRunner.class)
public class TimeValueTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private String mTestName = null;
  
  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    log.debug("-----START----- {}", mTestName);
  }
  @After
  public void tearDown()
  {
    log.debug("----- END ----- {}", mTestName);    
  }

  @Test
  public void testConstruction()
  {
    TimeValue d = new TimeValue(3, TimeUnit.SECONDS);
    assertNotNull(d);
  }
  
  @Test
  public void testGet()
  {
    long duration = 6;
    TimeUnit unit = TimeUnit.SECONDS;
    TimeValue d = new TimeValue(duration, unit);
    assertNotNull(d);
    assertEquals(duration*1000*1000, d.get(TimeUnit.MICROSECONDS));
    assertEquals(duration*1000, d.get(TimeUnit.MILLISECONDS));
  }
  
  @Test
  public void testGetTimeUnit()
  {
    long duration = 62;
    TimeUnit unit = TimeUnit.MICROSECONDS;
    TimeValue d = new TimeValue(duration, unit);
    assertNotNull(d);
    assertEquals(unit, d.getTimeUnit());
  }
  
  @Test
  public void testCopy()
  {
    TimeValue d1 = new TimeValue(5, TimeUnit.NANOSECONDS);
    assertNotNull(d1);
    TimeValue d2 = new TimeValue(d1);
    assertNotNull(d2);
    assertEquals(d1.get(TimeUnit.NANOSECONDS), d2.get(TimeUnit.NANOSECONDS));
    assertEquals(d1.getTimeUnit(), d2.getTimeUnit());
    assertEquals(d1, d2);
    assertEquals(0, d1.compareTo(d2));
    assertEquals(d1.hashCode(), d2.hashCode());
  }
  
  @Test
  public void testCompareTo()
  {
    TimeValue d1 = new TimeValue(1001, TimeUnit.MILLISECONDS);
    TimeValue d2 = new TimeValue(1, TimeUnit.SECONDS);
    assertEquals(0, d1.compareTo(d1));
    assertEquals(0, d2.compareTo(d2));
    assertTrue(0 < d1.compareTo(d2));
    assertTrue(0 > d2.compareTo(d1));
  }
  
  @Test
  public void testCompareToValuesWrapped()
  {
    TimeValue d1 = new TimeValue(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    TimeValue d2 = new TimeValue(Long.MIN_VALUE, TimeUnit.NANOSECONDS);
    assertEquals(0, d1.compareTo(d1));
    assertEquals(0, d2.compareTo(d2));
    assertTrue(0 > d1.compareTo(d2));
    assertTrue(0 < d2.compareTo(d1));
  }
  
  @Test
  public void testEquals()
  {
    TimeValue d1 = new TimeValue(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
    TimeValue d2 = new TimeValue(Long.MAX_VALUE-1, TimeUnit.MICROSECONDS);
    assertEquals(0, d1.compareTo(d1));
    assertEquals(0, d2.compareTo(d2));
    assertTrue(0 < d1.compareTo(d2));
    assertTrue(0 > d2.compareTo(d1));
    
    // now, the hash code
    assertFalse(d1.equals(d2));
    // our hash down-grades to nano-seconds, which should cause the hash
    // to be the same here.
    assertEquals(d1.hashCode(), d2.hashCode());
    
    assertFalse(d1.equals(null));
  }
  
  @Test(expected=NullPointerException.class)
  public void testCompareToNullInput()
  {
    TimeValue d1 = new TimeValue(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
    d1.compareTo(null); // should throw exception
  }
  
  @Test
  public void testStringFormat()
  {
    TimeValue d1 = new TimeValue(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
    String s = d1.toString();
    assertEquals("9,223,372,036,854,775,807 (MICROSECONDS)", s);
  }

  @Test
  public void testConvert()
  {
    TimeValue d1 = new TimeValue(2, TimeUnit.MICROSECONDS);
    
    long nanoSeconds = d1.get(TimeUnit.NANOSECONDS);
    assertEquals(2000, nanoSeconds);
  }
}

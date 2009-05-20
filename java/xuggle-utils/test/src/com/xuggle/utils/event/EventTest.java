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

package com.xuggle.utils.event;
import static junit.framework.Assert.assertTrue;


import com.xuggle.test_utils.NameAwareTestClassRunner;
import com.xuggle.utils.event.Event;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(NameAwareTestClassRunner.class)
public class EventTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private String mTestName = null;
  
  @Before
  public void setUp()
  {
    mTestName = NameAwareTestClassRunner.getTestMethodName();
    log.debug("Running test: {}", mTestName);
  }

  @Test
  public void testCreateEvent()
  {
    Event event = new Event(this){};
    assertTrue(event.getSource() == this);
  }
}

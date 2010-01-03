/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Utils.
 *
 * Xuggle-Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Utils.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.xuggle.test_utils;

import java.util.ConcurrentModificationException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/**
 * Provides a JUnit4 way of getting the name of the test method currently executing.
 * <p>
 * Provides the current test name, available for the <code>@Test</code> method
 * and <code>@Before</code> and <code>@After</code> methods, <strong>just
 *  like the default runner should!</strong><br/> (see <code>Test.getName()</code>
 *   in JUnit3)
 *   </p>
 * @since JUnit 4
 * @author aclarke@xuggle.com
 * @author Joshua.Graham@thoughtworks.com
 * @author based on http://tech.groups.yahoo.com/group/junit/message/18728
 * @author based on http://grahamis.com/blog/2007/05/22/junit-4-cant-tell-the-test-its-name/
 * 
 */
public class NameAwareTestClassRunner extends BlockJUnit4ClassRunner {
  private static Object mClassLock = new Object();
  private static String mGlobalTestClassName=null;
  private static String mGlobalTestMethodName=null;
  private final static ThreadLocal<String> testClassName = new ThreadLocal<String>();

  private final static ThreadLocal<String> testMethodName = new ThreadLocal<String>();

  protected static void setName(final String name) {
    testMethodName.set(extractTestMethodName(name));
    testClassName.set(extractTestClassName(name));
    synchronized(mClassLock)
    {
      mGlobalTestClassName = testClassName.get();
      mGlobalTestMethodName = testMethodName.get();
    }
  }

  /**
   * JUnit 4 test name format
   * @return method(package.class)
   */
  public static String getTestName() {
    return String.format("%s(%s)", getTestMethodName(), getTestClassName());
  }

  public static String getTestMethodName() {
    String methodName = testMethodName.get();
    if (methodName == null)
      // use the global
      methodName = mGlobalTestMethodName;
    return methodName;
  }

  public static String getTestClassName() {
    String className = testClassName.get();
    if (className == null)
      className = mGlobalTestClassName;
    return className;
  }

  public NameAwareTestClassRunner(final Class<?> klass) throws InitializationError {
    super(klass);
  }

  public void run(final RunNotifier notifier) {
    notifier.addListener(new NameListener());
    super.run(notifier);
  }

  private static String extractTestMethodName(final String name) {
    if (name != null) {
      int last = name.lastIndexOf('(');
      String methodName = last < 0 ? name : name.substring(0, last);
      return methodName;
    }
    return null;
  }

  private static String extractTestClassName(final String name) {
    if (name != null) {
      int last = name.lastIndexOf('(');
      return last < 0 ? null : name.substring(last + 1, name.length() - 1);
    }
    return null;
  }

  private static class NameListener extends RunListener {
    public void testStarted(final Description description) {
      setName(description.isTest() ? description.getDisplayName() : null); /* record start of tests, not suites */
    }

    public void testFinished(final Description description) {
      if (getTestMethodName() != null) {
        if (getTestName().equals(description.getDisplayName())) {
          setName(null);
        } else {
          throw new ConcurrentModificationException("Test name mismatch. Was " + description.getDisplayName() + " expected "
              + getTestName());
        }
      }
    }
  }
}

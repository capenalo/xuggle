package com.xuggle.test_utils;

import java.util.UUID;

/**
 * A collection of utilities that might be useful for testing.
 * 
 * @author aclarke
 *
 */
public class TestUtils
{
  /**
   * This method returns the name of the method that called it.
   * @return the name of the calling method, or a unique name if unknown.
   */
  public static String getNameOfCallingMethod()
  {
    try {
      try {
        throw new RuntimeException();
      }
      catch(RuntimeException e)
      {
        e.fillInStackTrace();
        StackTraceElement[] elements = e.getStackTrace();
        if (elements != null && elements.length > 2)
        {
          return elements[1].getMethodName();
        }
      }
    } catch (Throwable t) {
      // make sure we don't let any other errors trip us up
    }
    return "unknownMethod"+UUID.randomUUID().toString(); 
  }
}

package com.xuggle.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class TracerTest
{

  @Test
  public void testTracer()
  {
    try {
      new Tracer(null, null, null);
      fail("should not get here");
    } catch (NullPointerException ex) {}
    assertNotNull(new Tracer(this, null, null).getTimeStamp());
  }

  @Test
  public void testStamp()
  {
    Tracer tracer = new Tracer(this, null, null);
    try {
      tracer.stamp(null, null, null);
      fail("should not get here");
    } catch (NullPointerException ex) {}
    assertNotNull(tracer.stamp(this, null, null).getTimeStamp());
  }

  @Test
  public void testGetID()
  {
    Tracer tracer = new Tracer(this, null, null);
    assertNotNull(tracer.getID());
    Tracer tracer2 = tracer.stamp(this, null, null);
    assertNotSame(tracer, tracer2);
    assertEquals(tracer.getID(), tracer2.getID());
  }

  @Test
  public void testGetSource()
  {
    Tracer tracer = new Tracer(this, null, null);
    assertEquals(this, tracer.getSource());
  }

  @Test
  public void testGetTimeStamp()
  {
    Tracer tracer = new Tracer(this, null, null);
    assertNotNull(tracer.getTimeStamp());
  }

  @Test
  public void testGetMessage()
  {
    final String message = "test";
    Tracer tracer = new Tracer(this, message, null);
    assertSame(message, tracer.getMessage());
  }

  @Test
  public void testGetParent()
  {
    final int numIters = 10;
    Tracer tracer = new Tracer(this, null, null);
    for(int i = 0; i < numIters; i++)
    {
      tracer = tracer.stamp(this);
    }
    for(int i = 0; i < numIters; i++)
    {
      tracer = tracer.getParent();
      assertNotNull(tracer);
    }
    assertNull(tracer.getParent());
  }

  @Test
  public void testToString()
  {
    final int numIters = 10;
    Tracer tracer = new Tracer(this, "start", null);
    for(int i = 0; i < numIters; i++)
    {
      tracer = tracer.stamp(this, i, null);
    }
    System.out.println(tracer);
  }

}

package com.xuggle.utils.collections;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

public class MultiMapTest
{

  @Test
  public final void testMultiMap()
  {
    assertNotNull(new MultiMap<Integer, Long>());
  }

  @Test
  public final void testGetMappedA()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;
    
    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));
    final Set<Integer> set = multiMap.getMappedA(a);
    assertEquals(1, set.size());
    assertTrue(set.contains(b));
    assertFalse(set.contains(a));
    
  }

  @Test
  public final void testGetMappedB()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;
    
    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));
    final Set<Integer> set = multiMap.getMappedB(b);
    assertEquals(1, set.size());
    assertTrue(set.contains(a));
    assertFalse(set.contains(b));
    
  }

  @Test
  public final void testMap()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;
    
    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));
  }

  @Test
  public final void testUnmap()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;

    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));

    assertTrue(multiMap.unmap(a, b));
    assertTrue(multiMap.unmap(b, a));
    assertFalse(multiMap.unmap(a, b));
    assertFalse(multiMap.unmap(b, a));
  }

  @Test
  public final void testRemoveAllA()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;

    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));

    assertTrue(multiMap.removeAllA(a));
    assertFalse(multiMap.removeAllA(a));
    assertFalse(multiMap.unmap(a, b));

    assertTrue(multiMap.unmap(b, a));
    assertFalse(multiMap.unmap(a, b));
    assertFalse(multiMap.unmap(b, a));
  }

  @Test
  public final void testRemoveAllB()
  {
    IMultiMap<Integer, Integer> multiMap = new MultiMap<Integer, Integer>();
    Integer a = 6;
    Integer b = 3;

    assertTrue(multiMap.map(a, b));
    assertFalse(multiMap.map(a, b));
    assertTrue(multiMap.map(b, a));
    assertFalse(multiMap.map(b, a));

    assertTrue(multiMap.removeAllB(a));
    assertFalse(multiMap.removeAllB(a));
    assertTrue(multiMap.unmap(a, b));

    assertFalse(multiMap.unmap(b, a));
    assertFalse(multiMap.unmap(a, b));
    assertFalse(multiMap.unmap(b, a));
  }

  @Test
  public void testMultiMapping()
  {
    IMultiMap<Integer, Long> map = new MultiMap<Integer, Long>();
    int a = 0;
    int numB = 10;
    for(long b = 0; b < numB; b++)
      assertTrue(map.map(a, b));
    Set<Long> setB = map.getMappedB(a);
    assertEquals(numB, setB.size());
    
    for(long b = 0; b < numB; b++)
      assertTrue(setB.contains(b));
    
    for(long b = 0; b < numB; b++)
    {
      final Set<Integer> setA = map.getMappedA(b);
      assertEquals(1, setA.size());
      assertTrue(setA.contains(a));
    }
    setB = map.getMappedB(-1);
    assertNotNull(setB);
    assertEquals(0, setB.size());
    
    map.removeAllA(a);
    setB = map.getMappedB(a);
    assertNotNull(setB);
    assertEquals(0, setB.size());
    for(long b = 0; b < numB; b++)
    {
      final Set<Integer> setA = map.getMappedA(b);
      assertEquals(0, setA.size());
    }
  }

}

package com.xuggle.utils.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiMap<A, B> implements IMultiMap<A, B>
{
  private final Map<A, Set<B>> mAtoB;
  private final Map<B, Set<A>> mBtoA;
  public MultiMap()
  {
    mAtoB = new HashMap<A, Set<B>>();
    mBtoA = new HashMap<B, Set<A>>();
  }
  public Set<A> getMappedA(final B key)
  {
    final Set<A> retval = mBtoA.get(key);
    if (retval == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(retval);
  }
  public Set<B> getMappedB(A key)
  {
    final Set<B> retval = mAtoB.get(key);
    if (retval == null)
      return Collections.emptySet();
    return Collections.unmodifiableSet(retval);
  }
  public boolean map(A a, B b)
  {
    if (a == null || b == null)
      throw new NullPointerException();
    Set<B> aSet = mAtoB.get(a);
    Set<A> bSet = mBtoA.get(b);
    if (aSet == null) {
      aSet = new HashSet<B>();
      mAtoB.put(a, aSet);
    }
    final boolean retval = aSet.add(b);
    if (retval) {
      // we added a mapping, so add one to the b set.
      if (bSet == null) {
        bSet = new HashSet<A>();
        mBtoA.put(b, bSet);
      }
      final boolean check = bSet.add(a);
      assert check : "did not add to bSet but added to aSet";
    }
    return retval;
  }
  public boolean unmap(A a, B b)
  {
    if (a == null || b == null)
      throw new NullPointerException();
    Set<B> aSet = mAtoB.get(a);
    Set<A> bSet = mBtoA.get(b);
    final boolean retval;
    if (aSet != null) {
      if (aSet.remove(b)) {
        assert bSet != null : "had a but no b";
        final boolean check = bSet.remove(a);
        assert check : "removed from a but not b";
        if (bSet.size() == 0)
          mBtoA.remove(b);
        if (aSet.size() == 0)
          mAtoB.remove(a);
        retval = true;
      } else
        retval = true;
    } else
      retval = false;
    return retval;
  }
  public boolean removeAllA(A key)
  {
    final Set<B> set = mAtoB.remove(key);
    if (set == null)
      return false;
    assert set.size() > 0 : "unexpected empty set";
    for(B value : set)
    {
      Set<A> mirror = mBtoA.get(value);
      assert mirror != null : "not bi-directional";
      if (mirror != null) {
        final boolean check = mirror.remove(key);
        assert check : "not bi-directional";
        if (mirror.size() == 0)
          mBtoA.remove(value);
      }
    }
    return true;
  }
  public boolean removeAllB(B key)
  {
    final Set<A> set = mBtoA.remove(key);
    if (set == null)
      return false;
    assert set.size() > 0 : "unexpected empty set";
    for(A value : set)
    {
      Set<B> mirror = mAtoB.get(value);
      assert mirror != null : "not bi-directional";
      if (mirror != null) {
        final boolean check = mirror.remove(key);
        assert check : "not bi-directional";
        if (mirror.size() == 0)
          mAtoB.remove(value);
      }
    }
    return true;
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("mAtoB(").append(mAtoB.size()).append("):\n");
    for(A key: mAtoB.keySet())
    {
      builder.append(key).append("=[");
      Set<B> values = mAtoB.get(key);
      for(B value : values) {
        builder.append(value).append(", ");
      }
      builder.append("]\n");
    }
    builder.append("mBtoA(").append(mBtoA.size()).append("):\n");
    for(B key: mBtoA.keySet())
    {
      builder.append(key).append("=[");
      Set<A> values = mBtoA.get(key);
      for(A value : values) {
        builder.append(value).append(", ");
      }
      builder.append("]\n");
    }
    
    return builder.toString();
  }
  
}

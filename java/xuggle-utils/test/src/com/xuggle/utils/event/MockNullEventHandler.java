package com.xuggle.utils.event;

public class MockNullEventHandler<E extends IEvent> implements IEventHandler<E>
{
  public boolean handleEvent(IEventDispatcher dispatcher, E event)
  {
    // do nothing.
    return false;
  }

}

/*
 * This file is part of Xuggler.
 * 
 * Xuggler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Xuggler.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.utils.event;

/**
 * An abstract implementation of a {@link ISelfHandlingEvent}.
 * 
 * @author aclarke
 *
 */
public abstract class SelfHandlingEvent<E extends IEvent>
extends Event
implements ISelfHandlingEvent<E>
{

  public SelfHandlingEvent(Object aSource)
  {
    super(aSource);
  }

  public abstract boolean handleEvent(IEventDispatcher aDispatcher,
      E aEvent);

}

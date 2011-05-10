/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.mop.navigation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A queuing implementation of the {@link NodeChangeListener} interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NodeChangeQueue<N> implements NodeChangeListener<N>, Iterable<NodeChange<N>>
{

   /** . */
   private Collection<NodeChange<N>> changes;

   public NodeChangeQueue(Collection<NodeChange<N>> changes)
   {
      this.changes = changes;
   }

   public NodeChangeQueue()
   {
      this(null);
   }

   public Iterator<NodeChange<N>> iterator()
   {
      return changes == null ? Collections.<NodeChange<N>>emptyList().iterator() : changes.iterator();
   }

   protected Collection<NodeChange<N>> create()
   {
      return new LinkedList<NodeChange<N>>();
   }

   public void clear()
   {
      if (changes != null)
      {
         changes.clear();
      }
   }

   protected void onChange(NodeChange<N> change)
   {
      if (changes == null)
      {
         changes = create();
      }
      changes.add(change);
   }

   public void onAdd(NodeChange.Added<N> added)
   {
      onChange(added);
   }

   public void onRemove(NodeChange.Removed<N> removed)
   {
      onChange(removed);
   }

   public void onRename(NodeChange.Renamed<N> renamed)
   {
      onChange(renamed);
   }

   public void onUpdate(NodeChange.Updated<N> updated)
   {
      onChange(updated);
   }

   public void onMove(NodeChange.Moved<N> moved)
   {
      onChange(moved);
   }
}

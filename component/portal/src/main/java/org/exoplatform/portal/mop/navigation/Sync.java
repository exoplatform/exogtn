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

import org.exoplatform.portal.tree.sync.ListAdapter;
import org.exoplatform.portal.tree.sync.SyncModel;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class Sync
{

   /** . */
   private static final SyncModel nodeContextModel = new SyncModel<NodeContext<?>, NodeContext<?>, Object>()
   {
      public NodeContext getHandle(NodeContext node)
      {
         return node;
      }
      public NodeContext getChildren(NodeContext node)
      {
         return node;
      }
      public NodeContext getDescendant(NodeContext node, Object handle)
      {
         NodeContext handle1 = (NodeContext)handle;
         return handle1;
      }
   };

   static <N> SyncModel<NodeContext<N>, NodeContext<N>, Object> getNodeContextModel() {
      @SuppressWarnings("unchecked")
      SyncModel<NodeContext<N>, NodeContext<N>, Object> tmp = (SyncModel<NodeContext<N>, NodeContext<N>, Object>) nodeContextModel;
      return tmp;
   }

   /** . */
   private static final ListAdapter nodeContextAdapter = new ListAdapter<NodeContext, Object>()
   {
      public int size(NodeContext list)
      {
         return list.getSize();
      }
      public Iterator<Object> iterator(final NodeContext list, final boolean reverse)
      {
         return new Iterator<Object>()
         {
            NodeContext current = reverse ? (NodeContext)list.getLast() : (NodeContext)list.getFirst();
            public boolean hasNext()
            {
               return current != null;
            }
            public NodeContext next()
            {
               if (current == null)
               {
                  throw new NoSuchElementException();
               }
               NodeContext tmp = current;
               current = reverse ? (NodeContext)current.getPrevious() : (NodeContext)current.getNext();
               return tmp;
            }
            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }
   };

   static <N> ListAdapter<NodeContext<N>, Object> getNodeContextAdapter()
   {
      @SuppressWarnings("unchecked")
      ListAdapter<NodeContext<N>, Object> tmp = (ListAdapter<NodeContext<N>, Object>)nodeContextAdapter;
      return tmp;
   }

}

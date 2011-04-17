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

import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeContext<N>
{

   /** . */
   private Map<String, NodeContext<N>> nodes = new HashMap<String, NodeContext<N>>();

   /** . */
   private LinkedList<Change> changes;

   /** . */
   final NodeModel<N> model;

   TreeContext(NodeModel<N> model)
   {
      this.model = model;
   }

   public NodeContext<N> newContext(NodeData data)
   {
      NodeContext<N> context = new NodeContext<N>(this, data);
      nodes.put(context.getId(), context);
      return context;
   }

   public String getId(NodeData node)
   {
      return node.id;
   }

   void addChange(Change change)
   {
      if (changes == null)
      {
         changes = new LinkedList<Change>();
      }
      changes.addLast(change);
   }

   boolean hasChange()
   {
      return changes != null && changes.size() > 0;
   }

   public Change nextChange()
   {
      if (changes == null || changes.size() == 0)
      {
         throw new NoSuchElementException();
      }
      return changes.removeFirst();
   }
}

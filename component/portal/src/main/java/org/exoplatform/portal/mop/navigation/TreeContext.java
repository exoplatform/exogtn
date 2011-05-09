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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The context of a tree.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeContext<N>
{

   /** . */
   private LinkedList<NodeChange<NodeContext<N>>> changes;

   /** . */
   final NodeModel<N> model;

   /** . */
   boolean editMode;

   TreeContext(NodeModel<N> model)
   {
      this.model = model;
      this.editMode = false;
   }

   void addChange(NodeChange<NodeContext<N>> change)
   {
      if (editMode)
      {
         throw new AssertionError();
      }
      if (changes == null)
      {
         changes = new LinkedList<NodeChange<NodeContext<N>>>();
      }
      changes.addLast(change);
   }

   boolean hasChanges() {
      return changes != null && changes.size() > 0;
   }

   List<NodeChange<NodeContext<N>>> popChanges()
   {
      if (hasChanges())
      {
         LinkedList<NodeChange<NodeContext<N>>> tmp = changes;
         changes = null;
         return tmp;
      }
      else
      {
         return Collections.emptyList();
      }
   }
}

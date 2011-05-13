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

/**
 * <p>The context of a tree, that performs:
 * <ul>
 *    <li>holding the list of pending changes</li>
 *    <li>keep a reference to the {@link NodeModel}</li>
 *    <li>hold a sequence for providing id for transient contexts</li>
 *    <li>hold the root context</li>
 * </ul>
 * </p>
 *
 * <p>The class implements the {@link Scope.Visitor} and defines a scope describing the actual content
 * of the context tree.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeContext<N> implements Scope.Visitor
{

   /** . */
   private LinkedList<NodeChange<N>> changes;

   /** . */
   final NodeModel<N> model;

   /** . */
   boolean editMode;

   /** . */
   int sequence;

   /** . */
   final NodeContext<N> root;

   TreeContext(NodeModel<N> model, NodeContext<N> root)
   {
      this.model = model;
      this.editMode = false;
      this.sequence =  0;
      this.root = root;
   }

   void addChange(NodeChange<N> change)
   {
      if (editMode)
      {
         throw new AssertionError();
      }
      if (changes == null)
      {
         changes = new LinkedList<NodeChange<N>>();
      }
      changes.addLast(change);
   }

   boolean hasChanges() {
      return changes != null && changes.size() > 0;
   }

   List<NodeChange<N>> peekChanges()
   {
      if (hasChanges())
      {
         return changes;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   List<NodeChange<N>> popChanges()
   {
      if (hasChanges())
      {
         LinkedList<NodeChange<N>> tmp = changes;
         changes = null;
         return tmp;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   public VisitMode visit(int depth, String id, String name, NodeState state)
   {
      NodeContext<N> descendant = root.getDescendant(id);
      if (descendant != null)
      {
         return descendant.isExpanded() ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
      }
      else
      {
         return VisitMode.NO_CHILDREN;
      }
   }
}

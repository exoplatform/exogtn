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
   private LinkedList<NodeChange<NodeContext<N>>> changes;

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

      //
      if (change.source.tree != this)
      {
         // Normally should be done for all arguments depending on the change type
         throw new AssertionError("Ensure we are not mixing badly things");
      }

      // Perform state modification here
      if (change instanceof NodeChange.Renamed<?>)
      {
         NodeChange.Renamed<NodeContext<N>> renamed = (NodeChange.Renamed<NodeContext<N>>)change;
         renamed.source.name = renamed.name;
      }
      else if (change instanceof NodeChange.Created<?>)
      {
         NodeChange.Created<NodeContext<N>> added = (NodeChange.Created<NodeContext<N>>)change;
         if (added.previous != null)
         {
            added.previous.insertAfter(added.source);
         }
         else
         {
            added.parent.insertAt(0, added.source);
         }
      }
      else if (change instanceof NodeChange.Moved<?>)
      {
         NodeChange.Moved<NodeContext<N>> moved = (NodeChange.Moved<NodeContext<N>>)change;
         if (moved.previous != null)
         {
            moved.previous.insertAfter(moved.source);
         }
         else
         {
            moved.to.insertAt(0, moved.source);
         }
      }
      else if (change instanceof NodeChange.Destroyed<?>)
      {
         NodeChange.Destroyed<NodeContext<N>> removed = (NodeChange.Destroyed<NodeContext<N>>)change;
         removed.source.remove();
      }
      else if (change instanceof NodeChange.Updated<?>)
      {
         NodeChange.Updated<NodeContext<N>> updated = (NodeChange.Updated<NodeContext<N>>)change;
         updated.source.state = updated.state;
      }

      //
      changes.addLast(change);
   }

   boolean hasChanges() {
      return changes != null && changes.size() > 0;
   }

   List<NodeChange<NodeContext<N>>> peekChanges()
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

   public VisitMode enter(int depth, String id, String name, NodeState state)
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

   public void leave(int depth, String id, String name, NodeState state)
   {
   }
}

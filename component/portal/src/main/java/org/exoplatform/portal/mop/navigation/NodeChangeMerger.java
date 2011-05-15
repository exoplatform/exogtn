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

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
class NodeChangeMerger<S, C, D> extends NodeChangeListener.Base<NodeContext<S>>
{

   /** . */
   private final C context;

   /** . */
   private final HierarchyManager<C, D> manager;

   /** . */
   private final NodeChangeListener<D> next;

   NodeChangeMerger(C context, HierarchyManager<C, D> manager, NodeChangeListener<D> next)
   {
      this.context = context;
      this.manager = manager;
      this.next = next;
   }

   public void onCreate(NodeContext<S> _source, NodeContext<S> _parent, NodeContext<S> _previous, String name) throws NavigationServiceException
   {
      String parentHandle = _parent.data.id;
      D parent = manager.getNode(context, parentHandle);
      if (parent == null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
      }

      //
      D previous;
      if (_previous != null)
      {
         previous = manager.getNode(context, _previous.data.id);
         if (previous == null)
         {
            throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
         }
      }
      else
      {
         previous = null;
      }

      //
      D added = manager.getChild(context, parent, name);
      if (added != null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
      }

      //
      next.onCreate(null, parent, previous, name);
   }

   public void onDestroy(NodeContext<S> _source, NodeContext<S> _parent)
   {
      D removed = manager.getNode(context, _source.data.id);
      D parent = manager.getNode(context, _parent.data.id);

      //
      next.onDestroy(removed, parent);
   }

   public void onRename(NodeContext<S> _source, NodeContext<S> _parent, String _name) throws NavigationServiceException
   {
      //
      String renamedHandle = _source.data.id;
      D renamed = manager.getNode(context, renamedHandle);
      if (renamed == null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
      }

      //
      D parent = manager.getParent(context, renamed);
      if (manager.getChild(context, parent, _name) != null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
      }

      //
      next.onRename(renamed, parent, _name);
   }

   public void onUpdate(NodeContext<S> _source, NodeState state) throws NavigationServiceException
   {
      String updatedHandle = _source.data.id;
      D navigation = manager.getNode(context, updatedHandle);
      if (navigation == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      next.onUpdate(navigation, state);
   }

   public void onMove(NodeContext<S> _source, NodeContext<S> _from, NodeContext<S> _to, NodeContext<S> _previous) throws NavigationServiceException
   {
      String srcHandle = _from.data.id;
      D src = manager.getNode(context, srcHandle);
      if (src == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
      }

      //
      String dstHandle = _to.data.id;
      D dst = manager.getNode(context, dstHandle);
      if (dst == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
      }

      //
      String movedHandle = _source.data.id;
      D moved = manager.getNode(context, movedHandle);
      if (moved == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
      }

      //
      D previous;
      if (_previous != null)
      {
         previous = manager.getNode(context, _previous.data.id);
         if (previous == null)
         {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
         }
      }
      else
      {
         previous = null;
      }

      //
      if (src != manager.getParent(context, moved))
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
      }

      //
      if (src != dst)
      {
         String name = manager.getName(context, moved);
         D existing = manager.getChild(context, dst, name);
         if (existing != null)
         {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_DUPLICATE_NAME);
         }
      }

      //
      next.onMove(moved, src, dst, previous);
   }
}

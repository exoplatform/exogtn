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
class Merge<N1, N2> extends NodeChangeListener.Base<NodeContext<N1>>
{

   /** . */
   private final MergeAdapter<N2> adapter;

   /** . */
   private final NodeChangeListener<N2> next;

   Merge(MergeAdapter<N2> adapter, NodeChangeListener<N2> next)
   {
      this.adapter = adapter;
      this.next = next;
   }

   public void onCreate(NodeContext<N1> _source, NodeContext<N1> _parent, NodeContext<N1> _previous, String name) throws NavigationServiceException
   {
      String parentHandle = _parent.handle;
      N2 parent = adapter.getNode(parentHandle);
      if (parent == null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
      }

      //
      N2 previous;
      if (_previous != null)
      {
         previous = adapter.getNode(_previous.handle);
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
      N2 added = adapter.getChild(parent, name);
      if (added != null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
      }

      //
      next.onCreate(null, parent, previous, name);
   }

   public void onDestroy(NodeContext<N1> _source, NodeContext<N1> _parent)
   {
      N2 removed = adapter.getNode(_source.handle);

      //
      if (removed != null)
      {
         N2 parent = adapter.getNode(_parent.handle);
         next.onDestroy(removed, parent);
      }
   }

   public void onRename(NodeContext<N1> _source, NodeContext<N1> _parent, String _name) throws NavigationServiceException
   {
      //
      String renamedHandle = _source.handle;
      N2 renamed = adapter.getNode(renamedHandle);
      if (renamed == null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
      }

      //
      N2 parent = adapter.getParent(renamed);
      if (adapter.getChild(parent, _name) != null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
      }

      //
      next.onRename(renamed, parent, _name);
   }

   public void onUpdate(NodeContext<N1> _source, NodeState state) throws NavigationServiceException
   {
      String updatedHandle = _source.handle;
      N2 navigation = adapter.getNode(updatedHandle);
      if (navigation == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      next.onUpdate(navigation, state);
   }

   public void onMove(NodeContext<N1> _source, NodeContext<N1> _from, NodeContext<N1> _to, NodeContext<N1> _previous) throws NavigationServiceException
   {
      String srcHandle = _from.handle;
      N2 src = adapter.getNode(srcHandle);
      if (src == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
      }

      //
      String dstHandle = _to.handle;
      N2 dst = adapter.getNode(dstHandle);
      if (dst == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
      }

      //
      String movedHandle = _source.handle;
      N2 moved = adapter.getNode(movedHandle);
      if (moved == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
      }

      //
      N2 previous;
      if (_previous != null)
      {
         previous = adapter.getNode(_previous.handle);
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
      if (src != adapter.getParent(moved))
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
      }

      //
      if (src != dst)
      {
         String name = adapter.getName(moved);
         N2 existing = adapter.getChild(dst, name);
         if (existing != null)
         {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_DUPLICATE_NAME);
         }
      }

      //
      next.onMove(moved, src, dst, previous);
   }
}

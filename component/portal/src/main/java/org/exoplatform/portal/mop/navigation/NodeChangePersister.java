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
class NodeChangePersister<C, D> extends NodeChangeListener.Base<D>
{

   /** . */
   private final C context;

   /** . */
   private final HierarchyManager<C, D> manager;

   /** . */
   private final NodeChangeListener<String> next;

   NodeChangePersister(C context, HierarchyManager<C, D> manager, NodeChangeListener<String> next)
   {
      this.context = context;
      this.manager = manager;
      this.next = next;
   }

   @Override
   public void onCreate(D source, D parent, D previous, String name) throws NavigationServiceException
   {
      //
      int index;
      String previousHandle;
      if (previous != null)
      {
         index = manager.getChildIndex(context, parent, previous) + 1;
         previousHandle = manager.getHandle(context, previous);
      }
      else
      {
         index = 0;
         previousHandle = null;
      }

      //
      D added = manager.addChild(context, parent, index, name);
      String addedHandle = manager.getHandle(context, added);
      String parentHandle = manager.getHandle(context, parent);

      //
      next.onCreate(addedHandle, parentHandle, previousHandle, name);
   }

   @Override
   public void onDestroy(D source, D parent)
   {
      if (source != null)
      {
         String parentHandle = manager.getHandle(context, parent);
         String removedId = manager.getHandle(context, source);
         manager.destroy(context, source);

         // that is not possible anymore it should be in the processor ????
         //_source.data = null;

         //
         next.onDestroy(removedId, parentHandle);
      }
      else
      {
         // It was already removed concurrently
      }
   }

   @Override
   public void onRename(D source, D parent, String name) throws NavigationServiceException
   {
      // We rename and reorder to compensate the move from the rename
      manager.setName(context, source, name);
      String renamedHandle = manager.getHandle(context, source);
      String parentHandle = manager.getHandle(context, parent);
      next.onRename(renamedHandle, parentHandle, name);
   }

   @Override
   public void onUpdate(D source, NodeState state) throws NavigationServiceException
   {
      manager.setState(context, source, state);

      //
      String updatedHandle = manager.getHandle(context, source);
      next.onUpdate(updatedHandle, state);
   }

   @Override
   public void onMove(D source, D from, D to, D previous) throws NavigationServiceException
   {
      int index;
      String previousHandle;
      if (previous != null)
      {
         index = manager.getChildIndex(context, to, previous) + 1;
         previousHandle = manager.getHandle(context, previous);
      }
      else
      {
         index = 0;
         previousHandle = null;
      }
      manager.addChild(context, to, index, source);

      //
      String movedHandle = manager.getHandle(context, source);
      String srcHandle = manager.getHandle(context, from);
      String dstHandle = manager.getHandle(context, to);
      next.onMove(movedHandle, srcHandle, dstHandle, previousHandle);
   }
}

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

import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
interface HierarchyManager<C, N>
{

   N getParent(C context, N node);

   N getNode(C context, String handle);

   N getChild(C context, N node, String name);

   String getName(C context, N node);

   HierarchyManager<TreeContext<Object>, NodeContext<Object>> CONTEXT = new HierarchyManager<TreeContext<Object>, NodeContext<Object>>()
   {
      public NodeContext<Object> getParent(TreeContext<Object> context, NodeContext<Object> node)
      {
         return node.getParent();
      }

      public NodeContext<Object> getNode(TreeContext<Object> context, String handle)
      {
         return context.root.getDescendant(handle);
      }

      public NodeContext<Object> getChild(TreeContext<Object> context, NodeContext<Object> node, String name)
      {
         return node.get(name);
      }

      public String getName(TreeContext<Object> context, NodeContext<Object> node)
      {
         return node.getName();
      }
   };

   HierarchyManager<POMSession, Navigation> MOP = new HierarchyManager<POMSession, Navigation>()
   {
      public Navigation getParent(POMSession context, Navigation node)
      {
         return node.getParent();
      }

      public Navigation getNode(POMSession context, String handle)
      {
         return context.findObjectById(ObjectType.NAVIGATION, handle);
      }

      public Navigation getChild(POMSession context, Navigation node, String name)
      {
         return node.getChild(name);
      }

      public String getName(POMSession context, Navigation node)
      {
         return node.getName();
      }
   };
}

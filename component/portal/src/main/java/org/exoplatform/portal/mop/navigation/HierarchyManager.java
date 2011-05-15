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

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.PageLink;

import java.util.List;

import static org.exoplatform.portal.pom.config.Utils.split;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
interface HierarchyManager<C, N>
{

   N getParent(C context, N node);

   N getNode(C context, String handle);

   N getChild(C context, N node, String name);

   N addChild(C context, N node, int index, String name);

   void addChild(C context, N node, int index, N child);

   int getChildIndex(C context, N node, N child);

   NodeData getData(C context, N node);

   String getHandle(C context, N node);

   void destroy(C context, N node);

   void setName(C context, N node, String name);

   String getName(C context, N node);

   void setState(C context, N node, NodeState state);

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

      public NodeContext<Object> addChild(TreeContext<Object> context, NodeContext<Object> node, int index, String name)
      {
         return node.add(index, name);
      }

      public void addChild(TreeContext<Object> context, NodeContext<Object> node, int index, NodeContext<Object> child)
      {
         node.add(index, child);
      }

      public int getChildIndex(TreeContext<Object> context, NodeContext<Object> node, NodeContext<Object> child)
      {
         int index = 0;
         for (NodeContext<Object> current = node.getFirst();current != null;current = current.getNext())
         {
            if (current == child)
            {
               return index;
            }
            else
            {
               index++;
            }
         }
         return -1;
      }

      public NodeData getData(TreeContext<Object> context, NodeContext<Object> node)
      {
         return node.data;
      }

      public String getHandle(TreeContext<Object> context, NodeContext<Object> node)
      {
         return node.handle;
      }

      public void destroy(TreeContext<Object> context, NodeContext<Object> node)
      {
         node.remove();
      }

      public void setName(TreeContext<Object> context, NodeContext<Object> node, String name)
      {
         node.setName(name);
      }

      public String getName(TreeContext<Object> context, NodeContext<Object> node)
      {
         return node.getName();
      }

      public void setState(TreeContext<Object> context, NodeContext<Object> node, NodeState state)
      {
         node.setState(state);
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

      public Navigation addChild(POMSession context, Navigation node, int index, String name)
      {
         Navigation child = node.addChild(name);
         node.getChildren().add(index, child);
         return child;
      }

      public void addChild(POMSession context, Navigation node, int index, Navigation child)
      {
         node.getChildren().add(index, child);
      }

      public int getChildIndex(POMSession context, Navigation node, Navigation child)
      {
         return node.getChildren().indexOf(child);
      }

      public NodeData getData(POMSession context, Navigation node)
      {
         return new NodeData(node);
      }

      public String getHandle(POMSession context, Navigation node)
      {
         return node.getObjectId();
      }

      public void destroy(POMSession context, Navigation node)
      {
         node.destroy();
      }

      public void setName(POMSession context, Navigation node, String name)
      {
         List<Navigation> children = node.getParent().getChildren();
         int index = children.indexOf(node);
         node.setName(name);
         children.add(index, node);
      }

      public String getName(POMSession context, Navigation node)
      {
         return node.getName();
      }

      public void setState(POMSession context, Navigation node, NodeState state)
      {
         Workspace workspace = node.getSite().getWorkspace();
         String reference = state.getPageRef();
         if (reference != null)
         {
            String[] pageChunks = split("::", reference);
            ObjectType<? extends Site> siteType = Mapper.parseSiteType(pageChunks[0]);
            Site site = workspace.getSite(siteType, pageChunks[1]);
            org.gatein.mop.api.workspace.Page target = site.getRootPage().getChild("pages").getChild(pageChunks[2]);
            PageLink link = node.linkTo(ObjectType.PAGE_LINK);
            link.setPage(target);
         }
         else
         {
            PageLink link = node.linkTo(ObjectType.PAGE_LINK);
            link.setPage(null);
         }

         //
         Described described = node.adapt(Described.class);
         described.setName(state.getLabel());

         //
         Visible visible = node.adapt(Visible.class);
         visible.setVisibility(state.getVisibility());

         //
         visible.setStartPublicationDate(state.getStartPublicationDate());
         visible.setEndPublicationDate(state.getEndPublicationDate());

         //
         Attributes attrs = node.getAttributes();
         attrs.setValue(MappedAttributes.URI, state.getURI());
         attrs.setValue(MappedAttributes.ICON, state.getIcon());
      }
   };
}

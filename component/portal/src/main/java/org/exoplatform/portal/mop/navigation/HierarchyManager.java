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
 * Abstract how a node hierarchy can be managed.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @param <C> the context generic type
 * @param <N> the node generic type
 */
interface HierarchyManager<C, N>
{

   abstract N getParent(C context, N node);

   abstract N getNode(C context, String id);

   abstract N getChild(C context, N node, String name);

   abstract N addChild(C context, N node, int index, String name);

   abstract void addChild(C context, N node, int index, N child);

   abstract int getChildIndex(C context, N node, N child);

   abstract NodeData getData(C context, N node);

   abstract String getId(C context, N node);

   abstract void destroy(C context, N node);

   abstract void setName(C context, N node, String name);

   abstract void setState(C context, N node, NodeState state);
   
   HierarchyManager<POMSession, Navigation> MOP = new HierarchyManager<POMSession, Navigation>()
   {
      public Navigation getParent(POMSession context, Navigation node)
      {
         return node.getParent();  
      }

      public Navigation getNode(POMSession context, String id)
      {
         return context.findObjectById(ObjectType.NAVIGATION, id);  
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

      public String getId(POMSession context, Navigation node)
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

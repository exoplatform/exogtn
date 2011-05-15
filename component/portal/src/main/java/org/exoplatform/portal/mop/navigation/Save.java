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
class Save
{

   static interface Adapter<C, N>
   {

      abstract N getParent(C context, N node);

      abstract N getNode(C context, String handle);

      abstract N getChild(C context, N node, String name);

      abstract N addChild(C context, N node, int index, String name);

      abstract void addChild(C context, N node, int index, N child);

      abstract int getChildIndex(C context, N node, N child);

      abstract NodeData getData(C context, N node);

      abstract String getHandle(C context, N node);

      abstract void destroy(C context, N node);

      abstract void setName(C context, N node, String name);

      abstract String getName(C context, N node);

      abstract void setState(C context, N node, NodeState state);

      Adapter<TreeContext<Object>, NodeContext<Object>> CONTEXT = new Adapter<TreeContext<Object>, NodeContext<Object>>()
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

      Adapter<POMSession, Navigation> MOP = new Adapter<POMSession, Navigation>()
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

   static class Filter<S, C, D> implements NodeChangeFilter<S, String>
   {

      private final C context;

      private final Adapter<C, D> manager;

      Filter(C context, Adapter<C, D> manager)
      {
         this.context = context;
         this.manager = manager;
      }

      public NodeChange<String> filter(NodeChange<S> change) throws NavigationServiceException
      {
         if (change instanceof NodeChange.Created<?>)
         {
            NodeChange.Created<NodeContext<S>> add = (NodeChange.Created<NodeContext<S>>)change;

            //
            String parentHandle = add.parent.data.id;
            D parent = manager.getNode(context, parentHandle);
            if (parent == null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
            }

            //
            D previous;
            String previousHandle;
            if (add.previous != null)
            {
               previousHandle = add.previous.data.id;
               previous = manager.getNode(context, previousHandle);
               if (previous == null)
               {
                  throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
               }
            }
            else
            {
               previous = null;
               previousHandle = null;
            }

            //
            D added = manager.getChild(context, parent, add.name);
            if (added != null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
            }

            //
            int index;
            if (previous != null)
            {
               index = manager.getChildIndex(context, parent, previous) + 1;
            }
            else
            {
               index = 0;
            }
            added = manager.addChild(context, parent, index, add.name);
            String addedHandle = manager.getHandle(context, added);
            return new NodeChange.Created<String>(
               parentHandle,
               previousHandle,
               addedHandle,
               add.name
            );
         }
         else if (change instanceof NodeChange.Destroyed<?>)
         {
            NodeChange.Destroyed<NodeContext<S>> remove = (NodeChange.Destroyed<NodeContext<S>>)change;
            D removed = manager.getNode(context, remove.source.data.id);

            //
            if (removed != null)
            {
               D parent = manager.getParent(context, removed);
               String parentHandle = manager.getHandle(context, parent);
               String removedId = manager.getHandle(context, removed);
               manager.destroy(context, removed);
               remove.source.data = null;

               //
               return new NodeChange.Destroyed<String>(
                  parentHandle,
                  removedId
               );
            }
            else
            {
               // It was already removed concurrently
               return null;
            }
         }
         else if (change instanceof NodeChange.Moved<?>)
         {
            NodeChange.Moved<NodeContext<S>> move = (NodeChange.Moved<NodeContext<S>>)change;
            String srcHandle = move.from.data.id;
            D src = manager.getNode(context, srcHandle);
            if (src == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
            }

            //
            String dstHandle = move.to.data.id;
            D dst = manager.getNode(context, dstHandle);
            if (dst == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
            }

            //
            String movedHandle = move.source.data.id;
            D moved = manager.getNode(context, movedHandle);
            if (moved == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
            }

            //
            D previous;
            String previousHandle;
            if (move.previous != null)
            {
               previousHandle = move.previous.data.id;
               previous = manager.getNode(context, previousHandle);
               if (previous == null)
               {
                  throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
               }
            }
            else
            {
               previous = null;
               previousHandle = null;
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
            int index;
            if (previous != null)
            {
               index = manager.getChildIndex(context, dst, previous) + 1;
            }
            else
            {
               index = 0;
            }
            manager.addChild(context, dst, index, moved);

            //
            return new NodeChange.Moved<String>(
               srcHandle,
               dstHandle,
               previousHandle,
               movedHandle);
         }
         else if (change instanceof NodeChange.Renamed<?>)
         {
            NodeChange.Renamed<NodeContext<S>> rename = (NodeChange.Renamed<NodeContext<S>>)change;

            //
            String renamedHandle = rename.source.data.id;
            D renamed = manager.getNode(context, renamedHandle);
            if (renamed == null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
            }

            //
            D parent = manager.getParent(context, renamed);
            String parentHandle = manager.getHandle(context, parent);
            if (manager.getChild(context, parent, rename.name) != null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
            }

            // We rename and reorder to compensate the move from the rename
            manager.setName(context, renamed, rename.name);

            //
            return new NodeChange.Renamed<String>(
               parentHandle,
               renamedHandle,
               rename.name
            );
         }
         else if (change instanceof NodeChange.Updated<?>)
         {
            NodeChange.Updated<NodeContext<S>> updated = (NodeChange.Updated<NodeContext<S>>)change;

            //
            String updatedHandle = updated.source.data.id;
            D navigation = manager.getNode(context, updatedHandle);
            if (navigation == null)
            {
               throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
            }

            //
            manager.setState(context, navigation, updated.state);

            //
            return new NodeChange.Updated<String>(
               updatedHandle,
               updated.state
            );
         }
         else
         {
            throw new AssertionError("Cannot execute " + change);
         }
      }
   }
}

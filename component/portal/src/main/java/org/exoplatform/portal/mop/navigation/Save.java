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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

   /**
    * Performs a state saving iteration over the provided changes.
    *
    * @param changes the changes to iterate
    * @param context the manager context
    * @param manager the manager
    * @param <S> the source generic type
    * @param <C> the context generic type
    * @param <D> the destination generic type
    * @return the list of modified ids
    * @throws org.exoplatform.portal.mop.navigation.NavigationServiceException any navigation exception
    */
   static <S, C, D> Collection<String> save(
      Iterable<NodeChange<S>> changes,
      C context,
      Adapter<C, D> manager) throws NavigationServiceException
   {

      // The ids to remove from the cache
      Set<String> ids = new HashSet<String>();

      // First pass we update persistent store
      for (NodeChange<S> change : changes)
      {
         if (change instanceof NodeChange.Created<?>)
         {
            NodeChange.Created<S> add = (NodeChange.Created<S>)change;

            //
            D parent = manager.getNode(context, add.parent.data.id);
            if (parent == null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
            }

            //
            D previous;
            if (add.previous != null)
            {
               previous = manager.getNode(context, add.previous.data.id);
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
            add.source.data = manager.getData(context, added);
            add.source.handle = manager.getHandle(context, added);
            ids.add(manager.getHandle(context, parent));
         }
         else if (change instanceof NodeChange.Destroyed<?>)
         {
            NodeChange.Destroyed<S> remove = (NodeChange.Destroyed<S>)change;
            D removed = manager.getNode(context, remove.source.data.id);

            //
            if (removed != null)
            {
               D parent = manager.getParent(context, removed);
               String removedId = manager.getHandle(context, removed);
               manager.destroy(context, removed);
               remove.source.data = null;

               //
               ids.add(removedId);
               ids.add(manager.getHandle(context, parent));
            }
            else
            {
               // It was already removed concurrently
            }
         }
         else if (change instanceof NodeChange.Moved<?>)
         {
            NodeChange.Moved<S> move = (NodeChange.Moved<S>)change;
            D src = manager.getNode(context, move.from.data.id);
            if (src == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
            }

            //
            D dst = manager.getNode(context, move.to.data.id);
            if (dst == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
            }

            //
            D moved = manager.getNode(context, move.source.data.id);
            if (moved == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
            }

            //
            D previous;
            if (move.previous != null)
            {
               previous = manager.getNode(context, move.previous.data.id);
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
            ids.add(manager.getHandle(context, src));
            ids.add(manager.getHandle(context, dst));
         }
         else if (change instanceof NodeChange.Renamed<?>)
         {
            NodeChange.Renamed<S> rename = (NodeChange.Renamed<S>)change;

            //
            D renamed = manager.getNode(context, rename.source.data.id);
            if (renamed == null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
            }

            //
            D parent = manager.getParent(context, renamed);
            if (manager.getChild(context, parent, rename.name) != null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
            }

            // We rename and reorder to compensate the move from the rename
            manager.setName(context, renamed, rename.name);

            //
            ids.add(manager.getHandle(context, parent));
            ids.add(manager.getHandle(context, renamed));
         }
         else if (change instanceof NodeChange.Updated<?>)
         {
            NodeChange.Updated<S> updated = (NodeChange.Updated<S>)change;

            //
            D navigation = manager.getNode(context, updated.source.data.id);
            if (navigation == null)
            {
               throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
            }

            //
            manager.setState(context, navigation, updated.state);

            //
            ids.add(manager.getHandle(context, navigation));
         }
         else
         {
            throw new AssertionError("Cannot execute " + change);
         }
      }

      //
      return ids;
   }
}

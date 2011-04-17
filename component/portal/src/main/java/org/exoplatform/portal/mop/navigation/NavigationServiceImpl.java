/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.chromattic.api.Chromattic;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import static org.exoplatform.portal.mop.navigation.Utils.*;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.PageLink;

import javax.jcr.Session;
import java.util.*;

import static org.exoplatform.portal.pom.config.Utils.split;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   final POMSessionManager manager;

   /** . */
   private final Cache cache;

   /** . */
   final Logger log = LoggerFactory.getLogger(NavigationServiceImpl.class);

   public NavigationServiceImpl(POMSessionManager manager)
   {
      if (manager == null)
      {
         throw new NullPointerException("No null pom session manager allowed");
      }
      this.manager = manager;
      this.cache = new CacheById();
   }

   public void start() throws Exception
   {
      Chromattic chromattic = manager.getLifeCycle().getChromattic();
      Session session = chromattic.openSession().getJCRSession();
      cache.start(session);
   }


   public void stop()
   {
      cache.stop();
   }

   public Navigation loadNavigation(SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      return cache.getNavigation(session, key);
   }

   public boolean saveNavigation(SiteKey key, NavigationState state) throws NavigationServiceException
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = objectType(key.getType());
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, key.getName());
      if (site == null)
      {
         throw new NavigationServiceException(NavigationError.NAVIGATION_CONCURRENCY_REMOVED);
      }

      //
      if (state != null)
      {
         org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
         org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
         boolean created = rootNode == null;
         if (created)
         {
            rootNode = root.addChild("default");
         }
         rootNode.getAttributes().setValue(MappedAttributes.PRIORITY, state.getPriority());
         return created;
      }
      else
      {
         org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
         org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
         boolean destroyed = rootNode != null;
         if (destroyed)
         {
            rootNode.destroy();
         }
         return destroyed;
      }
   }

   public <N> N loadNode(NodeModel<N> model, Navigation navigation, Scope scope)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (navigation == null)
      {
         throw new NullPointerException();
      }
      if (scope == null)
      {
         throw new NullPointerException();
      }
      String nodeId = navigation.rootId;
      if (navigation.rootId != null)
      {
         POMSession session = manager.getSession();
         Scope.Visitor visitor = scope.get();
         NodeData data = cache.getNodeData(session, nodeId);
         if (data != null)
         {
            NodeContext<N> context = load(new TreeContext<N>(), model, session, data, visitor, 0);
            return context.node;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   private <N> NodeContext<N> load(
      TreeContext<N> tree,
      NodeModel<N> model,
      POMSession session,
      NodeData data,
      Scope.Visitor visitor,
      int depth)
   {
      VisitMode visitMode = visitor.visit(depth, data.id, data.name, data.state);

      //
      NodeContext<N> context;
      if (visitMode == VisitMode.ALL_CHILDREN)
      {
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.length);
         for (String childId : data.children)
         {
            NodeData childData = cache.getNodeData(session, childId);
            if (childData != null)
            {
               NodeContext<N> childContext = load(tree, model, session, childData, visitor, depth + 1);
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context = tree.newContext(model, data);
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         context = tree.newContext(model, data);
      }
      else
      {
         context = tree.newContext(model, data);
      }

      //
      return context;
   }

   public <N> N loadNode(NodeModel<N> model, N node, Scope scope)
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      Scope.Visitor visitor = scope.get();

      //
      String nodeId = context.getId();
      NodeData data = cache.getNodeData(session, nodeId);

      //
      if (data != null)
      {
         context.data = data;
         visit(model, session, context, visitor, 0);
         return context.node;
      }
      return null;
   }

   private <N> void visit(
      NodeModel<N> model,
      POMSession session,
      NodeContext<N> context,
      Scope.Visitor visitor,
      int depth)
   {
      NodeData data = context.data;

      //
      VisitMode visitMode;
      if (context.hasTrees())
      {
         visitMode = VisitMode.ALL_CHILDREN;
      }
      else
      {
         visitMode = visitor.visit(depth, data.id, data.name, data.state);
      }

      //
      if (visitMode == VisitMode.ALL_CHILDREN)
      {
         Map<String, NodeContext<N>> previous = Collections.emptyMap();
         if (context.hasTrees())
         {
            previous = new HashMap<String, NodeContext<N>>();
            for (NodeContext<N> a : context.getContexts())
            {
               if (a.data != null)
               {
                  previous.put(a.getId(), a);
               }
            }
            context.setContexts(null);
         }

         //
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.length);
         for (String childId : data.children)
         {
            NodeData childData = cache.getNodeData(session, childId);
            if (childData != null)
            {
               NodeContext<N> childContext = previous.get(childId);
               if (childContext != null)
               {
                  childContext.data = childData;
                  visit(model, session, childContext, visitor, depth + 1);
               }
               else
               {
                  childContext = load(context.tree, model, session, childData, visitor, depth + 1);
               }
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         if (context.hasTrees())
         {
            context.setContexts(null);
         }
      }
      else
      {
         throw new AssertionError();
      }
   }


   public <N> void saveNode(NodeModel<N> model, N node) throws NullPointerException, NavigationServiceException
   {


      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      TreeContext<N> tree = context.tree;

      while (tree.hasChange())
      {
         Change change = tree.nextChange();
         if (change instanceof Change.Add)
         {
            Change.Add add = (Change.Add)change;

            //
            org.gatein.mop.api.workspace.Navigation parent = session.findObjectById(ObjectType.NAVIGATION, add.parent.data.id);
            if (parent == null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
            }

            //
            org.gatein.mop.api.workspace.Navigation added = parent.getChild(add.name);
            if (added != null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
            }
            else
            {
               added = parent.addChild(add.name);
               int index = 0;
               if (add.previous != null)
               {
                  org.gatein.mop.api.workspace.Navigation previous = session.findObjectById(ObjectType.NAVIGATION, add.previous.data.id);
                  if (previous == null)
                  {
                     throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
                  }
                  index = parent.getChildren().indexOf(previous) + 1;
               }
               parent.getChildren().add(index, added);
               add.node.data = new NodeData(added);
               add.parent.data = new NodeData(parent);
            }
         }
         else if (change instanceof Change.Remove)
         {
            Change.Remove remove = (Change.Remove)change;
            org.gatein.mop.api.workspace.Navigation removed = session.findObjectById(ObjectType.NAVIGATION, remove.node.data.id);
            if (removed != null)
            {
               org.gatein.mop.api.workspace.Navigation parent = removed.getParent();
               removed.destroy();
               remove.node.data = null;
               remove.parent.data = new NodeData(parent);
            }
            else
            {
               // It was already removed concurrently
            }
         }
         else if (change instanceof Change.Move)
         {
            Change.Move move = (Change.Move)change;
            org.gatein.mop.api.workspace.Navigation src = session.findObjectById(ObjectType.NAVIGATION, move.src.data.id);
            if (src == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
            }

            //
            org.gatein.mop.api.workspace.Navigation dst = session.findObjectById(ObjectType.NAVIGATION, move.dst.data.id);
            if (dst == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
            }

            //
            org.gatein.mop.api.workspace.Navigation moved = session.findObjectById(ObjectType.NAVIGATION, move.node.data.id);
            if (moved == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
            }

            //
            if (src != moved.getParent())
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
            }

            //
            int index = 0;
            if (move.previous != null)
            {
               org.gatein.mop.api.workspace.Navigation previous = session.findObjectById(ObjectType.NAVIGATION, move.previous.data.id);
               if (previous == null)
               {
                  throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
               }
               index = dst.getChildren().indexOf(previous) + 1;
            }
            dst.getChildren().add(index, moved);
            move.src.data = new NodeData(src);
            move.dst.data = new NodeData(dst);
         }
         else if (change instanceof Change.Rename)
         {
            Change.Rename rename = (Change.Rename)change;
            org.gatein.mop.api.workspace.Navigation renamed = session.findObjectById(ObjectType.NAVIGATION, rename.node.data.id);
            if (renamed == null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
            }

            //
            org.gatein.mop.api.workspace.Navigation parent = renamed.getParent();
            int index = parent.getChildren().indexOf(renamed);
            renamed.setName(rename.name);
            parent.getChildren().add(index, renamed);
         }
         else
         {
            throw new AssertionError("Cannot execute " + change);
         }
      }

      // Update state
      saveState(session, context);
   }

   private <N> void saveState(POMSession session, NodeContext<N> context) throws NavigationServiceException
   {
      org.gatein.mop.api.workspace.Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, context.data.id);

      //
      if (navigation == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      NodeState state = context.getState();
      if (state != null)
      {
         Workspace workspace = navigation.getSite().getWorkspace();
         String reference = state.getPageRef();
         if (reference != null)
         {
            String[] pageChunks = split("::", reference);
            ObjectType<? extends Site> siteType = Mapper.parseSiteType(pageChunks[0]);
            Site site = workspace.getSite(siteType, pageChunks[1]);
            org.gatein.mop.api.workspace.Page target = site.getRootPage().getChild("pages").getChild(pageChunks[2]);
            PageLink link = navigation.linkTo(ObjectType.PAGE_LINK);
            link.setPage(target);
         }
         else
         {
            PageLink link = navigation.linkTo(ObjectType.PAGE_LINK);
            link.setPage(null);
         }

         //
         Described described = navigation.adapt(Described.class);
         described.setName(state.getLabel());

         //
         Visible visible = navigation.adapt(Visible.class);
         visible.setVisibility(state.getVisibility());

         //
         visible.setStartPublicationDate(state.getStartPublicationDate());
         visible.setEndPublicationDate(state.getEndPublicationDate());

         //
         Attributes attrs = navigation.getAttributes();
         attrs.setValue(MappedAttributes.URI, state.getURI());
         attrs.setValue(MappedAttributes.ICON, state.getIcon());
      }

      //
      if (context.hasTrees())
      {
         for (NodeContext<N> child : context.getContexts())
         {
            saveState(session, child);
         }
      }
   }
}

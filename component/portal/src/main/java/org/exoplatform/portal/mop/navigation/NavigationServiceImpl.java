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

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import static org.exoplatform.portal.mop.navigation.Utils.*;
import static org.exoplatform.portal.pom.config.Utils.split;

import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.PageLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   final POMSessionManager manager;

   /** . */
   private final DataCache dataCache;

   /** . */
   final Logger log = LoggerFactory.getLogger(NavigationServiceImpl.class);

   public NavigationServiceImpl(POMSessionManager manager) throws NullPointerException
   {
      this(manager, new SimpleDataCache());
   }

   public NavigationServiceImpl(POMSessionManager manager, DataCache dataCache) throws NullPointerException
   {
      if (manager == null)
      {
         throw new NullPointerException("No null pom session manager allowed");
      }
      if (dataCache == null)
      {
         throw new NullPointerException("No null data cache allowed");
      }
      this.manager = manager;
      this.dataCache = dataCache;
   }

   public NavigationContext loadNavigation(SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      NavigationData data = dataCache.getNavigationData(session, key);
      return data != null ? new NavigationContext(data) : null;
   }

   public void saveNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      if (navigation == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = objectType(navigation.key.getType());
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, navigation.key.getName());

      //
      if (site == null)
      {
         throw new NavigationServiceException(NavigationError.NAVIGATION_NO_SITE);
      }

      //
      Navigation rootNode = site.getRootNavigation();

      //
      Navigation defaultNode = rootNode.getChild("default");
      if (defaultNode == null)
      {
         defaultNode = rootNode.addChild("default");
      }

      //
      NavigationState state = navigation.state;
      if (state != null)
      {
         Integer priority = state.getPriority();
         defaultNode.getAttributes().setValue(MappedAttributes.PRIORITY, priority);
      }

      //
      dataCache.removeNavigationData(session, navigation.key);

      // Update state
      navigation.data = dataCache.getNavigationData(session, navigation.key);
      navigation.state = null;
   }

   public boolean destroyNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      if (navigation == null)
      {
         throw new NullPointerException("No null navigation argument");
      }
      if (navigation.data == null)
      {
         throw new IllegalArgumentException("Already removed");
      }

      //
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = objectType(navigation.key.getType());
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, navigation.key.getName());

      //
      if (site == null)
      {
         throw new NavigationServiceException(NavigationError.NAVIGATION_NO_SITE);
      }

      //
      Navigation rootNode = site.getRootNavigation();
      Navigation defaultNode = rootNode.getChild("default");

      //
      if (defaultNode != null)
      {
         dataCache.removeNavigation(navigation.key);
         defaultNode.destroy();
         navigation.data = null;
         return true;
      }
      else
      {
         return false;
      }
   }

   public <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope, NodeChangeListener<NodeContext<N>> listener)
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
      String nodeId = navigation.data.rootId;
      if (navigation.data.rootId != null)
      {
         POMSession session = manager.getSession();
         NodeData data = dataCache.getNodeData(session, nodeId);
         if (data != null)
         {
            NodeContext<N> context = new NodeContext<N>(model, data);
            updateNode(context, scope, listener);
            return context;
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

   class DstAdapter implements HierarchyAdapter<String[], NodeData, String>
   {

      /** . */
      private final POMSession session;

      DstAdapter(POMSession session)
      {
         this.session = session;
      }

      public String getHandle(NodeData node)
      {
         return node.id;
      }

      public String[] getChildren(NodeData node)
      {
         return node.children;
      }

      public NodeData getDescendant(NodeData node, String handle)
      {
         NodeData data = dataCache.getNodeData(session, handle);
         NodeData current = data;
         while (current != null)
         {
            if (node.id.equals(current.id))
            {
               return data;
            }
            else
            {
               if (current.parentId != null)
               {
                  current = dataCache.getNodeData(session, current.parentId);
               }
               else
               {
                  current = null;
               }
            }
         }
         return null;
      }
   }

   public <N> void updateNode(NodeContext<N> root, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, IllegalArgumentException, NavigationServiceException
   {

      final POMSession session = manager.getSession();
      TreeContext<N> tree = root.tree;

      //
      if (tree.hasChanges())
      {
         throw new IllegalArgumentException("For now we don't accept to update a context that has pending changes");
      }

      //
      Scope.Visitor visitor;
      if (scope != null)
      {
         visitor = new FederatingVisitor<N>(root.tree, root, scope);
      }
      else
      {
         visitor = root.tree;
      }

      //
      if (root.tree.root != root)
      {
         root = root.tree.root;
      }

      //
      NodeData data = dataCache.getNodeData(session, root.data.id);
      if (data == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      // Switch to edit mode
      tree.editMode = true;

      // Apply diff changes to the model
      try
      {

         Update.perform(
            root,
            aaa,
            data,
            new DstAdapter(session),
            UpdateAdapter.NODE_DATA,
            listener,
            visitor);
      }
      finally
      {
         // Disable edit mode
         tree.editMode = false;
      }
   }

   public <N> void saveNode(NodeContext<N> context) throws NullPointerException, NavigationServiceException
   {
      final POMSession session = manager.getSession();
      TreeContext<N> tree = context.tree;
      List<NodeChange<NodeContext<N>>> changes = tree.popChanges();

      //
      NodeData data = dataCache.getNodeData(session, context.tree.root.data.id);
      if (data == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      final AtomicReference<NodeContext<N>> node = new AtomicReference<NodeContext<N>>();
      final Set<String> ids = new HashSet<String>();

      //
      NodeChangeListener<Navigation> persister = new NodeChangeListener.Base<Navigation>()
      {
         @Override
         public void onCreate(Navigation source, Navigation parent, Navigation previous, String name) throws NavigationServiceException
         {
            ids.add(parent.getObjectId());
            source = parent.addChild(name);
            List<Navigation> children = parent.getChildren();
            int index = previous != null ? children.indexOf(previous) + 1 : 0;
            children.add(index, source);
            node.get().data = new NodeData(source);
            node.get().handle = node.get().data.id;
         }

         @Override
         public void onDestroy(Navigation source, Navigation parent)
         {
            ids.add(source.getObjectId());
            ids.add(parent.getObjectId());
            source.destroy();
         }

         @Override
         public void onRename(Navigation source, Navigation parent, String name) throws NavigationServiceException
         {
            ids.add(source.getObjectId());
            ids.add(parent.getObjectId());
            List<Navigation> children = parent.getChildren();
            int index = children.indexOf(source);
            source.setName(name);
            children.add(index, source);
         }

         @Override
         public void onUpdate(Navigation source, NodeState state) throws NavigationServiceException
         {
            ids.add(source.getObjectId());
            Workspace workspace = source.getSite().getWorkspace();
            String reference = state.getPageRef();
            if (reference != null)
            {
               String[] pageChunks = split("::", reference);
               ObjectType<? extends Site> siteType = Mapper.parseSiteType(pageChunks[0]);
               Site site = workspace.getSite(siteType, pageChunks[1]);
               org.gatein.mop.api.workspace.Page target = site.getRootPage().getChild("pages").getChild(pageChunks[2]);
               PageLink link = source.linkTo(ObjectType.PAGE_LINK);
               link.setPage(target);
            }
            else
            {
               PageLink link = source.linkTo(ObjectType.PAGE_LINK);
               link.setPage(null);
            }

            //
            Described described = source.adapt(Described.class);
            described.setName(state.getLabel());

            //
            Visible visible = source.adapt(Visible.class);
            visible.setVisibility(state.getVisibility());

            //
            visible.setStartPublicationDate(state.getStartPublicationDate());
            visible.setEndPublicationDate(state.getEndPublicationDate());

            //
            Attributes attrs = source.getAttributes();
            attrs.setValue(MappedAttributes.URI, state.getURI());
            attrs.setValue(MappedAttributes.ICON, state.getIcon());
         }

         @Override
         public void onMove(Navigation source, Navigation from, Navigation to, Navigation previous) throws NavigationServiceException
         {
            ids.add(source.getObjectId());
            ids.add(from.getObjectId());
            ids.add(to.getObjectId());
            List<Navigation> children = to.getChildren();
            int index = previous != null ? children.indexOf(previous) + 1 : 0;
            children.add(index, source);
         }
      };

      //
      NodeChangeListener<NodeContext<N>> merger = new Merge<N, Navigation>(new MOPMergeAdapter(session), persister);

      // Compute set of ids to invalidate
      for (final NodeChange<NodeContext<N>> src : changes)
      {
         node.set(src.source);
         src.dispatch(merger);
      }

      // Make consistent
      update(context);

      //
      dataCache.removeNodeData(session, ids);
   }

   private <N> void update(NodeContext<N> context) throws NavigationServiceException
   {
      context.data = context.toData();
      context.state = null;
      if (context.isExpanded())
      {
         for (NodeContext<N> child : context.getContexts())
         {
            update(child);
         }
      }
   }

   private static class MOPMergeAdapter implements MergeAdapter<Navigation>
   {

      /** . */
      private final POMSession session;

      MOPMergeAdapter(POMSession session)
      {
         this.session = session;
      }

      public Navigation getParent(Navigation node)
      {
         return node.getParent();
      }

      public Navigation getNode(String handle)
      {
         return session.findObjectById(ObjectType.NAVIGATION, handle);
      }

      public Navigation getChild(Navigation node, String name)
      {
         return node.getChild(name);
      }

      public String getName(Navigation node)
      {
         return node.getName();
      }
   };

   private static final HierarchyAdapter aaa = new HierarchyAdapter<String[], NodeContext<Object>, String>()
   {
      public String getHandle(NodeContext<Object> node)
      {
         return node.handle;
      }

      public String[] getChildren(NodeContext<Object> node)
      {
         ArrayList<String> blah = new ArrayList<String>();
         for (NodeContext<Object> current = node.getFirst(); current != null; current = current.getNext())
         {
            blah.add(current.handle);
         }
         return blah.toArray(new String[blah.size()]);
      }

      public NodeContext<Object> getDescendant(NodeContext<Object> node, String handle)
      {
         return node.getDescendant(handle);
      }
   };

   private static final UpdateAdapter bbb = new UpdateAdapter<NodeContext<Object>>()
   {
      public NodeData getData(NodeContext<Object> node)
      {
         return node.data;
      }
   };

   public <N> void rebaseNode(NodeContext<N> root, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NavigationServiceException
   {
      // No changes -> do an update operation instead as it's cheaper
      if (!root.tree.hasChanges())
      {
         updateNode(root, scope, listener);
      }
      else
      {
         Scope.Visitor visitor;
         if (scope != null)
         {
            visitor = new FederatingVisitor<N>(root.tree.origin(), root, scope);
         }
         else
         {
            visitor = root.tree.origin();
         }

         //
         if (root.tree.root != root)
         {
            root = root.tree.root;
         }

         //
         POMSession session = manager.getSession();
         NodeData data = dataCache.getNodeData(session, root.getId());
         final NodeContext<N> context = new NodeContext<N>(root.tree.model, data);

         //
         if (data == null)
         {
            throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
         }

         //
         Update.perform(
            context,
            aaa,
            data,
            new DstAdapter(session),
            UpdateAdapter.NODE_DATA,
            null,
            visitor);

         //
         List<NodeChange<NodeContext<N>>> changes = root.tree.peekChanges();

         //
         NodeChangeListener<NodeContext<N>> persister = new NodeChangeListener.Base<NodeContext<N>>()
         {
            @Override
            public void onCreate(NodeContext<N> source, NodeContext<N> parent, NodeContext<N> previous, String name) throws NavigationServiceException
            {
               source = new NodeContext<N>(context.tree, name, new NodeState.Builder().capture());
               source.expand();
               context.tree.addChange(new NodeChange.Created<NodeContext<N>>(parent, previous, source, name));
            }

            @Override
            public void onDestroy(NodeContext<N> source, NodeContext<N> parent)
            {
               context.tree.addChange(new NodeChange.Destroyed<NodeContext<N>>(parent, source));
            }

            @Override
            public void onRename(NodeContext<N> source, NodeContext<N> parent, String name) throws NavigationServiceException
            {
               context.tree.addChange(new NodeChange.Renamed<NodeContext<N>>(parent, source, name));
            }

            @Override
            public void onUpdate(NodeContext<N> source, NodeState state) throws NavigationServiceException
            {
               context.tree.addChange(new NodeChange.Updated<NodeContext<N>>(source, state));
            }

            @Override
            public void onMove(NodeContext<N> source, NodeContext<N> from, NodeContext<N> to, NodeContext<N> previous) throws NavigationServiceException
            {
               context.tree.addChange(new NodeChange.Moved<NodeContext<N>>(from, to, previous, source));
            }
         };

         //
         NodeChangeListener<NodeContext<N>> merger = new Merge<N, NodeContext<N>>(
            context.tree,
            persister
         );

         //
         for (NodeChange<NodeContext<N>> change : changes)
         {
            change.dispatch(merger);
         }

         //
         Update.perform(
            root,
            aaa,
            context,
            aaa,
            bbb,
            listener,
            context.tree);
      }
   }

   public void clearCache()
   {
      dataCache.clear();
   }
}

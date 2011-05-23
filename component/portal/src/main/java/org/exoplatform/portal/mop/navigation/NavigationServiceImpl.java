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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
         // Invalidate cache
         dataCache.removeNavigation(navigation.key);
         String rootId = navigation.data.rootId;
         if (rootId != null)
         {
            dataCache.removeNodes(Collections.singleton(rootId));
         }

         // Destroy nav
         defaultNode.destroy();

         // Update state
         navigation.data = null;

         //
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

   public <N> void updateNode(NodeContext<N> root, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, IllegalArgumentException, NavigationServiceException
   {

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
      updateTree(root.tree, visitor, listener);
   }

   public <N> void saveNode(NodeContext<N> context, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException
   {
      saveTree(context.tree, listener);
   }


   public <N> void rebaseNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NavigationServiceException
   {
      Scope.Visitor visitor;
      if (scope != null)
      {
         visitor = new FederatingVisitor<N>(context.tree.origin(), context, scope);
      }
      else
      {
         visitor = context.tree.origin();
      }

      //
      rebaseTree(context.tree, visitor, listener);
   }

   private <N> void updateTree(TreeContext<N> tree, Scope.Visitor visitor, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, IllegalArgumentException, NavigationServiceException
   {
      if (tree.hasChanges())
      {
         throw new IllegalArgumentException("For now we don't accept to update a context that has pending changes");
      }

      //
      POMSession session = manager.getSession();
      NodeData data = dataCache.getNodeData(session, tree.root.data.id);
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
            tree.root,
            ContextHierarchyAdapter.<N>create(),
            data,
            DataHierarchyAdapter.create(dataCache, session),
            DataUpdateAdapter.create(),
            listener,
            visitor);
      }
      finally
      {
         // Disable edit mode
         tree.editMode = false;
      }
   }

   public void clearCache()
   {
      dataCache.clear();
   }

   private <N> void saveTree(TreeContext<N> tree, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException
   {
      POMSession session = manager.getSession();

      //
      NodeData data = dataCache.getNodeData(session, tree.root.data.id);
      if (data == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      // Attempt to rebase
      TreeContext<N> rebased = rebase(tree, tree.origin());

      //
      NavigationPersister<N> persister = new NavigationPersister<N>(session);

      //
      NodeChangeQueue<NodeContext<N>> changes = rebased.getChanges();
      if (changes != null)
      {
         changes.broadcast(persister);
         changes.clear();
         tree.getChanges().clear();
      }

      //
      for (Map.Entry<String, NodeData> entry : persister.dataMap.entrySet())
      {
         NodeContext<N> a = tree.getNode(entry.getKey());
         if (a != null)
         {
            NodeData d = entry.getValue();
            a.handle = d.id;
            a.data = d;
            a.state = null;
         }
         else
         {
            // The node may be removed
            // find a better way to handle that
         }
      }

      Update.perform(
         tree.root,
         ContextHierarchyAdapter.<N>create(),
         rebased.root,
         ContextHierarchyAdapter.<N>create(),
         ContextUpdateAdapter.<N>create(),
         listener,
         rebased);

      //
      dataCache.removeNodeData(session, persister.ids);
   }

   private <N> void rebaseTree(TreeContext<N> tree, Scope.Visitor visitor, NodeChangeListener<NodeContext<N>> listener) throws NavigationServiceException
   {
      if (!tree.hasChanges())
      {
         updateTree(tree, visitor, listener);
      }
      else
      {
         TreeContext<N> rebased = rebase(tree, visitor);

         //
         Update.perform(
            tree.root,
            ContextHierarchyAdapter.<N>create(),
            rebased.root,
            ContextHierarchyAdapter.<N>create(),
            ContextUpdateAdapter.<N>create(),
            listener,
            rebased);
      }
   }

   private <N> TreeContext<N> rebase(TreeContext<N> tree, Scope.Visitor visitor) throws NavigationServiceException
   {
      POMSession session = manager.getSession();
      NodeData data = dataCache.getNodeData(session, tree.root.getId());
      if (data == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      NodeContext<N> rebased = new NodeContext<N>(tree.model, data);

      //
      Update.perform(
         rebased,
         ContextHierarchyAdapter.<N>create(),
         data,
         DataHierarchyAdapter.create(dataCache, session),
         DataUpdateAdapter.create(),
         null,
         visitor);

      //
      NodeChangeQueue<NodeContext<N>> changes = tree.getChanges();

      //
      NodeChangeListener<NodeContext<N>> merger = new Merge<N>(rebased.tree, rebased.tree);

      //
      if (changes != null)
      {
         changes.broadcast(merger);
      }

      //
      return rebased.tree;
   }

   private static class ContextHierarchyAdapter<N> implements HierarchyAdapter<String[], NodeContext<N>, String>
   {

      /** . */
      private static final ContextHierarchyAdapter<?> _instance = new ContextHierarchyAdapter();

      static <N> ContextHierarchyAdapter<N> create()
      {
         @SuppressWarnings("unchecked")
         ContextHierarchyAdapter<N> instance = (ContextHierarchyAdapter<N>)_instance;
         return instance;
      }

      public String getHandle(NodeContext<N> node)
      {
         return node.handle;
      }

      public String[] getChildren(NodeContext<N> node)
      {
         if (node.getFirst() != null)
         {
            ArrayList<String> tmp = new ArrayList<String>();
            for (NodeContext<N> current = node.getFirst(); current != null; current = current.getNext())
            {
               tmp.add(current.handle);
            }
            return tmp.toArray(new String[tmp.size()]);
         }
         else
         {
            return Utils.EMPTY_STRING_ARRAY;
         }
      }

      public NodeContext<N> getDescendant(NodeContext<N> node, String handle)
      {
         return node.getDescendant(handle);
      }
   }

   static class DataHierarchyAdapter implements HierarchyAdapter<String[], NodeData, String>
   {

      static DataHierarchyAdapter create(DataCache dataCache, POMSession session)
      {
         return new DataHierarchyAdapter(dataCache, session);
      }

      /** . */
      private final DataCache dataCache;

      /** . */
      private final POMSession session;

      private DataHierarchyAdapter(DataCache dataCache, POMSession session)
      {
         this.dataCache = dataCache;
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

   private static class ContextUpdateAdapter<N> implements UpdateAdapter<NodeContext<N>>
   {

      /** . */
      private static final ContextUpdateAdapter _instance = new ContextUpdateAdapter();

      static <N> ContextUpdateAdapter<N> create()
      {
         @SuppressWarnings("unchecked")
         ContextUpdateAdapter<N> instance = (ContextUpdateAdapter<N>)_instance;
         return instance;
      }

      public NodeData getData(NodeContext<N> node)
      {
         return node.data;
      }
   }

   private static class DataUpdateAdapter implements UpdateAdapter<NodeData>
   {

      /** . */
      private static final DataUpdateAdapter instance = new DataUpdateAdapter();

      static DataUpdateAdapter create()
      {
         return instance;
      }

      public NodeData getData(NodeData node)
      {
         return node;
      }
   }

   private static class NavigationPersister<N> extends NodeChangeListener.Base<NodeContext<N>>
   {

      /** . */
      private final Map<String, NodeData> dataMap;

      /** . */
      private final POMSession session;

      /** . */
      private final Set<String> ids;

      private NavigationPersister(POMSession session)
      {
         this.dataMap = new HashMap<String, NodeData>();
         this.session = session;
         this.ids = new HashSet<String>();
      }

      @Override
      public void onCreate(NodeContext<N> source, NodeContext<N> parent, NodeContext<N> previous, String name) throws NavigationServiceException
      {
         Navigation parentNav = session.findObjectById(ObjectType.NAVIGATION, parent.data.id);
         ids.add(parentNav.getObjectId());
         int index = 0;
         if (previous != null)
         {
            Navigation previousNav = session.findObjectById(ObjectType.NAVIGATION, previous.data.id);
            index = previousNav.getIndex() + 1;
         }

         //
         Navigation sourceNav = parentNav.addChild(index, name);

         //
         parent.data = new NodeData(parentNav);
         parent.handle = parent.data.id;

         //
         NodeData data = new NodeData(sourceNav);
         dataMap.put(source.handle, data);
         source.data = data;
         source.handle = source.data.id;
      }
      @Override
      public void onDestroy(NodeContext<N> source, NodeContext<N> parent)
      {
         Navigation parentNav = session.findObjectById(ObjectType.NAVIGATION, parent.data.id);
         Navigation sourceNav = session.findObjectById(ObjectType.NAVIGATION, source.data.id);

         //
         ids.add(sourceNav.getObjectId());
         ids.add(parentNav.getObjectId());
         sourceNav.destroy();

         //
         parent.data = new NodeData(parentNav);
         parent.handle = parent.data.id;
      }
      @Override
      public void onUpdate(NodeContext<N> source, NodeState state) throws NavigationServiceException
      {
         Navigation sourceNav = session.findObjectById(ObjectType.NAVIGATION, source.data.id);

         //
         ids.add(sourceNav.getObjectId());
         Workspace workspace = sourceNav.getSite().getWorkspace();
         String reference = state.getPageRef();
         if (reference != null)
         {
            String[] pageChunks = split("::", reference);
            ObjectType<? extends Site> siteType = Mapper.parseSiteType(pageChunks[0]);
            Site site = workspace.getSite(siteType, pageChunks[1]);
            org.gatein.mop.api.workspace.Page target = site.getRootPage().getChild("pages").getChild(pageChunks[2]);
            PageLink link = sourceNav.linkTo(ObjectType.PAGE_LINK);
            link.setPage(target);
         }
         else
         {
            PageLink link = sourceNav.linkTo(ObjectType.PAGE_LINK);
            link.setPage(null);
         }

         //
         Described described = sourceNav.adapt(Described.class);
         described.setName(state.getLabel());

         //
         Visible visible = sourceNav.adapt(Visible.class);
         visible.setVisibility(state.getVisibility());

         //
         visible.setStartPublicationDate(state.getStartPublicationDate());
         visible.setEndPublicationDate(state.getEndPublicationDate());

         //
         Attributes attrs = sourceNav.getAttributes();
         attrs.setValue(MappedAttributes.URI, state.getURI());
         attrs.setValue(MappedAttributes.ICON, state.getIcon());

         //
         source.data = new NodeData(sourceNav);
         source.handle = source.data.id;
      }
      @Override
      public void onMove(NodeContext<N> source, NodeContext<N> from, NodeContext<N> to, NodeContext<N> previous) throws NavigationServiceException
      {
         Navigation sourceNav = session.findObjectById(ObjectType.NAVIGATION, source.data.id);
         Navigation fromNav = session.findObjectById(ObjectType.NAVIGATION, from.data.id);
         Navigation toNav = session.findObjectById(ObjectType.NAVIGATION, to.data.id);

         //
         ids.add(sourceNav.getObjectId());
         ids.add(fromNav.getObjectId());
         ids.add(toNav.getObjectId());
         int index;
         if (previous != null)
         {
            Navigation previousNav = session.findObjectById(ObjectType.NAVIGATION, previous.data.id);
            index = previousNav.getIndex() + 1;
         }
         else
         {
            index = 0;
         }
         toNav.getChildren().add(index, sourceNav);

         //
         from.data = new NodeData(fromNav);
         from.handle = from.data.id;

         //
         to.data = new NodeData(toNav);
         to.handle = to.data.id;

         //
         source.data = new NodeData(sourceNav);
         source.handle = source.data.id;
      }
      public void onRename(NodeContext<N> source, NodeContext<N> parent, String name) throws NavigationServiceException
      {
         Navigation sourceNav = session.findObjectById(ObjectType.NAVIGATION, source.data.id);
         Navigation parentNav = session.findObjectById(ObjectType.NAVIGATION, parent.data.id);

         //
         ids.add(sourceNav.getObjectId());
         ids.add(parentNav.getObjectId());
         sourceNav.setName(name);

         //
         source.data = new NodeData(sourceNav);
         source.handle = source.data.id;

         //
         parent.data = new NodeData(parentNav);
         parent.handle = parent.data.id;
      }
   }
}

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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import static org.exoplatform.portal.mop.navigation.Utils.*;

import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
         throw new NullPointerException();
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

   public <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope, NodeChangeListener<N> listener)
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
            Scope.Visitor visitor = scope.get();
            expand(session, context, visitor, 0, listener);
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

   class NodeDataAdapter implements HierarchyAdapter<String[], NodeData, String>
   {

      /** . */
      private final TreeContext<?> tree;

      /** . */
      private final POMSession session;

      NodeDataAdapter(TreeContext<?> tree, POMSession session)
      {
         this.tree = tree;
         this.session = session;
      }

      public String getHandle(NodeData node)
      {
         return node.id;
      }

      public String[] getChildren(NodeData node)
      {
         NodeContext<?> context = tree.root.getDescendant(node.getId());
         return context != null && context.isExpanded() ? node.children : new String[0];
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

   public <N> void updateNode(final NodeContext<N> root, Scope scope, NodeChangeListener<N> listener) throws NullPointerException, IllegalArgumentException, NavigationServiceException
   {

      final POMSession session = manager.getSession();
      TreeContext<N> tree = root.tree;

      //
      if (tree.hasChanges())
      {
         throw new IllegalArgumentException("For now we don't accept to update a context that has pending changes");
      }

      //
      NodeData dataRoot = dataCache.getNodeData(session, root.data.id);
      if (dataRoot == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      // Switch to edit mode
      tree.editMode = true;

      //
      HierarchyAdapter<String[], NodeContext<N>, String> toto = new HierarchyAdapter<String[], NodeContext<N>, String>()
      {
         public String getHandle(NodeContext<N> node)
         {
            return node.data.id;
         }

         public String[] getChildren(NodeContext<N> node)
         {
            return node.isExpanded() ? node.data.children : new String[0];
         }

         public NodeContext<N> getDescendant(NodeContext<N> node, String handle)
         {
            return node.getDescendant(handle);
         }
      };

      // Apply diff changes to the model
      try
      {

         Update.perform(
            root,
            toto,
            dataRoot,
            new NodeDataAdapter(root.tree, session),
            Update.Adapter.NODE_DATA,
            listener);
      }
      finally
      {
         // Disable edit mode
         tree.editMode = false;
      }

      // Now expand
      expand(session, root, scope != null ? scope.get() : null, 0, listener);
   }

   private <N> void expand(
      POMSession session,
      NodeContext<N> context,
      Scope.Visitor visitor,
      int depth,
      NodeChangeListener<N> listener)
   {
      // Obtain most actual data
      NodeData cachedData = dataCache.getNodeData(session, context.data.id);

      //
      if (context.isExpanded())
      {
         for (NodeContext<N> current = context.getFirst();current != null;current = current.getNext())
         {
            expand(session, current, visitor, depth + 1, listener);
         }
      }
      else
      {
         if (visitor != null)
         {
            VisitMode visitMode = visitor.visit(depth, cachedData.id, cachedData.name, cachedData.state);
            if (visitMode == VisitMode.ALL_CHILDREN)
            {
               context.expand();

               NodeContext<N> previous = null;

               for (String childId : cachedData.children)
               {
                  NodeData childData = dataCache.getNodeData(session, childId);
                  if (childData != null)
                  {
                     NodeContext<N> childContext = context.insertLast(childData);

                     // Generate event
                     if (listener != null)
                     {
                        listener.onAdd(new NodeChange.Added<N>(context, previous, childContext, childContext.data.name));
                        previous = childContext;
                     }

                     //
                     expand(session, childContext, visitor, depth + 1, listener);
                  }
                  else
                  {
                     throw new UnsupportedOperationException("Handle me gracefully");
                  }
               }

               //
               context.data = cachedData;
            }
         }
      }
   }

   public <N> void saveNode(NodeContext<N> context) throws NullPointerException, NavigationServiceException
   {
      POMSession session = manager.getSession();
      TreeContext<N> tree = context.tree;
      List<NodeChange<N>> changes = tree.popChanges();

      //
      Collection<String> ids = Save.save(changes, session, Save.Adapter.MOP);

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

   public <N> void rebaseNode(NodeContext<N> root, Scope scope, NodeChangeListener<N> listener) throws NavigationServiceException
   {
      // No changes -> do an update operation instead as it's simpler and cheaper
      if (!root.tree.hasChanges())
      {
         updateNode(root, scope, listener);
      }

      //
      POMSession session = manager.getSession();
      NodeData data = dataCache.getNodeData(session, root.id);
      NodeContext<N> context = new NodeContext<N>(root.tree.model, data);

      // Expand
      expand(session, context, root.tree, 0, null  );

      List<NodeChange<N>> changes = root.tree.peekChanges();
      NodeContext<Object> baba = (NodeContext<Object>)context;

      //
      Save.save(changes, baba.tree, Save.Adapter.CONTEXT);

      //
      HierarchyAdapter<String[], NodeContext<N>, String> aaa = new HierarchyAdapter<String[], NodeContext<N>, String>()
      {
         public String getHandle(NodeContext<N> node)
         {
            return node.id;
         }

         public String[] getChildren(NodeContext<N> node)
         {
            if (node.isExpanded() && node.getFirst() != null)
            {
               ArrayList<String> blah = new ArrayList<String>();
               for (NodeContext<N> current = node.getFirst(); current != null; current = current.getNext())
               {
                  blah.add(current.id);
               }
               return blah.toArray(new String[blah.size()]);
            } else
            {
               return new String[0];
            }
         }

         public NodeContext<N> getDescendant(NodeContext<N> node, String handle)
         {
            return node.getDescendant(handle);
         }
      };

      //
      Update.perform(
         root,
         aaa,
         context,
         aaa,
         new Update.Adapter<NodeContext<N>>()
         {
            public NodeData getData(NodeContext<N> node)
            {
               return node.data;
            }
         },
         listener);
   }

   public void clearCache()
   {
      dataCache.clear();
   }
}

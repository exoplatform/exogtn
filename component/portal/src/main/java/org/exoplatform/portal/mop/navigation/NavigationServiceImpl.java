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
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   private Map<SiteKey, NavigationDataImpl> navigationKeyCache;

   /** . */
   private Map<String, SiteKey> navigationPathCache;

   /** . */
   private Map<String, NodeData> nodeIdCache;

   /** . */
   private Map<String, String> nodePathCache;

   /** . */
   private final POMSessionManager manager;

   /** . */
   private Session bridgeSession;

   /** . */
   private InvalidationManager invalidationManager;

   /** . */
   private static final EnumMap<SiteType, ObjectType<Site>> a = new EnumMap<SiteType, ObjectType<Site>>(SiteType.class);

   /** . */
   final Logger log = LoggerFactory.getLogger(NavigationServiceImpl.class);

   static
   {
      a.put(SiteType.PORTAL, ObjectType.PORTAL_SITE);
      a.put(SiteType.GROUP, ObjectType.GROUP_SITE);
      a.put(SiteType.USER, ObjectType.USER_SITE);
   }

   public NavigationServiceImpl(POMSessionManager manager)
   {
      if (manager == null)
      {
         throw new NullPointerException("No null pom session manager allowed");
      }
      this.manager = manager;
      this.navigationKeyCache = new ConcurrentHashMap<SiteKey, NavigationDataImpl>(1000);
      this.navigationPathCache = new ConcurrentHashMap<String, SiteKey>(1000);
      this.nodeIdCache = new ConcurrentHashMap<String, NodeData>(1000);
      this.nodePathCache = new ConcurrentHashMap<String, String>(1000);
      this.invalidationManager = null;
   }

   public void start() throws Exception
   {
      Chromattic chromattic = manager.getLifeCycle().getChromattic();
      Session session = chromattic.openSession().getJCRSession();
      ObservationManager observationManager = session.getWorkspace().getObservationManager();

      invalidationManager = new InvalidationManager(observationManager);

      //
      final String NAVIGATION_CONTAINER = "mop:navigationcontainer";
      final String NAVIGATION = "mop:navigation";
      final String ATTRIBUTES = "mop:attributes";

      //
      invalidationManager.register(NAVIGATION_CONTAINER, Event.NODE_REMOVED + Event.NODE_ADDED, new Invalidator()
      {
         @Override
         void invalidate(int eventType, String nodeType, String itemPath)
         {
            if (nodeType.equals(NAVIGATION_CONTAINER))
            {
               switch (eventType)
               {
                  case Event.NODE_REMOVED:
                  {
                     String nodeId = nodePathCache.remove(itemPath);
                     if (nodeId != null)
                     {
                        nodeIdCache.remove(nodeId);
                     }
                     String parentPath = parentPath(parentPath(itemPath));
                     String id = nodePathCache.remove(parentPath);
                     if (id != null)
                     {
                        nodeIdCache.remove(id);
                     }
                     String a = parentPath(parentPath(parentPath(itemPath)));
                     SiteKey sk = navigationPathCache.remove(a);
                     if (sk != null)
                     {
                        navigationKeyCache.remove(sk);
                     }
                     break;
                  }
                  case Event.NODE_ADDED:
                  {
                     String parentPath = parentPath(parentPath(itemPath));
                     String id = nodePathCache.remove(parentPath);
                     if (id != null)
                     {
                        nodeIdCache.remove(id);
                     }
                     String a = parentPath(parentPath(parentPath(itemPath)));
                     SiteKey sk = navigationPathCache.remove(a);
                     if (sk != null)
                     {
                        navigationKeyCache.remove(sk);
                     }
                     break;
                  }
               }
            }
         }
      });

      //
      invalidationManager.register(NAVIGATION, Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED, new Invalidator()
      {
         @Override
         void invalidate(int eventType, String nodeType, String itemPath)
         {
            // Look for node
            String nodePath = parentPath(itemPath);
            String id = nodePathCache.remove(nodePath);
            if (id != null)
            {
               nodeIdCache.remove(id);
            }
         }
      });

      //
      invalidationManager.register(ATTRIBUTES, Event.NODE_ADDED + Event.NODE_REMOVED, new Invalidator()
      {
         @Override
         void invalidate(int eventType, String nodeType, String itemPath)
         {
            String nodePath = parentPath(parentPath(itemPath));

            //
            String id = nodePathCache.remove(nodePath);
            if (id != null)
            {
               nodeIdCache.remove(id);
            }

            //
            String navPath = parentPath(parentPath(parentPath(nodePath)));
            SiteKey navigationKey = navigationPathCache.remove(navPath);
            if (navigationKey != null)
            {
               navigationKeyCache.remove(navigationKey);
            }
         }
      });

      //
      this.bridgeSession = session;
   }

   private String parentPath(String path)
   {
      int index = path.lastIndexOf('/');
      return path.substring(0, index);
   }

   public void stop()
   {
      if (bridgeSession != null)
      {
         Session session = bridgeSession;
         bridgeSession = null;

         // Unregister
         try
         {
            ObservationManager om = session.getWorkspace().getObservationManager();
            EventListenerIterator i = om.getRegisteredEventListeners();
            while (i.hasNext())
            {
               EventListener listener = i.nextEventListener();
               om.removeEventListener(listener);
            }
         }
         catch (RepositoryException e)
         {
            e.printStackTrace();
         }

         //
         session.logout();
      }
   }

   public NavigationDataImpl getNavigation(SiteKey key)
   {
      NavigationDataImpl data = navigationKeyCache.get(key);
      if (data == null)
      {
         POMSession session = manager.getSession();
         ObjectType<Site> objectType = a.get(key.getType());
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(objectType, key.getName());
         if (site != null)
         {
            Navigation nav = site.getRootNavigation();
            Navigation root = nav.getChild("default");
            String rootId;
            int priority;
            if (root != null)
            {

               priority = root.getAttributes().getValue(MappedAttributes.PRIORITY, 1);
               rootId = root.getObjectId();
            }
            else
            {
               priority = 1;
               rootId = null;
            }
            data = new NavigationDataImpl(key, priority, rootId);
            navigationKeyCache.put(key, data);
            navigationPathCache.put(session.pathOf(site), key);
         }
      }
      return data;
   }


   public <N> N load(NodeModel<N> model, NavigationData navigation, Scope scope)
   {
      String nodeId = navigation.getNodeId();
      if (nodeId != null)
      {
         return load(model, nodeId, scope);
      }
      else
      {
         return null;
      }
   }

   public <N> N load(NodeModel<N> model, N node, Scope scope)
   {
      NodeContext data = model.getContext(node);
      String id = data.getId();
      return load(model, id, scope);
   }

   private <N> N load(NodeModel<N> model, String nodeId, Scope scope)
   {
      POMSession session = manager.getSession();
      Scope.Visitor visitor = scope.get();
      return load(model, session, nodeId, visitor, 0);
   }

   private NodeData getNodeData(POMSession session, String nodeId)
   {
      NodeData data = nodeIdCache.get(nodeId);
      if (data == null)
      {
         Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
         if (navigation != null)
         {
            data = new NodeData(navigation);
            nodeIdCache.put(nodeId, data);
            nodePathCache.put(session.pathOf(navigation), nodeId);
         }
      }
      return data;
   }

   private <N> N load(NodeModel<N> model, POMSession session, String nodeId, Scope.Visitor visitor, int depth)
   {
      NodeData data = getNodeData(session, nodeId);

      //
      if (data != null)
      {
         VisitMode visitMode = visitor.visit(depth, data.id, data.name, data.state);
         if (visitMode == VisitMode.ALL_CHILDREN)
         {
            NodeContextModel<N> context = new NodeContextModel<N>(model, data);
            context.children = new LinkedHashMap<String, N>(data.children.size());
            for (Map.Entry<String, String> entry : data.children.entrySet())
            {
               N child = load(model, session, entry.getValue(), visitor, depth + 1);
               if (child != null)
               {
                  context.children.put(entry.getKey(), child);
                  ((NodeContextModel)model.getContext(child)).parent = context.node;
               }
               else
               {
                  // Node is either not found (for some reason that we should try to figure out)
                  // or it was not desired
                  // in both case we don't add it to the children and it's fine for now
                  // however later when we add readability we will need to make a clear distinction
                  // as we will need to know that a node exist but was not loaded on purpose
               }
            }
            return context.node;
         }
         else if (visitMode == VisitMode.NO_CHILDREN)
         {
            return new NodeContextModel<N>(model, data).node;
         }
         else if (visitMode == VisitMode.SKIP)
         {
            return null;
         }
         else
         {
            throw new AssertionError();
         }
      }
      else
      {
         return null;
      }
   }

   public <N> void save(NodeModel<N> model, N node)
   {
      POMSession session = manager.getSession();
      save(session, model, node);
   }

   private static abstract class Action
   {

      protected Action()
      {
      }

      private static class NoOp extends Action
      {

         /** . */
         final String dstId;

         private NoOp(String dstId)
         {
            this.dstId = dstId;
         }

         @Override
         public String toString()
         {
            return "NoOp[dstId=" + dstId + "]";
         }
      }

      private static class Create extends Action
      {

         /** . */
         private final String name;

         /** . */
         private final NodeState state;

         private Create(String name, NodeState state)
         {
            this.name = name;
            this.state = state;
         }

         @Override
         public String toString()
         {
            return "Create[name=" + name + "]";
         }
      }

      private static class Order extends Action
      {

         /** . */
         private final String dstId;

         private Order(String dstId)
         {
            this.dstId = dstId;
         }

         @Override
         public String toString()
         {
            return "Order[dstId=" + dstId + "]";
         }
      }

      private static class Remove extends Action
      {

         /** . */
         private final String dstId;

         private Remove(String dstId)
         {
            this.dstId = dstId;
         }

         @Override
         public String toString()
         {
            return "Remove[dstId=" + dstId + "]";
         }
      }
   }

   public <N> void save(POMSession session, NodeModel<N> model, N node)
   {
      NodeContextModel<N> context = (NodeContextModel<N>)model.getContext(node);

      // Get the navigation node
      if (context.data == null)
      {
         throw new NullPointerException();
      }

      //
//      Navigation navigation = (Navigation)session.findObjectById(context.getId());

      //
      if (context.children != null)
      {
         if (context.data == null)
         {
            throw new UnsupportedOperationException();
         }
         else
         {
            // The source children
            ArrayList<NodeContextModel<N>> srcContexts = new ArrayList<NodeContextModel<N>>(context.children.size());
            Set<String> srcIds = new HashSet<String>();
            for (N child : context.children.values())
            {
               NodeContextModel<N> srcContext = (NodeContextModel<N>)model.getContext(child);
               srcContexts.add(srcContext);
               if (srcContext.data != null)
               {
                  srcIds.add(srcContext.data.getName());
               }

            }

            // The destination children
            ArrayList<String> dstIdList = new ArrayList<String>(context.data.children.values());

            //
            int srcIndex = 0;
            int dstIndex = 0;
            ArrayList<Action> actions = new ArrayList<Action>(context.data.children.size());
            while (srcIndex < srcContexts.size())
            {
               NodeContextModel<N> srcContext = srcContexts.get(srcIndex);
               if (srcContext.data == null)
               {
                  actions.add(new Action.Create(srcContext.name, srcContext.getState()));
                  srcIndex++;
               }
               else
               {
                  String srcId = srcContext.data.getId();
                  if (dstIndex < dstIdList.size())
                  {
                     String dstId = dstIdList.get(dstIndex);
                     if (srcId.equals(dstId))
                     {
                        actions.add(new Action.NoOp(dstId));
                        srcIndex++;
                        dstIndex++;
                     }
                     else
                     {
                        int index = dstIdList.lastIndexOf(srcId);
                        if (index > dstIndex)
                        {
                           actions.add(new Action.Order(srcId));
                           dstIdList.remove(index); // Need to find a way to avoid this remove that is expensive
                           srcIndex++;
                        }
                        else
                        {
                           throw new UnsupportedOperationException("Move operation not supported");
                        }
                     }
                  }
                  else
                  {
                     // It's a move that we don't support for now
                     throw new UnsupportedOperationException("Move operation not supported");
                  }
               }
            }

            //
            while (dstIndex < dstIdList.size())
            {
               String dstId = dstIdList.get(dstIndex);
               actions.add(new Action.Remove(dstId));
               dstIndex++;
            }

            //
            for (Action action : actions)
            {
               System.out.println("action = " + action);
            }
         }
      }
   }
}

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
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.PageLink;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.exoplatform.portal.pom.config.Utils.split;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   private Map<SiteKey, Navigation> navigationKeyCache;

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
      this.navigationKeyCache = new ConcurrentHashMap<SiteKey, Navigation>(1000);
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

   public Navigation loadNavigation(SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      Navigation data = navigationKeyCache.get(key);
      if (data == null)
      {
         POMSession session = manager.getSession();
         ObjectType<Site> objectType = a.get(key.getType());
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(objectType, key.getName());
         if (site != null)
         {
            org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
            org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
            if (rootNode != null)
            {

               Integer priority = rootNode.getAttributes().getValue(MappedAttributes.PRIORITY, 1);
               String rootId = rootNode.getObjectId();
               data = new Navigation(key, new NavigationState(priority), rootId);
            }
            else
            {
               data = new Navigation(key, null, null);
            }
            navigationKeyCache.put(key, data);
            navigationPathCache.put(session.pathOf(site), key);
         }
      }
      return data;
   }

   public boolean saveNavigation(SiteKey key, NavigationState state) throws NavigationException
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = a.get(key.getType());
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, key.getName());
      if (site == null)
      {
         throw new NavigationException("The site " + key + " does not exist");
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
         return load(model, nodeId, scope);
      }
      else
      {
         return null;
      }
   }

   public <N> N loadNode(NodeModel<N> model, N node, Scope scope)
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
         org.gatein.mop.api.workspace.Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
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
            NodeContext<N> context = new NodeContext<N>(model, data);
            context.createChildren();
            for (Map.Entry<String, String> entry : data.children.entrySet())
            {
               N child = load(model, session, entry.getValue(), visitor, depth + 1);
               if (child != null)
               {
                  context.children.put(null, (NodeContext<N>)model.getContext(child));
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
            return new NodeContext<N>(model, data).node;
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

   public <N> void saveNode(NodeModel<N> model, N node)
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = (NodeContext<N>)model.getContext(node);
      save(session, model, context);
   }

   public <N> void save(POMSession session, NodeModel<N> model, NodeContext<N> context)
   {
      // Get the navigation node
      if (context.data == null)
      {
         throw new NullPointerException();
      }

      //
      org.gatein.mop.api.workspace.Navigation navigation = (org.gatein.mop.api.workspace.Navigation)session.findObjectById(context.getId());

      // Save state
      NodeState state = context.state;
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
      if (context.children != null)
      {
         if (context.data == null)
         {
            throw new UnsupportedOperationException();
         }
         else
         {
            // The source children
            ArrayList<NodeContext<N>> srcContexts = new ArrayList<NodeContext<N>>(context.children.size());
            for (NodeContext<N> srcContext : context.children.values)
            {
               srcContexts.add(srcContext);
            }

            // The destination children
            ArrayList<String> dstIdList = new ArrayList<String>(context.data.children.values());

            //
            int srcIndex = 0;
            int dstIndex = 0;
            final List<String> orders = new ArrayList<String>();
            while (srcIndex < srcContexts.size())
            {
               NodeContext<N> srcContext = srcContexts.get(srcIndex);
               if (srcContext.data == null)
               {
                  org.gatein.mop.api.workspace.Navigation added = navigation.addChild(srcContext.name);
                  srcContext.data = new NodeData(added);
                  orders.add(added.getObjectId());
                  srcIndex++;
               }
               else
               {
                  String srcId = srcContext.data.getId();
                  orders.add(srcId);
                  if (dstIndex < dstIdList.size())
                  {
                     String dstId = dstIdList.get(dstIndex);
                     if (srcId.equals(dstId))
                     {
                        srcIndex++;
                        dstIndex++;
                     }
                     else
                     {
                        int index = dstIdList.lastIndexOf(srcId);
                        if (index > dstIndex)
                        {
                           dstIdList.remove(index); // Need to find a way to avoid this remove that is under efficient (but still very cheap)
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

               // Recurse
               save(session, model, srcContext);
            }

            // Need to make some more consistency check (for phantoms)

            // Remove the orphans
            while (dstIndex < dstIdList.size())
            {
               String dstId = dstIdList.get(dstIndex);
               org.gatein.mop.api.workspace.Navigation removed = session.findObjectById(ObjectType.NAVIGATION, dstId);
               if (removed == null)
               {
                  throw new UnsupportedOperationException("Not consistent, need a custom exception");
               }
               else if (removed.getParent() != navigation)
               {
                  throw new UnsupportedOperationException("Not consistent, need a custom exception");
               }
               removed.destroy();
               dstIndex++;
            }

            // Now sort children according to the order provided by the container
            // need to replace that with Collections.sort once the set(int index, E element) is implemented in Chromattic lists
            org.gatein.mop.api.workspace.Navigation[] a = navigation.getChildren().toArray(new org.gatein.mop.api.workspace.Navigation[navigation.getChildren().size()]);
            Arrays.sort(a, new Comparator<org.gatein.mop.api.workspace.Navigation>()
            {
               public int compare(org.gatein.mop.api.workspace.Navigation o1, org.gatein.mop.api.workspace.Navigation o2)
               {
                  int i1 = orders.indexOf(o1.getObjectId());
                  int i2 = orders.indexOf(o2.getObjectId());
                  return i1 - i2;
               }
            });
            for (int j = 0; j < a.length; j++)
            {
               navigation.getChildren().add(j, a[j]);
            }

            // Finally update context data
            context.data = new NodeData(navigation);
            context.state = null;
         }
      }
   }
}

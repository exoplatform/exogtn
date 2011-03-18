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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
   final POMSessionManager manager;

   /** . */
   private Session bridgeSession;

   /** . */
   private InvalidationManager invalidationManager;

   /** . */
   final Logger log = LoggerFactory.getLogger(NavigationServiceImpl.class);

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

   private NodeData getNodeData(POMSession session, String nodeId)
   {
      NodeData data;
      if (session.isModified())
      {
         org.gatein.mop.api.workspace.Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
         if (navigation != null)
         {
            data = new NodeData(navigation);
         }
         else
         {
            data = null;
         }
      }
      else
      {
         data = nodeIdCache.get(nodeId);
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
      }
      return data;
   }

   public Navigation loadNavigation(SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      Navigation data;
      POMSession session = manager.getSession();
      if (session.isModified())
      {
         data = findNavigation(session, key);
      }
      else
      {
         data = navigationKeyCache.get(key);
         if (data == null)
         {
            data = findNavigation(session, key);
            if (data != null)
            {
               navigationKeyCache.put(key, data);
               navigationPathCache.put(data.path, key);
            }
         }
      }

      //
      return data;
   }

   private Navigation findNavigation(POMSession session, SiteKey key)
   {
      Workspace workspace = session.getWorkspace();
      ObjectType<Site> objectType = objectType(key.getType());
      Site site = workspace.getSite(objectType, key.getName());
      if (site != null)
      {
         org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
         org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
         String path = session.pathOf(site);
         if (rootNode != null)
         {

            Integer priority = rootNode.getAttributes().getValue(MappedAttributes.PRIORITY, 1);
            String rootId = rootNode.getObjectId();
            return new Navigation(path, key, new NavigationState(priority), rootId);
         }
         else
         {
            return new Navigation(path, key, null, null);
         }
      }
      else
      {
         return null;
      }
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
         throw new NavigationServiceException("The site " + key + " does not exist");
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
         NodeData data = getNodeData(session, nodeId);
         if (data != null)
         {
            NodeContext<N> context = load(model, session, data, visitor, 0);
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
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.size());
         for (String childId : data.children)
         {
            NodeData childData = getNodeData(session, childId);
            if (childData != null)
            {
               NodeContext<N> childContext = load(model, session, childData, visitor, depth + 1);
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context = new NodeContext<N>(model, data);
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         context = new NodeContext<N>(model, data);
      }
      else
      {
         context = new NodeContext<N>(model, data);
      }

      //
      return context;
   }

   public <N> N loadNode(NodeModel<N> model, N node, Scope scope)
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      Scope.Visitor visitor = scope.get();
      return load(model, session, context, visitor, 0);
   }

   private <N> N load(NodeModel<N> model, POMSession session, NodeContext<N> context, Scope.Visitor visitor, int depth)
   {
      String nodeId = context.getId();
      NodeData data = getNodeData(session, nodeId);

      //
      if (data != null)
      {
         context.data = data;
         visit(model, session, context, visitor, depth);
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
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.size());
         for (String childId : data.children)
         {
            NodeData childData = getNodeData(session, childId);
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
                  childContext = load(model, session, childData, visitor, depth + 1);
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


   public <N> void refresh(NodeModel<N> model, N node, Scope scope) throws NullPointerException, NavigationServiceException
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      refresh(model, session, context, scope.get(), 0);
   }

   private <N> void refresh(
      NodeModel<N> model,
      POMSession session,
      NodeContext<N> context,
      Scope.Visitor visitor,
      int depth)
   {

      String id = context.data.getId();

      NodeData from = context.data;

      NodeData to = getNodeData(session, id);

      if (to == null)
      {
         throw new UnsupportedOperationException("Handle me gracefully");
      }

      //
      if (context.hasTrees())
      {
         Iterable<NodeContext<N>> children = context.getContexts();

         // Remove what we need
         for (Iterator<NodeContext<N>> it = children.iterator();it.hasNext();)
         {
            NodeContext<N> child = it.next();
            if (child.data == null || !to.children.contains(child.data.getId()))
            {
               it.remove();
            }
            else
            {
               // We do nothing for now
            }
         }
      }
      else
      {
         throw new UnsupportedOperationException("Handle me gracefully");
      }

      // Update data now
      context.data = to;
   }

   public <N> void saveNode(NodeModel<N> model, N node)
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);

      //
      SaveContext<N> save = new SaveContext<N>(context);

      //
      save.phase0(session);

      //
      save.phase1(session);

      //
      save.phase2(session);

      //
      save.phase3(session);

      //
      save.phase4(session);

      //
      save.phase5(session);

      //
      save.phase6(session);
   }

   private static class SaveContext<N>
   {

      /** . */
      private final NodeContext<N> context;

      /** . */
      private final List<SaveContext<N>> children;

      /** The list of actual children ids, maintained during the phases. */
      private List<String> childrenIds;

      /** . */
      private SaveContext<N> parent;

      /** The related navigation object. */
      private org.gatein.mop.api.workspace.Navigation  navigation;

      private SaveContext(NodeContext<N> context)
      {
         List<SaveContext<N>> children;
         if (context.hasTrees())
         {
            children = new ArrayList<SaveContext<N>>();
            for (NodeContext<N> childCtx : context.getContexts())
            {
               SaveContext<N> child = new SaveContext<N>(childCtx);
               children.add(child);
               child.parent = this;
            }
         }
         else
         {
            children = Collections.emptyList();
         }

         //
         ArrayList<String> bilto;
         if (context.data != null)
         {
            bilto = new ArrayList<String>(context.data.children);
         }
         else
         {
            bilto = new ArrayList<String>();
         }

         //
         this.context = context;
         this.children = children;
         this.childrenIds = bilto;
         this.navigation = null;
      }

      static abstract class Finder<N>
      {

         final SaveContext<N> any(SaveContext<N> context)
         {
            SaveContext<N> root = context;
            while (root.parent != null)
            {
               root = root.parent;
            }
            return descendants(root);
         }

         final SaveContext<N> descendants(SaveContext<N> context)
         {
            SaveContext<N> found = null;
            if (accept(context))
            {
               found = context;
            }
            else
            {
               int size = context.children.size();
               for (int i = 0;i < size;i++)
               {
                  found = descendants(context.children.get(i));
                  if (found != null)
                  {
                     break;
                  }
               }
            }
            return found;
         }

         abstract boolean accept(SaveContext<N> context);
      }

      // Remove orphans
      void phase0(POMSession session)
      {
         if (context.hasTrees())
         {
            if (context.data != null)
            {
               for (final String childId : context.data.children)
               {
                  // Is it still here ?
                  boolean found = false;
                  for (NodeContext<N> childContext : context.getContexts())
                  {
                     if (childContext.data != null && childContext.data.id.equals(childId))
                     {
                        found = true;
                     }
                  }

                  //
                  if (!found)
                  {
                     Finder<N> finder = new Finder<N>()
                     {
                        boolean accept(SaveContext<N> context)
                        {
                           return context.context.data != null && context.context.data.getId().equals(childId);
                        }
                     };
                     if (finder.any(this) == null)
                     {
                        org.gatein.mop.api.workspace.Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, childId);
                        navigation.destroy();
                     }
                     else
                     {
                        // It's a move operation
                     }
                     childrenIds.remove(childId);
                  }
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase0(session);
         }
      }

      // Create new nodes and associates with navigation object
      void phase1(POMSession session)
      {
         if (context.data == null)
         {
            throw new IllegalArgumentException();
         }
         else
         {
            navigation = session.findObjectById(ObjectType.NAVIGATION, context.getId());

            //
            for (SaveContext<N> child : children)
            {
               if (child.context.data == null)
               {
                  org.gatein.mop.api.workspace.Navigation added = navigation.addChild(child.context.getName());
                  child.context.data = new NodeData(added);
                  childrenIds.add(added.getObjectId());
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase1(session);
         }
      }

      // Rename nodes
      void phase2(POMSession session)
      {
         if (!context.data.name.equals(context.getName()))
         {
            navigation.setName(context.getName());
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase2(session);
         }
      }

      // Update state
      void phase3(POMSession session)
      {
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
         for (SaveContext<N> child : children)
         {
            child.phase3(session);
         }
      }

      // Move nodes
      void phase4(POMSession session)
      {

         if (context.hasTrees())
         {
            for (NodeContext<N> childCtx : context.getContexts())
            {
               final String childId = childCtx.data.id;
               if (!context.data.children.contains(childId))
               {
                  if (!childrenIds.contains(childId))
                  {
                     Finder<N> finder = new Finder<N>()
                     {
                        boolean accept(SaveContext<N> context)
                        {
                           return context.context.data.getId().equals(childId);
                        }
                     };
                     SaveContext<N> movedCtx = finder.any(this);
                     org.gatein.mop.api.workspace.Navigation moved = movedCtx.navigation;
                     navigation.getChildren().add(moved);
                     childrenIds.add(childId);
                  }
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase4(session);
         }
      }

      // Reorder nodes
      void phase5(POMSession session)
      {
         if (context.hasTrees())
         {
            final ArrayList<String> orders = new ArrayList<String>();
            for (NodeContext<N> foo : context.getContexts())
            {
               orders.add(foo.data.id);
            }

            //
            if (!childrenIds.equals(orders))
            {
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
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase5(session);
         }
      }

      // Update model
      void phase6(POMSession session)
      {
         Set<String> childMap;
         if (context.hasTrees())
         {
            // todo : I think we have a bug here :-)
            childMap = new LinkedHashSet<String>();
         }
         else
         {
            childMap = context.data.children;
         }

         //
         String id = context.data.id;
         String name = context.getName();
         NodeState state = context.getState();

         //
         context.data = new NodeData(id, name, state, childMap);

         //
         for (SaveContext<N> child : children)
         {
            child.phase6(session);
         }
      }
   }
}

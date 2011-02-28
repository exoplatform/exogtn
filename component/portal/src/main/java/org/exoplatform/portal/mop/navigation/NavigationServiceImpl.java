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
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   private Map<SiteKey, NavigationData> navigationKeyCache;

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

   static
   {
      a.put(SiteType.PORTAL, ObjectType.PORTAL_SITE);
      a.put(SiteType.GROUP, ObjectType.GROUP_SITE);
      a.put(SiteType.USER, ObjectType.USER_SITE);
   }

   public NavigationServiceImpl(POMSessionManager manager)
   {
      this.manager = manager;
      this.navigationKeyCache = new ConcurrentHashMap<SiteKey, NavigationData>(1000);
      this.navigationPathCache = new ConcurrentHashMap<String, SiteKey>(1000);;
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

   public String getRootId(SiteType siteType, String siteName)
   {
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = a.get(siteType);
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, siteName);
      Navigation nav = site.getRootNavigation();
      Navigation root = nav.getChild("default");
      return root != null ? root.getObjectId() : null;
   }

   public NavigationData getNavigation(SiteKey key)
   {
      NavigationData data = navigationKeyCache.get(key);
      if (data == null)
      {
         POMSession session = manager.getSession();
         ObjectType<Site> objectType = a.get(key.getType());
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(objectType, key.getName());
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
         data = new NavigationData(key, priority, rootId);
         navigationKeyCache.put(key, data);
         navigationPathCache.put(session.pathOf(site), key);
      }

      //
      return data;
   }

   public Node load(String nodeId, Scope scope)
   {
      POMSession session = manager.getSession();
      Scope.Visitor visitor = scope.get();
      return load(session, nodeId, visitor, 0);
   }

   private NodeImpl load(POMSession session, String navigationId, Scope.Visitor visitor, int depth)
   {
      NodeData data = nodeIdCache.get(navigationId);
      if (data == null)
      {
         Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, navigationId);
         if (navigation == null)
         {
            return null;
         }
         else
         {
            data = new NodeData(navigation);
            nodeIdCache.put(navigationId, data);
            nodePathCache.put(session.pathOf(navigation), navigationId);
         }
      }

      //
      switch (visitor.visit(depth, data.id, data.name, data))
      {
         case CHILDREN:
            NodeImpl.FragmentImpl children = new NodeImpl.FragmentImpl(data.children.size());
            for (Map.Entry<String, String> entry : data.children.entrySet())
            {
               NodeImpl child = load(session, entry.getValue(), visitor, depth + 1);
               children.put(child.data.name, child);
            }
            return new NodeImpl(data, children);
         case NODE:
            return new NodeImpl(data);
         default:
            throw new AssertionError();
      }
   }

   public void save(Node node)
   {
      throw new UnsupportedOperationException();
   }
}

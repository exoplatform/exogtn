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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.exoplatform.portal.mop.navigation.Utils.objectType;

/**
 * Manages id.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class CacheById extends Cache implements Invalidator
{

   /** . */
   final String NAVIGATION_CONTAINER = "mop:navigationcontainer";

   /** . */
   final String NAVIGATION = "mop:navigation";

   /** . */
   final String ATTRIBUTES = "mop:attributes";

   /** . */
   private InvalidationManager invalidationManager;

   /** . */
   private Map<SiteKey, NavigationContext> navigationKeyCache;

   /** . */
   private Map<String, SiteKey> navigationPathCache;

   /** . */
   private Map<String, NodeData> nodeIdCache;

   /** . */
   private Map<String, String> nodePathCache;

   /** . */
   private Session bridgeSession;

   CacheById()
   {
      this.nodeIdCache = new ConcurrentHashMap<String, NodeData>(1000);
      this.nodePathCache = new ConcurrentHashMap<String, String>(1000);
      this.navigationKeyCache = new ConcurrentHashMap<SiteKey, NavigationContext>(1000);
      this.navigationPathCache = new ConcurrentHashMap<String, SiteKey>(1000);
   }

   NodeData getNodeData(POMSession session, String nodeId)
   {
      NodeData data;
      if (session.isModified())
      {
         Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
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
            Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
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

   NavigationContext getNavigation(POMSession session, SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      NavigationContext data;
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

   private NavigationContext findNavigation(POMSession session, SiteKey key)
   {
      Workspace workspace = session.getWorkspace();
      ObjectType<Site> objectType = objectType(key.getType());
      Site site = workspace.getSite(objectType, key.getName());
      if (site != null)
      {
         Navigation root = site.getRootNavigation();
         Navigation rootNode = root.getChild("default");
         String path = session.pathOf(site);
         if (rootNode != null)
         {

            Integer priority = rootNode.getAttributes().getValue(MappedAttributes.PRIORITY, 1);
            String rootId = rootNode.getObjectId();
            return new NavigationContext(path, key, new NavigationState(priority), rootId);
         }
         else
         {
            return new NavigationContext(path, key, null, null);
         }
      }
      else
      {
         return null;
      }
   }

   void start(Session session) throws Exception
   {
      ObservationManager observationManager = session.getWorkspace().getObservationManager();

      //
      InvalidationManager invalidationManager = new InvalidationManager(observationManager);
      invalidationManager.register(NAVIGATION_CONTAINER, Event.NODE_REMOVED + Event.NODE_ADDED, this);
      invalidationManager.register(NAVIGATION, Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED, this);
      invalidationManager.register(ATTRIBUTES, Event.NODE_ADDED + Event.NODE_REMOVED, this);

      //
      this.invalidationManager = invalidationManager;
      this.bridgeSession = session;
   }

   void stop()
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

   public void invalidate(int eventType, String nodeType, String itemPath)
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
      else if (nodeType.equals(NAVIGATION))
      {
         // Look for node
         String nodePath = parentPath(itemPath);
         String id = nodePathCache.remove(nodePath);
         if (id != null)
         {
            nodeIdCache.remove(id);
         }
      }
      else if (nodeType.equals(ATTRIBUTES))
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
   }

   private String parentPath(String path)
   {
      int index = path.lastIndexOf('/');
      return path.substring(0, index);
   }
}

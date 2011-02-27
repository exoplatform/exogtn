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
import org.chromattic.api.ChromatticSession;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** The cache. */
   private Map<String, NodeData> idCache;

   /** The cache. */
   private Map<String, String> pathCache;

   /** . */
   private final POMSessionManager manager;

   /** . */
   private Session bridgeSession;

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
      this.idCache = new ConcurrentHashMap<String, NodeData>(1000);
      this.pathCache = new ConcurrentHashMap<String, String>(1000);
   }

   public void start() throws Exception
   {
      Chromattic chromattic = manager.getLifeCycle().getChromattic();
      Session session = chromattic.openSession().getJCRSession();
      final CompletionService<Void> completer = new ExecutorCompletionService<Void>(Executors.newSingleThreadExecutor());
      ObservationManager observationManager = session.getWorkspace().getObservationManager();
      observationManager.addEventListener(
         new EventListener()
         {
            public void onEvent(final EventIterator events)
            {
               completer.submit(new Callable<Void>()
               {
                  public Void call() throws Exception
                  {
                     while (events.hasNext())
                     {
                        Event event = events.nextEvent();
                        String id = pathCache.remove(event.getPath());
                        if (id != null)
                        {
                           idCache.remove(id);
                        }
                     }
                     return null;
                  }
               });
            }
         }, Event.NODE_REMOVED, "/", true, null, null, false );

      //
      this.bridgeSession = session;
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

   public Node load(String nodeId, Scope scope)
   {
      POMSession session = manager.getSession();
      Scope.Visitor visitor = scope.get();
      return load(session, nodeId, visitor);
   }

   private NodeImpl load(POMSession session, String navigationId, Scope.Visitor visitor)
   {
      NodeData data = idCache.get(navigationId);
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
            idCache.put(navigationId, data);
            pathCache.put(session.pathOf(navigation), navigationId);
         }
      }

      //
      if (visitor.children(data.id, data.name))
      {
         LinkedHashMap<String, NodeImpl> children = new LinkedHashMap<String, NodeImpl>(data.children.size());
         for (Map.Entry<String, String> entry : data.children.entrySet())
         {
            NodeImpl child = load(session, entry.getValue(), visitor);
            children.put(child.data.id, child);
         }
         return new NodeImpl.FragmentImpl(data, children);
      }
      else
      {
         return new NodeImpl(data);
      }
   }

   public void save(Node node)
   {
      throw new UnsupportedOperationException();
   }
}

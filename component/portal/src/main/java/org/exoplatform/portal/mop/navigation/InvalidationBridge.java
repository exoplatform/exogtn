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

import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class InvalidationBridge
{

   /** . */
   private final DataCache cache;

   /** . */
   private ObservationManager mgr;

   /** . */
   private final EventListenerImpl portalBridge;

   /** . */
   private final EventListenerImpl groupBridge;

   /** . */
   private final EventListenerImpl userBridge;

   public InvalidationBridge(DataCache cache)
   {
      this.cache = cache;
      this.portalBridge = new EventListenerImpl("mop:portalsite", SiteType.PORTAL);
      this.groupBridge = new EventListenerImpl("mop:groupsite", SiteType.GROUP);
      this.userBridge = new EventListenerImpl("mop:usersite", SiteType.USER);
   }

   void start(Session session) throws RepositoryException
   {
      mgr = session.getWorkspace().getObservationManager();

      //
      portalBridge.register(mgr);
      groupBridge.register(mgr);
      userBridge.register(mgr);
   }

   void stop()
   {
      portalBridge.unregister();
      groupBridge.unregister();
      userBridge.unregister();
   }

   private class EventListenerImpl implements EventListener
   {

      /** . */
      private final String nodeType;

      /** . */
      private final SiteType type;

      /** . */
      private ObservationManager mgr;

      private EventListenerImpl(String nodeType, SiteType type)
      {
         this.nodeType = nodeType;
         this.type = type;
      }

      void register(ObservationManager mgr) throws RepositoryException
      {
         mgr.addEventListener(portalBridge, Event.NODE_REMOVED, null, true, null, new String[]{nodeType}, false);

         //
         this.mgr = mgr;
      }

      void unregister()
      {
         if (mgr != null)
         {
            try
            {
               mgr.removeEventListener(this);
            }
            catch (RepositoryException ignore)
            {
            }
         }
      }

      public void onEvent(EventIterator events)
      {
         while (events.hasNext())
         {
            try
            {
               Event event = events.nextEvent();
               String path = event.getPath();
               int pos = path.lastIndexOf('/');
               String name = path.substring(pos + 1);
               SiteKey key = new SiteKey(type, name);
               cache.removeNavigation(key);
            }
            catch (RepositoryException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}

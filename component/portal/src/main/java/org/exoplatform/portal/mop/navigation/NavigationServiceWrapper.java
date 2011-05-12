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

import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.SiteKey;
import static org.exoplatform.portal.mop.navigation.Utils.*;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceWrapper implements NavigationService
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(NavigationServiceWrapper.class);

   /** . */
   private final NavigationServiceImpl service;

   /** . */
   private ListenerService listenerService;

   public NavigationServiceWrapper(POMSessionManager manager, ListenerService listenerService)
   {
      this.service = new NavigationServiceImpl(manager);
      this.listenerService = listenerService;
   }

   public NavigationServiceWrapper(POMSessionManager manager, ListenerService listenerService, CacheService cacheService)
   {
      this.service = new NavigationServiceImpl(manager, new ExoDataCache(cacheService));
      this.listenerService = listenerService;
   }

   public NavigationContext loadNavigation(SiteKey key)
   {
      return service.loadNavigation(key);
   }

   public void saveNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      boolean created = navigation.data == null;

      //
      service.saveNavigation(navigation);

      //
      if (created)
      {
         notify(EventType.NAVIGATION_CREATED, navigation.key);
      }
      else
      {
         notify(EventType.NAVIGATION_UPDATED, navigation.key);
      }
   }

   public boolean destroyNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      boolean destroyed = service.destroyNavigation(navigation);

      //
      if (destroyed)
      {
         notify(EventType.NAVIGATION_DESTROYED, navigation.key);
      }

      //
      return destroyed;
   }

   public <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope, NodeChangeListener<N> listener)
   {
      return service.loadNode(model, navigation, scope, listener);
   }

   public <N> void saveNode(NodeContext<N> context) throws NavigationServiceException
   {
      service.saveNode(context);
      org.gatein.mop.api.workspace.Navigation nav = service.manager.getSession().findObjectById(ObjectType.NAVIGATION, context.data.id);
      Site site = nav.getSite();
      SiteKey key = new SiteKey(siteType(site.getObjectType()), site.getName());
      notify(EventType.NAVIGATION_UPDATED, key);
   }

   public <N> void updateNode(NodeContext<N> context, Scope scope, NodeChangeListener<N> listener) throws NullPointerException, NavigationServiceException
   {
      service.updateNode(context, scope, listener);
   }

   public <N> void rebaseNode(NodeContext<N> context, Scope scope, NodeChangeListener<N> listener) throws NullPointerException, NavigationServiceException
   {
      service.rebaseNode(context, scope, listener);
   }

   private void notify(String name, SiteKey key)
   {
      try
      {
         listenerService.broadcast(name, this, key);
      }
      catch (Exception e)
      {
         log.error("Error when delivering notification " + name + " for navigation " + key, e);
      }
   }
}

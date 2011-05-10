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

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.util.Collection;

/**
 * An implementation using the cache service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExoDataCache extends DataCache
{

   /** . */
   protected ExoCache<SiteKey, NavigationData> navigationsCache;

   /** . */
   protected FutureExoCache<SiteKey, NavigationData, POMSession> navigations;

   /** . */
   protected ExoCache<String, NodeData> nodeCache;

   /** . */
   protected FutureExoCache<String, NodeData, POMSession> nodes;

   /** . */
   private Loader<String, NodeData, POMSession> nodeLoader = new Loader<String, NodeData, POMSession>()
   {
      public NodeData retrieve(POMSession session, String nodeId) throws Exception
      {
         return loadNode(session, nodeId);
      }
   };

   /** . */
   private Loader<SiteKey, NavigationData, POMSession> navigationLoader = new Loader<SiteKey, NavigationData, POMSession>()
   {
      public NavigationData retrieve(POMSession session, SiteKey key) throws Exception
      {
         return loadNavigation(session, key);
      }
   };

   public ExoDataCache(CacheService cacheService)
   {
      this.navigationsCache = cacheService.getCacheInstance(ExoDataCache.class + ".navigations");
      this.navigations = new FutureExoCache<SiteKey, NavigationData, POMSession>(navigationLoader, navigationsCache);
      this.nodeCache = cacheService.getCacheInstance(ExoDataCache.class + ".nodes");
      this.nodes = new FutureExoCache<String, NodeData, POMSession>(nodeLoader, nodeCache);
   }

   @Override
   protected void removeNodes(Collection<String> keys)
   {
      for (String key : keys)
      {
         nodeCache.remove(key);
      }
   }

   @Override
   protected NodeData getNode(POMSession session, String key)
   {
      return nodes.get(session, key);
   }

   @Override
   protected void removeNavigation(SiteKey key)
   {
      navigationsCache.remove(key);
   }

   @Override
   protected NavigationData getNavigation(POMSession session, SiteKey key)
   {
      return navigations.get(session, key);
   }

   @Override
   protected void clear()
   {
      navigationsCache.clearCache();
      nodeCache.clearCache();
   }
}

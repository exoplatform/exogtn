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
   protected ExoCache<SiteKey, NavigationData> navigations;

   /** . */
   protected ExoCache<String, NodeData> nodes;

   public ExoDataCache(CacheService cacheService)
   {
      this.navigations = cacheService.getCacheInstance(ExoDataCache.class + ".navigations");
      this.nodes = cacheService.getCacheInstance(ExoDataCache.class + ".nodes");
   }

   @Override
   protected void removeNodes(Collection<String> keys)
   {
      for (String key : keys)
      {
         nodes.remove(key);
      }
   }

   @Override
   protected void putNode(String key, NodeData node)
   {
      nodes.put(key, node);
   }

   @Override
   protected NodeData getNode(String key)
   {
      return nodes.get(key);
   }

   @Override
   protected void removeNavigation(SiteKey key)
   {
      navigations.remove(key);
   }

   @Override
   protected void putNavigation(SiteKey key, NavigationData navigation)
   {
      navigations.put(key, navigation);
   }

   @Override
   protected NavigationData getNavigation(SiteKey key)
   {
      return navigations.get(key);
   }

   @Override
   protected void clear()
   {
      navigations.clearCache();
      nodes.clearCache();
   }
}

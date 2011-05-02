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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple implementation for unit testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache
{

   /** . */
   protected Map<SiteKey, NavigationData> navigations;

   /** . */
   protected Map<String, NodeData> nodes;

   public SimpleDataCache()
   {
      this.navigations = new ConcurrentHashMap<SiteKey, NavigationData>();
      this.nodes = new ConcurrentHashMap<String, NodeData>();
   }

   @Override
   protected void removeNodes(Collection<String> keys)
   {
      nodes.keySet().removeAll(keys);
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
}

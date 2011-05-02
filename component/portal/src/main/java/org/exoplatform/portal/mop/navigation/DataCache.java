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

import java.util.Collection;

import static org.exoplatform.portal.mop.navigation.Utils.objectType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class DataCache
{

   protected abstract void removeNodes(Collection<String> keys);

   protected abstract void putNode(String key, NodeData navigation);

   protected abstract NodeData getNode(String key);

   protected abstract void putNavigation(SiteKey key, NavigationData navigation);

   protected abstract NavigationData getNavigation(SiteKey key);

   protected abstract void removeNavigation(SiteKey key);

   final NodeData getNodeData(POMSession session, String nodeId)
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
         data = getNode(nodeId);
         if (data == null)
         {
            Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
            if (navigation != null)
            {
               data = new NodeData(navigation);
               putNode(nodeId, data);
            }
         }
      }
      return data;
   }

   final void removeNodeData(Collection<String> nodeId)
   {
      removeNodes(nodeId);
   }

   NavigationData getNavigationContext(POMSession session, SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      NavigationData navigation;
      if (session.isModified())
      {
         navigation = findNavigation(session, key);
      }
      else
      {
         navigation = getNavigation(key);
         if (navigation == null)
         {
            navigation = findNavigation(session, key);
            if (navigation != null)
            {
               putNavigation(key, navigation);
            }
         }
      }

      //
      return navigation;
   }

   void removeNavigationContext(SiteKey key)
   {
      removeNavigation(key);
   }

   private NavigationData findNavigation(POMSession session, SiteKey key)
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
            return new NavigationData(key, new NavigationState(priority), rootId);
         }
         else
         {
            return new NavigationData(key, null, null);
         }
      }
      else
      {
         return null;
      }
   }
}

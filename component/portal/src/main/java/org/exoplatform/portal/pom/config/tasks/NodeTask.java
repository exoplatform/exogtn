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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.NodeData;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.link.PageLink;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class NodeTask<K extends Serializable> implements CacheableDataTask<K, NodeData>
{

   public abstract static class KeyType<K>
   {

      public static final KeyType<String> STRING = new KeyType<String>()
      {
         @Override
         protected Navigation load(POMSession session, String key)
         {
            return session.findObjectById(ObjectType.NAVIGATION, key);
         }
      };

      public static final KeyType<NavigationKey> NAVIGATION = new KeyType<NavigationKey>()
      {
         @Override
         protected Navigation load(POMSession session, NavigationKey key)
         {
            ObjectType<Site> siteType = Mapper.parseSiteType(key.getType());
            Workspace workspace = session.getWorkspace();
            Site site = workspace.getSite(siteType, key.getId());
            Navigation nav = site.getRootNavigation();
            return nav.getChild("default");
         }
      };

      protected abstract Navigation load(POMSession session, K key);

   }

   /** . */
   protected final KeyType<K> keyType;

   /** . */
   protected final K key;

   protected NodeTask(KeyType<K> keyType, K key)
   {
      if (keyType == null)
      {
         throw new NullPointerException("No null key type accepted");
      }
      if (key == null)
      {
         throw new NullPointerException("No null key accepted");
      }

      //
      this.keyType = keyType;
      this.key = key;
   }

   public final K getKey()
   {
      return key;
   }

   public final Class<NodeData> getValueType()
   {
      return NodeData.class;
   }

   public static class Load<K extends Serializable> extends NodeTask<K>
   {

      public Load(KeyType<K> keyType, K key)
      {
         super(keyType, key);
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public NodeData run(POMSession session) throws Exception
      {
         Navigation src = keyType.load(session, key);

         //
         Attributes attrs = src.getAttributes();
         Described described = src.adapt(Described.class);
         Visible visible = src.adapt(Visible.class);

         //
         String pageReference = null;
         Link link = src.getLink();
         if (link instanceof PageLink)
         {
            PageLink pageLink = (PageLink)link;
            org.gatein.mop.api.workspace.Page target = pageLink.getPage();
            if (target != null)
            {
               Site site = target.getSite();
               ObjectType<? extends Site> siteType = site.getObjectType();
               pageReference = Mapper.getOwnerType(siteType) + "::" + site.getName() + "::" + target.getName();
            }
         }

         //
         return new NodeData(
            src.getObjectId(),
            attrs.getValue(MappedAttributes.URI),
            described.getName(),
            attrs.getValue(MappedAttributes.ICON),
            src.getName(),
            visible.getStartPublicationDate(),
            visible.getEndPublicationDate(),
            visible.getVisibility() != null ? visible.getVisibility() : Visibility.DISPLAYED,
            pageReference,
            new String[0]
         );
      }
   }
}

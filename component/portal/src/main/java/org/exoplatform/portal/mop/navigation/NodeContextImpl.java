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

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.link.PageLink;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * An immutable node data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeContextImpl implements NodeContext, NodeState, Serializable
{

   /** . */
   final String id;

   /** . */
   final String name;

   /** . */
   final String uri;

   /** . */
   final String label;

   /** . */
   final String icon;

   /** . */
   final long startPublicationTime;

   /** . */
   final long endPublicationTime;

   /** . */
   final Visibility visibility;

   /** . */
   final String pageRef;

   /** . */
   final LinkedHashMap<String, String> children;

   NodeContextImpl(Navigation nav)
   {
      LinkedHashMap<String, String> children = new LinkedHashMap<String, String>();
      for (Navigation child : nav.getChildren())
      {
         children.put(child.getName(), child.getObjectId());
      }

      //
      String label = null;
      if (nav.isAdapted(Described.class))
      {
         Described described = nav.adapt(Described.class);
         label = described.getName();
      }

      //
      Visibility visibility = Visibility.DISPLAYED;
      Date startPublicationDate = null;
      Date endPublicationDate = null;
      if (nav.isAdapted(Visible.class))
      {
         Visible visible = nav.adapt(Visible.class);
         visibility = visible.getVisibility();
         startPublicationDate = visible.getStartPublicationDate();
         endPublicationDate = visible.getEndPublicationDate();
      }

      //
      String pageRef = null;
      Link link = nav.getLink();
      if (link instanceof PageLink)
      {
         PageLink pageLink = (PageLink)link;
         org.gatein.mop.api.workspace.Page target = pageLink.getPage();
         if (target != null)
         {
            Site site = target.getSite();
            ObjectType<? extends Site> siteType = site.getObjectType();
            pageRef = Mapper.getOwnerType(siteType) + "::" + site.getName() + "::" + target.getName();
         }
      }

      //
      Attributes attrs = nav.getAttributes();

      //
      this.id = nav.getObjectId();
      this.name = nav.getName();
      this.uri = attrs.getValue(MappedAttributes.URI);
      this.label = label;
      this.icon = attrs.getValue(MappedAttributes.ICON);
      this.startPublicationTime = startPublicationDate != null ? startPublicationDate.getTime() : -1;
      this.endPublicationTime = endPublicationDate != null ? endPublicationDate.getTime() : -1;
      this.visibility = visibility;
      this.pageRef = pageRef;
      this.children = children;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public int getChildrenCount()
   {
      return children.size();
   }

   public NodeState getState()
   {
      return this;
   }

   public String getURI()
   {
      return uri;
   }

   public String getLabel()
   {
      return label;
   }

   public String getIcon()
   {
      return icon;
   }

   public long getStartPublicationTime()
   {
      return startPublicationTime;
   }

   public long getEndPublicationTime()
   {
      return endPublicationTime;
   }

   public Visibility getVisibility()
   {
      return visibility;
   }

   public String getPageRef()
   {
      return pageRef;
   }
}

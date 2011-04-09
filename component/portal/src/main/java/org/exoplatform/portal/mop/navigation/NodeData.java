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
import java.util.*;

/**
 * An immutable node data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeData implements Serializable
{

   /** . */
   final String id;

   /** . */
   final String name;

   /** . */
   final NodeState state;

   /** . */
   final String[] children;

   NodeData(String id, String name, NodeState state, String[] children)
   {
      this.id = id;
      this.name = name;
      this.state = state;
      this.children = children;
   }

   boolean hasChild(String childId)
   {
      for (int i = 0;i < children.length;i++)
      {
         if (children[i].equals(childId))
         {
            return true;
         }
      }
      return false;
   }

   NodeData(Navigation nav)
   {
      String[] children;
      List<Navigation> _children = nav.getChildren();
      if (_children == null)
      {
         children = new String[0];
      }
      else
      {
         children = new String[_children.size()];
         int index = 0;
         for (Navigation child : _children)
         {
            children[index++] = child.getObjectId();
         }
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
      NodeState state = new NodeState(
         attrs.getValue(MappedAttributes.URI),
         label,
         attrs.getValue(MappedAttributes.ICON),
         startPublicationDate != null ? startPublicationDate.getTime() : -1,
         endPublicationDate != null ? endPublicationDate.getTime() : -1,
         visibility,
         pageRef
      );

      //
      this.id = nav.getObjectId();
      this.name = nav.getName();
      this.state = state;
      this.children = children;
   }

   public Iterator<String> iterator(boolean reverse)
   {
      if (reverse)
      {
         return new Iterator<String>()
         {
            int index = children.length;
            public boolean hasNext()
            {
               return index > 0;
            }
            public String next()
            {
               if (index > 0)
               {
                  return children[--index];
               }
               else
               {
                  throw new NoSuchElementException();
               }
            }
            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }
      else
      {
         return new Iterator<String>()
         {
            int index = 0;
            public boolean hasNext()
            {
               return index < children.length;
            }
            public String next()
            {
               if (index < children.length)
               {
                  return children[index++];
               }
               else
               {
                  throw new NoSuchElementException();
               }
            }
            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public NodeState getState()
   {
      return state;
   }
}

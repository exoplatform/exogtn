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

import org.exoplatform.portal.mop.Visibility;
import org.gatein.mop.api.workspace.Navigation;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

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
   final String uri;

   /** . */
   final String label;

   /** . */
   final String icon;

   /** . */
   final Date startPublicationDate;

   /** . */
   final Date endPublicationDate;

   /** . */
   final Visibility visibility;

   /** . */
   final String pageReference;

   /** . */
   final LinkedHashMap<String, String> children;

   NodeData(Navigation nav)
   {
      LinkedHashMap<String, String> children = new LinkedHashMap<String, String>();
      for (Navigation child : nav.getChildren())
      {
         children.put(child.getName(), child.getObjectId());
      }

      //
      this.id = nav.getObjectId();
      this.name = nav.getName();
      this.uri = null;
      this.label = null;
      this.icon = null;
      this.startPublicationDate = null;
      this.endPublicationDate = null;
      this.visibility = null;
      this.pageReference = null;
      this.children = children;
   }

   NodeData(String id, String name, LinkedHashMap<String, String> children)
   {
      this.id = id;
      this.name = name;
      this.children = children;
      this.uri = null;
      this.label = null;
      this.icon = null;
      this.startPublicationDate = null;
      this.endPublicationDate = null;
      this.visibility = null;
      this.pageReference = null;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }
}

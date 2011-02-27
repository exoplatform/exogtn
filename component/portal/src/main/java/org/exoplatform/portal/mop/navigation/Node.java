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

import java.util.Date;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Node
{

   String getId();

   String getName();

   /**
    * Returns the node data.
    *
    * @return the data
    */
   Data getData();

   /**
    * A navigation whose relationships are not determined.
    */
   public interface Data
   {

      String getURI();

      String getLabel();

      String getIcon();

      long getStartPublicationTime();

      long getEndPublicationTime();

      Visibility getVisibility();

      String getPageRef();

   }

   /**
    * A navigation whose relationships are fully determined.
    */
   public interface Fragment extends Node
   {

      Node getParent();

      Iterable<? extends Node> getChildren();

      Node getChild(String childName);

      Fragment addChild(String childName);

      void removeChild(String childName);

   }

}

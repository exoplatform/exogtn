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

import java.util.LinkedHashMap;

/**
 * Represents a navigation node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Node
{

   /** . */
   final NodeData data;

   /** . */
   final Relationships relationships;

   Node(NodeData data, Relationships relationships)
   {
      this.data = data;
      this.relationships = relationships;
   }

   Node(NodeData data)
   {
      this.data = data;
      this.relationships = null;
   }

   public String getId()
   {
      return data.getId();
   }

   public String getName()
   {
      return data.getName();
   }

   public NodeData getData()
   {
      return data;
   }

   public Relationships getRelationships()
   {
      return relationships;
   }

   @Override
   public String toString()
   {
      return "Node[" + data.getName() + "]";
   }

   /**
    * A navigation whose relationships are fully determined.
    */
   public static class Relationships extends LinkedHashMap<String, Node>
   {

      Relationships(int initialCapacity)
      {
         super(initialCapacity);
      }

      Relationships()
      {
      }

      public Iterable<? extends Node> getChildren()
      {
         return values();
      }

      public Node getChild(String childName)
      {
         return get(childName);
      }
   }
}

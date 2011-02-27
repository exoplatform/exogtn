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

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeImpl implements Node
{

   /** . */
   final NodeData data;

   public NodeImpl(NodeData data)
   {
      this.data = data;
   }

   public String getId()
   {
      return data.getId();
   }

   public String getName()
   {
      return data.getName();
   }

   public Data getData()
   {
      return data;
   }

   @Override
   public String toString()
   {
      return "Node[" + data.getName() + "]";
   }

   static class FragmentImpl extends NodeImpl implements Fragment
   {

      /** . */
      private final Map<String, NodeImpl> children;

      FragmentImpl(NodeData data, Map<String, NodeImpl> children)
      {
         super(data);

         //
         this.children = children;
      }

      public Node getParent()
      {
         throw new UnsupportedOperationException();
      }

      public Iterable<? extends Node> getChildren()
      {
         return children.values();
      }

      public Node getChild(String childName)
      {
         return children.get(childName);
      }

      public Fragment addChild(String childName)
      {
         throw new UnsupportedOperationException();
      }

      public void removeChild(String childName)
      {
         throw new UnsupportedOperationException();
      }
   }
   
}

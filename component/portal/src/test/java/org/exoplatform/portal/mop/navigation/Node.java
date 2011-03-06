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

import java.util.Collection;

/**
 * Represents a navigation node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Node
{

   /** . */
   public static final NodeModel<Node> MODEL = new NodeModel<Node>()
   {
      public NodeContext<Node> getContext(Node node)
      {
         return node.context;
      }

      public Node create(NodeContext<Node> context)
      {
         return new Node(context);
      }
   };

   /** . */
   final NodeContext<Node> context;

   Node(NodeContext<Node> context)
   {
      this.context = context;
   }

   public String getId()
   {
      return context.getId();
   }

   public String getName()
   {
      return context.getName();
   }

   public NodeContext getContext()
   {
      return context;
   }

   public Collection<Node> getChildren()
   {
      return context.getChildren();
   }

   public Node getChild(String childName)
   {
      return context.getChild(childName);
   }

   public Node addChild(String childName)
   {
      return context.addChild(MODEL, childName);
   }

   @Override
   public String toString()
   {
      return "Node[" + context.getName() + "]";
   }
}

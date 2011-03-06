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
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeContextModel<N> implements NodeContext<N>
{

   /** The original node context data. */
   final NodeData data;

   /** The new state if any. */
   private NodeState state;

   /** . */
   N parent;

   /** . */
   LinkedHashMap<String, N> children;

   NodeContextModel(NodeData data)
   {
      this.data = data;
      this.children = null;
   }

   public String getId()
   {
      return data.getId();
   }

   public String getName()
   {
      return data.getName();
   }

   public int getChildrenCount()
   {
      return data.getChildrenCount();
   }

   public NodeState getState()
   {
      return state != null ? state : data.getState();
   }

   public void setState(NodeState state)
   {
      this.state = state;
   }

   public N getParent()
   {
      return parent;
   }

   public N getChild(String childName)
   {
      if (children != null)
      {
         return children.get(childName);
      }
      else
      {
         return null;
      }
   }

   public Collection<N> getChildren()
   {
      return children != null ? children.values() : null;
   }
}

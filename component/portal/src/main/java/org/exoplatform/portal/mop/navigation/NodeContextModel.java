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

   /** . */
   final N node;

   /** The original node context data. */
   final NodeData data;

   /** ; */
   final String name;

   /** The new state if any. */
   private NodeState state;

   /** . */
   N parent;

   /** . */
   LinkedHashMap<String, N> children;

   NodeContextModel(NodeModel<N> model, NodeData data)
   {
      this.node = model.create(this);
      this.data = data;
      this.name = data.getName();
      this.state = data.getState();
      this.children = null;
   }

   NodeContextModel(NodeModel<N> model, String name, NodeState state)
   {
      this.node = model.create(this);
      this.data = null;
      this.name = name;
      this.state = state;
      this.children = new LinkedHashMap<String, N>();
   }

   public String getId()
   {
      return data != null ? data.getId() : null;
   }

   public String getName()
   {
      return name;
   }

   public int getChildrenCount()
   {
      return data.getChildrenCount();
   }

   public NodeState getState()
   {
      return state;
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

   public N addChild(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }
      else if (children.containsKey(name))
      {
         throw new IllegalArgumentException();
      }


      //
      NodeContextModel<N> childCtx = new NodeContextModel<N>(model, name, new NodeState.Builder().capture());
      children.put(name, childCtx.node);
      childCtx.parent = node;

      //
      return childCtx.node;
   }

   public void addChild(NodeModel<N> model, N child)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }
      else if (children.containsKey(name))
      {
         throw new IllegalArgumentException();
      }

      //
      NodeContextModel<N> childCtx = (NodeContextModel<N>)model.getContext(child);
      if (childCtx.parent != null)
      {
         if (childCtx.parent != node)
         {
            throw new UnsupportedOperationException("not supported");
         }
         children.remove(childCtx.name);
         children.put(childCtx.name, child);
      }
      else
      {
         childCtx.parent = node;
         children.put(childCtx.name, child);
      }
   }

   public boolean removeChild(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }

      //
      N child = children.remove(name);
      if (child != null)
      {
         NodeContextModel<N> childCtx = (NodeContextModel<N>)model.getContext(child);
         childCtx.parent = null;
         return true;
      }
      else
      {
         return false;
      }
   }
}

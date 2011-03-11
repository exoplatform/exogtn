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

import org.exoplatform.portal.mop.navigation2.ListTree;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * The context of a node.
 */
public final class NodeContext<N> extends ListTree<NodeContext<N>, N>
{

   /** . */
   final N node;

   /** node data representing persistent state. */
   NodeData data;

   /** The new state if any. */
   NodeState state;

   NodeContext(NodeModel<N> model, NodeData data, boolean hidden)
   {
      super(data.getName(), hidden);

      //
      this.node = model.create(this);
      this.data = data;
      this.state = data.getState();
   }

   NodeContext(NodeModel<N> model, String name, NodeState state, boolean hidden)
   {
      super(name, hidden);

      //
      this.node = model.create(this);
      this.data = null;
      this.state = state;
   }

   public N getElement()
   {
      return node;
   }

   public String getId()
   {
      return data != null ? data.getId() : null;
   }

   public void setName(String name)
   {
      NodeContext<N> parent = getParent();
      if (parent == null)
      {
         throw new IllegalStateException("Cannot rename a node when its parent is not visible");
      }
      else
      {
         parent.rename(getName(), name);
      }
   }

   public int getNodeCount()
   {
      if (hasTrees())
      {
         int count = 0;
         for (N node : this)
         {
            count++;
         }
         return count;
      }
      else
      {
         return data.getChildrenCount();
      }
   }

   public NodeState getState()
   {
      if (state != null)
      {
         return state;
      }
      else if (data != null)
      {
         return data.getState();
      }
      else
      {
         return null;
      }
   }

   public void setState(NodeState state)
   {
      this.state = state;
   }

   public N getParentNode()
   {
      NodeContext<N> parent = getParent();
      return parent != null ? parent.node : null;
   }

   public N getNode(String name) throws NullPointerException
   {
      NodeContext<N> child = get(name);
      return child != null ? child.node: null;
   }

   public N getNode(int index)
   {
      NodeContext<N> child = get(index);
      return child != null ? child.node: null;
   }

   /** . */
   private Collection<N> nodes;

   public Collection<N> getNodes()
   {
      if (hasTrees())
      {
         if (nodes == null)
         {
            nodes = new AbstractCollection<N>()
            {
               public Iterator<N> iterator()
               {
                  return NodeContext.this.iterator();
               }
               public int size()
               {
                  return getNodeCount();
               }
            };
         }
         return nodes;
      }
      else
      {
         return null;
      }
   }

   public N addNode(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }

      //
      NodeContext<N> nodeContext = new NodeContext<N>(model, name, new NodeState.Builder().capture(), false);
      nodeContext.setContexts(Collections.<NodeContext<N>>emptyList());
      insert(null, nodeContext);
      return nodeContext.node;
   }

   public void addNode(NodeModel<N> model, Integer index, N child)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }

      //
      NodeContext<N> nodeContext = model.getContext(child);
      insert(index, nodeContext);
   }

   public boolean removeNode(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }

      //
      return remove(name) != null;
   }

   NodeContext<N> getRoot()
   {
      NodeContext<N> root = this;
      while (root.getParent() != null)
      {
         root = root.getParent();
      }
      return root;
   }

   Iterable<NodeContext<N>> getContexts()
   {
      return getTrees();
   }

   void setContexts(Iterable<NodeContext<N>> contexts)
   {
      setTrees(contexts);
   }

}

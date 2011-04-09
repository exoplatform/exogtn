/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.exoplatform.portal.tree.list.ListTree;

import java.util.*;

/**
 * The context of a node.
 */
public class NodeContext<N> extends ListTree<NodeContext<N>, N>
{


   /** . */
   final TreeContext<N> tree;

   /** . */
   final N node;

   /** node data representing persistent state. */
   NodeData data;

   /** The new state if any. */
   NodeState state;

   /** . */
   private boolean hidden;

   /** . */
   private int hiddenCount;

   NodeContext(TreeContext<N> tree, NodeModel<N> model, NodeData data)
   {
      super(data.getName());

      //
      this.tree = tree;
      this.node = model.create(this);
      this.data = data;
      this.state = data.getState();
      this.hidden = false;
      this.hiddenCount = 0;
   }

   private NodeContext(TreeContext<N> tree, NodeModel<N> model, String name, NodeState state)
   {
      super(name);

      //
      this.tree = tree;
      this.node = model.create(this);
      this.data = null;
      this.state = state;
      this.hidden = false;
      this.hiddenCount = 0;
   }

   public boolean isHidden()
   {
      return hidden;
   }

   public void afterInsert(NodeContext<N> tree)
   {
      if (tree.hidden)
      {
         hiddenCount++;
      }
   }

   public void afterRemove(NodeContext<N> tree)
   {
      if (tree.hidden)
      {
         hiddenCount--;
      }
   }

   public final void setHidden(boolean hidden)
   {
      if (this.hidden != hidden)
      {
         NodeContext<N> parent = getParent();
         if (parent != null)
         {
            if (hidden)
            {
               parent.hiddenCount++;
            }
            else
            {
               parent.hiddenCount--;
            }
         }
         this.hidden = hidden;
      }
   }

   /**
    * Applies a filter recursively.
    *
    * @param filter the filter to apply
    */
   public void filter(NodeFilter filter)
   {
      filter(0, filter);
   }

   private void filter(int depth, NodeFilter filter)
   {
      boolean accept = filter.accept(depth, getId(), getName(), state);
      setHidden(!accept);
      if (hasTrees())
      {
         for (NodeContext<N> node : getTrees())
         {
            node.filter(depth + 1, filter);
         }
      }
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

   /**
    * Returns the total number of nodes.
    *
    * @return the total number of nodes
    */
   public int getNodeSize()
   {
      if (hasTrees())
      {
         return getSize();
      }
      else
      {
         return data.children.length;
      }
   }

   /**
    * Returns the node count defined by:
    * <ul>
    *    <li>when the node has a children relationship, the number of non hidden nodes</li>
    *    <li>when the node has not a children relationship, the total number of nodes</li>
    * </ul>
    *
    * @return the node count
    */
   public int getNodeCount()
   {
      if (hasTrees())
      {
         return getSize() - hiddenCount;
      }
      else
      {
         return data.children.length;
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
      return child != null && !child.hidden ? child.node: null;
   }

   public N getNode(int index)
   {
      if (index < 0)
      {
         throw new IndexOutOfBoundsException("Index " + index + " cannot be negative");
      }
      NodeContext<N> context = getFirst();
      while (context != null && (context.hidden || index-- > 0))
      {
         context = context.getNext();
      }
      if (context == null)
      {
         throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
      }
      else
      {
         return context.node;
      }
   }

   public final Iterator<N> iterator()
   {
      return new Iterator<N>()
      {
         NodeContext<N> next = getFirst();
         {
            while (next != null && next.isHidden())
            {
               next = next.getNext();
            }
         }
         public boolean hasNext()
         {
            return next != null;
         }
         public N next()
         {
            if (next != null)
            {
               NodeContext<N> tmp = next;
               do
               {
                  next = next.getNext();
               }
               while (next != null && next.isHidden());
               return tmp.getElement();
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

   public N addNode(NodeModel<N> model, Integer index, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }

      //
      NodeContext<N> nodeContext = new NodeContext<N>(tree, model, name, new NodeState.Builder().capture());
      nodeContext.setContexts(Collections.<NodeContext<N>>emptyList());
      addNode(index, nodeContext);
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

      //
      addNode(index, nodeContext);
   }

   private void addNode(Integer index, NodeContext<N> child)
   {
      if (index == null)
      {
         NodeContext<N> before = getLast();
         while (before != null && before.isHidden())
         {
            before = before.getPrevious();
         }
         if (before == null)
         {
            insertAt(0, child);
         }
         else
         {
            before.insertAfter(child);
         }
      }
      else if (index == 0)
      {
         insertAt(0, child);
      }
      else
      {
         NodeContext<N> before = getFirst();
         while (index > 1)
         {
            before = before.getNext();
            if (!before.isHidden())
            {
               index--;
            }
         }
         before.insertAfter(child);
      }
   }

   public boolean removeNode(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }

      //
      NodeContext<N> node = get(name);
      if (node.hidden)
      {
         return false;
      }
      else
      {
         node.remove();
         return true;
      }
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

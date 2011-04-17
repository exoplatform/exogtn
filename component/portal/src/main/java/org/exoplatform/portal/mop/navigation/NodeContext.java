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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The context of a node.
 */
public class NodeContext<N> extends ListTree<NodeContext<N>>
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

   NodeContext(TreeContext<N> tree, NodeData data)
   {
      super(data.getName());

      //
      this.tree = tree;
      this.node = tree.model.create(this);
      this.data = data;
      this.state = data.getState();
      this.hidden = false;
      this.hiddenCount = 0;
   }

   private NodeContext(TreeContext<N> tree, String name, NodeState state)
   {
      super(name);

      //
      this.tree = tree;
      this.node = tree.model.create(this);
      this.data = null;
      this.state = state;
      this.hidden = false;
      this.hiddenCount = 0;
   }

   /**
    * Returns true if the context is currently hidden.
    *
    * @return the hidden value
    */
   public boolean isHidden()
   {
      return hidden;
   }

   /**
    * Updates the hiddent value.
    *
    * @param hidden the hidden value
    */
   public void setHidden(boolean hidden)
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
    * Applies a filter recursively, the filter will update the hiddent status of the
    * fragment.
    *
    * @param filter the filter to apply
    */
   public void filter(NodeFilter filter)
   {
      doFilter(0, filter);
   }

   private void doFilter(int depth, NodeFilter filter)
   {
      boolean accept = filter.accept(depth, getId(), getName(), state);
      setHidden(!accept);
      if (hasTrees())
      {
         for (NodeContext<N> node : getTrees())
         {
            node.doFilter(depth + 1, filter);
         }
      }
   }

   /**
    * Returns the associated node with this context
    *
    * @return the node
    */
   public N getNode()
   {
      return node;
   }

   /**
    * Reutrns the context id or null if the context is not associated with a persistent navigation node.
    *
    * @return the id
    */
   public String getId()
   {
      return data != null ? data.getId() : null;
   }

   public int getIndex()
   {
      int count = 0;
      for (NodeContext<N> current = getPrevious();current != null;current = current.getPrevious())
      {
         count++;
      }
      return count;
   }

   /**
    * Rename this context.
    *
    * @param name the new name
    * @throws NullPointerException if the name is null
    * @throws IllegalStateException if the parent is null
    * @throws IllegalArgumentException if the parent already have a child with the specified name
    */
   public void setName(String name) throws NullPointerException, IllegalStateException, IllegalArgumentException
   {
      NodeContext<N> parent = getParent();
      if (parent == null)
      {
         throw new IllegalStateException("Cannot rename a node when its parent is not visible");
      }
      else
      {
         if (parent.rename(getName(), name))
         {
            tree.addChange(new Change.Rename(this, name));
         }
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
               return tmp.getNode();
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

   /**
    * Add a child node at the specified index with the specified name. If the index argument
    * is null then the node is added at the last position among the children otherwise
    * the node is added at the specified index.
    *
    * @param index the index
    * @param name the node name
    * @return the created node
    * @throws NullPointerException if the model or the name is null
    * @throws IndexOutOfBoundsException if the index is negative or greater than the children size
    */
   public N addNode(Integer index, String name) throws NullPointerException, IndexOutOfBoundsException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      NodeContext<N> nodeContext = new NodeContext<N>(tree, name, new NodeState.Builder().capture());
      nodeContext.setContexts(Collections.<NodeContext<N>>emptyList());
      addNode(index, nodeContext);
      return nodeContext.node;
   }

   /**
    * Move a node as a child node of this node at the specified index. If the index argument
    * is null then the node is added at the last position among the children otherwise
    * the node is added at the specified index.
    *
    * @param index the index
    * @param node the node to move
    * @throws NullPointerException if the model or the node is null
    * @throws IndexOutOfBoundsException if the index is negative or greater than the children size
    */
   public void addNode(Integer index, N node) throws NullPointerException, IndexOutOfBoundsException
   {
      if (node == null)
      {
         throw new NullPointerException("No null node argument accepted");
      }

      //
      NodeContext<N> nodeContext = tree.model.getContext(node);

      //
      addNode(index, nodeContext);
   }

   private void addNode(final Integer index, NodeContext<N> child)
   {
      NodeContext<N> previousParent = child.getParent();

      //
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
      else if (index < 0)
      {
         throw new IndexOutOfBoundsException("No negative index accepted");
      }
      else if (index == 0)
      {
         insertAt(0, child);
      }
      else
      {
         NodeContext<N> before = getFirst();
         if (before == null)
         {
            throw new IndexOutOfBoundsException("Index " + index + " is greater than 0");
         }
         for (int count = index;count > 1;count -= before.isHidden() ? 0 : 1)
         {
            before = before.getNext();
            if (before == null)
            {
               throw new IndexOutOfBoundsException("Index " + index + " is greater than the number of children " + (index - count));
            }
         }
         before.insertAfter(child);
      }

      //
      if (previousParent != null)
      {
         tree.addChange(new Change.Move(previousParent, this, child.getPrevious(), child));
      }
      else
      {
         tree.addChange(new Change.Add(this, child.getPrevious(), child, child.getName()));
      }
   }

   /**
    * Remove a specified context.
    *
    * @param name the name of the context to remove
    * @return true if the context was removed
    * @throws NullPointerException if the name argument is null
    * @throws IllegalArgumentException if the named context does not exist
    */
   public boolean removeNode(String name) throws NullPointerException, IllegalArgumentException
   {
      NodeContext<N> node = get(name);
      if (node == null)
      {
         throw new IllegalArgumentException("Cannot remove non existent " + name + " child");
      }

      //
      if (node.hidden)
      {
         return false;
      }
      else
      {
         node.remove();

         //
         tree.addChange(new Change.Remove(this, node));

         //
         return true;
      }
   }

   Iterable<NodeContext<N>> getContexts()
   {
      return getTrees();
   }

   void setContexts(Iterable<NodeContext<N>> contexts)
   {
      setTrees(contexts);
   }

   protected void afterInsert(NodeContext<N> tree)
   {
      if (tree.hidden)
      {
         hiddenCount++;
      }
   }

   protected void afterRemove(NodeContext<N> tree)
   {
      if (tree.hidden)
      {
         hiddenCount--;
      }
   }
}

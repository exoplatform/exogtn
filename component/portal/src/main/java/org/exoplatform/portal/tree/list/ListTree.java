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

package org.exoplatform.portal.tree.list;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>A tree structure where the children in which each child has a name and an index. The name
 * and index are unique for a given tree node. A node maintains a map of its children keyed by their names.</p>
 *
 * <p>The children of a node is a linked list and can be iterated with an iterator
 * or thanks to the {@link #getFirst()}, {@link #getLast()}, {@link #getNext()} and {@link #getPrevious()}
 * methods.</p>
 *
 * <p>Accessing a child by its name is done in constant time because of the map maintained by the
 * the parent. Accessing a child by its index is not provided as it would be done in a linear time, instead
 * the relevant method to navigate the child list are provided.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ListTree<T extends ListTree<T>>
{

   /** . */
   private String name;

   /** . */
   private T parent;

   /** . */
   private Map<String, T> map;

   /** . */
   private T next;

   /** . */
   private T previous;

   /** . */
   private T head;

   /** . */
   private T tail;

   public ListTree(String name)
   {
      if (name == null)
      {
         throw new NullPointerException("No null name is accepted");
      }

      //
      this.name = name;
      this.next = null;
      this.previous = null;
      this.head = null;
      this.tail = null;
   }

   public final int getSize()
   {
      return map == null ? -1 : map.size();
   }

   public final T getNext()
   {
      return next;
   }

   public final T getPrevious()
   {
      return previous;
   }

   public final String getName()
   {
      return name;
   }

   public final T getParent()
   {
      return parent;
   }

   public final boolean rename(String from, String to) throws NullPointerException, IllegalStateException, IllegalArgumentException
   {
      if (from == null)
      {
         throw new NullPointerException("No null from name accepted");
      }
      if (to == null)
      {
         throw new NullPointerException("No null to name accepted");
      }
      if (map == null)
      {
         throw new IllegalStateException();
      }

      //
      if (!from.equals(to))
      {
         if (map.containsKey(to))
         {
            throw new IllegalArgumentException("the node " + to + " already exist");
         }
         T child = map.remove(from);
         if (child == null)
         {
            throw new IllegalArgumentException("the node " + from + " + does not exist");
         }
         child.name = to;
         map.put(to, child);
         return true;
      }
      else
      {
         return false;
      }
   }

   public final boolean contains(String name) throws NullPointerException, IllegalStateException
   {
      return get(name) != null;
   }

   public final T get(String name) throws NullPointerException, IllegalStateException
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (map == null)
      {
         throw new IllegalStateException("No children relationship");
      }

      //
      return map.get(name);
   }

   public final T getFirst()
   {
      if (map == null)
      {
         throw new IllegalStateException("no children");
      }
      return head;
   }

   public final T getLast()
   {
      if (map == null)
      {
         throw new IllegalStateException("no children");
      }
      return tail;
   }

   public final T get(int index) throws IllegalStateException, IndexOutOfBoundsException
   {
      if (map == null)
      {
         throw new IllegalStateException("no children");
      }
      if (index < 0)
      {
         throw new IndexOutOfBoundsException("No negative index allowed");
      }

      //
      T current = head;
      while (true)
      {
         if (current == null)
         {
            throw new IndexOutOfBoundsException("index " + index + " is greater than the children size");
         }
         if (index == 0)
         {
            break;
         }
         else
         {
            current = current.next;
            index--;
         }
      }
      return current;
   }

   public final boolean hasTrees()
   {
      return map != null;
   }

   /**
    * Insert the specified tree.
    *
    * @param index the index
    * @param tree the tree
    * @throws NullPointerException if the context is null
    * @throws IllegalStateException if the children relationship does not exist
    * @throws IllegalArgumentException if an existing child with the same name already exist
    * @throws IndexOutOfBoundsException if the index is negative or is greater than the children size
    */
   public final void insertAt(Integer index, T tree) throws NullPointerException, IllegalStateException, IllegalArgumentException, IndexOutOfBoundsException
   {
      if (tree == null)
      {
         throw new NullPointerException("No null tree accepted");
      }
      if (map == null)
      {
         throw new IllegalStateException("No trees relationship");
      }
      if (index != null && index < 0)
      {
         throw new IndexOutOfBoundsException("No negative index permitted");
      }

      //
      if (index != null)
      {
         T a = head;
         if (index == 0)
         {
            insertFirst(tree);
         }
         else
         {
            while (index > 0)
            {
               if (a == null)
               {
                  throw new IndexOutOfBoundsException();
               }
               index--;
               a = a.next;
            }

            //
            if (a == null)
            {
               insertLast(tree);
            }
            else if (a != tree)
            {
               a.insertBefore(tree);
            }
         }
      }
      else
      {
         T a = tail;
         if (a == null)
         {
            insertFirst(tree);
         }
         else if (a != tree)
         {
            a.insertAfter(tree);
         }
      }
   }

   private void insertLast(T context)
   {
      if (tail == null)
      {
         insertFirst(context);
      }
      else
      {
         tail.insertAfter(context);
      }
   }

   /**
    * Insert the specified context at the first position among the children of this context.
    *
    * @param tree the content to insert
    */
   private void insertFirst(T tree)
   {
      T existing = map.get(tree.name);
      if (existing != null && existing != tree)
      {
         throw new IllegalArgumentException("Tree " + tree.name + " already in the map");
      }
      if (head == null)
      {
         if (tree.parent != null)
         {
            tree.remove();
         }
         head = tail = tree;
         if (map == Collections.EMPTY_MAP)
         {
            map = new HashMap<String, T>();
         }
         map.put(tree.name, tree);
         tree.parent = (T)this;
         afterInsert(tree);
      }
      else
      {
         head.insertBefore(tree);
      }
   }

   public final void insertAfter(T tree)
   {
      if (this != tree)
      {
         T existing = parent.map.get(tree.name);
         if (existing != null && existing != tree)
         {
            throw new IllegalArgumentException("Tree " + tree.name + " already in the map");
         }
         if (tree.parent != null)
         {
            tree.remove();
         }
         tree.previous = (T)this;
         tree.next = next;
         if (next == null)
         {
            parent.tail = tree;
         }
         else
         {
            next.previous = tree;
         }
         next = tree;
         if (parent.map == Collections.EMPTY_MAP)
         {
            parent.map = new HashMap<String, T>();
         }
         parent.map.put(tree.name, tree);
         tree.parent = parent;
         afterInsert(tree);
      }
   }

   public final void insertBefore(T tree)
   {
      if (this != tree)
      {
         T existing = parent.map.get(tree.name);
         if (existing != null && existing != tree)
         {
            throw new IllegalArgumentException("Tree " + tree.name + " already in the map");
         }
         if (tree.parent != null)
         {
            tree.remove();
         }
         tree.previous = previous;
         tree.next = (T)this;
         if (previous == null)
         {
            parent.head = tree;
         }
         else
         {
            previous.next = tree;
         }
         previous = tree;
         if (parent.map == Collections.EMPTY_MAP)
         {
            parent.map = new HashMap<String, T>();
         }
         parent.map.put(tree.name, tree);
         tree.parent = parent;
         afterInsert(tree);
      }
   }

   public final void remove()
   {
      if (previous == null)
      {
         parent.head = next;
      }
      else
      {
         previous.next = next;
      }
      if (next == null)
      {
         parent.tail = previous;
      }
      else
      {
         next.previous = previous;
      }
      parent.map.remove(name);
      T _parent = parent;
      parent = null;
      previous = null;
      next = null;
      _parent.afterRemove((T)this);
   }

   public final ListIterator<T> listIterator()
   {
      if (map == null)
      {
         return null;
      }
      return new ListIterator<T>()
      {
         T next = head;
         T current = null;
         T previous = null;
         int index = 0;

         public boolean hasNext()
         {
            return next != null;
         }

         public T next()
         {
            if (next != null)
            {
               current = next;

               //
               previous = next;
               next = next.next;
               index++;
               return current;
            }
            else
            {
               throw new NoSuchElementException();
            }
         }

         public boolean hasPrevious()
         {
            return previous != null;
         }

         public T previous()
         {
            if (previous != null)
            {
               current = previous;

               //
               next = previous;
               previous = previous.previous;
               index--;
               return current;
            }
            else
            {
               throw new NoSuchElementException();
            }
         }

         public int nextIndex()
         {
            return index;
         }

         public int previousIndex()
         {
            return index - 1;
         }

         public void remove()
         {
            if (current == null)
            {
               throw new IllegalStateException("no element to remove");
            }
            if (current == previous)
            {
               index--;
            }
            next = current.next;
            previous = current.previous;
            current.remove();
            current = null;
         }

         public void set(T tree)
         {
            throw new UnsupportedOperationException();
         }

         public void add(T tree)
         {
            if (previous == null)
            {
               insertFirst(tree);
            }
            else
            {
               previous.insertAfter(tree);
            }
            index++;
            previous = tree;
            next = tree.next;
         }
      };
   }

   public final Iterable<T> getTrees()
   {
      if (map != null)
      {
         return new Iterable<T>()
         {
            public Iterator<T> iterator()
            {
               return listIterator();
            }
         };
      }
      else
      {
         return null;
      }
   }

   public final void setTrees(Iterable<T> children)
   {
      if (children == null)
      {
         if (map == null)
         {
            throw new IllegalStateException();
         }
         else
         {
            while (head != null)
            {
               head.remove();
            }
            this.map = null;
         }
      }
      else
      {
         if (map == null)
         {
            @SuppressWarnings("unchecked") Map<String, T> map = Collections.EMPTY_MAP;
            this.map = map;
            for (T child : children)
            {
               insertLast(child);
            }
         }
         else
         {
            throw new IllegalStateException();
         }
      }
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void afterInsert(T tree)
   {
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void afterRemove(T tree)
   {
   }

   public String toString()
   {
      return getClass().getSimpleName() + "[name=" + getName() + "]";
   }
}


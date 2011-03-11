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

package org.exoplatform.portal.mop.navigation2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ListTree<T extends ListTree<T, E>, E> implements Iterable<E>
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

   /** . */
   private boolean hidden;

   public ListTree(String name, boolean hidden)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }

      //
      this.name = name;
      this.next = null;
      this.previous = null;
      this.head = null;
      this.tail = null;
      this.hidden = hidden;
   }

   public boolean isHidden()
   {
      return hidden;
   }

   public void setHidden(boolean hidden)
   {
      this.hidden = hidden;
   }

   public abstract E getElement();

   public final String getName()
   {
      return name;
   }

   public final T getParent()
   {
      return parent;
   }

   public final void rename(String from, String to) throws NullPointerException, IllegalStateException, IllegalArgumentException
   {
      if (from == null)
      {
         throw new NullPointerException();
      }
      if (to == null)
      {
         throw new NullPointerException();
      }
      if (map == null)
      {
         throw new IllegalStateException();
      }

      //
      if (map.containsKey(to))
      {
         throw new IllegalArgumentException("the node " + to + " already exist");
      }
      T child = map.remove(from);
      if (child == null)
      {
         throw new IllegalArgumentException("the node " + from + " + does not exist");
      }
      if (child.hidden)
      {
         throw new IllegalArgumentException("the node " + from + " + is hidden");
      }
      child.name = to;
      map.put(to, child);
   }

   public final T remove(String name) throws NullPointerException, IllegalStateException
   {
      T child = get(name);
      if (child != null)
      {
         child.remove();
         return child;
      }
      else
      {
         return null;
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
      T child = map.get(name);
      return child == null || child.hidden ? null : child;
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
         while (current != null && current.hidden)
         {
            current = current.next;
         }
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
    * @throws IllegalArgumentException if an existing child with the same name already exist or if the context is hidden
    * @throws IndexOutOfBoundsException if the index is negative or is greater than the children size
    */
   public final void insert(Integer index, T tree) throws NullPointerException, IllegalStateException, IllegalArgumentException, IndexOutOfBoundsException
   {
      if (tree == null)
      {
         throw new NullPointerException("No null tree accepted");
      }
      if (tree.hidden)
      {
         throw new IllegalArgumentException("Cannot insert hidden tree");
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
      T existing = map.get(tree.name);
      if (existing != null && existing != tree)
      {
         throw new IllegalArgumentException("Tree " + tree.name + " already in the map");
      }

      //
      if (index != null)
      {
         T a = head;
         if (index == 0)
         {
            if (tree.parent != null)
            {
               tree.remove();
            }
            insertFirst(tree);
         }
         else
         {
            while (index > 0)
            {
               while (a != null && a.hidden)
               {
                  a = a.next;
               }
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
               if (tree.parent != null)
               {
                  tree.remove();
               }
               insertLast(tree);
            }
            else if (a != tree)
            {
               if (tree.parent != null)
               {
                  tree.remove();
               }
               a.insertBefore(tree);
            }
         }
      }
      else
      {
         T a = tail;
         while (a != null && a.hidden)
         {
            a = a.previous;
         }
         if (a == null)
         {
            if (tree.parent != null)
            {
               tree.remove();
            }
            insertFirst(tree);
         }
         else if (a != tree)
         {
            if (tree.parent != null)
            {
               tree.remove();
            }
            a.insertAfter(tree);
         }
      }
   }

   public final Iterator<E> iterator()
   {
      return new Iterator<E>()
      {
         T next = head;
         {
            while (next != null && next.hidden)
            {
               next = next.next;
            }
         }
         public boolean hasNext()
         {
            return next != null;
         }
         public E next()
         {
            if (next != null)
            {
               T tmp = next;
               do
               {
                  next = next.next;
               }
               while (next != null && next.hidden);
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

   protected final Iterable<T> getTrees()
   {
      if (map != null)
      {
         return new Iterable<T>()
         {
            public Iterator<T> iterator()
            {
               return new Iterator<T>()
               {
                  T next = head;
                  public boolean hasNext()
                  {
                     return next != null;
                  }
                  public T next()
                  {
                     if (next != null)
                     {
                        T tmp = next;
                        next = next.next;
                        return tmp;
                     }
                     else
                     {
                        throw new UnsupportedOperationException();
                     }
                  }
                  public void remove()
                  {
                     throw new UnsupportedOperationException();
                  }
               };
            }
         };
      }
      else
      {
         return null;
      }
   }

   protected final void setTrees(Iterable<T> children)
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
            this.map = new HashMap<String, T>();
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

   private void insertAfter(T context)
   {
      context.previous = (T)this;
      context.next = next;
      if (next == null)
      {
         parent.tail = context;
      }
      else
      {
         next.previous = context;
      }
      next = context;
      parent.map.put(context.name, context);
      context.parent = parent;
   }

   private void insertBefore(T context)
   {
      context.previous = previous;
      context.next = (T)this;
      if (previous == null)
      {
         parent.head = context;
      }
      else
      {
         previous.next = context;
      }
      previous = context;
      parent.map.put(context.name, context);
      context.parent = parent;
   }

   /**
    * Insert the specified context at the first position among the children of this context.
    *
    * @param context the content to insert
    */
   private void insertFirst(T context)
   {
      if (head == null)
      {
         head = tail = context;
         map.put(context.name, context);
         context.parent = (T)this;
      }
      else
      {
         head.insertBefore(context);
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

   private void remove()
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
      parent = null;
   }
}

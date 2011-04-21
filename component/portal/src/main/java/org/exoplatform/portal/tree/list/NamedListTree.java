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

package org.exoplatform.portal.tree.list;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NamedListTree<N extends NamedListTree<N>> extends ListTree<N>
{

   /** . */
   private String name;

   /** . */
   Map<String, N> map;

   public NamedListTree(String name)
   {
      if (name == null)
      {
         throw new NullPointerException("No null name is accepted");
      }

      //
      this.name = name;
      this.map = null;
   }

   public final boolean contains(String name) throws NullPointerException, IllegalStateException
   {
      return get(name) != null;
   }

   public final N get(String name) throws NullPointerException, IllegalStateException
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

   public final String getName()
   {
      return name;
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
         N child = map.remove(from);
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

   public final Iterable<N> getTrees()
   {
      if (map != null)
      {
         return new Iterable<N>()
         {
            public Iterator<N> iterator()
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

   @Override
   protected void beforeInsert(N tree)
   {
      if (map == null)
      {
         throw new IllegalStateException("No children relationship");
      }
      N existing = map.get(tree.name);
      if (existing != null && existing != tree)
      {
         throw new IllegalArgumentException("Tree " + tree.name + " already in the map");
      }
   }

   @Override
   protected void afterInsert(N tree)
   {
      if (map == Collections.EMPTY_MAP)
      {
         map = new HashMap<String, N>();
      }
      map.put(tree.name, tree);
   }

   @Override
   protected void beforeRemove(N tree)
   {
      if (map == null)
      {
         throw new IllegalStateException("No children relationship");
      }
      map.remove(tree.name);
   }

   @Override
   protected void afterRemove(N tree)
   {
   }

   public final boolean hasTrees()
   {
      return map != null;
   }

   public final void setTrees(Iterable<N> children)
   {
      if (children == null)
      {
         if (map == null)
         {
            throw new IllegalStateException();
         }
         else
         {
            while (getFirst() != null)
            {
               getFirst().remove();
            }
            this.map = null;
         }
      }
      else
      {
         if (map == null)
         {
            @SuppressWarnings("unchecked") Map<String, N> map = Collections.EMPTY_MAP;
            this.map = map;
            for (N child : children)
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

   public String toString()
   {
      return getClass().getSimpleName() + "[name=" + getName() + "]";
   }

}

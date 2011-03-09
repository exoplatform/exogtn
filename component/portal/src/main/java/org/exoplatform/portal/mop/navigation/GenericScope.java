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

/**
* A flexible scope implementation.
*/
public class GenericScope implements Scope
{

   /** . */
   private final Visitor visitor;

   /**
    * @param height the max height of the pruned tree
    * @see #GenericScope(int, NodeFilter)
    */
   public GenericScope(int height)
   {
      this(height, null);
   }

   /**
    * Creates a new navigation scope. When the height is positive or null, the tree will be pruned to the specified
    * height, when the height is negative  no pruning will occur.
    *
    * @param height the max height of the pruned tree
    * @param filter the filter
    */
   public GenericScope(final int height, final NodeFilter filter)
   {
      this.visitor = new Visitor()
      {
         public VisitMode visit(int depth, String id, String name, NodeState state)
         {
            if (filter == null || filter.accept(depth, id, name, state))
            {
               if (height < 0 || depth < height)
               {
                  return VisitMode.ALL_CHILDREN;
               }
               else if (depth == height)
               {
                  return VisitMode.NO_CHILDREN;
               }
               else
               {
                  return VisitMode.SKIP;
               }
            }
            else
            {
               return VisitMode.SKIP;
            }
         }
      };
   }

   public Visitor get()
   {
      return visitor;
   }
}

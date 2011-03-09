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

import org.exoplatform.portal.mop.Visibility;

import java.util.EnumSet;

/**
 * A flexible filter implementation.
 */
public class ScopeImpl implements Scope
{

   /** . */
   private final Visitor visitor;

   /**
    * Creates a new navigation scope with no filtering
    *
    * @param height the max height of the pruned tree
    * @throws IllegalArgumentException if the height is negative
    */
   public ScopeImpl(int height)
   {
      this(height, (EnumSet<Visibility>)null);
   }

   public ScopeImpl(int height, Visibility first)
   {
      this(height, EnumSet.of(first));
   }

   public ScopeImpl(int height, Visibility first, Visibility... other)
   {
      this(height, EnumSet.of(first, other));
   }

   /**
    * Creates a new navigation scope.
    *
    * @param height the max height of the pruned tree
    * @param filter the filter
    * @throws IllegalArgumentException if the height is negative
    */
   public ScopeImpl(final int height, final EnumSet<Visibility> filter) throws IllegalArgumentException
   {
      this.visitor = new Visitor()
      {
         public VisitMode visit(int depth, String id, String name, NodeState state)
         {
            if (isShown(state))
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

         private boolean isShown(NodeState state)
         {
            if (filter == null)
            {
               return true;
            }
            else
            {
               Visibility visibility = state.getVisibility() != null ? state.getVisibility() : Visibility.DISPLAYED;
               boolean shown = filter.contains(visibility);
               if (shown && visibility == Visibility.TEMPORAL)
               {
                  long now = System.currentTimeMillis();
                  shown = false;
                  if (state.getStartPublicationTime() == -1 || now < state.getStartPublicationTime())
                  {
                     if (state.getEndPublicationTime() == -1 && now > state.getEndPublicationTime())
                     {
                        shown = true;
                     }
                  }
               }
               return shown;
            }
         }

      };
   }

   public Visitor get()
   {
      return visitor;
   }

}

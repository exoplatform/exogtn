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
 * <p>The scope describes a set of nodes, the scope implementation should be stateless and should be shared
 * between many threads.</p>
 *
 * <p>A scope is responsible for provided a {@link Visitor} object that is used to determine which node should
 * be loaded when a node loading operation occurs. Visitors are not thread safe, as a consequence
 * the {@link #get()} operation should create a new visitor instance on each call, unless the visitor itself is stateless
 * by nature.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Scope
{

   /**
    * A flexible filter implementation.
    */
   public static class Navigation implements Scope
   {

      /** . */
      private final Visitor visitor;

      /**
       * Creates a new navigation scope.
       *
       * @param height the max height of the pruned tree
       * @throws IllegalArgumentException if the height is negative
       */
      public Navigation(int height)
      {
         this(height, null);
      }

      /**
       * Creates a new navigation scope.
       *
       * @param height the max height of the pruned tree
       * @param filter the filter
       * @throws IllegalArgumentException if the height is negative
       */
      public Navigation(final int height, final EnumSet<Visibility> filter) throws IllegalArgumentException
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

   Scope SINGLE = new Navigation(0);

   Scope CHILDREN = new Navigation(1);

   Scope GRANDCHILDREN = new Navigation(2);

   Scope ALL = new Navigation(-1);

   Scope NAVIGATION = new Navigation(2, EnumSet.of(Visibility.DISPLAYED, Visibility.TEMPORAL));

   Visitor get();

   /**
    * A scope visitor responsible for determining the loading of a node.
    */
   public interface Visitor
   {
      /**
       * Returns the visit mode for the specified node.
       *
       * @param depth the relative depth to the root of the loading
       * @param id the node persistent id
       * @param name the node name
       * @param state the node state
       * @return the visit mode
       */
      VisitMode visit(int depth, String id, String name, NodeState state);
   }
}

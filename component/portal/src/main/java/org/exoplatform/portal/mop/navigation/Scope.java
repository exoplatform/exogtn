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

   Scope SINGLE = new GenericScope(0);

   Scope CHILDREN = new GenericScope(1);

   Scope GRANDCHILDREN = new GenericScope(2);

   Scope ALL = new GenericScope(-1);

   Scope NAVIGATION = new GenericScope(2, new NodeFilter()
   {
      public boolean accept(int depth, String id, String name, NodeState state)
      {
         Visibility visibility = state.getVisibility() != null ? state.getVisibility() : Visibility.DISPLAYED;
         switch (visibility)
         {
            case DISPLAYED:
               return true;
            case TEMPORAL:
               long now = System.currentTimeMillis();
               if (state.getStartPublicationTime() == -1 || now < state.getStartPublicationTime())
               {
                  if (state.getEndPublicationTime() == -1 && now > state.getEndPublicationTime())
                  {
                     return false;
                  }
               }
               return true;
            default:
               return false;
         }
      }
   });

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

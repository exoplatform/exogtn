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

/**
 * Describe a change applied to a node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NodeChange<N>
{

   private NodeChange()
   {
   }

   public static class Removed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N node;

      Removed(N parent, N node)
      {
         this.parent = parent;
         this.node = node;
      }
   }

   public static class Added<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N previous;

      /** . */
      final N node;

      /** . */
      final String name;

      Added(N parent, N previous, N node, String name)
      {
         this.parent = parent;
         this.previous = previous;
         this.node = node;
         this.name = name;
      }
   }

   public static class Moved<N> extends NodeChange<N>
   {
      
      /** . */
      final N from;

      /** . */
      final N to;

      /** . */
      final N previous;

      /** . */
      final N node;

      Moved(N from, N to, N previous, N node)
      {
         this.from = from;
         this.to = to;
         this.previous = previous;
         this.node = node;
      }
   }

   public static class Renamed<N> extends NodeChange<N>
   {

      /** . */
      final N node;

      /** . */
      final String name;

      Renamed(N node, String name)
      {
         this.node = node;
         this.name = name;
      }
   }
}

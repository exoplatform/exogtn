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
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class Change
{
   static class Remove extends Change
   {

      /** . */
      final NodeContext<?> parent;

      /** . */
      final NodeContext<?> node;

      Remove(NodeContext<?> parent, NodeContext<?> node)
      {
         this.parent = parent;
         this.node = node;
      }
   }

   static class Move extends Change
   {

      /** . */
      final NodeContext<?> src;

      /** . */
      final NodeContext<?> dst;

      /** . */
      final NodeContext<?> predecessor;

      /** . */
      final NodeContext<?> node;

      Move(NodeContext<?> src, NodeContext<?> dst, NodeContext<?> predecessor, NodeContext<?> node)
      {
         this.src = src;
         this.dst = dst;
         this.predecessor = predecessor;
         this.node = node;
      }
   }

   static class Rename extends Change
   {

      /** . */
      final NodeContext<?> node;

      /** . */
      final String name;

      Rename(NodeContext<?> node, String name)
      {
         this.node = node;
         this.name = name;
      }
   }

   static class Add extends Change
   {

      /** . */
      final NodeContext<?> parent;

      /** . */
      final NodeContext<?> predecessor;

      /** . */
      final NodeContext<?> node;

      /** . */
      final String name;

      Add(NodeContext<?> parent, NodeContext<?> predecessor, NodeContext<?> node, String name)
      {
         this.parent = parent;
         this.predecessor = predecessor;
         this.node = node;
         this.name = name;
      }
   }
}

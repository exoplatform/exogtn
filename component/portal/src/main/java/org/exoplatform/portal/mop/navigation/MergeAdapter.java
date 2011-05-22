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
 * Adapter for the merge operation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
interface MergeAdapter<N>
{

   /**
    * Returns the parent of the node argument.
    *
    * @param node the node
    * @return the parent node
    */
   N getParent(N node);

   /**
    * Returns the node corresponding to the handle argument.
    *
    * @param handle the node handle
    * @return the node
    */
   N getNode(String handle);

   /**
    * Returns a named child of a node.
    *
    * @param node the child
    * @param name the name
    * @return the child
    */
   N getChild(N node, String name);

   /**
    * Returns the name of the node argument.
    *
    * @param node the node
    * @return the node name
    */
   String getName(N node);

   N create(String handle, String name, NodeState state);

}

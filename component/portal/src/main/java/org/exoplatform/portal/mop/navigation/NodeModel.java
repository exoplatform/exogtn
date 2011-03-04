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

import java.util.Collection;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface NodeModel<N>
{

   /**
    * Returns the id of a node.
    *
    * @param node the node
    * @return the node id
    */
   NodeData getData(N node);

   /**
    * Create a node whose children are not determined.
    *
    * @param data the node data
    * @return the node instance
    */
   N create(NodeData data);


   /**
    * Update the nodes to modify their relationships.
    *
    * @param node the parent
    * @param children the children
    */
   void setChildren(N node, Collection<N> children);

   /**
    * Returns the children of the node, if the node does not know about its children then null should be returned.
    *
    * @param node the node
    * @return the node children
    */
//   Collection<N> getChildren(N node);

}

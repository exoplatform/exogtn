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

import org.exoplatform.portal.tree.sync.ListAdapter;
import org.exoplatform.portal.tree.sync.SyncModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TreeContext<N> implements
   SyncModel<NodeData, NodeData, String>,
   ListAdapter<NodeData, String>
{

   /** . */
   private Map<String, NodeContext<N>> nodes = new HashMap<String, NodeContext<N>>();

   public NodeContext<N> newContext(NodeModel<N> model, NodeData data)
   {
      NodeContext<N> context = new NodeContext<N>(this, model, data);
      nodes.put(context.getId(), context);
      return context;
   }

   public String getId(NodeData node)
   {
      return node.id;
   }

   public NodeData getChildren(NodeData node)
   {
      return node;
   }

   public String getHandle(NodeData node)
   {
      return node.getId();
   }

   public NodeData getDescendant(NodeData node, String handle)
   {
      if (handle.length() > 0 && handle.charAt(0) == '/')
      {
         return null;
      }
      NodeContext<N> context = nodes.get(handle);
      return context != null ? context.data : null;
   }

   public int size(NodeData list)
   {
      return list.children.length;
   }

   public Iterator<String> iterator(NodeData list, boolean reverse)
   {
      return (Iterator)list.iterator(reverse);
   }
}

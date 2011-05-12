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

import org.exoplatform.commons.utils.Queues;
import org.exoplatform.portal.tree.diff.Adapters;
import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.exoplatform.portal.tree.diff.HierarchyChangeIterator;
import org.exoplatform.portal.tree.diff.HierarchyChangeType;
import org.exoplatform.portal.tree.diff.HierarchyDiff;

import java.util.Queue;

/**
 * Gather various operations.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class Update
{

   interface Manager<N>
   {

      NodeData getData(N node);

      Manager<NodeData> NODE_DATA = new Manager<NodeData>()
      {
         public NodeData getData(NodeData node)
         {
            return node;
         }
      };

   }

   static <N, N2> void update(
      NodeContext<N> context,
      HierarchyAdapter<String[], NodeContext<N>, String> blah,
      HierarchyAdapter<String[], N2, String> hierarchyAdapter,
      N2 root,
      NodeChangeListener<N> listener,
      Manager<N2> ndAdapter)
   {
      //
      HierarchyDiff<String[], NodeContext<N>, String[], N2, String> diff = HierarchyDiff.create(
         Adapters.<String>list(),
         blah,
         Adapters.<String>list(),
         hierarchyAdapter,
         Utils.<String>comparator());

      //
      HierarchyChangeIterator<String[], NodeContext<N>, String[], N2, String> it = diff.iterator(context, root);
      Queue<NodeContext<N>> stack = Queues.lifo();
      NodeContext<N> last = null;

      //
      while (it.hasNext())
      {
         HierarchyChangeType change = it.next();
         switch (change)
         {
            case ENTER:
               stack.add(it.getSource());
               break;
            case LEAVE:
               last = stack.poll();
               N2 lastData = it.getDestination();
               if (last != null && lastData != null)
               {
                  // Generate node change event (that will occur below)
                  NodeData lastDataData = ndAdapter.getData(lastData);

                  // Data can be null for transient nodes
                  if (lastDataData != null)
                  {
                     NodeState lastDataState = lastDataData.state;
                     String lastDataName = lastDataData.name;
                     if (!last.data.state.equals(lastDataState))
                     {
                        if (listener != null)
                        {
                           listener.onUpdate(new NodeChange.Updated<N>(last, lastDataState));
                        }
                     }

                     // Update name and generate event
                     if (!last.data.name.equals(lastDataName))
                     {
                        last.name = lastDataName;
                        if (listener != null)
                        {
                           listener.onRename(new NodeChange.Renamed<N>(last, lastDataName));
                        }
                     }

                     //
                     last.data = lastDataData;
                  }
               }
               break;
            case MOVED_OUT:
               break;
            case MOVED_IN:
            {
               NodeContext<N> to = stack.peek();
               NodeContext<N> moved = it.getSource();
               NodeContext<N> from = moved.getParent();
               NodeContext<N> previous;
               if (last == null || last.getParent() != to)
               {
                  previous = null;
                  to.insertAt(0, moved);
               }
               else
               {
                  previous = last;
                  last.insertAfter(moved);
               }

               //
               if (listener != null)
               {
                  listener.onMove(new NodeChange.Moved<N>(
                     from,
                     to,
                     previous != null ? previous : null,
                     moved));
               }

               //
               break;
            }
            case ADDED:
            {
               NodeContext<N> parent = stack.peek();
               NodeContext<N> added;
               NodeContext<N> previous;
               N2 destination = it.getDestination();
               NodeData desData = ndAdapter.getData(destination);
               if (last == null || last.getParent() != parent)
               {
                  previous = null;
                  added = parent.insertAt(0, desData);
               }
               else
               {
                  previous = last;
                  added = last.insertAfter(desData);
               }

               //
               if (listener != null)
               {
                  listener.onAdd(new NodeChange.Added<N>(
                     parent,
                     previous != null ? previous : null,
                     added,
                     added.getName()));
               }

               //
               break;
            }
            case REMOVED:
            {
               NodeContext<N> removed = it.getSource();
               NodeContext<N> parent = removed.getParent();

               //
               removed.remove();

               //
               if (listener != null)
               {
                  listener.onRemove(new NodeChange.Removed<N>(
                     parent,
                     removed));
               }

               //
               break;
            }
            default:
               throw new UnsupportedOperationException("todo : " + change);
         }
      }
   }
}

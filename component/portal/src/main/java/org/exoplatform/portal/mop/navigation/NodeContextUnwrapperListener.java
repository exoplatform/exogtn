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
public class NodeContextUnwrapperListener<N> implements NodeChangeListener<NodeContext<N>>
{

   public static <N> NodeChangeListener<NodeContext<N>> safeWrap(NodeChangeListener<N> listener)
   {
      return listener != null ? new NodeContextUnwrapperListener<N>(listener) : null;
   }

   /** . */
   private final NodeChangeListener<N> delegate;

   public NodeContextUnwrapperListener(NodeChangeListener<N> delegate)
   {
      this.delegate = delegate;
   }

   private N unwrap(NodeContext<N> context)
   {
      return context != null ? context.node : null;
   }

   public void onAdd(NodeChange.Added<NodeContext<N>> nodeContextAdded)
   {
      delegate.onAdd(new NodeChange.Added<N>(
         unwrap(nodeContextAdded.getParent()),
         unwrap(nodeContextAdded.getPrevious()),
         unwrap(nodeContextAdded.getNode())
      ));
   }

   public void onRemove(NodeChange.Removed<NodeContext<N>> nodeContextRemoved)
   {
      delegate.onRemove(new NodeChange.Removed<N>(
         unwrap(nodeContextRemoved.getParent()),
         unwrap(nodeContextRemoved.getNode())
      ));
   }

   public void onRename(NodeChange.Renamed<NodeContext<N>> nodeContextRenamed)
   {
      delegate.onRename(new NodeChange.Renamed<N>(
         unwrap(nodeContextRenamed.getNode()),
         nodeContextRenamed.name
      ));
   }

   public void onUpdate(NodeChange.Updated<NodeContext<N>> nodeContextUpdated)
   {
      delegate.onUpdate(new NodeChange.Updated<N>(
         unwrap(nodeContextUpdated.getNode()),
         nodeContextUpdated.getState()
      ));
   }

   public void onMove(NodeChange.Moved<NodeContext<N>> nodeContextMoved)
   {
      delegate.onMove(new NodeChange.Moved<N>(
         unwrap(nodeContextMoved.getFrom()),
         unwrap(nodeContextMoved.getTo()),
         unwrap(nodeContextMoved.getPrevious()),
         unwrap(nodeContextMoved.getNode())
      ));
   }
}

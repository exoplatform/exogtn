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

package org.exoplatform.portal.tree.sync.diff;

import org.exoplatform.portal.tree.sync.ListAdapter;
import org.exoplatform.portal.tree.sync.SyncContext;
import org.exoplatform.portal.tree.sync.SyncModel;

import java.util.Comparator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Diff<L1, N1, L2, N2, H> {

   /** . */
   final ListAdapter<L1, H> adapter1;

   /** . */
   final SyncModel<L1, N1, H> model1;

   /** . */
   final ListAdapter<L2, H> adapter2;

   /** . */
   final SyncModel<L2, N2, H> model2;

   /** . */
   final Comparator<H> comparator;

   public Diff(ListAdapter<L1, H> adapter1, SyncModel<L1, N1, H> model1, ListAdapter<L2, H> adapter2, SyncModel<L2, N2, H> model2, Comparator<H> comparator) {
      this.adapter1 = adapter1;
      this.model1 = model1;
      this.adapter2 = adapter2;
      this.model2 = model2;
      this.comparator = comparator;
   }

   public DiffChangeIterator<L1, N1, L2, N2, H> perform(N1 node1, N2 node2) {
      return new DiffChangeIterator<L1, N1, L2, N2, H>(this, new SyncContext<L1, N1, H>(adapter1, model1, node1), new SyncContext<L2, N2, H>(adapter2, model2, node2));
   }
}

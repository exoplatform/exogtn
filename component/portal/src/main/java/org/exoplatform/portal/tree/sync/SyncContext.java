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

package org.exoplatform.portal.tree.sync;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SyncContext<L, N, H> {

   /** . */
   final ListAdapter<L, H> adapter;

   /** . */
   final SyncModel<L, N, H> model;

   /** . */
   final N root;

   public SyncContext(ListAdapter<L, H> adapter, SyncModel<L, N, H> model, N root) throws NullPointerException {
      if (adapter == null) {
         throw new NullPointerException();
      }
      if (model == null) {
         throw new NullPointerException();
      }
      if (root == null) {
         throw new NullPointerException();
      }

      //
      this.adapter = adapter;
      this.model = model;
      this.root = root;
   }

   public SyncModel<L, N, H> getModel() {
      return model;
   }

   public N getRoot() {
      return root;
   }

   public N findByHandle(H handle) {
      return model.getDescendant(root, handle);
   }
}

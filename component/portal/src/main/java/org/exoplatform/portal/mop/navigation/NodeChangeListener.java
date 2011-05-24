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
 * A listener for node changes.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface NodeChangeListener<N>
{

   void onAdd(N source, N parent, N previous);

   void onCreate(N source, N parent, N previous, String name);

   void onRemove(N source, N parent);

   void onDestroy(N source, N parent);

   void onRename(N source, N parent, String name);

   void onUpdate(N source, NodeState state);

   void onMove(N source, N from, N to, N previous);

   class Base<N> implements NodeChangeListener<N>
   {

      public void onAdd(N source, N parent, N previous)
      {
      }

      public void onCreate(N source, N parent, N previous, String name) throws NavigationServiceException
      {
      }

      public void onRemove(N source, N parent)
      {
      }

      public void onDestroy(N source, N parent)
      {
      }

      public void onRename(N source, N parent, String name) throws NavigationServiceException
      {
      }

      public void onUpdate(N source, NodeState state) throws NavigationServiceException
      {
      }

      public void onMove(N source, N from, N to, N previous) throws NavigationServiceException
      {
      }
   }
}

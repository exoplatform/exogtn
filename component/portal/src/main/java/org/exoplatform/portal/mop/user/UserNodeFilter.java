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

package org.exoplatform.portal.mop.user;

import org.exoplatform.portal.mop.Visibility;

import java.util.EnumSet;
import java.util.Set;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
public class UserNodeFilter
{

   public static UserNodeFilter get()
   {
      return new UserNodeFilter();
   }

   /** . */
   Set<Visibility> withVisibility = null;

   /** . */
   boolean withAuthorizationCheck = false;

   /** . */
   boolean withTemporalCheck = false;

   public UserNodeFilter withVisibility(Visibility first, Visibility... rest)
   {
      withVisibility = EnumSet.of(first, rest);
      return this;
   }

   public UserNodeFilter withVisibility(Visibility first)
   {
      withVisibility = EnumSet.of(first);
      return this;
   }

   public UserNodeFilter withoutVisibility()
   {
      withVisibility = null;
      return this;
   }

   public UserNodeFilter withTemporalCheck()
   {
      this.withTemporalCheck = true;
      return this;
   }

   public UserNodeFilter withoutTemporalCheck()
   {
      this.withTemporalCheck = false;
      return this;
   }

   public UserNodeFilter withAuthorizationChek()
   {
      this.withAuthorizationCheck = true;
      return this;
   }

   public UserNodeFilter withoutAuthorizationChek()
   {
      this.withAuthorizationCheck = false;
      return this;
   }
}

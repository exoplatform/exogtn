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

import java.util.ResourceBundle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface BundleResolver<K>
{

   /**
    * A resolver that always return null.
    */
   BundleResolver NULL_RESOLVER = new BundleResolver()
   {
      public ResourceBundle resolve(UserNavigation navigation)
      {
         return null;
      }
   };

   /**
    * Provide an opportunity to use a resource bundle per navigation. It no such bundle can be found then null
    * can be returned.
    *
    * @param navigation the navigation that will be localized
    * @return the resource bundle to use
    */
   ResourceBundle resolve(UserNavigation navigation);

}

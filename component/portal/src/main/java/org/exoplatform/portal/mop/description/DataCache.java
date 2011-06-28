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

package org.exoplatform.portal.mop.description;

import org.exoplatform.portal.mop.Described;

import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class DataCache
{

   Described.State get(CacheKey key)
   {
      CacheValue value = getState(key);
      if (value != null)
      {
         if (value.origin != null)
         {
            CacheValue origin = getState(value.origin);
            if (origin == null || value.serial < origin.serial)
            {
               value = null;
               remove(key);
            }
         }
      }
      return value != null ? value.value : null;
   }

   void put(CacheKey key, Locale resolvedLocale, Described.State state)
   {
      if (key.locale.equals(resolvedLocale))
      {
         putState(key, new CacheValue(state));
      }
      else
      {
         CacheValue origin = new CacheValue(state);
         CacheKey originKey = new CacheKey(resolvedLocale, key.id);
         putState(originKey, origin);
         CacheValue foo = new CacheValue(originKey, origin.serial, state);
         putState(key, foo);
      }
   }

   protected abstract void remove(CacheKey key);

   protected abstract CacheValue getState(CacheKey key);

   protected abstract void putState(CacheKey key, CacheValue value);

}

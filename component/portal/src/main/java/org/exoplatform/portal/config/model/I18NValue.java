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

package org.exoplatform.portal.config.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class I18NValue<V, L extends LocalizedValue<V>> extends ArrayList<L>
{

   public static I18NValue create()
   {
      return new I18NValue();
   }

   public I18NValue()
   {
   }

   public I18NValue(Collection<? extends L> c)
   {
      super(c);
   }

   public I18NValue(L... c)
   {
      super(Arrays.asList(c));
   }

   public final Map<Locale, V> getValue(Locale defaultLocale)
   {
      Map<Locale, V> map = Collections.emptyMap();
      L unqualifiedLocalizedValue = null;
      for (L localizedValue : this)
      {
         if (localizedValue.getLang() != null)
         {
            if (map.isEmpty())
            {
               map = new HashMap<Locale, V>();
            }
            map.put(localizedValue.getLang(), localizedValue.getValue());
         }
         else
         {
            unqualifiedLocalizedValue = localizedValue;
         }
      }
      if (map.isEmpty())
      {
         return null;
      }
      else
      {
         if (unqualifiedLocalizedValue != null && !map.containsKey(defaultLocale))
         {
            map.put(defaultLocale, unqualifiedLocalizedValue.getValue());
         }
         return map;
      }
   }
}

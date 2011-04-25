/**
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.portal.mop.i18n;

import java.util.Map;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 15, 2011
 */

@PrimaryType(name = "gtn:languages")
public abstract class LanguageSpace
{
   public ChromatticSession session;

   @Create
   public abstract Language createLanguage();

   @OneToMany
   public abstract Map<String, Language> getChildren();

   public <E> E addNewLanguage(Class<E> classType, String locale)
   {
      Language language = null;
      Map<String, Language> children = getChildren();
      if (!children.containsKey(locale))
      {
         language = session.create(Language.class);
         children.put(locale, language);
      }
      else
      {
         language = children.get(locale);
      }
      
      E e = session.getEmbedded(language, classType);
      if (e == null)
      {
         e = session.create(classType);
         session.setEmbedded(language, classType, e);
      }
      
      e = session.getEmbedded(language, classType);

      return e;
   }
}

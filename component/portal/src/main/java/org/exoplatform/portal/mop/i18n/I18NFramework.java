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

import org.chromattic.api.ChromatticSession;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 21, 2011
 */
public class I18NFramework
{
   private ChromatticSession session;
   public I18NFramework()
   {
      
   }
   
   public I18NFramework(ChromatticSession _session)
   {
      this.session = _session;
   }
   
   public I18Nized createI18nMixin(Object entityNode)
   {
      I18Nized mixin = session.create(I18Nized.class);
      session.setEmbedded(entityNode, I18Nized.class, mixin);
      
      // Language space is mandatory
      LanguageSpace languageSpace = session.create(LanguageSpace.class);
      mixin.setLanguageSpace(languageSpace);
      return mixin;
   }
}

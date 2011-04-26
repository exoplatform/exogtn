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

   public I18NFramework(ChromatticSession _session)
   {
      this.session = _session;
   }

   /**
    * Add new language to the entity node
    * 
    * @param entityNode the node which want to add new language
    * @param classType a mixin node type which existed in entity node
    * @param locale the language added to i18n mixin
    * @return mixin node type added to the entity node
    */
   public <M> M putMixin(Object entityNode, Class<M> classType, String locale)
   {
      // Check whether class type is entity node's embedded
      M m = session.getEmbedded(entityNode, classType);
      if (m == null)
      {
         throw new IllegalStateException("Can not put i18n mixin for entity node with mixin " + classType.getName());
      }

      I18Nized mixin = session.getEmbedded(entityNode, I18Nized.class);
      if (mixin == null)
      {
         mixin = session.create(I18Nized.class);
         session.setEmbedded(entityNode, I18Nized.class, mixin);

         // Language space is mandatory
         LanguageSpace languageSpace = session.create(LanguageSpace.class);
         mixin.setLanguageSpace(languageSpace);
      }

      return mixin.getMixin(classType, locale, true);
   }

   /**
    * Return a mixin type of a language node
    * <p>
    * If the language has not defined in entity node, 
    * mixin nodetype of entity node will be returned as default value
    * 
    * @param entityNode The node which want to get i18n mixin
    * @param classType Mixin nodetype in entity node want to be localized
    * @param locale
    * @return Mixin nodetype has been localized
    */
   public <M> M getMixin(Object entityNode, Class<M> classType, String locale)
   {
      // Check whether class type is entity node's embedded
      M m = session.getEmbedded(entityNode, classType);
      if (m == null)
      {
         throw new IllegalStateException(classType.getName() + " is not a mixin of the entity node");
      }

      I18Nized mixin = session.getEmbedded(entityNode, I18Nized.class);
      if (mixin != null && mixin.getMixin(classType, locale, false) != null)
      {
         return mixin.getMixin(classType, locale, false);
      }
      else
      {
         return m;
      }
   }
}

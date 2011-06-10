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
import java.util.Map;

/**
 * The description service provides configuration and runtime interaction of described objects.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface DescriptionService
{

   /**
    * <p>Resolve a description, the <code>wantedLocale</code> argument specifies which locale is relevant for retrieval,
    * the <code>defaultLocale</code> specifies which locale should be defaulted to when the <code>wantedLocale</code>
    * cannot provide any relevant match. The <code>defaultLocale</code> argument is optional.</p>
    *
    * <p>The resolution follows those rules:
    * <ul>
    *    <li>The resolution is performed against the wanted locale.</li>
    *    <li>When the wanted locale does not resolve and a default locale is provided, a resolution
    *    is performed on that default locale.</li>
    *    <li>Otherwise null is returned.<li>
    * </ul>
    * </p>
    *
    * @param id the object id
    * @param defaultLocale the default locale
    * @param wantedLocale the wanted locale
    * @return the description
    * @throws NullPointerException if the id or the wanted locale argument is null
    */
   Described.State resolveDescription(String id, Locale defaultLocale, Locale wantedLocale) throws NullPointerException;

   /**
    * Returns the default description or null if it does not exist.
    *
    * @param id the object id
    * @return the description
    * @throws NullPointerException if the id argument is null
    */
   Described.State getDescription(String id) throws NullPointerException;

   /**
    * Update the default description to the new description or remove it if the description argument is null.
    *
    * @param id the object id
    * @param description the new description
    * @throws NullPointerException if the id argument is null
    */
   void setDescription(String id, Described.State description) throws NullPointerException;

   /**
    * Returns a description for the specified locale argument or null if it does not exist.
    *
    * @param id the object id
    * @param locale the locale
    * @return the description
    * @throws NullPointerException if the id or locale argument is null
    */
   Described.State getDescription(String id, Locale locale) throws NullPointerException;

   /**
    * Update the description for the specified locale to the new description or remove it if the description
    * argument is null.
    *
    * @param id the object id
    * @param locale the locale
    * @param description the new description
    * @throws NullPointerException if the id or locale argument is null
    */
   void setDescription(String id, Locale locale, Described.State description) throws NullPointerException;

   /**
    * Returns a map containing all the descriptions of an object or null if the object is not internationalized.
    *
    * @param id the object id
    * @return the map the description map
    * @throws NullPointerException if the id is null
    */
   Map<Locale, Described.State> getDescriptions(String id) throws NullPointerException;

   /**
    * Updates the description of the specified object or remove the internationalized characteristic of
    * the object if the description map is null.
    *
    * @param id the object id
    * @param descriptions the new descriptions
    * @throws NullPointerException if the id is null
    */
   void setDescriptions(String id, Map<Locale, Described.State> descriptions) throws NullPointerException;

}

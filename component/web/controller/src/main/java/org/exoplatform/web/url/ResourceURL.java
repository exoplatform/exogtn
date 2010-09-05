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

package org.exoplatform.web.url;

/**
 * An URL for a resource.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ResourceURL<R, L extends ResourceLocator<R>>
{

   /** . */
   protected final L locator;

   /**
    * Create a new instance.
    *
    * @param locator the resource locator that can't be null
    * @throws NullPointerException if the resource locator is null
    */
   public ResourceURL(L locator) throws NullPointerException
   {
      if (locator == null)
      {
         throw new NullPointerException("No null locator");
      }

      //
      this.locator = locator;
   }

   /**
    * Returns the resource locator of this URL.
    *
    * @return the resource locator
    */
   public final L getResourceLocator()
   {
      return locator;
   }

   /**
    * Returns the current resource associated with this URL.
    *
    * @return the resource
    */
   public final R getResource()
   {
      return locator.getResource();
   }

   /**
    * Set a new resource on this URL.
    *
    * @param resource the new resource
    */
   public final void setResource(R resource)
   {
      locator.setResource(resource);
   }

   /**
    * Generates the URL value.
    *
    * @return the URL value
    */
   public abstract String toString();
}

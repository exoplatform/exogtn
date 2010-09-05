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
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class LocatorFactory
{

   /**
    * Returns a resource context for a resource type or null if none can be found.
    *
    * @param resourceType the resource type
    * @param <R> the resource parameter type
    * @param <C> the resource context parameter type
    * @param <L> the resource locator parameter type
    * @return the context
    * @throws NullPointerException if the resource type is null
    */
   protected abstract <R, C, L extends ResourceLocator<R>> C getContext(ResourceType<R, C, L> resourceType) throws NullPointerException;

   public <R, C, U extends ResourceLocator<R>> U newLocator(ResourceType<R, C, U> resourceType)
   {
      C context = getContext(resourceType);
      return resourceType.newLocator(context);
   }

   public <R, C, L extends ResourceLocator<R>> L newLocator(ResourceType<R, C, L> resourceType, R resource)
   {
      L locator = newLocator(resourceType);
      locator.setResource(resource);
      return locator;
   }
}

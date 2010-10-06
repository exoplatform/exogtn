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

package org.exoplatform.portal.url;

import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.url.LocatorProvider;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceType;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class PortalURLProvider
{
   private static ThreadLocal<PortalURLProvider> currentProvider = new ThreadLocal<PortalURLProvider>();

   private ControllerContext controllerContext;

   private LocatorProvider locatorProvider;
   
   public PortalURLProvider(ControllerContext controllerCtx, LocatorProvider locatorService)
   {
      this.controllerContext = controllerCtx;
      this.locatorProvider = locatorService;
   }
   
   public static void setCurrentPortalURLProvider(PortalURLProvider provider)
   {
      currentProvider.set(provider);
   }
   
   public static PortalURLProvider getCurrentPortalURLProvider()
   {
      return currentProvider.get();
   }

   public final <R, L extends ResourceLocator<R>> PortalURL<R, L> createPortalURL(String access, String siteName, ResourceType<R, L> resourceType)
   {
      PortalURLProvider portalURLProvider = currentProvider.get();
      if (portalURLProvider != null)
      {
         L newLocator = locatorProvider.newLocator(resourceType);
         return new PortalURL<R, L>(controllerContext, newLocator, false, siteName, access);
      }
      return null;
   }
}

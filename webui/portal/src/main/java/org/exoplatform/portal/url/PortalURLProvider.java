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
import org.exoplatform.web.url.URLFactory;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;

import java.util.Locale;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class PortalURLProvider
{
   private static ThreadLocal<PortalURLProvider> currentProvider = new ThreadLocal<PortalURLProvider>();

   private ControllerContext controllerContext;

   private URLFactory urlFactory;

   private Locale requestLocale;
   
   public PortalURLProvider(ControllerContext controllerCtx, URLFactory urlFactory, Locale requestLocale)
   {
      this.controllerContext = controllerCtx;
      this.urlFactory = urlFactory;
      this.requestLocale = requestLocale;
   }
   
   public static void setCurrentPortalURLProvider(PortalURLProvider provider)
   {
      currentProvider.set(provider);
   }
   
   public static PortalURLProvider getCurrentPortalURLProvider()
   {
      return currentProvider.get();
   }

   public final <R, U extends PortalURL<R, U>> U createPortalURL(String siteType, String siteName, ResourceType<R, U> resourceType)
   {
      PortalURLProvider portalURLProvider = currentProvider.get();
      if (portalURLProvider != null)
      {
         PortalURLContext context = new PortalURLContext(controllerContext, siteType, siteName);
         return urlFactory.newURL(resourceType, context, false, requestLocale);
      }
      return null;
   }
}

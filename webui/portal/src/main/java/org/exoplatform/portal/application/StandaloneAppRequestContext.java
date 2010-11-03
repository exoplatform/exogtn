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

package org.exoplatform.portal.application;

import org.exoplatform.portal.webui.application.UIStandaloneAppContainer;
import org.exoplatform.portal.webui.workspace.UIStandaloneApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.webui.core.UIApplication;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public class StandaloneAppRequestContext extends PortalRequestContext
{
   private String storageId;

   protected static Log log = ExoLogger.getLogger(StandaloneAppRequestContext.class);

   public StandaloneAppRequestContext(StandaloneApplication app, ControllerContext controllerContext,
      String requestSiteType, String requestSiteName, String requestPath, String access)
      throws Exception
   {
      super(app, controllerContext, requestSiteType, requestSiteName, requestPath, access);
      HttpServletRequest req = controllerContext.getRequest();
      int idx = (req.getServletPath() + req.getContextPath()).length() + 1;
      if (idx <= getRequestURI().length())
      {
         storageId = getRequestURI().substring(idx);
      }
      else
      {
         storageId = "";
      }
   }

   public String getStorageId()
   {
      return storageId;
   }

   public String getTitle() throws Exception
   {
      String title = null;
      UIApplication uiApp = getUIApplication();
      UIStandaloneAppContainer container = uiApp.getChild(UIStandaloneAppContainer.class);
      if (container != null)
      {
         title = container.getCurrAppName();
      }

      if (title == null)
      {
         title = "";
      }
      return title;
   }

   public Orientation getOrientation()
   {
      return ((UIStandaloneApplication)uiApplication_).getOrientation();
   }

   public Locale getLocale()
   {
      return ((UIStandaloneApplication)uiApplication_).getLocale();
   }

   public String getPortalOwner()
   {
      return null;
   }

   public String getNodePath()
   {
      throw new UnsupportedOperationException();
   }
}

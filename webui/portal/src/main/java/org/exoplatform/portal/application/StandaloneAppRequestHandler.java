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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;

import java.util.Locale;


public class StandaloneAppRequestHandler extends PortalRequestHandler
{
   
   private String webuiConfigPath;

   public StandaloneAppRequestHandler(InitParams params)
   {
      ValueParam valueParam = params.getValueParam("webui.configuration");
      if (valueParam != null)
      {
         webuiConfigPath = valueParam.getValue();
      }
   }

   private String[] PATHS = {"/standalone"};

   public String[] getPath()
   {
      return PATHS;
   }

   @Override
   public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception
   {
      StandaloneApplication standaloneApplication = new StandaloneApplication(sConfig);
      standaloneApplication.setWebUIConfigPath(webuiConfigPath);
      standaloneApplication.onInit();
      controller.addApplication(standaloneApplication);
   }
   
   @Override
   public void execute(ControllerContext controllerContext) throws Exception
   {
      HttpServletRequest req = controllerContext.getRequest();
      HttpServletResponse res = controllerContext.getResponse();


      log.debug("Session ID = " + req.getSession().getId());
      res.setHeader("Cache-Control", "no-cache");

      //
      String requestPath = controllerContext.getParameter(REQUEST_PATH);
      String requestSiteType = controllerContext.getParameter(REQUEST_SITE_TYPE);
      String requestSiteName = controllerContext.getParameter(REQUEST_SITE_NAME);
      String access = controllerContext.getParameter(ACCESS);
      
      //
      Locale requestLocale;
      String lang = controllerContext.getParameter(LANG);
      if (lang.length() == 0)
      {
         requestLocale = null;
      }
      else
      {
         requestLocale = new Locale(lang.substring(0, 2));
      }
      //
      if (requestSiteName == null) {
         res.sendRedirect(req.getContextPath());
         return;
      }
      StandaloneApplication app = controllerContext.getController().getApplication(StandaloneApplication.STANDALONE_APPLICATION_ID);
      StandaloneAppRequestContext context = new StandaloneAppRequestContext(app, controllerContext, requestSiteType, requestSiteName, requestPath, access, requestLocale);
      processRequest(context, app);
      
      log.debug("Session ID = " + req.getSession().getId());
      res.setHeader("Cache-Control", "no-cache");

      processRequest(context, app);
   }     
}

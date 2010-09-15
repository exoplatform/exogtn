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

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.url.ResourceURL;
import org.exoplatform.webui.application.UIComponentURLBuilder;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.exoplatform.webui.url.ComponentLocator;

import java.net.URLEncoder;

/**
 * Created by The eXo Platform SAS
 * Apr 3, 2007  
 */
public class PortalURLBuilder extends UIComponentURLBuilder
{

   private static Logger LOGGER = LoggerFactory.getLogger(PortalURLBuilder.class);

   public PortalURLBuilder(PortalRequestContext ctx, ResourceURL<UIComponent, ComponentLocator> url)
   {
      super(configure(ctx, url));
   }

   /*
    * This is a hack.
    */
   private static ResourceURL<UIComponent, ComponentLocator> configure(PortalRequestContext prc, ResourceURL<UIComponent, ComponentLocator> url)
   {
      String path = prc.getNodePath();
      url.getResourceLocator().setParameterValue(PortalRequestHandler.REQUEST_PATH, path);
      return url;
   }

   protected void createURL(StringBuilder builder, UIComponent targetComponent, String action, String targetBeanId,
      Parameter[] params)
   {
      builder.append("baseurl").append("?").append(PortalRequestContext.UI_COMPONENT_ID).append('=').append(
         targetComponent.getId());
      if (action != null && action.trim().length() > 0)
      {
         builder.append("&amp;").append(PortalRequestContext.UI_COMPONENT_ACTION).append('=').append(action);
      }

      if (targetBeanId != null && targetBeanId.trim().length() > 0)
      {
         builder.append("&amp;").append(UIComponent.OBJECTID).append('=').append(targetBeanId);
      }

      if (params == null || params.length < 1)
         return;
      for (Parameter param : params)
      {
         try
         {
            param.setValue(URLEncoder.encode(param.getValue(), "utf-8"));
         }
         catch (Exception e)
         {
            LOGGER.error("Could not encode URL", e);
         }
         builder.append("&amp;").append(param.getName()).append('=').append(param.getValue());
      }

   }

}

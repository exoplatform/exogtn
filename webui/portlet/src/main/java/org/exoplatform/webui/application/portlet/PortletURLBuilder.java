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

package org.exoplatform.webui.application.portlet;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;

import javax.portlet.PortletURL;
import java.net.URLEncoder;

/**
 * julien todo : use PortletURL parameter instead of appending them to the url returned by the PortletURL
 *
 * Created by The eXo Platform SAS
 * Apr 3, 2007  
 */
public class PortletURLBuilder extends URLBuilder<UIComponent>
{

   /** . */
   private final PortletURL url;

   public PortletURLBuilder(PortletURL url)
   {
      this.url = url;
   }

   public String createAjaxURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      StringBuilder builder = new StringBuilder("javascript:");
      if (confirm != null && confirm.length() > 0)
      {
         builder.append("if(confirm('").append(confirm.replaceAll("'", "\\\\'")).append("'))");
      }
      builder.append("ajaxGet('");
      if (targetBeanId != null)
      {
         try
         {
            targetBeanId = URLEncoder.encode(targetBeanId, "utf-8");
         }
         catch (Exception e)
         {
            System.err.println(e.toString());
         }
      }
      createURL(builder, targetComponent, action, targetBeanId, params);
      builder.append("&amp;ajaxRequest=true')");
      return builder.toString();
   }

   public String createURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      StringBuilder builder = new StringBuilder();
      boolean hasConfirm = confirm != null && confirm.length() > 0;
      if (hasConfirm)
      {
         builder.append("javascript:if(confirm('").append(confirm.replaceAll("'", "\\\\'")).append("'))");
         builder.append("window.location=\'");
      }
      if (targetBeanId != null)
      {
         try
         {
            targetBeanId = URLEncoder.encode(targetBeanId, "utf-8");
         }
         catch (Exception e)
         {
            System.err.println(e.toString());
         }
      }
      createURL(builder, targetComponent, action, targetBeanId, params);
      if (hasConfirm)
         builder.append("\';");
      return builder.toString();
   }

   private void createURL(StringBuilder builder, UIComponent targetComponent, String action, String targetBeanId,
      Parameter[] params)
   {
      // Clear URL
      url.getParameterMap().clear();

      //
      url.setParameter(UIComponent.UICOMPONENT, targetComponent.getId());

      //
      if (action != null && action.trim().length() > 0)
      {
         url.setParameter(WebuiRequestContext.ACTION, action);
      }

      //
      if (targetBeanId != null && targetBeanId.trim().length() > 0)
      {
         url.setParameter(UIComponent.OBJECTID, targetBeanId);
      }

      //
      if (params != null && params.length > 0)
      {
         for (Parameter param : params)
         {
            url.setParameter(param.getName(), param.getValue());
         }
      }

      //
      builder.append(url.toString());
   }
}

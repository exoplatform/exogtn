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

import org.exoplatform.Constants;
import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.SimpleRenderContext;
import org.exoplatform.web.url.ControllerURL;
import org.exoplatform.web.url.MimeType;
import org.exoplatform.web.url.ResourceLocator;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalURL<R, L extends ResourceLocator<R>> extends ControllerURL<R, L>
{

   /** . */
   private static final Map<MimeType, String> AMP_MAP = new EnumMap<MimeType, String>(MimeType.class);

   static
   {
      AMP_MAP.put(MimeType.XHTML, "&amp;");
      AMP_MAP.put(MimeType.PLAIN, "&");
   }

   /** . */
   private final ControllerContext controllerContext;

   /** . */
   private final String access;

   /** . */
   private final String siteType;
   
   /** . */
   private final String siteName;

   /** . */
   private StringBuilder buffer;

   /** . */
   private SimpleRenderContext renderContext;

   public PortalURL(ControllerContext requestContext, L locator, Boolean ajax, String siteType, String siteName, String access)
   {
      super(locator, ajax);

      //
      if (requestContext == null)
      {
         throw new NullPointerException("No null request context");
      }

      //
      this.siteType = siteType;
      this.siteName = siteName;
      this.controllerContext = requestContext;
      this.access = access;
   }

   public String toString()
   {
      //
      if (renderContext == null)
      {
         buffer = new StringBuilder();
         renderContext = new SimpleRenderContext(buffer);
      }
      else
      {
         renderContext.reset();
      }

      //
      if (locator.getResource() == null)
      {
         throw new IllegalStateException("No resource set on portal URL");
      }

      //
      if (ajax)
      {
         buffer.append("javascript:");
         if (confirm != null && confirm.length() > 0)
         {
            buffer.append("if(confirm('").append(confirm.replaceAll("'", "\\\\'")).append("'))");
         }
         buffer.append("ajaxGet('");
      }
      else
      {
         if (confirm != null && confirm.length() > 0)
         {
            // Need to find a way to make the confirm message appear
            // I think we could use :
            // 1/ the if(confirm('')) ...
            // 2/ a call function that updates window.location
            // for now it is disabled
         }
      }

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(WebAppController.HANDLER_PARAM, "portal");
      parameters.put(PortalRequestHandler.ACCESS, access);
      parameters.put(PortalRequestHandler.REQUEST_SITE_TYPE, siteType);      
      parameters.put(PortalRequestHandler.REQUEST_SITE_NAME, siteName);

      //
      for (QualifiedName parameterName : locator.getParameterNames())
      {
         String parameterValue = locator.getParameterValue(parameterName);
         if (parameterValue != null)
         {
            parameters.put(parameterName, parameterValue);
         }
      }

      //
      controllerContext.renderURL(parameters, renderContext);

      //
      MimeType mt = mimeType;
      if (mt == null)
      {
         mt = MimeType.XHTML;
      }
      String amp = AMP_MAP.get(mt);

      //
      boolean questionMarkDone = false;
      Map<String, String> queryParams = renderContext.getQueryParams();
      if (queryParams.size() > 0)
      {
         for (Map.Entry<String, String> entry : queryParams.entrySet())
         {
            buffer.append(questionMarkDone ? amp : "?");
            buffer.append(entry.getKey());
            buffer.append('=');
            buffer.append(entry.getValue());
            questionMarkDone = true;
         }
      }

      // Now append generic query parameters
      for (Map.Entry<String, String[]> entry : getQueryParameters().entrySet())
      {
         for (String value : entry.getValue())
         {
            buffer.append(questionMarkDone ? amp : "?");
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(value);
            questionMarkDone = true;
         }
      }

      //
      if (ajax)
      {
         buffer.append(questionMarkDone ? amp : "?");
         buffer.append("ajaxRequest=true");
         buffer.append("')");
      }

      //
      return buffer.toString();
   }
}

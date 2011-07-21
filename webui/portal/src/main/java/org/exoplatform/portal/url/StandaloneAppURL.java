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

package org.exoplatform.portal.url;

import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.ControllerURL;
import org.exoplatform.web.url.ResourceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Revision$
 */
public class StandaloneAppURL<R, L extends ResourceLocator<R>> extends ControllerURL<R, L>
{

   /** . */
   private final ControllerContext controllerContext;

   /** . */
   private PortalURLRenderContext renderContext;

   public StandaloneAppURL(
      ControllerContext requestContext,
      L locator,
      Boolean ajax)
   {
      super(locator, ajax, null);

      //
      if (requestContext == null)
      {
         throw new NullPointerException("No null request context");
      }

      this.controllerContext = requestContext;
   }

   public String toString()
   {
      if (renderContext == null)
      {
         renderContext = new PortalURLRenderContext(new StringBuilder());
      }
      else
      {
         renderContext.reset();
      }

      //
      if (locator.getResource() == null)
      {
         throw new IllegalStateException("No resource set on standaloneApp URL");
      }

      // Configure mime type
      renderContext.setMimeType(mimeType);

      boolean hasConfirm = confirm != null && confirm.length() > 0;
      
      //
      if (ajax)
      {
         renderContext.append("javascript:", false);
         if (hasConfirm)
         {
            renderContext.append("if(confirm('", false);
            renderContext.append(confirm.replaceAll("'", "\\\\'"), false);
            renderContext.append("'))", false);
         }
         renderContext.append("ajaxGet('", false);
      }
      else
      {
         if (hasConfirm)
         {
            renderContext.append("javascript:", false);
            renderContext.append("if(confirm('", false);
            renderContext.append(confirm.replaceAll("'", "\\\\'"), false);
            renderContext.append("'))", false);
            renderContext.append("window.location=\'", false);
         }
      }

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(WebAppController.HANDLER_PARAM, "standalone");

      //
      for (QualifiedName parameterName : locator.getParameterNames())
      {
         String parameterValue = locator.getParameterValue(parameterName);
         if (parameterValue != null)
         {
            parameters.put(parameterName, parameterValue);
         }
      }

      // Render url via controller
      controllerContext.renderURL(parameters, renderContext);

      // Now append generic query parameters
      for (Map.Entry<String, String[]> entry : getQueryParameters().entrySet())
      {
         for (String value : entry.getValue())
         {
            renderContext.appendQueryParameter(entry.getKey(), value);
         }
      }

      //
      if (ajax)
      {
         renderContext.appendQueryParameter("ajaxRequest", "true");
         renderContext.flush();
         renderContext.append("')", false);
      }
      else
      {
         renderContext.flush();
         if (hasConfirm)
         {
            renderContext.append("\'", false);            
         }
      }

      //
      return renderContext.toString();
   }
}

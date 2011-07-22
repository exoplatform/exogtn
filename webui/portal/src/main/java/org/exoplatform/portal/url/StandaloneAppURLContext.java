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

import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.URLContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class StandaloneAppURLContext implements URLContext
{

   /** . */
   private final ControllerContext controllerContext;

   /** . */
   private PortalURLRenderContext renderContext;

   public StandaloneAppURLContext(ControllerContext controllerContext)
   {
      if (controllerContext == null)
      {
         throw new NullPointerException("No null controller context");
      }

      //
      this.controllerContext = controllerContext;
   }

   public <R, U extends PortalURL<R, U>> String render(U url)
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
      if (url.getResource() == null)
      {
         throw new IllegalStateException("No resource set on standaloneApp URL");
      }

      // Configure mime type
      renderContext.setMimeType(url.getMimeType());

      //
      String confirm = url.getConfirm();
      boolean hasConfirm = confirm != null && confirm.length() > 0;

      //
      Boolean ajax = url.getAjax() != null && url.getAjax();
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
      for (QualifiedName parameterName : url.getParameterNames())
      {
         String parameterValue = url.getParameterValue(parameterName);
         if (parameterValue != null)
         {
            parameters.put(parameterName, parameterValue);
         }
      }

      // Render url via controller
      controllerContext.renderURL(parameters, renderContext);

      // Now append generic query parameters
      for (Map.Entry<String, String[]> entry : url.getQueryParameters().entrySet())
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

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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.RenderContext;
import org.exoplatform.web.controller.router.SimpleRenderContext;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceURL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalURL<R, L extends ResourceLocator<R>> extends ResourceURL<R, L>
{

   /** . */
   private final PortalRequestContext requestContext;

   /** . */
   private final String access;

   /** . */
   private final String siteName;

   /** . */
   private StringBuilder buffer;

   /** . */
   private SimpleRenderContext renderContext;

   public PortalURL(PortalRequestContext requestContext, L locator, Boolean ajax, String siteName, String access)
   {
      super(locator, ajax);

      //
      if (requestContext == null)
      {
         throw new NullPointerException("No null request context");
      }

      //
      this.siteName = siteName;
      this.requestContext = requestContext;
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
         buffer.append("javascript:ajaxGet('");
      }

      // julien : find out how to change the hardcoded "classic"
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(WebAppController.HANDLER_PARAM, "portal");
      parameters.put(PortalRequestHandler.ACCESS, access);
      parameters.put(PortalRequestHandler.REQUEST_SITE_NAME, siteName);

      //
      for (QualifiedName parameterName : locator.getParameterNames())
      {
         String parameterValue = locator.getParameterValue(parameterName);
         parameters.put(parameterName, parameterValue);
      }

      //
      requestContext.getControllerContext().renderURL(parameters, renderContext);

      //
      if (ajax)
      {
         buffer.append("?ajaxRequest=true");
         buffer.append("')");
      }

      //
      return buffer.toString();
   }
}

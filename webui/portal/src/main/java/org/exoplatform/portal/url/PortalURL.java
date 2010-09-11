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

   public PortalURL(PortalRequestContext requestContext, L locator, Boolean ajax)
   {
      super(locator, ajax);

      //
      if (requestContext == null)
      {
         throw new NullPointerException("No null request context");
      }

      //
      this.requestContext = requestContext;
   }

   public String toString()
   {
      //
      StringBuilder url = new StringBuilder();

      //
      if (locator.getResource() == null)
      {
         throw new IllegalStateException("No resource set on portal URL");
      }

      //
      if (ajax)
      {
         url.append("javascript:ajaxGet('");
      }

      //
      StringBuilder builder = new StringBuilder();
      try
      {
         locator.append(builder);
      }
      catch (IOException e)
      {
         AssertionError ae = new AssertionError();
         ae.initCause(e);
         throw ae;
      }

      // julien : find out how to change the hardcoded "classic"
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(PortalRequestHandler.REQUEST_PATH, builder.toString());
      parameters.put(PortalRequestHandler.REQUEST_SITE_NAME, "classic");
      parameters.put(WebAppController.HANDLER_PARAM, PortalRequestHandler.class.getSimpleName());

      //
      ControllerContext controllerContext = requestContext.getControllerContext();
      String s = controllerContext.renderURL(parameters);

      //
      url.append(s);

      //
      if (ajax)
      {
         url.append("?ajaxRequest=true");
         url.append("')");
      }

      //
      return url.toString();
   }
}

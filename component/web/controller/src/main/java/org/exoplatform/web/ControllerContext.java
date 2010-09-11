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

package org.exoplatform.web;

import org.exoplatform.web.controller.QualifiedName;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ControllerContext
{

   /** . */
   private final HttpServletRequest request;

   /** . */
   private final HttpServletResponse response;

   /** . */
   private final WebAppController controller;

   /** . */
   private final Map<QualifiedName, String> parameters;

   ControllerContext(
      WebAppController controller,
      HttpServletRequest request,
      HttpServletResponse response,
      Map<QualifiedName, String> parameters)
   {
      this.controller = controller;
      this.request = request;
      this.response = response;
      this.parameters = parameters;
   }

   public WebAppController getController()
   {
      return controller;
   }

   public HttpServletRequest getRequest()
   {
      return request;
   }

   public HttpServletResponse getResponse()
   {
      return response;
   }

   public String getParameter(QualifiedName parameter)
   {
      return parameters.get(parameter);
   }

   public String renderURL(Map<QualifiedName, String> parameters)
   {
      return request.getContextPath() +  controller.router.render(parameters);
   }
}

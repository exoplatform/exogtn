/*
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
package org.exoplatform.portal.url;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.url.URLFactory;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class PortalURLProviderFilter extends AbstractFilter
{

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {

      ExoContainer container = getContainer();

      WebAppController webAppController = (WebAppController)container.getComponentInstanceOfType(WebAppController.class);

      URLFactory urlFactory = (URLFactory)container.getComponentInstanceOfType(URLFactory.class);
      
      HttpServletRequest httpRequest = (HttpServletRequest)request;

      HttpServletResponse httpResponse = (HttpServletResponse)response;
      try
      {
         ControllerContext controllerCtx = new ControllerContext(webAppController, httpRequest, httpResponse, null);
         PortalURLProvider urlProvider = new PortalURLProvider(controllerCtx, urlFactory, null);
         PortalURLProvider.setCurrentPortalURLProvider(urlProvider);
         chain.doFilter(httpRequest, httpResponse);
      }
      finally
      {
         PortalURLProvider.setCurrentPortalURLProvider(null);
      }
      
   }

   public void destroy()
   {
   }
}

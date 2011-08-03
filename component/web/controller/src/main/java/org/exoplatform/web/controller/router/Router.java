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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * The router takes care of mapping a request to a a map.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Router
{

   /** . */
   final Route root;

   public Router(ControllerDescriptor metaData) throws MalformedRouteException
   {
      this.root = new Route();

      //
      for (RouteDescriptor routeMetaData : metaData.getRoutes())
      {
         addRoute(routeMetaData);
      }
   }

   public void addRoute(RouteDescriptor routeMetaData) throws MalformedRouteException
   {
      root.append(routeMetaData);
   }

   public void render(Map<QualifiedName, String> parameters, RenderContext renderContext)
   {
      root.render(parameters, renderContext);
   }

   public String render(Map<QualifiedName, String> parameters)
   {
      SimpleRenderContext renderContext = new SimpleRenderContext();
      render(parameters, renderContext);
      return renderContext.getPath();
   }

   public Map<QualifiedName, String> route(String path) throws IOException
   {
      return route(path, Collections.<String, String[]>emptyMap());
   }

   public Map<QualifiedName, String> route(String path, Map<String, String[]> queryParams) throws IOException
   {
      Route.Frame frame = root.route(path, queryParams);
      return frame != null ? frame.getParameters() : null;
   }

   @Override
   public String toString()
   {
      return "Router[" + root.toString() + "]";
   }
}

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

package org.exoplatform.web.controller.metadata;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.EncodingMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a route.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RouteDescriptor
{

   /** . */
   private final String path;

   /** . */
   private final Map<QualifiedName, String> params;

   /** . */
   private final Map<QualifiedName, PathParamDescriptor> pathParams;

   /** . */
   private final Map<String, RequestParamDescriptor> requestParams;

   /** . */
   private final List<RouteDescriptor> children;

   public RouteDescriptor(String path)
   {
      this.path = path;
      this.params = new HashMap<QualifiedName, String>();
      this.pathParams = new HashMap<QualifiedName, PathParamDescriptor>();
      this.requestParams = new HashMap<String, RequestParamDescriptor>();
      this.children = new ArrayList<RouteDescriptor>();
   }

   public String getPath()
   {
      return path;
   }

   public RouteDescriptor addParam(QualifiedName name, String value)
   {
      params.put(name, value);
      return this;
   }

   public RouteDescriptor addParam(String name, String value)
   {
      return addParam(QualifiedName.parse(name), value);
   }

   public Map<QualifiedName, String> getParams()
   {
      return params;
   }

   public RouteDescriptor addRequestParam(QualifiedName name, String matchName, String matchValue, boolean required)
   {
      return addRequestParam(new RequestParamDescriptor(name, matchName, matchValue, required));
   }

   public RouteDescriptor addRequestParam(RequestParamDescriptor requestParam)
   {
      requestParams.put(requestParam.getMatchName(), requestParam);
      return this;
   }

   public RouteDescriptor addPathParam(QualifiedName name, String pattern, EncodingMode encodingMode)
   {
      return addRequestParam(new PathParamDescriptor(name, pattern, encodingMode));
   }

   public RouteDescriptor addRequestParam(PathParamDescriptor requestParam)
   {
      pathParams.put(requestParam.getName(), requestParam);
      return this;
   }

   public Map<String, RequestParamDescriptor> getRequestParams()
   {
      return requestParams;
   }

   public Map<QualifiedName, PathParamDescriptor> getPathParams()
   {
      return pathParams;
   }

   public RouteDescriptor addRoute(RouteDescriptor child)
   {
      children.add(child);
      return this;
   }

   public List<RouteDescriptor> getChildren()
   {
      return children;
   }
}

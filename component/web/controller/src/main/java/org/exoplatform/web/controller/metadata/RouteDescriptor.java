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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RouteDescriptor
{

   /** . */
   private final String path;

   /** . */
   private final Map<QualifiedName, String> parameters;

   /** . */
   private final Map<QualifiedName, String> queryParams;

   /** . */
   private final List<RouteDescriptor> children;

   public RouteDescriptor(String path)
   {
      this.path = path;
      this.parameters = new HashMap<QualifiedName, String>();
      this.queryParams = new HashMap<QualifiedName, String>();
      this.children = new ArrayList<RouteDescriptor>();
   }

   public String getPath()
   {
      return path;
   }

   public RouteDescriptor addParameter(QualifiedName name, String value)
   {
      parameters.put(name, value);
      return this;
   }

   public RouteDescriptor addParameter(String name, String value)
   {
      return addParameter(new QualifiedName(name), value);
   }

   public Map<QualifiedName, String> getParameters()
   {
      return parameters;
   }

   public RouteDescriptor addQueryParam(QualifiedName name, String value)
   {
      queryParams.put(name, value);
      return this;
   }

   public RouteDescriptor addQueryParam(String name, String value)
   {
      return addQueryParam(new QualifiedName(name), value);
   }

   public Map<QualifiedName, String> getQueryParams()
   {
      return queryParams;
   }


   public RouteDescriptor addChild(RouteDescriptor child)
   {
      children.add(child);
      return this;
   }

   public List<RouteDescriptor> getChildren()
   {
      return children;
   }
}

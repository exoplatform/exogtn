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

package org.exoplatform.web.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>The controller context captures the state of a stage in the decoding of a request. The controller context
 * should not be mutated, if a new controller with modified state is needed, a new controller should be created
 * with the new state.</p>
 *
 * <p>The context augments the http request and response with additional state. Usually this state is obtained
 * from a processing context and is used by a controller to create a response.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ControllerContext
{

   /** The current path. */
   final String path;

   /** The current parameters. */
   final Map<QualifiedName, String[]> parameters;

   public ControllerContext(String path)
   {
      this.path = path;
      this.parameters = new HashMap<QualifiedName, String[]>();
   }

   public ControllerContext(String path, Map<QualifiedName, String[]> parameters)
   {
      this.path = path;
      this.parameters = parameters;
   }

   public Map<QualifiedName, String[]> getParameters()
   {
      return parameters;
   }

   public String getPath()
   {
      return path;
   }
}

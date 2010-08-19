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

package org.exoplatform.web.controller.protocol;

import org.exoplatform.web.controller.QualifiedName;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class ProcessResponse extends ControllerResponse
{

   /** . */
   private final String controllerId;

   /** . */
   private final String path;

   /** . */
   private final Map<QualifiedName, String[]> parameters;

   public ProcessResponse(String controllerId)
   {
      this(controllerId, null, Collections.<QualifiedName, String[]>emptyMap());
   }

   public ProcessResponse(String controllerId, String path)
   {
      this(controllerId, path, Collections.<QualifiedName, String[]>emptyMap());
   }

   public ProcessResponse(String controllerId, Map<QualifiedName, String[]> parameters)
   {
      this(controllerId, null, parameters);
   }

   public ProcessResponse(String controllerId, String path, Map<QualifiedName, String[]> parameters)
   {
      if (controllerId == null)
      {
         throw new NullPointerException();
      }
      if (parameters == null)
      {
         throw new NullPointerException();
      }
      this.controllerId = controllerId;
      this.path = path;
      this.parameters = parameters;
   }

   public String getPath()
   {
      return path;
   }

   public String getControllerId()
   {
      return controllerId;
   }

   public Map<QualifiedName, String[]> getParameters()
   {
      return parameters;
   }
}

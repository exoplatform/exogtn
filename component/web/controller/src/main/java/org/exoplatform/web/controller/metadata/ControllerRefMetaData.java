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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ControllerRefMetaData
{

   /** . */
   private final Map<QualifiedName, String[]> parameters;

   public ControllerRefMetaData()
   {
      this.parameters = new HashMap<QualifiedName, String[]>();
   }

   public ControllerRefMetaData addParameter(QualifiedName name, String first, String... rest)
   {
      String[] value = new String[1 + rest.length];
      value[0] = first;
      System.arraycopy(rest, 0, value, 1, rest.length);
      parameters.put(name, value);
      return this;
   }

   public ControllerRefMetaData addParameter(String name, String first, String... rest)
   {
      return addParameter(new QualifiedName(name), first, rest);
   }

   public Map<QualifiedName, String[]> getParameters()
   {
      return parameters;
   }
}

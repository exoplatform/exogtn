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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SimpleRenderContext implements RenderContext
{

   /** . */
   private StringBuilder sb = null;

   /** . */
   private Map<String, String> queryParams = Collections.emptyMap();

   public SimpleRenderContext()
   {
   }

   public SimpleRenderContext(StringBuilder sb)
   {
      this.sb = sb;
   }

   public String getPath()
   {
      return sb != null ? sb.toString() : null;
   }

   public Map<String, String> getQueryParams()
   {
      return queryParams;
   }

   public void reset()
   {
      if (sb != null)
      {
         sb.setLength(0);
      }
      if (queryParams.size() > 0)
      {
         queryParams.clear();
      }
   }

   public void appendPath(char c)
   {
      if (sb == null)
      {
         sb = new StringBuilder();
      }
      sb.append(c);
   }

   public void appendPath(String s)
   {
      if (sb == null)
      {
         sb = new StringBuilder();
      }
      sb.append(s);
   }

   public void appendQueryParameter(String parameterName, String paramaterValue)
   {
      if (queryParams.isEmpty())
      {
         queryParams = new HashMap<String, String>();
      }
      queryParams.put(parameterName, paramaterValue);
   }

}

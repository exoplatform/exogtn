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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
   private static final Map<String, String> EMPTY = Collections.emptyMap();

   /** . */
   private StringBuilder sb;

   /** . */
   private Map<String, String> queryParams;

   /** . */
   private boolean questionMarkDone;

   public SimpleRenderContext()
   {
      this(null);
   }

   public SimpleRenderContext(StringBuilder sb)
   {
      this.sb = sb;
      this.queryParams = EMPTY;
      this.questionMarkDone = false;
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
      this.questionMarkDone = false;
   }

   public void appendPath(char c, boolean escape)
   {
      appendPath(String.valueOf(c), escape);
   }

   public void appendPath(String s, boolean escape)
   {
      if (questionMarkDone)
      {
         throw new IllegalStateException("Query separator already written");
      }
      if (sb == null)
      {
         sb = new StringBuilder();
      }
      if (escape)
      {
         try
         {
            sb.append(URLEncoder.encode(s, "UTF-8"));
         }
         catch (UnsupportedEncodingException e)
         {
            throw new AssertionError(e);
         }
      }
      else
      {
         sb.append(s);
      }
   }
   public void appendQueryParameter(String parameterName, String paramaterValue)
   {
      if (queryParams == EMPTY)
      {
         queryParams = new HashMap<String, String>();
      }
      queryParams.put(parameterName, paramaterValue);
   }
}

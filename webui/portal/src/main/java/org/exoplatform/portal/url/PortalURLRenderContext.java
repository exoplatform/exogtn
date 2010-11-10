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

package org.exoplatform.portal.url;

import org.exoplatform.commons.utils.CharEncoder;
import org.exoplatform.commons.utils.CharsetCharEncoder;
import org.exoplatform.web.controller.router.RenderContext;
import org.exoplatform.web.url.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PortalURLRenderContext implements RenderContext
{

   /** . */
   private static final Map<MediaType, String> AMP_MAP = new EnumMap<MediaType, String>(MediaType.class);

   static
   {
      AMP_MAP.put(MediaType.XHTML, "&amp;");
      AMP_MAP.put(MediaType.PLAIN, "&");
   }

   /** . */
   private static final char[] ALPHABET = "0123456789ABCDEF".toCharArray();

   /** . */
   private static final List<String[]> EMPTY = Collections.emptyList();

   /** . */
   private static final CharEncoder encoder = CharsetCharEncoder.getUTF8();

   /** . */
   private StringBuilder buffer;

   /** . */
   private List<String[]> queryParams;

   /** . */
   private MediaType mimeType;


   PortalURLRenderContext(StringBuilder buffer)
   {
      this.buffer = buffer;
      this.queryParams = EMPTY;
   }

   public MediaType getMimeType()
   {
      return mimeType;
   }

   public void setMimeType(MediaType mimeType)
   {
      this.mimeType = mimeType;
   }

   public void appendSlash()
   {
      append('/', false);
   }

   public void appendPath(char c)
   {
      append(c, true);
   }

   public void appendPath(String s)
   {
      append(s, true);
   }

   public void appendQueryParameter(String parameterName, String paramaterValue)
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name accepted");
      }
      if (paramaterValue == null)
      {
         throw new NullPointerException("No null parameter value accepted");
      }

      //
      if (queryParams == EMPTY)
      {
         queryParams = new ArrayList<String[]>();
      }

      //
      queryParams.add(new String[]{parameterName,paramaterValue});
   }

   void reset()
   {
      buffer.setLength(0);
      queryParams.clear();
   }

   /**
    * Finish to write to the buffer.
    */
   void flush()
   {
      MediaType mt = mimeType;
      if (mt == null)
      {
         mt = MediaType.XHTML;
      }
      String amp = AMP_MAP.get(mt);

      //
      boolean questionMarkDone = false;
      if (queryParams.size() > 0)
      {
         for (String[] pair : queryParams)
         {
            append(questionMarkDone ? amp : "?", false);
            append(pair[0], true);
            append('=', false);
            append(pair[1], true);
            questionMarkDone = true;
         }
      }
   }

   public void appendPath(char c, boolean escape)
   {
      append(c, escape);
   }

   public void appendPath(String s, boolean escape)
   {
      append(s, escape);
   }

   /**
    * Append a string to the underlying buffer.
    *
    * @param s the string to append
    * @param encode true if the char should be pplication/x-www-form-urlencoded encoded
    */
   void append(String s, boolean encode)
   {
      for (int i = 0;i < s.length();i++)
      {
         append(s.charAt(i), encode);
      }
   }

   /**
    * Append a char to the underlying buffer.
    *
    * @param c the char to append
    * @param encode true if the char should be pplication/x-www-form-urlencoded encoded
    */
   void append(char c, boolean encode)
   {
      if (!encode || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.' || c == '-' || c == '*' || c == '_')
      {
         buffer.append(c);
      }
      else if (c == ' ')
      {
         buffer.append('+');
      }
      else
      {
         byte[] bytes = encoder.encode(c);
         buffer.append('%');
         for (byte b : bytes)
         {
            buffer.append(ALPHABET[(b & 0xF0) >> 4]);
            buffer.append(ALPHABET[b & 0xF]);
         }
      }
   }

   @Override
   public String toString()
   {
      return buffer.toString();
   }
}

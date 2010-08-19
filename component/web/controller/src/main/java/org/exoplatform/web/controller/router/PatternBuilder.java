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

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PatternBuilder
{

   /** . */
   private static final char[] TABLE = "0123456789ABCDEF".toCharArray();
   
   /** . */
   private final StringBuilder buffer = new StringBuilder();

   public void appendExpression(String s)
   {
      buffer.append(s);
   }

   public void append(String s, int from, int to)
   {
      for (int i = from;i < to;i++)
      {
         char c = s.charAt(i);
         append(c);
      }
   }

   public void append(String s, int from)
   {
      append(s, from, s.length());
   }

   public void append(char c)
   {
      buffer.append("\\u");
      buffer.append(TABLE[(c & 0xF000) >> 12]);
      buffer.append(TABLE[(c & 0x0F00) >> 8]);
      buffer.append(TABLE[(c & 0x00F0) >> 4]);
      buffer.append(TABLE[c & 0x000F]);
   }

   public Pattern build()
   {
      return Pattern.compile(buffer.toString());
   }
   
}

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

package org.exoplatform.web.controller.regexp;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Parser
{

   /** . */
   protected final CharSequence s;

   /** . */
   protected final int from;

   /** . */
   protected final int to;

   public Parser(CharSequence s)
   {
      if (s == null)
      {
         throw new NullPointerException();
      }

      //
      this.s = s;
      this.from = 0;
      this.to = s.length();
   }

   public Parser(CharSequence s, int from, int to)
   {
      if (s == null)
      {
         throw new NullPointerException();
      }
      if (to > s.length())
      {
         throw new IllegalArgumentException();
      }
      if (from < 0)
      {
         throw new IllegalArgumentException();
      }
      if (from > to)
      {
         throw new IllegalArgumentException();
      }

      //
      this.s = s;
      this.from = from;
      this.to = to;
   }

   public final int getFrom()
   {
      return from;
   }

   public final int getTo()
   {
      return to;
   }

   protected final int lastIndexOf(char c)
   {
      for (int i = to - 1;i >= from;i--)
      {
         if (s.charAt(i) == c)
         {
            return i;
         }
      }
      return -1;
   }

   protected final int indexOfBlah(int from, int to, char start, char end)
   {
      int closing = -1;
      int depth = 0;
      for (int i = from + 1;i < to;i++)
      {
         char c = s.charAt(i);
         if (c == start)
         {
            depth++;
         }
         else if (c == end)
         {
            if (depth == 0)
            {
               closing = i;
               break;
            }
            else
            {
               depth--;
            }
         }
      }
      return closing;
   }

   protected final int indexOf(int from, char c, int to)
   {
      for (int i = from;i < to;i++)
      {
         if (s.charAt(i) == c)
         {
            return i;
         }
      }
      return -1;
   }
}

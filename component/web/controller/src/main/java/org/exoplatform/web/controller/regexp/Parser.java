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

   /**
    * Find the closing char.
    *
    * @param from the index to start from, the char at this index should be the opening char
    * @param to the max exclusive value this closing char can be
    * @param openingChar the opening char
    * @param closingChar the closing char
    * @return the index of the closing char or -1
    */
   protected final int findClosing(int from, int to, char openingChar, char closingChar)
   {
      if (s.charAt(from) != openingChar)
      {
         throw new AssertionError();
      }
      int closing = -1;
      int depth = 0;
      for (int i = from + 1;i < to;i++)
      {
         char c = s.charAt(i);
         if (c == openingChar)
         {
            depth++;
         }
         else if (c == closingChar)
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

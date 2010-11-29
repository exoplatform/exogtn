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

import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;

/**
 * Various utilities related to the regular expression package.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RE
{

   private RE()
   {
   }

   /**
    * Appends a char sequence to an appendable. Chars are appended as is unless they are regular expression meta
    * characters. Meta characters are escaped by appending a <i>\</i> character before the actual character.
    *
    * @param appendable the appendable
    * @param s the char sequence
    * @param start the start offset
    * @param end the end offset
    * @throws UndeclaredIOException any io exception
    * @throws IndexOutOfBoundsException when index go out of bounds
    * @return the appendable argument
    * @param <A> the appendable parameter type
    */
   public static <A extends Appendable> A appendLiteral(A appendable, CharSequence s, int start, int end) throws UndeclaredIOException, IndexOutOfBoundsException
   {
      if (appendable == null)
      {
         throw new NullPointerException("No null appendable argument");
      }
      if (s == null)
      {
         throw new NullPointerException("No null char sequence argument");
      }
      try
      {
         while (start < end)
         {
            char c = s.charAt(start++);
            if (isMeta(c))
            {
               appendable.append('\\');
            }
            appendable.append(c);
         }
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
      return appendable;
   }

   /**
    * @see #appendLiteral(Appendable, CharSequence, int, int)
    * @param appendable the appendable
    * @param s the char sequence
    * @throws UndeclaredIOException any io exception
    * @throws IndexOutOfBoundsException when index go out of bounds
    * @return the appendable argument
    * @param <A> the appendable parameter type
    */
   public static <A extends Appendable> A appendLiteral(A appendable, CharSequence s) throws UndeclaredIOException, IndexOutOfBoundsException
   {
      if (s == null)
      {
         throw new NullPointerException("No null char sequence argument");
      }
      return appendLiteral(appendable, s, 0, s.length());
   }

   /**
    * @see #appendLiteral(Appendable, CharSequence, int, int)
    * @param appendable the appendable
    * @param c the char to append
    * @throws UndeclaredIOException any io exception
    * @throws IndexOutOfBoundsException when index go out of bounds
    * @return the appendable argument
    * @param <A> the appendable parameter type
    */
   public static <A extends Appendable> A appendLiteral(A appendable, char c) throws UndeclaredIOException
   {
      if (appendable == null)
      {
         throw new NullPointerException("No null appendable argument");
      }
      try
      {
         if (isMeta(c))
         {
            appendable.append('\\');
         }
         appendable.append(c);
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
      return appendable;
   }

   /**
    * Returns a regular expression meta character.
    *
    * @param c the char to test
    * @return true for a meta character, false otherwise
    */
   public static boolean isMeta(char c)
   {
      return c >= '(' && c <= '+' // ()*+
          || c == '?'
          || c == '{'
          || c == '|'
          || c == '$'
          || c == '^'
          || c == '.'
          || c == '['
          || c == '\\';
   }
}

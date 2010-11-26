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
public class Quantifier
{

   public enum Mode
   {

      GREEDY(""), RELUCTANT("?"), POSSESSIVE("+");

      /** . */
      private final String value;

      Mode(String value)
      {
         this.value = value;
      }
   }

   /** . */
   private final Mode mode;

   /** . */
   private final int min;

   /** . */
   private final Integer max;

   protected Quantifier(Mode mode, int min, Integer max)
   {
      this.mode = mode;
      this.min = min;
      this.max = max;
   }

   public static Quantifier onceOrNotAtAll(Mode mode)
   {
      return new Quantifier(mode, 0, 1);
   }

   public static Quantifier zeroOrMore(Mode mode)
   {
      return new Quantifier(mode, 0, null);
   }

   public static Quantifier oneOrMore(Mode mode)
   {
      return new Quantifier(mode, 1, null);
   }

   public static Quantifier exactly(Mode mode, int value)
   {
      return new Quantifier(mode, value, value);
   }

   public static Quantifier atLeast(Mode mode, int value)
   {
      return new Quantifier(mode, value, null);
   }

   public static Quantifier between(Mode mode, int min, int max)
   {
      return new Quantifier(mode, min, max);
   }

   @Override
   public boolean equals(Object o)
   {
      if (o == this)
      {
         return true;
      }
      else if (o instanceof Quantifier)
      {
         Quantifier that = (Quantifier)o;
         return mode == that.mode && min == that.min && (max == null ? that.max == null : max.equals(that.max));
      }
      return false;
   }

   @Override
   public String toString()
   {
      if (min == 0)
      {
         if (max == null)
         {
            return "*" + mode.value;
         }
         else if (max == 1)
         {
            return "?" + mode.value;
         }
      }
      else if (min == 1 && max == null)
      {
         return "+" + mode.value;
      }
      if (max == null)
      {
         return "{" + min + ",}" + mode.value;
      }
      else if (min == max)
      {
         return "{" + min + "}" + mode.value;
      }
      else
      {
         return "{" + min + "," + max + "}" + mode.value;
      }
   }
}

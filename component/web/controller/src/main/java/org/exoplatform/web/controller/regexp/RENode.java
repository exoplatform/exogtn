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

import org.hibernate.criterion.Disjunction;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class RENode
{

   public static class Disjunction extends RENode
   {

      /** . */
      private final Disjunction next;

      /** . */
      private final Alternative alternative;

      public Disjunction(Alternative alternative)
      {
         this.alternative = alternative;
         this.next = null;
      }

      public Disjunction(Alternative alternative, Disjunction next)
      {
         this.alternative = alternative;
         this.next = next;
      }

      @Override
      public String toString()
      {
         if (next != null)
         {
            return alternative + "|" + next;
         }
         else
         {
            return alternative.toString();
         }
      }
   }

   public static class Alternative extends RENode
   {

      /** . */
      private final Exp exp;

      /** . */
      private final Alternative next;

      public Alternative(Exp exp)
      {
         this.exp = exp;
         this.next = null;
      }

      public Alternative(Exp exp, Alternative next)
      {
         this.exp = exp;
         this.next = next;
      }

      @Override
      public String toString()
      {
         if (next != null)
         {
            return exp.toString() + next;
         }
         else
         {
            return exp.toString();
         }
      }
   }

   public String toString()
   {
      throw new UnsupportedOperationException();
   }

   public static abstract class Exp extends RENode
   {
   }

   public static class Assertion extends Exp
   {
      /** . */
      public static final Assertion BEGIN = new Assertion()
      {
         @Override
         public String toString()
         {
            return "<^/>";
         }
      };

      /** . */
      public static final Assertion END = new Assertion()
      {
         @Override
         public String toString()
         {
            return "<$/>";
         }
      };

      private Assertion()
      {
      }
   }

   public static final class QuantifiedExp extends Exp
   {
      /** . */
      private final Exp exp;

      /** . */
      private final Quantifier quantifier;

      public QuantifiedExp(Exp exp, Quantifier quantifier)
      {
         this.exp = exp;
         this.quantifier = quantifier;
      }

      @Override
      public String toString()
      {
         return "<" + quantifier + ">" + exp + "</" + quantifier + ">";
      }
   }

   public static abstract class Atom extends Exp
   {
   }

   public static final class Dot extends Atom
   {

      /** . */
      public static Dot INSTANCE = new Dot();

      private Dot()
      {
      }

      public String toString()
      {
         return "<./>";
      }
   }

   public static final class Group extends Atom
   {

      /** . */
      private final Disjunction disjunction;

      public Group(Disjunction disjunction)
      {
         this.disjunction = disjunction;
      }

      @Override
      public String toString()
      {
         return "<(>" + disjunction + "</)>";
      }
   }

   public static final class Character extends Atom
   {

      /** . */
      private final char value;

      public Character(char value)
      {
         this.value = value;
      }

      @Override
      public String toString()
      {
         return "<c>" + value + "</c>";
      }
   }

   public static abstract class CharacterClass extends Atom
   {

      protected abstract void toString(StringBuilder sb);

      public static class Not extends CharacterClass
      {

         /** . */
         private final CharacterClass negated;

         public Not(CharacterClass negated)
         {
            this.negated = negated;
         }

         @Override
         public String toString()
         {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append("[^");
            negated.toString(sb);
            sb.append("]");
         }
      }

      public static class Or extends CharacterClass
      {

         /** . */
         private final CharacterClass left;

         /** . */
         private final CharacterClass right;

         public Or(CharacterClass left, CharacterClass right)
         {
            this.left = left;
            this.right = right;
         }

         @Override
         public String toString()
         {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            toString(sb);
            sb.append("]");
            return sb.toString();
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            left.toString(sb);
            right.toString(sb);
         }
      }

      public static class And extends CharacterClass
      {

         /** . */
         private final CharacterClass left;

         /** . */
         private final CharacterClass right;

         public And(CharacterClass left, CharacterClass right)
         {
            this.left = left;
            this.right = right;
         }

         @Override
         public String toString()
         {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            toString(sb);
            sb.append("]");
            return sb.toString();
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            left.toString(sb);
            sb.append("&&");
            right.toString(sb);
         }
      }

      public static class Simple extends CharacterClass
      {

         /** . */
         private final char value;

         public Simple(char value)
         {
            this.value = value;
         }

         @Override
         public String toString()
         {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            toString(sb);
            sb.append("]");
            return sb.toString();
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append(value);
         }
      }

      public static class Range extends CharacterClass
      {

         /** From inclusive. */
         private final char from;

         /** To inclusive. */
         private final char to;

         public Range(char from, char to)
         {
            this.from = from;
            this.to = to;
         }

         @Override
         public String toString()
         {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            toString(sb);
            sb.append("]");
            return sb.toString();
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append(from);
            sb.append('-');
            sb.append(to);
         }
      }
   }
}

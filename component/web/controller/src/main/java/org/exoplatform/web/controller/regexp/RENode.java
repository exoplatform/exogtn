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
public abstract class RENode
{

   public abstract String toString();

   public static class Disjunction extends RENode
   {

      /** . */
      private final Disjunction next;

      /** . */
      private final Alternative alternative;

      public Disjunction(Alternative alternative)
      {
         this(alternative, null);
      }

      public Disjunction(Alternative alternative, Disjunction next)
      {
         if (alternative == null)
         {
            throw new NullPointerException();
         }

         //
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
         this(exp, null);
      }

      public Alternative(Exp exp, Alternative next)
      {
         if (exp == null)
         {
            throw new NullPointerException();
         }
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

   public static abstract class Exp extends RENode
   {

      /** . */
      Quantifier quantifier;

      @Override
      public final String toString()
      {
         StringBuilder sb = new StringBuilder();
         if (quantifier != null)
         {
            String q = quantifier.toString();
            sb.append('<').append(q).append('>');
            toString(sb);
            sb.append("</").append(q).append('>');
         }
         else
         {
            toString(sb);
         }
         return sb.toString();
      }

      protected abstract void toString(StringBuilder sb);
   }

   public static abstract class Assertion extends Exp
   {
      /** . */
      public static final Assertion BEGIN = new Assertion()
      {
         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append("<^/>");
         }
      };

      /** . */
      public static final Assertion END = new Assertion()
      {
         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append("<$/>");
         }
      };

      private Assertion()
      {
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

      @Override
      protected void toString(StringBuilder sb)
      {
         sb.append("<./>");
      }
   }

   public static final class Group extends Atom
   {

      /** . */
      private final Disjunction disjunction;

      public Group(Disjunction disjunction)
      {
         if (disjunction == null)
         {
            throw new NullPointerException();
         }
         this.disjunction = disjunction;
      }

      @Override
      protected void toString(StringBuilder sb)
      {
         sb.append("<(>").append(disjunction).append("</)>");
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
      protected void toString(StringBuilder sb)
      {
         sb.append("<c>").append(value).append("</c>");
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
            if (negated == null)
            {
               throw new NullPointerException();
            }
            this.negated = negated;
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
            if (left == null)
            {
               throw new NullPointerException();
            }
            if (right == null)
            {
               throw new NullPointerException();
            }
            this.left = left;
            this.right = right;
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append("[");
            left.toString(sb);
            right.toString(sb);
            sb.append("]");
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
            if (left == null)
            {
               throw new NullPointerException();
            }
            if (right == null)
            {
               throw new NullPointerException();
            }
            this.left = left;
            this.right = right;
         }

         @Override
         protected void toString(StringBuilder sb)
         {
            sb.append("[");
            left.toString(sb);
            sb.append("&&");
            right.toString(sb);
            sb.append("]");
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
         protected void toString(StringBuilder sb)
         {
            sb.append("[");
            sb.append(value);
            sb.append("]");
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
         protected void toString(StringBuilder sb)
         {
            sb.append("[");
            sb.append(from);
            sb.append('-');
            sb.append(to);
            sb.append("]");
         }
      }
   }
}

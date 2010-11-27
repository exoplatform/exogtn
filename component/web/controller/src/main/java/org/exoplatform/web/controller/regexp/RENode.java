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

   public static final class Disjunction extends RENode
   {

      /** . */
      private final Alternative alternative;

      /** . */
      private final Disjunction next;

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

      public Disjunction getNext()
      {
         return next;
      }

      public Alternative getAlternative()
      {
         return alternative;
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

   public static final class Alternative extends RENode
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

      public Exp getExp()
      {
         return exp;
      }

      public Alternative getNext()
      {
         return next;
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

      private Exp()
      {
      }

      public Quantifier getQuantifier()
      {
         return quantifier;
      }

      @Override
      public final String toString()
      {
         StringBuilder sb = new StringBuilder();
         if (quantifier != null)
         {
            String q = quantifier.toString();
            sb.append('<').append(q).append('>');
            writeTo(sb);
            sb.append("</").append(q).append('>');
         }
         else
         {
            writeTo(sb);
         }
         return sb.toString();
      }

      protected abstract void writeTo(StringBuilder sb);
   }

   public static abstract class Assertion extends Exp
   {

      /** . */
      public static final Assertion BEGIN = new Assertion()
      {
         @Override
         protected void writeTo(StringBuilder sb)
         {
            sb.append("<^/>");
         }
      };

      /** . */
      public static final Assertion END = new Assertion()
      {
         @Override
         protected void writeTo(StringBuilder sb)
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
      private Atom()
      {
      }
   }

   public static final class Dot extends Atom
   {

      /** . */
      public static Dot INSTANCE = new Dot();

      private Dot()
      {
      }

      @Override
      protected void writeTo(StringBuilder sb)
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

      public Disjunction getDisjunction()
      {
         return disjunction;
      }

      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append("<(>").append(disjunction).append("</)>");
      }
   }

   public static final class Character extends Atom
   {

      /** . */
      private final char value;

      public char getValue()
      {
         return value;
      }

      public Character(char value)
      {
         this.value = value;
      }

      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append("<c>").append(value).append("</c>");
      }
   }

   public static class CharacterClass extends Atom
   {

      /** . */
      private final CharacterClassExpr expr;

      protected CharacterClass(CharacterClassExpr expr)
      {
         if (expr == null)
         {
            throw new NullPointerException();
         }
         this.expr = expr;
      }

      public CharacterClassExpr getExpr()
      {
         return expr;
      }

      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append(expr);
      }
   }

   public static abstract class CharacterClassExpr extends RENode
   {

      private CharacterClassExpr()
      {
      }

      public static class Not extends CharacterClassExpr
      {

         /** . */
         private final CharacterClassExpr negated;

         public Not(CharacterClassExpr negated)
         {
            if (negated == null)
            {
               throw new NullPointerException();
            }
            this.negated = negated;
         }

         public CharacterClassExpr getNegated()
         {
            return negated;
         }

         @Override
         public String toString()
         {
            return "[^" + negated + "]";
         }
      }

      public static class Or extends CharacterClassExpr
      {

         /** . */
         private final CharacterClassExpr left;

         /** . */
         private final CharacterClassExpr right;

         public Or(CharacterClassExpr left, CharacterClassExpr right)
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

         public CharacterClassExpr getLeft()
         {
            return left;
         }

         public CharacterClassExpr getRight()
         {
            return right;
         }

         @Override
         public String toString()
         {
            return "[" + left + right + "]";
         }
      }

      public static class And extends CharacterClassExpr
      {

         /** . */
         private final CharacterClassExpr left;

         /** . */
         private final CharacterClassExpr right;

         public And(CharacterClassExpr left, CharacterClassExpr right)
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

         public CharacterClassExpr getLeft()
         {
            return left;
         }

         public CharacterClassExpr getRight()
         {
            return right;
         }

         @Override
         public String toString()
         {
            return "[" + left + "&&" + right + "]";
         }
      }

      public static class Simple extends CharacterClassExpr
      {

         /** . */
         private final char value;

         public Simple(char value)
         {
            this.value = value;
         }

         public char getValue()
         {
            return value;
         }

         @Override
         public String toString()
         {
            return "[" + value + "]";
         }
      }

      public static class Range extends CharacterClassExpr
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

         public char getFrom()
         {
            return from;
         }

         public char getTo()
         {
            return to;
         }

         @Override
         public String toString()
         {
            return "[" + from + "-" + to + "]";
         }
      }
   }
}

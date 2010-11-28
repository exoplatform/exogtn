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

   /** The owner. */
   private Ref<?> owner;

   public abstract String toString();

   public static final class Disjunction extends RENode
   {

      /** . */
      private final NonNullableRef<Alternative> alternative;

      /** . */
      private final NullableRef<Disjunction> next;

      public Disjunction(Alternative alternative)
      {
         this(alternative, null);
      }

      public Disjunction(Alternative alternative, Disjunction next)
      {
         this.alternative = new NonNullableRef<Alternative>(alternative);
         this.next = new NullableRef<Disjunction>(next);
      }

      public Alternative getAlternative()
      {
         return alternative.get();
      }

      public void setAlternative(Alternative alternative)
      {
         this.alternative.set(alternative);
      }

      public Disjunction getNext()
      {
         return next.get();
      }

      public void setNext(Disjunction next)
      {
         this.next.set(next);
      }

      @Override
      public String toString()
      {
         if (next.isNotNull())
         {
            return alternative.get() + "|" + next.get();
         }
         else
         {
            return alternative.get().toString();
         }
      }
   }

   public static final class Alternative extends RENode
   {

      /** . */
      private final Ref<Expr> exp;

      /** . */
      private final Ref<Alternative> next;

      public Alternative(Expr exp)
      {
         this(exp, null);
      }

      public Alternative(Expr exp, Alternative next)
      {
         this.exp = new NonNullableRef<Expr>(exp);
         this.next = new NullableRef<Alternative>(next);
      }

      public Expr getExp()
      {
         return exp.get();
      }

      public void setExp(Expr exp)
      {
         this.exp.set(exp);
      }

      public Alternative getNext()
      {
         return next.get();
      }

      public void setNext(Alternative next)
      {
         this.next.set(next);
      }

      @Override
      public String toString()
      {
         if (next.isNotNull())
         {
            return exp.get().toString() + next.get();
         }
         else
         {
            return exp.get().toString();
         }
      }
   }

   public static abstract class Expr extends RENode
   {

      /** . */
      private Quantifier quantifier;

      private Expr()
      {
      }

      public final Quantifier getQuantifier()
      {
         return quantifier;
      }

      public final void setQuantifier(Quantifier quantifier)
      {
         this.quantifier = quantifier;
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

   public static abstract class Assertion extends Expr
   {

      private Assertion()
      {
      }

      public static final class Begin extends Assertion
      {
         @Override
         protected void writeTo(StringBuilder sb)
         {
            sb.append("<^/>");
         }
      }

      public static final class End extends Assertion
      {
         @Override
         protected void writeTo(StringBuilder sb)
         {
            sb.append("<$/>");
         }
      }
   }

   public static abstract class Atom extends Expr
   {
      private Atom()
      {
      }
   }

   public static final class Dot extends Atom
   {
      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append("<./>");
      }
   }

   public static final class Group extends Atom
   {

      /** . */
      private final Ref<Disjunction> disjunction;

      /** . */
      private boolean capturing;

      public Group(Disjunction disjunction, boolean capturing)
      {
         this.disjunction = new NonNullableRef<Disjunction>(disjunction);
         this.capturing = capturing;
      }

      public Disjunction getDisjunction()
      {
         return disjunction.get();
      }

      public void setDisjunction(Disjunction disjunction)
      {
         this.disjunction.set(disjunction);
      }

      public boolean isCapturing()
      {
         return capturing;
      }

      public void setCapturing(boolean capturing)
      {
         this.capturing = capturing;
      }

      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append("<").append(capturing ? "(" : "(?:").append('>').append(disjunction.get()).append("</").append(capturing ? ")" : ":?)").append(">");
      }
   }

   public static final class Character extends Atom
   {

      /** . */
      private char value;

      public Character(char value)
      {
         this.value = value;
      }

      public char getValue()
      {
         return value;
      }

      public void setValue(char value)
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
      private final Ref<CharacterClassExpr> expr;

      protected CharacterClass(CharacterClassExpr expr)
      {
         this.expr = new NonNullableRef<CharacterClassExpr>(expr);
      }

      public CharacterClassExpr getExpr()
      {
         return expr.get();
      }

      public void setExpr(CharacterClassExpr expr)
      {
         this.expr.set(expr);
      }

      @Override
      protected void writeTo(StringBuilder sb)
      {
         sb.append(expr.get());
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
         private final Ref<CharacterClassExpr> negated;

         public Not(CharacterClassExpr negated)
         {
            this.negated = new NonNullableRef<CharacterClassExpr>(negated);
         }

         public CharacterClassExpr getNegated()
         {
            return negated.get();
         }

         public void setNegated(CharacterClassExpr negated)
         {
            this.negated.set(negated);
         }

         @Override
         public String toString()
         {
            return "[^" + negated.get() + "]";
         }
      }

      public static class Or extends CharacterClassExpr
      {

         /** . */
         private final Ref<CharacterClassExpr> left;

         /** . */
         private final Ref<CharacterClassExpr> right;

         public Or(CharacterClassExpr left, CharacterClassExpr right)
         {
            this.left = new NonNullableRef<CharacterClassExpr>(left);
            this.right = new NonNullableRef<CharacterClassExpr>(right);
         }

         public CharacterClassExpr getLeft()
         {
            return left.get();
         }

         public void setLeft(CharacterClassExpr left)
         {
            this.left.set(left);
         }

         public CharacterClassExpr getRight()
         {
            return right.get();
         }

         public void setRight(CharacterClassExpr right)
         {
            this.right.set(right);
         }

         @Override
         public String toString()
         {
            return "[" + left.get() + right.get() + "]";
         }
      }

      public static class And extends CharacterClassExpr
      {

         /** . */
         private final Ref<CharacterClassExpr> left;

         /** . */
         private final Ref<CharacterClassExpr> right;

         public And(CharacterClassExpr left, CharacterClassExpr right)
         {
            this.left = new NonNullableRef<CharacterClassExpr>(left);
            this.right = new NonNullableRef<CharacterClassExpr>(right);
         }

         public CharacterClassExpr getLeft()
         {
            return left.get();
         }

         public void setLeft(CharacterClassExpr left)
         {
            this.left.set(left);
         }

         public CharacterClassExpr getRight()
         {
            return right.get();
         }

         public void setRight(CharacterClassExpr right)
         {
            this.right.set(right);
         }

         @Override
         public String toString()
         {
            return "[" + left.get() + "&&" + right.get() + "]";
         }
      }

      public static class Simple extends CharacterClassExpr
      {

         /** . */
         private char value;

         public Simple(char value)
         {
            this.value = value;
         }

         public char getValue()
         {
            return value;
         }

         public void setValue(char value)
         {
            this.value = value;
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
         private char from;

         /** To inclusive. */
         private char to;

         public Range(char from, char to)
         {
            this.from = from;
            this.to = to;
         }

         public char getFrom()
         {
            return from;
         }

         public void setFrom(char from)
         {
            this.from = from;
         }

         public char getTo()
         {
            return to;
         }

         public void setTo(char to)
         {
            this.to = to;
         }

         @Override
         public String toString()
         {
            return "[" + from + "-" + to + "]";
         }
      }
   }

   protected abstract class Ref<N extends RENode>
   {
      protected abstract Ref<N> set(N node);
      protected abstract N get();
      protected final boolean isNull()
      {
         return get() == null;
      }
      protected final boolean isNotNull()
      {
         return get() != null;
      }
   }

   protected class NullableRef<N extends RENode> extends Ref<N>
   {

      /** . */
      private N node;

      public NullableRef()
      {
         this(null);
      }

      public NullableRef(N node)
      {
         if (node != null && node.owner != null)
         {
            throw new IllegalArgumentException();
         }
         this.node = node;
      }

      @Override
      protected Ref<N> set(N node)
      {
         if (node != null && node.owner != null)
         {
            throw new IllegalArgumentException();
         }
         if (this.node != null)
         {
            this.node.owner = null;
         }
         if (node != null)
         {
            node.owner = this;
            this.node = node;
         }
         else
         {
            this.node = null;
         }
         return this;
      }

      @Override
      protected N get()
      {
         return node;
      }
   }

   protected class NonNullableRef<N extends RENode> extends Ref<N>
   {

      /** . */
      private N node;

      public NonNullableRef(N node)
      {
         if (node == null)
         {
            throw new NullPointerException();
         }
         if (node.owner != null)
         {
            throw new IllegalArgumentException();
         }
         node.owner = this;
         this.node = node;
      }

      @Override
      protected Ref<N> set(N node)
      {
         if (node == null)
         {
            throw new NullPointerException();
         }
         if (node.owner != null)
         {
            throw new IllegalArgumentException();
         }
         this.node.owner = null;
         node.owner = this;
         this.node = node;
         return this;
      }

      @Override
      protected N get()
      {
         return node;
      }
   }
}

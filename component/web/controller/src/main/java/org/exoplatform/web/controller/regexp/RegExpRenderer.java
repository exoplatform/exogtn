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

import java.io.IOException;

/**
 * Renders a {@link RENode} to its pattern representation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegExpRenderer
{

   public <A extends Appendable> A render(RENode re, A appendable) throws IOException, NullPointerException
   {
      if (re == null)
      {
         throw new NullPointerException("No null disjunction accepted");
      }
      if (appendable == null)
      {
         throw new NullPointerException("No null appendable accepted");
      }

      //
      if (re instanceof RENode.Disjunction)
      {
         render((RENode.Disjunction)re, appendable);
      }
      else if (re instanceof RENode.Alternative)
      {
         render((RENode.Alternative)re, appendable);
      }
      else if (re instanceof RENode.Expr)
      {
         render((RENode.Expr)re, appendable);
      }
      else if (re instanceof RENode.CharacterClassExpr)
      {
         render((RENode.CharacterClassExpr)re, appendable);
      }
      else
      {
         throw new AssertionError();
      }

      //
      return appendable;
   }

   private void render(RENode.Disjunction disjunction, Appendable appendable) throws IOException, NullPointerException
   {
      render(disjunction.getAlternative(), appendable);
      RENode.Disjunction next = disjunction.getNext();
      if (next != null)
      {
         appendable.append('|');
         render(next, appendable);
      }
   }

   private void render(RENode.Alternative alternative, Appendable appendable) throws IOException, NullPointerException
   {
      render(alternative.getExp(), appendable);
      RENode.Alternative next = alternative.getNext();
      if (next != null)
      {
         render(next, appendable);
      }
   }

   private void render(RENode.Expr expression, Appendable appendable) throws IOException, NullPointerException
   {
      Quantifier quantifier = null;
      if (expression instanceof RENode.Any)
      {
         appendable.append('.');
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Group)
      {
         RENode.Group group = (RENode.Group)expression;
         appendable.append(group.getType().getOpen());
         this.render(group.getDisjunction(), appendable);
         appendable.append(group.getType().getClose());
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Char)
      {
         RENode.Char character = (RENode.Char)expression;
         appendable.append(character.getValue());
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.CharacterClass)
      {
         RENode.CharacterClass characterClass = (RENode.CharacterClass)expression;
         render(characterClass.getExpr(), appendable);
         quantifier = expression.getQuantifier();
      }

      //
      if (quantifier != null)
      {
         appendable.append(quantifier.toString());
      }
   }

   private void render(RENode.CharacterClassExpr expr, Appendable appendable) throws IOException, NullPointerException
   {
      appendable.append("[");
      render(expr, true, appendable);
      appendable.append("]");
   }

   private void render(RENode.CharacterClassExpr expr, boolean braced, Appendable appendable) throws IOException, NullPointerException
   {
      if (expr instanceof RENode.CharacterClassExpr.Char)
      {
         RENode.CharacterClassExpr.Char simple = (RENode.CharacterClassExpr.Char)expr;
         appendable.append(simple.getValue());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Range)
      {
         RENode.CharacterClass.CharacterClassExpr.Range range = (RENode.CharacterClass.CharacterClassExpr.Range)expr;
         appendable.append(range.getFrom());
         appendable.append('-');
         appendable.append(range.getTo());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.And)
      {
         RENode.CharacterClass.CharacterClassExpr.And and = (RENode.CharacterClass.CharacterClassExpr.And)expr;
         render(and.getLeft(), false, appendable);
         appendable.append("&&");
         render(and.getRight(), false, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Or)
      {
         RENode.CharacterClass.CharacterClassExpr.Or or = (RENode.CharacterClass.CharacterClassExpr.Or)expr;
         render(or.getLeft(), false, appendable);
         render(or.getRight(), false, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Not)
      {
         RENode.CharacterClass.CharacterClassExpr.Not not = (RENode.CharacterClass.CharacterClassExpr.Not)expr;
         if (!braced)
         {
            appendable.append("[");
         }
         appendable.append("^");
         render(not.getNegated(), false, appendable);
         if (!braced)
         {
            appendable.append(']');
         }
      }
   }
}

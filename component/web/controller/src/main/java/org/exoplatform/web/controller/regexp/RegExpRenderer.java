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

   public final <A extends Appendable> A render(RENode re, A appendable) throws IOException, NullPointerException
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
      doRender(re, appendable);

      //
      return appendable;
   }

   private <A extends Appendable> void doRender(RENode re, A appendable) throws IOException
   {
      if (re instanceof RENode.Disjunction)
      {
         doRender((RENode.Disjunction) re, appendable);
      }
      else if (re instanceof RENode.Alternative)
      {
         doRender((RENode.Alternative) re, appendable);
      }
      else if (re instanceof RENode.Expr)
      {
         doRender((RENode.Expr) re, appendable);
      }
      else
      {
         throw new AssertionError();
      }
   }

   protected void doRender(RENode.Disjunction disjunction, Appendable appendable) throws IOException, NullPointerException
   {
      doRender(disjunction.getAlternative(), appendable);
      RENode.Disjunction next = disjunction.getNext();
      if (next != null)
      {
         appendable.append('|');
         doRender(next, appendable);
      }
   }

   protected void doRender(RENode.Alternative alternative, Appendable appendable) throws IOException, NullPointerException
   {
      doRender(alternative.getExp(), appendable);
      RENode.Alternative next = alternative.getNext();
      if (next != null)
      {
         doRender(next, appendable);
      }
   }

   protected void doRender(RENode.Expr expr, Appendable appendable) throws IOException, NullPointerException
   {
      if (expr instanceof RENode.Any)
      {
         doRender((RENode.Any) expr, appendable);
      }
      else if (expr instanceof RENode.Group)
      {
         doRender((RENode.Group)expr, appendable);
      }
      else if (expr instanceof RENode.Char)
      {
         doRender((RENode.Char)expr, appendable);
      }
      else if (expr instanceof RENode.CharacterClass)
      {
         doRender((RENode.CharacterClass)expr, appendable);
      }
      else
      {
         throw new AssertionError();
      }
   }

   protected void doRender(RENode.Char expr, Appendable appendable) throws IOException
   {
      appendable.append(expr.getValue());
      if (expr.getQuantifier() != null)
      {
         appendable.append(expr.getQuantifier().toString());
      }
   }

   protected void doRender(RENode.Group expr, Appendable appendable) throws IOException
   {
      appendable.append(expr.getType().getOpen());
      this.doRender(expr.getDisjunction(), appendable);
      appendable.append(expr.getType().getClose());
      if (expr.getQuantifier() != null)
      {
         appendable.append(expr.getQuantifier().toString());
      }
   }

   protected void doRender(RENode.Any expr, Appendable appendable) throws IOException
   {
      appendable.append('.');
      if (expr.getQuantifier() != null)
      {
         appendable.append(expr.getQuantifier().toString());
      }
   }

   protected void doRender(RENode.CharacterClass expr, Appendable appendable) throws IOException
   {
      appendable.append("[");
      doRender(expr.getExpr(), appendable);
      appendable.append("]");
      if (expr.getQuantifier() != null)
      {
         appendable.append(expr.getQuantifier().toString());
      }
   }

   protected void doRender(RENode.CharacterClassExpr expr, Appendable appendable) throws IOException, NullPointerException
   {
      if (expr instanceof RENode.CharacterClassExpr.Char)
      {
         doRender((RENode.CharacterClassExpr.Char) expr, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Range)
      {
         doRender((RENode.CharacterClassExpr.Range) expr, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.And)
      {
         doRender((RENode.CharacterClassExpr.And) expr, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Or)
      {
         doRender((RENode.CharacterClassExpr.Or) expr, appendable);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Not)
      {
         doRender((RENode.CharacterClassExpr.Not) expr, appendable);
      }
      else
      {
         throw new AssertionError();
      }
   }

   protected void doRender(RENode.CharacterClassExpr.Not expr, Appendable appendable) throws IOException
   {
      boolean needBrace = false;
      for (RENode current = expr.getParent();current != null;current = current.getParent())
      {
         if (current instanceof RENode.CharacterClassExpr.Or)
         {
            needBrace = true;
            break;
         }
         else if (current instanceof RENode.CharacterClassExpr.And)
         {
            needBrace = true;
            break;
         }
         else if (current instanceof RENode.CharacterClassExpr.Not)
         {
            needBrace = true;
            break;
         }
      }
      if (needBrace)
      {
         appendable.append("[");
      }
      appendable.append("^");
      doRender(expr.getNegated(), appendable);
      if (needBrace)
      {
         appendable.append(']');
      }
   }

   protected void doRender(RENode.CharacterClassExpr.Or expr, Appendable appendable) throws IOException
   {
      doRender(expr.getLeft(), appendable);
      doRender(expr.getRight(), appendable);
   }

   protected void doRender(RENode.CharacterClassExpr.And expr, Appendable appendable) throws IOException
   {
      doRender(expr.getLeft(), appendable);
      appendable.append("&&");
      doRender(expr.getRight(), appendable);
   }

   protected void doRender(RENode.CharacterClassExpr.Range expr, Appendable appendable) throws IOException
   {
      appendable.append(expr.getFrom());
      appendable.append('-');
      appendable.append(expr.getTo());
   }

   protected void doRender(RENode.CharacterClassExpr.Char expr, Appendable appendable) throws IOException
   {
      appendable.append(expr.getValue());
   }
}

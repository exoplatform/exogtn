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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.regexp.Quantifier;
import org.exoplatform.web.controller.regexp.RENode;
import org.exoplatform.web.controller.regexp.RegExpParser;
import org.exoplatform.web.controller.regexp.SyntaxException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegExpAnalyser
{

   /** . */
   private StringBuilder pattern;

   /** . */
   private boolean needReset;

   public RegExpAnalyser()
   {
      this.pattern = new StringBuilder();
      this.needReset = false;
   }

   public String getPattern()
   {
      return pattern.toString();
   }

   public RegExpAnalyser reset()
   {
      pattern.setLength(0);
      needReset = false;
      return this;
   }

   public void process(CharSequence pattern) throws MalformedRegExpException
   {
      try
      {
         RegExpParser parser = new RegExpParser(pattern);
         RENode.Disjunction disjunction = parser.parseDisjunction();
         process(disjunction);
      }
      catch (SyntaxException e)
      {
         throw new MalformedRegExpException(e);
      }
   }

   public void process(RENode.Disjunction disjunction) throws MalformedRegExpException
   {
      if (needReset)
      {
         throw new IllegalStateException();
      }

      //
      needReset = true;
      visit(disjunction);
   }

   private void visit(RENode.Disjunction disjunction) throws MalformedRegExpException
   {
      visit(disjunction.getAlternative());
      RENode.Disjunction next = disjunction.getNext();
      if (next != null)
      {
         pattern.append('|');
         visit(next);
      }
   }

   private void visit(RENode.Alternative alternative) throws MalformedRegExpException
   {
      visit(alternative.getExp());
      RENode.Alternative next = alternative.getNext();
      if (next != null)
      {
         visit(next);
      }
   }

   private void visit(RENode.Exp expression) throws MalformedRegExpException
   {
      Quantifier quantifier = null;
      if (expression instanceof RENode.Dot)
      {
         pattern.append('.');
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Group)
      {
         RENode.Group group = (RENode.Group)expression;
         pattern.append("(?:");
         visit(group.getDisjunction());
         pattern.append(")");
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Character)
      {
         RENode.Character character = (RENode.Character)expression;
         pattern.append(character.getValue());
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.CharacterClass)
      {
         RENode.CharacterClass characterClass = (RENode.CharacterClass)expression;
         pattern.append("[");
         visit(characterClass.getExpr(), true);
         pattern.append("]");
         quantifier = expression.getQuantifier();
      }

      //
      if (quantifier != null)
      {
         pattern.append(quantifier);
      }
   }

   private void visit(RENode.CharacterClassExpr expr, boolean braced)
   {
      if (expr instanceof RENode.CharacterClassExpr.Simple)
      {
         RENode.CharacterClass.CharacterClassExpr.Simple simple = (RENode.CharacterClass.CharacterClassExpr.Simple)expr;
         pattern.append(simple.getValue());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Range)
      {
         RENode.CharacterClass.CharacterClassExpr.Range range = (RENode.CharacterClass.CharacterClassExpr.Range)expr;
         pattern.append(range.getFrom());
         pattern.append('-');
         pattern.append(range.getTo());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.And)
      {
         RENode.CharacterClass.CharacterClassExpr.And and = (RENode.CharacterClass.CharacterClassExpr.And)expr;
         visit(and.getLeft(), false);
         pattern.append("&&");
         visit(and.getRight(), false);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Or)
      {
         RENode.CharacterClass.CharacterClassExpr.Or or = (RENode.CharacterClass.CharacterClassExpr.Or)expr;
         visit(or.getLeft(), false);
         visit(or.getRight(), false);
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Not)
      {
         RENode.CharacterClass.CharacterClassExpr.Not not = (RENode.CharacterClass.CharacterClassExpr.Not)expr;
         if (!braced)
         {
            pattern.append("[");
         }
         pattern.append("^");
         visit(not.getNegated(), false);
         if (!braced)
         {
            pattern.append(']');
         }
      }
   }
}

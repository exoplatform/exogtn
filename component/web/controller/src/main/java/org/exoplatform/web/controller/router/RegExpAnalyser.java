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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegExpAnalyser
{

   /** . */
   private StringBuilder sb;

   /** . */
   private boolean groupContainer;

   /** . */
   private boolean needReset;

   public RegExpAnalyser()
   {
      this.sb = new StringBuilder();
      this.groupContainer = false;
      this.needReset = false;
   }

   public boolean isGroupContainer()
   {
      return groupContainer;
   }

   public String getPattern()
   {
      return sb.toString();
   }

   public RegExpAnalyser reset()
   {
      sb.setLength(0);
      groupContainer = false;
      needReset = false;
      return this;
   }

   public void process(CharSequence pattern)
   {
      RegExpParser parser = new RegExpParser(pattern);
      RENode.Disjunction disjunction = parser.parseDisjunction();
      process(disjunction);
   }

   public void process(RENode.Disjunction disjunction)
   {
      if (needReset)
      {
         throw new IllegalStateException();
      }

      //
      needReset = true;
      visit(disjunction);
   }

   private void visit(RENode.Disjunction disjunction)
   {
      visit(disjunction.getAlternative());
      RENode.Disjunction next = disjunction.getNext();
      if (next != null)
      {
         sb.append('|');
         visit(next);
      }
   }

   private void visit(RENode.Alternative alternative)
   {
      visit(alternative.getExp());
      RENode.Alternative next = alternative.getNext();
      if (next != null)
      {
         visit(next);
      }
   }

   private void visit(RENode.Exp expression)
   {
      Quantifier quantifier = null;
      if (expression instanceof RENode.Dot)
      {
         sb.append('.');
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Group)
      {
         RENode.Group group = (RENode.Group)expression;
         sb.append(groupContainer ? "(?:" : "(");
         groupContainer = true;
         visit(group.getDisjunction());
         sb.append(")");
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.Character)
      {
         RENode.Character character = (RENode.Character)expression;
         sb.append(character.getValue());
         quantifier = expression.getQuantifier();
      }
      else if (expression instanceof RENode.CharacterClass)
      {
         RENode.CharacterClass characterClass = (RENode.CharacterClass)expression;
         sb.append("[");
         visit(characterClass.getExpr());
         sb.append("]");
      }

      //
      if (quantifier != null)
      {
         sb.append(quantifier);
      }
   }

   private void visit(RENode.CharacterClassExpr expr)
   {
      if (expr instanceof RENode.CharacterClassExpr.Simple)
      {
         RENode.CharacterClass.CharacterClassExpr.Simple simple = (RENode.CharacterClass.CharacterClassExpr.Simple)expr;
         sb.append(simple.getValue());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Range)
      {
         RENode.CharacterClass.CharacterClassExpr.Range range = (RENode.CharacterClass.CharacterClassExpr.Range)expr;
         sb.append(range.getFrom());
         sb.append('-');
         sb.append(range.getTo());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.And)
      {
         RENode.CharacterClass.CharacterClassExpr.And and = (RENode.CharacterClass.CharacterClassExpr.And)expr;
         visit(and.getLeft());
         sb.append("&&");
         visit(and.getRight());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Or)
      {
         RENode.CharacterClass.CharacterClassExpr.Or or = (RENode.CharacterClass.CharacterClassExpr.Or)expr;
         visit(or.getLeft());
         visit(or.getRight());
      }
      else if (expr instanceof RENode.CharacterClass.CharacterClassExpr.Not)
      {
         RENode.CharacterClass.CharacterClassExpr.Not not = (RENode.CharacterClass.CharacterClassExpr.Not)expr;
         sb.append("[^");
         visit(not.getNegated());
         sb.append(']');
      }
   }
}

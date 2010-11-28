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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegExpParser extends Parser
{

   /** . */
   private static final Set<Character> ESCAPABLE = new HashSet<Character>();

   static
   {
      for (char c : "^.[$()|*+?{\\".toCharArray())
      {
         ESCAPABLE.add(c);
      }
   }

   /** . */
   private static final Pattern pattern = Pattern.compile("^([0-9]+)" + "(?:" + "(,)([0-9]+)?" + ")?$");

   /** . */
   private int index;

   public RegExpParser(CharSequence s)
   {
      super(s);

      //
      this.index = 0;
   }

   public RegExpParser(CharSequence s, int from, int to)
   {
      super(s, from, to);

      //
      this.index = from;
   }

   public int getIndex()
   {
      return index;
   }

   /**
    * disjunction -> alternative | alternative '|' disjunction
    *
    * @return the disjunction
    * @throws SyntaxException any syntax exception
    */
   public RENode.Disjunction parseDisjunction() throws SyntaxException
   {
      int pipe = indexOf('|', from);
      if (pipe == -1)
      {
         return new RENode.Disjunction(parseAlternative());
      }
      else
      {
         RegExpParser left = new RegExpParser(s, from, pipe);
         RENode.Alternative alternative = left.parseAlternative();
         RegExpParser right = new RegExpParser(s, pipe + 1, to);
         RENode.Disjunction next = right.parseDisjunction();
         index = to;
         return new RENode.Disjunction(alternative, next);
      }
   }

   /**
    * alternative -> expression | expression '|' alternative
    *
    * @return the alternative
    * @throws SyntaxException any syntax exception
    */
   public RENode.Alternative parseAlternative() throws SyntaxException
   {
      if (index < to)
      {
         RENode.Expr exp = parseExpression();
         if (index < to)
         {
            RENode.Alternative next = parseAlternative();
            return new RENode.Alternative(exp, next);
         }
         else
         {
            return new RENode.Alternative(exp);
         }
      }
      else
      {
         throw new SyntaxException();
      }
   }

   /**
    * expression        -> assertion | group | character | expression quantifier
    * group             -> '(' disjunction ')' | '(' '?' ':' disjunction ')'
    * assertion         -> '^' | '$'
    * character         -> '.' | escaped | character_class | literal
    * escaped           -> '\' any char
    * quantifier        -> quantifier_prefix | quantifier_prefix ?
    * quantifier_prefix -> '*' | '+' | '?' | '{' count '}' | '{' count ',' '}' | '{' count ',' count '}'
    *
    * @return the expression
    * @throws SyntaxException any syntax exception
    */
   public RENode.Expr parseExpression() throws SyntaxException
   {
      if (index == to)
      {
         throw new SyntaxException();
      }
      RENode.Expr exp;
      char c = s.charAt(index);
      switch (c)
      {
         case '^':
            exp = new RENode.Expr.Assertion.Begin();
            index++;
            break;
         case '$':
            exp = new RENode.Expr.Assertion.End();
            index++;
            break;
         case '(':
            int endGroup = findClosing(index, to, '(', ')');
            if (endGroup == -1)
            {
               throw new SyntaxException();
            }

            // Do we have a special construct ?
            int startGroup = index + 1;
            boolean capturing = true;
            if (startGroup < endGroup)
            {
               if (s.charAt(startGroup) == '?')
               {
                  if (startGroup + 1 < endGroup)
                  {
                     if (s.charAt(startGroup + 1) == ':')
                     {
                        // It's a non capturing group, so it's ok
                        startGroup += 2;
                        capturing = false;
                     }
                     else
                     {
                        throw createSyntaxException("Only non capturing group syntax is supported", index + 1, index + 3);
                     }
                  }
                  else
                  {
                     throw createSyntaxException("Group containing a single question mark are not allowed", index, index + 2);
                  }
               }
            }

            //
            RENode.Disjunction grouped = new RegExpParser(s, startGroup, endGroup).parseDisjunction();
            exp = new RENode.Group(grouped, capturing);
            index = endGroup + 1;
            break;
         case '*':
         case '+':
         case '?':
         case '{':
         case '|':
            throw createSyntaxException("Was not expecting the char " + c, index, index + 1);

         case '[':
            int closingBracket = findClosing(index, to, '[', ']');
            if (closingBracket == -1)
            {
               throw new SyntaxException();
            }
            exp = new RENode.CharacterClass(parseCharacterClass(index, closingBracket + 1));
            index = closingBracket + 1;
            break;
         case '\\':
            if (index +1 < to)
            {
               index++;
               char escaped = s.charAt(index);
               if (!ESCAPABLE.contains(escaped))
               {
                  throw new SyntaxException();
               }
               exp = new RENode.Character(escaped);
               index++;
               break;
            }
            else
            {
               throw new SyntaxException();
            }
         case '.':
            exp = new RENode.Dot();
            index++;
            break;
         default:
            exp = new RENode.Character(c);
            index++;
            break;
            //
      }

      //
      exp.setQuantifier(parseQuantifier());

      //
      return exp;
   }

   Quantifier parseQuantifier() throws SyntaxException
   {
      if (index < to)
      {
         char c = s.charAt(index);
         switch (c)
         {
            case '*':
               index++;
               return Quantifier.zeroOrMore(parseQuantifierMode());
            case '+':
               index++;
               return Quantifier.oneOrMore(parseQuantifierMode());
            case '?':
               index++;
               return Quantifier.onceOrNotAtAll(parseQuantifierMode());
            case '{':
               index++;
               int closingBrace = indexOf(index, '}', to);
               if (closingBrace == -1)
               {
                  throw new SyntaxException();
               }
               SubCharSequence sub = new SubCharSequence(s, index, closingBrace);
               index = closingBrace + 1;
               Matcher matcher = pattern.matcher(sub);
               if (!matcher.matches())
               {
                  throw new SyntaxException();
               }
               if (matcher.group(2) == null)
               {
                  return Quantifier.exactly(
                     parseQuantifierMode(),
                     Integer.parseInt(matcher.group(1)));
               }
               else if (matcher.group(3) == null)
               {
                  return Quantifier.atLeast(
                     parseQuantifierMode(),
                     Integer.parseInt(matcher.group(1)));
               }
               else
               {
                  return Quantifier.between(
                     parseQuantifierMode(),
                     Integer.parseInt(matcher.group(1)),
                     Integer.parseInt(matcher.group(3)));
               }
            default:
               return null;
         }
      }
      else
      {
         return null;
      }
   }

   private Quantifier.Mode parseQuantifierMode()
   {
      if (index < to)
      {
         switch (s.charAt(index))
         {
            case '?':
               index++;
               return Quantifier.Mode.RELUCTANT;
            case '+':
               index++;
               return Quantifier.Mode.POSSESSIVE;
         }
      }
      return Quantifier.Mode.GREEDY;
   }

   /**
    * character_class -> '[' bracket_list ']' | '[' '^' bracket_list ']'
    * bracket_list    -> bracket_term | bracket_term bracket_list | bracket_term '&' '&' bracket_list
    * bracket_term    -> character_class | single_term | range_term
    *
    * @param begin the begin
    * @param end the end
    * @return a character class expression
    * @throws SyntaxException any syntax exception
    */
   private RENode.CharacterClassExpr parseCharacterClass(int begin, int end) throws SyntaxException
   {
      if (begin == end)
      {
         throw new SyntaxException();
      }

      //
      if (begin < end)
      {
         RENode.CharacterClassExpr next;
         if (s.charAt(begin) == '[')
         {
            int closing = findClosing(begin, end, '[', ']');
            if (closing == -1)
            {
               throw new SyntaxException("Was expecting a closing brack");
            }

            //
            boolean matching = true;
            int nestedStart = begin + 1;
            int nestedEnd = closing;
            if (s.charAt(nestedStart) == '^' && nestedStart + 1 < nestedEnd)
            {
               nestedStart++;
               matching = false;
            }

            //
            RENode.CharacterClassExpr nested = parseCharacterClass(nestedStart, nestedEnd);
            next = matching ? nested : new RENode.CharacterClassExpr.Not(nested);
            begin = closing + 1;
         }
         else
         {
            char c = s.charAt(begin);
            begin++;
            if (begin + 1 < end && s.charAt(begin) == '-')
            {
               next = new RENode.CharacterClassExpr.Range(c, s.charAt(begin + 1));
               begin += 2;
            }
            else
            {
               next = new RENode.CharacterClassExpr.Simple(c);
            }
         }

         //
         if (begin < end)
         {
            char n = s.charAt(begin);
            if (n == '&' && begin + 1 < end && s.charAt(begin + 1) == '&')
            {
               RENode.CharacterClassExpr next2 = parseCharacterClass(begin + 2, end);
               next = new RENode.CharacterClassExpr.And(next, next2);
            }
            else
            {
               RENode.CharacterClassExpr next2 = parseCharacterClass(begin, end);
               next = new RENode.CharacterClassExpr.Or(next, next2);
            }
         }

         //
         return next;
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }
}

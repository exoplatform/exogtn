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
    */
   public RENode.Disjunction parseDisjunction()
   {
      int pipe = lastIndexOf('|');
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
    */
   public RENode.Alternative parseAlternative()
   {
      if (index < to)
      {
         RENode.Exp exp = parseExpression();
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
    * expression        -> assertion | '(' disjunction ')' | character | expression quantifier
    * assertion         -> '^' | '$'
    * character         -> '.' | escaped | character_class | literal
    * escaped           -> '\' any char
    * quantifier        -> quantifier_prefix | quantifier_prefix ?
    * quantifier_prefix -> '*' | '+' | '?' | '{' count '}' | '{' count ',' '}' | '{' count ',' count '}'
    *
    * @return the expression
    */
   public RENode.Exp parseExpression()
   {
      if (index == to)
      {
         throw new SyntaxException();
      }
      RENode.Exp exp;
      char c = s.charAt(index);
      switch (c)
      {
         case '^':
            exp = RENode.Exp.Assertion.BEGIN;
            index++;
            break;
         case '$':
            exp = RENode.Exp.Assertion.END;
            index++;
            break;
         case '(':
            int closingParenthesis = indexOfBlah(index + 1, to, '(', ')');
            if (closingParenthesis == -1)
            {
               throw new SyntaxException();
            }
            RENode.Disjunction grouped = new RegExpParser(s, index + 1, closingParenthesis).parseDisjunction();
            exp = new RENode.Group(grouped);
            index = closingParenthesis + 1;
            break;
         case '*':
         case '+':
         case '?':
         case '{':
         case '|':
            throw new SyntaxException();

         case '[':
            int closingBracket = indexOfBlah(index + 1, to, '[', ']');
            if (closingBracket == -1)
            {
               throw new SyntaxException();
            }
            exp = parseCharacterClass(index, closingBracket + 1);
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
            exp = RENode.Dot.INSTANCE;
            index++;
            break;
         default:
            exp = new RENode.Character(c);
            index++;
            break;
            //
      }

      //
      exp.quantifier = parseQuantifierSymbol();

      //
      return exp;
   }

   Quantifier parseQuantifierSymbol()
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
    * bracket_term    -> bracket_term | bracket_term bracket_term | bracket_term '&' '&' bracket_term
    * bracket_term    -> character_class | single_term | range_term
    *
    * @param begin the begin
    * @param end the end
    * @return a character class expression
    */
   private RENode.CharacterClass parseCharacterClass(int begin, int end)
   {
      if (begin == end)
      {
         throw new SyntaxException();
      }

      // Check if we do have bracket around or not
      boolean matching = true;
      if (s.charAt(begin) == '[')
      {
         if (end - begin < 3)
         {
            throw new SyntaxException();
         }
         if (s.charAt(end - 1) != ']')
         {
            throw new SyntaxException();
         }

         //
         begin++;
         end--;

         //
         if (s.charAt(begin) == '^' && begin + 1 < end)
         {
            begin++;
            matching = false;
         }
      }

      //
      //
      RENode.CharacterClass cc;
      char c = s.charAt(begin);
      begin++;
      if (begin + 1 < end && s.charAt(begin) == '-')
      {
         cc = new RENode.CharacterClass.Range(c, s.charAt(begin + 1));
         begin += 2;
      }
      else
      {
         cc = new RENode.CharacterClass.Simple(c);
      }

      //
      if (begin < end)
      {
         char n = s.charAt(begin);
         if (n == '&' && begin + 1 < end && s.charAt(begin + 1) == '&')
         {
            RENode.CharacterClass next = parseCharacterClass(begin + 2, end);
            cc = new RENode.CharacterClass.And(cc, next);
         }
         else
         {
            RENode.CharacterClass next = parseCharacterClass(begin, end);
            cc = new RENode.CharacterClass.Or(cc, next);
         }
      }

      //
      if (matching)
      {
         return cc;
      }
      else
      {
         return new RENode.CharacterClass.Not(cc);
      }
   }
}

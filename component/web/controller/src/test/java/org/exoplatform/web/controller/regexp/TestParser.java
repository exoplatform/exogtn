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

import junit.framework.TestCase;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestParser extends TestCase
{


   public void testSomeStuff()
   {
      Pattern pattern = Pattern.compile("^[\\^a\b]$");
      assertTrue(pattern.matcher("^").matches());
      assertTrue(pattern.matcher("a").matches());

   }

   private static class ParserTester
   {
      private final RegExpParser parser;
      private ParserTester(CharSequence s)
      {
         this.parser = new RegExpParser(s);
      }
      private ParserTester(CharSequence s, int from, int to)
      {
         this.parser = new RegExpParser(s, from, to);
      }
      ParserTester assertParseBracketExpression(String expectedValue)
      {
         RENode node = parser.parseExpression();
         assertTrue(node instanceof RENode.CharacterClass);
         assertEquals(expectedValue, node.toString());
         return this;
      }
      ParserTester assertParseExtendedRegExp(String expectedValue)
      {
         int expectedIndex = parser.getTo();
         RENode.Disjunction disjunction = parser.parseDisjunction();
         assertEquals(expectedValue, disjunction.toString());
         assertEquals(expectedIndex, parser.getIndex());
         return this;
      }
      ParserTester assertNotParseExtendedRegExp()
      {
         int expectedIndex = parser.getIndex();
         try
         {
            parser.parseDisjunction();
            fail();
         }
         catch (SyntaxException e)
         {
            assertEquals(expectedIndex, parser.getIndex());
         }
         return this;
      }
      ParserTester assertParseExpression(String expectedValue, int expectedIndex)
      {
         RENode.Exp exp = parser.parseExpression();
         assertEquals(expectedValue, exp.toString());
         assertEquals(expectedIndex, parser.getIndex());
         return this;
      }
      ParserTester assertNotParseEREExpression()
      {
         int index = parser.getIndex();
         try
         {
            parser.parseExpression();
            fail();
         }
         catch (SyntaxException e)
         {
            assertEquals(index, parser.getIndex());
         }
         return this;
      }
      ParserTester assertParseEREDuplSymbol(Quantifier expectedQuantifier)
      {
         int index = parser.getIndex();
         if (expectedQuantifier != null)
         {
            assertEquals(expectedQuantifier, parser.parseQuantifierSymbol());
            assertEquals(index + 1, parser.getIndex());
         }
         else
         {
            assertNull(parser.parseQuantifierSymbol());
            assertEquals(index, parser.getIndex());
         }
         return this;
      }
      ParserTester assertIndex(int expectedIndex)
      {
         assertEquals(expectedIndex, parser.getIndex());
         return this;
      }
   }

   public void testExtendedRegexp()
   {
      new ParserTester("^").assertParseExtendedRegExp("<^/>");
      new ParserTester("^$").assertParseExtendedRegExp("<^/><$/>");
      new ParserTester("a").assertParseExtendedRegExp("<c>a</c>");
      new ParserTester("a|b").assertParseExtendedRegExp("<c>a</c>|<c>b</c>");
      new ParserTester("a+|b*").assertParseExtendedRegExp("<+><c>a</c></+>|<*><c>b</c></*>");
   }
   
   public void testExpression()
   {
      new ParserTester("").assertNotParseEREExpression();
      new ParserTester("^").assertParseExpression("<^/>", 1);
      new ParserTester("^+").assertParseExpression("<+><^/></+>", 2);
      new ParserTester("$").assertParseExpression("<$/>", 1);
      new ParserTester("$+").assertParseExpression("<+><$/></+>", 2);
      new ParserTester("a").assertParseExpression("<c>a</c>", 1);
      new ParserTester("a+").assertParseExpression("<+><c>a</c></+>", 2);
      new ParserTester(".").assertParseExpression("<./>", 1);
      new ParserTester(".+").assertParseExpression("<+><./></+>", 2);
      new ParserTester("\\+").assertParseExpression("<c>+</c>", 2);
      new ParserTester("\\++").assertParseExpression("<+><c>+</c></+>", 3);
      new ParserTester("*").assertNotParseEREExpression();
      new ParserTester("+").assertNotParseEREExpression();
      new ParserTester("?").assertNotParseEREExpression();
      new ParserTester("{").assertNotParseEREExpression();
      new ParserTester("|").assertNotParseEREExpression();
      new ParserTester("(a)").assertParseExpression("<(><c>a</c></)>", 3);
      new ParserTester("(a(b)c)").assertParseExpression("<(><c>a</c><(><c>b</c></)><c>c</c></)>", 7);
   }

   // missing stuff:
   // ()
   // +?
   // {0,2}
   // escape in bracket


   public void testEREDuplSymbol()
   {
      new ParserTester("*").assertParseEREDuplSymbol(Quantifier.STAR);
      new ParserTester("+").assertParseEREDuplSymbol(Quantifier.PLUS);
      new ParserTester("?").assertParseEREDuplSymbol(Quantifier.QUESTION_MARK);
      new ParserTester("a").assertParseEREDuplSymbol(null);
      new ParserTester("").assertParseEREDuplSymbol(null);
   }

   public void testParseBracketExpression()
   {
      new ParserTester("[a]").assertParseBracketExpression("[a]");
      new ParserTester("[^a]").assertParseBracketExpression("[^a]");
      new ParserTester("[^a-b]").assertParseBracketExpression("[^a-b]");
      new ParserTester("[a-b]").assertParseBracketExpression("[a-b]");
      new ParserTester("[ab]").assertParseBracketExpression("[ab]");
      new ParserTester("[a&]").assertParseBracketExpression("[a&]");
      new ParserTester("[a&&b]").assertParseBracketExpression("[a&&b]");
      new ParserTester("[a&&[^b]]").assertParseBracketExpression("[a&&[^b]]");
      new ParserTester("[a[^b]]").assertParseBracketExpression("[a[^b]]");
      new ParserTester("[a[b]]").assertParseBracketExpression("[ab]");
      new ParserTester("[-]").assertParseBracketExpression("[-]");
      new ParserTester("[a-]").assertParseBracketExpression("[a-]");
      new ParserTester("[---]").assertParseBracketExpression("[---]");
      new ParserTester("[#--]").assertParseBracketExpression("[#--]");
   }

}

/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestRegExpRenderer extends TestCase
{

   static void assertRender(String regexp, String expected)
   {
      try
      {
         RegExpParser parser = new RegExpParser(regexp);
         RENode re = parser.parse();
         assertTrue(parser.isDone());
         String rendered;
         if (re != null)
         {
            RegExpRenderer renderer = new RegExpRenderer();
            rendered = renderer.render(re, new StringBuilder()).toString();
         }
         else
         {
            rendered = "";
         }
         assertEquals(expected, rendered);
      }
      catch (SyntaxException e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
      catch (IOException e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

   public void testSimple() throws Exception
   {
      assertRender("", "");
      assertRender(".", ".");
      assertRender("^", "^");
      assertRender("\\.", "\\.");


   }

   public void testDisjunction() throws Exception
   {
      assertRender("|", "");
      assertRender("a|", "a");
      assertRender("|a", "a");
   }
}

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

import org.exoplatform.component.test.BaseGateInTest;
import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRE extends BaseGateInTest
{

   public void testAppendLiteral1()
   {
      StringBuilder sb = new StringBuilder();
      assertSame(sb, RE.appendLiteral(sb, "a\\b"));
      assertEquals("a\\\\b", sb.toString());
   }

   public void testAppendLiteral2()
   {
      StringBuilder sb = new StringBuilder();
      assertSame(sb, RE.appendLiteral(sb, "a\\b", 0, 2));
      assertEquals("a\\\\", sb.toString());
   }

   public void testAppendLiteral3()
   {
      StringBuilder sb = new StringBuilder();
      assertSame(sb, RE.appendLiteral(sb, 'a'));
      assertSame(sb, RE.appendLiteral(sb, '\\'));
      assertEquals("a\\\\", sb.toString());
   }

   public void testAppendLiteralThrowsIOOBE()
   {
      try
      {
         RE.appendLiteral(new StringBuilder(), "a\\b", -1, 2);
         fail();
      }
      catch (IndexOutOfBoundsException expected)
      {
      }
      try
      {
         RE.appendLiteral(new StringBuilder(), "a\\b", 1, 4);
         fail();
      }
      catch (IndexOutOfBoundsException expected)
      {
      }
   }

   public void testAppendLiteralThrowsNPE()
   {
      try
      {
         RE.appendLiteral(null, "a", 0, 1);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
      try
      {
         RE.appendLiteral(new StringBuilder(), null, 0, 1);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
      try
      {
         RE.appendLiteral(null, "a");
         fail();
      }
      catch (NullPointerException expected)
      {
      }
      try
      {
         RE.appendLiteral(new StringBuilder(), null);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
      try
      {
         RE.appendLiteral(null, 'a');
         fail();
      }
      catch (NullPointerException expected)
      {
      }
   }

   public void testAppendLiteralThrowsIOE()
   {
      final IOException e = new IOException();
      Writer appendable = new Writer()
      {
         public void write(char[] cbuf, int off, int len) throws IOException { throw e; }
         public void flush() throws IOException { }
         public void close() throws IOException { }
      };
      try
      {
         RE.appendLiteral(appendable, "a", 0, 1);
         fail();
      }
      catch (UndeclaredIOException expected)
      {
         assertSame(e, expected.getCause());
      }
      try
      {
         RE.appendLiteral(appendable, "a");
         fail();
      }
      catch (UndeclaredIOException expected)
      {
         assertSame(e, expected.getCause());
      }
      try
      {
         RE.appendLiteral(appendable, 'a');
         fail();
      }
      catch (UndeclaredIOException expected)
      {
         assertSame(e, expected.getCause());
      }
   }
}

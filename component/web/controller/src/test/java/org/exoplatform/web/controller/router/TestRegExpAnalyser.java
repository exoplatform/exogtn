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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRegExpAnalyser extends TestCase
{

   /** . */
   private RegExpAnalyser analyser = new RegExpAnalyser();

   private void assertAnalyse(String expectedPattern, boolean expectedGroupContainer, String pattern)
   {
      analyser.reset();
      analyser.process(pattern);
      assertEquals(expectedPattern, analyser.getPattern());
      assertEquals(expectedGroupContainer, analyser.isGroupContainer());
   }

   public void testCharacterClass()
   {
      assertAnalyse("[a]", false, "[a]");
      assertAnalyse("[ab]", false, "[ab]");
      assertAnalyse("[ab]", false, "[a[b]]");
      assertAnalyse("[abc]", false, "[abc]");
      assertAnalyse("[abc]", false, "[[a]bc]");
      assertAnalyse("[abc]", false, "[a[b]c]");
      assertAnalyse("[abc]", false, "[ab[c]]");
      assertAnalyse("[abc]", false, "[[ab]c]");
      assertAnalyse("[abc]", false, "[a[bc]]");
      assertAnalyse("[abc]", false, "[[abc]]");
   }

   public void testGroupContainer()
   {
      assertAnalyse("(abc)", true, "(abc)");
      assertAnalyse("(a(?:bc))", true, "(a(bc))");
      assertAnalyse("(a)(?:b)", true, "(a)(b)");
   }

   public void testBilto()
   {
      assertAnalyse("[a]+", false, "[a]+");
   }
}

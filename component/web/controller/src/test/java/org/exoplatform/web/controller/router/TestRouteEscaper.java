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

import org.exoplatform.component.test.BaseGateInTest;
import org.exoplatform.web.controller.regexp.RENode;
import org.exoplatform.web.controller.regexp.RegExpParser;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRouteEscaper extends BaseGateInTest
{

   private void assertFoo(String pattern) throws Exception
   {
      RegExpParser parser = new RegExpParser(pattern);
      RouteEscaper escaper = new RouteEscaper('/', '_');
      RENode.Disjunction re = parser.parseDisjunction();
      escaper.visit(re);
      RegExpAnalyser analyser = new RegExpAnalyser();
      analyser.process(re);
      System.out.println(pattern + " --> " + analyser.getPattern());
   }

   public void testFoo() throws Exception
   {
      assertFoo("/+");
      assertFoo(".*");
      assertFoo("[a/]");
      assertFoo("[,-1]");
   }
}

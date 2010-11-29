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

import org.exoplatform.web.controller.QualifiedName;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestMatch extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      Router router = router().add(route("/")).build();

      //
      assertNull(router.route(""));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/"));
      assertNull(router.route("/a"));
      assertNull(router.route("a"));
   }

   public void testA() throws Exception
   {
      Router router = router().add(route("/a")).build();

      //
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a"));
      assertNull(router.route("a"));
      assertNull(router.route("a/"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/"));
      assertNull(router.route(""));
      assertNull(router.route("/"));
      assertNull(router.route("/b"));
      assertNull(router.route("b"));
      assertNull(router.route("/a/b"));
   }

   public void testAB() throws Exception
   {
      Router router = router().add(route("/a/b")).build();

      //
      assertNull(router.route("a/b"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b/"));
      assertNull(router.route("a/b/"));
      assertNull(router.route(""));
      assertNull(router.route("/"));
      assertNull(router.route("/b"));
      assertNull(router.route("b"));
      assertNull(router.route("/a/b/c"));
   }

   public void testParameter() throws Exception
   {
      Router router = router().add(route("/{p}")).build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
   }

   public void testParameterPropagationToDescendants() throws Exception
   {
      Router router = router().
         add(route("/").add(routeParam("p").withValue("a"))).
         add(route("/a")).build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
   }

   public void testSimplePattern() throws Exception
   {
      Router router = router().add(route("/{p}").add(pathParam("p").withPattern("a"))).build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
      assertNull(router.route("a"));
      assertNull(router.route("/ab"));
      assertNull(router.route("ab"));
   }

   public void testPrecedence() throws Exception
   {
      Router router = router().
         add(route("/a")).
         add(route("/{p}/b").add(pathParam("p").withPattern("a"))).
         build();

      //
      assertNull(router.route("a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a/b"));
   }

   public void testTwoRules1() throws Exception
   {
      Router router = router().
         add(route("/a").add(routeParam("b").withValue("b"))).
         add(route("/a/b")).
         build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("b"), "b"), router.route("/a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b"));
   }

   public void testTwoRules2() throws Exception
   {
      Router router = router().
         add(route("/{a}").add(routeParam("b").withValue("b"))).
         add(route("/{a}/b")).
         build();

      //
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("a"), "a");
      expectedParameters.put(QualifiedName.create("b"), "b");
      assertEquals(expectedParameters, router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.create("a"), "a"), router.route("/a/b"));
   }

   public void testLang() throws Exception
   {
      Router router = router().
         add(route("/{a}b").add(pathParam("a").withPattern("(([A-Za-z]{2})/)?").preservingPath())).
         build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("a"), "fr/"), router.route("/fr/b"));
      assertEquals(Collections.singletonMap(QualifiedName.create("a"), ""), router.route("/b"));
   }
}

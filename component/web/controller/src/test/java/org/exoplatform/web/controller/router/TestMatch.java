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
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.RouterDescriptor;

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
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/"));
      Router router = new Router(routerMD);

      //
      assertNull(router.route(""));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/"));
      assertNull(router.route("/a"));
      assertNull(router.route("a"));
   }

   public void testA() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a"));
      Router router = new Router(routerMD);

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
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a/b"));
      Router router = new Router( routerMD);

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
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}"));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
   }

   public void testParameterPropagationToDescendants() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/").addParam("p", "a"));
      routerMD.addRoute(new RouteDescriptor("/a"));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
   }

   public void testWildcardPattern() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}").addPathParam(QualifiedName.parse("p"), ".*", EncodingMode.PRESERVE_PATH, true));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), ""), router.route("/"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
      assertNull(router.route("a"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a/b"), router.route("/a/b"));
   }

   public void testDefaultForm() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}").addPathParam(QualifiedName.parse("p"), "[^/]+", EncodingMode.DEFAULT_FORM, true));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/"), router.route("/~"));
   }

   public void testPreservePath() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}").addPathParam(QualifiedName.parse("p"), "[^/]+", EncodingMode.PRESERVE_PATH, true));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "~"), router.route("/~"));
      assertNull(router.route("//"));
   }

   public void testSimplePattern() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}").addPathParam(QualifiedName.parse("p"), "a", EncodingMode.DEFAULT_FORM, true));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
      assertNull(router.route("a"));
      assertNull(router.route("/ab"));
      assertNull(router.route("ab"));
   }

   public void testPrecedence() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a"));
      routerMD.addRoute(new RouteDescriptor("/{p}/b").addPathParam(QualifiedName.parse("p"), "a", EncodingMode.DEFAULT_FORM, true));
      Router router = new Router(routerMD);

      //
      assertNull(router.route("a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a/b"));
   }

   public void testTwoRules1() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a").addParam("b", "b"));
      routerMD.addRoute(new RouteDescriptor("/a/b"));
      Router router = new Router(routerMD);

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("b"), "b"), router.route("/a"));
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/a/b"));
   }

   public void testTwoRules2() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{a}").addParam("b", "b"));
      routerMD.addRoute(new RouteDescriptor("/{a}/b"));
      Router router = new Router(routerMD);

      //
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("a"), "a");
      expectedParameters.put(QualifiedName.create("b"), "b");
      assertEquals(expectedParameters, router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.create("a"), "a"), router.route("/a/b"));
   }
}

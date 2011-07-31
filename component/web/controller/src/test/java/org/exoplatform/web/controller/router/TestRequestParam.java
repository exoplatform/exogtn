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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRequestParam extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      Router router = router().add(route("/").with(requestParam("foo").named("a").matchedByLiteral("a").required())).build();

      //
      assertNull(router.route("/"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext);
      assertEquals("/", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext.getQueryParams());
   }

   public void testSegment() throws Exception
   {
      Router router = router().add(route("/a").with(requestParam("foo").named("a").matchedByLiteral("a").required())).build();

      //
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext);
      assertEquals("/a", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext.getQueryParams());
   }

   public void testValuePattern() throws Exception
   {
      Router router = router().add(route("/a").with(requestParam("foo").named("a").matchedByPattern("[0-9]+").required())).build();

      //
      assertNull(router.route("/a"));
      assertNull(router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "0123"), router.route("/a", Collections.singletonMap("a", new String[]{"0123"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      assertNull(router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a")));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "12"), renderContext);
      assertEquals("/a", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "12"), renderContext.getQueryParams());
   }

   public void testPrecedence() throws Exception
   {
      Router router = router().
         add(route("/a").with(requestParam("foo").named("a").matchedByLiteral("a").required())).
         add(route("/a").with(requestParam("bar").named("b").matchedByLiteral("b").required())).
         build();

      //
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertEquals(Collections.singletonMap(QualifiedName.parse("bar"), "b"), router.route("/a", Collections.singletonMap("b", new String[]{"b"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext1 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext1);
      assertEquals("/a", renderContext1.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext1.getQueryParams());
      SimpleRenderContext renderContext2 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("bar"), "b"), renderContext2);
      assertEquals("/a", renderContext2.getPath());
      assertEquals(Collections.singletonMap("b", "b"), renderContext2.getQueryParams());
   }

   public void testInheritance() throws Exception
   {
      Router router = router().
         add(route("/a").with(requestParam("foo").named("a").matchedByLiteral("a").required()).
            sub(route("/b").with(requestParam("bar").named("b").matchedByLiteral("b").required()))).
         build();

      //
      assertNull(router.route("/a"));
      // assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertNull(router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertNull(router.route("/a/b"));
      Map<String, String[]> requestParameters = new HashMap<String, String[]>();
      requestParameters.put("a", new String[]{"a"});
      requestParameters.put("b", new String[]{"b"});
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.parse("foo"), "a");
      expectedParameters.put(QualifiedName.parse("bar"), "b");
      assertEquals(expectedParameters, router.route("/a/b", requestParameters));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext1 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext1);
      // assertEquals("/a", renderContext1.getPath());
      // assertEquals(Collections.singletonMap("a", "a"), renderContext1.getQueryParams());
      assertNull(renderContext1.getPath());
      SimpleRenderContext renderContext2 = new SimpleRenderContext();
      router.render(expectedParameters, renderContext2);
      assertEquals("/a/b", renderContext2.getPath());
      Map<String, String> expectedRequestParameters = new HashMap<String, String>();
      expectedRequestParameters.put("a", "a");
      expectedRequestParameters.put("b", "b");
      assertEquals(expectedRequestParameters, renderContext2.getQueryParams());
   }

   public void testOptional() throws Exception
   {
      Router router = router().add(route("/").
         with(requestParam("foo").named("a").matchedByLiteral("a"))).build();

      //
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/", Collections.<String, String[]>emptyMap()));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));

      //
      SimpleRenderContext renderContext1 = new SimpleRenderContext();
      router.render(Collections.<QualifiedName, String>emptyMap(), renderContext1);
      assertEquals("/", renderContext1.getPath());
      assertEquals(Collections.<String, String>emptyMap(), renderContext1.getQueryParams());
      SimpleRenderContext renderContext2 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext2);
      assertEquals("/", renderContext2.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext2.getQueryParams());
   }

   public void testMatchDescendantOfRootParameters() throws Exception
   {
      Router router = router().
         add(route("/").with(requestParam("foo").named("a").matchedByLiteral("a")).
            sub(route("/a").with(requestParam("bar").named("b").matchedByLiteral("b")))).
         build();

      //
      SimpleRenderContext renderContext = new SimpleRenderContext();
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "a");
      parameters.put(QualifiedName.parse("bar"), "b");
      router.render(parameters, renderContext);
      assertEquals("/a", renderContext.getPath());
      Map<String, String> expectedRequestParameters = new HashMap<String, String>();
      expectedRequestParameters.put("a", "a");
      expectedRequestParameters.put("b", "b");
      assertEquals(expectedRequestParameters, renderContext.getQueryParams());
   }

   public void testLiteralMatch() throws Exception
   {
      Router router = router().
         add(route("/").with(requestParam("foo").canonical().optional().named("a").matchedByLiteral("foo_value"))).
         build();

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "foo_value");
      SimpleRenderContext rc = new SimpleRenderContext();
      router.render(parameters, rc);
      assertEquals("/", rc.getPath());
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList("foo_value"), Arrays.asList(rc.getQueryParams().get("a")));
      Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"foo_value"}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("foo_value", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "bar_value");
      rc.reset();
      router.render(parameters, rc);
      assertEquals("", rc.getPath());
      assertEquals(Collections.<String>emptySet(), rc.getQueryParams().keySet());
      a = router.route("/", Collections.singletonMap("a", new String[]{"bar_value"}));
      assertNull(a);
   }

   public void testCanonical() throws Exception
   {
      Router router = router().
         add(route("/").with(requestParam("foo").canonical().optional().named("a"))).
         build();

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "bar");
      SimpleRenderContext rc = new SimpleRenderContext();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList("bar"), Arrays.asList(rc.getQueryParams().get("a")));
      Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"bar"}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("bar", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "");
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList(""), Arrays.asList(rc.getQueryParams().get("a")));
      a = router.route("/", Collections.singletonMap("a", new String[]{""}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.<String>emptySet(), rc.getQueryParams().keySet());
      a = router.route("/");
      assertNotNull(a);
      assertEquals(Collections.<QualifiedName>emptySet(), a.keySet());
   }

   public void testNeverEmpty() throws Exception
   {
      Router router = router().
         add(route("/").with(requestParam("foo").neverEmpty().optional().named("a"))).
         build();

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "bar");
      SimpleRenderContext rc = new SimpleRenderContext();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList("bar"), Arrays.asList(rc.getQueryParams().get("a")));
      Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"bar"}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("bar", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "");
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.EMPTY_MAP, rc.getQueryParams());
      a = router.route("/", Collections.singletonMap("a", new String[]{""}));
      assertNotNull(a);
      assertEquals(Collections.<QualifiedName>emptySet(), a.keySet());

      //
      parameters = new HashMap<QualifiedName, String>();
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.EMPTY_MAP, rc.getQueryParams());
      a = router.route("/");
      assertNotNull(a);
      assertEquals(Collections.<QualifiedName>emptySet(), a.keySet());
   }

   public void testNeverNull() throws Exception
   {
      Router router = router().
         add(route("/").with(requestParam("foo").neverNull().optional().named("a"))).
         build();

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "bar");
      SimpleRenderContext rc = new SimpleRenderContext();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList("bar"), Arrays.asList(rc.getQueryParams().get("a")));
      Map<QualifiedName, String> a = router.route("/", Collections.singletonMap("a", new String[]{"bar"}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("bar", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      parameters.put(QualifiedName.parse("foo"), "");
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList(""), Arrays.asList(rc.getQueryParams().get("a")));
      a = router.route("/", Collections.singletonMap("a", new String[]{""}));
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("", a.get(QualifiedName.parse("foo")));

      //
      parameters = new HashMap<QualifiedName, String>();
      rc.reset();
      router.render(parameters, rc);
      assertEquals(Collections.singleton("a"), rc.getQueryParams().keySet());
      assertEquals(Collections.singletonList(""), Arrays.asList(rc.getQueryParams().get("a")));
      a = router.route("/");
      assertNotNull(a);
      assertEquals(Collections.singleton(QualifiedName.parse("foo")), a.keySet());
      assertEquals("", a.get(QualifiedName.parse("foo")));
   }
}

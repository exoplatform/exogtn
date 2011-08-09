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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPathEncoding extends AbstractTestController
{

   public void testSegment1() throws Exception
   {
      Router router = router().add(route("/?")).build();
      assertEquals("/%3F", router.render(Collections.<QualifiedName, String>emptyMap()));
   }

   public void testSegment2() throws Exception
   {
      Router router = router().add(route("/?{p}?")).build();
      assertEquals("/%3Fa%3F", router.render(Collections.singletonMap(QualifiedName.parse("p"), "a")));
   }

   public void testParamDefaultForm() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy(".+"))).build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/"), router.route("/_"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "_"), router.route("/%5F"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "_/"), router.route("/%5F_"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/_"), router.route("/_%5F"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "?"), router.route("/%3F"));

      // Render
      assertEquals("/_", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
      assertEquals("/%5F", router.render(Collections.singletonMap(QualifiedName.create("p"), "_")));
      assertEquals("/%5F_", router.render(Collections.singletonMap(QualifiedName.create("p"), "_/")));
      assertEquals("/_%5F", router.render(Collections.singletonMap(QualifiedName.create("p"), "/_")));
      assertEquals("/%3F", router.render(Collections.singletonMap(QualifiedName.create("p"), "?")));
   }

   public void testAlternativeSlashEscape() throws Exception
   {
      Router router = router().slashEscapedBy(':').add(route("/{p}").with(pathParam("p").matchedBy(".+"))).build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/"), router.route("/:"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "_"), router.route("/_"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), ":"), router.route("/%3A"));

      // Render
      assertEquals("/:", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
      assertEquals("/_", router.render(Collections.singletonMap(QualifiedName.create("p"), "_")));
      assertEquals("/%3A", router.render(Collections.singletonMap(QualifiedName.create("p"), ":")));
   }

   public void testBug() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy("[^_]+"))).build();

      // This is a *known* bug
      assertNull(router.route("/_"));

      // This is expected
      assertEquals("/_", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));

      // This is expected
      assertNull(router.route("/%5F"));
      assertEquals("", router.render(Collections.singletonMap(QualifiedName.create("p"), "_")));
   }

   public void testParamPreservePath() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy("[^/]+").preservePath())).build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "_"), router.route("/_"));
      assertNull(router.route("//"));

      // Render
      assertEquals("", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
   }

   public void testD() throws Exception
   {
      Router router = router().
         add(route("/{p}").
            with(pathParam("p").matchedBy("/[a-z]+/[a-z]+/?"))).
         build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator"), router.route("/_platform_administrator"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator"), router.route("/_platform_administrator/"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator/"), router.route("/_platform_administrator_"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator/"), router.route("/_platform_administrator_/"));

      // Render
      assertEquals("/_platform_administrator", router.render(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator")));
      assertEquals("/_platform_administrator_", router.render(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator/")));
      assertEquals("", router.render(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator//")));
   }

   public void testWildcardPathParamWithPreservePath() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy(".*").preservePath())).build();

      // Render
      assertEquals("/", router.render(Collections.singletonMap(QualifiedName.create("p"), "")));
      assertEquals("//", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
      assertEquals("/a", router.render(Collections.singletonMap(QualifiedName.create("p"), "a")));
      assertEquals("/a/b", router.render(Collections.singletonMap(QualifiedName.create("p"), "a/b")));

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), ""), router.route("/"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/"), router.route("//"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a"), router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "a/b"), router.route("/a/b"));
   }

   public void testWildcardParamPathWithDefaultForm() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy(".*"))).build();

      //
      assertEquals("/_", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
   }

}

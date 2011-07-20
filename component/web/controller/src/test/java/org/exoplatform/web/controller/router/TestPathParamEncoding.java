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
public class TestPathParamEncoding extends AbstractTestController
{

   public void testDefaultForm() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy(".+"))).build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "/"), router.route("/_"));

      // Render
      assertEquals("/_", router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
   }

   public void testPreservePath() throws Exception
   {
      Router router = router().add(route("/{p}").with(pathParam("p").matchedBy("[^/]+").preservePath())).build();

      // Route
      assertEquals(Collections.singletonMap(QualifiedName.create("p"), "_"), router.route("/_"));
      assertNull(router.route("//"));

      // Render
      assertEquals(null, router.render(Collections.singletonMap(QualifiedName.create("p"), "/")));
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
      assertEquals(null, router.render(Collections.singletonMap(QualifiedName.create("p"), "/platform/administrator//")));
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

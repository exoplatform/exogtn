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
public class TestRequestParam extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/").addRequestParam("foo", "a", "a"));
      Router router = new Router(descriptor);
      assertNull(router.route("/"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));
   }

   public void testSegment() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam("foo", "a", "a"));
      Router router = new Router(descriptor);
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
   }

   public void testPrecedence() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam("foo", "a", "a"));
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam("bar", "b", "b"));
      Router router = new Router(descriptor);
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertEquals(Collections.singletonMap(QualifiedName.parse("bar"), "b"), router.route("/a", Collections.singletonMap("b", new String[]{"b"})));
   }

   public void testInheritance() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam("foo", "a", "a").addChild(new RouteDescriptor("/b").addRequestParam("bar", "b", "b")));
      Router router = new Router(descriptor);
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertNull(router.route("/a/b"));
      Map<String, String[]> requestParameters = new HashMap<String, String[]>();
      requestParameters.put("a", new String[]{"a"});
      requestParameters.put("b", new String[]{"b"});
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.parse("foo"), "a");
      expectedParameters.put(QualifiedName.parse("bar"), "b");
      assertEquals(expectedParameters, router.route("/a/b", requestParameters));
   }

}

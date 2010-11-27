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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRender extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/"));
      Router router = new Router(routerMD);

      //
      assertEquals("/", router.render(Collections.<QualifiedName, String>emptyMap()));
   }

   public void testA() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a"));
      Router router = new Router(routerMD);

      //
      assertEquals("/a", router.render(Collections.<QualifiedName, String>emptyMap()));
   }

   public void testAB() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a/b"));
      Router router = new Router( routerMD);

      //
      assertEquals("/a/b", router.render(Collections.<QualifiedName, String>emptyMap()));
   }

   public void testPathParam() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}"));
      Router router = new Router(routerMD);

      //
      assertEquals("/a", router.render(Collections.singletonMap(QualifiedName.create("p"), "a")));
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
   }

   public void testSimplePatternPathParam() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{p}").addPathParam(QualifiedName.parse("p"), "a"));
      Router router = new Router(routerMD);

      //
      assertEquals("/a", router.render(Collections.singletonMap(QualifiedName.create("p"), "a")));
      assertNull(router.render(Collections.singletonMap(QualifiedName.create("p"), "ab")));
   }

   public void testPrecedence() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/a"));
      routerMD.addRoute(new RouteDescriptor("/{p}/b").addPathParam(QualifiedName.parse("p"), "a"));
      Router router = new Router(routerMD);

      //
      assertEquals("/a", router.render(Collections.<QualifiedName, String>emptyMap()));

      //
      assertEquals("/a/b", router.render(Collections.singletonMap(QualifiedName.create("p"), "a")));
   }

   public void testLang() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/{a}b").addPathParam(QualifiedName.parse("a"), "(([A-Za-z]{2})/)?", EncodingMode.PRESERVE_PATH));
      Router router = new Router(routerMD);

      //
      assertEquals("/fr/b", router.render(Collections.singletonMap(QualifiedName.parse("a"), "fr/")));
      assertEquals("/b", router.render(Collections.singletonMap(QualifiedName.parse("a"), "")));
   }
}

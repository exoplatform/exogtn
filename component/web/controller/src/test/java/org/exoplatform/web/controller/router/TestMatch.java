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

import org.exoplatform.web.controller.ControllerContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.ControllerRefMetaData;
import org.exoplatform.web.controller.metadata.RouterMetaData;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestMatch extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/", new ControllerRefMetaData());
      Router router = new Router(routerMD);

      //
      assertNull(router.process(new ControllerContext("")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/")));

      //
      assertNull(router.process(new ControllerContext("/a")));

      //
      assertNull(router.process(new ControllerContext("a")));
   }

   public void testA() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/a", new ControllerRefMetaData());
      Router router = new Router(routerMD);

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a")));

      //
      assertNull(router.process(new ControllerContext("a")));

      //
      assertNull(router.process(new ControllerContext("a/")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a/")));

      //
      assertNull(router.process(new ControllerContext("")));

      //
      assertNull(router.process(new ControllerContext("/")));

      //
      assertNull(router.process(new ControllerContext("/b")));

      //
      assertNull(router.process(new ControllerContext("b")));

      //
      assertNull(router.process(new ControllerContext("/a/b")));
   }

   public void testAB() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/a/b", new ControllerRefMetaData());
      Router router = new Router( routerMD);

      //
      assertNull(router.process(new ControllerContext("a/b")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a/b")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a/b/")));

      //
      assertNull(router.process(new ControllerContext("a/b/")));

      //
      assertNull(router.process(new ControllerContext("")));

      //
      assertNull(router.process(new ControllerContext("/")));

      //
      assertNull(router.process(new ControllerContext("/b")));

      //
      assertNull(router.process(new ControllerContext("b")));

      //
      assertNull(router.process(new ControllerContext("/a/b/c")));
   }

   public void testParameter() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/{p}", new ControllerRefMetaData());
      Router router = new Router(routerMD);
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a"), router.process(new ControllerContext("/a")));
   }

   public void testParameterPropagationToDescendants() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/", new ControllerRefMetaData().addParameter("p", "a"));
      routerMD.addRoute("/a", new ControllerRefMetaData());
      Router router = new Router(routerMD);
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a"), router.process(new ControllerContext("/a")));
   }

   public void testWildcardPattern() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/{p:.*}", new ControllerRefMetaData());
      Router router = new Router(routerMD);

      //
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), ""), router.process(new ControllerContext("/")));

      //
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a"), router.process(new ControllerContext("/a")));

      //
      assertNull(router.process(new ControllerContext("a")));

      //
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a/b"), router.process(new ControllerContext("/a/b")));
   }

   public void testSimplePattern() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/{p:a}", new ControllerRefMetaData());
      Router router = new Router(routerMD);

      //
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a"), router.process(new ControllerContext("/a")));

      //
      assertNull(router.process(new ControllerContext("a")));

      //
      assertNull(router.process(new ControllerContext("/ab")));

      //
      assertNull(router.process(new ControllerContext("ab")));
   }

   public void testPrecedence() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/a", new ControllerRefMetaData());
      routerMD.addRoute("/{p:a}/b", new ControllerRefMetaData());
      Router router = new Router(routerMD);

      //
      assertNull(router.process(new ControllerContext("a")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a")));

      //
      assertProcessResponse(Collections.<QualifiedName, String>emptyMap(), router.process(new ControllerContext("/a/")));

      //
      assertProcessResponse(Collections.singletonMap(new QualifiedName("p"), "a"), router.process(new ControllerContext("/a/b")));
   }
}

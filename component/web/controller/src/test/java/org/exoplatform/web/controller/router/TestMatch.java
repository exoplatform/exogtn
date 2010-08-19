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
      routerMD.addRoute("/", new ControllerRefMetaData("ref1"));
      Router router = new Router(routerMD);

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/")));

      //
      assertNull(router.process(new ControllerContext("/a")));

      //
      assertNull(router.process(new ControllerContext("a")));
   }

   public void testA() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/a", new ControllerRefMetaData("ref1"));
      Router router = new Router(routerMD);

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("a")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("a/")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a/")));

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
      routerMD.addRoute("/a/b", new ControllerRefMetaData("ref1"));
      Router router = new Router( routerMD);

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("a/b")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a/b")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a/b/")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("a/b/")));

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
      routerMD.addRoute("/{p}", new ControllerRefMetaData("ref1"));
      Router router = new Router(routerMD);
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("/a")));
   }

   public void testWildcardPattern() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/{p:.*}", new ControllerRefMetaData("ref1"));
      Router router = new Router(routerMD);

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{""}), router.process(new ControllerContext("/")));

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("/a")));

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("a")));

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a/b"}), router.process(new ControllerContext("/a/b")));
   }

   public void testSimplePattern() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/{p:a}", new ControllerRefMetaData("ref1"));
      Router router = new Router(routerMD);

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("/a")));

      //
      assertProcessResponse("ref1", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("a")));

      //
      assertNull(router.process(new ControllerContext("/ab")));

      //
      assertNull(router.process(new ControllerContext("ab")));
   }

   public void testPrecedence() throws Exception
   {
      RouterMetaData routerMD = new RouterMetaData();
      routerMD.addRoute("/a", new ControllerRefMetaData("ref1"));
      routerMD.addRoute("/{p:a}/b", new ControllerRefMetaData("ref2"));
      Router router = new Router(routerMD);

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("a")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a")));

      //
      assertProcessResponse("ref1", Collections.<QualifiedName, String[]>emptyMap(), router.process(new ControllerContext("/a/")));

      //
      assertProcessResponse("ref2", Collections.singletonMap(new QualifiedName("p"), new String[]{"a"}), router.process(new ControllerContext("/a/b")));
   }
}

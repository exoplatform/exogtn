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
public class TestPortal extends AbstractTestController
{

   public void testLanguage1() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor().addRoute(
         new RouteDescriptor("/public{gtn:lang}").addPathParam(
         QualifiedName.parse("gtn:lang"), "(/[A-Za-z][A-Za-z])?", EncodingMode.PRESERVE_PATH)
      );
      Router router = new Router(routerMD);
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), ""), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "/fr"), router.route("/public/fr"));
   }

   public void testLanguage2() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor().addRoute(
         new RouteDescriptor("/{gtn:lang}public").addPathParam(
         QualifiedName.parse("gtn:lang"), "([A-Za-z]{2}/)?", EncodingMode.PRESERVE_PATH)
      );
      Router router = new Router(routerMD);
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), ""), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "fr/"), router.route("/fr/public"));
   }

   public void testLanguage3() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor().
         addRoute(new RouteDescriptor("/public")).
         addRoute(new RouteDescriptor("/{gtn:lang}/public").
            addPathParam(QualifiedName.parse("gtn:lang"), "([A-Za-z]{2})", EncodingMode.FORM
            )
         );
      Router router = new Router(routerMD);
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "fr"), router.route("/fr/public"));
   }

}

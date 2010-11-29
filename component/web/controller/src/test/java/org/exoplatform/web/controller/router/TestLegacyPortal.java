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

import junit.framework.TestCase;
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
public class TestLegacyPortal extends TestCase
{

   /** . */
   private Router router;

   @Override
   protected void setUp() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();

      RouteDescriptor portal = new RouteDescriptor("/").
         addRouteParam(QualifiedName.parse("gtn:handler"), "portal").
         addRequestParam(QualifiedName.parse("gtn:componentid"), "portal:componentId", null, false).
         addRequestParam(QualifiedName.parse("gtn:action"), "portal:action", null, false).
         addRequestParam(QualifiedName.parse("gtn:objectid"), "portal:objectId", null, false).
         addRoute(
            new RouteDescriptor("/public/{gtn:sitename}{gtn:path}").
               addRouteParam(QualifiedName.parse("gtn:access"), "public")).
               addPathParam(QualifiedName.parse("gtn:path"), ".*", EncodingMode.PRESERVE_PATH).
         addRoute(
            new RouteDescriptor("/private/{gtn:sitename}{gtn:path}").
               addPathParam(QualifiedName.parse("gtn:path"), ".*", EncodingMode.PRESERVE_PATH).
               addRouteParam(QualifiedName.parse("gtn:access"), "private"));

      //
      routerMD.addRoute(portal);

      //
      this.router = new Router(routerMD);
   }

   public void testPrivateClassicComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "");
      expectedParameters.put(QualifiedName.create("gtn", "componentid"), "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassic() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "");

      //
      assertEquals(expectedParameters, router.route("/private/classic"));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/");

      //
      assertEquals(expectedParameters, router.route("/private/classic/"));
      assertEquals("/private/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassicSlashComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/");
      expectedParameters.put(QualifiedName.create("gtn", "componentid"), "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic/", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassicHome() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/home");

      //
      assertEquals(expectedParameters, router.route("/private/classic/home"));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }

   public void testPrivateClassicHomeComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "handler"), "portal");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "access"), "private");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/home");
      expectedParameters.put(QualifiedName.create("gtn", "componentid"), "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic/home", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }
}

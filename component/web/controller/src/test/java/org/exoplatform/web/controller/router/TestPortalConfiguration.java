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
public class TestPortalConfiguration extends AbstractTestController
{

   /** . */
   private Router router;

   @Override
   protected void setUp() throws Exception
   {
      RouterDescriptor routerMD = new RouterDescriptor();

      //
      RouteDescriptor portalRouteMD = new RouteDescriptor("/private/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
      portalRouteMD.addParam(new QualifiedName("gtn", "sitetype"), "portal");
      portalRouteMD.addRequestParam(new QualifiedName("gtn", "componentid"), "portal:componentId", null, false);
      routerMD.addRoute(portalRouteMD);

      //
      RouteDescriptor portalRouteMD2 = new RouteDescriptor("/private/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD2.addParam(new QualifiedName("gtn", "controller"), "site");
      portalRouteMD2.addParam(new QualifiedName("gtn", "sitetype"), "portal");
      routerMD.addRoute(portalRouteMD2);

      //
      RouteDescriptor groupRouteMD = new RouteDescriptor("/groups/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
      groupRouteMD.addParam(new QualifiedName("gtn", "sitetype"), "group");
      routerMD.addRoute(groupRouteMD);

      //
      RouteDescriptor userRouteMD = new RouteDescriptor("/users/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
      userRouteMD.addParam(new QualifiedName("gtn", "sitetype"), "user");
      routerMD.addRoute(userRouteMD);

      //
      this.router = new Router(routerMD);
   }

   public void testComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "");
      expectedParameters.put(new QualifiedName("gtn", "componentid"), "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassic() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "");

      //
      assertEquals(expectedParameters, router.route("/private/classic"));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      router.route("/private/classic/");
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/");

      //
      assertEquals(expectedParameters, router.route("/private/classic/"));
      assertEquals("/private/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassicHome() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/home");

      //
      assertEquals(expectedParameters, router.route("/private/classic/home"));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }
}

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
      portalRouteMD.addParameter(new QualifiedName("gtn", "controller"), "site");
      portalRouteMD.addParameter(new QualifiedName("gtn", "sitetype"), "portal");
      routerMD.addRoute(portalRouteMD);

      //
      RouteDescriptor groupRouteMD = new RouteDescriptor("/groups/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParameter(new QualifiedName("gtn", "controller"), "site");
      groupRouteMD.addParameter(new QualifiedName("gtn", "sitetype"), "group");
      routerMD.addRoute(groupRouteMD);

      //
      RouteDescriptor userRouteMD = new RouteDescriptor("/users/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParameter(new QualifiedName("gtn", "controller"), "site");
      userRouteMD.addParameter(new QualifiedName("gtn", "sitetype"), "user");
      routerMD.addRoute(userRouteMD);

      //
      this.router = new Router(routerMD);
   }

   public void testPrivateClassic() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "");

      //
      assertEquals(expectedParameters, router.process("/private/classic"));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      router.process("/private/classic/");
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/");

      //
      assertEquals(expectedParameters, router.process("/private/classic/"));
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
      assertEquals(expectedParameters, router.process("/private/classic/home"));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }
}

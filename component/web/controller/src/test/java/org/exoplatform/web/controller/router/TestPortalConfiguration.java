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
      RouteDescriptor portalRouteMD = new RouteDescriptor("/private/{{gtn}sitetype}/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
      portalRouteMD.addRequestParam(new QualifiedName("gtn", "componentid"), "portal:componentId", null, false);
      routerMD.addRoute(portalRouteMD);

      //
      RouteDescriptor portalRouteMD2 = new RouteDescriptor("/private/{{gtn}sitetype}/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD2.addParam(new QualifiedName("gtn", "controller"), "site");
      routerMD.addRoute(portalRouteMD2);

      //
      RouteDescriptor groupRouteMD = new RouteDescriptor("/groups/{{gtn}sitetype}/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
      routerMD.addRoute(groupRouteMD);

      //
      RouteDescriptor userRouteMD = new RouteDescriptor("/users/{{gtn}sitetype}/{{gtn}sitename}{{gtn}path:.*}");
      portalRouteMD.addParam(new QualifiedName("gtn", "controller"), "site");
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
      expectedParameters.put(new QualifiedName("gtn", "path"), "/");
      expectedParameters.put(new QualifiedName("gtn", "componentid"), "foo");

      //
      assertEquals(expectedParameters, router.route("/private/portal/classic/", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/portal/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassic() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "");

      //
      assertEquals(expectedParameters, router.route("/private/portal/classic"));
      assertEquals("/private/portal/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/");

      //
      assertEquals(expectedParameters, router.route("/private/portal/classic/"));
      assertEquals("/private/portal/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassicHome() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/home");

      //
      assertEquals(expectedParameters, router.route("/private/portal/classic/home"));
      assertEquals("/private/portal/classic/home", router.render(expectedParameters));
   }
   
   public void testSiteType() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "group");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "platform_administrator");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/administration/registry");

      //
      assertEquals(expectedParameters, router.route("/private/group/platform_administrator/administration/registry"));
      assertEquals("/private/group/platform_administrator/administration/registry", router.render(expectedParameters));
      
      Map<QualifiedName, String> expectedParameters1 = new HashMap<QualifiedName, String>();
      expectedParameters1.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters1.put(new QualifiedName("gtn", "sitetype"), "user");
      expectedParameters1.put(new QualifiedName("gtn", "sitename"), "root");
      expectedParameters1.put(new QualifiedName("gtn", "path"), "/tab_0");
      
      //
      assertEquals(expectedParameters1, router.route("/private/user/root/tab_0"));
      assertEquals("/private/user/root/tab_0", router.render(expectedParameters1));
   }
}

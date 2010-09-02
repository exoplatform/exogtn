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
      RouterMetaData routerMD = new RouterMetaData();
      ControllerRefMetaData portalControllerRef = new ControllerRefMetaData();
      portalControllerRef.addParameter(new QualifiedName("gtn", "controller"), "site");
      portalControllerRef.addParameter(new QualifiedName("gtn", "sitetype"), "portal");
      routerMD.addRoute("/private/{{gtn}sitename}{{gtn}path:.*}", portalControllerRef);
      ControllerRefMetaData groupControllerRef = new ControllerRefMetaData();
      portalControllerRef.addParameter(new QualifiedName("gtn", "controller"), "site");
      groupControllerRef.addParameter(new QualifiedName("gtn", "sitetype"), "group");
      routerMD.addRoute("/groups/{{gtn}sitename}{{gtn}path:.*}", groupControllerRef);
      ControllerRefMetaData userControllerRef = new ControllerRefMetaData();
      portalControllerRef.addParameter(new QualifiedName("gtn", "controller"), "site");
      userControllerRef.addParameter(new QualifiedName("gtn", "sitetype"), "user");
      routerMD.addRoute("/users/{{gtn}sitename}{{gtn}path:.*}", userControllerRef);

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
      assertProcessResponse(expectedParameters, router.process(new ControllerContext("/private/classic")));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      router.process(new ControllerContext("/private/classic/"));
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(new QualifiedName("gtn", "controller"), "site");
      expectedParameters.put(new QualifiedName("gtn", "sitename"), "classic");
      expectedParameters.put(new QualifiedName("gtn", "sitetype"), "portal");
      expectedParameters.put(new QualifiedName("gtn", "path"), "/");

      //
      assertProcessResponse(expectedParameters, router.process(new ControllerContext("/private/classic/")));
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
      assertProcessResponse(expectedParameters, router.process(new ControllerContext("/private/classic/home")));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }
}

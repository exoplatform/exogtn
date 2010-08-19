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
import org.exoplatform.web.controller.metadata.ControllerRefMetaData;
import org.exoplatform.web.controller.metadata.RouterMetaData;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestBuildRoute extends TestCase
{

   private ControllerRefMetaData ref1 = new ControllerRefMetaData("ref1");

   public void testRoot()
   {
      String[] paths = {"/",""};
      for (String path : paths)
      {
         RouterMetaData routerMD = new RouterMetaData();
         routerMD.addRoute(path, ref1);
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         expectedRoute.controllerRef = "ref1";
         assertEquals(expectedRoute, router.root);
      }
   }

   public void testSimpleSegment()
   {
      String[] paths = {"/a","a"};
      for (String path : paths)
      {
         RouterMetaData routerMD = new RouterMetaData();
         routerMD.addRoute(path, ref1);
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         SimpleRoute a = new SimpleRoute("a");
         a.controllerRef = "ref1";
         expectedRoute.simpleRoutes.put("a", a);
         assertEquals(expectedRoute, router.root);
      }
   }
   
/*
   public void testParameterSegment()
   {
      String[] paths = {"/{a}","{a}"};
      for (String path : paths)
      {
         RouterMetaData routerMD = new RouterMetaData();
         routerMD.addRoute(path, ref1);
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         PatternRoute a = new PatternRoute(Pattern.compile("^([^/]+)"), Arrays.asList(new QualifiedName("a")));
         a.controllerRef = "ref1";
         expectedRoute.patternRoutes.add(a);
         assertEquals(expectedRoute, router.root);
      }
   }

   public void testQualifiedParameterSegment()
   {
      String[] paths = {"/{{q}a}","{{q}a}"};
      for (String path : paths)
      {
         RouterMetaData routerMD = new RouterMetaData();
         routerMD.addRoute(path, ref1);
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         PatternRoute a = new PatternRoute(Pattern.compile("^([^/]+)"), Arrays.asList(new QualifiedName("q", "a")));
         a.controllerRef = "ref1";
         expectedRoute.patternRoutes.add(a);
         assertEquals(expectedRoute, router.root);
      }
   }

   public void testPatternSegment()
   {
      String[] paths = {"/{a:.*}","{a:.*}"};
      for (String path : paths)
      {
         RouterMetaData routerMD = new RouterMetaData();
         routerMD.addRoute(path, ref1);
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         PatternRoute a = new PatternRoute(Pattern.compile("^(.*)"), Arrays.asList(new QualifiedName("a")));
         a.controllerRef = "ref1";
         expectedRoute.patternRoutes.add(a);
         assertEquals(expectedRoute, router.root);
      }
   }
*/

   private void assertEquals(Route expectedRoute, Route route)
   {
      assertEquals(expectedRoute.getClass(), route.getClass());
      assertEquals(expectedRoute.controllerRef, route.controllerRef);
      assertEquals(expectedRoute.simpleRoutes.keySet(), route.simpleRoutes.keySet());
      for (Map.Entry<String, SimpleRoute> entry : expectedRoute.simpleRoutes.entrySet())
      {
         assertEquals(entry.getValue(), expectedRoute.simpleRoutes.get(entry.getKey()));
      }
      assertEquals(expectedRoute.patternRoutes.size(), route.patternRoutes.size());
      for (int i = 0;i < expectedRoute.patternRoutes.size();i++)
      {
         assertEquals(expectedRoute.patternRoutes.get(i), route.patternRoutes.get(i));
      }
      if (route instanceof PatternRoute)
      {
         assertEquals(((PatternRoute)expectedRoute).pattern.toString(), ((PatternRoute)route).pattern.toString());
         assertEquals(((PatternRoute)expectedRoute).parameterNames, ((PatternRoute)route).parameterNames);
      }
      else if (route instanceof SimpleRoute)
      {
         assertEquals(((SimpleRoute)expectedRoute).value, ((SimpleRoute)route).value);
      }
   }
}

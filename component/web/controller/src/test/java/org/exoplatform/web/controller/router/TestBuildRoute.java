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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestBuildRoute extends TestCase
{

   public void testRoot()
   {
      String[] paths = {"/",""};
      for (String path : paths)
      {
         RouterDescriptor routerMD = new RouterDescriptor();
         routerMD.addRoute(new RouteDescriptor(path));
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         assertEquals(expectedRoute, router.root);
      }
   }

   public void testSimpleSegment()
   {
      String[] paths = {"/a","a"};
      for (String path : paths)
      {
         RouterDescriptor routerMD = new RouterDescriptor();
         routerMD.addRoute(new RouteDescriptor(path));
         Router router = new Router(routerMD);
         Route expectedRoute = new Route();
         SimpleRoute a = new SimpleRoute(expectedRoute, "a");
         expectedRoute.simpleRoutes.put("a", Arrays.asList(a));
         assertEquals(expectedRoute, router.root);
      }
   }
   
   public void testParameterSegment()
   {
      String[] paths = {"/{a}","{a}"};
      for (String path : paths)
      {
         RouterDescriptor routerMD = new RouterDescriptor();
         routerMD.addRoute(new RouteDescriptor(path));
         Router router = new Router(routerMD);

         //
         assertEquals(0, router.root.simpleRoutes.size());
         assertEquals(1, router.root.patternRoutes.size());
         PatternRoute patternRoute = router.root.patternRoutes.get(0);
         assertEquals("^([^/]+)", patternRoute.pattern.toString());
         assertEquals(Collections.singletonList(new QualifiedName("a")), patternRoute.parameterNames);
         assertEquals(1, patternRoute.parameterPatterns.size());
         assertEquals("^[^/]+$", patternRoute.parameterPatterns.get(0).toString());
         assertEquals(2, patternRoute.chunks.size());
         assertEquals("", patternRoute.chunks.get(0));
         assertEquals("", patternRoute.chunks.get(1));
      }
   }

   public void testQualifiedParameterSegment()
   {
      String[] paths = {"/{{q}a}","{{q}a}"};
      for (String path : paths)
      {
         RouterDescriptor routerMD = new RouterDescriptor();
         routerMD.addRoute(new RouteDescriptor(path));
         Router router = new Router(routerMD);

         //
         assertEquals(0, router.root.simpleRoutes.size());
         assertEquals(1, router.root.patternRoutes.size());
         PatternRoute patternRoute = router.root.patternRoutes.get(0);
         assertEquals("^([^/]+)", patternRoute.pattern.toString());
         assertEquals(Collections.singletonList(new QualifiedName("q", "a")), patternRoute.parameterNames);
         assertEquals(1, patternRoute.parameterPatterns.size());
         assertEquals("^[^/]+$", patternRoute.parameterPatterns.get(0).toString());
         assertEquals(2, patternRoute.chunks.size());
         assertEquals("", patternRoute.chunks.get(0));
         assertEquals("", patternRoute.chunks.get(1));
      }
   }

   public void testPatternSegment()
   {
      String[] paths = {"/{a:.*}","{a:.*}"};
      for (String path : paths)
      {
         RouterDescriptor routerMD = new RouterDescriptor();
         routerMD.addRoute(new RouteDescriptor(path));
         Router router = new Router(routerMD);

         //
         assertEquals(0, router.root.simpleRoutes.size());
         assertEquals(1, router.root.patternRoutes.size());
         PatternRoute patternRoute = router.root.patternRoutes.get(0);
         assertEquals("^(.*)", patternRoute.pattern.toString());
         assertEquals(Collections.singletonList(new QualifiedName("a")), patternRoute.parameterNames);
         assertEquals(1, patternRoute.parameterPatterns.size());
         assertEquals("^.*$", patternRoute.parameterPatterns.get(0).toString());
         assertEquals(2, patternRoute.chunks.size());
         assertEquals("", patternRoute.chunks.get(0));
         assertEquals("", patternRoute.chunks.get(1));
      }
   }

   public void testSamePrefix()
   {
      RouterDescriptor routerMD = new RouterDescriptor();
      routerMD.addRoute(new RouteDescriptor("/public/foo"));
      routerMD.addRoute(new RouteDescriptor("/public/bar"));

      //
      Router router = new Router(routerMD);
      assertEquals(2, router.root.simpleRoutes.get("public").size());
      Route publicRoute1 = router.root.simpleRoutes.get("public").get(0);
      assertEquals(1, publicRoute1.simpleRoutes.get("foo").size());
      Route publicRoute2 = router.root.simpleRoutes.get("public").get(1);
      assertEquals(1, publicRoute2.simpleRoutes.get("bar").size());
   }

   private void assertEquals(Route expectedRoute, Route route)
   {
      assertEquals(expectedRoute.getClass(), route.getClass());
      assertEquals(expectedRoute.simpleRoutes.keySet(), route.simpleRoutes.keySet());
      for (Map.Entry<String, List<SimpleRoute>> entry : expectedRoute.simpleRoutes.entrySet())
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

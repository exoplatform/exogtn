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
         expectedRoute.add(new SimpleRoute("a"));
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
         assertEquals(0, router.root.getSegmentNames().size());
         assertEquals(1, router.root.getPatternSize());
         PatternRoute patternRoute = router.root.getPattern(0);
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
         assertEquals(0, router.root.getSegmentNames().size());
         assertEquals(1, router.root.getPatternSize());
         PatternRoute patternRoute = router.root.getPattern(0);
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
         assertEquals(0, router.root.getSegmentNames().size());
         assertEquals(1, router.root.getPatternSize());
         PatternRoute patternRoute = router.root.getPattern(0);
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
      assertEquals(2, router.root.getSegmentSize("public"));
      Route publicRoute1 = router.root.getSegment("public", 0);
      assertEquals(1, publicRoute1.getSegmentSize("foo"));
      Route publicRoute2 = router.root.getSegment("public", 1);
      assertEquals(1, publicRoute2.getSegmentSize("bar"));
   }

   private void assertEquals(Route expectedRoute, Route route)
   {
      assertEquals(expectedRoute.getClass(), route.getClass());
      assertEquals(expectedRoute.getSegmentNames(), route.getSegmentNames());
      for (String segmentName : expectedRoute.getSegmentNames())
      {
         assertEquals(expectedRoute.getSegmentSize(segmentName), route.getSegmentSize(segmentName));
         for (int segmentIndex = 0;segmentIndex < expectedRoute.getSegmentSize(segmentName);segmentIndex++)
         {
            SimpleRoute expectedSimpleRoute = expectedRoute.getSegment(segmentName, segmentIndex);
            SimpleRoute simpleRoute  = route.getSegment(segmentName, segmentIndex);
            assertEquals(expectedSimpleRoute, simpleRoute);
         }
      }
      assertEquals(expectedRoute.getPatternSize(), route.getPatternSize());
      for (int i = 0;i < expectedRoute.getPatternSize();i++)
      {
         assertEquals(expectedRoute.getPattern(i), route.getPattern(i));
      }
      if (route instanceof PatternRoute)
      {
         assertEquals(((PatternRoute)expectedRoute).pattern.toString(), ((PatternRoute)route).pattern.toString());
         assertEquals(((PatternRoute)expectedRoute).parameterNames, ((PatternRoute)route).parameterNames);
      }
      else if (route instanceof SimpleRoute)
      {
         assertEquals(((SimpleRoute)expectedRoute).name, ((SimpleRoute)route).name);
      }
   }
}

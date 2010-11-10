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
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.RouterDescriptor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestDescriptorBuilder extends TestCase
{

   public void testFoo() throws Exception
   {

      URL routerURL = TestDescriptorBuilder.class.getResource("router.xml");
      XMLStreamReader routerReader = XMLInputFactory.newInstance().createXMLStreamReader(routerURL.openStream());
      RouterDescriptor routerDesc = new DescriptorBuilder().build(routerReader);

      Iterator<RouteDescriptor> i = routerDesc.getRoutes().iterator();

      //
      assertTrue(i.hasNext());
      RouteDescriptor route1 = i.next();
      assertEquals("/public/{gtn:sitetype}/{gtn:sitename}{gtn:path}", route1.getPath());
      assertEquals(Collections.singleton(WebAppController.HANDLER_PARAM), route1.getRouteParamNames());
      assertEquals(WebAppController.HANDLER_PARAM, route1.getRouteParam(WebAppController.HANDLER_PARAM).getName());
      assertEquals("portal", route1.getRouteParam(WebAppController.HANDLER_PARAM).getValue());
      assertEquals(Collections.singleton(QualifiedName.parse("gtn:path")), route1.getPathParams().keySet());
      assertEquals(QualifiedName.parse("gtn:path"), route1.getPathParams().get(QualifiedName.parse("gtn:path")).getName());
      assertEquals(".*", route1.getPathParams().get(QualifiedName.parse("gtn:path")).getPattern());
      assertEquals(EncodingMode.DEFAULT_FORM, route1.getPathParams().get(QualifiedName.parse("gtn:path")).getEncodingMode());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route2 = i.next();
      assertEquals("/private/{gtn:sitetype}/{gtn:sitename}{gtn:path}", route2.getPath());
      assertEquals(Collections.singleton(WebAppController.HANDLER_PARAM), route2.getRouteParamNames());
      assertEquals(WebAppController.HANDLER_PARAM, route2.getRouteParam(WebAppController.HANDLER_PARAM).getName());
      assertEquals("portal", route2.getRouteParam(WebAppController.HANDLER_PARAM).getValue());
      assertEquals(Collections.singleton(QualifiedName.parse("gtn:path")), route2.getPathParams().keySet());
      assertEquals(QualifiedName.parse("gtn:path"), route2.getPathParams().get(QualifiedName.parse("gtn:path")).getName());
      assertEquals(".*", route2.getPathParams().get(QualifiedName.parse("gtn:path")).getPattern());
      assertEquals(EncodingMode.PRESERVE_PATH, route2.getPathParams().get(QualifiedName.parse("gtn:path")).getEncodingMode());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route3 = i.next();
      assertEquals("/upload", route3.getPath());
      assertEquals(Collections.singleton(WebAppController.HANDLER_PARAM), route3.getRouteParamNames());
      assertEquals(WebAppController.HANDLER_PARAM, route3.getRouteParam(WebAppController.HANDLER_PARAM).getName());
      assertEquals("upload", route3.getRouteParam(WebAppController.HANDLER_PARAM).getValue());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route4 = i.next();
      assertEquals("/download", route4.getPath());
      assertEquals(Collections.singleton(WebAppController.HANDLER_PARAM), route4.getRouteParamNames());
      assertEquals(WebAppController.HANDLER_PARAM, route4.getRouteParam(WebAppController.HANDLER_PARAM).getName());
      assertEquals("download", route4.getRouteParam(WebAppController.HANDLER_PARAM).getValue());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route5 = i.next();
      assertEquals("/a", route5.getPath());
      assertEquals(Collections.singleton(QualifiedName.create("a")), route5.getRouteParamNames());
      assertEquals(QualifiedName.create("a"), route5.getRouteParam(QualifiedName.create("a")).getName());
      assertEquals("a_value", route5.getRouteParam(QualifiedName.create("a")).getValue());
      assertEquals(1, route5.getChildren().size());
      RouteDescriptor route5_1 = route5.getChildren().get(0);
      assertEquals("/b", route5_1.getPath());
      assertEquals(Collections.singleton(QualifiedName.create("b")), route5_1.getRouteParamNames());
      assertEquals(QualifiedName.create("b"), route5_1.getRouteParam(QualifiedName.create("b")).getName());
      assertEquals("b_value", route5_1.getRouteParam(QualifiedName.create("b")).getValue());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route6 = i.next();
      assertEquals("/b", route6.getPath());
      assertEquals(new HashSet<String>(Arrays.asList("foo", "bar", "juu")), route6.getRequestParamMatchNames());
      assertEquals(QualifiedName.parse("foo"), route6.getRequestParam("foo").getName());
      assertEquals("foo", route6.getRequestParam("foo").getMatchName());
      assertEquals(null, route6.getRequestParam("foo").getMatchValue());
      assertEquals(false, route6.getRequestParam("foo").isRequired());
      assertEquals(QualifiedName.parse("bar"), route6.getRequestParam("bar").getName());
      assertEquals("bar", route6.getRequestParam("bar").getMatchName());
      assertEquals("bar", route6.getRequestParam("bar").getMatchValue());
      assertEquals(false, route6.getRequestParam("bar").isRequired());
      assertEquals(QualifiedName.parse("juu"), route6.getRequestParam("juu").getName());
      assertEquals("juu", route6.getRequestParam("juu").getMatchName());
      assertEquals("juu", route6.getRequestParam("juu").getMatchValue());
      assertEquals(true, route6.getRequestParam("juu").isRequired());

      //
      assertFalse(i.hasNext());
   }
}

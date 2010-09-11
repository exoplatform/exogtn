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
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.RouterDescriptor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.net.URL;
import java.util.Collections;
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
      assertEquals("/public/{{gtn}sitename}{{gtn}path:.*}", route1.getPath());
      assertEquals(Collections.singletonMap(WebAppController.HANDLER_PARAM, "portal"), route1.getParameters());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route2 = i.next();
      assertEquals("/private/{{gtn}sitename}{{gtn}path:.*}", route2.getPath());
      assertEquals(Collections.singletonMap(WebAppController.HANDLER_PARAM, "portal"), route2.getParameters());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route3 = i.next();
      assertEquals("/upload", route3.getPath());
      assertEquals(Collections.singletonMap(WebAppController.HANDLER_PARAM, "upload"), route3.getParameters());

      //
      assertTrue(i.hasNext());
      RouteDescriptor route4 = i.next();
      assertEquals("/download", route4.getPath());
      assertEquals(Collections.singletonMap(WebAppController.HANDLER_PARAM, "download"), route4.getParameters());
   }
}

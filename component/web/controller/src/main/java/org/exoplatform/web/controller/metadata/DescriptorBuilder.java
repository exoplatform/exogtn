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

package org.exoplatform.web.controller.metadata;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.exoplatform.web.controller.QualifiedName;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptorBuilder
{

   /** . */
   private static final QName routeQN = new QName("http://www.gatein.org/xml/ns/gatein_router_1_0", "route");

   /** . */
   private static final QName parameterQN = new QName("http://www.gatein.org/xml/ns/gatein_router_1_0", "parameter");

   public RouterDescriptor build(XMLStreamReader reader) throws Exception
   {
      RouterDescriptor routerDesc = new RouterDescriptor();
      SMHierarchicCursor routerC = SMInputFactory.rootElementCursor(reader);
      routerC.getNext();

      //
      SMInputCursor routeC = routerC.childElementCursor(routeQN);
      while (routeC.getNext() != null)
      {
         build(routeC, routerDesc.getRoutes());
      }

      //
      return routerDesc;
   }

   private void build(SMInputCursor routeC, List<RouteDescriptor> descriptors) throws XMLStreamException
   {
      String path = routeC.getAttrValue("path");
      RouteDescriptor routeDesc = new RouteDescriptor(path);

      //
      SMInputCursor childC = routeC.childElementCursor();
      while (childC.getNext() != null)
      {
         if (childC.getQName().equals(parameterQN))
         {
            String name = childC.getAttrValue("name");
            String value = childC.getAttrValue("value");
            routeDesc.addParameter(QualifiedName.parse(name), value);
         }
         else if (childC.getQName().equals(routeQN))
         {
            build(childC, routeDesc.getChildren());
         }
      }

      //
      descriptors.add(routeDesc);
   }

}

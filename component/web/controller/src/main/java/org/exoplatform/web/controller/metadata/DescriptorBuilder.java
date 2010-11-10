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

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.EncodingMode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
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
   private static final QName paramQN = new QName("http://www.gatein.org/xml/ns/gatein_router_1_0", "route-param");

   /** . */
   private static final QName requestParamQN = new QName("http://www.gatein.org/xml/ns/gatein_router_1_0", "request-param");

   /** . */
   private static final QName pathParamQN = new QName("http://www.gatein.org/xml/ns/gatein_router_1_0", "path-param");

   public RouterDescriptor build(XMLStreamReader reader) throws Exception
   {
      RouterDescriptor routerDesc = new RouterDescriptor();

      //
      while (true)
      {
         int event = reader.next();
         if (event == XMLStreamConstants.END_DOCUMENT)
         {
            reader.close();
            break;
         }
         else if (event == XMLStreamConstants.START_ELEMENT)
         {
            if (routeQN.equals(reader.getName()))
            {
               build(reader, routerDesc.getRoutes());
            }
         }
      }

      //
      return routerDesc;
   }

   private void build(XMLStreamReader reader, List<RouteDescriptor> descriptors) throws XMLStreamException
   {
      String path = reader.getAttributeValue(null, "path");
      RouteDescriptor routeDesc = new RouteDescriptor(path);

      //
      while (true)
      {
         int event = reader.next();
         if (event == XMLStreamConstants.END_ELEMENT)
         {
            if (routeQN.equals(reader.getName()))
            {
               break;
            }
         }
         else if (event == XMLStreamConstants.START_ELEMENT)
         {
            if (paramQN.equals(reader.getName()))
            {
               String name = reader.getAttributeValue(null, "name");
               String value = reader.getAttributeValue(null, "value");
               routeDesc.addRouteParam(QualifiedName.parse(name), value);
            }
            else if (requestParamQN.equals(reader.getName()))
            {
               String name = reader.getAttributeValue(null, "name");
               String matchName = reader.getAttributeValue(null, "matchName");
               String matchValue = reader.getAttributeValue(null, "matchValue");
               String optional = reader.getAttributeValue(null, "required");
               routeDesc.addRequestParam(QualifiedName.parse(name), matchName, matchValue, "true".equals(optional));
            }
            else if (pathParamQN.equals(reader.getName()))
            {
               String name = reader.getAttributeValue(null, "name");
               String pattern = reader.getAttributeValue(null, "pattern");
               String encoded = reader.getAttributeValue(null, "encoding");
               EncodingMode encodingMode = "preserve-path".equals(encoded) ? EncodingMode.PRESERVE_PATH : EncodingMode.DEFAULT_FORM;
               routeDesc.addPathParam(QualifiedName.parse(name), pattern, encodingMode);
            }
            else if (routeQN.equals(reader.getName()))
            {
               build(reader, routeDesc.getChildren());
            }
         }
      }

      //
      descriptors.add(routeDesc);
   }

}

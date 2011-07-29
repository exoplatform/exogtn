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

import org.exoplatform.web.controller.router.EncodingMode;
import org.staxnav.Axis;
import org.staxnav.Naming;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorImpl;

import javax.xml.stream.XMLStreamReader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptorBuilder
{

   // http://www.gatein.org/xml/ns/gatein_router_1_0

   public static PathParamDescriptor pathParam(String qualifiedName)
   {
      return new PathParamDescriptor(qualifiedName);
   }

   public static RequestParamDescriptor requestParam(String qualifiedName)
   {
      return new RequestParamDescriptor(qualifiedName);
   }

   public static RouteParamDescriptor routeParam(String qualifiedName)
   {
      return new RouteParamDescriptor(qualifiedName);
   }

   public static RouteDescriptor route(String path)
   {
      return new RouteDescriptor(path);
   }

   public static RouterDescriptor router()
   {
      return new RouterDescriptor();
   }

   public RouterDescriptor build(XMLStreamReader reader) throws Exception
   {
      RouterDescriptor router = router();
      StaxNavigator<Element> root = new StaxNavigatorImpl<Element>(new Naming.Enumerated.Simple<Element>(Element.class, Element.UNKNOWN), reader);
      if (root.child() != null)
      {
         for (StaxNavigator<Element> routeNav : root.fork(Element.ROUTE))
         {
            RouteDescriptor route = buildRoute(routeNav);
            router.add(route);
         }
      }
      return router;
   }

   private RouteDescriptor buildRoute(StaxNavigator<Element> root) throws Exception
   {
      String path = root.getAttribute("path");

      //
      RouteDescriptor route = new RouteDescriptor(path);

      if (root.child() != null)
      {
         while (root.getName() != null)
         {
            StaxNavigator<Element> fork = root.fork(Axis.FOLLOWING_SIBLING);

            //
            switch (fork.getName())
            {
               case PATH_PARAM:
               {
                  String qualifiedName = fork.getAttribute("qname");
                  String encoded = fork.getAttribute("encoding");
                  String pattern = null;
                  if (fork.child(Element.PATTERN))
                  {
                     pattern = fork.getContent();
                  }
                  EncodingMode encodingMode = "preserve-path".equals(encoded) ? EncodingMode.PRESERVE_PATH : EncodingMode.FORM;
                  route.with(new PathParamDescriptor(qualifiedName).encodedBy(encodingMode).matchedBy(pattern));
                  break;
               }
               case ROUTE_PARAM:
               {
                  String qualifiedName = fork.getAttribute("qname");
                  String value = null;
                  if (fork.child(Element.VALUE))
                  {
                     value = fork.getContent();
                  }
                  route.with(new RouteParamDescriptor(qualifiedName).withValue(value));
                  break;
               }
               case REQUEST_PARAM:
               {
                  String qualifiedName = fork.getAttribute("qname");
                  String name = fork.getAttribute("name");
                  String required = fork.getAttribute("required");
                  RequestParamDescriptor param = new RequestParamDescriptor(qualifiedName).named(name).required("true".equals(required));
                  if (fork.child(Element.VALUE))
                  {
                     param.setValue(fork.getContent());
                     param.setValueType(ValueType.LITERAL);
                  }
                  if (fork.child(Element.PATTERN))
                  {
                     param.setValue(fork.getContent());
                     param.setValueType(ValueType.PATTERN);
                  }
                  route.with(param);
                  break;
               }
               case ROUTE:
                  RouteDescriptor sub = buildRoute(fork);
                  route.sub(sub);
                  break;
               default:
                  throw new AssertionError();
            }
         }
      }

      //
      return route;
   }
}

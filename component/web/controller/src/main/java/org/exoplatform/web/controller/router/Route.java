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

//import javanet.staxutils.IndentingXMLStreamWriter;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.PathParamDescriptor;
import org.exoplatform.web.controller.metadata.RequestParamDescriptor;
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.RouteParamDescriptor;
import org.gatein.common.util.Tools;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * The implementation of the routing algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Route
{

   void writeTo(XMLStreamWriter writer) throws XMLStreamException
   {
      if (this instanceof SegmentRoute)
      {
         writer.writeStartElement("segment");
         writer.writeAttribute("path", "/" + ((SegmentRoute)this).name);
         writer.writeAttribute("terminal", "" + terminal);
      }
      else if (this instanceof PatternRoute)
      {
         PatternRoute pr = (PatternRoute)this;
         StringBuilder path = new StringBuilder("/");
         for (int i = 0;i < pr.params.length;i++)
         {
            path.append(pr.chunks[i]).append("{").append(pr.params[i].name.getValue()).append("}");
         }
         path.append(pr.chunks[pr.chunks.length - 1]);
         writer.writeStartElement("pattern");
         writer.writeAttribute("path", path.toString());
         writer.writeAttribute("terminal", Boolean.toString(terminal));
         for (PathParam param : pr.params)
         {
            writer.writeStartElement("path-param");
            writer.writeAttribute("qname", param.name.getValue());
            writer.writeAttribute("encodingMode", param.encodingMode.toString());
            writer.writeAttribute("pattern", param.renderingPattern.toString());
            writer.writeEndElement();
         }
      }
      else
      {
         writer.writeStartElement("route");
      }

      //
      for (RouteParam routeParam : routeParams.values())
      {
         writer.writeStartElement("route-param");
         writer.writeAttribute("qname", routeParam.name.getValue());
         writer.writeAttribute("value", routeParam.value);
         writer.writeEndElement();
      }

      //
      for (RequestParam requestParam : requestParams.values())
      {
         writer.writeStartElement("request-param");
         writer.writeAttribute("qname", requestParam.name.getValue());
         writer.writeAttribute("name", requestParam.matchName);
         if (requestParam.matchPattern != null)
         {
            writer.writeAttribute("value", requestParam.matchPattern.pattern());
         }
         writer.writeEndElement();
      }

      //
/*
      for (Map.Entry<String, SegmentRoute[]> entry : segments.entrySet())
      {
         writer.writeStartElement("segment");
         writer.writeAttribute("name", entry.getKey());
         for (SegmentRoute segment : entry.getValue())
         {
            segment.writeTo(writer);
         }
         writer.writeEndElement();
      }

      //
      for (PatternRoute pattern : patterns)
      {
         pattern.writeTo(writer);
      }
*/

      //
      writer.writeEndElement();
   }

   @Override
   public String toString()
   {
      try
      {
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
         StringWriter sw = new StringWriter();
         XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(sw);
//         xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
         writeTo(xmlWriter);
         return sw.toString();
      }
      catch (XMLStreamException e)
      {
         throw new AssertionError(e);
      }
   }

   /** Julien : make that configurable. */
   private static final char slashEscape = '_';

   /** . */
   private static final Route[] EMPTY_ROUTE_ARRAY = new Route[0];

   /** . */
   private Route parent;

   /** . */
   private boolean terminal;

   /** . */
   private Route[] children;

   /** . */
   private final Map<QualifiedName, RouteParam> routeParams;

   /** . */
   private final Map<String, RequestParam> requestParams;

   Route()
   {
      this.parent = null;
      this.terminal = true;
      this.children = EMPTY_ROUTE_ARRAY;
      this.routeParams = new HashMap<QualifiedName, RouteParam>();
      this.requestParams = new HashMap<String, RequestParam>();
   }

   final boolean isTerminal()
   {
      return terminal;
   }

   /*
    * Ok, so this is not the fastest way to do it, but for now it's OK, it's what is needed, we'll find
    * a way to optimize it later with some precompilation. 
    */
   final void render(Map<QualifiedName, String> blah, RenderContext renderContext)
   {
      Route r = find(blah);

      // We found a route we need to render it now
      if (r != null)
      {
         // Append path first
         r.renderPath(blah, renderContext, false);

         // Append query parameters after
         r.renderQueryString(blah, renderContext);
      }
   }

   private boolean renderPath(Map<QualifiedName, String> blah, RenderContext renderContext, boolean hasChildren)
   {
      boolean endWithSlash;
      if (parent != null)
      {
         endWithSlash = parent.renderPath(blah, renderContext, true);
      }
      else
      {
         endWithSlash = false;
      }

      //
      if (this instanceof SegmentRoute)
      {
         SegmentRoute sr = (SegmentRoute)this;
         if (!endWithSlash)
         {
            renderContext.appendPath('/', false);
            endWithSlash = true;
         }
         String name = sr.name;
         renderContext.appendPath(name, true);
         if (name.length() > 0)
         {
            endWithSlash = false;
         }
      }
      else if (this instanceof PatternRoute)
      {
         PatternRoute pr = (PatternRoute)this;
         if (!endWithSlash)
         {
            renderContext.appendPath('/', false);
            endWithSlash = true;
         }
         int i = 0;
         int count = 0;
         while (i < pr.params.length)
         {
            String chunk = pr.chunks[i];
            renderContext.appendPath(chunk, true);
            count += chunk.length();

            //
            PathParam def = pr.params[i];
            String value = blah.get(def.name);
            count += value.length();

            //
            int from = 0;
            while (true)
            {
               int to = value.indexOf('/', from);
               if (to == -1)
               {
                  break;
               }
               else
               {
                  renderContext.appendPath(value.substring(from, to), true);
                  renderContext.appendPath(def.encodingMode == EncodingMode.PRESERVE_PATH ? '/' : slashEscape, false);
                  from = to +1;
               }
            }
            renderContext.appendPath(value.substring(from), false);

            //
            i++;
         }
         String lastChunk = pr.chunks[i];
         renderContext.appendPath(lastChunk, false);
         count += lastChunk.length();
         if (count > 0)
         {
            endWithSlash = false;
         }
      }
      else
      {
         if (!hasChildren)
         {
            renderContext.appendPath('/', false);
            endWithSlash = true;
         }
      }

      //
      return endWithSlash;
   }

   private void renderQueryString(Map<QualifiedName, String> blah, RenderContext renderContext)
   {
      if (parent != null)
      {
         parent.renderQueryString(blah, renderContext);
      }

      //
      if (requestParams.size() > 0)
      {
         for (RequestParam requestParamDef : requestParams.values())
         {
            String s = blah.get(requestParamDef.name);
            switch (requestParamDef.valueMapping)
            {
               case CANONICAL:
                  break;
               case NEVER_EMPTY:
                  if (s != null && s.length() == 0)
                  {
                     s = null;
                  }
                  break;
               case NEVER_NULL:
                  if (s == null)
                  {
                     s = "";
                  }
                  break;
            }
            if (s != null)
            {
               renderContext.appendQueryParameter(requestParamDef.matchName, s);
            }
         }
      }
   }

   final Route find(Map<QualifiedName, String> blah)
   {

      // Remove what is matched
      Map<QualifiedName, String> abc = new HashMap<QualifiedName, String>(blah);

      // Match first the static parameteters
      for (RouteParam param : routeParams.values())
      {
         String value = blah.get(param.name);
         if (param.value.equals(value))
         {
            abc.remove(param.name);
         }
         else
         {
            return null;
         }
      }

      // Match any request parameter
      if (requestParams.size() > 0)
      {
         for (RequestParam requestParamDef : requestParams.values())
         {
            String a = blah.get(requestParamDef.name);
            boolean matched = false;
            if (a != null)
            {
               if (requestParamDef.matchValue(a))
               {
                  matched = true;
               }
            }
            if (matched)
            {
               abc.remove(requestParamDef.name);
            }
            else
            {
               switch (requestParamDef.controlMode)
               {
                  case OPTIONAL:
                     // Do nothing
                     break;
                  case REQUIRED:
                     return null;
                  default:
                     throw new AssertionError();
               }
            }
         }
      }

      // Match any pattern parameter
      if (this instanceof PatternRoute)
      {
         PatternRoute prt = (PatternRoute)this;
         for (int i = 0;i < prt.params.length;i++)
         {
            PathParam param = prt.params[i];
            String s = blah.get(param.name);
            boolean matched = false;
            if (s != null)
            {
               switch (param.encodingMode)
               {
                  case FORM:
                     matched = param.renderingPattern.matcher(s).matches();
                     break;
                  case PRESERVE_PATH:
                     matched = param.renderingPattern.matcher(s).matches();
                     break;
                  default:
                     throw new AssertionError();
               }
            }
            if (matched)
            {
               abc.remove(param.name);
            }
            else
            {
               return null;
            }
         }
      }

      //
      if (abc.isEmpty() && terminal)
      {
         return this;
      }

      //
      for (Route route : children)
      {
         Route a = route.find(abc);
         if (a != null)
         {
            return a;
         }
      }

      //
      return null;
   }

   /**
    *
    * @param path the path
    * @param requestParams the query parameters
    * @return null or the parameters when it matches
    */
   final Map<QualifiedName, String> route(String path, Map<String, String[]> requestParams)
   {
      Map<QualifiedName, String> ret = null;

      // Check request parameters
      Map<QualifiedName, String> routeRequestParams = Collections.emptyMap();
      if (this.requestParams.size() > 0)
      {
         for (RequestParam requestParamDef : this.requestParams.values())
         {
            String value = null;
            String[] values = requestParams.get(requestParamDef.matchName);
            if (values != null && values.length > 0 && values[0] != null)
            {
               value = values[0];
            }
            if (value == null)
            {
               switch (requestParamDef.controlMode)
               {
                  case OPTIONAL:
                     // Do nothing
                     break;
                  case REQUIRED:
                     return null;
               }
            }
            else if (!requestParamDef.matchValue(value))
            {
               return null;
            }
            switch (requestParamDef.valueMapping)
            {
               case CANONICAL:
                  break;
               case NEVER_EMPTY:
                  if (value != null && value.length() == 0)
                  {
                     value = null;
                  }
                  break;
               case NEVER_NULL:
                  if (value == null)
                  {
                     value = "";
                  }
                  break;
            }
            if (value != null)
            {
               if (routeRequestParams.isEmpty())
               {
                  routeRequestParams = new HashMap<QualifiedName, String>();
               }
               routeRequestParams.put(requestParamDef.name, value);
            }
         }
      }

      // Anything that does not begin with '/' returns null
      if (path.length() > 0 && path.charAt(0) == '/')
      {
         // The '/' means the current controller if any, otherwise it may be processed by the pattern matching
         if (path.length() == 1)
         {
            if (terminal)
            {
               ret = new HashMap<QualifiedName, String>();
            }
         }

         // Try to find a pattern matching route otherwise
         if (ret == null)
         {

            // Find the next '/' for determining the segment and next path
            int pos = -1;
            String segment = null;

            // Determine next path
            String nextSegmentPath = null;

            //
            for (Route route : children)
            {
               if (route instanceof SegmentRoute)
               {
                  SegmentRoute segmentRoute = (SegmentRoute)route;

                  //
                  if (segmentRoute.name.length() == 0)
                  {
                     // Delegate the process to the next route
                     Map<QualifiedName, String> response = segmentRoute.route(path, requestParams);

                     // If we do have a response we return it
                     if (response != null)
                     {
                        ret = response;
                        break;
                     }
                  }
                  else
                  {
                     // Find the next '/' for determining the segment and next path
                     if (segment == null)
                     {
                        pos = path.indexOf('/', 1);
                        if (pos == -1)
                        {
                           pos = path.length();
                        }
                        segment = path.substring(1, pos);
                     }

                     // Determine next path
                     if (segmentRoute.name.equals(segment))
                     {
                        // Lazy create next segment path
                        if (nextSegmentPath == null)
                        {
                           if (pos == path.length())
                           {
                              nextSegmentPath = "/";
                           }
                           else
                           {
                              nextSegmentPath = path.substring(pos);
                           }
                        }

                        // Delegate the process to the next route
                        Map<QualifiedName, String> response = segmentRoute.route(nextSegmentPath, requestParams);

                        // If we do have a response we return it
                        if (response != null)
                        {
                           ret = response;
                           break;
                        }
                     }
                  }
               }
               else if (route instanceof PatternRoute)
               {
                  PatternRoute patternRoute = (PatternRoute)route;

                  //
                  Matcher matcher = patternRoute.pattern.matcher(path);

                  // We match
                  if (matcher.find())
                  {
                     // Build next controller context
                     int nextPos = matcher.end();
                     String nextPath;
                     if (path.length() == nextPos)
                     {
                        nextPath = "/";
                     }
                     else
                     {
                        if (nextPos > 0 && path.charAt(nextPos - 1) == '/')
                        {
                           nextPos--;
                        }

                        //
                        nextPath = path.substring(nextPos);
                     }

                     // Delegate to next patternRoute
                     Map<QualifiedName, String> response = patternRoute.route(nextPath, requestParams);

                     // If we do have a response we return it
                     if (response != null)
                     {
                        // Append parameters
                        int group = 1;
                        for (int i = 0;i < patternRoute.params.length;i++)
                        {
                           PathParam param = patternRoute.params[i];

                           //
                           String value = matcher.group(group);

                           //
                           if (value != null)
                           {
                              if (param.encodingMode == EncodingMode.FORM)
                              {
                                 value = value.replace(slashEscape, '/');
                              }
                              response.put(param.name, value);
                           }
                           else
                           {
                              // We have an optional match
                           }

                           //
                           group++;
                        }

                        //
                        ret = response;
                        break;
                     }
                  }
               }
            }
         }

         // Update parameters if it is possible
         if (ret != null)
         {
            if (routeParams.size() > 0)
            {
               for (RouteParam param : routeParams.values())
               {
                  if (!ret.containsKey(param.name))
                  {
                     ret.put(param.name, param.value);
                  }
               }
            }
            if (routeRequestParams.size() > 0)
            {
               for (Map.Entry<QualifiedName, String> entry : routeRequestParams.entrySet())
               {
                  if (!ret.containsKey(entry.getKey()))
                  {
                     ret.put(entry.getKey(), entry.getValue());
                  }
               }
            }
         }
      }

      //
      return ret;
   }

   final <R extends Route> R add(R route) throws MalformedRouteException
   {
      if (route == null)
      {
         throw new NullPointerException("No null route accepted");
      }
      if (route.parent != null)
      {
         throw new IllegalArgumentException("No route with an existing parent can be accepted");
      }

      //
      LinkedList<Param> ancestorParams = new LinkedList<Param>();
      findAncestorOrSelfParams(ancestorParams);
      LinkedList<Param> descendantParams = new LinkedList<Param>();
      for (Param param : ancestorParams)
      {
         route.findDescendantOrSelfParams(param.name, descendantParams);
         if (descendantParams.size() > 0)
         {
            throw new MalformedRouteException("Duplicate parameter " + param.name);
         }
      }

      //
      if (route instanceof PatternRoute || route instanceof SegmentRoute)
      {
         children = Tools.appendTo(children, route);
         terminal = false;
         route.parent = this;
      }
      else
      {
         throw new IllegalArgumentException("Only accept segment or pattern routes");
      }

      //
      return route;
   }

   final Set<String> getSegmentNames()
   {
      Set<String> names = new HashSet<String>();
      for (Route child : children)
      {
         if (child instanceof SegmentRoute)
         {
            SegmentRoute childSegment = (SegmentRoute)child;
            names.add(childSegment.name);
         }
      }
      return names;
   }

   final int getSegmentSize(String segmentName)
   {
      int size = 0;
      for (Route child : children)
      {
         if (child instanceof SegmentRoute)
         {
            SegmentRoute childSegment = (SegmentRoute)child;
            if (segmentName.equals(childSegment.name))
            {
               size++;
            }
         }
      }
      return size;
   }

   final SegmentRoute getSegment(String segmentName, int index)
   {
      for (Route child : children)
      {
         if (child instanceof SegmentRoute)
         {
            SegmentRoute childSegment = (SegmentRoute)child;
            if (segmentName.equals(childSegment.name))
            {
               if (index == 0)
               {
                  return childSegment;
               }
               else
               {
                  index--;
               }
            }
         }
      }
      return null;
   }

   final int getPatternSize()
   {
      int size = 0;
      for (Route route : children)
      {
         if (route instanceof PatternRoute)
         {
            size++;
         }
      }
      return size;
   }

   final PatternRoute getPattern(int index)
   {
      for (Route route : children)
      {
         if (route instanceof PatternRoute)
         {
            if (index == 0)
            {
               return (PatternRoute)route;
            }
            else
            {
               index--;
            }
         }
      }
      return null;
   }

   final Route append(RouteDescriptor descriptor) throws MalformedRouteException
   {
      Route route = append(descriptor.getPathParams(), descriptor.getPath());

      //
      for (RouteParamDescriptor routeParamDesc : descriptor.getRouteParams())
      {
         route.add(RouteParam.create(routeParamDesc));
      }

      //
      for (RequestParamDescriptor requestParamDesc : descriptor.getRequestParams())
      {
         route.add(RequestParam.create(requestParamDesc));
      }

      //
      for (RouteDescriptor childDescriptor : descriptor.getChildren())
      {
         route.append(childDescriptor);
      }

      //
      return route;
   }

   final Route add(RouteParam param) throws MalformedRouteException
   {
      Param existing = findParam(param.name);
      if (existing != null)
      {
         throw new MalformedRouteException("Duplicate parameter " + param.name);
      }
      routeParams.put(param.name, param);
      return this;
   }

   final Route add(RequestParam param) throws MalformedRouteException
   {
      Param existing = findParam(param.name);
      if (existing != null)
      {
         throw new MalformedRouteException("Duplicate parameter " + param.name);
      }
      requestParams.put(param.matchName, param);
      return this;
   }

   /**
    * Append a path, creates the necessary routes and returns the last route added.
    *
    * @param pathParamDescriptors the path param descriptors
    * @param path the path to append
    * @return the last route added
    */
   private Route append(Map<QualifiedName, PathParamDescriptor> pathParamDescriptors, String path) throws MalformedRouteException
   {
      if (path.length() == 0 || path.charAt(0) != '/')
      {
         throw new MalformedRouteException();
      }

      //
      int pos = path.length();
      int level = 0;
      List<Integer> start = new ArrayList<Integer>();
      List<Integer> end = new ArrayList<Integer>();
      for (int i = 1;i < path.length();i++)
      {
         char c = path.charAt(i);
         if (c == '{')
         {
            if (level++ == 0)
            {
               start.add(i);
            }
         }
         else if (c == '}')
         {
            if (--level == 0)
            {
               end.add(i);
            }
         }
         else if (c == '/')
         {
            if (level == 0)
            {
               pos = i;
               break;
            }
         }
      }

      //
      Route next;
      if (start.isEmpty())
      {
         String segment = path.substring(1, pos);
         SegmentRoute route = new SegmentRoute(segment);
         add(route);
         next = route;
      }
      else
      {
         if (start.size() == end.size())
         {
            PatternBuilder builder = new PatternBuilder();
            builder.expr("^").expr('/');
            List<String> chunks = new ArrayList<String>();
            List<PathParam> parameterPatterns = new ArrayList<PathParam>();

            //
            int previous = 1;
            for (int i = 0;i < start.size();i++)
            {
               builder.litteral(path, previous, start.get(i));
               chunks.add(path.substring(previous, start.get(i)));
               String parameterName = path.substring(start.get(i) + 1, end.get(i));

               //
               QualifiedName parameterQName = QualifiedName.parse(parameterName);

               // Now get path param metadata
               PathParamDescriptor parameterDescriptor = pathParamDescriptors.get(parameterQName);

               //
               PathParam param;
               if (parameterDescriptor != null)
               {
                  param = PathParam.create(parameterDescriptor);
               }
               else
               {
                  param = PathParam.create(parameterQName);
               }

               // Append routing regex to the route regex
               builder.expr("(").expr(param.routingRegex).expr(")");

               // Add the path param with the rendering regex
               parameterPatterns.add(param);
               previous = end.get(i) + 1;
            }

            //
            builder.litteral(path, previous, pos);

            // We want to satisfy one of the following conditions
            // - the next char after the matched expression is '/'
            // - the expression matched until the end
            // - the match expression is the '/' expression
            builder.expr("(?:(?<=^/)|(?=/)|$)");

            //
            chunks.add(path.substring(previous, pos));
            PatternRoute route = new PatternRoute(builder.build(), parameterPatterns, chunks);

            // Wire
            add(route);

            //
            next = route;
         }
         else
         {
            throw new UnsupportedOperationException("Report error");
         }
      }

      //
      if (pos < path.length())
      {
         return next.append(pathParamDescriptors, path.substring(pos));
      }
      else
      {
         return next;
      }
   }

   private Param getParam(QualifiedName name)
   {
      if (routeParams.containsKey(name))
      {
         return routeParams.get(name);
      }
      else
      {
         for (RequestParam param : requestParams.values())
         {
            if (param.name.equals(name))
            {
               return param;
            }
         }
      }
      if (this instanceof PatternRoute)
      {
         for (PathParam param : ((PatternRoute)this).params)
         {
            if  (param.name.equals(name))
            {
               return param;
            }
         }
      }
      return null;
   }

   private Param findParam(QualifiedName name)
   {
      Param param = getParam(name);
      if (param == null && parent != null)
      {
         param = parent.findParam(name);
      }
      return param;
   }

   private void findParams(List<Param> params)
   {
      for (RouteParam param : routeParams.values())
      {
         params.add(param);
      }
      for (RequestParam param : requestParams.values())
      {
         params.add(param);
      }
      if (this instanceof PatternRoute)
      {
         Collections.addAll(params, ((PatternRoute)this).params);
      }
   }

   private void findAncestorOrSelfParams(List<Param> params)
   {
      findParams(params);
      if (parent != null)
      {
         parent.findAncestorOrSelfParams(params);
      }
   }

   /**
    * Find the params having the specified <code>name</code> among this route or its descendants.
    *
    * @param name the name
    * @param params the list collecting the found params
    */
   private void findDescendantOrSelfParams(QualifiedName name, List<Param> params)
   {
      Param param = getParam(name);
      if (param != null)
      {
         params.add(param);
      }
   }
}

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

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.RequestParamDescriptor;
import org.exoplatform.web.controller.metadata.RouteDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation of the routing algorithm.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Route
{

   /** . */
   private Route parent;

   /** . */
   boolean terminal;

   /** . */
   private final Map<String, List<SegmentRoute>> segments;

   /** Actually here we allow to store several times the same pattern and routing could be optimized instead. */
   private final List<PatternRoute> patterns;

   /** . */
   private final Map<QualifiedName, String> routeParameters;

   /** . */
   private final Map<String, RequestParamDef> requestParamDefs;

   Route()
   {
      this.parent = null;
      this.terminal = false;
      this.segments = new LinkedHashMap<String, List<SegmentRoute>>();
      this.patterns = new ArrayList<PatternRoute>();
      this.routeParameters = new HashMap<QualifiedName, String>();
      this.requestParamDefs = new HashMap<String, RequestParamDef>();
   }

   /**
    * Ok, so this is not the fastest way to do it, but for now it's OK, it's what is needed, we'll find
    * a way to optimize it later with some precompilation. 
    */
   final void render(Map<QualifiedName, String> blah, RenderContext renderContext)
   {
      Route r = find(blah);
      if (r != null)
      {
         r._render(blah, renderContext);
      }
   }

   private void _render(Map<QualifiedName, String> blah, RenderContext renderContext)
   {
      if (parent != null && parent.parent != null)
      {
         parent._render(blah, renderContext);
      }

      //
      if (requestParamDefs.size() > 0)
      {
         for (RequestParamDef requestParamDef : requestParamDefs.values())
         {
            String s = blah.get(requestParamDef.getName());
            renderContext.appendQueryParameter(requestParamDef.getMatchName(), s);
         }
      }

      //
      if (this instanceof SegmentRoute)
      {
         SegmentRoute sr = (SegmentRoute)this;
         renderContext.appendPath('/');
         renderContext.appendPath(sr.name);
      }
      else if (this instanceof PatternRoute)
      {
         PatternRoute pr = (PatternRoute)this;
         renderContext.appendPath('/');
         int i = 0;
         while (i < pr.parameterNames.size())
         {
            renderContext.appendPath(pr.chunks.get(i));
            String value = blah.get(pr.parameterNames.get(i));
            renderContext.appendPath(value);
            i++;
         }
         renderContext.appendPath(pr.chunks.get(i));
      }
      else
      {
         renderContext.appendPath("/");
      }
   }

   final Route find(Map<QualifiedName, String> blah)
   {

      // Remove what is matched
      Map<QualifiedName, String> abc = new HashMap<QualifiedName, String>(blah);

      // Match first the static parameteters
      for (Map.Entry<QualifiedName, String> a : routeParameters.entrySet())
      {
         String s = blah.get(a.getKey());
         if (a.getValue().equals(s))
         {
            abc.remove(a.getKey());
         }
         else
         {
            return null;
         }
      }

      // Match any request parameter
      if (requestParamDefs.size() > 0)
      {
         for (RequestParamDef requestParamDef : requestParamDefs.values())
         {
            String a = blah.get(requestParamDef.name);
            if (a != null)
            {
               if (requestParamDef.matchValue.matcher(a).matches())
               {
                  //
                  abc.remove(requestParamDef.name);
                  continue;
               }
            }
            return null;
         }
      }

      // Match any pattern parameter
      if (this instanceof PatternRoute)
      {
         PatternRoute prt = (PatternRoute)this;
         for (int i = 0;i < prt.parameterNames.size();i++)
         {
            QualifiedName qd = prt.parameterNames.get(i);
            String s = blah.get(qd);
            if (s != null && prt.parameterPatterns.get(i).matcher(s).matches())
            {
               abc.remove(qd);
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
      for (List<SegmentRoute> routes : segments.values())
      {
         for (SegmentRoute route : routes)
         {
            Route a = route.find(abc);
            if (a != null)
            {
               return a;
            }
         }
      }
      for (PatternRoute route : patterns)
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
    * @param path the path
    * @param requestParams the query parameters
    * @return null or the parameters when it matches
    */
   final Map<QualifiedName, String> route(String path, Map<String, String[]> requestParams)
   {
      Map<QualifiedName, String> ret = null;

      // Check request parameters
      Map<QualifiedName, String> routeRequestParams = Collections.emptyMap();
      if (requestParamDefs.size() > 0)
      {
         for (RequestParamDef requestParamDef : requestParamDefs.values())
         {
            String[] values = requestParams.get(requestParamDef.getMatchName());
            if (values != null && values.length > 0)
            {
               String value = values[0];
               if (value != null)
               {
                  Matcher matcher = requestParamDef.matchValue.matcher(value);
                  if (matcher.matches())
                  {
                     if (routeRequestParams.isEmpty())
                     {
                        routeRequestParams = new HashMap<QualifiedName, String>();
                     }
                     routeRequestParams.put(requestParamDef.getName(), value);
                     continue;
                  }
               }
            }
            return null;
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
         else
         {
            // Find the next '/' for determining the segment and next path
            int pos = path.indexOf('/', 1);
            if (pos == -1)
            {
               pos = path.length();
            }

            String segment = path.substring(1, pos);

            // Try to find a route for the segment
            List<SegmentRoute> routes = segments.get(segment);
            if (routes != null)
            {
               // Determine next path
               String nextPath;
               if (pos == path.length())
               {
                  nextPath = "/";
               }
               else
               {
                  nextPath = path.substring(pos);
               }

               //
               for (SegmentRoute route : routes)
               {
                  // Delegate the process to the next route
                  Map<QualifiedName, String> response = route.route(nextPath, requestParams);

                  // If we do have a response we return it
                  if (response != null)
                  {
                     ret = response;
                     break;
                  }
               }
            }
         }

         // Try to find a pattern matching route otherwise
         if (ret == null)
         {
            for (PatternRoute route : patterns)
            {
               Matcher matcher = route.pattern.matcher(path.substring(1));

               // We match
               if (matcher.find())
               {
                  // Build next controller context
                  int nextPos = matcher.end() + 1;
                  String nextPath;
                  if (path.length() == nextPos)
                  {
                     nextPath = "/";
                  }
                  else
                  {
                     nextPath = path.substring(nextPos);
                  }

                  // Delegate to next route
                  Map<QualifiedName, String> response = route.route(nextPath, requestParams);

                  // If we do have a response we return it
                  if (response != null)
                  {
                     // Append parameters
                     int group = 1;
                     for (QualifiedName parameterName : route.parameterNames)
                     {
                        response.put(parameterName, matcher.group(group++));
                     }

                     //
                     ret = response;
                     break;
                  }
               }
            }
         }

         // Update parameters if it is possible
         if (ret != null)
         {
            if (routeParameters.size() > 0)
            {
               for (Map.Entry<QualifiedName, String> entry : routeParameters.entrySet())
               {
                  if (!ret.containsKey(entry.getKey()))
                  {
                     ret.put(entry.getKey(), entry.getValue());
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

   /** . */
   private static final Pattern PARAMETER_REGEX = Pattern.compile("^(?:\\{([^\\}]*)\\})?(.*)$");

   final <R extends Route> R add(R route)
   {
      if (route.parent != null)
      {
         throw new IllegalArgumentException();
      }

      //
      if (route instanceof SegmentRoute)
      {
         SegmentRoute segment = (SegmentRoute)route;
         List<SegmentRoute> routes = segments.get(segment.name);
         if (routes == null)
         {
            routes = new ArrayList<SegmentRoute>();
            segments.put(segment.name, routes);
         }
         routes.add(segment);
         ((Route)segment).parent = this;
         return (R)segment;
      }
      else if (route instanceof PatternRoute)
      {
         PatternRoute pattern = (PatternRoute)route;

         patterns.add(pattern);
         route.parent = this;
         return (R)pattern;
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }

   final Set<String> getSegmentNames()
   {
      return segments.keySet();
   }

   final int getSegmentSize(String segmentName)
   {
      List<SegmentRoute> routes = segments.get(segmentName);
      return routes != null ? routes.size() : 0;
   }

   final SegmentRoute getSegment(String segmentName, int index)
   {
      List<SegmentRoute> routes = segments.get(segmentName);
      return routes != null ? routes.get(index) : null;
   }

   final int getPatternSize()
   {
      return patterns.size();
   }

   final PatternRoute getPattern(int index)
   {
      return patterns.get(index);
   }

   final Route append(RouteDescriptor descriptor)
   {
      Route route = append(descriptor.getPath());

      //
      route.terminal = true;
      route.routeParameters.putAll(descriptor.getParameters());
      for (RequestParamDescriptor requestParamDescriptor : descriptor.getRequestParams().values())
      {
         RequestParamDef requestParamDef = new RequestParamDef(requestParamDescriptor);
         route.requestParamDefs.put(requestParamDef.getMatchName(), requestParamDef);
      }

      //
      for (RouteDescriptor childDescriptor : descriptor.getChildren())
      {
         route.append(childDescriptor);
      }

      //
      return route;
   }

   final Route append(
      String path,
      Map<QualifiedName, String> parameters)
   {
      Route route = append(path);
      route.terminal = true;
      route.routeParameters.putAll(parameters);
      return route;
   }

   /**
    * Append a path, creates the necessary routes and returns the last route added.
    *
    * @param path the path to append
    * @return the last route added
    */
   private Route append(String path)
   {
      int pos = path.length();
      int level = 0;
      List<Integer> start = new ArrayList<Integer>();
      List<Integer> end = new ArrayList<Integer>();
      for (int i = 0;i < path.length();i++)
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
         if (pos == 0)
         {
            next = this;
         }
         else
         {
            String segment = path.substring(0, pos);
            SegmentRoute route = new SegmentRoute(segment);
            add(route);
            next = route;
         }
      }
      else
      {
         if (start.size() == end.size())
         {
            List<QualifiedName> parameterNames = new ArrayList<QualifiedName>();
            PatternBuilder builder = new PatternBuilder();
            builder.expr("^");
            List<String> chunks = new ArrayList<String>();
            List<Pattern> parameterPatterns = new ArrayList<Pattern>();
            int previous = 0;
            for (int i = 0;i < start.size();i++)
            {
               builder.litteral(path, previous, start.get(i));
               chunks.add(path.substring(previous, start.get(i)));
               String parameterDef = path.substring(start.get(i) + 1, end.get(i));
               int colon = parameterDef.indexOf(':');
               String regex;
               String parameterName;
               if (colon == -1)
               {
                  regex = "[^/]+";
                  parameterName = parameterDef;
               }
               else
               {
                  regex = parameterDef.substring(colon + 1);
                  parameterName = parameterDef.substring(0, colon);
               }

               //
               QualifiedName parameterQName;
               Matcher parameterMatcher = PARAMETER_REGEX.matcher(parameterName);
               if (parameterMatcher.matches())
               {
                  String qualifier = parameterMatcher.group(1);
                  String name = parameterMatcher.group(2);
                  parameterQName = new QualifiedName(qualifier == null ? "" : qualifier, name);
               }
               else
               {
                  throw new AssertionError();
               }


               //
               builder.expr("(");
               builder.expr(regex);
               builder.expr(")");
               parameterNames.add(parameterQName);
               parameterPatterns.add(Pattern.compile("^" + regex + "$"));
               previous = end.get(i) + 1;
            }
            builder.litteral(path, previous, pos);
            chunks.add(path.substring(previous, pos));
            // Julien : should the pattern end with a $ ?????? I don't see that for now
            // we need to figure out clearly
            Pattern pattern = builder.build();
            PatternRoute route = new PatternRoute(pattern, parameterNames, parameterPatterns, chunks);

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
         return next.append(path.substring(pos + 1));
      }
      else
      {
         return next;
      }
   }
}

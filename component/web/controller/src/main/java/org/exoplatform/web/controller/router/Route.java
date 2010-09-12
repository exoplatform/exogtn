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

import java.util.ArrayList;
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

   Route()
   {
      this.parent = null;
      this.terminal = false;
      this.segments = new LinkedHashMap<String, List<SegmentRoute>>();
      this.patterns = new ArrayList<PatternRoute>();
      this.routeParameters = new HashMap<QualifiedName, String>();
   }

   /**
    * Ok, so this is not the fastest way to do it, but for now it's OK, it's what is needed, we'll find
    * a way to optimize it later with some precompilation. 
    */
   final String render(Map<QualifiedName, String> blah)
   {
      Route r = find(blah);
      if (r == null)
      {
         return null;
      }
      else
      {
         if (r instanceof PatternRoute || r instanceof SegmentRoute)
         {
            StringBuilder sb = new StringBuilder();
            r.render(blah, sb);
            return sb.toString();
         }
         else
         {
            return "/";
         }
      }
   }

   private void render(Map<QualifiedName, String> blah, StringBuilder sb)
   {
      if (parent != null)
      {
         parent.render(blah, sb);
      }

      //
      if (this instanceof SegmentRoute)
      {
         SegmentRoute sr = (SegmentRoute)this;
         sb.append('/').append(sr.name);
      }
      else if (this instanceof PatternRoute)
      {
         PatternRoute pr = (PatternRoute)this;
         sb.append('/');
         int i = 0;
         while (i < pr.parameterNames.size())
         {
            sb.append(pr.chunks.get(i));
            String value = blah.get(pr.parameterNames.get(i));
            sb.append(value);
            i++;
         }
         sb.append(pr.chunks.get(i));
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
    * Note : the parameters arguments is modified, I don't like it much but as this is only used
    * by the framework, there is no side effects, but I should investigate about doing this in a
    * better way.
    *
    * @param path the path
    * @param parameters the parameters
    * @return null or the parameters when it matches
    */
   final Map<QualifiedName, String> route(String path, Map<QualifiedName, String> parameters)
   {
      Map<QualifiedName, String> ret = null;

      // Anything that does not begin with '/' returns null
      if (path.length() > 0 && path.charAt(0) == '/')
      {


         // The '/' means the current controller if any, otherwise it may be processed by the pattern matching
         if (path.length() == 1)
         {
            if (terminal)
            {
               ret = parameters;
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
                  Map<QualifiedName, String> response = route.route(nextPath, parameters);

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
                  // Update parameters
                  int group = 1;
                  for (QualifiedName parameterName : route.parameterNames)
                  {
                     parameters.put(parameterName, matcher.group(group++));
                  }

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
                  Map<QualifiedName, String> response = route.route(nextPath, parameters);

                  // If we do have a response we return it
                  if (response != null)
                  {
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
         }
      }

      //
      return ret;
   }

   /** . */
   private static final Pattern PARAMETER_REGEX = Pattern.compile("^(?:\\{([^\\}]*)\\})?(.*)$");

   final Route append(
      String path,
      Map<QualifiedName, String> parameters)
   {
      Route route = append(path);
      route.terminal = true;
      route.routeParameters.putAll(parameters);
      return route;
   }

   <R extends Route> R add(R route)
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

   Set<String> getSegmentNames()
   {
      return segments.keySet();
   }

   int getSegmentSize(String segmentName)
   {
      List<SegmentRoute> routes = segments.get(segmentName);
      return routes != null ? routes.size() : 0;
   }

   SegmentRoute getSegment(String segmentName, int index)
   {
      List<SegmentRoute> routes = segments.get(segmentName);
      return routes != null ? routes.get(index) : null;
   }

   int getPatternSize()
   {
      return patterns.size();
   }

   PatternRoute getPattern(int index)
   {
      return patterns.get(index);
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
            builder.appendExpression("^");
            List<String> chunks = new ArrayList<String>();
            List<Pattern> parameterPatterns = new ArrayList<Pattern>();
            int previous = 0;
            for (int i = 0;i < start.size();i++)
            {
               builder.append(path, previous, start.get(i));
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
               builder.appendExpression("(");
               builder.appendExpression(regex);
               builder.appendExpression(")");
               parameterNames.add(parameterQName);
               parameterPatterns.add(Pattern.compile("^" + regex + "$"));
               previous = end.get(i) + 1;
            }
            builder.append(path, previous, pos);
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

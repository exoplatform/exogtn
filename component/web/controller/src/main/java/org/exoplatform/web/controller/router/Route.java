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

import org.exoplatform.web.controller.ControllerContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.protocol.ProcessResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   String controllerRef;

   /** . */
   final Map<String, SimpleRoute> simpleRoutes;

   /** . */
   final List<PatternRoute> patternRoutes;

   /** . */
   final Map<QualifiedName, String[]> routeParameters;

   Route()
   {
      this.simpleRoutes = new HashMap<String, SimpleRoute>();
      this.patternRoutes = new ArrayList<PatternRoute>();
      this.routeParameters = new HashMap<QualifiedName, String[]>();
   }

   /**
    * todo: a version that does not string concatenation but instead a buffer 
    */
   String render(String controllerId, Map<QualifiedName, String[]> blah)
   {
      if (controllerRef != null && controllerRef.equals(controllerId))
      {
         for (Map.Entry<QualifiedName, String[]> entry : routeParameters.entrySet())
         {
            String[] a = blah.get(entry.getKey());
            if (a == null || !Arrays.equals(entry.getValue(), a))
            {
               return null;
            }
         }
         return "/";
      }
      else
      {
         for (Map.Entry<String, SimpleRoute> a : simpleRoutes.entrySet())
         {
            String b = a.getValue().render(controllerId, blah);
            if (b != null)
            {
               if (b.length() > 1)
               {
                  return "/" + a.getKey() + b;
               }
               else
               {
                  return "/" + a.getKey();
               }
            }
         }
         there:
         for (PatternRoute a : patternRoutes)
         {
            int i = 0;
            while (i < a.parameterNames.size())
            {
               String[] value = blah.get(a.parameterNames.get(i));
               if (value == null || value.length < 1)
               {
                  continue there;
               }
               if (!a.parameterPatterns.get(i).matcher(value[0]).matches())
               {
                  continue there;
               }
               i++;
            }

            //
            i = 0;
            StringBuilder sb = new StringBuilder();
            while (i < a.parameterNames.size())
            {
               sb.append(a.chunks.get(i));
               String[] value = blah.get(a.parameterNames.get(i));
               sb.append(value[0]);
               i++;
            }
            sb.append(a.chunks.get(i));

            //
            String bilto = a.render(controllerId, blah);
            if (bilto != null)
            {
               if (bilto.length() > 1)
               {
                  return "/" + sb + bilto;
               }
               else
               {
                  return "/" + sb.toString();
               }
            }
         }
      }
      return null;
   }

   ProcessResponse route(ControllerContext context)
   {
      String path = context.getPath();

      ProcessResponse ret = null;

      // Anything that does not begin with '/' returns null
      if (path.length() > 0 && path.charAt(0) == '/')
      {


         // The '/' means the current controller if any, otherwise it may be processed by the pattern matching
         if (path.length() == 1)
         {
            if (controllerRef != null)
            {
               ret = new ProcessResponse(controllerRef, context.getPath(), context.getParameters());
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
            Route route = simpleRoutes.get(segment);
            if (route != null)
            {
               Map<QualifiedName, String[]> parameters = context.getParameters();

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

               // Build next controller context
               ControllerContext nextContext = new ControllerContext(nextPath, parameters);

               // Delegate the process to the next route
               ProcessResponse response = route.route(nextContext);

               // If we do have a response we return it
               if (response != null)
               {
                  ret = response;
               }
            }
         }

         // Try to find a pattern matching route
         for (PatternRoute route : patternRoutes)
         {
            Matcher matcher = route.pattern.matcher(path.substring(1));

            // We match
            if (matcher.find())
            {
               // Update parameters
               Map<QualifiedName, String[]> parameters = new HashMap<QualifiedName, String[]>(context.getParameters());
               int group = 1;
               for (QualifiedName parameterName : route.parameterNames)
               {
                  parameters.put(parameterName, new String[]{matcher.group(group++)});
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

               //
               ControllerContext nextContext = new ControllerContext(nextPath, parameters);

               // Delegate to next route
               ProcessResponse response = route.route(nextContext);

               // If we do have a response we return it
               if (response != null)
               {
                  ret = response;
                  break;
               }
            }
         }

         // Update parameters if it is possible
         if (ret != null)
         {
            if (routeParameters.size() > 0)
            {
               for (Map.Entry<QualifiedName, String[]> entry : routeParameters.entrySet())
               {
                  if (!ret.getParameters().containsKey(entry.getKey()))
                  {
                     // julien : should do a safe put all here on String[]
                     ret.getParameters().put(entry.getKey(), entry.getValue());
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

   Route append(
      String path,
      String controllerRef,
      Map<QualifiedName, String[]> parameters)
   {
      Route route = append(path);
      route.controllerRef = controllerRef;
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
            SimpleRoute route = new SimpleRoute(path.substring(0, pos));
            simpleRoutes.put(route.value, route);
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
            patternRoutes.add(route);
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

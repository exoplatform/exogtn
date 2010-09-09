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

package org.exoplatform.portal.url;

import org.exoplatform.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalURL<R, L extends ResourceLocator<R>> extends ResourceURL<R, L>
{

   private List<Parameter> params;

   /** . */
   private final PortalRequestContext requestContext;

   public PortalURL(PortalRequestContext requestContext, L locator, Boolean ajax)
   {
      super(locator, ajax);

      //
      if (requestContext == null)
      {
         throw new NullPointerException("No null request context");
      }

      //
      this.requestContext = requestContext;
   }

   public PortalURL<R, L> addParameters(Parameter... param)
   {
      if (params == null)
      {
         params = new ArrayList<Parameter>();
      }
      params.addAll(Arrays.asList(param));
      return this;
   }

   public PortalURL<R, L> setParameters(Parameter... param)
   {
      params = (param != null) ? Arrays.asList(param) : null;
      return this;
   }

   public Parameter[] getParameters()
   {
      return (Parameter[])params.toArray();
   }

   public String toString()
   {
      //
      StringBuilder url = new StringBuilder();

      if (ajax)
      {
         url.append(requestContext.getRequestURI());
      }
      else
      {
         if (locator.getResource() == null)
         {
            throw new IllegalStateException("No resource set of the portal URL");
         }
         
         //
         url.append(requestContext.getPortalURI());
         
         //
         try
         {
            locator.append(url);
         }
         catch (IOException e)
         {
            AssertionError ae = new AssertionError();
            ae.initCause(e);
            throw ae;
         }
      }

      if (ajax || params != null)
      {
         url.append("?");
         boolean addedAmpersand = false;

         if (params != null)
         {
            for (Parameter param : params)
            {
               if (addedAmpersand)
               {
                  url.append(Constants.AMPERSAND);
               }
               url.append(param.getName()).append("=").append(param.getValue());
               addedAmpersand = true;
            }
         }

         if (ajax)
         {
            if (addedAmpersand)
            {
               url.append(Constants.AMPERSAND);
            }
            url.append("ajaxRequest=true");

            //adding the ajaxGet javascript function to handle the response
            url.insert(0, "javascript:ajaxGet('");
            url.append("')");
         }
      }

      return url.toString();
   }
}

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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceURL;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalURL<R, L extends ResourceLocator<R>> extends ResourceURL<R, L>
{

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

   public String toString()
   {
      if (locator.getResource() == null)
      {
         throw new IllegalStateException("No resource set of the portal URL");
      }

      //
      StringBuilder sb = new StringBuilder();

      //
      sb.append(requestContext.getPortalURI());

      //
      try
      {
         locator.append(sb);
      }
      catch (IOException e)
      {
         AssertionError ae = new AssertionError();
         ae.initCause(e);
         throw ae;
      }

      //
      return sb.toString();
   }
}

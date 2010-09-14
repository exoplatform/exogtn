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

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceURL;

import javax.portlet.MimeResponse;
import javax.portlet.PortletURL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletResourceURL<R, L extends ResourceLocator<R>> extends ResourceURL<R, L>
{

   /** . */
   public static final QualifiedName CONFIRM = new QualifiedName("gtn", "confirm");

   /** . */
   private PortletURL url;

   private final MimeResponse response;


   public PortletResourceURL(MimeResponse response, L locator, Boolean ajax) throws NullPointerException
   {
      super(locator, ajax);

      //
      this.response = response;
   }

   @Override
   public String toString()
   {
      if (url == null)
      {
         url = response.createActionURL();
      }

      //
      for (QualifiedName parameterName : locator.getParameterNames())
      {
         String parameterValue = locator.getParameterValue(parameterName);
         url.setParameter(parameterName.getValue(), parameterValue);
      }

      //
      return url.toString();
   }
}

/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.gatein.portal.samples.api;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

/**
 * This portlet shows how to leverage the portlet URL <code>gtn:lang</code> property to set the language
 * for render urls.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LocaleURLPortlet extends GenericPortlet
{

   @Override
   protected void doView(RenderRequest req, RenderResponse resp) throws PortletException, IOException
   {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      PortletURL url = resp.createRenderURL();
      Locale current = req.getLocale();
      writer.print("<div>Current locale: " + current + "</div>");
      writer.print("<ul>");

      // Setting the gtn:lang property to the empty string removes the language from the url
      url.setProperty("gtn:lang", "");
      writer.print("<li>");
      writer.print("<a href='" + url + "'>None</a>");
      writer.print("</li>");

      // Setting the gtn:lang property to a valid locale value will set the locale in the url
      // note that this is valid for language and country code, variant are not supported
      for (String lang : new String[]{"en","fr","it","vi"})
      {
         url.setProperty("gtn:lang", lang);
         writer.print("<li>");
         writer.print("<a href='" + url + "'>" + new Locale(lang).getDisplayName(current) + "</a>");
         writer.print("</li>");
      }

      //
      writer.print("</ul>");
      writer.close();
   }
}

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

/**
 * The render context provides callback when URL generation is performed by the {@link Router#render(java.util.Map)}
 * method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface RenderContext
{

   /**
    * Append a char to the path.
    *
    * @param c the char to append
    * @param escape when the char should be escaped
    */
   void appendPath(char c, boolean escape);

   /**
    * Append a string to the path.
    *
    * @param s the string to append.
    * @param escape when the char should be escaped
    */
   void appendPath(String s, boolean escape);

   /**
    * Append a query parameter to the parameter set. Note that the query parameters are ordered
    * and the sequence of call to this method should be honoured when an URL is generated. Note also that
    * the same parameter name can be used multiple times.
    *
    * @param parameterName the parameter name
    * @param paramaterValue the parameter value
    */
   void appendQueryParameter(String parameterName, String paramaterValue);

}

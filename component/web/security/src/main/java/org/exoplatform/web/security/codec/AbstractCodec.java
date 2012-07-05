/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.web.security.codec;

/**
 * Abstract codec used to encode/decode password stored/loaded on/from token entry
 * 
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Nov 19, 2010
 */

public abstract class AbstractCodec
{

   public String getName()
   {
      return this.getClass().toString();
   }
   
   public abstract String encode(String plainInput);
   
   public abstract String decode(String encodedInput);
   
}

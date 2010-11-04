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

package org.exoplatform.web.controller.metadata;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.EncodingMode;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PathParamDescriptor
{

   /** . */
   private final QualifiedName name;

   /** . */
   private final String pattern;

   /** . */
   private final EncodingMode encodingMode;

   public PathParamDescriptor(QualifiedName name, String pattern, EncodingMode encodingMode)
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      if (pattern == null)
      {
         throw new NullPointerException("No null pattern accepted");
      }

      //
      this.name = name;
      this.pattern = pattern;
      this.encodingMode = encodingMode;
   }

   public QualifiedName getName()
   {
      return name;
   }

   public String getPattern()
   {
      return pattern;
   }

   public EncodingMode getEncodingMode()
   {
      return encodingMode;
   }
}

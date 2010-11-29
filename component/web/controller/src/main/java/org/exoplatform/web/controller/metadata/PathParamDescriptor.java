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
   private final QualifiedName qualifiedName;

   /** . */
   private String pattern;

   /** . */
   private EncodingMode encodingMode = EncodingMode.FORM;

   public PathParamDescriptor(QualifiedName qualifiedName)
   {
      if (qualifiedName == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      this.qualifiedName = qualifiedName;
   }

   public PathParamDescriptor(String qualifiedName)
   {
      if (qualifiedName == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      this.qualifiedName = QualifiedName.parse(qualifiedName);
   }

   public PathParamDescriptor withPattern(String pattern)
   {
      this.pattern = pattern;
      return this;
   }

   public PathParamDescriptor withEncodingMode(EncodingMode encodingMode)
   {
      this.encodingMode = encodingMode;
      return this;
   }

   public PathParamDescriptor preservingPath()
   {
      this.encodingMode = EncodingMode.PRESERVE_PATH;
      return this;
   }

   public PathParamDescriptor form()
   {
      this.encodingMode = EncodingMode.FORM;
      return this;
   }

   public QualifiedName getQualifiedName()
   {
      return qualifiedName;
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

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
import org.exoplatform.web.controller.metadata.RequestParamDescriptor;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class RequestParam
{

   /** . */
   final QualifiedName name;

   /** . */
   final String matchName;

   /** . */
   final Pattern matchValue;

   /** . */
   final boolean required;

   RequestParam(RequestParamDescriptor descriptor)
   {
      if (descriptor == null)
      {
         throw new NullPointerException("No null descriptor accepted");
      }

      //
      Pattern matchValue = null;
      if (descriptor.getValue() != null)
      {
         PatternBuilder matchValueBuilder = new PatternBuilder();
         matchValueBuilder.expr("^");
         int level = 0;
         for (char c : descriptor.getValue().toCharArray())
         {
            switch (c)
            {
               case '{':

                  if (level++ > 0)
                  {
                     matchValueBuilder.expr('{');
                  }
                  break;
               case '}':
                  if (--level > 0)
                  {
                     matchValueBuilder.expr('}');
                  }
                  break;
               default:
                  if (level == 0)
                  {
                     matchValueBuilder.litteral(c);
                  }
                  else
                  {
                     matchValueBuilder.expr(c);
                  }
                  break;
            }
         }
         matchValueBuilder.expr("$");
         matchValue = matchValueBuilder.build();
      }

      //
      this.name = descriptor.getQualifiedName();
      this.matchName = descriptor.getName();
      this.matchValue = matchValue;
      this.required = descriptor.isRequired();
   }

   RequestParam(QualifiedName name, String matchName, Pattern matchValue, boolean required)
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      if (matchName == null)
      {
         throw new NullPointerException("No null match name accepted");
      }
      if (matchValue == null)
      {
         throw new NullPointerException("No null match value accepted");
      }

      //
      this.name = name;
      this.matchName = matchName;
      this.matchValue = matchValue;
      this.required = required;
   }

   public QualifiedName getName()
   {
      return name;
   }

   public String getMatchName()
   {
      return matchName;
   }

   public Pattern getMatchValue()
   {
      return matchValue;
   }

   public boolean matchValue(String value)
   {
      return matchValue == null || matchValue.matcher(value).matches();
   }

   public boolean isRequired()
   {
      return required;
   }
}

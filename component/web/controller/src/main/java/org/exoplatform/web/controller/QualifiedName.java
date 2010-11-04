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

package org.exoplatform.web.controller;

/**
 * A qualified name that is a qualifier and a name. It can be seen as a simplified version of an XML QName
 * that retains only the prefix (qualifier) and the local name (name) and leaves out the namespace.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class QualifiedName
{

   public static QualifiedName parse(String qname)
   {
      if (qname.length() > 0)
      {
         int index = qname.indexOf(':');
         if (index > -1)
         {
            return new QualifiedName(qname.substring(0, index), qname.substring(index + 1));
         }
      }
      return new QualifiedName(qname);
   }

   /** . */
   private final String qualifier;

   /** . */
   private final String name;

   /**
    * Creates a qualified name with an empty string qualifier.
    *
    * @param name the name
    */
   public QualifiedName(String name)
   {
      this("", name);
   }

   /**
    * Creates a qualified name.
    *
    * @param qualifier the qualifier
    * @param name the name
    */
   public QualifiedName(String qualifier, String name)
   {
      if (qualifier == null)
      {
         throw new NullPointerException("No null prefix accepted");
      }
      if (name == null)
      {
         throw new NullPointerException("No null prefix accepted");
      }

      //
      this.qualifier = qualifier;
      this.name = name;
   }

   public String getQualifier()
   {
      return qualifier;
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      if (qualifier.isEmpty())
      {
         return name;
      }
      else
      {
         return qualifier + ":" + name;
      }
   }

   @Override
   public int hashCode()
   {
      return qualifier.hashCode() ^ name.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof QualifiedName)
      {
         QualifiedName that = (QualifiedName)obj;
         return qualifier.equals(that.qualifier) && name.equals(that.name);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "QualifiedName[prefix=" + qualifier + ",name=" + name + "]";
   }
}

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

package org.exoplatform.web.url;

import org.gatein.common.util.ParameterMap;

import java.util.Map;

/**
 * An URL for a resource.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ResourceURL<R, L extends ResourceLocator<R>>
{

   /** . */
   private static final ParameterMap.AccessMode ACCES_MODE = ParameterMap.AccessMode.get(false, false);

   /** . */
   protected final L locator;

   /** . */
   protected Boolean ajax;

   /** . */
   protected String confirm;

   /** . */
   protected ParameterMap queryParams;

   /**
    * Create a resource URL instance.
    *
    * @param locator the resource locator that can't be null
    * @param ajax the ajax mode
    * @throws NullPointerException if the resource locator is null
    */
   public ResourceURL(L locator, Boolean ajax) throws NullPointerException
   {
      if (locator == null)
      {
         throw new NullPointerException("No null locator");
      }

      //
      this.locator = locator;
      this.ajax = ajax;
      this.confirm = null;
      this.queryParams = null;
   }

   /**
    * Returns the resource locator of this URL.
    *
    * @return the resource locator
    */
   public final L getResourceLocator()
   {
      return locator;
   }

   /**
    * Returns the ajax mode.
    *
    * @return the ajax mode
    */
   public final Boolean getAjax()
   {
      return ajax;
   }

   /**
    * Update the ajax mode.
    *
    * @param ajax the new ajax mode
    * @return this object
    */
   public final ResourceURL setAjax(Boolean ajax)
   {
      this.ajax = ajax;
      return this;
   }

   /**
    * Returns the confirm message.
    *
    * @return the confirm message
    */
   public String getConfirm()
   {
      return confirm;
   }

   /**
    * Updates the confirm message.
    *
    * @param confirm the new confirm message
    * @return this object
    */
   public final ResourceURL setConfirm(String confirm)
   {
      this.confirm = confirm;
      return this;
   }

   /**
    * Returns the current resource associated with this URL.
    *
    * @return the resource
    */
   public final R getResource()
   {
      return locator.getResource();
   }

   /**
    * Set a new resource on this URL.
    *
    * @param resource the new resource
    * @return this object
    */
   public final ResourceURL setResource(R resource)
   {
      locator.setResource(resource);
      return this;
   }

   public Map<String, String[]> getQueryParameters()
   {
      if (queryParams == null)
      {
         queryParams = new ParameterMap(ACCES_MODE);
      }
      return queryParams;
   }

   public String getQueryParameterValue(String parameterName)
   {
      if (parameterName == null)
      {
         throw new NullPointerException("");
      }
      else if (queryParams == null)
      {
         return null;
      }
      else
      {
         String[] parameterValues = queryParams.get(parameterName);
         return parameterValues != null ? parameterValues[0] : null;
      }
   }

   public void setQueryParameterValue(String parameterName, String parameterValue)
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      if (queryParams == null)
      {
         queryParams = new ParameterMap(ACCES_MODE);
      }
      queryParams.setValue(parameterName, parameterValue);
   }

   public String[] getQueryParameterValues(String parameterName)
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      return queryParams != null ? queryParams.getValues(parameterName) : null;
   }

   public void setQueryParameterValues(String parameterName, String[] parameterValues)
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      if (queryParams == null)
      {
         queryParams = new ParameterMap(ACCES_MODE);
      }
      queryParams.setValues(parameterName, parameterValues);
   }

   /**
    * Generates the URL value.
    *
    * @return the URL value
    */
   public abstract String toString();
}

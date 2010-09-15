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

package org.exoplatform.webui.application;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.web.url.ResourceURL;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.url.ComponentLocator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class UIComponentURLBuilder extends URLBuilder<UIComponent>
{

   /** . */
   private final ResourceURL<UIComponent, ComponentLocator> url;

   private final ComponentLocator locator;

   public UIComponentURLBuilder(ResourceURL<UIComponent, ComponentLocator> url)
   {
      this.url = url;
      this.locator = url.getResourceLocator();
   }

   @Override
   public String createAjaxURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      return createURL(true, targetComponent, action, confirm, targetBeanId, params);
   }

   @Override
   public String createURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      return createURL(false, targetComponent, action, confirm, targetBeanId, params);
   }

   private String createURL(boolean ajax, UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      url.getQueryParameters().clear();

      //
      url.setAjax(ajax);
      url.setConfirm(confirm);
      url.setResource(targetComponent);

      //
      locator.setAction(action);
      locator.setTargetBeanId(targetBeanId);

      //
      if (params != null)
      {
         for (Parameter param : params)
         {
            url.setQueryParameterValue(param.getName(), param.getValue());
         }
      }

      //
      return url.toString();
   }
}

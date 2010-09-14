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

package org.exoplatform.webui.url;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.webui.core.UIComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ComponentLocator implements ResourceLocator<UIComponent>
{

   /** . */
   public static final ResourceType<UIComponent, ComponentLocator> TYPE = new ResourceType<UIComponent, ComponentLocator>() {};

   /** . */
   public static final QualifiedName COMPONENT = new QualifiedName("gtn", "componentid");

   /** . */
   public static final QualifiedName ACTION = new QualifiedName("gtn", "action");

   /** . */
   public static final QualifiedName TARGET = new QualifiedName("gtn", "objectid");

   /** . */
   private UIComponent resource;

   /** . */
   private Map<QualifiedName, String> parameters;

   public ComponentLocator()
   {
      this.parameters = new HashMap<QualifiedName, String>();
   }

   public UIComponent getResource()
   {
      return resource;
   }

   public void setResource(UIComponent resource)
   {
      setParameterValue(COMPONENT, resource != null ? resource.getId() : null);

      //
      this.resource = resource;
   }

   public String getAction()
   {
      return parameters.get(ACTION);
   }

   public void setAction(String action)
   {
      setParameterValue(ACTION, action);
   }

   public String getTargetBeanId()
   {
      return parameters.get(TARGET);
   }

   public void setTargetBeanId(String targetBeanId)
   {
      setParameterValue(TARGET, targetBeanId);
   }

   public void addParameter(Parameter param)
   {
      throw new UnsupportedOperationException("is it really used?");
   }

   public Set<QualifiedName> getParameterNames()
   {
      return parameters.keySet();
   }

   public String getParameterValue(QualifiedName parameterName)
   {
      return parameters.get(parameterName);
   }

   public void setParameterValue(QualifiedName parameterName, String parameterValue)
   {
      if (parameterValue == null)
      {
         parameters.remove(parameterName);
      }
      else
      {
         parameters.put(parameterName, parameterValue);
      }
   }
}

/**
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

package org.exoplatform.navigation.webui.component;

import javax.portlet.PortletRequest;

import org.exoplatform.portal.webui.page.UIPageBrowser;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Tan Pham Dinh
 *          pdtanit@gmail.com
 * Jul 29, 2009  
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
@Serialized
public class UIPageManagementPortlet extends UIPortletApplication
{
   public static String PAGE_LIST_HEIGHT = "pageListHeight";
   
   public UIPageManagementPortlet() throws Exception
   {            
      UIPageBrowser pageBrowser = addChild(UIPageBrowser.class, null, null); 
      pageBrowser.setShowAddNewPage(true);      
   }

   @Override
   public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception
   {
      PortletRequest portletRequest = ((PortletRequestContext)context).getRequest(); 
      String pref = portletRequest.getPreferences().getValue(PAGE_LIST_HEIGHT, "0");
      int height = 0;
      try 
      {
         height = Integer.parseInt(pref);         
      }
      catch (NumberFormatException ex) 
      {         
      }
      getChild(UIPageBrowser.class).setPageListHeight(height);
      super.processRender(app, context);
   }
      
}

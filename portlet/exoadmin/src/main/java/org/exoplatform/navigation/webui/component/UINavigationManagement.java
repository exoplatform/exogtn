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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "app:/groovy/navigation/webui/component/UINavigationManagement.gtmpl", events = {
   @EventConfig(listeners = UINavigationManagement.SaveActionListener.class),
   @EventConfig(listeners = UINavigationManagement.AddRootNodeActionListener.class)})
public class UINavigationManagement extends UIContainer
{

   private SiteKey siteKey;

   public UINavigationManagement() throws Exception
   {
      addChild(UINavigationNodeSelector.class, null, null);
   }

   public SiteKey getSiteKey()
   {
      return siteKey;
   }
   
   public void setSiteKey(SiteKey key)
   {
      siteKey = key;
   }

   public <T extends UIComponent> T setRendered(boolean b)
   {
      return super.<T> setRendered(b);
   }

   public void loadView(Event<? extends UIComponent> event) throws Exception
   {
      UINavigationNodeSelector uiNodeSelector = getChild(UINavigationNodeSelector.class);
      UITree uiTree = uiNodeSelector.getChild(UITree.class);
      uiTree.createEvent("ChangeNode", event.getExecutionPhase(), event.getRequestContext()).broadcast();
   }

   static public class SaveActionListener extends EventListener<UINavigationManagement>
   {

      public void execute(Event<UINavigationManagement> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UINavigationManagement uiManagement = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiManagement.getChild(UINavigationNodeSelector.class);
         UserPortalConfigService portalConfigService = uiManagement.getApplicationComponent(UserPortalConfigService.class);

         UIPopupWindow uiPopup = uiManagement.getParent();
         uiPopup.createEvent("ClosePopup", Phase.PROCESS, event.getRequestContext()).broadcast();
         
         UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         prContext.ignoreAJAXUpdateOnPortlets(true);

         UserNavigation navigation = uiNodeSelector.getEdittedNavigation();
         SiteKey siteKey = navigation.getKey();
         String editedOwnerId = siteKey.getName();

         // Check existed
         UserPortalConfig userPortalConfig;
         if (SiteType.PORTAL.equals(siteKey.getType()))
         {
            userPortalConfig = portalConfigService.getUserPortalConfig(editedOwnerId, event.getRequestContext().getRemoteUser());
            if (userPortalConfig == null)
            {
               prContext.getUIApplication().addMessage(
                  new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null, ApplicationMessage.ERROR));
               return;
            }
         }
         else
         {
            userPortalConfig =  portalConfigService.getUserPortalConfig(prContext.getPortalOwner(), event.getRequestContext().getRemoteUser());
         }

         UserNavigation persistNavigation =  userPortalConfig.getUserPortal().getNavigation(siteKey);
         if (persistNavigation == null)
         {
            prContext.getUIApplication().addMessage(
               new ApplicationMessage("UINavigationManagement.msg.NavigationNotExistAnymore", null, ApplicationMessage.ERROR));
            return;
         }         

         uiNodeSelector.save();
         
         //check current node existed
         UIPortal uiPortal = Util.getUIPortal();
         UserPortal userPortal = userPortalConfig.getUserPortal();
         UserNode targetNode = userPortal.resolvePath(navigation, null, uiPortal.getSelectedUserNode().getURI());
         if(targetNode == null) 
         {
            targetNode = userPortal.getDefaultPath(UserNodeFilterConfig.builder().build());
            if(targetNode != null) 
            {
               uiPortal.setNavPath(targetNode);
               uiPortal.refreshUIPage();
            }
         }
         else
         {
           if(targetNode.getId().equals(uiPortal.getSelectedUserNode().getParent().getId()))
           {
             targetNode = userPortal.getDefaultPath(UserNodeFilterConfig.builder().build());
             uiPortal.setNavPath(targetNode);
             uiPortal.refreshUIPage();
           }
         }
      }
   }

   static public class AddRootNodeActionListener extends EventListener<UINavigationManagement>
   {

      @Override
      public void execute(Event<UINavigationManagement> event) throws Exception
      {
         UINavigationManagement uiManagement = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiManagement.getChild(UINavigationNodeSelector.class);
         UIRightClickPopupMenu menu = uiNodeSelector.getChild(UIRightClickPopupMenu.class);
         menu.createEvent("AddNode", Phase.PROCESS, event.getRequestContext()).broadcast();
      }

   }
}
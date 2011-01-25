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

package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.MoveChildActionListener;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * May 19, 2006
 */
@ComponentConfig(lifecycle = UIPageLifecycle.class, template = "system:/groovy/portal/webui/page/UIPage.gtmpl", events = {@EventConfig(listeners = MoveChildActionListener.class),
	@EventConfig(name = "EditCurrentPage", listeners = UIPage.EditCurrentPageActionListener.class)})
public class UIPage extends UIContainer
{
   /** . */
   private String pageId;

   private String ownerId;

   private String ownerType;

   private String editPermission;

   private boolean showMaxWindow = false;

   private UIPortlet maximizedUIPortlet;

   protected static Map<String, Class<? extends UIPage>> realClass;

   public static String DEFAULT_FACTORY_ID = "Default";

   static
   {
      if (realClass == null)
      {
         realClass = new HashMap<String, Class<? extends UIPage>>();
         realClass.put(DEFAULT_FACTORY_ID, UIPage.class);
      }
   }

   public static Class<? extends UIPage> getRealClass(String factoryID)
   {
      if (factoryID == null || factoryID.trim().equals("") || factoryID.trim().equals(DEFAULT_FACTORY_ID))
      {
         return UIPage.class;
      }
      return realClass.get(factoryID);
   }

   public String getOwnerId()
   {
      return ownerId;
   }

   public void setOwnerId(String s)
   {
      ownerId = s;
   }

   public boolean isShowMaxWindow()
   {
      return showMaxWindow;
   }

   public void setShowMaxWindow(Boolean showMaxWindow)
   {
      this.showMaxWindow = showMaxWindow;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public void setEditPermission(String editPermission)
   {
      this.editPermission = editPermission;
   }

   public String getPageId()
   {
      return pageId;
   }

   public void setPageId(String id)
   {
      pageId = id;
   }

   public UIPortlet getMaximizedUIPortlet()
   {
      return maximizedUIPortlet;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public void setMaximizedUIPortlet(UIPortlet maximizedUIPortlet)
   {
      this.maximizedUIPortlet = maximizedUIPortlet;
   }
   
   public static class EditCurrentPageActionListener extends EventListener<UIPage>
   {
		@Override
		public void execute(Event<UIPage> event) throws Exception {
			UIPortalApplication uiApp = Util.getUIPortalApplication();
			UIWorkingWorkspace uiWorkingWS = uiApp
					.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

			// check edit permission for page
			UIPageBody pageBody = uiWorkingWS
					.findFirstComponentOfType(UIPageBody.class);
			UIPage uiPage = (UIPage) pageBody.getUIComponent();
			if (uiPage == null) {
				uiApp.addMessage(new ApplicationMessage(
						"UIPageBrowser.msg.PageNotExist", null));
				return;
			}
			Page page = PortalDataMapper.toPageModel(uiPage);

			UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
			if (!userACL.hasEditPermission(page)) {
				uiApp.addMessage(new ApplicationMessage(
						"UIPortalManagement.msg.Invalid-EditPage-Permission", null));
				return;
			}

			uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

			UIPortalComposer portalComposer = uiWorkingWS.findFirstComponentOfType(
					UIPortalComposer.class).setRendered(true);
			portalComposer.setComponentConfig(UIPortalComposer.class, "UIPageEditor");
			portalComposer.setId("UIPageEditor");
			portalComposer.setShowControl(true);
			portalComposer.setEditted(false);
			portalComposer.setCollapse(false);

			UIPortalToolPanel uiToolPanel = uiWorkingWS
					.findFirstComponentOfType(UIPortalToolPanel.class);
			uiToolPanel.setShowMaskLayer(false);
			uiApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);

			// We clone the edited UIPage object, that is required for Abort action
			Class<? extends UIPage> clazz = UIPage.getRealClass(uiPage.getFactoryId());
			UIPage newUIPage = uiWorkingWS.createUIComponent(clazz, null, null);
			PortalDataMapper.toUIPage(newUIPage, page);
			uiToolPanel.setWorkingComponent(newUIPage);

			// Remove current UIPage from UIPageBody
			pageBody.setUIComponent(null);

			event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
			Util.getPortalRequestContext().setFullRender(true);

		}
   }
}
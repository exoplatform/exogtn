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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputItemSelector;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormPopupWindow;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NotHTMLTagValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;
import org.exoplatform.webui.organization.UIListPermissionSelector;
import org.exoplatform.webui.organization.UIListPermissionSelector.EmptyIteratorValidator;
import org.exoplatform.webui.organization.UIPermissionSelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ComponentConfigs({
   @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
      @EventConfig(listeners = UIPageForm.SaveActionListener.class),
      @EventConfig(listeners = UIPageForm.ChangeOwnerTypeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageForm.SelectMembershipActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageForm.ChangeOwnerIdActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE)}, initParams = @ParamConfig(name = "PageTemplate", value = "system:/WEB-INF/conf/uiconf/portal/webui/page/PageTemplate.groovy")),
   @ComponentConfig(type = UIFormInputSet.class, id = "PermissionSetting", template = "system:/groovy/webui/core/UITabSelector.gtmpl", events = {@EventConfig(listeners = UIFormInputSet.SelectComponentActionListener.class)})})
public class UIPageForm extends UIFormTabPane
{

   private UIPage uiPage_;

   protected UIFormInputSet uiPermissionSetting;

   protected UIFormSelectBox groupIdSelectBox = null;

   protected UIFormStringInput ownerIdInput = null;

   public static final String OWNER_TYPE = "ownerType";

   public static final String OWNER_ID = "ownerId";

   public UIPageForm(InitParams initParams) throws Exception
   {
      super("UIPageForm");

      setActions(new String[]{"Save", "Close"});
   }

   public UIPage getUIPage()
   {
      return uiPage_;
   }

   /**
    * @deprecated use {@link #buildForm(UIPage)} to initialize page form instead
    * 
    * @param uiPage
    * @throws Exception
    */
   @Deprecated
   public void setValues(UIPage uiPage) throws Exception
   {
      buildForm(uiPage);
   }

   public void invokeSetBindingBean(Object bean) throws Exception
   {
      String ownerType = getUIFormSelectBox("ownerType").getValue();
      String ownerId = getUIStringInput("ownerId").getValue();

      //As ownerId is now normalized, we have to maker sure that owenerId of 'group' type starts with a '/'
      if (SiteType.GROUP.getName().equals(ownerType) && ownerId.charAt(0) != '/')
      {
         ownerId = "/" + ownerId;
      }

      Page page = (Page)bean;
      page.setPageId(getUIStringInput("pageId").getValue());
      page.setOwnerType(ownerType);
      page.setOwnerId(ownerId);
      page.setName(getUIStringInput("name").getValue());
      String title = getUIStringInput("title").getValue();
      if (title == null || title.trim().length() < 1)
         title = page.getName();
      page.setTitle(title);

      if (!page.isShowMaxWindow())
      {
         page.setShowMaxWindow((Boolean)getUIFormCheckBoxInput("showMaxWindow").getValue());
      }
      if (!SiteType.USER.getName().equals(page.getOwnerType()))
      {
         page.setAccessPermissions(uiPermissionSetting.getChild(UIListPermissionSelector.class).getValue());
         page.setEditPermission(uiPermissionSetting.getChild(UIPermissionSelector.class).getValue());
      }
      UserACL userACL = getApplicationComponent(UserACL.class);
      userACL.hasPermission(page);

      UIFormInputItemSelector uiTemplate = getChildById("Template");
      if (uiTemplate != null)
      {
         SelectItemOption<?> itemOption = uiTemplate.getSelectedItemOption();
         if (itemOption != null)
         {
            page.setFactoryId(itemOption.getIcon());
            //        page.setTemplate((String)itemOption.getValue());
         }
      }
      UIPageTemplateOptions uiConfigOptions = getChild(UIPageTemplateOptions.class);
      if (uiConfigOptions == null)
         return;
      Page selectedPage = uiConfigOptions.createPageFromSelectedOption(page.getOwnerType(), page.getOwnerId());
      if (selectedPage == null)
         return;
      page.setChildren(selectedPage.getChildren());
      page.setFactoryId(selectedPage.getFactoryId());
   }
   
   private void loadMakableGroupNavigations() throws Exception
   {
      if (groupIdSelectBox == null)
      {
         UIFormInputSet uiSettingSet = getChildById("PageSetting");
         PortalRequestContext pcontext = Util.getPortalRequestContext();
         UserPortalConfigService userPortalConfigService= getApplicationComponent(UserPortalConfigService.class);
         List<String> groups = userPortalConfigService.getMakableNavigations(pcontext.getRemoteUser(), true);
         if (groups.size() > 0)
         {
            Collections.sort(groups);
            List<SelectItemOption<String>> groupsItem = new ArrayList<SelectItemOption<String>>();
            for (String group : groups)
            {
               groupsItem.add(new SelectItemOption<String>(group));
            }
            groupIdSelectBox = new UIFormSelectBox(OWNER_ID, OWNER_ID, groupsItem);
            groupIdSelectBox.setOnChange("ChangeOwnerId");
            groupIdSelectBox.setParent(uiSettingSet);
         }
      }
   }
   
   public void buildForm(UIPage uiPage) throws Exception
   {
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      DataStorage dataStorage = getApplicationComponent(DataStorage.class);

      PortalConfig pConfig = dataStorage.getPortalConfig(pcontext.getPortalOwner());
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL)container.getComponentInstanceOfType(UserACL.class);

      UIFormInputSet uiSettingSet = new UIFormInputSet("PageSetting");
      uiSettingSet.addUIFormInput(new UIFormStringInput("pageId", "pageId", null).setReadOnly(true));
      
      List<SelectItemOption<String>> ownerTypes = new ArrayList<SelectItemOption<String>>();
      if (pConfig != null && acl.hasEditPermission(pConfig))
      {
         ownerTypes.add(new SelectItemOption<String>(SiteType.PORTAL.getName()));
      }
      ownerTypes.add(new SelectItemOption<String>(SiteType.GROUP.getName()));
      ownerTypes.add(new SelectItemOption<String>(SiteType.USER.getName()));
      UIFormSelectBox uiSelectBoxOwnerType = new UIFormSelectBox(OWNER_TYPE, OWNER_TYPE, ownerTypes);
      uiSelectBoxOwnerType.setOnChange("ChangeOwnerType");
      uiSettingSet.addUIFormInput(uiSelectBoxOwnerType);
      
      ownerIdInput = new UIFormStringInput(OWNER_ID, OWNER_ID, null);
      ownerIdInput.setReadOnly(true).setValue(pcontext.getRemoteUser());
      uiSettingSet.addUIFormInput(ownerIdInput);
      
      uiSettingSet.addUIFormInput(
            new UIFormStringInput("name", "name", null).addValidator(StringLengthValidator.class, 3, 30)
               .addValidator(IdentifierValidator.class).addValidator(MandatoryValidator.class))
         .addUIFormInput(
            new UIFormStringInput("title", "title", null).addValidator(StringLengthValidator.class, 3, 120).
               addValidator(NotHTMLTagValidator.class))
         .addUIFormInput(new UIFormCheckBoxInput("showMaxWindow", "showMaxWindow", false));
      
      addUIFormInput(uiSettingSet);
      setSelectedTab(uiSettingSet.getId());
      
      if (uiPage == null)
      {
         UIPageTemplateOptions uiTemplateConfig = createUIComponent(UIPageTemplateOptions.class, null, null);
         addUIFormInput(uiTemplateConfig);
      }
      
      if (uiPage == null || (!uiPage.getSiteKey().getType().equals(SiteType.USER) && getChildById("PermissionSetting") == null))
      {
         uiPermissionSetting = createUIComponent(UIFormInputSet.class, "PermissionSetting", null);
         UIListPermissionSelector uiListPermissionSelector = createUIComponent(UIListPermissionSelector.class, null, null);
         uiListPermissionSelector.configure("UIListPermissionSelector", "accessPermissions");
         uiListPermissionSelector.addValidator(EmptyIteratorValidator.class);
         uiPermissionSetting.addChild(uiListPermissionSelector);
         uiPermissionSetting.setSelectedComponent(uiListPermissionSelector.getId());
         UIPermissionSelector uiEditPermission = createUIComponent(UIPermissionSelector.class, null, null);
         uiEditPermission.setRendered(false);
         uiEditPermission.addValidator(org.exoplatform.webui.organization.UIPermissionSelector.MandatoryValidator.class);
         uiEditPermission.setEditable(false);
         uiEditPermission.configure("UIPermissionSelector", "editPermission");
         uiPermissionSetting.addChild(uiEditPermission);

         //TODO: This following line is fixed for bug PORTAL-2127
         uiListPermissionSelector.getChild(UIFormPopupWindow.class).setId("UIPageFormPopupGroupMembershipSelector");
         addUIFormInput(uiPermissionSetting);
         uiPermissionSetting.getChild(UIPermissionSelector.class).setEditable(true);
      }

      if (uiPage != null)
      {
         uiPage_ = uiPage;
         Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
         invokeGetBindingBean(page);
         getUIStringInput("name").setReadOnly(true);
         getUIStringInput("pageId").setValue(uiPage.getPageId());
         getUIStringInput("title").setValue(uiPage.getTitle());
         getUIFormCheckBoxInput("showMaxWindow").setValue(uiPage.isShowMaxWindow());
         getUIFormSelectBox(OWNER_TYPE).setDisabled(true).setValue(uiPage.getSiteKey().getTypeName());
      }

   }

   static public class SaveActionListener extends EventListener<UIPageForm>
   {
      public void execute(Event<UIPageForm> event) throws Exception
      {
         UIPageForm uiPageForm = event.getSource();
         UIPortalApplication uiPortalApp = uiPageForm.getAncestorOfType(UIPortalApplication.class);
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         UIPage uiPage = uiPageForm.getUIPage();
         if (uiPage == null)
            return;
         Page page = new Page();
         uiPageForm.invokeSetBindingBean(page);

         uiPage.setAccessPermissions(page.getAccessPermissions());
         uiPage.setEditPermission(page.getEditPermission());
         uiPage.setTitle(page.getTitle());
         uiPage.setShowMaxWindow(page.isShowMaxWindow());

         uiMaskWS.setUIComponent(null);
         uiMaskWS.setShow(false);
         pcontext.addUIComponentToUpdateByAjax(uiMaskWS);
         pcontext.getJavascriptManager().addJavascript("eXo.portal.UIPortal.changeComposerSaveButton();");
      }
   }

   static public class ChangeOwnerTypeActionListener extends EventListener<UIPageForm>
   {
      public void execute(Event<UIPageForm> event) throws Exception
      {
         UIPageForm uiForm = event.getSource();
         UIFormSelectBox uiSelectBox = uiForm.getUIFormSelectBox(OWNER_TYPE);
         String ownerType = uiSelectBox.getValue();
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIFormInputSet uiSettingSet = uiForm.getChildById("PageSetting");
         uiForm.setSelectedTab("PageSetting");
         List<UIComponent> list = uiSettingSet.getChildren();
         
         if (SiteType.PORTAL.getName().equals(ownerType))
         {
            list.remove(2);
            list.add(2, uiForm.ownerIdInput);
            uiForm.ownerIdInput.setValue(prContext.getPortalOwner());
            uiForm.findFirstComponentOfType(UIListPermissionSelector.class).setValue(
               Util.getUIPortal().getAccessPermissions());
            uiForm.findFirstComponentOfType(UIPermissionSelector.class)
               .setValue(Util.getUIPortal().getEditPermission());
         }
         else
         {
            list.remove(2);
            uiForm.loadMakableGroupNavigations();
            list.add(2, uiForm.groupIdSelectBox);
            String groupIdSelected = uiForm.groupIdSelectBox.getValue();
            groupIdSelected = groupIdSelected.startsWith("/") ? groupIdSelected : "/" + groupIdSelected;
            String permission = "*:" + groupIdSelected;
            uiForm.findFirstComponentOfType(UIListPermissionSelector.class).setValue(new String[]{permission});
            UserACL userACL = uiForm.getApplicationComponent(UserACL.class);
            permission = userACL.getMakableMT() + ":" + groupIdSelected;
            uiForm.findFirstComponentOfType(UIPermissionSelector.class).setValue(permission);
         }
         prContext.addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }

   static public class ChangeOwnerIdActionListener extends EventListener<UIPageForm>
   {
      public void execute(Event<UIPageForm> event) throws Exception
      {
         UIPageForm uiForm = event.getSource();
         String groupIdSelected = uiForm.groupIdSelectBox.getValue();
         groupIdSelected = groupIdSelected.startsWith("/") ? groupIdSelected : "/" + groupIdSelected;
         String permission = "*:" + groupIdSelected;
         uiForm.findFirstComponentOfType(UIListPermissionSelector.class).setValue(new String[]{permission});
         UserACL userACL = uiForm.getApplicationComponent(UserACL.class);         
         permission = userACL.getMakableMT() + ":" + groupIdSelected;
         uiForm.findFirstComponentOfType(UIPermissionSelector.class).setValue(permission);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }

   static public class SelectMembershipActionListener extends EventListener<UIGroupMembershipSelector>
   {
      public void execute(Event<UIGroupMembershipSelector> event) throws Exception
      {
         UIPageForm uiForm = event.getSource().getAncestorOfType(UIPageForm.class);
         /*if (!uiForm.getUIStringInput(OWNER_TYPE).getValue().equals(PortalConfig.GROUP_TYPE))
            return;
         String editPer = uiForm.findFirstComponentOfType(UIPermissionSelector.class).getValue();
         if (editPer == null || editPer.length() < 1)
            return;
         String group = editPer.substring(editPer.indexOf("/") + 1);
         uiForm.ownerIdInput.setValue(group);*/
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }
}

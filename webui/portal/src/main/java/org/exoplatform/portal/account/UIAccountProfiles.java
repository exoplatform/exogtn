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

package org.exoplatform.portal.account;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NaturalLanguageValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.UsernameValidator;
import org.picketlink.idm.common.exception.IdentityException;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl",

events = {@EventConfig(listeners = UIAccountProfiles.SaveActionListener.class, csrfCheck = true),
   @EventConfig(listeners = UIAccountProfiles.ResetActionListener.class, phase = Phase.DECODE)})
public class UIAccountProfiles extends UIForm
{  
   public UIAccountProfiles() throws Exception
   {
      super();
      String username = Util.getPortalRequestContext().getRemoteUser();
      OrganizationService service = this.getApplicationComponent(OrganizationService.class);
      User useraccount = service.getUserHandler().findUserByName(username);

      UIFormStringInput userName = new UIFormStringInput("userName", "userName", username);
      userName.setReadOnly(true);
      addUIFormInput(userName.addValidator(MandatoryValidator.class).addValidator(UsernameValidator.class, 3, 30));
      
      addUIFormInput(new UIFormStringInput("firstName", "firstName", useraccount.getFirstName()).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class).addValidator(NaturalLanguageValidator.class));
      
      addUIFormInput(new UIFormStringInput("lastName", "lastName", useraccount.getLastName()).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class).addValidator(NaturalLanguageValidator.class));
      
      addUIFormInput(new UIFormStringInput("email", "email", useraccount.getEmail()).addValidator(
         MandatoryValidator.class).addValidator(EmailAddressValidator.class));
   }

   static public class ResetActionListener extends EventListener<UIAccountProfiles>
   {
      public void execute(Event<UIAccountProfiles> event) throws Exception
      {
         UIAccountProfiles uiForm = event.getSource();
         String userName = uiForm.getUIStringInput("userName").getValue();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         User user = service.getUserHandler().findUserByName(userName);
         uiForm.getUIStringInput("firstName").setValue(user.getFirstName());
         uiForm.getUIStringInput("lastName").setValue(user.getLastName());
         uiForm.getUIStringInput("email").setValue(user.getEmail());
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
   }

   static public class SaveActionListener extends EventListener<UIAccountProfiles>
   {
      public void execute(Event<UIAccountProfiles> event) throws Exception
      {
         UIAccountProfiles uiForm = event.getSource();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();

         ConversationState state = ConversationState.getCurrent();
         String userName = ((User)state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName();                  
         User user = service.getUserHandler().findUserByName(userName);
         if (user != null)
         {
            String oldEmail = user.getEmail();
            String newEmail = uiForm.getUIStringInput("email").getValue();
            
            // Check if mail address is already used
            Query query = new Query();
            query.setEmail(newEmail);
            if (service.getUserHandler().findUsers(query).getAll().size() > 0 && !oldEmail.equals(newEmail))
            {
               //Be sure it keep old value
               user.setEmail(oldEmail);
               Object[] args = {userName};
               uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args));
               return;
            }
            user.setFirstName(uiForm.getUIStringInput("firstName").getValue());
            user.setLastName(uiForm.getUIStringInput("lastName").getValue());
            user.setEmail(newEmail);
            uiApp.addMessage(new ApplicationMessage("UIAccountProfiles.msg.update.success", null));
            try 
            {
              service.getUserHandler().saveUser(user, true);
            } 
            catch (IdentityException e) 
            {
              uiApp.addMessage(new ApplicationMessage("UIAccountProfiles.msg.update.fail", null, ApplicationMessage.ERROR));
              return;
            }
            
            state.setAttribute(CacheUserProfileFilter.USER_PROFILE, user);
            UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
            uiWorkingWS.updatePortletsByName("UserInfoPortlet");
            uiWorkingWS.updatePortletsByName("OrganizationPortlet");
         }
         else
         {        
            JavascriptManager jsManager = context.getJavascriptManager();
            jsManager.importJavascript("eXo");
            
            StringBuilder js = new StringBuilder("if(confirm('");
            js.append(context.getApplicationResourceBundle().getString("UIAccountProfiles.msg.NotExistingAccount"));
            js.append("')) {eXo.portal.logout();}");
            jsManager.addJavascript(js.toString());            
         }
      }
   }
}

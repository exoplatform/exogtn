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

package org.exoplatform.webui.core;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trong.tran@exoplatform.com
 * @version: $Id$
 * Aug 14, 2006  
 * 
 * A component to display messages of different types, in a popup window
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIPopupMessages.gtmpl", events = @EventConfig(listeners = UIPopupMessages.CloseActionListener.class))
@Serialized
public class UIPopupMessages extends UIPopupWindow
{
   /**
    * The error messages
    */
   private List<AbstractApplicationMessage> errors_;

   /**
    * The warning messages
    */
   private List<AbstractApplicationMessage> warnings_;

   /**
    * The info messages
    */
   private List<AbstractApplicationMessage> infos_;

   public UIPopupMessages()
   {
      errors_ = new ArrayList<AbstractApplicationMessage>();
      warnings_ = new ArrayList<AbstractApplicationMessage>();
      infos_ = new ArrayList<AbstractApplicationMessage>();
      setShowMask(true);
      setShow(true);
   }

   public List<AbstractApplicationMessage> getErrors()
   {
      return errors_;
   }

   public void setErrors(List<AbstractApplicationMessage> errors_)
   {
      this.errors_ = errors_;
   }

   public List<AbstractApplicationMessage> getInfos()
   {
      return infos_;
   }

   public void setInfos(List<AbstractApplicationMessage> infos_)
   {
      this.infos_ = infos_;
   }

   public List<AbstractApplicationMessage> getWarnings()
   {
      return warnings_;
   }

   public void setWarnings(List<AbstractApplicationMessage> warnings_)
   {
      this.warnings_ = warnings_;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      super.processRender(context);
   }

   public void addMessage(AbstractApplicationMessage msg)
   {
      msg.setResourceBundle(getResourceBundle());
      switch (msg.getType())
      {
         case ApplicationMessage.ERROR :
            errors_.add(msg);
            break;
         case ApplicationMessage.WARNING :
            warnings_.add(msg);
            break;
         default :
            infos_.add(msg);
      }
   }

   public void addMessage(ApplicationMessage msg)
   {
      addMessage((AbstractApplicationMessage)msg);
   }

   private ResourceBundle getResourceBundle()
   {
      RequestContext context = RequestContext.getCurrentInstance();
      if (context == null)
      {
         return null;
      }
      return context.getApplicationResourceBundle();
   }

   public boolean hasMessage()
   {
      return (errors_.size() > 0 || warnings_.size() > 0 || infos_.size() > 0);
   }

   public void clearMessages()
   {
      errors_.clear();
      warnings_.clear();
      infos_.clear();
   }

   static public class CloseActionListener extends EventListener<UIPopupMessages>
   {
      public void execute(Event<UIPopupMessages> event) throws Exception
      {
         UIPopupMessages uiPopupMessage = event.getSource();
         uiPopupMessage.clearMessages();
         WebuiRequestContext context = event.getRequestContext();
         if (uiPopupMessage.getParent() == null)
         {
            context.addUIComponentToUpdateByAjax(uiPopupMessage);
            return;
         }
         if (!uiPopupMessage.isShow())
            return;
         UIComponent uiParent = uiPopupMessage.getParent();
         context.addUIComponentToUpdateByAjax(uiParent);
         Event<UIComponent> pEvent =
            uiParent.createEvent("ClosePopup", event.getExecutionPhase(), event.getRequestContext());
         if (pEvent != null)
            pEvent.broadcast();
      }
   }
}

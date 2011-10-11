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

package org.exoplatform.webui.form.validator;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 * 
 * Validates whether an email is in the correct format
 * Valid characters that can be used in a domain name are:
 *     a-z
 *     0-9
 *     - (dash) or . (dot) but not as a starting or ending character
 *     . (dot) as a separator for the textual portions of a domain name
 *     
 * Valid characters that can be used in a domain name are:
 *     a-z
 *     0-9
 *     _ (underscore) or .  (dot) but not as a starting or ending character
 */
@Serialized
public class EmailAddressValidator implements Validator
{

   public void validate(UIFormInput uiInput) throws Exception
   {
      //  modified by Pham Dinh Tan
      UIComponent uiComponent = (UIComponent)uiInput;
      UIForm uiForm = uiComponent.getAncestorOfType(UIForm.class);
      String label;
      try
      {
    	  label = uiForm.getId() + ".label." + uiInput.getName();
      }
      catch (Exception e)
      {
         label = uiInput.getName();
      }
      Object[] args = {label};
      
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).trim().length() == 0)
         return;
      
      String s = (String)uiInput.getValue();
      int atIndex = s.indexOf('@'); 
      if(atIndex == -1)
         throw new MessageException(new ApplicationMessage("EmailAddressValidator.msg.Invalid-input", args,
            ApplicationMessage.WARNING));
      
      String emailName = s.substring(0, atIndex);
      String emailAddress = s.substring(atIndex + 1);
      
      if(emailAddress.lastIndexOf('.') == -1)
         throw new MessageException(new ApplicationMessage("EmailAddressValidator.msg.Invalid-input", args,
            ApplicationMessage.WARNING));
      
      if (checkEmail(emailName.toCharArray(), emailAddress.toCharArray()))
         return;
      throw new MessageException(new ApplicationMessage("EmailAddressValidator.msg.Invalid-input", args,
         ApplicationMessage.WARNING));
   }
   
   private boolean checkEmail(char[] emailName, char[] emailAddress)
   {
      if(!isAlphabet(emailName[0]) || !isAlphabet(emailAddress[0])) return false;
      else if(!isAlphabet(emailName[emailName.length - 1]) && !isDigit(emailName[emailName.length - 1])) return false;
      else if(!isAlphabet(emailAddress[emailAddress.length - 1]) && !isDigit(emailAddress[emailAddress.length - 1])) return false;
      else 
      {
         for(int i = 0; i < emailName.length; i++)
         {
            if(isDigit(emailName[i]) || isAlphabet(emailName[i])) continue;
            else if(isEmailNameSymbol(emailName[i]) && !isEmailNameSymbol(emailName[i + 1])) continue;
            return false;
         }
         
         for(int i = 0; i < emailAddress.length; i++)
         {
            if(isDigit(emailAddress[i]) || isAlphabet(emailAddress[i])) continue;
            else if(isEmailAddressSymbol(emailAddress[i]) && !isEmailAddressSymbol(emailAddress[i + 1])) continue;
            return false;
         }
         
         int lastDot = -1;
         for(int i = emailAddress.length - 1; i >=0; i--)
         {
            if(emailAddress[i] == '.') lastDot = i;
         }
         
         if(lastDot == -1) return false;
         else
         {
            for(int i= emailAddress.length - 1; i >= lastDot; i--)
            {
               if(emailAddress[i] == '-') return false;
            }
         }
      }
      return true;
   }
   
   private boolean isAlphabet(char c)
   {
      if(c >= 'a' && c <='z') return true;
      return false;
   }
   
   private boolean isDigit(char c)
   {
      if(c >= '0' && c <= '9') return true;
      return false;
   }
   
   private boolean isEmailNameSymbol(char c)
   {
      if(c == '_' || c == '.') return true;
      return false;
   }
   
   private boolean isEmailAddressSymbol(char c)
   {
      if(c == '-' || c == '.') return true;
      return false;
   }
}
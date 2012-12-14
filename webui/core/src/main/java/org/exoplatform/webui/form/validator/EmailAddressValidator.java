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
import org.exoplatform.web.application.CompoundApplicationMessage;
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
 *     and not appear two or more times consecutively
 *     
 *     . (dot) as a separator for the textual portions of a domain name
 *     
 * Valid characters that can be used in a local part are:
 *     a-z
 *     0-9
 *     - (dash) or _ (underscore) or .  (dot) but not as a starting or ending character
 *     and not appear two or more times consecutively
 */
@Serialized
public class EmailAddressValidator extends MultipleConditionsValidator
{

   @Override
   protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput)
   {

      Object[] args = {label};
      
	int atIndex = value.indexOf('@');
      if (atIndex == -1)
      {
		messages.addMessage("EmailAddressValidator.msg.Invalid-input", args);
      }
      
	else
      {
         String localPart = value.substring(0, atIndex);
         String domainName = value.substring(atIndex + 1);

         if (!validateLocalPart(localPart.toCharArray()) || !validateDomainName(domainName.toCharArray()))
         {
            messages.addMessage("EmailAddressValidator.msg.Invalid-input", args);
         }
      }
	  
   }

   private boolean validateLocalPart(char[] localPart)
   {
      if(!Character.isLetter(localPart[0]) || !Character.isLetterOrDigit(localPart[localPart.length - 1]))
      {
         return false;
      }

      for(int i = 1; i < localPart.length -1; i++)
      {
         char c = localPart[i];
         char next = localPart[i+1];

         if(Character.isLetterOrDigit(c) || (isLocalPartSymbol(c) && Character.isLetterOrDigit(next)))
         {
            continue;
         }
         else
         {
            return false;
         }
      }
      return true;
   }

   private boolean validateDomainName(char[] domainName)
   {
      if(!Character.isLetter(domainName[0]) || !Character.isLetterOrDigit(domainName[domainName.length - 1]))
      {
         return false;
      }

      //Check if there is no non-alphabet following the last dot
      boolean foundValidLastDot = false;
      for(int i = 1; i < domainName.length -1; i++)
      {
         char c = domainName[i];
         char next = domainName[i+1];

         if(c == '.')
         {
            foundValidLastDot = true;
         }
         else if(!Character.isLetter(c))
         {
            foundValidLastDot = false;
         }

         if(Character.isLetterOrDigit(c) || (isDomainNameSymbol(c) && Character.isLetterOrDigit(next)))
         {
            continue;
         }
         else
         {
            return false;
         }
      }
      return foundValidLastDot;
   }
 
   private boolean isLocalPartSymbol(char c)
   {
      return c == '-' || c == '_' || c == '.';
   }
   
   private boolean isDomainNameSymbol(char c)
   {
      return c == '-' || c == '.';
   }
}

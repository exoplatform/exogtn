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

import org.exoplatform.webui.form.UIFormInput;

import java.io.Serializable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 * 
 * Validates whether this value is null
 */
public class NullFieldValidator extends AbstractValidator implements Serializable
{

   public void validate(UIFormInput uiInput) throws Exception
   {
      if ((uiInput.getValue() == null))
      {
         throw createMessageException(null, uiInput);
      }
   }

   @Override
   protected String getMessageLocalizationKey()
   {
      return "EmptyFieldValidator.msg.empty-input";
   }

   @Override
   protected boolean isValid(String value, UIFormInput uiInput)
   {
      throw new UnsupportedOperationException("Unneeded by this implementation");
   }
}

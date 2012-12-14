/**
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.test.validator;

import junit.framework.TestCase;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.form.validator.ResourceValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;
import org.exoplatform.webui.form.validator.URLValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;
import org.exoplatform.webui.form.validator.UsernameValidator;
import org.exoplatform.webui.form.validator.Validator;

import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 * 
 * @datSep 26, 2011
 */
public class TestWebuiValidator extends TestCase
{
   public void testUrlValidator()
   {
      Validator validator = new URLValidator();
      // Test ip address and invalidate subnet masks ip
      assertTrue(expected(validator, "https://192.168.1.1"));
      assertTrue(expected(validator, "ftp://255.255.255.1"));
      assertTrue(expected(validator, "ftps://255.255.0.1"));
      assertTrue(expected(validator, "ftp://0.0.0.0"));
      assertTrue(expected(validator, "http://127.0.0.1"));
      assertTrue(expected(validator, "https://192.168.4.90"));
      assertTrue(expected(validator, "https://192.168.4.90:8080"));
      assertTrue(expected(validator, "http://127.0.0.1:8080"));
      assertFalse(expected(validator, "http://127.0.0.01"));
      assertFalse(expected(validator, "ftp://255.255.255.255"));

      // Test domain name and uri
      assertTrue(expected(validator, "https://www.exoplatform.com"));
      assertTrue(expected(validator, "ftps://root:gtn@exoplatform.com"));
      assertTrue(expected(validator, "ftps://root@exoplatform.com"));
      assertTrue(expected(validator, "https://www.dev.exoplatform.com"));
      assertTrue(expected(validator, "https://www.dev.exoplatform.com:8888"));
      assertFalse(expected(validator, "https://www.dev.exoplatform.com:8888?arg=value"));
      assertTrue(expected(validator, "https://www.dev.exoplatform.com:8888/path?arg=value"));
      assertTrue(expected(validator, "https://www.dev.exoplatform.com:8888/path?arg=value#"));
   }

   public void testDateTimeValidator()
   {
      Validator validator = new DateTimeValidator();
      WebuiRequestContext.setCurrentInstance(new MockRequestContext(new Locale("fr")));
      UIFormDateTimeInput uiInput = new UIFormDateTimeInput("currentDate", "currentDate", null);
      uiInput.setValue("28/09/2011 10:59:59");
      assertTrue(expected(validator, uiInput));
      uiInput.setValue("09/28/2011 10:59:59");
      assertFalse(expected(validator, uiInput));

      WebuiRequestContext.setCurrentInstance(new MockRequestContext(new Locale("en")));
      uiInput = new UIFormDateTimeInput("currentDate", "currentDate", null);
      uiInput.setValue("09/28/2011 10:59:59");
      assertTrue(expected(validator, uiInput));
      uiInput.setValue("09-28-2011 10:59:59");
      assertFalse(expected(validator, uiInput));
      uiInput.setValue("28/09/2011 10:59:59");
      assertFalse(expected(validator, uiInput));
   }

   public void testUsernameValidator()
   {
      Validator validator = new UsernameValidator(3, 30);
      validateUsernames(validator);

      validator = new UserConfigurableValidator(UserConfigurableValidator.USERNAME);
      validateUsernames(validator);
   }

   private void validateUsernames(Validator validator)
   {
      assertTrue(expected(validator, "root.gtn"));
      assertTrue(expected(validator, "root_gtn"));
      assertTrue(expected(validator, "root_gtn.01"));
      assertFalse(expected(validator, "root_gtn_"));
      assertFalse(expected(validator, "_root_gtn"));
      assertFalse(expected(validator, "root__gtn"));
      assertFalse(expected(validator, "root._gtn"));
      assertFalse(expected(validator, "root--gtn"));
      assertFalse(expected(validator, "root*gtn"));
      assertFalse(expected(validator, "Root"));
   }

   public void testEmailValidator()
   {
      Validator validator = new EmailAddressValidator();
      assertFalse(expected(validator, "root"));
      assertTrue(expected(validator, "root.gtn@exoplatform.com"));
      assertTrue(expected(validator, "root.exo.gtn.portal@explatform.biz.edu.vn"));
      assertTrue(expected(validator, "root_exo_gtn_portal@explatform-edu.biz.vn"));
      assertTrue(expected(validator, "exo-sys@exoplatform.com"));
      assertFalse(expected(validator, "exo--sys@exoplatform.com"));
      assertFalse(expected(validator, "root_exo_gtn_portal@explatform-edu.biz-vn"));
      assertFalse(expected(validator, "root_exo_gtn_portal@explatform-edu.biz9vn"));
      assertFalse(expected(validator, "root_exo_gtn_portal@explatform--edu.biz.vn"));
      assertFalse(expected(validator, "root_exo_gtn_portal@-explatform.biz"));
      assertFalse(expected(validator, "root_exo_gtn_portal@explatform_biz_edu.vn"));
      assertFalse(expected(validator, "root_exo_gtn_portal@explatform_biz_edu_vn"));
      assertFalse(expected(validator, "root_gtn--@portal.org"));
      assertFalse(expected(validator, "root__gtn@portal.org"));
      assertFalse(expected(validator, "root_.gtn@portal.org"));
      assertFalse(expected(validator, "--root.gtn@portal.org"));
      assertFalse(expected(validator, "root.gtn@.portal.org"));
   }

   public void testNumberValidator()
   {
      Validator validator = new NumberFormatValidator();
      assertTrue(expected(validator, "1001"));
      assertTrue(expected(validator, "0"));
      assertFalse(expected(validator, "01"));
      assertFalse(expected(validator, "-01"));
      assertFalse(expected(validator, "-0"));
      assertFalse(expected(validator, "000"));
      assertFalse(expected(validator, "-01"));
      assertFalse(expected(validator, "1,5"));
      assertFalse(expected(validator, "1.5"));
   }

   public void testPositiveNumberValidator()
   {
      Validator validator = new PositiveNumberFormatValidator();
      assertTrue(expected(validator, "1"));
      assertTrue(expected(validator, "0"));
      assertFalse(expected(validator, "-1"));
      assertFalse(expected(validator, "01"));
      assertFalse(expected(validator, "-01"));
   }
   
   /* \u00e9 is 'é' character  */
   public void testSpecialCharacterValidator()
   {
      Validator validator= new SpecialCharacterValidator();
      assertTrue(expected(validator,"aAzZ  caff\u00e9"));
      assertFalse(expected(validator,"aAzZ\tcaff\u00e9"));
      assertFalse(expected(validator,"aAzZ\ncaff\u00e9"));
      assertFalse(expected(validator,"aAzZ \rcaff\u00e9"));
      assertFalse(expected(validator,"\tcaff\u00e9"));
      assertFalse(expected(validator,"\ncaff\u00e9"));
      assertFalse(expected(validator,"\rcaff\u00e9"));
      assertTrue(expected(validator,"\n"));
      assertTrue(expected(validator, "\t"));
      assertTrue(expected(validator, "\n"));
   }
   
   public void testResourceValidator()
   {
      Validator validator = new ResourceValidator();
      assertTrue(expected(validator, "caff\u00e9_-.--"));
      assertFalse(expected(validator, "_caff\u00e9"));
      assertFalse(expected(validator, "0caff\u00e9"));
   }
   
   public void testNameValidator()
   {
      Validator validator = new NameValidator();
      assertTrue(expected(validator, "caff\u00e9_-.*"));
      assertTrue(expected(validator, "*caff\u00e9"));
      assertTrue(expected(validator, "0caff\u00e9"));
   }
   
   public void testIdentifierValidator()
   {
      Validator validator = new IdentifierValidator();
      assertTrue(expected(validator, "caff\u00e9-_"));
      assertTrue(expected(validator, "caff\u00e901"));
      assertFalse(expected(validator, "-caff\u00e9"));
      assertFalse(expected(validator, "01caff\u00e9"));
   }

   public boolean expected(Validator validator, final String input)
   {
      UIFormInput uiInput = new MockUIFormImput()
      {
         public Object getValue() throws Exception
         {
            return input;
         }
      };
      return expected(validator, uiInput);
   }

   public boolean expected(Validator validator, UIFormInput uiInput)
   {
      try
      {
         validator.validate(uiInput);
         return true;
      }
      catch (MessageException e)
      {
         return false;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private static class MockUIFormImput extends UIComponent implements UIFormInput
   {
      public String getBindingField()
      {
         return null;
      }

      public String getLabel()
      {
         return null;
      }

      public UIFormInput addValidator(Class clazz, Object... params) throws Exception
      {
         return null;
      }

      public List getValidators()
      {
         return null;
      }

      public Object getValue() throws Exception
      {
         return null;
      }

      public UIFormInput setValue(Object value) throws Exception
      {
         return null;
      }

      public Class getTypeValue()
      {
         return null;
      }

      public void reset()
      {
      }
   }
}

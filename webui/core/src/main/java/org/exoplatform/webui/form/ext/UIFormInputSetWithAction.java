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

package org.exoplatform.webui.form.ext;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.validator.Validator;

import java.util.HashMap;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh
 * minh.dang@exoplatform.com Sep 20, 2006
 */
@ComponentConfig(template = "system:/groovy/webui/form/ext/UIFormInputSetWithAction.gtmpl")
public class UIFormInputSetWithAction extends UIFormInputSet implements UIFormInput
{

   private String[] actions;

   private String[] values;

   private boolean isView;

   private boolean isShowOnly = false;

   private boolean isDeleteOnly = false;

   private HashMap<String, String> info = new HashMap<String, String>();

   private HashMap<String, List<String>> listInfo = new HashMap<String, List<String>>();

   private HashMap<String, String[]> actionInfo = new HashMap<String, String[]>();

   private HashMap<String, String[]> fieldActions = new HashMap<String, String[]>();

   private boolean isShowActionInfo = false;

   private HashMap<String, String> msgKeys = new HashMap<String, String>();

   public UIFormInputSetWithAction(String name)
   {
      setId(name);
      setComponentConfig(getClass(), null);
   }

   public boolean isShowActionInfo()
   {
      return isShowActionInfo;
   }

   public void showActionInfo(boolean isShow)
   {
      isShowActionInfo = isShow;
   }

   /**
    * Sets the actions.
    * 
    * @param actionList the action list
    * @param values the values
    */
   public void setActions(String[] actionList, String[] values)
   {
      actions = actionList;
      this.values = values;
   }

   public String[] getInputSetActions()
   {
      return actions;
   }

   public String[] getActionValues()
   {
      return values;
   }

   public String getFormName()
   {
      UIForm uiForm = getAncestorOfType(UIForm.class);
      return uiForm.getId();
   }

   public boolean isShowOnly()
   {
      return isShowOnly;
   }

   public void setIsShowOnly(boolean isShowOnly)
   {
      this.isShowOnly = isShowOnly;
   }

   public boolean isDeleteOnly()
   {
      return isDeleteOnly;
   }

   public void setIsDeleteOnly(boolean isDeleteOnly)
   {
      this.isDeleteOnly = isDeleteOnly;
   }

   public void setListInfoField(String fieldName, List<String> listInfor)
   {
      listInfo.put(fieldName, listInfor);
   }

   public List<String> getListInfoField(String fieldName)
   {
      if (listInfo.containsKey(fieldName))
         return listInfo.get(fieldName);
      return null;
   }

   public void setInfoField(String fieldName, String fieldInfo)
   {
      info.put(fieldName, fieldInfo);
   }

   public String getInfoField(String fieldName)
   {
      if (info.containsKey(fieldName))
         return info.get(fieldName);
      return null;
   }

   public void setActionInfo(String fieldName, String[] actionNames)
   {
      actionInfo.put(fieldName, actionNames);
   }

   public String[] getActionInfo(String fieldName)
   {
      if (actionInfo.containsKey(fieldName))
         return actionInfo.get(fieldName);
      return null;
   }

   public void setFieldActions(String fieldName, String[] actionNames)
   {
      fieldActions.put(fieldName, actionNames);
   }

   public String[] getFieldActions(String fieldName)
   {
      return fieldActions.get(fieldName);
   }

   public void setIsView(boolean isView)
   {
      this.isView = isView;
   }

   public boolean isView()
   {
      return isView;
   }
   
   public void setIntroduction(String fieldName, String msgKey)
   {
      msgKeys.put(fieldName, msgKey);
   }

   public String getMsgKey(String fieldName)
   {
      return msgKeys.get(fieldName);
   }

   @Override
   public String getBindingField()
   {
      return null;
   }

   @Override
   public List<Validator> getValidators()
   {
      return null;
   }

   @Override
   public UIFormInput addValidator(Class clazz, Object... params) throws Exception
   {
      return this;
   }

   @Override
   public Object getValue() throws Exception
   {
      return null;
   }

   @Override
   public UIFormInput setValue(Object value) throws Exception
   {
      return null;
   }

   @Override
   public Class getTypeValue()
   {
      return null;
   }

   @Override
   public String getLabel()
   {
      return getId();
   }
}

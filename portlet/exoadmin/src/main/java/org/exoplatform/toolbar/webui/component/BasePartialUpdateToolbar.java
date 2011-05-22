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

package org.exoplatform.toolbar.webui.component;

import java.util.Collection;
import java.util.Collections;

import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.NavigationPath;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIPortletApplication;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 *
 */
public abstract class BasePartialUpdateToolbar extends UIPortletApplication
{

   protected NodeFilter toolbarFilter;
   protected Scope toolbarScope;
   protected static final int DEFAULT_LEVEL = 2;

   public BasePartialUpdateToolbar() throws Exception
   {     
      int level = DEFAULT_LEVEL; 
      try 
      {
         PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
         PortletRequest prequest = context.getRequest();
         PortletPreferences prefers = prequest.getPreferences();
         
         level = Integer.valueOf(prefers.getValue("level", String.valueOf(DEFAULT_LEVEL)));       
      }
      catch (Exception ex) 
      {
         log.warn("Preference for navigation level can only be integer");
      }

      if (level <= 0)
      {
         toolbarScope = Scope.ALL;           
      }
      else
      {
         toolbarScope = new GenericScope(level);
      }
   }
   
   protected Collection<UserNode> getNavigationNodes(UserNavigation nav) throws Exception
   {
      UserPortal userPortal = getUserPortal();
      if (nav != null)
      {
         try 
         {
            UserNode rootNodes =  userPortal.getNode(nav, toolbarScope, null);
            rootNodes.filter(toolbarFilter);
            return rootNodes.getChildren();
         } 
         catch (NavigationServiceException ex)
         {
            log.warn(nav.getKey().getName() + " has been deleted");
         }
      }
      return Collections.emptyList();
   }
   
   protected UserNavigation getNavigation(SiteKey key) throws Exception
   {
      UserPortal userPortal = getUserPortal();
      return userPortal.getNavigation(key);
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      super.serveResource(context);
      
      ResourceRequest req = context.getRequest();
      String id = req.getResourceID();
      
      JSONArray jsChilds = getChildrenAsJSON(getPathFromResourceID(id));
      if (jsChilds == null)
      {
         return;
      }
      
      MimeResponse res = context.getResponse(); 
      res.setContentType("text/json"); 
      res.getWriter().write(jsChilds.toString());
   }
   
   private JSONArray getChildrenAsJSON(NavigationPath navPath) throws Exception
   {           
      if (navPath == null)
      {
         return null;
      }

      UserNode userNode = navPath.getTarget();
      getUserPortal().updateNode(userNode, toolbarScope, null);
      userNode.filter(toolbarFilter);
      Collection<UserNode> childs = userNode.getChildren();         
      
      JSONArray jsChilds = new JSONArray();       
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();       
      MimeResponse res = context.getResponse();
      for (UserNode child : childs)
      {
         jsChilds.put(toJSON(child, navPath.getNavigation().getKey().getName(), res));
      }
      return jsChilds;
   }

   protected JSONObject toJSON(UserNode node, String navId, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      String nodeId = node.getId();
      
      json.put("label", node.getEncodedResolvedLabel());      
      json.put("hasChild", node.getChildrenCount() > 0);            
      json.put("isSelected", nodeId.equals(getSelectedNode().getId()));
      json.put("icon", node.getIcon());       
      
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(res.encodeURL(getResourceIdFromNode(node, navId)));
      json.put("getNodeURL", rsURL.toString());                  
      json.put("actionLink", Util.getPortalRequestContext().getPortalURI() + node.getURI());
      
      JSONArray childs = new JSONArray();
      for (UserNode child : node.getChildren())
      {
         childs.put(toJSON(child, navId, res));
      }      
      json.put("childs", childs);
      return json;
   }
   

   protected UserPortal getUserPortal()
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      return uiApp.getUserPortalConfig().getUserPortal();
   }
   
   protected UserNode getSelectedNode() throws Exception
   {
      return Util.getUIPortal().getNavPath().getTarget();
   }
   
   protected abstract String getResourceIdFromNode(UserNode node, String navId) throws Exception;
   
   protected abstract NavigationPath getPathFromResourceID(String resourceId) throws Exception;
}
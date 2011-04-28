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
import java.util.List;

import javax.portlet.MimeResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.NavigationPath;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarSitePortlet.gtmpl"

)
public class UIUserToolBarSitePortlet extends UIPortletApplication
{

   private final NodeFilter TOOLBAR_SITE_FILTER;
   private static final Scope TOOLBAR_SITE_SCOPE = Scope.CHILDREN;

   public UIUserToolBarSitePortlet() throws Exception
   {
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();

      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      scopeBuilder.withTemporalCheck();
      TOOLBAR_SITE_FILTER = userPortal.createFilter(scopeBuilder.build());
   }

   public List<String> getAllPortalNames() throws Exception
   {
      UserPortalConfigService dataStorage = getApplicationComponent(UserPortalConfigService.class);
      return dataStorage.getAllPortalNames();
   }
   
   public String getPortalLabel(String portalName) throws Exception
   {
      DataStorage storage_ = getApplicationComponent(DataStorage.class);
      PortalConfig portalConfig = storage_.getPortalConfig(portalName);
      String label = portalConfig.getLabel();
      if (label != null && label.trim().length() > 0)
      {
         return label;
      }
      
      return portalName;
   }

   public String getCurrentPortal()
   {
      return Util.getPortalRequestContext().getPortalOwner();
   }

   public String getPortalURI(String portalName)
   {
      String currentPortalURI = Util.getPortalRequestContext().getPortalURI();
      return currentPortalURI.substring(0, currentPortalURI.lastIndexOf(getCurrentPortal())) + portalName + "/";
   }

   public UserNavigation getCurrentUserNavigation(UserPortal userPortal) throws Exception
   {      
      return userPortal.getNavigation(SiteKey.portal(getCurrentPortal()));      
   }
   
   public Collection<UserNode> getNavigationNodes() throws Exception
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UserPortal userPortal = uiApp.getUserPortalConfig().getUserPortal();
      UserNavigation nav = getCurrentUserNavigation(userPortal);
      if (nav != null)
      {
         UserNode rootNodes =  userPortal.getNode(nav, TOOLBAR_SITE_SCOPE);
         if (rootNodes != null)
         {
            rootNodes.filter(TOOLBAR_SITE_FILTER);
            return rootNodes.getChildren();
         }
      }
      return Collections.emptyList();
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      super.serveResource(context);
      
      ResourceRequest req = context.getRequest();
      String nodeURI = req.getResourceID();
      
      JSONArray jsChilds = getChildrenAsJSON(nodeURI);
      if (jsChilds == null)
      {
         return;
      }
      
      MimeResponse res = context.getResponse(); 
      res.setContentType("text/json"); 
      res.getWriter().write(jsChilds.toString());
   }

   public UserNode getSelectedNode() throws Exception
   {
      return Util.getUIPortal().getNavPath().getTarget();
   }
   
   private JSONArray getChildrenAsJSON(String nodeURI) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();       
      Collection<UserNode> childs = null;
            
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNavigation currNav = getCurrentUserNavigation(userPortal);
      if (currNav == null) return null;
      
      NavigationPath navPath = userPortal.resolvePath(currNav, nodeURI);
      
      if (navPath != null)
      {         
         UserNode userNode = navPath.getTarget();
         userNode = userPortal.getNode(userNode, TOOLBAR_SITE_SCOPE);
         userNode.filter(TOOLBAR_SITE_FILTER);
         childs = userNode.getChildren();         
      }
      
      JSONArray jsChilds = new JSONArray();
      if (childs == null)
      {
         return null;
      }                  
      MimeResponse res = context.getResponse();
      for (UserNode child : childs)
      {
         jsChilds.put(toJSON(child, res));
      }
      return jsChilds;
   }

   private JSONObject toJSON(UserNode node, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      String nodeId = node.getId();
      
      json.put("label", node.getEncodedResolvedLabel());      
      json.put("hasChild", node.getChildrenCount() > 0);            
      json.put("isSelected", nodeId.equals(getSelectedNode().getId()));
      json.put("icon", node.getIcon());       
      
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(res.encodeURL(node.getURI()));
      json.put("getNodeURL", rsURL.toString());                  
      json.put("actionLink", Util.getPortalRequestContext().getPortalURI() + node.getURI());
      
      JSONArray childs = new JSONArray();
      for (UserNode child : node.getChildren())
      {
         childs.put(toJSON(child, res));
      }      
      json.put("childs", childs);
      return json;
   }
}
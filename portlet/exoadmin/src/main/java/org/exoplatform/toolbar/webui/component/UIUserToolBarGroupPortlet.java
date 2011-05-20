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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.GenericScope;
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
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.common.util.ParameterValidation;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarGroupPortlet.gtmpl",
   events = {
      @EventConfig(listeners = UIUserToolBarGroupPortlet.NavigationChangeActionListener.class)
   }
)
public class UIUserToolBarGroupPortlet extends UIPortletApplication
{

   private final NodeFilter TOOLBAR_GROUP_FILTER;
   private final Scope TOOLBAR_GROUP_SCOPE;
   private static final int DEFAULT_LEVEL = 2;
   private static final String SPLITTER_STRING = "::";

   public UIUserToolBarGroupPortlet() throws Exception
   {                  
      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      scopeBuilder.withTemporalCheck();
      TOOLBAR_GROUP_FILTER = getUserPortal().createFilter(scopeBuilder.build());
      
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
         TOOLBAR_GROUP_SCOPE = Scope.ALL;           
      }
      else
      {
         TOOLBAR_GROUP_SCOPE = new GenericScope(level);
      }
   }

   public List<UserNavigation> getGroupNavigations() throws Exception
   {
      UserPortal userPortal = getUserPortal();
      List<UserNavigation> allNavs = userPortal.getNavigations();

      List<UserNavigation> groupNav = new ArrayList<UserNavigation>();
      for (UserNavigation nav : allNavs)
      {
         if (nav.getKey().getType().equals(SiteType.GROUP))
         {
            groupNav.add(nav);
         }
      }
      return groupNav;
   }

   public Collection<UserNode> getNodes(UserNavigation groupNav) throws Exception
   {
      UserPortal userPortal = getUserPortal();
      UserNode rootNodes =  userPortal.getNode(groupNav, TOOLBAR_GROUP_SCOPE, null);
      if (rootNodes != null)
      {
         rootNodes.filter(TOOLBAR_GROUP_FILTER);
         return rootNodes.getChildren();
      }
      return Collections.emptyList();
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      super.serveResource(context);
      
      ResourceRequest req = context.getRequest();
      String resourceId = req.getResourceID();
      
      JSONArray jsChilds = getChildrenAsJSON(resourceId);
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
   
   private JSONArray getChildrenAsJSON(String resourceId) throws Exception
   {
      String[] parsedId = parseResourceId(resourceId); 
      if (parsedId == null)
      {
         throw new IllegalArgumentException("resourceId " + resourceId + " is invalid");
      }
      String groupId = parsedId[0];
      String nodeURI = parsedId[1];
                                   
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNavigation grpNav = getNavigation(groupId);
      if (grpNav == null) return null;
      
      NavigationPath navPath = userPortal.resolvePath(grpNav, nodeURI);
      
      Collection<UserNode> childs = null;
      if (navPath != null)
      {         
         UserNode userNode = navPath.getTarget();
         userPortal.updateNode(userNode, TOOLBAR_GROUP_SCOPE, null);
         userNode.filter(TOOLBAR_GROUP_FILTER);
         childs = userNode.getChildren();         
      }
      
      JSONArray jsChilds = new JSONArray();
      if (childs == null)
      {
         return null;
      }                  
      
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      MimeResponse res = context.getResponse();
      for (UserNode child : childs)
      {
         jsChilds.put(toJSON(child, groupId, res));
      }
      return jsChilds;
   }

   private JSONObject toJSON(UserNode node, String groupId, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      String nodeId = node.getId();
      
      json.put("label", node.getEncodedResolvedLabel());      
      json.put("hasChild", node.getChildrenCount() > 0);            
      json.put("isSelected", nodeId.equals(getSelectedNode().getId()));
      json.put("icon", node.getIcon());       
      
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(res.encodeURL(groupId + SPLITTER_STRING + node.getURI()));
      json.put("getNodeURL", rsURL.toString());                  
      json.put("actionLink", Util.getPortalRequestContext().getPortalURI() + node.getURI());
      
      JSONArray childs = new JSONArray();
      for (UserNode child : node.getChildren())
      {
         childs.put(toJSON(child, groupId, res));
      }      
      json.put("childs", childs);
      return json;
   }
   
   private UserNavigation getNavigation(String groupId) throws Exception
   {
      UserPortal userPortal = getUserPortal();
      return userPortal.getNavigation(SiteKey.group(groupId));
   }

   private String[] parseResourceId(String resourceId)
   {
      if (!ParameterValidation.isNullOrEmpty(resourceId)) 
      {
         String[] parsedId = resourceId.split(SPLITTER_STRING);
         if (parsedId.length == 2) 
         {
            return parsedId;
         }
      }
      return null;
   }

   private UserPortal getUserPortal()
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      return uiApp.getUserPortalConfig().getUserPortal();
   }

   public static class NavigationChangeActionListener extends EventListener<UIUserToolBarGroupPortlet>
   {
      @Override
      public void execute(Event<UIUserToolBarGroupPortlet> event) throws Exception
      {
         // This event is only a trick for updating the Toolbar group portlet
      }
   }   
}
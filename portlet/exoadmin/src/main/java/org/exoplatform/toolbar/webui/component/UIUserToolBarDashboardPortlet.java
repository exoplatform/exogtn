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

import javax.portlet.EventRequest;
import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl",
   events = {@EventConfig(name = "AddDefaultDashboard", listeners = UIUserToolBarDashboardPortlet.AddDashboardActionListener.class),
      @EventConfig(listeners = UIUserToolBarDashboardPortlet.NavigationChangeActionListener.class)})
public class UIUserToolBarDashboardPortlet extends UIPortletApplication
{

   public static String DEFAULT_TAB_NAME = "Tab_Default";
   private final NodeFilter TOOLBAR_DASHBOARD_FILTER;
   private final Scope TOOLBAR_DASHBOARD_SCOPE;
   private static final int DEFAULT_LEVEL = 2;

   public UIUserToolBarDashboardPortlet() throws Exception
   {
      UserNodePredicate.Builder builder = UserNodePredicate.builder();
      builder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      builder.withTemporalCheck();
      TOOLBAR_DASHBOARD_FILTER = getUserPortal().createFilter(builder.build());
      
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
         TOOLBAR_DASHBOARD_SCOPE = Scope.ALL;           
      }
      else
      {
         TOOLBAR_DASHBOARD_SCOPE = new GenericScope(level);
      }
   }

   public Collection<UserNode> getUserNodes() throws Exception
   {
      UserPortal userPortal = getUserPortal();
      UserNavigation userNav = getCurrentUserNavigation();
      if (userNav != null)
      {
         UserNode rootNodes = userPortal.getNode(userNav, TOOLBAR_DASHBOARD_SCOPE, null);
         if (rootNodes != null)
         {
            rootNodes.filter(TOOLBAR_DASHBOARD_FILTER);
            return rootNodes.getChildren();
         }
      }

      return Collections.emptyList();
   }

   public UserNode getSelectedNode() throws Exception
   {
      return Util.getUIPortal().getNavPath().getTarget();
   }

   public UserNavigation getCurrentUserNavigation() throws Exception
   {
      UserPortal userPortal = getUserPortal();
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      return userPortal.getNavigation(SiteKey.user(rcontext.getRemoteUser()));
   }

   private UserPortal getUserPortal()
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      return uiApp.getUserPortalConfig().getUserPortal();
   }

   static public class NavigationChangeActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {
      private Log log = ExoLogger.getExoLogger(NavigationChangeActionListener.class);

      @Override
      public void execute(Event<UIUserToolBarDashboardPortlet> event) throws Exception
      {
         log.debug("PageNode : " + ((EventRequest)event.getRequestContext().getRequest()).getEvent().getValue() + " is deleted");
      }
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
   
   private JSONArray getChildrenAsJSON(String nodeURI) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();       
      Collection<UserNode> childs = null;
            
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNavigation currNav = getCurrentUserNavigation();
      if (currNav == null) return null;
      
      NavigationPath navPath = userPortal.resolvePath(currNav, nodeURI);
      
      if (navPath != null)
      {         
         UserNode userNode = navPath.getTarget();
         userPortal.updateNode(userNode, TOOLBAR_DASHBOARD_SCOPE, null);
         userNode.filter(TOOLBAR_DASHBOARD_FILTER);
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
   
   static public class AddDashboardActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {

      private final static String PAGE_TEMPLATE = "dashboard";

      private static Log logger = ExoLogger.getExoLogger(AddDashboardActionListener.class);

      public void execute(Event<UIUserToolBarDashboardPortlet> event) throws Exception
      {
         UIUserToolBarDashboardPortlet toolBarPortlet = event.getSource();
         String nodeName = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         Collection<UserNode> nodes = toolBarPortlet.getUserNodes();
         if (nodes.size() < 1)
         {
            createDashboard(nodeName, toolBarPortlet);
         }
         else
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(
               prContext.getPortalURI() + nodes.iterator().next().getURI());
         }
      }

      private static void createDashboard(String _nodeName, UIUserToolBarDashboardPortlet toolBarPortlet)
      {
         try
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            if (_nodeName == null)
            {
               logger.debug("Parsed nodeName is null, hence use Tab_0 as default name");
               _nodeName = DEFAULT_TAB_NAME;
            }

            UserPortal userPortal = toolBarPortlet.getUserPortal();
            UserNavigation userNav = toolBarPortlet.getCurrentUserNavigation();
            if (userNav == null)
            {
               return;
            }
            SiteKey siteKey = userNav.getKey();

            UserPortalConfigService _configService = toolBarPortlet.getApplicationComponent(UserPortalConfigService.class);
            Page page =
               _configService.createPageTemplate(PAGE_TEMPLATE, siteKey.getTypeName(), siteKey.getName());
            page.setTitle(_nodeName);
            page.setName(_nodeName);
            toolBarPortlet.getApplicationComponent(DataStorage.class).create(page);

            UserNode rootNode = userPortal.getNode(userNav, Scope.CHILDREN, null);
            if (rootNode == null)
            {
               return;
            }
            rootNode.filter(toolBarPortlet.TOOLBAR_DASHBOARD_FILTER);
            UserNode tabNode = rootNode.addChild(_nodeName);
            tabNode.setLabel(_nodeName);            
            tabNode.setPageRef(page.getPageId());

            rootNode.save();            
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + tabNode.getURI());
         }
         catch (Exception ex)
         {
            logger.info("Could not create default dashboard page", ex);
         }
      }
   }
}

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

package org.exoplatform.portal.webui.navigation;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com
 * Jul 12, 2006
 */
public class UIPortalNavigation extends UIComponent
{
   private boolean useAJAX = true;

   private boolean showUserNavigation = true;

   private TreeNode treeNode_;

   private String cssClassName = "";

   private String template;

   private final Scope PORTAL_NAVIGATION_SCOPE;

   private final Scope SITEMAP_SCOPE;

   public UIPortalNavigation()
   {
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      scopeBuilder.withTemporalCheck();
      PORTAL_NAVIGATION_SCOPE = userPortal.createScope(2, scopeBuilder.build());

      UserNodePredicate.Builder sitemapScopeBuilder = UserNodePredicate.builder();
      sitemapScopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL, Visibility.SYSTEM);
      sitemapScopeBuilder.withTemporalCheck();
      SITEMAP_SCOPE = userPortal.createScope(1, scopeBuilder.build());
   }

   @Override
   public String getTemplate()
   {
      return template != null ? template : super.getTemplate();
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public UIComponent getViewModeUIComponent()
   {
      return null;
   }

   public void setUseAjax(boolean bl)
   {
      useAJAX = bl;
   }

   public boolean isUseAjax()
   {
      return useAJAX;
   }

   public boolean isShowUserNavigation()
   {
      return showUserNavigation;
   }

   public void setShowUserNavigation(boolean showUserNavigation)
   {
      this.showUserNavigation = showUserNavigation;
   }

   public void setCssClassName(String cssClassName)
   {
      this.cssClassName = cssClassName;
   }

   public String getCssClassName()
   {
      return cssClassName;
   }

   public List<UserNode> getNavigations() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      List<UserNode> nodes = new ArrayList<UserNode>();
      if (context.getRemoteUser() != null)
      {                                      
         nodes.add(getCurrentNavigation());
      }
      else
      {
         UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
         List<UserNavigation> navigations = userPortal.getNavigations();
         for (UserNavigation userNav : navigations)
         {
            if (!showUserNavigation && userNav.getKey().getType().equals(SiteType.USER))
            {
               continue;
            }

            UserNode rootNode = userPortal.getNode(userNav, PORTAL_NAVIGATION_SCOPE);
            nodes.add(rootNode);
         }
      }
      return nodes;
   }

   public void loadTreeNodes() throws Exception
   {
      treeNode_ = new TreeNode();

      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      List<UserNavigation> listNavigations = userPortal.getNavigations();

      List<UserNode> childNodes = new LinkedList<UserNode>();
      for (UserNavigation nav : rearrangeNavigations(listNavigations))
      {
         if (!showUserNavigation && nav.getKey().getTypeName().equals(PortalConfig.USER_TYPE))
         {
            continue;
         }
         UserNode rootNode = userPortal.getNode(nav, SITEMAP_SCOPE);
         childNodes.addAll(rootNode.getChildren());         
      }
      treeNode_.setChildren(childNodes);
   }

   /**
    * 
    * @param listNavigation
    * @return
    */
   private List<UserNavigation> rearrangeNavigations(List<UserNavigation> listNavigation)
   {
      List<UserNavigation> returnNavs = new ArrayList<UserNavigation>();

      List<UserNavigation> portalNavs = new ArrayList<UserNavigation>();
      List<UserNavigation> groupNavs = new ArrayList<UserNavigation>();
      List<UserNavigation> userNavs = new ArrayList<UserNavigation>();

      for (UserNavigation nav : listNavigation)
      {
         String ownerType = nav.getKey().getTypeName();
         if (PortalConfig.PORTAL_TYPE.equals(ownerType))
         {
            portalNavs.add(nav);
         }
         else if (PortalConfig.GROUP_TYPE.equals(ownerType))
         {
            groupNavs.add(nav);
         }
         else if (PortalConfig.USER_TYPE.equals(ownerType))
         {
            userNavs.add(nav);
         }
      }

      returnNavs.addAll(portalNavs);
      returnNavs.addAll(groupNavs);
      returnNavs.addAll(userNavs);

      return returnNavs;
   }

   public TreeNode getTreeNodes()
   {
      return treeNode_;
   }

   public UserNode getCurrentNavigation() throws Exception
   {
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNavigation userNavigation = Util.getUIPortal().getUserNavigation();
      return userPortal.getNode(userNavigation, PORTAL_NAVIGATION_SCOPE);
   }
   
   /**
    * @deprecated use {@link #getCurrentNavigation()} instead
    * 
    * @return
    * @throws Exception
    */
   @Deprecated
   public PageNavigation getSelectedNavigation() throws Exception
   {
      return Util.getUIPortal().getNavigation();
   }

   public UserNode getSelectedPageNode() throws Exception
   {
      return  Util.getUIPortal().getSelectedUserNode();
   }

   static public class SelectNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         UIPortal uiPortal = Util.getUIPortal();
         String treePath = event.getRequestContext().getRequestParameter(OBJECTID);

         TreeNode selectedode = event.getSource().getTreeNodes().findNodes(treePath);
         //There're may be interuption between browser and server
         if (selectedode == null)
         {
            event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
            return;
         }
         
         PageNodeEvent<UIPortal> pnevent;
         pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, selectedode.getNode().getURI());
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
      }
   }

   static public class ExpandNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         String treePath = event.getRequestContext().getRequestParameter(OBJECTID);
                                                        
         TreeNode treeNode = event.getSource().getTreeNodes();
         TreeNode expandTree = treeNode.findNodes(treePath);
         //There're may be interuption between browser and server
         if (expandTree == null)
         {
            event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
            return;
         }

         UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();

         UserNode expandNode = userPortal.getNode(expandTree.getNode(),  event.getSource().SITEMAP_SCOPE);
         if (expandNode == null)
         {
            event.getSource().loadTreeNodes();
            event.getRequestContext().getUIApplication().addMessage(new
               ApplicationMessage("UIPortalNavigation.msg.staleData", null, ApplicationMessage.WARNING));
         }
         else
         {
            expandTree.setChildren(expandNode.getChildren());
            expandTree.setExpanded(true);
         }
                               
         event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
      }
   }

   static public class CollapseNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         // get URI
         String treePath = event.getRequestContext().getRequestParameter(OBJECTID);

         UIPortalNavigation uiNavigation = event.getSource();
         TreeNode rootNode = uiNavigation.getTreeNodes();
         
         TreeNode collapseTree = rootNode.findNodes(treePath);
         if (collapseTree != null)
         {
            collapseTree.setExpanded(false);
         }         
         
         Util.getPortalRequestContext().setResponseComplete(true);
      }
   }

   static public class CollapseAllNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         UIPortalNavigation uiNavigation = event.getSource();
         uiNavigation.loadTreeNodes();

         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation);
      }
   }

   static public class ExpandAllNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIPortalNavigation uiNavigation = event.getSource();
         // reload TreeNodes
         uiNavigation.loadTreeNodes();
         TreeNode treeNode = uiNavigation.getTreeNodes();

         expandAllNode(treeNode);

         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation);
      }

      public void expandAllNode(TreeNode treeNode) throws Exception
      {

         if (treeNode.getChildren().size() > 0)
         {
            for (TreeNode child : treeNode.getChildren())
            {
//               PageNode expandNode = child.getNode();
//               PageNavigation selectNav = child.getNavigation();
//
//               // set node to child tree
//               if (expandNode.getChildren().size() > 0)
//               {
//                  child.setChildren(expandNode.getChildren(), selectNav);
//               }

               // expand child tree
               expandAllNode(child);
            }
         }
      }
   }   
}

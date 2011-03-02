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
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Navigation;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com
 * Jul 12, 2006
 */
public class UIPortalNavigation extends UIComponent
{
   private boolean useAJAX = true;

   private boolean showUserNavigation = true;

   protected PageNode selectedNode_;

   protected Object selectedParent_;

   private TreeNode treeNode_;

   private String cssClassName = "";

   private String template;
   
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

   public List<Node> getNavigations() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      List<Node> nodes = new ArrayList<Node>();
      if (context.getRemoteUser() != null)
      {
         nodes.add(PageNavigationUtils.filter(getCurrentNavigation(), context.getRemoteUser()));
      }
      else
      {
         UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
         NavigationService naviSer = getApplicationComponent(NavigationService.class);
         
         List<UserNavigation> navigations = userPortal.getNavigations();
         
         for (UserNavigation userNav : navigations)
         {
            Navigation navi = userNav.getNavigation();
            
            if (!showUserNavigation && navi.getKey().getType().equals(SiteType.USER))
            {
               continue;
            }
            
            Node rootNode = naviSer.load(navi, Scope.ALL);
            PageNavigationUtils.filter(rootNode, null);
            nodes.add(rootNode);
         }
      }
      return nodes;
   }

   public void loadTreeNodes() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      treeNode_ = new TreeNode(new PageNode(), new PageNavigation(), true);
      List<PageNavigation> listNavigations = Util.getUIPortalApplication().getNavigations();
      
      for (PageNavigation nav : rearrangeNavigations(listNavigations))
      {
         if (!showUserNavigation && nav.getOwnerType().equals("user"))
         {
            continue;
         }
         PageNavigation filterNav = PageNavigationUtils.filter(nav, context.getRemoteUser());
         treeNode_.setChildren(filterNav.getNodes(), filterNav);
      }
   }
   
   /**
    * 
    * @param listNavigation
    * @return
    */
   private List<PageNavigation> rearrangeNavigations(List<PageNavigation> listNavigation)
   {
      List<PageNavigation> returnNavs = new ArrayList<PageNavigation>();

      List<PageNavigation> portalNavs = new ArrayList<PageNavigation>();
      List<PageNavigation> groupNavs = new ArrayList<PageNavigation>();
      List<PageNavigation> userNavs = new ArrayList<PageNavigation>();

      for (PageNavigation nav : listNavigation)
      {
         String ownerType = nav.getOwnerType();
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

   public Node getCurrentNavigation() throws Exception
   {
      UserNavigation userNavigation = Util.getUIPortal().getUserNavigation();
      Navigation nav = userNavigation.getNavigation();
      
      NavigationService naviSer = getApplicationComponent(NavigationService.class);
      return naviSer.load(nav, Scope.ALL);
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

   public Object getSelectedParent()
   {
      return selectedParent_;
   }

   public PageNode getSelectedPageNode() throws Exception
   {
      if (selectedNode_ != null)
         return selectedNode_;
      selectedNode_ = Util.getUIPortal().getSelectedNode();
      return selectedNode_;
   }

   public boolean isSelectedNode(PageNode node)
   {
      if (selectedNode_ != null && node.getUri().equals(selectedNode_.getUri()))
         return true;
      if (selectedParent_ == null || selectedParent_ instanceof PageNavigation)
         return false;
      PageNode pageNode = (PageNode)selectedParent_;
      return node.getUri().equals(pageNode.getUri());
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIPortal uiPortal = Util.getUIPortal();
      if ((uiPortal.getSelectedNode() != null) && (uiPortal.getSelectedNode() != selectedNode_))
      {
         setSelectedPageNode(uiPortal.getSelectedNode());
      }
      super.processRender(context);
   }

   private void setSelectedPageNode(PageNode selectedNode) throws Exception
   {
      return;
//      selectedNode_ = selectedNode;
//      selectedParent_ = null;
//      String seletctUri = selectedNode.getUri();
//      int index = seletctUri.lastIndexOf("/");
//      String parentUri = null;
//      if (index > 0)
//         parentUri = seletctUri.substring(0, seletctUri.lastIndexOf("/"));
//      List<Node> pageNavs = getNavigations();
//      for (Node pageNav : pageNavs)
//      {
//         if (PageNavigationUtils.searchPageNodeByUri(pageNav, selectedNode.getUri()) != null)
//         {
//            if (parentUri == null || parentUri.length() < 1)
//               selectedParent_ = pageNav;
//            else
//               selectedParent_ = PageNavigationUtils.searchPageNodeByUri(pageNav, parentUri);
//            break;
//         }
//      }
   }

   static public class SelectNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         UIPortalNavigation uiNavigation = event.getSource();
         UIPortal uiPortal = Util.getUIPortal();
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         int index = uri.lastIndexOf("::");
         String id = uri.substring(index + 2);
         PageNavigation selectNav = null;
         if (index <= 0)
         {
            selectNav = uiPortal.getSelectedNavigation();
         }
         else
         {
            String navId = uri.substring(0, index);
            //selectNav = uiPortal.getPageNavigation(Integer.parseInt(navId));
            selectNav = uiPortal.getSelectedNavigation();
         }
         PageNode selectNode = PageNavigationUtils.searchPageNodeByUri(selectNav, id);
         uiNavigation.selectedNode_ = selectNode;
         String parentUri = null;
         index = uri.lastIndexOf("/");
         if (index > 0)
            parentUri = uri.substring(0, index);
         if (parentUri == null || parentUri.length() < 1)
            uiNavigation.selectedParent_ = selectNav;
         else
            uiNavigation.selectedParent_ = PageNavigationUtils.searchPageNodeByUri(selectNav, parentUri);

         PageNodeEvent<UIPortal> pnevent;
         pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
      }
   }

   static public class ExpandNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         UIPortalNavigation uiNavigation = event.getSource();

         TreeNode treeNode = uiNavigation.getTreeNodes();
         List<PageNavigation> all_Navigations = Util.getUIPortalApplication().getNavigations();
         
         // get URI
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         int index = uri.lastIndexOf("::");
         String id = uri.substring(index + 2);

         // get PageNavigation by uri
         PageNavigation selectNav = null;

         String navId = uri.substring(0, index);
         selectNav = PageNavigationUtils.findNavigationByID(all_Navigations, Integer.parseInt(navId));
         if(selectNav == null)
         {
            return;
         }
         
         // get PageNode by uri
         PageNode expandNode = PageNavigationUtils.searchPageNodeByUri(selectNav, id);

         TreeNode expandTree = null;
         if (treeNode.getChildren() != null)
         {
            expandTree = treeNode.getChildByPath(uri, treeNode);
         }

         if(expandTree != null)
         {
            expandTree.setChildren(expandNode.getChildren(), selectNav);
         }
         
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation);
      }
   }

   static public class CollapseNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         UIPortalNavigation uiNavigation = event.getSource();

         TreeNode treeNode = uiNavigation.getTreeNodes();
         UIPortal uiPortal = Util.getUIPortal();

         // get URI
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);

         int index = uri.lastIndexOf("::");
         String id = uri.substring(index + 2);

         // get PageNavigation by uri
         PageNavigation selectNav = null;

         String navId = uri.substring(0, index);
         
         //TODO: Minh Hoang TO
         //selectNav = uiPortal.getPageNavigation(Integer.parseInt(navId));
         selectNav = uiPortal.getSelectedNavigation();
         
         // get PageNode by uri
         PageNode expandNode = PageNavigationUtils.searchPageNodeByUri(selectNav, id);

         TreeNode expandTree = null;
         if (treeNode.getChildren() != null)
         {
            expandTree = treeNode.getChildByPath(uri, treeNode);
         }
         
         if(expandTree != null) 
         {
            expandTree.setExpanded(false);
         }
         
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation);
      }
   }

   static public class CollapseAllNodeActionListener extends EventListener<UIPortalNavigation>
   {
      public void execute(Event<UIPortalNavigation> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
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
               PageNode expandNode = child.getNode();
               PageNavigation selectNav = child.getNavigation();

               // set node to child tree
               if (expandNode.getChildren().size() > 0)
               {
                  child.setChildren(expandNode.getChildren(), selectNav);
               }

               // expand child tree
               expandAllNode(child);
            }
         }
      }
   }
}

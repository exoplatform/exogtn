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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ComponentConfig(
   template = "system:/groovy/portal/webui/navigation/UIPageNodeSelector.gtmpl",
   events = {@EventConfig(listeners = UIPageNodeSelector.ChangeNodeActionListener.class)}
)
public class UIPageNodeSelector extends UIContainer
{
   private UserNavigation navigation;

   private UserNode rootNode;

   private SelectedNode selectedNode;

   private static final Scope NODE_SELECTOR_SCOPE = Scope.GRANDCHILDREN;

   private final NodeFilter NODE_SELECTOR_FILTER;

   public UIPageNodeSelector() throws Exception
   {
      UITree uiTree = addChild(UITree.class, null, "TreePageSelector");
      uiTree.setIcon("DefaultPageIcon");
      uiTree.setSelectedIcon("DefaultPageIcon");
      uiTree.setBeanIdField("URI");
      uiTree.setBeanLabelField("encodedResolvedLabel");
      uiTree.setBeanIconField("icon");

      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck();
      NODE_SELECTOR_FILTER = userPortal.createFilter(scopeBuilder.build());
   }

   public void setNavigation(UserNavigation nav) throws Exception
   {
      this.navigation = nav;
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();

      if (navigation != null)
      {
         rootNode = userPortal.getNode(nav, NODE_SELECTOR_SCOPE);
         if (rootNode == null)
         {
            return;
         }
         rootNode.filter(NODE_SELECTOR_FILTER);
         selectNavigation(navigation);
         UserNode selectedNode = Util.getUIPortal().getSelectedUserNode();
         if (selectedNode != null)
         {
            selectPageNodeByUri(selectedNode.getURI());
         }
         return;
      }
      selectNavigation();
   }

   private void selectNavigation()
   {
      if (navigation == null)
      {
         return;
      }
      if (selectedNode == null || !navigation.getKey().equals(selectedNode.getNavigation().getKey()))
      {
         selectedNode = new SelectedNode(navigation, rootNode);

         Iterator<UserNode> iterator = rootNode.getChildren().iterator();

         if (iterator.hasNext())
         {
            selectedNode.setNode(iterator.next());
         }
      }
      selectNavigation(selectedNode.getNavigation());
      if (selectedNode.getNode() != null)
      {
         selectPageNodeByUri(selectedNode.getNode().getURI());
      }
   }

   public void selectNavigation(UserNavigation pageNav)
   {
      navigation = pageNav;
      selectedNode = new SelectedNode(pageNav, rootNode);
      selectPageNodeByUri(null);
      UITree uiTree = getChild(UITree.class);
      uiTree.setSibbling(new ArrayList(rootNode.getChildren()));
   }

   public void selectPageNodeByUri(String uri)
   {
      if (selectedNode == null || !(navigation.getKey().equals(selectedNode.getNavigation().getKey())))
      {
         return;
      }
      UITree tree = getChild(UITree.class);
      List<?> sibbling = tree.getSibbling();
      tree.setSibbling(null);
      tree.setParentSelected(null);
      selectedNode.setNode(searchPageNodeByUri(rootNode, uri));
      if (selectedNode.getNode() != null)
      {
         tree.setSelected(selectedNode.getNode());
         tree.setChildren(new ArrayList(selectedNode.getNode().getChildren()));
         return;
      }
      tree.setSelected(null);
      tree.setChildren(null);
      tree.setSibbling(sibbling);
   }

   public UserNode searchPageNodeByUri(UserNode rootNode, String uri)
   {
      if (rootNode == null || uri == null)
      {
         return null;
      }
      if (rootNode.getURI().equals(uri))
      {
         return rootNode;
      }
      Collection<UserNode> pageNodes = rootNode.getChildren();
      Iterator<UserNode> iterator = pageNodes.iterator();
      UITree uiTree = getChild(UITree.class);
      while (iterator.hasNext())
      {
         UserNode ele = iterator.next();
         UserNode returnPageNode = searchPageNodeByUri(ele, uri, uiTree);
         if (returnPageNode == null)
         {
            continue;
         }
         if (uiTree.getSibbling() == null)
         {
            uiTree.setSibbling(new ArrayList(pageNodes));
         }
         return returnPageNode;
      }
      return null;
   }

   private UserNode searchPageNodeByUri(UserNode userNode, String uri, UITree tree)
   {
      if (userNode.getURI().equals(uri))
      {
         return userNode;
      }
      Collection<UserNode> children = userNode.getChildren();
      if (children == null)
      {
         return null;
      }
      Iterator<UserNode> iterator = children.iterator();
      while (iterator.hasNext())
      {
         UserNode ele = iterator.next();
         UserNode returnPageNode = searchPageNodeByUri(ele, uri, tree);
         if (returnPageNode == null)
         {
            continue;
         }
         if (tree.getSibbling() == null)
         {
            tree.setSibbling(new ArrayList(children));
         }
         if (tree.getParentSelected() == null)
         {
            tree.setParentSelected(userNode);
         }
         //         selectedNode.setParentNode(pageNode);
         return returnPageNode;
      }
      return null;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIRightClickPopupMenu uiPopupMenu = getChild(UIRightClickPopupMenu.class);
      if (uiPopupMenu != null)
      {
         uiPopupMenu.setRendered(true);
      }
      super.processRender(context);
   }

   public SelectedNode getSelectedNode()
   {
      return selectedNode;
   }

   public UserNavigation getNavigation()
   {
      return navigation;
   }

   public UserNode getSelectedPageNode()
   {
      return selectedNode == null ? null : selectedNode.getNode();
   }

   public String getUpLevelUri()
   {
      return selectedNode.getParentNode().getURI();
   }

   static public class ChangeNodeActionListener extends EventListener<UITree>
   {
      public void execute(Event<UITree> event) throws Exception
      {
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         UIPageNodeSelector uiPageNodeSelector = event.getSource().getParent();
         uiPageNodeSelector.selectPageNodeByUri(uri);

         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         UIPortalApplication uiPortalApp = uiPageNodeSelector.getAncestorOfType(UIPortalApplication.class);
         UIPortalToolPanel uiToolPanel = Util.getUIPortalToolPanel();
         uiToolPanel.setRenderSibling(UIPortalToolPanel.class);
         uiToolPanel.setShowMaskLayer(true);
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         pcontext.setFullRender(true);

         UIContainer uiParent = uiPageNodeSelector.getParent();
         UserNode node = null;
         if (uiPageNodeSelector.getSelectedNode() == null)
         {
            node = Util.getUIPortal().getNavPath().getTarget();
         }
         else
         {
            node = uiPageNodeSelector.getSelectedNode().getNode();
         }
         if (node == null)
         {
            uiPageNodeSelector.selectNavigation(uiPageNodeSelector.getNavigation());
            uiToolPanel.setUIComponent(null);
            return;
         }

         UserPortalConfigService configService = uiParent.getApplicationComponent(UserPortalConfigService.class);
         Page page = null;
         if (node.getPageRef() != null)
         {
            page = configService.getPage(node.getPageRef(), event.getRequestContext().getRemoteUser());
         }

         if (page == null)
         {
            uiToolPanel.setUIComponent(null);
            return;
         }

         UIPage uiPage = Util.toUIPage(node.getPageRef(), uiToolPanel);
         UIPageBody uiPageBody = uiPortalApp.findFirstComponentOfType(UIPageBody.class);
         if (uiPageBody.getUIComponent() != null)
         {
            uiPageBody.setUIComponent(null);
         }
         uiToolPanel.setUIComponent(uiPage);
      }
   }

   public static class SelectedNode
   {

      private UserNavigation nav;

      private UserNode node;

      public SelectedNode(UserNavigation navigation, UserNode userNode)
      {
         this.nav = navigation;
         this.node = userNode;
      }

      public UserNavigation getNavigation()
      {
         return nav;
      }

      public void setNavigation(UserNavigation nav)
      {
         this.nav = nav;
      }

      private UserNode getParentNode()
      {
         return node.getParent();
      }

      public UserNode getNode()
      {
         return node;
      }

      public void setNode(UserNode node)
      {
         this.node = node;
      }
   }

}

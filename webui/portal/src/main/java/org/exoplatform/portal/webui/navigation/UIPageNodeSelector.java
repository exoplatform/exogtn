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

import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;

import java.util.Iterator;

@ComponentConfig(
   template = "system:/groovy/portal/webui/navigation/UIPageNodeSelector.gtmpl"
)
public class UIPageNodeSelector extends UIContainer
{
   private UserNavigation navigation;

   private UserNode selectedNode;

   private final NodeFilter NODE_SELECTOR_FILTER;

   private UserPortal userPortal;

   public UIPageNodeSelector() throws Exception
   {
      UITree uiTree = addChild(UITree.class, null, "TreePageSelector");
      uiTree.setIcon("DefaultPageIcon");
      uiTree.setSelectedIcon("DefaultPageIcon");
      uiTree.setBeanIdField("URI");
      uiTree.setBeanLabelField("encodedResolvedLabel");
      uiTree.setBeanIconField("icon");
      uiTree.setBeanChildCountField("childrenCount");

      userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck();
      NODE_SELECTOR_FILTER = userPortal.createFilter(scopeBuilder.build());
   }

   public void setNavigation(UserNavigation nav) throws Exception
   {
      navigation = nav;      
   }

   private void load(UserNode node) throws Exception
   {
      userPortal.updateNode(node, Scope.GRANDCHILDREN, null);
      node.filter(NODE_SELECTOR_FILTER);
   }
   
   public void setSelectedNode(UserNode node) throws Exception
   {
      if (node == null)
      {
         return;
      }
      UITree tree = getChild(UITree.class);
      UserNode parent = node.getParent();
      if (parent != null)
      {
         load(node);
         tree.setSelected(node);
         tree.setChildren(node.getChildren());
         load(node.getParent());
         tree.setSibbling(parent.getChildren());
         tree.setParentSelected(parent);
      }
      else
      {
         tree.setSelected(null);
         tree.setChildren(null);
      }
      selectedNode = node;
   }
   
   public void setSelectedURI(String uri) throws Exception
   {
      UserNode node;
      if (selectedNode.getParent() != null)
      {
         node = findUserNodeByURI(selectedNode.getParent(), uri);
      }
      else
      {
         node = findUserNodeByURI(selectedNode, uri);
      }
      setSelectedNode(node);
   }
   
   private UserNode findUserNodeByURI(UserNode rootNode, String uri)
   {
      if (rootNode.getURI().equals(uri))
      {
         return rootNode;
      }
      Iterator<UserNode> iterator = rootNode.getChildren().iterator();
      while (iterator.hasNext())
      {
         UserNode next = iterator.next();
         UserNode node = findUserNodeByURI(next, uri);
         if (node == null)
         {
            continue;
         }
         return node;
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

   public UserNode getSelectedNode()
   {
      return selectedNode;
   }

   public UserNavigation getNavigation()
   {
      return navigation;
   }
}

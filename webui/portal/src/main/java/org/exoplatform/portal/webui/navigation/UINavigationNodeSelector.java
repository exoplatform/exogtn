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

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageNodeForm;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Copied by The eXo Platform SARL Author May 28, 2009 3:07:15 PM */
@ComponentConfigs({
   @ComponentConfig(template = "system:/groovy/portal/webui/navigation/UINavigationNodeSelector.gtmpl", events = {@EventConfig(listeners = UINavigationNodeSelector.ChangeNodeActionListener.class)}),
   @ComponentConfig(id = "NavigationNodePopupMenu", type = UIRightClickPopupMenu.class, template = "system:/groovy/webui/core/UIRightClickPopupMenu.gtmpl", events = {
      @EventConfig(listeners = UINavigationNodeSelector.AddNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.EditPageNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.EditSelectedNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.CopyNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.CutNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.CloneNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.PasteNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.MoveUpActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.MoveDownActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.DeleteNodeActionListener.class, confirm = "UIPageNodeSelector.deleteNavigation")}),
   @ComponentConfig(id = "UINavigationNodeSelectorPopupMenu", type = UIRightClickPopupMenu.class, template = "system:/groovy/webui/core/UIRightClickPopupMenu.gtmpl", events = {
      @EventConfig(listeners = UINavigationNodeSelector.AddNodeActionListener.class),
      @EventConfig(listeners = UINavigationNodeSelector.PasteNodeActionListener.class)})})
public class UINavigationNodeSelector extends UIContainer
{
   
   private UserNavigation edittedNavigation;

   /** This field holds transient copy of edittedTreeNodeData, which is used when user pastes the content to a new tree node */
   private TreeNodeData copyOfTreeNodeData;

   private TreeNodeData rootNode;
   
   private UserPortal userPortal;

   private NodeFilter nodeFilter;

   private static final Scope NODE_SCOPE = Scope.CHILDREN;

   private Map<String, TreeNodeData> cachedNodes = new HashMap<String, TreeNodeData>();

   public UINavigationNodeSelector() throws Exception
   {
      UIRightClickPopupMenu rightClickPopup =
         addChild(UIRightClickPopupMenu.class, "UINavigationNodeSelectorPopupMenu", null).setRendered(true);
      rightClickPopup.setActions(new String[]{"AddNode", "PasteNode"});

      UITree uiTree = addChild(UITree.class, null, "TreeNodeSelector");
      uiTree.setIcon("DefaultPageIcon");
      uiTree.setSelectedIcon("DefaultPageIcon");
      uiTree.setBeanIdField("Id");
      uiTree.setBeanChildCountField("childrenCount");
      uiTree.setBeanLabelField("encodedResolvedLabel");
      uiTree.setBeanIconField("icon");
      
      UIRightClickPopupMenu uiPopupMenu =
         createUIComponent(UIRightClickPopupMenu.class, "NavigationNodePopupMenu", null);
      uiPopupMenu.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode",
         "CutNode", "DeleteNode", "MoveUp", "MoveDown"});
      uiTree.setUIRightClickPopupMenu(uiPopupMenu);
   }
      
   /**
    * Init the UITree wrapped in UINavigationNodeSelector and localize the label
    * @throws Exception
    */
   public void initTreeData() throws Exception
   {      
      if(edittedNavigation == null || userPortal == null)
      {
         throw new IllegalStateException("edittedNavigation and userPortal must be initialized first");
      }

      cachedNodes.clear();

      initRootNode();
      selectNode(rootNode);
   }

   public TreeNodeData selectNode(String nodeID) throws Exception
   {
      TreeNodeData node = searchNode(nodeID);
      return selectNode(node);
   }

   public TreeNodeData selectNode(TreeNodeData node) throws Exception
   {
      node = lazyLoadData(node);

      if (node == null)
      {
         return null;
      }

      UITree tree = getChild(UITree.class);
      tree.setSelected(node);
      if (node.getParent() == null)
      {
         tree.setChildren(null);
         tree.setSibbling(node.getChildren());
         tree.setParentSelected(null);
      }
      else
      {
         TreeNodeData parentNode = node.getParent();
         tree.setChildren(node.getChildren());                    
         tree.setSibbling(parentNode.getChildren());
         tree.setParentSelected(parentNode);
      }
      return node;
   }

   public TreeNodeData searchNode(String nodeID)
   {
      if (nodeID == null || nodeID.trim().isEmpty())
      {
         nodeID = rootNode.getId();
      }

      return cachedNodes.get(nodeID);
   }

   private TreeNodeData initRootNode() throws Exception
   {
      this.rootNode = new TreeNodeData(edittedNavigation, userPortal.getNode(edittedNavigation, NODE_SCOPE).filter(nodeFilter), this);
      rootNode.setLoaded(true);      
      return addToCached(rootNode);
   }

   private TreeNodeData addToCached(TreeNodeData node)
   {
      if (node == null)
      {
         return null;
      }
      if (!cachedNodes.containsKey(node.getId()))
      {
         cachedNodes.put(node.getId(), node);
      }

      for (TreeNodeData child : node.getChildren())
      {
         addToCached(child);
      }
      return node;
   }

   private TreeNodeData removeFromCached(TreeNodeData node)
   {
      if (node == null)
      {
         return null;
      }

      TreeNodeData currentCopy = getCopyNode();
      if (currentCopy != null && currentCopy.getURI().equals(node.getURI()))
      {
         setCopyNode(null);
         UIRightClickPopupMenu popup = getChild(UITree.class).getUIRightClickPopupMenu();
         popup.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CutNode",
            "CloneNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
      
      cachedNodes.remove(node.getId());
      for (TreeNodeData child : node.getChildren())
      {
         removeFromCached(child);
      }
      return node;
   }

   private TreeNodeData lazyLoadData(TreeNodeData treeNode) throws Exception
   {
      if (treeNode == null || treeNode.getNode() == null)
      {
         return treeNode;
      }

      UserNode node = treeNode.getNode();
      if (node.getId() == null ||  treeNode.isLoaded())
      {
         return treeNode;
      }

      if (userPortal.getNode(node, NODE_SCOPE) == null)
      {
         TreeNodeData parent = treeNode.getParent();
         if (parent != null)
         {
            parent.removeChild(treeNode);
            selectNode(parent);  
         }
         return null;
      }
      node.filter(nodeFilter);
      treeNode.setLoaded(true);
      return addToCached(treeNode);
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIRightClickPopupMenu uiPopupMenu = getChild(UIRightClickPopupMenu.class);
      if (uiPopupMenu != null)
      {
         if (edittedNavigation == null)
         {
            uiPopupMenu.setRendered(false);
         }
         else
         {
            uiPopupMenu.setRendered(true);
         }
      }
      super.processRender(context);
   }

   public TreeNodeData getCopyNode()
   {
      return copyOfTreeNodeData;
   }

   public void setCopyNode(TreeNodeData copyNode)
   {
      this.copyOfTreeNodeData = copyNode;
   }

   public TreeNodeData getRootNode()
   {
      if (userPortal == null)
      {
         throw new IllegalStateException("userPortal must be initialized first");
      }                                                         
      return rootNode;
   }

   public void setUserPortal(UserPortal userPortal) throws Exception
   {
      if (edittedNavigation == null)
      {
         throw new IllegalStateException("edittedNavigation must be initialized first");
      }
      this.userPortal = userPortal;

      setNodeFilter(userPortal.createFilter(UserNodePredicate.builder().withAuthorizationCheck().build()));
   }

   private void setNodeFilter(NodeFilter nodeFilter)
   {
      this.nodeFilter = nodeFilter;
   }

   public void setEdittedNavigation(UserNavigation _filteredEdittedNavigation) throws Exception
   {
      this.edittedNavigation = _filteredEdittedNavigation;
   }

   public UserNavigation getEdittedNavigation()
   {
      return this.edittedNavigation;
   }

   static public class ChangeNodeActionListener extends EventListener<UITree>
   {
      public void execute(Event<UITree> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getParent();
         uiNodeSelector.selectNode(nodeID);

         UINavigationManagement nodeManager = uiNodeSelector.getParent();
         event.getRequestContext().addUIComponentToUpdateByAjax(nodeManager);

         UIContainer uiParent = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = {UINavigationNodeSelector.class};
         uiParent.setRenderedChildrenOfTypes(childrenToRender);
      }
   }

   static public class AddNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIRightClickPopupMenu uiPopupMenu = event.getSource();

         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         TreeNodeData selectedNode = uiNodeSelector.searchNode(nodeID);
         if (selectedNode == null || uiNodeSelector.lazyLoadData(selectedNode) == null)
         {
            return;
         }

         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm uiNodeForm = uiManagementPopup.createUIComponent(UIPageNodeForm.class, null, null);
         uiNodeForm.setValues(null);
         uiManagementPopup.setUIComponent(uiNodeForm);

         uiNodeForm.setSelectedParent(selectedNode);

         UserNavigation edittedNavigation = uiNodeSelector.getEdittedNavigation();
         uiNodeForm.setContextPageNavigation(edittedNavigation);
         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }
   }

   static public class EditPageNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         // get nodeID
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         // get UINavigationNodeSelector
         UIRightClickPopupMenu uiPopupMenu = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         // get Selected Node
         TreeNodeData selectedPageNode = uiNodeSelector.searchNode(nodeID);

         UIPortalApplication uiApp = Util.getUIPortalApplication();
         if (selectedPageNode == null || selectedPageNode.getPageRef() == null)
         {
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.notAvailable", null));
            return;
         }

         UserPortalConfigService userService = uiNodeSelector.getApplicationComponent(UserPortalConfigService.class);

         // get selected page
         String pageId = selectedPageNode.getPageRef();
         Page selectPage = (pageId != null) ? userService.getPage(pageId) : null;
         if (selectPage != null)
         {
            UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermission(selectPage))
            {
               uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.UserNotPermission", new String[]{pageId}, 1));
               return;
            }

            uiApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
            //uiWorkingWS.setRenderedChild(UIPortalToolPanel.class);
            //uiWorkingWS.addChild(UIPortalComposer.class, "UIPageEditor", null);

            UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            UIPortalToolPanel uiToolPanel =
               uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class).setRendered(true);
            uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

            UIPortalComposer portalComposer =
               uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(true);
            portalComposer.setShowControl(true);
            portalComposer.setEditted(false);
            portalComposer.setCollapse(false);
            portalComposer.setId("UIPageEditor");
            portalComposer.setComponentConfig(UIPortalComposer.class, "UIPageEditor");

            uiToolPanel.setShowMaskLayer(false);
            uiToolPanel.setWorkingComponent(UIPage.class, null);
            UIPage uiPage = (UIPage)uiToolPanel.getUIComponent();

            if(selectPage.getTitle() == null)
               selectPage.setTitle(selectedPageNode.getLabel());

            // convert Page to UIPage
            PortalDataMapper.toUIPage(uiPage, selectPage);
            Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
            Util.getPortalRequestContext().setFullRender(true);
         }
         else
         {
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.notAvailable", null));
         }
      }
   }

   static public class EditSelectedNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext ctx = event.getRequestContext();
         UIRightClickPopupMenu popupMenu = event.getSource();
         UIApplication uiApp = ctx.getUIApplication();
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = popupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         UserNavigation edittedNav = uiNodeSelector.getEdittedNavigation();
         TreeNodeData selectedNode = uiNodeSelector.searchNode(nodeID);
         if (selectedNode == null)
         {
            return;
         }
         String pageId = selectedNode.getPageRef();

         UserPortalConfigService service = uiApp.getApplicationComponent(UserPortalConfigService.class);
         Page page = (pageId != null) ? service.getPage(pageId) : null;
         if (page != null)
         {
            UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
            if (!userACL.hasPermission(page))
            {
               uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.UserNotPermission", new String[]{pageId}, 1));
               return;
            }
         }

         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm uiNodeForm = uiApp.createUIComponent(UIPageNodeForm.class, null, null);
         uiManagementPopup.setUIComponent(uiNodeForm);

         uiNodeForm.setContextPageNavigation(edittedNav);
         uiNodeForm.setValues(selectedNode);
         uiNodeForm.setSelectedParent(selectedNode.getParent());
         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }
   }

   static public class CopyNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         UINavigationManagement uiManagement = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
         uiManagement.setRenderedChildrenOfTypes(childrenToRender);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

         TreeNodeData node = uiNodeSelector.searchNode(nodeID);
         if (node == null)
         {
            return;
         }

         node.setDeleteNode(false);
         uiNodeSelector.setCopyNode(node);
         event.getSource().setActions(
            new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode", "CutNode",
               "PasteNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
   }

   static public class CutNodeActionListener extends UINavigationNodeSelector.CopyNodeActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         WebuiRequestContext pcontext = event.getRequestContext();
         UIApplication uiApp = pcontext.getUIApplication();
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         UINavigationManagement uiManagement = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
         uiManagement.setRenderedChildrenOfTypes(childrenToRender);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

         TreeNodeData node = uiNodeSelector.searchNode(nodeID);
         if (node == null)
         {
            return;
         }

         if(Visibility.SYSTEM.equals(node.getVisibility()))
         {
            uiApp.addMessage(new ApplicationMessage("UINavigationNodeSelector.msg.systemnode-move", null));
            return;
         }

         node.setDeleteNode(true);
         uiNodeSelector.setCopyNode(node);
         event.getSource().setActions(
            new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode", "CutNode",
               "PasteNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
   }

   static public class CloneNodeActionListener extends UINavigationNodeSelector.CopyNodeActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         super.execute(event);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         uiNodeSelector.getCopyNode().setCloneNode(true);
      }
   }

   static public class PasteNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      private UINavigationNodeSelector uiNodeSelector;
      private DataStorage dataStorage;
      private UserPortalConfigService service;

      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIRightClickPopupMenu uiPopupMenu = event.getSource();
         uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);
         UINavigationManagement uiManagement = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
         uiManagement.setRenderedChildrenOfTypes(childrenToRender);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

         TreeNodeData targetNode = uiNodeSelector.searchNode(nodeID);
         TreeNodeData sourceNode = uiNodeSelector.getCopyNode();
         if (sourceNode == null || uiNodeSelector.lazyLoadData(targetNode) == null)
         {
            return;
         }

         if (targetNode != null && sourceNode.getURI().equals(targetNode.getURI()))
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameSrcAndDes", null));
            return;
         }

         if (isExistChild(targetNode, sourceNode))
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameName", null));
            return;
         }

         if (sourceNode.isDeleteNode())
         {
            sourceNode.getParent().removeChild(sourceNode);
         }
         uiNodeSelector.setCopyNode(null);

         UITree uitree = uiNodeSelector.getChild(UITree.class);
         UIRightClickPopupMenu popup = uitree.getUIRightClickPopupMenu();
         popup.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CutNode",
            "CloneNode", "DeleteNode", "MoveUp", "MoveDown"});

         service = uiNodeSelector.getApplicationComponent(UserPortalConfigService.class);
         dataStorage = uiNodeSelector.getApplicationComponent(DataStorage.class);

         pasteNode(sourceNode, targetNode, sourceNode.isCloneNode());
         uiNodeSelector.selectNode(targetNode);
      }

      private TreeNodeData pasteNode(TreeNodeData sourceNode, TreeNodeData parent, boolean isClone) throws Exception
      {
         if (uiNodeSelector.lazyLoadData(sourceNode) == null)
         {
            return null;
         }
         TreeNodeData node = parent.addChild(sourceNode.getName());
         node.setLabel(sourceNode.getLabel());
         node.setVisibility(sourceNode.getVisibility());
         node.setIcon(sourceNode.getIcon());
         node.setStartPublicationTime(sourceNode.getStartPublicationTime());
         node.setEndPublicationTime(sourceNode.getEndPublicationTime());

         if (isClone)
         {
            String pageName = "page" + node.hashCode();
            node.setPageRef(clonePageFromNode(sourceNode, pageName, sourceNode.getPageNavigation().getKey()));
         }
         else
         {
            node.setPageRef(sourceNode.getPageRef());            
         }

         for (TreeNodeData child : sourceNode.getChildren())
         {
            pasteNode(child, node, isClone);
         }

         return node;
      }

      private String clonePageFromNode(TreeNodeData node,String pageName, SiteKey siteKey) throws Exception
      {
         String pageId = node.getPageRef();
         if (pageId != null)
         {
            Page page = service.getPage(pageId);
            if (page != null)
            {
               page = dataStorage.clonePage(pageId, siteKey.getTypeName(), siteKey.getName(), pageName);
               return page.getPageId();
            }
         }
         return null;
      }

      private boolean isExistChild(TreeNodeData parent, TreeNodeData child)
      {
         return parent != null && parent.getChild(child.getName()) != null;
      }
   }

   static public class MoveUpActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         moveNode(event, -1);
      }

      protected void moveNode(Event<UIRightClickPopupMenu> event, int i) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeSelector.getParent());
         TreeNodeData targetNode = uiNodeSelector.searchNode(nodeID);
         if (targetNode == null)
         {
            return;
         }
         TreeNodeData parentNode = targetNode.getParent();
         Collection<TreeNodeData> children = parentNode.getChildren();

         int k;
         for (k = 0; k < children.size(); k++)
         {
            if (parentNode.getChild(k).getURI().equals(targetNode.getURI()))
            {
               break;
            }
         }
         if (k > children.size())
         {
            return;
         }
         if (k == 0 && i == -1)
         {
            return;
         }
         if (k == children.size() - 1 && i == 1)
         {
            return;
         }
         parentNode.addChild(k + i, targetNode);

         uiNodeSelector.selectNode(targetNode);
      }
   }

   static public class MoveDownActionListener extends UINavigationNodeSelector.MoveUpActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         super.moveNode(event, 1);
      }
   }

   static public class DeleteNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         WebuiRequestContext pcontext = event.getRequestContext();
         UIApplication uiApp = pcontext.getUIApplication();
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         pcontext.addUIComponentToUpdateByAjax(uiNodeSelector);

         TreeNodeData childNode = uiNodeSelector.searchNode(nodeID);
         if (childNode == null)
         {
            return;
         }
         TreeNodeData parentNode = childNode.getParent();

         if(Visibility.SYSTEM.equals(childNode.getVisibility())) {
        		uiApp.addMessage(new ApplicationMessage("UINavigationNodeSelector.msg.systemnode-delete", null));
        		return;
        	}

         parentNode.removeChild(childNode);
         uiNodeSelector.selectNode(parentNode);
      }
   }

   public TreeNodeData getSelectedNode()
   {
      TreeNodeData selectedNode = getChild(UITree.class).getSelected();
      if (selectedNode == null)
      {
         selectedNode = rootNode;
      }
      return selectedNode;
   }

   /**
    *   This class encapsulate data bound to an editted tree node. It consists of a page node (to be added,
    * removed, moved) its parent node and its navigation
    * 
    * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
    * @version $Id$
    *
    */
   public static class TreeNodeData
   {

      private UserNavigation nav;

      private UserNode node;

      private UINavigationNodeSelector selector;

      private boolean deleteNode = false;

      private boolean cloneNode = false;

      private List<TreeNodeData> wrappedChilds;

      private boolean loaded;

      public TreeNodeData(UserNavigation nav, UserNode node, UINavigationNodeSelector selector)
      {
         if (nav == null || node == null)
         {
            throw new IllegalArgumentException("nav or node is null");
         }
         this.nav = nav;
         this.node = node;
         this.selector = selector;
      }

      public UserNavigation getPageNavigation()
      {
         return nav;
      }

      public UserNode getNode()
      {
         return node;
      }

      public boolean isDeleteNode()
      {
         return deleteNode;
      }

      public void setDeleteNode(boolean deleteNode)
      {
         this.deleteNode = deleteNode;
      }

      public boolean isCloneNode()
      {
         return cloneNode;
      }

      public void setCloneNode(boolean b)
      {
         cloneNode = b;
      }

      public List<TreeNodeData> getChildren()
      {
         if (wrappedChilds == null)
         {
            wrappedChilds = new ArrayList<TreeNodeData>();
            for (UserNode child : node.getChildren())
            {
               TreeNodeData node = selector.searchNode(child.getURI());
               //This is for the first time rootNode is loaded
               if (node == null)
               {
                  node = new TreeNodeData(nav, child, selector);
               }
               wrappedChilds.add(node);
            }
         }
         return wrappedChilds;
      }

      public TreeNodeData getChild(String name)
      {
         UserNode child = node.getChild(name);
         if (child == null)
         {
            return null;
         }
         return selector.searchNode(child.getURI());
      }

      public TreeNodeData addChild(String childName)
      {
         wrappedChilds = null;
         UserNode child = node.addChild(childName);
         return selector.addToCached(new TreeNodeData(nav, child, selector));
      }

      public boolean removeChild(String childName)
      {
         wrappedChilds = null;
         UserNode child = node.getChild(childName);
         if (child == null)
         {
            return false;
         }
         selector.removeFromCached(selector.searchNode(child.getURI()));
         return node.removeChild(childName);
      }

      public boolean removeChild(TreeNodeData child)
      {
         wrappedChilds = null;
         if (child == null)
         {
            return false;
         }
         selector.removeFromCached(child);
         return node.removeChild(child.getName());
      }

      public TreeNodeData getParent()
      {
         if (node.getParent() == null)
            return null;
         return selector.searchNode(node.getParent().getURI());
      }

      public String getPageRef()
      {
         return node.getPageRef();
      }

      public String getId()
      {
         return getURI();
      }

      public String getURI()
      {
         return node.getURI();
      }

      public String getIcon()
      {
         return node.getIcon();
      }

      public void setIcon(String icon)
      {
         node.setIcon(icon);
      }

      public String getEncodedResolvedLabel()
      {
         if (node.getParent() == null)
         {
            return "";
         }
         return node.getResolvedLabel();
      }

      public String getName()
      {
         return node.getName();
      }

      public void setName(String name)
      {
         node.setName(name);
      }

      public String getLabel()
      {
         return node.getLabel();
      }

      public void setLabel(String label)
      {
         node.setLabel(label);
      }

      public Visibility getVisibility()
      {
         return node.getVisibility();
      }

      public void setVisibility(Visibility visibility)
      {
         node.setVisibility(visibility);
      }

      public long getStartPublicationTime()
      {
         return node.getStartPublicationTime();
      }

      public void setStartPublicationTime(long startPublicationTime)
      {
         node.setStartPublicationTime(startPublicationTime);
      }

      public long getEndPublicationTime()
      {
         return node.getEndPublicationTime();
      }

      public void setEndPublicationTime(long endPublicationTime)
      {
         node.setEndPublicationTime(endPublicationTime);
      }

      public void setPageRef(String pageRef)
      {
         node.setPageRef(pageRef);
      }

      public String getResolvedLabel()
      {
         String resolvedLabel = node.getResolvedLabel();

         return resolvedLabel == null ? "" : resolvedLabel;
      }

      public boolean hasChildrenRelationship()
      {
         return node.hasChildrenRelationship();
      }

      public int getChildrenCount()
      {
         return node.getChildrenCount();
      }

      public TreeNodeData getChild(int childIndex) throws IndexOutOfBoundsException
      {
         UserNode child = node.getChild(childIndex);
         if (child == null)
         {
            return null;
         }
         return selector.searchNode(child.getURI());
      }

      public void addChild(TreeNodeData node)
      {
         addChild(node.getChildrenCount(), node);
      }

      public void addChild(int index, TreeNodeData child)
      {
         wrappedChilds = null;
         node.addChild(index, child.getNode());
         selector.addToCached(child);
      }

      public boolean isLoaded()
      {
         return loaded;
      }

      public void setLoaded(boolean loaded)
      {
         wrappedChilds = null;
         this.loaded = loaded;
      }

      public void save() throws NavigationServiceException
      {
         selector.cachedNodes.clear();
         node.save();
      }
   }
}

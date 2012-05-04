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

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.webui.navigation.ParentChildPair;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageNodeForm;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIFilterableTree;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

   //private List<PageNavigation> navigations;
   
   private PageNavigation edittedNavigation;
   
   private TreeNodeData edittedTreeNodeData;

   /** This field holds transient copy of edittedTreeNodeData, which is used when user pastes the content to a new tree node */
   private TreeNodeData copyOfTreeNodeData;

   private List<PageNavigation> deleteNavigations = new ArrayList<PageNavigation>();

   public UINavigationNodeSelector() throws Exception
   {
      UIRightClickPopupMenu rightClickPopup =
         addChild(UIRightClickPopupMenu.class, "UINavigationNodeSelectorPopupMenu", null).setRendered(true);
      rightClickPopup.setActions(new String[]{"AddNode", "PasteNode"});

      UIFilterableTree uiTree = addChild(UIFilterableTree.class, null, "TreeNodeSelector");
      uiTree.setIcon("DefaultPageIcon");
      uiTree.setSelectedIcon("DefaultPageIcon");
      uiTree.setBeanIdField("uri");
      uiTree.setBeanLabelField("encodedResolvedLabel");
      uiTree.setBeanIconField("icon");
      
      UIRightClickPopupMenu uiPopupMenu =
         createUIComponent(UIRightClickPopupMenu.class, "NavigationNodePopupMenu", null);
      uiPopupMenu.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode",
         "CutNode", "DeleteNode", "MoveUp", "MoveDown"});
      uiTree.setUIRightClickPopupMenu(uiPopupMenu);
      setupTreeFilter();
   }
   
   /**
    * Setup a filter on the tree node. In this case, SYSTEM node is not displayed if user is not super user
    *
    */
   private void setupTreeFilter()
   {
      UIFilterableTree.TreeNodeFilter nodeFilter = new UIFilterableTree.TreeNodeFilter()
      {
         public boolean filterThisNode(Object nodeObject, WebuiRequestContext context)
         {
            boolean isSystemNode = (((PageNode)nodeObject).getVisibility() == Visibility.SYSTEM );
            if(!isSystemNode)
            {
               return false;
            }else
            {
               UserACL userACL = context.getUIApplication().getApplicationComponent(UserACL.class);
               return !userACL.getSuperUser().equals(context.getRemoteUser());
            }
            
         }
      };
      this.getChild(UIFilterableTree.class).setTreeNodeFilter(nodeFilter);
   }

   public void setEdittedNavigation(PageNavigation _filteredEdittedNavigation) throws Exception
   {
      this.edittedNavigation = _filteredEdittedNavigation;
   }
   
   public PageNavigation getEdittedNavigation()
   {
      return this.edittedNavigation;
   }
      
   /**
    * Init the UITree wrapped in UINavigationNodeSelector and localize the label
    * @throws Exception
    */
   public void initTreeData() throws Exception
   {
      WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
      localizeNavigation(requestContext.getLocale());
      
      initEdittedTreeNodeData();
   }

   /**
    * Init the edited node as well as its parent, navigation
    *
    */
   private void initEdittedTreeNodeData()
   {
      if(edittedNavigation == null)
      {
         return;
      }
      if (edittedTreeNodeData == null)
      {
         edittedTreeNodeData = new TreeNodeData(edittedNavigation);
         if(edittedTreeNodeData.getNode() != null)
         {
            selectPageNodeByUri(edittedTreeNodeData.getNode().getUri());
         }
      }
      
      UITree tree = getChild(UITree.class);
      tree.setSibbling(edittedNavigation.getNodes());
   }
   

   private void localizeNavigation(Locale locale)
   {
      String localeLanguage = (locale.getCountry().length() > 0) ? locale.getLanguage() + "_" + locale.getCountry() : locale.getLanguage();
      LocaleConfig localeConfig =
         getApplicationComponent(LocaleConfigService.class).getLocaleConfig(localeLanguage);
      String ownerType = edittedNavigation.getOwnerType();
      if (!PortalConfig.USER_TYPE.equals(ownerType))
      {
         String ownerId = edittedNavigation.getOwnerId();
         if (PortalConfig.GROUP_TYPE.equals(ownerType))
         {
            // Remove the trailing '/' for a group
            ownerId = ownerId.substring(1);
         }
         ResourceBundle res = localeConfig.getNavigationResourceBundle(ownerType, ownerId);
         for (PageNode node : edittedNavigation.getNodes())
         {
            resolveLabel(res, node);
         }
      }
   }

   private void resolveLabel(ResourceBundle res, PageNode node)
   {
      node.setResolvedLabel(res);
      if (node.getChildren() == null)
      {
         return;
      }
      for (PageNode childNode : node.getChildren())
      {
         resolveLabel(res, childNode);
      }
   }

   public void selectPageNodeByUri(String uri)
   {
      if (edittedTreeNodeData == null)
      {
         return;
      }
      UITree tree = getChild(UITree.class);
      List<?> sibbling = tree.getSibbling();
      tree.setSibbling(null);
      tree.setParentSelected(null);
      edittedTreeNodeData.setNode(searchPageNodeByUri(edittedTreeNodeData.getPageNavigation(), uri));
      if (edittedTreeNodeData.getNode() != null)
      {
         tree.setSelected(edittedTreeNodeData.getNode());
         tree.setChildren(edittedTreeNodeData.getNode().getChildren());
         return;
      }
      tree.setSelected(null);
      tree.setChildren(null);
      tree.setSibbling(sibbling);
   }

   public PageNode searchPageNodeByUri(PageNavigation pageNav, String uri)
   {
      if (pageNav == null || uri == null)
      {
         return null;
      }
      List<PageNode> pageNodes = pageNav.getNodes();
      UITree uiTree = getChild(UITree.class);
      for (PageNode ele : pageNodes)
      {
         PageNode returnPageNode = searchPageNodeByUri(ele, uri, uiTree);
         if (returnPageNode == null)
         {
            continue;
         }
         if (uiTree.getSibbling() == null)
         {
            uiTree.setSibbling(pageNodes);
         }
         return returnPageNode;
      }
      return null;
   }

   private PageNode searchPageNodeByUri(PageNode pageNode, String uri, UITree tree)
   {
      if (pageNode.getUri().equals(uri))
      {
         return pageNode;
      }
      List<PageNode> children = pageNode.getChildren();
      if (children == null)
      {
         return null;
      }
      for (PageNode ele : children)
      {
         PageNode returnPageNode = searchPageNodeByUri(ele, uri, tree);
         if (returnPageNode == null)
         {
            continue;
         }
         if (tree.getSibbling() == null)
         {
            tree.setSibbling(children);
         }
         if (tree.getParentSelected() == null)
         {
            tree.setParentSelected(pageNode);
         }
         edittedTreeNodeData.setParentNode(pageNode);
         return returnPageNode;
      }
      return null;
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

   static public class ChangeNodeActionListener extends EventListener<UITree>
   {
      public void execute(Event<UITree> event) throws Exception
      {
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getParent();
         uiNodeSelector.selectPageNodeByUri(uri);

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
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIRightClickPopupMenu uiPopupMenu = event.getSource();

         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);
         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm uiNodeForm = uiManagementPopup.createUIComponent(UIPageNodeForm.class, null, null);
         uiNodeForm.setValues(null);
         uiManagementPopup.setUIComponent(uiNodeForm);

         Object parent = null;
         PageNavigation edittedNavigation = uiNodeSelector.getEdittedNavigation();
         List<PageNode> pageNodes = edittedNavigation.getNodes();
         if (uri != null && uri.trim().length() > 0)
         {
            for (PageNode pageNode : pageNodes)
            {
               parent = PageNavigationUtils.searchPageNodeByUri(pageNode, uri);
               if (parent != null)
               {
                  break;
               }
            }
         }
         if (parent == null)
         {
            parent = edittedNavigation;
         }
         
         uiNodeForm.setSelectedParent(parent);

         uiNodeForm.setContextPageNavigation(edittedNavigation);
         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }
   }

   static public class EditPageNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         // get URI
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         // get UINavigationNodeSelector
         UIRightClickPopupMenu uiPopupMenu = event.getSource();

         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         // get Selected PageNode
         PageNode selectedPageNode = null;
         List<PageNode> pageNodes = uiNodeSelector.getEdittedNavigation().getNodes();
         if (uri != null && uri.trim().length() > 0)
         {
            for (PageNode pageNode : pageNodes)
            {
               selectedPageNode = PageNavigationUtils.searchPageNodeByUri(pageNode, uri);
               if (selectedPageNode != null)
               {
                  break;
               }
            }
         }

         UIPortalApplication uiApp = Util.getUIPortalApplication();

         if (selectedPageNode.getPageReference() == null)
         {
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.notAvailable", null));
            return;
         }

         UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         UIPortalToolPanel uiToolPanel =
            uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class).setRendered(true);
         UserPortalConfigService userService = uiToolPanel.getApplicationComponent(UserPortalConfigService.class);

         // get selected page
         String pageId = selectedPageNode.getPageReference();
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

            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
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
            return;
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
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = popupMenu.getAncestorOfType(UINavigationNodeSelector.class);
         PageNavigation edittedNav = uiNodeSelector.getEdittedNavigation();
         Object obj = PageNavigationUtils.searchParentNode(edittedNav, uri);
         PageNode selectedNode = PageNavigationUtils.searchPageNodeByUri(edittedNav, uri);
         String pageId = selectedNode.getPageReference();

         UserPortalConfigService service = uiApp.getApplicationComponent(UserPortalConfigService.class);
         Page node = (pageId != null) ? service.getPage(pageId) : null;
         if (node != null)
         {
            UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
            if (!userACL.hasPermission(node))
            {
               uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.UserNotPermission", new String[]{pageId}, 1));;
               return;
            }
         }
         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm uiNodeForm = uiApp.createUIComponent(UIPageNodeForm.class, null, null);
         uiManagementPopup.setUIComponent(uiNodeForm);

         uiNodeForm.setContextPageNavigation(edittedNav);
         uiNodeForm.setValues(selectedNode);
         uiNodeForm.setSelectedParent(obj);
         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }
   }

   static public class CopyNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         UINavigationManagement uiManagement = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
         uiManagement.setRenderedChildrenOfTypes(childrenToRender);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

         PageNavigation nav = uiNodeSelector.getEdittedNavigation();
         if (nav == null)
         {
            return;
         }
         PageNode[] pageNodes = PageNavigationUtils.searchPageNodesByUri(nav, uri);
         if (pageNodes == null)
         {
            return;
         }
         TreeNodeData selectedNode = new TreeNodeData(nav, pageNodes[0], pageNodes[1]);
         selectedNode.setDeleteNode(false);
         uiNodeSelector.setCopyNode(selectedNode);
         event.getSource().setActions(
            new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode", "CutNode",
               "PasteNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
   }

   static public class CutNodeActionListener extends UINavigationNodeSelector.CopyNodeActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
    	  String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
    	  WebuiRequestContext pcontext = event.getRequestContext();
          UIApplication uiApp = pcontext.getUIApplication();
          UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
          UINavigationManagement uiManagement = uiNodeSelector.getParent();
          Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
          uiManagement.setRenderedChildrenOfTypes(childrenToRender);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

          PageNavigation nav = uiNodeSelector.getEdittedNavigation();
          if (nav == null)
          {
             return;
          }
          
          PageNode[] pageNodes = PageNavigationUtils.searchPageNodesByUri(nav, uri);
          if (pageNodes == null)
          {
             return;
          }
          
          for (PageNode pageNode : pageNodes) {
  			 if(pageNode != null && pageNode.isSystem()) {
  				 uiApp.addMessage(new ApplicationMessage("UINavigationNodeSelector.msg.systemnode-move", null));
  				 return;
  			 }
          }
          
          TreeNodeData selectedNode = new TreeNodeData(nav, pageNodes[0], pageNodes[1]);
          selectedNode.setDeleteNode(false);
          uiNodeSelector.setCopyNode(selectedNode);
          event.getSource().setActions(
             new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode", "CutNode",
                "PasteNode", "DeleteNode", "MoveUp", "MoveDown"});         

          if (uiNodeSelector.getCopyNode() == null)
          {
             return;
          }
          uiNodeSelector.getCopyNode().setDeleteNode(true);
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
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         String targetUri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIRightClickPopupMenu uiPopupMenu = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);
         UINavigationManagement uiManagement = uiNodeSelector.getParent();
         Class<?>[] childrenToRender = new Class<?>[]{UINavigationNodeSelector.class};
         uiManagement.setRenderedChildrenOfTypes(childrenToRender);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
         TreeNodeData selectedNode = uiNodeSelector.getCopyNode();
         if (selectedNode == null)
         {
            return;
         }

         PageNode newNode = selectedNode.getNode().clone();
         PageNavigation targetNav = uiNodeSelector.getEdittedNavigation();
         PageNode targetNode = PageNavigationUtils.searchPageNodeByUri(targetNav, targetUri);

         if (targetNode != null && newNode.getUri().equals(targetNode.getUri()))
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameSrcAndDes", null));
            return;
         }

         if (isExistChild(targetNode, newNode) || (targetNode == null && isExitChild(targetNav, newNode)))
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameName", null));
            return;
         }
         if (selectedNode.isDeleteNode())
         {
            if (selectedNode.getParentNode() != null)
            {
               selectedNode.getParentNode().getChildren().remove(selectedNode.getNode());
            }
            else
            {
               selectedNode.getPageNavigation().getNodes().remove(selectedNode.getNode());
            }
         }
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeSelector);
         uiNodeSelector.setCopyNode(null);
         UITree uitree = uiNodeSelector.getChild(UITree.class);
         UIRightClickPopupMenu popup = uitree.getUIRightClickPopupMenu();
         popup.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CutNode",
            "CloneNode", "DeleteNode", "MoveUp", "MoveDown"});

         UserPortalConfigService service = uiPopupMenu.getApplicationComponent(UserPortalConfigService.class);
         if (targetNode == null)
         {
            targetNav.addNode(newNode);
         }
         else
         {
            targetNode.getChildren().add(newNode);
            uiNodeSelector.selectPageNodeByUri(targetNode.getUri());
         }
         
         setNewUri(targetNode, newNode);
         if (selectedNode.isCloneNode())
         {
            clonePageFromNode(newNode, targetNav.getOwnerType(), targetNav.getOwnerId(), service);
         }
      }

      private void clonePageFromNode(PageNode node, String ownerType, String ownerId, UserPortalConfigService service)
         throws Exception
      {
         String pageId = node.getPageReference();
         if (pageId != null)
         {
            Page page = service.getPage(pageId);
            if (page != null)
            {
               String newName = "page" + node.hashCode();
               page = service.renewPage(pageId, newName, ownerType, ownerId);
               node.setPageReference(page.getPageId());
            }
         }
         List<PageNode> children = node.getChildren();
         if (children == null || children.size() < 1)
         {
            return;
         }
         for (PageNode ele : children)
         {
            clonePageFromNode(ele, ownerType, ownerId, service);
         }
      }

      private void setNewUri(PageNode parent, PageNode child)
      {
         String newUri = (parent != null) ? parent.getUri() + "/" + child.getName() : child.getName();
         child.setUri(newUri);
         List<PageNode> children = child.getChildren();
         if (children != null)
         {
            for (PageNode node : children)
            {
               setNewUri(child, node);
            }
         }
      }

      private boolean isExistChild(PageNode parent, PageNode child)
      {
         if (parent == null)
         {
            return false;
         }
         List<PageNode> nodes = parent.getChildren();
         if (nodes == null)
         {
            parent.setChildren(new ArrayList<PageNode>());
            return false;
         }
         for (PageNode node : nodes)
         {
            if (node.getName().equals(child.getName()))
            {
               return true;
            }
         }
         return false;
      }

      private boolean isExitChild(PageNavigation nav, PageNode child)
      {
         List<PageNode> nodes = nav.getNodes();
         if (nodes.size() == 0)
         {
            return false;
         }
         for (PageNode node : nodes)
         {
            if (node.getName().equals(child.getName()))
            {
               return true;
            }
         }
         return false;
      }
   }

   static public class MoveUpActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         moveNode(event, -1);
      }

      protected void moveNode(Event<UIRightClickPopupMenu> event, int i)
      {
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeSelector.getParent());
         PageNavigation nav = uiNodeSelector.getEdittedNavigation();
         PageNode targetNode = PageNavigationUtils.searchPageNodeByUri(nav, uri);
         Object parentNode = PageNavigationUtils.searchParentNode(nav, uri);
         List<PageNode> children = new ArrayList<PageNode>();
         if (parentNode instanceof PageNavigation)
         {
            children = ((PageNavigation)parentNode).getNodes();
         }
         else if (parentNode instanceof PageNode)
         {
            children = ((PageNode)parentNode).getChildren();
         }
         int k = children.indexOf(targetNode);
         if (k < 0)
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
         children.remove(k);
         children.add(k + i, targetNode);
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
         String uri = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         WebuiRequestContext pcontext = event.getRequestContext();
         UIApplication uiApp = pcontext.getUIApplication();
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         pcontext.addUIComponentToUpdateByAjax(uiNodeSelector);

         PageNavigation nav = uiNodeSelector.getEdittedNavigation();
         if (nav == null)
         {
            return;
         }

         ParentChildPair parentChildPair = PageNavigationUtils.searchParentChildPairByUri(nav, uri);
         if (parentChildPair == null)
         {
            return;
         }

         PageNode parentNode = parentChildPair.getParentNode();
         PageNode childNode = parentChildPair.getChildNode();
         
         if(childNode.isSystem()) {
        		uiApp.addMessage(new ApplicationMessage("UINavigationNodeSelector.msg.systemnode-delete", null));
        		return;
        	}
        	
         if(parentNode == null)
         {
            nav.getNodes().remove(childNode);
         }
         else
         {
            parentNode.getNodes().remove(childNode);
            uiNodeSelector.selectPageNodeByUri(parentNode.getUri());
         }
      }
   }

   public TreeNodeData getSelectedNode()
   {
      return edittedTreeNodeData;
   }

   public PageNavigation getSelectedNavigation()
   {
      return edittedTreeNodeData == null ? null : edittedTreeNodeData.getPageNavigation();
   }

   public PageNode getSelectedPageNode()
   {
      return edittedTreeNodeData == null ? null : edittedTreeNodeData.getNode();
   }

   public String getUpLevelUri()
   {
      return edittedTreeNodeData.getParentNode().getUri();
   }

   public List<PageNavigation> getDeleteNavigations()
   {
      return deleteNavigations;
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

      private PageNavigation nav;

      private PageNode parentNode;

      private PageNode node;

      private boolean deleteNode = false;

      private boolean cloneNode = false;

      public TreeNodeData(PageNavigation nav, PageNode parentNode, PageNode node)
      {
         this.nav = nav;
         this.parentNode = parentNode;
         this.node = node;
      }
      
      public TreeNodeData(PageNavigation nav)
      {
         this.nav = nav;
         List<PageNode> children = nav.getNodes();
         if(children != null && children.size() > 0)
         {
            this.node = children.get(0);
         }
      }

      public PageNavigation getPageNavigation()
      {
         return nav;
      }

      public void setPageNavigation(PageNavigation nav)
      {
         this.nav = nav;
      }

      public PageNode getParentNode()
      {
         return parentNode;
      }

      public void setParentNode(PageNode parentNode)
      {
         this.parentNode = parentNode;
      }

      public PageNode getNode()
      {
         return node;
      }

      public void setNode(PageNode node)
      {
         this.node = node;
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
   }

}

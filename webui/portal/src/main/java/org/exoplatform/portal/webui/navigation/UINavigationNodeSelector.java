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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeChange;
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
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

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
    * Init the UITree wrapped in UINavigationNodeSelector
    * @throws Exception
    */
   public void initTreeData() throws Exception
   {      
      if(edittedNavigation == null || userPortal == null)
      {
         throw new IllegalStateException("edittedNavigation and userPortal must be initialized first");
      }

      cachedNodes.clear();

      this.rootNode = new TreeNodeData(edittedNavigation, userPortal.getNode(edittedNavigation, NODE_SCOPE).filter(nodeFilter), this);
      addToCached(rootNode);
      if (rootNode.getChildren().size() > 0)
      {
         TreeNodeData firstNode = rootNode.getChild(0);
         if (updateNode(firstNode) == null) 
         {
            initTreeData();
         }
         else
         {          
            selectNode(firstNode);
         }
      }
      else
      {
         selectNode(rootNode);
      }
   }

   public TreeNodeData selectNode(TreeNodeData node) throws Exception
   {
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
         tree.setParentSelected(node);
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

   private TreeNodeData addToCached(TreeNodeData node)
   {
      if (node == null)
      {
         return null;
      }
      
      cachedNodes.put(node.getId(), node);
      if (node.hasChildrenRelationship())
      {
         for (TreeNodeData child : node.getChildren())
         {
            addToCached(child);
         }         
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
      if (currentCopy != null && currentCopy.getId().equals(node.getId()))
      {
         setCopyNode(null);
         UIRightClickPopupMenu popup = getChild(UITree.class).getUIRightClickPopupMenu();
         popup.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CutNode",
            "CloneNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
      
      cachedNodes.remove(node.getId());
      if (node.hasChildrenRelationship())
      {
         for (TreeNodeData child : node.getChildren())
         {
            removeFromCached(child);
         }         
      }
      return node;
   }

   public Iterator<NodeChange<UserNode>> updateNode(TreeNodeData treeNode) throws Exception
   {
      return updateNode(treeNode, NODE_SCOPE);
   }
   
   public Iterator<NodeChange<UserNode>> updateNode(TreeNodeData treeNode, Scope scope) throws Exception
   {
      if (treeNode == null || treeNode.getNode() == null)
      {
         return null;
      }

      UserNode userNode = treeNode.getNode();
      if (userNode.getId() == null)
      {
         //Transient node
         return Collections.<NodeChange<UserNode>>emptyList().iterator();
      }

      try 
      {
         boolean hasLoaded = treeNode.hasChildrenRelationship();
         Iterator<NodeChange<UserNode>> changes = userPortal.updateNode(userNode, scope);    
         if (changes.hasNext()) 
         {
            userNode.filter(nodeFilter);            
            while (changes.hasNext())
            {
               NodeChange<UserNode> ch = changes.next();
               if (ch instanceof NodeChange.Removed)
               {
                  removeFromCached(searchNode(ch.getNode().getId()));
               }
               else if (ch instanceof NodeChange.Added)
               {
                  addToCached(new TreeNodeData(edittedNavigation, ch.getNode(), this));
               } 
            }
            treeNode.setNode(userNode);
         }
         //In case node has never been loaded updated before --> no NodeChange returned
         //but the childrens should be added to cached
         if (!hasLoaded)
         {
            addToCached(treeNode);
         }
         return changes;
      } 
      catch (NullPointerException ex) 
      {
         //Node has been deleted
         removeFromCached(treeNode);
         while (treeNode.getParent() != null && updateNode(treeNode.getParent()) == null)
         {
            treeNode = treeNode.getParent();
         }
         return null;      
      }
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
      return rootNode;
   }

   public void setUserPortal(UserPortal userPortal) throws Exception
   {
      this.userPortal = userPortal;
      setNodeFilter(userPortal.createFilter(UserNodePredicate.builder().withAuthorizationCheck().build()));
   }

   private void setNodeFilter(NodeFilter nodeFilter)
   {
      this.nodeFilter = nodeFilter;
   }

   public void setEdittedNavigation(UserNavigation nav) throws Exception
   {
      this.edittedNavigation = nav;
   }

   public UserNavigation getEdittedNavigation()
   {
      return this.edittedNavigation;
   }

   static public class ChangeNodeActionListener extends EventListener<UITree>
   {
      public void execute(Event<UITree> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();         
         UINavigationNodeSelector uiNodeSelector = event.getSource().getParent();
         
         String nodeID = context.getRequestParameter(OBJECTID);
         TreeNodeData node = uiNodeSelector.searchNode(nodeID);
         boolean staleData = false;
         if (node == null)
         {
            staleData = true;
         } 
         else 
         {
            if ((node.getId().equals(uiNodeSelector.getSelectedNode().getId()) || !node.hasChildrenRelationship())) 
            {               
               try 
               {
                  if (uiNodeSelector.updateNode(node) == null)
                  {
                     staleData = true;
                     node = uiNodeSelector.getRootNode();
                  }
               } 
               catch (Exception ex)
               {
                  context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.select", null));
                  UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
                  popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
                  return;
               }       
            }
            uiNodeSelector.selectNode(node);
         }

         if (staleData)
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));
         }
         context.addUIComponentToUpdateByAjax(uiNodeSelector);
      }
   }

   static public abstract class BaseRightClickActionListener extends EventListener<UIRightClickPopupMenu>
   {
      protected boolean isStaleData(String nodeID, UINavigationNodeSelector selector) throws Exception
      {
         TreeNodeData node = selector.searchNode(nodeID);
         if (node == null) 
         {
            return true;
         }
         else 
         {
            if (selector.updateNode(node) == null)
            {
               return true;                  
            }
         }
         return false;
      }
   }
   
   static public class AddNodeActionListener extends BaseRightClickActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext(); 
         UIRightClickPopupMenu uiPopupMenu = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         String nodeID = context.getRequestParameter(UIComponent.OBJECTID);
         try 
         {
            if (isStaleData(nodeID, uiNodeSelector))
            {
               uiNodeSelector.selectNode(uiNodeSelector.getRootNode());
               context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));
               context.addUIComponentToUpdateByAjax(uiNodeSelector);
               return;
            }                     
         } 
         catch (Exception ex)
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.add", null));
            UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
            popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
            return;
         }
                  
         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm uiNodeForm = uiManagementPopup.createUIComponent(UIPageNodeForm.class, null, null);
         uiNodeForm.setValues(null);
         uiManagementPopup.setUIComponent(uiNodeForm);

         uiNodeForm.setSelectedParent(uiNodeSelector.searchNode(nodeID));
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

   static public class EditSelectedNodeActionListener extends BaseRightClickActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIRightClickPopupMenu popupMenu = event.getSource();
         UINavigationNodeSelector uiNodeSelector = popupMenu.getAncestorOfType(UINavigationNodeSelector.class);

         String nodeID = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         try
         {
            if (isStaleData(nodeID, uiNodeSelector))
            {
               uiNodeSelector.selectNode(uiNodeSelector.getRootNode());
               context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));
               context.addUIComponentToUpdateByAjax(uiNodeSelector);
               return;
            }              
         }
         catch (Exception ex)
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.edit", null));
            UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
            popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
            return;
         }

         UIApplication uiApp = context.getUIApplication();
         UserPortalConfigService service = uiApp.getApplicationComponent(UserPortalConfigService.class);
         TreeNodeData node = uiNodeSelector.searchNode(nodeID);
         String pageId = node.getPageRef();
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

         UserNavigation edittedNav = uiNodeSelector.getEdittedNavigation();
         uiNodeForm.setContextPageNavigation(edittedNav);
         uiNodeForm.setValues(node);
         uiNodeForm.setSelectedParent(node.getParent());
         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }
   }

   static public class CopyNodeActionListener extends EventListener<UIRightClickPopupMenu>
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext(); 
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeSelector);

         String nodeID = context.getRequestParameter(UIComponent.OBJECTID);
         TreeNodeData node = uiNodeSelector.searchNode(nodeID);
         try 
         {            
            if (node == null || uiNodeSelector.updateNode(node, Scope.ALL) == null)
            {
               uiNodeSelector.selectNode(uiNodeSelector.getRootNode());
               context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));
               return;
            }    
         } 
         catch (Exception e) 
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.copy", null));
            UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
            popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
            return;
         }
         
         node.setDeleteNode(false);
         uiNodeSelector.setCopyNode(node);
         event.getSource().setActions(
            new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CloneNode", "CutNode",
               "PasteNode", "DeleteNode", "MoveUp", "MoveDown"});
      }
   }

   static public class CutNodeActionListener extends CopyNodeActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext(); 
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         context.addUIComponentToUpdateByAjax(uiNodeSelector);
         
         String nodeID = context.getRequestParameter(UIComponent.OBJECTID);
         TreeNodeData node = uiNodeSelector.searchNode(nodeID);         
         if(node != null && Visibility.SYSTEM.equals(node.getVisibility()))
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationNodeSelector.msg.systemnode-move", null));
            return;
         }         
         super.execute(event);         
         node.setDeleteNode(true);
      }
   }

   static public class CloneNodeActionListener extends CopyNodeActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         super.execute(event);
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         uiNodeSelector.getCopyNode().setCloneNode(true);
      }
   }

   static public class PasteNodeActionListener extends BaseRightClickActionListener
   {
      private UINavigationNodeSelector uiNodeSelector;
      private DataStorage dataStorage;
      private UserPortalConfigService service;

      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext(); 
         UIRightClickPopupMenu uiPopupMenu = event.getSource();
         uiNodeSelector = uiPopupMenu.getAncestorOfType(UINavigationNodeSelector.class);
         context.addUIComponentToUpdateByAjax(uiNodeSelector);

         String nodeID = context.getRequestParameter(UIComponent.OBJECTID);
         TreeNodeData targetNode = uiNodeSelector.searchNode(nodeID);
         TreeNodeData sourceNode = uiNodeSelector.getCopyNode();
         if (sourceNode == null) return;
         
         try 
         {
            if (isStaleData(nodeID, uiNodeSelector))
            {
               uiNodeSelector.selectNode(uiNodeSelector.getRootNode());
               context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));            
               return;
            }               
         }
         catch (Exception ex)
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.paste", null));
            UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
            popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
            return;
         }
         
         if (targetNode != null && sourceNode.getId().equals(targetNode.getId()))
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameSrcAndDes", null));
            return;
         }

         if (isExistChild(targetNode, sourceNode))
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UIPageNodeSelector.msg.paste.sameName", null));
            return;
         }
         
         UITree uitree = uiNodeSelector.getChild(UITree.class);
         UIRightClickPopupMenu popup = uitree.getUIRightClickPopupMenu();
         popup.setActions(new String[]{"AddNode", "EditPageNode", "EditSelectedNode", "CopyNode", "CutNode",
            "CloneNode", "DeleteNode", "MoveUp", "MoveDown"});

         if (sourceNode.isDeleteNode())
         {
            sourceNode.getParent().removeChild(sourceNode);
         }
         uiNodeSelector.setCopyNode(null);
         
         service = uiNodeSelector.getApplicationComponent(UserPortalConfigService.class);
         dataStorage = uiNodeSelector.getApplicationComponent(DataStorage.class);

         pasteNode(sourceNode, targetNode, sourceNode.isCloneNode());
         uiNodeSelector.selectNode(targetNode);
      }

      private TreeNodeData pasteNode(TreeNodeData sourceNode, TreeNodeData parent, boolean isClone) throws Exception
      {
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

   static public class MoveUpActionListener extends BaseRightClickActionListener
   {
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         moveNode(event, -1);
      }

      protected void moveNode(Event<UIRightClickPopupMenu> event, int i) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         context.addUIComponentToUpdateByAjax(uiNodeSelector.getParent());
       
         String nodeID = context.getRequestParameter(UIComponent.OBJECTID);
         TreeNodeData targetNode = uiNodeSelector.searchNode(nodeID);
         TreeNodeData parentNode = targetNode.getParent();
         try 
         {
            if (isStaleData(parentNode.getId(), uiNodeSelector) || uiNodeSelector.searchNode(nodeID) == null)
            {
               uiNodeSelector.selectNode(uiNodeSelector.getRootNode());
               context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.staleData", null));
               context.addUIComponentToUpdateByAjax(uiNodeSelector);
               return;
            }                       
         }
         catch (Exception ex)
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UINavigationManagement.msg.fail.move", null));
            UIPopupWindow popup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
            popup.createEvent("ClosePopup", Phase.PROCESS, context).broadcast();
            return;
         }
         
         Collection<TreeNodeData> children = parentNode.getChildren();

         int k;
         for (k = 0; k < children.size(); k++)
         {
            if (parentNode.getChild(k).getId().equals(targetNode.getId()))
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
         WebuiRequestContext pcontext = event.getRequestContext();         
         UINavigationNodeSelector uiNodeSelector = event.getSource().getAncestorOfType(UINavigationNodeSelector.class);
         pcontext.addUIComponentToUpdateByAjax(uiNodeSelector);

         String nodeID = pcontext.getRequestParameter(UIComponent.OBJECTID);
         TreeNodeData childNode = uiNodeSelector.searchNode(nodeID);
         if (childNode == null)
         {
            return;
         }
         TreeNodeData parentNode = childNode.getParent();

         if(Visibility.SYSTEM.equals(childNode.getVisibility())) 
         {
            UIApplication uiApp = pcontext.getUIApplication();
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
      
      private String id;

      private List<TreeNodeData> wrappedChilds;

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

      public void setNode(UserNode node)
      {
         if (node == null)
         {
            throw new IllegalArgumentException("node can't be null");
         }
         wrappedChilds = null;
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

      public List<TreeNodeData> getChildren()
      {
         if (wrappedChilds == null)
         {
            wrappedChilds = new ArrayList<TreeNodeData>();
            for (UserNode child : node.getChildren())
            {
               String key = child.getId() == null ? String.valueOf(child.hashCode()) : child.getId();
               TreeNodeData node = selector.searchNode(key);
               //This is for the first time a node is loaded
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
         return selector.searchNode(child.getId());
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
         return selector.searchNode(node.getParent().getId());
      }

      public String getPageRef()
      {
         return node.getPageRef();
      }

      public String getId()
      {
         if (this.id == null)
         {
            this.id = node.getId() == null ? String.valueOf(node.hashCode()) : node.getId();
         }
         return this.id;
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
         return selector.searchNode(child.getId());
      }

      public TreeNodeData addChild(String childName)
      {
         wrappedChilds = null;
         UserNode child = node.addChild(childName);
         return selector.addToCached(new TreeNodeData(nav, child, selector));
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

      public void save() throws NavigationServiceException
      {
         selector.cachedNodes.clear();
         node.save();
      }
   }
}

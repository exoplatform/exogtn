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

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodePredicate;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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
   private static final Scope TOOLBAR_GROUP_SCOPE = Scope.GRANDCHILDREN;

   public UIUserToolBarGroupPortlet() throws Exception
   {                  
      UserNodePredicate.Builder scopeBuilder = UserNodePredicate.builder();
      scopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      scopeBuilder.withTemporalCheck();
      TOOLBAR_GROUP_FILTER = getUserPortal().createFilter(scopeBuilder.build());
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
      UserNode rootNodes =  userPortal.getNode(groupNav, TOOLBAR_GROUP_SCOPE);
      if (rootNodes != null)
      {
         rootNodes.filter(TOOLBAR_GROUP_FILTER);
         return rootNodes.getChildren();
      }
      return Collections.emptyList();
   }

   public UserNode getSelectedNode() throws Exception
   {
      return Util.getUIPortal().getNavPath().getTarget();
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
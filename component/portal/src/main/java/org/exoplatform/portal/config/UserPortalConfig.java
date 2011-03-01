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

package org.exoplatform.portal.config;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Navigation;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.services.organization.Group;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserPortalConfig
{

   private PortalConfig portal;

   private List<PageNavigation> navigations;

   private final UserPortalConfigService service;

   private final String portalName;

   private final String accessUser;

   /** Added by Minh Hoang TO */
   private PageNavigation selectedNavigation;

   public UserPortalConfig()
   {
      this.portal = null;
      this.navigations = new ArrayList<PageNavigation>();
      this.service = null;
      this.portalName = null;
      this.accessUser = null;
   }

   public UserPortalConfig(PortalConfig portal, UserPortalConfigService service, String portalName, String accessUser)
   {
      this.portal = portal;
      this.navigations = null;
      this.service = service;
      this.portalName = portalName;
      this.accessUser = accessUser;
   }

   public PortalConfig getPortalConfig()
   {
      return portal;
   }

   public void setPortal(PortalConfig portal)
   {
      this.portal = portal;
   }
   
   public void setSelectedNavigation(PageNavigation _selectedNavigation)
   {
      this.selectedNavigation = _selectedNavigation;
   }

   /** Fetch navigation (specified by ownerType, ownerId) from the list of all navigations and set it as selected navigation **/
   public void updateSelectedNavigation(String ownerType, String ownerId)
   {
      PageNavigation targetNavigation = null;
      for (PageNavigation nav : getNavigations())
      {
         if (nav.getOwnerType().equals(ownerType) && nav.getOwnerId().equals(ownerId))
         {
            targetNavigation = nav;
            break;
         }
      }

      if (targetNavigation != null)
      {
         this.setSelectedNavigation(targetNavigation);
      }
   }

   private List<Navigation> navigations2;

   /**
    * Returns an immutable sorted list of the valid navigations related to the user.
    *
    * @return the navigations
    * @throws Exception any exception
    */
   public List<Navigation> getNavigations2() throws Exception
   {
      if (navigations2 == null)
      {
         List<Navigation> navigations = new ArrayList<Navigation>(accessUser == null ? 1 : 10);
         navigations.add(service.navService.getNavigation(new SiteKey(SiteType.PORTAL, portalName)));
         if (accessUser != null)
         {
            // Add user nav if any
            Navigation userNav = service.navService.getNavigation(SiteKey.user(accessUser));
            if (userNav != null)
            {
//               navigation.setModifiable(true);
               navigations.add(userNav);
            }

            //
            Collection<?> groups;
            if (service.userACL_.getSuperUser().equals(accessUser))
            {
               groups = service.orgService_.getGroupHandler().getAllGroups();
            }
            else
            {
               groups = service.orgService_.getGroupHandler().findGroupsOfUser(accessUser);
            }
            for (Object group : groups)
            {
               Group m = (Group)group;
               String groupId = m.getId().trim();
               if (groupId.equals(service.userACL_.getGuestsGroup()))
               {
                  continue;
               }
               Navigation navigation = service.navService.getNavigation(SiteKey.group(groupId));
               if (navigation == null || navigation.getNodeId() == null)
               {
                  continue;
               }
//               navigation.setModifiable(service.userACL_.hasEditPermission(navigation));
               navigations.add(navigation);
            }

            // Sort the list finally
            Collections.sort(navigations, new Comparator<Navigation>()
            {
               public int compare(Navigation nav1, Navigation nav2)
               {
                  return nav1.getPriority() - nav2.getPriority();
               }
            });
         }

         //
         this.navigations2 = Collections.unmodifiableList(navigations);
      }
      return navigations2;
   }

   public UserNavigation resolveNavigation(String path) throws Exception
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }

      // Get navigations
      List<Navigation> navs = getNavigations2();

      // Split into segments
      if (path.length() > 0 && path.charAt(0) == '/')
      {
         path = path.substring(1);
      }
      final String[] segments = path.split("/");

      //
      class ScoringScope implements Scope
      {
         final Navigation navigation;
         int score;
         List<Node.Data> path;
         ScoringScope(Navigation navigation)
         {
            this.navigation = navigation;
         }
         public Visitor get()
         {
            return new Visitor()
            {
               public VisitMode visit(int depth, String nodeId, String nodeName, Node.Data nodeData)
               {
                  if (depth == 0 && "default".equals(nodeName))
                  {
                     score = 0;
                     path = new ArrayList<Node.Data>();
                     return VisitMode.CHILDREN;
                  }
                  else if (depth <= segments.length && nodeName.equals(segments[depth - 1]))
                  {
                     score++;
                     path.add(nodeData);
                     return VisitMode.CHILDREN;
                  }
                  else
                  {
                     return VisitMode.NODE;
                  }
               }
            };
         }
      }

      //
      ScoringScope best = null;
      for (Navigation nav : navs)
      {
         ScoringScope scope = new ScoringScope(nav);
         service.navService.load(nav.getNodeId(), scope);
         if (scope.score == segments.length)
         {
            best = scope;
            break;
         }
         else
         {
            if (best == null)
            {
               best = scope;
            }
            else
            {
               if (scope.score > best.score)
               {
                  best = scope;
               }
            }
         }
      }

      //
      return new UserNavigation(best.navigation,  best.path);
   }
   
   public PageNavigation getSelectedNavigation()
   {
      if(this.selectedNavigation != null)
      {
         return this.selectedNavigation;
      }
      return getNavigations().get(0);
   }
   
   public void setNavigations(List<PageNavigation> navs)
   {
      navigations = navs;
   }

   public List<PageNavigation> getNavigations()
   {

      if (navigations == null)
      {
         try
         {
            List<PageNavigation> navigations = new ArrayList<PageNavigation>();
            PageNavigation navigation = service.storage_.getPageNavigation(PortalConfig.PORTAL_TYPE, portalName);
            if (navigation != null)
            {
               navigation.setModifiable(service.userACL_.hasPermission(portal.getEditPermission()));
               navigations.add(navigation);
            }

            if (accessUser == null)
            {
               // navigation = getPageNavigation(PortalConfig.GROUP_TYPE,
               // userACL_.getGuestsGroup());
               // if (navigation != null)
               // navigations.add(navigation);
            }
            else
            {
               navigation = service.storage_.getPageNavigation(PortalConfig.USER_TYPE, accessUser);
               if (navigation != null)
               {
                  navigation.setModifiable(true);
                  navigations.add(navigation);
               }

               Collection<?> groups = null;
               if (service.userACL_.getSuperUser().equals(accessUser))
               {
                  groups = service.orgService_.getGroupHandler().getAllGroups();
               }
               else
               {
                  groups = service.orgService_.getGroupHandler().findGroupsOfUser(accessUser);
               }
               for (Object group : groups)
               {
                  Group m = (Group)group;
                  String groupId = m.getId().trim();
                  if (groupId.equals(service.userACL_.getGuestsGroup()))
                  {
                     continue;
                  }
                  navigation = service.storage_.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
                  if (navigation == null)
                  {
                     continue;
                  }
                  navigation.setModifiable(service.userACL_.hasEditPermission(navigation));
                  navigations.add(navigation);
               }
            }
            Collections.sort(navigations, new Comparator<PageNavigation>()
            {
               public int compare(PageNavigation nav1, PageNavigation nav2)
               {
                  return nav1.getPriority() - nav2.getPriority();
               }
            });

            //
            this.navigations = navigations;
         }
         catch (Exception e)
         {
            throw new UndeclaredThrowableException(e);
         }
      }

      return navigations;
   }

   public void addNavigation(PageNavigation nav)
   {
      if (service == null)
      {
         if (navigations == null)
            navigations = new ArrayList<PageNavigation>();
         if (nav == null)
            return;
      }
      else
      {
         // Ensure navs are loaded
         getNavigations();
      }
      navigations.add(nav);
   }
}
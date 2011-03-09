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
import org.exoplatform.portal.mop.user.BundleResolver;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.mop.user.UserPortalImpl;
import org.exoplatform.services.organization.Group;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserPortalConfig
{

   PortalConfig portal;

   private List<PageNavigation> navigations;

   final UserPortalConfigService service;

   final String portalName;

   final String accessUser;

   /** Added by Minh Hoang TO */
   private PageNavigation selectedNavigation;

   /** . */
   private UserPortalImpl userPortal;

   /** . */
   private BundleResolver bundleResolver;

   public UserPortalConfig()
   {
      this.portal = null;
      this.navigations = new ArrayList<PageNavigation>();
      this.service = null;
      this.portalName = null;
      this.accessUser = null;
      this.bundleResolver = null;
   }

   public UserPortalConfig(PortalConfig portal, UserPortalConfigService service, String portalName, String accessUser, BundleResolver bundleResolver)
   {
      this.portal = portal;
      this.navigations = null;
      this.service = service;
      this.portalName = portalName;
      this.accessUser = accessUser;
      this.bundleResolver = bundleResolver;
   }

   public UserPortal getUserPortal()
   {
         userPortal = new UserPortalImpl(
            service,
            service.navService,
            service.orgService_,
            service.userACL_,
            portalName,
            portal,
            accessUser,
            bundleResolver
         );
      return userPortal;
   }

   public PortalConfig getPortalConfig()
   {
      return portal;
   }

   public void setPortal(PortalConfig portal)
   {
      this.portal = portal;
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
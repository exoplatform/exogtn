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

package org.exoplatform.organization.webui.component;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Huu-Dung Kieu	
 *          kieuhdung@gmail.com
 * 22 déc. 08  
 */
public class GroupManagement
{

   public static OrganizationService getOrganizationService()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      OrganizationService orgService =
         (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      return orgService;
   }

   public static UserACL getUserACL()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL)container.getComponentInstanceOfType(UserACL.class);
      return acl;
   }

   private static boolean isMembershipOfGroup(String membership, String groupId) throws Exception
   {
      boolean ret = false;
      String username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
      OrganizationService orgService = getOrganizationService();
      Collection groups = orgService.getGroupHandler().findGroupByMembership(username, membership);
      for (Object group : groups)
      {
         if (((Group)group).getId().equals(groupId))
         {
            ret = true;
            break;
         }
      }
      return ret;
   }

   public static boolean isManagerOfGroup(String groupId) throws Exception
   {
      return isMembershipOfGroup(getUserACL().getAdminMSType(), groupId);
   }

   private static boolean isMemberOfGroup(String username, String groupId) throws Exception
   {
      boolean ret = false;
      OrganizationService orgService = getOrganizationService();
      Collection groups = orgService.getGroupHandler().findGroupsOfUser(username);
      for (Object group : groups)
      {
         if (((Group)group).getId().equals(groupId))
         {
            ret = true;
            break;
         }
      }
      return ret;
   }

   /**
    * @deprecated Use {@link UserACL#isUserInGroup(String)} instead
    */
   public static boolean isRelatedOfGroup(String username, String groupId) throws Exception
   {
      throw new UnsupportedOperationException("This method is not supported anymore");
   }

   public static Collection getRelatedGroups(Collection groups) throws Exception
   {
      String username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
      List relatedGroups = new ArrayList();
      OrganizationService orgService = getOrganizationService();
      Collection userGroups = orgService.getGroupHandler().findGroupsOfUser(username);
      for (Object group : groups)
      {
         if (isRelatedGroup((Group)group, userGroups))
            relatedGroups.add(group);
      }
      return relatedGroups;
   }

   private static boolean isRelatedGroup(Group group, Collection groups)
   {
      boolean ret = false;
      String groupId = group.getId();
      for (Object g : groups)
      {
         if (((Group)g).getId().startsWith(groupId))
         {
            ret = true;
            break;
         }
      }
      return ret;
   }

   public static boolean isAdministrator() throws Exception
   {
      String username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
      if (getUserACL().getSuperUser().equals(username))
      {
         return true;
      }
      return isMemberOfGroup(username, getUserACL().getAdminGroups());
   }

   public static boolean isSuperUserOfGroup(String groupId)
   {
      try
      {
         //        return false;
         // 2nd the selected group must be a normal group
         //      if (isPlatformAdminGroup(groupId) || isPlatformUsersGroup(groupId))
         //        return false;
         //      
         boolean ret =
            (GroupManagement.isManagerOfGroup(groupId) || (GroupManagement.isAdministrator()));
         // finally, user must be manager of that group
         return ret;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return false;
   }
}

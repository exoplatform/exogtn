/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Navigation;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.portal.mop.user.NavigationPath;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.organization.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserPortalImpl implements UserPortal
{

   /** . */
   private final UserPortalConfig config;

   /** . */
   private List<UserNavigation> navigations;

   public UserPortalImpl(UserPortalConfig config)
   {
      this.config = config;
   }

   /**
    * Returns an immutable sorted list of the valid navigations related to the user.
    *
    * @return the navigations
    * @throws Exception any exception
    */
   public List<UserNavigation> getNavigations() throws Exception
   {
      if (navigations == null)
      {
         List<UserNavigation> navigations = new ArrayList<UserNavigation>(config.accessUser == null ? 1 : 10);
         navigations.add(new UserNavigation(
            config.service.navService.getNavigation(new SiteKey(SiteType.PORTAL, config.portalName)),
            config.service.userACL_.hasPermission(config.portal.getEditPermission())));
         if (config.accessUser != null)
         {
            // Add user nav if any
            Navigation userNav = config.service.navService.getNavigation(SiteKey.user(config.accessUser));
            if (userNav != null)
            {
               navigations.add(new UserNavigation(userNav, true));
            }

            //
            Collection<?> groups;
            if (config.service.userACL_.getSuperUser().equals(config.accessUser))
            {
               groups = config.service.orgService_.getGroupHandler().getAllGroups();
            }
            else
            {
               groups = config.service.orgService_.getGroupHandler().findGroupsOfUser(config.accessUser);
            }
            for (Object group : groups)
            {
               Group m = (Group)group;
               String groupId = m.getId().trim();
               if (groupId.equals(config.service.userACL_.getGuestsGroup()))
               {
                  continue;
               }
               Navigation navigation = config.service.navService.getNavigation(SiteKey.group(groupId));
               if (navigation == null || navigation.getNodeId() == null)
               {
                  continue;
               }
//               navigations.add(new UserNavigation(navigation, config.service.userACL_.hasEditPermission(navigation)));
               // julien : todo need to adapt user acl
               navigations.add(new UserNavigation(navigation, false));
            }

            // Sort the list finally
            Collections.sort(navigations, new Comparator<UserNavigation>()
            {
               public int compare(UserNavigation nav1, UserNavigation nav2)
               {
                  return nav1.getNavigation().getPriority() - nav2.getNavigation().getPriority();
               }
            });
         }

         //
         this.navigations = Collections.unmodifiableList(navigations);
      }
      return navigations;
   }

   public UserNavigation getNavigation(SiteKey key) throws Exception
   {
      for (UserNavigation navigation : getNavigations())
      {
         if (navigation.getNavigation().getKey().equals(key))
         {
            return navigation;
         }
      }

      //
      return null;
   }

   private static class MatchingScope implements Scope
   {
      final UserNavigation navigation;
      final String[] match;
      int score;
      List<UserNode> path;
      MatchingScope(UserNavigation navigation, String[] match)
      {
         this.navigation = navigation;
         this.match = match;
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
                  path = new ArrayList<UserNode>();
                  return VisitMode.CHILDREN;
               }
               else if (depth <= match.length && nodeName.equals(match[depth - 1]))
               {
                  score++;
                  path.add(new UserNode(nodeData));
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

   public NavigationPath resolvePath(String path) throws Exception
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }

      // Get navigations
      List<UserNavigation> navigations = getNavigations();

      // Split into segments
      if (path.length() > 0 && path.charAt(0) == '/')
      {
         path = path.substring(1);
      }
      final String[] segments = path.split("/");

      //
      MatchingScope best = null;
      for (UserNavigation navigation : navigations)
      {
         MatchingScope scope = new MatchingScope(navigation, segments);
         config.service.navService.load(navigation.getNavigation().getNodeId(), scope);
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
      return new NavigationPath(best.navigation,  best.path);
   }

   public NavigationPath resolvePath(UserNavigation navigation, String path) throws Exception
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }

      //
      if (path.length() > 0 && path.charAt(0) == '/')
      {
         path = path.substring(1);
      }
      if (path.length() == 0)
      {
         return null;
      }
      final String[] segments = path.split("/");

      //

      //
      MatchingScope scope = new MatchingScope(navigation, segments);
      config.service.navService.load(navigation.getNavigation().getNodeId(), scope);

      //
      return new NavigationPath(scope.navigation,  scope.path);
   }
}

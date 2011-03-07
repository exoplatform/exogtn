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

package org.exoplatform.portal.mop.user;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationData;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

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
   final NavigationService navigationService;
   
   /** . */
   private final OrganizationService organizationService;

   /** . */
   private final UserACL acl;
   
   /** . */
   private final PortalConfig portal;

   /** . */
   final BundleResolver bundleResolver;

   /** . */
   private final String userName;

   /** . */
   private List<UserNavigation> navigations;

   private final String portalName;

   public UserPortalImpl(
      NavigationService navigationService,
      OrganizationService organizationService,
      UserACL acl,
      String portalName,
      PortalConfig portal,
      String userName,
      BundleResolver bundleResolver)
   {
      // So we don't care about testing nullity
      if (bundleResolver == null)
      {
         bundleResolver = BundleResolver.NULL_RESOLVER;
      }

      //
      this.navigationService = navigationService;
      this.organizationService = organizationService;
      this.acl = acl;
      this.portalName = portalName;
      this.portal = portal;
      this.userName = userName;
      this.bundleResolver = bundleResolver;
      this.navigations = null;
   }

   //

   public BundleResolver getBundleResolver()
   {
      return bundleResolver;
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
         List<UserNavigation> navigations = new ArrayList<UserNavigation>(userName == null ? 1 : 10);
         NavigationData portalNav = navigationService.getNavigation(new SiteKey(SiteType.PORTAL, portalName));
         navigations.add(new UserNavigation(
            this,
            portalNav,
            acl.hasEditPermissionOnNavigation(portalNav.getKey())));
         if (userName != null)
         {
            // Add user nav if any
            NavigationData userNav = navigationService.getNavigation(SiteKey.user(userName));
            if (userNav != null)
            {
               navigations.add(new UserNavigation(this, userNav, true));
            }

            //
            Collection<?> groups;
            if (acl.getSuperUser().equals(userName))
            {
               groups = organizationService.getGroupHandler().getAllGroups();
            }
            else
            {
               groups = organizationService.getGroupHandler().findGroupsOfUser(userName);
            }
            for (Object group : groups)
            {
               Group m = (Group)group;
               String groupId = m.getId().trim();
               if (groupId.equals(acl.getGuestsGroup()))
               {
                  continue;
               }
               NavigationData navigation = navigationService.getNavigation(SiteKey.group(groupId));
               if (navigation == null || navigation.getNodeId() == null)
               {
                  continue;
               }
               navigations.add(new UserNavigation(
                  this,
                  navigation,
                  acl.hasEditPermissionOnNavigation(navigation.getKey())));
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

   public UserNode getNode(UserNavigation navigation, Scope scope) throws Exception
   {
      return navigationService.load(navigation.model, navigation.getNavigation(), scope);
   }

   public UserNode getNode(UserNode node, Scope scope) throws Exception
   {
      UserNavigation navigation = node.navigation;
      return navigationService.load(navigation.model, node, scope);
   }

   private class MatchingScope implements Scope
   {
      final UserNavigation navigation;
      final String[] match;
      int score;
      String id;
      UserNode userNode;
      private NavigationPath path;

      MatchingScope(UserNavigation navigation, String[] match)
      {
         this.navigation = navigation;
         this.match = match;
      }

      void resolve()
      {
         UserNode node = navigationService.load(navigation.model, navigation.getNavigation(), this);
         if (score > 0)
         {
            userNode = node.find(id);
            path = new NavigationPath(navigation, userNode);
         }
         else
         {
            path = new NavigationPath(navigation, null);
         }
      }

      public Visitor get()
      {
         return new Visitor()
         {
            public VisitMode visit(int depth, String id, String name, NodeState state)
            {
               if (depth == 0 && "default".equals(name))
               {
                  score = 0;
                  MatchingScope.this.id = null;
                  return VisitMode.ALL_CHILDREN;
               }
               else if (depth <= match.length && name.equals(match[depth - 1]))
               {
                  score++;
                  MatchingScope.this.id = id;
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
         };
      }
   }

   public NavigationPath getDefaultPath() throws Exception
   {
      for (UserNavigation userNavigation : getNavigations())
      {
         NavigationData navigation = userNavigation.getNavigation();
         if (navigation.getNodeId() != null)
         {
            UserNode root = navigationService.load(userNavigation.model, navigation, Scope.CHILDREN);
            for (UserNode node : root.getChildren())
            {
               return new NavigationPath(userNavigation, node);
            }
         }
      }

      //
      return null;
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

      // Find the first navigation available or return null
      if (path.length() == 0)
      {
         return getDefaultPath();
      }

      //
      MatchingScope best = null;
      for (UserNavigation navigation : navigations)
      {
         MatchingScope scope = new MatchingScope(navigation, segments);
         scope.resolve();
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
      if (best != null && best.score > 0)
      {
         return new NavigationPath(best.navigation,  best.userNode);
      }
      else
      {
         return getDefaultPath();
      }
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
      scope.resolve();

      //
      if (scope.score > 0)
      {
         return scope.path;
      }
      else
      {
         return null;
      }
   }
}

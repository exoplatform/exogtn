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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.gatein.common.util.EmptyResourceBundle;

import java.util.ResourceBundle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNavigation
{

   /** . */
   final UserPortalImpl portal;

   /** . */
   final NavigationContext navigation;

   /** . */
   private final boolean modifiable;

   /** . */
   ResourceBundle bundle;

   /** . */
   final NodeModel<UserNode> model = new NodeModel<UserNode>()
   {
      public NodeContext<UserNode> getContext(UserNode node)
      {
         return node.context;
      }
      public UserNode create(NodeContext<UserNode> context)
      {
         if (bundle == null)
         {
            bundle = portal.bundleResolver.getBundle(UserNavigation.this);
            if (bundle == null)
            {
               bundle = EmptyResourceBundle.INSTANCE;
            }
         }
         return new UserNode(UserNavigation.this, context);
      }
   };

   UserNavigation(UserPortalImpl portal, NavigationContext navigation, boolean modifiable)
   {
      if (navigation == null)
      {
         throw new NullPointerException();
      }
      if (navigation.getState() == null)
      {
         throw new IllegalArgumentException("No state for navigation " + navigation.getKey());
      }

      //
      this.portal = portal;
      this.navigation = navigation;
      this.modifiable = modifiable;
   }

   public SiteKey getKey()
   {
      return navigation.getKey();
   }

   public int getPriority()
   {
      Integer priority = navigation.getState().getPriority();
      return priority != null ? priority : 1;
   }
   
   public NavigationState getState()
   {
      return navigation.getState();
   }

   public boolean isModifiable()
   {
      return modifiable;
   }
}

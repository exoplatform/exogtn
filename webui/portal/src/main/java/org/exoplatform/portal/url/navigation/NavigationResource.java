/**
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
package org.exoplatform.portal.url.navigation;

import org.exoplatform.portal.mop.user.UserNode;

/**
 * A class that contains combination of a portal name and a page node
 * to determine the target URL
 * 
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class NavigationResource
{
   /** . */
   private final String access;
   
   /** . */
   private final String siteType;
   
   /** . */
   private final String siteName;
   
   /** . */
   private final UserNode pageNode;
   
   public NavigationResource(String siteName)
   {
      this(null, siteName, null);
   }
   
   public NavigationResource(UserNode node)
   {
      this(null, null, node);
   }
   
   public NavigationResource(String siteType, String siteName, UserNode node)
   {
      this(null, siteType, siteName, node);
   }

   public NavigationResource(String access, String siteType, String portalName, UserNode node)
   {
      this.access = access;
      this.siteType = siteType;
      this.siteName = portalName;
      this.pageNode = node;
   }

   public String getAccess()
   {
      return access;
   }
   
   public String getSiteType()
   {
      return siteType;
   }
   
   public String getSiteName()
   {
      return siteName;
   }

   public UserNode getUserNode()
   {
      return pageNode;
   }
}

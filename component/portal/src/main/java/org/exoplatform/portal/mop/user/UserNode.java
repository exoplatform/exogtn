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

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.gatein.common.text.EntityEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A navigation node as seen by a user.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNode
{

   /** . */
   final UserNavigation navigation;

   /** . */
   final NodeData data;

   /** . */
   private final ResourceBundle bundle;

   /** . */
   private String resolvedLabel;

   /** . */
   private String encodedResolvedLabel;

   /** . */
   Map<String, UserNode> childMap;

   /** . */
   UserNode parent;

   UserNode(UserNavigation navigation, NodeData data)
   {
      this(navigation, data, null);
   }

   UserNode(UserNavigation navigation, NodeData data, ResourceBundle bundle)
   {
      this.navigation = navigation;
      this.childMap = null;
      this.parent = null;
      this.data = data;
      this.resolvedLabel = null;
      this.encodedResolvedLabel = null;
      this.bundle = bundle;
   }

   public String getId()
   {
      return data.getId();
   }

   public String getName()
   {
      return data.getName();
   }

   public String getURI()
   {
      return data.getURI();
   }

   public String getLabel()
   {
      return data.getLabel();
   }

   public String getIcon()
   {
      return data.getIcon();
   }

   public long getStartPublicationTime()
   {
      return data.getStartPublicationTime();
   }

   public long getEndPublicationTime()
   {
      return data.getEndPublicationTime();
   }

   public Visibility getVisibility()
   {
      return data.getVisibility();
   }

   public String getPageRef()
   {
      return data.getPageRef();
   }

   public NodeData getData()
   {
      return data;
   }

   public String getResolvedLabel()
   {
      if (resolvedLabel == null)
      {
         String resolvedLabel;
         if (bundle != null && data.getLabel() != null)
         {
            resolvedLabel = ExpressionUtil.getExpressionValue(bundle, data.getLabel());
         }
         else
         {
            resolvedLabel = null;
         }

         //
         if (resolvedLabel == null)
         {
            resolvedLabel = data.getName();
         }

         //
         this.resolvedLabel = resolvedLabel;
      }
      return resolvedLabel;
   }

   public String getEncodedResolvedLabel()
   {
      if (encodedResolvedLabel == null)
      {
         encodedResolvedLabel = EntityEncoder.FULL.encode(getResolvedLabel());
      }
      return encodedResolvedLabel;
   }

   public UserNode getParent()
   {
      return parent;
   }

   /**
    * Returns true if the children relationship determined.
    *
    * @return ture if node has children
    */
   public boolean hasChildren()
   {
      return childMap != null;
   }

   public Collection<UserNode> getChildren()
   {
      return childMap != null ? childMap.values() : Collections.<UserNode>emptyList();
   }

   public UserNode getChild(String childName)
   {
      return childMap != null ? childMap.get(childName) : null;
   }

   // Keep this internal for now
   UserNode find(String nodeId)
   {
      UserNode found = null;
      if (data.getId().equals(nodeId))
      {
         found = this;
      }
      else if (childMap != null)
      {
         for (UserNode child : childMap.values())
         {
            UserNode a = child.find(nodeId);
            if (a != null)
            {
               found = a;
               break;
            }
         }
      }
      return found;
   }
}

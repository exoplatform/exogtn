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
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.common.text.EntityEncoder;

import java.util.Collection;
import java.util.Collections;

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
   final NodeContext<UserNode> context;

   /** . */
   private String resolvedLabel;

   /** . */
   private String encodedResolvedLabel;

   UserNode(UserNavigation navigation, NodeContext<UserNode> context)
   {
      this.navigation = navigation;
      this.context = context;
      this.resolvedLabel = null;
      this.encodedResolvedLabel = null;
   }

   public String getId()
   {
      return context.getId();
   }

   public String getName()
   {
      return context.getName();
   }

   public String getURI()
   {
      return context.getState().getURI();
   }

   public void setURI(String uri)
   {
      context.setState(new NodeState.Builder(context.getState()).setURI(uri).capture());
   }

   public String getLabel()
   {
      return context.getState().getLabel();
   }

   public void setLabel(String label)
   {
      context.setState(new NodeState.Builder(context.getState()).setLabel(label).capture());
   }

   public String getIcon()
   {
      return context.getState().getIcon();
   }

   public void setIcon(String icon)
   {
      context.setState(new NodeState.Builder(context.getState()).setIcon(icon).capture());
   }

   public long getStartPublicationTime()
   {
      return context.getState().getStartPublicationTime();
   }

   public void setStartPublicationTime(long startPublicationTime)
   {
      context.setState(new NodeState.Builder(context.getState()).setStartPublicationTime(startPublicationTime).capture());
   }

   public long getEndPublicationTime()
   {
      return context.getState().getEndPublicationTime();
   }

   public void setEndPublicationTime(long endPublicationTime)
   {
      context.setState(new NodeState.Builder(context.getState()).setEndPublicationTime(endPublicationTime).capture());
   }

   public Visibility getVisibility()
   {
      return context.getState().getVisibility();
   }

   public void setVisibility(Visibility visibility)
   {
      context.setState(new NodeState.Builder(context.getState()).setVisibility(visibility).capture());
   }

   public String getPageRef()
   {
      return context.getState().getPageRef();
   }

   public void setPageRef(String pageRef)
   {
      context.setState(new NodeState.Builder(context.getState()).setPageRef(pageRef).capture());
   }

   public String getResolvedLabel()
   {
      if (resolvedLabel == null)
      {
         String resolvedLabel;
         if (navigation.bundle != null && context.getState().getLabel() != null)
         {
            resolvedLabel = ExpressionUtil.getExpressionValue(navigation.bundle, context.getState().getLabel());
         }
         else
         {
            resolvedLabel = null;
         }

         //
         if (resolvedLabel == null)
         {
            resolvedLabel = context.getName();
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
      return context.getParent();
   }

   /**
    * Returns true if the children relationship determined.
    *
    * @return ture if node has children
    */
   public boolean hasChildrenRelationship()
   {
      return context.getChildren() != null;
   }

   /**
    * Returns the number of children, note that this does not guarantee that the children are effectively loaded, i.e
    * we have <code>getChildrenCount() != getChildren().size()</code>.
    *
    * @return the number of children
    */
   public int getChildrenCount()
   {
      return context.getChildrenCount();
   }

   public Collection<UserNode> getChildren()
   {
      Collection<UserNode> children = context.getChildren();
      return children != null ? children : Collections.<UserNode>emptyList();
   }

   public UserNode getChild(String childName)
   {
      return context.getChild(childName);
   }

   public void addChild(UserNode child)
   {
      context.addChild(navigation.model, null, child);
   }

   public void addChild(int index, UserNode child)
   {
      context.addChild(navigation.model, index, child);
   }

   public UserNode addChild(String childName)
   {
      return context.addChild(navigation.model, childName);
   }

   public boolean removeChild(String childName)
   {
      return context.removeChild(navigation.model, childName);
   }

   // Keep this internal for now
   UserNode find(String nodeId)
   {
      UserNode found = null;
      if (context.getId().equals(nodeId))
      {
         found = this;
      }
      else
      {
         Collection<UserNode> children = context.getChildren();
         if (children != null)
         {
            for (UserNode child : children)
            {
               UserNode a = child.find(nodeId);
               if (a != null)
               {
                  found = a;
                  break;
               }
            }
         }
      }
      return found;
   }
}

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

import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.navigation.NodeModel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A navigation node as seen by a user.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNode
{

   /** . */
   public static final NodeModel<UserNode> MODEL = new NodeModel<UserNode>()
   {
      public NodeData getData(UserNode node)
      {
         return node.data;
      }

      public UserNode create(NodeData data)
      {
         return new UserNode(data);
      }

      public UserNode create(NodeData data, Collection<UserNode> children)
      {
         return new UserNode(data, children);
      }
   };

   /** Marker. */
   private static final UserNode NO_PARENT_DETERMINED = new UserNode();

   /** . */
   final NodeData data;

   /** . */
   private String resolvedLabel;

   /** . */
   private String encodedResolvedLabel;

   /** . */
   private boolean modifiable;

   /** . */
   private Map<String, UserNode> childMap;

   /** . */
   private UserNode parent;

   private UserNode()
   {
      this.data = null;
      this.resolvedLabel = null;
      this.encodedResolvedLabel = null;
      this.childMap = null;
   }

   private UserNode(NodeData data)
   {
      this.parent = NO_PARENT_DETERMINED;
      this.data = data;
      this.resolvedLabel = data.getLabel();
      this.encodedResolvedLabel = data.getLabel();
      this.childMap = null;
   }

   private UserNode(NodeData data, Collection<UserNode> children)
   {
      this(data);

      Map<String, UserNode> childMap = new HashMap<String, UserNode>();
      for (UserNode child : children)
      {
         child.parent = this;
         childMap.put(child.data.getName(), child);
      }

      //
      this.childMap = childMap;
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
      return resolvedLabel;
   }

   public void setResolvedLabel(String resolvedLabel)
   {
      this.resolvedLabel = resolvedLabel;
   }

   public String getEncodedResolvedLabel()
   {
      return encodedResolvedLabel;
   }

   public void setEncodedResolvedLabel(String encodedResolvedLabel)
   {
      this.encodedResolvedLabel = encodedResolvedLabel;
   }

   public boolean isModifiable()
   {
      return modifiable;
   }

   public void setModifiable(boolean modifiable)
   {
      this.modifiable = modifiable;
   }

   public UserNode getParent()
   {
      return parent == NO_PARENT_DETERMINED ? null : parent;
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

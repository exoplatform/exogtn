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

import org.exoplatform.portal.mop.navigation.NodeData;

/**
 * A navigation node as seen by a user.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNode
{

   /** . */
   private final NodeData data;

   /** . */
   private String resolvedLabel;

   /** . */
   private String encodedResolvedLabel;

   /** . */
   private boolean modifiable;

   public UserNode(NodeData data)
   {
      this.data = data;
      this.resolvedLabel = data.getLabel();
      this.encodedResolvedLabel = data.getLabel();
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
}

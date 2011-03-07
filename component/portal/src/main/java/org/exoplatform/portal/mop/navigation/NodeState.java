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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.Visibility;

import java.util.Date;

/**
 * The state of a node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NodeState
{

   public static class Builder
   {

      /** . */
      private String uri;

      /** . */
      private String label;

      /** . */
      private String icon;

      /** . */
      private long startPublicationTime;

      /** . */
      private long endPublicationTime;

      /** . */
      private Visibility visibility;

      /** . */
      private String pageRef;

      public Builder()
      {
         this.uri = null;
         this.icon = null;
         this.label = null;
         this.startPublicationTime = -1;
         this.endPublicationTime = -1;
         this.visibility = null;
         this.pageRef = null;
      }

      /**
       * Creates a builder from a specified state.
       *
       * @param state the state to copy
       * @throws NullPointerException if the stateis null
       */
      public Builder(NodeState state) throws NullPointerException
      {
         if (state == null)
         {
            throw new NullPointerException();
         }
         this.uri = state.uri;
         this.label = state.label;
         this.icon = state.icon;
         this.startPublicationTime = state.startPublicationTime;
         this.endPublicationTime = state.endPublicationTime;
         this.visibility = state.visibility;
         this.pageRef = state.pageRef;
      }

      public String getURI()
      {
         return uri;
      }

      public Builder setURI(String uri)
      {
         this.uri = uri;
         return this;
      }

      public String getLabel()
      {
         return label;
      }

      public Builder setLabel(String label)
      {
         this.label = label;
         return this;
      }

      public String getIcon()
      {
         return icon;
      }

      public Builder setIcon(String icon)
      {
         this.icon = icon;
         return this;
      }

      public long getStartPublicationTime()
      {
         return startPublicationTime;
      }

      public Builder setStartPublicationTime(long startPublicationTime)
      {
         this.startPublicationTime = startPublicationTime;
         return this;
      }

      public long getEndPublicationTime()
      {
         return endPublicationTime;
      }

      public Builder setEndPublicationTime(long endPublicationTime)
      {
         this.endPublicationTime = endPublicationTime;
         return this;
      }

      public Visibility getVisibility()
      {
         return visibility;
      }

      public Builder setVisibility(Visibility visibility)
      {
         this.visibility = visibility;
         return this;
      }

      public String getPageRef()
      {
         return pageRef;
      }

      public Builder setPageRef(String pageRef)
      {
         this.pageRef = pageRef;
         return this;
      }

      public NodeState capture()
      {
         return new NodeState(
            uri,
            label,
            icon,
            startPublicationTime,
            endPublicationTime,
            visibility,
            pageRef
         );
      }
   }

   /** . */
   private final String uri;

   /** . */
   private final String label;

   /** . */
   private final String icon;

   /** . */
   private final long startPublicationTime;

   /** . */
   private final long endPublicationTime;

   /** . */
   private final Visibility visibility;

   /** . */
   private final String pageRef;

   public NodeState(
      String uri,
      String label,
      String icon,
      long startPublicationTime,
      long endPublicationTime,
      Visibility visibility,
      String pageRef)
   {
      this.uri = uri;
      this.label = label;
      this.icon = icon;
      this.startPublicationTime = startPublicationTime;
      this.endPublicationTime = endPublicationTime;
      this.visibility = visibility;
      this.pageRef = pageRef;
   }

   public String getURI()
   {
      return uri;
   }

   public String getLabel()
   {
      return label;
   }

   public String getIcon()
   {
      return icon;
   }

   public long getStartPublicationTime()
   {
      return startPublicationTime;
   }

   Date getStartPublicationDate()
   {
      return startPublicationTime != -1 ? new Date(startPublicationTime) : null;
   }

   public long getEndPublicationTime()
   {
      return endPublicationTime;
   }

   Date getEndPublicationDate()
   {
      return endPublicationTime != -1 ? new Date(endPublicationTime) : null;
   }

   public Visibility getVisibility()
   {
      return visibility;
   }

   public String getPageRef()
   {
      return pageRef;
   }
}

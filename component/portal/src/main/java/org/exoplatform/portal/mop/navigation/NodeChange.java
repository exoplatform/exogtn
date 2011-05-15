/*
 * Copyright (C) 2011 eXo Platform SAS.
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

/**
 * Describe a change applied to a node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class NodeChange<N>
{

   /** . */
   final N source;

   private NodeChange(N source)
   {
      this.source = source;
   }

   public final N getNode()
   {
      return source;
   }

   public final static class Destroyed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      Destroyed(N parent, N node)
      {
         super(node);

         //
         this.parent = parent;
      }

      public N getParent()
      {
         return parent;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Destroyed[node" + source + ",parent=" +  parent + "]";
      }
   }

   public final static class Removed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      Removed(N parent, N node)
      {
         super(node);

         //
         this.parent = parent;
      }

      public N getParent()
      {
         return parent;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Removed[node" + source + ",parent=" +  parent + "]";
      }
   }

   public final static class Created<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N previous;

      /** . */
      final String name;

      Created(N parent, N previous, N node, String name)
      {
         super(node);

         //
         this.parent = parent;
         this.previous = previous;
         this.name = name;
      }

      public N getParent()
      {
         return parent;
      }

      public N getPrevious()
      {
         return previous;
      }

      public String getName()
      {
         return name;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Created[node" + source + ",previous" + previous + ",parent=" + parent + ",name=" + name + "]";
      }
   }

   public final static class Added<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N previous;

      Added(N parent, N previous, N node)
      {
         super(node);

         //
         this.parent = parent;
         this.previous = previous;
      }

      public N getParent()
      {
         return parent;
      }

      public N getPrevious()
      {
         return previous != null ? previous : null;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Added[node" + source + ",previous" + previous + ",parent=" + parent + "]";
      }
   }

   public final static class Moved<N> extends NodeChange<N>
   {
      
      /** . */
      final N from;

      /** . */
      final N to;

      /** . */
      final N previous;

      Moved(N from, N to, N previous, N node)
      {
         super(node);

         //
         this.from = from;
         this.to = to;
         this.previous = previous;
      }

      public N getFrom()
      {
         return from;
      }

      public N getTo()
      {
         return to;
      }

      public N getPrevious()
      {
         return previous != null ? previous : null;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Moved[node" + source + ",from=" + from + ",to=" + to + ",previous=" + previous +  "]";
      }
   }

   public final static class Renamed<N> extends NodeChange<N>
   {

      /** . */
      final String name;

      Renamed(N node, String name)
      {
         super(node);

         //
         this.name = name;
      }

      public String getName()
      {
         return name;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Renamed[node" + source + ",name=" + name + "]";
      }
   }

   public final static class Updated<N> extends NodeChange<N>
   {

      /** . */
      final NodeState state;

      public Updated(N node, NodeState state)
      {
         super(node);

         //
         this.state = state;
      }

      public NodeState getState()
      {
         return state;
      }

      @Override
      public String toString()
      {
         return "NodeChange.Updated[node" + source + ",state=" + state + "]";
      }
   }
}

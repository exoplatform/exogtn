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

package org.exoplatform.portal.util.diff;

import org.exoplatform.portal.util.TreeContext;
import org.exoplatform.portal.util.TreeModel;
import org.exoplatform.portal.util.lcs.LCS;
import org.exoplatform.portal.util.lcs.LCSChangeIterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DiffChangeIterator<N1, N2> implements Iterator<DiffChangeType> {

   /** . */
   private final Diff<N1, N2> diff;

   /** . */
   private Frame frame;

   /** . */
   private final TreeContext<N1> context1;

   /** . */
   private final TreeContext<N2> context2;

   DiffChangeIterator(Diff<N1, N2> diff, TreeContext<N1> context1, TreeContext<N2> context2) {
      this.diff = diff;
      this.context1 = context1;
      this.context2 = context2;
      this.frame = new Frame(null, context1.getRoot(), context2.getRoot());
   }

   /**
    * The internal status.
    */
   private enum Status {

      INIT(null),

      ENTER(DiffChangeType.ENTER),

      ADDED(DiffChangeType.ADDED),

      REMOVED(DiffChangeType.REMOVED),

      MOVED_IN(DiffChangeType.MOVED_IN),

      MOVED_OUT(DiffChangeType.MOVED_OUT),

      LEAVE(DiffChangeType.LEAVE),

      ERROR(DiffChangeType.ERROR),

      CONTINUE(null);

      /** The associated change type. */
      final DiffChangeType changeType;

      private Status(DiffChangeType changeType) {
         this.changeType = changeType;
      }
   }

   private class Frame {

      /** . */
      private final Frame parent;

      /** . */
      private N1 node1;

      /** . */
      private N2 node2;

      /** . */
      private List<N1> children1;

      /** . */
      private String[] childrenIds1;

      /** . */
      private Iterator<N1> it1;

      /** . */
      private List<N2> children2;

      /** . */
      private String[] childrenIds2;

      /** . */
      private Iterator<N2> it2;

      /** . */
      private LCSChangeIterator<String> it;

      /** . */
      private Status previous;

      /** . */
      private Status next;

      /** . */
      private N1 source;

      /** . */
      private N2 destination;

      private Frame(Frame parent, N1 node1, N2 node2) {
         this.parent = parent;
         this.node1 = node1;
         this.node2 = node2;
         this.previous = Status.INIT;
      }
   }

   public boolean hasNext() {
      if (frame != null) {
         if (frame.next == null) {
            switch (frame.previous) {
               case INIT:
                  String id1 = context1.getModel().getId(frame.node1);
                  String id2 = context2.getModel().getId(frame.node2);
                  if (!id1.equals(id2)) {
                     frame.next = Status.ERROR;
                     frame.source = frame.node1;
                     frame.destination = frame.node2;
                  } else {
                     frame.next = Status.ENTER;
                     frame.source = frame.node1;
                     frame.destination = frame.node2;
                  }
                  break;
               case ERROR:
                  break;
               case LEAVE:
                  frame = frame.parent;
                  if (frame != null) {
                     frame.previous = Status.CONTINUE;
                     return hasNext();
                  } else {
                     return false;
                  }
               case MOVED_IN:
                  frame = new Frame(frame, frame.source, frame.destination);
                  return hasNext();
               case ENTER:
                  frame.children1 = context1.getModel().getChildren(frame.node1);
                  frame.childrenIds1 = ids(context1.getModel().getChildren(frame.node1), context1.getModel());
                  frame.it1 = frame.children1.iterator();
                  frame.children2 = context2.getModel().getChildren(frame.node2);
                  frame.childrenIds2 = ids(context2.getModel().getChildren(frame.node2), context2.getModel());
                  frame.it2 = frame.children2.iterator();
                  frame.it = new LCS<String>().perform(frame.childrenIds1, frame.childrenIds2);
               case ADDED:
               case REMOVED:
               case MOVED_OUT:
               case CONTINUE:
                  if (frame.it.hasNext()) {
                     switch (frame.it.next()) {
                        case KEEP:
                           frame = new Frame(frame, frame.it1.next(), frame.it2.next());
                           return hasNext();
                        case ADD:
                           frame.it2.next();
                           N1 a = context1.findById(frame.it.getElement());
                           N2 added = frame.children2.get(frame.it.getIndex2() - 1);
                           if (a != null) {
                              frame.next = Status.MOVED_IN;
                              frame.source = a;
                              frame.destination = added;
                           } else {
                              frame.next = Status.ADDED;
                              frame.source = null;
                              frame.destination = added;
                           }
                           break;
                        case REMOVE:
                           frame.it1.next();
                           N1 removed = frame.children1.get(frame.it.getIndex1() - 1);
                           N2 b = context2.findById(frame.it.getElement());
                           if (b != null) {
                              frame.next = Status.MOVED_OUT;
                              frame.source = removed;
                              frame.destination = b;
                           } else {
                              frame.next = Status.REMOVED;
                              frame.source = removed;
                              frame.destination = null;
                           }
                           break;
                        default:
                           throw new AssertionError();
                     }
                  } else {
                     frame.next = Status.LEAVE;
                     frame.source = frame.node1;
                     frame.destination = frame.node2;
                  }
                  break;
               default:
                  throw new AssertionError("Was not expecting status " + frame.previous);
            }
         }
         return frame.next != null;
      }
      return false;
   }

   public DiffChangeType next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      } else {
         frame.previous = frame.next;
         frame.next = null;
         return frame.previous.changeType;
      }
   }

   public N1 getSource() {
      return frame.source;
   }

   public N2 getDestination() {
      return frame.destination;
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   private static <N> String[] ids(List<N> nodes, TreeModel<N> model) {
      int size = nodes.size();
      String[] ids = new String[size];
      if (nodes instanceof RandomAccess) {
         for (int i = 0; i < size; i++) {
            ids[i] = model.getId(nodes.get(i));
         }
      } else {
         int i = 0;
         for (N node : nodes) {
            ids[i++] = model.getId(node);
         }
      }
      return ids;
   }
}

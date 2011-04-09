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

package org.exoplatform.portal.tree.sync.diff;

import org.exoplatform.portal.tree.sync.SyncContext;
import org.exoplatform.portal.tree.sync.SyncModel;
import org.exoplatform.portal.tree.sync.lcs.LCS;
import org.exoplatform.portal.tree.sync.lcs.LCSChangeIterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DiffChangeIterator<L1, N1, L2, N2, H> implements Iterator<DiffChangeType> {

   /** . */
   private final Diff<L1, N1, L2, N2, H> diff;

   /** . */
   private Frame frame;

   /** . */
   private final SyncContext<L1, N1, H> context1;

   /** . */
   private final SyncContext<L2, N2, H> context2;

   DiffChangeIterator(Diff<L1, N1, L2, N2, H> diff, SyncContext<L1, N1, H> context1, SyncContext<L2, N2, H> context2) {
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
      private L1 children1;

      /** . */
      private Iterator<H> it1;

      /** . */
      private L2 children2;

      /** . */
      private Iterator<H> it2;

      /** . */
      private LCSChangeIterator<L1, L2, H> it;

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
                  H id1 = context1.getModel().getHandle(frame.node1);
                  H id2 = context2.getModel().getHandle(frame.node2);
                  if (diff.comparator.compare(id1, id2) != 0) {
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
                  frame.it1 = diff.adapter1.iterator(frame.children1, false);
                  frame.children2 = context2.getModel().getChildren(frame.node2);
                  frame.it2 = diff.adapter2.iterator(frame.children2, false);
                  frame.it = LCS.create(
                        diff.adapter1,
                        diff.adapter2,
                        diff.comparator).perform(frame.children1, frame.children2);
               case ADDED:
               case REMOVED:
               case MOVED_OUT:
               case CONTINUE:
                  if (frame.it.hasNext()) {
                     switch (frame.it.next()) {
                        case KEEP:
                           N1 next1 = context1.getModel().getDescendant(frame.node1, frame.it1.next());
                           N2 next2 = context2.getModel().getDescendant(frame.node2, frame.it2.next());
                           frame = new Frame(frame, next1, next2);
                           return hasNext();
                        case ADD:
                           frame.it2.next();
                           H addedHandle = frame.it.getElement();
                           N2 added = context2.getModel().getDescendant(frame.node2, addedHandle);
                           H addedId = context2.getModel().getHandle(added);
                           N1 a = context1.findByHandle(addedId);
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
                           H removedHandle = frame.it.getElement();
                           N1 removed = context1.getModel().getDescendant(frame.node1, removedHandle);
                           H removedId = context1.getModel().getHandle(removed);
                           N2 b = context2.findByHandle(removedId);
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
}

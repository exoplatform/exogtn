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

import org.exoplatform.portal.tree.sync.ListAdapter;
import org.exoplatform.portal.tree.sync.SyncContext;
import org.exoplatform.portal.tree.sync.SyncModel;
import org.exoplatform.portal.tree.sync.lcs.LCS;
import org.exoplatform.portal.tree.sync.lcs.LCSChangeIterator;

import java.util.*;

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

      RESUME(null);

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
      private Iterator<H> it1;

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

//   private static

   public boolean hasNext() {
      if (frame != null && frame.next == null) {
         while (true) {

            if (frame.previous == Status.INIT) {
               H id2 = context2.getModel().getHandle(frame.node2);
               if (frame.node1 == null)
               {
                  frame.next = Status.ENTER;
                  frame.source = null;
                  frame.destination = frame.node2;
               }
               else
               {
                  H id1 = context1.getModel().getHandle(frame.node1);
                  if (diff.comparator.compare(id1, id2) != 0) {
                     frame.next = Status.ERROR;
                     frame.source = frame.node1;
                     frame.destination = frame.node2;
                  } else {
                     frame.next = Status.ENTER;
                     frame.source = frame.node1;
                     frame.destination = frame.node2;
                  }
               }
               break;
            } else if (frame.previous == Status.ERROR) {
               break;
            } else if (frame.previous == Status.LEAVE) {
               frame = frame.parent;
               if (frame != null) {
                  frame.previous = Status.RESUME;
                  continue;
               } else {
                  break;
               }
            } else if (frame.previous == Status.MOVED_IN) {
               frame = new Frame(frame, frame.source, frame.destination);
               continue;
            } else if (frame.previous == Status.ADDED) {
               frame = new Frame(frame, frame.source, frame.destination);
               continue;
            } else if (frame.previous == Status.ENTER) {
               ListAdapter<L1, H> adapter1;
               L1 children1;
               if (frame.source != null)
               {
                  children1 = context1.getModel().getChildren(frame.node1);
                  adapter1 = diff.adapter1;
               }
               else
               {
                  children1 = null;
                  adapter1 = new ListAdapter<L1, H>() {
                     public int size(L1 list) {
                        return 0;
                     }
                     public Iterator<H> iterator(L1 list, boolean reverse) {
                        return Collections.<H>emptyList().iterator();
                     }
                  };
               }
               L2 children2 = context2.getModel().getChildren(frame.node2);
               frame.it1 = adapter1.iterator(children1, false);
               frame.it2 = diff.adapter2.iterator(children2, false);
               frame.it = LCS.create(
                     adapter1,
                     diff.adapter2,
                     diff.comparator).perform(children1, children2);
            } else {
               // Nothing
            }

            //
            if (frame.it.hasNext()) {
               switch (frame.it.next()) {
                  case KEEP:
                     N1 next1 = context1.findByHandle(frame.it1.next());
                     N2 next2 = context2.findByHandle(frame.it2.next());
                     frame = new Frame(frame, next1, next2);
                     return hasNext();
                  case ADD:
                     frame.it2.next();
                     H addedHandle = frame.it.getElement();
                     N2 added = context2.findByHandle(addedHandle);
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
                     N1 removed = context1.findByHandle(removedHandle);
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

            //
            break;
         }
      }
      return frame != null && frame.next != null;
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

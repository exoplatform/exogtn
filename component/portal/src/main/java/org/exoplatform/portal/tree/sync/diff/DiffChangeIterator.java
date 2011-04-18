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
      private final N1 srcRoot;

      /** . */
      private final N2 dstRoot;

      /** . */
      private LCSChangeIterator<L1, L2, H> it;

      /** . */
      private Status previous;

      /** . */
      private Status next;

      /** . */
      private Iterator<H> srcIt;

      /** . */
      private Iterator<H> dstIt;

      /** . */
      private N1 src;

      /** . */
      private N2 dst;

      private Frame(Frame parent, N1 srcRoot, N2 dstRoot) {
         this.parent = parent;
         this.srcRoot = srcRoot;
         this.dstRoot = dstRoot;
         this.previous = Status.INIT;
      }
   }

   public boolean hasNext() {
      if (frame != null && frame.next == null) {
         while (true) {

            if (frame.previous == Status.INIT) {
               H id2 = context2.getModel().getHandle(frame.dstRoot);
               if (frame.srcRoot == null)
               {
                  frame.next = Status.ENTER;
                  frame.src = null;
                  frame.dst = frame.dstRoot;
               }
               else
               {
                  H id1 = context1.getModel().getHandle(frame.srcRoot);
                  if (diff.comparator.compare(id1, id2) != 0) {
                     frame.next = Status.ERROR;
                     frame.src = frame.srcRoot;
                     frame.dst = frame.dstRoot;
                  } else {
                     frame.next = Status.ENTER;
                     frame.src = frame.srcRoot;
                     frame.dst = frame.dstRoot;
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
               frame = new Frame(frame, frame.src, frame.dst);
               continue;
            } else if (frame.previous == Status.ADDED) {
               frame = new Frame(frame, frame.src, frame.dst);
               continue;
            } else if (frame.previous == Status.ENTER) {
               ListAdapter<L1, H> adapter1;
               L1 children1;
               if (frame.src != null)
               {
                  children1 = context1.getModel().getChildren(frame.srcRoot);
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
               L2 children2 = context2.getModel().getChildren(frame.dstRoot);
               frame.srcIt = adapter1.iterator(children1, false);
               frame.dstIt = diff.adapter2.iterator(children2, false);
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
                     N1 next1 = context1.findByHandle(frame.srcIt.next());
                     N2 next2 = context2.findByHandle(frame.dstIt.next());
                     frame = new Frame(frame, next1, next2);
                     continue;
                  case ADD:
                     frame.dstIt.next();
                     H addedHandle = frame.it.getElement();
                     N2 added = context2.findByHandle(addedHandle);
                     H addedId = context2.getModel().getHandle(added);
                     N1 a = context1.findByHandle(addedId);
                     if (a != null) {
                        frame.next = Status.MOVED_IN;
                        frame.src = a;
                        frame.dst = added;
                     } else {
                        frame.next = Status.ADDED;
                        frame.src = null;
                        frame.dst = added;
                     }
                     break;
                  case REMOVE:
                     frame.srcIt.next();
                     H removedHandle = frame.it.getElement();
                     N1 removed = context1.findByHandle(removedHandle);
                     H removedId = context1.getModel().getHandle(removed);
                     N2 b = context2.findByHandle(removedId);
                     if (b != null) {
                        frame.next = Status.MOVED_OUT;
                        frame.src = removed;
                        frame.dst = b;
                     } else {
                        frame.next = Status.REMOVED;
                        frame.src = removed;
                        frame.dst = null;
                     }
                     break;
                  default:
                     throw new AssertionError();
               }
            } else {
               frame.next = Status.LEAVE;
               frame.src = frame.srcRoot;
               frame.dst = frame.dstRoot;
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
      return frame.src;
   }

   public N2 getDestination() {
      return frame.dst;
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}

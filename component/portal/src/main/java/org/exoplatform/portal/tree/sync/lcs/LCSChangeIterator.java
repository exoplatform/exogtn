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

package org.exoplatform.portal.tree.sync.lcs;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LCSChangeIterator<L1, L2, E> implements Iterator<LCSChangeType> {

  /** . */
  private boolean buffered;

  /** . */
  private LCSChangeType type;

  /** . */
  private E element;

  /** . */
  private final LCS<L1, L2, E> lcs;

  /** . */
  private int size1;

  /** . */
  private int size2;

  /** . */
  private int i;

  /** . */
  private int j;

  /** . */
  private Iterator<E> it1;

  /** . */
  private E next1;

  /** . */
  private Iterator<E> it2;

  /** . */
  private E next2;

  LCSChangeIterator(LCS<L1, L2, E> lcs, L1 elements1, L2 elements2, int size1, int size2) {
    this.buffered = false;
    this.lcs = lcs;
    this.size1 = size1;
    this.size2 = size2;
    this.i = size1;
    this.j = size2;
    this.it1 = lcs.adapter1.iterator(elements1, false);
    this.it2 = lcs.adapter2.iterator(elements2, false);


    if (it1.hasNext()) {
      next1 = it1.next();
    }
    if (it2.hasNext()) {
      next2 = it2.next();
    }
  }

  private void next1() {
    i--;
    if (it1.hasNext()) {
      next1 = it1.next();
    } else {
      next1 = null;
    }
  }

  private void next2() {
    j--;
    if (it2.hasNext()) {
      next2 = it2.next();
    } else {
      next2 = null;
    }
  }

  public LCSChangeType getType() {
    return type;
  }

  public E getElement() {
    return element;
  }

  public int getIndex1() {
    return size1 - i;
  }

  public int getIndex2() {
    return size2 - j;
  }

  public boolean hasNext() {
    if (!buffered) {
      E elt1 = null;
      E elt2 = null;
      if (i > 0 && j > 0 && lcs.equals(elt1 = next1, elt2 = next2)) {
        type = LCSChangeType.KEEP;
        element = elt1;
        next1();
        next2();
        buffered = true;
      } else {
        int index1 = i + (j - 1) * lcs.m;
        int index2 = i - 1 + j * lcs.m;
        if (j > 0 && (i == 0  || lcs.matrix[index1] >= lcs.matrix[index2])) {
          type = LCSChangeType.ADD;
          element = elt2 == null ? next2 : elt2;
          next2();
          buffered = true;
        } else if (i > 0 && (j == 0 || lcs.matrix[index1] < lcs.matrix[index2])) {
          type = LCSChangeType.REMOVE;
          element = elt1 == null ? next1 : elt1;
          next1();
          buffered = true;
        } else {
          // Done
        }
      }
    }
    return buffered;
  }

  public LCSChangeType next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    } else {
      buffered = false;
      return type;
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}

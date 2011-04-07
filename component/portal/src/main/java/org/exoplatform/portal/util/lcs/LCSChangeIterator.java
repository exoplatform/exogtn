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

package org.exoplatform.portal.util.lcs;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LCSChangeIterator<E> implements Iterator<LCSChangeType> {

  /** . */
  private boolean buffered;

  /** . */
  private LCSChangeType type;

  /** . */
  private E element;

  /** . */
  private final LCS<E> lcs;

  /** . */
  private final E[] elements1;

  /** . */
  private final E[] elements2;

  /** . */
  private int i;

  /** . */
  private int j;

  LCSChangeIterator(LCS<E> lcs, E[] elements1, E[] elements2) {
    this.buffered = false;
    this.lcs = lcs;
    this.elements1 = elements1;
    this.elements2 = elements2;
    this.i = elements1.length;
    this.j = elements2.length;
  }

  public LCSChangeType getType() {
    return type;
  }

  public E getElement() {
    return element;
  }

  public int getIndex1() {
    return elements1.length - i;
  }

  public int getIndex2() {
    return elements2.length - j;
  }

  public boolean hasNext() {
    if (!buffered) {
      E e1 = null;
      E e2 = null;
      if (i > 0 && j > 0 && lcs.equals(e1 = elements1[elements1.length - i], e2 = elements2[elements2.length - j])) {
        type = LCSChangeType.KEEP;
        element = e1;
        i--;
        j--;
        buffered = true;
      } else {
        int index1 = i + (j - 1) * lcs.m;
        int index2 = i - 1 + j * lcs.m;
        if (j > 0 && (i == 0  || lcs.matrix[index1] >= lcs.matrix[index2])) {
          type = LCSChangeType.ADD;
          element = e2 == null ? elements2[elements2.length - j] : e2;
          j--;
          buffered = true;
        } else if (i > 0 && (j == 0 || lcs.matrix[index1] < lcs.matrix[index2])) {
          type = LCSChangeType.REMOVE;
          element = e1 == null ? elements1[elements1.length - i] : e1;
          i--;
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

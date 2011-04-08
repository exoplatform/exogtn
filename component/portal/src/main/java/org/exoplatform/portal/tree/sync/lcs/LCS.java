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

import java.util.Comparator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LCS<E> {

  public static <E extends Comparable<E>> LCS<E> create() {
    return new LCS<E>() {
      @Override
      protected boolean equals(E e1, E e2) {
        return e1.compareTo(e2) == 0;
      }
    };
  }

  public static <E> LCS<E> create(final Comparator<E> comparator) {
    return new LCS<E>() {
      @Override
      protected boolean equals(E e1, E e2) {
        return comparator.compare(e1, e2) == 0;
      }
    };
  }

  /** . */
  private static final int[] EMPTY = new int[0];

  /** . */
  int[] matrix;

  /** . */
  int m;

  /** . */
  int n;

  public LCS() {
    this.matrix = EMPTY;
    this.m = -1;
    this.n = -1;
  }

  public final LCSChangeIterator<E> perform(E[] elements1, E[] elements2) {
    m = 1 + elements1.length;
    n = 1 + elements2.length;
    int s = m * n;
    if (matrix.length < s) {
      matrix = new int[s];
    } else {
      for (int i = 0;i < m;i++) {
        matrix[i] = 0;
      }
      for (int j = 1;j < n;j++) {
        matrix[j * m] = 0;
      }
    }
    for (int i = 1;i < m;i++) {
      for (int j = 1;j < n;j++) {
        int index = i + j * m;
        int v;
        if (equals(elements1[elements1.length - i], elements2[elements2.length - j])) {
          v = matrix[index - m - 1] + 1;
        } else {
          int v1 = matrix[index - 1];
          int v2 = matrix[index - m];
          v = v1 < v2 ? v1 : v2;
        }
        matrix[index] = v;
      }
    }

    //
    return new LCSChangeIterator<E>(this, elements1, elements2);
  }

  protected boolean equals(E e1, E e2) {
    return e1.equals(e2);
  }
}

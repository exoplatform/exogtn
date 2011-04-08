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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LCSTestCase extends TestCase
{

  private static Character[] chars(String s) {
    Character[] chars = new Character[s.length()];
    for (int i = 0;i < chars.length;i++) {
      chars[i] = s.charAt(i);
    }
    return chars;
  }

  private LCSChangeIterator<Character> diff(String s1, String s2) {
    Character[] c1 = chars(s1);
    Character[] c2 = chars(s2);
    return new LCS<Character>().perform(c1, c2);
  }

  public void test1() {
    LCSChangeIterator<Character> it = diff("", "a");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(0, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertFalse(it.hasNext());
  }

  public void test2() {
    LCSChangeIterator<Character> it = diff("a", "");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.REMOVE, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(1, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertFalse(it.hasNext());
  }

  public void test3() {
    LCSChangeIterator<Character> it = diff("a", "a");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.KEEP, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(1, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertFalse(it.hasNext());
  }

  public void test4() {
    LCSChangeIterator<Character> it = diff("a", "b");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals('b', (char)it.getElement());
    assertEquals(0, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertEquals(LCSChangeType.REMOVE, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(1, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertFalse(it.hasNext());
  }

  public void test5() {
    LCSChangeIterator<Character> it = diff("", "ab");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(0, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals('b', (char)it.getElement());
    assertEquals(0, it.getIndex1());
    assertEquals(2, it.getIndex2());
    assertFalse(it.hasNext());
  }

  public void test6() {
    LCSChangeIterator<Character> it = diff("abc", "dbe");
    assertEquals(0, it.getIndex1());
    assertEquals(0, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals('d', (char)it.getElement());
    assertEquals(0, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertEquals(LCSChangeType.REMOVE, it.next());
    assertEquals('a', (char)it.getElement());
    assertEquals(1, it.getIndex1());
    assertEquals(1, it.getIndex2());
    assertEquals(LCSChangeType.KEEP, it.next());
    assertEquals('b', (char)it.getElement());
    assertEquals(2, it.getIndex1());
    assertEquals(2, it.getIndex2());
    assertEquals(LCSChangeType.ADD, it.next());
    assertEquals(2, it.getIndex1());
    assertEquals(3, it.getIndex2());
    assertEquals('e', (char)it.getElement());
    assertEquals(LCSChangeType.REMOVE, it.next());
    assertEquals('c', (char)it.getElement());
    assertEquals(3, it.getIndex1());
    assertEquals(3, it.getIndex2());
    assertFalse(it.hasNext());
  }
}

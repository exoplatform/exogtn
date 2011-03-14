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

package org.exoplatform.portal.mop.navigation2;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNodeContext extends TestCase
{

   public static class IntegerTree extends ListTree<IntegerTree, Integer>
   {

      /** . */
      private final int value;

      public IntegerTree(int value, String name)
      {
         super(name);

         //
         this.value = value;
      }

      @Override
      public Integer getElement()
      {
         return value;
      }
   }

   private static IntegerTree hidden(String name, int value, IntegerTree... trees)
   {
      IntegerTree tree = shown(name, value, trees);
      tree.setHidden(true);
      return tree;
   }

   private static IntegerTree shown(String name, int value, IntegerTree... trees)
   {
      IntegerTree tree = new IntegerTree(value, name);
      if (trees != null)
      {
         tree.setTrees(Arrays.asList(trees));
      }
      return tree;
   }

   private void assertChildren(IntegerTree tree, Integer... expected)
   {
      List<Integer> children = new ArrayList<Integer>();
      for (int child : tree)
      {
         children.add(child);
      }
      assertEquals(Arrays.asList(expected), children);
   }

   private void assertAllChildren(IntegerTree tree, Integer... expected)
   {
      List<Integer> children = new ArrayList<Integer>();
      for (IntegerTree child : tree.getTrees())
      {
         children.add(child.getElement());
      }
      assertEquals(Arrays.asList(expected), children);
   }

   private void assertAllChildren(IntegerTree tree)
   {
      assertAllChildren(tree, new Integer[0]);
      assertAllChildren(tree, new String[0]);
   }

   private void assertAllChildren(IntegerTree tree, String... expected)
   {
      List<String> children = new ArrayList<String>();
      for (IntegerTree child : tree.getTrees())
      {
         children.add(child.getName());
      }
      assertEquals(Arrays.asList(expected), children);
   }

   public void testInsert1()
   {
      IntegerTree root = shown("", 0);
      assertChildren(root);
      assertAllChildren(root);

      //
      root = shown("", 0);
      root.insert(0, shown("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");

      //
      root = shown("", 0);
      root.insert(null, shown("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");
   }

   public void testInsert2()
   {
      IntegerTree root = shown("", 0, hidden("a", 1));
      assertChildren(root);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");

      //
      root = shown("", 0, hidden("a", 1));
      root.insert(0, shown("b", 2));
      assertChildren(root, 2);
      assertAllChildren(root, 2, 1);
      assertAllChildren(root, "b", "a");

      //
      root = shown("", 0, hidden("a", 1));
      root.insert(null, shown("b", 2));
      assertChildren(root, 2);
      assertAllChildren(root, 2, 1);
      assertAllChildren(root, "b", "a");
   }

   public void testInsert3()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2));
      assertChildren(root, 1);
      assertAllChildren(root, 1, 2);
      assertAllChildren(root, "a", "b");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2));
      root.insert(0, shown("c", 3));
      assertChildren(root, 3, 1);
      assertAllChildren(root, 3, 1, 2);
      assertAllChildren(root, "c", "a", "b");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2));
      root.insert(1, shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 3, 2);
      assertAllChildren(root, "a", "c", "b");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2));
      root.insert(null, shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 3, 2);
      assertAllChildren(root, "a", "c", "b");
   }

   public void testInsert4()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(0, shown("d", 4));
      assertChildren(root, 4, 1, 3);
      assertAllChildren(root, 4, 1, 2, 3);
      assertAllChildren(root, "d", "a", "b", "c");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(1, shown("d", 4));
      assertChildren(root, 1, 4, 3);
      assertAllChildren(root, 1, 4, 2, 3);
      assertAllChildren(root, "a", "d", "b", "c");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(2, shown("d", 4));
      assertChildren(root, 1, 3, 4);
      assertAllChildren(root, 1, 2, 3, 4);
      assertAllChildren(root, "a", "b", "c", "d");

      //
      root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(null, shown("d", 4));
      assertChildren(root, 1, 3, 4);
      assertAllChildren(root, 1, 2, 3, 4);
      assertAllChildren(root, "a", "b", "c", "d");
   }

   public void testInsertDuplicate()
   {
      IntegerTree root = shown("", 0, shown("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");

      //
      try
      {
         root.insert(0, shown("a", 2));
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
         assertAllChildren(root, 1);
         assertAllChildren(root, "a");
      }
   }

   public void testInsertWithNoChildren()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);
      assertFalse(root.hasTrees());

      //
      try
      {
         root.insert(0, shown("a", 1));
         fail();
      }
      catch (IllegalStateException ignore)
      {
         assertFalse(root.hasTrees());
      }
   }

   public void testInsertMove1()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree b = shown("b", 2);
      IntegerTree root1 = shown("", 0, a, b);

      //
      root1.insert(0, b);
      assertAllChildren(root1, 2, 1);
   }

   public void testInsertMove2()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root1 = shown("", 0, a);

      //
      root1.insert(null, a);
      assertAllChildren(root1, 1);

      //
      root1.insert(0, a);
      assertAllChildren(root1, 1);
   }

   public void testInsertMove3()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root1 = shown("", 0, a);
      IntegerTree root2 = shown("", 0);

      //
      root2.insert(0, a);
      assertAllChildren(root1);
      assertAllChildren(root2, 1);
      assertAllChildren(root2, "a");
      assertSame(root2, a.getParent());
   }

   public void testInsertReorder1()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root1 = shown("", 0, a);

      //
      root1.insert(0, a);
      assertAllChildren(root1, 1);
      assertAllChildren(root1, "a");
      assertSame(root1, a.getParent());
   }

   public void testInsertReorder2()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root1 = shown("", 0, a, shown("b", 2));

      //
      root1.insert(2, a);
      assertAllChildren(root1, 2, 1);
      assertAllChildren(root1, "b", "a");
      assertSame(root1, a.getParent());

      //
      root1.insert(0, a);
      assertAllChildren(root1, 1, 2);
      assertAllChildren(root1, "a", "b");
      assertSame(root1, a.getParent());
   }

   public void testGetByKey()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      assertEquals(1, (int)root.get("a").getElement());
      assertNull(root.get("b"));
      assertNull(root.get("d"));
   }

   public void testGetByKeyWithNoChildren()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);
      assertFalse(root.hasTrees());

      //
      try
      {
         root.get("a");
         fail();
      }
      catch (IllegalStateException ignore)
      {
         assertFalse(root.hasTrees());
      }
   }

   public void testRemove()
   {
      IntegerTree root = shown("", 0, hidden("a", 1), shown("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      assertNull(root.remove("a"));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      IntegerTree b = root.remove("b");
      assertNull(b.getParent());
      assertNull(b.getPrevious());
      assertNull(b.getNext());
      assertEquals(2, (int)b.getElement());
      assertAllChildren(root, 1, 3);
      assertAllChildren(root, "a", "c");
   }

   public void testRemoveLast()
   {
      IntegerTree root = shown("", 0, shown("a", 1), shown("b", 2));
      assertAllChildren(root, 1, 2);
      assertAllChildren(root, "a", "b");

      //
      assertEquals(2, (int)root.remove("b").getElement());
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");
      assertEquals(1, (int)root.getLast().getElement());
   }

   public void testRemoveWithNoChildren()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);
      assertFalse(root.hasTrees());

      //
      try
      {
         root.remove("a");
         fail();
      }
      catch (IllegalStateException ignore)
      {
         assertFalse(root.hasTrees());
      }
   }

   public void testRename()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root.rename("a", "a");
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root.rename("a", "d");
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "d", "b", "c");
   }

   public void testRenameWithNoChildren()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);
      assertFalse(root.hasTrees());

      //
      try
      {
         root.rename("a", "b");
         fail();
      }
      catch (IllegalStateException e)
      {
         assertFalse(root.hasTrees());
      }
   }

   public void testRenameWithNonExisting()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      try
      {
         root.rename("d", "e");
         fail();
      }
      catch (IllegalArgumentException e)
      {
         assertAllChildren(root, 1, 2, 3);
         assertAllChildren(root, "a", "b", "c");
      }
   }

   public void testRenameWithExisting()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      try
      {
         root.rename("a", "c");
         fail();
      }
      catch (IllegalArgumentException e)
      {
         assertAllChildren(root, 1, 2, 3);
         assertAllChildren(root, "a", "b", "c");
      }
   }

   public void testRenameHidden()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      try
      {
         root.rename("b", "d");
         fail();
      }
      catch (IllegalArgumentException e)
      {
         assertAllChildren(root, 1, 2, 3);
         assertAllChildren(root, "a", "b", "c");
      }
   }

   public void testGetByIndex1()
   {
      IntegerTree root = shown("", 0, shown("a", 1), hidden("b", 2), shown("c", 3));

      //
      assertEquals(1, (int)root.get(0).getElement());
      assertEquals(3, (int)root.get(1).getElement());
      try
      {
         root.get(2);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
   }

   public void testGetByIndex2()
   {
      IntegerTree root = shown("", 0, hidden("a", 1), shown("b", 2), hidden("c", 3));

      //
      assertEquals(2, (int)root.get(0).getElement());
      try
      {
         root.get(1);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
   }

   public void testGetByIndexWithNoChildren()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);

      //
      try
      {
         root.get(0);
         fail();
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void testIteratorRemove()
   {
      IntegerTree root = shown("", 0, shown("a", 1));
      Iterator<IntegerTree> it = root.getTrees().iterator();

      //
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
      }

      //
      IntegerTree a = it.next();
      it.remove();
      assertNull(a.getParent());
      assertFalse(it.hasNext());
      assertAllChildren(root);
   }

   public void testListIterator1()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root = shown("", 0, a);

      //
      ListIterator<IntegerTree> i = root.listIterator();
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());

      //
      assertSame(a, i.next());
      assertFalse(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());

      //
      assertSame(a, i.previous());
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());
   }

   public void testListIterator2()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree b = shown("b", 2);
      IntegerTree root = shown("", 0, a, b);

      //
      ListIterator<IntegerTree> i = root.listIterator();
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());
      assertSame(a, i.next());
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(b, i.next());
      assertFalse(i.hasNext());
      assertEquals(2, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(1, i.previousIndex());
      i.remove();
      assertFalse(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
   }

   public void testListIterator3()
   {
      // Remove middle
      IntegerTree a = shown("a", 1);
      IntegerTree b = shown("b", 2);
      IntegerTree c = shown("c", 3);
      IntegerTree root = shown("", 0, a, b, c);
      ListIterator<IntegerTree> i = root.listIterator();
      i.next();
      i.next();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(c, i.next());

      // Remove middle
      root = shown("", 0, a = shown("a", 1), b = shown("b", 2), c = shown("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.next();
      i.previous();
      i.previous();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(c, i.next());

      // Remove middle
      root = shown("", 0, a = shown("a", 1), b = shown("b", 2), c = shown("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(a, i.previous());

      // Remove middle
      root = shown("", 0, a = shown("a", 1), b = shown("b", 2), c = shown("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.next();
      i.previous();
      i.previous();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(a, i.previous());
   }

   public void testCount()
   {
      IntegerTree root = shown("", 0, (IntegerTree[])null);
      assertEquals(-1, root.getCount());
      IntegerTree a = shown("a", 1);
      root.setTrees(Collections.singleton(a));
      assertEquals(1, root.getCount());
      a.setHidden(true);
      assertEquals(0, root.getCount());
      a.remove("a");
      assertEquals(0, root.getCount());
      root.setTrees(null);
      assertEquals(-1, root.getCount());
   }


   @SuppressWarnings("unchecked")
   public void testListIteratorNavigation()
   {
      IntegerTree root = shown("", 0, shown("1", 1), shown("2", 2), shown("3", 3), shown("4", 4), shown("5", 5));
      ListIterator<IntegerTree> it = root.listIterator();
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
      assertEquals(1, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(1, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
      assertEquals(1, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(2, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(2, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(2, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(3, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertEquals(4, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(3, it.previousIndex());
      assertEquals(4, it.nextIndex());
      assertEquals(5, it.next().value);
      assertTrue(!it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(4, it.previousIndex());
      assertEquals(5, it.nextIndex());
      assertEquals(5, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(3, it.previousIndex());
      assertEquals(4, it.nextIndex());
      assertEquals(4, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertEquals(3, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(2, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(1, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
   }

   /*
      @Override
      @SuppressWarnings("unchecked")
      public void testListIteratorSet() {
          list.add((E) "1");
          list.add((E) "2");
          list.add((E) "3");
          list.add((E) "4");
          list.add((E) "5");

          ListIterator<E> it = list.listIterator();
          assertEquals("1", it.next());
          it.set((E) "a");
          assertEquals("a", it.previous());
          it.set((E) "A");
          assertEquals("A", it.next());
          assertEquals("2", it.next());
          it.set((E) "B");
          assertEquals("3", it.next());
          assertEquals("4", it.next());
          it.set((E) "D");
          assertEquals("5", it.next());
          it.set((E) "E");
          assertEquals("[A, B, 3, D, E]", list.toString());
      }
   */

   public void testListIteratorRemove()
   {
      IntegerTree root = shown("", 0, shown("1", 1), shown("2", 2), shown("3", 3), shown("4", 4), shown("5", 5));
      ListIterator<IntegerTree> it = root.listIterator();
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
         // expected
      }
      assertEquals(1, it.next().value);
      assertEquals(2, it.next().value);
      assertAllChildren(root, 1, 2, 3, 4, 5);
      it.remove();
      assertAllChildren(root, 1, 3, 4, 5);
      assertEquals(3, it.next().value);
      assertEquals(3, it.previous().value);
      assertEquals(1, it.previous().value);
      it.remove();
      assertAllChildren(root, 3, 4, 5);
      assertTrue(!it.hasPrevious());
      assertEquals(3, it.next().value);
      it.remove();
      assertAllChildren(root, 4, 5);
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
         // expected
      }
      assertEquals(4, it.next().value);
      assertEquals(5, it.next().value);
      it.remove();
      assertAllChildren(root, 4);
      assertEquals(4, it.previous().value);
      it.remove();
      assertAllChildren(root);
   }

   public void testListIteratorAdd()
   {
      IntegerTree root = shown("", 0);
      ListIterator<IntegerTree> it = root.listIterator();
      it.add(shown("a", 1));
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertAllChildren(root, 1);
      it.add(shown("c", 3));
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertAllChildren(root, 1, 3);
      it.add(shown("e", 5));
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertAllChildren(root, 1, 3, 5);
      assertEquals(5, it.previous().value);
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      it.add(shown("d", 4));
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertAllChildren(root, 1, 3, 4, 5);
      assertEquals(4, it.previous().value);
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(3, it.previous().value);
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      it.add(shown("b", 2));
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertAllChildren(root, 1, 2, 3, 4, 5);
   }

   public void testListIteratorMove()
   {
      IntegerTree root = shown("", 0, shown("a", 1), shown("b", 2), shown("c", 3));
      ListIterator<IntegerTree> it = root.listIterator();
      it.add(root.get(2));
      assertAllChildren(root, 3, 1, 2);
   }
}

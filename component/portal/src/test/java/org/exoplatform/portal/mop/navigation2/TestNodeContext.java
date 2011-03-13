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
import java.util.List;

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

      public IntegerTree(int value, String name, boolean hidden)
      {
         super(name, hidden);

         //
         this.value = value;
      }

      @Override
      public Integer getElement()
      {
         return value;
      }
   }

   private static IntegerTree hidden(String name, int value)
   {
      return new IntegerTree(value, name, true);
   }

   private static IntegerTree shown(String name, int value)
   {
      return new IntegerTree(value, name, false);
   }

   private static IntegerTree node(IntegerTree... entries)
   {
      IntegerTree tree = new IntegerTree(0, "", false);
      tree.setTrees(Arrays.asList(entries));
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
      IntegerTree root = node();
      assertChildren(root);
      assertAllChildren(root);

      //
      root = node();
      root.insert(0, shown("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");

      //
      root = node();
      root.insert(null, shown("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");
   }

   public void testInsert2()
   {
      IntegerTree root = node(hidden("a", 1));
      assertChildren(root);
      assertAllChildren(root, 1);
      assertAllChildren(root, "a");

      //
      root = node(hidden("a", 1));
      root.insert(0, shown("b", 2));
      assertChildren(root, 2);
      assertAllChildren(root, 2, 1);
      assertAllChildren(root, "b", "a");

      //
      root = node(hidden("a", 1));
      root.insert(null, shown("b", 2));
      assertChildren(root, 2);
      assertAllChildren(root, 2, 1);
      assertAllChildren(root, "b", "a");
   }

   public void testInsert3()
   {
      IntegerTree root = node(shown("a", 1), hidden("b", 2));
      assertChildren(root, 1);
      assertAllChildren(root, 1, 2);
      assertAllChildren(root, "a", "b");

      //
      root = node(shown("a", 1), hidden("b", 2));
      root.insert(0, shown("c", 3));
      assertChildren(root, 3, 1);
      assertAllChildren(root, 3, 1, 2);
      assertAllChildren(root, "c", "a", "b");

      //
      root = node(shown("a", 1), hidden("b", 2));
      root.insert(1, shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 3, 2);
      assertAllChildren(root, "a", "c", "b");

      //
      root = node(shown("a", 1), hidden("b", 2));
      root.insert(null, shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 3, 2);
      assertAllChildren(root, "a", "c", "b");
   }

   public void testInsert4()
   {
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      assertChildren(root, 1, 3);
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(0, shown("d", 4));
      assertChildren(root, 4, 1, 3);
      assertAllChildren(root, 4, 1, 2, 3);
      assertAllChildren(root, "d", "a", "b", "c");

      //
      root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(1, shown("d", 4));
      assertChildren(root, 1, 4, 3);
      assertAllChildren(root, 1, 4, 2, 3);
      assertAllChildren(root, "a", "d", "b", "c");

      //
      root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(2, shown("d", 4));
      assertChildren(root, 1, 3, 4);
      assertAllChildren(root, 1, 2, 3, 4);
      assertAllChildren(root, "a", "b", "c", "d");

      //
      root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      root.insert(null, shown("d", 4));
      assertChildren(root, 1, 3, 4);
      assertAllChildren(root, 1, 2, 3, 4);
      assertAllChildren(root, "a", "b", "c", "d");
   }

   public void testInsertDuplicate()
   {
      IntegerTree root = node(shown("a", 1));
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
      IntegerTree root = shown("", 0);
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
      IntegerTree root1 = node(a);
      IntegerTree root2 = node();

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
      IntegerTree root1 = node(a);

      //
      root1.insert(0, a);
      assertAllChildren(root1, 1);
      assertAllChildren(root1, "a");
      assertSame(root1, a.getParent());
   }

   public void testInsertReorder2()
   {
      IntegerTree a = shown("a", 1);
      IntegerTree root1 = node(a, shown("b", 2));

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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      assertEquals(1, (int)root.get("a").getElement());
      assertNull(root.get("b"));
      assertNull(root.get("d"));
   }

   public void testGetByKeyWithNoChildren()
   {
      IntegerTree root = shown("", 0);
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      assertNull(root.remove("b"));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      assertEquals(1, (int)root.remove("a").getElement());
      assertAllChildren(root, 2, 3);
      assertAllChildren(root, "b", "c");
   }

   public void testRemoveLast()
   {
      IntegerTree root = node(shown("a", 1), shown("b", 2));
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
      IntegerTree root = shown("", 0);
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
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
      IntegerTree root = shown("", 0);
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));
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
      IntegerTree root = node(shown("a", 1), hidden("b", 2), shown("c", 3));

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
      IntegerTree root = node(hidden("a", 1), shown("b", 2), hidden("c", 3));

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
      IntegerTree root = shown("", 0);

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
}

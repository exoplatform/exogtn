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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationServiceSave extends AbstractTestNavigationService
{

   public void testNonExistingSite() throws Exception
   {
      assertNull(service.loadNavigation(SiteKey.portal("non_existing")));
   }

   public void testSaveNavigation() throws Exception
   {
      NavigationContext nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNull(nav);

      //
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_navigation");

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
      assertNull(nav.state);
      assertNull(nav.rootId);

      //
      assertTrue(service.saveNavigation(nav.getKey(), new NavigationState(5)));
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav.state);
      assertEquals(5, nav.state.getPriority().intValue());

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
      assertEquals(5, (int)nav.state.getPriority());
      assertNotNull(nav.rootId);

      //
      assertTrue(service.saveNavigation(nav.getKey(), null));
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNull(nav.state);
      assertNull(nav.rootId);

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertNull(nav.state);
      assertNull(nav.rootId);

      //
      assertFalse(service.saveNavigation(nav.getKey(), null));
   }


    public void testPendingChangesBypassCache() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "pending_changes_bypass_cache");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("pending_changes_bypass_cache"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      root.addChild("foo");
      service.saveNode(root.context);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      assertNotNull(root.getChild("foo"));
   }

   public void testAddChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "add_child");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("add_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      assertEquals(0, root.getNodeCount());

      // Test what happens when null is added
      try
      {
         root.addChild((String)null);
         fail();
      }
      catch (NullPointerException ignore)
      {
      }

      // Test what happens when an illegal index is added
      try
      {
         root.addChild(-1, "foo");
         fail();
      }
      catch (IndexOutOfBoundsException ignore)
      {
      }
      try
      {
         root.addChild(1, "foo");
         fail();
      }
      catch (IndexOutOfBoundsException ignore)
      {
      }

      //
      Node foo = root.addChild("foo");
      assertNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));
      assertEquals(1, root.getNodeCount());
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      foo = root.getChild("foo");
      assertNotNull(foo);
      assertEquals(1, root.getNodeCount());
      assertEquals("foo", foo.getName());
   }

   public void testRemoveChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "remove_child");
      portal.getRootNavigation().addChild("default").addChild("foo");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("remove_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();

      //
      try
      {
         root.removeChild(null);
         fail();
      }
      catch (NullPointerException e)
      {
      }
      try
      {
         root.removeChild("bar");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      //
      Node foo = root.getChild("foo");
      assertNotNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));

      //
      assertTrue(root.removeChild("foo"));
      assertNull(root.getChild("foo"));
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      foo = root.getChild("foo");
      assertNull(foo);
   }

   public void testRemoveTransientChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "remove_transient_child");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("remove_transient_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      Node foo = root.addChild("foo");
      assertNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));

      //
      assertTrue(root.removeChild("foo"));
      assertNull(root.getChild("foo"));
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      foo = root.getChild("foo");
      assertNull(foo);
   }

   public void testRename() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rename");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a");
      def.addChild("b");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("rename"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.GRANDCHILDREN).getNode();
      try
      {
         root.setName("something");
         fail();
      }
      catch (IllegalStateException e)
      {
      }

      //
      Node a = root.getChild("a");
      assertEquals(0, a.context.getIndex());
      try
      {
         a.setName(null);
         fail();
      }
      catch (NullPointerException e)
      {
      }
      try
      {
         a.setName("b");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      //
      a.setName("c");
      assertEquals("c", a.getName());
      assertEquals(0, a.context.getIndex());
      service.saveNode(a.context);

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("rename"));
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      Node c = root.getChild("c");
      assertNotNull(c);
      // assertEquals(0, c.context.getIndex());
   }

   public void testReorderChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "reorder_child");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      rootNavigation.addChild("juu");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("reorder_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      Iterator<Node> i = root.getChildren().iterator();
      Node foo = i.next();
      assertEquals("foo", foo.getName());
      Node bar = i.next();
      assertEquals("bar", bar.getName());
      Node juu = i.next();
      assertEquals("juu", juu.getName());
      assertFalse(i.hasNext());

      // Test what happens when null is added
      try
      {
         root.addChild(1, (Node)null);
         fail();
      }
      catch (NullPointerException expected)
      {
      }

      // Test what happens when an illegal index is added
      try
      {
         root.addChild(-1, juu);
         fail();
      }
      catch (IndexOutOfBoundsException expected)
      {
      }
      try
      {
         root.addChild(4, juu);
         fail();
      }
      catch (IndexOutOfBoundsException expected)
      {
      }

      //
      root.addChild(1, juu);
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      i = root.getChildren().iterator();
      foo = i.next();
      assertEquals("foo", foo.getName());
      juu = i.next();
      assertEquals("juu", juu.getName());
      bar = i.next();
      assertEquals("bar", bar.getName());
      assertFalse(i.hasNext());

      //
      root.addChild(0, bar);
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      i = root.getChildren().iterator();
      bar = i.next();
      assertEquals("bar", bar.getName());
      foo = i.next();
      assertEquals("foo", foo.getName());
      juu = i.next();
      assertEquals("juu", juu.getName());
      assertFalse(i.hasNext());
   }

   public void _testReorderChild2() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      rootNavigation.addChild("juu");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("reorder_child_2"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      assertEquals("bar", root.getChild(1).getName());
      assertTrue(root.removeChild("bar"));
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      root.addChild("daa");
      Node tab3 = root.getChild(2);
      assertEquals("daa", tab3.getName());
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = new NavigationServiceImpl(mgr).loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      for (Node child : root.getChildren())
      {
         System.out.println("child : " + child.getId());
      }
      tab3 = root.getChild(2);
      assertEquals("daa", tab3.getName());

      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      for (Node child : root.getChildren())
      {
         System.out.println("child : " + child.getId());
      }
      tab3 = root.getChild(2);
      assertEquals("daa", tab3.getName());
   }

   public void _testWeirdBug() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      rootNavigation.addChild("juu");

      //
      sync(true);

      //
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      rootNavigation.getChild("bar").destroy();

      //
      sync(true);

      //
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      rootNavigation.addChild("daa");

      //
      sync(true);

      //
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      Navigation daa = rootNavigation.getChildren().get(2);
      assertEquals("daa", daa.getName());
   }

   public void _testWeirdBug2() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Session session = mop.getModel().getSession().getJCRSession();
      javax.jcr.Node container = session.getRootNode().
         getNode("mop:workspace/mop:portalsites").
         addNode("mop:reorder_child_2").
         getNode("mop:rootnavigation/mop:children").
         addNode("mop:default").
         getNode("mop:children");
      container.addNode("mop:foo");
      container.addNode("mop:bar");
      container.addNode("mop:juu");

      //
      sync(true);

      //
      session = mop.getModel().getSession().getJCRSession();
      container = session.getRootNode().getNode("mop:workspace/mop:portalsites/mop:reorder_child_2/mop:rootnavigation/mop:children/mop:default/mop:children");
      container.getNode("mop:bar").remove();

      //
      sync(true);

      //
      session = mop.getModel().getSession().getJCRSession();
      container = session.getRootNode().getNode("mop:workspace/mop:portalsites/mop:reorder_child_2/mop:rootnavigation/mop:children/mop:default/mop:children");
      container.addNode("mop:daa");
      container.orderBefore("mop:daa", null);

      //
      sync(true);

      //
      container = session.getRootNode().getNode("mop:workspace/mop:portalsites/mop:reorder_child_2/mop:rootnavigation/mop:children/mop:default/mop:children");
      NodeIterator it = container.getNodes();
      assertEquals("mop:foo", it.nextNode().getName());
      assertEquals("mop:juu", it.nextNode().getName());
      assertEquals("mop:daa", it.nextNode().getName());
      assertFalse(it.hasNext());
   }

   public void testMoveChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_child");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo").addChild("juu");
      rootNavigation.addChild("bar");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("move_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      Node foo = root.getChild("foo");
      Node bar = root.getChild("bar");
      Node juu = foo.getChild("juu");
      bar.addChild(juu);
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      foo = root.getChild("foo");
      juu = foo.getChild("juu");
      assertNull(juu);
      bar = root.getChild("bar");
      juu = bar.getChild("juu");
      assertNotNull(juu);
   }

   public void testRenameNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rename_node");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("rename_node"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      Node foo = root.getChild("foo");
      foo.setName("foo");
      service.saveNode(root.context);

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("rename_node"));
      root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      foo = root.getChild("foo");
      foo.setName("bar");
      assertEquals("bar", foo.getName());
      assertSame(foo, root.getChild("bar"));
      service.saveNode(root.context);
      assertEquals("bar", foo.getName());
      assertSame(foo, root.getChild("bar"));

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      Node bar = root.getChild("bar");
      assertNotNull(bar);
      assertSame(bar, root.getChild("bar"));

      //
      root.addChild("foo");
      try
      {
         bar.setName("foo");
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
   }

   public void testSaveChildren() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_children");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("1");
      rootNavigation.addChild("2");
      rootNavigation.addChild("3");
      rootNavigation.addChild("4");
      rootNavigation.addChild("5");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("save_children"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      root.removeChild("5");
      root.removeChild("2");
      root.addChild(0, root.getChild("3"));
      root.addChild(1, root.addChild("."));
      service.saveNode(root.context);
      Iterator<Node> i = root.getChildren().iterator();
      assertEquals("3", i.next().getName());
      assertEquals(".", i.next().getName());
      assertEquals("1", i.next().getName());
      assertEquals("4", i.next().getName());
      assertFalse(i.hasNext());

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      i = root.getChildren().iterator();
      assertEquals("3", i.next().getName());
      assertEquals(".", i.next().getName());
      assertEquals("1", i.next().getName());
      assertEquals("4", i.next().getName());
      assertFalse(i.hasNext());
   }

   public void testSaveRecursive() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_recursive");
      Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("save_recursive"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      Node foo = root.getChild("foo");
      Node bar = foo.addChild("bar");
      bar.addChild("juu");
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      foo = root.getChild("foo");
      bar = foo.getChild("bar");
      assertNotNull(bar.getId());
      Node juu = bar.getChild("juu");
      assertNotNull(juu.getId());
   }

   public void testSaveState() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_state");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("save_state"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.SINGLE).getNode();
      NodeState state = root.getState();
      assertNull(state.getURI());
      assertNull(state.getLabel());
      assertEquals(-1, state.getStartPublicationTime());
      assertEquals(-1, state.getEndPublicationTime());
      long now = System.currentTimeMillis();
      root.setState(new NodeState.Builder().setURI("foo").setEndPublicationTime(now).setLabel("bar").capture());
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.ALL).getNode();
      state = root.getState();
      assertEquals("foo", state.getURI());
      assertEquals("bar", state.getLabel());
      assertEquals(-1, state.getStartPublicationTime());
      assertEquals(now, state.getEndPublicationTime());
      assertNull(state.getVisibility());
   }

   public void _testSaveStateOverwrite() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_state_overwrite");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      root.addChild("foo");
      service.saveNode(root.context);

      //
      sync(true);

      //
      root.addChild("bar");
      service.saveNode(root.context);

      //
      sync(true);

      //
      nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      assertEquals(2, root.getChildren().size());
   }

   public void testRecreateNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "recreate_node");
      portal.getRootNavigation().addChild("default").addChild("foo");

      //
      sync(true);

      //
      NavigationContext nav = service.loadNavigation(SiteKey.portal("recreate_node"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      String fooId = root.getChild("foo").getId();
      assertTrue(root.removeChild("foo"));
      assertNull(root.addChild("foo").getId());
      service.saveNode(root.context);

      //
      sync(true);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN).getNode();
      assertNotNull(root.getChild("foo").getId());
      assertNotSame(fooId, root.getChild("foo").getId());
   }

   public void testSaveMergeNodes() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_merge");
      Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a");
      nav.addChild("b");
      nav.addChild("c");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_merge"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN).getNode();

      //
      sync();

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN).getNode();
      root2.addChild(1, root2.addChild("2"));
      service.saveNode(root2.context);

      //
      sync(true);

      //
      service.saveNode(root1.context);
      root1.addChild(1, root1.addChild("1"));
      service.saveNode(root1.context);
   }

   public void testMoveToAdded() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_to_added");
      Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a").addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN).getNode();
      Node a = root1.getChild("a");
      Node b = a.getChild("b");
      Node c = root1.addChild("c");
      c.addChild(b);
      service.saveNode(root1.context);

      //
      sync(true);

      //
      navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
      root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN).getNode();
      a = root1.getChild("a");
      assertNotNull(a);
      c = root1.getChild("c");
      assertNotNull(c);
      b = c.getChild("b");
      assertNotNull(b);
   }

   public void testMoveFromRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "moved_from_removed");
      Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a").addChild("c");
      nav.addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN).getNode();
      Node a = root1.getChild("a");
      Node b = root1.getChild("b");
      Node c = a.getChild("c");
      b.addChild(c);
      root1.removeChild("a");
      service.saveNode(root1.context);

      //
      sync(true);

      //
      navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
      root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN).getNode();
      assertNull(root1.getChild("a"));
      b = root1.getChild("b");
      assertNotNull(b);
      c = b.getChild("c");
      assertNotNull(c);
   }

   public void testAddToRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "add_to_removed");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("add_to_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("a").addChild("b");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
      }
   }

   public void testRemoveConcurrentRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "remove_removed");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("remove_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.removeChild("a");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      service.saveNode(root.context);
   }

   public void testMoveRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_removed");
      portal.getRootNavigation().addChild("default").addChild("a").addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.addChild(root.getChild("a").getChild("b"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.getChild("a").removeChild("b");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE, e.getError());
      }
   }

   public void testMoveToRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_to_removed");
      portal.getRootNavigation().addChild("default").addChild("a");
      portal.getRootNavigation().getChild("default").addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_to_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("b").addChild(root.getChild("a"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("b");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE, e.getError());
      }
   }

   public void testMoveMoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_moved");
      portal.getRootNavigation().addChild("default").addChild("a");
      portal.getRootNavigation().getChild("default").addChild("b");
      portal.getRootNavigation().getChild("default").addChild("c");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_moved"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("b").addChild(root.getChild("a"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.getChild("c").addChild(root2.getChild("a"));
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE, e.getError());
      }
   }

   public void testConcurrentAddDuplicate() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_add_duplicate");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_add_duplicate"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.addChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      root.addChild("a");
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.ADD_CONCURRENTLY_ADDED_NODE, e.getError());
      }
   }

   public void testConcurrentAddAfterRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_add_after_removed");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_add_after_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.addChild(1, "b");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
      }
   }

   public void testConcurrentMoveAfterRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_move_after_removed");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a").addChild("b");
      def.addChild("c");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_move_after_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.addChild(2, root.getChild("a").getChild("b"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("c");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
      }
   }

   public void testConcurrentMoveFromRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_move_from_removed");
      portal.getRootNavigation().addChild("default").addChild("a").addChild("b");
      portal.getRootNavigation().getChild("default").addChild("c");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_move_from_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("c").addChild(root.getChild("a").getChild("b"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE, e.getError());
      }
   }

   public void testConcurrentRenameRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_rename_removed");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_rename_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("a").setName("b");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE, e.getError());
      }
   }

   public void testSavePhantomNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_save");
      portal.getRootNavigation().addChild("default");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_save"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.addChild("a");
      service.saveNode(root.context);

      //
      sync(true);

      // Reload the root node and modify it
      root = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root.getChild("a").setState(root.getState().builder().setLabel("foo").capture());

      //
      sync(true);

      // Edit navigation in another browser
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      // Now click Save button in the first browser
      try
      {
         service.saveNode(root.context);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
      }
   }

   public void testFoo() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "bilto");
      portal.getRootNavigation().addChild("default").addChild("a");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("bilto"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);

      //
      sync(true);

      //
      service.saveNode(root1.context);
   }
}

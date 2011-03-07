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

import junit.framework.AssertionFailedError;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.AbstractPortalTest;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationService extends AbstractPortalTest
{

   /** . */
   private POMSessionManager mgr;

   /** . */
   private NavigationServiceImpl service;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = PortalContainer.getInstance();
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      service = new NavigationServiceImpl(mgr);

      //
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }

   private void startService()
   {
      try
      {
         begin();
         service.start();
         end();
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

   private void stopService()
   {
      begin();
      service.stop();
      end();
   }

   public void testNonExistingSite() throws Exception
   {
      assertNull(service.getNavigation(SiteKey.portal("non_existing")));
   }

   public void testNavigationInvalidationByRootId() throws Exception
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "get_navigation");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "get_navigation");
      Navigation nav = service.getNavigation(key);
      assertNotNull(nav);
      assertEquals(1, (int)nav.getState().getPriority());
      assertEquals(key, nav.getKey());
      assertNull(nav.getState().getNodeId());
      end();

      //
      startService();
      begin();
      mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "get_navigation").getRootNavigation().addChild("default");
      end(true);
      stopService();

      //
      begin();
      nav = service.getNavigation(key);
      assertNotNull(nav);
      assertEquals(1, (int)nav.getState().getPriority());
      assertEquals(key, nav.getKey());
      assertNotNull(nav.getState().getNodeId());
      end();

      //
      startService();
      begin();
      mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "get_navigation").getRootNavigation().getChild("default").destroy();
      end(true);
      stopService();

      //
      begin();
      nav = service.getNavigation(key);
      assertNotNull(nav);
      assertEquals(1, (int)nav.getState().getPriority());
      assertEquals(key, nav.getKey());
      assertNull(nav.getState().getNodeId());
   }

   public void testNavigationInvalidationByPriority()
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change").getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "invalidation_by_priority_change");
      Navigation nav = service.getNavigation(key);
      assertEquals(1, (int)nav.getState().getPriority());
      end();

      //
      startService();
      begin();
      Site site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, 2);
      end(true);
      stopService();

      //
      begin();
      nav = service.getNavigation(key);
      assertEquals(2, (int)nav.getState().getPriority());
      end();

      //
      startService();
      begin();
      site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, 4);
      end(true);
      stopService();

      //
      begin();
      nav = service.getNavigation(key);
      assertEquals(4, (int)nav.getState().getPriority());
      end();

      //
      startService();
      begin();
      site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, null);
      end(true);
      stopService();

      //
      begin();
      nav = service.getNavigation(key);
      assertEquals(1, (int)nav.getState().getPriority());
   }

   public void testSkipVisitMode() throws Exception
   {
      Navigation nav = service.getNavigation(SiteKey.portal("classic"));
      Node root = service.load(Node.MODEL, nav, new Scope()
      {
         public Visitor get()
         {
            return new Visitor()
            {
               public VisitMode visit(int depth, String id, String name, NodeState state)
               {
                  if (name.equals("webexplorer"))
                  {
                     return VisitMode.SKIP;
                  }
                  else
                  {
                     return VisitMode.ALL_CHILDREN;
                  }
               }
            };
         }
      });
      assertEquals("default", root.getName());
      Iterator<? extends Node> i = root.getChildren().iterator();
      assertTrue(i.hasNext());
      Node home = i.next();
      assertNotNull(home.getChildren());
      assertEquals("home", home.getName());
      assertFalse(i.hasNext());
   }

   public void testLoadSingleScope() throws Exception
   {
      Navigation nav = service.getNavigation(SiteKey.portal("classic"));
      Node root = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(root.getChildren());
      assertEquals("default", root.getName());
   }

   public void testLoadChildrenScope() throws Exception
   {
      Navigation nav = service.getNavigation(SiteKey.portal("classic"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      assertEquals("default", root.getName());
      Iterator<? extends Node> i = root.getChildren().iterator();
      assertTrue(i.hasNext());
      Node home = i.next();
      assertNull(home.getChildren());
      assertEquals("home", home.getName());
      assertTrue(i.hasNext());
      Node webexplorer = i.next();
      assertNull(webexplorer.getChildren());
      assertEquals("webexplorer", webexplorer.getName());
      assertFalse(i.hasNext());
   }

   public void testLoadCustomScope() throws Exception
   {
      Navigation nav = service.getNavigation(SiteKey.portal("large"));
      Node root = service.load(Node.MODEL, nav, new Scope()
      {
         public Visitor get()
         {
            return new Visitor()
            {
               public VisitMode visit(int depth, String id, String name, NodeState state)
               {
                  boolean use = false;
                  switch (depth)
                  {
                     case 0:
                        use = "default".equals(name);
                        break;
                     case 1:
                        use = "b".equals(name);
                        break;
                     case 2:
                        use = "d".equals(name);
                        break;
                  }
                  return use ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
               }
            };
         }
      });
      assertNull(root.getChild("a").getChildren());
      Node b = root.getChild("b");
      Node d = b.getChild("d");
      assertNull(d.getChild("e").getChildren());
   }

   public void testState() throws Exception
   {
      Navigation nav = service.getNavigation(SiteKey.portal("test"));
      Node root = service.load(Node.MODEL, nav, Scope.ALL);
      Iterator<? extends Node> rootIterator = root.getChildren().iterator();
      Node child1 = rootIterator.next();
      Node child2 = rootIterator.next();
      assertFalse(rootIterator.hasNext());
      assertEquals("node_name", child1.getName());
      assertEquals("node_uri", child1.getContext().getState().getURI());
      assertEquals("node_label", child1.getContext().getState().getLabel());
      assertEquals("portal::test::test1", child1.getContext().getState().getPageRef());
      assertEquals(Visibility.TEMPORAL, child1.getContext().getState().getVisibility());
      assertEquals(953602380000L, child1.getContext().getState().getStartPublicationTime());
      assertEquals(1237599180000L, child1.getContext().getState().getEndPublicationTime());
      assertEquals("node_name2", child2.getName());
      assertEquals("node_uri2", child2.getContext().getState().getURI());
      assertEquals("node_label2", child2.getContext().getState().getLabel());
      assertEquals("portal::test::test1", child2.getContext().getState().getPageRef());
      assertEquals(Visibility.DISPLAYED, child2.getContext().getState().getVisibility());
   }

   public void testNodeInvalidationByRemoval() throws Exception
   {
      //
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_removal");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("invalidation_by_removal"));
      Node root = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNotNull(root);
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_removal").getRootNavigation().getChild("default").destroy();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(root);
   }

   public void testNodeInvalidationByChild() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_child");
      portal.getRootNavigation().addChild("default");
      end(true);

      // Put the navigation in the cache
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("invalidation_by_child"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      Iterator<? extends Node> iterator = root.getChildren().iterator();
      assertFalse(iterator.hasNext());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_child").getRootNavigation().getChild("default").addChild("new");
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      iterator = root.getChildren().iterator();
      iterator.next();
      assertFalse(iterator.hasNext());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_child").getRootNavigation().getChild("default").getChild("new").destroy();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      iterator = root.getChildren().iterator();
      assertFalse(iterator.hasNext());
   }

   public void testNodeInvalidationByProperty() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("invalidation_by_propertychange"));
      Node defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getLabel());
      end();

      //
      startService();
      begin();
      Described defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilto");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bilto", defaultNode.getContext().getState().getLabel());
      end();

      //
      startService();
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilta");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bilta", defaultNode.getContext().getState().getLabel());
      end();

      //
      startService();
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName(null);
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getLabel());
   }

   public void testNodeInvalidationByAttribute() throws Exception
   {
      //
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("invalidation_by_attribute"));
      Node defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "foo_uri");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("foo_uri", defaultNode.getContext().getState().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "bar_uri");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bar_uri", defaultNode.getContext().getState().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, null);
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getURI());
   }

   public void testAddChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "add_child");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("add_child"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);

      //
      Node foo = root.addChild("foo");
      assertNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));
      service.save(Node.MODEL, root);
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      foo = root.getChild("foo");
      assertNotNull(foo);
      assertEquals("foo", foo.getName());
   }

   public void testRemoveChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "remove_child");
      portal.getRootNavigation().addChild("default").addChild("foo");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("remove_child"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      Node foo = root.getChild("foo");
      assertNotNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));

      //
      assertTrue(root.removeChild("foo"));
      assertNull(root.getChild("foo"));
      service.save(Node.MODEL, root);
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      foo = root.getChild("foo");
      assertNull(foo);
   }

   public void testReorderChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "reorder_child");
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("reorder_child"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      Iterator<Node> i = root.getChildren().iterator();
      Node foo = i.next();
      assertNotNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));
      Node bar = i.next();
      assertNotNull(bar.getId());
      assertEquals("bar", bar.getName());
      assertSame(bar, root.getChild("bar"));
      assertFalse(i.hasNext());

      //
      root.addChild(foo);
      service.save(Node.MODEL, root);
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      i = root.getChildren().iterator();
      bar = i.next();
      assertNotNull(bar.getId());
      assertEquals("bar", bar.getName());
      assertSame(bar, root.getChild("bar"));
      foo = i.next();
      assertNotNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));
      assertFalse(i.hasNext());
   }

   public void testSaveChildren() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_children");
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("1");
      rootNavigation.addChild("2");
      rootNavigation.addChild("3");
      rootNavigation.addChild("4");
      rootNavigation.addChild("5");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("save_children"));
      Node root = service.load(Node.MODEL, nav, Scope.CHILDREN);
      root.removeChild("5");
      root.removeChild("2");
      root.addChild(0, root.getChild("3"));
      root.addChild(1, root.addChild("."));
      service.save(Node.MODEL, root);
      Iterator<Node> i = root.getChildren().iterator();
      assertEquals("3", i.next().getName());
      assertEquals(".", i.next().getName());
      assertEquals("1", i.next().getName());
      assertEquals("4", i.next().getName());
      assertFalse(i.hasNext());
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.CHILDREN);
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("save_recursive"));
      Node root = service.load(Node.MODEL, nav, Scope.ALL);
      Node foo = root.getChild("foo");
      Node bar = foo.addChild("bar");
      bar.addChild("juu");
      service.save(Node.MODEL, root);
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.ALL);
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
      end(true);

      //
      begin();
      Navigation nav = service.getNavigation(SiteKey.portal("save_recursive"));
      Node root = service.load(Node.MODEL, nav, Scope.SINGLE);
      NodeState state = root.getState();
      assertNull(state.getURI());
      assertNull(state.getLabel());
      assertEquals(-1, state.getStartPublicationTime());
      assertEquals(-1, state.getEndPublicationTime());
      long now = System.currentTimeMillis();
      root.setState(new NodeState.Builder().setURI("foo").setEndPublicationTime(now).setLabel("bar").capture());
      service.save(Node.MODEL, root);
      startService();
      end(true);
      stopService();

      //
      begin();
      root = service.load(Node.MODEL, nav, Scope.ALL);
      state = root.getState();
      assertEquals("foo", state.getURI());
      assertEquals("bar", state.getLabel());
      assertEquals(-1, state.getStartPublicationTime());
      assertEquals(now, state.getEndPublicationTime());
      assertNull(state.getVisibility());
   }
}

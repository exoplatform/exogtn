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
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
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
public class TestNavigationService extends AbstractPortalTest
{

   /** . */
   private POMSessionManager mgr;

   /** . */
   private NavigationServiceImpl service;

   /** . */
   private DataStorage dataStorage;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = PortalContainer.getInstance();
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      service = new NavigationServiceImpl(mgr);
      dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      //
      begin();
   }


   @Override
   protected void end(boolean save)
   {
      if (save)
      {
         try
         {
            startService();
            super.end(save);
         }
         finally
         {
            stopService();
         }
      }
      else
      {
         super.end(save);
      }
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
      assertNull(service.loadNavigation(SiteKey.portal("non_existing")));
   }

   public void testNavigationInvalidationByRootId() throws Exception
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "get_navigation");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "get_navigation");
      Navigation nav = service.loadNavigation(key);
      assertNotNull(nav);
      assertEquals(key, nav.getKey());
      assertNull(nav.getState());
      end();

      //
      begin();
      mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "get_navigation").getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      nav = service.loadNavigation(key);
      assertNotNull(nav);
      assertEquals(1, (int)nav.getState().getPriority());
      assertEquals(key, nav.getKey());
      assertNotNull(nav.getRootId());
      end();

      //
      begin();
      mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "get_navigation").getRootNavigation().getChild("default").destroy();
      end(true);

      //
      begin();
      nav = service.loadNavigation(key);
      assertNotNull(nav);
      assertEquals(key, nav.getKey());
      assertNull(nav.getState());
   }

   public void testNavigationInvalidationByPriority()
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change").getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "invalidation_by_priority_change");
      Navigation nav = service.loadNavigation(key);
      assertEquals(1, (int)nav.getState().getPriority());
      end();

      //
      begin();
      Site site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, 2);
      end(true);

      //
      begin();
      nav = service.loadNavigation(key);
      assertEquals(2, (int)nav.getState().getPriority());
      end();

      //
      begin();
      site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, 4);
      end(true);

      //
      begin();
      nav = service.loadNavigation(key);
      assertEquals(4, (int)nav.getState().getPriority());
      end();

      //
      begin();
      site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change");
      site.getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.PRIORITY, null);
      end(true);

      //
      begin();
      nav = service.loadNavigation(key);
      assertEquals(1, (int)nav.getState().getPriority());
   }

   public void testSaveNavigation() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNull(nav);

      //
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_navigation");
      end(true);

      //
      begin();
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
      assertNull(nav.getState());
      assertNull(nav.getRootId());

      //
      assertTrue(service.saveNavigation(nav.getKey(), new NavigationState(5)));
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav.getState());
      assertEquals(5, nav.getState().getPriority().intValue());
      end(true);
      
      //
      begin();
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
      assertEquals(5, (int)nav.getState().getPriority());
      assertNotNull(nav.getRootId());

      //
      assertTrue(service.saveNavigation(nav.getKey(), null));
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNull(nav.getState());
      assertNull(nav.getRootId());
      end(true);

      //
      begin();
      nav = service.loadNavigation(SiteKey.portal("save_navigation"));
      assertNotNull(nav);
      assertNull(nav.getState());
      assertNull(nav.getRootId());

      //
      assertFalse(service.saveNavigation(nav.getKey(), null));
   }


   public void testLoadSingleScope() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("classic"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertNull(root.getChildren());
      assertEquals("default", root.getName());
      try
      {
         root.getChild(0);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   public void testLoadChildrenScope() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("classic"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertEquals("default", root.getName());
      Iterator<? extends Node> i = root.getChildren().iterator();
      assertTrue(i.hasNext());
      Node home = i.next();
      assertSame(home, root.getChild(0));
      assertNull(home.getChildren());
      assertEquals("home", home.getName());
      assertTrue(i.hasNext());
      Node webexplorer = i.next();
      assertNull(webexplorer.getChildren());
      assertSame(webexplorer, root.getChild(1));
      assertEquals("webexplorer", webexplorer.getName());
      assertFalse(i.hasNext());
   }

   public void testLoadCustomScope() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("large"));
      Node root = service.loadNode(Node.MODEL, nav, new Scope()
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

   public void testLoadNode() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("large"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Node a = root.getChild("a");
      assertNotNull(a);
      assertNull(a.getChildren());
      assertSame(a, service.loadNode(Node.MODEL, a, Scope.CHILDREN));
      assertNotNull(a.getChildren());
      assertEquals(1, a.getChildren().size());
      Node c = a.getChild("c");
      assertEquals("c", c.getName());
      assertSame(a, c.getParent());
      assertSame(a, service.loadNode(Node.MODEL, a, Scope.SINGLE));
      assertNotNull(a.getChildren());
      assertEquals(1, a.getChildren().size());
      assertSame(c, a.getChild("c"));
      assertNotNull(c.getParent());
   }

   public void testState() throws Exception
   {
      Navigation nav = service.loadNavigation(SiteKey.portal("test"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL);
      assertEquals(5, root.getNodeCount());
      Node child1 = root.getChild("node_name");
      Node child2 = root.getChild("node_name4");
      assertEquals("node_name", child1.getName());
      assertEquals("node_uri", child1.getContext().getState().getURI());
      assertEquals("node_label", child1.getContext().getState().getLabel());
      assertEquals("portal::test::test1", child1.getContext().getState().getPageRef());
      assertEquals(Visibility.TEMPORAL, child1.getContext().getState().getVisibility());
      assertEquals(953602380000L, child1.getContext().getState().getStartPublicationTime());
      assertEquals(1237599180000L, child1.getContext().getState().getEndPublicationTime());
      assertEquals("node_name4", child2.getName());
      assertEquals("node_uri", child2.getContext().getState().getURI());
      assertEquals("node_label4", child2.getContext().getState().getLabel());
      assertEquals("portal::test::test1", child2.getContext().getState().getPageRef());
      assertEquals(Visibility.DISPLAYED, child2.getContext().getState().getVisibility());
   }

   public void testHiddenNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "hidden_node");
      org.gatein.mop.api.workspace.Navigation defaultNav = portal.getRootNavigation().addChild("default");
      defaultNav.addChild("a");
      defaultNav.addChild("b");
      defaultNav.addChild("c");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("hidden_node"));

      //
      Node root;
      Node a;
      Node b;
      Node c;

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      a.setHidden(true);
      assertEquals(2, root.getChildren().size());
      assertNull(root.getChild("a"));
      assertEquals("b", root.getChild(0).getName());
      try
      {
         root.getChild(2);
         fail();
      }
      catch (IndexOutOfBoundsException ignore)
      {
      }
      assertFalse(root.removeChild("a"));
      try
      {
         b.setName("a");
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      b.setHidden(true);
      assertSame(a, root.getChild(0));
      assertSame(c, root.getChild(1));
      try
      {
         root.getChild(2);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      a.setHidden(true);
      c.setHidden(true);
      assertSame(b, root.getChild(0));
      try
      {
         root.getChild(1);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
   }

   public void testHiddenInsert1() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "hidden_insert_1");
      org.gatein.mop.api.workspace.Navigation defaultNav = portal.getRootNavigation().addChild("default");
      defaultNav.addChild("a");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("hidden_insert_1"));

      //
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Node a = root.getChild("a");
      a.setHidden(true);
      Node b = root.addChild("b");
      assertEquals(1, root.getChildren().size());
      assertSame(b, root.getChildren().iterator().next());
      a.setHidden(false);
      assertEquals(2, root.getChildren().size());
      Iterator<Node> it = root.getChildren().iterator();
      assertSame(b, it.next());
      assertSame(a, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      a.setHidden(true);
      b = root.addChild(0, "b");
      assertEquals(1, root.getChildren().size());
      assertSame(b, root.getChildren().iterator().next());
      a.setHidden(false);
      assertEquals(2, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(b, it.next());
      assertSame(a, it.next());
   }

   public void testHiddenInsert2() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "hidden_insert_2");
      org.gatein.mop.api.workspace.Navigation defaultNav = portal.getRootNavigation().addChild("default");
      defaultNav.addChild("a");
      defaultNav.addChild("b");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("hidden_insert_2"));

      //
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Node a = root.getChild("a");
      Node b = root.getChild("b");
      b.setHidden(true);
      Node c = root.addChild(0, "c");
      assertEquals(2, root.getChildren().size());
      Iterator<Node> it = root.getChildren().iterator();
      assertSame(c, it.next());
      assertSame(a, it.next());
      b.setHidden(false);
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(c, it.next());
      assertSame(a, it.next());
      assertSame(b, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      b.setHidden(true);
      c = root.addChild(1, "c");
      assertEquals(2, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      b.setHidden(false);
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      assertSame(b, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      b.setHidden(true);
      c = root.addChild("c");
      assertEquals(2, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      b.setHidden(false);
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      assertSame(b, it.next());
   }

   public void testHiddenInsert3() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "hidden_insert_3");
      org.gatein.mop.api.workspace.Navigation defaultNav = portal.getRootNavigation().addChild("default");
      defaultNav.addChild("a");
      defaultNav.addChild("b");
      defaultNav.addChild("c");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("hidden_insert_3"));

      //
      Node root,a,b,c,d;
      Iterator<Node> it;

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      b.setHidden(true);
      d = root.addChild(0, "d");
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(d, it.next());
      assertSame(a, it.next());
      assertSame(c, it.next());
      b.setHidden(false);
      assertEquals(4, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(d, it.next());
      assertSame(a, it.next());
      assertSame(b, it.next());
      assertSame(c, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      b.setHidden(true);
      d = root.addChild(1, "d");
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(d, it.next());
      assertSame(c, it.next());
      b.setHidden(false);
      assertEquals(4, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(d, it.next());
      assertSame(b, it.next());
      assertSame(c, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      b.setHidden(true);
      d = root.addChild(2, "d");
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      assertSame(d, it.next());
      b.setHidden(false);
      assertEquals(4, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(b, it.next());
      assertSame(c, it.next());
      assertSame(d, it.next());

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      a = root.getChild("a");
      b = root.getChild("b");
      c = root.getChild("c");
      b.setHidden(true);
      d = root.addChild("d");
      assertEquals(3, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(c, it.next());
      assertSame(d, it.next());
      b.setHidden(false);
      assertEquals(4, root.getChildren().size());
      it = root.getChildren().iterator();
      assertSame(a, it.next());
      assertSame(b, it.next());
      assertSame(c, it.next());
      assertSame(d, it.next());
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
      Navigation nav = service.loadNavigation(SiteKey.portal("invalidation_by_removal"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertNotNull(root);
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_removal").getRootNavigation().getChild("default").destroy();
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
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
      Navigation nav = service.loadNavigation(SiteKey.portal("invalidation_by_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Iterator<? extends Node> iterator = root.getChildren().iterator();
      assertFalse(iterator.hasNext());
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_child").getRootNavigation().getChild("default").addChild("new");
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      iterator = root.getChildren().iterator();
      iterator.next();
      assertFalse(iterator.hasNext());
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_child").getRootNavigation().getChild("default").getChild("new").destroy();
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      Navigation nav = service.loadNavigation(SiteKey.portal("invalidation_by_propertychange"));
      Node defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getLabel());
      end();

      //
      begin();
      Described defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilto");
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bilto", defaultNode.getContext().getState().getLabel());
      end();

      //
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilta");
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bilta", defaultNode.getContext().getState().getLabel());
      end();

      //
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName(null);
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
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
      Navigation nav = service.loadNavigation(SiteKey.portal("invalidation_by_attribute"));
      Node defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getURI());
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "foo_uri");
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("foo_uri", defaultNode.getContext().getState().getURI());
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "bar_uri");
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertEquals("bar_uri", defaultNode.getContext().getState().getURI());
      end();

      //
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, null);
      end(true);

      //
      begin();
      defaultNode = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      assertNull(defaultNode.getContext().getState().getURI());
   }

   public void testPendingChangesBypassCache() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "pending_changes_bypass_cache");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("pending_changes_bypass_cache"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      root.addChild("foo");
      service.saveNode(Node.MODEL, root);

      //
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertNotNull(root.getChild("foo"));
   }

   public void testAddChild() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "add_child");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("add_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertEquals(0, root.getNodeCount());

      //
      Node foo = root.addChild("foo");
      assertNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));
      assertEquals(1, root.getNodeCount());
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("remove_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Node foo = root.getChild("foo");
      assertNotNull(foo.getId());
      assertEquals("foo", foo.getName());
      assertSame(foo, root.getChild("foo"));

      //
      assertTrue(root.removeChild("foo"));
      assertNull(root.getChild("foo"));
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      rootNavigation.addChild("juu");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("reorder_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      Iterator<Node> i = root.getChildren().iterator();
      Node foo = i.next();
      assertEquals("foo", foo.getName());
      Node bar = i.next();
      assertEquals("bar", bar.getName());
      Node juu = i.next();
      assertEquals("juu", juu.getName());
      assertFalse(i.hasNext());

      //
      root.addChild(1, juu);
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      rootNavigation.addChild("juu");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("reorder_child_2"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertEquals("bar", root.getChild(1).getName());
      assertTrue(root.removeChild("bar"));
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      root.addChild("daa");
      Node tab3 = root.getChild(2);
      assertEquals("daa", tab3.getName());
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = new NavigationServiceImpl(mgr).loadNode(Node.MODEL, nav, Scope.CHILDREN);
      for (Node child : root.getChildren())
      {
         System.out.println("child : " + child.getId());
      }
      tab3 = root.getChild(2);
      assertEquals("daa", tab3.getName());

      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      rootNavigation.addChild("bar");
      rootNavigation.addChild("juu");
      end(true);

      //
      begin();
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      rootNavigation.getChild("bar").destroy();
      end(true);

      //
      begin();
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      rootNavigation.addChild("daa");
      end(true);

      //
      begin();
      portal = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "reorder_child_2");
      rootNavigation = portal.getRootNavigation().getChild("default");
      org.gatein.mop.api.workspace.Navigation daa = rootNavigation.getChildren().get(2);
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
      end(true);

      //
      begin();
      session = mop.getModel().getSession().getJCRSession();
      container = session.getRootNode().getNode("mop:workspace/mop:portalsites/mop:reorder_child_2/mop:rootnavigation/mop:children/mop:default/mop:children");
      container.getNode("mop:bar").remove();
      end(true);

      //
      begin();
      session = mop.getModel().getSession().getJCRSession();
      container = session.getRootNode().getNode("mop:workspace/mop:portalsites/mop:reorder_child_2/mop:rootnavigation/mop:children/mop:default/mop:children");
      container.addNode("mop:daa");
      container.orderBefore("mop:daa", null);
      end(true);

      //
      begin();
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo").addChild("juu");
      rootNavigation.addChild("bar");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("move_child"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL);
      Node foo = root.getChild("foo");
      Node bar = root.getChild("bar");
      Node juu = foo.getChild("juu");
      bar.addChild(juu);
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.ALL);
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("foo");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("rename_node"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL);
      Node foo = root.getChild("foo");
      foo.setName("foo");
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      nav = service.loadNavigation(SiteKey.portal("rename_node"));
      root = service.loadNode(Node.MODEL, nav, Scope.ALL);
      foo = root.getChild("foo");
      foo.setName("bar");
      assertEquals("bar", foo.getName());
      assertSame(foo, root.getChild("bar"));
      service.saveNode(Node.MODEL, root);
      assertEquals("bar", foo.getName());
      assertSame(foo, root.getChild("bar"));
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.ALL);
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
      org.gatein.mop.api.workspace.Navigation rootNavigation = portal.getRootNavigation().addChild("default");
      rootNavigation.addChild("1");
      rootNavigation.addChild("2");
      rootNavigation.addChild("3");
      rootNavigation.addChild("4");
      rootNavigation.addChild("5");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("save_children"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      root.removeChild("5");
      root.removeChild("2");
      root.addChild(0, root.getChild("3"));
      root.addChild(1, root.addChild("."));
      service.saveNode(Node.MODEL, root);
      Iterator<Node> i = root.getChildren().iterator();
      assertEquals("3", i.next().getName());
      assertEquals(".", i.next().getName());
      assertEquals("1", i.next().getName());
      assertEquals("4", i.next().getName());
      assertFalse(i.hasNext());
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
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
      Navigation nav = service.loadNavigation(SiteKey.portal("save_recursive"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.ALL);
      Node foo = root.getChild("foo");
      Node bar = foo.addChild("bar");
      bar.addChild("juu");
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.ALL);
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
      Navigation nav = service.loadNavigation(SiteKey.portal("save_state"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.SINGLE);
      NodeState state = root.getState();
      assertNull(state.getURI());
      assertNull(state.getLabel());
      assertEquals(-1, state.getStartPublicationTime());
      assertEquals(-1, state.getEndPublicationTime());
      long now = System.currentTimeMillis();
      root.setState(new NodeState.Builder().setURI("foo").setEndPublicationTime(now).setLabel("bar").capture());
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.ALL);
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
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      root.addChild("foo");
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root.addChild("bar");
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertEquals(2, root.getChildren().size());
   }

   public void testDelayBetweenServices() throws Exception
   {
      PortalConfig portalConfig = new PortalConfig();
      portalConfig.setName("testPortalNavigation");
      dataStorage.create(portalConfig);

      PageNavigation pageNav = new PageNavigation();
      pageNav.setOwnerType("portal");
      pageNav.setOwnerId("testPortalNavigation");

      dataStorage.create(pageNav);

      pageNav = dataStorage.getPageNavigation("portal", "testPortalNavigation");
      assertNotNull(pageNav);
      assertEquals("portal", pageNav.getOwnerType());
      assertEquals("testPortalNavigation", pageNav.getOwnerId());

      Navigation navigation = service.loadNavigation(SiteKey.portal("testPortalNavigation"));

      assertNotNull(navigation);
      assertEquals(SiteType.PORTAL, navigation.getKey().getType());
      assertEquals("testPortalNavigation", navigation.getKey().getName());
   }

   public void testRecreateNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "recreate_node");
      portal.getRootNavigation().addChild("default").addChild("foo");
      end(true);

      //
      begin();
      Navigation nav = service.loadNavigation(SiteKey.portal("recreate_node"));
      Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      String fooId = root.getChild("foo").getId();
      assertTrue(root.removeChild("foo"));
      assertNull(root.addChild("foo").getId());
      service.saveNode(Node.MODEL, root);
      end(true);

      //
      begin();
      root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN);
      assertNotNull(root.getChild("foo").getId());
      assertNotSame(fooId, root.getChild("foo").getId());
   }

   public void testSaveMergeNodes() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "save_merge");
      org.gatein.mop.api.workspace.Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a");
      nav.addChild("b");
      nav.addChild("c");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("save_merge"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN);
      end();

      //
      begin();
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN);
      root2.addChild(1, root2.addChild("2"));
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      service.saveNode(Node.MODEL, root1);
      root1.addChild(1, root1.addChild("1"));
      service.saveNode(Node.MODEL, root1);
   }

   public void testMoveToAdded() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_to_added");
      org.gatein.mop.api.workspace.Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a").addChild("b");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN);
      Node a = root1.getChild("a");
      Node b = a.getChild("b");
      Node c = root1.addChild("c");
      c.addChild(b);
      service.saveNode(Node.MODEL, root1);
      end(true);

      //
      begin();
      navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
      root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN);
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
      org.gatein.mop.api.workspace.Navigation nav = portal.getRootNavigation().addChild("default");
      nav.addChild("a").addChild("c");
      nav.addChild("b");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN);
      Node a = root1.getChild("a");
      Node b = root1.getChild("b");
      Node c = a.getChild("c");
      b.addChild(c);
      root1.removeChild("a");
      service.saveNode(Node.MODEL, root1);
      end(true);

      //
      begin();
      navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
      root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN);
      assertNull(root1.getChild("a"));
      b = root1.getChild("b");
      assertNotNull(b);
      c = b.getChild("c");
      assertNotNull(c);
   }

   public void testCount()
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "count");
      org.gatein.mop.api.workspace.Navigation nav = portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("count"));
      Node root;

      //
      root = service.loadNode(Node.MODEL, navigation, Scope.SINGLE);
      assertEquals(0, root.getNodeCount());
      assertEquals(-1, root.getSize());

      //
      root = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN);
      assertEquals(0, root.getNodeCount());
      assertEquals(0, root.getSize());
      Node a = root.addChild("a");
      assertEquals(1, root.getNodeCount());
      assertEquals(1, root.getSize());
      a.setHidden(true);
      assertEquals(0, root.getNodeCount());
      assertEquals(1, root.getSize());
   }

   public void testAddToRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "add_to_removed");
      portal.getRootNavigation().addChild("default").addChild("a");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("add_to_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.getChild("a").addChild("b");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.removeChild("a");
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
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
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("remove_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.removeChild("a");
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.removeChild("a");
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      service.saveNode(Node.MODEL, root);
   }

   public void testMoveRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "move_removed");
      portal.getRootNavigation().addChild("default").addChild("a").addChild("b");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("move_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.addChild(root.getChild("a").getChild("b"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.getChild("a").removeChild("b");
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
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
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("move_to_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.getChild("b").addChild(root.getChild("a"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.removeChild("b");
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
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
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("move_moved"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.getChild("b").addChild(root.getChild("a"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.getChild("c").addChild(root2.getChild("a"));
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE, e.getError());
      }
   }

   public void testConcurrentMoveFromRemoved() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_move_from_removed");
      portal.getRootNavigation().addChild("default").addChild("a").addChild("b");
      portal.getRootNavigation().getChild("default").addChild("c");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("concurrent_move_from_removed"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.getChild("c").addChild(root.getChild("a").getChild("b"));
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.removeChild("a");
      service.saveNode(Node.MODEL, root2);
      end(true);

      //
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE, e.getError());
      }
   }

   public void testSavePhantomNode() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "concurrent_save");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      Navigation navigation = service.loadNavigation(SiteKey.portal("concurrent_save"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root.addChild("foo");
      service.saveNode(Node.MODEL, root);
      end(true);

      // Reload the root node
      begin();
      root = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      end(true);


      // Edit navigation in another browser
      begin();
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      root2.removeChild("foo");
      service.saveNode(Node.MODEL, root2);
      end(true);

      // Now click Save button in the first browser
      begin();
      try
      {
         service.saveNode(Node.MODEL, root);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
      }
   }
}

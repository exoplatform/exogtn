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

   public void testNavigationInvalidationByRootId() throws Exception
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "get_navigation");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "get_navigation");
      Navigation nav = service.getNavigation(key);
      assertNotNull(nav);
      assertEquals(1, (int)nav.getPriority());
      assertEquals(key, nav.getKey());
      assertNull(nav.getNodeId());
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
      assertEquals(1, (int)nav.getPriority());
      assertEquals(key, nav.getKey());
      assertNotNull(nav.getNodeId());
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
      assertEquals(1, (int)nav.getPriority());
      assertEquals(key, nav.getKey());
      assertNull(nav.getNodeId());
   }

   public void testNavigationInvalidationByPriority()
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_priority_change").getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      SiteKey key = new SiteKey(SiteType.PORTAL, "invalidation_by_priority_change");
      Navigation nav = service.getNavigation(key);
      assertEquals(1, (int)nav.getPriority());
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
      assertEquals(2, (int)nav.getPriority());
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
      assertEquals(4, (int)nav.getPriority());
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
      assertEquals(1, (int)nav.getPriority());
   }

   public void testLoadSingleScope() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "classic");
      Node root = service.load(rootId, Scope.SINGLE);
      assertNull(root.getRelationships());
      assertEquals(rootId, root.getId());
      assertEquals("default", root.getName());
   }

   public void testLoadChildrenScope() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "classic");
      Node root = service.load(rootId, Scope.CHILDREN);
      assertEquals(rootId, root.getId());
      assertEquals("default", root.getName());
      Iterator<? extends Node> i = root.getRelationships().getChildren().iterator();
      assertTrue(i.hasNext());
      Node home = i.next();
      assertNull(home.getRelationships());
      assertEquals("home", home.getName());
      assertTrue(i.hasNext());
      Node webexplorer = i.next();
      assertNull(webexplorer.getRelationships());
      assertEquals("webexplorer", webexplorer.getName());
      assertFalse(i.hasNext());
   }

   public void testLoadCustomScope() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "large");
      Node root = service.load(rootId, new Scope()
      {
         public Visitor get()
         {
            return new Visitor()
            {
               public VisitMode visit(int depth, String nodeId, String nodeName)
               {
                  boolean use = false;
                  switch (depth)
                  {
                     case 0:
                        use = "default".equals(nodeName);
                        break;
                     case 1:
                        use = "b".equals(nodeName);
                        break;
                     case 2:
                        use = "d".equals(nodeName);
                        break;
                  }
                  return use ? VisitMode.CHILDREN : VisitMode.NODE;
               }
            };
         }
      });
      assertNull(root.getRelationships().getChild("a").getRelationships());
      Node b = root.getRelationships().getChild("b");
      Node d = b.getRelationships().getChild("d");
      assertNull(d.getRelationships().getChild("e").getRelationships());
   }

   public void testState() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "test");
      Node root = service.load(rootId, Scope.ALL);
      Iterator<? extends Node> rootIterator = root.getRelationships().getChildren().iterator();
      Node child1 = rootIterator.next();
      Node child2 = rootIterator.next();
      assertFalse(rootIterator.hasNext());
      assertEquals("node_name", child1.getName());
      assertEquals("node_uri", child1.getData().getURI());
      assertEquals("node_label", child1.getData().getLabel());
      assertEquals("portal::test::test1", child1.getData().getPageRef());
      assertEquals(Visibility.TEMPORAL, child1.getData().getVisibility());
      assertEquals(953602380000L, child1.getData().getStartPublicationTime());
      assertEquals(1237599180000L, child1.getData().getEndPublicationTime());
      assertEquals("node_name2", child2.getName());
      assertEquals("node_uri2", child2.getData().getURI());
      assertEquals("node_label2", child2.getData().getLabel());
      assertEquals("portal::test::test1", child2.getData().getPageRef());
      assertEquals(Visibility.DISPLAYED, child2.getData().getVisibility());
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
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_removal");
      assertNotNull(rootId);
      Node root = service.load(rootId, Scope.SINGLE);
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
      root = service.load(rootId, Scope.SINGLE);
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
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_child");
      assertNotNull(rootId);
      Node root = service.load(rootId, Scope.CHILDREN);
      Iterator<? extends Node> iterator = root.getRelationships().getChildren().iterator();
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
      root = service.load(rootId, Scope.CHILDREN);
      iterator = root.getRelationships().getChildren().iterator();
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
      root = service.load(rootId, Scope.CHILDREN);
      iterator = root.getRelationships().getChildren().iterator();
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
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_propertychange");
      assertNotNull(rootId);
      Node defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getLabel());
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
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("bilto", defaultNode.getData().getLabel());
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
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("bilta", defaultNode.getData().getLabel());
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
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getLabel());
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
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_attribute");
      Node defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "foo_uri");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("foo_uri", defaultNode.getData().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, "bar_uri");
      end(true);
      stopService();

      //
      begin();
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("bar_uri", defaultNode.getData().getURI());
      end();

      //
      startService();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_attribute").getRootNavigation().getChild("default").getAttributes().setValue(MappedAttributes.URI, null);
      end(true);
      stopService();

      //
      begin();
      rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_attribute");
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getURI());
   }
}

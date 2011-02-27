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
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

   public void testLoadSingleCustom() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "classic");
      Node root = service.load(rootId, Scope.SINGLE);
      assertFalse(root instanceof Node.Fragment);
      assertEquals(rootId, root.getId());
      assertEquals("default", root.getName());
   }

   public void testLoadChildrenCustom() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "classic");
      Node root = service.load(rootId, Scope.CHILDREN);
      assertTrue(root instanceof Node.Fragment);
      Node.Fragment fragment = (Node.Fragment)root;
      assertEquals(rootId, fragment.getId());
      assertEquals("default", fragment.getName());
      Iterator<? extends Node> i = fragment.getChildren().iterator();
      assertTrue(i.hasNext());
      Node home = i.next();
      assertFalse(home instanceof Node.Fragment);
      assertEquals("home", home.getName());
      assertTrue(i.hasNext());
      Node webexplorer = i.next();
      assertFalse(webexplorer instanceof Node.Fragment);
      assertEquals("webexplorer", webexplorer.getName());
      assertFalse(i.hasNext());
   }

   public void testLoadCustomScope() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "large");
      Node.Fragment root = (Node.Fragment)service.load(rootId, new Scope()
      {
         public Visitor get()
         {
            return new Visitor()
            {
               final List<String> names = Arrays.asList("default", "b", "d");
               public boolean children(String nodeId, String nodeName)
               {
                  return names.contains(nodeName);
               }
            };
         }
      });
      assertFalse(root.getChild("a") instanceof Node.Fragment);
      Node.Fragment b = (Node.Fragment)root.getChild("b");
      assertFalse(b.getChild("c") instanceof Node.Fragment);
      Node.Fragment d = (Node.Fragment)b.getChild("d");
      assertFalse(d.getChild("e") instanceof Node.Fragment);
   }

   public void testInvalidationByRemoval() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_removal");
      portal.getRootNavigation().addChild("default");
      end(true);

      // Put the navigation in the cache
      begin();
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_removal");
      assertNotNull(rootId);
      Node root = service.load(rootId, Scope.SINGLE);
      assertNotNull(root);
      end();

      // Start invalidation
      startService();

      // Remove the navigation
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_removal").getRootNavigation().getChild("default").destroy();
      end(true);

      //
      stopService();

      // Let's check cache is now empty
      begin();
      root = service.load(rootId, Scope.SINGLE);
      assertNull(root);
   }

   public void testInvalidationByAddChild() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_childadd");
      portal.getRootNavigation().addChild("default");
      end(true);

      // Put the navigation in the cache
      begin();
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_childadd");
      assertNotNull(rootId);
      Node.Fragment root = (Node.Fragment)service.load(rootId, Scope.CHILDREN);
      Iterator<? extends Node> iterator = root.getChildren().iterator();
      assertFalse(iterator.hasNext());
      end();

      // Start invalidation
      startService();

      // Add a child navigation
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_childadd").getRootNavigation().getChild("default").addChild("new");
      end(true);

      //
      stopService();

      // Let's check cache is now empty
      begin();
      root = (Node.Fragment)service.load(rootId, Scope.CHILDREN);
      iterator = root.getChildren().iterator();
      iterator.next();
      assertFalse(iterator.hasNext());
   }

   public void testInvalidationByProperty() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange");
      portal.getRootNavigation().addChild("default");
      end(true);

      // Put the navigation in the cache
      begin();
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_propertychange");
      assertNotNull(rootId);
      Node defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getLabel());
      end();

      // Start invalidation
      startService();

      //
      begin();
      Described defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilto");
      end(true);

      //
      stopService();

      //
      begin();
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("bilto", defaultNode.getData().getLabel());
      end();

      //
      startService();

      //
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName("bilta");
      end(true);

      //
      stopService();

      //
      begin();
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertEquals("bilta", defaultNode.getData().getLabel());
      end();

      //
      startService();

      //
      begin();
      defaultDescribed = mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_propertychange").getRootNavigation().getChild("default").adapt(Described.class);
      defaultDescribed.setName(null);
      end(true);

      //
      stopService();

      //
      begin();
      defaultNode = service.load(rootId, Scope.SINGLE);
      assertNull(defaultNode.getData().getLabel());
   }
}

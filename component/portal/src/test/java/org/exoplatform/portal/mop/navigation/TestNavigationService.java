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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.AbstractPortalTest;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import java.util.Collections;
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

   public void testLoadSingle() throws Exception
   {
      String rootId = service.getRootId(SiteType.PORTAL, "classic");
      Node root = service.load(rootId, Scope.SINGLE);
      assertFalse(root instanceof Node.Fragment);
      assertEquals(rootId, root.getId());
      assertEquals("default", root.getName());
   }

   public void testLoadChildren() throws Exception
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

   public void testInvalidationByRemoval() throws Exception
   {
      // Create a navigation
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "invalidation_by_removal");
      portal.getRootNavigation().addChild("default");
      end(true);
      begin();

      // Start invalidation
      service.start();

      // Put the navigation in the cache
      String rootId = service.getRootId(SiteType.PORTAL, "invalidation_by_removal");
      assertNotNull(rootId);
      Node root = service.load(rootId, Scope.SINGLE);
      assertNotNull(root);

      // Remove the navigation
      end();
      begin();
      mop.getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "invalidation_by_removal").getRootNavigation().getChild("default").destroy();
      end(true);
      begin();

      // Let's check cache is now empty
      root = service.load(rootId, Scope.SINGLE);
      assertNull(root);
   }
}

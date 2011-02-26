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
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSessionManager;

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
   private NavigationService service;

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
      assertTrue(root instanceof Node.Data);
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
      assertTrue(home instanceof Node.Data);
      assertEquals("home", home.getName());
      assertTrue(i.hasNext());
      Node webexplorer = i.next();
      assertTrue(webexplorer instanceof Node.Data);
      assertEquals("webexplorer", webexplorer.getName());
      assertFalse(i.hasNext());
   }
}

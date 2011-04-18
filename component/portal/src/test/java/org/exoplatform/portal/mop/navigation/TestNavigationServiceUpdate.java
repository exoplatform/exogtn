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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationServiceUpdate extends AbstractTestNavigationService
{

   public void testAddFirst() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_add_first");
      portal.getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_add_first"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      assertEquals(0, root1.getNodeSize());
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.addChild("a");
      service.saveNode(root2.context);
      end(true);

      //
      begin();
      service.updateNode(root1);
      assertEquals(1, root1.getNodeSize());
      Node a = root1.getNode(0);
      assertEquals("a", a.getName());

   }

   public void testAddSecond() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_add_second");
      portal.getRootNavigation().addChild("default").addChild("a");
      end(true);

      //
      begin();
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_add_second"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      assertEquals(1, root1.getNodeSize());
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.addChild("b");
      service.saveNode(root2.context);
      end(true);

      //
      begin();
      service.updateNode(root1);
      assertEquals(2, root1.getNodeSize());
      assertEquals("a", root1.getNode(0).getName());
      assertEquals("b", root1.getNode(1).getName());
   }

   public void testRemove() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_remove");
      portal.getRootNavigation().addChild("default").addChild("a");
      end(true);

      //
      begin();
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("update_remove"));
      NodeContext<Node> root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL);
      assertEquals(1, root1.getNodeSize());
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL).getNode();
      root2.removeChild("a");
      service.saveNode(root2.context);
      end(true);

      //
      begin();
      service.updateNode(root1);
      assertEquals(0, root1.getNodeSize());
//      assertEquals("a", root1.getNode(0).getName());
//      assertEquals("b", root1.getNode(1).getName());
   }
}

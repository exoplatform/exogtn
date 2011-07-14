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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.*;
import org.gatein.common.util.Tools;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.core.api.MOPService;

import java.util.Locale;
import java.util.Map;

import static org.exoplatform.portal.mop.importer.Builder.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationImporter extends AbstractTestNavigationService
{

   public void testMergeCreateNavigation()
   {
      testCreate(ImportMode.MERGE);
   }

   public void testConserveCreateNavigation()
   {
      testCreate(ImportMode.CONSERVE);
   }

   public void testReimportCreateNavigation()
   {
      testCreate(ImportMode.REIMPORT);
   }

   private void testCreate(ImportMode mode)
   {
      String name = mode.name() + "_create_navigation";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));
      PageNavigation src = new PageNavigation("portal", name);
      src.setPriority(2);
      NavigationImporter merge = new NavigationImporter(Locale.ENGLISH, mode, src, service, descriptionService);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal(name));
      assertEquals(2, (int)ctx.getState().getPriority());
   }

   public void testMergeCreate()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_create");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_create")));

      //
      FragmentBuilder builder = fragment().add(node("a"));

      //
      PageNavigation src = new PageNavigation("portal", "merge_create").addFragment(builder.build());
      NavigationImporter merge = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_create"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      NodeContext<?> a = node.get("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals("a", a.getState().getLabel());
      assertEquals(0, a.getNodeCount());
   }

   public void testMergeNested()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_nested");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_nested")));

      //
      FragmentBuilder builder = fragment().add(node("a").add(node("b")));

      //
      PageNavigation src = new PageNavigation("portal", "merge_nested").addFragment(builder.build());
      NavigationImporter merge = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_nested"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      NodeContext<?> a = node.get("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals("a", a.getState().getLabel());
      assertEquals(1, a.getNodeCount());
   }

   public void testCreateMerge()
   {
      testMerge(ImportMode.CONSERVE);
   }

   public void testMergeMerge()
   {
      testMerge(ImportMode.MERGE);
   }

   public void testReimportMerge()
   {
      testMerge(ImportMode.REIMPORT);
   }

   private void testMerge(ImportMode importMode)
   {
      String name = importMode.name() + "_merge_merge";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));

      //
      FragmentBuilder builder = fragment().add(node("a").add(node("b")));

      //
      PageNavigation src = new PageNavigation("portal", name).addFragment(builder.build());
      NavigationImporter merge = new NavigationImporter(Locale.ENGLISH, ImportMode.CONSERVE, src, service, descriptionService);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal(name));
      Node node = service.loadNode(Node.MODEL, ctx, Scope.ALL, null).getNode();
      Node a = node.getChild("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals(1, a.getNodeCount());
      Node b = a.getChild("b");
      assertNotNull(b);
      assertEquals("b", b.getName());
      assertEquals(0, b.getNodeCount());

      //
      builder = fragment().add(node("a").add(node("d"))).add(node("c"));
      src = new PageNavigation("portal", name).addFragment(builder.build());
      merge = new NavigationImporter(Locale.ENGLISH, importMode, src, service, descriptionService);
      merge.perform();

      //
      ctx = service.loadNavigation(SiteKey.portal(name));
      node = service.loadNode(Node.MODEL, ctx, Scope.ALL, null).getNode();
      switch (importMode)
      {
         case MERGE:
         {
            assertEquals(2, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals("a", a.getState().getLabel());
            assertEquals(2, a.getNodeCount());
            b = a.getChild("b");
            assertNotNull(b);
            assertEquals("b", b.getState().getLabel());
            assertEquals(0, b.getNodeCount());
            Node c = node.getChild("c");
            assertNotNull(c);
            assertEquals("c", c.getState().getLabel());
            assertEquals(0, c.getNodeCount());
            Node d = a.getChild("d");
            assertNotNull(d);
            assertEquals("d", d.getName());
            assertEquals(0, d.getNodeCount());
            break;
         }
         case CONSERVE:
         {
            assertEquals(1, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals(1, a.getNodeCount());
            assertNotNull(b);
            assertEquals("b", b.getState().getLabel());
            assertEquals(0, b.getNodeCount());
            break;
         }
         case REIMPORT:
         {
            assertEquals(2, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals("a", a.getState().getLabel());
            assertEquals(1, a.getNodeCount());
            Node c = node.getChild("c");
            assertNotNull(c);
            assertEquals("c", c.getState().getLabel());
            assertEquals(0, c.getNodeCount());
            Node d = a.getChild("d");
            assertNotNull(d);
            assertEquals("d", d.getName());
            assertEquals(0, d.getNodeCount());
            break;
         }
      }
   }

   public void testMergeOrder()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_order");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_order")));

      //
      PageNavigation src = new PageNavigation("portal", "merge_order").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      NavigationImporter merge = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_order"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(3, node.getNodeCount());
      assertEquals("a", node.get(0).getName());
      assertEquals("b", node.get(1).getName());
      assertEquals("c", node.get(2).getName());

      //
      src.getFragment().getNodes().add(0, node("d").build());
      merge = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      merge.perform();

      //
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(4, node.getNodeCount());
      assertEquals("d", node.get(0).getName());
      assertEquals("a", node.get(1).getName());
      assertEquals("b", node.get(2).getName());
      assertEquals("c", node.get(3).getName());

      //
      src.getFragment().getNodes().add(node("e").build());
      merge = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      merge.perform();

      //
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(5, node.getNodeCount());
      assertEquals("d", node.get(0).getName());
      assertEquals("a", node.get(1).getName());
      assertEquals("b", node.get(2).getName());
      assertEquals("c", node.get(3).getName());
      assertEquals("e", node.get(4).getName());
   }

   public void testExtendedLabel()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "importer_extended_label");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("importer_extended_label")));

      //
      PageNavigation src = new PageNavigation("portal", "importer_extended_label").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      NavigationFragment fragment = src.getFragment();
      fragment.getNode("a").setLabels(new I18NString(new LocalizedString("a_en", Locale.ENGLISH), new LocalizedString("a_fr", Locale.FRENCH)));
      fragment.getNode("b").setLabels(new I18NString(new LocalizedString("b_en"), new LocalizedString("b_fr", Locale.FRENCH)));
      fragment.getNode("c").setLabels(new I18NString(new LocalizedString("c_en")));
      src.setOwnerId("importer_extended_label");
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.REIMPORT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("importer_extended_label"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();

      // The fully explicit case
      NodeContext<?> a = (NodeContext<?>)node.getNode("a");
      Map<Locale, Described.State> aDesc = descriptionService.getDescriptions(a.getId());
      assertNotNull(aDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), aDesc.keySet());
      assertEquals(new Described.State("a_en", null), aDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("a_fr", null), aDesc.get(Locale.FRENCH));
      assertNull(a.getState().getLabel());

      // No explicit language means to use the portal locale
      NodeContext<?> b = (NodeContext<?>)node.getNode("b");
      Map<Locale, Described.State> bDesc = descriptionService.getDescriptions(b.getId());
      assertNotNull(bDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), bDesc.keySet());
      assertEquals(new Described.State("b_en", null), bDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("b_fr", null), bDesc.get(Locale.FRENCH));
      assertNull(b.getState().getLabel());

      // The simple use case : one single label without the xml:lang attribute
      NodeContext<?> c = (NodeContext<?>)node.getNode("c");
      Map<Locale, Described.State> cDesc = descriptionService.getDescriptions(c.getId());
      assertNull(cDesc);
      assertEquals("c_en", c.getState().getLabel());
   }

   public void testFullNavigation()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "importer_full_navigation");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("importer_full_navigation")));

      //
      PageNavigation src = new PageNavigation("portal", "importer_full_navigation").addFragment(fragment().add(node("a")).build());
      src.addFragment(fragment().add(node("b")).build());
      src.addFragment(fragment("a").add(node("c")).build());

      //
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.REIMPORT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("importer_full_navigation"));
      NodeContext<NodeContext<?>> root = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null);
      assertEquals(2, root.getNodeSize());
//      Iterator<NodeContext<?>> i = root.iterator();
//      NodeContext<?> a = i.next();
//      assertEquals("a", a.getName());
//      assertEquals(1, a.getNodeSize());
//      NodeContext<?> b = i.next();
//      assertEquals("b", b.getName());
//      assertEquals(0, b.getNodeSize());
   }
}

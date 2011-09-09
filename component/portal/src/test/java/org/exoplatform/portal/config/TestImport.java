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

package org.exoplatform.portal.config;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.mop.api.workspace.Workspace;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestImport extends AbstractGateInTest
{
   private Set<String> clearProperties = new HashSet<String>();
   
   public void testMixin() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "conserve");
      setSystemProperty("import.portal.1", "navigation1");

      //
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);

      //
      RequestLifeCycle.begin(container);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      RequestLifeCycle.end();
      bootstrap.dispose();
   }

   public void testDefaultMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      setSystemProperty("import.portal.0", "navigation2");
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "merge");
      setSystemProperty("import.portal.1", "navigation1");

      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      NavigationService service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      NavigationContext nav = service.loadNavigation(SiteKey.portal("classic"));
      NodeContext<Node> root = service.loadNode(Node.MODEL, nav, Scope.ALL, null);
      Collection<Node> c = root.getNodes();
      assertEquals(3, c.size());
      assertNotNull(root.get("foo"));
      assertNotNull(root.get("daa"));
      assertNotNull(root.get("bar"));
      RequestLifeCycle.end();
      bootstrap.dispose();
   }

   public void testNoMixin() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "merge");
      setSystemProperty("import.portal.1", "site1");

      //
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      DataStorage service = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      RequestLifeCycle.begin(container);
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      long when1 = workspace.adapt(Imported.class).getCreationDate().getTime();
      PortalConfig portal = service.getPortalConfig("classic");
      Container layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      Application<Portlet> layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site1/layout", service.getId(layoutPortlet.getState()));
      Page page1 = service.getPage("portal::classic::page1");
      assertEquals(1, page1.getChildren().size());
      Application<Portlet> page1Portlet = (Application<Portlet>)page1.getChildren().get(0);
      assertEquals("site1/page1", service.getId(page1Portlet.getState()));
      workspace.removeAdapter(Imported.class);
      mgr.getSession().save();
      RequestLifeCycle.end();
      bootstrap.dispose();

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "conserve");
      setSystemProperty("import.portal.1", "site2");

      //
      bootstrap.boot();
      container = bootstrap.getContainer();
      service = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      RequestLifeCycle.begin(container);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      long when2 = workspace.adapt(Imported.class).getCreationDate().getTime();
      assertTrue(when2 > when1);
      portal = service.getPortalConfig("classic");
      layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site1/layout", service.getId(layoutPortlet.getState()));
      page1 = service.getPage("portal::classic::page1");
      assertEquals(1, page1.getChildren().size());
      page1Portlet = (Application<Portlet>)page1.getChildren().get(0);
      assertEquals("site1/page1", service.getId(page1Portlet.getState()));
      Page page2 = service.getPage("portal::classic::page2");
      assertNull(page2);
      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   public void testPageImporterInConserveMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      setSystemProperty("import.portal.0", "site1");

      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "conserve");
      setSystemProperty("import.portal.1", "site2");
      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      RequestLifeCycle.begin(container);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      Page home = dataStorage.getPage("portal::classic::page1");
      assertNotNull(home);
      assertEquals("site 1", home.getTitle());
      
      Page sitemap = dataStorage.getPage("portal::classic::page 2");
      assertNull(sitemap);

      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   public void testPageImporterInInsertMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      setSystemProperty("import.portal.0", "site1");
      
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "insert");
      setSystemProperty("import.portal.1", "site2");
      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      RequestLifeCycle.begin(container);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      Page home = dataStorage.getPage("portal::classic::page1");
      assertNotNull(home);
      assertEquals("site 1", home.getTitle());
      
      Page sitemap = dataStorage.getPage("portal::classic::page2");
      assertNotNull(sitemap);
      assertEquals("site 2", sitemap.getTitle());

      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   public void testPageImporterInMergeMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      setSystemProperty("import.portal.0", "site1");
      
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "merge");
      setSystemProperty("import.portal.1", "site2");
      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      RequestLifeCycle.begin(container);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      Page home = dataStorage.getPage("portal::classic::page1");
      assertNotNull(home);
      assertEquals("site 2", home.getTitle());
      
      Page sitemap = dataStorage.getPage("portal::classic::page2");
      assertNotNull(sitemap);
      assertEquals("site 2", sitemap.getTitle());

      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   public void testPageImporterInOverwriteMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      setSystemProperty("import.portal.0", "site1");

      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", "overwrite");
      setSystemProperty("import.portal.1", "site2");
      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      RequestLifeCycle.begin(container);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      Page home = dataStorage.getPage("portal::classic::page1");
      assertNotNull(home);
      assertEquals("site 2", home.getTitle());
      
      Page sitemap = dataStorage.getPage("portal::classic::page2");
      assertNotNull(sitemap);
      assertEquals("site 2", sitemap.getTitle());

      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   protected void setSystemProperty(String key, String value)
   {
      clearProperties.add(key);
      System.setProperty(key, value);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      for (String key : clearProperties)
      {
         System.clearProperty(key);
      }
      clearProperties.clear();
   }
}

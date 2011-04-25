/**
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.portal.i18n;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.i18n.I18NFramework;
import org.exoplatform.portal.mop.i18n.I18Nized;
import org.exoplatform.portal.mop.i18n.Injector;
import org.exoplatform.portal.mop.i18n.Language;

import org.chromattic.api.ChromatticSession;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 21, 2011
 */

@ConfiguredBy(
{@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-i18nframework-configuration.xml")})
public class TestI18NFramework extends AbstractKernelTest
{
   private ChromatticLifeCycle lifeCycle;

   private ChromatticManager chromatticManager;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
      lifeCycle = chromatticManager.getLifeCycle("i18n");
      lifeCycle.openContext();
   }

   public void testI18N()
   {
      String homepage_en = "Homepage";
      String homepage_vi = "Trangchu";
      String description_en = "This is the homepage";
      ChromatticSession session = null;
      session = lifeCycle.getChromattic().openSession();
      session.addEventListener(new Injector(session));
      NavigationNode node = session.insert(NavigationNode.class, "node1");

      Described described = session.getEmbedded(node, Described.class);
      if (described == null)
      {
         described = session.create(Described.class);
      }

      A a = session.getEmbedded(node, A.class);
      if (a == null)
      {
         a = session.create(A.class);
      }

      session.setEmbedded(node, Described.class, described);
      session.setEmbedded(node, A.class, a);

      I18NFramework framework = new I18NFramework(session);
      I18Nized i18n = framework.createI18nMixin(node);
      Described describe_en = i18n.putMixin(Described.class, "en");
      describe_en.setName(homepage_en);

      Described describe_vi = i18n.putMixin(Described.class, "vi");
      describe_vi.setName(homepage_vi);

      Language language = session.findByPath(Language.class, "node1/gtn:languages/en");
      assertNotNull(language);
      Described describe_en_new = session.getEmbedded(language, Described.class);
      assertEquals(describe_en_new.getName(), homepage_en);

      language = session.findByPath(Language.class, "node1/gtn:languages/vi");
      assertNotNull(language);
      Described describe_vi_new = session.getEmbedded(language, Described.class);
      assertEquals(describe_vi_new.getName(), homepage_vi);

      A a_en = i18n.putMixin(A.class, "en");
      a_en.setDescription(description_en);

      a_en = i18n.putMixin(A.class, "en");
      assertEquals(description_en, a_en.getDescription());
      session.save();
      session.close();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      lifeCycle.closeContext(false);
   }
}

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
package org.exoplatform.portal.resource;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import org.exoplatform.services.resources.Orientation;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/29/11
 */
@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/skin-service-configuration.xml"), 
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/resource-compressor-service-configuration.xml")})
public class TestSkinService extends AbstractKernelTest
{
   private PortalContainer portalContainer;

   private SkinService skinService;

   private ServletContext mockServletContext;

   private volatile boolean initSkinService = true;

   private InMemoryCSSResolver cssResolver;

   @Override
   protected void setUp() throws Exception
   {
      if (initSkinService)
      {
         initSkinService = false;

         portalContainer = getContainer();
         skinService = (SkinService)portalContainer.getComponentInstanceOfType(SkinService.class);
         mockServletContext = new MockServletContext("mockwebapp", portalContainer.getPortalClassLoader());
         skinService.registerContext(mockServletContext);
         cssResolver = new InMemoryCSSResolver();
         skinService.addResourceResolver(cssResolver);
         // No cache, no css compressor
         PropertyManager.setProperty(PropertyManager.DEVELOPING, "true");

         processSkinConfiguration("/gatein-resources.xml");
      }
   }

   private void processSkinConfiguration(String configResource) throws Exception
   {
      URL url = mockServletContext.getResource(configResource);
      SkinConfigParser.processConfigResource(DocumentSource.create(url), skinService, mockServletContext);
   }

   public void testInitService()
   {
      assertNotNull(portalContainer);
      assertEquals("portal", portalContainer.getName());
      assertNotNull(skinService);
   }

   public void testInitSkin()
   {
      assertNotNull(skinService.getAvailableSkinNames());
      assertEquals(1, skinService.getAvailableSkinNames().size());
      assertTrue(skinService.getAvailableSkinNames().contains("TestSkin"));
   }

   public void testDeployedSkinModules()
   {
      Collection<SkinConfig> portalSkinConfigs = skinService.getPortalSkins("TestSkin");
      assertNotNull(portalSkinConfigs);

      SkinConfig corePortalSkin = null;
      SkinConfig customModulePortalSkin = null;
      for (SkinConfig config : portalSkinConfigs)
      {
         if ("CoreSkin".equals(config.getModule()))
         {
            corePortalSkin = config;
         }
         else if ("CustomModule".equals(config.getModule()))
         {
            customModulePortalSkin = config;
         }
      }
      assertNotNull(corePortalSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/Stylesheet.css", corePortalSkin.getCSSPath());

      assertNotNull(customModulePortalSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/customModule/Stylesheet.css",
         customModulePortalSkin.getCSSPath());

      SkinConfig firstPortletSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      assertNotNull(firstPortletSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/portlet/FirstPortlet/Stylesheet.css",
         firstPortletSkin.getCSSPath());
   }

   public void testDeployedThemes()
   {
      Map<String, Set<String>> themeStyles = skinService.getPortletThemes();
      Set<String> themes = themeStyles.get("Simple");
      assertNotNull(themes);
      assertTrue(themes.contains("SimpleBlue"));
      assertTrue(themes.contains("SimpleViolet"));

      assertNotNull(themeStyles.get("VistaStyle"));
   }

   public void testCompositeSkin()
   {
      SkinConfig fSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      SkinConfig sSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
      assertNotNull(fSkin);
      assertNotNull(sSkin);            
      
      Skin merged = skinService.merge(Arrays.asList(fSkin, sSkin));
      SkinURL url = merged.createURL();
      
      url.setOrientation(Orientation.LT);      
      assertEquals("@import url(/mockwebapp/skin/portlet/FirstPortlet/Stylesheet-lt.css);\n" +
      		"@import url(/mockwebapp/skin/portlet/SecondPortlet/Stylesheet-lt.css);\n", skinService.getCSS(url.toString()));
      
      url.setOrientation(Orientation.RT);
      assertEquals("@import url(/mockwebapp/skin/portlet/FirstPortlet/Stylesheet-rt.css);\n" +
         "@import url(/mockwebapp/skin/portlet/SecondPortlet/Stylesheet-rt.css);\n", skinService.getCSS(url.toString()));            
   }
   
   public void testRenderer() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ResourceRenderer renderer = new MockResourceRenderer(out);

      assertEquals(0, out.size());
      skinService.renderCSS(renderer, "/mockwebapp/skin/Stylesheet-lt.css");
      assertTrue(out.size() > 0);
      out.reset();

      assertEquals(0, out.size());
      skinService.renderCSS(renderer, "/mockwebapp/skin/Stylesheet-rt.css");
      assertTrue(out.size() > 0);
   }

   public void testOrientation()
   {
      // caching is turn off so we can reuse this path
      String path = "/path/to/css"; 

      cssResolver.addFile("aaa;/*orientation=lt*/bbb;/*orientation=rt*/", path + ".css");
      assertEquals("aaa;\n", skinService.getCSS(path + "-lt.css"));
      assertEquals("bbb;/*orientation=rt*/\n", skinService.getCSS(path + "-rt.css"));

      cssResolver.addFile(" aaa; /* orientation=lt */ bbb; /* orientation=rt */ ", path + ".css");
      assertEquals(" aaa; \n", skinService.getCSS(path + "-lt.css"));
      assertEquals(" bbb; /* orientation=rt */ \n", skinService.getCSS(path + "-rt.css"));

      cssResolver.addFile("{aaa;bbb;/*orientation=lt*/ccc;ddd;/*orientation=rt*/}", path + ".css");
      assertEquals("{aaa;bbb;/*orientation=lt*/ccc;}\n", skinService.getCSS(path + "-lt.css"));
      assertEquals("{aaa;ccc;ddd;/*orientation=rt*/}\n", skinService.getCSS(path + "-rt.css"));

      cssResolver.addFile("{aaa;/*orientation=lt*/bbb;}{ccc;/*orientation=rt*/ddd;}", path + ".css");
      assertEquals("{aaa;/*orientation=lt*/bbb;}{ddd;}\n", skinService.getCSS(path + "-lt.css"));
      assertEquals("{bbb;}{ccc;/*orientation=rt*/ddd;}\n", skinService.getCSS(path + "-rt.css"));
   }

   public void testBackgroundURL()
   {
      String path = "/portal/eXoResources/skin/StyleSheet";

      cssResolver.addFile("background:url(images/foo.gif);", path + ".css");
      assertEquals("background:url(/portal/eXoResources/skin/images/foo.gif);\n", skinService.getCSS(path + "-lt.css"));
      assertEquals("background:url(/portal/eXoResources/skin/images/foo.gif);\n", skinService.getCSS(path + "-rt.css"));

      cssResolver.addFile("background:url('/images/foo.gif');", path + ".css");
      assertEquals("background:url('/images/foo.gif');\n", skinService.getCSS(path + "-lt.css"));
      
      cssResolver.addFile("aaa; background: #fff url('images/foo.gif') no-repeat center -614px; ccc;", path + ".css");
      assertEquals("aaa; background: #fff url('/portal/eXoResources/skin/images/foo.gif') no-repeat center -614px; ccc;\n",
         skinService.getCSS(path + "-lt.css"));           
   }

   public void testImport()
   {
      String parent = "/path/to/parent";
      cssResolver.addFile("@import url(Portlet/Stylesheet.css);", parent + ".css");
      assertEquals("@import url(/path/to/Portlet/Stylesheet-lt.css);\n", skinService.getCSS(parent + "-lt.css"));
      
      cssResolver.addFile("@import url('/Portlet/Stylesheet.css');", parent + ".css");
      assertEquals("@import url('/Portlet/Stylesheet-lt.css');\n", skinService.getCSS(parent + "-lt.css"));
      
      cssResolver.addFile("@import url(Portlet/Stylesheet.css); aaa;", parent + ".css");
      assertEquals("@import url(/path/to/Portlet/Stylesheet-rt.css); aaa;\n", skinService.getCSS(parent + "-rt.css"));
    
      //parent file import child css file
      cssResolver.addFile("@import url(childCSS/child.css);  background:url(images/foo.gif);", parent + ".css");      
      String child = "/path/to/childCSS/child.css";
      cssResolver.addFile("background:url(bar.gif);", child);
      
      /*
       * Now test merge and process recursively (run in non-dev mode)
       * We have folder /path/to/parent.css
       *                                        /images/foo.gif
       *                                        /childCSS/child.css
       *                                                        /bar.gif
       */
      PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
      assertEquals("background:url(/path/to/childCSS/bar.gif);background:url(/path/to/images/foo.gif);", 
         skinService.getCSS(parent + "-lt.css"));
   }
   
   public void testComment()
   {
      String path = "path/to/file";
      cssResolver.addFile("/**#@%$!a'*/ background:url(bar.gif); /**#@%$!a'*/",  path + ".css");
      assertEquals("/**#@%$!a'*/ background:url(path/to/bar.gif); /**#@%$!a'*/\n", skinService.getCSS(path + "-lt.css"));      
      
      PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
      assertEquals("background:url(path/to/bar.gif);", skinService.getCSS(path + "-lt.css"));
   }
   
   public void testCache()
   {
      String path = "path/to/cachedFile";
      cssResolver.addFile("foo", path + ".css");
      
      assertTrue(skinService.getCSS(path + "-lt.css").length() > 0);
      //No cache
      assertEquals(Long.MAX_VALUE, skinService.getLastModified(path + "-lt.css"));
      
      //Turn on caching
      PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");

      //Add to cache
      assertTrue(skinService.getCSS(path + "-lt.css").length() > 0);
      //Get css in cache
      assertTrue(skinService.getLastModified(path + "-lt.css") < System.currentTimeMillis());
   }

   @Override
   protected void tearDown() throws Exception
   {
   }

   private static class InMemoryCSSResolver implements ResourceResolver
   {
      private Map<String, String> cssFiles = new HashMap<String, String>();

      public void addFile(String content, String path)
      {
         cssFiles.put(path, content);
      }

      @Override
      public Resource resolve(String path) throws NullPointerException
      {
         final String css = cssFiles.get(path);
         if (css != null)
         {
            return new Resource(path)
            {
               @Override
               public Reader read()
               {
                  return new StringReader(css);
               }
            };
         }
         return null;
      }
   }
}

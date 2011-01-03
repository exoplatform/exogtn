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

package org.exoplatform.services.resources.test;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.resources.AbstractResourceBundleTest;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/*
 * Thu, May 15, 2004 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestResourceBundleService.java 5799 2006-05-28 17:55:42Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestResourceBundleService extends AbstractResourceBundleTest
{

   final static private String PROPERTIES = "language=en\nproperty=property";

   final static private String PROPERTIES_FR = "language=fr";

   final static private String PROPERTIES_FR_UPDATE = "language=fr\nproperty=fr-property";

   private static String databaseRes = "exo.locale";

   private static String fileRes = "locale.test.resources.test";

   private static String[] mergeRes = {fileRes, databaseRes};

   private ResourceBundleService service_;

   private LocaleConfigService lservice_;

   public TestResourceBundleService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      service_ = (ResourceBundleService)manager.getComponentInstanceOfType(ResourceBundleService.class);
      lservice_ = (LocaleConfigService)manager.getComponentInstanceOfType(LocaleConfigService.class);
   }

   public void testResourceBundleServiceUpdate() throws Exception
   {
      //-------getResourceBundle have loaded from property file to database--------
      ResourceBundle res = service_.getResourceBundle(fileRes, Locale.ENGLISH);

      //    //------------create ressource bundle in database------------------
      createResourceBundle(databaseRes, PROPERTIES, Locale.ENGLISH);
      createResourceBundle(databaseRes, PROPERTIES_FR, Locale.FRANCE);

      res = service_.getResourceBundle(databaseRes, Locale.ENGLISH);
      assertTrue("Expect to find the ResourceBundle", res != null);

      res = service_.getResourceBundle(databaseRes, Locale.FRANCE);
      assertTrue("Expect to find the ResourceBundle", res != null);
      assertEquals("Expect French locale bundle", "fr", res.getString("language"));
      assertEquals("Expect French locale bundle", "property", res.getString("property"));
      //--------- Update a databseRes resource bundle in database ----------------
      createResourceBundle(databaseRes, PROPERTIES_FR_UPDATE, Locale.FRANCE);
      res = service_.getResourceBundle(databaseRes, Locale.FRANCE);
      assertEquals("Expect French locale bundle", "fr-property", res.getString("property"));

      //--------Update fileRes resource bundle in databse--------------
      String datas = "key1=fileSystem\nlanguage=french";
      createResourceBundle(fileRes, datas, Locale.FRANCE);
      res = service_.getResourceBundle(fileRes, Locale.FRANCE);
      assertTrue("Expect to find the ResourceBundle", res != null);
      assertTrue("Expect 'fileRes' is updated", res.getString("key1").equals("fileSystem"));
      assertEquals("Expect languge property is:", "french", res.getString("language"));

      //--------Update fileRes resource bundle in databse--------------
      datas = "key1=fileSystemUpdate\nlanguage=french";
      createResourceBundle(fileRes, datas, Locale.FRANCE);
      res = service_.getResourceBundle(fileRes, Locale.FRANCE);
      assertTrue("Expect to find the ResourceBundle", res != null);
      assertTrue("Expect 'fileRes' is updated", res.getString("key1").equals("fileSystemUpdate"));
      assertEquals("Expect languge property is:", "french", res.getString("language"));
   }

   public void testResourceBundleServiceRemove() throws Exception
   {
      //-------getResourceBundle have loaded from property file to database--------
      ResourceBundle res = service_.getResourceBundle(fileRes, Locale.ENGLISH);

      //------------create ressource bundle in database------------------
      createResourceBundle(databaseRes, PROPERTIES, Locale.ENGLISH);
      createResourceBundle(databaseRes, PROPERTIES_FR, Locale.FRANCE);

      res = service_.getResourceBundle(databaseRes, Locale.ENGLISH);
      assertTrue("Expect to find the ResourceBundle", res != null);

      res = service_.getResourceBundle(databaseRes, Locale.FRANCE);
      assertTrue("Expect to find the ResourceBundle", res != null);
      assertEquals("Expect French locale bundle", "fr", res.getString("language"));
      assertEquals("Expect French locale bundle", "property", res.getString("property"));

      //    //-----------get all resource bundle-----------    
      Query q = new Query(null, null);
      List l = service_.findResourceDescriptions(q).getAll();

      //----------remove a resource bundle data with Id: databaseRes_en------
      int sizeBeforeRemove = l.size();
      ResourceBundleData data = service_.getResourceBundleData(databaseRes + "_en");
      service_.removeResourceBundleData(data.getId());
      l = service_.findResourceDescriptions(q).getAll();

      assertEquals("Expect resources bundle in in database decrease", sizeBeforeRemove - 1, l.size());
      assertTrue("expect resource bundle is removed", service_.getResourceBundleData(databaseRes + "_en") == null);
   }

   public void testResourceBundleServiceList() throws Exception
   {

      Query q = new Query(null, null);
      List l = service_.findResourceDescriptions(q).getAll();

      //-------getResourceBundle have loaded from property file to database--------
      ResourceBundle res = service_.getResourceBundle(fileRes, Locale.ENGLISH);

      //------------create ressource bundle in database------------------
      createResourceBundle(databaseRes, PROPERTIES, Locale.ENGLISH);
      createResourceBundle(databaseRes, PROPERTIES_FR, Locale.FRANCE);

      res = service_.getResourceBundle(databaseRes, Locale.ENGLISH);
      assertTrue("Expect to find the ResourceBundle", res != null);

      res = service_.getResourceBundle(databaseRes, Locale.FRANCE);
      assertTrue("Expect to find the ResourceBundle", res != null);
      assertEquals("Expect French locale bundle", "fr", res.getString("language"));
      assertEquals("Expect French locale bundle", "property", res.getString("property"));

      //    //-----------get all resource bundle-----------    
      q = new Query(null, null);
      l = service_.findResourceDescriptions(q).getAll();

      assertTrue("Expect at least 2 locale properties resources", l.size() >= 2);
   }

   private void createResourceBundle(String name, String datas, Locale locale) throws Exception
   {
      ResourceBundleData data = service_.createResourceBundleDataInstance();
      data.setName(name);
      data.setData(datas);
      data.setLanguage(locale.getLanguage());
      if (locale.getCountry().trim().length() != 0)
      {
    	  data.setCountry(locale.getCountry());
      }
      service_.saveResourceBundle(data);
   }

   protected String getDescription()
   {
      return "Test Resource Bundle Service";
   }
   
   public void testClasspathResourceCache()
   {
      String oldValue = PropertyManager.getProperty(PropertyManager.DEVELOPING);
      try
      {
         PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
         assertFalse(PropertyManager.isDevelopping());
         MyClassLoader cl1 = new MyClassLoader();
         ResourceBundle res = service_.getResourceBundle("locale.portlet", Locale.ENGLISH, cl1);
         assertNotNull(res);
         assertTrue(res == service_.getResourceBundle("locale.portlet", Locale.ENGLISH, cl1));
         assertFalse(res == service_.getResourceBundle("locale.portlet", Locale.ENGLISH, new MyClassLoader()));
      }
      finally
      {
         PropertyManager.setProperty(PropertyManager.DEVELOPING, oldValue);
      }
   }
   
   private static class MyClassLoader extends ClassLoader
   {

      @Override
      public String toString()
      {
         return "MyClassLoader";
      }
   }
}

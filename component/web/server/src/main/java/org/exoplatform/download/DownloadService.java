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

package org.exoplatform.download;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 26, 2005
 */
public class DownloadService
{

   private final ExoCache<String, DownloadResource> downloadResources_;

   private Map<String, DownloadResource> defaultResources_;

   private PortalContainerInfo pinfo_;

   public static final String DOWNLOAD_HANDLER_PATH = "download";

   public DownloadService(PortalContainerInfo pinfo, InitParams params) throws Exception
   {
      int maxSize = Integer.parseInt(params.getValueParam("download.resource.cache.size").getValue());
      ConcurrentFIFOExoCache<String, DownloadResource> tmp = new ConcurrentFIFOExoCache<String, DownloadResource>(maxSize);
      tmp.addCacheListener(new CacheListener<String, DownloadResource>()
      {
         @Override
         public void onExpire(CacheListenerContext context, String key, DownloadResource obj) throws Exception
         {
            if (obj instanceof InputStreamDownloadResource)
            {
               try
               {
                  obj.getInputStream().close();
               }
               catch (IOException ioEx)
               {
               }
            }
         }

         @Override
         public void onRemove(CacheListenerContext context, String key, DownloadResource obj) throws Exception {}

         @Override
         public void onPut(CacheListenerContext context, String key, DownloadResource obj) throws Exception {}

         @Override
         public void onGet(CacheListenerContext context, String key, DownloadResource obj) throws Exception {}

         @Override
         public void onClearCache(CacheListenerContext context) throws Exception {}
      });
      downloadResources_ = tmp;

      defaultResources_ = new HashMap<String, DownloadResource>();
      pinfo_ = pinfo;
   }

   public void addDefaultDownloadResource(DownloadResource resource)
   {
      defaultResources_.put(resource.getDownloadType(), resource);
   }

   public String addDownloadResource(DownloadResource resource)
   {
      String id = Integer.toString(resource.hashCode());
      if (resource.getDownloadType() != null)
      { id = resource.getDownloadType() + ":/" + id; }
      downloadResources_.put(id, resource);
      return id;
   }

   public DownloadResource getDownloadResource(String id)
   {
      DownloadResource resource = downloadResources_.get(id);
      if (resource != null)
      {
         downloadResources_.remove(id);
         return resource;
      }
      String[] temp = id.split(":");
      if (temp.length > 1)
      {
         String downloadType = temp[0];
         resource = defaultResources_.get(downloadType);
      }
      return resource;
   }

   public String getDownloadLink(String id)
   {
      return "/" + pinfo_.getContainerName() + "/" + DOWNLOAD_HANDLER_PATH + "?"
         + "resourceId=" + id;
   }

   public ExoCache<String, DownloadResource> getCache()
   {
      return downloadResources_;
   }
}
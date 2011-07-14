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

package org.exoplatform.portal.config.model;

import org.exoplatform.portal.mop.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PageNode extends PageNodeContainer
{

   /** . */
   private ArrayList<LocalizedString> labels;

   /** . */
   private String icon;

   /** . */
   private String name;

   /** . */
   private Date startPublicationDate;

   /** . */
   private Date endPublicationDate;

   /** . */
   private Visibility visibility = Visibility.DISPLAYED;

   /** . */
   private String pageReference;

   public PageNode()
   {
   }

   public String getUri()
   {
      return null;
   }

   public void setUri(String s)
   {
      // No op for back war compatibility during unmarshalling
   }

   public ArrayList<LocalizedString> getLabels()
   {
      return labels;
   }

   public Map<Locale, String> getLocalizedLabel(Locale defaultLocale)
   {
      Map<Locale, String> map = Collections.emptyMap();
      LocalizedString portalLocaleLabel = null;
      for (LocalizedString label : labels)
      {
         if (label.getLang() != null)
         {
            if (map.isEmpty())
            {
               map = new HashMap<Locale, String>();
            }
            map.put(label.getLang(), label.getValue());
         }
         else
         {
            portalLocaleLabel = label;
         }
      }
      if (map.isEmpty())
      {
         return null;
      }
      else
      {
         if (portalLocaleLabel != null && !map.containsKey(defaultLocale))
         {
            map.put(defaultLocale, portalLocaleLabel.getValue());
         }
         return map;
      }
   }

   public void setLabels(ArrayList<LocalizedString> labels)
   {
      this.labels = labels;
   }

   public String getLabel()
   {
      if (labels != null)
      {
         for (LocalizedString label : labels)
         {
            if (label.getLang() == null)
            {
               return label.getValue();
            }
         }
      }
      return null;
   }

   public void setLabel(String s)
   {
      if (labels == null)
      {
         labels = new ArrayList<LocalizedString>();
      }
      else
      {
         labels.clear();
      }
      labels.add(new LocalizedString(s));
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String s)
   {
      icon = s;
   }

   public String getPageReference()
   {
      return pageReference;
   }

   public void setPageReference(String s)
   {
      pageReference = s;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public List<PageNode> getChildren()
   {
      return getNodes();
   }

   public void setChildren(ArrayList<PageNode> children)
   {
      setNodes(children);
   }

   public Date getStartPublicationDate()
   {
      return startPublicationDate;
   }

   public void setStartPublicationDate(Date startDate)
   {
      startPublicationDate = startDate;
   }

   public Date getEndPublicationDate()
   {
      return endPublicationDate;
   }

   public void setEndPublicationDate(Date endDate)
   {
      endPublicationDate = endDate;
   }

   public void setVisibility(Visibility visibility)
   {
      this.visibility = visibility;
   }
   
   public Visibility getVisibility()
   {
      return this.visibility;
   }

   public PageNode getChild(String name)
   {
      return getNode(name);
   }

   @Override
   public String toString()
   {
      return "PageNode[" + name + "]";
   }
}
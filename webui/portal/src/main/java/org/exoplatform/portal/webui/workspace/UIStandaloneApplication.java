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

package org.exoplatform.portal.webui.workspace;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.StandaloneAppRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.resource.Skin;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.application.UIStandaloneAppContainer;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.login.InitiateLoginServlet;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ComponentConfig(lifecycle = UIStandaloneApplicationLifecycle.class, template = "system:/groovy/portal/webui/workspace/UIPortalApplication.gtmpl")
public class UIStandaloneApplication extends UIApplication
{
   public static final int NORMAL_MODE = 0;

   //modeState, locale, skin_, orientation will be used when we display portlet in standalone mode
   private int modeState = NORMAL_MODE;

   private Locale locale_ = Locale.ENGLISH;

   private String skin_ = "Default";

   private Orientation orientation_ = Orientation.LT;

   private boolean isSessionOpen = false;

   public UIStandaloneApplication() throws Exception
   {
      addChild(UIStandaloneAppContainer.class, null, null);
   }  

   //Temporary need this, don't want to render UIPopupMessage
   public void renderChildren() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      super.renderChildren(context);
   }

   @Override
   public void processDecode(WebuiRequestContext context) throws Exception
   {
      String storageId = ((StandaloneAppRequestContext)context).getStorageId();      
      
      UIStandaloneAppContainer staContaner = getChild(UIStandaloneAppContainer.class);
      if (!storageId.equals(staContaner.getCurrStorageId()) )
      {         
         staContaner.setCurrStorageId(storageId);
      }
      super.processDecode(context);
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      Writer w = context.getWriter();

      //
      if (!context.useAjax())
      {
         super.processRender(context);
      }
      else
      {
         PortalRequestContext pcontext = (PortalRequestContext)context;

//         if (getUIPopupMessages().hasMessage())
//         {
//            pcontext.addUIComponentToUpdateByAjax(getUIPopupMessages());
//         }         

         Set<UIComponent> list = context.getUIComponentToUpdateByAjax();
//         List<UIPortlet> uiPortlets = new ArrayList<UIPortlet>(3);
//         List<UIComponent> uiDataComponents = new ArrayList<UIComponent>(5);

//         if (list != null)
//         {
//            for (UIComponent uicomponent : list)
//            {
//               if (uicomponent instanceof UIPortlet)
//                  uiPortlets.add((UIPortlet)uicomponent);
//               else
//                  uiDataComponents.add(uicomponent);
//            }
//         }
         w.write("<div class=\"PortalResponse\">");
         w.write("<div class=\"PortalResponseData\">");
         if (list != null)
         {
            for (UIComponent uicomponent : list)
            {
               if (log.isDebugEnabled())
               {
                  log.debug("AJAX call: Need to refresh the UI component " + uicomponent.getName());
               }
               renderBlockToUpdate(uicomponent, context, w);
            }
         }
         w.write("</div>");

//         if (!context.getFullRender())
//         {
//            for (UIPortlet uiPortlet : uiPortlets)
//            {
//               if (log.isDebugEnabled())
//                  log.debug("AJAX call: Need to refresh the Portlet " + uiPortlet.getId());
//
//               w.write("<div class=\"PortletResponse\" style=\"display: none\">");
//               w.append("<div class=\"PortletResponsePortletId\">" + uiPortlet.getId() + "</div>");
//               w.append("<div class=\"PortletResponseData\">");
//
//               /*
//                * If the portlet is using our UI framework or supports it then
//                * it will return a set of block to updates. If there is not
//                * block to update the javascript client will see that as a full
//                * refresh of the content part
//                */
//               uiPortlet.processRender(context);
//
//               w.append("</div>");
//               w.append("<div class=\"PortletResponseScript\"></div>");
//               w.write("</div>");
//            }
//         }

         w.write("<div class=\"PortalResponseScript\">");
         pcontext.getJavascriptManager().writeJavascript(w);
         w.write("eXo.core.Browser.onLoad();\n");
         pcontext.getJavascriptManager().writeCustomizedOnLoadScript(w);
         w.write("</div>");
         w.write("</div>");
      }
   }

    public Collection<String> getJavascriptURLs()
   {
      JavascriptConfigService service = getApplicationComponent(JavascriptConfigService.class);
      return service.getAvailableScriptsPaths();
   }

   public Collection<Skin> getPortalSkins()
   {
      SkinService skinService = getApplicationComponent(SkinService.class);
      Collection<Skin> skins = new ArrayList<Skin>(skinService.getPortalSkins(skin_));
//      Collection<Skin> skins = new ArrayList<Skin>();
      return skins;
   }

   public Set<Skin> getPortletSkins()
   {
      Set<Skin> skins = new HashSet<Skin>();
      return skins;
   }
   
   public boolean isSessionOpen()
   {
      return isSessionOpen;
   }

   public void setSessionOpen(boolean isSessionOpen)
   {
      this.isSessionOpen = isSessionOpen;
   }

   public String getSkin()
   {
      return skin_;
   }

   public Orientation getOrientation()
   {
      return orientation_;
   }

   public void setOrientation(Orientation orientation)
   {
      this.orientation_ = orientation;
   }

   public Locale getLocale()
   {
      return locale_;
   }

   public void setLocale(Locale locale)
   {
      locale_ = locale;
   }

   public void setModeState(int mode)
   {
      this.modeState = mode;
   }

   public int getModeState()
   {
      return modeState;
   }
}

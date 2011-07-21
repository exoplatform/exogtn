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

package org.exoplatform.portal.application;

import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.portal.url.LocatorProviderService;
import org.exoplatform.portal.url.PortalURL;
import org.exoplatform.portal.url.navigation.NavigationLocator;
import org.exoplatform.portal.url.navigation.NavigationResource;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This handler resolves legacy request and redirect them to the new URL computed dynamically against the
 * routing table.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LegacyRequestHandler extends WebRequestHandler
{

   /** . */
   private final LocatorProviderService locatorFactory;

   /** . */
   private final UserPortalConfigService userPortalService;

   /** . */
   private final UserPortalContext userPortalContext = new UserPortalContext()
   {
      public ResourceBundle getBundle(UserNavigation navigation)
      {
         return null;
      }

      public Locale getUserLocale()
      {
         return Locale.ENGLISH;
      }
   };

   public LegacyRequestHandler(LocatorProviderService locatorFactory, UserPortalConfigService userPortalService)
   {
      this.locatorFactory = locatorFactory;
      this.userPortalService = userPortalService;
   }

   @Override
   public String getHandlerName()
   {
      return "legacy";
   }

   @Override
   public void execute(ControllerContext context) throws Exception
   {
      String requestSiteName = context.getParameter(PortalRequestHandler.REQUEST_SITE_NAME);
      String requestPath = context.getParameter(PortalRequestHandler.REQUEST_PATH);

      // Resolve the user node
      UserPortalConfig cfg = userPortalService.getUserPortalConfig(requestSiteName,  context.getRequest().getRemoteUser(), userPortalContext);
      UserPortal userPortal = cfg.getUserPortal();
      UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder().withAuthMode(UserNodeFilterConfig.AUTH_READ);
      UserNode userNode = userPortal.resolvePath(builder.build(), requestPath);

      //
      String uri;
      String siteName;
      String siteType;
      if (userNode != null)
      {
         SiteKey siteKey = userNode.getNavigation().getKey();
         uri = userNode.getURI();
         siteName = siteKey.getName();
         siteType = siteKey.getTypeName();
      }
      else
      {
         uri = "";
         siteName = "classic";
         siteType = "portal";
      }

      //
      NavigationLocator locator = locatorFactory.newLocator(NavigationLocator.TYPE);
      PortalURL<NavigationResource, NavigationLocator> url = new PortalURL<NavigationResource, NavigationLocator>(context, locator, false, null, "portal", "classic");

      // For now we redirect on the default classic site
      url.setResource(new NavigationResource(siteType, siteName, uri));
      String s = url.toString();
      HttpServletResponse resp = context.getResponse();
      resp.sendRedirect(resp.encodeRedirectURL(s));
   }
}

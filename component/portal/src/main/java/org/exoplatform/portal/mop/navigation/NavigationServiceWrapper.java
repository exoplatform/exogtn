/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceWrapper implements Startable, NavigationService
{

   /** . */
   private final NavigationServiceImpl service;

   public NavigationServiceWrapper(POMSessionManager manager, OrganizationService organization)
   {
      this.service = new NavigationServiceImpl(manager);
   }

   public void start()
   {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try
      {
         service.start();
      }
      catch (Exception e)
      {
         service.log.error("Could not start navigation service", e);
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   public void stop()
   {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try
      {
         service.stop();
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   public NavigationData getNavigation(SiteKey key)
   {
      return service.getNavigation(key);
   }

   public <N> N load(NodeModel<N> model, NavigationData navigation, Scope scope)
   {
      return service.load(model, navigation, scope);
   }

   public <N> N load(NodeModel<N> model, N node, Scope scope)
   {
      return service.load(model, node, scope);
   }
}

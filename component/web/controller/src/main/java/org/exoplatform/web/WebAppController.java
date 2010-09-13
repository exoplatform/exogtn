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

package org.exoplatform.web;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.metadata.RouterDescriptor;
import org.exoplatform.web.controller.router.Router;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Created by The eXo Platform SAS
 * Mar 21, 2007  
 * 
 * The WebAppController is the entry point of the eXo web framework
 * 
 * It also stores WebRequestHandlers, Attributes and deployed Applications
 * 
 */
public class WebAppController
{

   /** . */
   public static final QualifiedName HANDLER_PARAM = new QualifiedName("gtn", "handler");

   /** . */
   protected static Logger log = LoggerFactory.getLogger(WebAppController.class);

   /** . */
   private HashMap<String, Object> attributes_;

   private volatile HashMap<String, Application> applications_;

   /** . */
   private HashMap<String, WebRequestHandler> handlers;

   /** . */
   final Router router;

   /**
    * The WebAppControler along with the PortalRequestHandler defined in the init() method of the
    * PortalController servlet (controller.register(new PortalRequestHandler())) also add the
    * CommandHandler object that will listen for the incoming /command path in the URL
    * 
    * @throws Exception
    */
   public WebAppController(ConfigurationManager configurationManager) throws Exception
   {
      // Read configuration (a bit hardcoded for now)
      URL routerURL = configurationManager.getResource("war:/conf/router.xml");
      XMLStreamReader routerReader = XMLInputFactory.newInstance().createXMLStreamReader(routerURL.openStream());
      RouterDescriptor routerDesc = new DescriptorBuilder().build(routerReader);

      // Build router from configuration
      Router router = new Router(routerDesc);

      //
      this.applications_ = new HashMap<String, Application>();
      this.attributes_ = new HashMap<String, Object>();
      this.handlers = new HashMap<String, WebRequestHandler>();
      this.router = router;
   }

   public Object getAttribute(String name, Object value)
   {
      return attributes_.get(name);
   }

   @SuppressWarnings("unchecked")
   public <T extends Application> T getApplication(String appId)
   {
      return (T)applications_.get(appId);
   }

   public List<Application> getApplicationByType(String type)
   {
      List<Application> applications = new ArrayList<Application>();
      for (Application app : applications_.values())
      {
         if (app.getApplicationType().equals(type))
            applications.add(app);
      }
      return applications;
   }

   public synchronized void removeApplication(String appId)
   {
      applications_.remove(appId);
   }

   /**
    * This methods will add the new application if and only if it hasn't yet been registered
    * @param app the {@link Application} to add
    * @return the given application if no application with the same id has been added
    * otherwise the application already registered
    */
   @SuppressWarnings("unchecked")
   public <T extends Application> T addApplication(T app)
   {
      Application result = getApplication(app.getApplicationId());
      if (result == null)
      {
         synchronized (this)
         {
            result = getApplication(app.getApplicationId());
            if (result == null)
            {
               HashMap<String, Application> applications = new HashMap<String, Application>(applications_);
               applications.put(app.getApplicationId(), app);
               this.applications_ = applications;
               result = app;
            }
         }
      }
      return (T)result;
   }

   public void register(WebRequestHandler handler) throws Exception
   {
      handlers.put(handler.getHandlerName(), handler);
   }
   
   public void onHandlersInit(ServletConfig config) throws Exception
   {
      Collection<WebRequestHandler> hls = handlers.values();
      for (WebRequestHandler handler : hls)
      {
         handler.onInit(this, config);
      }
   }
   
   /**
    * This is the first method - in the eXo web framework - reached by incoming HTTP request, it acts like a
    * servlet service() method
    * 
    * According to the servlet path used the correct handler is selected and then executed.
    * 
    * The event "exo.application.portal.start-http-request" and "exo.application.portal.end-http-request" are also sent 
    * through the ListenerService and several listeners may listen to it.
    * 
    * Finally a WindowsInfosContainer object using a ThreadLocal (from the portlet-container product) is created 
    */
   public void service(HttpServletRequest req, HttpServletResponse res) throws Exception
   {

      String portalPath = req.getRequestURI().substring(req.getContextPath().length());
      log.info("Portal path: " + portalPath);

      //
      Map<QualifiedName, String> parameters = router.route(portalPath);
      log.info("Decoded parameters: " + parameters);

      //
      if (parameters != null)
      {
         String handlerKey = parameters.get(HANDLER_PARAM);
         if (handlerKey != null)
         {
            WebRequestHandler handler = handlers.get(handlerKey);
            log.info("Handler used for this path: " + handler);

            //
            ExoContainer portalContainer = ExoContainerContext.getCurrentContainer();
            RequestLifeCycle.begin(portalContainer);

            //
            ControllerContext context = new ControllerContext(this, req, res, parameters);

            //
            try
            {
               handler.execute(context);
            }
            finally
            {
               RequestLifeCycle.end();
            }
         }
      }
   }
}
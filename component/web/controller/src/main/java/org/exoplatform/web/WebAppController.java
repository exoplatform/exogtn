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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.metadata.RouterDescriptor;
import org.exoplatform.web.controller.router.Router;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The WebAppController is the entry point of the GateIn service.
 */
public class WebAppController
{

   /** . */
   public static final QualifiedName HANDLER_PARAM = QualifiedName.create("gtn", "handler");

   /** . */
   protected static Logger log = LoggerFactory.getLogger(WebAppController.class);

   /** . */
   private HashMap<String, Object> attributes_;

   /** . */
   private volatile HashMap<String, Application> applications_;

   /** . */
   private HashMap<String, WebRequestHandler> handlers;

   /** . */
   private final AtomicReference<Router> routerRef;

   /**
    * The WebAppControler along with the PortalRequestHandler defined in the init() method of the
    * PortalController servlet (controller.register(new PortalRequestHandler())) also add the
    * CommandHandler object that will listen for the incoming /command path in the URL.
    *
    * @param params the init params
    * @param configurationManager the configuration manager
    * @throws Exception any exception
    */
   public WebAppController(InitParams params, ConfigurationManager configurationManager) throws Exception
   {
      // Get router config
      ValueParam routerConfig = params.getValueParam("router.config");
      if (routerConfig == null)
      {
         throw new IllegalArgumentException("No router param defined");
      }
      String routerConfigPath = routerConfig.getValue();

      // Read configuration
      URL routerURL = configurationManager.getResource(routerConfigPath);
      RouterDescriptor routerDesc = new DescriptorBuilder().build(routerURL.openStream());

      // Build router from configuration
      Router router = new Router(routerDesc);

      //
      this.applications_ = new HashMap<String, Application>();
      this.attributes_ = new HashMap<String, Object>();
      this.handlers = new HashMap<String, WebRequestHandler>();
      this.routerRef = new AtomicReference<Router>(router);
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

   /**
    * Register an handler as a component plugin, this method is invoked by the kernel with reflection.
    *
    * @param handler the handler
    * @throws Exception any exception
    */
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
    * Reconfigure the controller.
    *
    * @param xml the router configuration
    */
   public void setConfiguration(String xml)
   {
      Reader r = new StringReader(xml);
      RouterDescriptor routerDesc = new DescriptorBuilder().build(r);
      Router router = new Router(routerDesc);
      routerRef.set(router);
   }

   Router getRouter()
   {
      return routerRef.get();
   }

   /**
    * <p>This is the first method - in the GateIn portal - reached by incoming HTTP request, it acts like a
    * servlet service() method. According to the servlet path used the correct handler is selected and then executed.</p>
    *
    * <p>During a request the request life cycle is demarcated by calls to {@link RequestLifeCycle#begin(ExoContainer);}
    * and {@link RequestLifeCycle#end()}.</p>
    *
    * @param req the http request
    * @param res the http response
    * @throws Exception any exception
    */
   public void service(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      boolean debug = log.isDebugEnabled();
      String portalPath = req.getRequestURI().substring(req.getContextPath().length());
      Map<QualifiedName, String> parameters = routerRef.get().route(portalPath, req.getParameterMap());

      //
      if (parameters != null)
      {
         String handlerKey = parameters.get(HANDLER_PARAM);
         if (handlerKey != null)
         {
            WebRequestHandler handler = handlers.get(handlerKey);
            if (debug)
            {
               log.debug("Serving request path=" + portalPath + ", parameters=" + parameters + " with handler " + handler);
            }

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
      else
      {
         // JULIEN : found something to do like a default handler
         if (debug)
         {
            log.debug("Could not associate the request path=" + portalPath + ", parameters=" + parameters + " with an handler");
         }
      }
   }
}
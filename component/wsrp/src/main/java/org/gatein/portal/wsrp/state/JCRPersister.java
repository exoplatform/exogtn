/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state;

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.chromattic.api.format.FormatterContext;
import org.chromattic.api.format.ObjectFormatter;
import org.chromattic.spi.jcr.SessionLifeCycle;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.gatein.common.util.ParameterValidation;
import org.gatein.portal.wsrp.state.mapping.BaseMapping;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRPersister
{
   private Chromattic chrome;
   public static final String WSRP_WORKSPACE_NAME = "wsrp-system";
   public static final String PORTLET_STATES_WORKSPACE_NAME = "pc-system";
   private static final String REPOSITORY_NAME = "repository";
   private String workspaceName;
   private Map<Class, Class<? extends BaseMapping>> modelToMapping;

   private ThreadLocal<ChromatticSession> sessionHolder = new ThreadLocal<ChromatticSession>();

   public JCRPersister(ExoContainer container, String workspaceName)
   {
      this.workspaceName = workspaceName;
   }

   public void initializeBuilderFor(List<Class> mappingClasses) throws Exception
   {
      ChromatticBuilder builder = ChromatticBuilder.create();
      builder.setOptionValue(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, "org.chromattic.apt.InstrumentorImpl");
      if (PORTLET_STATES_WORKSPACE_NAME.equals(workspaceName))
      {
         builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, PortletStatesSessionLifeCycle.class.getName());
      }
      else if (WSRP_WORKSPACE_NAME.equals(workspaceName))
      {
         builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, WSRPSessionLifeCycle.class.getName());
      }
      else
      {
         throw new IllegalArgumentException("Unknown workspace name: '" + workspaceName + "'");
      }

      modelToMapping = new HashMap<Class, Class<? extends BaseMapping>>(mappingClasses.size());
      for (Class mappingClass : mappingClasses)
      {
         if (BaseMapping.class.isAssignableFrom(mappingClass))
         {
            Type[] interfaces = mappingClass.getGenericInterfaces();
            if (ParameterValidation.existsAndIsNotEmpty(interfaces))
            {
               Class type = (Class)((ParameterizedType)interfaces[0]).getActualTypeArguments()[0];
               modelToMapping.put(type, mappingClass);
            }
         }
         builder.add(mappingClass);
      }

      chrome = builder.build();
   }

   public ChromatticSession getSession()
   {
      ChromatticSession chromatticSession = sessionHolder.get();
      if (chromatticSession == null)
      {
         ChromatticSession session = chrome.openSession();
         sessionHolder.set(session);
         return session;
      }
      else
      {
         return chromatticSession;
      }
   }

   public void closeSession(boolean save)
   {
      ChromatticSession session = getOpenedSessionOrFail();
      if (save)
      {
         synchronized (this)
         {
            session.save();
         }
      }
      session.close();
      sessionHolder.set(null);
   }

   private ChromatticSession getOpenedSessionOrFail()
   {
      ChromatticSession session = sessionHolder.get();
      if(session == null)
      {
         throw new IllegalStateException("Cannot close the session as it hasn't been opened first!");
      }
      return session;
   }

   public synchronized void save()
   {
      getOpenedSessionOrFail().save();
   }

   public <T> boolean delete(T toDelete, StoresByPathManager<T> manager)
   {
      Class<? extends Object> modelClass = toDelete.getClass();
      Class<? extends BaseMapping> baseMappingClass = modelToMapping.get(modelClass);
      if (baseMappingClass == null)
      {
         throw new IllegalArgumentException("Cannot find a mapping class for " + modelClass.getName());
      }

      ChromatticSession session = getSession();

      Object old = session.findByPath(baseMappingClass, manager.getChildPath(toDelete));

      if (old != null)
      {
         session.remove(old);
         closeSession(true);
         return true;
      }
      else
      {
         closeSession(false);
         return false;
      }

   }

   public static class WSRPSessionLifeCycle implements SessionLifeCycle
   {
      private ManageableRepository repository;
      private SessionProvider provider;

      public WSRPSessionLifeCycle()
      {
         try
         {
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            RepositoryService repoService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
            repository = repoService.getRepository(REPOSITORY_NAME);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         provider = SessionProvider.createSystemProvider();
      }

      public Session login() throws RepositoryException
      {
         return provider.getSession(WSRP_WORKSPACE_NAME, repository);
      }

      public Session login(String s) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public Session login(Credentials credentials, String s) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public Session login(Credentials credentials) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public void save(Session session) throws RepositoryException
      {
         session.save();
      }

      public void close(Session session)
      {
         session.logout();
      }
   }

   public static class PortletStatesSessionLifeCycle implements SessionLifeCycle
   {
      private ManageableRepository repository;
      private SessionProvider provider;

      public PortletStatesSessionLifeCycle()
      {
         try
         {
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            RepositoryService repoService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
            repository = repoService.getRepository(REPOSITORY_NAME);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         provider = SessionProvider.createSystemProvider();
      }

      public Session login() throws RepositoryException
      {
         return provider.getSession(PORTLET_STATES_WORKSPACE_NAME, repository);
      }

      public Session login(String s) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public Session login(Credentials credentials, String s) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public Session login(Credentials credentials) throws RepositoryException
      {
         throw new UnsupportedOperationException();
      }

      public void save(Session session) throws RepositoryException
      {
         session.save();
      }

      public void close(Session session)
      {
         session.logout();
      }
   }

   public static class QNameFormatter implements ObjectFormatter
   {
      private static final String OPEN_BRACE_REPLACEMENT = "-__";
      private static final String CLOSE_BRACE_REPLACEMENT = "__-";
      private static final String COLON_REPLACEMENT = "_-_";
      private static final String CLOSE_BRACE = "}";
      private static final String OPEN_BRACE = "{";
      private static final String COLON = ":";

      public String decodeNodeName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodeNodeName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      public String decodePropertyName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodePropertyName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      public static String decode(String s)
      {
         return s.replace(CLOSE_BRACE_REPLACEMENT, CLOSE_BRACE).replace(OPEN_BRACE_REPLACEMENT, OPEN_BRACE).replace(COLON_REPLACEMENT, COLON);
      }

      public static String encode(String s)
      {
         return s.replace(OPEN_BRACE, OPEN_BRACE_REPLACEMENT).replace(CLOSE_BRACE, CLOSE_BRACE_REPLACEMENT).replace(COLON, COLON_REPLACEMENT);
      }
   }

   public static class PortletNameFormatter implements ObjectFormatter
   {
      public static final String SLASH_REPLACEMENT = "-_-";
      private static final String SLASH = "/";

      public String decodeNodeName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public static String decode(String s)
      {
         return s.replace(SLASH_REPLACEMENT, SLASH);
      }

      public String encodeNodeName(FormatterContext formatterContext, String s) throws IllegalArgumentException, NullPointerException
      {
         return encode(s);
      }

      public static String encode(String s)
      {
         return s.replace(SLASH, SLASH_REPLACEMENT);
      }
   }
}

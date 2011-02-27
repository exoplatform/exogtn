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

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class InvalidationManager
{

   /** . */
   private final ObservationManager om;

   /** . */
   private LinkedList<Registration> registrations;

   InvalidationManager(ObservationManager om)
   {
      this.om = om;
      this.registrations = new LinkedList<Registration>();
   }

   void register(String nodeType, int eventTypes, Invalidator invalidator) throws RepositoryException
   {
      Registration registration = new Registration(nodeType, invalidator);
      om.addEventListener(registration, eventTypes, "/", true, null, new String[]{nodeType}, false);
      registrations.add(registration);
   }

   void close()
   {
      for (Registration registration : registrations)
      {
         try
         {
            om.removeEventListener(registration);
         }
         catch (RepositoryException e)
         {
            e.printStackTrace();
         }
      }
   }

   private static class Registration implements EventListener
   {

      /** . */
      private final String nodeType;

      /** . */
      private final Invalidator invalidator;

      private Registration(String nodeType, Invalidator invalidator)
      {
         this.nodeType = nodeType;
         this.invalidator = invalidator;
      }

      public void onEvent(EventIterator events)
      {
         while (events.hasNext())
         {
            try
            {
               Event event = events.nextEvent();
               invalidator.invalidate(event.getType(), nodeType, event.getPath());
            }
            catch (RepositoryException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}

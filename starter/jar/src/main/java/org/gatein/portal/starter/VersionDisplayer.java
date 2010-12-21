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
package org.gatein.portal.starter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * To display in the log the used version of EPP on startup
 * @author <a href="theute@redhat.com">Thomas Heute</a>
 * @version 1.0
 */
public class VersionDisplayer implements ServletContextListener
{
	private Logger logger = LoggerFactory.getLogger(VersionDisplayer.class);

	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	public void contextInitialized(ServletContextEvent arg0) {
		String eppVersion;
		try {
			eppVersion = this.getClass().getPackage().getImplementationVersion();
		} catch (Exception e) {
			eppVersion = "Version not found";
			e.printStackTrace();
		}
		logger.info("JBoss Enterprise Portal Platform [" + eppVersion + "]");
	}

}

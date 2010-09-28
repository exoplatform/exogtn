====
    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.
====

########################################
# HOW TO DEPLOY
########################################

########################################
# On JBoss 
########################################

We assume that you have a clean JBoss version of GateIn: ie We assume that you have already the file gatein.ear in the deploy directory 
of jboss

You need to:

1. Add the file sample-portal.ear from sample/portal/ear/target/ to the deploy directory of jboss 
2. Add the file starter.ear from starter/ear/target/ to the deploy directory of jboss 
  
WARNING: This can only work if a Unified ClassLoader has been configured on your JBoss (default behavior) and
the load order is first the gatein.ear then the sample-portal.ear and finally the starter.ear

########################################
# HOW TO TEST
########################################

########################################
# On JBoss (tested on JBoss 5.1.0.GA)
########################################

You need to:

1. Go to the bin directory of jboss 
2. Launch "./run.sh" or "run.bat"
3. When jboss is ready, you can launch your web browser and access to http://localhost:8080/sample-portal 

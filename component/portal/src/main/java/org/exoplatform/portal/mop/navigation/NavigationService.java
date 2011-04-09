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

import org.exoplatform.portal.mop.SiteKey;

/**
 * <p>The navigation service takes care of managing the various portal navigations and their nodes.</p>
 *
 * <p>In order to manage an efficient loading of the nodes, a {@link Scope} is used to describe the set of nodes
 * that should be retrieved when a loading operation is performed.</p>
 *
 * <p>The node operations does not provide a model per se, but instead use the {@link NodeModel} interface to plug
 * an API model. Various node operations are quite complex and any API in front of this service would need to perform
 * a manual, error prone and tedious synchronization. Instead the model interface allows the navigation service to
 * operate directly on an existing model.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface NavigationService
{

   /**
    * Find and returns a navigation, if no such site exist, null is returned instead.
    *
    * @param key the navigation key
    * @return the matching navigation
    * @throws NullPointerException if the key is null
    * @throws NavigationServiceException if the navigation could not be loaded
    */
   Navigation loadNavigation(SiteKey key) throws NullPointerException, NavigationServiceException;

   /**
    * Create, update or destroy a navigation. When the navigation state is not null, the navigation
    * will be created or updated depending on whether or not the navigation already exists. When
    * the navigation state is null, the navigation will be destroyed.
    *
    * @param key they navigation key
    * @param state the navigation state
    * @return true if the intent succeeded
    * @throws NullPointerException if the key is null
    * @throws NavigationServiceException if the navigation could not be saved
    */
   boolean saveNavigation(SiteKey key, NavigationState state) throws NullPointerException, NavigationServiceException;

   /**
    * Load a navigation node from a specified navigation. The returned node will be the root node of the navigation.
    *
    * @param model the node model
    * @param navigation the navigation
    * @param scope the scope
    * @param <N> the node model generic type
    * @return the loaded node
    * @throws NullPointerException if any argument is null
    * @throws NavigationServiceException if the loading operation could not be performed
    */
   <N> N loadNode(NodeModel<N> model, Navigation navigation, Scope scope) throws NullPointerException, NavigationServiceException;

   /**
    * Load a navigation node from a specified node. It will affect the node argument as well as all its descendants
    * when they are loaded according to the specified scope. The returned node is either the same node or null
    * if the node was skipped precisely.
    *
    * @param model the node model
    * @param node the node
    * @param scope the scope
    * @param <N> the node model generic type
    * @return the loaded node
    * @throws NullPointerException if any argument is null
    * @throws NavigationServiceException if the loading operation could not be performed
    */
   <N> N loadNode(NodeModel<N> model, N node, Scope scope) throws NullPointerException, NavigationServiceException;

   <N> void saveNode(NodeModel<N> model, N node) throws NullPointerException, NavigationServiceException;

   <N> void saveNode2(NodeModel<N> model, N node) throws NullPointerException, NavigationServiceException;

}

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

import org.chromattic.api.Chromattic;
import org.exoplatform.commons.utils.Queues;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import static org.exoplatform.portal.mop.navigation.Utils.*;

import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.exoplatform.portal.tree.diff.HierarchyChangeIterator;
import org.exoplatform.portal.tree.diff.HierarchyChangeType;
import org.exoplatform.portal.tree.diff.HierarchyDiff;
import org.exoplatform.portal.tree.diff.ListAdapter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.PageLink;

import javax.jcr.Session;
import java.util.*;

import static org.exoplatform.portal.pom.config.Utils.split;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceImpl implements NavigationService
{

   /** . */
   final POMSessionManager manager;

   /** . */
   private final Cache cache;

   /** . */
   final Logger log = LoggerFactory.getLogger(NavigationServiceImpl.class);

   public NavigationServiceImpl(POMSessionManager manager)
   {
      if (manager == null)
      {
         throw new NullPointerException("No null pom session manager allowed");
      }
      this.manager = manager;
      this.cache = new CacheById();
   }

   public void start() throws Exception
   {
      Chromattic chromattic = manager.getLifeCycle().getChromattic();
      Session session = chromattic.openSession().getJCRSession();
      cache.start(session);
   }


   public void stop()
   {
      cache.stop();
   }

   public NavigationContext loadNavigation(SiteKey key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      return cache.getNavigation(session, key);
   }

   public boolean saveNavigation(SiteKey key, NavigationState state) throws NavigationServiceException
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      POMSession session = manager.getSession();
      ObjectType<Site> objectType = objectType(key.getType());
      Workspace workspace = session.getWorkspace();
      Site site = workspace.getSite(objectType, key.getName());
      if (site == null)
      {
         throw new NavigationServiceException(NavigationError.NAVIGATION_CONCURRENCY_REMOVED);
      }

      //
      if (state != null)
      {
         Navigation root = site.getRootNavigation();
         Navigation rootNode = root.getChild("default");
         boolean created = rootNode == null;
         if (created)
         {
            rootNode = root.addChild("default");
         }
         rootNode.getAttributes().setValue(MappedAttributes.PRIORITY, state.getPriority());
         return created;
      }
      else
      {
         Navigation root = site.getRootNavigation();
         Navigation rootNode = root.getChild("default");
         boolean destroyed = rootNode != null;
         if (destroyed)
         {
            rootNode.destroy();
         }
         return destroyed;
      }
   }

   public <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (navigation == null)
      {
         throw new NullPointerException();
      }
      if (scope == null)
      {
         throw new NullPointerException();
      }
      String nodeId = navigation.rootId;
      if (navigation.rootId != null)
      {
         POMSession session = manager.getSession();
         NodeData data = cache.getNodeData(session, nodeId);
         if (data != null)
         {
            NodeContext<N> context = new NodeContext<N>(new TreeContext<N>(model), data);
            Scope.Visitor visitor = scope.get();
            expand(session, context, visitor, 0);
            return context;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   public <N> Iterator<NodeChange<N>> updateNode(final NodeContext<N> root, Scope scope) throws NullPointerException, NavigationServiceException
   {

      final POMSession session = manager.getSession();
      TreeContext<N> tree = root.tree;

      List<NodeChange<NodeContext<N>>> changes = tree.popChanges();
      if (changes.size() > 0)
      {
         throw new IllegalArgumentException("For now we don't accept to update a context that has pending changes " + changes);
      }

      ListAdapter<String[], String> a1 = new ListAdapter<String[], String>()
      {
         public int size(String[] list)
         {
            return list.length;
         }
         public Iterator<String> iterator(final String[] list, final boolean reverse)
         {
            return new Iterator<String>()
            {
               int count = 0;
               public boolean hasNext()
               {
                  return count < list.length;
               }
               public String next()
               {
                  if (!hasNext())
                  {
                     throw new NoSuchElementException();
                  }
                  int index = count++;
                  if (reverse)
                  {
                     index = list.length - index - 1;
                  }
                  return list[index];
               }
               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };

      //
      class M1 implements HierarchyAdapter<String[], NodeContext<N>, String>
      {
         public String getHandle(NodeContext<N> node)
         {
            return node.data.id;
         }
         public String[] getChildren(NodeContext<N> node)
         {
            return node.hasContexts() ? node.data.children : new String[0];
         }
         public NodeContext<N> getDescendant(NodeContext<N> node, String handle)
         {
            return root.getDescendant(handle);
         }
      }

      //
      class M2 implements HierarchyAdapter<String[], NodeData, String>
      {
         public String getHandle(NodeData node)
         {
            return node.id;
         }
         public String[] getChildren(NodeData node)
         {
            NodeContext<N> context = root.getDescendant(node.getId());
            return context != null && context.hasContexts() ? node.children : new String[0];
         }
         public NodeData getDescendant(NodeData node, String handle)
         {
            return cache.getNodeData(session, handle);
         }
      }

      //
      HierarchyDiff<String[], NodeContext<N>, String[], NodeData, String> diff =
         new HierarchyDiff<String[], NodeContext<N>, String[], NodeData, String>(
            a1,
            new M1(),
            a1,
            new M2(),
            new Comparator<String>()
            {
               public int compare(String s1, String s2)
               {
                  return s1.compareTo(s2);
               }
            }
         );

      //
      List<NodeChange<N>> list = Collections.emptyList();

      // Switch to edit mode
      tree.editMode = true;

      // Apply diff changes to the model
      try
      {
         NodeData dataRoot = cache.getNodeData(session, root.data.id);
         HierarchyChangeIterator<String[], NodeContext<N>, String[], NodeData, String> it = diff.iterator(root, dataRoot);
         Queue<NodeContext<N>> stack = Queues.lifo();
         NodeContext<N> last = null;
         while (it.hasNext())
         {
            HierarchyChangeType change = it.next();
            switch (change)
            {
               case ENTER:
                  stack.add(it.getSource());
                  break;
               case LEAVE:
                  last = stack.poll();
                  break;
               case MOVED_OUT:
                  break;
               case MOVED_IN:
               {
                  NodeContext<N> to = stack.peek();
                  NodeContext<N> moved = it.getSource();
                  NodeContext<N> from = moved.getParent();
                  NodeContext<N> previous;
                  if (last == null || last.getParent() != to)
                  {
                     previous = null;
                     to.insertAt(0, moved);
                  }
                  else
                  {
                     previous = last;
                     last.insertAfter(moved);
                  }

                  //
                  if (list.isEmpty()) {
                     list = new LinkedList<NodeChange<N>>();
                  }
                  list.add(new NodeChange.Moved<N>(
                     from.getNode(),
                     to.getNode(),
                     previous != null ? previous.getNode() : null,
                     moved.getNode()));

                  //
                  break;
               }
               case ADDED:
               {
                  NodeContext<N> parent = stack.peek();
                  NodeContext<N> added = new NodeContext<N>(parent.tree, it.getDestination());
                  NodeContext<N> previous;
                  if (last == null || last.getParent() != parent)
                  {
                     previous = null;
                     parent.insertAt(0, added);
                  }
                  else
                  {
                     previous = last;
                     last.insertAfter(added);
                  }

                  //
                  if (list.isEmpty()) {
                     list = new LinkedList<NodeChange<N>>();
                  }
                  list.add(new NodeChange.Added<N>(
                     parent.getNode(),
                     previous != null ? previous.getNode() : null,
                     added.getNode(),
                     added.getName()));

                  //
                  break;
               }
               case REMOVED:
               {
                  NodeContext<N> removed = it.getSource();
                  NodeContext<N> parent = removed.getParent();

                  //
                  removed.remove();

                  //
                  if (list.isEmpty()) {
                     list = new LinkedList<NodeChange<N>>();
                  }
                  list.add(new NodeChange.Removed<N>(
                     parent.getNode(),
                     removed.getNode()));

                  //
                  break;
               }
               default:
                  throw new UnsupportedOperationException("todo : " + change);
            }
         }
      }
      finally
      {
         // Disable edit mode
         tree.editMode = false;
      }

      // Now update with scope
      if (scope != null)
      {
         expand(session, root, scope.get(), 0);
      }

      //
      return list.iterator();
   }

   private <N> void expand(POMSession session, NodeContext<N> context, Scope.Visitor visitor, int depth)
   {
      if (context.hasContexts())
      {
         for (NodeContext<N> current = context.getFirst();current != null;current = current.getNext())
         {
            expand(session, current, visitor, depth + 1);
         }
      }
      else
      {
         NodeData data = context.data;
         VisitMode visitMode = visitor.visit(depth, data.id, data.name, data.state);
         if (visitMode == VisitMode.ALL_CHILDREN)
         {
            ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.length);
            for (String childId : data.children)
            {
               NodeData childData = cache.getNodeData(session, childId);
               if (childData != null)
               {
                  NodeContext<N> child = new NodeContext<N>(context.tree, childData);
                  expand(session, child, visitor, depth + 1);
                  children.add(child);
               }
               else
               {
                  throw new UnsupportedOperationException("Handle me gracefully");
               }
            }
            context.setContexts(children);
         }
         else
         {
            // Do nothing
         }
      }
   }

   public <N> void saveNode(NodeContext<N> context) throws NullPointerException, NavigationServiceException
   {
      POMSession session = manager.getSession();
      TreeContext<N> tree = context.tree;

      //
      List<NodeChange<NodeContext<N>>> changes = tree.popChanges();

      // First pass we update persistent store
      for (NodeChange<NodeContext<N>> change : changes)
      {
         if (change instanceof NodeChange.Added<?>)
         {
            NodeChange.Added<NodeContext<N>> add = (NodeChange.Added<NodeContext<N>>)change;

            //
            Navigation parent = session.findObjectById(ObjectType.NAVIGATION, add.parent.data.id);
            if (parent == null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
            }

            //
            Navigation added = parent.getChild(add.name);
            if (added != null)
            {
               throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
            }
            else
            {
               added = parent.addChild(add.name);
               int index = 0;
               if (add.previous != null)
               {
                  Navigation previous = session.findObjectById(ObjectType.NAVIGATION, add.previous.data.id);
                  if (previous == null)
                  {
                     throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
                  }
                  index = parent.getChildren().indexOf(previous) + 1;
               }
               parent.getChildren().add(index, added);
               add.node.data = new NodeData(added);
            }
         }
         else if (change instanceof NodeChange.Removed<?>)
         {
            NodeChange.Removed<NodeContext<N>> remove = (NodeChange.Removed<NodeContext<N>>)change;
            Navigation removed = session.findObjectById(ObjectType.NAVIGATION, remove.node.data.id);
            if (removed != null)
            {
               Navigation parent = removed.getParent();
               removed.destroy();
               remove.node.data = null;
            }
            else
            {
               // It was already removed concurrently
            }
         }
         else if (change instanceof NodeChange.Moved<?>)
         {
            NodeChange.Moved<NodeContext<N>> move = (NodeChange.Moved<NodeContext<N>>)change;
            Navigation src = session.findObjectById(ObjectType.NAVIGATION, move.from.data.id);
            if (src == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
            }

            //
            Navigation dst = session.findObjectById(ObjectType.NAVIGATION, move.to.data.id);
            if (dst == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
            }

            //
            Navigation moved = session.findObjectById(ObjectType.NAVIGATION, move.node.data.id);
            if (moved == null)
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
            }

            //
            if (src != moved.getParent())
            {
               throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
            }

            //
            int index = 0;
            if (move.previous != null)
            {
               Navigation previous = session.findObjectById(ObjectType.NAVIGATION, move.previous.data.id);
               if (previous == null)
               {
                  throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
               }
               index = dst.getChildren().indexOf(previous) + 1;
            }
            dst.getChildren().add(index, moved);
         }
         else if (change instanceof NodeChange.Renamed)
         {
            NodeChange.Renamed<NodeContext<N>> rename = (NodeChange.Renamed<NodeContext<N>>)change;
            Navigation renamed = session.findObjectById(ObjectType.NAVIGATION, rename.node.data.id);
            if (renamed == null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
            }

            //
            Navigation parent = renamed.getParent();
            if (parent.getChild(rename.name) != null)
            {
               throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
            }

            // We rename and reorder to compensate the move from the rename
            List<Navigation> children = parent.getChildren();
            int index = children.indexOf(renamed);
            renamed.setName(rename.name);
            children.add(index, renamed);
         }
         else
         {
            throw new AssertionError("Cannot execute " + change);
         }
      }

      // Update state
      saveState(session, context);
   }

   private <N> void saveState(POMSession session, NodeContext<N> context) throws NavigationServiceException
   {
      NodeState state = context.state;

      // For now we just do a reference comparison but it would make sense
      // to use an equals instead
      if (state != null && !state.equals(context.data.state))
      {
         Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, context.data.id);

         //
         if (navigation == null)
         {
            throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
         }

         //
         Workspace workspace = navigation.getSite().getWorkspace();
         String reference = state.getPageRef();
         if (reference != null)
         {
            String[] pageChunks = split("::", reference);
            ObjectType<? extends Site> siteType = Mapper.parseSiteType(pageChunks[0]);
            Site site = workspace.getSite(siteType, pageChunks[1]);
            org.gatein.mop.api.workspace.Page target = site.getRootPage().getChild("pages").getChild(pageChunks[2]);
            PageLink link = navigation.linkTo(ObjectType.PAGE_LINK);
            link.setPage(target);
         }
         else
         {
            PageLink link = navigation.linkTo(ObjectType.PAGE_LINK);
            link.setPage(null);
         }

         //
         Described described = navigation.adapt(Described.class);
         described.setName(state.getLabel());

         //
         Visible visible = navigation.adapt(Visible.class);
         visible.setVisibility(state.getVisibility());

         //
         visible.setStartPublicationDate(state.getStartPublicationDate());
         visible.setEndPublicationDate(state.getEndPublicationDate());

         //
         Attributes attrs = navigation.getAttributes();
         attrs.setValue(MappedAttributes.URI, state.getURI());
         attrs.setValue(MappedAttributes.ICON, state.getIcon());
      }

      //
      context.data = context.toData();

      //
      context.state = null;

      //
      if (context.hasContexts())
      {
         for (NodeContext<N> child : context.getContexts())
         {
            saveState(session, child);
         }
      }
   }















   // Code to remove soon below

   private <N> NodeContext<N> load(
      TreeContext<N> tree,
      POMSession session,
      NodeData data,
      Scope.Visitor visitor,
      int depth)
   {
      VisitMode visitMode = visitor.visit(depth, data.id, data.name, data.state);

      //
      NodeContext<N> context;
      if (visitMode == VisitMode.ALL_CHILDREN)
      {
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.length);
         for (String childId : data.children)
         {
            NodeData childData = cache.getNodeData(session, childId);
            if (childData != null)
            {
               NodeContext<N> childContext = load(tree, session, childData, visitor, depth + 1);
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context = new NodeContext<N>(tree, data);
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         context = new NodeContext<N>(tree, data);
      }
      else
      {
         context = new NodeContext<N>(tree, data);
      }

      //
      return context;
   }

   public <N> NodeContext<N> loadNode(NodeContext<N> context, Scope scope)
   {
      POMSession session = manager.getSession();
      Scope.Visitor visitor = scope.get();

      //
      String nodeId = context.getId();
      NodeData data = cache.getNodeData(session, nodeId);

      //
      if (data != null)
      {
         context.data = data;
         visit(session, context, visitor, 0);
         return context;
      }
      return null;
   }

   private <N> void visit(
      POMSession session,
      NodeContext<N> context,
      Scope.Visitor visitor,
      int depth)
   {
      NodeData data = context.data;

      //
      VisitMode visitMode;
      if (context.hasContexts())
      {
         visitMode = VisitMode.ALL_CHILDREN;
      }
      else
      {
         visitMode = visitor.visit(depth, data.id, data.name, data.state);
      }

      //
      if (visitMode == VisitMode.ALL_CHILDREN)
      {
         Map<String, NodeContext<N>> previous = Collections.emptyMap();
         if (context.hasContexts())
         {
            previous = new HashMap<String, NodeContext<N>>();
            for (NodeContext<N> a : context.getContexts())
            {
               if (a.data != null)
               {
                  previous.put(a.getId(), a);
               }
            }
            context.setContexts(null);
         }

         //
         ArrayList<NodeContext<N>> children = new ArrayList<NodeContext<N>>(data.children.length);
         for (String childId : data.children)
         {
            NodeData childData = cache.getNodeData(session, childId);
            if (childData != null)
            {
               NodeContext<N> childContext = previous.get(childId);
               if (childContext != null)
               {
                  childContext.data = childData;
                  visit(session, childContext, visitor, depth + 1);
               }
               else
               {
                  childContext = load(context.tree, session, childData, visitor, depth + 1);
               }
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         if (context.hasContexts())
         {
            context.setContexts(null);
         }
      }
      else
      {
         throw new AssertionError();
      }
   }
}

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
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import static org.exoplatform.portal.mop.navigation.Utils.*;

import org.exoplatform.portal.tree.sync.diff.Diff;
import org.exoplatform.portal.tree.sync.diff.DiffChangeIterator;
import org.exoplatform.portal.tree.sync.diff.DiffChangeType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;
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

   public Navigation loadNavigation(SiteKey key)
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
         throw new NavigationServiceException("The site " + key + " does not exist");
      }

      //
      if (state != null)
      {
         org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
         org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
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
         org.gatein.mop.api.workspace.Navigation root = site.getRootNavigation();
         org.gatein.mop.api.workspace.Navigation rootNode = root.getChild("default");
         boolean destroyed = rootNode != null;
         if (destroyed)
         {
            rootNode.destroy();
         }
         return destroyed;
      }
   }

   public <N> N loadNode(NodeModel<N> model, Navigation navigation, Scope scope)
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
         Scope.Visitor visitor = scope.get();
         NodeData data = cache.getNodeData(session, nodeId);
         if (data != null)
         {
            NodeContext<N> context = load(new TreeContext<N>(), model, session, data, visitor, 0);
            return context.node;
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

   private <N> NodeContext<N> load(
      TreeContext<N> tree,
      NodeModel<N> model,
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
               NodeContext<N> childContext = load(tree, model, session, childData, visitor, depth + 1);
               children.add(childContext);
            }
            else
            {
               throw new UnsupportedOperationException("Handle me gracefully");
            }
         }

         //
         context = tree.newContext(model, data);
         context.setContexts(children);
      }
      else if (visitMode == VisitMode.NO_CHILDREN)
      {
         context = tree.newContext(model, data);
      }
      else
      {
         context = tree.newContext(model, data);
      }

      //
      return context;
   }

   public <N> N loadNode(NodeModel<N> model, N node, Scope scope)
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      Scope.Visitor visitor = scope.get();

      //
      String nodeId = context.getId();
      NodeData data = cache.getNodeData(session, nodeId);

      //
      if (data != null)
      {
         context.data = data;
         visit(model, session, context, visitor, 0);
         return context.node;
      }
      return null;
   }

   private <N> void visit(
      NodeModel<N> model,
      POMSession session,
      NodeContext<N> context,
      Scope.Visitor visitor,
      int depth)
   {
      NodeData data = context.data;

      //
      VisitMode visitMode;
      if (context.hasTrees())
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
         if (context.hasTrees())
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
                  visit(model, session, childContext, visitor, depth + 1);
               }
               else
               {
                  childContext = load(context.tree, model, session, childData, visitor, depth + 1);
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
         if (context.hasTrees())
         {
            context.setContexts(null);
         }
      }
      else
      {
         throw new AssertionError();
      }
   }


   public <N> void saveNode2(NodeModel<N> model, N node) throws NullPointerException, NavigationServiceException
   {


      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);
      TreeContext<N> tree = context.tree;


      Diff<NodeData, NodeData, NodeContext<N>, NodeContext<N>, String> diff =
         new Diff<NodeData, NodeData, NodeContext<N>, NodeContext<N>, String>(
            tree,
            tree,
            Sync.<N>getNodeContextAdapter(),
            Sync.<N>getNodeContextModel(),
            new Comparator<String>()
            {
               public int compare(String o1, String o2)
               {
                  return o1.compareTo(o2);
               }
            }
         );

      DiffChangeIterator<NodeData, NodeData, NodeContext<N>, NodeContext<N>, String> it = diff.perform(context.data, context);
      org.gatein.mop.api.workspace.Navigation current = session.findObjectById(ObjectType.NAVIGATION, context.getId());
      while (it.hasNext())
      {
         DiffChangeType change = it.next();
         switch (change)
         {
            case ENTER:
               current = session.findObjectById(ObjectType.NAVIGATION, it.getSource().getId());
               break;
            case LEAVE:
               break;
            case ADDED:
            {
               NodeContext<N> destination = it.getDestination();
               current.addChild(destination.getName());
               break;
            }
            case REMOVED:
            {
               NodeData source = it.getSource();
               org.gatein.mop.api.workspace.Navigation a = current.getChild(source.getName());
               if (a != null)
               {
                  if (a.getObjectId().equals(source.getId()))
                  {
                     a.destroy();
                  }
                  else
                  {
                     throw new UnsupportedOperationException("Handle me gracefully");
                  }
               }
               break;
            }
            default:
               throw new AssertionError("Does not handle yet " + change);
         }
      }


   }













   public <N> void saveNode(NodeModel<N> model, N node) throws NavigationServiceException
   {
      POMSession session = manager.getSession();
      NodeContext<N> context = model.getContext(node);

      //
      SaveContext<N> save = new SaveContext<N>(context);

      //
      save.phase0(session);

      //
      save.phase1(session);

      //
      save.phase2(session);

      //
      save.phase3(session);

      //
      save.phase4(session);

      //
      save.phase5(session);

      //
      save.phase6(session);
   }

   private static class SaveContext<N>
   {

      /** . */
      private final NodeContext<N> context;

      /** . */
      private final List<SaveContext<N>> children;

      /** The list of actual children ids, maintained during the phases. */
      private List<String> childrenIds;

      /** . */
      private SaveContext<N> parent;

      /** The related navigation object. */
      private org.gatein.mop.api.workspace.Navigation navigation;

      private SaveContext(NodeContext<N> context)
      {
         List<SaveContext<N>> children;
         if (context.hasTrees())
         {
            children = new ArrayList<SaveContext<N>>();
            for (NodeContext<N> childCtx : context.getContexts())
            {
               SaveContext<N> child = new SaveContext<N>(childCtx);
               children.add(child);
               child.parent = this;
            }
         }
         else
         {
            children = Collections.emptyList();
         }

         //
         List<String> bilto;
         if (context.data != null)
         {
            String[] array = context.data.children;
            int length = array.length;
            bilto = new ArrayList<String>(length);
            for (int i = 0;i < length;i++)
            {
               bilto.add(array[i]);
            }
         }
         else
         {
            bilto = new ArrayList<String>();
         }

         //
         this.context = context;
         this.children = children;
         this.childrenIds = bilto;
         this.navigation = null;
      }

      static abstract class Finder<N>
      {

         final SaveContext<N> any(SaveContext<N> context)
         {
            SaveContext<N> root = context;
            while (root.parent != null)
            {
               root = root.parent;
            }
            return descendants(root);
         }

         final SaveContext<N> descendants(SaveContext<N> context)
         {
            SaveContext<N> found = null;
            if (accept(context))
            {
               found = context;
            }
            else
            {
               int size = context.children.size();
               for (int i = 0;i < size;i++)
               {
                  found = descendants(context.children.get(i));
                  if (found != null)
                  {
                     break;
                  }
               }
            }
            return found;
         }

         abstract boolean accept(SaveContext<N> context);
      }

      // Remove orphans
      void phase0(POMSession session)
      {
         if (context.hasTrees())
         {
            if (context.data != null)
            {
               for (final String childId : context.data.children)
               {
                  // Is it still here ?
                  boolean found = false;
                  for (NodeContext<N> childContext : context.getContexts())
                  {
                     if (childContext.data != null && childContext.data.id.equals(childId))
                     {
                        found = true;
                     }
                  }

                  //
                  if (!found)
                  {
                     Finder<N> finder = new Finder<N>()
                     {
                        boolean accept(SaveContext<N> context)
                        {
                           return context.context.data != null && context.context.data.getId().equals(childId);
                        }
                     };
                     if (finder.any(this) == null)
                     {
                        org.gatein.mop.api.workspace.Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, childId);
                        navigation.destroy();
                     }
                     else
                     {
                        // It's a move operation
                     }
                     childrenIds.remove(childId);
                  }
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase0(session);
         }
      }

      // Create new nodes and associates with navigation object
      void phase1(POMSession session) throws NavigationServiceException
      {
         if (context.data == null)
         {
            throw new IllegalArgumentException();
         }
         else
         {
            navigation = session.findObjectById(ObjectType.NAVIGATION, context.getId());

            //
            if (navigation == null)
            {
               throw new NavigationSaveException("The node " + context.getId() + " does not exist anymore");
            }
            else
            {
               for (SaveContext<N> child : children)
               {
                  if (child.context.data == null)
                  {
                     org.gatein.mop.api.workspace.Navigation added = navigation.addChild(child.context.getName());
                     child.context.data = new NodeData(added);
                     childrenIds.add(added.getObjectId());
                  }
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase1(session);
         }
      }

      // Rename nodes
      void phase2(POMSession session)
      {
         if (!context.data.name.equals(context.getName()))
         {
            navigation.setName(context.getName());
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase2(session);
         }
      }

      // Update state
      void phase3(POMSession session)
      {
         NodeState state = context.state;
         if (state != null)
         {
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
         for (SaveContext<N> child : children)
         {
            child.phase3(session);
         }
      }

      // Move nodes
      void phase4(POMSession session)
      {

         if (context.hasTrees())
         {
            for (NodeContext<N> childCtx : context.getContexts())
            {
               final String childName = childCtx.data.name;
               final String childId = childCtx.data.id;
               if (!context.data.hasChild(childId))
               {
                  if (!childrenIds.contains(childId))
                  {
                     Finder<N> finder = new Finder<N>()
                     {
                        boolean accept(SaveContext<N> context)
                        {
                           return context.context.data.getId().equals(childId);
                        }
                     };
                     SaveContext<N> movedCtx = finder.any(this);
                     org.gatein.mop.api.workspace.Navigation moved = movedCtx.navigation;
                     navigation.getChildren().add(moved);
                     childrenIds.add(childId);
                  }
               }
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase4(session);
         }
      }

      // Reorder nodes
      void phase5(POMSession session)
      {
         if (context.hasTrees())
         {
            final ArrayList<String> orders = new ArrayList<String>();
            for (NodeContext<N> foo : context.getContexts())
            {
               orders.add(foo.data.id);
            }

            // Now sort children according to the order provided by the container
            // need to replace that with Collections.sort once the set(int index, E element) is implemented in Chromattic lists
            org.gatein.mop.api.workspace.Navigation[] a = navigation.getChildren().toArray(new org.gatein.mop.api.workspace.Navigation[navigation.getChildren().size()]);


            //
            LinkedList<org.gatein.mop.api.workspace.Navigation> d = new LinkedList<org.gatein.mop.api.workspace.Navigation>();
            org.gatein.mop.api.workspace.Navigation[] b = new org.gatein.mop.api.workspace.Navigation[a.length];
            for (org.gatein.mop.api.workspace.Navigation c : a)
            {
               int order = orders.indexOf(c.getObjectId());
               if (order == -1)
               {
                  d.add(c);
               }
               else
               {
                  // Could we have an array index out of bounds
                  b[order] = c;
               }
            }
            for (int i = 0;i < b.length;i++)
            {
               if (b[i] == null)
               {
                  b[i] = d.removeFirst();
               }
            }

            //
            for (int j = 0; j < b.length; j++)
            {
               navigation.getChildren().add(j, b[j]);
            }
         }

         //
         for (SaveContext<N> child : children)
         {
            child.phase5(session);
         }
      }

      // Update model
      void phase6(POMSession session)
      {
         String[] childrenIds;
         if (context.hasTrees())
         {
            // todo : I think we have a bug here :-)
            childrenIds = new String[0];
         }
         else
         {
            childrenIds = context.data.children;
         }

         //
         String id = context.data.id;
         String name = context.getName();
         NodeState state = context.getState();

         //
         context.data = new NodeData(id, name, state, childrenIds);

         //
         for (SaveContext<N> child : children)
         {
            child.phase6(session);
         }
      }
   }
}

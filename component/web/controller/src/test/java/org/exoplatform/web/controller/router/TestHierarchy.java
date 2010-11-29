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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestHierarchy extends AbstractTestController
{

   public void testFoo() throws Exception
   {
      Router router = router().
         add(route("/a").add(routeParam("foo").withValue("bar")).
         add(route("/b").add(routeParam("juu").withValue("daa")))).
         build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.create("foo"), "bar"), router.route("/a"));

      //
      Map<QualifiedName, String> expected = new HashMap<QualifiedName, String>();
      expected.put(QualifiedName.create("foo"), "bar");
      expected.put(QualifiedName.create("juu"), "daa");
      assertEquals(expected, router.route("/a/b"));
   }
}

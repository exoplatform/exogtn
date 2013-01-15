/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.web.application;

import java.io.Serializable;
import java.util.Arrays;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ApplicationMessage extends AbstractApplicationMessage implements Serializable
{
   private final String messageKey_;
   private final Object[] messageArgs_;

   public ApplicationMessage(String key, Object[] args)
   {
      this.messageKey_ = key;
      this.messageArgs_ = args;
   }

   public ApplicationMessage(String key, Object[] args, int type)
   {
      this.messageKey_ = key;
      this.messageArgs_ = args;
      setType(type);
   }
   
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ApplicationMessage that = (ApplicationMessage) o;

    if (!Arrays.equals(messageArgs_, that.messageArgs_)) {
      return false;
    }
    if (!messageKey_.equals(that.messageKey_)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = messageKey_.hashCode();
    result = 31 * result
        + (messageArgs_ != null ? Arrays.hashCode(messageArgs_) : 0);
    return result;
  }
   
   public String getMessageKey()
   {
      return messageKey_;
   }

   public String getMessage()
   {
      String msg = resolveMessage(messageKey_);
      if (msg != null && messageArgs_ != null)
      {
         for (int i = 0; i < messageArgs_.length; i++)
         {
            String arg = messageArgs_[i].toString();
            if (isArgsLocalized())
            {
               arg = resolveMessage(arg);
            }
            msg = msg.replace("{" + i + "}", arg);
         }
      }

      return msg;
   }
}

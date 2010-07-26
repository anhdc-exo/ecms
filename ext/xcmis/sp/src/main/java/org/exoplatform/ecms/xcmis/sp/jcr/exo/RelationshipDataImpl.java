/**
 *  Copyright (C) 2003-2010 eXo Platform SAS.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.ecms.xcmis.sp.jcr.exo;

import org.exoplatform.ecms.xcmis.sp.jcr.exo.index.IndexListener;
import org.xcmis.spi.CmisConstants;
import org.xcmis.spi.ConstraintException;
import org.xcmis.spi.ContentStream;
import org.xcmis.spi.FolderData;
import org.xcmis.spi.RelationshipData;
import org.xcmis.spi.StorageException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: RelationshipDataImpl.java 1177 2010-05-25 12:03:35Z
 *          alexey.zavizionov@gmail.com $
 */
class RelationshipDataImpl extends BaseObjectData implements RelationshipData
{

   public RelationshipDataImpl(JcrNodeEntry jcrEntry, IndexListener indexListener)
   {
      super(jcrEntry, indexListener);
   }

   /**
    * {@inheritDoc}
    */
   public String getSourceId()
   {
      return jcrEntry.getString(CmisConstants.SOURCE_ID);
   }

   /**
    * {@inheritDoc}
    */
   public String getTargetId()
   {
      return jcrEntry.getString(CmisConstants.TARGET_ID);
   }

   /**
    * {@inheritDoc}
    */
   public ContentStream getContentStream(String streamId)
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public FolderData getParent() throws ConstraintException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<FolderData> getParents()
   {
      return Collections.emptyList();
   }

   protected void delete() throws StorageException
   {
      {
         String objectId = getObjectId();
         try
         {
            Node node = getNode();
            Session session = node.getSession();
            node.remove();
            session.save();
         }
         catch (RepositoryException re)
         {
            throw new StorageException("Unable delete object. " + re.getMessage(), re);
         }
         if (indexListener != null)
         {
            Set<String> removed = new HashSet<String>();
            removed.add(objectId);
            indexListener.removed(removed);
         }
      }
   }

}

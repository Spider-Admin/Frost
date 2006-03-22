/*
  UploadModelDAO.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.fileTransfer.upload;

import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public interface UploadModelDAO {
    /**
     * This method checks if underlying storage exists. If it does, it
     * returns true. If it doesn't (for instance, when the application
     * is started for the first time) it returns false.
     * @return true if the underlying storage exists. False otherwise.
     */
    public boolean exists();

    /**
     * This method loads the information contained in the storage and fills
     * the given UploadModel object with it.
     * @param uploadModel UploadModel object to be filled with the information
     * in the storage
     * @throws StorageException if there was a problem while loading the information.
     */
    public void load(UploadModel uploadModel) throws StorageException;

    /**
     * This method saves the information contained in the given UploadModel object
     * on the storage.
     * @param uploadModel UploadModel whose information is going to be saved
     * @throws StorageException if there was a problem while saving the information.
     */
    public void save(UploadModel uploadModel) throws StorageException;

    /**
     * This method creates the underlying storage.
     * @throws StorageException if there was a problem while creating the storage.
     */
    public void create() throws StorageException;
}

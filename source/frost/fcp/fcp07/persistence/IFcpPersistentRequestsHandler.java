/*
  IFcpPersistentRequestsHandler.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp07.persistence;


public interface IFcpPersistentRequestsHandler {

    public void persistentRequestAdded(FcpPersistentPut uploadRequest);
    public void persistentRequestAdded(FcpPersistentGet downloadRequest);

    public void persistentRequestUpdated(FcpPersistentPut uploadRequest);
    public void persistentRequestUpdated(FcpPersistentGet downloadRequest);
    
    public void persistentRequestRemoved(FcpPersistentPut uploadRequest);
    public void persistentRequestRemoved(FcpPersistentGet downloadRequest);
    
    public void persistentRequestModified(FcpPersistentPut uploadRequest);
    public void persistentRequestModified(FcpPersistentGet downloadRequest);
}

/*
  FrostMessageRequestJob.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.hyperocha;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Hashtable;

import frost.Core;
import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.job.KSKMessageRequestJob;

/**
 * @author saces
 */
public class FrostMessageRequestJob extends KSKMessageRequestJob {
	
	private long metaDataSize;
	private byte[] metaData;

	/**
	 * @param requirednetworktype
	 */
	public FrostMessageRequestJob(String key, File dest) {
		super(Core.getFcpVersion(), FHUtil.getNextJobID(), string2key(key), dest);
	}
	
	private static FreenetKey string2key(String uri) {
		FreenetKey key;
		if (Core.getFcpVersion() == Network.FCP2) {
			key = FreenetKey.KSKfromString(FHUtil.StripSlashes(uri));
		} else {
			key = FreenetKey.KSKfromString(uri);
		}
		return key;
	}

	/* override default handler and write the mo (meta + datafile) direct */ 
	
	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob#incommingData(java.lang.String, java.util.Hashtable, hyperocha.freenet.fcp.FCPConnection)
	 */
	public void incomingData(String id, Hashtable message, FCPConnection conn) {
		if (Core.getFcpVersion() == Network.FCP1) {
			if ("DataChunk".equals(message.get(FCPConnection.MESSAGENAME))) {
				long size = Long.parseLong((String)(message.get("Length")), 16);
				
				// FIXME TODO
				
				// write metaDataSize to mo.metadata
				System.out.println("KSK save DataHandler: " + message);
				System.out.println("KSK save DataHandler: " + metaDataSize + " -> " + size);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
				
				conn.copyFrom(metaDataSize, bos);
				
				metaData = bos.toByteArray();
//				 write (size - metaDataSize) to defaultfile
				conn.copyFrom((size - metaDataSize), getOutStream());
				// FIXME: daten sind ins file copiert, feierabend
				setSuccess();
				return;
			}
		}
		super.incomingData(id, message, conn);
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob#incommingMessage(java.lang.String, java.util.Hashtable)
	 */
	public void incomingMessage(String id, Hashtable message) {
		if (Core.getFcpVersion() == Network.FCP1) {
			if ("DataFound".equals(message.get(FCPConnection.MESSAGENAME))) {
				metaDataSize = Long.parseLong((String)(message.get("MetadataLength")), 16); 
			}
		}
		super.incomingMessage(id, message);		
	}

	public byte[] getMetaData() {
		return metaData;
	}
}

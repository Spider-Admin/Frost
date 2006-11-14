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

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.job.CHKMessageRequestJob;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Hashtable;

import frost.Core;

/**
 * @version $id"
 */
public class FrostCHKMessageRequestJob extends CHKMessageRequestJob {
	
    // FIXME: overwrite doPrepare(), jobStarted(), jobFinished()

	private long metaDataSize;
	private byte[] metaData;

	/**
	 * @param requirednetworktype
	 */
	public FrostCHKMessageRequestJob(String key, File dest) {
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
	public void incomingData(String id, NodeMessage msg, FCPConnection conn) {
		if (Core.getFcpVersion() == Network.FCP1) {
			if (msg.isMessageName("DataChunk")) {
				long size = msg.getLongValue("Length", 16);
				
				// FIXME TODO
				
				// write metaDataSize to mo.metadata
				System.out.println("KSK save DataHandler: " + msg);
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
		super.incomingData(id, msg, conn);
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob#incommingMessage(java.lang.String, java.util.Hashtable)
	 */
	public void incomingMessage(String id, NodeMessage msg) {
		if (Core.getFcpVersion() == Network.FCP1) {
			if (msg.isMessageName("DataFound")) {
				metaDataSize = msg.getLongValue("MetadataLength", 16); 
			}
		}
		super.incomingMessage(id, msg);		
	}

	public byte[] getMetaData() {
		return metaData;
	}
}

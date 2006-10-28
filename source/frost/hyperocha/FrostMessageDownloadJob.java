/**
 * 
 */
package frost.hyperocha;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Hashtable;

import frost.Core;
import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FreenetKey;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob;

/**
 * @author saces
 *
 */
public class FrostMessageDownloadJob extends KSKMessageDownloadJob {
	
	private long metaDataSize;
	private byte[] metaData;

	/**
	 * @param requirednetworktype
	 */
	public FrostMessageDownloadJob(String key, File dest) {
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
	public void incommingData(String id, Hashtable message, FCPConnection conn) {
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
		super.incommingData(id, message, conn);
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.job.KSKMessageDownloadJob#incommingMessage(java.lang.String, java.util.Hashtable)
	 */
	public void incommingMessage(String id, Hashtable message) {
		if (Core.getFcpVersion() == Network.FCP1) {
			if ("DataFound".equals(message.get(FCPConnection.MESSAGENAME))) {
				metaDataSize = Long.parseLong((String)(message.get("MetadataLength")), 16); 
			}
		}
		super.incommingMessage(id, message);		
	}

	public byte[] getMetaData() {
		return metaData;
	}
}

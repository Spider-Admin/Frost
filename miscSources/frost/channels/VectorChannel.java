/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.channels;

/**
 * @author zlatinb
 *
 * Currently Frost uses only Vector channels
 * the slots in these channels are formed prefix+index
 */
public abstract class VectorChannel implements Channel {
	public abstract IndexGenerator getIndexGenerator();
	
	
	protected IndexGenerator generator;
	protected final String prefix;
	
	public VectorChannel(IndexGenerator gen, String prefix) {
		generator = gen;
		this.prefix = prefix;
	}
	
	public VectorChannel(String prefix) {
		generator = new DefaultIndexGenerator();
		this.prefix = prefix;
	}

}

/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import frost.Core;
import frost.FileAccess;
import frost.frame1;
import frost.identities.Identity;


public class Saver extends Thread {
	private final Core core;
	/**
	 * @param Core
	 */
	public Saver(Core core) {
		this.core = core;
		// TODO Auto-generated constructor stub
	}
	public void run() {
								System.out.println("saving identities");
								File identities = new File("identities");
								if( identities.exists() )
								{
									String bakFilename = "identities.bak";
									File bakFile = new File(bakFilename);
									bakFile.delete();
									identities.renameTo(bakFile);
									identities = new File("identities");
								}

								try
								{ //TODO: replace this with a call to XML serializer
									FileWriter fout = new FileWriter(identities);
									fout.write(Core.mySelf.getName() + "\n");
									fout.write(Core.mySelf.getKeyAddress() + "\n");
									fout.write(Core.mySelf.getKey() + "\n");
									fout.write(Core.mySelf.getPrivKey() + "\n");

									//now do the friends
									fout.write("*****************\n");
									Iterator i = Core.friends.values().iterator();
									while( i.hasNext() )
									{
										Identity cur = (Identity)i.next();
										fout.write(cur.getName() + "\n");
										fout.write(cur.getKeyAddress() + "\n");
										fout.write(cur.getKey() + "\n");
									}
									fout.write("*****************\n");
						i = Core.getGoodIds().values().iterator();
						while (i.hasNext()) {
							fout.write((String)i.next() + "\n");
						}
						fout.write("*****************\n");
									i = Core.getEnemies().values().iterator();
									while( i.hasNext() )
									{
										Identity cur = (Identity)i.next();
										fout.write(cur.getName() + "\n");
										fout.write(cur.getKeyAddress() + "\n");
										fout.write(cur.getKey() + "\n");
									}
									fout.write("*****************\n");
						i = Core.getBadIds().values().iterator();
						while (i.hasNext()) {
							fout.write((String)i.next() + "\n");
						}
						fout.write("*****************\n");
									fout.close();
									System.out.println("identities saved successfully.");

								}
								catch( IOException e )
								{
									System.out.println("ERROR: couldn't save identities:");
									e.printStackTrace(System.out);
								}
						try {
							StringBuffer buf = new StringBuffer();
						Iterator i = Core.getMyBatches().keySet().iterator();
						while (i.hasNext()) 
							buf.append((String)i.next()).append("_");
						if (buf.length() > 0)
							buf.deleteCharAt(buf.length()-1); //remove the _ at the end
						File batches = new File("batches");
						FileAccess.writeFile(buf.toString(),batches);
		
						} catch (Throwable t) {
							t.printStackTrace(System.out);
						}
								core.saveOnExit();
								FileAccess.cleanKeypool(frame1.keypool);
							}
	};
/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.io.*;
import java.util.Iterator;
import org.w3c.dom.*;

import frost.*;


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
		
		Core.getOut().println("saving identities.xml");
		File identities = new File("identities.xml");
		if (identities.exists()) {
			String bakFilename = "identities.xml.bak";
			File bakFile = new File(bakFilename);
			bakFile.delete();
			identities.renameTo(bakFile);
			identities = new File("identities.xml");
		}
			try{
				Document d =XMLTools.createDomDocument();
				Element rootElement = d.createElement("FrostIdentities");
				//first save myself
				rootElement.appendChild(Core.getMyId().getXMLElement(d));
				//then friends
				Element friends = Core.getFriends().getXMLElement(d);
				friends.setAttribute("type","friends");
				rootElement.appendChild(friends);
				//then enemies 
				Element enemies = Core.getEnemies().getXMLElement(d);
				enemies.setAttribute("type","enemies");
				rootElement.appendChild(enemies);
				//then everybody else
				Element neutral = Core.getNeutral().getXMLElement(d);
				neutral.setAttribute("type","neutral");
				rootElement.appendChild(neutral);
				
				d.appendChild(rootElement);
				
				//save to file
				XMLTools.writeXmlFile(d,"identities.xml");
			}catch(Throwable e) {
				e.printStackTrace(Core.getOut());
		}
		
		/*
								Core.getOut().println("saving identities");
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
									Core.getOut().println("identities saved successfully.");

								}
								catch( IOException e )
								{
									Core.getOut().println("ERROR: couldn't save identities:");
									e.printStackTrace(Core.getOut());
								}*/
								//save the batches
						try {
							StringBuffer buf = new StringBuffer();
						Iterator i = Core.getMyBatches().keySet().iterator();
						while (i.hasNext()) {
							String current = (String)i.next();
							if (current.length()>0)
								buf.append(current).append("_");
							else
								i.remove(); //make sure no empty batches are saved
						}
							
						if (buf.length() > 0)
							buf.deleteCharAt(buf.length()-1); //remove the _ at the end
						File batches = new File("batches");
						FileAccess.writeFile(buf.toString(),batches);
		
						} catch (Throwable t) {
							t.printStackTrace(Core.getOut());
						}
						
						//save the known boards
						try {
							StringBuffer buf = new StringBuffer();
							Iterator i = Core.getKnownBoards().iterator();
							while (i.hasNext()) {
								String current = (String)i.next();
								buf.append(current).append(":");
							}
							if (buf.length() >0)
								buf.deleteCharAt(buf.length()-1);
							File boards = new File("boards");
							FileAccess.writeFile(buf.toString(),boards);
						}catch (Throwable t){
							t.printStackTrace(Core.getOut());
						}
						
						//save the hashes
						try{
							File hashes = new File("hashes");
							ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(hashes));
							oos.writeObject(Core.getMessageSet());
						} catch (Throwable t){
							t.printStackTrace(Core.getOut());
						}
								core.saveOnExit();
								FileAccess.cleanKeypool(frame1.keypool);
			}
	};
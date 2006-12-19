/**
 *   This file is part of JHyperochaUtilLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaUtilLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaUtilLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.util.exec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author saces
 * 
 */
public class Execute {
	
	private static final int BUFF_SIZE = 10240;
	
	public static ExecResult run_wait(String cmd) {
		return run_wait(cmd, "UTF-8");
	}
	
	/**
     * start an external program, and return their output
     * @param cmd the command to execute
     * @param cs the charset
     * @return the output generated by the program. Standard ouput and Error output are captured.
     */
    public static ExecResult run_wait(String cmd, String cs) {
    	
    	ExecResult result = new ExecResult();; 
    	char[] cbuf; 
        Process p;

        try {
			
			cbuf = new char[BUFF_SIZE];
			p = Runtime.getRuntime().exec(cmd); // java 1.4 String Order
        //ProcessBuilder pb = new ProcessBuilder(order);   // java 1.5 List<String> order 
        //Process p = pb.start();
        
        InputStream isStdOut = p.getInputStream();
        InputStream isStdErr = p.getErrorStream();
        
        result.stdOut = new StringBuffer();
        
        InputStreamReader iSReader = new InputStreamReader(isStdOut, cs);
        BufferedReader reader = new BufferedReader(iSReader);
        int count = 0;
        while( count != -1 ) {
         	count = reader.read(cbuf, 0, BUFF_SIZE);
           	if (count != -1)
          		result.stdOut.append(cbuf, 0, count);
        }
        reader.close();

        result.stdErr = new StringBuffer();
        
        iSReader = new InputStreamReader(isStdErr, cs);
        reader = new BufferedReader(iSReader);
        count = 0;
        while( count != -1 ) {
           	count = reader.read(cbuf, 0, BUFF_SIZE);
          	if (count != -1)
           		result.stdOut.append(cbuf, 0, count);
        }
        reader.close();
        
        p.waitFor();
        
        result.retcode = p.exitValue();
        
		} catch (Throwable t) {
			// DEBUG
			t.printStackTrace(); 
			result.error = t;
		}  
        
        return result;
    }

}

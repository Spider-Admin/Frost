/*
  AbstractFcpMultiRequestConnection.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp07;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;

import frost.fcp.*;
import frost.util.Logging;

public abstract class AbstractBasicConnection {

    protected static final Logger mylogger = Logger.getLogger(AbstractBasicConnection.class.getName());

    protected FcpSocket fcpSocket;
    protected final NodeAddress nodeAddress;
    private final ReentrantLock writeSocketLock;

    protected AbstractBasicConnection(final NodeAddress na) throws UnknownHostException, IOException {
        nodeAddress = na;
        fcpSocket = new FcpSocket(nodeAddress, true);
        writeSocketLock = new ReentrantLock(true);
    }
    
    public void aquireFcpWriteLock() {
        writeSocketLock.lock();
    }
    public void releaseFcpWriteLock() {
        writeSocketLock.unlock();
    }

    public void closeConnection() {
        fcpSocket.close();
    }

    /**
     * Writes a message to the socket. Ensures that only 1 thread writes at any time (writeSocketLock).
     * Appends EndMessage to the specified message lines.
     * @param message     the message to send
     */
    public boolean sendMessage(final List<String> message) {

        writeSocketLock.lock();
        final boolean doLogging = Logging.inst().doLogFcp2Messages();
        try {
            if(doLogging) {
                System.out.println("### SEND >>>>>>> (FcpMultiRequestConnection.sendMessage)");
            }
            for( final String msgLine : message ) {
                fcpSocket.getFcpOut().println(msgLine);
                if(doLogging) {
                    System.out.println(msgLine);
                }
            }
            fcpSocket.getFcpOut().println("EndMessage");
            if(doLogging) {
                System.out.println("*EndMessage*");
            }
            final boolean isError = fcpSocket.getFcpOut().checkError();
            if(doLogging) {
                System.out.println("### SEND <<<<<<< isError="+isError);
            }
            return isError;
        } finally {
            writeSocketLock.unlock();
        }
    }

    /**
     * Writes a message together with data from the specified source file to the socket.
     * Ensures that only 1 thread writes at any time (writeSocketLock).
     * @param message     the message to send
     * @param sourceFile  file containing the data to be send
     */
    public boolean sendMessageAndData(final List<String> message, final File sourceFile) {

        writeSocketLock.lock();
        final boolean doLogging = Logging.inst().doLogFcp2Messages();
        try {
            if(doLogging) {
                System.out.println("### SEND_DATA >>>>>>>");
            }
            for( final String msgLine : message ) {
                fcpSocket.getFcpOut().println(msgLine);
                if(doLogging) {
                    System.out.println(msgLine);
                }
            }

            fcpSocket.getFcpOut().println("DataLength=" + Long.toString(sourceFile.length()));
            fcpSocket.getFcpOut().println("Data");

            fcpSocket.getFcpOut().flush();

            // send file
            final BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));
            while( true ) {
                final int d = fileInput.read();
                if( d < 0 ) {
                    break; // EOF
                }
                fcpSocket.getFcpRawOut().write(d);
            }
            fileInput.close();
            fcpSocket.getFcpRawOut().flush();

            if(doLogging) {
                System.out.println("### SEND_DATA <<<<<<<");
            }
            return false; // no error
        } catch(final Throwable t) {
            mylogger.log(Level.SEVERE, "Error sending file to socket", t);
            return true; // error
        } finally {
            writeSocketLock.unlock();
        }
    }

    /**
     * Writes a message together with data from the specified byte array to the socket.
     * Ensures that only 1 thread writes at any time (writeSocketLock).
     * @param message     the message to send
     * @param data        byte[] containing the data to be send
     */
    public boolean sendMessageAndData(final List<String> message, final byte[] data) {

        writeSocketLock.lock();
        final boolean doLogging = Logging.inst().doLogFcp2Messages();
        try {
            if(doLogging) {
                System.out.println("### SEND_DATA >>>>>>>");
            }
            for( final String msgLine : message ) {
                fcpSocket.getFcpOut().println(msgLine);
                if(doLogging) {
                    System.out.println(msgLine);
                }
            }
            fcpSocket.getFcpOut().println("DataLength="+Integer.toString(data.length));
            fcpSocket.getFcpOut().println("Data");

            fcpSocket.getFcpOut().flush();

            // send data
            for (final byte b : data) {
                fcpSocket.getFcpRawOut().write(b);
            }
            fcpSocket.getFcpRawOut().flush();

            if(doLogging) {
                System.out.println("### SEND_DATA <<<<<<<");
            }
            return false; // no error
        } catch(final Throwable t) {
            mylogger.log(Level.SEVERE, "Error sending data to socket", t);
            return true; // error
        } finally {
            writeSocketLock.unlock();
        }
    }
}

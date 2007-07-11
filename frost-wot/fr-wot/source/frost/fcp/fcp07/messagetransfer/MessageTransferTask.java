/*
  MessageTransferTask.java / Frost
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
package frost.fcp.fcp07.messagetransfer;

import java.io.*;

import frost.fcp.*;

public class MessageTransferTask {
    
    public static final int MODE_DOWNLOAD = 1;
    public static final int MODE_UPLOAD = 2;
    
    private final int transferMode;
    private final String identifier;
    private final String key;
    private final File file;
    private final int maxSize;
    private final int priority;
    
    private boolean taskFinished = false;

    FcpResultPut putResult = null;
    FcpResultGet getResult = null;
    
    /**
     * Construct task for DOWNLOAD
     */
    public MessageTransferTask(String id, String key, File targetFile, int prio, int maxSize) {
        transferMode = MODE_DOWNLOAD;
        identifier = id;
        this.key = key;
        this.file = targetFile;
        this.maxSize = maxSize;
        this.priority = prio;
    }

    /**
     * Construct task for UPLOAD
     */
    public MessageTransferTask(String id, String key, File sourceFile, int prio) {
        transferMode = MODE_UPLOAD;
        identifier = id;
        this.key = key;
        this.file = sourceFile;
        this.maxSize = -1;
        this.priority = prio;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getKey() {
        return key;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Called by MessageThread to wait for a message.
     */
    public synchronized void waitForFinished() {
        if( taskFinished ) {
            return; // already finished
        }
        try {
            wait();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Called by NodeMessageHandler when task is finished.
     */
    public synchronized void setFinished() {
        taskFinished = true;
        notifyAll();
    }

    public boolean isModeUpload() {
        return transferMode == MODE_UPLOAD;
    }
    public boolean isModeDownload() {
        return transferMode == MODE_DOWNLOAD;
    }

    public FcpResultPut getFcpResultPut() {
        return putResult;
    }
    public void setFcpResultPut(FcpResultPut putResult) {
        this.putResult = putResult;
    }

    public FcpResultGet getFcpResultGet() {
        return getResult;
    }
    public void setFcpResultGet(FcpResultGet getResult) {
        this.getResult = getResult;
        if( !getResult.isSuccess() ) {
            // failure, delete temp file
            if( getFile().isFile() ) {
                getFile().delete();
            }
        }
    }
    
    /**
     * Quickly fail because socket was disconnected.
     */
    public void setFailed() {
        if( isModeDownload() ) {
            this.getResult = FcpResultGet.RESULT_FAILED;
        } else {
            this.putResult = FcpResultPut.NO_CONNECTION_RESULT;
        }
    }

    public int getPriority() {
        return priority;
    }

}
//
//    static MessageTransferTask task = new MessageTransferTask(1);
//    
//    public static void main(String[] args) {
//        new t1(task).start();
//        new t2(task).start();
//    }
//}
//class t1 extends Thread {
//    MessageTransferTask task;
//    public t1(MessageTransferTask task) { this.task = task; }
//    public void run() {
//        System.out.println("sleeping 3s");
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("starting");
//        task.awake();
//        System.out.println("wachgemacht");
//    }
//}
//class t2 extends Thread {
//    MessageTransferTask task;
//    public t2(MessageTransferTask task) { this.task = task; }
//    public void run() {
//        System.out.println("waiting");
//        task.sleep();
//        System.out.println("awake!");
//    }
//}

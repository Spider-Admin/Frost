/**
*************************************************************************
Copyright (c) 2003 Ingo Franzki
All rights reserved.

* BSD style license *

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*************************************************************************
*/


import java.io.*;
import java.util.*;

public class JSysTrayIcon
{
  private native int nativeShowWindow(String title,int cmd);
  private native int nativeCreateSystrayIcon(int iconIndex,String tooltip,String title);
  private native int nativeModifySystrayIcon(int handle,int iconIndex,String tooltip,String title);
  private native int nativeDeleteSystrayIcon(int handle);

  private static int ERR_NO_ERROR            = 0;
  private static int ERR_INVALID_PARAM       = 1;
  private static int ERR_WINDOW_NOT_FOUND    = 2;
  private static int ERR_ICON_NOT_FOUND      = 3;


  public static int SHOW_CMD_HIDE            = 1;
  public static int SHOW_CMD_SHOW            = 2;

  private static boolean libLoaded = false;
  private static Vector  iconList = new Vector();

  private int     handle = 0;
  private String  windowTitle = null;

  /**
   * Creates a new Systray icon
   *
   * @param iconIndex - the index of the icon to display (starting at 0)
   * @param tooltipText - the tooltip text
   * @param windowTitle - the window title
   */
  public JSysTrayIcon(int iconIndex,String tooltipText,String windowTitle) throws IOException
  {
    if(!libLoaded)
    {
      // load the native library
      try
      {
        System.loadLibrary("JSysTray"); // Load JSysTray.dll
      }
      catch(UnsatisfiedLinkError e)
      {
        throw(new IOException("Could not load JSysTray.dll: "+e.toString()));
      }

      // regsiter a shutdown hook to do cleanup
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());

      libLoaded = true;
    }

    this.windowTitle = windowTitle;

    this.handle = nativeCreateSystrayIcon(iconIndex,tooltipText,windowTitle);

    if(handle==0)
      throw(new IOException("Systray icon has not been created."));

    iconList.addElement(this);
  }

  /**
   * Sets the icon of the icon
   *
   * @param iconIndex - the new index of the icon to display (starting at 0)
   */
  public void setIcon(int iconIndex) throws IOException
  {
    int rc;

    if(handle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeModifySystrayIcon(this.handle,iconIndex,null,null);

    if(rc==ERR_ICON_NOT_FOUND)
      throw(new IOException("The specified icon has not been found"));
    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Sets the tooltip text of the icon
   *
   * @param tooltipText - the new tooltip text
   */
  public void setTooltip(String tooltipText) throws IOException
  {
    int rc;

    if(handle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeModifySystrayIcon(this.handle,-1,tooltipText,null);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Sets the window title
   *
   * @param windowTitle - the new window title
   */
  public void setWindowTitle(String windowTitle) throws IOException
  {
    int rc;

    if(handle==0)
      throw(new IOException("Systray icon has already been deleted"));

    this.windowTitle = windowTitle;
    rc = nativeModifySystrayIcon(this.handle,-1,null,this.windowTitle);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Deletes the icon from the systray
   */
  public void delete() throws IOException
  {
    int rc;

    if(handle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeDeleteSystrayIcon(this.handle);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been deleted. rc="+rc));

    handle = 0;
    windowTitle = null;

    iconList.removeElement(this);
  }

  /**
   * Sends a show command to the window identified by the window title of the icon
   *
   * @param showCMD - one of the SHOW_CMD_xx values
   */
  public void showWindow(int showCmd) throws IOException
  {
    int rc;

    if(handle==0)
      throw(new IOException("Systray icon has already been deleted"));
    
    rc = nativeShowWindow(windowTitle,showCmd);

    if(rc==ERR_WINDOW_NOT_FOUND)
      throw(new IOException("The specified window has not been found"));
    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalod parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("The Window state has not been changed. rc="+rc));
  }

  class ShutdownHook extends Thread
  {
    public void run()
    {
      Enumeration e = iconList.elements();

      while(e.hasMoreElements())
      {
        JSysTrayIcon icon = (JSysTrayIcon)e.nextElement();

        try {
          icon.delete();
        } catch(Throwable t) {}
      }
    }
  }
}

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

package frost.ext;

import java.io.IOException;
import java.util.*;

/**
 * This class wraps the functionality of the external 
 * system depended libraries that maintain the SysTray
 * support for several environments.
 * The original class was enhanced + better documented. (bback)
 * 
 * Currently there is support for:
 *  - win32 (JSysTray.dll)
 */

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

  public static int SHOW_CMD_HIDE               = 1;
  public static int SHOW_CMD_SHOW               = 2;
  public static int SHOW_CMD_HIDE_WAS_MAXIMIZED = 3;

  private static boolean libLoaded = false;
  private static Vector<JSysTrayIcon> iconList = new Vector<JSysTrayIcon>(); // list of all created systray icons

  private int     systrayHandle = 0;
  private String  windowTitle = null;

  /**
   * Creates a new Systray icon.
   * Must contain code for any supported platform, see comments.
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
      // ADD YOUR PLATFORM CODE HERE
      if( System.getProperty("os.name").startsWith("Windows") )
      {
          try {
              System.loadLibrary("exec/JSysTray"); // Load JSysTray.dll
              libLoaded = true;
          }
          catch(UnsatisfiedLinkError e)
          {
            throw(new IOException("Could not load JSysTray.dll: "+e.toString()));
          }
      }
//      else if(System.getProperty("os.name").startsWith("Linux"))
//      {
//      }
      else
      {
          throw(new IOException("SysTrayIcon is not supported on this system."));
      }
      // register a shutdown hook to do cleanup
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    this.windowTitle = windowTitle;

    this.systrayHandle = nativeCreateSystrayIcon(iconIndex,tooltipText,windowTitle);

    if(systrayHandle==0)
      throw(new IOException("Systray icon has not been created."));

    iconList.addElement(this);
  }

  /**
   * Sets the index of the icon to show.
   * index starts at 0, this represents the 1st icon stored in the library.
   * The source pictures are enumerated, this order is expected here.
   *
   * @param iconIndex - the new index of the icon to display (starting at 0)
   */
  public void setIcon(int iconIndex) throws IOException
  {
    int rc;

    if(systrayHandle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeModifySystrayIcon(this.systrayHandle,iconIndex,null,null);

    if(rc==ERR_ICON_NOT_FOUND)
      throw(new IOException("The specified icon has not been found"));
    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Sets the tooltip text of the icon.
   *
   * @param tooltipText - the new tooltip text
   */
  public void setTooltip(String tooltipText) throws IOException
  {
    int rc;

    if(systrayHandle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeModifySystrayIcon(this.systrayHandle,-1,tooltipText,null);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Sets the window title that is used to find the window.
   *
   * @param windowTitle - the new window title
   */
  public void setWindowTitle(String windowTitle) throws IOException
  {
    int rc;

    if(systrayHandle==0)
      throw(new IOException("Systray icon has already been deleted"));

    this.windowTitle = windowTitle;
    rc = nativeModifySystrayIcon(this.systrayHandle,-1,null,this.windowTitle);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been modified. rc="+rc));
  }

  /**
   * Deletes the icon from the systray.
   */
  public void delete() throws IOException
  {
    int rc;

    if(systrayHandle==0)
      throw(new IOException("Systray icon has already been deleted"));

    rc = nativeDeleteSystrayIcon(this.systrayHandle);

    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalid parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("Systray icon has not been deleted. rc="+rc));

    systrayHandle = 0;
    windowTitle = null;

    iconList.removeElement(this);
  }

  /**
   * Sends a show command to the window identified by the window title of the icon
   * - SHOW_CMD_HIDE
   * - SHOW_CMD_SHOW
   *
   * @param showCMD - one of the SHOW_CMD_xx values
   */
  public void showWindow(int showCmd) throws IOException
  {
    int rc;

    if(systrayHandle==0)
      throw(new IOException("Systray icon has already been deleted"));
    
    rc = nativeShowWindow(windowTitle,showCmd);

    if(rc==ERR_WINDOW_NOT_FOUND)
      throw(new IOException("The specified window has not been found"));
    if(rc==ERR_INVALID_PARAM)
      throw(new IOException("An invalod parameter has been specified"));
    if(rc!=ERR_NO_ERROR)
      throw(new IOException("The Window state has not been changed. rc="+rc));
  }

  /**
   * A shutdown hook to remove the systray icon on exit.
   */
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
  
  /***********************************
   * Additional wrapper methods, maintain 1 systray icon.
   * If you need additional icons, instanciate this class
   * directly. 
   ***********************************/
  private static JSysTrayIcon sysTrayIcon = null;
  
  /**
   * Creates a single instance of a systray icon.
   * The instance can be aquired by caalling getInstance().
   * For multiple systray icons instanciate the class
   * directly.
   *  
   * @param iconIndex - the index of the icon to display (starting at 0)
   * @param tooltipText - the tooltip text
   * @param windowTitle - the window title
   * @return true if instance was successfully created, false otherwise
   */
  public static void createInstance(int iconIx,String tooltipTxt,String winTitle) throws Throwable {
      JSysTrayIcon.sysTrayIcon = new JSysTrayIcon(iconIx, tooltipTxt, winTitle);    
  }

  /**
   * Returns the created instance of the systray icon.
   * 
   * @return the instance or null if not created
   */
  public static JSysTrayIcon getInstance()
  {
      return JSysTrayIcon.sysTrayIcon;
  }
}

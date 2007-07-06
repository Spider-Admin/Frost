/**
*************************************************************************
Copyright (c) 2003, 2007 Ingo Franzki
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

#include <windows.h>
#include <stdio.h>

#include "frost_ext_JSysTrayIcon.h"

#include "resource.h"


#define ERR_NO_ERROR             0
#define ERR_INVALID_PARAM        1
#define ERR_WINDOW_NOT_FOUND     2
#define ERR_ICON_NOT_FOUND       3

#define WM_NOTIFY_ICON_MSG  WM_USER

HINSTANCE hInstance      = NULL;
HWND   hSystrayWnd       = NULL;
HANDLE hThread           = NULL; 
HANDLE hEvent            = NULL;

BOOL maximizeOnNextRestore = FALSE;

typedef struct tag_JSYSTRAYICON
        {
          HICON  hIcon;
          CHAR   szWindowTitle[500];
          HWND   hWindow;
        }
        JSYSTRAYICON;


BOOL WINAPI DllMain(HINSTANCE hinstDLL,DWORD fdwReason,LPVOID lpvReserved)
{
  hInstance = hinstDLL;
  return(TRUE);
};

#define SHOW_CMD_HIDE                 1   // hide the window
#define SHOW_CMD_SHOW                 2   // show/restore the window
#define SHOW_CMD_HIDE_WAS_MAXIMIZED   3   // hide the window, maximize on next restore

int JShowWindow(LPSTR szTitle,int iCmd)
{
  HWND hWnd;
  WINDOWPLACEMENT WndPlace;

  if(szTitle==NULL)
    return(ERR_INVALID_PARAM);

  hWnd = FindWindow(NULL,szTitle);
  if(hWnd==NULL)
    return(ERR_WINDOW_NOT_FOUND);

  memset(&WndPlace,0,sizeof(WINDOWPLACEMENT));
  WndPlace.length = sizeof(WINDOWPLACEMENT);
  GetWindowPlacement(hWnd,&WndPlace);

  switch(iCmd)
  {
    case SHOW_CMD_HIDE:
      // Hide the window
      ShowWindow(hWnd,SW_HIDE);
      break;

    case SHOW_CMD_SHOW:
      // Show/Restore the window
      if(maximizeOnNextRestore == TRUE) {
        ShowWindow(hWnd,SW_SHOWMAXIMIZED);
        maximizeOnNextRestore = FALSE;
      }
      else if(WndPlace.showCmd==SW_SHOWMAXIMIZED)
        ShowWindow(hWnd,SW_SHOWMAXIMIZED);
      else if(WndPlace.showCmd==SW_SHOWMINIMIZED)
        ShowWindow(hWnd,SW_RESTORE);
      else
        ShowWindow(hWnd,SW_SHOWNORMAL);
      SetForegroundWindow(hWnd);
      break;

    case SHOW_CMD_HIDE_WAS_MAXIMIZED:
      // Hide the window, maximize on next restore
      maximizeOnNextRestore = TRUE;
      ShowWindow(hWnd,SW_HIDE);
      break;

    default:
      return(ERR_INVALID_PARAM);
  }

  return(ERR_NO_ERROR);
}

int JSystrayClicked(HWND hWnd,WPARAM wParam)
{
  JSYSTRAYICON*   lpSystray;
  CHAR            szText[600];
  WINDOWPLACEMENT WndPlace;

  lpSystray = (JSYSTRAYICON*)wParam;
  if(lpSystray==NULL)
    return(ERR_INVALID_PARAM);

  if(lpSystray->hWindow==NULL)
  {
    // try to find the window now
    lpSystray->hWindow = FindWindow(NULL,lpSystray->szWindowTitle);
  };
  if(lpSystray->hWindow==NULL)
  {
    sprintf(szText,"Could not find window with title '%s'",lpSystray->szWindowTitle);
    MessageBox(NULL,szText,"JSysTray",MB_OK | MB_ICONERROR);
    return(ERR_WINDOW_NOT_FOUND);
  }

  // get the status
  memset(&WndPlace,0,sizeof(WINDOWPLACEMENT));
  WndPlace.length = sizeof(WINDOWPLACEMENT);
  GetWindowPlacement(lpSystray->hWindow,&WndPlace);

  if(WndPlace.showCmd==SW_HIDE || !IsWindowVisible(lpSystray->hWindow))
  {
    if(WndPlace.showCmd==SW_SHOWMAXIMIZED)
      ShowWindow(lpSystray->hWindow,SW_SHOWMAXIMIZED);
    else {
      if(maximizeOnNextRestore == TRUE) {
        ShowWindow(lpSystray->hWindow,SW_SHOWMAXIMIZED);
        maximizeOnNextRestore = FALSE;
      } else {
        ShowWindow(lpSystray->hWindow,SW_SHOWNORMAL);
      }
    }

    SetForegroundWindow(lpSystray->hWindow);
  }
  else
  {
    if(WndPlace.showCmd==SW_SHOWMINIMIZED)
    {
      ShowWindow(lpSystray->hWindow,SW_RESTORE);
      SetForegroundWindow(lpSystray->hWindow);
    }
    else
      ShowWindow(lpSystray->hWindow,SW_HIDE);
  }

  return(ERR_NO_ERROR);
}



LRESULT CALLBACK JSystrayWndProc(HWND hWnd,UINT uMsg,WPARAM wParam,LPARAM lParam)
{
  switch(uMsg)
  {
    case WM_NOTIFY_ICON_MSG:
      switch(lParam)
      {
        case WM_LBUTTONDOWN:
          JSystrayClicked(hWnd,wParam);
          break;
      }
      break;

    default:
      return(DefWindowProc(hWnd,uMsg,wParam,lParam));
  }
  return(TRUE);
}

DWORD WINAPI MessageThread(LPVOID param)
{
  MSG msg;
  WNDCLASS WndClass;

  // create a Systray window
  if(hSystrayWnd==NULL)
  {
    WndClass.style         = 0;
    WndClass.lpfnWndProc   = (WNDPROC)JSystrayWndProc;
    WndClass.cbClsExtra    = 0;
    WndClass.cbWndExtra    = 0;
    WndClass.hInstance     = hInstance;
    WndClass.hIcon         = NULL;
    WndClass.hCursor       = NULL;
    WndClass.hbrBackground = NULL;
    WndClass.lpszMenuName  = NULL;
    WndClass.lpszClassName = "JSYSTRAY";

    RegisterClass(&WndClass);

    hSystrayWnd = CreateWindow("JSYSTRAY","JSYSTRAY",WS_OVERLAPPED,CW_USEDEFAULT,CW_USEDEFAULT,10,10,
                                HWND_DESKTOP,NULL,hInstance,NULL);
    if(hSystrayWnd==NULL)
    {
      SetEvent(hEvent);
      return(-1);
    };

    // invisible
    ShowWindow(hSystrayWnd,SW_HIDE);
  };
  // Signal thread Init ready
  SetEvent(hEvent);


  while(GetMessage(&msg,NULL,0,0))
  {
    TranslateMessage(&msg);
    DispatchMessage(&msg);
  }

  return(0);
}


// Cretaes a Systray icon
void* JCreateSystrayIcon(int iconIndex,LPSTR szTooltip,LPSTR szTitle)
{
  NOTIFYICONDATA nData;
  JSYSTRAYICON*  lpSystray;
  DWORD id;

  if(szTooltip==NULL ||szTitle==NULL)
    return(NULL);

  // allocate the JSYSTRAYICON block
  lpSystray = malloc(sizeof(JSYSTRAYICON));
  if(lpSystray==NULL)
    return(NULL);

  memset(lpSystray,0,sizeof(JSYSTRAYICON));

  strncpy(lpSystray->szWindowTitle,szTitle,500);
  lpSystray->szWindowTitle[499] = '\0';

  // load the icon
  lpSystray->hIcon = LoadIcon(hInstance,MAKEINTRESOURCE(iconIndex+IDI_ICON1));
  if(lpSystray->hIcon==NULL)
  {
    // failed
    free(lpSystray);
    return(NULL);
  };

  if(hThread==NULL)
  {
    hEvent = CreateEvent(NULL,FALSE,FALSE,NULL);
    hThread = CreateThread(NULL,0,MessageThread,NULL,0,&id);
    WaitForSingleObject(hEvent,INFINITE);
  }
  if(hSystrayWnd==NULL)
  {
    // failed
    free(lpSystray);
    return(NULL);
  };

  // Create the Notifyicon
  nData.cbSize   = sizeof(NOTIFYICONDATA);
  nData.hWnd     = hSystrayWnd;
  nData.uID      = (UINT)lpSystray;
  nData.uFlags   = NIF_ICON | NIF_TIP | NIF_MESSAGE;
  nData.uCallbackMessage = WM_NOTIFY_ICON_MSG;
  nData.hIcon    = lpSystray->hIcon;
  strncpy(nData.szTip,szTooltip,64);
  nData.szTip[63] = '\0';

  Shell_NotifyIcon(NIM_ADD,&nData);

  // try to find the window now
  lpSystray->hWindow = FindWindow(NULL,lpSystray->szWindowTitle);

  return(lpSystray);
}

// Modifies a Systray icon
int JModifySystrayIcon(void* hSystray,int iconIndex,LPSTR szTooltip,LPSTR szTitle)
{
  JSYSTRAYICON*  lpSystray;
  NOTIFYICONDATA nData;

  if(hSystray==NULL)
    return(ERR_INVALID_PARAM);
  lpSystray = (JSYSTRAYICON*)hSystray;

  // Delete the Notifyicon
  nData.cbSize   = sizeof(NOTIFYICONDATA);
  nData.hWnd     = hSystrayWnd;
  nData.uID      = (UINT)lpSystray;
  nData.uFlags   = NIF_MESSAGE;
  nData.uCallbackMessage = WM_NOTIFY_ICON_MSG;
  if(iconIndex>=0)
  {
    // load the icon
    lpSystray->hIcon = LoadIcon(hInstance,MAKEINTRESOURCE(iconIndex+IDI_ICON1));
    if(lpSystray->hIcon==NULL)
      return(ERR_ICON_NOT_FOUND);
    nData.uFlags   |= NIF_ICON;
    nData.hIcon    = lpSystray->hIcon;
  }
  if(szTooltip!=NULL)
  {
    nData.uFlags   |= NIF_TIP;
    strncpy(nData.szTip,szTooltip,64);
    nData.szTip[63] = '\0';
  }
  if(szTitle!=NULL)
  {
    strncpy(lpSystray->szWindowTitle,szTitle,500);
    lpSystray->szWindowTitle[499] = '\0';

    // try to find the window now
    lpSystray->hWindow = FindWindow(NULL,lpSystray->szWindowTitle);
  }

  Shell_NotifyIcon(NIM_MODIFY,&nData);

  return(ERR_NO_ERROR);
}


// Deletes a Systray icon
int JDeleteSystrayIcon(void* hSystray)
{
  JSYSTRAYICON*  lpSystray;
  NOTIFYICONDATA nData;

  if(hSystray==NULL)
    return(ERR_INVALID_PARAM);
  lpSystray = (JSYSTRAYICON*)hSystray;

  // Delete the Notifyicon
  nData.cbSize   = sizeof(NOTIFYICONDATA);
  nData.hWnd     = hSystrayWnd;
  nData.uID      = (UINT)lpSystray;
  nData.uFlags   = NIF_ICON | NIF_MESSAGE;
  nData.uCallbackMessage = WM_NOTIFY_ICON_MSG;
  nData.hIcon    = lpSystray->hIcon;

  Shell_NotifyIcon(NIM_DELETE,&nData);

  free(lpSystray);
  return(ERR_NO_ERROR);
}

/*
 * Class:     JSysTrayIcon
 * Method:    nativeShowWindow
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_frost_ext_JSysTrayIcon_nativeShowWindow(JNIEnv* env,jobject obj,jstring title,jint cmd)
{
  int rc;

  rc = JShowWindow((LPSTR)(*env)->GetStringUTFChars(env,title,NULL),(int)cmd);

  return((jint)rc);
}

/*
 * Class:     JSysTrayIcon
 * Method:    nativeCreateSystrayIcon
 * Signature: (ILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_frost_ext_JSysTrayIcon_nativeCreateSystrayIcon(JNIEnv* env,jobject obj,jint iconIndex,jstring tooltip,jstring title)
{
  void* rc;
  LPSTR  szTitle = NULL;
  LPSTR  szTooltip = NULL;

  if(tooltip!=NULL)
    szTooltip = (LPSTR)(*env)->GetStringUTFChars(env,tooltip,NULL);
  if(title!=NULL)
    szTitle = (LPSTR)(*env)->GetStringUTFChars(env,title,NULL);

  rc = JCreateSystrayIcon((int)iconIndex,szTooltip,szTitle);

  return((jint)rc);
}

/*
 * Class:     JSysTrayIcon
 * Method:    nativeModifySystrayIcon
 * Signature: (IILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_frost_ext_JSysTrayIcon_nativeModifySystrayIcon(JNIEnv* env,jobject obj,jint handle,jint iconIndex,jstring tooltip,jstring title)
{
  int    rc;
  LPSTR  szTitle = NULL;
  LPSTR  szTooltip = NULL;

  if(tooltip!=NULL)
    szTooltip = (LPSTR)(*env)->GetStringUTFChars(env,tooltip,NULL);
  if(title!=NULL)
    szTitle = (LPSTR)(*env)->GetStringUTFChars(env,title,NULL);

  rc = JModifySystrayIcon((void*)handle,(int)iconIndex,szTooltip,szTitle);

  return((jint)rc);
}

/*
 * Class:     JSysTrayIcon
 * Method:    nativeDeleteSystrayIcon
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_frost_ext_JSysTrayIcon_nativeDeleteSystrayIcon(JNIEnv* env,jobject obj,jint handle)
{
  int rc;

  rc = JDeleteSystrayIcon((void*)handle);

  return((jint)rc);
}






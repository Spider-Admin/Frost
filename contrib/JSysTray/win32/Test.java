import javax.swing.*;
import javax.swing.JTree;

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

public class Test
{
  public static void main(String[] args)
  {
    try
    {
      JSysTrayIcon si = new JSysTrayIcon(0,"Tooltip","Notepad");

      si.setTooltip("Blah Blah");

      si.setWindowTitle("Untitled - Notepad");

      si.setIcon(1);

      //JOptionPane.showMessageDialog(null,"Wait...");

//      si.showWindow(JSysTrayIcon.SHOW_CMD_HIDE);

      si.showWindow(JSysTrayIcon.SHOW_CMD_SHOW);

      si.delete();
    }
    catch(Throwable t)
    {
      t.printStackTrace();
    }
  }

}

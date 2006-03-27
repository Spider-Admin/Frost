/*
AltEdit.java / Frost
Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.ext;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.*;
import frost.gui.*;
import frost.util.gui.translation.*;

/**
 * Class provides alternate editor functionality.
 *
 * @author bback
 */
public class AltEdit extends Thread {

    private Language language = Language.getInstance();

    private Frame parentFrame;
    private String linesep = System.getProperty("line.separator");

    private String oldSubject;
    private String oldText;

    private final String SUBJECT_MARKER = language.getString("*--- Subject line (changeable) ---*");
    private final String TEXT_MARKER = language.getString("*--- Enter your text after this line ---*");

    private Object transferObject;
    private MessageFrame callbackTarget;

    public AltEdit(String subject, String text, Frame parentFrame, Object transferObject, MessageFrame callbackTarget) {
        this.parentFrame = parentFrame;
        this.oldSubject = subject;
        this.oldText = text;
        this.transferObject = transferObject;
        this.callbackTarget = callbackTarget;
    }
    
    private void callbackMessageFrame(final String newSubject, final String newText) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                callbackTarget.altEditCallback(transferObject, newSubject, newText);
            }
        });
    }

    public void run() {

        // paranoia
        if( Core.frostSettings.getBoolValue("useAltEdit") == false ) {
            callbackMessageFrame(null, null);
            return;
        }

        String editor = Core.frostSettings.getValue("altEdit");

        if( editor == null || editor.length() == 0 ) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("No alternate editor configured."),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            callbackMessageFrame(null, null);
            return;
        }

        if( editor.indexOf("%f") == -1 ) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("Configured alternate editor line must contain a '%f' as placeholder for the filename."),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            callbackMessageFrame(null, null);
            return;
        }

        // part before and after %f
        String editor_pre_file = editor.substring(0, editor.indexOf("%f"));
        String editor_post_file = editor.substring(editor.indexOf("%f") + 2, editor.length());

        File editFile = null;
        try {
            editFile =  File.createTempFile("frostmsg", ".txt", new File(Core.frostSettings.getValue("temp.dir")));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("Could not create message file for alternate editor: ")+editFile.getPath()+"\n"+e.toString(),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            callbackMessageFrame(null, null);
            return;
        }
        editFile.deleteOnExit();

        StringBuffer sb = new StringBuffer();
        sb.append(language.getString(">>> This is a Frost alternate editor message file.                <<<")).append(linesep);
        sb.append(language.getString(">>> You can edit the subject and add text at the end of the file. <<<")).append(linesep);
        sb.append(language.getString(">>> Don't change or delete the marker lines!                      <<<")).append(linesep).append(linesep);
        sb.append(SUBJECT_MARKER).append(linesep);
        sb.append(oldSubject).append(linesep).append(linesep);
        sb.append(oldText).append(linesep); // contains new from-header-line
        sb.append(TEXT_MARKER).append(linesep);

        if( FileAccess.writeFile(sb.toString(), editFile, "UTF-8") == false ) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("Could not create message file for alternate editor: ")+editFile.getPath(),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            callbackMessageFrame(null, null);
            return;
        }
        sb = null;

        String editorCmdLine = editor_pre_file + editFile.getPath() + editor_post_file;
        try {
            Execute.run(editorCmdLine, true);
        } catch(Throwable t) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("Could not start alternate editor using command: ")+editorCmdLine+"\n"+t.toString(),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            editFile.delete();
            callbackMessageFrame(null, null);
            return;
        }

        List lines = FileAccess.readLines(editFile, "UTF-8");
        if( lines.size() < 4 ) { // subject marker,subject,from line, text marker
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("The message file returned by the alternate editor is invalid."),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            editFile.delete();
            callbackMessageFrame(null, null);
            return;
        }

        String newSubject = null;
        StringBuffer newTextSb = new StringBuffer();

        boolean inNewText = false;
        for( Iterator it=lines.iterator(); it.hasNext(); ) {
            String line = (String)it.next();

            if( inNewText ) {
                newTextSb.append(line).append(linesep);
                continue;
            }

            if( line.equals(SUBJECT_MARKER) ) {
                // next line is the new subject
                if( it.hasNext() == false ) {
                    JOptionPane.showMessageDialog(parentFrame,
                            language.getString("The message file returned by the alternate editor is invalid."),
                            language.getString("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    editFile.delete();
                    callbackMessageFrame(null, null);
                    return;
                }
                line = (String)it.next();
                if( line.equals(TEXT_MARKER) ) {
                    JOptionPane.showMessageDialog(parentFrame,
                            language.getString("The message file returned by the alternate editor is invalid."),
                            language.getString("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    editFile.delete();
                    callbackMessageFrame(null, null);
                    return;
                }
                newSubject = line.trim();
                continue;
            }

            if( line.equals(TEXT_MARKER) ) {
                // text begins
                inNewText = true;
            }
        }

        if( newSubject == null ) {
            JOptionPane.showMessageDialog(parentFrame,
                    language.getString("The message file returned by the alternate editor is invalid."),
                    language.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            editFile.delete();
            callbackMessageFrame(null, null);
            return;
        }

        // finished, we have a newSubject and a newText now
        callbackMessageFrame(newSubject, newTextSb.toString());
    }
}

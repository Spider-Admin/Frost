package frost;

import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;

import frost.gui.*;
import frost.threads.*;

public class AltEdit extends Thread {
    private Frame parentFrame;
    private String linesep;
    private String board;
    private String boardNew;
    private String from;
    private String fromNew;
    private String subject;
    private String subjectNew;
    private String text;
    private String textNew;
    private String keypool;
    private String editor;
    private String editor_pre_file;
    private String editor_post_file;
    private Process editorProcess;
    private String lastUsedDirectory;
    private SettingsClass frostSettings;


    private String getFileContent(File editFile){
        String fileContent = "";
    try{

        BufferedReader fileContentReader = new BufferedReader(new FileReader(editFile));
        char[] buf = new char[256];
        while (fileContentReader.read(buf, 0, 256) >= 0){
        fileContent += new String(buf);
        }
        fileContentReader.close();
    }
    catch(Exception e){
        e.printStackTrace();
    }
    return fileContent;
    }



    public void run(){
    System.out.println("run is started");
    String fileContentOld = "";
    String fileContentNew = "";
    String editContent = "";
    editContent += "board=" + board + linesep;
    editContent += "from=" + from + linesep;
    editContent += "subject=" + subject + linesep;
    editContent += "--- message ---" + linesep;
    editContent += text;

    System.out.println(editContent);
    try{
        File editFile =  File.createTempFile("nmsg", ".alt", new File(keypool));
        try{
        editFile.deleteOnExit();
        FileWriter editWriter = new FileWriter(editFile);
        editWriter.write(editContent, 0, editContent.length());
        editWriter.flush();
        editWriter.close();
        fileContentOld = getFileContent(editFile);
        System.out.println(fileContentOld);
        }
        catch(Exception e){
        e.printStackTrace();
        }

        try{
        String myEdit = editor_post_file.equals("") && editor_pre_file.equals("")
            ? editor + " " + editFile
            : editor_pre_file + " " + editFile + " " + editor_post_file;
        System.out.println("trying to start alternative editor: " + myEdit);
        editorProcess = Runtime.getRuntime().exec(myEdit);
        editorProcess.waitFor();
        }
        catch(Exception e){
        System.out.println("Can not start alternative editor, using builtin!");
        String[] args = {board,
                 from,
                 subject,
                 text,
                 lastUsedDirectory
        };
        MessageFrame newMessage = new MessageFrame(args, keypool, frostSettings, parentFrame);
/*        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = newMessage.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        newMessage.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
*/
        newMessage.show();
        }
        try{
        fileContentNew = getFileContent(editFile);
        System.out.println(fileContentNew);
        BufferedReader editReader = new BufferedReader(new FileReader(editFile));
        String line = " ";
        boolean inMessage = false;
        textNew = "";
        boardNew = board;
        fromNew = from;
        subjectNew = subject;
        while (line != null){
            line = editReader.readLine();
            if(!inMessage){
            line.trim();
            if(line.equals("--- message ---")){
                inMessage = true;
                continue;
            }
            StringTokenizer strtok = new StringTokenizer(line, "=");
            if(strtok.countTokens() == 2){
                String keyToken = strtok.nextToken();
                if(keyToken.equals("board")){
                boardNew = strtok.nextToken();
                }
                else if(keyToken.equals("from")){
                fromNew = strtok.nextToken();
                }
                else if(keyToken.equals("subject")){
                subjectNew = strtok.nextToken();
                }
            }
            else{
                System.out.println("Wrong number of arguments for values in message-header, leaving values untouched");
            }
            }
            else if (line != null){
            textNew += line + linesep;
            }
        }
        editReader.close();
        }
        catch(Exception e){
        e.printStackTrace();
        }
    }
    catch(Exception e){
        e.printStackTrace();
    }

    if(!fileContentNew.equals(fileContentOld)){
        String[] args = {boardNew,
                 fromNew,
                 subjectNew,
                 textNew,
                 frostSettings.getValue("tofUploadHtl"),
                 keypool,
                 frostSettings.getValue("tofDownloadHtl")
        };

        frostSettings.setValue("userName", from);
        MessageUploadThread mut = new MessageUploadThread(args, parentFrame);
        mut.start();
    }
    else{
        System.out.println("Message not edited, skipping upload!");
    }
    }


    public AltEdit (String board, String subject, String text, String keypool, SettingsClass frostSettings, Frame parentFrame){
    this.parentFrame = parentFrame;
    this.frostSettings = frostSettings;
    this.board = board;
    this.from = frostSettings.getValue("userName");
    this.subject = subject;
    this.text = text;
    this.lastUsedDirectory = frostSettings.getValue("lastUsedDirectory");


    this.editor_pre_file = "";
    this.editor_post_file = "";


    this.editor = frostSettings.getValue("altEdit");
    if(editor.indexOf("%f") != -1){
        this.editor_pre_file = editor.substring(0, editor.indexOf("%f"));
        this.editor_post_file = editor.substring(editor.indexOf("%f") + 2, editor.length());
    }

    System.out.println("pre_file = " + editor_pre_file);
    System.out.println("post_file = " + editor_post_file);

    this.keypool = keypool;
    linesep = System.getProperty("line.separator");
    }
}

/*
  CleanUp.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author FillaMent
 */
public class CleanUp {

   private String folder;
   
   private static Logger logger = Logger.getLogger(CleanUp.class.getName());

   /**
    * @param Folder Folder for cleaning
    * @param DaysOld Days of files to keep
    */
   public CleanUp(String Folder) {
    this(Folder, false);
   }

   /**
    * @param Folder Folder for cleaning
    * @param DaysOld Days of files to keep
    * @param goNow Clean immediately
    */
   public CleanUp(String Folder, boolean goNow) {
    folder = Folder;
    if (goNow) {
        doCleanup();
    }
   }

   public void doCleanup() {
    recursDir(folder);
   }

   /**
    * @param dirItem Folder for recursion
    */
   private void recursDir(String dirItem)
   {
      String list[];
      File file = new File(dirItem);
      if (file.isDirectory() && file.listFiles().length > 0) {
          logger.fine(file.toString());
          Vector vd = new Vector();
          Vector vf = new Vector();
          list = file.list();
          Arrays.sort(list,String.CASE_INSENSITIVE_ORDER);

          for (int i = 0; i < list.length; i++)
          {
             File f = new File(dirItem + File.separatorChar + list[i]);
             if (f.isDirectory()) vd.add(list[i]); else vf.add(list[i]);
          }
          for (int a=0; a < vf.size(); a++)
            recursDir (dirItem + File.separatorChar + vf.get(a));
          for (int d=0; d < vd.size(); d++)
            recursDir (dirItem + File.separatorChar + vd.get(d));
      } else
      processItem(dirItem);
      list=null;
    }

   /**
    * @param dirItem Item for inspection/deletion
    */
    private void processItem(String dirItem)
    {
        int daysOld = frame1.frostSettings.getIntValue("maxMessageDisplay")+1;
        File f = new File(dirItem);
        long expiration = new Date().getTime() - (daysOld * 24 * 60 * 60 * 1000);
        if (f.lastModified() < expiration || f.isDirectory() && !f.getName().endsWith(".key"))
        {
            f.delete();
        }
    }
}

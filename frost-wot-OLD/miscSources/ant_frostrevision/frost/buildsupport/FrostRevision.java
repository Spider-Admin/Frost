package frost.buildsupport;

import org.apache.tools.ant.*;

/**
 * Simple ANT task to build the display revision, shown in about dialog.
 * Uses the following properties:
 * - revision      (set in build.xml)
 * - isRelease     (set in build.xml)
 * - modified      (set by antsvn)
 * - revision.max  (set by antsvn)
 */
public class FrostRevision extends Task {

   /**
    * called by ANT when executing this task.
    */
   public void execute() throws BuildException {

      String displayRevision = getProject().getProperty("revision") + " (" + getProject().getProperty("revision.max");

      if( getProject().getProperty("isRelease") != null ) {
          displayRevision += ",release";
      } else {
          displayRevision += ",dev";
      }

      if( getProject().getProperty("modified") != null ) {
          displayRevision += ",modified";
      }

      displayRevision += ")";

      getProject().setProperty("displayRevision", displayRevision);
      
      System.out.println("displayRevision = " + displayRevision);
   }
}


// $Id: EmacsEditor.java,v 1.2 2006/06/28 02:11:51 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E M A C S   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU extensible, customizable, self-documenting text editor.
 */
public
class EmacsEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EmacsEditor()
  {
    super("Emacs", new VersionID("2.0.9"), "Temerity", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "Emacs");

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.Unix)  
      return "emacs";
    else if(PackageInfo.sOsType == OsType.Windows) 
      return "runemacs.exe"; 
    return super.getProgram();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   */  
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  )   
    throws PipelineException
  {
    SubProcessLight proc = null;
    if(PackageInfo.sOsType == OsType.MacOS) { 
      /* 
       * The Mac OS X version of Emacs requires a ":" at the start of absolute file paths
       * which is different than every other AppleScript application.  For this reason, the
       * launch method from BaseAppleScriptEditor cannot be used in this plugin.
       */
      ArrayList<String> args = new ArrayList<String>();
      args.add("-e");
      args.add("tell application \"" + getProgram() + "\"");
      
      for(File file : fseq.getFiles()) {
	String macpath = file.getPath().replace("/",":");
	args.add("-e");
	args.add("open file \"" + macpath + "\"");
      }
      
      args.add("-e");
      args.add("end tell");
      
      proc = new SubProcessLight(getName(), "osascript", args, env, dir);
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : fseq.getFiles()) 
	args.add(file.getPath());
      
      proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    }

    proc.start();
    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

   private static final long serialVersionUID = 3797613382593920250L;

}



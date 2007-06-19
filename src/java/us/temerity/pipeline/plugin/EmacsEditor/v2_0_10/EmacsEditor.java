// $Id: EmacsEditor.java,v 1.1 2007/06/19 18:24:49 jim Exp $

package us.temerity.pipeline.plugin.EmacsEditor.v2_0_10;

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
    super("Emacs", new VersionID("2.0.10"), "Temerity", 
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
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * This implementation returns <CODE>null</CODE> to fix the bug
   * <A href="http://temerity.us/community/forums/viewtopic.php?t=933"><B>Inherited Editor 
   * Prep Method</B></A>.
   * 
   * @param author
   *   The name of the user owning the files.
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
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    return null;
  }

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

      /* forces buffers edited in Emacs to use Unix newline semantics */ 
      String eval = ("--eval=(add-hook 'first-change-hook " + 
		     "(lambda () (set-buffer-file-coding-system 'utf-8-unix) nil))");
      if(PackageInfo.sOsType == OsType.Unix)
	args.add(eval);
      else 
	args.add("\"" + eval + "\"");

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

  private static final long serialVersionUID = -2932110831921721413L;

}



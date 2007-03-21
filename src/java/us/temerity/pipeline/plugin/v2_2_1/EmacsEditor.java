// $Id: EmacsEditor.java,v 1.2 2007/03/21 22:14:04 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
  extends SimpleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EmacsEditor()
  {
    super("Emacs", new VersionID("2.2.1"), "Temerity", 
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
      
      return new SubProcessLight(author, getName(), "osascript", args, env, dir);
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
      
      return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    }
  }

  /** 
   * This implementation always throws a PipelineException, to insure that the {@link #prep
   * prep} method is used for this Editor instead of this deprecated method.
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
  @SuppressWarnings("deprecation")
  @Deprecated
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  )   
    throws PipelineException
  {
    throw new PipelineException
      ("This launch() method should never be called since the prep() method returns " + 
       "a non-null SubProcessLight instance!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -964685465904115612L;

}



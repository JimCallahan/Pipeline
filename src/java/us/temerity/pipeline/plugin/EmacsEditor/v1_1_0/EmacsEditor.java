// $Id: EmacsEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.EmacsEditor.v1_1_0;

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
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EmacsEditor()
  {
    super("Emacs", new VersionID("1.1.0"), "Temerity", 
	  "The GNU extensible, customizable, self-documenting text editor.", 
	  "Emacs");
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
    if(PackageInfo.sOsType == OsType.Unix) {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : fseq.getFiles()) 
	args.add(file.getPath());
      
      SubProcessLight proc = new SubProcessLight(getName(), "emacs", args, env, dir);
      proc.start();
      
      return proc;
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      return super.launch(fseq, env, dir);
    }

    throw new PipelineException
      ("The Emacs Editor is not yet supported on the Windows operating system.");
  }





  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 152200663197303643L;

}



// $Id: GimpEditor.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_1_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G I M P   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU Image Manipuation Program. 
 */
public
class GimpEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GimpEditor()
  {
    super("Gimp", new VersionID("1.1.0"), "Temerity", 
	  "The GNU Image Manipulation Program.", 
	  "Gimp");
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
    if(PackageInfo.sOsType == OsType.Unix) {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : fseq.getFiles()) 
	args.add(file.getPath());
      
      SubProcessLight proc = new SubProcessLight(getName(), "gimp", args, env, dir);
      proc.start();
      
      return proc;
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      return super.launch(fseq, env, dir);
    }

    throw new PipelineException
      ("The GIMP Editor is not yet supported on the Windows operating system.");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 483224756697841093L;

}



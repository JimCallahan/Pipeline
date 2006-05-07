// $Id: IllustratorEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   I L L U S T R A T O R   E D I T O R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Illustrator vector graphics editor.
 */
public
class IllustratorEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IllustratorEditor()
  {
    super("Illustrator", new VersionID("2.0.9"), "Temerity",
	  "Adobe Illustrator vector graphics editor.", 
	  "Adobe Illustrator");

    removeSupport(OsType.Unix);
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
    if(PackageInfo.sOsType == OsType.Windows) 
      return "Illustrator.exe"; 
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
    if(PackageInfo.sOsType == OsType.MacOS) 
      return super.launch(fseq, env, dir);

    ArrayList<String> args = new ArrayList<String>();
    for(File file : fseq.getFiles()) 
      args.add(file.getPath());
    
    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    
    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 636923773622312453L;

}



// $Id: AcrobatEditor.java,v 1.1 2007/04/05 10:02:58 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A C R O B A T   E D I T O R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Adobe Acrobat PDF document editor.
 */
public
class AcrobatEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AcrobatEditor()
  {
    super("Acrobat", new VersionID("2.2.1"), "Temerity",
	  "Adobe Acrobat PDF document editor.",           
	  "Adobe Acrobat 7.0 Professional");

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
      return "Acrobat.exe"; 
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
    if(PackageInfo.sOsType == OsType.MacOS) 
      return super.prep(author, fseq, env, dir);

    ArrayList<String> args = new ArrayList<String>();
    for(File file : fseq.getFiles()) 
      args.add(file.getPath());
    
    return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
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

  private static final long serialVersionUID = 1648580267246320040L;

}

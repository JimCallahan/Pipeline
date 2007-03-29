// $Id: GimpEditor.java,v 1.3 2007/03/29 18:56:31 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G I M P   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The GNU Image Manipuation Program. <P> 
 * 
 * If the environmental variable GIMP_BINARY is defined, its value will be used as the 
 * name of the GIMP executable instead of the default "gimp" (Unix) or "gimp-2.2.exe" 
 * (Windows).  On Windows, the GIMP program name should include the ".exe" extension.  
 * Mac OS X systems use Apple Script to launch GIMP and therefore ignore the name of the 
 * program binary.
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
    super("Gimp", new VersionID("2.2.1"), "Temerity", 
	  "The GNU Image Manipulation Program.", 
	  "Gimp");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    
    return new SubProcessLight(author, getName(), getGimpProgram(env), args, env, dir);
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
  /*   H E L P E R                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the name of the GIMP program based on the Toolset environment and 
   * current operating system type.<P> 
   */
  private String
  getGimpProgram
  (
    Map<String,String> env
  ) 
    throws PipelineException
  {
    String gimp = env.get("GIMP_BINARY");
    if((gimp != null) && (gimp.length() > 0)) 
      return gimp; 
    
    if(PackageInfo.sOsType == OsType.Windows) 
      return "gimp-2.2.exe";

    return "gimp";
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4059947840377971319L;

}



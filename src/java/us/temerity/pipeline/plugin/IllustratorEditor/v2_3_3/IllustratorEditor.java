// $Id: IllustratorEditor.java,v 1.1 2007/06/30 23:31:37 jim Exp $

package us.temerity.pipeline.plugin.IllustratorEditor.v2_3_3;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P H O T O S H O P   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Illustrator vector graphics editor.
 * 
 * The ADOBE_CS_VERSION environmental variable from the Toolset can be specified to 
 * override the default version of the Adobe Create Suite used to launch Illustrator.  For
 * example, setting ADOBE_CS_VERSION="3" will cause "Adobe Illustrator CS3" to be used as 
 * the application instead of the default "Adobe Illustrator CS2".
 */
public
class IllustratorEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IllustratorEditor()
  {
    super("Illustrator", new VersionID("2.3.3"), "Temerity",
	  "The Adobe Illustrator vector graphics editor.", 
	  "Illustrator.exe");

    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 

    underDevelopment();
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
    try {
      ArrayList<String> args = new ArrayList<String>();
      if(PackageInfo.sOsType == OsType.MacOS) {
        String csv = env.get("ADOBE_CS_VERSION");
        if(csv == null) 
          csv = "2";
        
        args.add("-a");
        args.add("/Applications/Adobe Illustrator CS" + csv + "/Adobe Illustrator.app"); 
        
        for(Path path : fseq.getPaths()) 
          args.add(path.getName()); 
        
        return new SubProcessLight(author, getName(), "open", args, env, dir);
      }
      else {
        for(File file : fseq.getFiles()) 
          args.add(file.getPath());
        
        return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
      }
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to launch this Editor!\n" +
	 ex.getMessage());
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

  //private static final long serialVersionUID = 

}



// $Id: ShakeEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.ShakeEditor.v2_0_9;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   E D I T O R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The Shake image viewer.
 */
public
class ShakeEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeEditor()
  {
    super("Shake", new VersionID("2.0.9"), "Temerity",
	  "The Shake image viewer.", 
	  "shake");  

    addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch fcheck under the given environmant to view the images in the given 
   * file sequence. 
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
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    if(fseq.isSingle()) {
      args.add(fseq.getFile(0).toString());
    }
    else {      
      args.add(fseq.getFilePattern().toString());
      args.add("-t");
      args.add(fseq.getFrameRange().toString());
    }

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2592763684826805674L;

}



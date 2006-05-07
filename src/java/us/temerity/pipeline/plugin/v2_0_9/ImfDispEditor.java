// $Id: ImfDispEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   I M F   D I S P   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Mental Ray image viewer.
 */
public
class ImfDispEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ImfDispEditor()
  {
    super("ImfDisp", new VersionID("2.0.9"), "Temerity",
	  "The Mental Ray image viewer.", 
	  "imf_disp");

    addSupport(OsType.Windows); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch imfdisp under the given environmant to view the images in the given 
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
    for(File file : fseq.getFiles()) 
      args.add(file.getName());

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1410283998296965898L;

}



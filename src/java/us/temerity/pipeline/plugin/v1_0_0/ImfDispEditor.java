// $Id: ImfDispEditor.java,v 1.2 2004/10/14 22:38:15 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   I M F   D I S P   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An alternative Maya image viewer.
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
    super("ImfDisp", new VersionID("1.0.0"),
	  "An alternative Maya image viewer.", 
	  "imf_disp");
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcess
   */  
  public SubProcess
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

    SubProcess proc = new SubProcess(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6115291021569654950L;

}



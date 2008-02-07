package com.theorphanage.pipeline.plugin.FrameCyclerEditor.v1_0_0;


import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   C Y C L E R   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The FrameCycler image viewer.
 */
public
class FrameCyclerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
	 * 
	 */
	private static final long serialVersionUID = -5450027692388828671L;



public
  FrameCyclerEditor()
  {
    super("FrameCycler", new VersionID("1.0.0"), "TheO",
	  "The FrameCycler image viewer.", 
	  "framecycler");  
    underDevelopment();
    //addSupport(OsType.MacOS);
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
    if(PackageInfo.sOsType == OsType.MacOS)  
      return "FrameCycler";
    else if(PackageInfo.sOsType == OsType.Windows) 
      return "FrameCyclerLUT.exe"; 
    return super.getProgram();
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
    if(fseq.hasFrameNumbers()) {
      args.add(fseq.getFilePattern().toString());

      FrameRange range = fseq.getFrameRange();
      args.add(range.getStart() + "-" + range.getEnd());
    }
    else {
      args.add(fseq.getFile(0).toString());
    }

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  
}



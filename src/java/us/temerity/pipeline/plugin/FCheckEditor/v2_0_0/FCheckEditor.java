// $Id: FCheckEditor.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.FCheckEditor.v2_0_0;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F C H E C K   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The Maya image viewer.
 */
public
class FCheckEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  FCheckEditor()
  {
    super("FCheck", new VersionID("2.0.0"), "Temerity",
	  "The Maya image viewer.", 
	  "fcheck");
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
    if(PackageInfo.sOsType == OsType.Unix) {
      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix == null) || (!suffix.equals("iff"))) {
	throw new PipelineException
	  ("Illegal image format (" + suffix + "), only IFF format images are currently " +
	   "supported by the FCheck editor on Unix systems.");
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    if(fseq.isSingle()) {
      args.add(fseq.getFile(0).toString());
    }
    else {
      FrameRange range = fseq.getFrameRange();
      FilePattern pat  = fseq.getFilePattern();
      
      switch(pat.getPadding()) {
      case 0: 
      case 1: 
      case 4:
	args.add("-n");
	args.add(String.valueOf(range.getStart()));
	args.add(String.valueOf(range.getEnd()));
	args.add(String.valueOf(range.getBy()));
	break; 
	
      default:
	throw new PipelineException
	  ("FCheck only supports unpadded (@) and four place zero padded (#) " + 
	   "frame numbers for image sequences.");
      }
      
      args.add(pat.toString());
    }

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5543534132573576197L;

}



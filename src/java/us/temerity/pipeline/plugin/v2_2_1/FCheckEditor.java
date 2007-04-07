// $Id: FCheckEditor.java,v 1.1 2007/04/07 01:12:25 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
    super("FCheck", new VersionID("2.2.1"), "Temerity",
	  "The Maya image viewer.", 
	  "fcheck");
    
    addSupport(OsType.Windows); 
    addSupport(OsType.MacOS); 
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

  private static final long serialVersionUID = -3372887820953175511L;

}



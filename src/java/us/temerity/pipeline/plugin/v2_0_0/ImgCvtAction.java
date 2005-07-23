// $Id: ImgCvtAction.java,v 1.1 2005/07/23 21:57:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   I M G   C V T   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Convert the images files which make up the primary file sequence of one of the source
 * nodes into the image format of the primary file sequence of this node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images to convert. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class ImgCvtAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ImgCvtAction() 
  {
    super("ImgCvt", new VersionID("2.0.0"), 
	  "Converts images files from another format.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node containing the images to convert.",
	 null);
      addSingleParam(param);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    File fromPath = null;
    FileSeq fromSeq = null;
    FileSeq toSeq = null;
    {    
      ArrayList<String> formats = new ArrayList<String>(); 
      formats.add("yuv");
      formats.add("als");
      formats.add("tdi");
      formats.add("iff");
      formats.add("gif");
      formats.add("jpg");
      formats.add("jpeg");
      formats.add("cin");
      formats.add("lff");
      formats.add("pxb");
      formats.add("ppm");
      formats.add("pri");
      formats.add("qtl");
      formats.add("rgb");
      formats.add("sgi");
      formats.add("bw");
      formats.add("icon");
      formats.add("pic");
      formats.add("tga");
      formats.add("tif");
      formats.add("tiff");
      formats.add("vst");
      formats.add("rla");

      {
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source " + 
	     "nodes!");

	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !formats.contains(suffix)) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Image Source " + 
	     "(" + sname + ") does not contain a supported image format!");
	  
	if(!fseq.hasFrameNumbers()) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Image Source " + 
	     "(" + sname + ") does not have frame numbers!");

	fromSeq = fseq;

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, 
			    snodeID.getWorkingParent() + "/" + fseq.getFilePattern());
      }
	
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !formats.contains(suffix) || suffix.equals("gif")) 
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") is not a supported " + 
	     "output image format!");

	if(!fseq.hasFrameNumbers()) 
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have frame numbers!");

	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source file sequence (" + fromSeq + ") did not have the same number of " +
	   "frames as the target file sequence (" + toSeq + ")!");
    }
    
    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-r");
      args.add(fromSeq.getFrameRange().toString()); 

      args.add("-R");
      args.add(toSeq.getFrameRange().toString()); 

      args.add(fromPath.toString());
      args.add(toSeq.getFilePattern().toString());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "imgcvt", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //  private static final long serialVersionUID = 

}


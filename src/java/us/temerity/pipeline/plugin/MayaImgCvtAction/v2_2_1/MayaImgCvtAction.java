// $Id: MayaImgCvtAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MayaImgCvtAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   I M G   C V T   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Maya image conversion utility. 
 * 
 * Converts the images files which make up the primary file sequence of one of the source
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
 * 
 * By default, the "python" program is used by this action to run the "imgcvt" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class MayaImgCvtAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaImgCvtAction() 
  {
    super("MayaImgCvt", new VersionID("2.2.1"), "Temerity",
	  "Converts images files from another format.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node containing the images to convert.",
	 null);
      addSingleParam(param);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);   
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
    /* file sequence checks */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    FileSeq targetSeq = null;
    {
      {    
	String sname = getSingleStringParamValue(aImageSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
	
	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have a " + 
	     "filename suffix!");
	
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

        if(!formats.contains(suffix)) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Image Source does not " + 
             "contain a supported image format!");

	targetSeq = fseq;
      }

      if(sourceSeq.numFrames() != targetSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + sourceSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + targetSeq + ")!");
    }

    /* image conversion program */ 
    String program = "imgcvt";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "imgcvt.exe";

    /* create the process to run the action */ 
    ArrayList<String> args = new ArrayList<String>();
    args.add("-v");
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 526393557539731440L;

  public static final String aImageSource = "ImageSource";

}


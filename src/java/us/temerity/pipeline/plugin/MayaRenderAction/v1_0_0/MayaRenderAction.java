// $Id: MayaRenderAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderAction.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images using a single Maya scene source node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
 *   Processors <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of processors to use (0 = all available). <BR> 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaRenderAction() 
  {
    super("MayaRender", new VersionID("1.0.0"), "Temerity", 
	  "Renders a Maya scene.");
    
    {
      ActionParam param = 
	new StringActionParam
	("CameraOverride",
	 "Overrides the render camera (if set).", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("Processors", 
	 "The number of processors to use (0 = all available).", 
	 0);
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
    /* sanity checks */ 
    File scene = null;
    {
      {
	ArrayList<String> exts = new ArrayList<String>();
	exts.add("bmp");  
	exts.add("gif");  
	exts.add("jpeg");  
	exts.add("jpg");  
	exts.add("rgb"); 
	exts.add("rla"); 
	exts.add("sgi");
	exts.add("tga");
	exts.add("tif");
	exts.add("tiff");
	exts.add("iff");

	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!exts.contains(suffix)) 
	  throw new PipelineException
	    ("The MayaRender Action does not support target images with a suffix of " + 
	     "(" + suffix + ").");

	if(!fseq.hasFrameNumbers())
	  throw new PipelineException
	    ("The MayaRender Action requires that the output images have frame numbers.");
      }
      
      NodeID nodeID = agenda.getNodeID();
      if(agenda.getSourceNames().size() == 1) {	
	for(String sname : agenda.getSourceNames()) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  String suffix = fseq.getFilePattern().getSuffix();
	  if((suffix != null) && (suffix.equals("ma") || suffix.equals("mb"))) {
	    NodeID snodeID = new NodeID(nodeID, sname);
	    scene = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	  }
	  
	  break;
	}
      }

      if(scene == null)
	throw new PipelineException
	  ("The MayaRender Action reqiures exactly ONE source node which must be " + 
	   "a Maya scene file!");
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-render");
      args.add(scene.getPath());

      NodeID nodeID = agenda.getNodeID();
      File path = new File(nodeID.getName());
      FileSeq fseq = agenda.getPrimaryTarget();
      FrameRange range = fseq.getFrameRange();
      FilePattern fpat = fseq.getFilePattern();

      args.add("-s");
      args.add(String.valueOf(range.getStart()));
      args.add("-e");
      args.add(String.valueOf(range.getEnd()));
      args.add("-b");
      args.add(String.valueOf(range.getBy()));
      
      args.add("-pad"); 
      args.add(String.valueOf(fpat.getPadding()));

      File dir = new File(PackageInfo.sProdDir, 
			  nodeID.getWorkingParent().toFile().getPath());
      args.add("-rd");
      args.add(dir.getPath()); 
      
      args.add("-im");
      args.add(path.getName());      
    
      args.add("-of");
      args.add(fpat.getSuffix());

      {
	String camera = (String) getSingleParamValue("CameraOverride");
	if((camera != null) && (camera.length() > 0)) {
	  args.add("-cam");
	  args.add(camera);
	}
      }

      {
	Integer procs = (Integer) getSingleParamValue("Processors");
	if(procs != null) {
	  if(procs < 0) 
	    throw new PipelineException
	      ("The MayaRender Action requires that the Processors parameter is " + 
	       "non-negative.");

	  args.add("-n");
	  args.add(procs.toString());
	}
      }

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "maya", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 5401912309053601408L;

}


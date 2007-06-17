// $Id: MayaRenderAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderAction.v1_2_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images from a source Maya scene source node. <P> 
 * 
 * This version supports Maya(v6) rendering command options and pre/post MEL scripts. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Renderer <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of renderer (Hardware, Software or Mental Ray) used to render the images. <BR>
 *   </DIV> <BR>
 * 
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
 *   Processors <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of processors to use (0 = all available). <BR> 
 *   </DIV> <BR>
 * 
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file to render. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate before rendering begins. <BR>
 *   </DIV> <BR>
 * 
 *   Post Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate after rendering ends. <BR>
 *   </DIV> <BR>
 * 
 *   Pre Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate before rendering each 
 *     frame. <BR>
 *   </DIV> <BR>
 * 
 *   Post Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate after rendering each 
 *     frame. <BR>
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
    super("MayaRender", new VersionID("1.2.0"), "Temerity", 
	  "Renders a Maya scene.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("MayaScene",
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> names = new ArrayList<String>();
      names.add("Hardware");
      names.add("Software");
      names.add("Mental Ray");

      ActionParam param = 
	new EnumActionParam
	("Renderer",
	 "The type of renderer to use.", 
	 "Software", names); 
      addSingleParam(param);
    }

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
	 1);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PreRenderMEL",
	 "The pre-render MEL script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostRenderMEL",
	 "The post-render MEL script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PreFrameMEL",
	 "The pre-frame MEL script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostFrameMEL",
	 "The post-frame MEL script.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Renderer");
      layout.addSeparator();
      layout.addEntry("CameraOverride");
      layout.addEntry("Processors"); 
      layout.addSeparator();
      layout.addSeparator();
      layout.addEntry("MayaScene");

      {
	LayoutGroup mel = new LayoutGroup
	  ("MEL Scripts", 
	   "MEL scripts run at various stages of the rendering process.", 
	   true);
	mel.addEntry("PreRenderMEL"); 
	mel.addEntry("PostRenderMEL");
	mel.addSeparator();
	mel.addEntry("PreFrameMEL"); 
	mel.addEntry("PostFrameMEL"); 

	layout.addSubGroup(mel);
      }

      setSingleLayout(layout);
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
    File scene = null;
    File preRender = null;
    File postRender = null;
    File preFrame = null;
    File postFrame = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	if(!fseq.hasFrameNumbers())
	  throw new PipelineException
	    ("The MayaRender Action requires that the output images have frame numbers.");
      }

      {
	String sname = (String) getSingleParamValue("MayaScene"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || 
	     (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	    throw new PipelineException
	      ("The MayaRender Action requires that the source node specified by the Maya " +
	       "Scene parameter (" + sname + ") must have a single Maya scene file as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaRender Action requires the Maya Scene parameter to be set!");
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PreRenderMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Render MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaRender Action requires that the source node specified by the Pre " +
	       "Render MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preRender = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("PostRenderMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Render MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaRender Action requires that the source node specified by the Post " +
	       "Render MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postRender = new File(PackageInfo.sProdDir,
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PreFrameMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Frame MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaFrame Action requires that the source node specified by the Pre " +
	       "Frame MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preFrame = new File(PackageInfo.sProdDir,
			      snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("PostFrameMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Frame MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaFrame Action requires that the source node specified by the Post " +
	       "Frame MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postFrame = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-renderer");

      String renderer = (String) getSingleParamValue("Renderer"); 
      if(renderer.equals("Software")) 
	args.add("sw");
      else if(renderer.equals("Hardware")) 
	args.add("hw");
      else if(renderer.equals("Mental Ray")) 
	args.add("mr");
      else 
	throw new PipelineException
	  ("Unsupported renderer type (" + renderer + ")!");
    
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
	
      if(preRender != null) {
	args.add("-preRender");
	args.add("source \"" + preRender.getPath() + "\"");
      }
	  
      if(postRender != null) {
	args.add("-postRender");
	args.add("source \"" + postRender.getPath() + "\"");
      }

      if(renderer.equals("Hardware")) {

	// should check here to make sure node matches the hardware renderer's output
	// image format, padding and frame naming conventions
	
      }
      else {
	args.add("-fnc"); 
	args.add("3"); 

	args.add("-pad"); 
	args.add(String.valueOf(fpat.getPadding()));

	args.add("-of");  
	args.add(fpat.getSuffix());
      }

      File dir = new File(PackageInfo.sProdDir, 
			  nodeID.getWorkingParent().toFile().getPath());
      args.add("-rd");
      args.add(dir.getPath()); 
      
      args.add("-im");
      args.add(path.getName());      
      
      {
	String camera = (String) getSingleParamValue("CameraOverride");
	if((camera != null) && (camera.length() > 0)) {
	  args.add("-cam");
	  args.add(camera);
	}
      }
      
      if(renderer.equals("Software")) {  
	Integer procs = (Integer) getSingleParamValue("Processors");
	if(procs != null) {
	  if(procs < 0) 
	    throw new PipelineException
	      ("The MayaRender Action requires that the Processors parameter is " + 
	       "non-negative.");
	  
	  args.add("-n"); 
	  args.add(procs.toString());
	}

	if(preFrame != null) {
	  args.add("-pre");
	  args.add("source \"" + preFrame.getPath() + "\"");
	}
	
	if(postFrame != null) {
	  args.add("-post");
	  args.add("source \"" + postFrame.getPath() + "\"");
	}
      }

      args.add(scene.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "Render", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 7574872839083068401L;

}


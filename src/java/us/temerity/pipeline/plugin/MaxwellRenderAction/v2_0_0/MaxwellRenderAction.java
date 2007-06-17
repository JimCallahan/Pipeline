// $Id: MaxwellRenderAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MaxwellRenderAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X W E L L   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images from a source Maxwell input file.<P> 
 * 
 * Normally the behavior of Maxwell is specified during generation of the input MXS file.
 * However, this action provides the ability to override a subset of those parameters 
 * controllable from the mxcl(1) command line.  When these parameters are not overridden, the 
 * MXS file will determine the behavior of Maxwell for those parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maxwell Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maxwell scene file to render. <BR> 
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Override Threads <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to override the number of render threads. <BR>
 *   </DIV> <BR>
 *  
 *   Threads <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of render threads to use (0 = autodetect). <BR> 
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Override Resolution <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to override the image resolution. <BR>
 *   </DIV> <BR>
 *  
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     The horizontal resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 *  
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;">
 *     The vertical resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 *   <BR>
 * 
 *   <I>Render Output Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Override Output <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to override the image output settings. <BR>
 *     </DIV> <BR>
 * 
 *     Output RGB <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output a RGB color image.
 *     </DIV> <BR>
 * 
 *     Output Alpha <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an Alpha mask image.
 *     </DIV> <BR>
 * 
 *     Output Depth <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an Z Depth image.
 *     </DIV> <BR>
 * 
 *     Depth Min<BR>
 *     <DIV style="margin-left: 40px;">
 *       The minimum depth value.   ???
 *     </DIV> <BR>
 * 
 *     Depth Max<BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum depth value.   ???
 *     </DIV> <BR>
 *     <BR>
 * 
 *     Output Camera Cosine <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an image containing the cosine of the angle between surface normals
 *       and the view direction.
 *     </DIV> <BR>
 * 
 *     Output MXI <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to output an MXI file. 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Render Quality Parameters</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Override Quality <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to override the render quality settings. <BR>
 *     </DIV> <BR>
 * 
 *     Render Time <BR>
 *     <DIV style="margin-left: 40px;">
 *       The maximum number of minutes to render.<BR> 
 *     </DIV> <BR>
 *   
 *     Sample Level <BR> 
 *     <DIV style="margin-left: 40px;">
 *       The maxmimum sampling level.<BR> 
 *     </DIV> <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MaxwellRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxwellRenderAction() 
  {
    super("MaxwellRender", new VersionID("2.0.0"), "Temerity",
	  "Renders a Maxwell scene.");
    
    /* general */ 
    {
      ActionParam param = 
	new LinkActionParam
	("MaxwellScene",
	 "The source Maxwell scene (MXS) node.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	("OverrideThreads",
	 "Whether to override the the number of render threads.", 
	 false);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	("Threads", 
	 "The number of render threads to use (0 = autodetect).", 
	 1);
      addSingleParam(param);
    }

    /* image resolution */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OverrideResolution",
	   "Whether to override the image resolution.", 
	   false);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageWidth",
	   "The horizontal resolution of the output image in pixels.", 
	   640);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  ("ImageHeight",
	   "The vertical resolution of the output image in pixels.", 
	   480);
	addSingleParam(param);
      }
    }

    /* render output */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OverrideOutput",
	   "Whether to override the image output settings.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputRGB",
	   "Whether to output a RGB color image.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputAlpha",
	   "Whether to output an Alpha mask image.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputDepth",
	   "Whether to output an Z Depth image.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("DepthMin",
	   "The minimum depth value.",
	   -1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new DoubleActionParam
	  ("DepthMax",
	   "The maximum depth value.",
	   1.0);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputCameraCosine",
	   "Whether to output an image containing the cosine of the angle between surface " + 
	   "normals and the view direction.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OutputMXI",
	   "Whether to output an MXI file.",
	   false);
	addSingleParam(param);
      }
    }

    /* render quality */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("OverrideQuality",
	   "Whether to override the render quality settings.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new IntegerActionParam
	  ("RenderTime",
	   "The maximum number of minutes to render.", 
	   10);
	addSingleParam(param);
      }

      {      
	ActionParam param = 
	  new DoubleActionParam
	  ("SampleLevel",
	   "The maxmimum sampling level.",
	   20.0);
	addSingleParam(param);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("MaxwellScene");
      layout.addSeparator();
      layout.addEntry("OverrideThreads"); 
      layout.addEntry("Threads"); 
      layout.addSeparator();
      layout.addEntry("OverrideResolution");
      layout.addEntry("ImageWidth");
      layout.addEntry("ImageHeight");

      {
	LayoutGroup ro = new LayoutGroup
	  ("RenderOutput", "Controls image file output.", false);
	ro.addEntry("OverrideOutput");
	ro.addSeparator(); 	
	ro.addEntry("OutputRGB");
	ro.addEntry("OutputAlpha");
	ro.addSeparator(); 
	ro.addEntry("OutputDepth");
	ro.addEntry("DepthMin");
	ro.addEntry("DepthMax");
	ro.addSeparator(); 
	ro.addEntry("OutputCameraCosine");
	ro.addEntry("OutputMXI");

	layout.addSubGroup(ro);
      }

      {
	LayoutGroup rq = new LayoutGroup
	  ("RenderQuality", "Overall quality controls.", false);
	rq.addEntry("OverrideQuality");
	rq.addSeparator(); 	
	rq.addEntry("RenderTime");
	rq.addEntry("SampleLevel");

	layout.addSubGroup(rq);
      }

      setSingleLayout(layout);
    }

    underDevelopment();
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
    {
      {
	String sname = (String) getSingleParamValue("MaxwellScene"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Maxwell Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || 
	     (suffix == null) || !(suffix.equals("mxs")))
	    throw new PipelineException
	      ("The MaxwellRender Action requires that the source node specified by the Maxwell " +
	       "Scene parameter (" + sname + ") must have a single Maxwell scene file (.mxs) as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MaxwellRender Action requires the Maxwell Scene parameter to be set!");
	}
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-mxs:" + scene);

      File path = new File(nodeID.getName());
      FileSeq fseq = agenda.getPrimaryTarget();
      FrameRange range = fseq.getFrameRange();
      FilePattern fpat = fseq.getFilePattern();

      if(fpat.hasFrameNumbers())  
	args.add("-animation:" + range.getStart() + "-" + range.getEnd());

      if(fpat.getSuffix() == null) 
	throw new PipelineException 
	  ("The target file sequence (" + fseq + ") must have a filename suffix!");

      {
	// FIX THIS ONCE FILE PATTERNS ARE IMPROVED!!
	File file = 
	  new File(PackageInfo.sProdDir,
		   nodeID.getWorkingParent() + "/" + fpat.getPrefix() + "." + fpat.getSuffix());
	args.add("-output:" + file);
      }

      /* override threads */ 
      {
	Boolean override = (Boolean) getSingleParamValue("OverrideThreads"); 
	if((override != null) && override) {
	  Integer threads = (Integer) getSingleParamValue("Threads"); 
	  if((threads == null) || (threads <= 0)) 
	    throw new PipelineException
	      ("The value of Threads (" + threads + ") was illegal!");
	  args.add("-threads:" + threads);
	}
      }

      /* override image resolution */ 
      {
	Boolean override = (Boolean) getSingleParamValue("OverrideResolution"); 
	if((override != null) && override) {
	  Integer width = (Integer) getSingleParamValue("ImageWidth"); 
	  if((width == null) || (width <= 0)) 
	    throw new PipelineException
	      ("The value of ImageWidth (" + width + ") was illegal!");

	  Integer height = (Integer) getSingleParamValue("ImageHeight"); 
	  if((height == null) || (height <= 0)) 
	    throw new PipelineException
	      ("The value of ImageHeight (" + height + ") was illegal!");
	  
	  args.add("-res:" + width + "x" + height);
	}
      }

      /* override render quality */ 
      {
	Boolean override = (Boolean) getSingleParamValue("OverrideQuality"); 
	if((override != null) && override) {
	  Integer time = (Integer) getSingleParamValue("RenderTime"); 
	  if((time == null) || (time <= 0)) 
	    throw new PipelineException
	      ("The value of RenderTime (" + time + ") was illegal!");
	  args.add("-time:" + time);

	  Double samples = (Double) getSingleParamValue("SampleLevel");
	  if((samples == null) || (samples <= 0.0)) 
	    throw new PipelineException
	      ("The value of SampleLevel (" + samples + ") was illegal!");
	  args.add("-sampling:" + samples.intValue());
	}
      }
      
      /* override render output */ 
      {
	Boolean override = (Boolean) getSingleParamValue("OverrideOutput"); 
	if((override != null) && override) {
	  Boolean rgb = (Boolean) getSingleParamValue("OutputRGB");  
	  if(rgb == null) 
	    throw new PipelineException
	      ("The OutputRGB was (null)!");
	  
	  Boolean alpha = (Boolean) getSingleParamValue("OutputAlpha");  
	  if(alpha == null) 
	    throw new PipelineException
	      ("The OutputAlpha was (null)!");
	  
	  Boolean depth = (Boolean) getSingleParamValue("OutputDepth");  
	  if(depth == null) 
	    throw new PipelineException
	      ("The OutputDepth was (null)!");
	  
	  Double dmin = (Double) getSingleParamValue("DepthMin");
	  if(dmin == null)
	    throw new PipelineException
	      ("The value of DepthMin (" + dmin + ") was illegal!");
	  
	  Double dmax = (Double) getSingleParamValue("DepthMax");
	  if(dmax == null)
	    throw new PipelineException
	      ("The value of DepthMax (" + dmax + ") was illegal!");
	  
	  if(dmin > dmax) 
	    throw new PipelineException
	      ("The value of DepthMin (" + dmin + ") must be less-than DepthMax (" + dmax + ")!");
	  
	  Boolean cosine = (Boolean) getSingleParamValue("OutputCameraCosine");  
	  if(cosine == null) 
	    throw new PipelineException
	      ("The OutputCameraCosine was (null)!");
	  
	  Boolean mxi = (Boolean) getSingleParamValue("OutputMXI");  
	  if(mxi == null) 
	    throw new PipelineException
	      ("The OutputMXI was (null)!");
	  
	  {
	    StringBuilder buf = new StringBuilder();
	    boolean first = true;
	    if(rgb) {
	      buf.append("r");
	      first = false;
	    }

	    if(alpha) {
	      if(!first) 
		buf.append(",");
	      buf.append("a");
	      first = false;
	    }

	    if(cosine) {
	      if(!first) 
		buf.append(",");
	      buf.append("c");
	      first = false;
	    }

	    if(depth) {
	      if(!first) 
		buf.append(",");
	      buf.append("z(" + dmin + "," + dmax + ")");
	      first = false;
	    }

	    if(!first) 
	      args.add("-layers:" + buf);
	  }
	  
	  if(mxi) 
	    args.add("-mxi:write");
	}
      }

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "mxcl", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -8605298485939986164L;

}


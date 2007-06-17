// $Id: AirRenderAction.java,v 1.1 2007/06/17 15:34:38 jim Exp $

package us.temerity.pipeline.plugin.AirRenderAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   R E N D E R   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The AIR (3.1+) RenderMan compliant renderer. <P> 
 * 
 * All of the RIB file (.rib) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed.  The frame range rendered will be limited by frame 
 * numbers of the target images.  In most cases, an Execution Method of (Parallel) and a Batch Size 
 * of (1) should be used with this action so that each image frame is rendered by a seperate 
 * invocation of air(1) which is only passed the RIBs required for the frame being rendered.  It is 
 * also possible to render multi-frame RIBs or even multiple single frame RIBs at one time by using 
 * a larger Batch Size.  Depending on the RIBs processed, one or more images, depthmaps or deep 
 * shadow maps may be generated in one rendering pass. <P> 
 * 
 * If the Generate Animation parameter is set, all RIB file sequences specified by the Order 
 * per-source sequence parameters must contain single RIB files. This parameter causes automatic
 * generation of frame blocks which subdivide the motion blocks within the RIBs in order to 
 * specify the scene rendered for each output image frame. <P>  
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>air</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Gamma <BR>
 *   <DIV style="margin-left: 40px;">
 *     Sets the default display gamma. 
 *   </DIV> <BR>
 * 
 *   Spiral Buckets <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to render buckets in a spiral order.
 *   </DIV> <BR>
 * 
 * 
 *   Generate Animation <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to generate frame blocks for animation time-varying RIBs.
 *   </DIV> <BR>
 *   
 *   Frame Blocks <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of animation frame blocks to generate.
 *   </DIV> <BR>
 * 
 *   Start Time <BR>
 *   <DIV style="margin-left: 40px;">
 *     The animation start time.
 *   </DIV> <BR>
 * 
 *   End Time <BR>
 *   <DIV style="margin-left: 40px;">
 *     The animation end time.
 *   </DIV> <BR>
 * 
 *   Shutter Open <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fraction of the animation frame time that the shutter is open.
 *   </DIV> <BR>
 * 
 * </DIV>
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a RIB file as its primary
 *     file sequence.  This parameter determines the order in which the input RIB files are
 *     processed. If this parameter is not set for a source node, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class AirRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirRenderAction() 
  {
    super("AirRender", new VersionID("2.0.0"), "Temerity", 
	  "The AIR RenderMan compliant renderer.");

    {
      ActionParam param = 
	new DoubleActionParam
	("Gamma", 
	 "Sets the default display gamma.", 
	 1.0);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("SpiralBuckets", 
	 "Whether to render buckets in a spiral order.",
	 false);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new BooleanActionParam
	("GenerateAnimation", 
	 "Whether to generate frame blocks for animation time-varying RIBs.", 
	 false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("FrameBlocks", 
	 "The number of animation frame blocks to generate.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("StartTime", 
	 "The animation start time.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("EndTime", 
	 "The animation end time.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("ShutterOpen", 
	 "The fraction of the animation frame time that the shutter is open.", 
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Gamma");
      layout.addEntry("SpiralBuckets");   

      {
	LayoutGroup anim = new LayoutGroup
	  ("Animation", "Animation generation controls.", false);
	anim.addEntry("GenerateAnimation");
	anim.addSeparator();
	anim.addEntry("FrameBlocks");
	anim.addEntry("StartTime");
	anim.addEntry("EndTime");
	anim.addEntry("ShutterOpen");

	layout.addSubGroup(anim);
      }

      setSingleLayout(layout);   
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
	new IntegerActionParam
	("Order", 
	 "Processes the RIB file in this order.",
	 100);
      params.put(param.getName(), param);
    }

    return params;
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
    boolean generate = false;
    FrameRange range = null;
    TreeMap<Integer,LinkedList<File>> sourceRIBs = new TreeMap<Integer,LinkedList<File>>();
    {
      {
	Boolean tf = (Boolean) getSingleParamValue("GenerateAnimation");
	generate = ((tf != null) && tf);
      }

      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceRIBs(generate, nodeID, sname, fseq, order, sourceRIBs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceRIBs(generate, nodeID, sname, fseq, order, sourceRIBs);
	  }
	}
      }

      if(sourceRIBs.isEmpty()) 
	throw new PipelineException
	  ("No source RIB files where specified using the per-source Order parameter!");

      {
	FileSeq fseq = agenda.getPrimaryTarget();
	range = fseq.getFrameRange();
      }
    }

    ArrayList<String> args = new ArrayList<String>(); 
    args.add("-stats");
    
    Boolean spiral = (Boolean) getSingleParamValue("SpiralBuckets");
    if((spiral != null) && spiral) 
      args.add("-spiral");
    
    Double gamma = (Double) getSingleParamValue("Gamma");
    if(gamma != null) {
      args.add("-g");
      args.add(gamma.toString());
    }

    if(generate) {
      Integer frames = (Integer) getSingleParamValue("FrameBlocks");
      if(frames == null) 
	throw new PipelineException
	  ("The number of Frame Blocks to generate must be specified!");	

      args.add("-anim");
      args.add(frames.toString());

      Double startTime = (Double) getSingleParamValue("StartTime");
      Double endTime = (Double) getSingleParamValue("EndTime");
      Double shutter = (Double) getSingleParamValue("ShutterOpen");

      if((startTime != null) || (endTime != null)) {
	if(startTime == null) 
	  throw new PipelineException
	    ("A Start Time must be specified if an End Time is given!");

	if(endTime == null) 
	  throw new PipelineException
	    ("A End Time must be specified if an Start Time is given!");
	
	args.add(startTime.toString());
	args.add(endTime.toString());

	if(shutter != null) 
	  args.add(shutter.toString());
      }
      else if(shutter != null) {
	throw new PipelineException
	  ("Start/End Times must be specified if the Shutter Open fraction is given!");
      }
    }

    /* create the process to run the action */ 
    try {
      
      if(range != null) {
	args.add("-frames");
	args.add(String.valueOf(range.getStart()));
	args.add(String.valueOf(range.getEnd()));
	
	args.add("-step");
	args.add(String.valueOf(range.getBy()));
      }

      for(LinkedList<File> ribs : sourceRIBs.values()) 
	for(File file : ribs) 
	  args.add(file.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "air", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * A helper method for generating source RIB filenames.
   */ 
  private void 
  addSourceRIBs
  (
   boolean generate, 
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,LinkedList<File>> sourceRIBs
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("rib"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain RIB files!");
    
    if(generate && !fseq.isSingle()) 
      throw new PipelineException
	("When the Generate Animation parameter is set, the source node (" + sname + ") with " + 
	 "per-source Order parameter must have a primary file sequence (" + fseq + ") which " +
	 "contains a single RIB file!");

    NodeID snodeID = new NodeID(nodeID, sname);
    for(File file : fseq.getFiles()) {
      File source = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + file);
    
      LinkedList<File> ribs = sourceRIBs.get(order);
      if(ribs == null) {
	ribs = new LinkedList<File>();
	sourceRIBs.put(order, ribs);
      }
      
      ribs.add(source);
    }      
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4397681861395834930L;

}


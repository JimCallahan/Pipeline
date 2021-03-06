// $Id: AirRenderAction.java,v 1.1 2007/06/17 15:34:38 jim Exp $

package us.temerity.pipeline.plugin.AirRenderAction.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   R E N D E R   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The AIR RenderMan compliant renderer. <P> 
 * 
 * The RIB file (.rib) which is the single member of the primary file sequence of each 
 * source node which sets the Order per-source parameter will be processed.  One or more 
 * images, depthmaps or deep shadow maps may be generated in one rendering pass. <P> 
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
    super("AirRender", new VersionID("1.0.0"), "Temerity", 
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
    FrameRange range = null;
    TreeMap<Integer,LinkedList<File>> sourceRIBs = new TreeMap<Integer,LinkedList<File>>();
    {
      for(String sname : getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sname, "Order");
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow an per-source Order parameter exists for a node (" + sname + ") " + 
	     "which was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !suffix.equals("rib"))
	  throw new PipelineException
	    ("The source node (" + sname + ") with per-source Order parameter must have a " + 
	     "a primary file sequence (" + fseq + ") which contains a single RIB file!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	File source = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));

	LinkedList<File> ribs = sourceRIBs.get(order);
	if(ribs == null) {
	  ribs = new LinkedList<File>();
	  sourceRIBs.put(order, ribs);
	}
	
	ribs.add(source);
      }
      
      if(sourceRIBs.isEmpty()) 
	throw new PipelineException
	  ("No source RIB files where specified!");

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

    Boolean anim = (Boolean) getSingleParamValue("GenerateAnimation");
    if((anim != null) && anim) {
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7893944435360298080L;

}


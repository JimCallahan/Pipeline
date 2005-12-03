// $Id: AirLightdomeAction.java,v 1.1 2005/12/03 23:09:46 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   L I G H T D O M E   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates RIB file for creating a set of lights, shadow maps, and occlusion maps for use 
 * in simulating dome or sky lighting. <P> 
 * 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>lightdome</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 *
 * <DIV style="margin-left: 40px;">
 *   Dome Lights <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to create a "domelights.lis" RIB file containing the light source definitions. 
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Geometry RIB <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the RIB file specifying the geometry used when generating
 *     shadow, depth or occlusion maps.  This parameter is required when generating one or more
 *     of these types of maps.<BR>
 *   </DIV> <BR>
 * 
 *   Shadow Maps <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to create a "makesmaps.rib" file for rendering shadow maps.  The generated shadow 
 *     maps will be named "shadow.@@@.sm".
 *   </DIV> <BR>
 * 
 *   Depth Maps <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to create a "makezmaps.rib" file for rendering Z-depth maps. The generated depth 
 *     maps will be named "shadow.@@@.z".
 *   </DIV> <BR>
 * 
 *   Occlusion Map <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to create a "makeomap.rib" file for rendering an occlusion map. The generated  
 *     multichannel occlusion map will be named "occlusion.sm".
 *   </DIV> <BR>
 * 
 *   <I>Light Placement</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Light Count <BR>
 *     <DIV style="margin-left: 40px;">
 *       The number of light sources to distribute over the sphere.  Must be in the [1,256] range.
 *     </DIV> <BR>
 *  
 *     Random Seed <BR>
 *     <DIV style="margin-left: 40px;">
 *       The seed used to initialize the random placement of light sources. 
 *     </DIV> <BR>
 *  
 *     Dome Radius <BR>
 *     <DIV style="margin-left: 40px;">
 *       The radius of the sphere over which the light sources are distributed.
 *     </DIV> <BR>
 *  
 *     Map Size <BR>
 *     <DIV style="margin-left: 40px;">
 *       The width/height (in pixels) of the generated shadow, depth and/or occlusion maps. 
 *     </DIV> <BR>
 *   </DIV> 
 * </DIV>
 */
public
class AirLightdomeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirLightdomeAction() 
  {
    super("AirLightdome", new VersionID("2.0.0"), "Temerity", 
	  "Generates RIB file for creating a set of lights, shadow maps, and occlusion maps " + 
	  "for use in simulating dome or sky lighting.");

    {
      ActionParam param = 
	new BooleanActionParam
	("DomeLights", 
	 "Whether to create a \"domelights.lis\" RIB file containing the light source definitions.",
	 true);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new LinkActionParam
	("GeometryRIB",
	 "The source node which contains the RIB file specifying the geometry used when " + 
	 "generating shadow, depth or occlusion maps.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("ShadowMaps", 
	 "Whether to create a \"makesmaps.rib\" file for rendering shadow maps.", 
	 true);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new BooleanActionParam
	("DepthMaps", 
	 "Whether to create a \"makezmaps.rib\" file for rendering Z-depth maps.", 
	 false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("OcclusionMap", 
	 "Whether to create a \"makeomap.rib\" file for rendering an occlusion map.", 
	 false);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new IntegerActionParam
	("LightCount", 
	 "The number of light sources to distribute over the sphere.",
	 64);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("RandomSeed", 
	 "The seed used to initialize the random placement of light sources.", 
	 1);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("DomeRadius", 
	 "The radius of the sphere over which the light sources are distributed.", 
	 100.0);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("MapSize", 
	 "The width/height (in pixels) of the generated shadow, depth and/or occlusion maps.", 
	 256);
      addSingleParam(param);
    }


    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("DomeLights");
      layout.addSeparator();
      layout.addEntry("GeometryRIB"); 
      layout.addEntry("ShadowMaps"); 
      layout.addEntry("DepthMaps"); 
      layout.addEntry("OcclusionMap");   

      {
	LayoutGroup lp = new LayoutGroup
	  ("Light Placement", "Light source distribution controls.", false);
	lp.addEntry("LightCount");
	lp.addEntry("RandomSeed");
	lp.addEntry("DomeRadius");
	lp.addEntry("MapSize");

	layout.addSubGroup(lp);
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
    boolean lights = false;
    boolean shadow = false;
    boolean depth = false;
    boolean occlusion = false;
    File geometry = null;
    {
      TreeSet<File> required = new TreeSet<File>(); 
      {
	Boolean tf = (Boolean) getSingleParamValue("DomeLights");
	if((tf != null) && tf) {
	  lights = true;
	  required.add(new File("domelights.lis"));	  
	}
      }

      {
	Boolean tf = (Boolean) getSingleParamValue("ShadowMaps");
	if((tf != null) && tf) {
	  shadow = true;
	  required.add(new File("makesmaps.rib"));	  
	}
      }

      {
	Boolean tf = (Boolean) getSingleParamValue("DepthMaps");
	if((tf != null) && tf) {
	  depth = true;
	  required.add(new File("makezmaps.rib"));	  
	}
      }
      
      {
	Boolean tf = (Boolean) getSingleParamValue("OcclusionMap");
	if((tf != null) && tf) {
	  occlusion = true;
	  required.add(new File("makeomap.rib"));	  
	}
      }
	
      if(!lights && !shadow && !depth && !occlusion) 
	throw new PipelineException
	  ("You must select at least one of Dome Lights, Shadow Maps, Depth Maps or Occlusion " +
	   "Map for generation.");

      TreeSet<File> targets = new TreeSet<File>(); 
      validateTarget(agenda.getPrimaryTarget(), targets);
      for(FileSeq fseq : agenda.getSecondaryTargets())
	validateTarget(fseq, targets);
	
      for(File file : required) {
	if(!targets.contains(file)) 
	  throw new PipelineException
	    ("The parameters of the AirLightdome action specify that a file " + 
	     "(" + file + ") will be generated which is not currently a member of any of the " + 
	     "target file sequences!");
      }

      for(File file : targets) {
	if(!required.contains(file)) 
	  throw new PipelineException
	    ("The current target file sequences contain a file (" + file + ") which will not be " +
	     "generated based on the parameters of the Air Lightdome action!");
      }

      if(shadow || depth || occlusion) {
	String rib = (String) getSingleParamValue("GeometryRIB"); 
	if(rib == null) 
	  throw new PipelineException
	    ("The GemetryRIB node must be specified if Shadow Maps, Depth Maps or Occlusion " +
	     "Map is set!");

	FileSeq fseq = agenda.getPrimarySource(rib);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Geometry RIB node (" + rib + ") was not one of the source " + 
	     "nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("rib"))
	  throw new PipelineException
	    ("The AirLightdome Action requires that the source node specified by the Geometry " +
	       "RIB parameter (" + rib + ") must have a single RIB file as its primary file " + 
	     "sequence!");
	
	geometry = fseq.getFile(0);
      }
    }

    ArrayList<String> args = new ArrayList<String>(); 

    {
      Integer nlts = (Integer) getSingleParamValue("LightCount");
      if(nlts == null) 
	throw new PipelineException	
	  ("The LightCount must be specified!"); 
      if((nlts < 1) || (nlts > 256)) 
	throw new PipelineException
	  ("The value of LightCount (" + nlts + ") was illegal!  The number of lights must be " + 
	   "in the [1-256] range.");
      
      args.add("-n");
      args.add(nlts.toString());
    }

    {
      Integer seed = (Integer) getSingleParamValue("RandomSeed");
      if(seed != null) {
	args.add("-seed");
	args.add(seed.toString());
      }
    }

    {
      Double radius = (Double) getSingleParamValue("DomeRadius");
      if(radius == null) 
	throw new PipelineException
	  ("The DomeRadius must be specified!"); 
      if(radius <= 0.0)
	throw new PipelineException
	  ("The value of DomeRadius (" + radius + ") must be positive.");
      
      args.add("-radius");
      args.add(radius.toString());
    }

    {
      Integer size = (Integer) getSingleParamValue("MapSize");
      if(size != null) {
	if(size <= 0) 
	  throw new PipelineException
	    ("The value of MapSize (" + size + ") must be positive.");
	  
	args.add("-mapsize");
	args.add(size.toString());
      }
    }

    if(geometry != null) {
      args.add("-scene");
      args.add(geometry.toString());
    }
    
    if(lights) 
      args.add("-lights");

    if(shadow) 
      args.add("-smaps");

    if(depth) 
      args.add("-zmaps");

    if(occlusion) 
      args.add("-omap");
      
    /* create the process to run the action */ 
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "lightdome", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * A helper method for validating the target file sequences.
   */ 
  private void 
  validateTarget
  (
   FileSeq fseq, 
   TreeSet<File> targets
   ) 
    throws PipelineException   
  {
    if(!fseq.isSingle()) 
      throw new PipelineException
	("The AirLightdome action requires that all target file sequences contain only a " + 
	 "single file.  The file sequence ("  + fseq + ") is a not valid.");

    for(File file : fseq.getFiles()) 
      targets.add(file);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5284590058409660436L;

}


// $Id: PRBrickMakeAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PRBrickMakeAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P R   B R I C K   M A K E   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a Pixar brick map from 3D point cloud files. <P> 
 * 
 * All of the 3D point cloud file (.ptc) dependencies of the target image which set the Order 
 * per-source sequence parameter will be included in the generated brick map. <P> 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan ProServer</A>
 * documentation for details about <B>brickmake</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maximum Error <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the amount of error that can be tolerated in the brick map.
 *   </DIV> <BR>
 * 
 *   Radius Scale <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies a scaling factor applied to all point radii.
 *   </DIV> <BR>
 * 
 *   Maximum Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the maximum subdivision depth of the brick map.
 *   </DIV> <BR>
 * 
 *   Ignore Normals <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether all data should be stored in the brick map as if their normal was (0,0,0).
 *   </DIV> <BR>
 * </DIV>
 *
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain 3D point cloud files. 
 *     This parameter determines the order in which the input point cloud files are processed. 
 *     If this parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class PRBrickMakeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PRBrickMakeAction() 
  {
    super("PRBrickMake", new VersionID("2.0.0"), "Temerity",
	  "Creates a Pixar brick map from 3D point cloud files.");
    
    {
      ActionParam param = 
	new DoubleActionParam
	("MaximumError",
	 "Specifies the amount of error that can be tolerated in the brick map.", 
	 0.002);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("RadiusScale",
	 "Specifies a scaling factor applied to all point radii.", 
	 1.0);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	("MaximumDepth",
	 "Specifies the maximum subdivision depth of the brick map.", 
	 15);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("IgnoreNormals",
	 "Whether all data should be stored in the brick map as if their normal was (0,0,0).",
	 false);
      addSingleParam(param);
    }
    
    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("MaximumError");
      layout.addEntry("RadiusScale");
      layout.addEntry("MaximumDepth");
      layout.addEntry("IgnoreNormals");

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
	 "Process the 3D point cloud file in this order.",
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
    File target = null;
    TreeMap<Integer,LinkedList<File>> sourcePTCs = new TreeMap<Integer,LinkedList<File>>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourcePTCs(nodeID, sname, fseq, order, sourcePTCs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourcePTCs(nodeID, sname, fseq, order, sourcePTCs);
	  }
	}
      }

      if(sourcePTCs.isEmpty()) 
	throw new PipelineException
	  ("No source 3D point cloud (.ptc) files where specified using the per-source Order " +
	   "parameter!");
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("bkm"))
	  throw new PipelineException
	    ("The PRBrickMake Action requires that primary target file sequence must " + 
	     "be a single brick map (.bkm) file!");
	target = fseq.getFile(0);
      }      
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>(); 
      
      {
	Double error = (Double) getSingleParamValue("MaximumError");
	if((error == null) || (error <= 0.0)) 
	  throw new PipelineException
	    ("The value of MaximumError (" + error + ") must be positive!");
	args.add("-maxerror");
	args.add(error.toString());
      }
      
      {
	Double scale = (Double) getSingleParamValue("RadiusScale");
	if((scale == null) || (scale <= 0.0)) 
	  throw new PipelineException
	    ("The value of RadiusScale (" + scale + ") must be positive!");
	args.add("-radiusscale");
	args.add(scale.toString());
      }
      
      {
	Integer depth = (Integer) getSingleParamValue("MaximumDepth");
	if((depth == null) || (depth <= 0)) 
	  throw new PipelineException
	    ("The value of MaximumDepth (" + depth + ") must be positive!");
	args.add("-maxdepth");
	args.add(depth.toString());
      }
      
      {
	Boolean ignore = (Boolean) getSingleParamValue("IgnoreNormals");  
	if((ignore != null) && ignore) {
	  args.add("-ignorenormals");
	  args.add("1");
	}
      }

      args.add("-progress");
      args.add("2");

      for(LinkedList<File> ptcs : sourcePTCs.values())
	for(File file : ptcs) 
	  args.add(file.getPath());

      args.add(target.getName());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "brickmake", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * A helper method for generating source PTC filenames.
   */ 
  private void 
  addSourcePTCs
  (
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,LinkedList<File>> sourcePTCs
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("ptc"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain 3D point cloud (.ptc) files!");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(File file : fseq.getFiles()) {
      File source = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + file);
    
      LinkedList<File> ptcs = sourcePTCs.get(order);
      if(ptcs == null) {
	ptcs = new LinkedList<File>();
	sourcePTCs.put(order, ptcs);
      }
      
      ptcs.add(source);
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4696711314669440530L;

}


// $Id: PRSubSurfDiffBAction.java,v 1.1 2005/12/09 09:25:12 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P R   S U B   S U R F   D I F F   B   A C T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an irradiance 3D point cloud using a scattering, absorption and index of 
 * refraction model of subsurface diffusion of existing irradiance point clouds. <P> 
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
 *   <I>Scattering</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     RedScattering <BR>
 *     <DIV style="margin-left: 40px;">
 *       The red reduced scattering coefficients of the material.
 *     </DIV> <BR>
 * 
 *     GreenScattering <BR>
 *     <DIV style="margin-left: 40px;">
 *       The green reduced scattering coefficients of the material.
 *     </DIV> <BR>
 * 
 *     BlueScattering <BR>
 *     <DIV style="margin-left: 40px;">
 *       The blue reduced scattering coefficients of the material.
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   <I>Absorption</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     RedAbsorption <BR>
 *     <DIV style="margin-left: 40px;">
 *       The red absorption coefficients of the material.
 *     </DIV> <BR>
 * 
 *     GreenAbsorption <BR>
 *     <DIV style="margin-left: 40px;">
 *       The green absorption coefficients of the material.
 *     </DIV> <BR>
 * 
 *     BlueAbsorption <BR>
 *     <DIV style="margin-left: 40px;">
 *       The blue absorption coefficients of the material.
 *     </DIV> <BR>
 *   </DIV> <BR>
 * 
 *   Index Of Refraction <BR>
 *   <DIV style="margin-left: 40px;">
 *     The index of refraction of the material.
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
class PRSubSurfDiffBAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PRSubSurfDiffBAction() 
  {
    super("PRSubSurfDiffB", new VersionID("2.0.0"), "Temerity",
	  "Generates an irradiance 3D point cloud using a " + 
	  "scattering, absorption and index of refraction model of " + 
	  "subsurface diffusion of existing irradiance point clouds.");
    
    {
      ActionParam param = 
	new DoubleActionParam
	("RedScattering",
	 "The red reduced scattering coefficient of the material.", 
	 2.19);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("GreenScattering",
	 "The green reduced scattering coefficient of the material.", 
	 2.62);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("BlueScattering",
	 "The blue reduced scattering coefficient of the material.", 
	 3.00);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new DoubleActionParam
	("RedAbsorption",
	 "The red absorption coefficient of the material.", 
	 0.0021);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("GreenAbsorption",
	 "The green absorption coefficient of the material.", 
	 0.0041);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("BlueAbsorption",
	 "The blue absorption coefficient of the material.", 
	 0.0071);
      addSingleParam(param);
    }
    
    
    {
      ActionParam param = 
	new DoubleActionParam
	("IndexOfRefraction",
	 "The index of refraction of the material.", 
	 1.5);
      addSingleParam(param);
    }
    
    {  
      LayoutGroup layout = new LayoutGroup(true);  
      layout.addEntry("IndexOfRefraction");

      { 
	LayoutGroup sc = new LayoutGroup
	  ("Scattering", "The reduced scattering coefficients of the material.", true);
	sc.addEntry("RedScattering");
	sc.addEntry("GreenScattering");
	sc.addEntry("BlueScattering");
	
	layout.addSubGroup(sc);
      }

      { 
	LayoutGroup sc = new LayoutGroup
	  ("Absorption", "The absorption coefficients of the material.", true);
	sc.addEntry("RedAbsorption");
	sc.addEntry("GreenAbsorption");
	sc.addEntry("BlueAbsorption");
	
	layout.addSubGroup(sc);
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
	if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("ptc"))
	  throw new PipelineException
	    ("The PRSubSurfDiffB Action requires that primary target file sequence must " + 
	     "be a single 3D point cloud (.ptc) file!");
	target = fseq.getFile(0);
      }      
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>(); 
      
      args.add("-filter");
      args.add("ssdiffusion");

      {      
	Double red = (Double) getSingleParamValue("RedScattering");
	if(red == null) 
	  throw new PipelineException
	    ("The value of RedScattering (" + red + ") must be specified!");
     
	Double green = (Double) getSingleParamValue("GreenScattering");
	if(green == null)
	  throw new PipelineException
	    ("The value of GreenScattering (" + green + ") must be specified!");
     
	Double blue = (Double) getSingleParamValue("BlueScattering");
	if(blue == null)
	  throw new PipelineException
	    ("The value of BlueScattering (" + blue + ") must be specified!");

	args.add("-scattering");
	args.add(red.toString());
	args.add(green.toString());
	args.add(blue.toString());
      }
      
      {      
	Double red = (Double) getSingleParamValue("RedAbsorption");
	if(red == null) 
	  throw new PipelineException
	    ("The value of RedAbsorption (" + red + ") must be specified!");
     
	Double green = (Double) getSingleParamValue("GreenAbsorption");
	if(green == null)
	  throw new PipelineException
	    ("The value of GreenAbsorption (" + green + ") must be specified!");
     
	Double blue = (Double) getSingleParamValue("BlueAbsorption");
	if(blue == null) 
	  throw new PipelineException
	    ("The value of BlueAbsorption (" + blue + ") must be specified!");

	args.add("-absorption");
	args.add(red.toString());
	args.add(green.toString());
	args.add(blue.toString());
      }
      
      {      
	Double iof = (Double) getSingleParamValue("IndexOfRefraction"); 
	if(iof == null)
	  throw new PipelineException
	    ("The value of Index Of Refraction (" + iof + ") must be specified!");
     
	args.add("-ior");
	args.add(iof.toString());
      }

      args.add("-progress");
      args.add("2");

      for(LinkedList<File> ptcs : sourcePTCs.values())
	for(File file : ptcs) 
	  args.add(file.getPath());

      args.add(target.getName());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "ptfilter", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 892381897846546231L;

}


// $Id: PxRenderAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PxRenderAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P X   R E N D E R   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * The Pixie RenderMan compliant renderer. <P> 
 * 
 * All of the RIB file (.rib) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed.  The frame range rendered will be limited by frame 
 * numbers of the target images.  In most cases, an Execution Method of (Parallel) and a Batch Size 
 * of (1) should be used with this action so that each image frame is rendered by a seperate 
 * invocation of render(1) which is only passed the RIBs required for the frame being rendered. 
 * It is also possible to render multi-frame RIBs or even multiple single frame RIBs at one time 
 * by using a larger Batch Size.  Depending on the RIBs processed, one or more images, depthmaps 
 * or deep shadow maps may be generated in one rendering pass. <P> 
 * 
 * See the <A href="http://pixie.sourceforge.net/">Pixie</A> documentation for details 
 * about <B>rndr</B>(1). <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain RIB files. This 
 *     parameter determines the order in which the input RIB files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class PxRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PxRenderAction() 
  {
    super("PxRender", new VersionID("2.0.0"), "Temerity",
	  "The Pixie RenderMan compliant renderer.");

    underDevelopment();
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
	 "Process the RIB file in this order.",
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
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceRIBs(nodeID, sname, fseq, order, sourceRIBs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceRIBs(nodeID, sname, fseq, order, sourceRIBs);
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

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>(); 

      if(range != null) {
	args.add("-f");
	args.add(range.getStart() + ":" + range.getBy() + ":" + range.getEnd());
      }

      args.add("-stats");

      for(LinkedList<File> ribs : sourceRIBs.values()) 
	for(File file : ribs) 
	  args.add(file.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "rndr", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 3998363691479413920L;

}


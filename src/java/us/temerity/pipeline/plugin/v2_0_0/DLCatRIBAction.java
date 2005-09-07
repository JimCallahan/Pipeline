// $Id: DLCatRIBAction.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   C A T   R I B   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Processes RIB files to generate binary, compressed and/or evaluated RIBs. <P> 
 * 
 * The RIB file (.rib) which is the single member of the primary file sequence of each 
 * source node which sets the Order per-source parameter will be processed to generate the 
 * output RIB which is he single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for
 * <A href="http://www.3delight.com/ZDoc/3delight_10.html"><B>renderdl</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   OutputFormat <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output RIB file: <BR>
 *     <UL>
 *       <LI>ASCII - Generates plain-text RIB.
 *       <LI>Binary - Generates binary RIB. 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output RIB file:<BR>
 *     <UL>
 *       <LI>None - Uncompressed. 
 *       <LI>GZip - Use gzip(1) to compress the output RIB.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Evaluate Procedurals <BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to evaluate all procedurals encountered in the input RIBs.
 *   </DIV> <BR>
 * </DIV> <P> 
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
class DLCatRIBAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLCatRIBAction() 
  {
    super("DLCatRIB", new VersionID("2.0.0"), "Temerity", 
	  "Processes RIB files to generate binary, compressed and/or evaluated RIBs.");
    
    {
      ActionParam param = 
	new BooleanActionParam
	("EvaluateProcedurals", 
	 "Whether to evaluate all procedurals encountered in the input RIBs.",
	 false);
      addSingleParam(param);
    } 

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("ASCII");
      choices.add("Binary");

      ActionParam param = 
	new EnumActionParam
	("OutputFormat", 
	 "The format of the output RIB file.",
	 "ASCII", choices);
      addSingleParam(param);
    } 

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Gzip");

      ActionParam param = 
	new EnumActionParam
	("Compression", 
	 "The compression method to use for the output RIB file.",
	 "None", choices);
      addSingleParam(param);
    } 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("EvaluateProcedurals");
      layout.addSeparator();
      layout.addEntry("OutputFormat");
      layout.addEntry("Compression");   

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
    File target = null; 
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
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !suffix.equals("rib")) 
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "RIB file (.rib) file.");
	  
	target = fseq.getFile(0);
      }
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n" + 
		"renderdl -noinit -catrib");

      Boolean eval = (Boolean) getSingleParamValue("EvaluateProcedurals");
      if((eval != null) && eval)
	out.write(" -callprocedurals");
      
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("OutputFormat");
	switch(param.getIndex()) {
	case 0:
	  break;
	  
	case 1:
	  out.write(" -binary");
	  break;

	default:
	  throw new PipelineException
	    ("Illegal OutputFormat value!");
	}
      }
    
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("Compression");
	switch(param.getIndex()) {
	case 0:
	  break;
	  
	case 1:
	  out.write(" -gzip");
	break;
	
	default:
	  throw new PipelineException
	    ("Illegal Compression value!");
	}
      }
      
      for(LinkedList<File> ribs : sourceRIBs.values()) 
	for(File file : ribs) 
	  out.write(" " + file);
      
      out.write(" > " + target + "\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -4188624756020185019L;

}


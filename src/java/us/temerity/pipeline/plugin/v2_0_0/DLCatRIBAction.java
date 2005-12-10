// $Id: DLCatRIBAction.java,v 1.4 2005/12/10 15:37:03 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   C A T   R I B   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Concatenates and converts RIB files. <P> 
 * 
 * All of the RIB file (.rib) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed to produce the target RIB file(s). <P> 
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
 *     Each source node sequence which sets this parameter should contain RIB files. This 
 *     parameter determines the order in which the input RIB files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
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
	  "Concatenates and converts RIB files.");
    
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
	 "The order in which the input RIB files are processed.", 
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
    TreeMap<Integer,ArrayList<File>> sourceRIBs = new TreeMap<Integer,ArrayList<File>>();
    {
      int numFrames = agenda.getPrimaryTarget().numFrames();
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceRIBs(nodeID, numFrames, sname, fseq, order, sourceRIBs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceRIBs(nodeID, numFrames, sname, fseq, order, sourceRIBs);
	  }
	}
      }

      if(sourceRIBs.isEmpty()) 
	throw new PipelineException
	  ("No source RIB files where specified using the per-source Order parameter!");
    }

    String options = null;
    {
      StringBuffer buf = new StringBuffer(); 

      Boolean eval = (Boolean) getSingleParamValue("EvaluateProcedurals");
      if((eval != null) && eval)
	buf.append(" -callprocedurals");
      
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("OutputFormat");
	switch(param.getIndex()) {
	case 0:
	  break;
	  
	case 1:
	  buf.append(" -binary");
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
	  buf.append(" -gzip");
	break;
	
	default:
	  throw new PipelineException
	    ("Illegal Compression value!");
	}
      }

      options = buf.toString();
    }


    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");

      int wk=0;
      for(File target : agenda.getPrimaryTarget().getFiles()) {
	out.write("renderdl -noinit -catrib" + options);

	for(ArrayList<File> ribs : sourceRIBs.values()) {
	  if(ribs.size() == 1)
	    out.write(" " + ribs.get(0));
	  else 
	    out.write(" " + ribs.get(wk));
	}
	
	out.write(" > " + target + "\n");
	wk++;
      }
      
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

  /**
   * A helper method for generating source RIB filenames.
   */ 
  private void 
  addSourceRIBs
  (
   NodeID nodeID, 
   int numFrames, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,ArrayList<File>> sourceRIBs
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("rib"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain RIB files!");

    if((fseq.numFrames() != 1) && (fseq.numFrames() != numFrames))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have the contain the same number of RIB files as the target sequence or exactly " + 
	 "one RIB file.");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(File file : fseq.getFiles()) {
      File source = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + file);
    
      ArrayList<File> ribs = sourceRIBs.get(order);
      if(ribs == null) {
	ribs = new ArrayList<File>();
	sourceRIBs.put(order, ribs);
      }
      
      ribs.add(source);
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4188624756020185019L;

}


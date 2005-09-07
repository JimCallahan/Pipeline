// $Id: HfsScriptAction.java,v 1.2 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_1_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   S C R I P T   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Loads or executes set of Houdini scenes, OTLs and command scripts. <P> 
 * 
 * This general purpose action provides a way of performing arbitrary operations with
 * Houdini using hscript(1).  The per-source Order parameter controls the order the scenes,
 * OTLs and command scripts are loaded or executed.  The target file of this action should
 * be created as a side-effect of one of the executed command scripts.  This target file 
 * (or files) could be Houdini scenes, OTLs, channel data, images or any other type of 
 * file data Houdini is capable of generating. <P> 
 * 
 * See the <A href="http://www.sidefx.com"><B>Houdini</B></A> documentation for details about
 * hscript(1). <P> 
 *  
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hscript(1).  
 *     Normally, hscript(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a Houdini scene file, OTL or
 *     command script sole member of the selected file sequence.  This parameter specifies 
 *     the order in which will these files will be loaded (or executed). If this parameter 
 *     is not set for a source node, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsScriptAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsScriptAction() 
  {
    super("HfsScript", new VersionID("1.1.0"), "Temerity", 
	  "Loads or executes set of Houdini scenes, OTLs and command scripts.");

    {
      ActionParam param = 
	new BooleanActionParam
	("UseGraphicalLicense",
	 "Whether to use an interactive graphical Houdini license when running hscript(1).",
	 false);
      addSingleParam(param);
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
	 "Loads the Houdini scene, OTL or command file in this order.", 
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
    ArrayList<File> sourceFiles = new ArrayList<File>();
    {
      DoubleMap<Integer,String,TreeSet<FileSeq>> sources = 
	new DoubleMap<Integer,String,TreeSet<FileSeq>>();

      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceSeq(order, sname, fseq, sources);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = 
	      (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceSeq(order, sname, fseq, sources);
	  }
	}
      }

      for(Integer order : sources.keySet()) {
	for(String sname : sources.get(order).keySet()) {
	  for(FileSeq fseq : sources.get(order).get(sname)) {
	    NodeID snodeID = new NodeID(nodeID, sname);
	    File sfile = new File(PackageInfo.sProdDir,
				  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	    sourceFiles.add(sfile);
	  }
	}
      } 
    }

    /* license type */ 
    String licopt = " -R";
    {
      Boolean tf = (Boolean) getSingleParamValue("UseGraphicalLicense"); 
      if((tf != null) && tf)
	licopt = "";
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n" +
		"hscript" + licopt);      
      for(File file : sourceFiles) 
	out.write(" " + file);
      out.write("\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given source file sequence to the to be loaded set.
   */ 
  private void 
  addSourceSeq
  (
   Integer order, 
   String sname, 
   FileSeq fseq, 
   DoubleMap<Integer,String,TreeSet<FileSeq>> sources
  ) 
    throws PipelineException
  {
    if(order != null) {
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || 
	 !(suffix.equals("hip") || suffix.equals("otl") || suffix.equals("cmd")))
	throw new PipelineException
	  ("The file sequence (" + fseq + ") of source node (" + sname + ") " + 
	   "does not contain a single Houdini scene (.hip), Operator Type Library " + 
	   "(.otl) or Command Script (.cmd) file!");
      
      TreeSet<FileSeq> fseqs = sources.get(order, sname);
      if(fseqs == null) {
	fseqs = new TreeSet<FileSeq>();
	sources.put(order, sname, fseqs);
      }
      
      fseqs.add(fseq);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2982916981706767601L;

}


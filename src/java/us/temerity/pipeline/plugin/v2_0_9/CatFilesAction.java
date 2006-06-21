// $Id: CatFilesAction.java,v 1.1 2006/06/21 04:16:10 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C A T   F I L E S   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Concatenates generic files. <P> 
 * 
 * All of the dependencies of the target node which set the Order per-source sequence 
 * parameter will be processed to produce the files associated with the target node. <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     This parameter determines the order in which the input source files are processed. 
 *     If this parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class CatFilesAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CatFilesAction() 
  {
    super("CatFiles", new VersionID("2.0.9"), "Temerity", 
	  "Concatenates generic files.");

    addSupport(OsType.MacOS); 
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
	 "The order in which the input source files are processed.", 
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
    TreeMap<Integer,ArrayList<Path>> sourcePaths = new TreeMap<Integer,ArrayList<Path>>();
    {
      int numFrames = agenda.getPrimaryTarget().numFrames();
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourcePaths(nodeID, numFrames, sname, fseq, order, sourcePaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourcePaths(nodeID, numFrames, sname, fseq, order, sourcePaths);
	  }
	}
      }

      if(sourcePaths.isEmpty()) 
	throw new PipelineException
	  ("No source files where specified using the per-source Order parameter!");
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");

      int wk=0;
      for(Path target : agenda.getPrimaryTarget().getPaths()) {
	out.write("cat"); 

	for(ArrayList<Path> paths : sourcePaths.values()) {
	  if(paths.size() == 1)
	    out.write(" " + paths.get(0).toOsString());
	  else 
	    out.write(" " + paths.get(wk).toOsString());
	}

	out.write(" > " + target.toOsString() + "\n");

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
  addSourcePaths
  (
   NodeID nodeID, 
   int numFrames, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,ArrayList<Path>> sourcePaths
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((fseq.numFrames() != 1) && (fseq.numFrames() != numFrames))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have the contain the same number of files as the target sequence or " + 
	 "exactly one file.");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(Path path : fseq.getPaths()) {
      Path source = new Path(PackageInfo.sProdPath, 
			     snodeID.getWorkingParent() + "/" + path);

      ArrayList<Path> paths = sourcePaths.get(order);
      if(paths == null) {
	paths = new ArrayList<Path>();
	sourcePaths.put(order, paths);
      }
      
      paths.add(source);
    }      
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6747603135040058085L; 

}


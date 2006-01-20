// $Id: ExtractSeqAction.java,v 1.1 2006/01/20 22:56:13 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E X T R A C T   S E Q   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Replaces the primary files of the target node with copies of a selected file sequence
 * of one of the source nodes. <P> 
 * 
 * The source sequence to copy is selected by setting the Select per-source parameter for
 * the sequence to copy.  Only one sequence can be selected and it must have exactly the
 * same number of files as the target file sequence. <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Select <BR>
 *   <DIV style="margin-left: 40px;">
 *     Selects the source file sequence to copy.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class ExtractSeqAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ExtractSeqAction() 
  {
    super("ExtractSeq", new VersionID("2.0.0"), "Temerity",
	  "Replaces the primary files of the target node with copies of a selected " + 
	  "file sequence of one of the source nodes.");
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
	new BooleanActionParam
	("Select", 
	 "Selects the source file sequence to copy.",
	 true);
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
    FileSeq target = agenda.getPrimaryTarget();
    ArrayList<File> sourceFiles = new ArrayList<File>();
    {
      boolean isSelected = false;
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Boolean select = (Boolean) getSourceParamValue(sname, "Select");
	  isSelected |= addSourceFiles(nodeID, target, sname, fseq, 
				       select, isSelected, sourceFiles);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Boolean select = (Boolean) getSecondarySourceParamValue(sname, fpat, "Select");
	    isSelected |= addSourceFiles(nodeID, target, sname, fseq, 
					 select, isSelected, sourceFiles);
	  }
	}
      }

      if(!isSelected) 
	throw new PipelineException
	  ("No source input files where specified using the per-source Select parameter!");
    }
    
    /* create a temporary script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");

      int wk;
      for(wk=0; wk<target.numFrames(); wk++) 
	out.write("cp --remove-destination " + 
		  sourceFiles.get(wk) + " " + target.getFile(wk) + "\n");
      
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
   * A helper method for generating source File filenames.
   */ 
  private boolean
  addSourceFiles
  (
   NodeID nodeID, 
   FileSeq target, 
   String sname, 
   FileSeq fseq, 
   Boolean select, 
   boolean isSelected, 
   ArrayList<File> sourceFiles
  )
    throws PipelineException 
  {
    if((select != null) && select) {
      if(isSelected) 
	throw new PipelineException
	  ("Only one source file sequence can be selected!");

      if(fseq.numFrames() != target.numFrames()) 
	throw new PipelineException 
	  ("The selected file sequence (" + fseq + ") of source node (" + sname + ")" + 
	   "does not contain the same number of frames (" + target.numFrames() + ") " +
	   "as the primary file sequence (" + target + ") of the target node " + 
	   "(" + nodeID.getName() + ")!");
      
      NodeID snodeID = new NodeID(nodeID, sname);
      for(File file : fseq.getFiles()) 
	sourceFiles.add(new File(PackageInfo.sProdDir,
				 snodeID.getWorkingParent() + "/" + file));
      
      return true; 
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5797930720034973162L;

}


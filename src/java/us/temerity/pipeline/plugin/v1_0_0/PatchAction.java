// $Id: PatchAction.java,v 1.2 2004/10/14 22:38:15 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P A T C H   A C T I O N                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Applies a series of diff(1) generated patches to original file. <P>     
 * 
 * Each source node with a primary file sequence which contains a single patch file ("patch")
 * and which sets the Order per-source parameter will be applied to the original file.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Original <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node which contains the original file to patch.  The primary file sequence 
 *      of the source node must contain a single file. 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *      Determines the order in which the patches are applied to the original file. 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class PatchAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PatchAction() 
  {
    super("Patch", new VersionID("1.0.0"), 
	  "Applies a series of diff(1) generated patches to original file.");
    
    {
      BaseActionParam param = 
	new LinkActionParam
	("Original", 
	 "The original file to patch.", 
	 null);
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
  public TreeMap<String,BaseActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,BaseActionParam> params = new TreeMap<String,BaseActionParam>();
    
    {
      BaseActionParam param = 
	new IntegerActionParam
	("Order", 
	 "The order patches are applied",
	 100);
      params.put(param.getName(), param);
    }

    return params;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcess SubProcess} instance which when executed will fulfill
   * the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda.
   */
  public SubProcess
  prep
  (
   ActionAgenda agenda
  )
    throws PipelineException
  { 
    makeTargetDir(agenda);

    /* sanity checks */ 
    File origFile = null;
    TreeMap<Integer,LinkedList<File>> patchFiles = 
      new TreeMap<Integer,LinkedList<File>>();
    File targetFile = null;
    {
      /* original file */ 
      {
	File script = null; 
	String oname = (String) getSingleParamValue("Original"); 
	if(oname != null) {
	  FileSeq fseq = agenda.getPrimarySource(oname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Original node (" + oname + ") was not one of the " + 
	       "source nodes!");
	  
	  NodeID onodeID = new NodeID(agenda.getNodeID(), oname);
	  origFile = new File(PackageInfo.sProdDir,
			      onodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
	}

	if(origFile == null) 
	  throw new PipelineException
	    ("No original file was specified!");
      }

      /* patch filenames */ 
      for(String sname : getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sname, "Order");
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow an per-source Order parameter exists for a node (" + sname + ") " + 
	     "which was not one of the source nodes!");

	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !suffix.equals("patch")) 
	  throw new PipelineException
	    ("The Patch Action requires that the source node (" + sname + ") with " + 
	     "per-source Order parameter must have a patch file as its primary file " + 
	     "sequence!");
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	File file = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + fseq.getFile(0));

	LinkedList<File> patches = patchFiles.get(order);
	if(patches == null) {
	  patches = new LinkedList<File>();
	  patchFiles.put(order, patches);
	}
	
	patches.add(file);
      }

      if(patchFiles.isEmpty()) 
	throw new PipelineException
	  ("There where no patches to apply!");

      /* the target filename */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle()) 
	  throw new PipelineException
	    ("The Patch Action requires that the primary target file sequence " + 
	     "must be a single file."); 
	
	targetFile = new File(PackageInfo.sProdDir,
			      agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
      }
    }

    /* create a temporary script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");

      out.write("cat ");
      for(LinkedList<File> patches : patchFiles.values())
	for(File file : patches) 
	  out.write(file + " ");
      out.write("| patch --output=" + targetFile + " " + origFile + "\n");

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
      return new SubProcess(agenda.getNodeID().getAuthor(), 
			    getName() + "-" + agenda.getJobID(), 
			    script.getPath(), new ArrayList<String>(), 
			    agenda.getEnvironment(), agenda.getWorkingDir());
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

  private static final long serialVersionUID = 3048548050281964155L;

}


// $Id: CatSequenceAction.java,v 1.1 2007/06/17 15:34:38 jim Exp $

package us.temerity.pipeline.plugin.CatSequenceAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C A T   S E Q U E N C E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Concatenates the file sequences associated with the selected source nodes to generate
 * the files associated with the target node.  The selected source and target nodes must
 * all have the same number of primary sequences.  The corresponding sequences 
 * must have the same filename suffix.
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter will have its associated files copied to 
 *     the target node.  Sequences are concatenated in lowest to highest value of this 
 *     paramter. If this parameter is not set for a source node, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class CatSequenceAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CatSequenceAction() 
  {
    super("CatSequence", new VersionID("2.0.0"), "Temerity", 
	  "Concatenates file sequences.");
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
	 "Concatenates the file sequences in this order.",
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
    TreeMap<Integer,LinkedList<String>> sources = 
      new TreeMap<Integer,LinkedList<String>>();
    {
      /* generate the table source node names to process in the order of concatenation */ 
      for(String sname : getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sname, "Order");

	LinkedList<String> names = sources.get(order);
	if(names == null) {
	  names = new LinkedList<String>();
	  sources.put(order, names);
	}
	
	names.add(sname);
      }
    }

    /* create a shell script file */ 
    File script = createTemp(agenda, 0644, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      
      /* primary file sequences */ 
      {
	FileSeq targetSeq = agenda.getPrimaryTarget();
	String suffix = targetSeq.getFilePattern().getSuffix();
	ArrayList<File> targets = targetSeq.getFiles();
	int idx = 0;
	for(Integer order : sources.keySet()) {
	  out.write("\n# Order: " + order + "\n");

	  for(String sname : sources.get(order)) {
	    NodeID snodeID = new NodeID(nodeID, sname);
	    FileSeq fseq = agenda.getPrimarySource(sname);

	    {
	      String sfix = fseq.getFilePattern().getSuffix();
	      if(((suffix == null) && (sfix != null)) ||
		 !suffix.equals(sfix))
		throw new PipelineException
		  ("The target sequence (" + targetSeq + ") and the source " + 
		   "sequence (" + fseq + ") do not have the same filename suffix!");
	    }

	    out.write("\n# Source Node: " + sname + "\n");
	    for(File file : fseq.getFiles()) {
	      if(idx < targets.size()) {
		File source = new File(PackageInfo.sProdDir, 
				       snodeID.getWorkingParent() + "/" + file);
		File target = targets.get(idx);
		out.write("cp --force --reply=yes " + source + " " + target + "\n");
	      }
	      
	      idx++;
	    }
	  }
	}

	if(idx != targets.size()) 
	  throw new PipelineException
	    ("The number of target files (" + targets.size() + ") was not equal to the " + 
	     "total of all source files (" + idx + ")!");
      }

      /* secondary file sequences */ 
      int sidx = 0;
      for(FileSeq targetSeq : agenda.getSecondaryTargets()) {
	String suffix = targetSeq.getFilePattern().getSuffix();
	ArrayList<File> targets = targetSeq.getFiles();
	int idx = 0;
	for(Integer order : sources.keySet()) {
	  out.write("\n# Order: " + order + "\n");

	  for(String sname : sources.get(order)) {
	    NodeID snodeID = new NodeID(nodeID, sname);
	    
	    ArrayList<FileSeq> sourceSeqs = 
	      new ArrayList<FileSeq>(agenda.getSecondarySources(sname));
	    if(sidx < sourceSeqs.size()) {
	      FileSeq fseq = sourceSeqs.get(sidx);
	      {
		String sfix = fseq.getFilePattern().getSuffix();
		if(((suffix == null) && (sfix != null)) ||
		   !suffix.equals(sfix))
		  throw new PipelineException
		    ("The target sequence (" + targetSeq + ") and the source " + 
		     "sequence (" + fseq + ") do not have the same filename suffix!");
	      }
	      
	      out.write("\n# Source Node: " + sname + "\n");
	      for(File file : fseq.getFiles()) {
		if(idx < targets.size()) {
		  File source = new File(PackageInfo.sProdDir, 
					 snodeID.getWorkingParent() + "/" + file);
		  File target = targets.get(idx);
		  out.write("cp --force --reply=yes " + source + " " + target + "\n");
		}
		
		idx++;
	      }
	    }
	    else {
	      throw new PipelineException
		("The source node (" + sname + ") has no secondary sequences which " + 
		 "corresponds to the target secondary sequence (" + targetSeq + ")!");
	    }
	  }
	}

	sidx++;
      }

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary shell script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -5045032125321308944L;

}


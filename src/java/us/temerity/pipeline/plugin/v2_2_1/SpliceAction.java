// $Id: SpliceAction.java,v 1.3 2007/03/24 03:02:13 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S P L I C E   A C T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a new target primary file sequence by splicing together selected portions of 
 * of the file sequences associated with the source nodes. <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     One or more files from each source file sequence which sets this parameter will be 
 *     copied to the target file sequence.  The selected file sequences are concatenated in 
 *     lowest to highest value of this parameter.  If this parameter is not set for a file
 *     sequence, it will be silently ignored.
 *   </DIV> <BR>
 * 
 *   Start <BR>
 *   <DIV style="margin-left: 40px;">
 *     For each file sequences which sets the Order parameter above, this parameter specifies
 *     the index of the first frame in the sequence copied.  The first frame of a file 
 *     is index (0), therefore the Start index must be non-negative and less than the total 
 *     number of frames in the associated source file sequence.
 *   </DIV> 
 * 
 *   Length <BR>
 *   <DIV style="margin-left: 40px;">
 *     For each file sequences which sets the Order parameter above, this parameter determines
 *     how many total files are copied from the source to target file sequences.  The Start 
 *     index plus this Length parameter must be less than the total number of frames in the
 *     associated source file sequence.  The total of all Length parameters set must match 
 *     the number of frames in the target file sequence exactly.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * For any source file sequence, if any of the above parameters are set then all must be set.
 * In order for this Action to function properly the Execution Method of the node using this
 * plugin must be Serial.  Since by definition the target file sequence will have a different
 * number of frames than any one of the individual source nodes being spliced, the Overflow
 * Policy of the node needs to be set to Ignore or error will be generated when you try to 
 * submit jobs using this Action.  You should also take care not to set the Offset parameter
 * of the upstream links to a value other than (0) since this will change the meaning of the
 * Start per-source parameter and likely produce unwanted results.
 */
public
class SpliceAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  SpliceAction() 
  {
    super("Splice", new VersionID("2.2.1"), "Temerity", 
	  "Splices together selected portions of several file sequences to create a " + 
          "new target primary file sequence.");

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aOrder); 
      layout.add(aStart);    
      layout.add(aLength); 

      setSourceLayout(layout);
    }

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
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
	(aOrder, 
	 "The order in which the selected the file sequences are concatenated.",
	 100);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aStart, 
	 "The index of the first of the copied files from the source file sequence.", 
	 0);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aLength, 
	 "The number of files to copy from the source file sequence.", 
	 1);
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
    FileSeq targetSeq = agenda.getPrimaryTarget();
    LinkedList<Path> sourcePaths = new LinkedList<Path>();
    {
      MappedLinkedList<Integer,FileSeq> sourceSeqs = new MappedLinkedList<Integer,FileSeq>();
      for(String sname : agenda.getSourceNames()) {
        if(hasSourceParams(sname)) {
          FileSeq fseq = agenda.getPrimarySource(sname);
          Integer order  = (Integer) getSourceParamValue(sname, aOrder);
          Integer start  = (Integer) getSourceParamValue(sname, aStart);
          Integer length = (Integer) getSourceParamValue(sname, aLength);
          addSourceSeqs(targetSeq, nodeID, sname, fseq, order, start, length, sourceSeqs);
        }
	  
        for(FileSeq fseq : agenda.getSecondarySources(sname)) {
          FilePattern fpat = fseq.getFilePattern();
          if(hasSecondarySourceParams(sname, fpat)) {
            Integer order  = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            Integer start  = (Integer) getSecondarySourceParamValue(sname, fpat, aStart);
            Integer length = (Integer) getSecondarySourceParamValue(sname, fpat, aLength);
            addSourceSeqs(targetSeq, nodeID, sname, fseq, order, start, length, sourceSeqs);
          }
        }
      }
      
      for(LinkedList<FileSeq> fseqs : sourceSeqs.values()) {
        for(FileSeq fseq : fseqs) 
          sourcePaths.addAll(fseq.getPaths());
      }
      
      if(sourcePaths.isEmpty())
	throw new PipelineException
	  ("No source files where specified using the per-source Order parameter!");

      if(targetSeq.numFrames() != sourcePaths.size()) 
	throw new PipelineException
	  ("The number of files (" + targetSeq.numFrames() + ") in the target file " + 
           "sequence does not the total number of files (" + sourcePaths.size() + ") " + 
           "selected from the source file sequences!");
    }

    /* create a temporary script file to perform the copies */ 
    File script = createTempScript(agenda); 
    try {      
      FileWriter out = new FileWriter(script);
      
      String cpOpts = "--remove-destination";
      if(PackageInfo.sOsType == OsType.MacOS)
	cpOpts = "-f";

      int idx = 0;
      ArrayList<Path> targets = targetSeq.getPaths();
      for(Path spath : sourcePaths) {
        Path target = targets.get(idx);
        if(PackageInfo.sOsType == OsType.Windows) {
          Path tpath = new Path(PackageInfo.sProdPath, 
                                nodeID.getWorkingParent() + "/" + target);
          out.write("copy /y \"" + spath.toOsString() + "\" " + 
                    "\"" + tpath.toOsString() + "\"\n");
        }
        else {
          out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                    target.toOsString() + "\n");
        }
	      
        idx++;
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
    return createScriptSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating selected source file sequences.
   */
  private void 
  addSourceSeqs
  (
   FileSeq target, 
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order,
   Integer start,
   Integer length,
   MappedLinkedList<Integer,FileSeq> sourceSeqs
  )
    throws PipelineException
  {
    if((order == null) && (start == null) && (length == null)) 
      return; 

    if((order != null) && (start != null) && (length != null)) {
      String tfix = target.getFilePattern().getSuffix();
      String sfix = fseq.getFilePattern().getSuffix();
      if(((tfix == null) && (sfix != null)) ||
         ((tfix != null) && !tfix.equals(sfix)))
        throw new PipelineException
          ("The target file sequence (" + target + ") does not have the same file name " + 
           "suffix as the file sequence (" + fseq + ") of source node (" + sname + ")!"); 
      
      if(start < 0) 
        throw new PipelineException
          ("The Start index parameter (" + start + ") for file sequence (" + fseq + ") of " +
           "source node (" + sname + ") cannot be negative!");
         
      if(length < 0) 
        throw new PipelineException
          ("The Length parameter (" + length + ") for file sequence (" + fseq + ") of " +
           "source node (" + sname + ") cannot be negative!");

      if((start+length-1) > fseq.numFrames()) 
        throw new PipelineException
          ("The index of the last frame (" + (start+length) + ") selected using a Start " +
           "of (" + start + ") and a Length of (" + length + ") is outside the valid " + 
           "range of indices for the file sequence (" + fseq + ") of source node " +
           "(" + sname + ")!");
      
      NodeID snodeID = new NodeID(nodeID, sname);
      Path spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      FileSeq selected = new FileSeq(spath.toString(), 
                                     new FileSeq(fseq, start, start+length-1));

      sourceSeqs.put(order, selected); 
    }
    else if((order != null) || (start != null) || (length != null)) {
      throw new PipelineException
        ("If any of the Order, Start or Length parameters are set then all must be set! " + 
         "Fix the settings for file sequence (" + fseq + ") of source node (" + sname + ").");
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2046337700994432637L;

  public static final String aOrder  = "Order";
  public static final String aStart  = "Start"; 
  public static final String aLength = "Length"; 
 
}


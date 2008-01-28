// $Id: CopyAction.java,v 1.1 2008/01/28 07:58:30 jesse Exp $

package us.temerity.pipeline.plugin.CopyAction.v2_3_15;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;

import us.temerity.pipeline.ActionAgenda;
import us.temerity.pipeline.FileSeq;
import us.temerity.pipeline.MappedArrayList;
import us.temerity.pipeline.NodeID;
import us.temerity.pipeline.OsType;
import us.temerity.pipeline.PackageInfo;
import us.temerity.pipeline.Path;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.SubProcessHeavy;
import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   C O P Y   A C T I O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Copies the source file to the target file.
 */
public 
class CopyAction 
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CopyAction() 
  {
    super("Copy", new VersionID("2.3.15"), "Temerity",
	  "Copies the source file to the target file.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
   *   If unable to prepare a SubProcess due to illegal, missing or incompatible 
   *   information in the action agenda or a general failure of the prep method code.
   */
  @Override
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
    String sourceNode = null;
    
    /* sanity checks */ 
    {
      FileSeq target = agenda.getPrimaryTarget();
      
      {
	if (agenda.getSourceNames().size() != 1)
	  throw new PipelineException
	    ("The Copy Action requires exactly one source.");
	for (String s : agenda.getSourceNames())
	  sourceNode = s;
      }
      
      /* the primary file sequences */ 
      {
	for(String sname : agenda.getSourceNames()) {
	  FileSeq source = agenda.getPrimarySource(sname);

	  if(target.numFrames() != source.numFrames()) 
	    throw new PipelineException 
	      ("The primary file sequence (" + source + ") of source node (" + sname + ")" + 
	       "does not contain the same number of frames (" + target.numFrames() + ") " +
	       "as the primary file sequence (" + target + ") of the target node " + 
	       "(" + nodeID.getName() + ")!");
	}
      }
      
      /* the secondary file sequences */ 
      {
	int num = agenda.getSecondaryTargets().size();
	for(String sname : agenda.getSourceNames()) {
	  if(num != agenda.getSecondarySources(sname).size()) 
	    throw new PipelineException 
	      ("The source node (" + sname + ") does not have the same number of secondary" +
	       "file sequences as the target node (" + nodeID.getName() + ")!");
	}
      }
    }

    /* create a temporary script file */   
    File script = createTempScript(agenda); 
    try {      
      FileWriter out = new FileWriter(script);

      NodeID snodeID = new NodeID(nodeID, sourceNode);

      String cpOpts = "--remove-destination";
      if(PackageInfo.sOsType == OsType.MacOS)
	cpOpts = "-f";

      /* the primary file sequences */
      {
        ArrayList<Path> targetPaths = getPrimaryTargetPaths(agenda, "destination files");
        FileSeq source = agenda.getPrimarySource(sourceNode);
        for(int wk=0; wk<targetPaths.size(); wk++) {
          Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));
          
          if(PackageInfo.sOsType != OsType.Windows)
            out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                targetPaths.get(wk).toOsString() + "\n");
          else
            out.write("copy /y \"" + spath.toOsString() + "\" " + 
                "\"" + targetPaths.get(wk).toOsString() + "\"\n");
        }
      }
      
      
      /* the secondary file sequences */ 
      {
	MappedArrayList<FileSeq, Path> targetPaths = 
	  getSecondaryTargetPaths(agenda);
	ArrayList<FileSeq> targets = 
	  new ArrayList<FileSeq>(agenda.getSecondaryTargets());
	ArrayList<FileSeq> sources = 
	  new ArrayList<FileSeq>(agenda.getSecondarySources(sourceNode));
	
	for(int sk=0; sk<targets.size(); sk++) {
	  FileSeq target = targets.get(sk);
	  FileSeq source = sources.get(sk);
	  ArrayList<Path> targetPath = targetPaths.get(target);
	  
	  for(int wk=0; wk<target.numFrames(); wk++) {
	    Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));
	    
	    if(PackageInfo.sOsType != OsType.Windows)
	      out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
	                targetPath.get(wk).toOsString() + "\n");
	    else
	      out.write("copy /y \"" + spath.toOsString() + "\" " + 
	                "\"" + targetPath.get(wk).toOsString() + "\"\n");
	  }
	}
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
  
  
  /**
   *  Get the abstract paths to the primary files associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the target files. 
   */ 
  private MappedArrayList<FileSeq, Path>
  getSecondaryTargetPaths
  (
    ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    MappedArrayList<FileSeq, Path> toReturn = new MappedArrayList<FileSeq, Path>();
    SortedSet<FileSeq> seqs = agenda.getSecondaryTargets();
    
    for (FileSeq fseq : seqs) {
    for(Path path : fseq.getPaths()) 
      toReturn.put(fseq, new Path(agenda.getTargetPath(), path));
    }
    return toReturn;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7106310981662053638L;

}

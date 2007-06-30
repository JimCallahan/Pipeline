package us.temerity.pipeline.plugin.CopyAction.v2_3_2;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;
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
    super("Copy", new VersionID("2.3.2"), "Temerity",
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
        FileSeq target = agenda.getPrimaryTarget();
	FileSeq source = agenda.getPrimarySource(sourceNode);

	int wk;
	for(wk=0; wk<target.numFrames(); wk++) {
          Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));

          if(PackageInfo.sOsType == OsType.Windows) {
            Path tpath = getWorkingNodeFilePath(nodeID, target.getPath(wk));

            out.write("copy /y \"" + spath.toOsString() + "\" " + 
                      "\"" + tpath.toOsString() + "\"\n");
          }
          else {
            out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                      target.getPath(wk).toOsString() + "\n");
          }
	}
      }

      /* the secondary file sequences */ 
      {
	ArrayList<FileSeq> targets = 
	  new ArrayList<FileSeq>(agenda.getSecondaryTargets());
	ArrayList<FileSeq> sources = 
	  new ArrayList<FileSeq>(agenda.getSecondarySources(sourceNode));
	
	int sk;
	for(sk=0; sk<targets.size(); sk++) {
	  FileSeq target = targets.get(sk);
	  FileSeq source = sources.get(sk);
	  
	  int wk;
	  for(wk=0; wk<target.numFrames(); wk++) {
            Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));

            if(PackageInfo.sOsType == OsType.Windows) {
              Path tpath = getWorkingNodeFilePath(nodeID, target.getPath(wk));

              out.write("copy /y \"" + spath.toOsString() + "\" " + 
                        "\"" + tpath.toOsString() + "\"\n");
            }
            else {
              out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                        target.getPath(wk).toOsString() + "\n");
            }
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -167220264923650256L;
}

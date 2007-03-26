// $Id: MRayInstGroupAction.java,v 1.1 2007/03/26 01:29:03 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   I N S T   G R O U P   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds MentalRay "instgroups" from attached MI files. <P>
 * 
 * This action scans each source MI files for "instance" statements and then generates 
 * a target MI file which includes the source MI file and contains an "instgroups" statement 
 * for all found instances.  
 */
public class 
MRayInstGroupAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MRayInstGroupAction()
  {
    super("MRayInstGroup", new VersionID("2.2.1"), "Temerity",
	  "Builds an inst group from attached mi files");

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
    /* the MIs to export */ 
    ArrayList<Path> targetPaths = 
      getPrimaryTargetPaths(agenda, "mi", "Mental Ray Input (.mi) files");

    /* the MIs to parse */ 
    ArrayList<ArrayList<Path>> sourcePaths = new ArrayList<ArrayList<Path>>();
    for(String sname : agenda.getSourceNames()) {
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq.numFrames() != targetPaths.size()) 
        throw new PipelineException
          ("The file sequence (" + fseq + ") of source node (" + sname + ") did not have " + 
           "the same number of frames as the target MI file seqence!"); 

      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix == null) || !suffix.equals("mi"))
        throw new PipelineException
          ("The " + getName() + " Action requires that the source node (" + sname + ") " + 
           "must have Mental Ray Input (.mi) files as its primary file sequence!");
      
      ArrayList<Path> spaths = new ArrayList<Path>();
      Path parent = (new Path(sname)).getParentPath();
      for(Path path : fseq.getPaths()) 
        spaths.add(new Path(parent, path));
      sourcePaths.add(spaths);
    }

    /* create a temporary Python script file to process the MI files */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      NodeID nodeID = agenda.getNodeID(); 
      String instGroupName = nodeID.getName().replaceAll("/", "_").substring(1);
      Path wpath = new Path(PackageInfo.sProdPath, 
                            "working/" + nodeID.getAuthor() + "/" + nodeID.getView()); 

      /* process the MI files */ 
      int idx = 0;
      for(Path tpath : targetPaths) {
        out.write("target = open('" + tpath + "', 'w')\n" +
                  "try:\n"); 

        for(ArrayList<Path> spaths : sourcePaths) 
          out.write("  target.write('$include \"$WORKING" + spaths.get(idx) + "\"\\n')\n");

        out.write("  target.write('instgroup \"" + instGroupName + "\"\\n')\n");

        for(ArrayList<Path> spaths : sourcePaths) {
          Path path = new Path(wpath, spaths.get(idx));
          out.write("  source = open('" + path + "', 'rU')\n" + 
                    "  try:\n" + 
                    "    for line in source:\n" + 
                    "      if line.startswith('instance'):\n" + 
                    "        target.write(line.split()[1] + '\\n')\n" + 
                    "  finally:\n" + 
                    "    source.close()\n");
        }

        out.write("  target.write('end instgroup\\n')\n" + 
                  "finally:\n" + 
                  "  target.close()\n");
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
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }     



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7563470325601433696L;

  private final String aGenerateRender   = "GenerateRender";
  private final String aGenerateIncludes = "GenerateIncludes";

}

package us.temerity.pipeline.plugin.MayaPartCacheGroupAction.v2_3_10;

import java.io.*;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P A R T   C A C H E   G R O U P   A C T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A grouping node for Maya Particle Caches which can be provided to the Maya Render Action.
 * <P>
 * There are rules that apply to this Action.  All the particle caches must be in a 
 */
public 
class MayaPartCacheGroupAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public MayaPartCacheGroupAction()
  {
    super("MayaPartCacheGroup", new VersionID("2.3.10"), "Temerity",
          "A grouping node for Maya Particle Caches which can be provided to the Maya" +
          "Render Action.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
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
    Path targetScene = getPrimaryTargetPath(agenda, "mel", "A Mel Script");
    
    TreeSet<Path> particleDir = new TreeSet<Path>();
    for (String s : agenda.getSourceNames()) {
      FileSeq sSeq = agenda.getPrimarySource(s);
      String suffix = sSeq.getFilePattern().getSuffix();
      if (!suffix.equals("pdc") || !sSeq.hasFrameNumbers())
        throw new PipelineException
          ("The MayaPartCacheGroup Action only takes .pdc sequences as source nodes.");
      Path p = getWorkingNodeFilePath(agenda, s, sSeq).getParentPath();
      particleDir.add(p);
    }
    
    if (particleDir.size() == 0)
      throw new PipelineException
        ("The MayaPartCacheGroup Action requires at least one source.");
    
    if (particleDir.size() > 1) {
      String badPaths = "";
      for (Path p : particleDir)
        badPaths += p + "\n";
      throw new PipelineException
        ("Due to the restrictions of Maya's particle caching setup, only one directory " +
         "can be specified as a cache directory.  The following directories were" + 
         "specified.\n" + 
         badPaths);
    }
    
    Path dir = particleDir.first();
    String cacheDir = dir.getName();
    Path workSpaceDir = dir.getParentPath();
    
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      out.write
        ("workspace -fr \"particles\" " + "\"" + workSpaceDir.toString() + "\";\n" + 
         "workspace -rt \"particles\" " + "\"" + workSpaceDir.toString() + "\";\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
      
    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, script, targetScene, outFile, errFile);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4664945470237586551L;

}

package us.temerity.pipeline.plugin.MayaPartCacheGroupAction.v2_4_28;

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
 * There are rules that apply to this Action.
 * <ul>
 * <li>This version of the Action is intended to be used with v2.4.28 or newer of the
 * MayaRender Action.
 * <li>The target file for this node must be named workspace.mel
 * <li>All the particle caches linked to this node must be in the same directory. That
 * directory must be located two directory levels below the location of this node. The
 * directory one level above the one containing this node will be the name of the particle
 * directory written in the workspace.mel.
 * </ul>
 * As an example, if a cache is a node called
 * <code>/tests/maya/particles/caches/particleShape1.#.pdc</code>, then the node with this
 * action should be named <code>/tests/maya/workspace.mel</code>, the mel file will contain
 * the value of <code>particles</code> for its particle data location and the maya scene being
 * rendered should have particle disk caching enabled and have the cache location set to
 * <code>caches</code>.
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
    super("MayaPartCacheGroup", new VersionID("2.4.28"), "Temerity",
          "A grouping node for Maya Particle Caches which can be provided to the Maya" +
          "Render Action.");
    
    underDevelopment();
    
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
    
    String name = targetScene.getName();
    if (!name.equals("workspace.mel"))
      throw new PipelineException
        ("The Primary file sequence of this node must be workspace.mel");
    
    TreeSet<Path> particleDir = new TreeSet<Path>();
    for (String s : agenda.getSourceNames()) {
      FileSeq sSeq = agenda.getPrimarySource(s);
      String suffix = sSeq.getFilePattern().getSuffix();
      if (!suffix.equals("pdc") || !sSeq.hasFrameNumbers())
        throw new PipelineException
          ("The MayaPartCacheGroup Action only takes .pdc sequences as source nodes.");
      Path p = new Path(s).getParentPath();
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

    Path targetParent = new Path(agenda.getNodeID().getName()).getParentPath(); 
    Path srcParent = dir.getParentPath().getParentPath();
    
    if (!targetParent.equals(srcParent))
      throw new PipelineException
        ("The particle caches must live two directory levels below this workspace.mel file.");
    
    String particleDirName = dir.getParentPath().getName();
    
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      out.write
        ("workspace -fr \"particles\" " + "\"" + particleDirName + "\";\n" + 
         "workspace -rt \"particles\" " + "\"" + particleDirName + "\";\n");
      
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

  private static final long serialVersionUID = -354889750709782058L;

}

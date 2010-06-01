package us.temerity.pipeline.plugin.TemplateGenDescManifestTool.v2_4_27;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G E N   D E S C   M A N I F E S T   T O O L                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to generate a {@link TemplateDescManifest} from a {@link TemplateGlueInformation} 
 * node. <p>
 * 
 * Select the TemplateGlueInformation node and then run this tool on the node that you want
 * to have the Manifest written into.
 */
public 
class TemplateGenDescManifestTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public 
  TemplateGenDescManifestTool()
  {
    super("TemplateGenDescManifest", new VersionID("2.4.27"), "Temerity", 
          "Tool to generate a TemplateDescManifest from a TemplateGlueInformationnode");
    
    underDevelopment();
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() != 2)
      throw new PipelineException
        ("You must have two nodes selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when running this tool.");
    
    NodeStatus stat = pSelected.get(pPrimary);
    NodeMod mod = stat.getLightDetails().getWorkingVersion();
    FileSeq seq = mod.getPrimarySequence();
    if (!seq.isSingle())
      throw new PipelineException
        ("The primary sequence must be a single frame.");
    String suffix = seq.getFilePattern().getSuffix();
    if (suffix == null || !suffix.equals("glue"))
      throw new PipelineException
        ("The primary sequence must be a (glue) file");
    
    return " : Generating manifest";
  }
 
  @Override
  public synchronized boolean 
  executePhase
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    String srcNode;
    {
      TreeSet<String> temp = new TreeSet<String>(pSelected.keySet());
      temp.remove(pPrimary);
      srcNode = temp.first();
    }
   
    NodeMod srcMod = mclient.getWorkingVersion(getAuthor(), getView(), srcNode);
    Path srcPath = getWorkingNodeFilePath(srcNode, srcMod.getPrimarySequence());
    
    TemplateGlueInformation info;
    try {
      info = 
        (TemplateGlueInformation) GlueDecoderImpl.decodeFile(aTemplateGlueInfo, srcPath.toFile());
      if (info == null)
        throw new PipelineException
          ("There is no TemplateGlueInfo in the srcNode (" + srcNode + ")");
    }
    catch (GlueException ex) {
      throw new PipelineException
        ("Error reading the Template Glue Info from the srcNode (" + srcNode + ").\n" + 
         ex.getMessage());
    }
    
    TemplateDescManifest manifest;
    
    TreeSet<String> nodes = info.getNodesInTemplate();
    if (nodes.isEmpty()) {
      VersionID vid = srcMod.getWorkingID();
      manifest = new TemplateDescManifest(srcNode, vid);
    }
    else {
      TreeMap<String, VersionID> roots = new TreeMap<String, VersionID>();
      for (String root : srcMod.getSourceNames()) {
        NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), root);
        roots.put(root, mod.getWorkingID());
      }
      manifest = new TemplateDescManifest(roots, nodes);
    }
    
    NodeMod tgtMod = mclient.getWorkingVersion(getAuthor(), getView(), pPrimary);
    Path tgtPath = getWorkingNodeFilePath(pPrimary, tgtMod.getPrimarySequence());
    
    try {
      GlueEncoderImpl.encodeFile(aDescManifest, manifest, tgtPath.toFile());
    }
    catch (GlueException ex) {
      throw new PipelineException
        ("Error writing the Template Glue Info into the tgtNode (" + pPrimary + ").\n" + 
         ex.getMessage());
    }
      
    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6854038423264038239L;
  
  public static final String aTemplateGlueInfo = "TemplateGlueInfo";
  public static final String aDescManifest     = "DescManifest";
}

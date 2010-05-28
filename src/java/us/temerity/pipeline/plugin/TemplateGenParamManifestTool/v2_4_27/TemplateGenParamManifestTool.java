package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G E N   P A R A M   M A N I F E S T   T O O L                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to generate a {@link TemplateParamManifest} from a {@link TemplateGlueInformation} 
 * node. <p>
 * 
 * Select the TemplateGlueInformation node and then run this tool on the node that you want
 * to have the Manifest written into.
 */
public 
class TemplateGenParamManifestTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public 
  TemplateGenParamManifestTool()
  {
    super("TemplateGenParamManifest", new VersionID("2.4.27"), "Temerity", 
          "Tool to generate a TemplateParamManifest from a TemplateGlueInformationnode");
    
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
    
    pAuthor = getAuthor();
    pView = getView();
    
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
    
    NodeMod paramMod = mclient.getWorkingVersion(pAuthor, pView, pPrimary);
    FileSeq pSeq = paramMod.getPrimarySequence();
    
    FileSeq seq = paramMod.getPrimarySequence();
    if (!seq.isSingle())
      throw new PipelineException
        ("The primary sequence must be a single frame.");
    String suffix = seq.getFilePattern().getSuffix();
    if (suffix == null || !suffix.equals("glue"))
      throw new PipelineException
        ("The primary sequence must be a (glue) file");

    TemplateParamManifest oldManifest = null;
    {
      Path p = getWorkingNodeFilePath(pPrimary, pSeq);
      File f = p.toFile(); 

      if (f.isFile()) {
        try {
          Object o = 
            GlueDecoderImpl.decodeFile("ParamManifest", f);
          if (o != null && o instanceof TemplateParamManifest )
            oldManifest = (TemplateParamManifest) o;
        }
        catch (GlueException ex) {
          throw new PipelineException
            ("Error reading the old Parameter file from node (" + pPrimary + ").\n" + 
             ex.getMessage());
        }
      }
    }
    
    TemplateGlueInformation glueInformation = null;
    {
      NodeMod glueMod = mclient.getWorkingVersion(pAuthor, pView, srcNode);
      FileSeq glueSeq = glueMod.getPrimarySequence();
      Path p = getWorkingNodeFilePath(srcNode, glueSeq);
      File f = p.toFile();
      
      try {
        Object o = GlueDecoderImpl.decodeFile(aTemplateGlueInfo, f);
        if (o == null || !(o instanceof TemplateGlueInformation))
          throw new PipelineException
            ("There is no TemplateGlueInfo in the srcNode (" + srcNode + ")");
        glueInformation = (TemplateGlueInformation) o;
      }
      catch (GlueException ex) {
        throw new PipelineException
          ("Error reading the glue file from node (" + srcNode + ").\n" + 
           ex.getMessage());
      }
    }
 
    JTemplateGenParamDialog dialog = 
      new JTemplateGenParamDialog(glueInformation, oldManifest);
    dialog.initUI();
    dialog.setVisible(true);
    
    
    return false;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTemplateGlueInfo = "TemplateGlueInfo";
  private static final long serialVersionUID = 6184290680922408846L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private String pAuthor;
  private String pView;
}

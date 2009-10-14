// $Id: TemplateGlueTool.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   T O O L                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateGlueTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TemplateGlueTool()
  {
    super("TemplateGlue", new VersionID("2.4.12"), "Temerity", 
          "Tool for creating and editing a template glue node.");

    addPhase(new ValidInputPass());
    
    underDevelopment();
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  private class
  ValidInputPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String collectInput()
      throws PipelineException
    {
      if (pPrimary == null)
        throw new PipelineException
         ("This tool requires at least one node to be selected.");
      
      pNodesInTemplate = new TreeSet<String>(pSelected.keySet());
      pNodesInTemplate.remove(pPrimary);
     
      pAuthor = getAuthor();
      pView = getView();
      
      return ": Validating input";
    }
    
    @Override
    public NextPhase execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      NodeMod templateMod = mclient.getWorkingVersion(pAuthor, pView, pPrimary);
      FileSeq pSeq = templateMod.getPrimarySequence();
      if (!pSeq.isSingle())
        throw new PipelineException("The template node must be a single file");
      String suffix = pSeq.getFilePattern().getSuffix();
      if (suffix == null || !suffix.equals("glue"))
        throw new PipelineException("The template file sequence must have the (glue) suffix");
      
      if (pNodesInTemplate.isEmpty()) {
        for (String source : templateMod.getSourceNames()) {
          TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(source, mclient);
          if (annots == null || annots.isEmpty())
            throw new PipelineException
              ("There were no nodes selected as being in the template and the nodes " +
               "attached to the template definition node do not contain task annotations.  " +
               "Cowardly refusing to make a Template Glue file that will not build anything.");
          break;
        }
      }
      
      pOldSettings = null;
      
      Path p = getWorkingNodeFilePath(pPrimary, pSeq);
      pTemplateFile = p.toFile();
      if (pTemplateFile.exists()) {
        try {
           Object o = GlueDecoderImpl.decodeFile(aTemplateGlueInfo, pTemplateFile);
           if (o instanceof us.temerity.pipeline.builder.v2_4_3.TemplateGlueInformation)
             pOldSettings = new TemplateGlueInformation(
               (us.temerity.pipeline.builder.v2_4_3.TemplateGlueInformation) o);
           else 
           pOldSettings = (TemplateGlueInformation) o;
        }
        catch (GlueException ex) {
          String error = Exceptions.getFullMessage
            ("Error reading the glue file from disk.  The template tool will continue as if it didn't exist.", ex);
          LogMgr.getInstance().log(Kind.Glu, Level.Warning, error);
        }
      }
      
      JTemplateGlueDialog dialog = 
        new JTemplateGlueDialog 
          (pPrimary, pOldSettings, pNodesInTemplate, mclient, pAuthor, pView, pTemplateFile);
      dialog.initUI();
      dialog.setVisible(true);
      if (dialog.isFinished()) {
        try {
          TemplateGlueInformation newSettings = dialog.getNewSettings();
          pTemplateFile.delete();
          GlueEncoderImpl.encodeFile(aTemplateGlueInfo, newSettings, pTemplateFile);
        }
        catch (GlueException ex) 
        {
          String error = Exceptions.getFullMessage("Error writing Glue Template File.", ex);
          throw new PipelineException(error);
        }
      }
      
      return NextPhase.Finish;
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 989908919124163650L;

  public static final String aTemplateGlueInfo = "TemplateGlueInfo";
  
  

  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeSet<String> pNodesInTemplate;
  
  private String pAuthor;
  private String pView;
  
  private File pTemplateFile;
  
  private TemplateGlueInformation pOldSettings;
  
}
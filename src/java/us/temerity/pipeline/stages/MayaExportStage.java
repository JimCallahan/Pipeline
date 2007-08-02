// $Id: MayaExportStage.java,v 1.1 2007/08/02 17:42:45 jesse Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   E X P O R T   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage to create a node that uses the MayaExportAction
 */
public 
class MayaExportStage
  extends MayaFileStage
{
  protected 
  MayaExportStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    boolean isAscii,
    PluginContext editor,
    String exportName,
    boolean cleanupNamespace
  )
    throws PipelineException
  {
    super(name,
  	  desc,
  	  stageInformation,
  	  context,
  	  client,
  	  null,
  	  nodeName,
  	  isAscii,
  	  editor,
  	  new PluginContext("MayaExport"));
    addSingleParamValue("ExportSet", exportName);
    addSingleParamValue(aCleanUpNamespace, cleanupNamespace);
  }
  
  protected void
  setExportHistory
  (
    Boolean value
  )
    throws PipelineException
  {
    addSingleParamValue(aExportHistory, value);
  }
  
  protected void
  setExportChannels
  (
    Boolean value
  )
    throws PipelineException
  {
    addSingleParamValue(aExportChannels, value);
  }
  
  protected void
  setExportExpressions
  (
    Boolean value
  )
    throws PipelineException
  {
    addSingleParamValue(aExportExpressions, value);
  }
  
  protected void
  setExportConstraints
  (
    Boolean value
  )
    throws PipelineException
  {
    addSingleParamValue(aExportConstraints, value);
  }
  
  public static final String aExportHistory      = "ExportHistory";
  public static final String aExportChannels     = "ExportChannels";
  public static final String aExportExpressions  = "ExportExpressions";
  public static final String aExportConstraints  = "ExportConstraints";
  
  public static final String aCleanUpNamespace   = "CleanUpNamespace";
  
  private static final long serialVersionUID = 3441932090118754356L;
}

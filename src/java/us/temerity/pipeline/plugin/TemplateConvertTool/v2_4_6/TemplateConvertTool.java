// $Id: TemplateConvertTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateConvertTool.v2_4_6;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N V E R T   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Convert all Per-Node Template annotations into Per-Version annotations.
 * <p>
 * This tool will overwrite any existing per-version annotation with the same name.
 */
public 
class TemplateConvertTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TemplateConvertTool()
  {
    super("TemplateConvert", new VersionID("2.4.6"), "Temerity", 
    "Convert all Per-Node Template annotations into Per-Version annotations.");

    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() < 1)
      throw new PipelineException
        ("You must have at least one node selected to run this tool.");
    
    return " : Converting annotations.";
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
    for (String node : pSelected.keySet()) {
      TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(node);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      for (String aName : annots.keySet()) {
        if (aName.startsWith("Template" )) {
          BaseAnnotation annot = annots.get(aName);
          mod.addAnnotation(aName, annot);
          mclient.removeAnnotation(node, aName);
        }
      }
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5884541437547549409L;
}

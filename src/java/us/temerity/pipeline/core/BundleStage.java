package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U N D L E   S T A G E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A generic stage for unpacking a node bundle.
 */ 
public 
class BundleStage
  extends StandardStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Construct a new stage from the information contained in the node bundle.
   */ 
  public
  BundleStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client,
    NodeMod mod, 
    TreeMap<String,BaseAnnotation> annotations, 
    TreeMap<String,String> toolsetRemap, 
    TreeMap<String,String> selectionKeyRemap,
    TreeMap<String,String> licenseKeyRemap,
    TreeMap<String,String> hardwareKeyRemap
  )
  {
    super("Bundle", "Description...", stageInfo, context, client, mod); 

    pOrigNodeMod = mod;

    {
      String ntset = toolsetRemap.get(mod.getToolset());
      if(ntset != null) 
        pRemappedToolset = ntset; 
      else 
        pRemappedToolset = super.getToolset();
    }

    JobReqs jreqs = mod.getJobRequirements();
    if(jreqs != null) {
      {
        TreeSet<String> selectionKeys = new TreeSet<String>();
        for(String key : jreqs.getSelectionKeys()) {
          String nkey = selectionKeyRemap.get(key); 
          if(nkey != null) 
            selectionKeys.add(nkey);
        }
        addSelectionKeys(selectionKeys);
      }
      
      {
        TreeSet<String> licenseKeys = new TreeSet<String>();
        for(String key : jreqs.getLicenseKeys()) {
          String nkey = licenseKeyRemap.get(key); 
          if(nkey != null) 
            licenseKeys.add(nkey);
        }
        addLicenseKeys(licenseKeys);
      }
      
      {
        TreeSet<String> hardwareKeys = new TreeSet<String>();
        for(String key : jreqs.getHardwareKeys()) {
          String nkey = hardwareKeyRemap.get(key); 
          if(nkey != null) 
            hardwareKeys.add(nkey);
        }
        addHardwareKeys(hardwareKeys);
      }
    }

    for(String aname : annotations.keySet()) 
      addAnnotation(aname, annotations.get(aname));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get job settings directly from the original node working version.
   */ 
  @Override
  protected void
  setJobSettings()
    throws PipelineException
  {
    pRegisteredNodeMod.setExecutionMethod(pExecutionMethod);
    if (pExecutionMethod == ExecutionMethod.Parallel)
      pRegisteredNodeMod.setBatchSize(pBatchSize);
    pRegisteredNodeMod.setJobRequirements(pOrigNodeMod.getJobRequirements()); 
  }
  
  /**
   * Shortcut method to get the toolset value.<P> 
   * 
   * This subclass overrides this to lookup the remapped toolset instead of using 
   * stage's {@link UtilContext}.
   * 
   * @return The Toolset.
   */
  @Override
  protected String 
  getToolset()
  {
    return pRemappedToolset; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -4175812464423957478L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The original node from the bundle being copied.
   */ 
  private NodeMod  pOrigNodeMod; 

  /**
   * The name of the local Toolset to use for this node.
   */ 
  private String  pRemappedToolset;

}

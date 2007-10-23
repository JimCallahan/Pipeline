package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U N D L E   B U I L D E R                                                            */
/*------------------------------------------------------------------------------------------*/

public 
class BundleBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  BundleBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation, 
    NodeBundle bundle, 
    Path bundlePath, 
    TreeMap<String,String> toolsetRemap, 
    TreeMap<String,String> selectionKeyRemap,
    TreeMap<String,String> licenseKeyRemap
  ) 
    throws PipelineException
  {
    super("BundleBuilder",
          new VersionID("1.0.0"),
          "Temerity", 
          "Builds a network of nodes based on the contents of a node bundle.", 
          mclient,
          qclient,
          builderInformation);
    
    pBundle            = bundle; 
    pBundlePath        = bundlePath; 
    pToolsetRemap      = toolsetRemap;      
    pSelectionKeyRemap = selectionKeyRemap; 
    pLicenseKeyRemap   = licenseKeyRemap;   

    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A S S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Override to change setup passes
   */
  protected void
  addSetupPasses()
    throws PipelineException
  {
    addSetupPass(new InformationPass());
  }
  
  /**
   * Override to change construct passes
   */
  protected void
  addConstructPasses()
    throws PipelineException
  {
    addConstructPass(new BuildPass()); 
  }
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * No check-ins required.
   */
  protected LinkedList<String>
  getNodesToCheckIn()
  {
    return new LinkedList<String>();
  }
    
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the BundleBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    
    private static final long serialVersionUID = -778071115009423508L;
  } 
  
  protected 
  class BuildPass
    extends ConstructPass
  {

    public 
    BuildPass()
    {
      super("Build Pass", 
            "The BundleBuilder Pass which actually constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      
      /* unpack the nodes */ 
      for(NodeMod mod : pBundle.getWorkingVersions()) {
        BundleStage stage = 
          new BundleStage(pStageInfo, pContext, pClient, mod, 
                          new TreeMap<String,BaseAnnotation>() /* FIX THIS!! */, 
                          pToolsetRemap, pSelectionKeyRemap, pLicenseKeyRemap);
	stage.build();

        if(!mod.isActionEnabled()) 
          addToDisableList(mod.getName());
      }
      
      /* disable the ones we've registered */
      disableActions(); 
    }

    private static final long serialVersionUID = -4727376614484620762L;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7437240995391076413L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   *
   */ 
  private NodeBundle  pBundle; 

  /**
   *
   */ 
  private Path  pBundlePath;  

  /**
   *
   */ 
  private TreeMap<String,String>  pToolsetRemap;  

  /**
   *
   */ 
  private TreeMap<String,String>  pSelectionKeyRemap; 

  /**
   *
   */ 
  private TreeMap<String,String>  pLicenseKeyRemap;

}


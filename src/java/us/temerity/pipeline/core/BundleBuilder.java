package us.temerity.pipeline.core;

import java.util.LinkedList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BuilderInformation;

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
 
  /**
   * Construct a new builder.
   * 
   * @param mclient
   *   The network connection to plmaster(1).
   * 
   * @param qclient
   *   The network connection to plqueuemgr(1).
   * 
   * @param builderInformation
   *   Common shared information among all builders.
   * 
   * @param bundle
   *   The extracted node bundle metadata.
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle file.
   * 
   * @param toolsetRemap
   *   A table mapping the names of toolsets associated with the nodes in the node bundle
   *   to toolsets at the local site.  Toolsets not found in this table will be remapped 
   *   to the local default toolset instead.
   * 
   * @param selectionKeyRemap
   *   A table mapping the names of selection keys associated with the nodes in the node 
   *   bundle to selection keys at the local site.  Any selection keys not found in this 
   *   table will be ignored.
   * 
   * @param licenseKeyRemap
   *   A table mapping the names of license keys associated with the nodes in the node 
   *   bundle to license keys at the local site.  Any license keys not found in this 
   *   table will be ignored.
   *   
   * @param hardwareKeyRemap
   *   A table mapping the names of hardware keys associated with the nodes in the node 
   *   bundle to hardware keys at the local site.  Any hardware keys not found in this 
   *   table will be ignored.
   */ 
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
    TreeMap<String,String> licenseKeyRemap,
    TreeMap<String,String> hardwareKeyRemap
  ) 
    throws PipelineException
  {
    super("BundleBuilder",
          "Builds a network of nodes based on the contents of a node bundle.", 
          mclient,
          qclient,
          builderInformation);
    
    pBundle            = bundle; 
    pBundlePath        = bundlePath; 
    pToolsetRemap      = toolsetRemap;      
    pSelectionKeyRemap = selectionKeyRemap; 
    pLicenseKeyRemap   = licenseKeyRemap;   
    pHardwareKeyRemap  = hardwareKeyRemap;

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
  @Override
  public LinkedList<String>
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
        TreeMap<String,BaseAnnotation> annots = new TreeMap<String,BaseAnnotation>();
        for(String aname : pBundle.getAnnotationNames(mod.getName())) {
          BaseAnnotation annot = pBundle.getAnnotation(mod.getName(), aname);
          if(annot != null) 
            annots.put(aname, annot);
        }

        BundleStage stage = 
          new BundleStage(getStageInformation(), pContext, pClient, mod, annots, pToolsetRemap, 
                          pSelectionKeyRemap, pLicenseKeyRemap, pHardwareKeyRemap);
	stage.build();

        if((mod.getAction() != null) && !mod.isActionEnabled()) 
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
   * The extracted node bundle metadata.
   */ 
  private NodeBundle  pBundle; 

  /**
   * The abstract file system path to the node bundle file.
   */ 
  private Path  pBundlePath;  

  /**
   * A table mapping the names of toolsets associated with the nodes in the node bundle
   * to toolsets at the local site.  Toolsets not found in this table will be remapped 
   * to the local default toolset instead.
   */ 
  private TreeMap<String,String>  pToolsetRemap;  

  /**
   * A table mapping the names of selection keys associated with the nodes in the node 
   * bundle to selection keys at the local site.  Any selection keys not found in this 
   * table will be ignored.
   */ 
  private TreeMap<String,String>  pSelectionKeyRemap; 

  /**
   * A table mapping the names of license keys associated with the nodes in the node 
   * bundle to license keys at the local site.  Any license keys not found in this 
   * table will be ignored.
   */ 
  private TreeMap<String,String>  pLicenseKeyRemap;
  
  /**
   * A table mapping the names of hardware keys associated with the nodes in the node 
   * bundle to hardware keys at the local site.  Any hardware keys not found in this 
   * table will be ignored.
   */ 
  private TreeMap<String,String>  pHardwareKeyRemap;

}


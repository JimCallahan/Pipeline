/*
 * Created on Jul 3, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.stages.StageInformation;

/**
 * Simple builder to constuct a model file from multiple pieces.
 * <p>
 * Cannot be run as a standalone Builder.
 * 
 */
public class ModelPiecesBuilder extends BaseBuilder
{

  public
  ModelPiecesBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuildsAssetNames assetNames,
    BuilderInformation builderInformation,
    int numberOfPieces
  )
    throws PipelineException
  {
    super("ModelPiecesBuilder",
          new VersionID("2.3.2"),
          "Temerity", 
          "Simple builder to constuct a model file from multiple pieces.",
          mclient,
          qclient,
          builderInformation);
    pNames = assetNames;

    if (numberOfPieces < 1 )
      throw new PipelineException
        ("You cannot initialize this builder with a number of pieces that is less than 1");

    {
      UtilityParam param = 
        new MayaContextUtilityParam
        (aMayaContext,
         "The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
         new MayaContext()); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aCheckinWhenDone,
         "Automatically check-in all the nodes when building is finished.", 
         false); 
      addParam(param);
    }
    
    for (int i = 0; i < numberOfPieces; i++) {
      String num = String.valueOf(i);
      UtilityParam param = 
        new StringUtilityParam
        ("ModelPiece" + num,
         "The name for a particular model piece.", 
         "Model" + num); 
      addParam(param);
      pPieceParams.add(new ParamMapping(param.getName()));
    }
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic information about the asset is collected " +
           "from the user.", 
           "BuilderSettings", 
           true);
      //layout.addColumn("Asset Information", true);
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aCheckinWhenDone);
      layout.addEntry(1, aActionOnExistance);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, null);
      layout.addEntry(1, aMayaContext);
      for (ParamMapping param : pPieceParams)
        layout.addEntry(1, param.getParamName());
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  protected TreeSet<String> getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  @Override
  protected boolean performCheckIn()
  {
    return pCheckInWhenDone;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  
  private static final long serialVersionUID = -621745474133340612L;


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private LinkedList<ParamMapping> pPieceParams;
  private TreeSet<String> pPieceNames;
  private BuildsAssetNames pNames;
  private boolean pCheckInWhenDone;
  private MayaContext pMayaContext;
  
  protected String pPlaceHolderMEL;
  protected String pVerifyModelMEL;
  
  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  
  
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
            "Information pass for the ModelPiecesBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      pPieceNames = new TreeSet<String>();
      for (ParamMapping param : pPieceParams) {
        String name = getStringParamValue(param);
        if (name == null || name.equals(""))
          throw new PipelineException
            ("Each model name must have a valid identifier. " +
             "(" + name  + ") is not a valid name.");
        boolean dup = pPieceNames.add(name);
        if (dup)
          throw new PipelineException
            ("Cannot have two models with the same name.  " +
             "(" + name + ") was present more than once.");
      }
      
      pPlaceHolderMEL = pNames.getPlaceholderScriptName();
      pVerifyModelMEL = pNames.getModelVerificationScriptName();
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -3548064419172163386L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class BuildPass
    extends ConstructPass
  {
    public 
    BuildPass()
    {
      super("Build Pass", 
            "The ModelPiecesBuilder Pass which actually constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      
      TreeMap<String, String> pieceNodes = new TreeMap<String, String>();
      
      for (String pieceName : pPieceNames) {
        String modelName = pNames.getModelPieceNodeName(pieceName);
        pieceNodes.put(pieceName, modelName);
        if(!checkExistance(modelName)) {
          AssetBuilderModelStage stage = 
            new AssetBuilderModelStage
            (info,
             pContext,
             pClient,
             pMayaContext, 
             modelName,
             pPlaceHolderMEL);
            stage.build();
            pModelStages.add(stage);
        }
      }
      String editName = pNames.getModelEditNodeName();
      if (!checkExistance(editName)) {
        ModelPiecesEditStage stage = 
          new ModelPiecesEditStage
          (info,
           pContext,
           pClient,
           pMayaContext,
           editName, 
           pieceNodes
          );
        stage.build();
        addToDisableList(editName);
        addToCheckInList(editName);
      }
      String verifyName = pNames.getModelVerifyNodeName();
      if (!checkExistance(verifyName)) {
        ModelPiecesVerifyStage stage = 
          new ModelPiecesVerifyStage
          (info,
           pContext,
           pClient,
           pMayaContext,
           verifyName, 
           pieceNodes,
           pVerifyModelMEL
          );
        stage.build();
        addToCheckInList(verifyName);
      }
    }
    @Override

    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> list = new TreeSet<String>();
      addNonNullValue(pPlaceHolderMEL, list);
      addNonNullValue(pVerifyModelMEL, list);
      return list;
    }
    private static final long serialVersionUID = 3284037619392844408L;
  }
  
  protected 
  class FinalizeLoop
    extends ConstructPass
  {
    public 
    FinalizeLoop()
    {
      super("Finalize Pass", 
            "The ModelPiecesBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the prebuild phase in the Finalize Pass");
      TreeSet<String> toReturn = new TreeSet<String>(getDisableList());
      return toReturn;
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Finalize Pass");
      for (AssetBuilderModelStage stage : pModelStages)
        stage.finalizeStage();
      disableActions();
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> list = new TreeSet<String>();
      list.addAll(getDisableList());
      return list;
    }
    private static final long serialVersionUID = -2922609248177441243L;
  }
}
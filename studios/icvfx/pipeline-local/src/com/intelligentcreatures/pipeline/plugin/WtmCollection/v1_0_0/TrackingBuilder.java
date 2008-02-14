// $Id: TrackingBuilder.java,v 1.3 2008/02/14 05:16:57 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T R A C K I N G   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Tracking task.<P> 
 * 
 * Primarily used to validate the a Maya scene containing the tracked camera and head model.
 * The validation process includes performing MEL based tests on the scene as well as 
 * rendering/comping the shot using a rig and shaders designed to show flaws in the tracking 
 * data. <P> 
 * 
 * This task relies on the existence of a Maya scene containing the camera/model tracking 
 * data in the following location: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   /projects/wtm/shots/SEQ/SHOT/tracking/edit/SEQSHOT_track.ma
 * </DIV>
 * 
 * Where SEQ is the two character sequence name and SHOT is the 3 digit shot number.  This
 * file can be created one of two ways.  It can either have been created by a tracking 
 * outsourcing studio and manually registered and checked-in or it can be built before hand 
 * using the InternalTracking builder.  This builder will fail if this Maya scene doesn not 
 * already exist with the correct name and location before the builder is run.<P> 
 * 
 * Besides the common parameters shared by all builders, this builder defines the following
 * additional parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Project Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the overall project.
 *   </DIV> <BR>
 * 
 *   Sequence Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the shot sequence.
 *   </DIV> <BR>
 * 
 *   Shot Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the shot within a sequence.
 *   </DIV> <BR>
 * </DIV> 
 */
public 
class TrackingBuilder 
  extends BaseTrackingBuilder 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor for to launch the builder.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   */ 
  public 
  TrackingBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    this(mclient, qclient, builderInfo, 
         new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         null, null);
  }
  
  /**
   * Provided to allow parent builders to create instances and share namers. 
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   * 
   * @param studioDefs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   * 
   * @param projectNamer
   *   Provides project-wide names of nodes and node directories.
   * 
   * @param shotNamer
   *   Provides the names of nodes and node directories which are shot specific.
   */ 
  public 
  TrackingBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo, 
   StudioDefinitions studioDefs,
   ProjectNamer projectNamer, 
   ShotNamer shotNamer
  )
    throws PipelineException
  {
    super("Tracking",
          "A builder for constructing the nodes associated with the Tracking task.", 
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

    /* initialize the project namer */ 
    initProjectNamer(); 

    /* create the setup passes */ 
    addSetupPass(new TrackingSetupShotEssentials());
    addSetupPass(new SetupTrackingEssentials()); 
    
    /* setup the default editors */ 
    setCommonDefaultEditors(); 

    /* create the construct passes */ 
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);
    }

    /* specify the layout of the parameters for each pass in the UI */ 
    {
      PassLayoutGroup layout = new PassLayoutGroup("Root", "Root Layout");

      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("ShotEssentials", true);

        sub.addEntry(1, aUtilContext);
        sub.addEntry(1, null);
        sub.addEntry(1, aCheckinWhenDone);
        sub.addEntry(1, aActionOnExistence);
        sub.addEntry(1, aReleaseOnError);
        sub.addEntry(1, null);
        sub.addEntry(1, aLocation);

        layout.addPass(sub.getName(), sub); 
      }
      
      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("None", true);
        layout.addPass(sub.getName(), sub);
      }
      
      setLayout(layout);
    }
  }
  

   
  /*----------------------------------------------------------------------------------------*/
  /*  O V E R R I D E S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Returns a list of Actions required by this Builder, indexed by the toolset that
   * needs to contain them.
   * <p>
   * Builders should override this method to provide their own requirements.  This
   * validation gets performed after all the Setup Passes have been run but before
   * any Construct Passes are run.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();	
    //plugins.add(new PluginContext("PFTrackBuild", "ICVFX"));		
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("CatFiles"));  
    plugins.add(new PluginContext("Composite"));   		
    plugins.add(new PluginContext("MayaBuild"));  		
    plugins.add(new PluginContext("MayaRender")); 		

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class TrackingSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    TrackingSetupShotEssentials()
    {
      super(); 
    }
   
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      super.validatePhase();

      /* register the required (locked) nodes */ 
      {
	/* tracking assets */ 
	pTrackPrepNodeName = pProjectNamer.getTrackPrepNode(); 
	pRequiredNodeNames.add(pTrackPrepNodeName); 

	pTrackVerifyRenderNodeName = pProjectNamer.getTrackVerifyRenderNode(); 
	pRequiredNodeNames.add(pTrackVerifyRenderNodeName); 

	pTrackExtractCameraNodeName = pProjectNamer.getTrackExtractCameraNode(); 
	pRequiredNodeNames.add(pTrackExtractCameraNodeName); 

	pTrackExtractTrackingNodeName = pProjectNamer.getTrackExtractTrackingNode(); 
	pRequiredNodeNames.add(pTrackExtractTrackingNodeName); 

	/* rorschach assets */ 
	pRorschachVerifyModelNodeName = pProjectNamer.getRorschachVerifyModelNode(); 
	pRequiredNodeNames.add(pRorschachVerifyModelNodeName); 

	pRorschachTestShadersNodeName = pProjectNamer.getRorschachTestShadersNode(); 
	pRequiredNodeNames.add(pRorschachTestShadersNodeName); 

	pRorschachTestLightsNodeName = pProjectNamer.getRorschachTestLightsNode(); 
	pRequiredNodeNames.add(pRorschachTestLightsNodeName); 
      }
    }
    
    private static final long serialVersionUID = -7271559542044510237L;
  }


  /*----------------------------------------------------------------------------------------*/
    
  protected
  class SetupTrackingEssentials
    extends BaseSetupTrackingEssentials
  {
    public 
    SetupTrackingEssentials()
    {
      super(); 
    }

    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    { 
      super.validatePhase(); 

      /* lookup the frame range of the shot by looking at the undistorted 2k plates node */ 
      {
	NodeVersion vsn = pClient.getCheckedInVersion(pUndistorted2kPlateNodeName, null); 
	if(vsn == null) 
	  throw new PipelineException
	    ("Somehow no checked-in version of the undistorted 2k plates node " + 
	     "(" + pUndistorted2kPlateNodeName + ") exists!"); 	

	pFrameRange = vsn.getPrimarySequence().getFrameRange(); 
      }
    }
    
    private static final long serialVersionUID = 3499087100113059674L; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected 
  class BuildNodesPass
    extends ConstructPass
  {
    public 
    BuildNodesPass()
    {
      super("Build Submit/Approve Nodes", 
            "Creates the nodes which make up the Plates task."); 
    }
    
    /**
     * Create the plates node networks.
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      /* lock the latest version of all of the prerequisites */ 
      lockNodePrerequitites(); 

      /* the submit network */
      {
	pTrackNodeName = pShotNamer.getTrackNode(); 
	{
	  if(!nodeExists(pTrackNodeName)) 
	    throw new PipelineException
	      ("Somehow the required camera/model tracking data node " + 
	       "(" + pTrackNodeName + ") does not exist!"); 

	  try {
	    NodeVersion vsn = pClient.getCheckedInVersion(pTrackNodeName, null); 
	    if(!vsn.hasSources()) {
	      /* lock it if it was generated by an outsourcer */ 
	      lockLatest(pTrackNodeName); 
	    }
	    else {
	      /* otherwise, it was probably generated internally so we should make sure
	  	   its up-to-date and modifiable so tracking can continue to be adjusted 
		   while doing tracking verification test renders */ 
	      checkOutLatest(pTrackNodeName, 
			     CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
	    }
	  }
	  catch(PipelineException ex) {
	    try {
	      /* if it already exists in the working area, just leave it alone */ 
	      pClient.getWorkingVersion(getAuthor(), getView(), pTrackNodeName); 
	    }
	    catch(PipelineException ex2) {
	      throw new PipelineException
		("Somehow no working version of the required camera/model tracking data " + 
		 "node (" + pTrackNodeName + ") exists in the current working area " + 
		 "(" + getAuthor() + "|" + getView() + " and it has never been " + 
		 "checked-in!");
	    }
	  }
	}

	String verifyNodeName = pShotNamer.getTrackingVerifyNode(); 
	{
	  BuildTrackingVerifyStage stage = 
	    new BuildTrackingVerifyStage
	    (pStageInfo, pContext, pClient, 
	     verifyNodeName, pTrackNodeName, pRorschachVerifyModelNodeName, 
	     pRorschachTestShadersNodeName, pRorschachTestLightsNodeName, 
	     pTrackPrepNodeName, pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String verifyImagesNodeName = pShotNamer.getTrackingVerifyImagesNode(); 
	{
	  RenderTrackingVerifyStage stage = 
	    new RenderTrackingVerifyStage
	    (pStageInfo, pContext, pClient, 
	     verifyImagesNodeName, pFrameRange, verifyNodeName, 
	     "track:camera01", pTrackVerifyRenderNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
	}

	String verifyCompNodeName = pShotNamer.getTrackingVerifyCompNode(); 
	{
	  BashCompStage stage = 
	    new BashCompStage(pStageInfo, pContext, pClient, 
			      verifyCompNodeName, pFrameRange, 
			      verifyImagesNodeName, pUndistorted2kPlateNodeName); 
	  stage.build();	  
	}

	String submitNodeName = pShotNamer.getTrackingSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(verifyCompNodeName); 

	  TargetStage stage = 
	    new TargetStage(pStageInfo, pContext, pClient, 
			    submitNodeName, sources); 
	  addTaskAnnotation(stage, NodePurpose.Submit); 
	  stage.build(); 
	  addToQueueList(submitNodeName);
	  addToCheckInList(submitNodeName);
	}
      }

      /* the approve network */ 
      {
	String extractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
	{
	  BuildTrackingExtractStage stage = 
	    new BuildTrackingExtractStage
	    (pStageInfo, pContext, pClient, 
	     extractedCameraNodeName, pTrackNodeName, pTrackExtractCameraNodeName, 
	     pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String extractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
	{
	  BuildTrackingExtractStage stage = 
	    new BuildTrackingExtractStage
	    (pStageInfo, pContext, pClient, 
	     extractedTrackNodeName, pTrackNodeName, pTrackExtractTrackingNodeName, 
	     pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approveNodeName = pShotNamer.getTrackingApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(extractedCameraNodeName); 
	  sources.add(extractedTrackNodeName); 

	  TargetStage stage = 
	    new TargetStage(pStageInfo, pContext, pClient, 
			    approveNodeName, sources); 
	  addTaskAnnotation(stage, NodePurpose.Approve); 
	  stage.build(); 
	  addToQueueList(approveNodeName);
	  addToCheckInList(approveNodeName);
	}
      }
    }

    private static final long serialVersionUID = -3481579311780824310L; 
  }
   
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2163808030372775045L; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The frame range of the shot.
   */ 
  private FrameRange pFrameRange; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and verify the tracking test render Maya scene.
   */ 
  private String pTrackPrepNodeName;  

  /**
   * The fully resolved name of the node containing the combined pre-render MEL 
   * script for the tracking verification test renders.
   */ 
  private String pTrackVerifyRenderNodeName;  

  /**
   * The fully resolved name of the node for the Maya scene containing 
   * the camera/model tracking data.
   */ 
  private String pTrackNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script which creates a 
   * worldspace duplicate of the tracked camera with baked animation and saves it into 
   * a clean scene.
   */ 
  private String pTrackExtractCameraNodeName;  

  /**
   * The fully resolved name of the node containing a MEL script which creates a 
   * worldspace locator with baked animation and saves it into a clean scene.
   */ 
  private String pTrackExtractTrackingNodeName;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a Maya scene which provides the 
   * test rig used in the tracking verification test renders.
   */ 
  private String pRorschachVerifyModelNodeName;  

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the tracking verification test renders.
   */ 
  private String pRorschachTestShadersNodeName;  

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test lights used in the tracking verification test renders.
   */ 
  private String pRorschachTestLightsNodeName;  

}

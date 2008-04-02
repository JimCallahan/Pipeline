// $Id: PlatesBuilder.java,v 1.26 2008/04/02 20:56:16 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L A T E S   B U I L D E R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Plates task.<P> 
 * 
 * Contains the scanned plate images, camera data and any other reference images shot on 
 * set.  Any required painting fixes are applied and then the images are undistored and 
 * linearized.  A GridWarp Nuke node is produced which can be used to redistort rendered
 * images later along with a MEL script to set the undistored image resolution for renders.
 * Finally, the undistored plates are resized down to 1k, a QuickTime movie is built and 
 * a thumbnail image is extracted. <P> 
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
 *   <P> 
 * 
 *   Background Plate<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The prefix of the existing scanned images node to use as the background plates for
 *     this shot.
 *   </DIV> <BR>
 * </DIV> 
 */
public 
class PlatesBuilder 
  extends BaseShotBuilder 
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
  PlatesBuilder
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
  PlatesBuilder
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
    super("Plates",
          "A builder for constructing the nodes associated with the Plates task.", 
          mclient, qclient, builderInfo, studioDefs, 
	  projectNamer, shotNamer, TaskType.Plates); 

    /* initialize fields */ 
    {
      pMiscReferenceNodeNames = new TreeSet<String>(); 
    }

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 

      /* the background plate images */ 
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aBackgroundPlate, 
           "Select the existing scanned images node to use as the background plates for " + 
           "this shot."); 
        addParam(param);
      }
    }
     
    /* initialize the project namer */ 
    initProjectNamer(); 
    
    /* create the setup passes */ 
    {
      addSetupPass(new PlatesSetupShotEssentials());
      addSetupPass(new SetupImageParams());
      addSetupPass(new GetPrerequisites());
    }

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
      
      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("GetPrerequisites", true);
        sub.addEntry(1, aBackgroundPlate);

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
  public MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();	
    plugins.add(new PluginContext("LensInfo", "ICVFX"));	
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("Copy"));   		
    plugins.add(new PluginContext("MayaResolution"));  
    plugins.add(new PluginContext("NukeSubstComp")); 		
    plugins.add(new PluginContext("NukeRead"));			
    plugins.add(new PluginContext("NukeRescale")); 		
    plugins.add(new PluginContext("NukeThumbnail"));   		
    plugins.add(new PluginContext("HfsRender", "Temerity", 
				  new Range<VersionID>(new VersionID("2.4.1"), null))); 
    plugins.add(new PluginContext("HfsReadCmd"));          
    plugins.add(new PluginContext("DjvUnixQt"));			

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class PlatesSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    PlatesSetupShotEssentials()
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
	pPlatesCameraGridNodeName = pProjectNamer.getPlatesCameraGridNode();
	pRequiredNodeNames.add(pPlatesCameraGridNodeName); 

	pPlatesUndistortNukeNodeName = pProjectNamer.getPlatesUndistortNukeNode();
	pRequiredNodeNames.add(pPlatesUndistortNukeNodeName); 

	pPlatesUndistortHipNodeName = pProjectNamer.getPlatesUndistortHipNode();
	pRequiredNodeNames.add(pPlatesUndistortHipNodeName); 
      }
    }
    
    private static final long serialVersionUID = -6691101175651749909L;
  }

    
  /*----------------------------------------------------------------------------------------*/

  private
  class SetupImageParams
  extends SetupPass
  {
    public 
    SetupImageParams()
    { 
      super("Setup Image Params", 
            "Setup the actual source image parameters.");
    }

    /**
     * Replace the placeholder ReferenceImages and BackgroundPlate parameters with a 
     * real ones.
     */ 
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      {
	Path path = pShotNamer.getPlatesScannedParentPath();
	ArrayList<String> pnames = findChildNodeNames(path); 
	if((pnames == null) || pnames.isEmpty()) 
	  throw new PipelineException
	    ("Unable to find any scanned image nodes in (" + path + ")!"); 

	EnumUtilityParam param =
          new EnumUtilityParam
          (aBackgroundPlate, 
           "Select the existing scanned images node to use as the background plates for " + 
           "this shot.", 
           pnames.get(0), pnames); 
        
        replaceParam(param);
      }
    }

    private static final long serialVersionUID = 5742118589827518495L;
  }

  
  /*----------------------------------------------------------------------------------------*/

  private
  class GetPrerequisites
  extends SetupPass
  {
    public 
    GetPrerequisites()
    {
      super("Get Prerequisites", 
            "Get the names of the prerequitsite nodes."); 
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase() 
      throws PipelineException 
    {
      /* the background plates node */ 
      {
        String bgName = (String) getParamValue(aBackgroundPlate);
        if(bgName == null) 
          throw new PipelineException
            ("No " + aBackgroundPlate + " image node was selected!"); 
        Path path = new Path(pShotNamer.getPlatesScannedParentPath(), bgName); 
	
	try {
	  NodeVersion vsn = pClient.getCheckedInVersion(path.toString(), null); 
	  pFrameRange = vsn.getPrimarySequence().getFrameRange(); 
	  pBackgroundPlateNodeName = vsn.getName(); 
	  pRequiredNodeNames.add(pBackgroundPlateNodeName); 
	}
	catch(PipelineException ex) {
	  throw new PipelineException
	    ("Somehow no checked-in version of the " + aBackgroundPlate + " node " + 
	     "(" + path + ") exists!"); 
	}
      }

      /* the miscellaneous reference images */ 
      {
        Path mpath = pShotNamer.getPlatesMiscReferenceParentPath(); 
        for(String rname : findChildNodeNames(mpath)) {
          Path path = new Path(mpath, rname);
          String nodeName = path.toString();
          pMiscReferenceNodeNames.add(nodeName); 
          pRequiredNodeNames.add(nodeName); 
        }
      }

      /* get the required lens distortion images exported from PFTrack 
          and validate the frame range of these images */ 
      { 
	FrameRange dotGridRange = null;
	{
	  String nname = pShotNamer.getDotGridImageNode(); 
	  try {
	    NodeVersion vsn = pClient.getCheckedInVersion(nname, null); 
	    dotGridRange = vsn.getPrimarySequence().getFrameRange(); 
	    pDotGridImageNodeName = vsn.getName(); 
	    pRequiredNodeNames.add(pDotGridImageNodeName); 
	  }
	  catch(PipelineException ex) {
	    throw new PipelineException
	      ("Somehow no checked-in version of the Dot Grid images node " + 
	       "(" + nname + ") exported from PFTrack exists!"); 
	  }
	}

	FrameRange uvWedgeRange = null;
	{
	  String nname = pShotNamer.getUvWedgeImageNode(); 
	  try {
	    NodeVersion vsn = pClient.getCheckedInVersion(nname, null); 
	    uvWedgeRange = vsn.getPrimarySequence().getFrameRange(); 
	    pUvWedgeImageNodeName = vsn.getName(); 
	    pRequiredNodeNames.add(pUvWedgeImageNodeName); 
	  }
	  catch(PipelineException ex) {
	    throw new PipelineException
	      ("Somehow no checked-in version of the UV Wedge images node " + 
	       "(" + nname + ") exported from PFTrack exists!"); 
	  }
	}
	
	if(!dotGridRange.equals(uvWedgeRange)) 
	  throw new PipelineException
	    ("Somehow the frame range of the Dot Grid images (" + dotGridRange + ") was " + 
	     "not consistent with the frame range of the UV Wedge images " + 
	     "(" + uvWedgeRange + ")!"); 

	if(!dotGridRange.isSingle() && !dotGridRange.equals(pFrameRange)) 
	  throw new PipelineException
	    ("Somehow the frame range of the Dot Grid and UV Wedge images " + 
	     "(" + dotGridRange + ") was more than a single frame, but is not consistent " + 
	     "with the frame range of the plates (" + pFrameRange + ")!"); 

	pDistortRange = dotGridRange; 
      }
    }

    private static final long serialVersionUID = 7830642124642481656L;
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
      StageInformation stageInfo = getStageInformation();

      /* stage prerequisites */ 
      {
	/* lock the latest version of all of the prerequisites */ 
	lockNodePrerequisites(); 

	String prereqNodeName = pShotNamer.getPlatesPrereqNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.addAll(pRequiredNodeNames); 

	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    prereqNodeName, sources); 
	  addTaskAnnotation(stage, NodePurpose.Prereq); 
	  stage.build(); 
	  addToQueueList(prereqNodeName);
	  addToCheckInList(prereqNodeName);
	}
      }

      /* add Edit annotations to all undistort images, reference images and plates */ 
      {
	addMissingTaskAnnotation(pDotGridImageNodeName, NodePurpose.Edit); 
	addMissingTaskAnnotation(pUvWedgeImageNodeName, NodePurpose.Edit); 
	addMissingTaskAnnotation(pBackgroundPlateNodeName, NodePurpose.Edit); 
	for(String name : pMiscReferenceNodeNames) 
	  addMissingTaskAnnotation(name, NodePurpose.Edit); 
      }

      /* the submit network */
      {
	String vfxRefNodeName = pShotNamer.getVfxReferenceNode(NodePurpose.Prepare); 
	{
	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  LensInfoStage stage = 
	    new LensInfoStage(stageInfo, pContext, pClient, 
			      vfxShotDataNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	}

	String dotGridCommandNodeName = pShotNamer.getDotGridCommandNode();
	{
	  HfsReadCmdStage stage = 
	    new HfsReadCmdStage(stageInfo, pContext, pClient,
				dotGridCommandNodeName, pDotGridImageNodeName, 
				"/img/IMAGES_FROM_PFTRACK/UNDISTORTED_GRID", "filename"); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String uvWedgeCommandNodeName = pShotNamer.getUvWedgeCommandNode(); 
	{
	  HfsReadCmdStage stage = 
	    new HfsReadCmdStage(stageInfo, pContext, pClient,
				uvWedgeCommandNodeName, pUvWedgeImageNodeName, 
				"/img/IMAGES_FROM_PFTRACK/UNDISTORTED_WEDGE", "filename"); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	pRenderUvCommandNodeName = pShotNamer.getRenderUvCommandNode();
	{
	  LinkedList<String> sources = new LinkedList<String>();  
	  sources.add(dotGridCommandNodeName); 
	  sources.add(uvWedgeCommandNodeName); 
	  
	  CatScriptStage stage = 
	    new CatScriptStage(stageInfo, pContext, pClient, 
			       pRenderUvCommandNodeName, "cmd", sources);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String undistortUvImageNodeName = pShotNamer.getUndistortUvImageNode();
	{
	  HfsRenderStage stage = 
	    new HfsRenderStage(stageInfo, pContext, pClient,
			       undistortUvImageNodeName, pDistortRange, 4, "tif", 
			       pPlatesUndistortHipNodeName, "undistort_uv_map", null, false, 
			       pRenderUvCommandNodeName, null, null, null); 
	  stage.addLink(new LinkMod(pPlatesCameraGridNodeName, LinkPolicy.Dependency));  
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	pReadUndistortUvImageNodeName = pShotNamer.getReadUndistortUvImageNode();
	{
	  NukeReadStage stage = 
	    new NukeReadStage(stageInfo, pContext, pClient, 
			      pReadUndistortUvImageNodeName, undistortUvImageNodeName, 
			      "Nearest Frame"); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	pReadPlatesNodeName = pShotNamer.getReadPlatesNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(stageInfo, pContext, pClient, 
			      pReadPlatesNodeName, pBackgroundPlateNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	pUndistorted2kPlateNodeName = pShotNamer.getUndistorted2kPlateNode(); 
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>(); 
	  subst.put(pReadUndistortUvImageNodeName, "uvImage"); 
	  subst.put(pReadPlatesNodeName, "plate"); 

	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	    (stageInfo, pContext, pClient, 
	     pUndistorted2kPlateNodeName, pFrameRange, 4, "tif", 
	     "Append & Process", pPlatesUndistortNukeNodeName, subst); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String undistorted2kPlateThumbNodeName = pShotNamer.getUndistorted2kPlateThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage
	      (stageInfo, pContext, pClient,
	       undistorted2kPlateThumbNodeName, "tif", pUndistorted2kPlateNodeName, 
	       1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getPlatesSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(vfxRefNodeName);
	  sources.add(vfxShotDataNodeName);
	  sources.add(undistorted2kPlateThumbNodeName);

	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    submitNodeName, sources); 
	  addTaskAnnotation(stage, NodePurpose.Submit); 
	  stage.build(); 
	  addToQueueList(submitNodeName);
	  addToCheckInList(submitNodeName);
	}
      }

      /* the approve network */ 
      {
	String vfxRefNodeName = pShotNamer.getVfxReferenceNode(NodePurpose.Product); 
	{
	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approvedUndistorted2kCineonPlateNodeName = 
	  pShotNamer.getApprovedUndistorted2kCineonPlateNode(); 
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>(); 
	  subst.put(pReadUndistortUvImageNodeName, "uvImage"); 
	  subst.put(pReadPlatesNodeName, "plate"); 

	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	    (stageInfo, pContext, pClient, 
	     approvedUndistorted2kCineonPlateNodeName, pFrameRange, 4, "cin", 
	     "Append & Process", pPlatesUndistortNukeNodeName, subst); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approvedUndistorted2kPlateNodeName = 
	  pShotNamer.getApprovedUndistorted2kPlateNode(); 
	{
	  CopyImagesStage stage = 
	    new CopyImagesStage
	      (stageInfo, pContext, pClient, 
	       approvedUndistorted2kPlateNodeName, pFrameRange, 4, "tif", 
	       pUndistorted2kPlateNodeName);  
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}


	String undistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
	{
	  NukeRescaleStage stage = 
	    new NukeRescaleStage(stageInfo, pContext, pClient,
				 undistorted1kPlateNodeName, pFrameRange, 4, "tif",
				 approvedUndistorted2kPlateNodeName, 0.5);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String undistorted1kQuickTimeNodeName = pShotNamer.getUndistorted1kQuickTimeNode(); 
	{
	  DjvUnixQtStage stage = 
	    new DjvUnixQtStage
	      (stageInfo, pContext, pClient,
	       undistorted1kQuickTimeNodeName, undistorted1kPlateNodeName, "24");
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  addTaskAnnotation(vfxShotDataNodeName, NodePurpose.Product);   
	}
	
	String resolutionNodeName = pShotNamer.getResolutionNode();
	{
	  MayaResolutionStage stage = 
	    new MayaResolutionStage(stageInfo, pContext, pClient,
				    resolutionNodeName, pUndistorted2kPlateNodeName, 1.0); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String redistortUvImageNodeName = pShotNamer.getRedistortUvImageNode();
	{
	  HfsRenderStage stage = 
	    new HfsRenderStage(stageInfo, pContext, pClient,
			       redistortUvImageNodeName, pDistortRange, 4, "tif", 
			       pPlatesUndistortHipNodeName, "redistort_uv_map", null, false, 
			       pRenderUvCommandNodeName, null, null, null); 
	  stage.addLink(new LinkMod(pPlatesCameraGridNodeName, LinkPolicy.Dependency));  
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approveNodeName = pShotNamer.getPlatesApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(approvedUndistorted2kCineonPlateNodeName);
	  sources.add(undistorted1kQuickTimeNodeName);
	  sources.add(resolutionNodeName);
	  sources.add(vfxRefNodeName);
	  sources.add(vfxShotDataNodeName);
	  sources.add(redistortUvImageNodeName);

	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    approveNodeName, sources); 
	  addTaskAnnotation(stage, NodePurpose.Approve); 
	  stage.build(); 
	  addToQueueList(approveNodeName);
	  addToCheckInList(approveNodeName);
	}
      }
    }

    private static final long serialVersionUID = -5216068758078265108L;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4601321412376464762L;
  
  public final static String aReferenceImages = "ReferenceImages";
  public final static String aBackgroundPlate = "BackgroundPlate";

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the undistorted camera grid geometry 
   * file.
   */ 
  private String pPlatesCameraGridNodeName; 

  /**
   * The fully resolved name of the node containing the master Nuke script used to 
   * undistort the plates. 
   */ 
  private String pPlatesUndistortNukeNodeName; 

  /**
   * The fully resolved name of the node containing Houdini scene which generates the
   * undistored UV map used by the master undistort Nuke script.
   */ 
  private String pPlatesUndistortHipNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name and frame range of background plates node. 
   */ 
  private String pBackgroundPlateNodeName;
  private FrameRange pFrameRange;
  private FrameRange pDistortRange; 
  
  /**
   * The fully resolved names of all reference image nodes.
   */ 
  private TreeSet<String> pMiscReferenceNodeNames; 

  /**
   * The fully resolved name of the node containing the undistorted grid dots image
   * exported from PFTrack and the Houdini command file which imports it.
   */ 
  private String pDotGridImageNodeName;
  private String pDotGridCommandNodeName;

  /**
   * The fully resolved name of the node containing the undistorted UV map image
   * exported from PFTrack and the Houdini command file which imports it.
   */ 
  private String pUvWedgeImageNodeName;
  private String pUvWedgeCommandNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing combined pre-render Houdini
   * command script which loads the undistorted UV wedge and grid dots images.
   */ 
  private String pRenderUvCommandNodeName; 

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  private String pUndistorted2kPlateNodeName; 

  /**
   * The fully resolved name of the node containing Nuke script to read the 
   * undistorted UV image.
   */ 
  private String pReadUndistortUvImageNodeName; 

  /**
   * The fully resolved name of the node contaning the Nuke script fragment
   * use to read in the raw cineon plates.
   */ 
  private String pReadPlatesNodeName;

}

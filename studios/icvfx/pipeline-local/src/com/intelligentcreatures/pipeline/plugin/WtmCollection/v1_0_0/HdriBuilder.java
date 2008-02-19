// $Id: HdriBuilder.java,v 1.1 2008/02/19 09:26:36 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H D R I   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the HDRI task.<P> 
 * 
 * Requires that the raw multiple exposure digital images shot on set have already been 
 * registered and checked-in.  This builder will create a single LatLon format HDR
 * environment map suitable for use in HDRI lighting as well as a number of diagnostic
 * images to validate and/or debug the process. The input images should be checked-in with 
 * the following names: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   /projects/wtm/shots/SEQ/SHOT/hdri/edit/raw/SEQSHOT_rawA.@.hdr
 *   /projects/wtm/shots/SEQ/SHOT/hdri/edit/raw/SEQSHOT_rawB.@.hdr
 *   /projects/wtm/shots/SEQ/SHOT/hdri/edit/raw/SEQSHOT_rawC.@.hdr
 * </DIV> <P> 
 * 
 * Where SEQ is the two letter shot sequence name and SHOT is the three digit shot number.
 * The "a", "b" and "c" image sequences contain the exposures from each of the 120 degree
 * horizontally offset angular fisheye views.  If the images are in raw format, the exposure
 * times will be read from the image headers.  Alternatively, an exposure text file can be
 * registered to manually specify the exposure times here: 
 * 
 * <DIV style="margin-left: 40px;">
 *   /projects/wtm/shots/SEQ/SHOT/hdri/edit/SEQSHOT_exp_times.txt
 * </DIV> <P> 
 * 
 * See the NukeMakeHDR Action plugin for details about the format of this file.<P> 
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
class HdriBuilder 
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
  HdriBuilder
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
  HdriBuilder
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
    super("HDRI",
          "A builder for constructing the nodes associated with the HDRI task.", 
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

    /* initialize fields */ 
    pRequiredNodeNames = new TreeSet<String>(); 

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 
    }
     
    /* initialize the project namer */ 
    initProjectNamer(); 
    
    /* create the setup passes */ 
    {
      addSetupPass(new BaseSetupShotEssentials()); 
      addSetupPass(new GetPrerequisites());
    }

    /* setup the default editors */ 
    setCommonDefaultEditors(); 

    /* create the construct passes */ 
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);
      
      ConstructPass qd = new QueueDisablePass(); 
      addConstructPass(qd); 
      addPassDependency(build, qd);
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
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("GetPrerequisites", true);
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
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("Copy"));   		
    plugins.add(new PluginContext("CatFiles"));  		
    plugins.add(new PluginContext("MayaFTNBuild"));  		
    plugins.add(new PluginContext("NukeMakeHDR")); 		
    plugins.add(new PluginContext("NukeExtract"));		

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param builderID
   *   The unique ID of the approval builder.
   */ 
  protected void
  addAproveTaskAnnotation
  (
   BaseStage stage, 
   BuilderID builderID
  )
    throws PipelineException
  {
    addApproveTaskAnnotation(stage,
			     pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
			     TaskType.HDRI.toString(), builderID);
  }

  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
   BaseStage stage,
   NodePurpose purpose
  )
    throws PipelineException
  {
    addTaskAnnotation(stage, purpose, 
                      pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
                      TaskType.HDRI.toString()); 
  }

  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
   String nodeName, 
   NodePurpose purpose
  )
    throws PipelineException
  {
    addTaskAnnotation(nodeName, purpose, 
                      pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
                      TaskType.HDRI.toString()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
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
      /* the raw exposure images nodes */ 
      {
	pRawExposuresNodeNames = pShotNamer.getRawExposuresNodes();
	pRequiredNodeNames.addAll(pRawExposuresNodeNames); 
      }

      /* optional exposure times node */ 
      try {
	String name = pShotNamer.getExposureTimesNode();
	pClient.getCheckedInVersion(name, null); 
	pExposureTimesNodeName = name;
	pRequiredNodeNames.add(pExposureTimesNodeName); 
      }
      catch(PipelineException ex) {
      }
    }

    private static final long serialVersionUID = -5099535348108847245L;
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
            "Creates the nodes which make up the HDRI task."); 
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
      for(String name : pRequiredNodeNames) {
	if(!nodeExists(name)) 
	  throw new PipelineException
	    ("The required prerequisite node (" + name + ") does not exist!"); 
	lockLatest(name); 
      }

      /* add Edit annotations to all raw exposure images and times */ 
      {
	for(String name : pRawExposuresNodeNames) 
	  addTaskAnnotation(name, NodePurpose.Edit); 

	if(pExposureTimesNodeName != null)
	  addTaskAnnotation(pExposureTimesNodeName, NodePurpose.Edit); 
      }

      /* the submit network */
      {
	pDiagnosticHdrImageNodeName = pShotNamer.getDiagnosticHdrImageNode();
	{
	  NukeMakeHDRStage stage = 
	    new NukeMakeHDRStage(pStageInfo, pContext, pClient, 
				 pDiagnosticHdrImageNodeName, pExposureTimesNodeName, 
				 pRawExposuresNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getHdriSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(pDiagnosticHdrImageNodeName); 

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
	String finalHdrImageNodeName = pShotNamer.getFinalHdrImageNode();
	{
	  PrimaryHDRStage stage = 
	    new PrimaryHDRStage(pStageInfo, pContext, pClient, 
				finalHdrImageNodeName, pDiagnosticHdrImageNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String finalHdrMayaNodeName = pShotNamer.getFinalHdrMayaNode(); 
	{
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(pStageInfo, pContext, pClient, 
				  new MayaContext(), finalHdrMayaNodeName, true); 
	  stage.addLink(new LinkMod(finalHdrImageNodeName, LinkPolicy.Dependency));
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}
        
	String approveNodeName = pShotNamer.getHdriApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(finalHdrMayaNodeName);

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

    private static final long serialVersionUID = -4352076885253199108L;
  }
   


  /*----------------------------------------------------------------------------------------*/

  protected 
  class QueueDisablePass
    extends ConstructPass
  {
    public 
    QueueDisablePass() 
    {
      super("Queue and Disable Actions", 
	    "");
    }
    
    /**
     * Return nodes which will have their actions disabled to be queued now.
     */ 
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      return getDisableList();
    }
    
    /**
     * Disable the actions for the second pass nodes. 
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      disableActions();
    }
 
    private static final long serialVersionUID = 4975463324007330401L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7935206859405710109L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of nodes required to exist for this builder to run. 
   */ 
  private TreeSet<String> pRequiredNodeNames;

  /** 
   * The fully resolved names of the nodes containing the series of varying exposure 
   * raw digital images used to construct the single high dynamic range (HDR) environment 
   * map.
   */ 
  private ArrayList<String> pRawExposuresNodeNames;
  
  /**
   * The fully resolved name of the node containing the plain text exposure times.
   */
  private String pExposureTimesNodeName;

  /**
   * The fully resolved name of the node containing the combined LatLon format 
   * HDR environment map and diagnostic images.
   */ 
  private String pDiagnosticHdrImageNodeName; 

}

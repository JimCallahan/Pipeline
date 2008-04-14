// $Id: QtDeliverBuilder.java,v 1.4 2008/04/14 01:38:05 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;
import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q T   D E L I V E R   B U I L D E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes required to prepare an image sequence for delivery 
 * to the client or for internal review. <P> 
 * 
 * This includes generating slates, adding per-frame overlays, reformatting the images to 
 * a specific resolution and converting to a QuickTime movie.<P> 
 * 
 * Besides the common parameters shared by all builders, this builder defines the following
 * additional parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Source Node<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved name of the node containing the images to be delivered.
 *   </DIV> <BR>
 * 
 *   Source Version <BR>
 *   <DIV style="margin-left: 40px;">
 *     The revision number of the source images node being delivered.
 *   </DIV> <BR>
 *   <P> 
 * 
 * 
 *   Delivery Type<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The reason the deliverable was created.
 *   </DIV> <BR>
 * 
 *   Deliverable<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The name for the content of the images being delivered to the client.  Typically this
 *     will be based on a combination of the shot (or asset) and Pipeline task which 
 *     generated the images such as: "ri120-blot" or "rorschach-model".  
 *   </DIV> <BR>
 * 
 *   Client Version<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The client revision number.  This revision number is unrelated to Pipeline's revision
 *     number for the source images and is purely for external client use.  
 *   </DIV> <BR>
 * 
 *   Notes <BR>
 *   <DIV style="margin-left: 40px;">
 *     A short description of the Deliverable to be included in the image slates.  If not
 *     specified the check-in message associated with the source images node will be used 
 *     instead.
 *   </DIV> <BR>
 *   <P> 
 *   
 *   
 *   Slate Script <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The node name prefix of the master slate creation Nuke script to use.
 *   </DIV> <BR>
 * 
 *   Slate Hold <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The number of frames to hold the constant slate image before the images being 
 *     reviewed begin animating. 
 *   </DIV> <BR>
 * 
 *   Format Script <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The node name prefix of the final image formatting Nuke script to use.");  
 *   </DIV> <BR>
 * 
 *   Codec Settings<BR> 
 *   <DIV style="margin-left: 40px;">
 *     The QuickTime codec settings to encode the final movie.
 *   </DIV> <BR>
 * </DIV> 
 */
public 
class QtDeliverBuilder 
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
  QtDeliverBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("QtDeliver",
          "A builder for constructing the nodes required to prepare an image sequence " + 
	  "for delivery to the client or for internal review.", 
          mclient, qclient, builderInfo, 
	  new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
	  null, null, null); 
   
    /* setup builder parameters */ 
    {
      /* hide and set parameters which shouldn't be visible to the user */ 
      {
	disableParam(new ParamMapping(BaseUtil.aUtilContext, 
				      UtilContextUtilityParam.aAuthor));
	disableParam(new ParamMapping(BaseUtil.aUtilContext, 
				      UtilContextUtilityParam.aView));

	//disableParam(new ParamMapping(aCheckinWhenDone));
	setParamValue(new ParamMapping(aCheckinWhenDone), true);

	disableParam(new ParamMapping(aReleaseOnError));
	setParamValue(new ParamMapping(aReleaseOnError), true);

	disableParam(new ParamMapping(aActionOnExistence));
	setParamValue(new ParamMapping(aActionOnExistence), 
		      ActionOnExistence.Conform.toString()); 
      }

      /* the source images being delivered */ 
      {
        UtilityParam param = 
          new NodePathUtilityParam
          (aSourceNode, 
	   "The fully resolved name of the node containing the images to be delivered.", 
	   new Path("/")); 
        addParam(param);
      }

      {
        UtilityParam param = 
          new StringUtilityParam
          (aSourceVersion, 
	   "The revision number of the source images node being delivered.",
	   null); 
        addParam(param);
      }

      /* deliverable info */ 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Test");
	choices.add("Temp");
	choices.add("For Approval");
	choices.add("For Preview");
	choices.add("Final");
	
	UtilityParam param = 
	  new EnumUtilityParam
	  (aDeliveryType, 
	   "The reason the deliverable was created.",
	   "For Approval", choices);
	addParam(param);
      }

      {
        UtilityParam param = 
          new StringUtilityParam
          (DeliverNamer.aDeliverable, 
	   "The name for the content of the images being delivered to the client. " + 
	   "Typically this will be based on a combination of the shot (or asset) and " + 
	   "Pipeline task which generated the images such as: \"ri120-blot\" or " + 
	   "\"rorschach-model\".", 
	   null); 
        addParam(param);	
      }

      {
        UtilityParam param = 
          new StringUtilityParam
	  (aClientVersion, 
	   "The client revision number.  This revision number is unrelated to Pipeline's " + 
	   "revision number for the source images and is purely for external client use.", 
	   null); 
        addParam(param);	
      }
      
      {
        UtilityParam param = 
          new StringUtilityParam
	  (aNotes, 
	   "A short description of the Deliverable to be included in the image slates.  " + 
	   "If not specified the check-in message associated with the source images node " + 
	   "will be used instead.", 
	   null); 
        addParam(param);	
      }
      
      /* Nuke components */ 
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aSlateScript, 
           "Select the master slate creation Nuke script to use."); 
        addParam(param);
      }

      {
        UtilityParam param = 
          new IntegerUtilityParam
	  (aSlateHold, 
	   "The number of frames to hold the constant slate image before the images being " +
	   "reviewed begin animating.", 
	   1);
        addParam(param);
      }

      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aFormatScript, 
           "Select the final image formatting Nuke script to use.");  
        addParam(param);
      }

      {	
	UtilityParam param = 
          new PlaceholderUtilityParam
          (aCodecSettings, 
           "Select the QuickTime codec settings to encode the final movie.");
	addParam(param);
      }
    }
 
    /* create the setup passes */ 
    {
      addSetupPass(new QuickTimeEssentials());
      addSetupPass(new SetupDeliveryParams());
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
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("QuickTimeEssentials", true);

        sub.addEntry(1, aUtilContext);
        sub.addEntry(1, null);
        sub.addEntry(1, aCheckinWhenDone);
        sub.addEntry(1, aActionOnExistence);
        sub.addEntry(1, aReleaseOnError);
        sub.addEntry(1, null);
	sub.addEntry(1, aSourceNode); 
	sub.addEntry(1, aSourceVersion); 

        layout.addPass(sub.getName(), sub); 
      }

      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("None", true);
        layout.addPass(sub.getName(), sub); 
      }

      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("DeliveryDetails", true);
	sub.addEntry(1, aDeliveryType);
	sub.addEntry(1, DeliverNamer.aDeliverable); 
	sub.addEntry(1, aClientVersion); 
	sub.addEntry(1, aNotes); 
        sub.addEntry(1, null);
        sub.addEntry(1, aSlateScript);
	sub.addEntry(1, aSlateHold); 
        sub.addEntry(1, null);
        sub.addEntry(1, aFormatScript);
	sub.addEntry(1, aCodecSettings); 

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
    plugins.add(new PluginContext("Touch")); 			
    plugins.add(new PluginContext("NukeRead", "Temertity", 
                                  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("NukeSubstComp")); 		
    plugins.add(new PluginContext("DjvUnixQt"));			
    plugins.add(new PluginContext("SlateSubst", "ICVFX"));	

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class QuickTimeEssentials
    extends SetupPass
  {
    public 
    QuickTimeEssentials()
    {
      super("QuickTime Essentials", 
            "Setup the common builder properties as well as looking up essential source " + 
	    "node information."); 
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
      /* sets up the built-in parameters common to all builders */ 
      validateBuiltInParams();

      /* setup the StudioDefinitions version of the UtilContext */ 
      pStudioDefs.setContext(pContext);  

      /* lookup the source images node */ 
      String sourceNodeName = null;
      VersionID sourceVersionID = null;
      {
	Path spath = (Path) getParamValue(aSourceNode);
	if(spath == null) 
	  throw new PipelineException
	    ("No " + aSourceNode + " was specified!");
	sourceNodeName = spath.toString(); 
	pSourcePrefix = spath.getName();

	String sversion = (String) getParamValue(aSourceVersion);
	if(sversion == null) 
	  throw new PipelineException
	    ("No " + aSourceVersion + " was specified!");

	try {
	  sourceVersionID = new VersionID(sversion); 
	}
	catch(Exception ex) {
	  throw new PipelineException
	    ("The value supplied for the " + aSourceVersion + " parameter " + 
	     "(" + sversion + ") is not a legal node revision number!\n\n" + 
	     ex.getMessage());
	}

	try {
	  pSourceVersion = pClient.getCheckedInVersion(sourceNodeName, sourceVersionID);
	}
	catch(PipelineException ex) {
	  throw new PipelineException
	    ("The source images node (" + sourceNodeName + " v" + sourceVersionID + ") " + 
	     "does not exist!"); 
	}
      }

      /* set namer/builder parameters based on the annotations on the source images node */
      {
	boolean validated = false;
	String projName = null;
	String taskName = null;
	String taskType = null;
	TreeMap<String,BaseAnnotation> annotations = pClient.getAnnotations(sourceNodeName); 
	TreeSet<String> otherPurposes = new TreeSet<String>(); 
        for(String aname : annotations.keySet()) {
	  if(aname.equals("Task") || aname.startsWith("AltTask")) {
	    BaseAnnotation annot = annotations.get(aname);
	    String purpose = lookupPurpose(sourceNodeName, aname, annot); 
	    if(purpose == null) {
	      otherPurposes.add("<UNKNWON>");
	    }
	    else if(purpose.equals(aFocus) || 
		    purpose.equals(aEdit) || 
		    purpose.equals(aProduct)) { 
	      projName = lookupProjectName(sourceNodeName, aname, annot);
	      taskName = lookupTaskName(sourceNodeName, aname, annot);
	      taskType = lookupTaskType(sourceNodeName, aname, annot);
	      validated = true;
	      break;
	    }
	    else {
	      otherPurposes.add(purpose);
	    }
	  }
	}
	
        if(!validated) {
	  StringBuilder buf = new StringBuilder();
	  buf.append
            ("Unable to find an valid " + aEdit + ", " + aFocus + " or " + aProduct + " " + 
	     "task annotation for the source images node (" + sourceNodeName + " v" + 
	     sourceVersionID + ")!"); 

	  if(!otherPurposes.isEmpty()) {
	    buf.append
	      ("\n\nHowever, there were task annotations on the source images node for " + 
	       "the following unsupported purposes:");
	    for(String purpose : otherPurposes) 
	      buf.append(" " + purpose);
	  }

	  throw new PipelineException(buf.toString());
	}
	
	if(taskName.length() != 5) 
	  throw new PipelineException
	    ("The " + aTaskName + " (" + taskName + ") of the source images " + 
	     "node (" + sourceNodeName + " v" + sourceVersionID + ") did not conform to " + 
	     "the 2-letter sequence name followed by 3-digit shot number format!"); 
	pProjectName = projName;
	pSeqName     = taskName.substring(0, 2);
	pShotName    = taskName.substring(2, 5); 

	try {
	  pTaskType = TaskType.valueOf(TaskType.class, taskType);
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException
	    ("The " + DeliverNamer.aTaskType + " (" + taskType + ") of the source images " + 
	     "node (" + sourceNodeName + " v" + sourceVersionID + ") was an unknown type " + 
	     "by this builder!"); 
	}
      }

      /* generate a temporary working area where the approval process will take place
           and change the util context to use it instead for all future operations */
      { 
	String tempView = ("QtDeliver" + "-" + 
			   pProjectName + "-" + pSeqName + pShotName + "-" + 
			   pSourcePrefix); 
        tempView = tempView.replaceAll(" ", "_");
	
	setContext(new UtilContext(pContext.getAuthor(), tempView, pContext.getToolset()));
      }

      /* turn on the DoAnnotations flag for the StageInformation shared by all 
         of the Stages created by this builder since we always want task annotations */
      getStageInformation().setDoAnnotations(true);  
   
      /* initialize internal Project namer */ 
      {
	pProjectNamer.setParamValue
	  (new ParamMapping(StudioDefinitions.aProjectName), pProjectName); 
	pProjectNamer.generateNames();
      }
    }

    private static final long serialVersionUID = -8543166852752126981L;
  }


  /*----------------------------------------------------------------------------------------*/
    
  private
  class SetupDeliveryParams
    extends SetupPass
  {
    public 
    SetupDeliveryParams()
    {
      super("Setup Delivery Params", 
            "Setup the specific delivery parameters based on the source images node."); 
    }
   
    /**
     *
     */ 
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      /* initialize builder parameters from source images node information */ 
      setParamValue
	(new ParamMapping(DeliverNamer.aDeliverable), pSourcePrefix); 
      setParamValue
	(new ParamMapping(aNotes), pSourceVersion.getMessage());
      setParamValue
	(new ParamMapping(aClientVersion), pSourceVersion.getVersionID().toString()); 

      /* replace placeholder parameters with the names of the available 
	   slate script, format script and codec settings nodes */ 
      {	
	Path path = pProjectNamer.getSlateNukeScriptsParentPath();
	ArrayList<String> pnames = findChildNodeNames(path); 
	if((pnames == null) || pnames.isEmpty()) 
	  throw new PipelineException
	    ("Unable to find any slate creation Nuke script nodes in (" + path + ")!"); 
	
	UtilityParam param =
          new EnumUtilityParam
          (aSlateScript, 
	   "Select the master slate creation Nuke script to use.", 
	   pnames.get(0), pnames); 
        replaceParam(param);
      }

      {
	Path path = pProjectNamer.getFormatNukeScriptsParentPath();
	ArrayList<String> pnames = findChildNodeNames(path); 
	if((pnames == null) || pnames.isEmpty()) 
	  throw new PipelineException
	    ("Unable to find any image formatting Nuke script nodes in (" + path + ")!"); 

	UtilityParam param =
          new EnumUtilityParam
          (aFormatScript, 
	   "Select final image formatting Nuke script to use.", 
	   pnames.get(0), pnames); 
        replaceParam(param);
      }

      {
	Path path = pProjectNamer.getQtCodecSettingsParentPath();
	ArrayList<String> pnames = findChildNodeNames(path); 
	if((pnames == null) || pnames.isEmpty()) 
	  throw new PipelineException
	    ("Unable to find any QuickTime codec settings nodes in (" + path + ")!"); 

	UtilityParam param =
	  new EnumUtilityParam
	  (aCodecSettings, 
	   "Select the QuickTime codec settings to encode the final movie.", 
	   pnames.get(0), pnames); 
        replaceParam(param);
      }
    }

    private static final long serialVersionUID = 1137715625182689459L;
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
            "Get the names of the prerequisite nodes."); 
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase() 
      throws PipelineException 
    {
      /* the slate and format Nuke script nodes */ 
      String slatePrefix = null;
      {
        String script = getStringParamValue(new ParamMapping(aSlateScript), false);
        Path path = new Path(pProjectNamer.getSlateNukeScriptsParentPath(), script); 
	pSlateNodeName = path.toString(); 
	slatePrefix = path.getName();
	pRequiredNodeNames.add(pSlateNodeName);
      }

      String formatPrefix = null;
      {
        String script = getStringParamValue(new ParamMapping(aFormatScript), false);
        Path path = new Path(pProjectNamer.getFormatNukeScriptsParentPath(), script); 
	pFormatNodeName = path.toString(); 
	formatPrefix = path.getName();
	pRequiredNodeNames.add(pFormatNodeName);
      }

      /* the final QuickTime codec settings */ 
      String codecPrefix = null;
      {
        String settings = getStringParamValue(new ParamMapping(aCodecSettings), false);
        Path path = new Path(pProjectNamer.getQtCodecSettingsParentPath(), settings); 
	pCodecNodeName = path.toString(); 
	codecPrefix = path.getName();
	pRequiredNodeNames.add(pCodecNodeName);
      }

      /* lookup the rest of the parameters */ 
      pDeliveryType = getStringParamValue(new ParamMapping(aDeliveryType));
      pDeliverable = getStringParamValue(new ParamMapping(DeliverNamer.aDeliverable), false);
      pClientVersion = getStringParamValue(new ParamMapping(aClientVersion));
      pNotes = getStringParamValue(new ParamMapping(aNotes));
      pSlateHold = getIntegerParamValue(new ParamMapping(aSlateHold), 
					    new Range<Integer>(0, null));

      /* compute the full frame range with slate holds added */ 
      FrameRange range = pSourceVersion.getPrimarySequence().getFrameRange();
      if(range.getBy() != 1) 
	throw new PipelineException
	  ("The source images node (" + pSourceVersion.getName() + " v" + 
	   pSourceVersion.getVersionID() + ") must have a frame step increment of (1)!"); 
      pFrameRange = new FrameRange(range.getStart(), range.getEnd()+pSlateHold, 1); 

      /* initialize internal Deliver (Shot) namer */ 
      {
	pDeliverNamer = new DeliverNamer(pClient, pQueue, pStudioDefs);
	pShotNamer = pDeliverNamer;

	pDeliverNamer.setParamValue
	  (new ParamMapping(StudioDefinitions.aProjectName), pProjectName); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(StudioDefinitions.aSequenceName), pSeqName);
	pDeliverNamer.setParamValue
	  (new ParamMapping(StudioDefinitions.aShotName), pShotName); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(DeliverNamer.aDeliverable), pDeliverable); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(DeliverNamer.aTaskType), pTaskType.toString()); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(DeliverNamer.aSlatePrefix), slatePrefix); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(DeliverNamer.aFormatPrefix), formatPrefix); 
	pDeliverNamer.setParamValue
	  (new ParamMapping(DeliverNamer.aCodecPrefix), codecPrefix); 

	pDeliverNamer.generateNames();
      }
    }

    private static final long serialVersionUID = 876194378918748661L;
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
            "Creates the nodes which make up the QuickTime task."); 
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
	
	/* lock the specific version of the source images node we are delivering */ 
	pClient.lock(getAuthor(), getView(), 
		     pSourceVersion.getName(), pSourceVersion.getVersionID()); 
      }

      /* the delivery network */ 
      { 
	String readQtDeliverableImagesNodeName = 
	  pDeliverNamer.getReadQtDeliverableImagesNode();
	{
	  NukeReadStage stage = 
	    new NukeReadStage
	      (stageInfo, pContext, pClient, 
	       readQtDeliverableImagesNodeName, pSourceVersion.getName()); 
	  stage.addSingleParamValue("ReadName", "Images"); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String qtDeliverSlateNukeNodeName = pDeliverNamer.getQtDeliverSlateNukeNode();
	{
	  SlateSubstStage stage = 
	    new SlateSubstStage(stageInfo, pContext, pClient, 
				qtDeliverSlateNukeNodeName, pSlateNodeName, 
				pDeliveryType, pDeliverable, pClientVersion, 
				pSourceVersion, pNotes, pSlateHold); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String qtDeliverSlatedImagesNodeName = pDeliverNamer.getQtDeliverSlatedImagesNode(); 
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>(); 
	  subst.put(readQtDeliverableImagesNodeName, "Images"); 
	  subst.put(pFormatNodeName, "FinalFormat"); 



	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	      (stageInfo, pContext, pClient, 
	       qtDeliverSlatedImagesNodeName, pFrameRange, 4, "jpg", 
	       "Append & Process", qtDeliverSlateNukeNodeName, subst);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String qtDeliverSlatedMovieNodeName = pDeliverNamer.getQtDeliverSlatedMovieNode();
	{
	  DjvUnixQtStage stage = 
	    new DjvUnixQtStage
	      (stageInfo, pContext, pClient,
	       qtDeliverSlatedMovieNodeName, qtDeliverSlatedImagesNodeName, "24");
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String qtDeliverableNodeName = pDeliverNamer.getQtDeliverableNode();
	{
	  QtEncodeStage stage = 
	    new QtEncodeStage
	      (stageInfo, pContext, pClient, 
	       qtDeliverableNodeName, qtDeliverSlatedMovieNodeName, pCodecNodeName); 
 	  addTaskAnnotation(stage, NodePurpose.Deliver); 
 	  stage.build(); 
 	  addToQueueList(qtDeliverableNodeName);
 	  addToCheckInList(qtDeliverableNodeName);
	}
      }
    }

    private static final long serialVersionUID = -8206152183341505182L;
  }
   
  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1915755291666458793L;
  
  public static final String aSourceNode    = "SourceNode";  
  public static final String aSourceVersion = "SourceVersion";  
  
  public static final String aDeliveryType  = "DeliveryType";  
  public static final String aClientVersion = "ClientVersion";  
  public static final String aNotes         = "Notes";  
  public static final String aSlateScript   = "SlateScript"; 
  public static final String aSlateHold     = "SlateHold";  
  public static final String aFormatScript  = "FormatScript";
  public static final String aCodecSettings = "CodecSettings"; 

  public static final String aTaskName = "TaskName";
  public static final String aEdit     = "Edit";
  public static final String aFocus    = "Focus";
  public static final String aProduct  = "Product";



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in version of the source images node and its node name prefix.
   */ 
  private NodeVersion pSourceVersion; 
  private String      pSourcePrefix; 

  /**
   * The frame range of the images with slate hold frames added.
   */
  private FrameRange pFrameRange; 
 
  /**
   * The information extracted from the task annotation on the source images node.
   */ 
  private String pProjectName; 
  private String pSeqName;
  private String pShotName;

  /**
   * The reason the deliverable was created.
   */ 
  private String pDeliveryType; 

  /**
   * The name for the content of the images being delivered to the client. 
   */ 
  private String pDeliverable; 

  /**
   * The client revision number. 
   */ 
  private String pClientVersion; 

  /**
   * A short description of the Deliverable to be included in the image slates.
   */ 
  private String pNotes; 

  /**
   * The node name of the master slate creation Nuke script to use.
   */ 
  private String pSlateNodeName; 

  /**
   * The node name of the final image formatting Nuke script to use.
   */ 
  private String pFormatNodeName; 

  /**
   * The node name of the QuickTime codec settings node.
   */ 
  private String pCodecNodeName;

  /**
   * The number of frames to hold the constant slate image before the images being 
   * reviewed begin animating.
   */ 
  private Integer pSlateHold;     

  /**
   * Provides the names of nodes and node directories which are deliverable and shot specific.
   */
  private DeliverNamer pDeliverNamer;

}

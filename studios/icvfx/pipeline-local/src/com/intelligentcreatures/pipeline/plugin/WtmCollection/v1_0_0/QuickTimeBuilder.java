// $Id: QuickTimeBuilder.java,v 1.2 2008/03/23 05:09:58 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U I C K T I M E   B U I L D E R                                                      */
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
 * </DIV> 
 */
public 
class QuickTimeBuilder 
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
  QuickTimeBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("QuickTime",
          "A builder for constructing the nodes required to prepare an image sequence " + 
	  "for delivery to the client or for internal review.", 
          mclient, qclient, builderInfo, 
	  new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
	  null, null, null); 
   
    /* setup builder parameters */ 
    {
      /* hide and set parameters which shouldn't be visible to the user */ 
      {
	disableParam(new ParamMapping(aCheckinWhenDone));
	//setParamValue(new ParamMapping(aCheckInWhenDone), true);

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
          (aDeliverable, 
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
          new PlaceholderUtilityParam
          (aFormatScript, 
           "Select the final image formatting Nuke script to use.");  
        addParam(param);
      }

      {
        UtilityParam param = 
          new IntegerUtilityParam
	  (aSlateHold, 
	   "The number of frames to hold the constant slate image before the images being " +
	   "reviewed begin animating.", 
	   48);
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
	sub.addEntry(1, aDeliverable); 
	sub.addEntry(1, aClientVersion); 
	sub.addEntry(1, aNotes); 
        sub.addEntry(1, null);
        sub.addEntry(1, aSlateScript);
	sub.addEntry(1, aSlateHold); 
        sub.addEntry(1, null);
        sub.addEntry(1, aFormatScript);

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
    plugins.add(new PluginContext("NukeRead"));	
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
	    else if(purpose.equals(aFocus) || purpose.equals(aEdit)) { 
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
            ("Unable to find an valid " + aEdit + " or " + aFocus + " task annotation for " +
	     "the source images node (" + sourceNodeName + " v" + sourceVersionID + ")!"); 

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
	    ("The " + aTaskName + " (" + taskName + ") of the source images node " + 
	     "(" + sourceNodeName + " v" + sourceVersionID + ") did not conform to the " + 
	     "2-letter sequence name followed by 3-digit shot number format!"); 
	pProjectName = projName;
	pSeqName     = taskName.substring(0, 2);
	pShotName    = taskName.substring(2, 5); 

	try {
	  pTaskType = TaskType.valueOf(TaskType.class, taskType);
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException
	    ("The " + aTaskType + " (" + taskType + ") of the source images node " + 
	     "(" + sourceNodeName + " v" + sourceVersionID + ") was an unknown type by " + 
	     "this builder!"); 
	}
      }

      /* generate a temporary working area where the approval process will take place
           and change the util context to use it instead for all future operations */
      { 
	String tempView = 
	  ("QuickTime" + "-" + pProjectName + "-" + pSeqName + pShotName + "-" + pTaskType); 
        tempView = tempView.replaceAll(" ", "_");
	
	setContext(new UtilContext(pContext.getAuthor(), tempView, pContext.getToolset()));
      }

      /* turn on the DoAnnotations flag for the StageInformation shared by all 
         of the Stages created by this builder since we always want task annotations */
      getStageInformation().setDoAnnotations(true);  
    }

    /**
     * 
     */ 
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      /* initialize internal Project namer */ 
      {
	addSubBuilder(pProjectNamer);

	String pname = pProjectNamer.getName(); 

// 	addMappedParam(pname, 
// 		       new ParamMapping(StudioDefinitions.aProjectName), 
// 		       ParamMapping.NullMapping);
	
	pProjectNamer.setParamValue(new ParamMapping(StudioDefinitions.aProjectName), 
				    pProjectName); 
      }
      
      /* initialize internal Shot namer */ 
      {
	pShotNamer = new ShotNamer(pClient, pQueue, pStudioDefs);
	addSubBuilder(pShotNamer);

	String sname = pShotNamer.getName(); 

// 	addParamMapping(sname, 
// 			new ParamMapping(StudioDefinitions.aProjectName), 
// 			ParamMapping.NullMapping);
			
	pShotNamer.setParamValue(new ParamMapping(StudioDefinitions.aProjectName), 
				 pProjectName); 

// 	addParamMapping(sname, 
// 			new ParamMapping(StudioDefinitions.aSequenceName), 
// 			ParamMapping.NullMapping);

	pShotNamer.setParamValue(new ParamMapping(StudioDefinitions.aSequenceName), pSeqName);

// 	addParamMapping(sname, 
// 			new ParamMapping(StudioDefinitions.aShotName), 
// 			ParamMapping.NullMapping);

	pShotNamer.setParamValue(new ParamMapping(StudioDefinitions.aShotName), pShotName); 
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
      setParamValue(new ParamMapping(aDeliverable), pShotNamer.getDeliverable(pTaskType)); 
      setParamValue(new ParamMapping(aNotes), pSourceVersion.getMessage());
      setParamValue(new ParamMapping(aClientVersion), 
		    pSourceVersion.getVersionID().toString()); 

      /* replace placeholder parameters with the names of the available 
	   slate and format nodes */ 
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
      /* the slate/format Nuke script nodes */ 
      {
        String script = getStringParamValue(new ParamMapping(aSlateScript), false);
        Path path = new Path(pProjectNamer.getSlateNukeScriptsParentPath(), script); 
	pSlateNodeName = path.toString(); 
	pSlatePrefix = path.getName();
	pRequiredNodeNames.add(pSlateNodeName);
      }

      {
        String script = getStringParamValue(new ParamMapping(aFormatScript), false);
        Path path = new Path(pProjectNamer.getFormatNukeScriptsParentPath(), script); 
	pFormatNodeName = path.toString(); 
	pFormatPrefix = path.getName();
	pRequiredNodeNames.add(pFormatNodeName);
      }

      /* lookup the rest of the parameters */ 
      pDeliveryType  = getStringParamValue(new ParamMapping(aDeliveryType));
      pDeliverable   = getStringParamValue(new ParamMapping(aDeliverable), false);
      pClientVersion = getStringParamValue(new ParamMapping(aClientVersion));
      pNotes         = getStringParamValue(new ParamMapping(aNotes));
      pSlateHold     = getIntegerParamValue(new ParamMapping(aSlateHold), 
					    new Range<Integer>(0, null));
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

      /* lock the latest version of all of the prerequisites */ 
      lockNodePrerequisites(); 

      /* lock the specific version of the source images node we are delivering */ 
      pClient.lock(getAuthor(), getView(), 
		   pSourceVersion.getName(), pSourceVersion.getVersionID()); 


      // ... 
      
    }

    private static final long serialVersionUID = -8206152183341505182L;
  }
   
  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1915755291666458793L;
  
  public final static String aSourceNode    = "SourceNode";  
  public final static String aSourceVersion = "SourceVersion";  
  public final static String aDeliveryType  = "DeliveryType";  
  public final static String aDeliverable   = "Deliverable";  
  public final static String aClientVersion = "ClientVersion";  
  public final static String aNotes         = "Notes";  
  public final static String aSlateScript   = "SlateScript";  
  public final static String aSlateHold     = "SlateHold";  
  public final static String aFormatScript  = "FormatScript";  

  public static final String aProjectName    = "ProjectName";
  public static final String aTaskName       = "TaskName";
  public static final String aTaskType       = "TaskType";
  public static final String aCustomTaskType = "CustomTaskType";
  public static final String aCUSTOM         = "[[CUSTOM]]";  
  public static final String aSupervisedBy   = "SupervisedBy";

  public static final String aPurpose   = "Purpose";
  public static final String aFocus     = "Focus";
  public static final String aEdit      = "Edit";



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in version of the source images node.
   */ 
  private NodeVersion pSourceVersion; 

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
   * The node name (and prefix) of the master slate creation Nuke script to use.
   */ 
  private String pSlateNodeName; 
  private String pSlatePrefix; 

  /**
   * The node name (and prefix) of the final image formatting Nuke script to use.
   */ 
  private String pFormatNodeName; 
  private String pFormatPrefix; 

  /**
   * The number of frames to hold the constant slate image before the images being 
   * reviewed begin animating.
   */ 
  private Integer pSlateHold;     

}

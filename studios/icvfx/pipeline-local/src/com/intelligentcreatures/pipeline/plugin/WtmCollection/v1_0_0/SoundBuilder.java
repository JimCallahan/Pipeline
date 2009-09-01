// $Id: SoundBuilder.java,v 1.6 2009/09/01 22:48:35 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S O U N D   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Sound task.<P>
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
class SoundBuilder
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
  SoundBuilder
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
  SoundBuilder
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
    super("Sound",
          "A builder for constructing the nodes associated with the Sound task.",
          mclient, qclient, builderInfo, studioDefs,
	  projectNamer, shotNamer, TaskType.Sound);

    /* setup builder parameters */
    {
      /* selects the project, sequence and shot for the task */
      addLocationParam();
    }

    /* initialize the project namer */
    initProjectNamer();

    /* initialize fields */
    pFinalStages = new ArrayList<FinalizableStage>();

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

      ConstructPass qdc = new QueueDisableCleanupPass();
      addConstructPass(qdc);
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
  public MappedArrayList<String, PluginContext>
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();
    plugins.add(new PluginContext("Touch"));
    plugins.add(new PluginContext("MayaAttachSound"));

    MappedArrayList<String, PluginContext> toReturn =
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
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
      pSoundtrackNodeName = pShotNamer.getSoundtrackNode();
      if(nodeExists(pSoundtrackNodeName))
      {
    	  pRequiredNodeNames.add(pSoundtrackNodeName);
      }
      else
      {
    	  // Check the filesystem for existing sound.
    	  String soundPath = "/projects/wtm/+ref/";
    	  soundPath += pShotNamer.getSequenceName().toUpperCase() + "/";
    	  soundPath += pShotNamer.getSequenceName().toUpperCase() + pShotNamer.getShotName() + "/Audio";

    	  File f = new File(soundPath);
    	  String[] soundFiles = f.list(new FilenameFilter() {
    		  public boolean accept(File d, String name) { return name.endsWith(".aif") && !name.startsWith(".")
    			  ; } });

    	  // If there's no sound directory or no files in it, use the placeholder
    	  if (soundFiles == null || soundFiles.length == 0)
    	  {
    		  pMissingSoundtrackNodeName = pProjectNamer.getMissingSoundtrackNode();
    	  }
    	  // If there's just one sound file, use it
    	  else if (soundFiles.length == 1)
    	  {
    		  // Need to copy the sound file into the working area

    		  String wrkPrefix = "/prod/working/" + getAuthor()
    		  				+ "/" + getView() + pSoundtrackNodeName;

    		  String editPath = wrkPrefix.substring(0, wrkPrefix.lastIndexOf("/"));
    		  File outDir = new File(editPath);

    		  try
    		  {
        		  outDir.mkdirs();
    			  Runtime.getRuntime().exec("cp " + soundPath + "/" + soundFiles[0] + " " + wrkPrefix + ".aiff");
    		  }
    		  catch (IOException e)
    		  {
    			  System.out.println(e.getMessage());
    			  throw new PipelineException("Error opening sound files for copy.");
    		  }

    		  FileSeq files = new FileSeq(wrkPrefix.substring(wrkPrefix.lastIndexOf("/")+1, wrkPrefix.length()), "aiff");
    		  NodeMod soundNode = new NodeMod(pSoundtrackNodeName, files,
                                                  null, false, getToolset(), null);

    		  pClient.register(getAuthor(), getView(), soundNode);
    		  soundNode.setActionEnabled(false);
    		  pClient.vouch(getAuthor(), getView(), pSoundtrackNodeName);
    	  }
    	  // More than one sound file in the folder.
    	  else
    	  {
    		  throw new PipelineException("More than one AIFF file in " + soundPath + ". Please " +
    				  "rename the files such that only one has the .aif extension and this file will be used.");
    	  }
      }

    }

    private static final long serialVersionUID = -2415120920052613187L;
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
            "Creates the nodes which make up the Sound task.");
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

	String prereqNodeName = pShotNamer.getSoundPrereqNode();
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

      /* the submit network */
      {
	if(pMissingSoundtrackNodeName != null) {
	  lockLatest(pMissingSoundtrackNodeName);

	  SoundtrackStage stage =
	    new SoundtrackStage(stageInfo, pContext, pClient,
				pSoundtrackNodeName, pMissingSoundtrackNodeName);
	  addTaskAnnotation(stage, NodePurpose.Edit);
	  addTaskAnnotation(stage, NodePurpose.Product);
	  stage.build();
	  pFinalStages.add(stage);
	}
	else {
	  addTaskAnnotation(pSoundtrackNodeName, NodePurpose.Edit);
	  addTaskAnnotation(pSoundtrackNodeName, NodePurpose.Product);
	}

	String submitNodeName = pShotNamer.getSoundSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(pSoundtrackNodeName);

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
	String approveNodeName = pShotNamer.getSoundApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(pSoundtrackNodeName);

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

    private static final long serialVersionUID = 2957421487240983918L;
  }



  /*----------------------------------------------------------------------------------------*/

  protected
  class QueueDisableCleanupPass
    extends ConstructPass
  {
    public
    QueueDisableCleanupPass()
    {
      super("Queue, Disable Actions and Cleanup",
	    "");
    }

    /**
     * Return both finalizable stage nodes and nodes which will have their actions
     * disabled to be queued now.
     */
    @Override
    public LinkedList<String>
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

      regenerate.addAll(getDisableList());
      for(FinalizableStage stage : pFinalStages)
 	regenerate.add(stage.getNodeName());

      return regenerate;
    }

    /**
     * Cleanup any temporary node structures used setup the network and
     * disable the actions of the newly regenerated nodes.
     */
    @Override
    public void
    buildPhase()
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalStages)
	stage.finalizeStage();
      disableActions();
    }

    private static final long serialVersionUID = 2705175673555741301L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3947058319650711004L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The stages which require running their finalizeStage() method before check-in.
   */
  private ArrayList<FinalizableStage> pFinalStages;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the shot soundtrack node.
   */
  private String pSoundtrackNodeName;

  /**
   * The fully resolved node name of the placeholder sound file.
   */
  private String pMissingSoundtrackNodeName;

}

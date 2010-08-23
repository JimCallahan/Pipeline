package us.temerity.pipeline.stages;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   T O U C H   F I L E S   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A stage that will substitute the Touch action for its own action and then swap back in its
 * real action during finalization, and will then Touch the files one last time to make the 
 * node's queue state Finished.
 * <p>
 * This is useful for when a builder needs to construct a node network with Actions that are 
 * expensive to run and does not actually wish to run those Actions.
 * <p>
 * Any class which wishes to extend this class needs to take care with the 
 * {@link #finalizeStage()} method.  The finalizeStage method in this class is what is 
 * responsible for restoring the action and touching the files.  In almost all cases, it 
 * should be called after any finalizing that the inheriting class does, to guarantee that the
 * node's Action is not run during the Queue Pass that runs at the end of builders. 
 * <p>
 * In addition, it is probably good practice to finalize nodes that are touching their files 
 * in a separate construct pass which is run after all other nodes are finalized.  This will 
 * prevent changes that are made upstream as a result of node finalization from making these
 * nodes stale after they've had their real action restored.
 */
public abstract
class TouchFilesStage
  extends StandardStage
  implements FinalizableStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *  The name of the stage.
   * @param desc
   *  A description of what the stage should do.
   * @param stageInformation
   *  Class containing basic information shared among all stages.
   * @param context
   *  The {@link UtilContext} that this stage acts in.
   * @param client
   *  The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *  The name of the node that is to be created.
   * @param suffix
   *  The suffix for the created node.
   * @param editor
   *  Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *  then an editor will be selected based on the Stage Function.
   * @param action
   *  Contains the name and vendor for the Action plugin.
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value.
   */
  protected 
  TouchFilesStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName, 
    String suffix,
    PluginContext editor, 
    PluginContext action,
    String stageFunction
  ) 
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client, nodeName, suffix, 
          editor, action, stageFunction);
  }
  
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and an
   * Action (with vendor).
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param stageInformation
   *        Class containing basic information shared among all stages.
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param client
   *        The instance of Master Manager that the stage performs all its actions in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param range
   *        The frame range for the node.
   * @param padding
   *        The padding for the file numbers. If this is set to <code>null</code>, a
   *        padding of 4 will be used.
   * @param suffix
   *        The suffix for the created node.
   * @param editor
   *        Contains the name and vendor for the Editor plugin. If this is <code>null</code>
   *        then the Maya Editor from Temerity will be used. 
   * @param action
   *        Contains the name and vendor for the Action plugin.
   * @param stageFunction
   *   A string which describes what sort of node the stage is building.  This is currently
   *   being used to decide which editor to assign to nodes.  This can be set to 
   *   <code>null</code> if a stage does not want to provide a value. 
   */
  protected 
  TouchFilesStage
  (
    String name, 
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    PluginContext editor, 
    PluginContext action, 
    String stageFunction
  )
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client, nodeName, range, padding, suffix, 
          editor, action, stageFunction);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   O P S                                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public boolean 
  build()
    throws PipelineException
  {
    pBuilt = super.build();
    if (pBuilt) {
      NodeMod mod = getNodeMod();
      pActualAction = mod.getAction();
      BaseAction action = lookupAction(new PluginContext("Touch"), getToolset());
      
      mod.setAction(action);
      pClient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return pBuilt;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   F I N A L I Z E   S T A G E                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  finalizeStage()
    throws PipelineException
  {
    NodeMod mod = getNodeMod();
    
    mod.setAction(pActualAction);
    pClient.modifyProperties(getAuthor(), getView(), mod);
    
    pRegisteredNodeMod = 
      pClient.getWorkingVersion(getAuthor(), getView(), getNodeName());
    
    try {
      Thread.sleep(5000l);
    }
    catch (InterruptedException ex1) {
      throw new PipelineException
        ("Interrupted while pausing before touching files.\n" + ex1.getMessage());
    }
    
    pLog.log(Kind.Bld, Level.Finer, 
      "Touching the files on (" + getNodeName() + ") to remove staleness as part of " +
      "finalization.");
    if (PackageInfo.sOsType != OsType.Windows) {
      SubProcessLight process = touchFilesProcess();
      process.run();
      try {
        process.join();
      }
      catch (InterruptedException ex) {
        String message = "The touch subprocess for node () failed.\n";
        throw new PipelineException(Exceptions.getFullMessage(message, ex));
      }
      Integer exit = process.getExitCode();
      if (exit == null || exit != 0)
        throw new PipelineException
        ("The touch subprocess did not finish correctly\n" +
          process.getStdOut() + "\n" + process.getStdErr());
    }
    else {
      NodeID nodeID = getNodeID();
      long currentTime = System.currentTimeMillis();
      Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
      
      wpath.toFile().mkdirs();
      
      for(Path target : pRegisteredNodeMod.getPrimarySequence().getPaths()) {
        Path path = new Path(wpath, target);
        File file = path.toFile();
        file.setWritable(true, true);
        file.setLastModified(currentTime);
        file.setWritable(false);
      }

      for(FileSeq fseq : pRegisteredNodeMod.getSecondarySequences()) {
        for(Path target : fseq.getPaths()) {
          Path path = new Path(wpath, target);
          File file = path.toFile();
          file.setWritable(true, true);
          file.setLastModified(currentTime);
          file.setWritable(false);
        }
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a subprocess which, when run, will touch all the files for the node.
   */
  private SubProcessLight
  touchFilesProcess()
    throws PipelineException
  {
    NodeID nodeID = getNodeID();
    ArrayList<String> args = new ArrayList<String>();
    for(File file : pRegisteredNodeMod.getPrimarySequence().getFiles()) 
      args.add(file.toString());

    for(FileSeq fseq : pRegisteredNodeMod.getSecondarySequences()) {
      for(File file : fseq.getFiles())
        args.add(file.toString());
    }

    Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent()); 
    wpath.toFile().mkdirs();

    TreeMap<String, String> toolset = 
      pClient.getToolsetEnvironment
      (getAuthor(), getView(), getToolset(), PackageInfo.sOsType);
    SubProcessLight light = 
      new SubProcessLight("BuilderTouch", "touch", args, toolset, wpath.toFile());
    return light;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7112444310081909841L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private BaseAction pActualAction;
  
  private boolean pBuilt;
}

package us.temerity.pipeline.plugin.TaskRunBuilderAction.v2_4_28;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   R U N   V E R I F Y   B U I L D E R   A C T I O N                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Run a builder associated with an automated task step.
 * <p>
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Builder ID <BR>
 *   <DIV style="margin-left: 40px;">
 *     The builder that this is going to run.  This builder should follow the same basic 
 *     template that is laid out in the VerifyTaskBuilder and the ApproveTaskBuilder.
 *   </DIV> <BR>
 *   
 *   Source Node<BR>
 *   <DIV style="margin-left: 40px;">
 *     The node and version that the builder is going to look at to determine what should be
 *     verified or approved.  Unlike with normal actions, the version of the source node is
 *     going to come into play, but the contents of the file will not.  Because of this, the 
 *     source node should always be locked, since we never want to be regenerating it as part
 *     of verifying or approving.  If this action is being used to run a Verify Builder, then 
 *     the task's submit node should be attached as the source node.  For an Approve Builder, 
 *     it should be the verify node.
 *   </DIV> <BR>
 */
public 
class TaskRunBuilderAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public
  TaskRunBuilderAction() 
  {
    super("TaskRunBuilder", new VersionID("2.4.28"), "Temerity",
          "Runs the designated builder that will complete a step in the task process.");
    
    {
      ActionParam param = 
        new BuilderIDActionParam
        (aBuilderID,
         "The builder that is going to be run.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aSourceNode,
         "The target node and version that the builder is going to use as its source.  " +
         "Should always be locked!", 
         null);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aBuilderID);
      layout.addEntry(aSourceNode);
      setSingleLayout(layout);
    }
    
    underDevelopment();
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  @Override
  public SubProcessHeavy
  prep
  (
    ActionAgenda agenda,
    File outFile, 
    File errFile 
  )
    throws PipelineException
  {
    Path targetPath = getPrimaryTargetPath(agenda, "builder log file");
    
    BuilderID id = (BuilderID) getSingleParamValue(aBuilderID);
    if (id == null)
      throw new PipelineException
        ("The (" + getName() + ") Action must declare a value for the BuilderID parameters.");
    
    String sourceNode = getSingleStringParamValue(aSourceNode, false);
    
    ArrayList<String> args = new ArrayList<String>();
    
    args.add("--collection=" + id.getName());
    args.add("--vendor=" + id.getVendor());
    args.add("--versionid=" + id.getVersionID());
    args.add("--builder-name=" + id.getBuilderName());
    args.add("--batch");
    args.add("--log=ops:finest,bld:finest");
    args.add("--builder="  + id.getBuilderName());
    args.add("--ReleaseOnError=true");
    args.add("--CheckinWhenDone=true");
    
    args.add("--SourceNode=" + sourceNode);

    {
      NodeID nodeID = agenda.getNodeID();
      args.add("--UtilContext-Author=" + nodeID.getAuthor());
      args.add("--UtilContext-View=" + nodeID.getView());
    }
    
    {
      String s = String.valueOf(agenda.getJobID());
      args.add("--CustomWorkingArea=" + s );
    }
    
    File py = createTemp(agenda, "py");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(py));
      
      out.write("import shutil\n");
      
      out.write(getPythonLaunchHeader());
      out.write(getPythonLauncher("plbuilder", args));
      out.write("shutil.copyfile('" + new Path(outFile).toString()  + "', '" + 
                 targetPath.toString() +"')\n\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary PY script file (" + py + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    return createPythonSubProcess(agenda, py, outFile, errFile);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5358219660571974224L;

  private static final String aBuilderID     = "BuilderID";
  private static final String aSourceNode    = "SourceNode";
}

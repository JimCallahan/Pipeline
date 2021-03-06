package us.temerity.pipeline.plugin.TemplateManifestAction.v2_4_27;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   M A N I F E S T   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Run the Template Manifest Builder in the current working area based on the manifests that
 * are connected to this action. <p>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Param Manifest<BR>
 *   <DIV style="margin-left: 40px;">
 *     The node containing the manifest with all the replacement values.
 *   </DIV> <BR>
 *   
 *   Desc Manifest<BR>
 *   <DIV style="margin-left: 40px;">
 *     The node containing the manifest with the description of the template.
 *   </DIV> <BR>
 *   
 *   AOE Mode<BR>
 *   <DIV style="margin-left: 40px;">
 *     The Action on Existence mode to be used when running the template.  This must be a 
 *     valid AOE mode for the template; this means it has to either be one of the four 
 *     built-in modes (see {@link ActionOnExistence}) or a custom mode defined in the Param
 *     Manifest.
 *   </DIV> <BR>
 *   
 *   Check In Message<BR>
 *   <DIV style="margin-left: 40px;">
 *     The message to check-in the nodes generated by the instantiation of the template or 
 *     <code>null</code> to use the default template check-in message.
 *   </DIV> <BR>
 *   
 *   Check In Level<BR>
 *   <DIV style="margin-left: 40px;">
 *     The check-in level to use when checking in the nodes generated by the instantiation of
 *     the template.
 *   </DIV> <BR>
 *   
 *   Allow Zero Contexts<BR>
 *   <DIV style="margin-left: 40px;">
 *     Allow contexts in this template to have no replacements defined.
 *   </DIV> <BR>
 *   
 *   Inhibit File Copy<BR>
 *   <DIV style="margin-left: 40px;">
 *     Inhibit the CopyFile flag on all nodes in the template.
 *   </DIV> <BR>
 * </DIV> <P>
 */
public 
class TemplateManifestAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public
  TemplateManifestAction() 
  {
    super("TemplateManifest", new VersionID("2.4.27"), "Temerity",
          "Run the Template Manifest Builder in the current working area based on the " +
          "manifests that are connected to this action.");
    
    {
      ActionParam param = 
        new LinkActionParam
        (aParamManifest,
         "The node containing the manifest with all the replacement values.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aDescManifest,
         "The node containing the manifest with the description of the template.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new StringActionParam
          (aAOEMode,
           "The Action on Existence mode to use in the template.\n" +
           "CheckOut, Conform, Continue, and Abort are the 4 built-in values.",
           "CheckOut");
      addSingleParam(param);
    }
    {
      ActionParam param =  
        new TextAreaActionParam
          (aCheckInMessage,
           "The check-in message to use.",
           null,
           4);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> values = new ArrayList<String>();
      Collections.addAll(values, "Major", "Minor", "Micro");
      ActionParam param = 
        new EnumActionParam
          (aCheckInLevel,
           "The check-in level to use.",
           "Minor",
           values);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
          (aAllowZeroContexts,
           "Allow contexts in this template to have no replacements defined.",
           false);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
          (aCustomWorkingArea,
           "Should a new temporary working area be created to run the builder in.",
           true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
          (aInhibitFileCopy,
           "Inhibit the CopyFile flag on all nodes in the template.",
           false);
      addSingleParam(param);
    }
   
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aParamManifest);
      layout.addEntry(aDescManifest);
      layout.addEntry(aCustomWorkingArea);
      layout.addSeparator();
      layout.addEntry(aAOEMode);
      layout.addEntry(aAllowZeroContexts);
      layout.addEntry(aInhibitFileCopy);
      layout.addSeparator();
      layout.addEntry(aCheckInLevel);
      layout.addEntry(aCheckInMessage);
      setSingleLayout(layout);
    }
    
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
    
    String paramManifest = getSingleStringParamValue(aParamManifest, false);
    String descManifest = getSingleStringParamValue(aDescManifest, false);
    
    String aoeMode = getSingleStringParamValue(aAOEMode, false);
    Boolean zeroContext = getSingleBooleanParamValue(aAllowZeroContexts);
    Boolean inhibitFC = getSingleBooleanParamValue(aInhibitFileCopy);
    Boolean customWorkingArea = getSingleBooleanParamValue(aCustomWorkingArea);
    
    String checkInLevel = getSingleStringParamValue(aCheckInLevel, false);
    String checkInMessage = getSingleStringParamValue(aCheckInMessage);
    
    ArrayList<String> args = new ArrayList<String>();
    
    args.add("--collection=Template");
    args.add("--vendor=Temerity");
    args.add("--versionid=2.4.12");
    args.add("--builder-name=TemplateManifestBuilder");
    args.add("--batch");
    args.add("--builder=TemplateManifestBuilder");
    args.add("--AllowZeroContexts=" + zeroContext);
    args.add("--InhibitFileCopy=" + inhibitFC);
    args.add("--ReleaseOnError=true");
    args.add("--AOEMode=" + aoeMode);
    args.add("--CheckInLevel=" + checkInLevel);
    if (checkInMessage != null)
      args.add("--CheckInMessage=" + checkInMessage);
    args.add("--CheckinWhenDone=true");
    args.add("--DescManifest=" + descManifest);
    args.add("--ParamManifest=" + paramManifest);
    
    NodeID id = agenda.getNodeID();
    args.add("--UtilContext-Author=" + id.getAuthor());
    args.add("--UtilContext-View=" + id.getView());
    args.add("--log=ops:finest,bld:finest");
    
    if (customWorkingArea) {
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
  
  private static final long serialVersionUID = -7652828982667624580L;
  
  private static final String aParamManifest     = "ParamManifest";
  private static final String aDescManifest      = "DescManifest";
  private static final String aAOEMode           = "AOEMode";
  private static final String aCheckInLevel      = "CheckInLevel";
  private static final String aCheckInMessage    = "CheckInMessage";
  private static final String aAllowZeroContexts = "AllowZeroContexts";
  private static final String aInhibitFileCopy   = "InhibitFileCopy";
  private static final String aCustomWorkingArea = "CustomWorkingArea";
}

// $Id: HfsActionUtils.java,v 1.1 2008/03/20 21:27:53 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;


/*------------------------------------------------------------------------------------------*/
/*   H F S   A C T I O N   U T I L S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of node Action plugins related to Houdini. <P> 
 */
public 
class HfsActionUtils
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  HfsActionUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M M O N   P A R A M E T E R S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds an Houdini Scene parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   Houdini Scene <BR>
   *   <DIV style="margin-left: 40px;">
   *     The Houdini scene that the Action is going to act upon.
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addHoudiniSceneParam()
  {
    ActionParam param = 
      new LinkActionParam
      (aHoudiniScene,
       "The Houdini scene that the Action is going to act upon.",
       null);
    addSingleParam(param);
  }



  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Adds an OutputOperator parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   OutputOperator <BR>
   *   <DIV style="margin-left: 40px;">
   *      The name of the render output operator. 
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   *
   * @param opname
   *   Name of the default Houdini ouput operator.
   */
  protected void
  addOutputOperatorParam
  (
   String opname
  )
  {
    ActionParam param = 
      new StringActionParam
      (aOutputOperator,
       "The name of the render output operator.", 
       opname);
    addSingleParam(param);
  }

  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Adds an CameraOverride parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   CameraOverride <BR>
   *   <DIV style="margin-left: 40px;">
   *      Overrides the render camera (if set).
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addCameraOverrideParam()
  {
    ActionParam param = 
      new StringActionParam
      (aCameraOverride,
       "Overrides the render camera (if set).", 
       null);
    addSingleParam(param);
  }

  /**
   * Generates the "opparm" command to set CameraOverride parameter of the given output
   * operator.
   */
  protected void
  writeCameraOverrideOpparm
  (
   String opname, 
   ActionAgenda agenda, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    String camera = getSingleStringParamValue(aCameraOverride);
    if(camera != null) 
      writeStringOpparm(opname, "camera", camera, out); 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds an PreRenderScript parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   PreRenderScript <BR>
   *   <DIV style="margin-left: 40px;">
   *      The source node containing the pre-render command script.
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addPreRenderScriptParam()
  {
    ActionParam param = 
      new LinkActionParam
      (aPreRenderScript,
       "The source node containing the the pre-render command script.", 
       null);
    addSingleParam(param);
  }
  
  /**
   * Adds an PostRenderScript parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   PostRenderScript <BR>
   *   <DIV style="margin-left: 40px;">
   *      The source node containing the post-render command script.
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addPostRenderScriptParam()
  {
    ActionParam param = 
      new LinkActionParam
      (aPostRenderScript,
       "The source node containing the the post-render command script.", 
       null);
    addSingleParam(param);
  }

  /**
   * Adds an PreFrameScript parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   PreFrameScript <BR>
   *   <DIV style="margin-left: 40px;">
   *      The source node containing the pre-frame command script.
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addPreFrameScriptParam()
  {
    ActionParam param = 
      new LinkActionParam
      (aPreFrameScript,
       "The source node containing the the pre-frame command script.", 
       null);
    addSingleParam(param);
  }
  
  /**
   * Adds an PostFrameScript parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   PostFrameScript <BR>
   *   <DIV style="margin-left: 40px;">
   *      The source node containing the post-frame command script.
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addPostFrameScriptParam()
  {
    ActionParam param = 
      new LinkActionParam
      (aPostFrameScript,
       "The source node containing the the post-frame command script.", 
       null);
    addSingleParam(param);
  }

  /**
   * Add the parameters created by the add[Pre|Post][Render|Frame] methods to the 
   * given parameter layout group.
   * 
   * @param layout
   *   The parent layout group.
   * 
   * @param desc
   *   The tooltip to give the drawer containing the script parameters.
   */ 
  protected void 
  addScriptParamsToLayout
  (
   LayoutGroup layout, 
   String desc
  ) 
  {
    LayoutGroup sub = new LayoutGroup("Command Scripts", desc, true); 

    sub.addEntry(aPreRenderScript);
    sub.addEntry(aPostRenderScript);
    sub.addSeparator();
    sub.addEntry(aPreFrameScript);
    sub.addEntry(aPostFrameScript);

    layout.addSubGroup(sub);
  }

  /**
   * Add the parameters created by the add[Pre|Post][Render|Frame] methods to the 
   * given parameter layout group.
   * 
   * @param layout
   *   The parent layout group.
   */ 
  protected void 
  addScriptParamsToLayout
  (
   LayoutGroup layout
  ) 
  {
    addScriptParamsToLayout
      (layout, "Houdini command scripts run at various stages of the rendering process.");
  }

  /**
   * Generates the "opparm" command to set PreRenderScript parameter of the given output
   * operator.
   * 
   * @param opname
   *   The Houdini output operator to be modified.
   */ 
  protected void 
  writePreRenderScriptOpparm
  (
   String opname, 
   ActionAgenda agenda, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    Path preRender = getCommandSourcePath(aPreRenderScript, agenda); 
    if(preRender != null) 
      writeStringOpparm(opname, "prerender", preRender.toString(), out);
  }

  /**
   * Generates the "opparm" command to set PostRenderScript parameter of the given output
   * operator.
   * 
   * @param opname
   *   The Houdini output operator to be modified.
   */ 
  protected void 
  writePostRenderScriptOpparm
  (
   String opname, 
   ActionAgenda agenda, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    Path postRender = getCommandSourcePath(aPostRenderScript, agenda); 
    if(postRender != null) 
      writeStringOpparm(opname, "postrender", postRender.toString(), out);
  }

  /**
   * Generates the "opparm" command to set PreFrameScript parameter of the given output
   * operator.
   * 
   * @param opname
   *   The Houdini output operator to be modified.
   */ 
  protected void 
  writePreFrameScriptOpparm
  (
   String opname, 
   ActionAgenda agenda, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    Path preFrame = getCommandSourcePath(aPreFrameScript, agenda); 
    if(preFrame != null) 
      writeStringOpparm(opname, "preframe", preFrame.toString(), out);
  }

  /**
   * Generates the "opparm" command to set PostFrameScript parameter of the given output
   * operator.
   * 
   * @param opname
   *   The Houdini output operator to be modified.
   */ 
  protected void 
  writePostFrameScriptOpparm
  (
   String opname, 
   ActionAgenda agenda, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    Path postFrame = getCommandSourcePath(aPostFrameScript, agenda); 
    if(postFrame != null) 
      writeStringOpparm(opname, "postframe", postFrame.toString(), out);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generates the "opparm" command to set the given string parameter value.
   * 
   * @param opname
   *   The Houdini output operator to be modified.
   */
  protected void
  writeStringOpparm
  (
   String opname, 
   String param, 
   String value, 
   FileWriter out
  )
    throws PipelineException, IOException
  {
    out.write("opparm " + opname + " " + param + " '" + value + "'\n");
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds an UseGraphicalLicense parameter to the action. <p>
   * 
   * The following single valued parameters are added: <BR>
   * 
   * <DIV style="margin-left: 40px;"> 
   *   UseGraphicalLicense <BR>
   *   <DIV style="margin-left: 40px;">
   *      Whether to use an interactive graphical Houdini license when running hscript(1).
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected void
  addUseGraphicalLicenseParam()
  {
    ActionParam param = 
      new BooleanActionParam
      (aUseGraphicalLicense,
       "Whether to use an interactive graphical Houdini license when running hscript(1).",
       false);
    addSingleParam(param);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary Houdini scene associated with the 
   * target node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the target Houdini scene script. 
   */ 
  public Path 
  getHoudiniSceneTargetPath
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimaryTargetPath(agenda, "hip", "Houidini scene file");
  }

  /**
   * Get the abstract path to the single primary Houdini command file associated with the 
   * target node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the target Houdini command file. 
   */ 
  public Path 
  getHoudiniCommandTargetPath
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimaryTargetPath(agenda, "cmd", "Houidini command file");
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary Houdini command file associated with a 
   * source node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued Houdini command file parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the Houdini command file or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getCommandSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, "cmd", "Houidini command file");
  }

  /**
   * Get the abstract path to the single primary Houdini scene file associated with a source
   * node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued Houdini scene parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the Houdini scene file or <CODE>null</CODE> if none was specified.
   */ 
  public Path 
  getHoudiniSceneSourcePath
  (
   String pname, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, "hip", "Houidini scene file");
  }




  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   N A M I N G                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a Pipeline file pattern into a Houdini filename expression string.
   */ 
  public static String
  toHoudiniFilePattern
  (
   FilePattern fpat
  ) 
  {
    StringBuilder buf = new StringBuilder();

    buf.append(fpat.getPrefix()); 

    if(fpat.hasFrameNumbers()) {
      buf.append(".$F"); 
      if(fpat.getPadding() > 1) 
        buf.append(fpat.getPadding());
    }

    buf.append("." + fpat.getSuffix());
    
    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R O G R A M   L O O K U P                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the version number of Houdini by looking at the directory where "houdini" can 
   * be found in the PATH.
   *
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @return 
   *   The houdini version or <CODE>null</CODE> if unable to determine it.
   */
  public static VersionID
  getHoudiniVersion
  (
   ActionAgenda agenda   
  ) 
    throws PipelineException
  {
    return getHoudiniVersion(agenda.getEnvironment()); 
  }

  /**
   * Get the version number of Houdini by looking at the directory where "houdini" can 
   * be found in the PATH.
   * 
   * @param env  
   *   The environment used to lookup PATH.
   * 
   * @return 
   *   The houdini version or <CODE>null</CODE> if unable to determine it.
   */ 
  public static VersionID
  getHoudiniVersion
  (
   Map<String,String> env
  ) 
    throws PipelineException
  {
    String pstr = env.get("PATH"); 
    if(pstr == null) 
      throw new PipelineException
        ("No PATH was defined in the Toolset environment!"); 

    ExecPath epath = new ExecPath(pstr);
    File houdini = epath.which("houdini"); 
    if(houdini == null) 
      throw new PipelineException
        ("Cannot find \"houdini\" anywhere on the PATH defined in the Toolset environment!"); 

    Path hpath = new Path(houdini);

    VersionID houdiniVersion = null;
    try {
      if(PackageInfo.sOsType != OsType.Windows) {
        Matcher m = sHoudiniBinary.matcher(hpath.toString()); 
        if(m.matches()) 
          houdiniVersion = new VersionID(m.group(2));
      }
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException
        ("Unable to determine the version of Houdini being used from the path to the " +
         "\"houdini\" binary (" + houdini + ")!"); 
    }

    return houdiniVersion;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1876197186810473749L;

  private static Pattern sHoudiniBinary = 
    Pattern.compile("(/[^/]*)+/hfs([0-9]+[.][0-9]+[.][0-9]+)/bin/houdini"); 


  public static final String aHoudiniScene        = "HoudiniScene";
  public static final String aOutputOperator      = "OutputOperator";
  public static final String aCameraOverride      = "CameraOverride";
  public static final String aPreRenderScript     = "PreRenderScript";
  public static final String aPostRenderScript    = "PostRenderScript";
  public static final String aPreFrameScript      = "PreFrameScript";
  public static final String aPostFrameScript     = "PostFrameScript";
  public static final String aUseGraphicalLicense = "UseGraphicalLicense";

}




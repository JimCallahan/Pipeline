// $Id: MayaBuildUtils.java,v 1.2 2008/03/06 13:01:35 jim Exp $

package us.temerity.pipeline.plugin;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   B U I L D   U T I L S                                                        */
/*------------------------------------------------------------------------------------------*/


public class MayaBuildUtils
  extends MayaActionUtils
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
  MayaBuildUtils
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
   * Adds a Start Frame Integer param to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Start Frame <BR>
   *   <DIV style="margin-left: 40px;">
   *     The start frame of the generated Maya scene.
   *   </DIV> <BR>
   */
  protected void
  addStartFrameParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aStartFrame,
       "The start frame of the generated Maya scene.  Accessible as $plStartFrame in " + 
       "optional MEL scripts.", 
       1);
    addSingleParam(param);
  }
  
  /**
   * Adds an End Frame Integer param to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   End Frame <BR>
   *   <DIV style="margin-left: 40px;">
   *     The end frame of the generated Maya scene.
   *   </DIV> <BR>
   */
  protected void
  addEndFrameParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aEndFrame,
       "The end frame of the generated Maya scene.  Accessible as $plEndFrame in " + 
       "optional MEL scripts.", 
       1);
    addSingleParam(param);
  }

  /**
   * Provide StartFrame and EndFrame parameter values as MEL variables 
   * $plStartFrame and $plEndFrame.
   */ 
  protected String
  genFrameRangeVarsMEL()
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 
    
    buf.append("// ACTION PARAMETERS: StartFrame, EndFrame\n"); 

    Integer startFrame = (Integer) getSingleParamValue(aStartFrame);
    if(startFrame != null) 
      buf.append("float $plStartFrame = " + startFrame + ";\n");
    
    Integer endFrame = (Integer) getSingleParamValue(aEndFrame);
    if(endFrame != null) 
      buf.append("float $plEndFrame = " + endFrame + ";\n"); 

    return buf.toString(); 
  }
        
  /**
   * Set the scene time range based on the StartFrame and EndFrame parameters.
   */
  protected String
  genPlaybackOptionsMEL() 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 

    buf.append("// TIME RANGE\n");
    
    Integer start = (Integer) getSingleParamValue(aStartFrame);
    if(start != null) {
      buf.append("playbackOptions -e -min " + start + ";\n");
      buf.append("playbackOptions -e -ast " + start + ";\n");
    }
    
    Integer end = (Integer) getSingleParamValue(aEndFrame);
    if(end != null) {
      buf.append("playbackOptions -e -max " + end + ";\n");
      buf.append("playbackOptions -e -aet " + end + ";\n");
    }
    
    buf.append("\n"); 

    return buf.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds an Enum Source Parameter called BuildType to the Action.
   * <p>
   * It has the following options:
   * <ul>
   * <li>Import
   * <li>Reference
   * <li>Proxy
   * </ul>
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Build Type<BR>
   *   <DIV style="margin-left: 40px;">
   *     The method Maya should use to merge the data from the source scene into the
   *     generated scene.
   *   </DIV> <BR>
   */
  protected void
  addBuildTypeSourceParam
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ArrayList<String> choices = new ArrayList<String>();
    choices.add(aImport);
    choices.add(aReference);
    choices.add(aProxy);

    ActionParam param = 
      new EnumActionParam
      (aBuildType,
       "The method Maya should use to merge the data from the source scene into the " + 
       "generated scene.", 
       aReference, choices);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Enum Source Parameter called BuildType to the Action.
   * <p>
   * It has the following options:
   * <ul>
   * <li>Import
   * <li>Reference
   * </ul>
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Build Type<BR>
   *   <DIV style="margin-left: 40px;">
   *     The method Maya should use to merge the data from the source scene into the
   *     generated scene.
   *   </DIV> <BR>
   */
  protected void
  addOldBuildTypeSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ArrayList<String> choices = new ArrayList<String>();
    choices.add(aImport);
    choices.add(aReference);

    ActionParam param = 
      new EnumActionParam
      (aBuildType,
       "The method Maya should use to merge the data from the source scene into the " + 
       "generated scene.", 
       aReference, choices);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a Boolean Source Parameter called NameSpace to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *  Name Space<BR>
   *  <DIV style="margin-left: 40px;">
   *    Whether Maya should create a namespace for the imported/referenced scene.
   *    This option is highly recommended to avoid name clashes.
   * </DIV> <BR>
   */
  protected void
  addNamespaceSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new BooleanActionParam
      (aNameSpace,
       "Whether Maya should create a namespace for the imported/referenced scene.  " + 
       "This option is highly recommended to avoid name clashes.",
       true);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a String Source Parameter called Prefix Name to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Prefix Name <BR>
   *   <DIV style="margin-left: 40px;">
   *     The namespace prefix to use for the imported/referenced Maya scene inside the 
   *     generated Maya scene.  If unset, the namespace is based on the filename.
   *   </DIV> <BR>
   */
  protected void
  addPrefixNameSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new StringActionParam
      (aPrefixName,
       "The namespace prefix to use for the imported/referenced Maya scene inside the " +
       "generated Maya scene.  If unset, the namespace is based on the filename.",
       null);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a String Source Parameter called Proxy Name to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Proxy Name <BR>
   *   <DIV style="margin-left: 40px;">
   *     The proxy name to be used for the referenced Maya scene.  If Build Type is set
   *     to Reference, this will be the proxy tag for the reference.  If Build Type is set
   *     to Import than this field will be ignored.
   *   </DIV> <BR>
   */
  protected void
  addProxyNameSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new StringActionParam
      (aProxyName,
       "This parameter will set the Maya Proxy name for this proxy level.",
       null);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Integer Source Parameter called Num Instances to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Num Instances<BR>
   *   <DIV style="margin-left: 40px;">
   *     The number of instances of a model that should be created.  If this is set
   *     to zero, then one instance will still be created, but it will not have
   *     any numbering in the namespace.  For example, if the Prefix Name is 'rock',
   *     setting Num Instances to '0' will result in a namespace of 'rock'.  Setting
   *     it to '1' will result in rock-0001
   *   </DIV> <BR>
   */
  protected void
  addNumInstancesSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aNumInstances,
       "The number of instances of a model that should be created.  If this is set " + 
       "to zero, then one instance will still be created, but it will not have " + 
       "any numbering in the namespace.", 
       0);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Integer Source Parameter called Instance Start to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Instance Start<BR>
   *   <DIV style="margin-left: 40px;">
   *     The number for the first instance of a reference.  This allows numbered 
   *     instances to start somewhere besides zero.
   *   </DIV> <BR>
   */
  protected void
  addInstanceStartSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aInstanceStart,
       "The number for the first instance of a reference.  This allows numbered " + 
       "instances to start somewhere besides zero.", 
       0);
    params.put(param.getName(), param);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3431517306706257L;
  
  public static final String aBuildType       = "BuildType";
  public static final String aSceneType       = "SceneType";
  public static final String aNameSpace       = "NameSpace";
  public static final String aPrefixName      = "PrefixName";
  public static final String aProxyName       = "ProxyName";
  public static final String aStartFrame      = "StartFrame";
  public static final String aEndFrame        = "EndFrame";
  public static final String aDeferReferences = "DeferReferences";
  public static final String aNumInstances    = "NumInstances";
  public static final String aInstanceStart   = "InstanceStart";
  
  public static final String aProxy           = "Proxy";
  public static final String aImport          = "Import";
  public static final String aReference       = "Reference";
  
  public static final String aModel           = "Model";
  public static final String aAnimation       = "Animation";
}

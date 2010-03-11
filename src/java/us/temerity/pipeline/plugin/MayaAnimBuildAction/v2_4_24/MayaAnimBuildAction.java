// $Id: BaseBuilder.java,v 1.33 2007/11/01 19:08:53 jesse Exp $

package us.temerity.pipeline.plugin.MayaAnimBuildAction.v2_4_24;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   A N I M   B U I L D   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Extends the MayaBuild Action to now allow for the referencing of Maya files that only
 * contain curves, which are then hooked up to models which have either been Imported
 * or Referenced. <p>
 * 
 * A new empty scene is first created.  The component scenes are either referenced as Maya 
 * references or directly imported depending on a per-source parameter from each source 
 * node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Initial MEL, Model MEL and Final MEL single valued
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Ignore Missing<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should curves that do not have a corresponding channel be skipped?  If this is set
 *     to false, a curve that is missing a channel will result in an error being thrown.
 *   </DIV> <BR>
 * 
 *   Linear Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The linear unit that the generated scene will use. 
 *   </DIV> <BR>
 * 
 *   Angular Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The angular unit that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Time Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unit of time and frame rate that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Start Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The start frame of the generated Maya scene.  Accessible as $plStartFrame in 
 *     the optional MEL scripts.
 *   </DIV> <BR>
 *   
 *   End Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The end frame of the generated Maya scene.  Accessible as $plEndFrame in 
 *     the optional MEL scripts.
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after scene creation
 *      and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after importing all models
 *      but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene. 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Scene Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     Is this a model scene or an animation scene.
 *   </DIV> <BR>
 * 
 *   Build Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The method Maya should use to merge the data from the source scene into the
 *     generated scene.
 *   </DIV> <BR>
 *   
 *   Name Space<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether Maya should create a namespace for the imported/referenced scene.
 *     This option is highly recomended to avoid name clashes.
 *   </DIV> <BR>
 * 
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     This differs depending on the Scene Type.  If this is a Model scene, then this is  
 *     the namespace prefix to use for the imported/referenced Maya scene inside the 
 *     generated Maya scene.  If unset, the namespace is based on the filename.  If this is
 *     an Animation scene, it is the namespace that the animation will be applied to.  In this
 *     case, the Animation will be given a namespace in the form PrefixName_a.
 *   </DIV> <BR>
 * </DIV> <P> 
 **/
public 
class MayaAnimBuildAction
  extends MayaBuildUtils
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MayaAnimBuildAction() 
  {
    super("MayaAnimBuild", new VersionID("2.4.24"), "Temerity",
          "Extends the MayaBuild Action to now allow for the referencing of Maya files " + 
          "that only contain curves, which are then hooked up to models which have either " + 
          "been Imported or Referenced.");
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aIgnoreMissing,
         "Should curves that do not have a corresponding channel be skipped?  If this is " +
         "set to false, a curve that is missing a channel will result in an error " +
         "being thrown.",
         false);
      addSingleParam(param);
    }
    
    addUnitsParams();

    addStartFrameParam();
    addEndFrameParam();
    
    addInitalMELParam();
    addAnimMELParam();
    addModelMELParam();
    addFinalMELParam();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aIgnoreMissing);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aStartFrame);
      layout.addEntry(aEndFrame);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aModelMEL);
      layout.addEntry(aAnimMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }
    
    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aSceneType);
      layout.add(aBuildType);
      layout.add(aNameSpace);
      layout.add(aPrefixName);
      layout.add(aProxyName);
      layout.add(aNumInstances);
      layout.add(aInstanceStart);

      setSourceLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  @Override
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  @Override
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    addSceneTypeSourceParam(params);
    addBuildTypeSourceParam(params); 
    addNamespaceSourceParam(params);
    addPrefixNameSourceParam(params);
    addProxyNameSourceParam(params);
    addNumInstancesSourceParam(params);
    addInstanceStartSourceParam(params);
    
    return params;
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
    boolean ignoreMissing = getSingleBooleanParamValue(aIgnoreMissing);
    
    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    
    scanAllSources(agenda);
    
    TreeMap<String, MayaBuildData> modelScenes = getModelDataByActualPrefix();
    
    TreeMap<String, MayaBuildData> animScenes = getAnimDataByActualPrefix();
    
    MappedLinkedList<String, MayaBuildData> proxyScenes = getProxyDataByActualPrefix();
    
    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      /* provide parameters as MEL variables */ 
      out.write(genFrameRangeVarsMEL()); 
      
      out.write("source \"proxyUtils.mel\";\n\n");

      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
                "file -rename \"" + targetScene + "\";\n" + 
                "file -type \"" + sceneType + "\";\n\n");

      out.write(genUnitsMEL());
      
      writeInitialMEL(agenda, out);
      
      for (MayaBuildData mdata : getModelDataInDefaultNamespace()) {
        out.write("{\n");
        mdata.writeBuildMEL(out, false);
        out.write("}\n");
      }
      
      for (Entry<String, MayaBuildData> entry : modelScenes.entrySet()) {
        String prefixName = entry.getKey();
        MayaBuildData mdata = entry.getValue();
        
        out.write("{\n");

        mdata.writeBuildMEL(out, false);

        LinkedList<MayaBuildData> proxies = proxyScenes.get(prefixName);
        if (proxies != null) {
          for (MayaBuildData pdata : proxies)
            pdata.writeBuildMEL(out, false);
        }
        out.write("}\n");
      }
      
      for (MayaBuildData adata : animScenes.values())
        adata.writeBuildMEL(out, ignoreMissing);
      
      writeModelMEL(agenda, out);
      
      writeAnimLinkingHeader(out);
      
      for (Entry<String, MayaBuildData> entry : animScenes.entrySet()) {
        String animPreifx = entry.getKey();
        String modelPrefix = animPreifx.substring(0, animPreifx.length() - 2);
        
        MayaBuildData animData = entry.getValue();
        MayaBuildData modelData = pModelDataByActualPrefix.get(modelPrefix);
        
        writeAnimLinkingMEL(out, ignoreMissing, animData, modelData);
      }
      
      writeAnimMEL(agenda, out);
      
      /* set the time options */ 
      out.write(genPlaybackOptionsMEL()); 
      
      /* save the file */ 
      out.write("// SAVE\n" + 
                "print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
                "file -save;\n\n");
      
      writeFinalMEL(agenda, out);
        
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(null, script, true, agenda, outFile, errFile);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9117915285464940432L;

  public static final String aIgnoreMissing = "IgnoreMissing";
}

// $Id: MayaBuildAction.java,v 1.2 2008/07/08 03:24:29 jim Exp $

package us.temerity.pipeline.plugin.MayaBuildAction.v2_4_24;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   B U I L D   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes. <P> 
 * 
 * A new empty scene is first created.  The component scenes are either referenced as Maya 
 * references or directly imported depending on a per-source parameter from each source 
 * node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * 
 * This new version also adds support for Maya's proxy referencing tools.  This allows
 * multiple versions of the same model to be loaded in as a single reference, which a
 * user can then switch between.  To use this feature, a new option has been added to
 * Build Type called Proxy.  When a source is set to Proxy, it will use its Prefix Name
 * parameter to figure out which model it should belong to.  If you set a model to Proxy
 * and give it a Prefix Name that does not match any of the Prefix Names for Reference
 * models, an error will be thrown by the Action.   
 * 
 * You must also specify a Proxy Name for all Proxy models.  These do not have to be 
 * unique for the Action, but must be unique for a given Prefix Name.  The Action 
 * also accepts a Proxy Name for a Reference model.
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL and Final MEL single valued
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Defer References<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should all the references be unloaded by default.
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
 *   Build Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The method Maya should use to merge the data from the source scene into the
 *     generated scene.
 *   </DIV> <BR>
 *   
 *   Name Space<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether Maya should create a namespace for the imported/referenced scene.
 *     This option is highly recommended to avoid name clashes.  There are four different 
 *     ways that Name Space and Build Type interact.
 *     <ul>
 *     <li> Reference / True: Reference the scene with a namespace
 *     <li> Reference / False: Reference the scene using the default namespace (essentially
 *          not using any namespace at all).  This option can be dangerous if the scenes 
 *          being referenced have name collisions in them.  This mode is primarily intended 
 *          for referencing in a single procedurally generated scene into a scene where an
 *          artist will be doing work.  For example, a preLight scene is constructed that 
 *          contains namespaces for each character.  That scene is then referenced into a 
 *          lighting scene using the default namespace.  So even though there are two 
 *          levels of referencing and the preLight scene can be procedurally rebuilt at any
 *          time, the lighting scene does not have an extra level of namespaces.
 *      <li> Import / True: Import the scene with a namespace.
 *      <li> Import / False: Import the scene and prepend the Prefix Name string to each 
 *           node.  No namespace is created.
 *   </DIV> <BR>
 *   
 *   Num Instances<BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of instances of a model that should be created.  If this is set
 *     to zero, then one instance will still be created, but it will not have
 *     any numbering in the namespace.  For example, if the Prefix Name is 'rock',
 *     setting Num Instances to '0' will result in a namespace of 'rock'.  Setting
 *     it to '1' will result in rock-0001
 *   </DIV> <BR>
 *   
 *   Instance Start<BR>
 *   <DIV style="margin-left: 40px;">
 *     The number for the first instance of a reference.  This allows numbered 
 *     instances to start somewhere besides zero.
 *   </DIV> <BR>
 * 
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The namespace prefix to use for the imported/referenced Maya scene inside the 
 *     generated Maya scene.  If unset, the namespace is based on the filename.
 *   </DIV> <BR>
 *   
 *   Proxy Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The proxy name to be used for the referenced Maya scene.  If Build Type is set
 *     to Reference, this will be the proxy tag for the reference.  If Build Type is set
 *     to Import than this field will be ignored.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaBuildAction
  extends MayaBuildUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  MayaBuildAction() 
  {
    super("MayaBuild", new VersionID("2.4.24"), "Temerity",
	  "Builds a Maya scene from component scenes.");
    
    addUnitsParams();
    addStartFrameParam();
    addEndFrameParam();
    addInitalMELParam();
    addModelMELParam();
    addFinalMELParam();
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aDeferReferences,
	 "Should all created references be in the unloaded state.", 
	 false);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aDeferReferences);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aStartFrame);
      layout.addEntry(aEndFrame);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aModelMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
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
    boolean deferReferences = getSingleBooleanParamValue(aDeferReferences);

    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    
    scanAllSources(agenda);
    
    TreeMap<String, MayaBuildData> modelScenes = getModelDataByActualPrefix();
    
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

      /* the initial MEL script */ 
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

        mdata.writeBuildMEL(out, deferReferences);

        LinkedList<MayaBuildData> proxies = proxyScenes.get(prefixName);
        if (proxies != null) {
          for (MayaBuildData pdata : proxies)
            pdata.writeBuildMEL(out, deferReferences);
        }
        out.write("}\n");
      }

      /* the model MEL script */ 
      writeModelMEL(agenda, out);
      
      /* set the time options */ 
      out.write(genPlaybackOptionsMEL()); 

      /* save the file */ 
      out.write("// SAVE\n" + 
		"print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
		"file -save;\n\n");

      /* the final MEL script */ 
      writeFinalMEL(agenda, out);

      out.write("print \"ALL DONE.\\n\";\n");
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

  private static final long serialVersionUID = -5350214123630761668L;
}

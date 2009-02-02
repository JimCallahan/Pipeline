package us.temerity.pipeline.plugin.MayaFTNBuildAction.v2_4_5;

import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   F T N   B U I L D   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a new Maya scene that can contains both Maya and MentalRay texture nodes for all
 * the texture nodes that are linked to this node. <P> 
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are ignored by this action
 * unless assigned to one of the Initial, Texture or Final MEL parameters. <P> 
 * 
 * In order to evalute filesystem paths with $WORKING in them when setting the BuildMRay 
 * parameter of this action, it is necessary to have the Maya plugin "plEvalEnvNode" in the
 * Toolset's MAYA_PLUG_IN_PATH.  The "plEvalEnvNode" plugin is provided in the 
 * "app-extras/maya/plugins" subdirectory of Pipeline. 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Build Maya<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the Action build Maya file texture nodes. 
 *   </DIV> <BR>
 *   
 *   Build MRay <BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the Action build MentalRay texture nodes. 
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
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after scene creation
 *      and before creating any texture nodes.
 *   </DIV> <BR>
 * 
 *   Texture MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after texture node creation
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
 *   Exclude<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should a texture node not be generated for the source.  This allows sequences which are 
 *     not actually textures and not mel scripts to be ignored (for example, in a case where 
 *     the primary sequence of a node is simply for grouping purposes and all the secondary 
 *     sequences are actually the textures.  This parameter only needs to be activated to 
 *     exclude a file sequence.  Any file sequence without source params will be consider to
 *     be a valid texture file.  This is to preserve the desired behavior of artists simply 
 *     being able to link textures directly to the node without having to muck with 
 *     parameters.  Because of this, the action can be used identically to the older version
 *     of MayaFTNBuild.
 *   </DIV> <BR>
 */
public 
class MayaFTNBuildAction 
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MayaFTNBuildAction()
  {
    super("MayaFTNBuild", new VersionID("2.4.5"), "Temerity",
	  "Builds a Maya scene containing texture nodes for linked textures.");
    
    addUnitsParams();
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aBuildMaya,
	 "Should the Action build Maya file texture nodes.", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aBuildMRay,
	 "Should the Action build Mental Ray texture nodes.", 
	 false);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aInitialMEL,
	 "The source node containing the MEL script to evaluate just after scene creation " + 
         "and before importing any models.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aTextureMEL,
	 "The source node containing the MEL script to evaluate after creating texture " + 
         "nodes but before saving the generated Maya scene.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aFinalMEL,
	 "The source node containing the MEL script to evaluate after saving the generated " +
         "Maya scene.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aBuildMaya);
      layout.addEntry(aBuildMRay);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aTextureMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }

    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
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

    {
      ActionParam param = 
        new BooleanActionParam
        (aInclude,
         "Should a texture node be generated for the source.",
         true);
      params.put(aInclude, param);
    }

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
    /* MEL script paths */ 
    Path initialMEL = getMelScriptSourcePath(aInitialMEL, agenda);
    Path textureMEL = getMelScriptSourcePath(aTextureMEL, agenda);
    Path finalMEL   = getMelScriptSourcePath(aFinalMEL, agenda);
    
    pTextures = new TreeMap<Path,String>();
    pPsdTextures = new TreeSet<Path>();
    pAnimTextures = new TreeSet<Path>();
    for(String sname : agenda.getSourceNames()) {
      FileSeq fseq = agenda.getPrimarySource(sname);
      prepTextureNode(sname, fseq, false);
      for (FileSeq sseq : agenda.getSecondarySources(sname)) {
        prepTextureNode(sname, sseq, true);
      }
    }
    
    boolean buildMaya = getSingleBooleanParamValue(aBuildMaya);
    boolean buildMRay = getSingleBooleanParamValue(aBuildMRay);
    
    if(!buildMaya && !buildMRay) 
      throw new PipelineException
        ("The MayaFTNBuild Action requires that at least one of the " +
         aBuildMaya + " and " + aBuildMRay + " parameters is set!");
    
    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    
    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      if(buildMRay) {
        out.write
          ("// TEXTURE CREATION HELPER METHOD\n" + 
           "global proc string mayaFTNBuildHelper(string $attr)\n" + 
           "{" +
           "  if(!size(`ls mentalrayGlobals`))\n" + 
           "	miCreateDefaultNodes();\n" + 
           "\n" + 
           "  int $idx = 0;\n" + 
           "  int $connections = size(`listConnections mentalrayGlobals.textures`);\n" + 
           "  for($idx = 0; $idx<$connections; $idx++) {\n" + 
           "    if(size(`listConnections mentalrayGlobals.textures[$idx]`) == 0)\n" + 
           "	  break;\n" + 
           "  }\n" + 
           "\n" + 
           "  string $textureNode = `createNode mentalrayTexture`;\n" + 
           "\n" + 
           "  miDebug(\"append at index: \" + $idx);\n" + 
           "  miDebug(\"append node: \" + $textureNode);\n" + 
           "  connectAttr ($textureNode + \".message\") " +
           "              (\"mentalrayGlobals.textures\" + \"[\" + $idx + \"]\");\n" + 
           "\n" + 
           "  connectAttr -f ($textureNode + \".message\") $attr;\n" + 
           "\n" + 
           "  return $textureNode;\n" + 
           "}\n\n");
      }
      
      /* rename the current scene as the output scene */ 
      out.write
        ("// SCENE SETUP\n" + 
         "file -rename \"" + targetScene + "\";\n" + 
         "file -type \"" + sceneType + "\";\n\n");
      
      if(buildMRay) 
        out.write
          ("// PLUGIN LOADING\n" +
           "if (!`pluginInfo -q -l \"plEnvEvalNode.py\"`)\n" +
           "  loadPlugin \"plEvalEnvNode.py\";\n" +
           "if (!`pluginInfo -q -l Mayatomr `)\n" +
           "  loadPlugin Mayatomr;\n\n");

      out.write(genUnitsMEL());
      
      /* the initial MEL script */ 
      if(initialMEL != null) 
	out.write
          ("// INITIAL SCRIPT\n" + 
           "print \"Initial Script: " + initialMEL + "\\n\";\n" +
           "source \"" + initialMEL + "\";\n\n");
      
      out.write("// TEXTURES\n"); 
      for(Path tpath : pTextures.keySet()) {
        String tname = pTextures.get(tpath);
        
	if(buildMaya) {
          out.write("print \"Adding Maya Texture: " + tpath + "\\n\";\n");
	  out.write
            ("{\n" +
             "  string $node = renderCreateNode" + 
                  "(\"-as2DTexture\", \"\", \"file\", \"\", 0, 0, 1, 0, 0, \"\");\n" +
             "  string $newName = `rename $node \"" + tname + "\"`;\n" +
             "  setAttr -type \"string\" ($newName + \".ftn\") \"" + tpath + "\";\n");

	  if(pAnimTextures.contains(tpath))
	    out.write(" setAttr ($newName + \".useFrameExtension\") 1;\n"); 

    	  out.write("}\n\n");
	}

	if(buildMRay && !pPsdTextures.contains(tpath)) {
          out.write("print \"Adding MentalRay Texture: " + tpath + "\\n\";\n");
	  out.write
            ("{\n" + 
             "  mrCreateCustomNode -asTexture \"\" mib_texture_lookup;\n" + 
             "  string $sel[] = `ls -sl`;\n" + 
             "  string $lookup = $sel[0];\n" + 
             "  $lookup = `rename $lookup \"" + tname + "_mr\"`;\n" + 
             "  string $texvec = `createNode mib_texture_vector`;\n" + 
             "  string $mrtex = mayaFTNBuildHelper($lookup + \".tex\");\n" + 
             "  connectAttr -f ($texvec + \".outValue\") ($lookup + \".coord\");\n" + 
             "  string $plenv = `createNode -name \"textureFile\" plEvalEnvNode`;\n" + 
             "  connectAttr -f ($plenv + \".output\") ($mrtex + \".ftn\");\n" + 
             "  setAttr -type \"string\" ($plenv + \".in\") \"" + tpath + "\";\n" + 
             "}\n\n");
	}
      }
      
      /* the texture MEL script */ 
      if(textureMEL != null) 
	out.write("// TEXTURE SCRIPT\n" + 
                  "print \"Texture Script: " + textureMEL + "\\n\";\n" +
		  "source \"" + textureMEL + "\";\n\n");
      
      /* save the file */ 
      out.write("// SAVE\n" + 
		"print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
		"file -save;\n\n");

      /* the final MEL script */ 
      if(finalMEL != null) 
	out.write("// FINAL SCRIPT\n" + 
                  "print \"Final Script: " + finalMEL + "\\n\";\n" +
		  "source \"" + finalMEL + "\";\n\n");

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



  private void 
  prepTextureNode
  (
    String sname, 
    FileSeq fseq,
    boolean secondary
  ) 
    throws PipelineException
  {
    FilePattern fpat = fseq.getFilePattern();
    String prefix = fpat.getPrefix(); 
    String suffix = fpat.getSuffix();
    
    boolean process = true;
    if (secondary) {
      if (hasSecondarySourceParams(sname, fpat))
        process = getSecondarySourceBooleanParamValue(sname, fpat, aInclude);
    }
    else
      if (hasSourceParams(sname))
        process = getSourceBooleanParamValue(sname, aInclude);
    
    
    if (process) {
      if(suffix == null)
        throw new PipelineException("All texture source nodes must have a file suffix");
      if(suffix.equals("mel"))
        return;

      Path spath = new Path(sname);
      Path ppath = new Path(new Path("$WORKING"), spath.getParent());
      Path tpath = new Path(ppath, fseq.getPath(0));

      pTextures.put(tpath, prefix);

      if(!fseq.isSingle())
        pAnimTextures.add(tpath);

      if(suffix.equals("psd"))
        pPsdTextures.add(tpath);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -212876204631908735L;
  
  public static final String aTextureMEL = "TextureMEL";
  public static final String aInclude  = "Include";
  
  public static final String aBuildMaya = "BuildMaya";
  public static final String aBuildMRay = "BuildMRay";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TreeMap<Path, String> pTextures;
  private TreeSet<Path> pPsdTextures;
  private TreeSet<Path> pAnimTextures;


}
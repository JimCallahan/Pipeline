// $Id: MayaAnimBuildAction.java,v 1.1 2008/03/06 13:04:02 jim Exp $

package us.temerity.pipeline.plugin.MayaAnimBuildAction.v2_4_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaBuildUtils;

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
  public
  MayaAnimBuildAction() 
  {
    super("MayaAnimBuild", new VersionID("2.4.1"), "Temerity",
	  "Extends the MayaBuild Action to now allow for the referencing of Maya files " + 
          "that only contain curves, which are then hooked up to models which have either " + 
          "been Imported or Referenced.");
    
    addUnitsParams();

    addStartFrameParam();
    addEndFrameParam();
    
    addInitalMELParam();
    addAnimMELParam();
    addModelMELParam();
    addFinalMELParam();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
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
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aModel);
      choices.add(aAnimation);

      ActionParam param = 
	new EnumActionParam
	(aSceneType,
	 "Is this a model scene or an animation scene.", 
         aModel, choices);  	 
      params.put(param.getName(), param);
    }
    
    addOldBuildTypeSourceParam(params); 
    addNamespaceSourceParam(params);
    addPrefixNameSourceParam(params);
    
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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    /* model filenames */ 
    TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    TreeMap<String, Path> animPaths = new TreeMap<String, Path>();
    
    TreeMap<String, String> nameSpaces = new TreeMap<String, String>();
    TreeMap<String, String> reverseNameSpaces = new TreeMap<String, String>();
    TreeMap<String, Boolean> usesNameSpace = new TreeMap<String, Boolean>(); 
    TreeMap<String, String> buildTypes = new TreeMap<String, String>();
    
    for(String sname : agenda.getSourceNames()) {
      if(hasSourceParams(sname)) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        String suffix = fseq.getFilePattern().getSuffix();
        if(fseq.isSingle() && (suffix != null)) {
          if(suffix.equals("ma") || suffix.equals("mb")) {
            Path npath = new Path(sname);
            String type = getSourceStringParamValue(sname, aSceneType);
            String nspace = getSourceStringParamValue(sname, aPrefixName);
            if(nspace == null) {
              nspace = npath.getName();
            }
            if (type.equals(aModel))
              modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
            else {
              animPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
              nspace += "_a";
            }
            
            boolean useNSpace = false;
            {
              Boolean tf = (Boolean) getSourceParamValue(sname, aNameSpace);
              useNSpace = ((tf != null) && tf);
            }
            
            String buildType = getSourceStringParamValue(sname, aBuildType);
            if(buildType == null) 
              throw new PipelineException
                ("The value of the " + aBuildType + " source parameter for node " + 
                 "(" + sname + ") was not set!"); 

            buildTypes.put(sname, buildType);
            nameSpaces.put(sname, nspace);
            reverseNameSpaces.put(nspace, sname);
            usesNameSpace.put(sname, useNSpace);
          }
        }
      }
    }
    
    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    
    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      /* provide parameters as MEL variables */ 
      out.write(genFrameRangeVarsMEL()); 

      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
		"file -rename \"" + targetScene + "\";\n" + 
                "file -type \"" + sceneType + "\";\n\n");

      out.write(genUnitsMEL());
      
      writeInitialMEL(agenda, out);
      
      /* the model file reference imports */ 
      TreeMap<String, Path> all = new TreeMap<String, Path>(modelPaths);
      all.putAll(animPaths);
      for(String sname : all.keySet()) {
	Path mpath = all.get(sname);

        String buildType =  buildTypes.get(sname);

	out.write("// MODEL: " + sname + "\n" + 
                  "print \"" + buildType + " Model: " + mpath + "\\n\";\n" + 
                  "file\n");
        
	String nspace = nameSpaces.get(sname); 

        boolean useNSpace = usesNameSpace.get(sname);
	
        if(buildType.equals(aImport)) {
          out.write("  -import\n");
             
          if(useNSpace) 
            out.write("  -namespace \"" + nspace + "\"\n");
        }
        else if(buildType.equals(aReference)) {
          out.write("  -reference\n");
          
          if(useNSpace)
            out.write("  -namespace \"" + nspace + "\"\n");
          else
            out.write("  -renamingPrefix \"" + nspace + "\"\n");
        }
        else {
          throw new PipelineException
            ("Unknown value for the " + aBuildType + " source parameter for node " + 
             "(" + sname + ") encountered!");
        }
          
	{
	  String fname = mpath.getName();
	  if(fname.endsWith("ma")) 
            out.write("  -type \"mayaAscii\"\n");
	  else if(fname.endsWith("mb")) 
	    out.write("  -type \"mayaBinary\"\n");
          else 
            throw new PipelineException
              ("Unknown Maya scene format for source file (" + mpath + ")!");
	}
          
	out.write("  -options \"v=0\"\n" + 
                  "  \"$WORKING" + mpath + "\";\n\n"); 
      }
      
      writeModelMEL(agenda, out);
      
      out.write("global proc string removePrefix(string $name)\n" + 
      		"{\n" + 
      		"  string $toReturn;\n" + 
      		"  string $buffer[];\n" + 
      		"  tokenize($name, \"|\", $buffer);\n" + 
      		"  string $part;\n" + 
      		"  for ($part in $buffer) {\n" + 
      		"    string $buffer2[];\n" + 
      		"    tokenize($part, \":\", $buffer2);\n" + 
      		"    if ($toReturn == \"\")\n" + 
      		"      $toReturn += $buffer2[(size($buffer2) -1)];\n" + 
      		"    else\n" + 
      		"      $toReturn += \"|\" + $buffer2[(size($buffer2) -1)];\n" + 
      		"  }\n" + 
      		"  return $toReturn;    \n" + 
      		"}\n");
      
      out.write("global proc string addPrefix(string $name, string $prefix)\n" + 
      		"{\n" + 
      		"  string $toReturn;\n" + 
      		"  string $buffer[];\n" + 
      		"  tokenize($name, \"|\", $buffer);\n" + 
      		"  string $part;\n" + 
      		"  for ($part in $buffer) {\n" + 
      		"    if ($toReturn == \"\")\n" + 
      		"      $toReturn += $prefix + $buffer[(size($buffer) -1)];\n" + 
      		"    else\n" + 
      		"      $toReturn += \"|\" + $prefix + $buffer[(size($buffer) -1)];\n" + 
      		"  }\n" + 
      		"  return $toReturn;\n" + 
      		"}\n");
      
      for(String sname : animPaths.keySet()) {
	String nspace = nameSpaces.get(sname);
	String otherNspace = nspace.substring(0, nspace.length() - 2);
	
	String modelName = reverseNameSpaces.get(otherNspace);
	
	if (modelName != null) {
	  boolean useNSpace = usesNameSpace.get(sname);  
	  boolean otherUseNSpace = usesNameSpace.get(modelName);
	  
	  String buildType = buildTypes.get(sname);
	  String otherBuildType = buildTypes.get(modelName);
	  String prefix = generatePrefix(nspace, useNSpace, buildType);
	  String otherPrefix = generatePrefix(otherNspace, otherUseNSpace, otherBuildType);
	  
	  out.write
            ("{\n" +
             "  string $crvPrefix = \"" + prefix + "\";\n" + 
             "  string $objPrefix = \"" + otherPrefix + "\";\n" +
             "  string $obj = $crvPrefix + \"curveInfo\";\n" + 
             "  if (`objExists $obj`) {\n" + 
             "    int $size = `getAttr -s ($obj + \".cn\")`;\n" + 
             "    int $i = 0;\n" + 
             "    for ($i = 0; $i < $size; $i++) {\n" + 
             "      string $curve = `getAttr  ($obj + \".cn[\" + $i + \"]\")`;\n" + 
             "      string $attr = `getAttr  ($obj + \".an[\" + $i + \"]\")`;\n" + 
             "      $curve =  addPrefixToName($crvPrefix, $curve);\n" + 
             "      $attr  =  addPrefixToName($objPrefix, $attr);\n" + 
             "      print(\"connectAttr -f \" + $curve + \" \" + $attr + \"\\n\");\n" + 
             "      connectAttr -f $curve $attr;   \n" + 
             "    }\n" + 
             "  }\n" + 
             "  else {\n" + 
             "    string $curves[] = `ls -type animCurve ($crvPrefix + \"*\")`;\n" + 
             "    string $curve;\n" + 
             "    for ($curve in $curves) {\n" + 
             "      string $shortName = removePrefix($curve);\n" + 
             "      string $buffer[];\n" + 
             "      tokenize($shortName, \"_\", $buffer);\n" + 
             "      string $attr = $buffer[(size($buffer) -1)];\n" + 
             "      string $name = \"\";\n" + 
             "      int $length = (size($buffer) -1);\n" + 
             "      int $i;\n" + 
             "      for ($i = 0; $i < $length; $i++) {\n" + 
             "        $name += $buffer[$i];\n" + 
             "        if ($i != ($length - 1) )\n" + 
             "          $name += \"_\";\n" + 
             "      }\n" + 
             "      string $channel = addPrefix($name, $objPrefix) + \".\" + $attr;\n" + 
             "      print ($curve + \"\\t\" + $channel + \"\\n\");\n" + 
             "      if (!`getAttr -l $channel`)\n" + 
             "        connectAttr -f ($curve + \".output\") $channel;\n" + 
             "    }\n" +
             "  }\n" +
             "}\n");
	}
      }
      
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private String
  generatePrefix
  (
    String nspace,
    boolean usesNSpace,
    String buildType
  )
  {
    String toReturn = "";
    if (usesNSpace) {
      toReturn = nspace + ":";
    }
    else {
      if (buildType.equals("Reference"))
	toReturn = nspace + "_";
    }
    
    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2375347519920100749L;

}

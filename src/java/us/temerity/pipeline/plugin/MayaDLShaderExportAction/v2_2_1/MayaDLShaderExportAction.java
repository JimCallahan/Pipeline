// $Id: MayaDLShaderExportAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MayaDLShaderExportAction.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   D L   S H A D E R   E X P O R T   A C T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports all 3Delight shading related information in a Maya scene. <P> 
 * 
 * The type of exported Maya nodes include: delightShapeSet, delightShaderCollection and 
 * delightShader.  The Maya shape nodes which where members of the exported delightShapeSet
 * will not be exported and the assignment of shaders to geometry will not be retained. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene containing the shaders to be exported.
 *   </DIV> <BR>
 * 
 *   Pre Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate before exporting the shaders.
 *   </DIV> <BR>
 * 
 *   Post Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate after exporting the shaders.
 *   </DIV> 
 * </DIV>   
 */ 
public
class MayaDLShaderExportAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  MayaDLShaderExportAction()
  {
    super("MayaDLShaderExport", new VersionID("2.2.1"), "Temerity",
	  "Exports all 3Delgith shader information in a Maya scene."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene, 
	 "The Maya scene containing the shaders to be exported.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPreExportMEL,
	 "The MEL script to evaluate before exporting the shaders.", 
	 null); 
      addSingleParam(param);
    }
      
    {
      ActionParam param = 
	new LinkActionParam
	(aPostExportMEL,
	 "The MEL script to evaluate after exporting the shaders.", 
	 null); 
      addSingleParam(param);
    }

    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addSeparator();
      layout.addEntry(aPreExportMEL);
      layout.addEntry(aPostExportMEL);

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);  

    underDevelopment(); 
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
    /* the target Maya scene containing the exported shaders */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    
    /* MEL script paths */ 
    Path preExportMEL  = getMelScriptSourcePath(aPreExportMEL, agenda);
    Path postExportMEL = getMelScriptSourcePath(aPostExportMEL, agenda);

    /* create a temporary MEL script used to export the shaders */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

      if(preExportMEL != null) 
	out.write("// PRE-EXPORT SCRIPT\n" + 
                  "print \"Pre-Export Script: " + preExportMEL + "\\n\";\n" +
                  "source \"" + preExportMEL + "\";\n\n");

      out.write
        ("// DISCONNECT AND SELECT THE SHADERS\n" + 
         "{\n" +
         "  print \"Disconnecting GeoShaders:\\n\";\n" + 
         "  string $sets[] = `ls -type delightShapeSet`;\n" + 
         "  for($s in $sets) {\n" + 
         "    string $shapes[] = " + 
                "`listConnections -d off -s on -shapes on ($s + \".dsm\")`;\n" +
         "    if(size($shapes) > 0) {\n" +
         "      for($p in $shapes) {\n" + 
         "        $cmd = (\"disconnectAttr \" + $p + \".iog \" + $s + \".dsm -na;\");\n" + 
         "        print (\"  \" + $cmd + \"\\n\");\n" + 
         "        eval($cmd);\n" + 
         "      }\n" + 
         "    }\n" + 
         "  }\n" + 
         "  print(\"\\n\");\n" + 
         "\n" + 
         "  print \"Disconnecting GeoAttribs:\\n\";\n" + 
         "  string $attrs[] = `ls -type delightGeoAttribs`;\n" + 
         "  for($a in $attrs) {\n" + 
         "    string $shapes[] = " + 
                "`listConnections -d off -s on -shapes on ($a + \".dsm\")`;\n" +
         "    if(size($shapes) > 0) {\n" +
         "      for($p in $shapes) {\n" + 
         "        $cmd = (\"disconnectAttr \" + $p + \".iog \" + $a + \".dsm -na;\");\n" + 
         "        print (\"  \" + $cmd + \"\\n\");\n" + 
         "        eval($cmd);\n" + 
         "      }\n" + 
         "    }\n" + 
         "  }\n" + 
         "  print(\"\\n\");\n" + 
         "\n" +
         "  string $collects[] = `ls -type delightShaderCollection`;\n" + 
         "  select -r -ne $sets $attrs $collects;\n" + 
         "}\n\n");

      out.write
        ("// EXPORT THE SELECTED SHADERS\n" +
         "print \"Exporting Shaders Scene: " + targetScene + "\\n\";\n" + 
         "{\n" + 
         "  string $names[] = `selectedNodes`;\n" + 
         "  if(size($names) > 0) {\n" + 
         "    file -op \"v=0\" -type \"" + sceneType + "\" -es \"" + targetScene + "\";\n" + 
         "  }\n" + 
         "  else {\n" + 
         "    file -new;\n" + 
         "    file -rename \"" + targetScene + "\";\n" + 
         "    file -type \"" + sceneType + "\";\n" + 
         "    file -save;\n" + 
         "  }\n" + 
         "}\n\n");
      
      if(postExportMEL != null)
	out.write("// POST-EXPORT SCRIPT\n" + 
                  "print \"Post-Export Script: " + postExportMEL + "\\n\";\n" +
                  "source \"" + postExportMEL + "\";\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    return createMayaSubProcess(sourceScene, script, true, agenda, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5522969351762485491L;

  public static final String aMayaScene       = "MayaScene";
  public static final String aPostExportMEL   = "PostExportMEL";
  public static final String aPreExportMEL    = "PreExportMEL";

}

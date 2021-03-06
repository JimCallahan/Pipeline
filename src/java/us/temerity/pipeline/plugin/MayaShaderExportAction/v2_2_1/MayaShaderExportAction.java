// $Id: MayaShaderExportAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaShaderExportAction.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   S H A D E R  E X P O R T   A C T I O N                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports all shaders with a given prefix.<P>
 * 
 * This will select all the materials, textures, and render utilities with a given prefix.
 * It will not select or export shading groups. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene containing the shaders to be exported.
 *   </DIV> <BR>
 * 
 *   Selection Prefix <BR>
 *   <DIV style="margin-left: 40px;">
 *     The prefix that will be used to select the shaders to export. 
 *     If it is a namespace, you need to include the ":" after the name.
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
class MayaShaderExportAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  MayaShaderExportAction()
  {
    super("MayaShaderExport", new VersionID("2.2.1"), "Temerity",
	  "Exports all shaders with a given prefix.");
    
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
	new StringActionParam
	(aSelectionPrefix, 
	 "The prefix that will be used to select the shaders to export. " + 
	 "If it is a namespace, you need to include the \":\" after the name.",
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
      layout.addEntry(aSelectionPrefix);
      layout.addSeparator();
      layout.addEntry(aPreExportMEL);
      layout.addEntry(aPostExportMEL);

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
        ("// SELECT THE SHADERS\n" + 
        "{\n" +
         "  string $filter1 = `itemFilterRender -shaders 1`;\n" +
         "  string $filter2 = `itemFilterRender -anyTextures 1`;\n" +
         "  string $filter3 = `itemFilterRender -renderUtilityNode 1`;\n" +
         "  string $one[] = `lsThroughFilter -na $filter1 -sort byName -reverse false`;\n" +
         "  string $two[] = `lsThroughFilter -na $filter2 -sort byName -reverse false`;\n" +
         "  string $three[] = `lsThroughFilter -na $filter3 -sort byName -reverse false`;\n" +
         "  select -r -ne $one $two $three;\n" + 
         "}\n");
    
      String prefixName = getSingleStringParamValue(aSelectionPrefix);
      if(prefixName != null)
	out.write("string $mats[] = `ls -sl \"" + prefixName + "*\"`;\n\n");
      else
	out.write("string $mats[] = `ls -sl`;\n\n");

      out.write
        ("// EXPORT THE SELECTED SHADERS\n" + 
         "print \"Exporting Shaders Scene: " + targetScene + "\\n\";\n" + 
         "if(size($mats) > 0) {\n" + 
         "  select -r $mats;\n" + 
         "  file -op \"v=0\" -type \"" + sceneType + "\" -es \"" + targetScene + "\";\n" + 
         "}\n" + 
         "else {\n" + 
         "  file -new;\n" + 
         "  file -rename \"" + targetScene + "\";\n" + 
         "  file -type \"" + sceneType + "\";\n" + 
         "  file -save;\n" + 
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

  private static final long serialVersionUID = 5801808055551457595L;

  public static final String aMayaScene       = "MayaScene";
  public static final String aPostExportMEL   = "PostExportMEL";
  public static final String aPreExportMEL    = "PreExportMEL";
  public static final String aSelectionPrefix = "SelectionPrefix";

}

// $Id: MayaObjExportAction.java,v 1.1 2008/03/06 13:06:18 jim Exp $

package us.temerity.pipeline.plugin.MayaObjExportAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   O B J   E X P O R T   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports per-frame OBJ files containing the baked point animation for a selected group of 
 * objects.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. 
 *   </DIV> <BR>
 * 
 *   Export Set 
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya Set used to select the DAG nodes to export from the Maya
 *     scene.  Can also be the name of a single geometry DAG node. 
 *   </DIV><BR>
 *   
 *   <I>MEL Scripts</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pre Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The MEL script to evaluate before exporting the OBJ files. 
 *     </DIV> <BR>
 *   
 *     Post Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The MEL script to evaluate after exporting the OBJ files. 
 *     </DIV> 
 *    </DIV> 
 * </DIV> <P> 
 */
public
class MayaObjExportAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaObjExportAction() 
  {
    super("MayaObjExport", new VersionID("2.4.1"), "Temerity",
          "Exports per-frame OBJ files containing the baked point animation for a " + 
          "selected group of objects."); 

    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene,
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aExportSet,
         "The name of the Maya Set used to select the DAG nodes to export from the Maya " + 
         "scene.  Can also be the name of a single geometry DAG node.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPreExportMEL,
         "The MEL script to evaluate before exporting the OBJ files.", 
           null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPostExportMEL,
         "The MEL script to evaluate after exporting the OBJ files.", 
         null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aExportSet);
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
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    
    /* MEL script paths */ 
    Path preExportMEL  = getMelScriptSourcePath(aPreExportMEL, agenda);
    Path postExportMEL = getMelScriptSourcePath(aPostExportMEL, agenda);

    /* name of the export set */ 
    String exportSet = getSingleStringParamValue(aExportSet); 
    if(exportSet == null) 
      throw new PipelineException
        ("No " + aExportSet + " was specified!"); 

    /* the OBJ files to export */ 
    ArrayList<Path> targetPaths = getPrimaryTargetPaths(agenda, "obj", "OBJ Geometry File");

    /* create a temporary MEL script to create the geometry cache */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

      out.write
        ("if(!`pluginInfo -q -l \"objExport\"`)\n" + 
         "  loadPlugin \"objExport\";\n\n");
      
      if(preExportMEL != null) 
	out.write("// PRE-EXPORT SCRIPT\n" + 
                  "print \"Pre-Export Script: " + preExportMEL + "\\n\";\n" +
                  "source \"" + preExportMEL + "\";\n\n");

      {
        out.write
          ("// EXPORT THE OBJs\n" + 
           "{\n" + 
           "select -r \"" + exportSet + "\";\n\n");
        
        FrameRange range = agenda.getPrimaryTarget().getFrameRange();
        int frame = range.getStart();
        for(Path tpath : targetPaths) {
          out.write
            ("  currentTime -e " + frame + ";\n" + 
             "  print \"Writing: " + tpath + "\\n\";\n" + 
             "  file -type \"OBJexport\" -exportSelected -force \"" + tpath + "\";\n\n");

          frame += range.getBy();
        }

        out.write("}\n\n"); 
      }

      if(postExportMEL != null)
	out.write("// POST-EXPORT SCRIPT\n" + 
                  "print \"Post-Export Script: " + postExportMEL + "\\n\";\n" +
                  "source \"" + postExportMEL + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(sourceScene, script, true, agenda, outFile, errFile);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3034647404233964944L;
  
  public static final String aMayaScene     = "MayaScene";
  public static final String aExportSet     = "ExportSet";
  public static final String aPostExportMEL = "PostExportMEL";
  public static final String aPreExportMEL  = "PreExportMEL";

}

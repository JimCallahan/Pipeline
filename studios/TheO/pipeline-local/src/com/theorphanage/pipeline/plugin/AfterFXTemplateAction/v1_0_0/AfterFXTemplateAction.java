package com.theorphanage.pipeline.plugin.AfterFXTemplateAction.v1_0_0;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.ActionAgenda;
import us.temerity.pipeline.ActionParam;
import us.temerity.pipeline.EnumActionParam;
import us.temerity.pipeline.LinkActionParam;
import us.temerity.pipeline.OsType;
import us.temerity.pipeline.Path;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.SubProcessHeavy;
import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.plugin.AfterFXActionUtils;
import us.temerity.pipeline.plugin.CommonActionUtils;

public class AfterFXTemplateAction extends AfterFXActionUtils {

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AfterFXTemplateAction()
  {
    super("AfterFXTemplate", new VersionID("1.0.0"), "TheO",
          "Generates a new After Effects scene from After Effects template and component image sources.");

    {
      ActionParam param = 
        new LinkActionParam
        (aTemplateName,
         "The name of the After Effects Scene to be used as a template.",
         null);
      addSingleParam(param);
    }
    
    underDevelopment();
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
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
      choices.add(aPlate);
      choices.add(aRoto);
      
      ActionParam param = 
        new EnumActionParam
        (aSourceType, 
         "Select input file type.",
         aPlate,
         choices);
      params.put(param.getName(), param);
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
   * 
   *   If unable to prepare a SubProcess due to illegal, missing or incompatible 
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
    Path targetPath = getAfterFXSceneTargetPath(agenda);
    Path templatePath = getPrimarySourcePath(aTemplateName, agenda, "aep", "AfterFX template scene");
    
    TreeSet<String> sourceNames = new TreeSet<String>(agenda.getSourceNames());
    sourceNames.remove(getSingleStringParamValue(aTemplateName));
    
    TreeSet<String> plateNames = new TreeSet<String>();
    TreeSet<String> rotoNames = new TreeSet<String>();
    
    for (String sourceName : sourceNames) {
      if (hasSourceParams(sourceName)) {
        String type = getSourceStringParamValue(sourceName, aSourceType);
        if (type!=null){
          
          if (type.equals(aPlate)) 
            plateNames.add(sourceName);
          
          if (type.equals(aRoto)) 
            rotoNames.add(sourceName);
          
        }
      }
    }
    
    /* create a temporary JSX script file */ 
    File script = createTemp(agenda, "jsx");

    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.write("\r\n" + 
                "app.beginSuppressDialogs();\n" + 
                "var f = new File(\"" + CommonActionUtils.escPath(templatePath) + "\");\n" +
                "app.open(f);\n" +
                "var root = app.project.rootFolder;\r\n" + 
      		"\r\n" + 
      		"var sourceFolder;\r\n" + 
      		"for (i = 1; i <= root.numItems ; i++) {\r\n" + 
      		"  if (root.item(i).name == \"Source\")\r\n" + 
      		"    sourceFolder = root.item(i);\r\n" + 
      		"}\r\n" + 
      		"\r\n" + 
      		"var platesFolder;\r\n" + 
      		"var rotoFolder;\r\n" + 
      		"for (i = 1; i <= sourceFolder.numItems ; i++) {\r\n" + 
      		"  if (sourceFolder.item(i).name == \"Roto\")\r\n" + 
      		"    rotoFolder = sourceFolder.item(i);\r\n" + 
      		" else if (sourceFolder.item(i).name == \"Plates\")\r\n" + 
      		"    platesFolder = sourceFolder.item(i);\r\n" + 
      		"}\r\n" + 
      		"\r\n" + 
      		"rotoFiles = new Array() ;\r\n" + 
      		"for (var i = 1; i <= rotoFolder.numItems;i++){\r\n" + 
      		" if (rotoFolder.item(i) instanceof FootageItem)\r\n" + 
      		"        rotoFiles[rotoFiles.length] = rotoFolder.item(i);\r\n" + 
      		"}\r\n" + 
      		"platesFiles = new Array() ;\r\n" + 
      		"for (var i = 1; i <= platesFolder.numItems;i++){\r\n" + 
      		" if (platesFolder.item(i) instanceof FootageItem)\r\n" + 
      		"        platesFiles[platesFiles.length] = platesFolder.item(i);\r\n" + 
      		"}" +
      		"var newPlates = new Array();\r\n" + 
      		"var newRotos = new Array();\r\n");
      
      for (String roto : rotoNames) {
        Path sPath = getWorkingNodeFilePath(agenda, roto, agenda.getPrimarySource(roto));
        out.write("newRotos[newRotos.length] = \""+ CommonActionUtils.escPath(sPath.toOsString())+"\" ;\n");
      }
      for (String plate : plateNames) {
        Path sPath = getWorkingNodeFilePath(agenda, plate, agenda.getPrimarySource(plate));
        out.write("newPlates[newPlates.length] = \""+ CommonActionUtils.escPath(sPath.toOsString())+"\" ;\n");
      }
      
      out.write("var platesLength = Math.min(newPlates.length, platesFiles.length);\r\n" + 
      		"var rotoLength = Math.min(newRotos.length, rotoFiles.length);\r\n");
      
      out.write("for (var i = 0; i < platesLength;i++){\r\n" + 
      		"        var newPlate = new File(newPlates[i]);\r\n" + 
      		"        if (newPlate.exists)\r\n" + 
      		"           platesFiles[i].replaceWithSequence(newPlate,false);\r\n" + 
      		"}\r\n" + 
      		"if (newPlates.length > platesFiles.length) {\r\n" + 
      		"        for (var i = platesFiles.length; i < newPlates.length ; i++) {\r\n" + 
      		"                var plateFile = new File(newPlates[i]);\r\n" + 
      		"                var plateImportOptions = new ImportOptions(plateFile);\r\n" + 
      		"                plateImportOptions.importAs = ImportAsType.FOOTAGE\r\n" + 
      		"                plateImportOptions.sequence = true;\r\n" + 
      		"                var plateFootageItem = app.project.importFile(plateImportOptions);\r\n" + 
      		"                plateFootageItem.parentFolder = plateFolder;\r\n" + 
      		"\r\n" + 
      		"   }\r\n" + 
      		"}\r\n" + 
      		"else if (newPlates.length < platesFiles.length) {\r\n" + 
      		"        for (var i = newPlates.length; i < platesFiles.length ; i++) {\r\n" + 
      		"                platesFiles[i].remove();\r\n" + 
      		"        }\r\n" + 
      		"}\n\r");
      
      out.write("for (var i = 0; i < rotoLength;i++){\r\n" + 
                "        var newRoto = new File(newRotos[i]);\r\n" + 
                "        if (newRoto.exists)\r\n" + 
                "          rotoFiles[i].replaceWithSequence(newRoto,false);\r\n" + 
                "}\r\n" + 
                "if (newRotos.length > rotoFiles.length) {\r\n" + 
                "        for (var i = rotoFiles.length; i < newRotos.length ; i++) {\r\n" + 
                "                var rotoFile = new File(newRotos[i]);\r\n" + 
                "                var rotoImportOptions = new ImportOptions(rotoFile);\r\n" + 
                "                rotoImportOptions.importAs = ImportAsType.FOOTAGE\r\n" + 
                "                rotoImportOptions.sequence = true;\r\n" + 
                "                var rotoFootageItem = app.project.importFile(rotoImportOptions);\r\n" + 
                "                rotoFootageItem.parentFolder = rotoFolder;\r\n" + 
                "\r\n" + 
                "   }\r\n" + 
                "}\r\n" + 
                "else if (newRotos.length < rotoFiles.length) {\r\n" + 
                "        for (var i = newRotos.length; i < rotoFiles.length ; i++) {\r\n" + 
                "                rotoFiles[i].remove();\r\n" + 
                "        }\r\n" + 
                "}\r\n");

      out.write("var f = new File(\"" + CommonActionUtils.escPath(targetPath.toOsString()) + "\");\n" +
          "app.project.save(f);\n" +
          "app.quit();\n");
      
      out.close();
      
    } catch(IOException ex) {
      throw new PipelineException
      ("Unable to write temporary JSX script file (" + script + ") for Job " + 
       "(" + agenda.getJobID() + ")!\n" +
       ex.getMessage());
    }
    
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-m");
      args.add("-r");
      args.add(script.getAbsolutePath());
      
      return createSubProcess(agenda, "AfterFX.exe", args, outFile, errFile);
    }
  }
    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = 4253318584434505032L;

  public static final String aTemplateName = "TemplateName";
  public static final String aSourceType = "SourceType";
  public static final String aPlate = "Plate";
  public static final String aRoto = "Roto";

  
}

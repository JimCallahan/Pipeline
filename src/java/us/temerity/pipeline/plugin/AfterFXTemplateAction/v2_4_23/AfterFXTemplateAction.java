package us.temerity.pipeline.plugin.AfterFXTemplateAction.v2_4_23;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   T E M P L A T E   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Generate a new After Effects scene from a template and some image sequences. <p>
 * 
 * This Action works by finding Folders in the template scene that have the same name as the
 * ReplaceName source parameter of a source and them replacing the first item in that folder
 * with the source.  Obviously it only really makes sense to have a single item in any given
 * folder, since only the first object is being replaced. <p>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Template Scene<BR>
 *   <DIV style="margin-left: 40px;">
 *     The template AfterFX scene which is going to have it sources replaced.
 *   </DIV> <BR>
 *   
 *   Fix Paths<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether the scene should have its paths remapped to point at the current working area.
 *   </DIV> <BR>
 *   
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Replace Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the directory to find and replace the source in.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class AfterFXTemplateAction
  extends AfterFXActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AfterFXTemplateAction()
  {
    super("AfterFXTemplate", new VersionID("2.4.23"), "Temerity",
          "Generate a new After Effects scene from a template and some image sequences.");
    
    {
      ActionParam param = 
        new LinkActionParam
        (aTemplateScene,
         "The template scene that this AfterFX scene is going to be built from.",
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aFixPaths,
         "Do the paths needs to be fixed in this file.",
         true);
      addSingleParam(param);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aReplaceName);
      
      setSourceLayout(layout);
    }
    
    {
      LayoutGroup group = new LayoutGroup(true);
      group.addEntry(aTemplateScene);
      group.addEntry(aFixPaths);
      
      setSingleLayout(group);
    }
    
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
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
        new StringActionParam
        (aReplaceName,
         "The name of the folder to find the sequence to be replaced with this source.",
         null);
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
    Path targetPath = getAfterFXSceneTargetPath(agenda);
    
    Path sourceScene = getAfterFXSceneSourcePath(aTemplateScene, agenda);
    
    int csv = -1;
    
    if(PackageInfo.sOsType == OsType.MacOS) { 
      String vsn = agenda.getEnvironment().get("ADOBE_CS_VERSION");

      try {
        if(vsn == null)
          throw new PipelineException
            ("The ADOBE_CS_VERSION was not defined!"); 
        csv = Integer.valueOf(vsn);
      }
      catch(NumberFormatException ex) {
        throw new PipelineException
          ("The ADOBE_CS_VERSION given (" + vsn + ") was not a number!"); 
      }

      if(csv < 3)
        throw new PipelineException
          ("The Mac OS X is only supported for Adobe After Effects CS3 and above!");
    }
    
    boolean fixPaths = getSingleBooleanParamValue(aFixPaths);
    
    File script = createTemp(agenda, "jsx");
    try {      
      BufferedWriter out = new BufferedWriter(new FileWriter(script));
      
      out.write(
        "app.exitAfterLaunchAndEval = true;\n" + 
        "app.beginSuppressDialogs();\n" + 
        "app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
        "var f = new File(\"" + CommonActionUtils.escPath(sourceScene) + "\");\n" +
        "app.open(f);\n\n");
      
      if (fixPaths)
        writeSourceRelinkingScript(out, agenda);
      
      for (String source : agenda.getSourceNames()) {
        if (hasSourceParams(source)) {
          String replaceName = getSourceStringParamValue(source, aReplaceName, false);
          FileSeq seq = agenda.getPrimarySource(source);
          ArrayList<Path> fullPaths = getWorkingNodeFilePaths(agenda, source, seq);
          Path first = fullPaths.get(0);
          out.write(
            "{\n" + 
            "  var name = \"" + replaceName + "\";\n" + 
            "  var proj = app.project;\n" + 
            "  var list = proj.items;\n" + 
            "  for (j=1; j <= list.length; j++) {\n" + 
            "    var item = list[j];\n" + 
            "    if (item instanceof FolderItem) {\n" + 
            "      if (item.name == name) {\n" + 
            "        if (item.numItems == 1) {\n" + 
            "          var toReplace = item.items[1];\n" +
            "          var newReplace = new File(\"" + CommonActionUtils.escPath(first) + 
                      "\");\n");
          if (fullPaths.size() > 1)
            out.write("          toReplace.replaceWithSequence(newReplace, false);\n");
          else
            out.write("          toReplace.replace(newReplace);\n");
          out.write(
           "        }\n" + 
           "      }\n" + 
           "    }\n" + 
           "  }\n" + 
           "}\n");
        }
      }
      
      out.write(
        "var f = new " + 
        "File(\"" + CommonActionUtils.escPath(targetPath.toOsString()) + "\");\n" +
        "app.project.save(f);\n" +
        "app.quit();\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary JSX script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    if(PackageInfo.sOsType == OsType.MacOS) { 
      File tempFile = createTemp(agenda, "oas");
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
        
        writeAfterFXAppleScriptLauncher(out, script, String.valueOf(csv));  
          
        out.close();
      }
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary appleScript file (" + script + ") to launch the " + 
           "AfterFX Editor!\n" +
           ex.getMessage());
      }

      ArrayList<String> args = new ArrayList<String>();
      args.add(tempFile.getPath());
      
      return createSubProcess(agenda, "osascript", args, outFile, errFile);
    }
    else {
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

  private static final long serialVersionUID = -6639201465602376264L;
  
  public static final String aTemplateScene = "TemplateScene";  
  public static final String aFixPaths      = "FixPaths";
  public static final String aReplaceName   = "ReplaceName";
}

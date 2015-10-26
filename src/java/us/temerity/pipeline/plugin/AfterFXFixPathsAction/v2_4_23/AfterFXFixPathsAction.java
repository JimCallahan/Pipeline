package us.temerity.pipeline.plugin.AfterFXFixPathsAction.v2_4_23;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   F I X   P A T H S   A C T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Generate a new After Effects scene in which all the paths are local to the working 
 * area. <p>
 * 
 * This Action is intended to be used on an Intermediate node before other nodes that use an
 * AfterFX scene, most likely a node which uses the aerender command to generate images from
 * the after effects scene.  By having this node prior to the render, the render is guaranteed 
 * to work correctly in all working areas (since the Intermediate state of the node with this
 * action on it will force this Action to be run each time the scene is checked-out), without 
 * having to modify the scene at render time on every machine.  Since modifying the afterfx
 * scene requires a full afterfx license rather than just a render license, it is much more
 * efficient to only modify the scene's paths once.  Once this Action is run to fix a scene's
 * paths, all further actions which are using that scene must run on the same operating 
 * system. <p>
 * 
 * This action could also be used before other AfterFX automation actions, which can then have
 * their FixPaths option disabled, enabling them to process faster.  In all cases, this action
 * should always be run on an Intermediate node to ensure that is always correctly 
 * regenerated.  Once this Action is run to fix a scene's paths, all further actions which 
 * are using that scene must run on the same operating system. <p>
 * 
 * This action defines the following single valued parameters: <BR>
 *  
 * <DIV style="margin-left: 40px;">
 *   AfterFX Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The scene whose paths we're going to fix.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class AfterFXFixPathsAction
  extends AfterFXActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  public
  AfterFXFixPathsAction()
  {
    super("AfterFXFixPaths", new VersionID("2.4.23"), "Temerity",
          "Generate a new After Effects scene in which all the paths are local to " +
          "the working area.");
    
    {
      ActionParam param =
        new LinkActionParam
        (aAfterFXScene, 
         "The source After Effects scene node.", 
         null);
      addSingleParam(param);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
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
    Path targetPath = getAfterFXSceneTargetPath(agenda);
    
    Path sourceScene = getAfterFXSceneSourcePath(aAfterFXScene, agenda);
    
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
    File script = createTemp(agenda, "jsx");
    try {      
      BufferedWriter out = new BufferedWriter(new FileWriter(script));
      
      out.write(
        "app.exitAfterLaunchAndEval = true;\n" + 
        "app.beginSuppressDialogs();\n" + 
        "app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
        "var f = new File(\"" + CommonActionUtils.escPath(sourceScene) + "\");\n" +
        "app.open(f);\n\n");
      
      writeSourceRelinkingScript(out, agenda);
      
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

   private static final long serialVersionUID = 8404126044185866119L;
}

// $Id: AfterFXActionUtils.java,v 1.4 2008/01/23 16:25:58 jim Exp $

package us.temerity.pipeline.plugin;

import java.io.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   A C T I O N   U T I L S                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Action plugins which interact with After Effects scenes.
 * <p>
 * This class provides convenience methods which make it easier to write Action plugins
 * which create dynamic JavaScripts scripts and After Effects scenes.
 */
public 
class AfterFXActionUtils
  extends CompositeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct with the given name, version, vendor and description.
   * 
   * @param name
   *   The short name of the action.
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc
   *   A short description of the action.
   */
  protected 
  AfterFXActionUtils
  (
    String name, 
    VersionID vid, 
    String vendor, 
    String desc
  )
  {
    super(name, vid, vendor, desc);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y    M E T H O D S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the abstract path to the single primary After Effects scene associated with the
   * target node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the target After Effects scene.
   */
  public Path 
  getAfterFXSceneTargetPath
  (
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimaryTargetPath(agenda, "aep", "After Effects scene file");
  }
  
  /**
   * Get the abstract path to the single primary JavaScript script associated with a source
   * node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued MEL parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the MEL script or null if none was specified.
   */
  public Path
  getJavaScriptSourcePath
  (
    String pname,
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimarySourcePath(pname, agenda, "jsx", "JavaScript file");
  }

  /**
   * Get the abstract path to the single primary After Effects Scene associated with a
   * source node specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued Scene parameter.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The path to the MEL script or null if none was specified.
   */
  public Path
  getAfterFXSceneSourcePath
  (
    String pname,
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimarySourcePath(pname, agenda, "aep", "After Effects scene file");
  }
  
  /**
   * Write a applescript afterfx launcher into the given buffer. <p> 
   * 
   * @param out
   *   The writer to write the script into.
   *   
   * @param script
   *   The javascript to be launched with AfterFX.
   *   
   * @param csv
   *   The version of AfterFX CS to be launched (should be a string representation of the 
   *   version number, like '4')
   *   
   * @throws IOException
   *   If there is a problem writing to the buffer.
   */
  protected void 
  writeAfterFXAppleScriptLauncher
  (
    BufferedWriter out,
    File script,
    String csv
  )
    throws IOException
  {
    out.write(
      "set variable to \"" + script.getAbsolutePath() + "\"\n" +
      "set toRun to POSIX file variable\n" +
      "tell application \"Adobe After Effects CS" + csv + "\"\n" + 
      "  DoScript toRun \n" +
      "end tell\n");
  }
  
  
  /**
   * Write a javascript which remaps After Effects path to point at the current working
   * directory. <p>
   * 
   * This should work on either windows or mac, but given the odd nature of how After Effects
   * handles paths, it may need to be tweaked given the individual studio stetup.
   * 
   * @param out
   *   The FileWriter to write the script into.
   *   
   * @param agenda
   *   The ActionAgenda for the action
   * 
   * @param csv
   *   The version of AfterFX being used in the action's toolset. 
   *   
   * @throws PipelineException
   * @throws IOException
   */
  public void
  writeSourceRelinkingScript
  (
    BufferedWriter out,
    ActionAgenda agenda
  ) 
    throws PipelineException, IOException
  {
    
    String currentWorking = agenda.getEnvironment().get("WORKING").replaceAll("\\\\", "/");
    
    String winWorkingStart = PackageInfo.getWorkPath(OsType.Windows).toString();
    String winRepo = PackageInfo.getRepoPath(OsType.Windows).toString();
    
    if (winWorkingStart.matches("[a-zA-Z]\\:.*")) {
      char driveLetter = winWorkingStart.charAt(0);
      driveLetter = Character.toLowerCase(driveLetter);
      winWorkingStart = "/" + driveLetter + winWorkingStart.substring(2);
      winRepo = "/" + driveLetter + winRepo.substring(2);
      /* If current OS is Windows, we need to fix the current working version too. */
      if (PackageInfo.sOsType == OsType.Windows)
        currentWorking = 
          "/" + driveLetter + currentWorking.substring(2, currentWorking.length());
    }
    
    String macWorkingStart = PackageInfo.getWorkPath(OsType.MacOS).toString(); 
    String macRepo = PackageInfo.getRepoPath(OsType.MacOS).toString();
    
    out.write(
      "var winWorkingStart = \"" + winWorkingStart + "/\";\n" +
      "var macWorkingStart = \"" + macWorkingStart + "/\";\n" +
      "var currentWorking = \"" + currentWorking + "/\";\n" +
      "\n" +
      "\n" +
      "var winRepo = \"" + winRepo + "/\";\n" + 
      "var macRepo = \"" + macRepo + "/\";\n" +
      "\n" +
      "var proj = app.project;\n" +
      "var list = proj.items;\n" +
      "var regExp1 = new RegExp(winWorkingStart, \"gi\");\n" + 
      "var regExp2 = new RegExp(macWorkingStart, \"g\");\n" +
      "var regWinRepo  = new RegExp(winRepo, \"gi\");\n" +
      "var regMacRepo  = new RegExp(macRepo, \"gi\");\n" +
      "var macFixRegExp = new RegExp(\".*/\", \"g\");\n" +
      "for (j=1; j <= list.length; j++)\n" + 
      "{\n" + 
      "  var item = list[j];\n" +
      "  if (item instanceof FootageItem  && item.file != null)\n" + 
      "  {\n" + 
      "     var file = item.file;\n" +
      "     var fileName = file.fullName;\n" +
      "     var doStuff = false;\n" +
      "     var endName = \"\";\n" +
      "     fileName = fileName.replace(/:/g, \"/\");\n" +
      "\n" +
      "     if (regWinRepo.test(fileName)) {\n" + 
      "       var stripped = fileName.replace(regWinRepo, \"\");\n" + 
      "       var split = stripped.split(\"/\");\n" + 
      "       var newEnd = \"\";\n" + 
      "       for (i = 0; i < split.length -3; i++) {\n" + 
      "         newEnd += split[i] += \"/\";\n" + 
      "       }\n" + 
      "       newEnd += split[split.length-1];\n" + 
      "       fileName = currentWorking + newEnd;\n" + 
      "     }\n" +
      "     if (regMacRepo.test(fileName)) {\n" + 
      "       var stripped = fileName.replace(regMacRepo, \"\");\n" + 
      "       var split = stripped.split(\"/\");\n" + 
      "       var newEnd = \"\";\n" + 
      "       for (i = 0; i < split.length -3; i++) { \n" + 
      "         newEnd += split[i] += \"/\";\n" + 
      "       }\n" + 
      "       newEnd += split[split.length-1];\n" + 
      "       fileName = currentWorking + newEnd; \n" + 
      "     }\n" +
      "     if (regExp1.test(fileName)) {\n" +
      "       endName = fileName.replace(regExp1, \"\");\n" +
      "       doStuff = true;\n" +
      "     }\n" + 
      "     else if (regExp2.test(fileName)) {\n" +
      "       endName = fileName.replace(macWorkingStart, \"\");\n" +
      "       doStuff = true;\n" +
      "     }\n" + 
      "     if (doStuff) {\n" +
      "       var split = endName.split(\"/\");\n" + 
      "       var newEnd = \"\";\n" + 
      "       for (i=2; i < split.length; i++)\n" + 
      "       {\n" + 
      "         newEnd += split[i];\n" + 
      "         if (i != split.length -1)\n" + 
      "           newEnd += \"/\";\n" + 
      "       }\n" + 
      "       var newFileName = currentWorking + newEnd;\n" +
      "       var newFile = new File(newFileName);\n" + 
      "       item.replaceWithSequence(newFile, false);\n" + 
      "    }\n" + 
      "  }\n" + 
      "}\n");
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2432054682863935407L;
  
  public static final String aAfterFXScene     = "AfterFXScene";
  public static final String aPreRenderScript  = "PreRenderScript";
  public static final String aPostRenderScript = "PostRenderScript";
}

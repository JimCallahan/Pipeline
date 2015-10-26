// $Id: AfterFXEditor.java,v 1.2 2008/09/14 22:14:42 jim Exp $

package us.temerity.pipeline.plugin.AfterFXEditor.v2_4_23;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Motion Graphics and Compositing program from Adobe. <P> 
 */
public 
class AfterFXEditor
  extends BaseEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public
  AfterFXEditor()
  {
    super("AfterFX", new VersionID("2.4.23"), "Temerity",
	  "The Adobe After Effects image editor.", 
	  "AfterFX");
    
    underDevelopment();

    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * @param author
   *   The name of the user owning the files. 
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  @Override
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    File script = createTemp("jsx");
    
    String csv = env.get("ADOBE_CS_VERSION");
    if(csv == null) 
      csv = "2";
    
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      String currentWorking = env.get("WORKING").replaceAll("\\\\", "/");
      
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
      
      out.write
        ("app.exitAfterLaunchAndEval = false;\n" +
         "var f = new File(\"" + CommonActionUtils.escPath(fseq.getPath(0)) + "\");\n" +
         "try {" +
         "  app.open(f);\n" +
         "}\n" +
         "catch(err) {}\n\n" );
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
         "     var endName = \"\";\n");
      out.write(
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
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary JSX script file (" + script + ") to launch the " + 
         "AfterFX Editor!\n" +
         ex.getMessage());
    }
    
    if(PackageInfo.sOsType == OsType.MacOS) {
      if(csv.equals("2"))
        throw new PipelineException
          ("The AfterFX Editor plugins does not support MacOS before Adobe CS3!");
      
      File tempFile = createTemp("oas");
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
        
        writeAfterFXAppleScriptLauncher(out, script, csv);  
          
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
      
      return new SubProcessLight(author, getName(), "osascript", args, env, dir);
    } 
    else {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-r");
      args.add(script.getAbsolutePath());
      
      return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    }
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
   * This implementation always throws a PipelineException, to insure that the {@link #prep
   * prep} method is used for this Editor instead of this deprecated method.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   */  
  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  )   
    throws PipelineException
  {
    throw new PipelineException
      ("This launch() method should never be called since the prep() method returns " + 
       "a non-null SubProcessLight instance!");
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4372255137399329664L;
}

// $Id: AfterFXEditor.java,v 1.2 2008/09/14 22:14:42 jim Exp $

package us.temerity.pipeline.plugin.AfterFXEditor.v2_4_1;

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
    super("AfterFX", new VersionID("2.4.1"), "Temerity",
	  "The Adobe After Effects image editor.", 
	  "AfterFX");

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
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      String workingStart = PackageInfo.sWorkPath.toOsString().replaceAll("\\\\", "/");
      String currentWorking = env.get("WORKING").replaceAll("\\\\", "/");
      
      out.write
        ("app.exitAfterLaunchAndEval = false;\n" +
         "var f = new File(\"" + CommonActionUtils.escPath(fseq.getPath(0)) + "\");\n" +
         "app.open(f);\n" + 

         "var workingStart = \"" + workingStart + "/\";\n" +
         "var currentWorking = \"" + currentWorking + "/\";\n" +
         "var proj = app.project;\n" +
         "var list = proj.items;\n" +
         "var regExp = new RegExp(workingStart, \"g\");\n" + 
         "for (j=1; j <= list.length; j++)\n" + 
         "{\n" + 
         "  var item = list[j];\n" +
         "  if (item instanceof FootageItem)\n" + 
         "  {\n" + 
         "     var file = item.file;\n" + 
         "     if (regExp.test(file))\n" + 
         "     {\n" + 
         "	var fileName = file.fullName;\n" + 
         "	var endName = fileName.replace(workingStart, \"\");\n" + 
         "	var split = endName.split(\"/\");\n" + 
         "	var newEnd = \"\";\n" + 
         "	for (i=2; i < split.length; i++)\n" + 
         "	{\n" + 
         "	  newEnd += split[i];\n" + 
         "	  if (i != split.length -1)\n" + 
         "	    newEnd += \"/\";\n" + 
         "	}\n" + 
         "	var newFileName = currentWorking + newEnd;\n" + 
         "	var newFile = new File(newFileName);\n" + 
         "	item.replaceWithSequence(newFile, false);\n" + 
         "    }\n" + 
         "  }\n" + 
         "}");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary JSX script file (" + script + ") to launch the " + 
         "AfterFX Editor!\n" +
         ex.getMessage());
    }
    
    
    String csv = env.get("ADOBE_CS_VERSION");
    if(csv == null) 
      csv = "2";

    if(PackageInfo.sOsType == OsType.MacOS) {
      if(csv.equals("2"))
        throw new PipelineException
          ("The AfterFX Editor plugins does not support MacOS before Adobe CS3!"); 

      ArrayList<String> args = new ArrayList<String>();
      args.add("-e");
      args.add("tell application \"Adobe After Effects CS" + csv + "\"");
      
      String macpath = script.getAbsolutePath().substring(1).replace("/",":");
      args.add("-e");
      args.add("DoScriptFile \"" + macpath + "\"");
      
      args.add("-e");
      args.add("end tell");
      
      return new SubProcessLight(author, getName(), "osascript", args, env, dir);
    } 
    else {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : fseq.getFiles()) 
        args.add(file.getPath());
      
      return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    }
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
  
  private static final long serialVersionUID = -3450720928818905276L;

}

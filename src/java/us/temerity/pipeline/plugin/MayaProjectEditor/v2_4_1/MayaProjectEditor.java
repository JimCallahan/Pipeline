// $Id: MayaProjectEditor.java,v 1.1 2008/05/21 01:48:05 jim Exp $

package us.temerity.pipeline.plugin.MayaProjectEditor.v2_4_1;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P R O J E C T   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering application from Autodesk.<P> 
 * 
 * This Editor also sets the Maya project to the current working directory so that Maya's 
 * File->Save/SaveAs dialog starts in the directory containing the node files.
 */
public 
class MayaProjectEditor 
  extends BaseEditor 
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MayaProjectEditor() 
  {
    super("MayaProject", new VersionID("2.4.1"), "Temerity",
	  "3D modeling, animation and rendering application from Autodesk. " + 
          "Sets the Maya project to the node working directory before opening the scene.", 
          "maya");
	
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * This implementation returns <CODE>null</CODE> to fix the bug
   * <A href="http://temerity.us/community/forums/viewtopic.php?t=933"><B>Inherited Editor 
   * Prep Method</B></A>.
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
    Map<String, String> nenv = new TreeMap<String, String>(env);
    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    if(midefs != null) {
      Path dpath = new Path(new Path(dir), midefs);
      nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
    }
    nenv.put("MAYA_CUSTOM_PROJECT_PATH", fseq.getPath(0).getParent());

    String program = getProgram(); 
    ArrayList<String> args = new ArrayList<String>();
    if(PackageInfo.sOsType == OsType.MacOS) {
      program = "osascript";

      String app = null;
      {
        String pstr = env.get("PATH");
        if(pstr == null) 
          throw new PipelineException
            ("Somehow the PATH environmental variable was not defined by the Toolset!");

        ExecPath ep = new ExecPath(pstr);
        File mfile = ep.which("maya");
        if(mfile == null) 
          throw new PipelineException 
            ("Unable to find the \"maya\" binary in any directory in the PATH!"); 

        String mstr = mfile.toString();
        String ending = "/Contents/bin/maya";
        if(!mstr.endsWith(ending)) 
          throw new PipelineException
            ("Did not find the \"maya\" binary in the expected location inside a " + 
             "\"Maya.app/Contents/bin\" directory!");

        app = mstr.substring(0, mstr.length() - ending.length());
      }

      Path spath = fseq.getPath(0);
      String macpath = spath.toOsString().substring(1).replace("/",":");

      args.add("-e");
      args.add("tell application \"" + app + "\"");
			      
      args.add("-e");
      args.add("open file \"" + macpath + "\"");
        
      args.add("-e");
      args.add("execute \"setProject(\\\"" + spath.getParent() + "\\\");\"");
      
      args.add("-e");
      args.add("end tell");
    }
    else if(PackageInfo.sOsType == OsType.Windows) {
      args.add("-command");
      args.add("setProject(\\\"" + fseq.getPath(0).getParent() + "\\\")");
      args.add("-file");
      args.add("\"" + fseq.getPath(0) + "\"");
    } 
    else {
      args.add("-command");
      args.add("setProject(\"" + fseq.getPath(0).getParent() + "\")");
      args.add("-file");
      args.add(fseq.getPath(0).toOsString());
    }
    
    return new SubProcessLight(author, getName(), program, args, nenv, dir);
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
  
  private static final long serialVersionUID = 4783414032200728532L;

}

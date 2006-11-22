// $Id: MayaEditor.java,v 1.3 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.plugin.v1_1_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * The 3D modeling, animation and rendering program from Alias|Wavefront.             
 */
public
class MayaEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaEditor()
  {
    super("Maya", new VersionID("1.1.0"), "Temerity", 
	  "3D modeling and animation software from Alias|Wavefront.", 
	  "maya");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
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
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  )   
    throws PipelineException
  {
    FrameRange range = fseq.getFrameRange(); 
    if((range != null) && (!range.isSingle()))
      throw new PipelineException
	("The " + getName() + " Editor can only edit a single scene at a time!");

    ArrayList<String> args = new ArrayList<String>();
    SubProcessLight proc = null;
    if(PackageInfo.sOsType == OsType.Unix) {
      args.add(fseq.getFile(0).getPath());
      
      proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
      proc.start();
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      args.add("-e");
      {
	ExecPath epath = new ExecPath(env.get("PATH"));
	File mpath = epath.which("maya");
	if((mpath == null) || !mpath.getPath().endsWith("/Contents/bin/maya")) {
	  StringBuilder buf = new StringBuilder();
	  buf.append("Could not find the Maya binary in any of the directories which " + 
		     "make up the PATH:\n");
	  for(File edir : epath.getDirectories()) 
	    buf.append("  " + edir + "\n");
	  
	  throw new PipelineException(buf.toString());
	}
	
	String maya = mpath.getPath();
	args.add("tell application \"" + 
		 "Macintosh HD" + maya.substring(0, maya.length()-18).replace("/",":") + 
		 "\"");
      }

      String macpath = fseq.getFile(0).getPath().substring(1).replace("/",":");
      args.add("-e");
      args.add("open file \"" + macpath + "\"");

      for(String key : env.keySet()) {
	args.add("-e"); 
	args.add("execute \"putenv \\\"" + key + "\\\" \\\"" + env.get(key) + "\\\"\"");
      }
      
      args.add("-e");
      args.add("end tell");
      
      proc = new SubProcessLight(getName(), "osascript", args, env, dir);
      proc.start();
    }
    else {
      throw new PipelineException
	("The Maya Editor is not yet supported on the Windows operating system.");
    }
     
    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4813721477507193337L;

}



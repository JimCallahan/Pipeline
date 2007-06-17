// $Id: TerminalEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.TerminalEditor.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E R M I N A L   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A UNIX terminal in the node's working directory with the current toolset environment.<P> 
 * 
 * When run on a Unix system, this Editor will attempt to use konsole(1), gnome-terminal(1) or
 * xterm(1) if found in the Toolset's PATH in that order.<P> 
 * 
 * On Mac OS X systems, the default Terminal program will be used via AppleScript.
 */
public
class TerminalEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TerminalEditor()
  {
    super("Terminal", new VersionID("2.2.1"), "Temerity",
	  "A UNIX terminal in the node's working directory with the current " + 
	  "toolset environment.", 
	  "Terminal");

    addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * The default implementation executes the editor program obtained with {@link #getProgram 
   * getProgram} method under the given environment.  Subclasses should override this method 
   * if more specialized behavior or different command line arguments are needed in order to 
   * launch the editor for the given file sequence. <P> 
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
    /* create temporary shell script */ 
    File script = null;
    try {
      Path spath = new Path(PackageInfo.sTempPath, "pipeline");
      File sdir = spath.toFile();
      sdir.mkdir();
      
      script = File.createTempFile("TerminalEditor-", ".bash", sdir);
      FileCleaner.add(script);
      
      FileWriter out = new FileWriter(script);

      if(PackageInfo.sOsType == OsType.Unix) {    
	if(!env.containsKey("XAUTHORITY")) {
	  String xauth = System.getenv("XAUTHORITY");
	  if((xauth != null) && (xauth.length() > 0))
	    env.put("XAUTHORITY", xauth);
	}
	  
	if(!env.containsKey("DISPLAY")) {
	  String display = System.getenv("DISPLAY");
	  if((display != null) && (display.length() > 0))
	    env.put("DISPLAY", display);
	}
      }
      
      for(String key : env.keySet()) 
	out.write("export " + key + "='" + env.get(key) + "'\n");	  
      
      out.write("export PS1=\"\\n\\u@\\h \\w\\n$ \"\n");
      out.write("cd " + dir + "\n");
      out.write("clear");
      
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to create the temporary script (" + script + ") for the Terminal " + 
	 "editor plugin!");
    }
    
    ArrayList<String> args = new ArrayList<String>();
    if(PackageInfo.sOsType == OsType.Unix) {    
      String path = env.get("PATH");
      if(path == null) 
	throw new PipelineException("No PATH was defined in the toolset!");
      ExecPath exec = new ExecPath(path);

      File envFile = exec.which("env");
      if(envFile == null) 
	throw new PipelineException("Unable to find the env(1) program!");
      
      File bashFile = exec.which("bash");
      if(bashFile == null) 
	throw new PipelineException("Unable to find the bash(1) program!");

      String terminal = null;
      if(exec.which("konsole") != null) {
	terminal = "konsole";
	args.add("-e");
      }
      else if(exec.which("gnome-terminal") != null) {
	terminal = "gnome-terminal";
	args.add("-x");
      }
      else if(exec.which("xterm") != null) {
	terminal = "xterm";
	args.add("-e");
      }
      else {
	throw new PipelineException
	  ("Unable to find any suitable X11 terminal program in the PATH!.\n\n" + 
	   "Search included:\n" + 
	   "  konsole\n" + 
	   "  gnome-terminal\n" + 
	   "  xterm");
      }

      args.add(envFile.toString());
      args.add("-i");
      args.add(bashFile.toString());
      args.add("--init-file");
      args.add(script.toString()); 
      
      return new SubProcessLight(author, getName(), terminal, args, env, dir);
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      args.add("-e");
      args.add("tell application \"Terminal\"");

      args.add("-e");
      args.add("do script \"/usr/bin/env -i /bin/bash --init-file " + script + "\""); 

      args.add("-e");
      args.add("end tell");

      return new SubProcessLight(author, getName(), "osascript", args, env, dir);
    }
    else {
      throw new PipelineException
	("The Terminal Editor is not yet supported on the Windows operating system.");
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
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
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
      ("This method should never be called since the prep() method does not return (null)!");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2407225813099656416L;

}



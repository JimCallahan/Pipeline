// $Id: WebBrowserEditor.java,v 1.2 2007/09/06 03:57:18 jim Exp $

package us.temerity.pipeline.plugin.WebBrowserEditor.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   W E B   B R O W S E R   E D I T O R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Opens a file using the local web browser. <P> 
 * 
 * On Unix and Windows, the file will be shown in a new tab if a firefox(1) browser is 
 * already running.  Otherwise, a new browser will be started to display the file.  On Mac 
 * OS X, either Safari or Preview will be used depending of the type of file being browsed.
 */
public
class WebBrowserEditor
  extends SimpleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  WebBrowserEditor()
  {
    super("WebBrowser", new VersionID("2.2.1"), "Temerity", 
	  "Opens a file using the local web browser.", 
	  "firefox");

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
    if(!fseq.isSingle())
      throw new PipelineException
        ("The " + getName() + " Editor can only display a single file at a time!");

    String url = null;
    if(PackageInfo.sOsType == OsType.Windows) 
      url = "file:///" + fseq.getPath(0); 
    else 
      url = "file://" + fseq.getPath(0); 

    String program = getProgram();
    ArrayList<String> args = new ArrayList<String>();

    if(PackageInfo.sOsType == OsType.MacOS) {
      program = "osascript";
      args.add("-e");
      args.add("open location \"" + url + "\"");
    }
    else {
      ExecPath epath = new ExecPath(env.get("PATH"));
      if(epath.which(program) != null) {
        if(isBrowserRunning(program, env)) 
          args.add("-new-tab"); 
        args.add(url);
      }
      else {
        throw new PipelineException 
          ("Unable to find firefox(1) in the Toolset PATH!"); 
      }
    }

    return new SubProcessLight(author, getName(), program, args, env, dir); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns whether a web browser is currently running.
   * 
   * @param browser
   *   The browser program name.
   * 
   * @param env
   *   The shell environment.
   * 
   * @throws PipelineException
   *   If unable to determine whether the browser is running.
   */    
  private boolean
  isBrowserRunning
  (
   String browser, 
   Map<String,String> env
  )
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    args.add("-remote");
    args.add("ping()");

    SubProcessLight proc = 
      new SubProcessLight("CheckBrowser", browser, args, env, PackageInfo.sTempPath.toFile());
    try {
      proc.start();
      proc.join();
    }
    catch(InterruptedException ex) {
    }
    
    return proc.wasSuccessful();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4421733422946806720L;

}



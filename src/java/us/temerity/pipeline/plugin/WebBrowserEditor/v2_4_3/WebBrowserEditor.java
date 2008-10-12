// $Id: WebBrowserEditor.java,v 1.1 2008/10/12 07:21:05 jim Exp $

package us.temerity.pipeline.plugin.WebBrowserEditor.v2_4_3;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   W E B   B R O W S E R   E D I T O R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Opens a file using the local web browser. <P> 
 * 
 * Uses the built-in desktop feature of Java 6 to launch the appropriate native browser for
 * the current operating system and user preferences.
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
    super("WebBrowser", new VersionID("2.4.3"), "Temerity", 
	  "Opens a file using the local web browser.", 
	  "plbrowse");

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

    ArrayList<String> args = new ArrayList<String>();
    if(PackageInfo.sOsType == OsType.Windows) 
      args.add("--url=file:///" + fseq.getPath(0));
    else 
      args.add("--url=file://" + fseq.getPath(0));

    TreeMap nenv = new TreeMap<String,String>(env);
    {
      Path ipath = 
        new Path(PackageInfo.sInstPath, 
                 "/" + PackageInfo.sOsType + "-" + PackageInfo.sArchType + "-Opt/bin");
      nenv.put("PATH", ipath.toOsString() + PackageInfo.getPathSep() + env.get("PATH"));
    }

    return new SubProcessLight(author, getName(), getProgram(), args, nenv, dir); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3202515786739784L;

}



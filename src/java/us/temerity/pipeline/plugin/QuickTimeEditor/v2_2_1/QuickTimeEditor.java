// $Id: QuickTimeEditor.java,v 1.3 2008/03/10 05:55:48 jim Exp $

package us.temerity.pipeline.plugin.QuickTimeEditor.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U I C K   T I M E   E D I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Mac OS X movie player.
 */
public
class QuickTimeEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  QuickTimeEditor()
  {
    super("QuickTime", new VersionID("2.2.1"), "Temerity", 
	  "The Mac OS X movie player.", 
	  "QuickTime Player");

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
    if(PackageInfo.sOsType == OsType.MacOS) 
      return super.prep(author, fseq, env, dir);

    /* get Windows format movie name */ 
    String movie = null;
    {
      FrameRange range = fseq.getFrameRange(); 
      if((range != null) && (!range.isSingle()))
        throw new PipelineException
          ("The " + getName() + " Editor can only view a single movie at a time!");

      movie = fseq.getPath(0).toString().replace("/", "\\\\");      
    }

    /* create a temporary JavaScript */ 
    File jscript = null;
    try {
      jscript = File.createTempFile("QuickTimeEditor", ".js", PackageInfo.sTempPath.toFile());
      FileCleaner.add(jscript);

      FileWriter out = new FileWriter(jscript);

      out.write
        ("var app = WScript.CreateObject(\"QuickTimePlayerLib.QuickTimePlayerApp\");\n" +
         "WScript.Sleep(2000);\n" +  // give Qt a chance to start if not already running
         "if(app != null) {\n" + 
         "  var player = app.Players(1);\n" + 
         "  if(player) {\n" + 
         "    player.OpenURL(\"" + movie + "\");\n" + 
         "    player.QTControl.Movie.Play();\n" + 
         "  }\n" + 
         "}\n");

      out.close();
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to create temporary JavaScript (" + jscript + ") used by the QuickTime " + 
         "Editor plugin!"); 
    }
    
    /* run the JavaScript */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("//nologo");
      args.add("//B");
      args.add("//E:jscript");
      args.add(jscript.toString());
      
      return new SubProcessLight(author, getName(), "cscript.exe", args, env, dir);
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

  private static final long serialVersionUID = -3808554430300173354L;

}



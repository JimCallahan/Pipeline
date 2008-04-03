// $Id: QtEncodeAction.java,v 1.3 2008/04/03 10:30:47 jim Exp $

package com.intelligentcreatures.pipeline.plugin.QtEncodeAction.v1_0_0; 

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.regex.*; 
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q T   E N C O D E   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * 
 * Due to the way the QuickTime application on Mac OS X works, this action need to use the
 * sudo(1) command to run Apple Script commands controlling the QuickTime player.  The user
 * logged into the Mac should be the "pipeline" administrative user.  This action will run 
 * as that user so it has access to the display.  That means you will need a line like this
 * in your /etc/sudoers file:
 * 
 * <DIV style="margin-left: 40px;">
 *   %users  ALL=(pipeline) NOPASSWD: /usr/bin/osascript
 * </DIV> <BR>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Source Movie <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the QuickTime movie to encode. 
 *   </DIV> <BR>
 * 
 *   Codec Settings <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the QuickTime codec export settings file.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class QtEncodeAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  QtEncodeAction() 
  {
    super("QtEncode", new VersionID("1.0.0"), "ICVFX", 
          ""); 
    
    {
      ActionParam param = 
        new LinkActionParam
        (aSourceMovie,
         "The source node which contains the QuickTime movie to encode.", 
         null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
        new LinkActionParam
        (aCodecSettings,
         "The source node which contains the QuickTime codec export settings file.", 
         null);
      addSingleParam(param);
    } 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aSourceMovie); 
      layout.addEntry(aCodecSettings); 

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    ArrayList<String> qtSuffixes = new ArrayList<String>();
    qtSuffixes.add("qt");
    qtSuffixes.add("mov");

    /* target QuickTime movie */
    Path targetPath = getPrimaryTargetPath(agenda, qtSuffixes, "QuickTime Movie");

    /* the source QuickTime movie */
    Path sourcePath = 
      getPrimarySourcePath(aSourceMovie, agenda, qtSuffixes, "QuickTime Movie");
    if(sourcePath == null) 
      throw new PipelineException
	("The " + aSourceMovie + " node was not specified!");

    /* the codec export settings */
    Path settingsPath = 
      getPrimarySourcePath(aCodecSettings, agenda, "settings", "QuickTime Codec Settings");
    if(settingsPath == null) 
      throw new PipelineException
	("The " + aCodecSettings + " node was not specified!");

    /* create a temporary output movie file */  
    Path moviePath = new Path(createTemp(agenda, "qt"));
    String movie = moviePath.toString().replaceAll("/",":");

    /* create a temporary apple script to run the QuickTime application */  
    Path appleScript = new Path(createTemp(agenda, "scpt"));
    try {
      FileWriter out = new FileWriter(appleScript.toFile()); 
      
      out.write
	("on run\n" + 
	 "  set infile to ((POSIX file \"" + sourcePath + "\") as alias)\n" + 
	 "  set codecSettings to ((POSIX file \"" + settingsPath + "\") as alias)\n" + 
	 "\n" + 
	 "  try\n" + 
	 "    tell application \"QuickTime Player\"\n" + 
	 "      activate\n" + 
	 "      close every window\n" + 
	 "    end tell\n" + 
	 "  end try\n" + 
	 "\n" + 
	 "  tell application \"QuickTime Player\"\n" + 
	 "    try\n" + 
	 "      open infile\n" + 
	 "      if (can export front document as QuickTime movie) then\n" + 
	 "        with timeout of 600 seconds -- 10 minutes\n" + 
	 "          export front document to \"" + movie + "\" " + 
	 "as Quicktime movie using settings codecSettings\n" + 
	 "        end timeout\n" + 
	 "      end if\n" + 
	 "    end try\n" + 
	 "  end tell\n" + 
	 "\n" + 
	 "  tell application \"QuickTime Player\"\n" + 
	 "    quit\n" + 
	 "  end tell\n" + 
	 "end run\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Apple Script file (" + appleScript + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    /* create a temporary script to run everything */  
    File script = createTemp(agenda, "bash");
    try {
      FileWriter out = new FileWriter(script); 
      
      out.write
	("rm -f " + moviePath + "\n" +
	 "sudo -u " + PackageInfo.sPipelineUser + 
	          " /usr/bin/osascript " + appleScript + "\n" + 
	 "cp " + moviePath + " " + targetPath + "\n" + 
	 "rm -f " + moviePath + "\n");
    
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createScriptSubProcess(agenda, script, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 213653165192978818L;

  public static final String aSourceMovie   = "SourceMovie"; 
  public static final String aCodecSettings = "CodecSettings"; 

}


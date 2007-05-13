// $Id: NukeActionUtils.java,v 1.1 2007/05/13 10:25:10 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.io.*;


/*------------------------------------------------------------------------------------------*/
/*   N U K E   A C T I O N   U T I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of node Action plugins related to Nuke compositing. <P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which manipulate Nuke scripts and run the Nuke compositor. 
 */
public 
class NukeActionUtils
  extends PythonActionUtils
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
  NukeActionUtils
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
  /*   P R O G R A M   L O O K U P                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert a Pipeline file sequence into a Nuke file pattern string.
   */ 
  public static final String
  toNukeSeq
  (
   FileSeq fseq
  ) 
  {
    StringBuilder buf = new StringBuilder();

    FilePattern fpat = fseq.getFilePattern();
    buf.append(fpat.getPrefix()); 

    if(fpat.hasFrameNumbers()) {
      buf.append(".%"); 
      if(fpat.getPadding() > 1) 
        buf.append("0" + fpat.getPadding());
      buf.append("d");
    }

    buf.append("." + fpat.getSuffix());
    
    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R O G R A M   L O O K U P                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the name of the Nuke compositor based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable NUKE_BINARY is defined, its value will be used as the 
   * name of the renderer executable instead of the "Nuke4.6" (Unix/MacOS) or "Nuke4.6.exe" 
   * (Windows).  On Windows, the renderer name should include the ".exe" extension.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   */ 
  public static String
  getNukeProgram
  (
   ActionAgenda agenda   
  ) 
    throws PipelineException
  {
    return getNukeProgram(agenda.getEnvironment()); 
  }

  /**
   * Generate the name of the Nuke compositor based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable NUKE_BINARY is defined, its value will be used as the 
   * name of the renderer executable instead of the "Nuke4.6" (Unix/MacOS) or "Nuke4.6.exe" 
   * (Windows).  On Windows, the renderer name should include the ".exe" extension.
   * 
   * @param env  
   *   The environment used to lookup NUKE_BINARY.
   */
  public static String
  getNukeProgram
  (
   Map<String,String> env
  ) 
    throws PipelineException
  {
    String nuke = env.get("NUKE_BINARY");
    if((nuke != null) && (nuke.length() > 0)) 
      return nuke;

    if(PackageInfo.sOsType == OsType.Windows) 
      return "Nuke4.6.exe";

    return "Nuke4.6";
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4490194607844089876L;
  
}




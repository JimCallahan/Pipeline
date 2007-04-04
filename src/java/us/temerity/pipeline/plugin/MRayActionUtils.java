// $Id: MRayActionUtils.java,v 1.1 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.io.*;


/*------------------------------------------------------------------------------------------*/
/*   M R A Y   A C T I O N   U T I L S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of node Action plugins related to Mental Ray rendering.<P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which manipulate MI files and run the Mental Ray renderer. 
 */
public 
class MRayActionUtils
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
  MRayActionUtils
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
   * Generate the name of the Mental Ray renderer based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable MRAY_BINARY is defined, its value will be used as the 
   * name of the renderer executable instead of the "ray" (Unix/MacOS) or "ray345.exe" 
   * (Windows).  On Windows, the renderer name should include the ".exe" extension.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   */ 
  public static String
  getMRayProgram
  (
   ActionAgenda agenda   
  ) 
    throws PipelineException
  {
    return getMRayProgram(agenda.getEnvironment()); 
  }

  /**
   * Generate the name of the Mental Ray renderer based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable MRAY_BINARY is defined, its value will be used as the 
   * name of the renderer executable instead of the "ray" (Unix/MacOS) or "ray345.exe" 
   * (Windows).  On Windows, the renderer name should include the ".exe" extension.
   * 
   * @param env  
   *   The environment used to lookup MRAY_BINARY.
   */
  public static String
  getMRayProgram
  (
   Map<String,String> env
  ) 
    throws PipelineException
  {
    String ray = env.get("RAY_BINARY");
    if((ray != null) && (ray.length() > 0)) 
      return ray; 
    
    if(PackageInfo.sOsType == OsType.Windows) 
      return "ray345.exe";

    return "ray";
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3026645578638542024L;
  
}




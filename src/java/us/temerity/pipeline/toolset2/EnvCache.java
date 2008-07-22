// $Id: EnvCache.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/**
 * An environment used to execute a process.
 */
public class 
EnvCache
  implements Serializable, Glueable
{
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  EnvCache()
  {}
  
  /**
   * Default Constructor
   * 
   * @param env
   *   The environment this cache represents.
   */
  public
  EnvCache
  (
    DoubleMap<MachineType, String, String>  env
  )
  {
    pEnvironment = new DoubleMap<MachineType, String, String>(env);
  }

  /**
   * Copy Constructor.
   * 
   * @param cache
   *   The cache to copy.
   */
  public
  EnvCache
  (
    EnvCache cache
  )
  {
   pEnvironment = new DoubleMap<MachineType, String, String>(cache.pEnvironment); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the cooked toolset environment.
   * 
   * @param machine
   *   The machine type the returned environment is for.
   *   
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
    MachineType machine  
  )
  {
    if(machine == null) 
      throw new IllegalArgumentException("The machine cannot be (null)!");
    
    TreeMap<String,String> toReturn = pEnvironment.get(machine);
    if(pEnvironment != null) 
      return new TreeMap<String,String>(toReturn);
    else 
      throw new IllegalArgumentException
        ("There is no valid environment for the machine type (" + machine.toString() + ")");
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user and 
   * operating system.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param machine
   *   The machine type the returned environment is for.
   *   
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   MachineType machine
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    TreeMap<String,String> env = getEnvironment(machine);

    OsType os = machine.getOsType();
    
    switch(os) {
    case Unix:
    case MacOS:
      {
        env.put("USER", author);

        Path home = new Path(PackageInfo.getHomePath(os), author);        
        env.put("HOME", home.toOsString(os)); 
      }
      break;

    case Windows:
      {
        env.put("USERNAME", author);        

        Path profile = PackageInfo.getUserProfilePath(author, os);
        if(profile != null) 
          env.put("USERPROFILE", profile.toOsString(os));

        Path appdata = PackageInfo.getAppDataPath(author, os);
        if(appdata != null) 
          env.put("APPDATA", appdata.toOsString(os));
      }
    }

    env.put("PIPELINE_OSTYPE", os.toString());
    env.put("PIPELINE_ARCHTYPE", machine.getArchType().toString());

    return env;
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user, working area and
   * operating system.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param machine
   *   The machine type the returned environment is for.
   *   
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   String view, 
   MachineType machine
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    TreeMap<String,String> env = getEnvironment(author, machine);
    
    OsType os = machine.getOsType();

    Path working = new Path(PackageInfo.getWorkPath(os), author + "/" + view);
    env.put("WORKING", working.toOsString(os));

    return env;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the set of {@link MachineType MachineTypes} supported by this cache.
   */
  public Set<MachineType>
  getSupportedMachineTypes()
  {
    return Collections.unmodifiableSet(pEnvironment.keySet());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Environment", pEnvironment);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    DoubleMap<MachineType, String,String> env = 
      (DoubleMap<MachineType, String, String>) decoder.decode("Environment");
    if(env == null) 
      throw new GlueException("The \"Environment\" was missing or (null)!");
    pEnvironment= env;
  }
    

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1145094881560586784L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cooked toolset environment for the different operating systems and architectures 
   * supported.
   */ 
  private DoubleMap<MachineType, String,String>  pEnvironment;
}

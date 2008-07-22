// $Id: ToolsetVersion.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T    V E R S I O N                                                         */
/*------------------------------------------------------------------------------------------*/

/**
 *  A read-only version of a toolset.
 */
public 
class ToolsetVersion
  extends ToolsetCommon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  ToolsetVersion()
  {}

  
  /**
   * Constructor used to create a toolset composed of the given packages.
   * 
   * @param author
   *   The name of the user creating the toolset.
   * 
   * @param desc
   *   The package description.
   *   
   * @param mod
   *   The modifiable toolset that this toolset version is being created from.
   *   
   * @param cache
   *   The collection of environment variables that this toolset represents.
   *   
   * @param vid
   *   The version of the toolset.
   */ 
  public
  ToolsetVersion
  (
    String author,
    String desc,
    ToolsetMod mod,
    EnvCache cache,
    VersionID vid
  )
  {
    super(mod.getName(), mod.getPackages(), cache.getSupportedMachineTypes());
    
    if (!mod.isFreezable())
      throw new IllegalStateException
        ("Invalid attempt to make a ToolsetVersion from a non-freezable ToolsetMod");
    
    pEnvCache = new EnvCache(cache);
    pVersionID = vid;
    pMessage = new SimpleLogMessage(author, desc);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * toolset was created.
   */ 
  public Long
  getTimeStamp() 
  {
    if(pMessage != null) 
      return pMessage.getTimeStamp();
    return null;
  }

  /**
   * Get the name of the user who created the toolset.
   */ 
  public String
  getAuthor() 
  {
    if(pMessage != null) 
      return pMessage.getAuthor();
    return null;
  }

  /**
   * Get the toolset description.
   */ 
  public String
  getDescription() 
  {
    if(pMessage != null) 
      return pMessage.getMessage();
    return null;
  }
  
  @Override
  public Set<MachineType>
  getActualMachineTypes()
  {
    return pEnvCache.getSupportedMachineTypes();
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the cooked toolset environment specialized for a specific machine type.
   * 
   * @param machine
   *   The machine type.
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
    return pEnvCache.getEnvironment(machine); 
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user and machine type..
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param machine
   *   The machine type.
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
    return pEnvCache.getEnvironment(author, machine); 
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user, working area, and 
   * machine type.
   * 
   * @param author
   *   The user owning the generated environment.
   *   
   * @param view
   *   The name of the user's working area view. 
   * 
   * @param machine
   *   The machine type.
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
    return pEnvCache.getEnvironment(author, view, machine); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A B S T R A C T   M E T H O D S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public boolean 
  isFrozen()
  {
    return true;
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
    super.toGlue(encoder);
    
    encoder.encode("Message",  pMessage);
    encoder.encode("VersionID",  pVersionID);
    encoder.encode("EnvCache",  pEnvCache);
  }
  
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    SimpleLogMessage msg = (SimpleLogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg; 
    
    EnvCache cache = (EnvCache) decoder.decode("EnvCache");
    if(cache == null) 
      throw new GlueException("The \"EnvCache\" was missing!");
    pEnvCache = cache;
    
    VersionID id = (VersionID) decoder.decode("VersionID");
    if (id == null)
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = id;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 744945991589864162L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The descriptive message given at the time the toolset was created. <P> 
   * 
   * The timestamp and author of the message are also the timestamp and author of the 
   * toolset. <P> 
   */
  private SimpleLogMessage  pMessage;
  
  /**
   * The cached environment for the toolset
   */
  private EnvCache pEnvCache;
  
  /**
   * The version of the toolset.
   */
  private VersionID pVersionID;
}

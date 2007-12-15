// $Id: BaseKey.java,v 1.1 2007/12/15 07:24:58 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*  B A S E   K E Y                                                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * The parent class for all keyed values used in the queue.
 * <p>
 * Keys can be as simple as a name and a description.  However, they also support a plugin
 * of the {@link BaseKeyChooser} which can be used to specify circumstances in which they
 * should be on or off.
 * 
 * @see HardwareKey
 * @see SelectionKey
 * @see LicenseKey
 */
public class BaseKey
  extends Described
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
  BaseKey() 
  {
    super();
    pKeyChooser = null;
  }

  /** 
   * Construct a new key, that does not use a {@link BaseKeyChooser} to
   * determine when it is on.
   * 
   * @param name 
   *   The name of the key.
   * 
   * @param desc 
   *   A short description of the key.
   */ 
  public
  BaseKey
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc);
    pKeyChooser = null;
  }
  
  /** 
   * Construct a new  key that uses a {@link BaseKeyChooser} to determine when
   * it is on.
   * 
   * @param name 
   *   The name of the key.
   * 
   * @param desc 
   *   A short description of the key.
   *   
   * @param plugin
   *   The plugin that will be used to determine when this key is on.
   */ 
  public
  BaseKey
  (
   String name,  
   String desc,
   BaseKeyChooser plugin
  ) 
  {
    super(name, desc);
    pKeyChooser = plugin;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this key contain a plugin that is used to determine when it is on?
   */
  public boolean
  hasPlugin()
  {
    return (pKeyChooser != null);
  }
  
  /**
   * Get the plugin that is used to determine when the key is on.
   * 
   * @return The {@link BaseKeyChooser} that is used to determine when the key is on
   * or <code>null</code> if there is no plugin associated with this key.
   */
  public BaseKeyChooser
  getPlugin()
  {
    return pKeyChooser;
  }
  
  /**
   * Set the plugin that is used to determine when the key is on.
   * 
   * @param keyChooser 
   *   The plugin or <code>null</code> to remove the existing plugin.
   */
  public void 
  setPlugin
  (
    BaseKeyChooser keyChooser
  )
  {
    pKeyChooser = keyChooser;
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
    
    encoder.encode("Description", pDescription);
    if (pKeyChooser != null)
      encoder.encode("KeyChooser", pKeyChooser);
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

    String desc = (String) decoder.decode("Description"); 
    if(desc == null) 
      throw new GlueException("The \"Description\" was missing!");
    pDescription = desc;
    
    pKeyChooser = (BaseKeyChooser) decoder.decode("KeyChooser");
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7320826512019638319L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private BaseKeyChooser pKeyChooser;
}

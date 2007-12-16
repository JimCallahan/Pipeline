// $Id: BaseKey.java,v 1.2 2007/12/16 06:26:40 jesse Exp $

package us.temerity.pipeline;

import java.io.IOException;
import java.io.Serializable;

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
  implements Serializable, Glueable
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
   * @param keyChooser
   *   The plugin that will be used to determine when this key is on.
   */ 
  public
  BaseKey
  (
   String name,  
   String desc,
   BaseKeyChooser keyChooser
  ) 
  {
    super(name, desc);
    pKeyChooser = keyChooser;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this key contain a plugin that is used to determine when it is on?
   */
  public boolean
  hasKeyChooser()
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
  getKeyChooser()
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
  setKeyChooser
  (
    BaseKeyChooser keyChooser
  )
  {
    pKeyChooser = keyChooser;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseKeyChooser instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    BaseKeyChooser plug = null;
    if(pKeyChooser != null) 
      plug = new BaseKeyChooser(pKeyChooser);
    out.writeObject(plug);
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  @SuppressWarnings("unchecked")
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    BaseKeyChooser plug = (BaseKeyChooser) in.readObject();
    if(plug != null) {
      try {
        PluginMgrClient client = PluginMgrClient.getInstance();
        pKeyChooser = client.newKeyChooser(plug.getName(), 
                                           plug.getVersionID(), 
                                           plug.getVendor());
        pKeyChooser.setParamValues(plug);
      }
      catch(PipelineException ex) {
        throw new IOException(ex.getMessage());
      }
    }
    else {
      pKeyChooser = null;
    }
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
    
    BaseKeyChooser key = (BaseKeyChooser) decoder.decode("KeyChooser");
    if(key != null) {
      try {
        PluginMgrClient client = PluginMgrClient.getInstance();
        pKeyChooser = client.newKeyChooser(key.getName(), 
                                           key.getVersionID(), 
                                           key.getVendor());
        pKeyChooser.setParamValues(key);
      }
      catch(PipelineException ex) {
        throw new GlueException(ex.getMessage());
      }
    }
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

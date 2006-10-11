// $Id: QueueExtensionConfig.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E X T E N S I O N   C O N F I G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A specific configuration of a Queue Manager server extension. <P> 
 */
public
class QueueExtensionConfig
  extends BaseExtensionConfig
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
  QueueExtensionConfig()
  {}
  
  /**
   * Construct a new queue extension configuration.
   * 
   * @param name 
   *   The name of this extension instance.
   * 
   * @param toolset 
   *   The named execution environment under which any spawned subprocess are run.
   * 
   * @param extension
   *   The plugin which implements the extension.
   * 
   * @param isEnabled
   *   Whether the extension is currently enabled.
   */ 
  public
  QueueExtensionConfig
  (
   String name, 
   String toolset, 
   BaseQueueExt extension, 
   boolean isEnabled
  ) 
  {
    super(name, toolset, isEnabled); 

    if(extension == null) 
      throw new IllegalArgumentException
	("The queue extension plugin cannot be (null)!");
    pExtension = (BaseQueueExt) extension.clone(); 
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Instantiate a new copy of the queue extension plugin. <P> 
   * 
   * Note that this method will always return a new instance which is completely independent
   * and therefore thread-safe.  This instance is also guaranteed to be up-to-date with the 
   * latest installed version of the plugin.
   * 
   * @return 
   *   The extension plugin instance.
   * 
   * @throws PipelineException
   *   If unable to instantiate the plugin.
   */
  public BaseQueueExt
  getQueueExt() 
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();

    BaseQueueExt ext = 
      client.newQueueExt(pExtension.getName(), 
			  pExtension.getVersionID(), 
			  pExtension.getVendor());
    ext.setParamValues(pExtension);
      
    return ext;
  }

  /** 
   * Instantiate a new copy of the extension plugin. <P> 
   * 
   * Note that this method will always return a new instance which is completely independent
   * and therefore thread-safe.  This instance is also guaranteed to be up-to-date with the 
   * latest installed version of the plugin.
   * 
   * @return 
   *   The extension plugin instance.
   * 
   * @throws PipelineException
   *   If unable to instantiate the plugin.
   */
  public BaseExt
  getExt() 
    throws PipelineException
  {
    return getQueueExt();
  }
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    BaseQueueExt ext = null; 
    if(pExtension != null) 
      ext = new BaseQueueExt(pExtension); 
    out.writeObject(ext);
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    BaseQueueExt ext = (BaseQueueExt) in.readObject();
    if(ext != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pExtension = client.newQueueExt(ext.getName(), ext.getVersionID(), ext.getVendor());
	pExtension.setParamValues(ext);
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pExtension = null;
    }
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("QueueExt", new BaseQueueExt(pExtension));
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    BaseQueueExt ext = (BaseQueueExt) decoder.decode("QueueExt");
    if(ext == null) 
      throw new GlueException("The \"QueueExt\" was missing or (null)!");
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pExtension = client.newQueueExt(ext.getName(), ext.getVersionID(), ext.getVendor());
      pExtension.setParamValues(ext);
    }
    catch(PipelineException ex) {
      throw new GlueException(ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5649218895958056020L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The queue extension plugin stored as an instance of BaseQueueExt. <P> 
   * 
   * The actual plugin subclass is instantiated on the fly by the {@link #getQueueExt} 
   * method since each instance may be running in parallel in its own Thread and so that 
   * the latest installed version of the plugin will always be used.
   */
  private BaseQueueExt  pExtension;       

}

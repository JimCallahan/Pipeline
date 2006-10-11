// $Id: MasterExtensionConfig.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   E X T E N S I O N   C O N F I G                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A specific configuration of a Master Manager server extension. <P> 
 */
public
class MasterExtensionConfig
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
  MasterExtensionConfig()
  {}
  
  /**
   * Construct a new master extension configuration.
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
  MasterExtensionConfig
  (
   String name, 
   String toolset, 
   BaseMasterExt extension, 
   boolean isEnabled
  ) 
  {
    super(name, toolset, isEnabled); 

    if(extension == null) 
      throw new IllegalArgumentException
	("The master extension plugin cannot be (null)!");
    pExtension = (BaseMasterExt) extension.clone(); 
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Instantiate a new copy of the master extension plugin. <P> 
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
  public BaseMasterExt
  getMasterExt() 
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();

    BaseMasterExt ext = 
      client.newMasterExt(pExtension.getName(), 
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
    return getMasterExt();
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
    BaseMasterExt ext = null; 
    if(pExtension != null) 
      ext = new BaseMasterExt(pExtension); 
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
    BaseMasterExt ext = (BaseMasterExt) in.readObject();
    if(ext != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pExtension = client.newMasterExt(ext.getName(), ext.getVersionID(), ext.getVendor());
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

    encoder.encode("MasterExt", new BaseMasterExt(pExtension));
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    BaseMasterExt ext = (BaseMasterExt) decoder.decode("MasterExt");
    if(ext == null) 
      throw new GlueException("The \"MasterExt\" was missing or (null)!");
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pExtension = client.newMasterExt(ext.getName(), ext.getVersionID(), ext.getVendor());
      pExtension.setParamValues(ext);
    }
    catch(PipelineException ex) {
      throw new GlueException(ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1968287627560968464L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The master extension plugin stored as an instance of BaseMasterExt. <P> 
   * 
   * The actual plugin subclass is instantiated on the fly by the {@link #getMasterExt} 
   * method since each instance may be running in parallel in its own Thread and so that 
   * the latest installed version of the plugin will always be used.
   */
  private BaseMasterExt  pExtension;       

}

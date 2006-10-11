// $Id: BaseExtensionConfig.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T E N S I O N   C O N F I G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A specific configuration of an server daemon extension. <P> 
 */
public abstract 
class BaseExtensionConfig
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected
  BaseExtensionConfig() 
  {} 

  /**
   * Construct a new job.
   * 
   * @param name 
   *   The name of this extension instance.
   * 
   * @param toolset 
   *   The named execution environment under which any spawned subprocess are run.
   * 
   * @param isEnabled
   *   Whether the extension is currently enabled.
   */ 
  protected
  BaseExtensionConfig
  (
   String name, 
   String toolset,  
   boolean isEnabled
  ) 
  {
    super(name);

    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset cannot be (null)!");
    pToolset = toolset;

    pIsEnabled = isEnabled;
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the execution environment under which any spawned subprocess are run.
   */
  public String
  getToolset()
  {
    return pToolset;
  }

  
  /*----------------------------------------------------------------------------------------*/

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
  public abstract BaseExt
  getExt() 
    throws PipelineException;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the extension his currently enabled.
   */ 
  public boolean 
  isEnabled() 
  {
    return pIsEnabled; 
  }
  
  /**
   * Set whether the extension his currently enabled.
   */ 
  public void
  setEnabled
  (
   boolean enabled
  ) 
  {
    pIsEnabled = enabled; 
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

    encoder.encode("Toolset", pToolset);
    encoder.encode("IsEnabled", isEnabled());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String toolset = (String) decoder.decode("Toolset");
    if(toolset == null) 
      throw new GlueException("The \"Toolset\" was missing or (null)!");
    pToolset = toolset;

    Boolean enabled = (Boolean) decoder.decode("IsEnabled");
    if(enabled == null) 
      throw new GlueException("The \"IsEnabled\" was missing or (null)!");
    pIsEnabled = enabled;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1844409119925309794L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of the execution environment under which any spawned subprocess are run.
   */
  private String  pToolset; 

  /** 
   * Whether the extension his currently enabled.
   */
  private boolean  pIsEnabled; 

}

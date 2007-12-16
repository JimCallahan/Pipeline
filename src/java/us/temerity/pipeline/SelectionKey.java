// $Id: SelectionKey.java,v 1.4 2007/12/16 06:26:40 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   K E Y                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * A symbolic key used to select the best host on which to run a job.
 * <p>
 * Selection Keys can have an optional plugin which determine when they are turned on for
 * jobs.  Under normal operation, a user selects which keys are on for each node that is 
 * submitted for regeneration.  However, if there a plugin associated with the selection key,
 * the user will not be able to specify a value for that selection key.  Instead the plugin will
 * be used to calculate whether the key should be on or off for the given node at the time
 * of job submission.
 * 
 * @see JobReqs
 * @see BaseKeyChooser
 */
public
class SelectionKey
  extends BaseKey
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
  SelectionKey() 
  {
    super();
  }

  /** 
   * Construct a new selection key, that does not use a {@link BaseKeyChooser} to
   * determine when it is on.
   * 
   * @param name 
   *   The name of the selection key.
   * 
   * @param desc 
   *   A short description of the selection key.
   */ 
  public
  SelectionKey
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc);
  }
  
  /** 
   * Construct a new selection key that uses a {@link BaseKeyChooser} to determine when
   * it is on.
   * 
   * @param name 
   *   The name of the selection key.
   * 
   * @param desc 
   *   A short description of the selection key.
   *   
   * @param keyChooser
   *   The plugin that will be used to determine when this key is on.
   */ 
  public
  SelectionKey
  (
   String name,  
   String desc,
   BaseKeyChooser keyChooser
  ) 
  {
    super(name, desc, keyChooser);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8056112617746926162L;

}




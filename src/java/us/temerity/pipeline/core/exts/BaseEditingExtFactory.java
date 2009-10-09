// $Id: BaseEditingExtFactory.java,v 1.1 2009/10/09 15:58:40 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.event.*;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E D I T I N G   E X T   F A C T O R Y                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for editing extension factories.
 */
public 
class BaseEditingExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param editID
   *   The unique ID for the editing session.
   * 
   * @param event
   *   The information known about the editing session.
   */ 
  public 
  BaseEditingExtFactory
  (
   long editID, 
   EditedNodeEvent event   
  )      
  {
    pEventID = editID; 
    pEvent = new EditedNodeEvent(event); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique ID for the editing session.
   */ 
  protected long pEventID; 

  /**
   * The information known about the editing session.
   */ 
  protected EditedNodeEvent pEvent; 
}




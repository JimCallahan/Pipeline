// $Id: BooleanRsp.java,v 1.1 2009/12/14 03:20:56 jim Exp $

package us.temerity.pipeline.message.simple;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*  B O O L E A N   R S P                                                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * A response containing only a simple boolean value. 
 */
public
class SimpleBooleanRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param isTrue
   *   The boolean value. 
   */
  public
  SimpleBooleanRsp
  (
   TaskTimer timer, 
   boolean isTrue
  )
  { 
    super(timer);

    pIsTrue = isTrue; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The boolean value. 
   */
  public boolean
  isTrue() 
  {
    return pIsTrue; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 955419249100827953L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The boolean value. 
   */
  private boolean  pIsTrue; 

}
  
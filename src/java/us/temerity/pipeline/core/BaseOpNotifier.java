package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.NotifyRsp;
import us.temerity.pipeline.math.ExtraMath;

import java.io.*;
import java.net.*;
import java.util.Locale;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   O P   N O T I F I E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A common base class for DirectOpNotifier and NetOpNotifier classes.
 */ 
public 
class BaseOpNotifier
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a operation progress helper.
   */
  public
  BaseOpNotifier()
  {
    pTotalSteps = 0L;
    pStepsCompleted = 0L; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the total numner of steps required to complete the operation.
   */
  public void 
  setTotalSteps
  (
   long total
  ) 
  {
    if(total < 0L) 
      throw new IllegalArgumentException("The total number of steps cannot be negative!"); 

    pTotalSteps = total;
    pStepsCompleted = 0L;
  }

  /**
   * Increase the number of steps completed. 
   * 
   * @param completed
   *   The number of steps completed. 
   */ 
  protected void 
  incrementCompleted
  (
   long completed   
  )
  {
    pStepsCompleted += completed;
  }

  /**
   * Get the completion percentage.
   */ 
  protected float 
  getPercentage() 
  {
    float percent = 0.0f;
    if(pTotalSteps > 0L) 
      percent = ExtraMath.clamp(((float) pStepsCompleted)/((float) pTotalSteps), 0.0f, 1.0f);
    return percent;
  }


  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total number of intermediate steps required to complete the operation.
   */ 
  private long pTotalSteps;

  /**
   * The number of intermediate steps completed.
   */ 
  private long pStepsCompleted;   

}


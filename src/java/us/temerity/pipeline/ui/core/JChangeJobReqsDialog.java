package us.temerity.pipeline.ui.core;

import javax.swing.JFrame;

/*------------------------------------------------------------------------------------------*/
/*   C H A N G E   J O B S   R E Q S   D I A L O G                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The job reqs change parameters dialog. 
 *
 */
public 
class JChangeJobReqsDialog
  extends JQueueJobsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  JChangeJobReqsDialog
  (
    JFrame parent  
  )
  {
    super(parent, "Change Job Reqs", false);
    disableBatchSize();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * @throws IllegalStateException when an attempt is made to access this field.
   */
  @Override
  public Integer 
  getBatchSize()
  {
    throw new IllegalStateException("Batch size is not a valid field for changing Job Requirements.");
  }
  
  /**
   * @throws IllegalStateException when an attempt is made to access this field.
   */
  @Override
  public boolean 
  overrideBatchSize()
  {
    throw new IllegalStateException("Batch size is not a valid field for changing Job Requirements.");
  }
  
  private static final long serialVersionUID = 8768242796930167950L;
}

// $Id: BaseExtThread.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E X T   T H R E A D                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all extension threads. <P> 
 * 
 * This class is used as a common base class for all threads started by Pipeline server 
 * daemons to run post-operation tasks for server extension plugins.  This class should 
 * never need to be used in user code.
 */ 
public 
class BaseExtThread
  extends Thread 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new thread.
   * 
   * @param title
   *   An identifying title for the thread.
   */ 
  protected
  BaseExtThread
  (
   String title
  )      
  {
    super(title); 
  }
}




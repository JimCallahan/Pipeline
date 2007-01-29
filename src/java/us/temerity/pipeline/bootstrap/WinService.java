// $Id: WinService.java,v 1.1 2007/01/29 20:51:42 jim Exp $

package us.temerity.pipeline.bootstrap;  

/*------------------------------------------------------------------------------------------*/
/*   W I N   S E R V I C E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class used to bootstrap all Pipeline windows services. <P> 
 * 
 * Any windows service which requires access to internal Pipeline classes which are license 
 * protected must be launched by a subclass of this class.  
 */ 
public abstract 
class WinService
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a windows service. 
   */ 
  public
  WinService()
  {}

  
  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S   S E R V I C E                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the processing that occurs when the service receives a Start command. <P> 
   * 
   * Subclasses should implement this method as the top-level entry function and main loop 
   * for the service which does not return as long as the service is still running. 
   */ 
  public abstract void 
  onStart();
  
  /**
   * Performs the processing that occurs when the service receives a Stop command. <P> 
   * 
   * Subclasses should implement this method to asynchronously signal the shutdown of the 
   * service.  This method need not wait on the shutdown to complete.  Typically, this 
   * method simply sets a flag to signal the shutdown which the {@link onStart} method
   * periodically checks.
   */ 
  public abstract void 
  onStop();

}

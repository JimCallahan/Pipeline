// $Id: BootApp.java,v 1.3 2004/08/29 09:22:20 jim Exp $

package us.temerity.pipeline.bootstrap;  

/*------------------------------------------------------------------------------------------*/
/*   B O O T   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class used to bootstrap all Pipeline applications.
 * 
 * Any application which requires access to internal Pipeline classes which are license 
 * protected must be launched by a subclass of this class.  See the {@link Main Main} class
 * for details about how this works.
 */ 
public abstract 
class BootApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an application. 
   */ 
  public
  BootApp()
  {}

  

  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments. <P> 
   * 
   * Subclasses should implement this method as the top-level entry function for the 
   * Pipeline application.  This method performs the same function as the standard 
   * <CODE>main(String[])</CODE> function of general Java applications.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public abstract void 
  run
  (
   String[] args
  );
}

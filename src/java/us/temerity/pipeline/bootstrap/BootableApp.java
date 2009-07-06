// $Id: BootableApp.java,v 1.1 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.bootstrap;  

/*------------------------------------------------------------------------------------------*/
/*   B O O T   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The interface needed for application class which need bootstraping in order to gain access
 * to the non-public classes in the code vault. <P> 
 * 
 * Any application which requires access to internal Pipeline classes which are license 
 * protected implement this interface.  See the {@link Main Main} class for details about how 
 * this works.
 */ 
public 
interface BootableApp
{  
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
  public void 
  run
  (
   String[] args
  );
}

// $Id: BootApp.java,v 1.1 2004/03/22 03:12:53 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   B O O T   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An abstract base class used to bootstrap all Pipeline applications
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
   * Run the application with the given command-line arguments.
   * 
   * @param args [<B>in</B>]
   *   The command-line arguments.
   */ 
  public abstract
  void 
  run
  (
   String[] args
  );
}

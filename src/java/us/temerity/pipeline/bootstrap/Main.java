// $Id: Main.java,v 1.1 2004/03/22 03:12:45 jim Exp $

package us.temerity.pipeline.bootstrap;

import us.temerity.pipeline.BootApp;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A I N                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The common entry point for all Pipeline applications.
 */
public
class Main
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function of all Pipeline applications.
   */ 
  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    if(args.length == 0) {
      System.out.print("Application name was missing!\n");
      System.exit(1);
    }

    String cname = args[0];

    String nargs[] = null;
    {
      nargs = new String[args.length-1];
      int wk;
      for(wk=0; wk<nargs.length; wk++) 
	nargs[wk] = args[wk+1];
    }
      
    try {
      BootStrapLoader loader = new BootStrapLoader();
      Class cls = loader.loadClass(cname);
      BootApp app = (BootApp) cls.newInstance();
      app.run(nargs);
    }
    catch(Exception ex) {
      System.out.print("Unable to start the application!\n");
      ex.printStackTrace(System.out);
    }

    System.exit(1);
  }
}

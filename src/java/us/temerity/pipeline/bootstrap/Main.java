// $Id: Main.java,v 1.3 2004/03/23 07:40:37 jim Exp $

package us.temerity.pipeline.bootstrap;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A I N                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The common entry point for all Pipeline applications. <P> 
 * 
 * This class must be used in order for an application to be able to load internal Pipeline 
 * classes which are license protected.  This class performs the steps needed to access
 * these classes before calling the application's {@link BootApp#run run} method. In order
 * for this process to work correctly, the application must be started using a Java
 * invocation similar to the following: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   java [<I>java-options</I>] -cp <I>root-install-dir</I>/lib/api.jar
 *   us/temerity/pipeline/bootstrap/Main <I>app-class-name</I> [<I>app-options</I>] 
 * </DIV><P> 
 * 
 * Where (<I>java-options</I>) are any extra options to the Java runtime required by 
 * the application.  The (<I>root-install-dir</I>) is the root directory of the Pipeline
 * installation for the site configured by <B>plconfig</B></A>(1). The 
 * (<I>app-class-name</I>) is the fully qualified name of the application subclass of 
 * {@link BootApp BootApp}.  The (<I>app-options</I>) are the arguments which are passed
 * to this application's {@link BootApp#run run} method.
 * 
 * @see BootApp
 */
public
class Main
{   
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private void 
  Main()
  {} 


  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function of all Pipeline applications. <P> 
   * 
   * The first element of the command line arguments (<CODE>args[0]</CODE>) is the fully 
   * qualified name of the application class to launch.  This application class must be 
   * subclass of {@link BootApp BootApp}.  The remaining command-line arguments are passed 
   * unaltered to the application's {@link BootApp#run run} method. <P> 
   * 
   * @param args 
   *   The command line arguments. 
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    if(args.length == 0) {
      System.out.print("Application name was missing!\n");
      System.exit(1);
    }

    String appArgs[] = null;
    {
      appArgs = new String[args.length-1];
      int wk;
      for(wk=0; wk<appArgs.length; wk++) 
	appArgs[wk] = args[wk+1];
    }
      
    try {
      BootStrapLoader loader = new BootStrapLoader();
      Class cls = loader.loadClass(args[0]);
      BootApp app = (BootApp) cls.newInstance();
      app.run(appArgs);
    }
    catch(Exception ex) {
      System.out.print("Unable to start the application!\n");
      ex.printStackTrace(System.out);
    }

    System.exit(1);
  }
}

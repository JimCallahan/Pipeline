// $Id: TestApp.java,v 1.2 2004/03/22 20:29:16 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A testing application.
 */
public
class TestApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  TestApp() 
  {
    super("pltest");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args [<B>in</B>]
   *   The command-line arguments.
   */ 
  public 
  void 
  run
  (
   String[] args
  )
  {
    packageArguments(args);

    System.out.print("ARGS: " + pPackedArgs + "\n");

    FileSeq fseq = new FileSeq(new FilePattern("foo", 4, "txt"), new FrameRange(1, 21, 2));

    System.out.print("FileSeq: " + fseq + "\n");

    System.exit(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {
    Logs.ops.info(
      "USAGE:\n" +
      "  pltest [options]\n\n");
  }
}



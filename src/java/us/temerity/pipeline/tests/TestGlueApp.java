// $Id: TestGlueApp.java,v 1.1 2004/02/12 15:50:12 jim Exp $

import us.temerity.pipeline.*;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*; 
import java.util.*; 


/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   G L U E                                                                */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

public 
class TestGlueApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    try {
      TestGlueApp app = new TestGlueApp();
      app.run();
      app.run2();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
    throws GlueError, InstantiationException, ClassNotFoundException, IllegalAccessException
  {
    String glue = null;
    {
      FilePattern pat  = new FilePattern("foo", 4, "rgb");
      FrameRange range = new FrameRange(10, 30, 5);
      PFileSeq fseq = new PFileSeq(pat, range);

      GlueEncoder ge = new GlueEncoder("Primary", fseq);
      glue = ge.getText();
      System.out.print("\n\n" + glue);
    }

//     PFileSeq fseq = null;
//     {
//       GlueDecoder gd = new GlueDecoder(glue);
//       fseq = (PFileSeq) gd.getObject();
//     }

//     {
//       GlueEncoder ge = new GlueEncoder("Primary", fseq);
//       System.out.print("\n\n" + ge.getText());
//     }
  }

  public void 
  run2() 
    throws GlueError, InstantiationException, ClassNotFoundException, IllegalAccessException
  {
    String glue = null;
    {
      Test test = new Test("Bullshit");

      GlueEncoder ge = new GlueEncoder("Test", test);
      glue = ge.getText();
      System.out.print("\n\n" + glue);
    }

//     Test test = null;
//     {
//       GlueDecoder gd = new GlueDecoder(glue);
//       test = (Test) gd.getObject();
//     }

//     {
//       GlueEncoder ge = new GlueEncoder("Test", test);
//       System.out.print("\n\n" + ge.getText());
//     }
  }

}

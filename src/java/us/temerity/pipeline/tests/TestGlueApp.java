// $Id: TestGlueApp.java,v 1.2 2004/02/15 16:17:26 jim Exp $

import us.temerity.pipeline.*;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*; 
import java.util.*; 


/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   G L U E                                                                */
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
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
    throws GlueException, 
           InstantiationException, ClassNotFoundException, IllegalAccessException
  {
    {
      FilePattern pat  = new FilePattern("foo", 4, "rgb");
      FrameRange range = new FrameRange(10, 30, 5);
      FileSeq fseq = new FileSeq(pat, range);
      test("Primary", fseq);
    }

    {
      GlueableStuff stuff = new GlueableStuff("Bullshit");
      test("Stuff", stuff);
    }
  }


  public void 
  test
  ( 
   String title, 
   Glueable obj   
  ) 
    throws GlueException
  {
    System.out.print("-----------------------------------\n" + 
		     "BEFORE:\n");

    GlueEncoder ge = new GlueEncoder(title, obj);
    String text = ge.getText();
    System.out.print(text + "\n");

    System.out.print("AFTER:\n");

    GlueDecoder gd = new GlueDecoder(text);
    Object obj2 = gd.getObject();

    GlueEncoder ge2 = new GlueEncoder(title, (Glueable) obj2);
    String text2 = ge.getText();
    System.out.print(text2 + "\n");
    
    assert(text.equals(text2));
  }
   

}

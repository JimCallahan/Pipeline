// $Id: TestMathApp.java,v 1.1 2004/12/16 21:40:24 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.io.*; 
import java.math.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   M A T H                                                                */
/*------------------------------------------------------------------------------------------*/

class TestMathApp
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
      TestMathApp app = new TestMathApp();
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
  {
    Point4f p = new Point4f(1.0f, 2.0f, 3.0f, 4.0f);
    Vector4f v = new Vector4f(5.0f, 7.0f, 9.0f, 11.0f);
    
    float vs[] = { 3.0f, 4.0f, 5.0f, 6.0f };
    TupleNf t = new TupleNf(vs);

    Point4f sum1 = Point4f.add(p, v);
    System.out.print("P" + p + " + V" + v + " = P" + sum1 + "\n"); 

    p.add(v).normalize();
    System.out.print("P" + p + "\n"); 

    System.out.print("P" + p + " < T" + t + " = " + p.allLt(t) + "\n");
    System.out.print("P" + p + " < V" + v + " = " + p.allLt(v) + "\n");
  }


}

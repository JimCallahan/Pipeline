// $Id: TestBigIntegerApp.java,v 1.2 2006/11/22 09:08:01 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.math.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   V E R S I O N   I D                                                    */
/*------------------------------------------------------------------------------------------*/

class TestBigIntegerApp
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
      TestBigIntegerApp app = new TestBigIntegerApp();
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
    BigInteger one = new BigInteger("101", 2);
    print(one);

    print(one.shiftLeft(1));
    print(one.shiftLeft(13));

    BigInteger a = new BigInteger("1");
    print("a", a);

    BigInteger b = new BigInteger("2");
    print("b", b);

    BigInteger c = new BigInteger("3");
    print("c", c);

    BigInteger d = new BigInteger("4");
    print("d", d);

    System.out.print("(a AND b) OR a = " + (a.and(b).or(a)) + " 0\n");
    System.out.print("(a AND c) OR a = " + (a.and(c).or(a)) + " 0\n");
    System.out.print("(a AND d) OR a = " + (a.and(c).or(a)) + " 1\n");

    System.out.print("(b AND c) OR b = " + (b.and(c).or(b)) + " 1\n");
    System.out.print("(b AND d) OR b = " + (b.and(d).or(b)) + " 0\n");

    System.out.print("(c AND d) OR c = " + (c.and(d).or(c)) + " 1\n");
  }



  private void 
  print
  (
   BigInteger i
  ) 
  {
    print(null, i);
  }

  private void 
  print
  (
   String name, 
   BigInteger i
  ) 
  {
    StringBuilder buf = new StringBuilder();

    if(name != null) 
      buf.append(name);
    else 
      buf.append("Value");
    
    buf.append(" = ");

    int wk; 
    for(wk=0; wk<i.bitLength(); wk++) 
      buf.append(i.testBit(wk) ? "0" : "1");

    buf.append(" (" + i + ")\n");

    System.out.print(buf.toString());
  }
}

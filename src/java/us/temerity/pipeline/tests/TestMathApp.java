// $Id: TestMathApp.java,v 1.2 2004/12/29 17:30:32 jim Exp $

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

    {
      Matrix33d s = Matrix33d.newScale(new Vector2d(10.0, 20.0));
      Matrix33d t = Matrix33d.newTranslate(new Vector2d(3.0, 7.0));
      Matrix33d mx = t.mult(s);
      Matrix33d inv = mx.inverse(0.000001);
      Matrix33d mx2 = inv.inverse(0.000001);

      System.out.print("T:\n" + t +"\n" + 
		       "S:\n" + s +"\n" + 
		       "MX:\n" + mx +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "MX2:\n" + mx2 +"\n\n");

      Tuple3d a  = new Tuple3d(1.0, 0.0, 1.0);
      Tuple3d a2 = mx.mult(a);
      Tuple3d a3 = inv.mult(a2);
      System.out.print("A: " + a + "  " + a2 + "  " + a3 + "\n");

      Tuple3d b  = new Tuple3d(0.0, 1.0, 1.0);
      Tuple3d b2 = mx.mult(b);
      Tuple3d b3 = inv.mult(b2);
      System.out.print("B: " + b + "  " + b2 + "  " + b3 + "\n");

      Tuple3d c  = new Tuple3d(0.5, 0.5, 1.0);
      Tuple3d c2 = mx.mult(c);
      Tuple3d c3 = inv.mult(c2);
      System.out.print("C: " + c + "  " + c2 + "  " + c3 + "\n");
    }

    {
      CoordSys2d cs = new CoordSys2d(new Vector2d(10.0, 0.0), 
				     new Vector2d(0.0, 20.0), 
				     new Point2d(3.0, 7.0));
      CoordSys2d inv = cs.inverse(0.000001);
      CoordSys2d cs2 = inv.inverse(0.000001);

      System.out.print("CS:\n" + cs +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "CS2:\n" + cs2 + "\n\n");
      
      Point2d a  = new Point2d(1.0, 0.0);
      Point2d a2 = cs.xform(a);
      Point2d a3 = inv.xform(a2);
      System.out.print("A: " + a + "  " + a2 + "  " + a3 + "\n");

      Point2d b  = new Point2d(0.0, 1.0);
      Point2d b2 = cs.xform(b);
      Point2d b3 = inv.xform(b2);
      System.out.print("B: " + b + "  " + b2 + "  " + b3 + "\n");

      Point2d c  = new Point2d(0.5, 0.5);
      Point2d c2 = cs.xform(c);
      Point2d c3 = inv.xform(c2);
      System.out.print("C: " + c + "  " + c2 + "  " + c3 + "\n");
    }

    {
      Matrix33d mx = Matrix33d.newRotate(Math.toRadians(45.0));
      Matrix33d inv = mx.inverse(0.000001);
      Matrix33d mx2 = inv.inverse(0.000001);

      System.out.print("MX:\n" + mx +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "MX2:\n" + mx2 +"\n\n");
    }

    {
      Matrix33d mx = Matrix33d.newRotate(Math.toRadians(50.0));
      Matrix33d inv = mx.inverse(0.000001);
      Matrix33d mx2 = inv.inverse(0.000001);

      System.out.print("MX:\n" + mx +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "MX2:\n" + mx2 +"\n\n");
    }

    {
      Matrix33d mx = Matrix33d.newRotate(Math.toRadians(60.0));
      Matrix33d inv = mx.inverse(0.000001);
      Matrix33d mx2 = inv.inverse(0.000001);

      System.out.print("MX:\n" + mx +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "MX2:\n" + mx2 +"\n\n");
    }

    {
      Matrix33d mx = Matrix33d.newRotate(Math.toRadians(65.0));
      Matrix33d inv = mx.inverse(0.000001);
      Matrix33d mx2 = inv.inverse(0.000001);

      System.out.print("MX:\n" + mx +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "MX2:\n" + mx2 +"\n\n");
    }

    {
      CoordSys2d cs = CoordSys2d.newRotate(Math.toRadians(45.0));
      CoordSys2d inv = cs.inverse(0.000001);
      CoordSys2d cs2 = inv.inverse(0.000001);

      System.out.print("CS:\n" + cs +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "CS2:\n" + cs2 +"\n\n");
    }
    {
      CoordSys2d cs = CoordSys2d.newRotate(Math.toRadians(55.0));
      CoordSys2d inv = cs.inverse(0.000001);
      CoordSys2d cs2 = inv.inverse(0.000001);

      System.out.print("CS:\n" + cs +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "CS2:\n" + cs2 +"\n\n");
    }
    {
      CoordSys2d cs = CoordSys2d.newRotate(Math.toRadians(65.0));
      CoordSys2d inv = cs.inverse(0.000001);
      CoordSys2d cs2 = inv.inverse(0.000001);

      System.out.print("CS:\n" + cs +"\n" + 
		       "INV:\n" + inv + "\n" +
		       "CS2:\n" + cs2 +"\n\n");
    }
  }

}

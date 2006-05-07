// $Id: TestPathApp.java,v 1.1 2006/05/07 21:34:00 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   P A T H                                                                */
/*------------------------------------------------------------------------------------------*/

public 
class TestPathApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      TestPathApp app = new TestPathApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  
 
 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TestPathApp() 
  {

  }

  public void 
  run() 
  {
//     try {
//       Path path = new Path("default", "/some/node/path/foo");
//       test(id);
//     }
//     catch(IllegalArgumentException ex) {
//       System.out.print("Caught: " + ex.getMessage() + "\n\n");
//     }

    {
      Path base = new Path("/some/unix/system/path/to/something");
      test(base);

      Path child = new Path("and/some/more");
      test(child);

      Path comp = new Path(base, child);
      test(comp);

      Path child2 = new Path("/and/some/more");
      test(child2);
      
      Path comp2 = new Path(base, child2);
      test(comp2);
      assert(comp2.equals(comp));    
      assert(base.compareTo(comp2) < 0);
      assert(comp2.compareTo(base) > 0);
      assert(comp2.compareTo(comp) == 0);

      Path comp3 = new Path(base, "and/some/more");
      test(comp3);
      assert(comp3.equals(comp));
      
      File file = new File("/some/unix/system/path/to/something");
      Path fpath = new Path(file);
      test(fpath);
      assert(fpath.equals(base));
      
      File file2 = new File("C:/foo/bar");
      Path fpath2 = new Path(file2);
      test(fpath2);
      assert(file2.equals(fpath2.toFile()));

      Path fpath3 = new Path("//server/share/foo/bar");
      test(fpath3);
    }
  }


  public void 
  test
  (
   Path path
  )
  {
    System.out.print("-----------------------------------\n" +
		     "Path\n" + 
		     "     StringRep: " + path + "\n" + 
		     "      HashCode: " + path.hashCode() + "\n" +
		     "        Prefix: " + path.getPrefix() + "\n" + 
		     "          Name: " + path.getName() + "\n" + 
		     "        Parent: " + path.getParent() + "\n" + 
		     "    IsAbsolute: " + path.isAbsolute() + "\n" + 
		     "          File: " + path.toFile() + "\n" + 
		     "      OsString: " + path.toOsString() + "\n" + 
		     "        (Unix): " + path.toOsString(OsType.Unix) + "\n" + 
		     "       (MacOS): " + path.toOsString(OsType.MacOS) + "\n" + 
		     "     (Windows): " + path.toOsString(OsType.Windows) + "\n");

    System.out.print("       Parents: ");
    Path ppath = path;
    while(true) {
      System.out.print(ppath + "\n");
      ppath = ppath.getParentPath();
      if(ppath != null) 
	System.out.print("                ");
      else 
	break;
    }

    System.out.print("  ---\n");

    try {
      GlueEncoder ge = new GlueEncoderImpl("Path", path);
      System.out.print(ge.getText());

      GlueDecoder gd = new GlueDecoderImpl(ge.getText());
      Path path2 = (Path) gd.getObject();
      
      System.out.print("Path (Glue): " + path2 + "\n");
      assert(path.equals(path2));
      assert(path.hashCode() == path2.hashCode());
    }
    catch(GlueException ex) {
      System.out.print(ex + "\n");
      assert(false);
    }

    {
      Path path2 = new Path(path);
      assert(path.equals(path2));
      assert(path.toString().equals(path2.toString()));
      assert(path.hashCode() == path2.hashCode());      
    }

    {    
      Path path2 = (Path) path.clone();
      assert(path.equals(path2));
      assert(path.toString().equals(path2.toString()));
      assert(path.hashCode() == path2.hashCode());      
    }
  } 
}

// $Id: TestNodeIDApp.java,v 1.5 2006/05/07 21:30:13 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   N O D E  I D                                                           */
/*------------------------------------------------------------------------------------------*/

public 
class TestNodeIDApp
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
      TestNodeIDApp app = new TestNodeIDApp();
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
  TestNodeIDApp() 
  {

  }

  public void 
  run() 
  {
    try {
      NodeID id = new NodeID(null, "default", "/some/node/path/foo");
      test(id);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    try {
      NodeID id = new NodeID("jim", null, "/some/node/path/foo");
      test(id);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    try {
      NodeID id = new NodeID("jim", "default", null);
      test(id);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    {
      NodeID id = new NodeID("jim", "default", "/some/node/path/foo");
      test(id);
    }
  }


  public void 
  test
  (
   NodeID id
  )
  {
    System.out.print("-----------------------------------\n" +
		     "NodeID\n" + 
		     "     StringRep: " + id + "\n" + 
		     "      HashCode: " + id.hashCode() + "\n" +
		     "        Author: " + id.getAuthor() + "\n" + 
		     "          View: " + id.getView() + "\n" + 
		     "        Parent: " + id.getParent() + "\n" + 
		     "   WorkingPath: " + id.getWorkingPath() + "\n" + 
		     " WorkingParent: " + id.getWorkingParent() + "\n" + 
		     " CheckedInPath: " + 
		     id.getCheckedInPath(new VersionID("1.2.3")) + "\n\n");

    System.out.print("  ---\n");

    try {
      GlueEncoder ge = new GlueEncoderImpl("NodeID", id);
      System.out.print(ge.getText());

      GlueDecoder gd = new GlueDecoderImpl(ge.getText());
      NodeID id2 = (NodeID) gd.getObject();
      
      System.out.print("NodeID (Glue): " + id2 + "\n");
      assert(id.equals(id2));
      assert(id.hashCode() == id2.hashCode());
    }
    catch(GlueException ex) {
      System.out.print(ex + "\n");
      assert(false);
    }

    {
      NodeID id2 = new NodeID(id);
      assert(id.equals(id2));
      assert(id.hashCode() == id2.hashCode());      
    }

    {
      NodeID id2 = new NodeID(id, "foo");
      assert(id.compareTo(id2) < 0); 
      assert(!id.equals(id2));
      assert(id2.toString().startsWith(id.toString()));
    }
		     
  } 
}

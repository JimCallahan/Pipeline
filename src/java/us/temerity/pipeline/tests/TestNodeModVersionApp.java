// $Id: TestNodeModVersionApp.java,v 1.1 2004/03/11 14:12:53 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   N O D E   M O D  /  V E R S I O N                                      */
/*------------------------------------------------------------------------------------------*/

public 
class TestNodeModVersionApp
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
    Logs.init();
    //Logs.sub.setLevel(Level.FINEST);

    try {
      Plugins.init();
      FileCleaner.init();

      TestNodeModVersionApp app = new TestNodeModVersionApp();
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
    throws GlueException, PipelineException
  {
    NodeMod mod1 = null;
    {
      FileSeq primary = 
	new FileSeq(new FilePattern("frog", 4, "txt"), 
		    new FrameRange(0, 10, 2));
      
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      secondary.add(new FileSeq(new FilePattern("tadpoleA", 0, "txt"), 
				new FrameRange(20, 30, 2)));
      secondary.add(new FileSeq(new FilePattern("tadpoleB", 2, "txt"), 
				new FrameRange(23, 33, 2)));
      
      NodeMod mod = 
	new NodeMod("/organisms/animals/reptiles/frog", 
		    primary, secondary, 
		    TestInfo.sBuildToolset, "Emacs", 
		    null, null, false, false, 0);

      test(mod);

      mod1 = mod;
    }
  
    NodeMod mod2 = null;
    {
      NodeMod mod = new NodeMod(mod1);
      test(mod);
      
      {
	mod.rename("/organisms/animals/reptiles/toad");
	  
	try {
	  mod.rename("");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
	
	try {
	  mod.rename("!/organisms/animals/reptiles/toad");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.rename("/2organisms/animals/reptiles/toad");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.rename("/organisms/ani%mals/reptiles/toad");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.rename("/organisms/animals/reptiles/");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.rename("/organisms/animals//reptiles/toad");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.rename("organisms/animals/reptiles/toad");
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
      }

      {
	mod.adjustFrameRange(new FrameRange(2, 8, 2));
	  
	try {
	  mod.adjustFrameRange(new FrameRange(2, 20, 1));
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.adjustFrameRange(new FrameRange(1, 11, 2));
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.adjustFrameRange(null);
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
      }
	
      {
	mod.addSecondarySequence(new FileSeq(new FilePattern("tadpoleC", 3, "txt"), 
					     new FrameRange(12, 15, 1)));

	try {
	  mod.addSecondarySequence(null);
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
	  
	try {
	  mod.addSecondarySequence(new FileSeq(new FilePattern("bad", "txt"), null));
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
      }
	 

      { 
	mod.removeSecondarySequence(new FileSeq(new FilePattern("tadpoleA", 0, "txt"), 
						new FrameRange(22, 28, 2)));

	try {
	  mod.removeSecondarySequence(new FileSeq(new FilePattern("bad", "txt"), null));
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
      }

      { 
	try {
	  mod.setToolset("sdev031214"); 
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}

	try {
	  mod.setToolset("fooy"); 
	}
	catch(IllegalArgumentException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n");
	}
      }

      test(mod);
      assert(!mod1.equals(mod));

      mod2 = mod;
    }

    NodeMod mod3 = null;
    {
      FileSeq primary = 
	new FileSeq(new FilePattern("snake", "txt"), null);

      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();

      BaseAction action = Plugins.newAction("Script");
      action.setSingleParamValue("Interpreter", "bash");
      action.setSingleParamValue("ScriptText", "echo I'm a snake...");

      HashSet<String> licenseKeys = new HashSet<String>();
      licenseKeys.add("MayaRender");

      HashSet<String> selectionKeys = new HashSet<String>();
      selectionKeys.add("Rush");
      selectionKeys.add("Pentium4");

      JobReqs jreqs = new JobReqs(50, 2.5f, 256*1024*1024, 512*1024*1024, 
				  licenseKeys, selectionKeys);

      NodeMod mod = 
	new NodeMod("/organisms/animals/reptiles/snake", 
		    primary, secondary, 
		    TestInfo.sBuildToolset, "Emacs", 
		    action, jreqs, false, false, 0);

      test(mod);

      mod3 = mod;
    }

    NodeMod mod4 = null;
    {
      NodeMod mod = new NodeMod(mod1);
      
      mod.rename("/organisms/animals/mammals/mouse");
      mod.removeAllSecondarySequences();
      mod.adjustFrameRange(new FrameRange(103, 115, 3));

      BaseAction action = Plugins.newAction("Script");
      action.setSingleParamValue("Interpreter", "bash");
      action.setSingleParamValue("ScriptText", "echo I'm a mouse...");
      mod.setAction(action);
      
      HashSet<String> licenseKeys = new HashSet<String>();
      licenseKeys.add("Houdini");

      HashSet<String> selectionKeys = new HashSet<String>();

      JobReqs jreqs = new JobReqs(50, 2.5f, 256*1024*1024, 512*1024*1024, 
				  licenseKeys, selectionKeys);
      mod.setJobRequirements(jreqs);

      test(mod);

      mod4 = mod;
    }

    NodeMod mod5 = null;
    {
      NodeMod mod = new NodeMod(mod4);
      
      mod.setIgnoreOverflow(true);
      mod.setIsSerial(true);

      try {
	mod.setBatchSize(10);
      }
      catch(IllegalArgumentException ex) {
	System.out.print("Caught: " + ex.getMessage() + "\n");
      }
      
      mod.setIsSerial(false);
      mod.setBatchSize(10);

      test(mod);

      mod5 = mod;
    }

    NodeMod mod6 = null;
    {
      NodeMod mod = new NodeMod(mod5);
      
      try {
	mod.setProperties(mod1);  
      }
      catch(IllegalArgumentException ex) {
	System.out.print("Caught: " + ex.getMessage() + "\n");
      }

      mod.setProperties(mod4);  

      test(mod);

      mod6 = mod;
    }

    try {
      NodeMod modA = new NodeMod(mod5);
      NodeMod modB = (NodeMod) mod5.clone();
      assert(modA.equals(modB));
    }
    catch(CloneNotSupportedException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n");
    }

    {
      NodeVersion vsn1 = 
	new NodeVersion(mod4, new VersionID(), 
			"Initial revision.");
      test(vsn1);

      NodeVersion vsn2 = 
	new NodeVersion(mod5, new VersionID(vsn1.getVersionID(), VersionID.Level.Minor), 
			"Changes the job requirements.");
      test(vsn2);

      NodeVersion vsn3 = 
	new NodeVersion(mod5, new VersionID(vsn2.getVersionID(), VersionID.Level.Major), 
			"Changed the node properties.");
      test(vsn3);

      TreeMap<VersionID,NodeVersion> table = new TreeMap<VersionID,NodeVersion>();
      table.put(vsn1.getVersionID(), vsn1);
      table.put(vsn2.getVersionID(), vsn2);
      table.put(vsn3.getVersionID(), vsn3);
      
      System.out.print("-----------------------------------\n");
      {
	NodeTest test = new NodeTest(table);
	GlueEncoder ge = new GlueEncoder("Node", test);
	System.out.print(ge.getText() + "\n");
      }
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  public void 
  test
  (
   NodeMod mod
  )
    throws GlueException
  {
    System.out.print("-----------------------------------\n");
    {
      GlueEncoder ge = new GlueEncoder("NodeMod", mod);
      System.out.print(ge.getText() + "\n");

      GlueDecoder gd = new GlueDecoder(ge.getText());
      NodeMod mod2 = (NodeMod) gd.getObject();
		       
      assert(mod.equals(mod2));
    }
  }

  public void 
  test
  (
   NodeVersion vsn
  )
    throws GlueException
  {
    System.out.print("-----------------------------------\n");
    {
      GlueEncoder ge = new GlueEncoder("NodeVersion", vsn);
      System.out.print(ge.getText() + "\n");

      GlueDecoder gd = new GlueDecoder(ge.getText());
      NodeVersion vsn2 = (NodeVersion) gd.getObject();

      assert(vsn.equals(vsn2));
    }
  }


}

// $Id: TestPluginsApp.java,v 1.2 2004/02/25 02:59:04 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   P L U G I N S                                                          */
/*------------------------------------------------------------------------------------------*/

public 
class TestPluginsApp
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
    Logs.sub.setLevel(Level.FINEST);
    Logs.plg.setLevel(Level.FINE);


    try {
      Plugins.init();
      FileCleaner.init();

      TestPluginsApp app = new TestPluginsApp();
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
    throws PipelineException, InterruptedException
  {
    TreeMap<String,String> env = Toolsets.lookup(TestInfo.sBuildToolset);

    File dir = new File(System.getProperty("user.dir") + "/data");

//     testEditors(env, dir);
    testActions(env, dir);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void
  testEditors
  (
   TreeMap<String,String> env, 
   File dir
  ) 
    throws PipelineException, InterruptedException
  {
    {
      BaseEditor emacs = Plugins.newEditor("Emacs");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = emacs.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor emacs = Plugins.newEditor("EmacsClient");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = emacs.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor maya = Plugins.newEditor("Maya");
      FileSeq fseq = new FileSeq("sphere", "ma");
      SubProcess proc = maya.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor ivview = Plugins.newEditor("IvView");
      FileSeq fseq = new FileSeq("sphere", "iv");
      SubProcess proc = ivview.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor houdini = Plugins.newEditor("Houdini");
      FileSeq fseq = new FileSeq("sphere", "hipnc");
      SubProcess proc = houdini.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor gplay = Plugins.newEditor("GPlay");
      FileSeq fseq = new FileSeq("sphere", "bgeo");
      SubProcess proc = gplay.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor fcheck = Plugins.newEditor("FCheck");
      FileSeq fseq = new FileSeq("testimage", "iff");
      SubProcess proc = fcheck.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor fcheck = Plugins.newEditor("FCheck");
      FilePattern pat = 
    	new FilePattern("normal", 4, "iff");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      SubProcess proc = fcheck.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor mplay = Plugins.newEditor("MPlay");
      FileSeq fseq = new FileSeq("testimage", "tif");
      SubProcess proc = mplay.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }

    {
      BaseEditor mplay = Plugins.newEditor("MPlay");
      FilePattern pat = 
    	new FilePattern("normal", 4, "tif");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      SubProcess proc = mplay.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor acroread = Plugins.newEditor("Acroread");
      FileSeq fseq = new FileSeq("roadmap", "pdf");
      SubProcess proc = acroread.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor gedit = Plugins.newEditor("GEdit");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = gedit.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor xdvi = Plugins.newEditor("XDvi");
      FileSeq fseq = new FileSeq("roadmap", "dvi");
      SubProcess proc = xdvi.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  
    {
      BaseEditor gimp = Plugins.newEditor("Gimp");
      FileSeq fseq = new FileSeq("testimage", "tif");
      SubProcess proc = gimp.launch(fseq, env, dir);
      proc.join();
      assert(proc.wasSuccessful());
    }
  }
 


  private void
  testActions
  (
   TreeMap<String,String> env, 
   File dir
  ) 
    throws PipelineException, InterruptedException
  {
    {
      BaseAction script = Plugins.newAction("Script");
      script.setSingleParamValue("Interpreter", "bash");
      script.setSingleParamValue("ScriptText", "echo 2^123 | bc -l");

      SubProcess proc = null;
      {
	FileSeq primaryTarget = new FileSeq("bob", "txt"); 
	
	ArrayList<FileSeq> secondaryTargets = new ArrayList<FileSeq>();
	secondaryTargets.add(new FileSeq(new FilePattern("huck", 3, "txt"), 
					 new FrameRange(3, 9, 2)));
	secondaryTargets.add(new FileSeq(new FilePattern("joe", 0, "txt"), 
					 new FrameRange(1, 4, 1)));
	secondaryTargets.add(new FileSeq(new FilePattern("george", 2, "txt"), 
					 new FrameRange(11, 14, 1)));
	
	TreeMap<String,FileSeq> primarySources = new TreeMap<String,FileSeq>();
	primarySources.put("/some/dependency/node/fred", 
			   new FileSeq(new FilePattern("fred", 4, "txt"), 
				     new FrameRange(10, 40, 10)));
	primarySources.put("/some/dependency/node/mike", 
			   new FileSeq(new FilePattern("mike", 4, "txt"), 
				       new FrameRange(2, 5, 1)));
      
	TreeMap<String,ArrayList> secondarySources = new TreeMap<String,ArrayList>();
	{
	  ArrayList fseqs = new ArrayList();
	  fseqs.add(new FileSeq(new FilePattern("jenny", 0, "txt"), 
				new FrameRange(33, 39, 2)));
	  fseqs.add(new FileSeq("jill", "txt"));

	  secondarySources.put("/some/dependency/node/fred", fseqs);
	}

	proc =script.prep(123, "/some/node/bob", System.getProperty("user.name"), 
			  primaryTarget, secondaryTargets, 
			  primarySources, secondarySources, 
			  env, dir);
      }

      proc.start();
      proc.join();
      assert(proc.wasSuccessful());   
      assert(proc.getStdOut().equals("10633823966279326983230456482242756608\n"));
    }

  }
}

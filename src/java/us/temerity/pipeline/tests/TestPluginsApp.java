// $Id: TestPluginsApp.java,v 1.7 2004/03/23 20:41:25 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;

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
    throws CloneNotSupportedException, GlueException, PipelineException, InterruptedException
  {
    SortedMap<String,String> env = Toolsets.lookup(TestInfo.sBuildToolset);

    File dir = new File(System.getProperty("user.dir") + "/data");

    testActions(env, dir);
    testEditors(env, dir);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void
  testEditors
  (
   SortedMap<String,String> env, 
   File dir
  ) 
    throws CloneNotSupportedException, GlueException, PipelineException, InterruptedException
  {
    {
      BaseEditor emacs = Plugins.newEditor("Emacs");
      FileSeq fseq = new FileSeq("sometext", null);
      testEditorHelper(emacs, fseq, env, dir);
    }
  
    {
      BaseEditor emacs = Plugins.newEditor("EmacsClient");
      FileSeq fseq = new FileSeq("sometext", null);
      testEditorHelper(emacs, fseq, env, dir);
    }

    {
      BaseEditor maya = Plugins.newEditor("Maya");
      FileSeq fseq = new FileSeq("sphere", "ma");
      testEditorHelper(maya, fseq, env, dir);
    }
    
    {
      BaseEditor ivview = Plugins.newEditor("IvView");
      FileSeq fseq = new FileSeq("sphere", "iv");
      testEditorHelper(ivview, fseq, env, dir);
    }

    {
      BaseEditor houdini = Plugins.newEditor("Houdini");
      FileSeq fseq = new FileSeq("sphere", "hipnc");
      testEditorHelper(houdini, fseq, env, dir);
    }

    {
      BaseEditor gplay = Plugins.newEditor("GPlay");
      FileSeq fseq = new FileSeq("sphere", "bgeo");
      testEditorHelper(gplay, fseq, env, dir);
    }

    {
      BaseEditor fcheck = Plugins.newEditor("FCheck");
      FileSeq fseq = new FileSeq("testimage", "iff");
      testEditorHelper(fcheck, fseq, env, dir);
    }
    
    {
      BaseEditor fcheck = Plugins.newEditor("FCheck");
      FilePattern pat = 
   	new FilePattern("normal", 4, "iff");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      testEditorHelper(fcheck, fseq, env, dir);
    }

    {
      BaseEditor mplay = Plugins.newEditor("MPlay");
      FileSeq fseq = new FileSeq("testimage", "tif");
      testEditorHelper(mplay, fseq, env, dir);
    }

    {
      BaseEditor mplay = Plugins.newEditor("MPlay");
      FilePattern pat = 
   	new FilePattern("normal", 4, "tif");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      testEditorHelper(mplay, fseq, env, dir);
    }

    {
      BaseEditor acroread = Plugins.newEditor("Acroread");
      FileSeq fseq = new FileSeq("roadmap", "pdf");
      testEditorHelper(acroread, fseq, env, dir);
    }

    {
      BaseEditor gedit = Plugins.newEditor("GEdit");
      FileSeq fseq = new FileSeq("sometext", null);
      testEditorHelper(gedit, fseq, env, dir);
    }
    
    {
      BaseEditor xdvi = Plugins.newEditor("XDvi");
      FileSeq fseq = new FileSeq("roadmap", "dvi");
      testEditorHelper(xdvi, fseq, env, dir);
    }

    {
      BaseEditor gimp = Plugins.newEditor("Gimp");
      FileSeq fseq = new FileSeq("testimage", "tif");
      testEditorHelper(gimp, fseq, env, dir);
    }
  }
 
  private void 
  testEditorHelper
  (
   BaseEditor editor, 
   FileSeq fseq, 
   SortedMap<String,String> env, 
   File dir
  ) 
    throws CloneNotSupportedException, GlueException, PipelineException, InterruptedException
  {
    SubProcess proc = editor.launch(fseq, env, dir);
    proc.join();
    assert(proc.wasSuccessful());

    testGlue(editor.getName(), editor);

    BaseEditor clone = (BaseEditor) editor.clone();
    assert(editor.equals(clone));

    System.out.print("Editor:\n" + 
		     "  Name = " + clone.getName() + "\n" +
		     "  Description = " + clone.getDescription() + "\n" +
		     "  Program = " + clone.getProgram() + "\n\n");
  }



  private void
  testActions
  (
   SortedMap<String,String> env, 
   File dir
  ) 
    throws CloneNotSupportedException, GlueException, PipelineException, InterruptedException
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

      testGlue("ScriptAction", script);

      BaseAction clone = null;
      {
	System.out.print("CLONE:\n");
	
	clone = (BaseAction) script.clone();
	assert(script.equals(clone));
	
	{
	  GlueEncoder ge = new GlueEncoder("ScriptAction", clone);
	  String text = ge.getText();
	  System.out.print(text + "\n");
	}
      }
      
      {
	System.out.print("MODIFIED:\n");
	
	script.setSingleParamValue("Interpreter", "perl");
	assert(!script.equals(clone));
	
	{
	  GlueEncoder ge = new GlueEncoder("ScriptAction", script);
	  String text = ge.getText();
	  System.out.print(text + "\n");
	}
	
	{
	  GlueEncoder ge = new GlueEncoder("ScriptAction", clone);
	  String text = ge.getText();
	  System.out.print(text + "\n");
	}
      }
    }
  }


  private void 
  testGlue
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
    assert(obj.equals(obj2));

    GlueEncoder ge2 = new GlueEncoder(title, (Glueable) obj2);
    String text2 = ge.getText();
    System.out.print(text2 + "\n");
    
    assert(text.equals(text2));
  }
}

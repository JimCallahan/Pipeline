// $Id: TestPluginsApp.java,v 1.13 2005/01/22 01:36:36 jim Exp $

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
    
    Logs.sub.setLevel(Level.FINEST);
    Logs.plg.setLevel(Level.FINE);


    try {
      PluginMgr.init();
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
    SortedMap<String,String> env = new TreeMap<String,String>(System.getenv());

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
      BaseEditor emacs = PluginMgr.getInstance().newEditor("Emacs", null);
      FileSeq fseq = new FileSeq("sometext", (String) null);
      testEditorHelper(emacs, fseq, env, dir);
    }
  
    {
      BaseEditor emacs = PluginMgr.getInstance().newEditor("EmacsClient", null);
      FileSeq fseq = new FileSeq("sometext", (String) null);
      testEditorHelper(emacs, fseq, env, dir);
    }

    {
      BaseEditor maya = PluginMgr.getInstance().newEditor("Maya", null);
      FileSeq fseq = new FileSeq("sphere", "ma");
      testEditorHelper(maya, fseq, env, dir);
    }
    
    {
      BaseEditor ivview = PluginMgr.getInstance().newEditor("IvView", null);
      FileSeq fseq = new FileSeq("sphere", "iv");
      testEditorHelper(ivview, fseq, env, dir);
    }

    {
      BaseEditor houdini = PluginMgr.getInstance().newEditor("Houdini", null);
      FileSeq fseq = new FileSeq("sphere", "hipnc");
      testEditorHelper(houdini, fseq, env, dir);
    }

    {
      BaseEditor gplay = PluginMgr.getInstance().newEditor("GPlay", null);
      FileSeq fseq = new FileSeq("sphere", "bgeo");
      testEditorHelper(gplay, fseq, env, dir);
    }

    {
      BaseEditor fcheck = PluginMgr.getInstance().newEditor("FCheck", null);
      FileSeq fseq = new FileSeq("testimage", "iff");
      testEditorHelper(fcheck, fseq, env, dir);
    }
    
    {
      BaseEditor fcheck = PluginMgr.getInstance().newEditor("FCheck", null);
      FilePattern pat = 
   	new FilePattern("normal", 4, "iff");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      testEditorHelper(fcheck, fseq, env, dir);
    }

    {
      BaseEditor mplay = PluginMgr.getInstance().newEditor("MPlay", null);
      FileSeq fseq = new FileSeq("testimage", "tif");
      testEditorHelper(mplay, fseq, env, dir);
    }

    {
      BaseEditor mplay = PluginMgr.getInstance().newEditor("MPlay", null);
      FilePattern pat = 
   	new FilePattern("normal", 4, "tif");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      testEditorHelper(mplay, fseq, env, dir);
    }

    {
      BaseEditor acroread = PluginMgr.getInstance().newEditor("Acroread", null);
      FileSeq fseq = new FileSeq("roadmap", "pdf");
      testEditorHelper(acroread, fseq, env, dir);
    }

    {
      BaseEditor gedit = PluginMgr.getInstance().newEditor("GEdit", null);
      FileSeq fseq = new FileSeq("sometext", (String) null);
      testEditorHelper(gedit, fseq, env, dir);
    }
    
    {
      BaseEditor xdvi = PluginMgr.getInstance().newEditor("XDvi", null);
      FileSeq fseq = new FileSeq("roadmap", "dvi");
      testEditorHelper(xdvi, fseq, env, dir);
    }

    {
      BaseEditor gimp = PluginMgr.getInstance().newEditor("Gimp", null);
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
    SubProcessLight proc = editor.launch(fseq, env, dir);
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
      BaseAction script = PluginMgr.getInstance().newAction("Script", null);
      script.setSingleParamValue("Interpreter", "bash");
      script.setSingleParamValue("ScriptText", "echo 2^123 | bc -l");

      SubProcessHeavy proc = null;
      {
	FileSeq primaryTarget = new FileSeq("bob", "txt"); 
	
	TreeSet<FileSeq> secondaryTargets = new TreeSet<FileSeq>();
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
   
	TreeMap<String,Set<FileSeq>> secondarySources = 
	  new TreeMap<String,Set<FileSeq>>();
	{
	  TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();
	  fseqs.add(new FileSeq(new FilePattern("jenny", 0, "txt"), 
				new FrameRange(33, 39, 2)));
	  fseqs.add(new FileSeq("jill", "txt"));

	  secondarySources.put("/some/dependency/node/fred", fseqs);
	}

	ActionAgenda agenda = 
	  new ActionAgenda(123L, 
			   new NodeID(System.getProperty("user.name"), "default", 
				      "/some/node/bob"), 
			   primaryTarget, secondaryTargets, 
			   primarySources, secondarySources, 
			   "DummyToolset", env, dir);

	proc = script.prep(agenda, new File(dir, "stdout"), new File(dir, "stderr"));
      }

      proc.start();
      proc.join();
      assert(proc.wasSuccessful());   

      testGlue("ScriptAction", script);

      BaseAction clone = null;
      {
	System.out.print("CLONE:\n");
	
	clone = (BaseAction) script.clone();
	assert(script.equals(clone));
	
	{
	  GlueEncoder ge = new GlueEncoderImpl("ScriptAction", clone);
	  String text = ge.getText();
	  System.out.print(text + "\n");
	}
      }
      
      {
	System.out.print("MODIFIED:\n");
	
	script.setSingleParamValue("Interpreter", "perl");
	assert(!script.equals(clone));
	
	{
	  GlueEncoder ge = new GlueEncoderImpl("ScriptAction", script);
	  String text = ge.getText();
	  System.out.print(text + "\n");
	}
	
	{
	  GlueEncoder ge = new GlueEncoderImpl("ScriptAction", clone);
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

    GlueEncoder ge = new GlueEncoderImpl(title, obj);
    String text = ge.getText();
    System.out.print(text + "\n");

    System.out.print("AFTER:\n");

    GlueDecoder gd = new GlueDecoderImpl(text);
    Object obj2 = gd.getObject();	
    assert(obj.equals(obj2));

    GlueEncoder ge2 = new GlueEncoderImpl(title, (Glueable) obj2);
    String text2 = ge.getText();
    System.out.print(text2 + "\n");
    
    assert(text.equals(text2));
  }
}

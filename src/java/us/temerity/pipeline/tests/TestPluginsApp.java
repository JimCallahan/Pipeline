// $Id: TestPluginsApp.java,v 1.1 2004/02/23 23:50:35 jim Exp $

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
    TreeMap<String,String> env = Toolsets.lookup("sdev040211");

    File dir = new File(System.getProperty("user.dir") + "/data");
    
    {
      BaseEditor maya = Plugins.newEditor("maya");
      FileSeq fseq = new FileSeq("sphere", "ma");
      SubProcess proc = maya.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor ivview = Plugins.newEditor("ivview");
      FileSeq fseq = new FileSeq("sphere", "iv");
      SubProcess proc = ivview.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor houdini = Plugins.newEditor("houdini");
      FileSeq fseq = new FileSeq("sphere", "hipnc");
      SubProcess proc = houdini.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor gplay = Plugins.newEditor("gplay");
      FileSeq fseq = new FileSeq("sphere", "bgeo");
      SubProcess proc = gplay.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor fcheck = Plugins.newEditor("fcheck");
      FileSeq fseq = new FileSeq("testimage", "iff");
      SubProcess proc = fcheck.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor fcheck = Plugins.newEditor("fcheck");
      FilePattern pat = 
    	new FilePattern("normal", 4, "iff");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      SubProcess proc = fcheck.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor mplay = Plugins.newEditor("mplay");
      FileSeq fseq = new FileSeq("testimage", "tif");
      SubProcess proc = mplay.launch(fseq, env, dir);
      proc.join();
    }

    {
      BaseEditor mplay = Plugins.newEditor("mplay");
      FilePattern pat = 
    	new FilePattern("normal", 4, "tif");
      FrameRange range = new FrameRange(0, 18, 2);
      FileSeq fseq = new FileSeq(pat, range);
      SubProcess proc = mplay.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor acroread = Plugins.newEditor("acroread");
      FileSeq fseq = new FileSeq("roadmap", "pdf");
      SubProcess proc = acroread.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor emacs = Plugins.newEditor("emacs");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = emacs.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor emacs = Plugins.newEditor("emacsclient");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = emacs.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor gedit = Plugins.newEditor("gedit");
      FileSeq fseq = new FileSeq("sometext", null);
      SubProcess proc = gedit.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor xdvi = Plugins.newEditor("xdvi");
      FileSeq fseq = new FileSeq("roadmap", "dvi");
      SubProcess proc = xdvi.launch(fseq, env, dir);
      proc.join();
    }
  
    {
      BaseEditor gimp = Plugins.newEditor("gimp");
      FileSeq fseq = new FileSeq("testimage", "tif");
      SubProcess proc = gimp.launch(fseq, env, dir);
      proc.join();
    }
  }
 
}

// $Id: TestNodeMgrApp.java,v 1.1 2004/03/26 04:38:06 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.message.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   F I L E   M G R                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestNodeMgrApp
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
    Logs.net.setLevel(Level.FINEST);
    Logs.sub.setLevel(Level.FINER);

    try {
      TestNodeMgrApp app = new TestNodeMgrApp();
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
    throws InterruptedException, PipelineException, IOException
  { 
    /* common stuff */ 
    Map<String,String> env = System.getenv();
    File cwd = new File(System.getProperty("user.dir") + "/data");
    String user = System.getProperty("user.name");

    /* initialize data files */ 
    File nodeDir = new File(cwd, "node");
    nodeDir.mkdirs();
    
    /* test nodes */ 
    NodeID idA = null;
    NodeMod modA = null;
    {
      String name = "/images/normal";
      idA = new NodeID(user, "default", name);
      
      FileSeq primary = new FileSeq(new FilePattern("normal", 4, "tif"), 
				    new FrameRange(0, 43, 1));
    
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      secondary.add(new FileSeq(new FilePattern("primary", 4, "tif"), 
				new FrameRange(0, 43, 1)));
      
      modA = new NodeMod(name, primary, secondary, 
			TestInfo.sBuildToolset, null);
    }

    NodeID idB = null;
    NodeMod modB = null;
    {
      String name = "/images/selected";
      idB = new NodeID(user, "default", name);
      
      FileSeq primary = new FileSeq(new FilePattern("selected", 4, "tif"), 
				    new FrameRange(0, 43, 1));
    
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      modB = new NodeMod(name, primary, secondary, 
			TestInfo.sBuildToolset, null);
    }

    /* initialize data files */ 
    File prod = new File(cwd, "prod");
    File dir = new File(prod, "working/" + user + "/default/images");

    {
      dir.mkdirs();
      ArrayList<String> args = new ArrayList<String>();

      for(FileSeq fseq : modA.getSequences()) 
	for(File file : fseq.getFiles()) 
	  if((new File(cwd, file.getPath())).exists())
	    args.add(file.getPath());

      for(FileSeq fseq : modB.getSequences()) 
	for(File file : fseq.getFiles()) 
	  if((new File(cwd, file.getPath())).exists())
	    args.add(file.getPath());

      args.add(dir.getPath());
	
      SubProcess proc = new SubProcess("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }
    

    /* start the node manager server */ 
    NodeMgrServer server = new NodeMgrServer(nodeDir, 53139);
    server.start();

    /* give the server a chance to start */ 
    Thread.currentThread().sleep(2000);


    /* run some client tasks */ 
    {
      ArrayList<ClientTask> clients = new ArrayList<ClientTask>();
      
      ClientTask clientA = new ClientTask(modA);
      clients.add(clientA);
      
      ClientTask clientB = new ClientTask(modB);
      clients.add(clientB);
      
      for(ClientTask client : clients) 
	client.start();

      for(ClientTask client : clients) 
 	client.join();
    }

    /* give the server a chance to shutdown */ 
    Thread.currentThread().sleep(1000);    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class ClientTask
    extends Thread
  {
    public 
    ClientTask
    (
     NodeMod mod
    ) 
    {
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      try {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);
      

	client.register("default", pNodeMod);
	
      
	client.shutdown();
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }

    private NodeMod  pNodeMod;
  }
}

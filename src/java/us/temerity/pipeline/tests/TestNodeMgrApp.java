// $Id: TestNodeMgrApp.java,v 1.3 2004/03/28 00:50:37 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;
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
    
    {
      /* start the node manager server */ 
      NodeMgrServer server = new NodeMgrServer(nodeDir, 53139);
      server.start();
      
      /* give the server a chance to start */ 
      Thread.currentThread().sleep(1000);
      
      /* run some client tasks */ 
      {
 	ArrayList<ClientTask1> clients = new ArrayList<ClientTask1>();
	
 	ClientTask1 clientA = new ClientTask1(123, modA);
 	clients.add(clientA);
	
 	ClientTask1 clientB = new ClientTask1(456, modB);
 	clients.add(clientB);
	
 	for(ClientTask1 client : clients) 
 	  client.start();
	
 	for(ClientTask1 client : clients) 
 	  client.join();

	modA = clientA.getNodeMod();
	modB = clientB.getNodeMod();
      }
      
      NodeMgrClient client = new NodeMgrClient("localhost", 53139);
      client.shutdown();
      
      server.join();
    }

    /* port a change to be released */ 
    Thread.currentThread().sleep(5000);

    {
      /* start the node manager server */ 
      NodeMgrServer server = new NodeMgrServer(nodeDir, 53139);
      server.start();
      
      /* give the server a chance to start */ 
      Thread.currentThread().sleep(1000);
      
      /* run some client tasks */ 
      {
	ArrayList<ClientTask2> clients = new ArrayList<ClientTask2>();
	
	ClientTask2 clientA = new ClientTask2(123, modA);
	clients.add(clientA);
	
	ClientTask2 clientB = new ClientTask2(456, modB);
	clients.add(clientB);
	
	for(ClientTask2 client : clients) 
	  client.start();
	
	for(ClientTask2 client : clients) 
	  client.join();
      }
      
      NodeMgrClient client = new NodeMgrClient("localhost", 53139);
      client.shutdown();

      server.join();
    }

  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class ClientTask1
    extends Thread
  {
    public 
    ClientTask1
    (
     long seed, 
     NodeMod mod
    ) 
    {
      pSeed    = seed;
      pNodeMod = mod;
    }

    public NodeMod
    getNodeMod()
    {
      return pNodeMod;
    }

    public void 
    run() 
    {
      Random random = new Random(pSeed);
      try {
	sleep(random.nextInt(2000));
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);
      
	client.register("default", pNodeMod);
	
	{
	  NodeMod mod = client.getWorkingVersion("default", pNodeMod.getName());
	  assert(pNodeMod.equals(mod));
	  pNodeMod = mod;
	}

	{
	  try {
	    NodeMod mod = client.getWorkingVersion("default", "/images/fooy");
	  }
	  catch(PipelineException ex) {
	    System.out.print("Caught: " + ex.getMessage() + "\n\n");
	  }
	}

	{
	  int cnt;
	  for(cnt=0; cnt<10; cnt++) {
	    FrameRange range = pNodeMod.getPrimarySequence().getFrameRange();
	    int f1 = range.getStart() + random.nextInt(10) - 5;
	    int f2 = range.getEnd() + random.nextInt(10) - 5;
	    int s = Math.min(43, Math.max(0, Math.min(f1, f2)));
	    int e = Math.min(43, Math.max(0, Math.max(f1, f2)));
	    FrameRange range2 = new FrameRange(s, e, range.getBy());
	    
	    pNodeMod.adjustFrameRange(range2);	    
	    client.modifyProperties("default", pNodeMod);
	  }
	  
	  {
	    NodeMod mod = client.getWorkingVersion("default", pNodeMod.getName());
	    assert(pNodeMod.equals(mod));
	    pNodeMod = mod;
	  }
	}



	// ...
      


	client.disconnect();
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }


    private long     pSeed; 
    private NodeMod  pNodeMod;
  }

  private 
  class ClientTask2
    extends Thread
  {
    public 
    ClientTask2
    (
     long seed, 
     NodeMod mod
    ) 
    {
      pSeed = seed;
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      Random random = new Random(pSeed);
      try {
	sleep(random.nextInt(2000));
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);
	
	{
	  int cnt;
	  for(cnt=0; cnt<10; cnt++) {	
	    NodeMod mod = client.getWorkingVersion("default", pNodeMod.getName());

	    assert(pNodeMod.equals(mod));
	    pNodeMod = mod;
	  }
	}


	// ...
      


	client.disconnect();
      }
      catch(Exception ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }

    private long     pSeed; 
    private String   pName;
    private NodeMod  pNodeMod;
  }


}

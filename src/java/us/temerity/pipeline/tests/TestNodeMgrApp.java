// $Id: TestNodeMgrApp.java,v 1.6 2004/03/31 02:01:18 jim Exp $

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
    Logs.ops.setLevel(Level.FINER);

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
    
    File prodDir = new File(cwd, "prod");
    prodDir.mkdirs();

    /* test nodes */ 
    NodeMod modA = null;
    {
      String name = "/images/normal";
      
      FileSeq primary = new FileSeq(new FilePattern("normal", 4, "tif"), 
				    new FrameRange(0, 43, 1));
    
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      secondary.add(new FileSeq(new FilePattern("primary", 4, "tif"), 
				new FrameRange(0, 43, 1)));
      
      modA = new NodeMod(name, primary, secondary, 
			TestInfo.sBuildToolset, null);
    }

    NodeMod modB = null;
    {
      String name = "/images/selected";
      
      FileSeq primary = new FileSeq(new FilePattern("selected", 4, "tif"), 
				    new FrameRange(0, 43, 1));
    
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      modB = new NodeMod(name, primary, secondary, 
			TestInfo.sBuildToolset, null);
    }

    NodeMod fly = null;
    {
      String name = "/animals/insects/fly";
      
      FileSeq primary = new FileSeq(new FilePattern("fly", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      fly = new NodeMod(name, primary, secondary, 
			TestInfo.sBuildToolset, null);
    }

    NodeMod dragonfly = null;
    {
      String name = "/animals/insects/dragonfly";
      
      FileSeq primary = new FileSeq(new FilePattern("dragonfly", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      dragonfly = new NodeMod(name, primary, secondary, 
			      TestInfo.sBuildToolset, null);
    }

    NodeMod frog = null;
    {
      String name = "/animals/amphibians/frog";
      
      FileSeq primary = new FileSeq(new FilePattern("frog", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      frog = new NodeMod(name, primary, secondary, 
			 TestInfo.sBuildToolset, null);
    }

    NodeMod salamander = null;
    {
      String name = "/animals/amphibians/salamander";
      
      FileSeq primary = new FileSeq(new FilePattern("salamander", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      salamander = new NodeMod(name, primary, secondary, 
			       TestInfo.sBuildToolset, null);
    }

    NodeMod snake = null;
    {
      String name = "/animals/reptiles/snake";
      
      FileSeq primary = new FileSeq(new FilePattern("snake", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      snake = new NodeMod(name, primary, secondary, 
			  TestInfo.sBuildToolset, null);
    }

    NodeMod sparrow = null;
    {
      String name = "/animals/birds/sparrow";
      
      FileSeq primary = new FileSeq(new FilePattern("sparrow", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      sparrow = new NodeMod(name, primary, secondary, 
			    TestInfo.sBuildToolset, null);
    }

    NodeMod eagle = null;
    {
      String name = "/animals/birds/eagle";
      
      FileSeq primary = new FileSeq(new FilePattern("eagle", "txt"), null);
      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      
      eagle = new NodeMod(name, primary, secondary, 
			  TestInfo.sBuildToolset, null);
    }

    LinkCatagory ref = new LinkCatagory("RenderReference", LinkPolicy.None);

    /* initialize data files */ 
    {
      File dir = new File(cwd, "prod/working/" + user + "/default/images");

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
      ArrayList<String> args = new ArrayList<String>();
      args.add("--recursive");
      args.add("animals");
      args.add(cwd + "/prod/working/" + user + "/default");

      SubProcess proc = new SubProcess("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }

    /* start the file manager server */ 
    FileMgrServer fileServer = new FileMgrServer(prodDir, 53138);
    fileServer.start();

    {
      /* start the node manager server */ 
      NodeMgrServer nodeServer = new NodeMgrServer(nodeDir, 53139, "localhost", 53138);
      nodeServer.start();
      
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
      client.link("default", modA.getName(), modB.getName(), 
		  ref, LinkRelationship.None, null);
      modA = client.getWorkingVersion("default", modA.getName());
      client.shutdown();
      
      nodeServer.join();
    }

    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--force");
      args.add("--recursive");
      args.add("downstream");
      
      SubProcess proc = 
	new SubProcess("RemoveDownstreamLinks", "rm", args, env, nodeDir);
      proc.start();
      
      proc.join();
    }

    {
      /* start the node manager server */ 
      NodeMgrServer nodeServer = new NodeMgrServer(nodeDir, 53139, "localhost", 53138);
      nodeServer.start();
      
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

      {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);
	client.register("default", fly);
	client.register("default", dragonfly);
	client.disconnect();
      }

      {
	ClientTask3 clientA = 
	  new ClientTask3(433, snake, frog, fly, dragonfly, salamander);
	clientA.start();
	
	ClientTask4 clientB = 
	  new ClientTask4(153, eagle, sparrow, fly, dragonfly);
	clientB.start();
	
	clientA.join();
	clientB.join();
      }


      {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);

	client.link("default", eagle.getName(), snake.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.revoke("default", dragonfly.getName(), true);
	
	client.rename("default", sparrow.getName(), "/animals/mammal/bat", true);

	client.disconnect();
      }
      
      NodeMgrClient client = new NodeMgrClient("localhost", 53139);
      client.shutdown();

      nodeServer.join();
    }

    /* shutdown the file manager server */ 
    {
      FileMgrClient client = new FileMgrClient("localhost", 53138);
      client.shutdown();
      
      fileServer.join();
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

  private 
  class ClientTask3
    extends Thread
  {
    public 
    ClientTask3
    (
     long seed,   
     NodeMod snake, 
     NodeMod frog, 
     NodeMod fly, 
     NodeMod dragonfly, 
     NodeMod salamander
    ) 
    {
      pSeed = seed;
      pSnake = snake;
      pFrog = frog;
      pFly = fly;
      pDragonfly = dragonfly;
      pSalamander = salamander;
    }

    public void 
    run() 
    {
      Random random = new Random(pSeed);
      try {
	sleep(random.nextInt(1000));
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);

	client.register("default", pSalamander);
	client.register("default", pFrog);
	client.register("default", pSnake);

	client.link("default", pSnake.getName(), pFrog.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.link("default", pFrog.getName(), pDragonfly.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.link("default", pSnake.getName(), pSalamander.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.link("default", pFrog.getName(), pFly.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);
	
	client.link("default", pDragonfly.getName(), pFly.getName(),
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);


	client.unlink("default", pSnake.getName(), pSalamander.getName()); 

	try {
	  client.unlink("default", pSnake.getName(), pSalamander.getName());
	}
	catch(PipelineException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n\n");
	}

	client.link("default", pSnake.getName(), pSalamander.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.disconnect();
      }
      catch(Exception ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }

    private long     pSeed; 
    private NodeMod  pSnake;
    private NodeMod  pFrog;
    private NodeMod  pFly;
    private NodeMod  pDragonfly;
    private NodeMod  pSalamander;
  }


  private 
  class ClientTask4
    extends Thread
  {
    public 
    ClientTask4
    (
     long seed,
     NodeMod eagle, 
     NodeMod sparrow, 
     NodeMod fly, 
     NodeMod dragonfly
    ) 
    {
      pSeed = seed;
      pEagle = eagle;
      pSparrow = sparrow;
      pFly = fly;
      pDragonfly = dragonfly;
    }

    public void 
    run() 
    {
      Random random = new Random(pSeed);
      try {
	sleep(random.nextInt(1000));
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	NodeMgrClient client = new NodeMgrClient("localhost", 53139);

	client.register("default", pSparrow);
	client.register("default", pEagle);

	client.link("default", pEagle.getName(), pSparrow.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.link("default", pSparrow.getName(), pDragonfly.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	client.link("default", pSparrow.getName(), pFly.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	try {
	  client.link("default", pFly.getName(), pFly.getName(), 
		      new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);
	} 
	catch(PipelineException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n\n");
	}

	try {
	  client.link("default", pFly.getName(), pEagle.getName(), 
		      new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);
	} 
	catch(PipelineException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n\n");
	}

	client.disconnect();
      }
      catch(Exception ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }

    private long     pSeed; 
    private NodeMod  pEagle;
    private NodeMod  pSparrow;
    private NodeMod  pFly;
    private NodeMod  pDragonfly;
  }


}

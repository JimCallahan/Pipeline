// $Id: TestNodeMgr2App.java,v 1.2 2004/05/03 04:31:01 jim Exp $

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
class TestNodeMgr2App
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
    Logs.sub.setLevel(Level.FINEST);
    Logs.ops.setLevel(Level.FINEST);

    try {
      TestNodeMgr2App app = new TestNodeMgr2App();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
    throws InterruptedException, PipelineException, GlueException, IOException
  { 
    /* common stuff */ 
    Map<String,String> env = System.getenv();
    File cwd = new File(System.getProperty("user.dir") + "/data");
    String user = System.getProperty("user.name");

    /* initialize data files */ 
    File nodeDir = PackageInfo.sNodeDir;    
    File prodDir = PackageInfo.sProdDir;

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
      File dir = new File(prodDir, "working/" + user + "/default/images");

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
      args.add(prodDir + "/working/" + user + "/default");

      SubProcess proc = new SubProcess("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }

    {
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
      
      NodeMgrClient client = new NodeMgrClient("localhost", 53135);

      printStatus(client.status("default", modA.getName()));
      printStatus(client.status("default", modB.getName()));

      printStatus(client.status("default", modA.getName()));
      printStatus(client.status("default", modB.getName()));

      client.link("default", modA.getName(), modB.getName(), 
		  ref, LinkRelationship.None, null);
      modA = client.getWorkingVersion("default", modA.getName());

      printStatus(client.status("default", modA.getName()));
      printStatus(client.status("default", modA.getName()));

      /* modify the directory where modA and modB live to see of plnotify(1) notices */ 
      {
	File dir = new File(prodDir, "working/jim/default/images");
	int wk;
	for(wk=0; wk<5; wk++) {
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}
      }

      printStatus(client.status("default", modA.getName()));
      printStatus(client.status("default", modB.getName()));

      printStatus(client.status("default", modA.getName()));
      printStatus(client.status("default", modB.getName()));

      printStatus(client.checkIn("default", modA.getName(), "Initial revision.", null));    

      printStatus(client.status("default", modA.getName()));

      modA = client.getWorkingVersion("default", modA.getName());
      modB = client.getWorkingVersion("default", modB.getName());

      client.disconnect();
    }

    {
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
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);

	client.checkOut("modeling", modA.getName(), null, false);
	printStatus(client.status("modeling", modA.getName()));

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
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);

	printStatus(client.status("default", eagle.getName()));
	printStatus(client.status("default", eagle.getName()));

	client.link("default", eagle.getName(), snake.getName(), 
		    new LinkCatagory("Eats", LinkPolicy.Both), LinkRelationship.All, null);

	//client.revoke("default", dragonfly.getName(), true);
	
	client.rename("default", sparrow.getName(), "/animals/mammal/bat", true);

	//client.revoke("default", fly.getName(), true);

	client.disconnect();
      }
   
      /* modify the some of the animal directories */ 
      NodeMgrClient client = new NodeMgrClient("localhost", 53135);

      {
	File animals = new File(prodDir, "working/jim/default/animals/");

	{
	  File dir = new File(animals, "insects");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status("default", eagle.getName()));

	{
	  File dir = new File(animals, "amphibians");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status("default", eagle.getName()));

	{
	  File dir = new File(animals, "reptiles");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status("default", eagle.getName()));

	{
	  File dir = new File(animals, "birds");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}
      }

      printStatus(client.status("default", eagle.getName()));
      printStatus(client.status("default", fly.getName()));
      printStatus(client.status("default", "/animals/mammal/bat"));
      printStatus(client.status("default", frog.getName()));

      //client.shutdown();
      client.disconnect();
    }
  }

  
  public void 
  printStatus
  (
   NodeStatus status
  ) 
    throws GlueException
  {
    StringBuffer buf = new StringBuffer();

    buf.append("-----------------------------------------------------------------------\n" +
	       "  N O D E   S T A T U S: " + status.getNodeID() + "\n" +
	       "-----------------------------------------------------------------------\n");

    printStatusShortHelper(status, 1, buf);

    buf.append("-----------------------------------------------------------------------\n");

    printStatusHelper(status, 1, buf);

    buf.append("-----------------------------------------------------------------------\n");

    System.out.print(buf.toString());
  }

  

  private void 
  printStatusShortHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    printStatusShortDownstreamHelper(status, level, buf);

    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append("---\n");

    printStatusShortUpstreamHelper(status, level, buf);
  }

  private void 
  printStatusShortDownstreamHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    for(NodeStatus tstatus : status.getTargets()) 
      printStatusShortDownstreamHelper(tstatus, level+1, buf);

    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append(status.getNodeID().getName() + "\n");
  }
  
  private void 
  printStatusShortUpstreamHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    buf.append("->");
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    buf.append(status.getNodeID().getName() + "\n");
    
    for(NodeStatus sstatus : status.getSources()) 
      printStatusShortUpstreamHelper(sstatus, level+1, buf);
  }
  

  private void 
  printStatusHelper
  (
   NodeStatus status,
   int level, 
   StringBuffer buf
  ) 
    throws GlueException
  {
    String indent = null;
    {
      StringBuffer ibuf = new StringBuffer();
      ibuf.append("->");
      int wk;
      for(wk=0; wk<level; wk++) 
 	ibuf.append("  ");
      indent = ibuf.toString();
    }
    String indent2 = (indent + "  ");

    
    buf.append(indent + "NodeStatus {\n" +
 	       indent2 + "NodeID = " + status.getNodeID() + "\n");

    NodeDetails details = status.getDetails();
    if(details != null) {
      buf.append(indent2 + "TimeStamp = " + details.getTimeStamp() + "\n");
    
      printGlue("WorkingVersion", details.getWorkingVersion(), indent, buf);
      printGlue("BaseVersion", details.getBaseVersion(), indent, buf);
      printGlue("LatestVersion", details.getLatestVersion(), indent, buf);
      
      buf.append(indent2 + "OverallNodeState = " + details.getOverallNodeState() + "\n" +
		 indent2 + "OverallQueueState = " + details.getOverallQueueState() + "\n" +
		 indent2 + "VersionState = " + details.getVersionState() + "\n" +
		 indent2 + "PropertyState = " + details.getPropertyState() + "\n" +
		 indent2 + "LinkState = " + details.getLinkState() + "\n");
      
      {
	buf.append(indent2 + "FileStates = {\n");
	for(FileSeq fseq : details.getFileStateSequences()) {
	  buf.append(indent2 + "  " + fseq + " = {\n");
	  FileState fs[] = details.getFileState(fseq);
	  Date ts[] = details.getFileTimeStamps(fseq);
	  
	  int wk = 0;
	  for(File file : fseq.getFiles()) {
	    buf.append(indent2 + "    [" + wk + "]: " + file + " = " + 
		       fs[wk] + "  (" + (ts[wk].getTime()) + ")\n");
	    wk++;
	  } 
	  
	  buf.append(indent2 + "  }\n");
	}
	buf.append(indent2 + "}\n");
      }
      
      {
	buf.append(indent2 + "QueueStates = Not yet...\n");
      }
    }
      
    buf.append(indent2 + "Targets = {\n");
    for(NodeStatus tstatus : status.getTargets()) 
      buf.append(indent2 + "  " + tstatus.getNodeID() + "\n");
    buf.append(indent2 + "}\n");
    
    buf.append(indent2 + "Sources = {\n");
    for(NodeStatus sstatus : status.getSources()) {
      printStatusHelper(sstatus, level+2, buf);
    }
    
    buf.append(indent2 + "}\n" +
	       indent + "}\n");
  }

  private void 
  printGlue
  (
   String title, 
   Glueable obj, 
   String indent,
   StringBuffer buf
  ) 
    throws GlueException
  {
    if(obj == null) {
      buf.append(indent + "  " + title + " = null\n");
      return;
    }

    GlueEncoder ge = new GlueEncoder(title, obj);
    String text = ge.getText();
    String lines[] = text.split("\n");
    int wk;
    for(wk=0; wk<lines.length; wk++) 
      buf.append(indent + "  " + lines[wk] + "\n");
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
      try {
	Random random = new Random(pSeed);
	sleep(random.nextInt(2000));
	
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);
	
	client.register("default", pNodeMod);
	
	{
	  NodeMod mod = client.getWorkingVersion("default", pNodeMod.getName());
	  if(!pNodeMod.equals(mod)) 
	    throw new PipelineException("Assert Failure!");
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
	    if(!pNodeMod.equals(mod)) 
	      throw new PipelineException("Assert Failure!");
	    pNodeMod = mod;
	  }
	}
	
	client.disconnect();
      } 
      catch (Exception ex) {
	ex.printStackTrace();
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
      try {
	Random random = new Random(pSeed);
	sleep(random.nextInt(2000));
	
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);
	
	{
	  int cnt;
	  for(cnt=0; cnt<10; cnt++) {	
	    NodeMod mod = client.getWorkingVersion("default", pNodeMod.getName());
	    if(!pNodeMod.equals(mod)) 
	      throw new PipelineException("Assert Failure!");
	    pNodeMod = mod;
	  }
	}
	
	client.disconnect();
      }
      catch (Exception ex) {
	ex.printStackTrace();
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
     
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);

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
      catch (Exception ex) {
	ex.printStackTrace();
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
    
	NodeMgrClient client = new NodeMgrClient("localhost", 53135);

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
      catch (Exception ex) {
	ex.printStackTrace();
      } 
    }

    private long     pSeed; 
    private NodeMod  pEagle;
    private NodeMod  pSparrow;
    private NodeMod  pFly;
    private NodeMod  pDragonfly;
  }


}

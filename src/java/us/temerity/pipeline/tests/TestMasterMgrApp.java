// $Id: TestMasterMgrApp.java,v 1.8 2005/01/22 01:36:36 jim Exp $

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
class TestMasterMgrApp
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
    
    Logs.net.setLevel(Level.FINEST);
    Logs.sub.setLevel(Level.FINER);
    Logs.ops.setLevel(Level.FINEST);

    try {
      TestMasterMgrApp app = new TestMasterMgrApp();
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
    String author = PackageInfo.sUser;
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
	
      SubProcessLight proc = new SubProcessLight("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }
    
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--recursive");
      args.add("animals");
      args.add(cwd + "/prod/working/" + user + "/default");

      SubProcessLight proc = new SubProcessLight("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }

    {
      /* start the file manager daemon */ 
      FileMgrServer fileServer = new FileMgrServer(prodDir, 53146);
      fileServer.start();
     
      /* give the servers a chance to start */ 
      Thread.currentThread().sleep(1000);

      /* start the node manager daemon */ 
      MasterMgrServer nodeServer = 
	new MasterMgrServer(nodeDir, 53145, 
			    prodDir, "localhost", 53146, 
			    "localhost", 53149);
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
      
      MasterMgrClient client = new MasterMgrClient("localhost", 53145);

      printStatus(client.status(author, "default", modA.getName(), false));
      printStatus(client.status(author, "default", modB.getName(), false));

      printStatus(client.status(author, "default", modA.getName(), false));
      printStatus(client.status(author, "default", modB.getName(), false));

      client.link(author, "default", modA.getName(), modB.getName(), 
		  LinkPolicy.Dependency, LinkRelationship.All, null);
      modA = client.getWorkingVersion(author, "default", modA.getName());

      printStatus(client.status(author, "default", modA.getName(), false));
      printStatus(client.status(author, "default", modA.getName(), false));

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

      printStatus(client.status(author, "default", modA.getName(), false));
      printStatus(client.status(author, "default", modB.getName(), false));

      printStatus(client.status(author, "default", modA.getName(), false));
      printStatus(client.status(author, "default", modB.getName(), false));

      client.checkIn(author, "default", modA.getName(), 
		     "Initial revision.", null);    

      printStatus(client.status(author, "default", modA.getName(), false));

      modA = client.getWorkingVersion(author, "default", modA.getName());
      modB = client.getWorkingVersion(author, "default", modB.getName());

      client.shutdown();

      /* wait for everything to shutdown */ 
      nodeServer.join();
      fileServer.join();
    }

    /* destroy the downstream links to see if the node manager will 
         automatically rebuild them... */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--force");
      args.add("--recursive");
      args.add("downstream");
      
      SubProcessLight proc = 
 	new SubProcessLight("RemoveDownstreamLinks", "rm", args, env, nodeDir);
      proc.start();
      
      proc.join();
    }

    {
      /* start the file manager daemon */ 
      FileMgrServer fileServer = new FileMgrServer(prodDir, 53146);
      fileServer.start();
      
      /* give the servers a chance to start */ 
      Thread.currentThread().sleep(1000);
      /* start the node manager server */ 
      MasterMgrServer nodeServer = 
	new MasterMgrServer(nodeDir, 53145, 
			    prodDir, "localhost", 53146, 
			    "localhost", 53149);
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
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);
	client.register(author, "default", fly);
	client.register(author, "default", dragonfly);
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
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);

	printStatus(client.status(author, "default", eagle.getName(), false));
	printStatus(client.status(author, "default", eagle.getName(), false));

	client.link(author, "default", eagle.getName(), snake.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	//client.revoke(author, "default", dragonfly.getName(), true);
	
	client.rename(author, "default", sparrow.getName(), "/animals/mammal/bat", true);

	//client.revoke(author, "default", fly.getName(), true);

	client.disconnect();
      }
   
      /* modify the some of the animal directories */ 
      MasterMgrClient client = new MasterMgrClient("localhost", 53145);

      {
	File animals = new File(prodDir, "working/jim/default/animals/");

	{
	  File dir = new File(animals, "insects");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status(author, "default", eagle.getName(), false));

	{
	  File dir = new File(animals, "amphibians");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status(author, "default", eagle.getName(), false));

	{
	  File dir = new File(animals, "reptiles");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}

	printStatus(client.status(author, "default", eagle.getName(), false));

	{
	  File dir = new File(animals, "birds");
	  File tmp = File.createTempFile("dummy", "test", dir);
	  System.out.print("Touch: " + tmp + "\n");
	  Thread.currentThread().sleep(2000);
	}
      }

      printStatus(client.status(author, "default", eagle.getName(), false));
      printStatus(client.status(author, "default", fly.getName(), false));
      printStatus(client.status(author, "default", "/animals/mammal/bat", false));
      printStatus(client.status(author, "default", frog.getName(), false));

      {
	TreeSet<String> paths = new TreeSet<String>();
	paths.add(eagle.getName());
	printNodePath(client.updatePaths("jim", "default", paths));
      }
      
      {
	TreeSet<String> paths = new TreeSet<String>();
	paths.add("/images");
	paths.add("/animals/fungus");
	printNodePath(client.updatePaths("jim", "default", paths));
      }
	
      {
	TreeSet<String> paths = new TreeSet<String>();
	paths.add("/");
	printNodePath(client.updatePaths("jim", "default", paths));
      }	  

      {
	TreeSet<String> paths = new TreeSet<String>();
	paths.add("/animals/insects");
	printNodePath(client.updatePaths("jim", "fooy", paths));
      } 

      client.shutdown();

      /* wait for everything to shutdown */ 
      nodeServer.join();
      fileServer.join();
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
	Date ts[] = details.getFileTimeStamps();
	for(FileSeq fseq : details.getFileStateSequences()) {
	  buf.append(indent2 + "  " + fseq + " = {\n");
	  FileState fs[] = details.getFileState(fseq);
	  
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

    GlueEncoder ge = new GlueEncoderImpl(title, obj);
    String text = ge.getText();
    String lines[] = text.split("\n");
    int wk;
    for(wk=0; wk<lines.length; wk++) 
      buf.append(indent + "  " + lines[wk] + "\n");
  }



  private void 
  printNodePath
  (
   NodeTreeComp rootComp
  ) 
  {
    StringBuffer buf = new StringBuffer();
    buf.append("Node Components:\n"); 
    printNodePathHelper(rootComp, 1, buf);
    System.out.print(buf.toString());
  }

  private void 
  printNodePathHelper
  (
   NodeTreeComp comp, 
   int level, 
   StringBuffer buf
  ) 
  {
    String istr = null;
    {
      StringBuffer ibuf = new StringBuffer();
      int wk;
      for(wk=0; wk<level; wk++) 
	ibuf.append("  ");
      istr = ibuf.toString();
    }

    buf.append(istr + "[" + comp.getName() + "]\n");

    if(comp.getState() != NodeTreeComp.State.Branch) {
      buf.append(istr + "  " + comp.getState() + "\n");
    }
    else {
      for(NodeTreeComp child : comp.values()) 
	printNodePathHelper(child, level+1, buf);
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
      try {
	Random random = new Random(pSeed);
	sleep(random.nextInt(2000));
	
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);
	
	String author = PackageInfo.sUser;

	client.register(author, "default", pNodeMod);
	
	{
	  NodeMod mod = client.getWorkingVersion(author, "default", pNodeMod.getName());
	  if(!pNodeMod.equals(mod)) 
	    throw new PipelineException("Assert Failure!");
	  pNodeMod = mod;
	}
	
	{
	  try {
	    NodeMod mod = client.getWorkingVersion(author, "default", "/images/fooy");
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
	    client.modifyProperties(author, "default", pNodeMod);
	  }
	  
	  {
	    NodeMod mod = client.getWorkingVersion(author, "default", pNodeMod.getName());
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
	
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);
	
	String author = PackageInfo.sUser;
		
	{
	  int cnt;
	  for(cnt=0; cnt<10; cnt++) {	
	    NodeMod mod = client.getWorkingVersion(author, "default", pNodeMod.getName());
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
     
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);

	String author = PackageInfo.sUser; 

	client.register(author, "default", pSalamander);
	client.register(author, "default", pFrog);
	client.register(author, "default", pSnake);

	client.link(author, "default", pSnake.getName(), pFrog.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	client.link(author, "default", pFrog.getName(), pDragonfly.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	client.link(author, "default", pSnake.getName(), pSalamander.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	client.link(author, "default", pFrog.getName(), pFly.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);
	
	client.link(author, "default", pDragonfly.getName(), pFly.getName(),
		    LinkPolicy.Dependency, LinkRelationship.All, null);


	client.unlink(author, "default", pSnake.getName(), pSalamander.getName()); 

	try {
	  client.unlink(author, "default", pSnake.getName(), pSalamander.getName());
	}
	catch(PipelineException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n\n");
	}

	client.link(author, "default", pSnake.getName(), pSalamander.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

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
    
	MasterMgrClient client = new MasterMgrClient("localhost", 53145);

	String author = PackageInfo.sUser; 

	client.register(author, "default", pSparrow);
	client.register(author, "default", pEagle);

	client.link(author, "default", pEagle.getName(), pSparrow.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	client.link(author, "default", pSparrow.getName(), pDragonfly.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	client.link(author, "default", pSparrow.getName(), pFly.getName(), 
		    LinkPolicy.Dependency, LinkRelationship.All, null);

	try {
	  client.link(author, "default", pFly.getName(), pFly.getName(), 
		      LinkPolicy.Dependency, LinkRelationship.All, null);
	} 
	catch(PipelineException ex) {
	  System.out.print("Caught: " + ex.getMessage() + "\n\n");
	}

	try {
	  client.link(author, "default", pFly.getName(), pEagle.getName(), 
		      LinkPolicy.Dependency, LinkRelationship.All, null);
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

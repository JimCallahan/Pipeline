// $Id: TestFileMgrApp.java,v 1.2 2004/03/12 23:11:43 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   F I L E   M G R                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestFileMgrApp
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
    Logs.sub.setLevel(Level.FINE);
    //Logs.sum.setLevel(Level.FINER);

    try {
      TestFileMgrApp app = new TestFileMgrApp();
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
    throws InterruptedException, PipelineException
  { 
    /* common stuff */ 
    Map<String,String> env = System.getenv();
    File cwd = new File(System.getProperty("user.dir") + "/data");
    String user = System.getProperty("user.name");

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
			TestInfo.sBuildToolset, null, 
			null, null, false, false, 0);
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
			TestInfo.sBuildToolset, null, 
			null, null, false, false, 0);
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
    
    /* start the file manager server */ 
    FileMgrServer server = new FileMgrServer(prod, 53138);
    server.start();

    /* give the server a chance to start */ 
    Thread.currentThread().sleep(2000);

    /* run some client tasks */ 
    {
      ArrayList<ClientTask> clients = new ArrayList<ClientTask>();
      {
	long time = (new Date()).getTime();
	
	int wk; 
	for(wk=0; wk<10; wk++) {
	  ClientTask client = new ClientTask(time + wk, idA, modA);
	  clients.add(client);
	}
	
	{
	  ClientTask client = new ClientTask(0, idB, modB);
	  clients.add(client);	
	}
      }
      
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
     long seed, 
     NodeID id, 
     NodeMod mod
    ) 
    {
      pSeed    = seed;
      pNodeID  = id;
      pNodeMod = mod;
    }

    public void 
    run() 
    {
      Random random = new Random(pSeed);
      int sleep = random.nextInt(1000);

      System.out.print(getName() + ": sleeping for " + sleep + " msecs...\n");
      try {
	sleep(sleep);
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	FileMgrClient client = new FileMgrClient("localhost", 53138);

	TreeMap<FileSeq, FileState[]> states = 
	  client.computeFileStates(pNodeID, pNodeMod, VersionState.Pending, null);
	{
	  StringBuffer buf = new StringBuffer(); 
	  buf.append(pNodeID + ":\n");
	  
	  for(FileSeq fseq : states.keySet()) {
	    buf.append("  " + fseq + ":\n");

	    FileState fs[] = states.get(fseq);
	    int wk;
	    for(wk=0; wk<fs.length; wk++) 
	      buf.append("    " + wk + ": " + fs[wk].name() + "\n");
	  }

	  System.out.print(buf.toString());
	}

	client.refreshCheckSums(pNodeID, pNodeMod.getSequences());

	client.shutdown();
      }
      catch(PipelineException ex) {
	Logs.ops.severe(ex.getMessage());
      }
    }

    private long     pSeed; 
    private NodeID   pNodeID;
    private NodeMod  pNodeMod;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/



}

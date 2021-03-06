// $Id: TestFileMgr2App.java,v 1.9 2006/11/22 09:08:01 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.message.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   F I L E   M G R                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestFileMgr2App
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
    LogMgr.setLevel(LogMgr.Kind.Net, LogMgr.Level.Finest); 
    LogMgr.setLevel(LogMgr.Kind.Sub, LogMgr.Level.Finer); 

    try {
      TestFileMgr2App app = new TestFileMgr2App();
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
    //File prod = new File(cwd, "prod");
    File prod = PackageInfo.sProdDir;
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
	
      SubProcessLight proc = new SubProcessLight("CopyFiles", "cp", args, env, cwd);
      proc.start();
      proc.join();
    }
    
    /* start the file manager server */ 
//     FileMgrServer server = new FileMgrServer(prod, 53138);
//     server.start();

    /* give the server a chance to start */ 
    Thread.currentThread().sleep(2000);

    /* run some client tasks */ 
    {
      ArrayList<ClientTask> clients = new ArrayList<ClientTask>();
      {
  	{
  	  int wk; 
  	  for(wk=0; wk<10; wk++) {
  	    ClientTask clientA = new ClientTask((new Date()).getTime(), idA, modA);
  	    clients.add(clientA);

  	    ClientTask clientB = new ClientTask((new Date()).getTime(), idB, modB);
  	    clients.add(clientB);
  	  }
  	}

	{
 	  ClientTask clientA = new ClientTask(0, idB, modB);
 	  clients.add(clientA);	

 	  ClientTask clientB = new ClientTask(0, idA, modA);
 	  clients.add(clientB);	
	}
      }
      
      for(ClientTask client : clients) 
	client.start();

       for(ClientTask client : clients) 
 	client.join();
    }

    FileMgrClient client = new FileMgrClient("localhost", 53136);
    client.shutdown();
    
//     server.join();
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
      Map<String,String> env = System.getenv();
      File cwd = new File(System.getProperty("user.dir") + "/data");
      String user = System.getProperty("user.name");

      //File prod = new File(cwd, "prod");
      File prod = PackageInfo.sProdDir;
      File dir = new File(prod, "working/" + user + "/default/images");

      Random random = new Random(pSeed);
      try {
	sleep(random.nextInt(1000));
      }
      catch(InterruptedException ex) {
	assert(false);
      }

      try {
	FileMgrClient client = new FileMgrClient("localhost", 53136);

	int count = 5;

	if(pSeed == 0) {
	  ArrayList<NodeVersion> versions = new ArrayList<NodeVersion>();

	  /* create some checked-in in versions */ 
	  VersionID latest = null;
	  {
	    VersionID lvid = null;
	    int rk;
	    for(rk=0; rk<count; rk++) {
	      VersionID vid = new VersionID();
	      VersionState vstate = VersionState.Pending;
	      
	      if(lvid != null) {
		VersionID.Level levels[] = VersionID.Level.values();
		vid = new VersionID(lvid, levels[random.nextInt(levels.length)]);
		vstate = VersionState.Identical;
	      }
	      
	      {
		TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();
		TreeMap<FileSeq, Date[]> timestamps  = new TreeMap<FileSeq, Date[]>();
		client.states(pNodeID, pNodeMod, vstate, false, lvid, states, timestamps);
		printStates(states, timestamps);
		
		/* build the file novelty table */ 
		TreeMap<FileSeq,boolean[]> isNovel = new TreeMap<FileSeq,boolean[]>();
		for(FileSeq fseq : states.keySet()) {
		  FileState[] fs = states.get(fseq);
		  boolean flags[] = new boolean[fs.length];
		  
		  int wk;
		  for(wk=0; wk<fs.length; wk++) {
		    switch(fs[wk]) {
		    case Pending:
		    case Modified:
		    case Added:
		      flags[wk] = true;
		      break;
		      
		    case Identical:
		      flags[wk] = false;
		      break;
		      
		    default:
		      assert(false);
		    }
		  }

		  isNovel.put(fseq, flags);
		}

		client.checkIn(pNodeID, pNodeMod, vid, lvid, isNovel);

		TreeMap<String,VersionID> lvids = new TreeMap<String,VersionID>();
		NodeVersion vsn = 
		  new NodeVersion(pNodeMod, vid, lvids, isNovel, 
				  pNodeID.getAuthor(), "Some Message...", null, null);
		versions.add(vsn);
		
		pNodeMod = new NodeMod(vsn, vsn.getTimeStamp(), false);
	      }
	      
	      {
		FrameRange range = pNodeMod.getPrimarySequence().getFrameRange();
		int f1 = range.getStart() + random.nextInt(10) - 5;
		int f2 = range.getEnd() + random.nextInt(10) - 5;
		int s = Math.min(43, Math.max(0, Math.min(f1, f2)));
		int e = Math.min(43, Math.max(0, Math.max(f1, f2)));
		
		pNodeMod.adjustFrameRange(new FrameRange(s, e, range.getBy()));
	      }
	    
	      if(rk < (count-1)) {
		for(File file : pNodeMod.getPrimarySequence().getFiles()) {
		  if(random.nextInt(10) == 0) {
		    int size = random.nextInt(100) + 600;
		    
		    ArrayList<String> args = new ArrayList<String>();
		    args.add("-resize");
		    args.add(size + "x" + size);
		    args.add(file.getPath());
		    args.add(file.getPath());	      
		    
		    SubProcessLight proc = 
		      new SubProcessLight("ScaleImage", "convert", args, env, dir);
		    proc.start(); 
		    
		    try {
		    proc.join();
		    }
		    catch(InterruptedException ex) {
		      assert(false);
		    }
		  }
		}
	      }
	      
	      lvid = vid;
	    }

	    latest = lvid;
	  }

	  /* check-out the versions */ 
	  for(NodeVersion vsn : versions) {

	    VersionState vstate = VersionState.NeedsCheckOut;
	    if(latest.equals(vsn.getVersionID())) 
	      vstate = VersionState.Identical;

	    client.checkOut(pNodeID, vsn, false);
	    pNodeMod = new NodeMod(vsn, vsn.getTimeStamp(), false);

	    {
	      TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();
	      TreeMap<FileSeq, Date[]> timestamps  = new TreeMap<FileSeq, Date[]>();
	      client.states(pNodeID, pNodeMod, vstate, false, latest, states, timestamps);
	      printStates(states, timestamps);
	    }

	    try {
	      sleep(2000);
	    }
	    catch(InterruptedException ex) {
	      assert(false);
	    }

	    if(!vsn.getVersionID().equals(latest)) {
	      for(File file : pNodeMod.getPrimarySequence().getFiles()) {
		if(random.nextInt(10) == 0) {
		  int size = random.nextInt(100) + 600;
		  
		  ArrayList<String> args = new ArrayList<String>();
		  args.add("-resize");
		  args.add(size + "x" + size);
		  args.add(file.getPath());
		  args.add(file.getPath());	      
		  
		  SubProcessLight proc = 
		    new SubProcessLight("ScaleImage", "convert", args, env, dir);
		  proc.start(); 
		  
		  try {
		    proc.join();
		  }
		  catch(InterruptedException ex) {
		    assert(false);
		  }
		}
	      }
	    }

	    {
	      TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();
	      TreeMap<FileSeq, Date[]> timestamps  = new TreeMap<FileSeq, Date[]>();
	      client.states(pNodeID, pNodeMod, vstate, false, latest, states, timestamps);
	      printStates(states, timestamps);
	    }
	  }
	}
	else { 
	  int wk=0;
	  for(wk=0; wk<count; wk++) {
	    TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();
	    TreeMap<FileSeq, Date[]> timestamps  = new TreeMap<FileSeq, Date[]>();
	    client.states(pNodeID, pNodeMod, VersionState.Pending, false, 
			  null, states, timestamps);
	    printStates(states, timestamps);
	    
	    try {
	      sleep(1000 + random.nextInt(1000));
	    }
	    catch(InterruptedException ex) {
	      assert(false);
	    }
	  }
	}

	client.disconnect();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
(LogMgr.Kind.Ops, LogMgr.Level.Severe,
ex.getMessage());
      }
    }


    private void 
    printStates
    (
     TreeMap<FileSeq, FileState[]> states,
     TreeMap<FileSeq, Date[]> timestamps
    ) 
    {
      if(true) {
	StringBuilder buf = new StringBuilder(); 
	buf.append(pNodeID + ":\n");
	
	for(FileSeq fseq : states.keySet()) {
	  buf.append("  " + fseq + ":\n");
	  
	  FileState fs[] = states.get(fseq);
	  Date stamps[]  = timestamps.get(fseq);

	  int wk;
	  for(wk=0; wk<fs.length; wk++) {

	    buf.append("    " + fseq.getFrameRange().indexToFrame(wk) + ":\t");

	    if(stamps[wk] != null) 
	      buf.append("[" + stamps[wk].getTime() + "] \t");

	    buf.append(fs[wk].name() + "\n");
	  }
	}
	
	System.out.print(buf.toString());
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

// $Id: MineApp.java,v 1.2 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.apps.BaseApp; 
import us.temerity.pipeline.bootstrap.BootableApp; 
import us.temerity.pipeline.event.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.util.*;
import java.util.regex.*;

/*------------------------------------------------------------------------------------------*/
/*   M I N E   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Extracts useful information from node event and repository version files for datamining.
 */
public
class MineApp
  extends BaseApp
  implements BootableApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  MineApp() 
  {
    super("plmine");

    pNextNodeID = 1L;
    pNodeIDs = new TreeMap<String,Long>();

    pNextArtistID = 1L;
    pArtistIDs = new TreeMap<String,Long>();

    pArtistWorkWriters  = new TreeMap<Long,BufferedWriter>();
    pNumEditEvents = 0;

    pNodeVersionWriters = new TreeMap<Long,BufferedWriter>();
    pNumEditedVersions = 0;

    pNodeVersions = new DoubleMap<String,VersionID,NodeVersion>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B O O T A B L E   A P P                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public
  void 
  run
  (
   String[] args
  )
  {
    packageArguments(args);

    /* parse the command line */ 
    boolean success = false;
    try {   
      MineOptsParser parser = new MineOptsParser(getPackagedArgsReader()); 
      parser.setApp(this);
      parser.CommandLine();   

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch (PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      PluginMgrClient pclient = PluginMgrClient.getInstance();
      if(pclient != null) 
	pclient.disconnect();

      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Extract data suitable for datamining from all event and repository files.
   * 
   * @param timeZone
   *   The time zone where the nodes where created.
   * 
   * @param nodePattern
   *   The regular expression used to limit the names of nodes considered or 
   *   <CODE>null</CODE> to consider all nodes.
   *
   * @param databasePath
   *   The file system path to the root directory where the node/event database files to 
   *   datamine reside.  Should contain "repository" and "event" subdirectories.
   * 
   * @param outputPut
   *   The file system path to the root directory where the extracted data files will
   *   be written.  The files will be named with a 8-digit number representing the day 
   *   in YYYYMMDD format extracted for the given day. 
   */ 
  public void 
  extract
  (
   TimeZone timeZone, 
   String nodePattern,
   Path databasePath, 
   Path outputPath
  ) 
    throws PipelineException
  { 
    pTimeZone = timeZone;

    pNodePattern = null;
    if(nodePattern != null) 
      pNodePattern = Pattern.compile(nodePattern);

    pRepoPath = new Path(databasePath, "repository"); 
    if(!pRepoPath.toFile().isDirectory())
      throw new PipelineException
	("The root repository directory (" + pRepoPath + ") does not exist!");

    pEventPath = new Path(databasePath, "events/nodes"); 
    if(!pEventPath.toFile().isDirectory())
      throw new PipelineException
	("The root node event directory (" + pEventPath + ") does not exist!");

    pOutputPath = outputPath;  
    {
      File dir = pOutputPath.toFile();
      if(!dir.exists()) {
        if(!dir.mkdirs()) 
          throw new PipelineException
            ("Unable to create find or create the output directory (" + pOutputPath + ")!"); 
      }
    }

    /* process the matching node events */ 
    processNodeEvents();

    /* process node versions for all edited nodes */ 
    processNodeVersions();

    /* write out artist name/ID mappings */ 
    writeArtistIndex();

    /* write out node name/ID mappings */ 
    writeNodeIndex();
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique node ID for a given node name.
   */ 
  public long
  getNodeID
  (
   String name
  ) 
  {
    Long nodeID = pNodeIDs.get(name);
    if(nodeID == null) {
      nodeID = pNextNodeID++;
      pNodeIDs.put(name, nodeID);
    }

    return nodeID;
  }

  /**
   * Get the unique artist ID for a given artist name.
   */ 
  public long
  getArtistID
  (
   String name
  ) 
  {
    Long artistID = pArtistIDs.get(name);
    if(artistID == null) {
      artistID = pNextArtistID++;
      pArtistIDs.put(name, artistID);
    }

    return artistID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I / O  H E L P E R S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  private void 
  processNodeEvents()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Processing Node Events...");
    LogMgr.getInstance().flush();

    processNodeEventDir(new Path("/"));
    
    for(Long stamp : pArtistWorkWriters.keySet()) {
      try {
        BufferedWriter writer = pArtistWorkWriters.get(stamp);
        writer.close();
      }
      catch(IOException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to cleanly close artist work file for the day starting on " +
           "(" + stamp + "):\n  " + 
           ex.getMessage());
      }
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Found (" + pNumEditEvents + ") edit events.\n" + 
       "  Processed in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }

  /**
   * Recursively process all node event files.
   */   
  private void 
  processNodeEventDir
  (
   Path nodePath
  ) 
    throws PipelineException
  {
    Path path = new Path(pEventPath, nodePath);
    File current = path.toFile();
    if(!current.isDirectory()) 
      return;

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest,
       "Scanning Node Event Directory: " + path); 

    TreeSet<String> snames = new TreeSet<String>();
    boolean allDirs = true;
    {
      File files[] = current.listFiles(); 
      int wk;
      for(wk=0; wk<files.length; wk++) {
        if(!files[wk].isDirectory()) 
          allDirs = false;
        snames.add(files[wk].getName());
      }
    }
    
    if(allDirs) {
      for(String sname : snames) 
        processNodeEventDir(new Path(nodePath, sname));
    }
    else {
      String nodeName = nodePath.toString();
      if((pNodePattern != null) && !pNodePattern.matcher(nodeName).matches())
        return;

      /* make sure there are versions for this node */ 
      if(getNodeVersions(nodeName) == null) 
        return;

      /* time spend (msec) working on the current node during a particular day, 
           indexed by day start stamp and artistID */ 
      DoubleMap<Long,Long,Long> dailyWorkPerArtist = new DoubleMap<Long,Long,Long>();

      /* process events */
      Long nodeID = null;
      for(String sname : snames) {

        /* read the event file */ 
        BaseNodeEvent event = null;
        {
          Path epath = new Path(path, sname);
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Finest,
             "Reading Node Event File: " + epath); 
          
          try {
            event = (BaseNodeEvent) GlueDecoderImpl.decodeFile("Event", epath.toFile());
          }	
          catch(GlueException ex) {
            LogMgr.getInstance().log
              (LogMgr.Kind.Ops, LogMgr.Level.Warning,
               "Unable to read Node Event file: " + epath + "\n" + 
               "  " + ex.getMessage());
          }
        }

        /* only process it if its been edited */ 
        if((event != null) && (event instanceof EditedNodeEvent)) { 
          EditedNodeEvent e = (EditedNodeEvent) event;
          pNumEditEvents++;

          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Finer,
             "Found Edit Event: " + e.getAuthor() + " " + e.getNodeName()); 

          TimeInterval interval = e.getInterval();
          if(interval != null) {
            if(nodeID == null)
              nodeID = getNodeID(nodeName);
  
            String artist = e.getImposter(); 
            if(artist == null) 
              artist = e.getAuthor();
            long artistID = getArtistID(artist); 
            
            Calendar cal = new GregorianCalendar(pTimeZone);
            cal.setTimeInMillis(e.getTimeStamp());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currentDayStart = cal.getTimeInMillis();
            cal.add(Calendar.DATE, 1);
            long nextDayStart = cal.getTimeInMillis();
          
            while(true) {
              long minStamp = Math.max(currentDayStart, e.getTimeStamp());
              long maxStamp = Math.min(nextDayStart, e.getFinishedStamp());
              long dailyTotal = maxStamp - minStamp;
              if(dailyTotal > 0) {
                Long prevTotal = dailyWorkPerArtist.get(currentDayStart, artistID);
                if(prevTotal != null) 
                  dailyTotal += prevTotal;
                dailyWorkPerArtist.put(currentDayStart, artistID, dailyTotal);
              }
              
              if(e.getFinishedStamp() < nextDayStart)
                break;

              currentDayStart = nextDayStart;
              cal.add(Calendar.DATE, 1);
              nextDayStart = cal.getTimeInMillis();
            }
          }
        }
      }

      try {
        for(Long stamp : dailyWorkPerArtist.keySet()) {
          BufferedWriter out = getArtistWorkWriters(stamp);
          out.write(nodeID + "\t");
        
          boolean first = true;
          for(Long artistID : dailyWorkPerArtist.keySet(stamp)) {
            if(!first) 
              out.write(",");
            first = false;
            
            out.write(artistID + ":" + dailyWorkPerArtist.get(stamp, artistID));
          }
          
          out.write("\n");
        }
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write daily aritst work to file:\n  " + 
           ex.getMessage());
      }
    }
  }

  /**
   * Lookup or open the buffered file writer for the work done by artists on a given day.
   * 
   * @param stamp
   *   The timestamp of the start of the day.
   */ 
  private BufferedWriter
  getArtistWorkWriters
  (
   Long stamp
  ) 
    throws PipelineException
  {
    BufferedWriter out = pArtistWorkWriters.get(stamp);
    if(out == null) {
      Path dpath = new Path(pOutputPath, "ArtistWork");
      File dir = dpath.toFile();
      if(!dir.exists()) {
        if(!dir.mkdirs()) 
          throw new PipelineException
            ("Unable to create find or create the directory (" + dpath + ") where the " + 
             "daily artist work files will be written!");
      }

      Calendar cal = new GregorianCalendar(pTimeZone);
      cal.setTimeInMillis(stamp);
      
      String fname = String.format("%1$4d%2$02d%3$02d-%4$d", 
                                   cal.get(Calendar.YEAR), 
                                   cal.get(Calendar.MONTH), 
                                   cal.get(Calendar.DAY_OF_MONTH), 
                                   stamp);

      Path path = new Path(dpath, fname);
      
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Finer,
         "Writing Artist Work in: " + fname);
      
      try {
        out = new BufferedWriter(new FileWriter(path.toFile()));
        pArtistWorkWriters.put(stamp, out);
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to open daily artist work file (" + path + "):\n  " + 
           ex.getMessage());
      }
    }

    return out;
  }

     
  /*----------------------------------------------------------------------------------------*/

  /**
   * Extract data from all versions of edited nodes.
   */ 
  private void
  processNodeVersions() 
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Processing Node Versions...");
    LogMgr.getInstance().flush();

    try {
      for(String name : pNodeIDs.keySet()) {
        long nodeID = pNodeIDs.get(name);
        
        TreeMap<VersionID,NodeVersion> versions = getNodeVersions(name);
        if(versions != null) {
          for(VersionID vid : versions.keySet()) {
            NodeVersion vsn = versions.get(vid);
            pNumEditedVersions++;
            
            TreeMap<Long,VersionID> depends = new TreeMap<Long,VersionID>();
            findNodeDependencies(vsn, depends);

            Calendar cal = new GregorianCalendar(pTimeZone);
            cal.setTimeInMillis(vsn.getTimeStamp()); 
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayStart = cal.getTimeInMillis();
            
            BufferedWriter out = getNodeVersionWriters(dayStart); 
            
            out.write(nodeID + "\t" + vsn.getVersionID() + "\t" + vsn.getTimeStamp() + "\t" + 
                      getArtistID(vsn.getAuthor()) + "\t");
            
            BaseEditor editor = vsn.getEditor();
            if(editor != null) 
              out.write(editor.getName());
            out.write("\t");
            
            String suffix = vsn.getPrimarySequence().getFilePattern().getSuffix();
            if(suffix != null)
              out.write(suffix);
            out.write("\t");
            
            boolean first = true;
            for(Long snodeID : depends.keySet()) {
              if(!first) 
                out.write(",");
              first = false;
              
              out.write(snodeID + ":" + depends.get(snodeID));
            }
            
            out.write("\n");
          }
        }
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write daily aritst work to file:\n  " + 
         ex.getMessage());
    }
    
    for(Long stamp : pNodeVersionWriters.keySet()) {
      try {
        BufferedWriter writer = pNodeVersionWriters.get(stamp);
        writer.close();
      }
      catch(IOException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to cleanly close the node version file for the day starting on " +
           "(" + stamp + "):\n  " + 
           ex.getMessage());
      }
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Found (" + pNumEditedVersions + ") user edited node versions.\n" + 
       "  Processed in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }

  /**
   * Recursively search the upstream dependencies of a specific node version for other 
   * edited node versions.
   */ 
  private void 
  findNodeDependencies
  (
   NodeVersion vsn, 
   TreeMap<Long,VersionID> depends
  ) 
    throws PipelineException
  {
    for(LinkVersion link : vsn.getSources()) {
      String lname = link.getName();
      VersionID lvid = link.getVersionID();
      Long lnodeID = pNodeIDs.get(lname);
      if(lnodeID != null) {
        depends.put(lnodeID, lvid);
      }
      else {
        TreeMap<VersionID,NodeVersion> versions = getNodeVersions(lname);
        if(versions != null) 
          findNodeDependencies(versions.get(lvid), depends);
      }
    }
  }
     
  /**
   * Get all checked-in versions of a node.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  private TreeMap<VersionID,NodeVersion>
  getNodeVersions
  (
   String name
  ) 
    throws PipelineException
  {
    if(!pNodeVersions.containsKey(name)) {
      Path path = new Path(pRepoPath, name);
      File dir = path.toFile();
      if(!dir.isDirectory()) 
        return null;
      
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Finer,
         "Reading Node Versions for: " + name);
      
      File files[] = dir.listFiles();
      if(files == null) 
        return null;

      int wk;
      for(wk=0; wk<files.length; wk++) {
        if(!files[wk].isFile()) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Warning,
             "Somehow the node version file (" + files[wk] + ") was not a regular file!");
          return null;
        }
        
        NodeVersion vsn = null;
        try {
          vsn = (NodeVersion) GlueDecoderImpl.decodeFile("NodeVersion", files[wk]);
          pNodeVersions.put(vsn.getName(), vsn.getVersionID(), vsn);
        }	
        catch(GlueException ex) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Warning,
             "Unable to read Node Version file: " + files[wk] + "\n" + 
             "  " + ex.getMessage());
        }
      }
    }

    return pNodeVersions.get(name);
  }

  /**
   * Lookup or open the buffered file writer for the node versions create on a given day.
   * 
   * @param stamp
   *   The timestamp of the start of the day.
   */ 
  private BufferedWriter
  getNodeVersionWriters
  (
   Long stamp
  ) 
    throws PipelineException
  {
    BufferedWriter out = pNodeVersionWriters.get(stamp);
    if(out == null) {
      Path dpath = new Path(pOutputPath, "NodeVersions");
      File dir = dpath.toFile();
      if(!dir.exists()) {
        if(!dir.mkdirs()) 
          throw new PipelineException
            ("Unable to create find or create the directory (" + dpath + ") where the " + 
             "daily node versions files will be written!");
      }

      Calendar cal = new GregorianCalendar(pTimeZone);
      cal.setTimeInMillis(stamp);
      
      String fname = String.format("%1$4d%2$02d%3$02d-%4$d", 
                                   cal.get(Calendar.YEAR), 
                                   cal.get(Calendar.MONTH), 
                                   cal.get(Calendar.DAY_OF_MONTH), 
                                   stamp);

      Path path = new Path(dpath, fname); 
      
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Finer,
         "Writing Node Versions in: " + fname);
      
      try {
        out = new BufferedWriter(new FileWriter(path.toFile()));
        pNodeVersionWriters.put(stamp, out);
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to open daily node versions file (" + path + "):\n  " + 
         ex.getMessage());
      }
    }

    return out;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Write a plain tab seperated text file containing artist ID and name, one per line.
   */ 
  private void 
  writeArtistIndex()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Writing Artist Index...");
    LogMgr.getInstance().flush();

    Path path = new Path(pOutputPath, "ArtistIndex");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path.toFile()));
      try {
        TreeMap<Long,String> table = new TreeMap<Long,String>();
        for(String name : pArtistIDs.keySet()) 
          table.put(pArtistIDs.get(name), name);

        for(Long artistID : table.keySet()) {
          String name = table.get(artistID);
          out.write(artistID + "\t" + name + "\n");
        }
      }
      finally {
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write artists file (" + path + "):\n  " + 
         ex.getMessage());
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Written in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }
    
  /**
   * Write a plain tab seperated text file containing node ID and name, one per line.
   */ 
  private void 
  writeNodeIndex()
    throws PipelineException
  {
    TaskTimer timer = new TaskTimer();
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Writing Node Index...");
    LogMgr.getInstance().flush();

    Path path = new Path(pOutputPath, "NodeIndex");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path.toFile()));
      try {
        TreeMap<Long,String> table = new TreeMap<Long,String>();
        for(String name : pNodeIDs.keySet()) 
          table.put(pNodeIDs.get(name), name);

        for(Long nodeID : table.keySet()) {
          String name = table.get(nodeID);
          out.write(nodeID + "\t" + name + "\n");
        }
      }
      finally {
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write nodes file (" + path + "):\n  " + 
         ex.getMessage());
    }

    timer.suspend();
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "  Written in " + TimeStamps.formatInterval(timer.getTotalDuration()));
    LogMgr.getInstance().flush();
  }
    



  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  plmine [options]\n" + 
       "\n" + 
       "  plmine --help\n" +
       "  plmine --html-help\n" +
       "  plmine --version\n" + 
       "  plmine --release-date\n" + 
       "  plmine --copyright\n" + 
       "  plmine --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--delta-gmt=...] [--pattern=...] [--database-dir=...] [--output-dir=...]\n" + 
       "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plmine --html-help\" to browse the full documentation.\n");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generate an explanitory message for the non-literal token.
   */ 
  protected String
  tokenExplain
  (
   int kind,
   boolean printLiteral
  ) 
  {
    switch(kind) {
    case MineOptsParserConstants.EOF:
      return "EOF";

    case MineOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case MineOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case MineOptsParserConstants.INTEGER:
      return "an integer";

    case MineOptsParserConstants.STRING:
      return "an string";

    case MineOptsParserConstants.EMPTY_STRING:
      return "an empty string";      

    case MineOptsParserConstants.PATH_ARG: 
      return "a filesystem path";

    default: 
      if(printLiteral) 
	return MineOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The local time zone where the nodes where created and artist work performed.
   */ 
  private TimeZone  pTimeZone; 

  /**
   * The regular expression pattern to match node names against or 
   * <CODE>null</CODE> to match all names.
   */ 
  private Pattern  pNodePattern; 

  /**
   * The root directory containing checked-in node metadata files. 
   */ 
  private Path  pRepoPath;
  
  /**
   * The root directory containing node event files. 
   */ 
  private Path  pEventPath;
  
  /**
   * The root directory where extracted data files will be written.
   */ 
  private Path  pOutputPath;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generation of unique node IDs, mapping from fully resolved node names and the associated 
   * file writer.
   */ 
  private long  pNextNodeID;
  private TreeMap<String,Long> pNodeIDs;  
  private BufferedWriter pNodeWriter;

  /**
   * Generation of unique artist IDs, mapping from artist names and the associated 
   * file writer.
   */ 
  private long  pNextArtistID;
  private TreeMap<String,Long> pArtistIDs;  
  private BufferedWriter pArtistWriter;

  /**
   * Per-day artist work files writers indexed by the time stamps at midnight on each day.
   */ 
  private TreeMap<Long,BufferedWriter>  pArtistWorkWriters;
  private long pNumEditEvents;

  /**
   * The extracted node versions file writer.
   */ 
  private TreeMap<Long,BufferedWriter> pNodeVersionWriters;
  private long pNumEditedVersions;

  /**
   * Checked-in node versions cache.
   */ 
  private DoubleMap<String,VersionID,NodeVersion>  pNodeVersions; 

}



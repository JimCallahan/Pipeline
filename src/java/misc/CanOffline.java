// $Id: CanOffline.java,v 1.1 2009/12/11 01:24:23 jim Exp $

import java.util.*;
import java.io.*;
import java.text.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   C A N   O F F L I N E                                                                  */
/*------------------------------------------------------------------------------------------*/

public class
CanOffline
{
  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length != 2)
        usage(); 

      String name = args[0];
      VersionID vid = new VersionID(args[1]);
      
      boolean tf = canOffline(name, vid);
      
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 name + " (" + vid + ") " + (tf ? "can" : "CANNOT") + " be offlined.");

      success = true;
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }

  private static void 
  usage()
    throws PipelineException
  {
    throw new PipelineException
      ("usage: CanOffline name version\n" + 
       "\n" +
       "Tests whether the given node version can be offlined."); 
  }

  public static boolean 
  canOffline
  (
   String name, 
   VersionID vid
  ) 
  {
    try {
      PluginMgrClient.init(true);
      MasterMgrClient client = new MasterMgrClient();

      TreeSet<VersionID> offline = client.getOfflineVersionIDs(name);
      if(offline.contains(vid))
        return false;
      
      MappedSet<String,VersionID> versions = new MappedSet<String,VersionID>();
      versions.put(name, vid);
      if(client.getArchivesContaining(versions).isEmpty()) 
        return false;

      boolean isLatest = false;
      {
        TreeSet<VersionID> vids = client.getCheckedInVersionIDs(name);
        if(vids.isEmpty()) 
          return false;
        isLatest = vid.equals(vids.last());
      }

      TreeMap<String,TreeSet<String>> areas = client.getWorkingAreasContaining(name); 
      if(isLatest && areas.isEmpty()) 
        return false;
      
      for(String author : areas.keySet()) {
        for(String view : areas.get(author)) {
          NodeMod mod = client.getWorkingVersion(author, view, name);
          if(vid.equals(mod.getWorkingID())) 
            return false;
        }
      }
      
      return true;
    }
    catch(PipelineException ex) {
      return false;
    }
  }
}
  

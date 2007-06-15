// $Id: ScriptApp.java,v 1.76 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.event.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   S C R I P T   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plscript.html"><B>plscript</B></A>(1) tool. <P> 
 */
public
class ScriptApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  ScriptApp() 
  {
    super("plscript");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
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
    ScriptOptsParser parser = null;
    try {
      parser = new ScriptOptsParser(new StringReader(pPackedArgs));
      parser.setApp(this);
      parser.CommandLine();   
      
      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 wordWrap(ex.getMessage(), 0, 80));
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
    }
    finally {
      if(parser != null)
	parser.disconnect();

      PluginMgrClient pclient = PluginMgrClient.getInstance();
      if(pclient != null) 
	pclient.disconnect();

      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
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
       "  plscript [global-options] command [command-options]\n" + 
       "  plscript [global-options] --batch=batchfile\n" + 
       "\n" + 
       "  plscript --help\n" +
       "  plscript --html-help\n" +
       "  plscript --version\n" + 
       "  plscript --release-date\n" + 
       "  plscript --copyright\n" + 
       "  plscript --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "COMMANDS:\n\n" +
       "  User Privileges:\n" +
       "    user\n" + 
       "      --get\n" + 
       "      --add=user-name\n" + 
       "      --remove=user-name\n" +
       "\n" + 
       "    work-group\n" + 
       "      --get\n" + 
       "      --get-info=group-name\n" + 
       "      --get-info-all\n" + 
       "      --add=group-name\n" + 
       "      --remove=group-name\n" +
       "      --set=group-name --manager | --member | --not-member\n" + 
       "        --user=user-name [--user=user-name ...]\n" +
       "\n" + 
       "    privilege\n" + 
       "      --get-info=user-name\n" + 
       "      --get-info-all\n" + 
       "      --grant=user-name --master-admin | --developer | --annotator |\n" + 
       "        --queue-admin | --queue-manager | --node-mananger\n" + 
       "      --revoke=user-name --master-admin | --developer | --annotator |\n" + 
       "        --queue-admin | --queue-manager | --node-mananger\n" + 
       "\n" + 
       "  Administration\n" +
       "    admin\n" + 
       "      --shutdown [--shutdown-jobmgrs] [--shutdown-pluginmgr]\n" + 
       "      --backup=dir\n" +
       "      --archive=archive-prefix [--pattern='node-regex'] [--max-archives=integer]\n" +
       "        [--min-size=bytes] --archiver=archiver-name[:major.minor.micro]]\n" + 
       "        [--param=name:value ...] [--auto-start] [--toolset=...]\n" + 
       "      --offline [--pattern='node-regex'] [--exclude-latest=integer]\n" + 
       "        [--min-archives=integer]\n" + 
       "      --restore=archive-name [--param=name:value ...] [--toolset=...]\n" + 
       "        [--vsn=node-name:major.minor.micro ...]\n" + 
       "    runtime\n" + 
       "      --set-master [--remote-log=logger:level[,logger:level[...]]]\n" + 
       "        [--node-gc-interval=msec] [--min-overhead=min] [--max-overhead=max]\n" + 
       "        [--avg-node-size=size] [--restore-cleanup-interval=msec]\n" + 
       "      --set-queue [--remote-log=logger:level[,logger:level[...]]]\n" + 
       "        [--collector-batch-size=num] [--dispatcher-interval=msec]\n" + 
       "      --get-master\n" + 
       "      --get-queue\n" +
       "    archive-volume\n" + 
       "      --get\n" + 
       "      --get-info=archive-name [--show=section[,section ...]]\n" + 
       "        [--hide=section[,section ...]]\n" + 
       "\n" + 
       "  Toolset Administration\n" + 
       "    default-toolset\n" + 
       "      --get\n" + 
       "      --set=toolset-name\n" + 
       "    active-toolset\n" + 
       "      --get\n" + 
       "      --add=toolset-name\n" + 
       "      --remove=toolset-name\n" + 
       "    toolset\n" + 
       "      --get\n" + 
       "      --get-info=toolset-name\n" + 
       "      --get-info-all\n" + 
       "      --export=toolset-name\n" + 
       "\n" + 
       "  Queue Administration\n" + 
       "    license-key\n" + 
       "      --get\n" + 
       "      --get-info=key-name\n" +
       "      --get-info-all\n" + 
       "      --add=key-name\n" + 
       "        --msg=\"key-description\" --per-slot | --per-host | --per-host-slot\n" +
       "        [--max-slots=integer] [--max-hosts=integer] [--max-host-slots=integer]\n" +
       "      --set=key-name\n" + 
       "        [--per-slot | --per-host | --per-host-slot]\n" +
       "        [--max-slots=integer] [--max-hosts=integer] [--max-host-slots=integer]\n" +
       "      --remove=key-name\n" +
       "    selection-key\n" + 
       "      --get\n" + 
       "      --get-info=key-name\n" + 
       "      --get-info-all\n" + 
       "      --add=key-name\n" + 
       "        --msg=\"key-description\"\n" + 
       "      --remove=key-name\n" + 
       "    job-server\n" + 
       "      --get\n" + 
       "      --get-info=host-name\n" + 
       "      --get-info-all\n" + 
       "      --add=host-name\n" + 
       "      --set=host-name\n" + 
       "       [--shutdown | --disable | --enable] [--reserve=user-name | --open]\n" + 
       "       [--order=integer] [--slots=integer] [--selection-bias=key-name:bias]\n" + 
       "       [--remove-key=key-name]\n" + 
       "      --remove=host-name\n" + 
       "\n" + 
       "  Plugins\n" + 
       "    editor\n" + 
       "      --get\n" + 
       "      --get-info=editor-name[:major.minor.micro]\n" + 
       "      --get-info-all\n" + 
       "    action\n" + 
       "      --get\n" + 
       "      --get-info=action-name[:major.minor.micro]\n" + 
       "      --get-info-all\n" + 
       "    comparator\n" + 
       "      --get\n" + 
       "      --get-info=comparator-name[:major.minor.micro]\n" + 
       "      --get-info-all\n" + 
       "    tool\n" + 
       "      --get\n" + 
       "      --get-info=tool-name[:major.minor.micro]\n" + 
       "      --get-info-all\n" + 
       "    archiver\n" + 
       "      --get\n" + 
       "      --get-info=archiver-name[:major.minor.micro]\n" + 
       "      --get-info-all\n" + 
       "\n" + 
       "  User Preferences\n" + 
       "    suffix-editor\n" + 
       "      --get\n" + 
       "      --get-info=suffix\n" + 
       "      --get-info-all\n" + 
       "      --set=suffix\n" + 
       "          --msg=\"suffix-description\" --editor=editor-name[:major.minor.micro]\n" + 
       "      --remove=suffix\n" + 
       "      --reset\n" + 
       "\n" +
       "  Working Area Views\n" + 
       "    view\n" + 
       "      --get\n" + 
       "      --create=view-name\n" + 
       "      --get [--author=user-name]\n" + 
       "      --create=view-name [--author=user-name]\n" + 
       "      --release=view-name [--author=user-name] [--pattern='node-regex']\n" + 
       "        [--remove-files] [--remove-area]\n" + 
       "\n" + 
       "  Working Node Versions\n" +
       "    working\n" +
       "      --get-info=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
       "      --register=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --fseq=prefix[.#|@...][.suffix][,start[-end[xby]]]\n" +
       "        [--toolset=toolset-name] [--editor=editor-name[:major.minor.micro]]\n" +
       "        [--no-action | --action=action-name[:major.minor.micro]]\n" +
       "        [--action-enabled=true|false] [--param=name:value ...]\n" +
       "        [--source-param=source-name,name:value ...]\n" +
       "        [--ignore | --abort] [--serial | --subdivided | --parallel]\n" + 
       "        [--batch-size=integer] [--priority=integer] [--ramp-up=milliseconds]\n" + 
       "        [--max-load=real] [--min-memory=bytes[K|M|G]] [--min-disk=bytes[K|M|G]]\n" +
       "        [--license-key=key-name[:true|false] ...]\n" +
       "        [--selection-key=key-name[:true|false] ...]\n" +
       "      --clone=node-name\n" + 
       "        [--author=user-name] [--view=view-name]\n" + 
       "        --clone-source=node-name\n" + 
       "      --release=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--remove-files]\n" +
       "      --set=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--toolset=toolset-name] [--editor=editor-name[:major.minor.micro]]\n" +
       "        [--no-action | --action=action-name[:major.minor.micro]]\n" +
       "        [--action-enabled=true|false] [--param=name:value ...]\n" + 
       "        [--no-param=source-name | --source-param=source-name,name:value ...]\n" +
       "        [--ignore | --abort] [--serial | --subdivided | --parallel]\n" + 
       "        [--batch-size=integer] [--priority=integer] [--ramp-up=milliseconds]\n" + 
       "        [--max-load=real] [--min-memory=bytes[K|M|G]] [--min-disk=bytes[K|M|G]]\n" +
       "        [--license-key=key-name[:true|false] ...]\n" + 
       "        [--selection-key=key-name[:true|false] ...]\n" +
       "      --link=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --assoc=node-name |\n" + 
       "        --ref=node-name[,all|offset] | \n" + 
       "        --depend=node-name[,all|offset]\n" + 
       "      --unlink=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --source=node-name ...\n" +
       "      --add-secondary=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --fseq=prefix[.#|@...][.suffix],start[-end[xby]]\n" +
       "      --remove-secondary=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --fseq=prefix[.#|@...][.suffix][,start[-end[xby]]]\n" +
       "      --rename=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --file-pattern=new-node-name[.#|@...][.suffix] [--rename-files]\n" +
       "      --renumber=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --range=start[-end[xby]] [--remove-files]\n" +
       "      --edit=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--editor=editor-name[:major.minor.micro]]\n" +
       "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
       "        [--fseq=prefix[.#|@...][.suffix][,start[-end[xby]]]]\n" +
       "        [--wait]\n" +
       "      --submit-jobs=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
       "        [--wait]\n" +
       "      --remove-files=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
       "      --views-containing=node-name\n" + 
       "      --views-editing=node-name\n" + 
       "\n" +
       "  Checked-In Node Versions\n" +
       "    checked-in\n" +
       "      --get-info=node-name\n" +
       "        [--version=major.minor.micro | --latest ...]\n" +
       "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
       "      --history=node-name\n" +
       "        [--version=major.minor.micro | --latest ...]\n" +
       "      --view=node-name\n" +
       "        [--version=major.minor.micro]\n" +
       "        [--editor=editor-name[:major.minor.micro]]\n" +
       "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
       "        [--fseq=prefix[.#|@...][.suffix][,start[-end[xby]]]]\n" +
       "\n" + 
       "  Node Operations\n" +
       "    node\n" +
       "      --status=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--brief] [--upstream] [--light] [--link-graph]\n" +
       "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
       "      --check-in=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        --msg=\"log-message\" [--major | --minor | --micro]\n" +
       "      --check-out=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--version=major.minor.micro] [--always | --keep-newer | --keep-modified]\n" +
       "      --evolve=node-name\n" +
       "        [--author=user-name] [--view=view-name]\n" +
       "        [--version=major.minor.micro]\n" +
       "      --get-events\n" + 
       "        [--node=node-name ...] [--author=user-name ...]\n" + 
       "        [--from=[YYYY-MM-DD,]hh:mm:ss] [--until=[YYYY-MM-DD,]hh:mm:ss]\n" + 
       "\n" +  
       "Use \"plscript --html-help\" to browse the full documentation.\n");
  }




  /*----------------------------------------------------------------------------------------*/
  /*   W O R K   G R O U P                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print the membership of the given work group.
   */ 
  public void
  printWorkGroupMembers
  (
   String gname, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    WorkGroups groups = client.getWorkGroups();
    if(!groups.getGroups().contains(gname)) 
      throw new PipelineException
	("No work group named (" + gname + ") exists!");

    StringBuilder buf = new StringBuilder();
    printWorkGroupMembersHelper(gname, groups, buf, true);

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();   
  }

  /**
   * Print the membership of all work groups.
   */ 
  public void
  printWorkGroupMembers
  (
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    WorkGroups groups = client.getWorkGroups();

    boolean first = true;
    StringBuilder buf = new StringBuilder();
    for(String gname : groups.getGroups()) {
      printWorkGroupMembersHelper(gname, groups, buf, first);
      first = false;
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();   
  }

  /**
   * Print the membership of one work group.
   */ 
  private void 
  printWorkGroupMembersHelper
  (
   String gname, 
   WorkGroups groups, 
   StringBuilder buf, 
   boolean first
  ) 
    throws PipelineException
  {
    TreeSet<String> managers = new TreeSet<String>();
    TreeSet<String> members  = new TreeSet<String>();
    for(String uname : groups.getUsers()) {
      Boolean tf = groups.isMemberOrManager(uname, gname);
      if(tf != null) {
	if(tf) 
	  managers.add(uname);
	else 
	  members.add(uname);
      }
    }
	
    if(first) 
      buf.append(tbar(80) + "\n");
    else 
      buf.append("\n" + 
		 bar(80) + "\n");

    buf.append("Work Group : " + gname + "\n" + 
	       "Managers   :");
    
    if(managers.isEmpty()) {
      buf.append(" -");
    }
    else {
      for(String uname : managers) 
	buf.append(" " + uname);
    }

    buf.append("\n" + 
	       "Members    :");

    if(managers.isEmpty()) {
      buf.append(" -");
    }
    else {
      for(String uname : members) 
	buf.append(" " + uname);
    }

    buf.append("\n");
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P R I V I L E G E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print the privileges of the given user. 
   */ 
  public void
  printUserPrivileges
  (
   String uname, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeMap<String,Privileges> privileges = client.getPrivileges();

    Privileges privs = privileges.get(uname);
    if(privs == null) 
      privs = new Privileges();
    
    StringBuilder buf = new StringBuilder();
    printUserPrivilegesHelper(uname, privs, buf, true);
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();   
  }

  /**
   * Print the privileges of all users. 
   */ 
  public void
  printUserPrivileges
  (
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeMap<String,Privileges> privileges = client.getPrivileges();

    boolean first = true;
    StringBuilder buf = new StringBuilder();
    for(String uname : privileges.keySet()) {
      Privileges privs = privileges.get(uname);
      printUserPrivilegesHelper(uname, privs, buf, first);
      first = false;
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();   
  }

  /**
   * Print the privileges of a users. 
   */ 
  private void
  printUserPrivilegesHelper
  (
   String uname, 
   Privileges privs,
   StringBuilder buf, 
   boolean first
  ) 
    throws PipelineException
  {
    if(first) 
      buf.append(tbar(80) + "\n");
    else 
      buf.append("\n" + 
		 bar(80) + "\n");

    buf.append("User Name     : " + uname + "\n" + 
	       "\n" +
	       "Master Admin  : " + (privs.isMasterAdmin() ? "YES" : "no") + "\n" + 
	       "Developer     : " + (privs.isDeveloper() ? "YES" : "no") + "\n" + 
	       "Annotator     : " + (privs.isAnnotator() ? "YES" : "no") + "\n" + 
	       "Queue Admin   : " + (privs.isQueueAdmin() ? "YES" : "no") + "\n" + 
	       "Queue Manager : " + (privs.isQueueManager() ? "YES" : "no") + "\n" + 
	       "Node Manager  : " + (privs.isNodeManager() ? "YES" : "no") + "\n");
  }

  /**
   * Edit the privileges of the given user. 
   */ 
  public void
  editPrivileges
  (
   String uname, 
   Boolean isMasterAdmin,
   Boolean isDeveloper,
   Boolean isAnnotator,
   Boolean isQueueAdmin, 
   Boolean isQueueManager, 
   Boolean isNodeManager,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeMap<String,Privileges> privileges = client.getPrivileges();
    Privileges privs = privileges.get(uname);
    if(privs == null) 
      privs = new Privileges();
    
    if(isMasterAdmin != null) 
      privs.setMasterAdmin(isMasterAdmin);

    if(isDeveloper != null) 
      privs.setDeveloper(isDeveloper);

    if(isAnnotator != null) 
      privs.setAnnotator(isAnnotator);

    if(isQueueAdmin != null) 
      privs.setQueueAdmin(isQueueAdmin);

    if(isQueueManager != null) 
      privs.setQueueManager(isQueueManager);

    if(isNodeManager != null) 
      privs.setNodeManager(isNodeManager);

    TreeMap<String,Privileges> edited = new TreeMap<String,Privileges>();
    edited.put(uname, privs);

    client.editPrivileges(edited);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new archive volume containing the matching checked-in versions.
   */ 
  public void 
  archive
  (
   String prefix, 
   String pattern, 
   Integer maxArchives, 
   Long minSize, 
   String archiverName, 
   VersionID archiverVersionID,
   String archiverVendor, 
   TreeMap params, 
   boolean autoStart, 
   String toolset, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    /* perform an archival candidate query */ 
    ArrayList<ArchiveInfo> infos = client.archiveQuery(pattern, maxArchives);
    if(infos.isEmpty()) 
      throw new PipelineException
	("No checked-in versions match the archive selection criteria!");

    /* instantiate the Archiver plugin and set its parameters */ 
    BaseArchiver archiver = 
      PluginMgrClient.getInstance().newArchiver
        (archiverName, archiverVersionID, archiverVendor);
    {
      TreeMap<String,String> aparams = (TreeMap<String,String>) params;
      for(String pname : aparams.keySet()) {
	String value = aparams.get(pname);
	try {
	  ArchiverParam aparam = archiver.getParam(pname);
	  if(aparam == null)
	    throw new PipelineException 
	      ("No parameter named (" + pname + ") exists for Archiver " + 
	       "(" + archiver.getName() + ")!");
	  
	  if(aparam instanceof BooleanArchiverParam) {
	    archiver.setParamValue(pname, new Boolean(value));
	  }
	  else if(aparam instanceof ByteSizeArchiverParam) {
	    archiver.setParamValue(pname, parseLong(value));
	  }
	  else if(aparam instanceof IntegerArchiverParam) {
	    archiver.setParamValue(pname, new Integer(value));
	  }
	  else if(aparam instanceof DoubleArchiverParam) {
	    archiver.setParamValue(pname, new Double(value));
	  }
	  else if((aparam instanceof StringArchiverParam) || 
		  (aparam instanceof DirectoryArchiverParam)) {
	    archiver.setParamValue(pname, value);
	  }
	  else if(aparam instanceof EnumArchiverParam) {
	    EnumArchiverParam eparam = (EnumArchiverParam) aparam;
	    if(!eparam.getValues().contains(value)) {
	      StringBuilder buf = new StringBuilder();
	      buf.append("The value (" + value + ") is not one of the enumerations of " +
			 "parameter (" + pname + ")!  The valid values are:\n");
	      for(String evalue : eparam.getValues()) 
		buf.append("  " + evalue + "\n");
	      throw new PipelineException(buf.toString());
	    }
	    
	    archiver.setParamValue(pname, value);
	  }
	}
	catch(NumberFormatException ex) {
	  throw new PipelineException
	    ("The value (" + value + ") is not legal for parameter (" + pname + ")!");
	}
      }
    }

    /* determine the total size of files associated with each selected version */ 
    TreeMap<String,TreeMap<VersionID,Long>> versionSizes = null;
    {
      TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
      for(ArchiveInfo info : infos) {
	TreeSet<VersionID> vids = versions.get(info.getName());
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(info.getName(), vids);
	}
	vids.add(info.getVersionID());
      }
      
      versionSizes = client.getArchivedSizes(versions);
    }

    /* assign enough versions to fill one archive volume */ 
    TreeMap<String,TreeSet<VersionID>> selected = new TreeMap<String,TreeSet<VersionID>>();
    long total = 0L;
    if(versionSizes != null) {
      long capacity = archiver.getCapacity();
      for(String name : versionSizes.keySet()) {
	TreeMap<VersionID,Long> sizes = versionSizes.get(name);
	for(VersionID vid : sizes.keySet()) {
	  Long size = sizes.get(vid);

	  if((total+size) >= archiver.getCapacity()) {
	    /* the version is too big to fit by itself in a volume */ 
	    if(total == 0L) {
	      throw new PipelineException 
		("The version (" + vid + ") of node (" + name + ") was larger than " + 
		 "the capacity of an entire archive volume!  The capacity of the " + 
		 "archive volume must be increased to at least (" + formatLong(size) + ") " + 
		 "in order to archive this version.");
	    }
	  }
	  else {
	    TreeSet<VersionID> vids = selected.get(name);
	    if(vids == null) {
	      vids = new TreeSet<VersionID>();
	      selected.put(name, vids);
	    }
	    
	    vids.add(vid);
	    total += size;
	  }
	}
      }

      if(total < minSize) 
	throw new PipelineException
	  ("The total size (" + formatLong(total) + ") of all versions selected for " + 
	   "archiving was less than the minimum archive volume size " + 
	   "(" + formatLong(minSize) + ")!  Either select enough versions to meet this " + 
	   "minimum size or specify a smaller minimum size using the --min-size option " + 
	   "to create an archive volume.");
    }

    /* ask the user if they are ready */
    if(archiver.isManual() && !autoStart) {
      while(true) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   "Are you ready to write the archive volume?\n" + 
	   "[y/n]: ");
	LogMgr.getInstance().flush();      
	
	try {
	  InputStreamReader in = new InputStreamReader(System.in);
	  switch(in.read()) {
	  case 'y':
	  case 'Y':
	    break;

	  case 'n':
	  case 'N':
	    throw new PipelineException("Archive operation aborted.");
	  }
	}
	catch(IOException ex) {
	  throw new PipelineException(ex);
	}
      }
    }

    /* create the archive volume */ 
    {
      StringBuilder buf = new StringBuilder();
      buf.append("Archiving Checked-In Versions:\n");
      for(String name : selected.keySet()) {
	for(VersionID vid : selected.get(name)) {
	  Long size = versionSizes.get(name).get(vid);
	  buf.append
	    ("  " + pad(name, ' ', 75) + "  v" + pad(vid.toString(), ' ', 10) + "  (" + 
	     formatLong(size) + ")\n");
	}
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 buf.toString());
      LogMgr.getInstance().flush();   

      String archiveName = client.archive(prefix, selected, archiver, toolset);

      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Created Archive Volume: " + archiveName + "  (" + formatLong(total) + ")");
      LogMgr.getInstance().flush();      
    }
  }

  /**
   * Offline the matching checked-in versions.
   */ 
  public void 
  offline
  (
   String pattern, 
   Integer exludeLatest, 
   Integer minArchives, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    /* perform an offline candidate query */ 
    ArrayList<OfflineInfo> infos = 
      client.offlineQuery(pattern, exludeLatest, minArchives, true);
    if(infos.isEmpty()) 
      throw new PipelineException
	("No checked-in versions match the offline selection criteria!");


    /* offline the versions */ 
    {
      TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();
      for(OfflineInfo info : infos) {
	TreeSet<VersionID> vids = versions.get(info.getName());
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(info.getName(), vids);
	}
	vids.add(info.getVersionID());
      }

      StringBuilder buf = new StringBuilder();
      buf.append("Offlining Checked-In Versions:\n");
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) 
	  buf.append("  " + pad(name, ' ', 75) + "  v" + vid.toString() + "\n");
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 buf.toString());
      LogMgr.getInstance().flush();   

      client.offline(versions);

      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Offline Complete.");
      LogMgr.getInstance().flush();      
    }
  }

  /**
   * Restore the given checked-in versions from an archive volume.
   */ 
  public void 
  restore
  (
   String archiveName, 
   TreeMap params,
   TreeMap versions, 
   String toolset, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeMap<String,String> aparams = (TreeMap<String,String>) params;
    TreeMap<String,TreeSet<VersionID>> aversions = 
      (TreeMap<String,TreeSet<VersionID>>) versions; 

    ArchiveVolume vol = client.getArchive(archiveName);

    BaseArchiver archiver = vol.getArchiver();
    for(String pname : aparams.keySet()) {
      String value = aparams.get(pname);
      try {
	ArchiverParam aparam = archiver.getParam(pname);
	if(aparam == null)
	  throw new PipelineException 
	    ("No parameter named (" + pname + ") exists for Archiver " + 
	     "(" + archiver.getName() + ")!");
	    
	if(aparam instanceof BooleanArchiverParam) {
	  archiver.setParamValue(pname, new Boolean(value));
	}
	else if(aparam instanceof ByteSizeArchiverParam) {
	  archiver.setParamValue(pname, parseLong(value));
	}
	else if(aparam instanceof DirectoryArchiverParam) {
	  archiver.setParamValue(pname, value);
	}
	else if(aparam instanceof DoubleArchiverParam) {
	  archiver.setParamValue(pname, new Double(value));
	}
	else if(aparam instanceof EnumArchiverParam) {
	  EnumArchiverParam eparam = (EnumArchiverParam) aparam;
	  if(!eparam.getValues().contains(value)){
	    StringBuilder buf = new StringBuilder();
	    buf.append("The value (" + value + ") is not one of the enumerations of " +
		       "parameter (" + pname + ")!  The valid values are:\n");
	    for(String evalue : eparam.getValues()) 
	      buf.append("  " + evalue + "\n");
	    throw new PipelineException(buf.toString());
	  }
	  
	  archiver.setParamValue(pname, value);
	}
	else if(aparam instanceof IntegerArchiverParam) {
	  archiver.setParamValue(pname, new Integer(value));
	}
	else if(aparam instanceof StringArchiverParam) {
	  archiver.setParamValue(pname, value);
	}
	else {
	  throw new IllegalStateException("Unknown archiver parameter type!");
	}
      }
      catch(NumberFormatException ex) {
	throw new PipelineException
	  ("The value (" + value + ") is not legal for parameter (" + pname + ")!");
      }

      archiver.setParamValue(pname, value);
    }

    client.restore(archiveName, aversions, archiver, toolset);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A R C H I V E   V O L U M E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print the names of all archive volumes.
   */ 
  public void 
  printArchiveVolumeNames
  (
   MasterMgrClient client
  ) 
    throws PipelineException 
  {
    TreeMap<String,Long> archives = client.getArchivedOn();

    StringBuilder buf = new StringBuilder();
    for(String name : archives.keySet()) 
      buf.append(name + "\n");

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }

  /**
   * Print a text representation of the given archive volume.
   */ 
  public void 
  printArchiveVolume
  (
   String archiveName, 
   TreeSet sections,
   MasterMgrClient client
  )
    throws PipelineException 
  {
    ArchiveVolume vol = client.getArchive(archiveName);

    StringBuilder buf = new StringBuilder();
    buf.append
      (tbar(80) + "\n" +
       "Archive Volume    : " + archiveName + "\n" + 
       "Created           : " + TimeStamps.format(vol.getTimeStamp()));

    if(sections.contains("arch")) {
      BaseArchiver archiver = vol.getArchiver();

      buf.append
	("\n\n" + 
	 pad("-- Archiver Plugin ", '-', 80) + "\n" +
	 "Archiver          : " + archiver.getName() + "\n" +
	 "Version           : " + archiver.getVersionID() + "\n" +
	 "Operation         : " + (archiver.isManual() ? "Manual" : "Automatic") + "\n" +
	 "Capacity          : " + formatLong(archiver.getCapacity()));

      if(archiver.hasParams()) {
	buf.append
	  ("\n\n" +
	   pad("-- Archiver Parameters ", '-', 80));
	
	for(String pname : archiver.getLayout()) {
	  buf.append
	    ("\n" + 
	     pad(pname, 18) + ": " + archiver.getParamValue(pname));
	}
      }
    }

    if(sections.contains("vsn")) {
      buf.append
	("\n\n" + 
	 pad("-- Versions ", '-', 80));
    
      for(String name : vol.getNames()) {
	for(VersionID vid : vol.getVersionIDs(name)) {
	  buf.append
	    ("\n" + 
	     pad(name + " (v" + vid + ")", 72) + "  " + formatLong(vol.getSize(name, vid)));
	}
      }
    }

    if(sections.contains("file")) {
      buf.append
	("\n\n" + 
	 pad("-- Files ", '-', 80));
      
      for(String name : vol.getNames()) {
	for(VersionID vid : vol.getVersionIDs(name)) {
	  for(File file : vol.getFiles(name, vid)) 
	    buf.append("\n" + name + "/" + vid + "/" + file);
	}
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   P A R A M E T E R S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print the master controls and logging levels.
   */ 
  public void 
  printRuntimeMaster
  (
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    StringBuilder buf = new StringBuilder();

    {
      MasterControls controls = client.getRuntimeControls();
      buf.append
	(tbar(80) + "\n" +
	 " Average Node Size : " + controls.getAverageNodeSize() + " (bytes)\n" + 
	 "  Minimum Overhead : " + controls.getMinimumOverhead() + 
	 " (" + ByteSize.longToFloatString(controls.getMinimumOverhead()) + ")\n" +
	 "  Maximum Overhead : " + controls.getMaximumOverhead() + 
	 " (" + ByteSize.longToFloatString(controls.getMaximumOverhead()) + ")\n" +
	 "  Node GC Interval : " + controls.getNodeGCInterval() + " (msec)\n" + 
	 "  Restore Interval : " + controls.getRestoreCleanupInterval() + " (msec)\n" + 
	 "\n" + 
	 pad("-- Logging Levels ", '-', 80) + "\n");
    }
     
    {
      LogControls controls = client.getLogControls();
      logLevelMessage(controls, LogMgr.Kind.Glu, buf);
      logLevelMessage(controls, LogMgr.Kind.Sub, buf);
      logLevelMessage(controls, LogMgr.Kind.Ops, buf);
      logLevelMessage(controls, LogMgr.Kind.Net, buf);
      logLevelMessage(controls, LogMgr.Kind.Plg, buf);
      logLevelMessage(controls, LogMgr.Kind.Mem, buf);
      logLevelMessage(controls, LogMgr.Kind.Ext, buf);
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }


  /**
   * Print the queue controls and logging levels.
   */ 
  public void 
  printRuntimeQueue
  (
   QueueMgrClient client
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder();

    {
      QueueControls controls = client.getRuntimeControls();    
      buf.append
	(tbar(80) + "\n" +
	 " Collector Batch Size : " + controls.getCollectorBatchSize() + "\n" + 
	 "  Dispatcher Interval : " + controls.getDispatcherInterval() + " (msec)\n" +
	 "\n" + 
	 pad("-- Logging Levels ", '-', 80) + "\n");
    }

    {
      LogControls controls = client.getLogControls();
      logLevelMessage(controls, LogMgr.Kind.Col, buf);
      logLevelMessage(controls, LogMgr.Kind.Dsp, buf);
      logLevelMessage(controls, LogMgr.Kind.Sch, buf);
      logLevelMessage(controls, LogMgr.Kind.Job, buf);
      logLevelMessage(controls, LogMgr.Kind.Glu, buf);
      logLevelMessage(controls, LogMgr.Kind.Ops, buf);
      logLevelMessage(controls, LogMgr.Kind.Net, buf);
      logLevelMessage(controls, LogMgr.Kind.Plg, buf);
      logLevelMessage(controls, LogMgr.Kind.Mem, buf);
      logLevelMessage(controls, LogMgr.Kind.Ext, buf);
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }

  /**
   * Generate a logging level message.
   */ 
  private void 
  logLevelMessage
  (
   LogControls controls, 
   LogMgr.Kind kind, 
   StringBuilder buf
  ) 
  {
    buf.append("  " + kind + " : " + controls.getLevel(kind) + "\n");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T O O L S E T                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print a text representation of the given toolset.
   */ 
  public void 
  printToolset
  (
   String tname, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    ArrayList<Toolset> selected = new ArrayList<Toolset>();
    if(tname == null) {
      TreeSet<String> tnames = client.getToolsetNames();
      for(String name : tnames) 
	selected.add(client.getToolset(name));
    }
    else {
      selected.add(client.getToolset(tname));
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(Toolset tset : selected) {
      if(!first) 
	buf.append("\n\n");
      first = false;

      buf.append
	(tbar(80) + "\n" +
	 "Toolset     : " + tset.getName() + "\n" + 
	 "Created     : " + (TimeStamps.format(tset.getTimeStamp()) + " by (" + 
			     tset.getAuthor()) + ")\n" +
	 "Description : " + wordWrap(tset.getDescription(), 14, 80) + "\n" + 
	 bar(80) + "\n");
    
      int wk;
      for(wk=0; wk<tset.getNumPackages(); wk++) {
	String pname = tset.getPackageName(wk);
	VersionID vid = tset.getPackageVersionID(wk);
	
	buf.append("Package     : " + pname + " (v" + vid + ")");
	try {
	  PackageVersion pkg = client.getToolsetPackage(pname, vid);
	  buf.append
	    ("\n" + 
	     "Created     : " + (TimeStamps.format(pkg.getTimeStamp()) + " by (" + 
				 pkg.getAuthor()) + ")\n" +
	     "Description : " + wordWrap(pkg.getDescription(), 14, 80));
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning,
	     ex.getMessage());
	}
	
	if(wk < (tset.getNumPackages()-1)) 
	  buf.append("\n\n");
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }

  /**
   * Print a bash(1) script representation of the given toolset.
   */ 
  public void 
  exportToolset
  (
   String tname, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    Toolset tset = client.getToolset(tname);
    TreeMap<String,String> env = tset.getEnvironment();

    StringBuilder buf = new StringBuilder();
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      {
	buf.append("export TOOLSET=" + tset.getName() + "\n");
	buf.append("export USER=`whoami`\n");
	buf.append("export HOME=" + PackageInfo.sHomePath + "/$USER\n");
	buf.append("export WORKING=" + PackageInfo.sWorkPath + "/$USER/default\n");
	buf.append("export _=/bin/env\n");
      
	for(String ename : env.keySet()) {
	  String evalue = env.get(ename);
	  buf.append("\nexport " + ename + "=" + ((evalue != null) ? evalue : ""));
	}
      }
      break;

    case Windows:
      {
	throw new PipelineException
	  ("Not implemented yet...");

	// ...

      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   Q U E U E   A D M I N I S T R A T I O N                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print a text representation of the given license key.
   */ 
  public void 
  printLicenseKey
  (
   String kname, 
   QueueMgrClient client
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 
    boolean first = true;
    boolean found = false;
    ArrayList<LicenseKey> keys = client.getLicenseKeys();
    for(LicenseKey key : keys) {
      if((kname == null) || key.getName().equals(kname)) {
	if(first) 
	  buf.append(tbar(80) + "\n");
	else 
	  buf.append("\n\n" + bar(80) + "\n");
	first = false;

	buf.append
	  ("License Key     : " + key.getName() + "\n" + 
	   "Available       : " + key.getAvailable() + "\n" + 
	   "License Scheme  : " + key.getScheme().toTitle() + "\n");

	switch(key.getScheme()) {
	case PerSlot:
	  buf.append("Max Slots       : " + key.getMaxSlots() + "\n");
	  break;
	
	case PerHost:
	  buf.append("Max Hosts       : " + key.getMaxHosts() + "\n");
	  break;
	  
	case PerHostSlot:
	  buf.append("Max Hosts       : " + key.getMaxHosts() + "\n" + 
		     "Max Host Slots  : " + key.getMaxHostSlots() + "\n");
	}

	buf.append("Description     : " + wordWrap(key.getDescription(), 18, 80));

	if(kname != null) {
	  found = true;
	  break;
	}
      }
    }

    if((kname != null) && !found) 
      throw new PipelineException
	("No license key named (" + kname + ") exists!");

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();
  }

  /**
   * Add a new license key.
   */ 
  public void 
  addLicenseKey
  (
   String kname, 
   String msg, 
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots,
   QueueMgrClient client
  )
    throws PipelineException
  {
    if(msg == null) 
      throw new PipelineException("The --msg option is required!");

    if(scheme == null) 
      throw new PipelineException
        ("One of the following options are required: " + 
	 "--per-slot, --per-host or --per-host-slot");

    switch(scheme) {
    case PerSlot:
      if(maxSlots == null) 
	throw new PipelineException
	  ("The --max-slots option is required when the --per-host option is given!");
      break;

    case PerHostSlot:
      if(maxHostSlots == null) 
	throw new PipelineException
	  ("The --max-host-slots option is required when the --per-host-slot " + 
	   "option is given!");      

    case PerHost:
      if(maxHosts == null) 
	throw new PipelineException
	  ("The --max-slots option is required when the --per-host or --per-host-slot " + 
	   "options are given!");
    }	

    LicenseKey key = null;
    try {
      key = new LicenseKey(kname, msg, scheme, maxSlots, maxHosts, maxHostSlots);
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException(ex.getMessage());
    }
    client.addLicenseKey(key);

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Added license key (" + kname + ").");
    LogMgr.getInstance().flush();
  }

  /**
   * Set the parameters of an existing license key. 
   */ 
  public void 
  setLicenseKey
  (
   String kname, 
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots,
   QueueMgrClient client
  )
    throws PipelineException
  {
    LicenseKey key = null;
    {
      ArrayList<LicenseKey> keys = client.getLicenseKeys();
      for(LicenseKey k : keys) {
	if(k.getName().equals(kname)) {
	  key = k;
	  break;
	}
      }
    }

    if(key == null) 
      throw new PipelineException
	("No license key named (" + kname + ") exists!");

    if(scheme != null)
      key.setScheme(scheme);

    try {
      if(maxSlots != null) 
	key.setMaxSlots(maxSlots);

      if(maxHosts != null) 
	key.setMaxHosts(maxHosts);

      if(maxHostSlots != null) 
	key.setMaxHostSlots(maxHostSlots);
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException(ex.getMessage());
    }
      
    client.setMaxLicenses(key.getName(), key.getScheme(), 
			  key.getMaxSlots(), key.getMaxHosts(), key.getMaxHostSlots());

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "License key (" + kname + ") updated.");
    LogMgr.getInstance().flush();
  }

  /**
   * Print a text representation of the given selection key.
   */ 
  public void 
  printSelectionKey
  (
   String kname, 
   QueueMgrClient client
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 
    boolean first = true;
    boolean found = false;
    ArrayList<SelectionKey> keys = client.getSelectionKeys();
    for(SelectionKey key : keys) {
      if((kname == null) || key.getName().equals(kname)) {
	if(first) 
	  buf.append(tbar(80) + "\n");
	else 
	  buf.append("\n\n" + bar(80) + "\n");
	first = false;

	buf.append
	  ("Selection Key : " + key.getName() + "\n" + 
	   "Description   : " + wordWrap(key.getDescription(), 16, 80));

	if(kname != null) {
	  found = true;
	  break;
	}
      }
    }

    if((kname != null) && !found) 
      throw new PipelineException
	("No selection key named (" + kname + ") exists!");

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();    
  }

  /**
   * Print a text representation of the given job server host.
   */ 
  public void 
  printHosts
  (
   String hname, 
   QueueMgrClient client
  ) 
    throws PipelineException 
  {
    TreeMap<String,QueueHostInfo> hosts = client.getHosts();
    ArrayList<QueueHostInfo> selected = new ArrayList<QueueHostInfo>();
    if(hname == null) {
      selected.addAll(hosts.values());
    }
    else {
      QueueHostInfo qinfo = hosts.get(hname);
      if(qinfo != null) 
	selected.add(qinfo); 
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(QueueHostInfo host : selected) {
      if(first) 
	buf.append(tbar(80) + "\n");
      else 
	buf.append("\n\n" + bar(80) + "\n");
      first = false;

      buf.append
	("Job Server         : " + host.getName() + "\n" + 
	 "Status             : " + host.getStatus() + "\n" +  
	 "OS                 : ");

      OsType os = host.getOsType();
      if(os != null) 
	buf.append(os.toString());
      else 
	buf.append("-");
      
      ResourceSample sample = host.getLatestSample();
      if(sample != null) {
	Integer numProcs = host.getNumProcessors();
	Long totalMem    = host.getTotalMemory();
	Long totalDisk   = host.getTotalDisk();

	if((numProcs != null) && (totalMem != null) && (totalDisk != null)) {
	  buf.append
	    ("\n" + 
	     "System Load        : " + 
	     String.format("%1$.2f", sample.getLoad()) + " (procs " + numProcs + ")\n" +
	     "Free Memory        : " + 
	     formatLong(sample.getMemory()) + " (total " + formatLong(totalMem) + ")" + "\n" +
	     "Free Disk          : " + 
	     formatLong(sample.getDisk()) + " (total " + formatLong(totalDisk) + ")" + "\n" +
	     "Jobs               : " + 
	     sample.getNumJobs() + " (slots " + host.getJobSlots() + ")");
	}
      }

      buf.append("\n" + 
		 "Reservation        : ");
      
      String reserve = host.getReservation();
      if(reserve != null) 
	buf.append(reserve);
      else 
	buf.append("-");

      buf.append("\n" + 
		 "Order              : " + host.getOrder());

      {
	String sgroup = host.getSelectionGroup();
	String sched  = host.getSelectionSchedule();
	buf.append
	  ("\n" + 
	   "Selection Schedule : " + ((sgroup != null) ? sgroup : "-") + "\n" + 
	   "Selection Group    : " + ((sched != null) ? sched : "-") + "\n");
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();      
  }
  
  /**
   * Print a text representation of job server histograms.
   */ 
  public void 
  printHistograms
  (
   QueueMgrClient client
  ) 
    throws PipelineException 
  {
    QueueHostHistograms hist = 
      client.getHostHistograms(QueueHostHistogramSpecs.getDefault());

    StringBuilder buf = new StringBuilder();
    {
      // TEMPORARY
      try {
	GlueEncoder ge = new GlueEncoderImpl("Histograms", hist);
	buf.append(ge.getText());
      }
      catch(GlueException ex) {
	buf.append
	  ("Unable to generate a Glue format representation of the histograms:\n  " + 
	   ex.getMessage());
      }
      // TEMPORARY
    }
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();      
  }


  /**
   * Edit the properties of the given host.
   */ 
  public void 
  editHost
  (
   String hname, 
   QueueHostStatusChange status, 
   String reserve, 
   boolean setReserve, 
   Integer order, 
   Integer slots, 
   String  selectionSchedule,
   boolean noSelectionSchedule,
   String  selectionGroup,
   boolean noSelectionGroup,
   QueueMgrClient client
  )
    throws PipelineException 
  {
    TreeMap<String,QueueHostInfo> hosts = client.getHosts();
    QueueHostInfo host = hosts.get(hname);
    if(host != null) {
      String group = null;
      boolean groupModified = false;
      if(selectionGroup != null) {
	group = selectionGroup;
	groupModified = true;
      }
      else if(noSelectionGroup)
	groupModified = true;

      String schedule = null;
      boolean scheduleModified = false;
      if(selectionSchedule != null) {
	schedule = selectionSchedule;
	scheduleModified = true;
      }
      else if(noSelectionSchedule)
	scheduleModified = true;

      QueueHostMod change = 
	new QueueHostMod(status, reserve, setReserve,  order, slots, 
			 group, groupModified, schedule, scheduleModified); 

      TreeMap<String,QueueHostMod> changes = new TreeMap<String,QueueHostMod>();
      changes.put(hname, change);

      client.editHosts(changes);
    }
  }
  
  /**
   * Edit the given host.
   */ 
  public void
  removeHost
  (
   String hname,
   QueueMgrClient client
  ) 
    throws PipelineException 
  {
    TreeSet<String> hnames = new TreeSet<String>();
    hnames.add(hname);

    client.removeHosts(hnames);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print information about all Editor plugins.
   */ 
  public void 
  editorGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getEditors();
    if(!versions.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 tbar(80) + "\n" + 
	 "  E D I T O R S");
	
      for(String vendor : versions.keySet()) {
	for(String name : versions.get(vendor).keySet()) {
	  for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	    BaseEditor plg = client.newEditor(name, vid, vendor);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Print information about all Action plugins.
   */ 
  public void 
  actionGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getActions();
    if(!versions.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 tbar(80) + "\n" + 
	 "  A C T I O N S");
      
      for(String vendor : versions.keySet()) {
	for(String name : versions.get(vendor).keySet()) {
	  for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	    BaseAction plg = client.newAction(name, vid, vendor);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Print information about all Comparator plugins.
   */ 
  public void 
  comparatorGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getComparators();
    if(!versions.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 tbar(80) + "\n" + 
	 "  C O M P A R A T O R S");
      
      for(String vendor : versions.keySet()) {
	for(String name : versions.get(vendor).keySet()) {
	  for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	    BaseComparator plg = client.newComparator(name, vid, vendor);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Print information about all Tool plugins.
   */ 
  public void 
  toolGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getTools();
    if(!versions.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 tbar(80) + "\n" + 
	 "  T O O L S"); 
      
      for(String vendor : versions.keySet()) {
	for(String name : versions.get(vendor).keySet()) {
	  for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	    BaseTool plg = client.newTool(name, vid, vendor);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Print information about all Archiver plugins.
   */ 
  public void 
  archiverGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getArchivers();
    if(!versions.isEmpty()) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 tbar(80) + "\n" + 
	 "  A R C H I V E R S");
      
      for(String vendor : versions.keySet()) {
	for(String name : versions.get(vendor).keySet()) {
	  for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	    BaseArchiver plg = client.newArchiver(name, vid, vendor);
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   P R E F E R E N C E S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Print a text representation of the default editor for a filename suffix.
   */ 
  public void
  printSuffixEditor
  (
   String suffix,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 
    boolean first = true;
    boolean found = false;
    TreeSet<SuffixEditor> editors = client.getSuffixEditors();
    for(SuffixEditor se : editors) {
      if((suffix == null) || se.getSuffix().equals(suffix)) {
	if(first) 
	  buf.append(tbar(80) + "\n");
	else 
	  buf.append("\n\n" + bar(80) + "\n");
	first = false;

	buf.append
	  ("Filename Suffix : " + se.getSuffix() + "\n" + 
	   "Description     : " + wordWrap(se.getDescription(), 16, 80) + "\n" +
	   "Default Editor  : " + se.getEditor());

	if(suffix != null) {
	  found = true;
	  break;
	}
      }
    }

    if((suffix != null) && !found) 
      throw new PipelineException
	("The filename suffix (" + suffix + ") does not exist!");

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();    
  }
  
  /**
   * Add or replace the default editor for a filename suffix.
   */ 
  public void
  addSuffixEditor
  (
   String suffix, 
   String msg, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    BaseEditor editor = 
      PluginMgrClient.getInstance().newEditor(editorName, editorVersionID, editorVendor);
    SuffixEditor se = new SuffixEditor(suffix, msg, editor);

    TreeSet<SuffixEditor> editors = client.getSuffixEditors();
    editors.add(se);
    client.setSuffixEditors(editors);
  }

  /**
   * Remove the default editor for a filename suffix.
   */ 
  public void
  removeSuffixEditor
  (
   String suffix,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeSet<SuffixEditor> old = client.getSuffixEditors();
    TreeSet<SuffixEditor> editors = new TreeSet<SuffixEditor>();
    for(SuffixEditor se : old) {
      if(!se.getSuffix().equals(suffix))
	editors.add(se);
    }
    client.setSuffixEditors(editors);
  }

  /**
   * Reset the suffix editors to site defaults.
   */ 
  public void
  resetSuffixEditors
  (
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    TreeSet<SuffixEditor> editors = client.getDefaultSuffixEditors();
    client.setSuffixEditors(editors);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   V I E W                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all nodes within the given working area which match the pattern.
   */ 
  public void
  releaseView
  (
   String author, 
   String view, 
   String pattern, 
   boolean removeFiles, 
   boolean removeArea,
   MasterMgrClient mclient
  ) 
    throws PipelineException
  {
    TreeSet<String> names = mclient.getWorkingNames(author, view, pattern);
    if(!names.isEmpty()) {
      StringBuilder buf = new StringBuilder();
      buf.append("Releasing Nodes:");
      for(String name : names)
	buf.append("\n  " + name);
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 buf.toString());

      mclient.release(author, view, names, removeFiles);
    }

    if(removeArea) {
      mclient.removeWorkingArea(author, view);
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Working Area (" + author + "|" + view + ") Removed.");
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   N O D E   V E R S I O N   C O M M A N D S                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print a text representation of the given working version.
   */ 
  public void
  printWorkingVersion
  (
   NodeID nodeID, 
   TreeSet sections, 
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    NodeMod mod = mclient.getWorkingVersion(nodeID);
    VersionID vid = mod.getWorkingID();

    StringBuilder buf = new StringBuilder();
    buf.append
      (tbar(80) + "\n" +
       "Working Node      : " + mod.getName() + "\n");

    printCommon(buf, mod, vid, sections, qclient);

    if(sections.contains("link") && mod.hasSources()) {
      buf.append
	("\n\n" +
	 pad("-- Upstream Links ", '-', 80));
      
      int wk = mod.getSourceNames().size();
      for(String sname : mod.getSourceNames()) {
	LinkMod link = mod.getSource(sname);
	buf.append
	  ("\n" +
	   "Source Node       : " + sname + "\n" +
	   "Link Policy       : " + link.getPolicy() + "\n" + 
	   "Link Relationship : " + link.getRelationship());
	
	switch(link.getRelationship()) {
	case OneToOne:
	  buf.append
	    ("\n" + 
	     "Frame Offset      : " + link.getFrameOffset()); 
	}

	if(wk > 0) 
	  buf.append("\n");
	wk--;
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();    
  }

  
  /**
   * Print a text representation of the given working version.
   */ 
  public void
  printCommon
  (
   StringBuilder buf, 
   NodeCommon com,
   VersionID vid, 
   TreeSet sections, 
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    BaseAction action = com.getAction();
    JobReqs jreqs = com.getJobRequirements();
    
    buf.append
      ("Primary Files     : " + com.getPrimarySequence());

    for(FileSeq fseq : com.getSecondarySequences()) {
      buf.append
	("\n" + 
	 "Secondary Files   : " + fseq);
    }
    
    if(sections.contains("vsn")) {
      String vstr = "(pending)";
      if(vid != null) 
	vstr = vid.toString();
      
      buf.append
	("\n\n" + 
	 pad("-- Versions ", '-', 80) + "\n" +
	 "Revision Number   : " + vstr);
    }
    
    if(sections.contains("prop")) {
      buf.append
	("\n\n" + 
	 pad("-- Properties ", '-', 80) + "\n" +
	 "Toolset           : " + com.getToolset() + "\n" +
	 "Editor            : " + com.getEditor());
    }
       
    if(sections.contains("act")) {
      buf.append
	("\n\n" + 
	 pad("-- Regeneration Action ", '-', 80) + "\n");

      if(action != null) {
	buf.append
	  ("Action            : " + action.getName() + "\n" +
	   "Version           : " + action.getVersionID() + "\n" + 
	   "Enabled           : " + (com.isActionEnabled() ? "YES" : "no"));
	
	if(action.hasSingleParams()) 
	  printSingleParams(action, action.getSingleLayout(), buf, 1);

	if(action.supportsSourceParams() && (action.getSourceNames().size() > 0)) {
	  buf.append("\n\n" +
		     pad("-- Per-Source Action Parameters ", '-', 80) + "\n");

	  boolean first = true;
	  for(String sname : action.getSourceNames()) {
	    buf.append((first ? "" : "\n\n") + 
		       "Source Node       : " + sname);
	    first = false;

	    for(String pname : action.getSourceLayout()) {
	      buf.append
		("\n" + 
		 pad(pname, 18) + ": " + action.getSourceParamValue(sname, pname));
	    }
	  }
	}
      }
      else {
	buf.append
	  ("Action            : (none)");	
      }	 
    }

    if(sections.contains("jreq") && (action != null)) {
      String batchSize = "-";
      if(com.getBatchSize() != null) 
	batchSize = com.getBatchSize().toString();

      buf.append
	("\n\n" +
	 pad("-- Job Requirements ", '-', 80) + "\n" +
	 "Overflow Policy   : " + com.getOverflowPolicy() + "\n" + 
	 "Execution Method  : " + com.getExecutionMethod() + "\n" + 
	 "Batch Size        : " + batchSize + "\n" + 
	 "\n" + 
	 "Priority          : " + jreqs.getPriority() + "\n" + 
	 "Ramp Up Interval  : " + jreqs.getRampUp() + "ms\n" +
	 "Maximum Load      : " + String.format("%1$.2f", jreqs.getMaxLoad()) + "\n" +
	 "Minimum Memory    : " + formatLong(jreqs.getMinMemory()) + "\n" +
	 "Minimum Disk      : " + formatLong(jreqs.getMinDisk()));
    }

    if(sections.contains("key") && (action != null)) {
      {
	buf.append
	  ("\n\n" +
	   pad("-- License Keys ", '-', 80));

	Set<String> keys = jreqs.getLicenseKeys();
	for(String kname : qclient.getLicenseKeyNames()) {
	  buf.append
	    ("\n" + 
	     pad(kname, 18) + ": " + (keys.contains(kname) ? "YES" : "no"));
	}
      }

      {
	buf.append
	  ("\n\n" +
	   pad("-- Selection Keys ", '-', 80));

	Set<String> keys = jreqs.getSelectionKeys();
	for(String kname : qclient.getSelectionKeyNames()) {
	  buf.append
	    ("\n" + 
	     pad(kname, 18) + ": " + (keys.contains(kname) ? "YES" : "no"));
	}
      }
    }
  }

  /**
   * Recursively print the single valued parameters.
   */ 
  private void 
  printSingleParams
  (
   BaseAction action, 
   LayoutGroup group, 
   StringBuilder buf, 
   int level 
  ) 
    throws PipelineException 
  {   
    buf.append("\n\n" +
	       pad(repeat('-', level*2) + " " + group.getNameUI() + " ", '-', 80));

    for(String pname : group.getEntries()) {
      buf.append("\n");	 
      if(pname != null) {
	if(action.getSingleParam(pname) != null) 
	  buf.append(pad(pname, 18) + ": " + action.getSingleParamValue(pname));
	else if(action.getPresetChoices(pname) != null) 
	  buf.append(pad(pname, 18) + ": PRESET");
      }
    }
    
    for(LayoutGroup sgroup : group.getSubGroups()) {
      printSingleParams(action, sgroup, buf, level+1);
    }
  }


  /**
   * Register a new working version.
   */ 
  public void 
  workingVersionRegister
  (
   NodeID nodeID, 
   FileSeq primary, 
   String toolset, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   Boolean noAction, 
   String actionName, 
   VersionID actionVersionID, 
   String actionVendor, 
   Boolean actionEnabled, 
   TreeMap params, 
   TreeMap sourceParams, 
   OverflowPolicy overflowPolicy,
   ExecutionMethod executionMethod,
   Integer batchSize,
   Integer rampUp, 
   Integer priority,
   Float maxLoad,
   Long minMemory,
   Long minDisk,
   TreeMap licenseKeys,
   TreeMap selectionKeys,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    NodeMod mod = null;
    try {
      if((toolset != null) && 
	 (!client.getToolsetNames().contains(toolset)))
	throw new PipelineException 
	  ("No toolset named (" + toolset + ") exists!");

      String tset = toolset;
      if(tset == null) 
	tset = client.getDefaultToolsetName();


      BaseEditor editor = null;
      {
	if(editorName == null) {
	  String suffix = primary.getFilePattern().getSuffix();
	  if(suffix != null) 
	    editor = client.getEditorForSuffix(suffix);
	}
	else {
	  PluginMgrClient pclient = PluginMgrClient.getInstance();
	  editor = pclient.newEditor(editorName, editorVersionID, editorVendor);
	}
      }
      
      mod = new NodeMod(nodeID.getName(), primary, new TreeSet<FileSeq>(), tset, editor);

      setActionProperties
	(mod, noAction, actionName, actionVersionID, actionVendor, actionEnabled, 
	 (TreeMap<String,String>) params, 
	 (TreeMap<String,TreeMap<String,String>>) sourceParams, 
	 overflowPolicy, executionMethod, batchSize,
	 priority, rampUp, maxLoad, minMemory, minDisk,
	 (TreeMap<String,Boolean>) licenseKeys, (TreeMap<String,Boolean>) selectionKeys,
	 null, null, null, null, 
	 client);
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException(ex.getMessage());
    }

    client.register(nodeID.getAuthor(), nodeID.getView(), mod);
  }

  /**
   * Set the properties of the given working version.
   */ 
  public void 
  workingVersionSet
  (
   NodeID nodeID, 
   String toolset, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   Boolean noAction, 
   String actionName, 
   VersionID actionVersionID, 
   String actionVendor, 
   Boolean actionEnabled, 
   TreeMap params, 
   TreeMap sourceParams, 
   OverflowPolicy overflowPolicy,
   ExecutionMethod executionMethod,
   Integer batchSize,
   Integer priority,
   Integer rampUp, 
   Float maxLoad,
   Long minMemory,
   Long minDisk,
   TreeMap licenseKeys,
   TreeMap selectionKeys,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    NodeMod mod = client.getWorkingVersion(nodeID);
    try {
      /* properties */ 
      {
	if(toolset != null) {
	  if(!client.getToolsetNames().contains(toolset)) 
	    throw new PipelineException 
	      ("No toolset named (" + toolset + ") exists!");
	  mod.setToolset(toolset);
	}
	
	{
	  BaseEditor editor = mod.getEditor(); 
	  if(editorName != null) {
	    PluginMgrClient pclient = PluginMgrClient.getInstance();
	    editor = pclient.newEditor(editorName, editorVersionID, editorVendor);
	  }
	  
	  mod.setEditor(editor); 
	}
      }

      /* previous job requirements (if any) */ 
      JobReqs prevJobReqs = mod.getJobRequirements();
      OverflowPolicy prevOverflowPolicy = mod.getOverflowPolicy();
      ExecutionMethod prevExecutionMethod = mod.getExecutionMethod();
      Integer prevBatchSize = mod.getBatchSize(); 

      /* actions and related properties */ 
      setActionProperties
	(mod, noAction, actionName, actionVersionID, actionVendor, actionEnabled, 
	 (TreeMap<String,String>) params, 
	 (TreeMap<String,TreeMap<String,String>>) sourceParams, 
	 overflowPolicy, executionMethod, batchSize,
	 priority, rampUp, maxLoad, minMemory, minDisk,
	 (TreeMap<String,Boolean>) licenseKeys, (TreeMap<String,Boolean>) selectionKeys,
	 prevJobReqs, prevOverflowPolicy, prevExecutionMethod, prevBatchSize, 
	 client);
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException(ex.getMessage());
    }

    client.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
  }

  /** 
   * Set the Action plugin and action related parameters of the given working version.
   */
  private void
  setActionProperties
  (
   NodeMod mod, 
   Boolean noAction, 
   String actionName, 
   VersionID actionVersionID, 
   String actionVendor, 
   Boolean actionEnabled, 
   TreeMap<String,String> params, 
   TreeMap<String,TreeMap<String,String>> sourceParams, 
   OverflowPolicy overflowPolicy,
   ExecutionMethod executionMethod,
   Integer batchSize,
   Integer priority,
   Integer rampUp, 
   Float maxLoad,
   Long minMemory,
   Long minDisk,
   TreeMap<String,Boolean> licenseKeys,
   TreeMap<String,Boolean> selectionKeys,
   JobReqs prevJobReqs,
   OverflowPolicy prevOverflowPolicy,
   ExecutionMethod prevExecutionMethod,
   Integer prevBatchSize,
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    /* actions */ 
    if((noAction != null) && noAction) {
      mod.setAction(null);	
    }
    else {
      if(actionName != null) {
	PluginMgrClient pclient = PluginMgrClient.getInstance();
	BaseAction action = pclient.newAction(actionName, actionVersionID, actionVendor);
	mod.setAction(action);
      }
	
      if(actionEnabled != null) 
	mod.setActionEnabled(actionEnabled);
      
      /* action parameters */ 
      if(!params.isEmpty() || !sourceParams.isEmpty()) {
	BaseAction action = mod.getAction();
	if(action == null) 
	  throw new PipelineException
	    ("No node (" + mod.getName() + ") has no Action plugin and therefore " + 
	     "cannot have its parameters set!");
	
	for(String pname : params.keySet()) {
	  String value = params.get(pname);
	  try {
	    ActionParam aparam = action.getSingleParam(pname);
	    if(aparam == null)
	      throw new PipelineException 
		("No parameter named (" + pname + ") exists for Action " + 
		 "(" + action.getName() + ")!");
	    
	    if(aparam instanceof BooleanActionParam) {
	      action.setSingleParamValue(pname, new Boolean(value));
	    }
	    else if(aparam instanceof IntegerActionParam) {
	      action.setSingleParamValue(pname, new Integer(value));
	    }
	    else if(aparam instanceof DoubleActionParam) {
	      action.setSingleParamValue(pname, new Double(value));
	    }
	    else if(aparam instanceof StringActionParam) {
	      action.setSingleParamValue(pname, value);
	    }
	    else if(aparam instanceof LinkActionParam) {
	      if(!mod.getSourceNames().contains(value))
		throw new PipelineException
		  ("The node (" + value + ") is not an upstream source of node " + 
		   "(" + mod.getName() + ") and is therefore not legal for the value " + 
		   "of parameter (" + pname + ")!");
	      
	      action.setSingleParamValue(pname, value);
	    }
	    else if(aparam instanceof EnumActionParam) {
	      EnumActionParam eparam = (EnumActionParam) aparam;
	      if(!eparam.getValues().contains(value)) {
		StringBuilder buf = new StringBuilder();
		buf.append("The value (" + value + ") is not one of the enumerations of " +
			   "parameter (" + pname + ")!  The valid values are:\n");
		for(String evalue : eparam.getValues()) 
		  buf.append("  " + evalue + "\n");
		throw new PipelineException(buf.toString());
	      }
	      
	      action.setSingleParamValue(pname, value);
	    }
	  }
	  catch(NumberFormatException ex) {
	    throw new PipelineException
	      ("The value (" + value + ") is not legal for parameter (" + pname + ")!");
	  }
	}
	
	for(String sname : sourceParams.keySet()) {
	  if(!mod.getSourceNames().contains(sname)) 
	    throw new PipelineException
	      ("The node (" + sname + ") is not an upstream source of node " + 
	       "(" + mod.getName() + ") and therefore cannot have per-source " + 
	       "parameters!");
	  
	  TreeMap<String,String> sparams = sourceParams.get(sname);
	  if(sparams == null) {
	    action.removeSourceParams(sname);
	  }
	  else {
	    for(String pname : sparams.keySet()) {
	      if(!action.hasSourceParams(sname)) 
		action.initSourceParams(sname);
	      
	      String value = sparams.get(pname);
	      try {
		ActionParam aparam = action.getSourceParam(sname, pname);
		if(aparam == null)
		  throw new PipelineException 
		    ("No per-source parameter named (" + pname + ") exists for Action " + 
		     "(" + action.getName() + ")!");

		if(aparam instanceof BooleanActionParam) {
		  action.setSourceParamValue(sname, pname, new Boolean(value));
		}
		else if(aparam instanceof IntegerActionParam) {
		  action.setSourceParamValue(sname, pname, new Integer(value));
		}
		else if(aparam instanceof DoubleActionParam) {
		  action.setSourceParamValue(sname, pname, new Double(value));
		}
		else if(aparam instanceof StringActionParam) {
		  action.setSourceParamValue(sname, pname, value);
		}
		else if(aparam instanceof LinkActionParam) {
		  if(!mod.getSourceNames().contains(value))
		    throw new PipelineException
		      ("The node (" + value + ") is not an upstream source of node " + 
		       "(" + mod.getName() + ") and is therefore not legal for the " + 
		       "value of per-source parameter (" + pname + ")!");
		  
		  action.setSourceParamValue(sname, pname, value);
		}
		else if(aparam instanceof EnumActionParam) {
		  EnumActionParam eparam = (EnumActionParam) aparam;
		  if(!eparam.getValues().contains(value)) {
		    StringBuilder buf = new StringBuilder();
		    buf.append("The value (" + value + ") is not one of the enumerations " + 
			       "of the per-source parameter (" + pname + ")!  The valid " + 
			       "values are:\n");
		    for(String evalue : eparam.getValues()) 
		      buf.append("  " + evalue + "\n");
		    throw new PipelineException(buf.toString());
		  }

		  action.setSourceParamValue(sname, pname, value);
		}
	      }
	      catch(NumberFormatException ex) {
		throw new PipelineException
		  ("The value (" + value + ") is not legal for per-source parameter " +
		   "(" + pname + ")!");
	      }
	    }
	  }
	}
	
	mod.setAction(action);
      }
      
      /* job requirements */ 
      {
	if(overflowPolicy != null) 
	  mod.setOverflowPolicy(overflowPolicy);
	else if(prevOverflowPolicy != null)
	  mod.setOverflowPolicy(prevOverflowPolicy);
	
	if(executionMethod != null) 
	  mod.setExecutionMethod(executionMethod);
	else if(prevExecutionMethod != null)
	  mod.setExecutionMethod(prevExecutionMethod);
	
	if(batchSize != null) 
	  mod.setBatchSize(batchSize);
	else if((prevBatchSize != null) && 
		(mod.getExecutionMethod() == ExecutionMethod.Parallel))
	  mod.setBatchSize(prevBatchSize);
	
	JobReqs jreqs = mod.getJobRequirements();
	if(prevJobReqs != null) 
	  jreqs = prevJobReqs;

	if(jreqs != null) {
	  if(priority != null) 
	    jreqs.setPriority(priority);
	  
	  if(rampUp != null) 
	    jreqs.setRampUp(rampUp);

	  if(maxLoad != null) 
	    jreqs.setMaxLoad(maxLoad);
	  
	  if(minMemory != null) 
	    jreqs.setMinMemory(minMemory);
	  
	  if(minDisk != null) 
	    jreqs.setMinDisk(minDisk);
	  
	  {
	    TreeMap<String,Boolean> keys = (TreeMap<String,Boolean>) licenseKeys;
	    for(String kname : keys.keySet()) {
	      boolean hasKey = keys.get(kname); 
	      if(hasKey) 
		jreqs.addLicenseKey(kname);
	      else 
		jreqs.removeLicenseKey(kname);
	    }
	  }
	  
	  {
	    TreeMap<String,Boolean> keys = (TreeMap<String,Boolean>) selectionKeys;
	    for(String kname : keys.keySet()) {
	      boolean hasKey = keys.get(kname); 
	      if(hasKey) 
		jreqs.addSelectionKey(kname);
	      else 
		jreqs.removeSelectionKey(kname);
	    }
	  }

	  mod.setJobRequirements(jreqs);
	}
      }
    }
  }

  /**
   * Launch the editor program for the given working version.
   */ 
  public void 
  workingEdit
  (
   NodeID nodeID, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   ArrayList frames, 
   ArrayList indices, 
   FileSeq fseq, 
   boolean wait, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    NodeMod mod = client.getWorkingVersion(nodeID);
    editCommon(nodeID, mod, editorName, editorVersionID, editorVendor, 
	       frames, indices, fseq, wait, client);
  }  

  /**
   * Launch the editor program for the given node version.
   */ 
  @SuppressWarnings("deprecation")
  private void 
  editCommon
  (
   NodeID nodeID, 
   NodeCommon com, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   ArrayList frames, 
   ArrayList indices, 
   FileSeq fseq, 
   boolean wait, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    FileSeq primary = com.getPrimarySequence();
    
    /* build the file sequences to edit */ 
    TreeSet<FileSeq> editSeqs = new TreeSet<FileSeq>();
    if(fseq != null) {
      boolean found = false;
      ArrayList<File> fs = fseq.getFiles();
      for(FileSeq nfseq : com.getSequences()) {
	if(nfseq.getFiles().containsAll(fs)) {
	  editSeqs.add(fseq);
	  found = true;
	  break;
	}
      }
	    
      if(!found) 
	throw new PipelineException
	  ("The given file sequence (" + fseq + ") does not match any of the primary or " +
	   "secondary file sequences of node (" + com.getName() + ")!");
    }
    else if(!indices.isEmpty()) {
      if(!primary.hasFrameNumbers()) 
	throw new PipelineException
	  ("The primary file sequence (" + primary + ") of node (" + com.getName() + ") " +
	   "does not have frame numbers, therefore the --index option cannot be used!");
	   
      for(int[] idx : (ArrayList<int[]>) indices) {
	try {
	  if(idx.length == 1) {
	    editSeqs.add(new FileSeq(primary, idx[0]));
	  }
	  else if(idx.length == 2) {
	    editSeqs.add(new FileSeq(primary, idx[0], idx[1]));
	  }
	  else {
	    throw new IllegalStateException(); 
	  }
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException
	    ("Illegal --index arguments!\n" + 
	     ex.getMessage());
	}
      }
    }
    else if(!frames.isEmpty()) {
      if(!primary.hasFrameNumbers()) 
	throw new PipelineException
	  ("The primary file sequence (" + primary + ") of node (" + com.getName() + ") " +
	   "does not have frame numbers, therefore the --frame option cannot be used!");

      FrameRange range = primary.getFrameRange();
      for(int[] frm : (ArrayList<int[]>) frames) {
	try {
	  if(frm.length == 1) {
	    editSeqs .add(new FileSeq(primary, range.frameToIndex(frm[0])));
	  }
	  else if(frm.length == 2) {
	    int s = range.frameToIndex(frm[0]);
	    int e = range.frameToIndex(frm[1]);
	    editSeqs.add(new FileSeq(primary, s, e));
	  }
	  else {
	    throw new IllegalStateException();
	  }
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException
	    ("Illegal --frame arguments!\n" + 
	     ex.getMessage());
	}
      }
    }
    else {
      editSeqs.add(primary);
    }
    if(editSeqs.isEmpty())
      throw new IllegalStateException();

    NodeMod mod = null;
    if(com instanceof NodeMod) 
      mod = (NodeMod) com;
    
    NodeVersion vsn = null;
    if(com instanceof NodeVersion) 
      vsn = (NodeVersion) com;
    
    /* create an editor plugin instance */ 
    BaseEditor editor = null;
    {
      if(editorName != null) 
	editor = PluginMgrClient.getInstance().newEditor
	           (editorName, editorVersionID, editorVendor);
      else 
	editor = com.getEditor();

      if(editor == null) 
	throw new PipelineException
	  ("No Editor plugin was specified for node (" + com.getName() + ")!");

      if(!editor.supports(PackageInfo.sOsType)) 
        throw new PipelineException
          ("The Editor plugin (" + editor.getName() + " v" + 
           editor.getVersionID() + ") from the vendor (" + editor.getVendor() + ") " + 
           "does not support the " + PackageInfo.sOsType.toTitle() + " operating system!");
    }
    
    /* lookup the toolset environment */ 
    TreeMap<String,String> env = null;
    {
      String tname = com.getToolset();
      if(tname == null) 
	throw new PipelineException
	  ("No toolset was specified for node (" + com.getName() + ")!");

      String author = null;
      String view   = null;
      if(mod != null) {
	author = nodeID.getAuthor();
	view   = nodeID.getView();
      }
      env = client.getToolsetEnvironment(author, view, tname, PackageInfo.sOsType);
      
      /* override these since the editor will be run as the current user */ 
      switch(PackageInfo.sOsType) {
      case Unix:
      case MacOS:
	{
	  Path home = new Path(PackageInfo.sHomePath, PackageInfo.sUser);
	  env.put("HOME", home.toOsString()); 
	  env.put("USER", PackageInfo.sUser);
	}
	break;

      case Windows:
	throw new PipelineException
	  ("Not implemented yet...");

	// FIX THIS: handle Windows specific vars here...
      }
    }
    
    /* get the directory containing the files */ 
    File dir = null; 
    {
      if(mod != null) {
	Path wpath = 
	  new Path(PackageInfo.sWorkPath, 
		   nodeID.getAuthor() + "/" + nodeID.getView() + "/" + mod.getName());
	dir = wpath.getParentPath().toFile();
      }
      else if(vsn != null) {
	Path path = new Path(PackageInfo.sRepoPath,  
			     vsn.getName() + "/" + vsn.getVersionID());
	dir = path.toFile(); 
      }
      else {
	throw new IllegalStateException(); 
      }
    }

    /* launch an editor for each file sequence */ 
    TreeMap<Long,SubProcessLight> procs = new TreeMap<Long,SubProcessLight>();
    for(FileSeq fs : editSeqs) {
      editor.makeWorkingDirs(dir);

      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info, 
	 "Editing: " + fs + " with " + 
	 editor.getName() + " (v" + editor.getVersionID() + ")");
      
      SubProcessLight proc = editor.prep(PackageInfo.sUser, fs, env, dir);
      if(proc != null) 
	proc.start();
      else 
	proc = editor.launch(fs, env, dir);

      Long editID = client.editingStarted(nodeID, editor);
      procs.put(editID, proc);
    }
    LogMgr.getInstance().flush();
      
    /* wait around for the results? */ 
    if(wait) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "\n" + 
	 "Waiting for Editor(s) to exit...");
      for(Long editID : procs.keySet()) {
	SubProcessLight proc = procs.get(editID); 
	try {
	  proc.join();
	  
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info, 
	     tbar(80) + "\n" +
	     "Editor Process : " + wordWrap(proc.getCommand(), 17, 80) + "\n" + 
	     "Exit Code      : " + proc.getExitCode() + " " +
	       (proc.wasSuccessful() ? "(success)" : "(failed)") + "\n" +
	     "\n" +
	     pad("-- Output ", '-', 80) + "\n" +
	     proc.getStdOut() + "\n" + 
	     pad("-- Errors ", '-', 80) + "\n" +
	     proc.getStdErr());
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe, 
	     "Interrupted while waiting on an Editor Process to exit!");
	}
	finally {
	  client.editingFinished(editID);
	}
      }
    }

    /* give the Editor threads a chance to start... */ 
    else {
      for(Long editID : procs.keySet()) {
	SubProcessLight proc = procs.get(editID); 
	while(true) {
	  if(proc.hasStarted()) 
	    break;

	  try {
	    Thread.sleep(500);
	  }
	  catch(InterruptedException ex) {
	  }
	}

	client.editingFinished(editID);
      }
    }
  }  

  /**
   * Submit the group of jobs needed to regenerate the selected {@link QueueState#Stale Stale}
   * files associated with the tree of nodes rooted at the given node. <P> 
   */ 
  public void 
  workingSubmitJobs
  (
   NodeID nodeID, 
   ArrayList frames, 
   ArrayList indices, 
   boolean wait, 
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  { 
    NodeMod mod = mclient.getWorkingVersion(nodeID);
    TreeSet<Integer> frameIndices = 
      buildFrameIndices(mod, (ArrayList<int[]>) frames, (ArrayList<int[]>) indices);
    QueueJobGroup group = mclient.submitJobs(nodeID, frameIndices);

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       "Submitted Job Group: [" + group.getGroupID() + "] " + group.getRootPattern());
    LogMgr.getInstance().flush();        
    
    if(wait) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
	 "Waiting for jobs to complete...");
      LogMgr.getInstance().flush();   

      TreeSet<Long> groupIDs = new TreeSet<Long>();
      groupIDs.add(group.getGroupID());

      while(true) {
	TreeMap<Long,JobStatus> table = qclient.getJobStatus(groupIDs);

	boolean done = true; 
	boolean failed = false;
	for(JobStatus status : table.values()) {
	  switch(status.getState()) {
	  case Queued: 
	  case Preempted:
	  case Paused:
	  case Running:
	    done = false;
	    break;

	  case Aborted:
	  case Failed:
	    failed = true;
	  }
	}

	if(done) {
	  for(JobStatus status : table.values()) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       pad("Job [" + status.getJobID() + "]: ", ' ', 15) + 
	       pad(status.getState().toTitle(), ' ', 15) + 
	       "(" + status.getTargetSequence() + ")");
	  }
	  
	  if(failed) 
	    throw new PipelineException("Jobs Failed.");
	  else {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Info,
	       "Jobs Completed Successfully.");
	    LogMgr.getInstance().flush(); 
	    return;
	  }
	}
	
	try {
	  Thread.sleep(5000);
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while waiting for jobs to complete!");
	}
      }
    }
  }  
  
  /**
   * Remove the working area files associated with the given node. <P>  
   */ 
  public void 
  workingRemoveFiles
  (
   NodeID nodeID, 
   ArrayList frames, 
   ArrayList indices, 
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    NodeMod mod = client.getWorkingVersion(nodeID);
    TreeSet<Integer> frameIndices = 
      buildFrameIndices(mod, (ArrayList<int[]>) frames, (ArrayList<int[]>) indices);
    client.removeFiles(nodeID, frameIndices);
  }  
  
  /**
   * Generate a set of individual frame indices from list of frame ranges or index ranges. 
   */ 
  private TreeSet<Integer>
  buildFrameIndices
  (
    NodeMod mod, 
    ArrayList<int[]> frames, 
    ArrayList<int[]> indices
  ) 
    throws PipelineException
  {
    TreeSet<Integer> frameIndices = null;

    FileSeq primary = mod.getPrimarySequence();
    if(!indices.isEmpty()) {
      if(!primary.hasFrameNumbers()) 
	throw new PipelineException
	  ("The primary file sequence (" + primary + ") of node (" + mod.getName() + ") " +
	   "does not have frame numbers, therefore the --index option cannot be used!");
	   
      frameIndices = new TreeSet<Integer>();
      for(int[] idx : indices) {
	if(idx.length == 1) {
	  if((idx[0] >= 0) && (idx[0] < primary.numFrames()))
	     frameIndices.add(idx[0]); 
	  else
	    throw new PipelineException
	     ("The given frame index (" + idx[0] + ") was not valid for the range: " + 
	      "[0," + primary.numFrames() + ")!"); 
	}
	else if(idx.length == 2) {
	  int wk;
	  for(wk=idx[0]; wk<=idx[1]; wk++) {
	    if((idx[0] >= 0) && (idx[0] < primary.numFrames()))
	      frameIndices.add(wk); 
	    else
	      throw new PipelineException
		("The given frame index (" + wk + ") was not valid for the range: " + 
		 "[0," + primary.numFrames() + ")!"); 
	  }
	}
	else {
	  throw new IllegalStateException(); 
	}
      }
    }
    else if(!frames.isEmpty()) {
      if(!primary.hasFrameNumbers()) 
	throw new PipelineException
	  ("The primary file sequence (" + primary + ") of node (" + mod.getName() + ") " +
	   "does not have frame numbers, therefore the --frame option cannot be used!");

      frameIndices = new TreeSet<Integer>();
      FrameRange range = primary.getFrameRange();
      for(int[] frm : frames) {
	try {
	  if(frm.length == 1) {
	    frameIndices.add(range.frameToIndex(frm[0]));
	  }
	  else if(frm.length == 2) {
	    int s = range.frameToIndex(frm[0]);
	    int e = range.frameToIndex(frm[1]);
	    int wk;
	    for(wk=s; wk<=e; wk++) 
	      frameIndices.add(wk);
	  }
	  else {
	    throw new IllegalStateException(); 
	  }
	}
	catch(IllegalArgumentException ex) {
	  throw new PipelineException
	    ("Illegal --frame arguments!\n" + 
	     ex.getMessage());
	}
      }
    }

    return frameIndices;
  }

  /**
   * Print the working area views containing the given node. 
   */ 
  public void 
  viewsContaining
  (
   String name, 
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    TreeMap<String,TreeSet<String>> areas = client.getWorkingAreasContaining(name);
    for(String author : areas.keySet()) {
      for(String view : areas.get(author)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   author + "|" + view); 
      }
    }
    LogMgr.getInstance().flush();   
  }  
  
  /**
   * Print the working area views currently editing the given node. 
   */ 
  public void 
  viewsEditing
  (
   String name, 
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    TreeMap<String,TreeSet<String>> areas = client.getWorkingAreasEditing(name);
    for(String author : areas.keySet()) {
      for(String view : areas.get(author)) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   author + "|" + view); 
      }
    }
    LogMgr.getInstance().flush();   
  }  
  

  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   N O D E   V E R S I O N   C O M M A N D S                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print a text representation of the given checked-in version.
   */ 
  public void
  printCheckedInVersion
  (
   String name, 
   TreeSet<VersionID> vids, 
   boolean latest, 
   TreeSet sections, 
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    ArrayList<NodeVersion> versions = new ArrayList<NodeVersion>();
    {
      TreeSet<VersionID> avids = mclient.getCheckedInVersionIDs(name);
      TreeSet<VersionID> svids = new TreeSet<VersionID>();
      
      if(!vids.isEmpty()) {
	for(VersionID vid : vids) {
	  if(avids.contains(vid)) 
	    svids.add(vid);
	  else 
	    throw new PipelineException
	      ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
	}
      }
      
      if(latest) 
	svids.add(avids.last());
      else if(svids.isEmpty()) 
	svids.addAll(avids);
      
      switch(svids.size()) {
      case 0:
	break; 

      case 1: 
	versions.add(mclient.getCheckedInVersion(name, svids.first()));
	break;

      default:
	versions.addAll(mclient.getAllCheckedInVersions(name).values());
      }
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(NodeVersion vsn : versions) {
      if(!first) 
	buf.append("\n\n");
      first = false;

      buf.append
	(tbar(80) + "\n" +
	 "Checked-In Node   : " + vsn.getName() + " (v" + vsn.getVersionID() + ")\n");

      printCommon(buf, vsn, vsn.getVersionID(), sections, qclient);

      if(sections.contains("link") && vsn.hasSources()) {
	buf.append
	  ("\n\n" +
	   pad("-- Upstream Links ", '-', 80));
	
	int wk = vsn.getSourceNames().size();
	for(String sname : vsn.getSourceNames()) {
	  LinkVersion link = vsn.getSource(sname);
	  buf.append
	    ("\n" +
	     "Source Node       : " + sname + " (v" + link.getVersionID() + ")\n" +
	     "Link Policy       : " + link.getPolicy() + "\n" + 
	     "Link Relationship : " + link.getRelationship());
	  
	  switch(link.getRelationship()) {
	  case OneToOne:
	    buf.append
	      ("\n" + 
	       "Frame Offset      : " + link.getFrameOffset()); 
	  }
	  
	  if(wk > 0) 
	    buf.append("\n");
	  wk--;
	}
      }

      if(sections.contains("dlink")) {
	DoubleMap<String,VersionID,LinkVersion> dlinks = 
	  mclient.getDownstreamCheckedInLinks(vsn.getName(), vsn.getVersionID());

	if((dlinks != null) && !dlinks.isEmpty()) {
	  buf.append
	    ("\n\n" +
	     pad("-- Downstream Links ", '-', 80));
	  
	  boolean firstLink = true;
	  for(String dname : dlinks.keySet()) {
	    for(VersionID dvid : dlinks.keySet(dname)) {
	      LinkVersion link = dlinks.get(dname, dvid); 

	      if(!firstLink) 
		buf.append("\n");
	      firstLink = false;
	      
	      buf.append
		("\n" +
		 "Target Node       : " + dname + " (v" + dvid + ")\n" +
		 "Link Policy       : " + link.getPolicy() + "\n" + 
		 "Link Relationship : " + link.getRelationship());
	  
	      switch(link.getRelationship()) {
	      case OneToOne:
		buf.append
		  ("\n" + 
		   "Frame Offset      : " + link.getFrameOffset()); 
	      }	  
	    }
	  }
	}
      }
    }
      
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();    
  }
  
  /**
   * Print a text representation of the given checked-in version.
   */ 
  public void
  printCheckedInHistory
  (
   String name, 
   TreeSet<VersionID> vids, 
   boolean latest, 
   MasterMgrClient mclient
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,LogMessage> logs = new TreeMap<VersionID,LogMessage>();
    {
      TreeSet<VersionID> avids = mclient.getCheckedInVersionIDs(name);
      TreeSet<VersionID> svids = new TreeSet<VersionID>();
      
      if(!vids.isEmpty()) {
	for(VersionID vid : vids) {
	  if(avids.contains(vid)) 
	    svids.add(vid);
	  else 
	    throw new PipelineException
	      ("No checked-in version (" + vid + ") of node (" + name + ") exists!");
	}
      }
      
      if(latest) 
	svids.add(avids.last());
      else if(svids.isEmpty()) 
	svids.addAll(avids);
      
      TreeMap<VersionID,LogMessage> history = mclient.getHistory(name);
      for(VersionID vid : svids) {
	LogMessage msg = history.get(vid);
	if(msg != null) 
	  logs.put(vid, msg);
      }		
    }

    StringBuilder buf = new StringBuilder();
    buf.append
      (tbar(80) + "\n" +
       "Checked-In Node  : " + name);
    
    for(VersionID vid : logs.keySet()) {
      LogMessage msg = logs.get(vid);
      buf.append
	("\n\n" + 
	 bar(80) + "\n" +
	 "Revision Number  : " + vid + "\n" + 
	 "Created          : " + (TimeStamps.format(msg.getTimeStamp()) + " by (" + 
				   msg.getAuthor()) + ")\n" +
	 "Check-In Message : " + wordWrap(msg.getMessage(), 20, 80));
    }
      
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();  
  }

  /**
   * Launch the editor program for the given working version.
   */ 
  public void 
  checkedInView
  (
   String name, 
   VersionID vid, 
   String editorName, 
   VersionID editorVersionID, 
   String editorVendor, 
   ArrayList frames, 
   ArrayList indices, 
   FileSeq fseq, 
   boolean wait, 
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    NodeVersion vsn = client.getCheckedInVersion(name, vid);
    editCommon(null, vsn, editorName, editorVersionID, editorVendor, 
	       frames, indices, fseq, wait, client);
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   N O D E   C O M M A N D S                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print a text representation of the node status.
   */ 
  public void
  printNodeStatus
  (
   NodeID nodeID, 
   boolean briefFormat, 
   boolean printUpstream,
   boolean lightweight, 
   boolean printLinkGraph, 
   boolean printGlue, 
   TreeSet sections, 
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    NodeStatus root = null;
    TreeMap<String,NodeStatus> table = new TreeMap<String,NodeStatus>();
    {
      root = mclient.status(nodeID, lightweight);
      if(printUpstream) 
	unpackStatus(root, table); 
      else 
	table.put(root.getName(), root);
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for(String name : table.keySet()) {
      NodeStatus status = table.get(name);
      NodeDetails details = status.getDetails();
      if(briefFormat) {
	if(!first) 
	  buf.append("\n");
	first = false;

        if(details.isLightweight()) 
          buf.append("- - ");
        else 
          buf.append(details.getOverallNodeState().toSymbol() + " " + 
                     details.getOverallQueueState().toSymbol() + " ");

        buf.append(status.getName()); 
      }
      else {
	NodeMod mod = details.getWorkingVersion();
	NodeVersion vsn = details.getLatestVersion();

	if(!first) 
	  buf.append("\n\n");
	first = false;
	
	buf.append
	  (tbar(80) + "\n" +
	   "Node Name         : " + name + "\n");
	
	{
	  String mstr = "-";
	  if(mod != null) 
	    mstr = mod.getPrimarySequence().toString();
	  
	  String vstr = "-";
	  if(vsn != null)
	    vstr = vsn.getPrimarySequence().toString();
	  
	  buf.append
	    ("Primary Files     : " + pad(mstr, ' ', 30) + " : " + vstr); 
	}
	
	{
	  TreeSet<FileSeq> mseqs = new TreeSet<FileSeq>();
	  TreeSet<FileSeq> vseqs = new TreeSet<FileSeq>();
	  TreeSet<FileSeq> aseqs = new TreeSet<FileSeq>();
	  
	  if(mod != null) 
	    mseqs.addAll(mod.getSecondarySequences());
	  
	  if(vsn != null)
	    vseqs.addAll(vsn.getSecondarySequences());

	  aseqs.addAll(mseqs);
	  aseqs.addAll(vseqs);
	  
	  for(FileSeq fseq : aseqs) {
	    buf.append
	      ("\n" + 
	       "Secondary Files   : " + 
	       pad(mseqs.contains(fseq) ? fseq.toString() : "-", ' ', 30) + " : " + 
	       (vseqs.contains(fseq) ? fseq.toString() : "-"));
	  }
	}

	if(!details.isLightweight()) {
          buf.append
            ("\n\n" + 
             pad("-- Overall State ", '-', 80) + "\n" +
             "Node State        : " + details.getOverallNodeState().toTitle() + "\n" + 
             "Queue State       : " + details.getOverallQueueState().toTitle()); 
        }
	
	if(sections.contains("vsn")) {
	  String mstr = null;
	  String vstr = null;
	  switch(details.getVersionState()) {
	  case Pending:
	    mstr = "-";
	    vstr = "-";
	    break;
	    
	  case CheckedIn:
	    mstr = ("-");
	    vstr = vsn.getVersionID().toString();
	    break;
	    
	  case Identical:
	  case NeedsCheckOut:
	    mstr = mod.getWorkingID().toString();
	    vstr = vsn.getVersionID().toString();
	  }
	  
	  buf.append
	    ("\n\n" + 
	     pad("-- Versions ", '-', 80) + "\n" +
	     "Version State     : " + details.getVersionState().toTitle() + "\n" +
	     "Revision Number   : " + 
	     pad(mstr + " (working)", ' ', 30) + " : " + vstr + " (latest)"); 
	}
      
	if(sections.contains("prop")) {
	  String mToolset = "-";
	  BaseEditor mEditor = null;
	  if(mod != null) {
	    mToolset = mod.getToolset();
	    mEditor  = mod.getEditor();
	  }

	  String vToolset = "-";
	  BaseEditor vEditor = null; 
	  if(vsn != null) {
	    vToolset = vsn.getToolset();
	    vEditor  = vsn.getEditor();
	  }

	  buf.append
	    ("\n\n" + 
	     pad("-- Properties ", '-', 80) + "\n" +
	     "Property State    : " + details.getPropertyState().toTitle() + "\n" +
	     "Toolset           : " + pad(mToolset, ' ', 30) + " : " + vToolset);

	  if((mEditor != null) || (vEditor != null)) {
	    String mname = "-";
	    String mvstr = "-";
	    String mvend = "-";
	    if(mEditor != null) {
	      mname = mEditor.getName();
	      mvstr = mEditor.getVersionID().toString();
	      mvend = mEditor.getVendor();
	    }

	    String vname = "-";
	    String vvstr = "-";
	    String vvend = "-";
	    if(vEditor != null) {
	      vname = vEditor.getName();
	      vvstr = vEditor.getVersionID().toString();
	      vvend = vEditor.getVendor();
	    }

	    buf.append
	      ("\n\n" + 
	       "Editor            : " + pad(mname, ' ', 30) + " : " + vname + "\n" +
	       "Version           : " + pad(mvstr, ' ', 30) + " : " + vvstr + "\n" +
	       "Vendor            : " + pad(mvend, ' ', 30) + " : " + vvend);
	  }
	}


	BaseAction mAction = null;
	if(mod != null) 
	  mAction = mod.getAction();
	
	BaseAction vAction = null;
	if(vsn != null) 
	  vAction = vsn.getAction();

	if(sections.contains("act")) {
	  buf.append
	    ("\n\n" + 
	     pad("-- Regeneration Action ", '-', 80) + "\n");
	  
	  if((mAction != null) || (vAction != null)) {
	    String mname    = "-";
	    String mvstr    = "-";
	    String mvend    = "-";
	    String menabled = "-";
	    if(mAction != null) {
	      mname = mAction.getName();
	      mvstr = mAction.getVersionID().toString();
	      mvend = mAction.getVendor();

	      if(mod != null) 
		menabled = (mod.isActionEnabled() ? "YES" : "no");
	    }

	    String vname    = "-";
	    String vvstr    = "-";
	    String vvend    = "-";
	    String venabled = "-";
	    if(vAction != null) {
	      vname = vAction.getName();
	      vvstr = vAction.getVersionID().toString();
	      vvend = vAction.getVendor();

	      if(vsn != null) 
		venabled = (vsn.isActionEnabled() ? "YES" : "no");
	    }

	    buf.append
	      ("Action            : " + pad(mname, ' ', 30) + " : " + vname + "\n" +
	       "Version           : " + pad(mvstr, ' ', 30) + " : " + vvstr + "\n" +
	       "Vendor            : " + pad(mvend, ' ', 30) + " : " + vvend + "\n" +
	       "Enabled           : " + pad(menabled, ' ', 30) + " : " + venabled);
	    
	    if(((mAction != null) && mAction.hasSingleParams()) || 
	       ((vAction != null) && vAction.hasSingleParams())) {

	      if((mAction != null) && mAction.hasSingleParams()) 
		printBothSingleParams(mAction, vAction, mAction.getSingleLayout(), buf, 1);
	      else if((vAction != null) && vAction.hasSingleParams()) 
		printCheckedInSingleParams(vAction, vAction.getSingleLayout(), buf, 1);
	    }
	    
	    if(((mAction != null) && mAction.supportsSourceParams() && 
		(mAction.getSourceNames().size() > 0)) || 
	       ((vAction != null) && vAction.supportsSourceParams() && 
		(vAction.getSourceNames().size() > 0))) {

	      buf.append("\n\n" +
			 pad("-- Per-Source Action Parameters ", '-', 80) + "\n");
	      
	      boolean afirst = true;
	      if((mAction != null) && mAction.supportsSourceParams() && 
		 (mAction.getSourceNames().size() > 0)) {

		for(String sname : mAction.getSourceNames()) {
		  buf.append((afirst ? "" : "\n\n") + 
			     "Source Node       : " + sname);
		  afirst = false;
		
		  for(String pname : mAction.getSourceLayout()) {
		    String mstr = "-";
		    if(mAction.getSourceParamValue(sname, pname) != null) 
		      mstr = mAction.getSourceParamValue(sname, pname).toString();
		    
		    String vstr = "-";
		    if((vAction != null) && 
		       vAction.getName().equals(mAction.getName()) && 
		       vAction.getVersionID().equals(mAction.getVersionID()) && 
		       (vAction.getSourceParamValue(sname, pname) != null))
		      vstr = vAction.getSourceParamValue(sname, pname).toString();

		    buf.append
		      ("\n" + 
		       pad(pname, 18) + ": " + pad(mstr, ' ', 30) + " : " + vstr);
		  }
		}
	      }
	      else if((vAction != null) && vAction.supportsSourceParams() && 
		      (vAction.getSourceNames().size() > 0)) {
		
		for(String sname : vAction.getSourceNames()) {
		  buf.append((afirst ? "" : "\n\n") + 
			     "Source Node       : " + sname);
		  afirst = false;
		
		  for(String pname : vAction.getSourceLayout()) {
		    String vstr = "-";
		    if(vAction.getSourceParamValue(sname, pname) != null)
		      vstr = vAction.getSourceParamValue(sname, pname).toString();

		    buf.append
		      ("\n" + 
		       pad(pname, 18) + ": " + pad("-", ' ', 30) + " : " + vstr);
		  }
		}		
	      }
	    }
	  }
	  else {
	    buf.append
	      ("Action            : (none)");	
	  }	 
	}


	JobReqs mjreqs = null;
	if(mAction != null) 
	  mjreqs = mod.getJobRequirements();

	JobReqs vjreqs = null;
	if(vAction != null) 
	  vjreqs = vsn.getJobRequirements();

	if(sections.contains("jreq") && ((mAction != null) || (vAction != null))) {
	  String mop = "-";
	  String mem = "-";
	  String mbs = "-";
	  String mp  = "-";
	  String mru = "-";
	  String mml = "-";
	  String mmm = "-";
	  String mmd = "-";
	  if(mAction != null) {
	    mop = mod.getOverflowPolicy().toString();
	    mem = mod.getExecutionMethod().toString();

	    if(mod.getBatchSize() != null) 
	      mbs = mod.getBatchSize().toString();
	    
	    mp  = String.valueOf(mjreqs.getPriority());
	    mru = String.valueOf(mjreqs.getRampUp());
	    mml = String.format("%1$.2f", mjreqs.getMaxLoad());
	    mmm = formatLong(mjreqs.getMinMemory()); 
	    mmd = formatLong(mjreqs.getMinDisk());
	  }
	
	  String vop = "-";
	  String vem = "-";
	  String vbs = "-";
	  String vp  = "-";
	  String vru = "-";
	  String vml = "-";
	  String vmm = "-";
	  String vmd = "-";
	  if(vAction != null) {
	    vop = vsn.getOverflowPolicy().toString();
	    vem = vsn.getExecutionMethod().toString();

	    if(vsn.getBatchSize() != null) 
	      vbs = vsn.getBatchSize().toString();
	    
	    vp  = String.valueOf(vjreqs.getPriority());
	    vru = String.valueOf(vjreqs.getRampUp());
	    vml = String.format("%1$.2f", vjreqs.getMaxLoad());
	    vmm = formatLong(vjreqs.getMinMemory()); 
	    vmd = formatLong(vjreqs.getMinDisk());
	  }
	  
	  buf.append
	    ("\n\n" +
	     pad("-- Job Requirements ", '-', 80) + "\n" +
	     "Overflow Policy   : " + pad(mop, ' ', 30) + " : " + vop + "\n" +
	     "Execution Method  : " + pad(mem, ' ', 30) + " : " + vem + "\n" +
	     "Batch Size        : " + pad(mbs, ' ', 30) + " : " + vbs + "\n" +
	     "\n" + 
	     "Priority          : " + pad(mp, ' ', 30) + " : " + vp + "\n" +
	     "Ramp Up Interval  : " + pad(mru, ' ', 30) + " : " + vru + "\n" +
	     "Maximum Load      : " + pad(mml, ' ', 30) + " : " + vml + "\n" +
	     "Minimum Memory    : " + pad(mmm, ' ', 30) + " : " + vmm + "\n" +
	     "Minimum Disk      : " + pad(mmd, ' ', 30) + " : " + vmd);
	}
	
	
	if(sections.contains("key") &&  ((mAction != null) || (vAction != null))) {
	  {
	    buf.append
	      ("\n\n" +
	       pad("-- License Keys ", '-', 80));

	    Set<String> mkeys = null;
	    if(mjreqs != null) 
	      mkeys = mjreqs.getLicenseKeys();
 
	    Set<String> vkeys = null;
	    if(vjreqs != null) 
	      vkeys = vjreqs.getLicenseKeys();
 
	    for(String kname : qclient.getLicenseKeyNames()) {
	      String mstr = "-";
	      if(mkeys != null) 
		mstr = (mkeys.contains(kname) ? "YES" : "no");

	      String vstr = "-";
	      if(vkeys != null) 
		vstr = (vkeys.contains(kname) ? "YES" : "no");

	      buf.append
		("\n" + 
		 pad(kname, 18) + ": " + pad(mstr, ' ', 30) + " : " + vstr);
	    }
	  }
	
	  {
	    buf.append
	      ("\n\n" +
	       pad("-- Selection Keys ", '-', 80));

	    Set<String> mkeys = null;
	    if(mjreqs != null) 
	      mkeys = mjreqs.getSelectionKeys();
 
	    Set<String> vkeys = null;
	    if(vjreqs != null) 
	      vkeys = vjreqs.getSelectionKeys();
 
	    for(String kname : qclient.getSelectionKeyNames()) {
	      String mstr = "-";
	      if(mkeys != null) 
		mstr = (mkeys.contains(kname) ? "YES" : "no");

	      String vstr = "-";
	      if(vkeys != null) 
		vstr = (vkeys.contains(kname) ? "YES" : "no");

	      buf.append
		("\n" + 
		 pad(kname, 18) + ": " + pad(mstr, ' ', 30) + " : " + vstr);
	    }
	  }
	}

	if(sections.contains("link") && 
	   (((mod != null) && mod.hasSources()) || ((vsn != null) && (vsn.hasSources())))) {
	  
	  buf.append
	    ("\n\n" +
	     pad("-- Upstream Links ", '-', 80));
	  
	  TreeSet<String> snames = new TreeSet<String>();
	  if(mod != null)
	    snames.addAll(mod.getSourceNames());
	  if(vsn != null) 
	    snames.addAll(vsn.getSourceNames());

	  int wk = snames.size();
	  for(String sname : snames) {
	    String mname = "-";
	    String mp = "-";
	    String mr = "-";
	    String mo = "-";
	    if(mod != null) {
	      LinkMod link = mod.getSource(sname);
	      if(link != null) {
		mname = sname;
		mp = link.getPolicy().toTitle();
		mr = link.getRelationship().toTitle();
		
		switch(link.getRelationship()) {
		case OneToOne:
		  mo = link.getFrameOffset().toString();
		}
	      }
	    }

	    String vname = "-";
	    String vp = "-";
	    String vr = "-";
	    String vo = "-";
	    if(vsn != null) {
	      LinkVersion link = vsn.getSource(sname);
	      if(link != null) {
		vname = (sname + " (v" + link.getVersionID() + ")");
		vp = link.getPolicy().toTitle();
		vr = link.getRelationship().toTitle();
		
		switch(link.getRelationship()) {
		case OneToOne:
		  vo = link.getFrameOffset().toString();
		}
	      }
	    }

	    buf.append
	      ("\n" +
	       "Source Node       : " + pad(mname, ' ', 30) + " : " + vname + "\n" +
	       "Link Policy       : " + pad(mp, ' ', 30) + " : " + vp + "\n" + 
	       "Link Relationship : " + pad(mr, ' ', 30) + " : " + vr);
	    
	    if(!mo.equals("-") || !vo.equals("-")) 
	      buf.append
		("\n" + 
		 "Frame Offset      : " + pad(mo, ' ', 30) + " : " + vo);

	    if(wk > 0) 
	      buf.append("\n");
	    wk--;
	  }
	}

	if(sections.contains("file") && !details.isLightweight()) {
	  buf.append
	    ("\n\n" +
	     pad("-- File Sequences ", '-', 80));

	  boolean ffirst = true;
	  QueueState qs[] = details.getQueueState();
	  for(FileSeq fseq : details.getFileStateSequences()) {
	    if(!ffirst) 
	      buf.append("\n");
	    ffirst = false;

	    FileState fs[] = details.getFileState(fseq);
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      buf.append("\n" + 
			 pad(file.toString(), ' ', 48) + " : " +
			 pad((fs[wk] != null) ? fs[wk].toTitle() : "-", 16) + " : " +
			 ((qs[wk] != null) ? qs[wk].toTitle() : "-"));
	      wk++;
	    }
	  }
	}
      }
    }
      
    if(printLinkGraph) {
      buf.append
	("\n\n" +
	 pad("-- Link Graph ", '-', 80));
      
      printGraph(buf, root, 2);
    }

    if(printGlue) {
      buf.append
	("\n\n" +
	 pad("-- GLUE Format ", '-', 80) + "\n");

      try {
	GlueEncoder ge = new GlueEncoderImpl("Status", root);
	buf.append(ge.getText());
      }
      catch(GlueException ex) {
	buf.append
	  ("Unable to generate a Glue format representation of the node status:\n  " + 
	   ex.getMessage());
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();  
  }

  private void 
  printBothSingleParams
  (
   BaseAction mAction, 
   BaseAction vAction, 
   LayoutGroup group, 
   StringBuilder buf, 
   int level 
  ) 
    throws PipelineException 
  {   
    buf.append("\n\n" +
	       pad(repeat('-', level*2) + " " + group.getNameUI() + " ", '-', 80));

    for(String pname : group.getEntries()) {
      buf.append("\n");	 
      if(pname != null) {
	String mstr = "-";
	if(mAction.getSingleParamValue(pname) != null) 
	  mstr = mAction.getSingleParamValue(pname).toString();
	else if(mAction.getPresetChoices(pname) != null)
	  mstr = "PRESET";
	
	String vstr = "-";
	if((vAction != null) && 
	   vAction.getName().equals(mAction.getName()) && 
	   vAction.getVersionID().equals(mAction.getVersionID())) {
	  if(vAction.getSingleParamValue(pname) != null)
	    vstr = vAction.getSingleParamValue(pname).toString();
	  else if(vAction.getPresetChoices(pname) != null)
	    vstr = "PRESET";
	}
	
	buf.append(pad(pname, 18) + ": " + pad(mstr, ' ', 30) + " : " + vstr);
      }
    }
    
    for(LayoutGroup sgroup : group.getSubGroups()) 
      printBothSingleParams(mAction, vAction, sgroup, buf, level+1);
  }

  private void 
  printCheckedInSingleParams
  (
   BaseAction vAction, 
   LayoutGroup group, 
   StringBuilder buf, 
   int level 
  ) 
    throws PipelineException 
  {
    buf.append("\n\n" +
	       pad(repeat('-', level*2) + " " + group.getNameUI() + " ", '-', 80));

    for(String pname : group.getEntries()) {
      buf.append("\n");
      if(pname != null) {
	String vstr = "-"; 
	if(vAction.getSingleParamValue(pname) != null) 
	  vstr = vAction.getSingleParamValue(pname).toString();
	else if(vAction.getPresetChoices(pname) != null)
	  vstr = "PRESET";

	buf.append(pad(pname, 18) + ": " + pad("-", ' ', 30) + " : " + vstr);
      }
    }

    for(LayoutGroup sgroup : group.getSubGroups()) 
      printCheckedInSingleParams(vAction, sgroup, buf, level+1);
  }

  private void 
  unpackStatus
  (
   NodeStatus status,
   TreeMap<String,NodeStatus> table
  ) 
  {
    if(table.containsKey(status.getName())) 
      return;

    table.put(status.getName(), status);
    
    for(NodeStatus lstatus : status.getSources())
      unpackStatus(lstatus, table);
  }

  private void 
  printGraph
  (
   StringBuilder buf, 
   NodeStatus status, 
   int indent
  ) 
  {
    buf.append
      ("\n" + 
       repeat(' ', indent) + status.getName());

    for(NodeStatus source : status.getSources())
      printGraph(buf, source, indent+2);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Print the selected node events. 
   */ 
  public void
  printNodeEvents
  (
   TreeSet names, 
   TreeSet users, 
   Long start, 
   Long finish, 
   MasterMgrClient mclient
  ) 
    throws PipelineException
  {
    TreeMap<Long,BaseNodeEvent> events = null;
    {
      TreeSet<String> names2 = null;
      if(!names.isEmpty()) 
	names2 = new TreeSet<String>(names);
      
      TreeSet<String> users2 = null;
      if(!users.isEmpty()) 
	users2 = new TreeSet<String>(users);
      
      Long start2 = 0L;
      if(start != null) 
	start2 = start;
      
      Long finish2 = Long.MAX_VALUE;
      if(finish != null) 
	finish2 = finish;

      events = mclient.getNodeEvents(names2, users2, new TimeInterval(start2, finish2));
    }
    
    StringBuilder buf = new StringBuilder();
    buf.append
      (tbar(80) + "\n" +
       "N O D E   E V E N T   H I S T O R Y"); 
    
    for(BaseNodeEvent event : events.values()) {
      printBaseNodeEvent(buf, event);
      switch(event.getNodeOp()) {
      case Registered: 
      case Released: 
      case PropsModified: 
      case LinksModified: 
      case SeqsModified: 
	{
	  BaseWorkingNodeEvent e = (BaseWorkingNodeEvent) event;
	  printBaseWorkingNodeEvent(buf, e);
	}
	break;
	
      case CheckedIn: 	
	{
	  CheckedInNodeEvent e = (CheckedInNodeEvent) event;
	  printBaseRepoNodeEvent(buf, e);
	  
	  String lstr = "(initial revision)";
	  if(e.getLevel() != null) 
	    lstr = e.getLevel().toString();

	  buf.append
	    ("\n" + 
	     "Level        : " + lstr); 
	}
	break;

      case CheckedOut:  	
	{
	  CheckedOutNodeEvent e = (CheckedOutNodeEvent) event;
	  printBaseRepoNodeEvent(buf, e);
	  
	  buf.append
	    ("\n" + 
	     "Method       : "); 

	  if(e.isFrozen()) {
	    if(e.isLocked()) 
	      buf.append("Locked");
	    else 
	      buf.append("Frozen");
	  }
	  else {
	    buf.append("Modifiable");
	  }
	}
	break;

      case Evolved: 
	{  
	  EvolvedNodeEvent e = (EvolvedNodeEvent) event;
	  printBaseRepoNodeEvent(buf, e);
	}
	break;
    
      case Edited:
	{
	  EditedNodeEvent	 e = (EditedNodeEvent) event;
	  printBaseWorkingNodeEvent(buf, e);
	  
	  buf.append
	    ("\n" + 
	     "Done Editing : " + TimeStamps.format(e.getFinishedStamp()) + "\n" +
	     "Hostname     : " + e.getHostname() + "\n" +
	     "Impostor     : " + ((e.getImposter() != null) ? e.getImposter() : "-") + "\n" +
	     "\n" + 
	     "Editor       : " + e.getEditorName() + "\n" +
	     "Version      : " + e.getEditorVersionID() + "\n" +
	     "Vendor       : " + e.getEditorVendor());
	}
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
    LogMgr.getInstance().flush();  
  }

  private void
  printBaseNodeEvent
  (
   StringBuilder buf, 
   BaseNodeEvent event
  ) 
  {
    buf.append
      ("\n\n" + 
       bar(80) + "\n" +
       "Operation    : " + event.getNodeOp().toTitle() + "\n" + 
       "Generated    : " + (TimeStamps.format(event.getTimeStamp()) + " by (" + 
			    event.getAuthor()) + ")\n" +
       "Node Name    : " + event.getNodeName()); 
  }
   
  private void
  printBaseWorkingNodeEvent
  (
   StringBuilder buf,
   BaseWorkingNodeEvent event
  ) 
  {
    buf.append
      ("\n" + 
       "Working Area : " + event.getAuthor() + "|" + event.getView());
  }
  
  private void
  printBaseRepoNodeEvent
  (
   StringBuilder buf,
   BaseRepoNodeEvent event
  ) 
  {
    printBaseWorkingNodeEvent(buf, event);
    buf.append
      ("\n" + 
       "Version      : " + event.getVersionID());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Convert the given byte size String to a Long value.
   * 
   * @param text
   *   The byte size string.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the given string is <CODE>null</CODE> or empty.
   * 
   * @throws NumberFormatException
   *   If the given string is invalid.
   */ 
  private Long
  parseLong
  (
   String text
  )
    throws NumberFormatException
  {
    return ByteSize.stringToLong(text);
  }

  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    return ByteSize.longToFloatString(value); 
  }


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
    case ScriptOptsParserConstants.EOF:
      return "EOF";

    case ScriptOptsParserConstants.UNKNOWN_COMMAND1:
    case ScriptOptsParserConstants.UNKNOWN_COMMAND2:
      return "an unknown command";

    case ScriptOptsParserConstants.UNKNOWN3:
    case ScriptOptsParserConstants.UNKNOWN4:
    case ScriptOptsParserConstants.UNKNOWN5:
    case ScriptOptsParserConstants.UNKNOWN6:
    case ScriptOptsParserConstants.UNKNOWN7:
    case ScriptOptsParserConstants.UNKNOWN8:
    case ScriptOptsParserConstants.UNKNOWN9:
    case ScriptOptsParserConstants.UNKNOWN10:
    case ScriptOptsParserConstants.UNKNOWN11:
    case ScriptOptsParserConstants.UNKNOWN12:
    case ScriptOptsParserConstants.UNKNOWN13:
    case ScriptOptsParserConstants.UNKNOWN14:
    case ScriptOptsParserConstants.UNKNOWN15:
    case ScriptOptsParserConstants.UNKNOWN16:
    case ScriptOptsParserConstants.UNKNOWN17:
    case ScriptOptsParserConstants.UNKNOWN18:
    case ScriptOptsParserConstants.UNKNOWN19:
    case ScriptOptsParserConstants.UNKNOWN20:
    case ScriptOptsParserConstants.UNKNOWN21:
    case ScriptOptsParserConstants.UNKNOWN22:
      return "an unknown argument";

    case ScriptOptsParserConstants.UNKNOWN_OPTION1:
    case ScriptOptsParserConstants.UNKNOWN_OPTION2:
    case ScriptOptsParserConstants.UNKNOWN_OPTION3:
    case ScriptOptsParserConstants.UNKNOWN_OPTION4:
    case ScriptOptsParserConstants.UNKNOWN_OPTION5:
    case ScriptOptsParserConstants.UNKNOWN_OPTION6:
    case ScriptOptsParserConstants.UNKNOWN_OPTION7:
    case ScriptOptsParserConstants.UNKNOWN_OPTION8:
    case ScriptOptsParserConstants.UNKNOWN_OPTION9:
    case ScriptOptsParserConstants.UNKNOWN_OPTION10:
    case ScriptOptsParserConstants.UNKNOWN_OPTION11:
    case ScriptOptsParserConstants.UNKNOWN_OPTION12:
    case ScriptOptsParserConstants.UNKNOWN_OPTION13:
    case ScriptOptsParserConstants.UNKNOWN_OPTION14:
    case ScriptOptsParserConstants.UNKNOWN_OPTION15:
    case ScriptOptsParserConstants.UNKNOWN_OPTION16:
    case ScriptOptsParserConstants.UNKNOWN_OPTION17:
    case ScriptOptsParserConstants.UNKNOWN_OPTION18:
    case ScriptOptsParserConstants.UNKNOWN_OPTION19:
    case ScriptOptsParserConstants.UNKNOWN_OPTION20:
    case ScriptOptsParserConstants.UNKNOWN_OPTION21:
    case ScriptOptsParserConstants.UNKNOWN_OPTION22:
      return "an unknown option";

    case ScriptOptsParserConstants.TRUE:
      return "\"true\"";

    case ScriptOptsParserConstants.FALSE:
      return "\"false\"";

    case ScriptOptsParserConstants.INTEGER:
      return "an integer";

    case ScriptOptsParserConstants.PORT_NUMBER:
      return "a port number";

    case ScriptOptsParserConstants.BYTE_SIZE:
      return "a byte size";

    case ScriptOptsParserConstants.KILO:
      return "\"K\" kilobytes";

    case ScriptOptsParserConstants.MEGA:
      return "\"M\" megabytes";

    case ScriptOptsParserConstants.GIGA:
      return "\"G\" gigabytes";

    case ScriptOptsParserConstants.STRING:
      return "an string";

    case ScriptOptsParserConstants.EMPTY_STRING:
      return "an empty string";      

    case ScriptOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case ScriptOptsParserConstants.HOST_NAME:
    case ScriptOptsParserConstants.HOST_NAME2:
      return "a hostname";
      
    case ScriptOptsParserConstants.NODE_NAME:
      return "a fully resolved node name";
      
    case ScriptOptsParserConstants.TOOLSET_NAME:
      return "a toolset name";

    case ScriptOptsParserConstants.USER_NAME:
      return "a user name";

    case ScriptOptsParserConstants.WORK_GROUP_NAME:
      return "a work group name";

    case ScriptOptsParserConstants.SCHEDULE_NAME:
      return "a selection schedule name";

    case ScriptOptsParserConstants.GROUP_NAME:
      return "a selection group name";

    case ScriptOptsParserConstants.KEY_NAME1:
    case ScriptOptsParserConstants.KEY_NAME2:
      return "a key name";

    case ScriptOptsParserConstants.SUFFIX1:
      return "a filename suffix";

    case ScriptOptsParserConstants.EDITOR_NAME:
      return "an Editor plugin name";

    case ScriptOptsParserConstants.ACTION_NAME:
      return "an Action plugin name";

    case ScriptOptsParserConstants.COMPARATOR_NAME:
      return "an Comparator plugin name";

    case ScriptOptsParserConstants.ARCHIVER_NAME:
      return "an Archiver plugin name";

    case ScriptOptsParserConstants.ARCHIVE_PREFIX: 
      return "an archive volume prefix/name"; 

    case ScriptOptsParserConstants.REVISION_NUMBER:
    case ScriptOptsParserConstants.REVISION_NUMBER2:
      return "a revision number";

    case ScriptOptsParserConstants.VENDOR_NAME: 
      return "a plugin vendor name";

    case ScriptOptsParserConstants.VIEW_NAME:
      return "a working area view name";

    case ScriptOptsParserConstants.PARAM_NAME: 
      return "an Action/Archiver parameter name";

    case ScriptOptsParserConstants.PARAM_VALUE: 
      return "an Action/Archiver parameter value";
      
    case ScriptOptsParserConstants.EMPTY_PARAM_VALUE:
      return "an empty Action/Archiver parameter value"; 

    case ScriptOptsParserConstants.SOURCE_NAME: 
    case ScriptOptsParserConstants.LINK_NAME: 
      return "an upstream source node name";

    case ScriptOptsParserConstants.LINK_ALL:
      return "\"all\" a all-to-all link relationship";

    case ScriptOptsParserConstants.OFFSET:
      return "a one-to-one link relationship frame offset";
      
    case ScriptOptsParserConstants.PREFIX: 
      return "a file sequence prefix";

    case ScriptOptsParserConstants.SUFFIX2:
      return "a file sequence suffix";

    case ScriptOptsParserConstants.FILE_PAT_PREFIX: 
      return "a file pattern prefix";

    case ScriptOptsParserConstants.FILE_PAT_SUFFIX: 
      return "a file pattern suffix";

    case ScriptOptsParserConstants.FRAME_NUMBER:
      return "a frame number";

    case ScriptOptsParserConstants.FRAME_INDEX:
      return "a frame index";

    case ScriptOptsParserConstants.JOB_GROUP_ID:
      return "a job group ID";

    case ScriptOptsParserConstants.JOB_ID:
      return "a job ID";

    case ScriptOptsParserConstants.STAMP_TWO:
      return "a two digit numeric timestamp component";

    case ScriptOptsParserConstants.STAMP_FOUR:
      return "a four digit numeric timestamp component";

    case ScriptOptsParserConstants.AE1:
    case ScriptOptsParserConstants.AE2:
    case ScriptOptsParserConstants.AE3:
    case ScriptOptsParserConstants.AE4:
    case ScriptOptsParserConstants.AE5:
    case ScriptOptsParserConstants.AE6:
    case ScriptOptsParserConstants.AE7:
    case ScriptOptsParserConstants.AE8:
    case ScriptOptsParserConstants.AE9:
    case ScriptOptsParserConstants.AE10:
    case ScriptOptsParserConstants.AE11:
    case ScriptOptsParserConstants.AE12:
    case ScriptOptsParserConstants.AE13:
    case ScriptOptsParserConstants.AE14:
    case ScriptOptsParserConstants.AE15:
    case ScriptOptsParserConstants.AE16:
    case ScriptOptsParserConstants.AE17:
    case ScriptOptsParserConstants.AE18:
    case ScriptOptsParserConstants.AE19:
    case ScriptOptsParserConstants.AE20:
    case ScriptOptsParserConstants.AE21:
    case ScriptOptsParserConstants.AE22:
    case ScriptOptsParserConstants.AE23:
    case ScriptOptsParserConstants.AE24:
    case ScriptOptsParserConstants.AE25:
    case ScriptOptsParserConstants.AE26:
    case ScriptOptsParserConstants.AE27:
    case ScriptOptsParserConstants.AE28:
    case ScriptOptsParserConstants.AE29:
    case ScriptOptsParserConstants.AE30:
    case ScriptOptsParserConstants.AE31:
    case ScriptOptsParserConstants.AE32:
    case ScriptOptsParserConstants.AE33:
    case ScriptOptsParserConstants.AE34:
    case ScriptOptsParserConstants.AE35:
    case ScriptOptsParserConstants.AE36:
    case ScriptOptsParserConstants.AE37:
    case ScriptOptsParserConstants.AE38:
    case ScriptOptsParserConstants.AE40:
    case ScriptOptsParserConstants.AE41:
    case ScriptOptsParserConstants.AE42:
    case ScriptOptsParserConstants.AE43:
    case ScriptOptsParserConstants.AE44:
    case ScriptOptsParserConstants.AE45:
    case ScriptOptsParserConstants.AE46:
    case ScriptOptsParserConstants.AE47:
    case ScriptOptsParserConstants.AE48:
    case ScriptOptsParserConstants.AE49:
    case ScriptOptsParserConstants.AE50:
    case ScriptOptsParserConstants.AE51:
    case ScriptOptsParserConstants.AE52:
    case ScriptOptsParserConstants.AE53:
    case ScriptOptsParserConstants.AE54:
    case ScriptOptsParserConstants.AE55:
    case ScriptOptsParserConstants.AE56:
    case ScriptOptsParserConstants.AE57:
      return null;

    default: 
      if(printLiteral)
	return ScriptOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}

// $Id: ScriptApp.java,v 1.26 2005/01/15 02:46:46 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
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
      Logs.ops.severe(wordWrap(ex.getMessage(), 0, 80));
    }
    catch(Exception ex) {
      Logs.ops.severe(getFullMessage(ex));
    }
    finally {
      if(parser != null)
	parser.disconnect();
      Logs.cleanup();
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
    Logs.ops.info(
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
      "  [--master-host=...] [--master-port=...]\n" + 
      "  [--queue-host=...] [--queue-port=...] [--job-port=...]\n" + 
      "  [--log-file=...][--log-backups=...][--log=...]\n" +
      "\n" + 
      "COMMANDS:\n\n" +
      "  Privileged Users:\n" +
      "    privileged\n" + 
      "      --get\n" + 
      "      --grant=user-name\n" + 
      "      --revoke=user-name\n" + 
      "\n" + 
      "  Administration\n" +
      "    admin\n" + 
      "      --shutdown\n" + 
      "      --backup=dir\n" +
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
      "        --msg=\"key-description\" --total=integer\n" + 
      "      --set=key-name\n" + 
      "        --total=integer\n" +
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
      "       [--slots=integer] [--selection-bias=key-name:bias]\n" + 
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
      "        [--ignore | --abort] [--serial | --parallel] [--batch-size=integer]\n" +
      "        [--priority=integer] [--ramp-up=milliseconds] [--max-load=real]\n" +
      "        [--min-memory=bytes[K|M|G]] [--min-disk=bytes[K|M|G]]\n" +
      "        [--license-key=key-name[:true|false] ...]\n" +
      "        [--selection-key=key-name[:true|false] ...]\n" +
      "      --release=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--remove-files]\n" +
      "      --set=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--toolset=toolset-name] [--editor=editor-name[:major.minor.micro]]\n" +
      "        [--no-action | --action=action-name[:major.minor.micro]]\n" +
      "        [--action-enabled=true|false] [--param=name:value ...]\n" + 
      "        [--no-param=source-name | --source-param=source-name,name:value ...]\n" +
      "        [--ignore | --abort] [--serial | --parallel] [--batch-size=integer]\n" +
      "        [--priority=integer] [--ramp-up=milliseconds] [--max-load=real]\n" +
      "        [--min-memory=bytes[K|M|G]] [--min-disk=bytes[K|M|G]]\n" +
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
      "        --name=new-node-name [--rename-files]\n" +
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
      "        [--brief] [--upstream] [--link-graph]\n" +
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
      "\n" +  
      "Use \"plscript --html-help\" to browse the full documentation.\n");
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

    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for(Toolset tset : selected) {
      if(!first) 
	buf.append("\n\n");
      first = false;

      buf.append
	(tbar(80) + "\n" +
	 "Toolset     : " + tset.getName() + "\n" + 
	 "Created     : " + (Dates.format(tset.getTimeStamp()) + " by (" + 
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
	     "Created     : " + (Dates.format(pkg.getTimeStamp()) + " by (" + 
				 pkg.getAuthor()) + ")\n" +
	     "Description : " + wordWrap(pkg.getDescription(), 14, 80));
	}
	catch(PipelineException ex) {
	  Logs.ops.warning(ex.getMessage());
	}
	
	if(wk < (tset.getNumPackages()-1)) 
	  buf.append("\n\n");
      }
    }

    Logs.ops.info(buf.toString());
    Logs.flush();
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

    StringBuffer buf = new StringBuffer();
    buf.append("export TOOLSET=" + tset.getName() + "\n");
    buf.append("export USER=`whoami`\n");
    buf.append("export HOME=" + PackageInfo.sHomeDir + "/$USER\n");
    buf.append("export WORKING=" + PackageInfo.sWorkDir + "/$USER/default\n");
    buf.append("export _=/bin/env\n");
    
    TreeMap<String,String> env = tset.getEnvironment();
    for(String ename : env.keySet()) {
      String evalue = env.get(ename);
      buf.append("\nexport " + ename + "=" + ((evalue != null) ? evalue : ""));
    }

    Logs.ops.info(buf.toString());
    Logs.flush();
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
    StringBuffer buf = new StringBuffer(); 
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
	  ("License Key : " + key.getName() + "\n" + 
	   "Available   : " + key.getAvailable() + "\n" + 
	   "Total       : " + key.getTotal() + "\n" + 
	   "Description : " + wordWrap(key.getDescription(), 14, 80));

	if(kname != null) {
	  found = true;
	  break;
	}
      }
    }

    if((kname != null) && !found) 
      throw new PipelineException
	("No license key named (" + kname + ") exists!");

    Logs.ops.info(buf.toString());
    Logs.flush();
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
    StringBuffer buf = new StringBuffer(); 
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

    Logs.ops.info(buf.toString());
    Logs.flush();    
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
    TreeMap<String,QueueHost> hosts = client.getHosts();
    ArrayList<QueueHost> selected = new ArrayList<QueueHost>();
    if(hname == null)
      selected.addAll(hosts.values());
    else 
      selected.add(hosts.get(hname));

    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for(QueueHost host : selected) {
      if(first) 
	buf.append(tbar(80) + "\n");
      else 
	buf.append("\n\n" + bar(80) + "\n");
      first = false;

      buf.append
	("Job Server     : " + host.getName() + "\n" + 
	 "Status         : " + host.getStatus() + "\n" + 
	 "Reservation    : ");
      
      String reserve = host.getReservation();
      if(reserve != null) 
	buf.append(reserve);
      else 
	buf.append("-");

      ResourceSample sample = host.getLatestSample();
      if(sample != null) {
	buf.append
	  ("\n" + 
	   "Jobs           : " + (sample.getNumJobs() + 
				  " (slots " + host.getJobSlots() + ")") + "\n" + 
	   "System Load    : " + (String.format("%1$.2f", sample.getLoad()) + 
				  " (procs " + host.getNumProcessors()) + ")\n" +
	   "Free Memory    : " + (formatLong(sample.getMemory()) + " (total " + 
				  formatLong(host.getTotalMemory()) + ")") + "\n" +
	   "Free Disk      : " + (formatLong(sample.getDisk()) + " (total " +
				  formatLong(host.getTotalDisk()) + ")"));
      }


      buf.append("\n" + 
		 "Selection Bias :");
      Set<String> keys = host.getSelectionKeys();
      if(keys.isEmpty()) {
	buf.append(" -");
      }
      else {
	StringBuffer kbuf = new StringBuffer();
	for(String kname : keys) 
	  kbuf.append(" " + kname + "[" + host.getSelectionBias(kname) + "]");
	buf.append(wordWrap(kbuf.toString(), 17, 80));
      }
    }

    Logs.ops.info(buf.toString());
    Logs.flush();      
  }

  /**
   * Edit the properties of the given host.
   */ 
  public void 
  editHost
  (
   String hname, 
   QueueHost.Status status, 
   String reserve, 
   boolean setReserve, 
   Integer slots, 
   TreeMap biases, 
   TreeSet removes, 
   QueueMgrClient client
  )
    throws PipelineException 
  {
    TreeMap<String,QueueHost> hosts = client.getHosts();
    QueueHost host = hosts.get(hname);
    if(host != null) {
      TreeMap<String,QueueHost.Status> statusTable = 
	new TreeMap<String,QueueHost.Status>();
      TreeMap<String,String> reserveTable = 
	new TreeMap<String,String>();
      TreeMap<String,Integer> slotsTable = 
	new TreeMap<String,Integer>();
      TreeMap<String,TreeMap<String,Integer>> biasesTable = 
	new TreeMap<String,TreeMap<String,Integer>>();
    
      if(status != null) 
	statusTable.put(hname, status);

      if(setReserve) 
	reserveTable.put(hname, reserve);

      if(slots != null) 
	slotsTable.put(hname, slots);

      if(!biases.isEmpty() || !removes.isEmpty()) {
	TreeMap<String,Integer> table = new TreeMap<String,Integer>();
	for(String kname : host.getSelectionKeys()) {
	  if(!removes.contains(kname)) 
	    table.put(kname, host.getSelectionBias(kname));
	}
	
	for(Object obj : biases.keySet()) {
	  String kname = (String) obj;
	  Integer bias = (Integer) biases.get(kname);
	  table.put(kname, bias);
	}
	
	biasesTable.put(hname, table);
      }
      
      client.editHosts(statusTable, reserveTable, slotsTable, biasesTable);
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
    TreeMap<String,TreeSet<VersionID>> versions = client.getEditors();
    if(!versions.isEmpty()) {
      Logs.ops.info(tbar(80) + "\n" + 
		    "  E D I T O R S");
      
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) {
	  BaseEditor plg = client.newEditor(name, vid);
	  Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	}
      }
    }

    Logs.flush();
  }

  /**
   * Print information about all Action plugins.
   */ 
  public void 
  actionGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TreeMap<String,TreeSet<VersionID>> versions = client.getActions();
    if(!versions.isEmpty()) {
      Logs.ops.info(tbar(80) + "\n" + 
		    "  A C T I O N S");
      
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) {
	  BaseAction plg = client.newAction(name, vid);
	  Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	}
      }
    }

    Logs.flush();
  }

  /**
   * Print information about all Comparator plugins.
   */ 
  public void 
  comparatorGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TreeMap<String,TreeSet<VersionID>> versions = client.getComparators();
    if(!versions.isEmpty()) {
      Logs.ops.info(tbar(80) + "\n" + 
		    "  C O M P A R A T O R S ");
      
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) {
	  BaseComparator plg = client.newComparator(name, vid);
	  Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	}
      }
    }

    Logs.flush();
  }

  /**
   * Print information about all Tool plugins.
   */ 
  public void 
  toolGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TreeMap<String,TreeSet<VersionID>> versions = client.getTools();
    if(!versions.isEmpty()) {
      Logs.ops.info(tbar(80) + "\n" + 
		    "  T O O L S"); 
      
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) {
	  BaseTool plg = client.newTool(name, vid);
	  Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	}
      }
    }

    Logs.flush();
  }

  /**
   * Print information about all Archiver plugins.
   */ 
  public void 
  archiverGetInfoAll()
    throws PipelineException
  {
    PluginMgrClient client = PluginMgrClient.getInstance();
    TreeMap<String,TreeSet<VersionID>> versions = client.getArchivers();
    if(!versions.isEmpty()) {
      Logs.ops.info(tbar(80) + "\n" + 
		    "  A R C H I V E R S");
      
      for(String name : versions.keySet()) {
	for(VersionID vid : versions.get(name)) {
	  BaseArchiver plg = client.newArchiver(name, vid);
	  Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	}
      }
    }

    Logs.flush();
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
    StringBuffer buf = new StringBuffer(); 
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

    Logs.ops.info(buf.toString());
    Logs.flush();    
  }
  
  /**
   * Add or replace the default editor for a filename suffix.
   */ 
  public void
  addSuffixEditor
  (
   SuffixEditor se, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
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

    StringBuffer buf = new StringBuffer();
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

    Logs.ops.info(buf.toString());
    Logs.flush();    
  }

  
  /**
   * Print a text representation of the given working version.
   */ 
  public void
  printCommon
  (
   StringBuffer buf, 
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
   StringBuffer buf, 
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
   String editor, 
   Boolean noAction, 
   String actionName, 
   VersionID actionVersionID, 
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

      if((editor != null) &&
	 (!PluginMgrClient.getInstance().getEditors().keySet().contains(editor)))
	throw new PipelineException 
	  ("No Editor plugin named (" + editor + ") exists!");

      String edit = editor;
      if(edit == null) {
	String suffix = primary.getFilePattern().getSuffix();
	if(suffix != null) 
	  edit = client.getEditorForSuffix(suffix);
      }

      mod = new NodeMod(nodeID.getName(), primary, new TreeSet<FileSeq>(), tset, edit);

      setActionProperties
	(mod, noAction, actionName, actionVersionID, actionEnabled, 
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
   String editor, 
   Boolean noAction, 
   String actionName, 
   VersionID actionVersionID, 
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
	
	if(editor != null) {
	  if(!PluginMgrClient.getInstance().getEditors().keySet().contains(editor))
	    throw new PipelineException 
	      ("No Editor plugin named (" + editor + ") exists!");
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
	(mod, noAction, actionName, actionVersionID, actionEnabled, 
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

	TreeSet<VersionID> table = pclient.getActions().get(actionName);
	if(table == null) 
	  throw new PipelineException 
	    ("No Action plugin named (" + actionName + ") exists!");
	
	if((actionVersionID != null) && !table.contains(actionVersionID)) 
	  throw new PipelineException 
	    ("No version (v" + actionVersionID + ") of Action plugin " + 
	     "(" + actionName + ") exists!");
	
	BaseAction action = pclient.newAction(actionName, actionVersionID);
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
	      if(!eparam.getValues().contains(value))
		throw new PipelineException
		  ("The value (" + value + ") is not one of the enumerations of " +
		   "parameter (" + pname + ")!");
	      
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
		  if(!eparam.getValues().contains(value))
		    throw new PipelineException
		      ("The value (" + value + ") is not one of the enumerations of " +
		       "per-source parameter (" + pname + ")!");
		  
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
   ArrayList frames, 
   ArrayList indices, 
   FileSeq fseq, 
   boolean wait, 
   MasterMgrClient client
  ) 
    throws PipelineException
  {
    NodeMod mod = client.getWorkingVersion(nodeID);
    editCommon(nodeID, mod, editorName, editorVersionID, frames, indices, fseq, wait, client);
  }  

  /**
   * Launch the editor program for the given node version.
   */ 
  private void 
  editCommon
  (
   NodeID nodeID, 
   NodeCommon com, 
   String editorName, 
   VersionID editorVersionID, 
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
	    assert(false);
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
	    assert(false);
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
    assert(!editSeqs.isEmpty());

    NodeMod mod = null;
    if(com instanceof NodeMod) 
      mod = (NodeMod) com;
    
    NodeVersion vsn = null;
    if(com instanceof NodeVersion) 
      vsn = (NodeVersion) com;
    
    /* create an editor plugin instance */ 
    BaseEditor editor = null;
    {
      String ename = editorName;
      if(ename == null) 
	ename = com.getEditor();
      if(ename == null) 
	throw new PipelineException
	  ("No editor was specified for node (" + com.getName() + ")!");
      
      editor = PluginMgrClient.getInstance().newEditor(ename, editorVersionID);
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
      env = client.getToolsetEnvironment(author, view, tname);
      
      /* override these since the editor will be run as the current user */ 
      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
      env.put("USER", PackageInfo.sUser);
    }
    
    /* get the directory containing the files */ 
    File dir = null; 
    {
      if(mod != null) {
	File wpath = 
	  new File(PackageInfo.sWorkDir, 
		   nodeID.getAuthor() + "/" + nodeID.getView() + "/" + mod.getName());
	dir = wpath.getParentFile();
      }
      else if(vsn != null) {
	dir = new File(PackageInfo.sRepoDir + "/" + 
		       vsn.getName() + "/" + vsn.getVersionID());
      }
      else {
	assert(false);
      }
    }

    /* launch an editor for each file sequence */ 
    ArrayList<SubProcessLight> procs = new ArrayList<SubProcessLight>();
    for(FileSeq fs : editSeqs) {
      Logs.ops.info
	("Editing: " + fs + " with " + 
	 editor.getName() + " (v" + editor.getVersionID() + ")");
      procs.add(editor.launch(fs, env, dir));
    }
    Logs.flush();

    /* wait around for the results? */ 
    if(wait) {
      Logs.ops.info("\n" + 
		    "Waiting for Editor(s) to exit...");
      for(SubProcessLight proc : procs) {
	try {
	  proc.join();
	  Logs.ops.info
	    (tbar(80) + "\n" +
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
	  Logs.sub.severe
	    ("Interrupted while waiting on an Editor Process to exit!");
	}
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

    Logs.ops.info
      ("Submitted Job Group: [" + group.getGroupID() + "] " + group.getRootPattern());
    Logs.flush();        

    if(wait) {
      Logs.ops.info("Waiting for jobs to complete...");
      Logs.flush();   

      TreeSet<Long> groupIDs = new TreeSet<Long>();
      groupIDs.add(group.getGroupID());

      while(true) {
	TreeMap<Long,JobStatus> table = qclient.getJobStatus(groupIDs);

	boolean done = true; 
	boolean failed = false;
	for(JobStatus status : table.values()) {
	  switch(status.getState()) {
	  case Queued: 
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
	    Logs.ops.info(pad("Job [" + status.getJobID() + "]: ", ' ', 15) + 
			  pad(status.getState().toTitle(), ' ', 15) + 
			  "(" + status.getTargetSequence() + ")");
	  }
	  
	  if(failed) 
	    throw new PipelineException("Jobs Failed.");
	  else {
	    Logs.ops.info("Jobs Completed Successfully.");
	    Logs.flush(); 
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
	  assert(false);
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
	    assert(false);
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
      
      for(VersionID vid : svids) {
	NodeVersion vsn = mclient.getCheckedInVersion(name, vid);
	versions.add(vsn);
      }		
    }

    StringBuffer buf = new StringBuffer();
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
    }
      
    Logs.ops.info(buf.toString());
    Logs.flush();    
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

    StringBuffer buf = new StringBuffer();
    buf.append
      (tbar(80) + "\n" +
       "Checked-In Node  : " + name);
    
    for(VersionID vid : logs.keySet()) {
      LogMessage msg = logs.get(vid);
      buf.append
	("\n\n" + 
	 bar(80) + "\n" +
	 "Revision Number  : " + vid + "\n" + 
	 "Created          : " + (Dates.format(msg.getTimeStamp()) + " by (" + 
				   msg.getAuthor()) + ")\n" +
	 "Check-In Message : " + wordWrap(msg.getMessage(), 20, 80));
    }
      
    Logs.ops.info(buf.toString());
    Logs.flush();  
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
   ArrayList frames, 
   ArrayList indices, 
   FileSeq fseq, 
   boolean wait, 
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    NodeVersion vsn = client.getCheckedInVersion(name, vid);
    editCommon(null, vsn, editorName, editorVersionID, frames, indices, fseq, wait, client);
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
   boolean printLinkGraph, 
   TreeSet sections, 
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {

    NodeStatus root = null;
    TreeMap<String,NodeStatus> table = new TreeMap<String,NodeStatus>();
    {
      root = mclient.status(nodeID);
      if(printUpstream) 
	unpackStatus(root, table); 
      else 
	table.put(root.getName(), root);
    }

    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for(String name : table.keySet()) {
      NodeStatus status = table.get(name);
      NodeDetails details = status.getDetails();
      if(briefFormat) {
	if(!first) 
	  buf.append("\n");
	first = false;

	buf.append(details.getOverallNodeState().toSymbol() + " " + 
		   details.getOverallQueueState().toSymbol() + " " + status.getName()); 
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
	
	buf.append
	  ("\n\n" + 
	   pad("-- Overall State ", '-', 80) + "\n" +
	   "Node State        : " + details.getOverallNodeState().toTitle() + "\n" + 
	   "Queue State       : " + details.getOverallQueueState().toTitle()); 
	
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
	  String mEditor  = "-";
	  if(mod != null) {
	    mToolset = mod.getToolset();
	    mEditor  = mod.getEditor();
	  }

	  String vToolset = "-";
	  String vEditor  = "-";
	  if(vsn != null) {
	    vToolset = vsn.getToolset();
	    vEditor  = vsn.getEditor();
	  }

	  buf.append
	    ("\n\n" + 
	     pad("-- Properties ", '-', 80) + "\n" +
	     "Property State    : " + details.getPropertyState().toTitle() + "\n" +
	     "Toolset           : " + pad(mToolset, ' ', 30) + " : " + vToolset + "\n" +
	     "Editor            : " + pad(mEditor, ' ', 30) + " : " + vEditor);
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
	    String menabled = "-";
	    if(mAction != null) {
	      mname = mAction.getName();
	      mvstr = mAction.getVersionID().toString();

	      if(mod != null) 
		menabled = (mod.isActionEnabled() ? "YES" : "no");
	    }

	    String vname    = "-";
	    String vvstr    = "-";
	    String venabled = "-";
	    if(vAction != null) {
	      vname = vAction.getName();
	      vvstr = vAction.getVersionID().toString();

	      if(vsn != null) 
		venabled = (vsn.isActionEnabled() ? "YES" : "no");
	    }

	    buf.append
	      ("Action            : " + pad(mname, ' ', 30) + " : " + vname + "\n" +
	       "Version           : " + pad(mvstr, ' ', 30) + " : " + vvstr + "\n" +
	       "Enabled           : " + pad(menabled, ' ', 30) + " : "  + venabled);
	    
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
	   ((mod != null) && mod.hasSources()) || ((vsn != null) && (vsn.hasSources()))) {
	  
	  buf.append
	    ("\n\n" +
	     pad("-- Upstream Links ", '-', 80));
	  
	  TreeSet<String> snames = new TreeSet<String>();
	  if(mod != null)
	    snames.addAll(mod.getSourceNames());
	  if(vsn != null) 
	    snames.addAll(vsn.getSourceNames());

	  int wk = vsn.getSourceNames().size();
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

	if(sections.contains("file")) {
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

    Logs.ops.info(buf.toString());
    Logs.flush();  
  }

  private void 
  printBothSingleParams
  (
   BaseAction mAction, 
   BaseAction vAction, 
   LayoutGroup group, 
   StringBuffer buf, 
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
   StringBuffer buf, 
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
   StringBuffer buf, 
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value == null) 
      return "-";

    if(value < 1048576) {
      return value.toString();
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.2fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.2fG", g);
    }
  }

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
      return "a hostname";
      
    case ScriptOptsParserConstants.NODE_NAME:
      return "a fully resolved node name";
      
    case ScriptOptsParserConstants.TOOLSET_NAME:
      return "a toolset name";

    case ScriptOptsParserConstants.USER_NAME:
      return "a user name";

    case ScriptOptsParserConstants.KEY_BIAS_NAME:
    case ScriptOptsParserConstants.KEY_NAME1:
    case ScriptOptsParserConstants.KEY_NAME2:
      return "a key name";

    case ScriptOptsParserConstants.KEY_BIAS:
      return "a selection bias";

    case ScriptOptsParserConstants.SUFFIX1:
      return "a filename suffix";

    case ScriptOptsParserConstants.EDITOR_NAME:
      return "an Editor plugin name";

    case ScriptOptsParserConstants.ACTION_NAME:
      return "an Action plugin name";

    case ScriptOptsParserConstants.COMPARATOR_NAME:
      return "an Comparator plugin name";

    case ScriptOptsParserConstants.REVISION_NUMBER:
      return "a revision number";

    case ScriptOptsParserConstants.VIEW_NAME:
      return "a working area view name";

    case ScriptOptsParserConstants.PARAM_NAME: 
      return "an Action parameter name";

    case ScriptOptsParserConstants.PARAM_VALUE: 
      return "an Action parameter value";
      
    case ScriptOptsParserConstants.EMPTY_PARAM_VALUE:
      return "an empty Action parameter value"; 

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

    case ScriptOptsParserConstants.FRAME_NUMBER:
      return "a frame number";

    case ScriptOptsParserConstants.FRAME_INDEX:
      return "a frame index";

    case ScriptOptsParserConstants.JOB_GROUP_ID:
      return "a job group ID";

    case ScriptOptsParserConstants.JOB_ID:
      return "a job ID";

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
      return null;

    default: 
      if(printLiteral)
	return ScriptOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}

// $Id: ScriptApp.java,v 1.12 2004/09/26 03:13:56 jim Exp $

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
    try {
      ScriptOptsParser parser = 
	new ScriptOptsParser(new StringReader(pPackedArgs));
      
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
      "  [--log=...]\n" +
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
      "      --set=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--toolset=toolset-name] [--editor=editor-name[:major.minor.micro]]\n" +
      "        [--no-action | --action=action-name[:major.minor.micro]]\n" +
      "        [--action-enabled=true|false] [--param=name:value ...]\n" + 
      "        [--no-param=source-name | --source-param=source-name,name:value ...]\n" +
      "        [--ignore | --abort] [--serial | --parallel] [--batch-size=integer]\n" +
      "        [--priority=integer] [--max-load=real]\n" +
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
      "\n" +
      "  Checked-In Node Versions\n" +
      "    checked-in\n" +
      "      --get-info=node-name\n" +
      "        [--version=major.minor.micro | --latest ...]\n" +
      "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
      "      --history=node-name\n" +
      "        [--version=major.minor.micro | --latest ...]\n" +
      "      --novelty=node-name\n" +
      "        [--version=major.minor.micro | --latest ...]\n" + 
      "\n" + 
      "  Node Operations\n" +
      "    node\n" +
      "      --status=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--single | --tree] [--link-graph]\n" +
      "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
      "      --register=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        --fseq=prefix[.#|@...][.suffix][,start[-end[xby]]]\n" +
      "        [--toolset=toolset-name] [--editor=editor-name[:major.minor.micro]]\n" +
      "        [--no-action | --action=action-name[:major.minor.micro]]\n" +
      "        [--action-enabled=true|false] [--param=name:value ...]\n" +
      "        [--source-param=source-name,name:value ...]\n" +
      "        [--ignore | --abort] [--serial | --parallel] [--batch-size=integer]\n" +
      "        [--priority=integer] [--max-load=real]\n" +
      "        [--min-memory=bytes[K|M|G]] [--min-disk=bytes[K|M|G]]\n" +
      "        [--license-key=key-name[:true|false] ...]\n" +
      "        [--selection-key=key-name[:true|false] ...]\n" +
      "      --release=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--remove-files]\n" +
      "      --check-in=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        --msg=\"log-message\" [--major | --minor | --micro]\n" +
      "      --check-out=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--version=major.minor.micro] [--keep-newer]\n" +
      "      --submit-jobs=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
      "      --remove-files=node-name\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "        [--frame=single|start-end[,...] ...] [--index=single|start-end[,...] ...]\n" +
      "\n" +
      "  Queue Operations\n" +
      "    job-group\n" +
      "      --status=group-id[,group-id ...]\n" +
      "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
      "      --kill=group-id[,group-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --pause=group-id[,group-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --resume=group-id[,group-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --remove=group-id[,group-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --remove-all\n" +
      "        [--author=user-name] [--view=view-name]\n" +
      "    job\n" +
      "      --status=job-id[,job-id ...]\n" +
      "        [--show=section[,section ...]] [--hide=section[,section ...]]\n" +
      "      --kill=job-id[,job-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --pause=job-id[,job-id ...]\n" +
      "        [--author=user-name]\n" +
      "      --resume=job-id[,job-id ...]\n" +
      "        [--author=user-name]\n" +
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
    BaseAction action = mod.getAction();
    JobReqs jreqs = mod.getJobRequirements();
    
    StringBuffer buf = new StringBuffer(); 
    
    buf.append
      (tbar(80) + "\n" +
       "Working Node      : " + mod.getName() + "\n" + 
       "Primary Files     : " + mod.getPrimarySequence());

    for(FileSeq fseq : mod.getSecondarySequences()) {
      buf.append
	("\n" + 
	 "Secondary Files   : " + fseq);
    }
    
    if(sections.contains("version")) {
      String vstr = "(pending)";
      if(mod.getWorkingID() != null) 
	vstr = mod.getWorkingID().toString();
      
      buf.append
	("\n\n" + 
	 pad("-- Versions ", '-', 80) + "\n" +
	 "Revision Number   : " + vstr);
    }
    
    if(sections.contains("prop")) {
      buf.append
	("\n\n" + 
	 pad("-- Properties ", '-', 80) + "\n" +
	 "Toolset           : " + mod.getToolset() + "\n" +
	 "Editor            : " + mod.getEditor());
    }
       
    if(sections.contains("action")) {
      buf.append
	("\n\n" + 
	 pad("-- Regeneration Action ", '-', 80) + "\n");

      if(action != null) {
	buf.append
	  ("Action            : " + action.getName() + "\n" +
	   "Version           : v" + action.getVersionID() + "\n" + 
	   "Enabled           : " + (mod.isActionEnabled() ? "YES" : "no"));
	
	if(action.hasSingleParams()) {
	  buf.append("\n\n" +
		     pad("-- Action Parameters ", '-', 80));
	  for(String pname : action.getSingleLayout()) {
	    buf.append("\n");	 
	    if(pname != null) 
	      buf.append(pad(pname, 18) + ": " + action.getSingleParamValue(pname));
	  }
	}

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
      if(mod.getBatchSize() != null) 
	batchSize = mod.getBatchSize().toString();

      buf.append
	("\n\n" +
	 pad("-- Job Requirements ", '-', 80) + "\n" +
	 "Overflow Policy   : " + mod.getOverflowPolicy() + "\n" + 
	 "Execution Method  : " + mod.getExecutionMethod() + "\n" + 
	 "Batch Size        : " + batchSize + "\n" + 
	 "\n" + 
	 "Priority          : " + jreqs.getPriority() + "\n" + 
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
	   "Link Policy       : " + link.getPolicy());

	switch(link.getPolicy()) {
	case Reference:
	case Dependency:
	  buf.append
	    ("\n" + 
	     "Link Relationship : " + link.getRelationship());
	}
	
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
	 (!PluginMgr.getInstance().getEditors().keySet().contains(editor)))
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
	 priority, maxLoad, minMemory, minDisk,
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
	  if(!PluginMgr.getInstance().getEditors().keySet().contains(editor))
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
	 priority, maxLoad, minMemory, minDisk,
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
	TreeSet<VersionID> table = PluginMgr.getInstance().getActions().get(actionName);
	if(table == null) 
	  throw new PipelineException 
	    ("No Action plugin named (" + actionName + ") exists!");
	
	if((actionVersionID != null) && !table.contains(actionVersionID)) 
	  throw new PipelineException 
	    ("No version (v" + actionVersionID + ") of Action plugin " + 
	     "(" + actionName + ") exists!");
	
	BaseAction action = PluginMgr.getInstance().newAction(actionName, actionVersionID);
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
	    BaseActionParam aparam = action.getSingleParam(pname);
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
		BaseActionParam aparam = action.getSourceParam(sname, pname);
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
    FileSeq primary = mod.getPrimarySequence();
    
    /* build the file sequences to edit */ 
    TreeSet<FileSeq> editSeqs = new TreeSet<FileSeq>();
    if(fseq != null) {
      boolean found = false;
      ArrayList<File> fs = fseq.getFiles();
      for(FileSeq nfseq : mod.getSequences()) {
	if(nfseq.getFiles().containsAll(fs)) {
	  editSeqs.add(fseq);
	  found = true;
	  break;
	}
      }
	    
      if(!found) 
	throw new PipelineException
	  ("The given file sequence (" + fseq + ") does not match any of the primary or " +
	   "secondary file sequences of node (" + mod.getName() + ")!");
    }
    else if(!indices.isEmpty()) {
      if(!primary.hasFrameNumbers()) 
	throw new PipelineException
	  ("The primary file sequence (" + primary + ") of node (" + mod.getName() + ") " +
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
	  ("The primary file sequence (" + primary + ") of node (" + mod.getName() + ") " +
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

    /* create an editor plugin instance */ 
    BaseEditor editor = null;
    {
      String ename = editorName;
      if(ename == null) 
	ename = mod.getEditor();
      
      if(ename == null) 
	throw new PipelineException
	  ("No editor was specified for node (" + mod.getName() + ")!");
     
      editor = PluginMgr.getInstance().newEditor(ename, editorVersionID);
    }

    /* lookup the toolset environment */ 
    TreeMap<String,String> env = null;
    {
      String tname = mod.getToolset();
      if(tname == null) 
	throw new PipelineException
	  ("No toolset was specified for node (" + mod.getName() + ")!");

      env = client.getToolsetEnvironment(nodeID.getAuthor(), nodeID.getView(), tname);
      
      /* override these since the editor will be run as the current user */ 
      env.put("HOME", PackageInfo.sHomeDir + "/" + PackageInfo.sUser);
      env.put("USER", PackageInfo.sUser);
    }
	    
    /* the working directory */ 
    File dir = null;
    {
      File path = new File(PackageInfo.sWorkDir, 
			   nodeID.getAuthor() + "/" + nodeID.getView() + "/" + mod.getName());
      dir = path.getParentFile();
    }

    /* launch an editor for each file sequence */ 
    ArrayList<SubProcess> procs = new ArrayList<SubProcess>();
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
      for(SubProcess proc : procs) {
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
   MasterMgrClient client
  ) 
    throws PipelineException
  { 
    NodeMod mod = client.getWorkingVersion(nodeID);
    TreeSet<Integer> frameIndices = 
      buildFrameIndices(mod, (ArrayList<int[]>) frames, (ArrayList<int[]>) indices);
    QueueJobGroup group = client.submitJobs(nodeID, frameIndices);

    Logs.ops.info
      ("Submitted Job Group: [" + group.getGroupID() + "] " + group.getRootPattern());
    Logs.flush();    
    
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
      return null;

    default: 
      if(printLiteral)
	return ScriptOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}

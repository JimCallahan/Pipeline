// $Id: ScriptApp.java,v 1.1 2004/09/19 04:50:17 jim Exp $

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
  extends VerifiedApp
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
      "      --set=user-name[:true|false]\n" + 
      "\n" + 
      "  Toolset Administration\n" + 
      "    default-toolset\n" + 
      "      --get\n" + 
      "      --set=toolset-name\n" + 
      "    active-toolset\n" + 
      "      --get\n" + 
      "      --set=toolset-name[:true|false]\n" + 
      "    toolset\n" + 
      "      --get\n" + 
      "      --get-info=toolset-name\n" + 
      "      --export=toolset-name\n" + 
      "\n" + 
      "  Queue Administration\n" + 
      "    license-key\n" + 
      "      --get\n" + 
      "      --get-info=key-name\n" +
      "      --add=key-name\n" + 
      "        --msg=\"key-description\" --total=integer\n" + 
      "      --set=key-name\n" + 
      "        --total=integer\n" +
      "      --remove=key-name\n" +
      "    selection-key\n" + 
      "      --get\n" + 
      "      --get-info=key-name\n" + 
      "      --add=key-name\n" + 
      "        --msg=\"key-description\"\n" + 
      "      --remove=key-name\n" + 
      "    job-server\n" + 
      "      --get\n" + 
      "      --get-info=host-name\n" + 
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
    Toolset tset = client.getToolset(tname);

    StringBuffer buf = new StringBuffer();
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
    ArrayList<LicenseKey> keys = client.getLicenseKeys();
    for(LicenseKey key : keys) {
      if(key.getName().equals(kname)) {
	Logs.ops.info
	  (tbar(80) + "\n" +
	   "License Key : " + key.getName() + "\n" + 
	   "Available   : " + key.getAvailable() + "\n" + 
	   "Total       : " + key.getTotal() + "\n" + 
	   "Description : " + wordWrap(key.getDescription(), 14, 80));

	return;
      }
    }
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
    ArrayList<SelectionKey> keys = client.getSelectionKeys();
    for(SelectionKey key : keys) {
      if(key.getName().equals(kname)) {
	Logs.ops.info
	  (tbar(80) + "\n" +
	   "Selection Key : " + key.getName() + "\n" + 
	   "Description   : " + wordWrap(key.getDescription(), 14, 80));

	return;
      }
    }
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
    case ScriptOptsParserConstants.EOF:
      return "EOF";

    case ScriptOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case ScriptOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case ScriptOptsParserConstants.INTEGER:
      return "an integer";

    case ScriptOptsParserConstants.STRING:
      return "an string";

    case ScriptOptsParserConstants.EMPTY_STRING:
      return "an empty string";      

    case ScriptOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case ScriptOptsParserConstants.HOSTNAME:
      return "a hostname";
      
    case ScriptOptsParserConstants.NODE_ARG:
      return "a fully resolved node name";
      
    case ScriptOptsParserConstants.NAME:
      return "a name";

    default: 
      if(printLiteral) { 
	String img = ScriptOptsParserConstants.tokenImage[kind];
	if(img.startsWith("<") && img.endsWith(">")) 
	  return null;
	else 
	  return img;
      }
      else {
	return null;
      }
    }      
  }

}

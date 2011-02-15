// $Id: MineApp.java,v 1.4 2010/01/06 23:34:09 jim Exp $

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
/*   F I X   B A C K S L A S H   A P P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Fixes unescaped backslashes in all existing GLUE format database files.
 */
public
class FixBackslashApp
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
  FixBackslashApp() 
  {
    super("plfixbackslash");

    pFixedFiles = new TreeSet<File>();
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
      FixBackslashOptsParser parser = new FixBackslashOptsParser(getPackagedArgsReader()); 
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
      LogMgr.getInstance().cleanupAll();
    }

    System.exit(success ? 0 : 1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  public void 
  fixAll
  (
   boolean dryRun
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser)) 
      throw new PipelineException
        ("You must be the (" + PackageInfo.sPipelineUser + ") user to run this!");

    boolean isRunning = false;
    {
      MasterMgrClient client = new MasterMgrClient();
      try {
        client.ping();
        isRunning = true;
      }
      catch(PipelineException ex) {
        // ignore...
      }
      finally {
        client.disconnect();
      }
    }
    
    if(isRunning) 
      throw new PipelineException
        ("The Master Manager server is running!  Shutdown first!");

    File lockFile = null;
    {
      Path p = new Path(new Path(PackageInfo.sNodePath, "etc"), "plfixbackslash.lock");
      lockFile = p.toFile();
      if(lockFile.exists()) 
        throw new PipelineException
          ("It appears that you've already run this program!");
      
      if(!dryRun) {
        try {
          lockFile.createNewFile();
        }
        catch(IOException ex) {
          throw new PipelineException ("Unable to create lock file!");
        }
      }
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       dryRun ? "DRY RUN -- NOTHING WILL BE MODIFIED!\n" : "" +
       "Processing Files...");

    {
      Path p = new Path(PackageInfo.sNodePath, "toolsets");
      fixDir(dryRun, p.toFile());
    }

    {
      Path p = new Path(PackageInfo.sNodePath, "repository");
      fixDir(dryRun, p.toFile());
    }

    {
      Path p = new Path(PackageInfo.sNodePath, "working");
      fixDir(dryRun, p.toFile());
    }

    try {
      Writer out = new BufferedWriter(new FileWriter(lockFile));
      out.write("Database files modified:\n");
      try {
        for(File file : pFixedFiles) 
          out.write("  " + file + "\n");
      }
      finally {
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException ("Unable to update lock file!");
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       "All Done.");
  }

  private void 
  fixDir
  (
   boolean dryRun,
   File dir
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
       "Working in dir: " + dir);

    File files[] = dir.listFiles(); 
    if(files != null) {
      for(File file : files) {
        if(file.isDirectory())
          fixDir(dryRun, file);
        else if(file.isFile())
          fixFile(dryRun, file);
      }
    }
  }

  private void 
  fixFile
  (
   boolean dryRun,
   File file
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Finer, 
       "Working on: " + file);
    
    /* first, determine if the file needs to be fixed */ 
    {
      boolean needsFixing = false;
      try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
          while(true) {
            String line = in.readLine();
            if(line == null) 
              break;
            
            Matcher m = sUnescaped.matcher(line);
            if(m.find()) {
              needsFixing = true;
              break;
            }
          }
        }
        finally {
          in.close();
        }
      }
      catch(IOException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
           "Unable to read: " + file);
        throw new PipelineException("Unable to read: " + file, ex);
      }

      if(!needsFixing) 
        return;

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Fine, 
         "Needs fixing: " + file);
    }

    /* create a fixed copy */ 
    if(!dryRun) {
      File fixed = new File(file + ".fixed");
      try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
          Writer out = new BufferedWriter
            (new OutputStreamWriter(new FileOutputStream(fixed), "UTF8"));   
          try {
            int wk, ck;
            int slashes = 0;                      
            char cs[] = new char[1024];
            while(true) {
              int cnt = in.read(cs);
              if(cnt == -1) 
                break;
              
              for(ck=0; ck<cnt; ck++) {
                switch(cs[ck]) {
                case '\\':
                  slashes++;
                  break;
                  
                case '"':
                  for(wk=0; wk<slashes-1; wk++) 
                    out.write("\\\\");
                  if(slashes > 0) 
                    out.write("\\");
                  out.write("\"");
                  slashes = 0;
                  break;
                  
                default:
                  for(wk=0; wk<slashes; wk++) 
                    out.write("\\\\");
                  out.write(cs[ck]);
                  slashes = 0;
                }
              }
            }
            
            for(wk=0; wk<slashes; wk++) 
              out.write("\\\\");
          }
          finally {
            out.close();
          }
        }
        finally {
          in.close();
        }
      }
      catch(IOException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
           "Unable to fix: " + file);
        throw new PipelineException("Unable to fix: " + file, ex);
      }

      /* swap the fixed copy for the original */ 
      if(!file.delete()) 
        throw new PipelineException
          ("Unable to delete existing unfixed file: " + file);
      
      if(!fixed.renameTo(file))
        throw new PipelineException
          ("Unable to replace existing unfixed file with fixed version: " + file);
    }

    pFixedFiles.add(file);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       "Fixed: " + file);
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
       "  plfixbackslash [options]\n" + 
       "\n" + 
       "  plfixbackslash --help\n" +
       "  plfixbackslash --html-help\n" +
       "  plfixbackslash --version\n" + 
       "  plfixbackslash --release-date\n" + 
       "  plfixbackslash --copyright\n" + 
       "  plfixbackslash --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--dry-run] [--log-file=...] [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plfixbackslash --html-help\" to browse the full documentation.\n");
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
    case FixBackslashOptsParserConstants.EOF:
      return "EOF";

    case FixBackslashOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case FixBackslashOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case FixBackslashOptsParserConstants.INTEGER:
      return "an integer";

    case FixBackslashOptsParserConstants.PATH_ARG: 
      return "a filesystem path";

    default: 
      if(printLiteral) 
	return FixBackslashOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final Pattern sUnescaped = 
    Pattern.compile("\\\\+[^\"]"); 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<File> pFixedFiles; 

}



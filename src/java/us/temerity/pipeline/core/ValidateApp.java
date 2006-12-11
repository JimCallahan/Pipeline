// $Id: ValidateApp.java,v 1.1 2006/12/11 21:57:12 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.security.*;
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   V A L I D A T E   A P P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Checks every file in the Pipeline repository against its repository checksum.
 */
public
class ValidateApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  ValidateApp() 
  {
    super("plvalidate");
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
    try {
      if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
	throw new PipelineException
	  ("The plvalidate(1) program may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");
      
      ValidateOptsParser parser = new ValidateOptsParser(new StringReader(pPackedArgs));
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
	 getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Validate all repository files.
   */ 
  public void 
  validate
  (
   Path prodPath
  ) 
    throws PipelineException
  {
    pProdDir = prodPath.toFile(); 

    pRepoDir = new File(pProdDir, "repository");
    if(!pRepoDir.isDirectory()) 
      throw new PipelineException
	("The root repository directory (" + pRepoDir + ") does not exist!");
      
    pSumDir = new File(pProdDir, "checksum/repository");
    if(!pSumDir.isDirectory()) 
      throw new PipelineException
	("The root repository checksum directory (" + pSumDir + ") does not exist!");

    try {
      pDigest = MessageDigest.getInstance("MD5");
    }
    catch(Exception ex) {
      throw new PipelineException("Unable to create Digest!\n  " + ex.getMessage());
    }

    pBuf = new byte[65536];

    validateHelper(pRepoDir);
  }

  /**
   * Recursively validate the repository files under the given directory (or file).
   */
  private void 
  validateHelper
  ( 
   File root
  ) 
  {
    if(root.isDirectory()) {
      File files[] = root.listFiles(); 
      if(files.length == 0) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info, 
	   "VERSION-OFFLINE " + root); 
	LogMgr.getInstance().flush();
      }
      else {
	int wk;
	for(wk=0; wk<files.length; wk++) 
	  validateHelper(files[wk]);
      }
    }
    else {
      String repo = pRepoDir.toString();
      String path = root.toString();

      String msg = null;
      
      if(!root.isFile()) {
	msg = "ILLEGAL-REPO   "; 
      }
      else {
	File cfile = new File(pSumDir, path.substring(repo.length()));
	if(!cfile.isFile()) {
	  msg = "NO-CHECKSUM    "; 
	}
	else {
	  /* read existing checksum */ 
	  byte[] sumA = new byte[pDigest.getDigestLength()]; 
	  {
	    try {
	      FileInputStream in = new FileInputStream(cfile);	
	      try {
		in.read(sumA);
	      }
	      finally {
		in.close();
	      }
	    } 
	    catch(Exception ex) {
	      msg = "BAD-CHECKSUM   "; 
	    }
	  }

	  /* generate a new checksum */ 
	  if(msg == null) {
	    byte sumB[] = null;
	    try {
	      FileInputStream in = new FileInputStream(root);
	      
	      try {
		MessageDigest digest = (MessageDigest) pDigest.clone();
		
		while(true) {
		  int num = in.read(pBuf);
		  if(num == -1) 
		    break;
		  digest.update(pBuf, 0, num);
		}
		
		sumB = digest.digest();
	      }
	      finally {
		in.close();
	      }
	    }
	    catch(Exception ex) {
	      msg = "FAILED-CHECKSUM"; 
	    }

	    /* compare checksums */ 
	    if(msg == null) {
	      if(Arrays.equals(sumA, sumB)) 
		msg = "OK             "; 
	      else if(root.length() == 0) 
		msg = "EMPTY-REPO     "; 
	      else 
		msg = "BAD-REPO       "; 
	    }
	  }
	}
      }
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info, 
	 msg + " " + root); 
      LogMgr.getInstance().flush();
    }
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
       "  plvalidate [options]\n" + 
       "\n" + 
       "  plvalidate --help\n" +
       "  plvalidate --html-help\n" +
       "  plvalidate --version\n" + 
       "  plvalidate --release-date\n" + 
       "  plvalidate --copyright\n" + 
       "  plvalidate --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--base-prod=...]\n" + 
       "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plvalidate --html-help\" to browse the full documentation.\n");
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
    case ValidateOptsParserConstants.EOF:
      return "EOF";

    case ValidateOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case ValidateOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case ValidateOptsParserConstants.INTEGER:
      return "an integer";

    case ValidateOptsParserConstants.PATH_ARG: 
      return "a filesystem path";

    default: 
      if(printLiteral) 
	return ValidateOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private File  pProdDir; 
  private File  pRepoDir; 
  private File  pSumDir; 
  private MessageDigest  pDigest; 
  private byte[]  pBuf; 
}



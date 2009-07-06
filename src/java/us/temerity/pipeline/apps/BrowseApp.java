// $Id: BrowseApp.java,v 1.1 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.apps;

import us.temerity.pipeline.*;
import us.temerity.pipeline.parser.*;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B R O W S E   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plbrowse.html"><B>plbrowse</B></A>(1) tool. <P> 
 */
public
class BrowseApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  BrowseApp() 
  {
    super("plbrowse");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public static void 
  main 
  (
   String[] args
  )
  {
    BrowseApp app = new BrowseApp();
    app.packageArguments(args);

    try {
      BrowseOptsParser parser = new BrowseOptsParser(app.getPackagedArgsReader());
      parser.setApp(app); 
      parser.CommandLine();

      System.exit(0);
    }
    catch(ParseException ex) {
      app.handleParseException(ex);
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }

    System.exit(1);
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
       "  plbrowse [options] --url=URL\n" +
       "\n" + 
       "  plbrowse --help\n" +
       "  plbrowse --html-help\n" +
       "  plbrowse --version\n" + 
       "  plbrowse --release-date\n" + 
       "  plbrowse --copyright\n" + 
       "  plbrowse --license\n" + 
       "\n" + 
       "Use \"plbrowse --html-help\" to browse the full documentation.\n");
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
    case BrowseOptsParserConstants.EOF:
      return "EOF";

    case BrowseOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case BrowseOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case BrowseOptsParserConstants.URL_ARG:
      return "a URL";

    default: 
      if(printLiteral) 
	return BrowseOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}



// $Id: BrowseApp.java,v 1.2 2009/06/04 09:02:00 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.ui.core.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*; 
import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;

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

    try {
      BrowseOptsParser parser = 
	new BrowseOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();
    }
    catch(ParseException ex) {
      handleParseException(ex);
      System.exit(1);
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
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



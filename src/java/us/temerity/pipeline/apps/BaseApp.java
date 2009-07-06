// $Id: BaseApp.java,v 1.1 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.apps;

import us.temerity.pipeline.*;
import us.temerity.pipeline.parser.*;

import java.io.*;
import java.util.*;
import java.awt.Desktop;
import java.net.URI; 

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all standard Pipeline applications. 
 */ 
public abstract 
class BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an application with the given name.
   */ 
  protected
  BaseApp
  ( 
   String name
  )
  {
    if(name == null) 
      throw new IllegalArgumentException("The name of the application cannot be (null)!");
    pName = name;

    /* init common support classes */     
    FileCleaner.init();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S O R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the application.
   */ 
  public String 
  getName()
  {
    return pName;
  }

  /**
   * Get the previously packaged command-line as a StringReader.
   */ 
  public StringReader
  getPackagedArgsReader() 
  {
    return new StringReader(pPackagedArgs); 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public abstract void
  help();

  /**
   * The implementation of the <CODE>--html-help</CODE> command-line option.
   */ 
  public void
  htmlHelp()
  {
    try {
      showURL("file:///" + PackageInfo.sInstPath + "/share/docs/man/" + pName + ".html");
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Sub, LogMgr.Level.Severe,
         "Unable to launch a web browser:\n" + 
         "  " +  ex.getMessage());
    }
  }

  /**
   * The implementation of the <CODE>--version</CODE> command-line option.
   */ 
  public void
  version()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       PackageInfo.sVersion);
  }

  /**
   * The implementation of the <CODE>--release-date</CODE> command-line option.
   */  
  public void
  releaseDate()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       PackageInfo.sRelease);
  }
    
  /**
   * The implementation of the <CODE>--copyright</CODE> command-line option.
   */ 
  public void
  copyright()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       PackageInfo.sCopyright);
  }

  /**
   * The implementation of the <CODE>--license</CODE> command-line option.
   */ 
  public void
  license()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       PackageInfo.sLicense);
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Open the given URL using the default web browser for the local environment.
   * 
   * @throws PipelineException
   *   If unable to launch the browser.
   */ 
  public static void
  showURL
  (
   String url 
  ) 
    throws PipelineException
  {
    try {
      Desktop.getDesktop().browse(new URI(url));
      return;
    }
    catch(Exception ex) {
      throw new PipelineException
        ("Unable to launch native browser to display the URL:\n  " + url + "\n\n" + 
         ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Concatentates the command-line arguments into a single <CODE>String</CODE> suitable 
   * for parsing by the command-line parser of the application subclass. <P> 
   * 
   * Arguments are seperated by (\0) characters.
   * 
   * @param args 
   *   The command-line arguments.
   */
  protected void
  packageArguments
  (
   String[] args
  )  
  {  
    StringBuilder buf = new StringBuilder();
    
    if(args != null) {
      int wk;
      for(wk=0; wk<args.length; wk++) 
        buf.append(args[wk] + "\0");
    }
    
    pPackagedArgs = buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Log a command-line argument parsing exception.
   */
  protected void
  handleParseException
  (
   ParseException ex
  ) 
  {
    StringBuilder buf = new StringBuilder();
    try {
      /* build a non-duplicate set of expected token strings */ 
      TreeSet expected = new TreeSet();
      {
	int wk;
	for(wk=0; wk<ex.expectedTokenSequences.length; wk++) {
	  int kind = ex.expectedTokenSequences[wk][0];
	  String explain = tokenExplain(kind, true);
	  if(explain != null) 
	    expected.add(explain);
	}
      }
      
      /* message header */ 
      Token tok = ex.currentToken.next;
      String next = ex.tokenImage[tok.kind];
      if(next.length() > 0) {
	String value = toASCII(tok.image);
	boolean hasValue = (value.length() > 0);	
	String explain = tokenExplain(tok.kind, false);

	if(hasValue || (explain != null)) {
	  buf.append("Found ");
	  
	  if(explain != null)
	    buf.append(explain + ", ");
	
	  if(hasValue)
	    buf.append("\"" + value + "\" ");

	  buf.append("s");
	}
	else {
	  buf.append("S");
	}

	buf.append("tarting at character (" + ex.currentToken.next.beginColumn + ").\n");
      }

      /* expected token list */ 
      Iterator iter = expected.iterator();
      if(expected.size()==1 && iter.hasNext()) {
	String str = (String) iter.next();
	if(str.equals("<EOF>")) 
	  buf.append("  Was NOT expecting any more arguments!");
	else 
	  buf.append("  Was expecting: " + str);
      }
      else {
	buf.append("  Was expecting one of:\n");
	while(iter.hasNext()) {
	  String str = (String) iter.next();
	  buf.append("    " + str);
	  if(iter.hasNext())
	    buf.append("\n");
	}
      }
    }
    catch (NullPointerException e) {
      buf.append(ex.getMessage());
    }

    /* log the message */ 
    LogMgr.getInstance().log
      (LogMgr.Kind.Arg, LogMgr.Level.Severe,
       buf.toString());
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
    return null;
  }
 
  /**
   * Convert non-printable characters in the given <CODE>String</CODE> into ASCII literals.
   */ 
  private String 
  toASCII
  (
   String str
  ) 
  {
    StringBuilder buf = new StringBuilder();

    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i)) {
      case 0 :
	continue;
      case '\b':
	buf.append("\\b");
	continue;
      case '\t':
	buf.append("\\t");
	continue;
      case '\n':
	buf.append("");  /* newlines are used to seperate args... */ 
	continue;
      case '\f':
	buf.append("\\f");
	continue;
      case '\r':
	buf.append("\\r");
	continue;
      case '\"':
	buf.append("\\\"");
	continue;
      case '\'':
	buf.append("\\\'");
	continue;
      case '\\':
	buf.append("\\\\");
	continue;
      default:
	if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
	  String s = "0000" + Integer.toString(ch, 16);
	  buf.append("\\u" + s.substring(s.length() - 4, s.length()));
	} else {
	  buf.append(ch);
	}
	continue;
      }
    }

    return (buf.toString());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a string consisting the the given character repeated N number of times.
   */ 
  public String
  repeat
  (
   char c,
   int size
  ) 
  {
    StringBuilder buf = new StringBuilder();
    int wk;
    for(wk=0; wk<size; wk++) 
      buf.append(c);
    return buf.toString();
  }

  /**
   * Generate a horizontal bar.
   */ 
  public String
  bar
  (
   int size
  ) 
  {
    return repeat('-', size);
  }

  /**
   * Generate a horizontal title bar.
   */ 
  public String
  tbar
  (
   int size
  ) 
  {
    return repeat('=', size);
  }

  /**
   * Pad the given string so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str, 
   char c,
   int size
  ) 
  {
    return (str + repeat(c, Math.max(0, size - str.length())));
  }

  /**
   * Pad the given string with spaces so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str,
   int size
  ) 
  {
    return pad(str, ' ', size);
  }

  /**
   * Generate N spaces. 
   */ 
  public String
  pad
  (
   int size
  ) 
  {
    return repeat(' ', size);
  }

  /**
   * Left pad the given string so that it is at least N characters long.
   */ 
  public String
  lpad
  (
   String str, 
   char c,
   int size
  ) 
  {
    return (repeat(c, Math.max(0, size - str.length())) + str);
  }

  /**
   * Left pad the given string with spaces so that it is at least N characters long.
   */ 
  public String
  lpad
  (
   String str,
   int size
  ) 
  {
    return lpad(str, ' ', size);
  }

  /**
   * Line wrap the given String at word boundries.
   */ 
  public String
  wordWrap
  (
   String str,
   int indent, 
   int size
  ) 
  {
    if(str.length() + indent < size) 
      return str;

    StringBuilder buf = new StringBuilder();
    String words[] = str.split("\\p{Blank}");
    int cnt = indent;
    int wk;
    for(wk=0; wk<words.length; wk++) {
      int ws = words[wk].length();
      if(ws > 0) {
	if((size - cnt - ws) > 0) {
	  buf.append(words[wk]);
	  cnt += ws;
	}
	else {
	  buf.append("\n" + repeat(' ', indent) + words[wk]);
	  cnt = indent + ws;
	}

	if(wk < (words.length-1)) {
	  if((size - cnt) > 0) {
	    buf.append(' ');
	    cnt++;
	  }
	  else {
	    buf.append("\n" + repeat(' ', indent));
	    cnt = indent;
	  }
	}
      }
    }

    return buf.toString();
  }

  /**
   * Generate a Jim title string.  Used while iterating through all the PluginType enums.
   * All strings should be NOT null and greater than zero length, but still doing the check
   * using the garbage in garbage out scheme.
   */
  public String
  title
  (
   String title
  )
  {
    if(title == null)
      return title;

    if(title.length() == 0)
      return title;

    StringBuilder buf = new StringBuilder();

    char[] cs = title.toCharArray();

    buf.append(" ");
    buf.append(" ");
    buf.append(cs[0]);

    for(int i = 1 ; i < cs.length ; i++) {
      buf.append(" ");
      if(Character.isUpperCase(cs[i])) {
	buf.append(" ");
	buf.append(" ");
	buf.append(cs[i]);
      }
      else {
	buf.append(Character.toUpperCase(cs[i]));
      }
    }

    return buf.toString();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the application.
   */ 
  private String pName;        

  /**
   * The single concatenated command-line argument string.
   */
  private String pPackagedArgs;  

}



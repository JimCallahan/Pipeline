// $Id: BaseApp.java,v 1.11 2005/01/11 12:52:55 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import java.io.*; 
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all Pipeline applications. 
 */ 
public abstract 
class BaseApp
  extends BootApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an application with the given name and command-line arguments.
   * 
   * @param name 
   *   The name of the application executable.
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
    Logs.init();
    FileCleaner.init();
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
    showURL("file://" + PackageInfo.sDocsDir + "/man/" + pName + ".html");
  }

  /**
   * The implementation of the <CODE>--version</CODE> command-line option.
   */ 
  public void
  version()
  {
    Logs.ops.info(PackageInfo.sVersion);
  }

  /**
   * The implementation of the <CODE>--release-date</CODE> command-line option.
   */  
  public void
  releaseDate()
  {
    Logs.ops.info(PackageInfo.sRelease);
  }
    
  /**
   * The implementation of the <CODE>--copyright</CODE> command-line option.
   */ 
  public void
  copyright()
  {
    Logs.ops.info(PackageInfo.sCopyright);
  }

  /**
   * The implementation of the <CODE>--license</CODE> command-line option.
   */ 
  public void
  license()
  {
    Logs.ops.info(PackageInfo.sLicense);
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Open the given URL in mozilla(1).
   */ 
  public static void
  showURL
  (
   String url 
  ) 
  {
    Map<String,String> env = System.getenv();
    File dir = PackageInfo.sTempDir;

    boolean isRunning = false;
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-remote");
      args.add("ping()");
      
      SubProcessLight proc = 
	new SubProcessLight("CheckMozilla", "mozilla", args, env, dir);
      try {
	proc.start();
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      
      isRunning = proc.wasSuccessful();
    }

    if(isRunning) {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-remote");
      args.add("openURL(" + url + ", new-tab)");

      SubProcessLight proc = 
	new SubProcessLight("RemoteMozilla", "mozilla", args, env, dir);
      try {      
	proc.start();
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      args.add(url.toString());

      SubProcessLight proc = 
	new SubProcessLight("LaunchMozilla", "mozilla", args, env, dir);
      proc.start();
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
    StringBuffer buf = new StringBuffer();
    
    int wk;
    for(wk=0; wk<args.length; wk++) 
      buf.append(args[wk] + "\0");
    
    pPackedArgs = buf.toString();
  }

  /**
   * Reads the given file containing command-line arguments and generates lines of input
   * suitable for parsing by the command-line parser of the application subclass. <P> 
   * 
   * The whitespace in the file is replaced by (\0) characters to seperate 
   * command-line arguments.  Each command-line of input from the file in converted into 
   * one of the Strings returned by this method.  Command lines in the file may continue
   * over multiple lines by escaping the newline with (\) characters.
   * 
   * @param file
   *   The arguments file.
   */
  public ArrayList<String>
  packageFile
  (
   File file
  )
    throws PipelineException
  {
    try {
      BatchParser parser = new BatchParser(new FileReader(file));
      return (ArrayList<String>) parser.Contents();
    }
    catch(Exception ex) {
      throw new PipelineException(ex);
    }    
  }



  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  protected String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuffer buf = new StringBuffer();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }
  

  /**
   * Log a command-line argument parsing exception.
   */
  protected void
  handleParseException
  (
   ParseException ex
  ) 
  {
    StringBuffer buf = new StringBuffer();
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
    Logs.arg.severe(buf.toString());
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
    StringBuffer buf = new StringBuffer();

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
    StringBuffer buf = new StringBuffer();
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
   * Pad the given string with the given string so that it is at least N characters long.
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

    StringBuffer buf = new StringBuffer();
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


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the application.
   */ 
  protected String pName;        

  /**
   * The single concatenated command-line argument string.
   */
  protected String pPackedArgs;  

}



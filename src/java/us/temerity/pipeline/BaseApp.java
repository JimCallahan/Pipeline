// $Id: BaseApp.java,v 1.1 2004/03/12 13:49:09 jim Exp $

package us.temerity.pipeline;

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
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an application with the given name and command-line arguments.
   * 
   * @param name [<B>in</B>]
   *   The name of the application executable.
   * 
   * @param args [<B>in</B>]
   *   The command-line arguments.
   */ 
  public
  BaseApp
  ( 
   String name, 
   String[] args
  )
  {
    if(name == null) 
      throw new IllegalArgumentException("The name of the application cannot be (null)!");
    pName = name;

    setPackedArgs(args);

    /* initialization */ 
    Logs.init();
    FileCleaner.init();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the application.
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }


  /**
   * Get the single concatenated command-line argument string.
   */ 
  public String
  getPackedArgs() 
  {
    return pPackedArgs;
  }

  /**
   * Concatentate all of the command-line arguments into a single <CODE>String</CODE>
   * suitable for parsing by the command-line parser of the application.
   * 
   * @param args [<B>in</B>]
   *   The command-line arguments.
   */ 
  public synchronized void
  setPackedArgs
  (
   String[] args 
  )
  {
    StringBuffer buf = new StringBuffer();
    
    int wk;
    for(wk=0; wk<args.length; wk++) {
      char chars[] = args[wk].toCharArray();

      int eqIdx = -1;
      boolean hasWs = false;

      int ck;
      for(ck=0; ck<chars.length; ck++) {
	if(chars[ck] == '=')
	  eqIdx = ck+1;
	else if((chars[ck] == ' ') || (chars[ck] == '\t')) {
	  hasWs = true;
	  break;
	}
      }

      if(hasWs && (eqIdx != -1) && (eqIdx < args[wk].length()))
	buf.append(args[wk].substring(0, eqIdx) + 
		   "\"" + args[wk].substring(eqIdx) + "\" ");
      else 
	buf.append(args[wk] + " ");
    }
    
    pPackedArgs = buf.toString();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The top-level method of the application. <P> 
   * 
   * Subclasses must override this method.
   */
  public abstract void
  run();




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
    Map<String,String> env = System.getenv();
    File dir = new File("/usr/tmp");

    boolean isRunning = false;
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-remote");
      args.add("ping()");
      
      SubProcess proc = 
	new SubProcess("CheckMozilla", PackageInfo.sMozilla.getPath(), args, env, dir);
      proc.start();

      try {
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
      args.add("openURL(" + 
	       "file://" + PackageInfo.sDocsDir + "/man/" + pName + ".html" + 
	       ", new-tab)");

      SubProcess proc = 
	new SubProcess("RemoteMozilla", PackageInfo.sMozilla.getPath(), args, env, dir);
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      args.add("file://" + PackageInfo.sDocsDir + "/man/" + pName + ".html");

      SubProcess proc = 
	new SubProcess("LaunchMozilla", PackageInfo.sMozilla.getPath(), args, env, dir);
      proc.start();
    }
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
    



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex [<B>in</B>]
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
    buf.append("Illegal Args: ");

    try {
      /* build a non-duplicate set of expected token strings */ 
      TreeSet expected = new TreeSet();
      {
	int wk;
	for(wk=0; wk<ex.expectedTokenSequences.length; wk++) {
	  String str = ex.tokenImage[ex.expectedTokenSequences[wk][0]];
	  if(!str.equals("\"\\n\"") && !str.equals("<NL1>"))
	    expected.add(ex.tokenImage[ex.expectedTokenSequences[wk][0]]);
	}
      }

      
      /* message header */ 
      Token tok = ex.currentToken.next;
      String next = ex.tokenImage[tok.kind];
      if(next.length() > 0) {
	
	char[] ary = next.toCharArray();
	boolean hasKind  = (ary.length>0 && ary[0] == '<' && ary[ary.length-1] == '>');
	
	String value = toASCII(tok.image);
	boolean hasValue = (value.length() > 0);
	
	if(hasKind || hasValue) 
	  buf.append("found ");
	
	if(hasKind) 
	  buf.append(next + ", ");
	
	if(hasValue)
	  buf.append("\"" + value + "\", ");
      }
      
      buf.append("starting at arg " + ex.currentToken.next.beginLine + 
		 ", character " + ex.currentToken.next.beginColumn + ".\n");
      

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
  private String pPackedArgs;  

}



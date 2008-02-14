// $Id: RemoteServer.java,v 1.2 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.core.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O T E   S E R V E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Handles requests from plremote(1) to control the bahavior of plui(1) remotely.
 * 
 * This class listens for new connections from plremote(1) and creats a thread to manage each 
 * connection.  Each of these threads then listens for raw ASCII string data terminated by a
 * NULL (0) character.  The server is responsible for parsing and interpreting the meaning
 * of this raw ASCII string data to determine how to call methods of UIMaster in response to
 * the requests received.
 */
public
class RemoteServer
  extends BaseMgrServer
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin manager server.
   * 
   * @param 
   *   The UIMaster parent instance.
   */
  public
  RemoteServer
  (
   UIMaster master
  )
  { 
    super("RemoteServer");

    pTimer  = new TaskTimer();
    pMaster = master;
    pTasks  = new HashSet<HandlerTask>();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initiate a shutdown of all network connections.
   */
  public void 
  shutdown() 
  {
    pShutdown.set(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin listening to the network port and spawn threads to process the file management 
   * requests received over the connection. <P>
   * 
   * This will only return if there is an unrecoverable error.
   */
  public void 
  run() 
  {
    ServerSocketChannel schannel = null;
    try {
      schannel = ServerSocketChannel.open();
      ServerSocket server = schannel.socket();
      InetSocketAddress saddr = new InetSocketAddress(PackageInfo.sRemotePort);
      server.bind(saddr, 100);
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Listening on Port: " + PackageInfo.sRemotePort);
      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Remote Ready.\n" + 
	 "  Started in " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();
      pTimer = new TaskTimer();

      schannel.configureBlocking(false);
      while(!pShutdown.get()) {
	SocketChannel channel = schannel.accept();
	if(channel != null) {
	  HandlerTask task = new HandlerTask(channel);
	  pTasks.add(task);
	  task.start();	
	}
	else {
	  Thread.sleep(PackageInfo.sServerSleep);
	}
      }

      try {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Info,
	   "Waiting on Client Handlers...");
	LogMgr.getInstance().flush();
	
	synchronized(pTasks) {
	  for(HandlerTask task : pTasks) 
	    task.closeConnection();
	}
	
	synchronized(pTasks) {
	  for(HandlerTask task : pTasks) 
	    task.join();
	}
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Net, LogMgr.Level.Severe,
	   "Interrupted while shutting down!");
	LogMgr.getInstance().flush();
      }
    }
    catch (IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
         ("IO problems on port (" + PackageInfo.sRemotePort + "):", ex)); 
      LogMgr.getInstance().flush();
    }
    catch (SecurityException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage
	 ("The Security Manager doesn't allow listening to sockets!", ex)); 
      LogMgr.getInstance().flush();
    }
    catch (Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
         Exceptions.getFullMessage(ex)); 
    }
    finally {
      if(schannel != null) {
	try {
	  schannel.close();
	}
	catch (IOException ex) {
	}
      }

      pTimer.suspend();
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Info,
	 "Remote Shutdown.\n" + 
	 "  Uptime " + TimeStamps.formatInterval(pTimer.getTotalDuration()));
      LogMgr.getInstance().flush();  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle an incoming connection from a <CODE>RemoteClient</CODE> instance.
   */
  private 
  class HandlerTask
    extends Thread
  {
    public 
    HandlerTask
    (
     SocketChannel channel
    ) 
    {
      super("RemoteServer:HandlerTask");
      pChannel = channel;
    }

    public void 
    run() 
    {
      try {
        try {
          pSocket = pChannel.socket();
          LogMgr.getInstance().log
            (LogMgr.Kind.Net, LogMgr.Level.Fine,
             "Connection Opened: " + pSocket.getInetAddress());
          LogMgr.getInstance().flush();

          if(pSocket.isConnected() && !pShutdown.get()) {
            InputStream in = pSocket.getInputStream();

            /* get the raw request text */ 
            String raw = null;
            {
              StringBuilder buf = new StringBuilder();
              byte[] bytes = new byte[1024]; 
              while(true) {
                int cnt = in.read(bytes);
                if(cnt < 0) 
                  break;

                buf.append(new String(bytes, 0, cnt, "US-ASCII"));
              }
            
              raw = buf.toString();

              LogMgr.getInstance().log
                (LogMgr.Kind.Net, LogMgr.Level.Finer,
                 "Request [" + pSocket.getInetAddress() + "]: " + raw);     
              LogMgr.getInstance().flush();
            }

            /* parse the request */ 
            try {
              BatchParser bparser = new BatchParser(new StringReader(raw));
              ArrayList lines = bparser.Contents();
              Iterator iter = lines.iterator();
              while(iter.hasNext()) {
                String line = (String) iter.next();
            
                LogMgr.getInstance().log
                  (LogMgr.Kind.Ops, LogMgr.Level.Info,
                   "Remote Command: " + line.replace('\0',' '));
                LogMgr.getInstance().flush();
              
                RemoteOptsParser parser = new RemoteOptsParser(new StringReader(line));
                parser.init(pMaster);
                parser.Command();
              }
            }
            catch(ParseException ex) {
              handleParseException(ex);
            }
          }
        }
        catch(AsynchronousCloseException ex) {
        }
        catch (EOFException ex) {
          throw new PipelineException
            ("Connection on port (" + PackageInfo.sRemotePort + ") terminated abruptly!");
        }
        catch (IOException ex) {
          throw new PipelineException
            ("IO problems on port (" + PackageInfo.sRemotePort + "):", ex, true, true); 
        }
        catch (PipelineException ex) {
          throw ex; 
        }
        catch (Exception ex) {
          throw new PipelineException(null, ex, true, true); 
        }
        finally {
          closeConnection();

          if(!pShutdown.get()) {
            synchronized(pTasks) {
              pTasks.remove(this);
            }
          }
        }
      }
      catch(PipelineException ex) {
        pMaster.showErrorDialog("Remote Server Error:", ex.getMessage());
      }
    }

    public void 
    closeConnection() 
    {
      if(!pChannel.isOpen()) 
	return;

      try {
	pChannel.close();
      }
      catch(IOException ex) {
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Fine,
	 "Client Connection Closed.");
      LogMgr.getInstance().flush();
    }
    
    private SocketChannel  pChannel; 
    private Socket         pSocket;     
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Log a command-line argument parsing exception.
   */
  protected void
  handleParseException
  (
   ParseException ex
  )
    throws PipelineException
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

    throw new PipelineException(buf.toString());
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
    case RemoteOptsParserConstants.EOF:
      return "EOF";
    
    case RemoteOptsParserConstants.UNKNOWN_COMMAND1:
    case RemoteOptsParserConstants.UNKNOWN_COMMAND2:
      return "an unknown command";

    case RemoteOptsParserConstants.UNKNOWN1:
    case RemoteOptsParserConstants.UNKNOWN2:
      return "an unknown argument";
      
    case RemoteOptsParserConstants.UNKNOWN_OPTION1:
    case RemoteOptsParserConstants.UNKNOWN_OPTION2:
    case RemoteOptsParserConstants.UNKNOWN_OPTION3:
    case RemoteOptsParserConstants.UNKNOWN_OPTION4:
      return "an unknown option";

    case RemoteOptsParserConstants.NODE_NAME:
      return "a fully resolved node name";

    case RemoteOptsParserConstants.REVISION_NUMBER:
      return "a revision number";

    case RemoteOptsParserConstants.AE1:
    case RemoteOptsParserConstants.AE2:
    case RemoteOptsParserConstants.AE3:
    case RemoteOptsParserConstants.AE4:
    case RemoteOptsParserConstants.AE5:
      return null;

    default: 
      if(printLiteral)
	return RemoteOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Times server startup and uptime.
   */ 
  private TaskTimer  pTimer; 

  /**
   * The parent UIMaster instance. 
   */
  private UIMaster  pMaster; 
  
  /**
   * The set of currently running tasks.
   */ 
  private HashSet<HandlerTask>  pTasks;
}


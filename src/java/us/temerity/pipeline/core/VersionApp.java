// $Id: VersionApp.java,v 1.2 2008/05/16 06:39:22 jim Exp $

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
/*   V E R S I O N   A P P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plversion.html"><B>plversion</B></A>(1) tool. <P> 
 */
public
class VersionApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  VersionApp() 
  {
    super("plversion");
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
      VersionOptsParser parser = 
	new VersionOptsParser(new StringReader(pPackedArgs));
      
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
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */ 
  public void 
  checkInFile
  (
   File file 
  ) 
  { 
    SwingUtilities.invokeLater(new StartupTask(new CheckInStatusTask(file))); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Show an error message dialog with the given title and message.
   */ 
  public void 
  showErrorDialog
  (
   String title, 
   String msg, 
   boolean quitOnClose
  ) 
  {
    pErrorDialog.setMessage(title, msg);
    SwingUtilities.invokeLater(new ShowErrorDialogTask(quitOnClose));
  }

  /**
   * Show an error message dialog for the given exception.
   */ 
  private void 
  showErrorDialog
  (
   Exception ex, 
   boolean quitOnClose
  ) 
  {
    pErrorDialog.setMessage(ex);
    SwingUtilities.invokeLater(new ShowErrorDialogTask(quitOnClose));
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
       "  plversion [options] --check-in=file\n" +  
       "\n" + 
       "  plversion --help\n" +
       "  plversion --html-help\n" +
       "  plversion --version\n" + 
       "  plversion --release-date\n" + 
       "  plversion --copyright\n" + 
       "  plversion --license\n" + 
       "\n" + 
       "OPTIONS:\n" +
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plversion --html-help\" to browse the full documentation.\n");
  }
 




  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Close the network connections and exit.
   */ 
  public void 
  doQuit()
  {
    if(pMasterMgrClient != null) 
      pMasterMgrClient.disconnect();
      
    /* give the sockets time to disconnect cleanly */ 
    try {
      Thread.sleep(500);
    }
    catch(InterruptedException ex) {
    }

    LogMgr.getInstance().cleanup();
    System.exit(0);
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
    case VersionOptsParserConstants.EOF:
      return "EOF";

    case VersionOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case VersionOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case VersionOptsParserConstants.PATH_ARG:
      return "an file system path";

    case VersionOptsParserConstants.INTEGER:
      return "an integer";

    default: 
      if(printLiteral) 
	return VersionOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Show a startup message.
   */ 
  private
  class StartupTask
    extends Thread
  { 
    StartupTask
    (
     Thread next
    ) 
    {
      super("StartupTask");
      
      pNextThread = next;
    }

    public void 
    run() 
    {  
      /* connect to servers */ 
      try {
        PluginMgrClient.init(true);           
        pMasterMgrClient = new MasterMgrClient(); 
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
        System.exit(1);
      }

      /* load the look-and-feel */ 
      try {
        SynthLookAndFeel synth = new SynthLookAndFeel();
        synth.load(LookAndFeelLoader.class.getResourceAsStream("synth.xml"),
                   LookAndFeelLoader.class);
        UIManager.setLookAndFeel(synth);
      }
      catch(java.text.ParseException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to parse the look-and-feel XML file (synth.xml):\n" + 
           "  " + ex.getMessage());
        System.exit(1);
      }
      catch(UnsupportedLookAndFeelException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           "Unable to load the Pipeline look-and-feel:\n" + 
           "  " + ex.getMessage());
        System.exit(1);
      }

      /* initialize error dialog */ 
      pErrorDialog = new JErrorDialog((Frame) null);

      /* get to work... */ 
      pNextThread.start(); 
    }

    private Thread pNextThread; 
  }   


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Determine the node associated with the file and whether it can be checked-in.
   */ 
  private
  class CheckInStatusTask
    extends Thread
  { 
    CheckInStatusTask
    (
     File file
    ) 
    {
      super("CheckInStatusTask");
      pCheckInFile = file; 
    }

    public void 
    run() 
    {  
      try {
        /* determine the working area and node name from the file name */ 
        String author = null;
        String view   = null;
        String name   = null;
        try {
          File file = NativeFileSys.realpath(pCheckInFile); 
          Path cpath = new Path(file); 
          String cstr = cpath.toString(); 

          Path working = new Path(PackageInfo.sProdPath, "working");
          String pstr = working.toString(); 

          if(cstr.startsWith(pstr)) {
            Path wpath = new Path(cstr.substring(pstr.length()));
            ArrayList<String> comps = wpath.getComponents(); 
            if(comps.size() > 2) {
              author = comps.get(0);
              view   = comps.get(1);
              
              Path npath = new Path("/"); 
              int wk;
              for(wk=2; wk<comps.size(); wk++) {
                String c = comps.get(wk);
                if(wk == (comps.size()-1)) {
                  String parts[] = c.split("\\."); 
                  c = parts[0];
                }

                npath = new Path(npath, c); 
              }

              name = npath.toString(); 
            }
          }

          if((author == null) || (view == null) || (name == null)) 
            throw new PipelineException
              ("The file to check-in (" + pCheckInFile + "), does not reside in any " + 
               "working area managed by Pipeline!"); 
        }
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to determine the canonicalized absolute form of the file to check-in " + 
             "(" + pCheckInFile + ")!"); 
        }

        /* get the lightweight status of the node */ 
        NodeStatus status = pMasterMgrClient.status(author, view, name, true);

        /* check for task annotations */ 
        ArrayList<String> purposes = new ArrayList<String>(); 
        {
          TreeMap<String,BaseAnnotation> annots = pMasterMgrClient.getAnnotations(name);
          for(String aname : annots.keySet()) {
            BaseAnnotation an = annots.get(aname);
            
            String purpose = (String) an.getParamValue(aPurpose);
            if((purpose != null) && (purpose.length() > 0)) 
              purposes.add(purpose); 
          }
        }

        /* hide the splash screen */ 
        {
          SplashScreen splash = SplashScreen.getSplashScreen(); 
          if(splash.isVisible())
            splash.close(); 
        }

        /* if its an Edit node or there aren't any annotations, 
             then proceed with the check-in */ 
        if(purposes.isEmpty() || purposes.contains(aEdit)) {
          SwingUtilities.invokeLater(new CheckInQueryTask(status)); 
        }
        /* otherwise, give the user a chance to inspect the node in plui(1) */ 
        else {
          StringBuilder buf = new StringBuilder(); 
          buf.append
            ("Unable to perform a trivial check-in of the file:\n\n" + 
             "  " + pCheckInFile + "\n\n" + 
             "This file is being managed by the following Pipeline node in the " + 
             "(" + author + "|" + view + ") working area:\n\n" + 
             "  " + name + "\n\n" + 
             "This node has task related annotations which specify its purpose(s) in the " + 
             "workflow as being a ("); 

          int wk; 
          for(wk=0; wk<purposes.size(); wk++) {
            if(wk != 0) 
              buf.append(", "); 
            buf.append(purposes.get(wk));
          }

          buf.append
            (") node.  Only (Edit) nodes or nodes without Task Annotations can be " + 
             "checked-in directly by this tool.  Other nodes must be checked-in inside " + 
             "the full Pipeline application where the consequences of the check-in " + 
             "operation can be properly evaluated.\n\n");

          buf.append
            ("Note that studio policies may prevent this node from being checked-in " + 
             "even inside of Pipeline, except as part of the check-in of a (Submit) node. " +
             "When Task Annotation are being used, usually only (Edit) or (Submit) nodes " +
             "may be checked-in directly."); 

          SwingUtilities.invokeLater(new InspectTask(status, buf.toString()));
        }
      }
      catch(PipelineException ex) {
        showErrorDialog(ex, true);
      }
    }   

    private File pCheckInFile; 
  }

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create the dialog to query the user for check-in parameters.
   */ 
  private
  class CheckInQueryTask
    extends Thread
  { 
    CheckInQueryTask
    (
     NodeStatus status
    ) 
    {
      super("CheckInQueryTask");
      pStatus = status; 
    }

    public void 
    run() 
    {  
      JCheckInDialog diag = new JCheckInDialog((Frame) null);
      
      {
        NodeDetails details = pStatus.getDetails(); 
        VersionID latest = null;
        if(details.getLatestVersion() != null) 
          latest = details.getLatestVersion().getVersionID();
        
        diag.updateNameVersion("Check-In: " + pStatus, latest, false);
      }
      
      diag.setVisible(true);        
      if(diag.wasConfirmed()) {
        String desc = diag.getDescription();          
        VersionID.Level level = diag.getLevel();
        
        CheckInTask task = new CheckInTask(pStatus.getNodeID(), desc, level);
        task.start();
      } 
      else {
        doQuit(); 
      }
    }

    private NodeStatus pStatus;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Check-in the given node.
   */ 
  private
  class CheckInTask
    extends Thread
  {
    public 
    CheckInTask
    (
     NodeID nodeID, 
     String desc,
     VersionID.Level level
    ) 
    {
      super("CheckInTask");
      pNodeID      = nodeID; 
      pDescription = desc;
      pLevel       = level;
    }

    public void 
    run() 
    {
      try {
        pMasterMgrClient.checkIn(pNodeID, pDescription, pLevel);
        doQuit(); 
      }
      catch(PipelineException ex) {
        showErrorDialog(ex, true);
      }
    }

    private NodeID           pNodeID; 
    private String           pDescription;  
    private VersionID.Level  pLevel;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show the error dialog. <P> 
   * 
   * The reason for the thread wrapper is to allow the rest of the UI to repaint before
   * showing the dialog.
   */ 
  private
  class ShowErrorDialogTask
    extends Thread
  { 
    public 
    ShowErrorDialogTask
    (
     boolean quitOnClose
    ) 
    {
      super("ShowErrorDialogTask");      
      pQuitOnClose = quitOnClose; 
    }

    public void 
    run() 
    {
      /* hide the splash screen */ 
      {
        SplashScreen splash = SplashScreen.getSplashScreen(); 
        if(splash.isVisible())
          splash.close(); 
      }
      
      pErrorDialog.setVisible(true);      

      if(pQuitOnClose)
        doQuit(); 
    }

    private boolean pQuitOnClose; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create the dialog giving the choice of inspecting related nodes in plui(1). 
   */ 
  private
  class InspectTask
    extends Thread
  { 
    InspectTask
    (
     NodeStatus status, 
     String msg
    ) 
    {
      super("InspectTask");
      pStatus  = status; 
      pMessage = msg; 
    }

    public void 
    run() 
    {  
      {
        JConfirmDialog diag = 
          new JConfirmDialog((Frame) null, "Inspect in Pipeline?", pMessage); 

        diag.setVisible(true);   
        if(diag.wasConfirmed()) {
          RemoteTask task = new RemoteTask(pStatus.getName());
          task.start();          
        }
        else {
          doQuit(); 
        }
      }
    }

    private NodeStatus pStatus;
    private String     pMessage; 
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Attempt to communicate with plui, directing it to inspect the node.
   */ 
  private
  class RemoteTask
    extends Thread
  { 
    RemoteTask
    (
     String name
    ) 
    {
      super("RemoteTask");
      pNodeName = name; 
    }

    public void 
    run() 
    {  
      if(!sendViewCommand()) {   
        try {
          Path plui = null;
          {
            String osarch = (PackageInfo.sOsType + "-" + PackageInfo.sArchType + "-Debug");

            String extra = "";
            switch(PackageInfo.sOsType) {
            case MacOS:
              extra = "-j2dgl";
              break;
              
            case Windows:
              extra = "-j2dgl.bat";
            }

            plui = new Path(PackageInfo.sInstPath, osarch + "/bin/plui" + extra);
          }

          ArrayList<String> args = new ArrayList<String>(); 
          args.add("--no-selections"); 

          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "Starting plui..."); 

          SubProcessLight proc = new SubProcessLight("plui", plui.toFile(), args);
          proc.start();

          {
            Thread.sleep(10000);

            boolean success = false;
            int wk;
            for(wk=0; wk<15; wk++) {
              if(sendViewCommand()) {
                success = true;
                break;
              }

              Thread.sleep(5000);
            }

            if(!success) 
              throw new Exception(); 
          }

          doQuit(); 
        }
        catch(Exception ex) {
          showErrorDialog("Error:", "Unable to contact or start plui!", true); 
        }
      }
    }

    private boolean 
    sendViewCommand() 
    {  
      try {
        InetSocketAddress addr = new InetSocketAddress("localhost", PackageInfo.sRemotePort);

        Socket s = new Socket();
        s.connect(addr, 10000);
      
        String cmd = ("working --select=" + pNodeName); 
        byte[] bytes = cmd.getBytes("US-ASCII");

        OutputStream out = s.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close(); 

        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "Told plui to view the node: " + pNodeName); 

        return true;
      } 
      catch(IOException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Warning,
           "Unable to contact plui!\n  " + ex.getMessage()); 

        return false;
      }
    }

    private String pNodeName; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String aPurpose = "Purpose";
  private static final String aEdit    = "Edit";



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network interface to the <B>plmaster</B>(1) daemon.
   */ 
  private MasterMgrClient  pMasterMgrClient;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The error message dialog.
   */ 
  private JErrorDialog  pErrorDialog;

}



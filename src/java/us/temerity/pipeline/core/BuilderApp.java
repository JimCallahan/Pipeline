package us.temerity.pipeline.core;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BuilderInformation;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   A P P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plbuilder.html"><B>plbuilder</B></A>(1) application. <P> 
 */
public class BuilderApp
  extends BaseApp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public BuilderApp()
  {
    super("plbuilder");
    pCommandLineParams = new MultiMap<String, String>();
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
  @SuppressWarnings("unchecked")
  public void 
  run
  (
    String[] args
  ) 
  {
    if(args.length == 0) {
      System.out.print("Builder name was missing!\n");
      System.exit(1);
    }
    
    boolean hasClassName = !(args[0].startsWith("--")); 
    pBuilderClassName = args[0];

    if (!hasClassName)
      packageArguments(args);
    else
    {
      String appArgs[] = null;
      {
	appArgs = new String[args.length-1];
	int wk;
	for(wk=0; wk<appArgs.length; wk++) { 
	  appArgs[wk] = args[wk+1];
	}
      }
      packageArguments(appArgs);
    }
    
    boolean success = false;
    
    try {
      PluginMgrClient.init();
    }
    catch (PipelineException ex)
    {
      LogMgr.getInstance().log(Kind.Ops, Level.Severe, 
	"Unable to Initialize the plugin Manager.  Builder Aborting.\n" + ex.getMessage());
      return;
    }
    
    MasterMgrClient mclient = new MasterMgrClient();
    QueueMgrClient qclient = new QueueMgrClient();
    
    LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
    
    try {
      BuilderOptsParser parser = 
	new BuilderOptsParser(new StringReader(pPackedArgs));
      parser.setApp(this);
      if (hasClassName) {
	ClassLoader loader = ClassLoader.getSystemClassLoader();
	Class cls = loader.loadClass(pBuilderClassName);
	Constructor construct = 
	  cls.getConstructor
	  (MasterMgrClient.class, 
	   QueueMgrClient.class, 
	   BuilderInformation.class);
	parser.CommandLine();
	BuilderInformation info = 
          new BuilderInformation(pGui, pAbortOnGui, pCommandLineParams);
	BaseBuilder builder = (BaseBuilder) construct.newInstance(mclient, qclient, info);
	builder.run();
	success = true;
      } 
      else {
	parser.CommandLine();
      }
    }
    catch(ParseException ex) {
      ex.printStackTrace();
      handleParseException(ex);
    }
    catch (PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch (NoSuchMethodException ex) {
      String message = 
	"Was unable to instantiate the constructor for the specified Builder.  " +
	"This most likely means that the Builder was not meant to be run as a " +
	"standalone builder.\n";
      message += ex.getMessage();
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 message);
      ex.printStackTrace();
    }
    catch (InvocationTargetException ex) {
      Throwable th = ex.getTargetException();
      String message = 
	"An Invocation Target Exception has occured.  This most likely indicates that " +
	"the name of the builder being passed to BuilderApp is specified incorrectly or " +
	"that an error occured in the Builder's constructor.\n";
      message += getFullMessage(th);
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 message);
      //th.printStackTrace();
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
      	ex.printStackTrace();
    }
    finally {
      LogMgr.getInstance().cleanup();
    }
    
    System.exit(success ? 0 : 1);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public 
  void help()
  {
    String wrapper = "BuilderName"; 
    {
      String parts[] = pBuilderClassName.split("\\.");
      if(parts.length > 0) 
        wrapper = parts[parts.length-1];
    }

    LogMgr.getInstance().log
    (LogMgr.Kind.Ops, LogMgr.Level.Info,
     "USAGE:\n" +
     "  pl" + wrapper + " [options]\n" + 
     "\n" + 
     "  pl" + wrapper + " --help\n" +
     "  pl" + wrapper + " --html-help\n" +
     "  pl" + wrapper + " --version\n" + 
     "  pl" + wrapper + " --release-date\n" + 
     "  pl" + wrapper + " --copyright\n" + 
     "  pl" + wrapper + " --license\n" + 
     "\n" + 
     "GLOBAL OPTIONS:\n" +
     "  [--log=...] \n" + 
     "  [--abort] [--gui] \n" + 
     "  [--builder=...] [--<ParamName>=<ParamValue>...] ...\n" +
     "\n" + 
     "\n" +  
     "Use \"pl" + wrapper + " --html-help\" to browse the full documentation.\n");
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
    case BuilderOptsParserConstants.EOF:
      return "EOF";

    case BuilderOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case BuilderOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case BuilderOptsParserConstants.GUI:
      return "the gui flag";

    case BuilderOptsParserConstants.PARAM_NAME:
      return "the name of a parameter";
      
    case BuilderOptsParserConstants.PARAM_VALUE:
      return "the value of a parameter";
      
    case BuilderOptsParserConstants.BUILDER_NAME:
      return "the name of a builder";
      
    default: 
      if(printLiteral)
	return BuilderOptsParserConstants.tokenImage[kind];
      return null;
    }      
  }
  
  public void
  setUsingGUI
  (
    boolean gui
  )
  {
    pGui = gui;
  }
  
  public void
  setAbortOnBadParam
  (
    boolean abort
  )
  {
    pAbortOnGui = abort;
  }
  
  public void
  setCommandLineParam
  (
    String builder, 
    LinkedList<String> keys, 
    String value
  )
  {
    if (builder == null)
      throw new IllegalArgumentException
        ("Illegal attempt in setting a Parameter value before specifying the Builder " +
         "that the Parameter resides in.");
    builder = builder.replaceAll("-", " - ");
    LogMgr.getInstance().log(Kind.Arg, Level.Finest, 
      "Reading command line arg for Builder (" + builder + ").\n" +
      "Keys are (" + keys + ").\n" +
      "Value is (" + value + ").");
    LinkedList<String> list;
    if (keys == null)
      list = new LinkedList<String>();
    else
      list = new LinkedList<String>(keys);
    list.addFirst(builder);
    pCommandLineParams.putValue(list, value, true);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pGui;
  private boolean pAbortOnGui;
  private MultiMap<String, String> pCommandLineParams; 
  private String pBuilderClassName;

}

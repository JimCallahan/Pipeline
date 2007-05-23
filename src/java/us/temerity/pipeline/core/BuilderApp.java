package us.temerity.pipeline.core;

import java.io.StringReader;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.stages.BaseStage;

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
  }

  
  public void help()
  {
    LogMgr.getInstance().log
    (LogMgr.Kind.Ops, LogMgr.Level.Info,
     "USAGE:\n" +
     "  builderName [options]\n" + 
     "\n" + 
     "  builderName --help\n" +
     "  builderName --html-help\n" +
     "  builderName --version\n" + 
     "  builderName --release-date\n" + 
     "  builderName --copyright\n" + 
     "  builderName --license\n" + 
     "\n" + 
     "GLOBAL OPTIONS:\n" +
     " [--log=...][--abort][--gui][--builder=...][--<paramName>=<paramValue>...]\n" +
     "\n" + 
     "\n" +  
     "Use \"builderName --html-help\" to browse the full documentation.\n");
  }

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
    
    try {
      PluginMgrClient.init();
    }
    catch (PipelineException ex)
    {
      LogMgr.getInstance().log(Kind.Ops, Level.Severe, 
	"Unable to Initialize the plugin Manager.  Builder Aborting.\n" + ex.getMessage());
      return;
    }
    
    BaseStage.initializeAddedNodes();

    
    try {
      BuilderOptsParser parser = 
	new BuilderOptsParser(new StringReader(pPackedArgs));
      parser.setApp(this);
      if(hasClassName) {
	ClassLoader loader = ClassLoader.getSystemClassLoader();
	Class cls = loader.loadClass(args[0]);
	parser.CommandLine();
	BaseBuilder builder = (BaseBuilder) cls.newInstance();
	builder.run();
      }else 
	parser.CommandLine();
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
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
      	ex.printStackTrace();
    }
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
}

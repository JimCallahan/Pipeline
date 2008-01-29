// $Id: BuilderApp.java,v 1.23 2008/01/29 09:13:19 jesse Exp $

package us.temerity.pipeline.core;

import java.io.StringReader;
import java.util.LinkedList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.Kind;
import us.temerity.pipeline.LogMgr.Level;
import us.temerity.pipeline.builder.BaseBuilderCollection;
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
    pNullParams = new ListMap<LinkedList<String>, String>();
    pGui = true;
    
    pCollectionName = null;
    pCollectionVendor = null;
    pCollectionVersion = null;
    pBuilderName = null;
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
  @Override
  @SuppressWarnings("unchecked")
  public void 
  run
  (
    String[] args
  ) 
  {
    packageArguments(args);
    
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
    
    LogMgr.getInstance().log(Kind.Ops, Level.Info, "Starting Builder Execution!");
    
    try {
      BuilderOptsParser parser = 
	new BuilderOptsParser(new StringReader(pPackedArgs));
      parser.setApp(this);
      
      parser.CommandLine();

      for (LinkedList<String> keys : pNullParams.keySet()) {
        String value = pNullParams.get(keys);
        keys.addFirst(pBuilderName);
        pCommandLineParams.putValue(keys, value, true);
      }
      
      BuilderInformation info = 
        new BuilderInformation(pGui, pAbortOnBadParam, pCommandLineParams);
      
      BaseBuilderCollection collection = 
        PluginMgrClient.getInstance().newBuilderCollection
          (pCollectionName, pCollectionVersion, pCollectionVendor);

      collection.instantiateBuilder(pBuilderName, mclient, qclient, info);
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
      	ex.printStackTrace();
    }
    finally {
      if (!pGui)
	LogMgr.getInstance().cleanup();
    }
    if (!pGui)
      System.exit(success ? 0 : 1);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  @Override
  public 
  void help()
  {
    String wrapper = "builder"; 

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
     "INVOCATION: \n" +
     "--collection=CollectionName(:VersionID)(,VendorName)\n" +
     "--builder-name=BuilderName\n" + 
     "GLOBAL OPTIONS:\n" +
     "  [--log=...] \n" + 
     "  [--abort] [--batch] \n" + 
     "  [--builder=...] [--<ParamName>=<ParamValue>...] ...\n" +
     "\n" + 
     "\n" +  
     "Use \"pl" + wrapper + " --html-help\" to browse the full documentation.\n");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generate an explanatory message for the non-literal token.
   */ 
  @Override
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
      
    case BuilderOptsParserConstants.COLLECTION_NAME:
      return "The name of the Builder Collection plugin to run.";
      
    case BuilderOptsParserConstants.REVISION_NUMBER:
      return "The version numner of the Builder Collection plugin to run.";
      
    case BuilderOptsParserConstants.VENDOR_NAME:
      return "The vendor of the Builder Collection plugin to run.";
      
    case BuilderOptsParserConstants.BUILDERNAME_NAME:
      return "The name of the Builder in the Builder Collection plugin to run.";

    case BuilderOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case BuilderOptsParserConstants.PARAM_NAME:
      return "the name of a parameter";
      
    case BuilderOptsParserConstants.PARAM_VALUE:
      return "the value of a parameter";
      
    case BuilderOptsParserConstants.BUILDER_NAME:
      return "the name of a builder currently having parameters assigned to it";
      
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
    pAbortOnBadParam = abort;
  }
  
  public void
  setCommandLineParam
  (
    String builder, 
    LinkedList<String> keys, 
    String value
  )
  {
    if (builder == null) {
      pNullParams.put(keys, value);
      LogMgr.getInstance().log(Kind.Arg, Level.Finest, 
	"Reading command line arg for Parent Builder.\n" +
	"Keys are (" + keys + ").\n" +
	"Value is (" + value + ").");
      return;
    }
    builder = builder.replaceAll("-", " - ");
    LogMgr.getInstance().log(Kind.Arg, Level.Finest, 
      "Reading command line arg for Builder (" + builder + ").\n" +
      "Keys are (" + keys + ").\n" +
      "Value is (" + value + ").");
    LinkedList<String> list;
      list = new LinkedList<String>(keys);
    list.addFirst(builder);
    pCommandLineParams.putValue(list, value, true);
  }

  /**
   * @param collectionName the collectionName to set
   */
  public void 
  setCollectionName
  (
    String collectionName
  )
    throws PipelineException
  {
    if (pCollectionName != null)
      throw new PipelineException
        ("It is not allowed to set the Collection Name parameter more than once.");
    pCollectionName = collectionName;
  }
  
  /**
   * @param collectionVersion the collectionVersion to set
   */
  public void 
  setCollectionVersion
  (
    VersionID collectionVersion
  )
    throws PipelineException
  {
    if (pCollectionVersion != null)
      throw new PipelineException
        ("It is not allowed to set the Collection Version parameter more than once.");

    pCollectionVersion = collectionVersion;
  }

  /**
   * @param collectionVendor the collectionVendor to set
   */
  public void 
  setCollectionVendor
  (
    String collectionVendor
  )
    throws PipelineException
  {
    if (pCollectionVendor != null)
      throw new PipelineException
        ("It is not allowed to set the Collection Vendor parameter more than once.");
    pCollectionVendor = collectionVendor;
  }
  
  /**
   * @param builderName the builderName to set
   */
  public void 
  setBuilderName
  (
    String builderName
  )
    throws PipelineException
  {
    if (pBuilderName != null)
      throw new PipelineException
        ("It is not allowed to set the Builder Name parameter more than once.");
    pBuilderName = builderName;
  } 
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pGui;
  private boolean pAbortOnBadParam;
  private MultiMap<String, String> pCommandLineParams; 
  private String pCollectionName;
  private VersionID pCollectionVersion;
  private String pCollectionVendor;
  private String pBuilderName;
  private ListMap<LinkedList<String>, String> pNullParams;
  

}

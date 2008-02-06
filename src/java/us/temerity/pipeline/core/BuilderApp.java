// $Id: BuilderApp.java,v 1.25 2008/02/06 07:53:23 jesse Exp $

package us.temerity.pipeline.core;

import java.io.StringReader;
import java.util.*;

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
        new BuilderInformation(pGui, pAbortOnBadParam, true, pCommandLineParams);
      
      if (pBuilderName != null && pCollectionName != null) {
        BaseBuilderCollection collection = 
          PluginMgrClient.getInstance().newBuilderCollection
          (pCollectionName, pCollectionVersion, pCollectionVendor);

        collection.instantiateBuilder(pBuilderName, mclient, qclient, info);
      } 
      else if (pBuilderName != null) {
        DoubleMap<String, String, TreeSet<VersionID>> toPrint = 
          new DoubleMap<String, String, TreeSet<VersionID>>();
        
        TripleMap<String, String, VersionID, LayoutGroup> layouts = 
          PluginMgrClient.getInstance().getBuilderCollectionLayouts();
        for (String vendor : layouts.keySet()) {
          for (String name : layouts.keySet(vendor)) {
            TreeSet<VersionID> versions = new TreeSet<VersionID>();
            for (VersionID id : layouts.keySet(vendor, name)) {
              LayoutGroup group = layouts.get(vendor, name, id);
              TreeSet<String> names = new TreeSet<String>(); 
              collectLayoutNames(group, names);
              if (names.contains(pBuilderName))
                 versions.add(id);
            }
            if (!versions.isEmpty())
              toPrint.put(vendor, name, versions);
          }
        }
        if (toPrint.isEmpty())
          LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
            "There are no Builder Collections containing a builder named " +
            "(" + pBuilderName + ")");
        else {
          LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
            "There are builders  named (" + pBuilderName + ") contained in the " +
            "following Builder Collections");
          for (String vendor : toPrint.keySet()) {
            for (String name : toPrint.keySet(vendor)) {
              for (VersionID id : toPrint.get(vendor, name)) {
                LogMgr.getInstance().log
                (LogMgr.Kind.Ops, LogMgr.Level.Info,
                  name + ":" + id.toString() + "," + vendor);
              }
            }
          }
        }
      }
      else
      {
        DoubleMap<String, String, TreeSet<VersionID>> toPrint = getPrintList();
        
        TripleMap<String, String, VersionID, LayoutGroup> layouts = 
          PluginMgrClient.getInstance().getBuilderCollectionLayouts();
        
        if (toPrint.isEmpty())
          LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
            "There are no Builder Collections which match the provided criteria.\n" +
            "Name: " + makeNameThingie(pCollectionName) + "\n" +
            "Vendor: " + makeNameThingie(pCollectionVendor) + "\n" +
            "Version: " + makeNameThingie2(pCollectionVersion));
        else {
          for (String vendor : toPrint.keySet()) {
            LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
              "Vendor: " + vendor);
            for (String name : toPrint.keySet(vendor)) {
              for (VersionID id : toPrint.get(vendor, name)) {
                LogMgr.getInstance().log
                (LogMgr.Kind.Ops, LogMgr.Level.Info,
                  "  Collection: " + name + " (" + id.toString() + ")");
                LayoutGroup group = layouts.get(vendor, name, id);
                TreeSet<String> names = new TreeSet<String>(); 
                collectLayoutNames(group, names);
                for (String bname : names) {
                  LogMgr.getInstance().log
                  (LogMgr.Kind.Ops, LogMgr.Level.Info,
                   "    Builder: " + bname);
                }
              }
            }
            LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "");
          }
        }
      }
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
  
  private String
  makeNameThingie
  (
    String name  
  )
  {
    if (name == null)
      return "[ALL]";
    return name;
  }
  
  private String
  makeNameThingie2
  (
    VersionID id
  )
  {
    if (id == null)
      return "[ALL]";
    return id.toString();
  }
  
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
  
  private DoubleMap<String, String, TreeSet<VersionID>>
  getPrintList()
  {
    TripleMap<String, String, VersionID, TreeSet<OsType>> coll = 
      PluginMgrClient.getInstance().getBuilderCollections();
    
    DoubleMap<String, String, TreeSet<VersionID>> toPrint = 
      new DoubleMap<String, String, TreeSet<VersionID>>();
    
    Set<String> vendors = new TreeSet<String>();
    
    if (pCollectionVendor == null )
      vendors = coll.keySet();
    else 
      vendors.add(pCollectionVendor);
    
    for (String vendor : vendors) {
      
      Set<String> collnames = new TreeSet<String>();
      Set<String> vendorSet = coll.keySet(vendor); 
      if (pCollectionName == null) {
        if (vendorSet != null )
          collnames = coll.keySet(vendor);
      }
      else 
        if (vendorSet != null && vendorSet.contains(pCollectionName) )
          collnames.add(pCollectionName);
      
      for (String collname : collnames) {
        
        TreeSet<VersionID> versions = new TreeSet<VersionID>();
        Set<VersionID> collVers =  coll.keySet(vendor, collname);
        if (pCollectionVersion == null) {
          if (collVers != null ) 
            versions = new TreeSet<VersionID>(collVers);
        }
        else
          if (collVers != null && collVers.contains(pCollectionVersion))
            versions.add(pCollectionVersion);
        
        if (!versions.isEmpty())
         toPrint.put(vendor, collname, versions);
      }
    }
    return toPrint;
  }
  
  /**
   * Recursively search the parameter groups to collect the builder names.
   */ 
  private final void 
  collectLayoutNames
  (
   LayoutGroup group, 
   TreeSet<String> names
  ) 
  {
    for(String name : group.getEntries()) 
        names.add(name);

    for(LayoutGroup sgroup : group.getSubGroups()) 
      collectLayoutNames(sgroup, names);
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

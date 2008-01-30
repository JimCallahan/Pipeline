package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;
import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;

public class BuildLairTurnTables extends BootApp
{
  MasterMgrClient client;
  PluginMgrClient plug;

  public BuildLairTurnTables()
  {
    try
    {
      PluginMgrClient.init();
      client = new MasterMgrClient();
      plug = PluginMgrClient.getInstance();
    } catch ( PipelineException ex )
    {
      ex.printStackTrace();
    }
  }

  // @SuppressWarnings("unused")
  public void run(String args[])
  {
    try
    {
      TreeMap<String, LinkedList<String>> parsedArgs = argParser(args);
      if ( !checkArgsAndSetParams(parsedArgs) )
      {
	return;
      }
    } catch ( PipelineException ex )
    {
      System.err.println("There was a problem reading the arguments.\n" + ex.getMessage());
      printHelp();
      return;
    }
    for (String turntableName : turntables)
    {
      for (String passName : passes)
      {
	for (LairAsset as : assets)
	{
	  LairTurntable tt = new LairTurntable(as, turntableName, passName);

	  Wrapper w;
	  try
	  {
	    w = new Wrapper(user, view, toolset, client);
	  } catch ( PipelineException ex )
	  {
	    System.err.println("Couldn't create the Wrapper.\n" + ex.getMessage());
	    return;
	  }
	  boolean ifAnotherTTPassDoesNotExists = false;
	  try
	  {
	    ifAnotherTTPassDoesNotExists = doesNodeExists(w, tt.turntableScene);
	    logLine("Value of boolean = " + ifAnotherTTPassDoesNotExists);
	  } catch ( PipelineException ex )
	  {
	    System.err.println("There was an error checking for node existance.\n"
		+ ex.getMessage());
	    return;
	  }

	  ArrayList<String> addedNodes = new ArrayList<String>();

	  if ( stage == 1 )
	  {
	    try
	    {
	      logLine("Checking out the texGroup");
	      getLatest(w, as.texGroup, over, froz);
	      logLine("Checking out the final model scene");
	      getNewest(w, as.finalScene, keep, pFroz);
	      logLine("Checking out the shader scene.");
	      getNewest(w, as.shdScene, keep, pFroz);
	      logLine("Checking out the turntable mel");
	      getNewest(w, MEL_importTurn, keep, modi);

	      log(tt.ttShaderDefsMI + ": ");
	      if ( !doesNodeExists(w, tt.ttShaderDefsMI) )
	      {
		logLine("Building");
		NodeMod mod = registerNode(w, tt.ttShaderDefsMI, "mi", editorEmacs());
		addedNodes.add(tt.ttShaderDefsMI);
		client.link(user, view, tt.ttShaderDefsMI, as.shdIncGroup, DEP, LINKALL,
		  null);
		BaseAction act = actionCatFiles();
		act.initSecondarySourceParams(as.shdIncGroup, as.shdIncGroupSecSeq
		  .getFilePattern());
		act.setSecondarySourceParamValue(as.shdIncGroup, as.shdIncGroupSecSeq
		  .getFilePattern(), "Order", 100);
		mod.setAction(act);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttOptionsMI + ": ");
	      if ( !doesNodeExists(w, tt.ttOptionsMI) )
	      { // Options MI
		logLine("Building");
		logLine(tt.baseTurntableOptions);
		getNewest(w, tt.baseTurntableOptions, keep, modi);
		cloneNode(w, tt.ttOptionsMI, tt.baseTurntableOptions);
		addedNodes.add(tt.ttOptionsMI);
	      } else
		logLine("Already Exists");

	      log(tt.turntableScene + ": ");
	      if ( !doesNodeExists(w, tt.turntableScene) )
	      { // Turntable Scene
		logLine("Building");
		logLine(tt.baseTurntableScene);
		getNewest(w, tt.baseTurntableScene, keep, modi);
		NodeMod mod = registerNode(w, tt.turntableScene, "ma", editorEmacs());
		addedNodes.add(tt.turntableScene);
		BaseAction act = actionMayaReference();
		referenceNode(w, tt.turntableScene, as.modScene, act, REF, "mod");
		referenceNode(w, tt.turntableScene, tt.baseTurntableScene, act, DEP, "turn");
		client.link(user, view, tt.turntableScene, MEL_importTurn, DEP, LINKALL,
		  null);
		act.setSingleParamValue("ModelMEL", MEL_importTurn);
		mod.setAction(act);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttLightMI + ": ");
	      if ( !doesNodeExists(w, tt.ttLightMI) )
	      { // Lights MI
		logLine("Building");
		NodeMod mod = registerSequence(w, tt.ttLightMI, 4, "mi", editorEmacs(),
		  range.getStart(), range.getEnd(), range.getBy());
		addedNodes.add(tt.ttLightMI);
		client
		  .link(user, view, tt.ttLightMI, tt.turntableScene, DEP, LINKALL, null);
		BaseAction act = actionMayaMiExport();
		SortedMap<String, Comparable> preset = act.getPresetValues("EntityPresets",
		  preset_MRLIGHTS);
		setPresets(act, preset);
		act.setSingleParamValue("MayaScene", tt.turntableScene);
		act.setSingleParamValue("ExportSet", "LIGHT");
		mod.setAction(act);
		JobReqs req = mod.getJobRequirements();
		req.addSelectionKey("MentalRay");
		mod.setJobRequirements(req);
		mod.setExecutionMethod(ExecutionMethod.Parallel);
		mod.setBatchSize(100);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttCamMI + ": ");
	      if ( !doesNodeExists(w, tt.ttCamMI) )
	      { // Camera MIs
		logLine("Building");
		NodeMod mod = registerSequence(w, tt.ttCamMI, 4, "mi", editorEmacs(), range
		  .getStart(), range.getEnd(), range.getBy());
		addedNodes.add(tt.ttCamMI);
		client.link(user, view, tt.ttCamMI, tt.turntableScene, DEP, LINKALL, null);
		BaseAction act = actionMayaMiExport();
		SortedMap<String, Comparable> preset = act.getPresetValues("EntityPresets",
		  preset_CAMERAS);
		setPresets(act, preset);
		act.setSingleParamValue("MayaScene", tt.turntableScene);
		act.setSingleParamValue("ExportSet", "CAMERA");
		mod.setAction(act);
		JobReqs req = mod.getJobRequirements();
		req.addSelectionKey("MentalRay");
		mod.setJobRequirements(req);
		mod.setExecutionMethod(ExecutionMethod.Parallel);
		mod.setBatchSize(100);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttCamOverMI + ": ");
	      if ( !doesNodeExists(w, tt.ttCamOverMI) )
	      { // Camera Override MI
		logLine("Building");
		NodeMod mod = registerNode(w, tt.ttCamOverMI, "mi", editorEmacs());
		addedNodes.add(tt.ttCamOverMI);
		BaseAction act = actionMRayCamOverride();
		act.setSingleParamValue("ImageWidth", 1280);
		act.setSingleParamValue("ImageHeight", 720);
		act.setSingleParamValue("AspectRatio", 1.777);
		act.setSingleParamValue("Aperture", 1.41732);
		act.setSingleParamValue("OverrideFocal", false);
		act.setSingleParamValue("OverrideClipping", false);
		mod.setAction(act);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttGeoMI + ": ");
	      if ( !doesNodeExists(w, tt.ttGeoMI) )
	      { // Geometry MIs
		logLine("Building");
		NodeMod mod = registerNode(w, tt.ttGeoMI, "mi", editorEmacs());
		addedNodes.add(tt.ttGeoMI);
		client.link(user, view, tt.ttGeoMI, as.finalScene, DEP, LINKALL, null);
		BaseAction act = actionMayaMiExport();
		SortedMap<String, Comparable> preset = act.getPresetValues("EntityPresets",
		  preset_GEOALL);
		setPresets(act, preset);
		act.setSingleParamValue("MayaScene", as.finalScene);
		act.setSingleParamValue("ExportSet", "GEOMETRY");
		mod.setAction(act);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttShadeMI + ": ");
	      if ( !doesNodeExists(w, tt.ttShadeMI) )
	      { // Shader MIs
		logLine("Building");
		NodeMod mod = registerNode(w, tt.ttShadeMI, "mi", editorEmacs());
		addedNodes.add(tt.ttShadeMI);
		client.link(user, view, tt.ttShadeMI, as.shdScene, DEP, LINKALL, null);
		client
		  .link(user, view, tt.ttShadeMI, tt.ttShaderDefsMI, DEP, LINKALL, null);
		BaseAction act = actionMayaMiShader();
		act.setSingleParamValue("MayaScene", as.shdScene);
		act.setSingleParamValue("MaterialNamespace", "final");
		mod.setAction(act);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");

	      log(tt.ttImages + ": ");
	      if ( !doesNodeExists(w, tt.ttImages) )
	      { // Images
		logLine("Building");
		NodeMod mod = registerSequence(w, tt.ttImages, 4, "iff", editorFCheck(),
		  range.getStart(), range.getEnd(), range.getBy());
		addedNodes.add(tt.ttImages);
		client.link(user, view, tt.ttImages, tt.ttShadeMI, DEP, LINKALL, null);
		client.link(user, view, tt.ttImages, tt.ttOptionsMI, DEP, LINKALL, null);
		client.link(user, view, tt.ttImages, tt.ttLightMI, DEP, LINKONE, 0);
		client.link(user, view, tt.ttImages, tt.ttCamMI, DEP, LINKONE, 0);
		client.link(user, view, tt.ttImages, tt.ttGeoMI, DEP, LINKALL, null);
		client.link(user, view, tt.ttImages, tt.ttCamOverMI, DEP, LINKALL, null);
		client.link(user, view, tt.ttImages, as.shdIncGroup, DEP, LINKALL, null);
		BaseAction act = actionMRayRender();
		act.initSourceParams(tt.ttShadeMI);
		act.initSourceParams(tt.ttOptionsMI);
		act.initSourceParams(tt.ttLightMI);
		act.initSourceParams(tt.ttCamMI);
		act.initSourceParams(tt.ttGeoMI);
		act.initSourceParams(as.shdIncGroup);
		act.setSourceParamValue(as.shdIncGroup, "Order", 100);
		act.setSourceParamValue(tt.ttOptionsMI, "Order", 200);
		act.setSourceParamValue(tt.ttShadeMI, "Order", 300);
		act.setSourceParamValue(tt.ttGeoMI, "Order", 400);
		act.setSourceParamValue(tt.ttLightMI, "Order", 500);
		act.setSourceParamValue(tt.ttCamMI, "Order", 600);
		act.setSingleParamValue("TexturePath", "$WORKING");
		mod.setAction(act);
		JobReqs req = mod.getJobRequirements();
		req.addSelectionKey("MentalRay");
		mod.setJobRequirements(req);
		mod.setExecutionMethod(ExecutionMethod.Parallel);
		mod.setBatchSize(1);
		client.modifyProperties(user, view, mod);
	      } else
		logLine("Already Exists");
	      // client.submitJobs(user, view, tt.ttImages, null);

	    } catch ( Exception ex )
	    {
	      for (String node : addedNodes)
	      {
		try
		{
		  client.release(user, view, node, true);
		} catch ( PipelineException e )
		{
		  e.printStackTrace();
		}
	      }
	      ex.printStackTrace();
	    }
	  } else if ( stage == 2 )
	  {
	    try
	    {
	      disableAction(w, tt.turntableScene);
	    } catch ( PipelineException e )
	    {
	      e.printStackTrace();
	    }
	  } else if ( stage == 3 )
	  {
	    try
	    {
	      NodeID nodeID = new NodeID(user, view, tt.ttImages);
	      client.checkIn(nodeID, "Inital turntable tree with "
		  + "placeholder geometry in model scene, no shaders.",
		VersionID.Level.Minor);
	    } catch ( PipelineException e )
	    {
	      e.printStackTrace();
	    }
	  }
	}
      }
    }
  }

  private boolean checkArgsAndSetParams(TreeMap<String, LinkedList<String>> parsedArgs)
  {
    if ( parsedArgs.containsKey("help") )
    {
      printHelp();
      return false;
    } else if ( parsedArgs.containsKey("version") )
    {
      printVersion();
      return false;
    }

    try
    {
      stage = getIntValue("stage", true, parsedArgs);
      user = getStringValue("user", true, parsedArgs);
      view = getStringValue("view", true, parsedArgs);
      toolset = getStringValue("toolset", true, parsedArgs);
      range = getFrameRange("start", "end", "by", parsedArgs);
      turntables = getStringValues("tt", true, parsedArgs);
      passes = getStringValues("pass", true, parsedArgs);
      assets = getAssetValues("asset", true, parsedArgs);
      {
	Boolean temp = getBooleanValue("verbose", false, parsedArgs);
	if ( temp == null )
	  verbose = false;
	else
	  verbose = temp;
      }
    } catch ( PipelineException ex )
    {
      System.err.println(ex.getMessage());
      printHelp();
      return false;
    }

    if ( parsedArgs.size() > 0 )
    {
      printExtraArgs(parsedArgs);
      return false;
    }
    return true;
  }

  private static void printVersion()
  {
    System.err.println("Lair Turntable Creator version 1.0");
  }

  private static void printHelp()
  {
    System.err.println("Use:  ./buildLairTurnTables <flags>");
    System.err.println("Flag List:");
    System.err.println("\t--verbose=<boolean>");
    System.err.println("\t\tprint out lots of info.");
    System.err.println("\t\tIf you do not use this flag, it will default to false.");
    System.err.println("\t--stage=<value>");
    System.err.println("\t\twhat commands to run.");
    System.err.println("\t\t\tStage: 1 = build tree.");
    System.err.println("\t\t\tStage: 2 = disable actions.");
    System.err.println("\t\t\tStage: 3 = check-in.");
    System.err.println("\t--user=<value>");
    System.err.println("\t\tThe user whose working area "
	+ "you want to build the tree in.");
    System.err.println("\t--view=<value>");
    System.err.println("\t\tThe working area you want to build the tree in.");
    System.err.println("\t--toolset=<value>");
    System.err.println("\t\tThe toolset to use for all the built nodes.");
    System.err.println("\t--start=<value>");
    System.err.println("\t\tThe start frame for the turntable.");
    System.err.println("\t--end=<value>");
    System.err.println("\t\tThe end frame for the turntable.");
    System.err.println("\t--by=<value>");
    System.err.println("\t\tThe frame increment for the turntable.");
    System.err.println("\t--pass=<value>");
    System.err.println("\t\tWhich render pass should the turntable be for.");
    System.err.println("\t\t\tUse a separate flag for each pass.");
    System.err.println("\t--tt=<value>");
    System.err.println("\t\tWhich turntable setups should be generated.");
    System.err.println("\t\t\tUse a separate flag for each setup.");
    System.err.println("\t--asset=<value,value>");
    System.err.println("\t\tAn asset in the shot, followed by the type of the asset.");
    System.err.println("\t\t\tUse a separate flag for each asset.");
    System.err.println("\t\tValid asset types:");
    System.err.println("\t\t\tcharacter");
    System.err.println("\t\t\tprop");
    System.err.println("\t\t\tset");
  }

  private void log(String s)
  {
    if ( verbose )
      System.err.print(s);
  }

  private void logLine(String s)
  {
    if ( verbose )
      System.err.println(s);
  }

  public static final LinkPolicy REF = LinkPolicy.Reference;
  public static final LinkRelationship LINKALL = LinkRelationship.All;
  public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
  public static final LinkPolicy DEP = LinkPolicy.Dependency;
  public static final CheckOutMode over = CheckOutMode.OverwriteAll;
  public static final CheckOutMode keep = CheckOutMode.KeepModified;
  public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
  public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
  public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
  public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

  // From MayaMiExportAction
  // private static final String preset_MRSHADE = "Mental Ray Shaders and
  // Material";
  // private static final String preset_GEOINST = "Geometry Instances";
  // private static final String preset_GEODEC = "Geometry Definition (Stub
  // Materials/No Instances)";
  private static final String preset_GEOALL = "Geometry (Including Instances/Stub Materials)";
  // private static final String preset_OPTIONS = "Options (Render Globals)";
  private static final String preset_CAMERAS = "Camera Declarations and Instances";
  private static final String preset_MRLIGHTS = "Mental Ray Lights";

  private int stage;
  private String user;
  private String view;
  private String toolset;
  private ArrayList<String> passes;
  private ArrayList<String> turntables;
  private ArrayList<LairAsset> assets;
  private FrameRange range;
  private boolean verbose;

}

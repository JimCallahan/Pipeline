package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;
import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;

public class BuildLairRenderPasses extends BootApp
{

   MasterMgrClient client;
   PluginMgrClient plug;

   public BuildLairRenderPasses()
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

   @SuppressWarnings("unused")
   public void run(String[] args)
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
	 System.err.println("There was a problem reading the arguments.\n"
	       + ex.getMessage());
	 printHelp();
	 return;
      }

      try
      {
	 Wrapper w = new Wrapper(user, view, toolset, client);
	 LairShot sh = LairShot.getShot(w, movie, seq, shot, passes, assetGroupings);
	 int start = 1;
	 int end = sh.length;
	 int by = 1;

	 if ( stage == 1 )
	 {
	    logLine("Starting stage 1");

	    ArrayList<String> addedNodes = new ArrayList<String>();
	    try
	    {
	       String lightsName = null;
	       {
		  TreeMap<String, String> lightList = LairConstants.getLairLightsList(w);
		  lightsName = lightList.get(lights);
		  if ( lightsName == null )
		     throw new PipelineException("The value (" + lights
			   + ") does not represent a valid Lair lightRig.\n"
			   + "Use the --list=lights option to get a list of "
			   + "valid cameras");
		  getNewest(w, lightsName, keep, pFroz);
	       }

	       getNewest(w, sh.preLightScene, over, pFroz);

	       for (LairAsset as : sh.assets)
	       {
		  log(as.shdExport + " : ");
		  if ( !doesNodeExists(w, as.shdExport) )
		  {
		     logLine("Creating");
		     getNewest(w, as.texGroup, over, froz);
		     getNewest(w, as.shdScene, keep, modi);
		     NodeMod mod = registerNode(w, as.shdExport, "ma", editorMaya());
		     addedNodes.add(as.shdExport);
		     BaseAction act = actionMayaShaderExport();
		     client.link(user, view, as.shdExport, as.shdScene, DEP, LINKALL, null);
		     act.setSingleParamValue("SelectionPrefix", as.assetName);
		     act.setSingleParamValue("MayaScene", as.shdScene);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		  {
		     logLine("Checking Out");
		     getNewest(w, as.texGroup, over, froz);
		     getNewest(w, as.shdExport, keep, modi);
		  }
	       }

	       log(sh.lightScene + " : ");
	       if ( !doesNodeExists(w, sh.lightScene) )
	       { // lighting scene
		  logLine("Building");
		  NodeMod mod = registerNode(w, sh.lightScene, "ma", editorMaya());
		  addedNodes.add(sh.lightScene);
		  client.link(user, view, sh.lightScene, sh.preLightScene, DEP, LINKALL,
		     null);
		  client.link(user, view, sh.lightScene, lightsName, DEP, LINKALL, null);
		  BaseAction act = actionMayaReference();
		  act.initSourceParams(sh.preLightScene);
		  act.initSourceParams(lightsName);
		  act.setSourceParamValue(sh.preLightScene, "PrefixName", "prelight");
		  act.setSourceParamValue(lightsName, "PrefixName", "lights");
		  for (LairAsset as : sh.assets)
		  {
		     referenceNode(w, sh.lightScene, as.shdExport, act, REF, as.assetName);
		  }
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
	       } else
		  logLine("Already exists");

	       getNewest(w, sh.cameraMI, keep, pFroz);

	       for (String pass : passes)
	       {

		  logLine("Doing Pass: " + pass);
		  String lightMI = sh.passLightMI.get(pass);
		  String optionMI = sh.passOptionMI.get(pass);
		  String camOverMI = sh.passCamOverMI.get(pass);

		  log(lightMI + " : ");
		  // Light MIs per pass
		  if ( !doesNodeExists(w, lightMI) )
		  {
		     logLine("Building");
		     NodeMod mod = registerSequence(w, lightMI, 4, "mi", editorEmacs(),
			start, end, by);
		     addedNodes.add(lightMI);
		     client.link(user, view, lightMI, sh.lightScene, DEP, LINKALL, null);
		     BaseAction act = actionMayaMiExport();
		     SortedMap<String, Comparable> preset = act.getPresetValues(
			"EntityPresets", preset_MRLIGHTS);
		     setPresets(act, preset);
		     act.setSingleParamValue("MayaScene", sh.lightScene);
		     act.setSingleParamValue("ExportSet", "lights:LIGHT");
		     mod.setAction(act);
		     JobReqs req = mod.getJobRequirements();
		     req.addSelectionKey("MentalRay");
		     mod.setJobRequirements(req);
		     mod.setExecutionMethod(ExecutionMethod.Parallel);
		     mod.setBatchSize(20);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already exists");

		  log(optionMI + " : ");
		  // Option MI per pass
		  if ( !doesNodeExists(w, optionMI) )
		  {
		     getNewest(w, sh.passBaseOptionMI.get(pass), keep, pFroz);
		     logLine("Building");
		     cloneNode(w, optionMI, sh.passBaseOptionMI.get(pass));
		     addedNodes.add(optionMI);
		  } else
		     logLine("Already exists");

		  log(camOverMI + " : ");
		  // Camera Override MI per pass
		  if ( !doesNodeExists(w, camOverMI) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, camOverMI, "mi", editorEmacs());
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
		     logLine("Already exists");

		  // The Per Asset Per Pass fields
		  for (LairAsset as : sh.assets)
		  {
		     String asset = as.assetName;
		     logLine("\tDoing Asset:" + asset);
		     String shaderDefMi = sh.assetPassShaderDefsMI.get(asset, pass);
		     String shaderMI = sh.assetPassShadeMI.get(asset, pass);
		     String images = sh.assetPassImages.get(asset, pass);

		     String geoMI = sh.assetGeoInstMI.get(asset);
		     getNewest(w, geoMI, over, froz);

		     doShaderDefMI(w, addedNodes, as, shaderDefMi);

		     doShaderMI(w, sh, addedNodes, asset, shaderDefMi, shaderMI);

		     if ( perAsset )
		     {
			log("\t" + images + " : ");
			// Per asset Per Pass images
			if ( !doesNodeExists(w, images) )
			{
			   logLine("Building");

			   NodeMod mod = registerSequence(w, images, 4, "iff",
			      editorFCheck(), start, end, by);
			   addedNodes.add(images);
			   BaseAction act = actionMRayRender();

			   LinkedList<LairAsset> assets = new LinkedList<LairAsset>();
			   assets.add(as);

			   buildMultipleAssetImageNode(w, sh, addedNodes, pass, lightMI,
			      optionMI, camOverMI, images, act, assets);

			   mod.setAction(act);
			   JobReqs req = mod.getJobRequirements();
			   req.addSelectionKey("MentalRay");
			   mod.setJobRequirements(req);
			   mod.setExecutionMethod(ExecutionMethod.Parallel);
			   mod.setBatchSize(10);
			   client.modifyProperties(user, view, mod);
			} else
			   logLine("Already exists");
		     }
		  }

		  if ( assetGroupings != null )
		  { // Asset Grouping Passes
		     for (String groupName : assetGroupings.keySet())
		     {
			logLine("Grouping: " + groupName);
			String images = sh.assetPassImages.get(groupName, pass);
			log(images + " : ");
			if ( !doesNodeExists(w, images) )
			{
			   logLine("Building");
			   NodeMod mod = registerSequence(w, images, 4, "iff",
			      editorFCheck(), start, end, by);
			   addedNodes.add(images);
			   BaseAction act = actionMRayRender();
			   LinkedList<LairAsset> assets = assetGroupings.get(groupName);

			   buildMultipleAssetImageNode(w, sh, addedNodes, pass, lightMI,
			      optionMI, camOverMI, images, act, assets);
			   mod.setAction(act);
			   JobReqs req = mod.getJobRequirements();
			   req.addSelectionKey("MentalRay");
			   mod.setJobRequirements(req);
			   mod.setExecutionMethod(ExecutionMethod.Parallel);
			   mod.setBatchSize(10);
			   client.modifyProperties(user, view, mod);
			} else
			   logLine("Already exists");
		     }
		  }

		  if ( global )
		  { // Global image node
		     String images = sh.passImages.get(pass);
		     log(images + " : ");
		     if ( !doesNodeExists(w, images) )
		     {
			logLine("Building");
			NodeMod mod = registerSequence(w, images, 4, "iff", editorFCheck(),
			   start, end, by);
			addedNodes.add(images);
			BaseAction act = actionMRayRender();
			LinkedList<LairAsset> assets = new LinkedList<LairAsset>(sh.assets);

			buildMultipleAssetImageNode(w, sh, addedNodes, pass, lightMI,
			   optionMI, camOverMI, images, act, assets);
			mod.setAction(act);
			JobReqs req = mod.getJobRequirements();
			req.addSelectionKey("MentalRay");
			mod.setJobRequirements(req);
			mod.setExecutionMethod(ExecutionMethod.Parallel);
			mod.setBatchSize(10);
			client.modifyProperties(user, view, mod);
		     } else
			logLine("Already exists");
		  }
	       }
	    } catch ( Exception ex )
	    {
	       for (String s : addedNodes)
	       {
		  client.release(user, view, s, true);
	       }
	       ex.printStackTrace();
	    }
	 } else if ( stage == 2 )
	 {
	    disableAction(w, sh.lightScene);
	 } else if ( stage == 3 )
	 {
	    if ( global )
	    {
	       NodeID nodeID = new NodeID(user, view, sh.geoTopGroup);
	       client.checkIn(nodeID, "Render Pass tree built with "
		     + "the BuildLairRenderPass tool", VersionID.Level.Minor);
	    }
	    if ( perAsset )
	    {
	       for (String pass : passes)
		  for (LairAsset as : sh.assets)
		  {
		     String images = sh.assetPassImages.get(as.assetName, pass);
		     NodeID nodeID = new NodeID(user, view, images);
		     client.checkIn(nodeID, "Render Pass tree built with "
			   + "the BuildLairRenderPass tool", VersionID.Level.Minor);
		  }
	    }
	    if ( assetGroupings != null )
	    { // Asset Grouping Passes
	       for (String pass : passes)
		  for (String groupName : assetGroupings.keySet())
		  {
		     String images = sh.assetPassImages.get(groupName, pass);
		     NodeID nodeID = new NodeID(user, view, images);
		     client.checkIn(nodeID, "Render Pass tree built with "
			   + "the BuildLairRenderPass tool", VersionID.Level.Minor);
		  }
	    }
	 }
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
      }

   }

   private void buildMultipleAssetImageNode(Wrapper w, LairShot sh,
	 ArrayList<String> addedNodes, String pass, String lightMI, String optionMI,
	 String camOverMI, String images, BaseAction act, LinkedList<LairAsset> assets)
      throws PipelineException
   {
      client.link(user, view, images, optionMI, DEP, LINKALL, null);
      client.link(user, view, images, lightMI, DEP, LINKONE, 0);
      client.link(user, view, images, sh.cameraMI, DEP, LINKONE, 0);

      for (LairAsset as : assets)
      {
	 String asset = as.assetName;
	 String shaderDefMi = sh.assetPassShaderDefsMI.get(asset, pass);
	 String shaderMI = sh.assetPassShadeMI.get(asset, pass);
	 String geoMI = sh.assetGeoInstMI.get(asset);

	 doShaderDefMI(w, addedNodes, as, shaderDefMi);

	 doShaderMI(w, sh, addedNodes, asset, shaderDefMi, shaderMI);

	 client.link(user, view, images, shaderMI, DEP, LINKALL, null);
	 client.link(user, view, images, geoMI, DEP, LINKONE, null);
	 client.link(user, view, images, camOverMI, DEP, LINKALL, null);
	 client.link(user, view, images, as.shdIncGroup, DEP, LINKALL, null);
	 act.initSourceParams(shaderMI);
	 act.initSourceParams(geoMI);
	 act.initSourceParams(as.shdIncGroup);
	 act.setSourceParamValue(as.shdIncGroup, "Order", 100);
	 act.setSourceParamValue(shaderMI, "Order", 300);
	 act.setSourceParamValue(geoMI, "Order", 400);
      }
      act.initSourceParams(optionMI);
      act.initSourceParams(lightMI);
      act.initSourceParams(sh.cameraMI);
      act.setSourceParamValue(optionMI, "Order", 200);
      act.setSourceParamValue(lightMI, "Order", 500);
      act.setSourceParamValue(sh.cameraMI, "Order", 600);
      act.setSingleParamValue("TexturePath", "$WORKING");
   }

   private void doShaderMI(Wrapper w, LairShot sh, ArrayList<String> addedNodes,
	 String asset, String shaderDefMi, String shaderMI) throws PipelineException
   {
      // Shader MIs
      log("\t" + shaderMI + " : ");
      if ( !doesNodeExists(w, shaderMI) )
      {
	 logLine("Building");
	 NodeMod mod = registerNode(w, shaderMI, "mi", editorEmacs());
	 addedNodes.add(shaderMI);
	 client.link(user, view, shaderMI, sh.lightScene, DEP, LINKALL, null);
	 client.link(user, view, shaderMI, shaderDefMi, DEP, LINKALL, null);
	 BaseAction act = actionMayaMiShader();
	 act.setSingleParamValue("MayaScene", sh.lightScene);
	 act.setSingleParamValue("MaterialNamespace", "prelight:" + asset);
	 act.setSingleParamValue("ShaderNamespace", asset);
	 act.setSingleParamValue("FinalNamespace", asset);
	 mod.setAction(act);
	 client.modifyProperties(user, view, mod);
      } else
	 logLine("Already exists");
   }

   private void doShaderDefMI(Wrapper w, ArrayList<String> addedNodes, LairAsset as,
	 String shaderDefMi) throws PipelineException
   {
      // Shader Def MI
      log("\t" + shaderDefMi + " : ");
      if ( !doesNodeExists(w, shaderDefMi) )
      {
	 logLine("Building");
	 NodeMod mod = registerNode(w, shaderDefMi, "mi", editorEmacs());
	 addedNodes.add(shaderDefMi);
	 client.link(user, view, shaderDefMi, as.shdIncGroup, DEP, LINKALL, null);
	 BaseAction act = actionCatFiles();
	 act.initSecondarySourceParams(as.shdIncGroup, as.shdIncGroupSecSeq
	    .getFilePattern());
	 act.setSecondarySourceParamValue(as.shdIncGroup, as.shdIncGroupSecSeq
	    .getFilePattern(), "Order", 100);
	 mod.setAction(act);
	 client.modifyProperties(user, view, mod);
      } else
	 logLine("Already exists");
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
      } else if ( parsedArgs.containsKey("list") )
      {
	 printList(parsedArgs.get("list"));
	 return false;
      }

      try
      {
	 stage = getIntValue("stage", true, parsedArgs);
	 user = getStringValue("user", true, parsedArgs);
	 view = getStringValue("view", true, parsedArgs);
	 toolset = getStringValue("toolset", true, parsedArgs);
	 movie = getStringValue("movie", true, parsedArgs);
	 seq = getStringValue("seq", true, parsedArgs);
	 shot = getStringValue("shot", true, parsedArgs);
	 passes = getStringValues("pass", true, parsedArgs);
	 lights = getStringValue("lights", true, parsedArgs);
	 {
	    Boolean temp = getBooleanValue("global", false, parsedArgs);
	    if ( temp == null )
	       global = false;
	    else
	       global = temp;
	 }
	 {
	    Boolean temp = getBooleanValue("assets", false, parsedArgs);
	    if ( temp == null )
	       perAsset = false;
	    else
	       perAsset = temp;
	 }
	 {
	    Boolean temp = getBooleanValue("verbose", false, parsedArgs);
	    if ( temp == null )
	       verbose = false;
	    else
	       verbose = temp;
	 }
	 assetGroupings = getAssetGroupingValues("group", false, parsedArgs);
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

   private static void printHelp()
   {
      System.err.println("Use:  ./buildLairRenderPasses <flags>");
      System.err.println("Flag List:");
      System.err.println("\t--help");
      System.err.println("\t\tPrint this message.");
      System.err.println("\t--verbose=<boolean>");
      System.err.println("\t\tprint out lots of info.");
      System.err.println("\t\tIf you do not use this flag, it will default to false.");
      System.err.println("\t--list=<value>,<value> . . . ");
      System.err
	 .println("\t\tPrint a list of all the project assets of the included types.");
      System.err.println("\t\tSupports 'camera' and 'lights' currently.");
      System.err.println();

      System.err.println("\t--stage=<value>");
      System.err.println("\t\twhat commands to run.");
      System.err.println("\t\t\tStage: 1 = build tree.");
      System.err.println("\t\t\tStage: 2 = disable action.");
      System.err.println("\t--user=<value>");
      System.err.println("\t\tThe user whose working area "
	    + "you want to build the tree in.");
      System.err.println("\t--view=<value>");
      System.err.println("\t\tThe working area you want to build the tree in.");
      System.err.println("\t--toolset=<value>");
      System.err.println("\t\tThe toolset to use for all the built nodes.");
      System.err.println("\t--movie=<value>");
      System.err.println("\t\tThe Movie the shot is in.");
      System.err.println("\t--seq=<value>");
      System.err.println("\t\tThe Sequence the shot is in.");
      System.err.println("\t--shot=<value>");
      System.err.println("\t\tThe name of the shot.");
      System.err.println("\t--lights=<value>");
      System.err.println("\t\tThe light rig to use.");
      System.err.println("\t--pass=<value>");
      System.err.println("\t\tThe render pass you want to build.");
      System.err.println("\t\t\tUse a separate flag for each pass.");
      System.err.println("\t--global=<boolean>");
      System.err.println("\t\tDo you want a global image node that "
	    + "incorporates all the assets.");
      System.err.println("\t\tIf you do not use this flag, it will default to false.");
      System.err.println("\t--assets=<boolean>");
      System.err.println("\t\tDo you want an image node per asset.");
      System.err.println("\t\tIf you do not use this flag, it will default to false.");
      System.err.println("\t--group=<value>:<assetName,assetType>"
	    + "-<assetName,assetType> . . .");
      System.err.println("\t\tCreate an image sequence for each pass "
	    + "named <value> which contains all the <assets>");
   }

   private static void printVersion()
   {
      System.err.println("Lair Render Pass Creator version 1.0");
   }

   @SuppressWarnings("hiding")
   private void printList(LinkedList<String> name)
   {
      if ( name == null )
	 return;

      String user = "temp";
      String view = "temp";
      String toolset = "temp";
      MasterMgrClient client = new MasterMgrClient();
      Wrapper w = null;
      try
      {
	 w = new Wrapper(user, view, toolset, client);
      } catch ( PipelineException ex )
      {
	 System.err.println("Unable to connect to the pipeline server");
      }
      try
      {
	 String n = name.getFirst();
	 if ( n.contains("camera") )
	 {
	    System.err.println("Camera List: First field is "
		  + "argument for BuildLairAnimation");
	    TreeMap<String, String> list = LairConstants.getLairCameraList(w);
	    for (String each : list.keySet())
	       System.err.println(each + "\t" + list.get(each));
	    System.err.println();
	 }

	 if ( n.contains("lights") )
	 {
	    System.err.println("Light List: First field is "
		  + "argument for BuildLairAnimation");
	    TreeMap<String, String> list = LairConstants.getLairLightsList(w);
	    for (String each : list.keySet())
	       System.err.println(each + "\t" + list.get(each));
	    System.err.println();
	 }

      } catch ( PipelineException ex )
      {
	 System.err.println("Problem printing the list.\n" + ex.getMessage());
      }
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

   private int stage;
   private String user;
   private String view;
   private String toolset;
   private String movie;
   private String seq;
   private String shot;
   private String lights;
   private ArrayList<String> passes;
   private TreeMap<String, LinkedList<LairAsset>> assetGroupings;
   private boolean global;
   private boolean perAsset;
   private boolean verbose;

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
   //private static final String preset_GEOALL = "Geometry (Including Instances/Stub Materials)";
   // private static final String preset_OPTIONS = "Options (Render Globals)";
   private static final String preset_MRLIGHTS = "Mental Ray Lights";

}

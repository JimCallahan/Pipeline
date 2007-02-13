package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.lair.LairConstants.*;
import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;

public class BuildLairAnimation extends BootApp
{

   MasterMgrClient client;
   PluginMgrClient plug;

   public BuildLairAnimation()
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
      // Path file = new Path("//Kronos/csg/Temp/jim/LairAssets.txt");
      // Path mayaDummy = new Path("//Kronos/csg/Temp/jim/dummy.mb");
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
      LairShot sh = new LairShot(movie, seq, shot, length, assets, null, null);

      try
      {
	 Wrapper w = new Wrapper(user, view, toolset, client);

	 if ( stage == 1 )
	 {
	    System.out.println("Starting stage 1");
	    ArrayList<String> addedNodes = new ArrayList<String>();
	    try
	    {
	       String cameraName = null;
	       {
		  TreeMap<String, String> cameras = LairConstants.getLairCameraList(w);
		  cameraName = cameras.get(camera);
		  if ( cameraName == null )
		     throw new PipelineException("The value (" + camera
			   + ") does not represent a valid Lair camera.\n"
			   + "Use the --list=camera option to get a list of "
			   + "valid cameras");
	       }
	       FileSeq cameraSeq = new FileSeq("camera", "anim");

	       { // AnimShot
		  NodeMod mod = registerNode(w, sh.animScene, "ma", editorMaya());
		  addedNodes.add(sh.animScene);
		  BaseAction act = actionMayaReference();
		  for (LairAsset as : assets)
		  {
		     referenceNode(w, sh.animScene, as.lr_finalScene, act, REF,
			as.assetName);
		  }
		  referenceNode(w, sh.animScene, cameraName, act, REF, "camera");
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
	       }
	       { // AnimExport Shot
		  NodeMod mod = registerNode(w, sh.animExportGroup, null, editorKWrite());
		  addedNodes.add(sh.animExportGroup);
		  for (FileSeq animSeq : sh.animExportGroupSecSeqs.values())
		  {
		     mod.addSecondarySequence(animSeq);
		  }
		  mod.addSecondarySequence(cameraSeq);
		  client.link(user, view, sh.animExportGroup, sh.animScene, DEP, LINKALL,
		     null);
		  BaseAction act = actionMayaAnimExport();
		  act.setSingleParamValue("Method", "Simulate");
		  act.setSingleParamValue("ExportSet", "SELECT");
		  act.setSingleParamValue("FirstFrame", 1);
		  act.setSingleParamValue("LastFrame", length);
		  act.setSingleParamValue("MayaScene", sh.animScene);
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
	       }
	       { // Shader Defs
		  NodeMod mod = registerNode(w, sh.lightShaderDefsMI, "mi", editorEmacs());
		  addedNodes.add(sh.lightShaderDefsMI);
		  BaseAction act = actionCatFiles();
		  for (LairAsset as : assets)
		  {
		     client.link(user, view, sh.lightShaderDefsMI, as.shdIncGroup, DEP,
			LINKALL, null);
		     act.initSecondarySourceParams(as.shdIncGroup, as.shdIncGroupSecSeq
			.getFilePattern());
		     act.setSecondarySourceParamValue(as.shdIncGroup, as.shdIncGroupSecSeq
			.getFilePattern(), "Order", 100);
		  }
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
	       }
	       { // prelighting scene
		  NodeMod mod = registerNode(w, sh.preLightScene, "ma", editorMaya());
		  addedNodes.add(sh.preLightScene);
		  client.link(user, view, sh.preLightScene, sh.animExportGroup, DEP,
		     LINKALL, null);
		  client.link(user, view, sh.preLightScene, sh.lightShaderDefsMI, DEP,
		     LINKALL, null);
		  client.link(user, view, sh.preLightScene, cameraName, DEP, LINKALL, null);
		  BaseAction act = actionMayaCollate();
		  act.setSingleParamValue("RootDAGNode", "Reference");
		  act.setSingleParamValue("ImportSet", "SELECT");
		  act.setSingleParamValue("BeginFrame", 1);

		  act.initSourceParams(cameraName);
		  act.setSourceParamValue(cameraName, "Order", null);
		  act.setSourceParamValue(cameraName, "PrefixName", "camera");
		  act.initSecondarySourceParams(sh.animExportGroup, cameraSeq
		     .getFilePattern());
		  act.setSecondarySourceParamValue(sh.animExportGroup, cameraSeq
		     .getFilePattern(), "Order", 100);

		  for (LairAsset as : assets)
		  {
		     client.link(user, view, sh.preLightScene, as.finalScene, DEP, LINKALL,
			null);
		     act.initSourceParams(as.finalScene);
		     act.setSourceParamValue(as.finalScene, "Order", null);
		     act.setSourceParamValue(as.finalScene, "PrefixName", as.assetName);
		  }
		  for (FileSeq animSeq : sh.animExportGroupSecSeqs.values())
		  {
		     act.initSecondarySourceParams(sh.animExportGroup, animSeq
			.getFilePattern());
		     act.setSecondarySourceParamValue(sh.animExportGroup, animSeq
			.getFilePattern(), "Order", 100);
		  }
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
	       }

	       { // asset collates
		  for (LairAsset as : assets)
		  {
		     String collateName = sh.assetCollateScenes.get(as.assetName);
		     NodeMod mod = registerNode(w, collateName, "ma", editorMaya());
		     addedNodes.add(collateName);
		     BaseAction act = actionMayaCollate();
		     act.setSingleParamValue("RootDAGNode", "Reference");
		     act.setSingleParamValue("ImportSet", "SELECT");
		     act.setSingleParamValue("BeginFrame", 1);
		     client.link(user, view, collateName, sh.animExportGroup, DEP, LINKALL,
			null);
		     client
			.link(user, view, collateName, as.finalScene, DEP, LINKALL, null);
		     client.link(user, view, collateName, MEL_crunch, DEP, LINKALL, null);
		     FileSeq animSeq = sh.animExportGroupSecSeqs.get(as.assetName);
		     act.initSecondarySourceParams(sh.animExportGroup, animSeq
			.getFilePattern());
		     act.setSecondarySourceParamValue(sh.animExportGroup, animSeq
			.getFilePattern(), "Order", 100);
		     act.setSingleParamValue("AnimMEL", MEL_crunch);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  }
	       }
	       { // asset geo exports
		  for (LairAsset as : assets)
		  {
		     String miName = sh.assetGeoMI.get(as.assetName);
		     String collateName = sh.assetCollateScenes.get(as.assetName);
		     NodeMod mod = registerSequence(w, miName, 4, "mi", editorEmacs(), 1,
			length, 1);
		     addedNodes.add(miName);
		     client.link(user, view, miName, collateName, DEP, LINKALL, null);
		     BaseAction act = actionMayaMiExport();
		     SortedMap<String, Comparable> preset = act.getPresetValues(
			"EntityPresets", preset_GEOALL);
		     setPresets(act, preset);
		     act.setSingleParamValue("MayaScene", collateName);
		     act.setSingleParamValue("ExportSet", "GEOMETRY");
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  }
	       }
	       { // asset geo inst

		  for (LairAsset as : assets)
		  {
		     String miName = sh.assetGeoInstMI.get(as.assetName);
		     String geoMI = sh.assetGeoMI.get(as.assetName);
		     NodeMod mod = registerSequence(w, miName, 4, "mi", editorEmacs(), 1,
			length, 1);
		     addedNodes.add(miName);
		     client.link(user, view, miName, geoMI, DEP, LINKONE, null);
		     BaseAction act = actionMRayInstGroup();
		     act.setSingleParamValue("GenerateIncludes", true);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  }
	       }
	       {
		  //log(sh.shotCamMI + " : ");
		  if ( !doesNodeExists(w, sh.cameraMI) )
		  { // Camera MIs
		     //logLine("Building");
		     NodeMod mod = registerSequence(w, sh.cameraMI, 4, "mi", editorEmacs(),
			1, length, 1);
		     addedNodes.add(sh.cameraMI);
		     client.link(user, view, sh.cameraMI, sh.animScene, DEP, LINKALL, null);
		     BaseAction act = actionMayaMiExport();
		     SortedMap<String, Comparable> preset = act.getPresetValues(
			"EntityPresets", preset_CAMERAS);
		     setPresets(act, preset);
		     act.setSingleParamValue("MayaScene", sh.animScene);
		     act.setSingleParamValue("ExportSet", "camera:CAMERA");
		     mod.setAction(act);
		     JobReqs req = mod.getJobRequirements();
		     req.addSelectionKey("MentalRay");
		     mod.setJobRequirements(req);
		     mod.setExecutionMethod(ExecutionMethod.Parallel);
		     mod.setBatchSize(20);
		     client.modifyProperties(user, view, mod);
		  } else
		     ;
		  //logLine("Already exists");

	       }

	       { // geo top
		  NodeMod mod = registerNode(w, sh.geoTopGroup, null, editorEmacs());
		  for (LairAsset as : assets)
		  {
		     String miName = sh.assetGeoInstMI.get(as.assetName);
		     client.link(user, view, sh.geoTopGroup, miName, DEP, LINKALL, null);
		  }
		  client.link(user, view, sh.geoTopGroup, sh.preLightScene, DEP, LINKALL,
		     null);
		  client.link(user, view, sh.geoTopGroup, sh.cameraMI, DEP, LINKALL, null);
		  BaseAction act = actionTouch();
		  mod.setAction(act);
		  client.modifyProperties(user, view, mod);
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
	    client.submitJobs(user, view, sh.geoTopGroup, null);
	 } else if ( stage == 3 )
	 {
	    //disableAction(w, sh.lightScene);
	    disableAction(w, sh.animScene);
	 } else if ( stage == 4 )
	 {
	    NodeID nodeID = new NodeID(user, view, sh.geoTopGroup);
	    client.checkIn(nodeID, "Animation tree built with "
		  + "the BuildLairAnimation tool", VersionID.Level.Minor);
	 }
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
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
      } else if ( parsedArgs.containsKey("list") )
      {
	 printList(parsedArgs.get("list"));
	 return false;
      }

      try
      {
	 stage = getIntValue("stage", true, parsedArgs);
	 length = getIntValue("length", true, parsedArgs);
	 user = getStringValue("user", true, parsedArgs);
	 view = getStringValue("view", true, parsedArgs);
	 toolset = getStringValue("toolset", true, parsedArgs);
	 movie = getStringValue("movie", true, parsedArgs);
	 seq = getStringValue("seq", true, parsedArgs);
	 shot = getStringValue("shot", true, parsedArgs);
	 camera = getStringValue("camera", true, parsedArgs);
	 assets = getAssetValues("asset", true, parsedArgs);
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

   private static void printHelp()
   {
      System.err.println("Use:  ./buildLairAnimation <flags>");
      System.err.println("Flag List:");
      System.err.println("\t--help");
      System.err.println("\t\tPrint this message.");
      System.err.println("\t--version");
      System.err.println("\t\tPrint the version number.");
      System.err.println("\t--list=<value>,<value> . . . ");
      System.err
	 .println("\t\tPrint a list of all the project assets of the included types.");
      System.err.println("\t\tSupports 'camera' and 'lights' currently.");
      System.err.println();
      System.err.println("\t--stage=<value>");
      System.err.println("\t\twhat commands to run.");
      System.err.println("\t\t\tStage: 1 = build tree.");
      System.err.println("\t\t\tStage: 2 = queue the tree.");
      System.err.println("\t\t\tStage: 3 = disable actions.");
      System.err.println("\t\t\tStage: 4 = check-in.");
      System.err.println("\t--user=<value>");
      System.err.println("\t\tThe user whose working area you want to build "
	    + "the tree in.");
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
      System.err.println("\t--length=<value>");
      System.err.println("\t\tThe length in frames of the shot.");
      System.err.println("\t--camera=<value>");
      System.err.println("\t\tThe camera rig to use.");
      System.err.println("\t--asset=<value,value>");
      System.err.println("\t\tAn asset in the shot, followed by the type of the asset.");
      System.err.println("\t\tUse a separate flag for each asset.");
      System.err.println("\t\tValid asset types:");
      System.err.println("\t\t\tcharacter");
      System.err.println("\t\t\tprop");
      System.err.println("\t\t\tset");
   }

   private static void printVersion()
   {
      System.err.println("Lair Animation Shot Creator version 1.0");
   }

   private int stage;
   private int length;
   private String user;
   private String view;
   private String toolset;
   private String movie;
   private String seq;
   private String shot;
   private String camera;
   private ArrayList<LairAsset> assets;

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

   private static final String preset_GEOALL = "Geometry (Including Instances/Stub Materials)";
   private static final String preset_CAMERAS = "Camera Declarations and Instances";

}

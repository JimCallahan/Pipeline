package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.lair.LairConstants.*;
import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;

public class BuildLairAssets extends BootApp
{

   MasterMgrClient client;
   PluginMgrClient plug;

   public BuildLairAssets()
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

	 for (LairAsset as : assets)
	 {
	    if ( stage == 1 )
	    {
	       logLine("Starting stage 1");
	       ArrayList<String> addedFiles = new ArrayList<String>();
	       try
	       {
		  log(as.modScene + " : ");
		  if ( !doesNodeExists(w, as.modScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.modScene, "ma", editorMaya());
		     addedFiles.add(as.modScene);
		     BaseAction act = actionMayaReference();
		     client.link(user, view, as.modScene, MEL_charPlaceholder, DEP,
			LINKALL, null);
		     act.setSingleParamValue("ModelMEL", MEL_charPlaceholder);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.rigScene + " : ");
		  if ( !doesNodeExists(w, as.rigScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.rigScene, "ma", editorMaya());
		     addedFiles.add(as.rigScene);
		     BaseAction act = actionMayaImport();
		     referenceNode(w, as.rigScene, as.modScene, act, REF, "mod");
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.matScene + " : ");
		  if ( !doesNodeExists(w, as.matScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.matScene, "ma", editorMaya());
		     addedFiles.add(as.matScene);
		     BaseAction act = actionMayaReference();
		     referenceNode(w, as.matScene, as.rigScene, act, REF, "rig");
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.finalScene + " : ");
		  if ( !doesNodeExists(w, as.finalScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.finalScene, "ma", editorMaya());
		     addedFiles.add(as.finalScene);
		     BaseAction act = actionMayaImport();
		     referenceNode(w, as.finalScene, as.matScene, act, DEP, "mat");
		     client.link(user, view, as.finalScene, MEL_finalizeCharacter, DEP,
			LINKALL, null);
		     act.setSingleParamValue("ModelMEL", MEL_finalizeCharacter);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  // lowrez
		  log(as.lr_modScene + " : ");
		  if ( !doesNodeExists(w, as.lr_modScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.lr_modScene, "ma", editorMaya());
		     addedFiles.add(as.lr_modScene);
		     BaseAction act = actionMayaReference();
		     client.link(user, view, as.lr_modScene, MEL_charPlaceholder, DEP,
			LINKALL, null);
		     act.setSingleParamValue("ModelMEL", MEL_charPlaceholder);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.lr_rigScene + " : ");
		  if ( !doesNodeExists(w, as.lr_rigScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.lr_rigScene, "ma", editorMaya());
		     addedFiles.add(as.lr_rigScene);
		     BaseAction act = actionMayaImport();
		     referenceNode(w, as.lr_rigScene, as.lr_modScene, act, REF, "mod");
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.lr_matScene + " : ");
		  if ( !doesNodeExists(w, as.lr_matScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.lr_matScene, "ma", editorMaya());
		     addedFiles.add(as.lr_matScene);
		     BaseAction act = actionMayaReference();
		     referenceNode(w, as.lr_matScene, as.lr_rigScene, act, REF, "rig");
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.lr_finalScene + " : ");
		  if ( !doesNodeExists(w, as.lr_finalScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.lr_finalScene, "ma", editorMaya());
		     addedFiles.add(as.lr_finalScene);
		     BaseAction act = actionMayaImport();
		     referenceNode(w, as.lr_finalScene, as.lr_matScene, act, DEP, "mat");
		     client.link(user, view, as.lr_finalScene, MEL_finalizeCharacter, DEP,
			LINKALL, null);
		     act.setSingleParamValue("ModelMEL", MEL_finalizeCharacter);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  // Texture Scene
		  log(as.texGroup + " : ");
		  if ( !doesNodeExists(w, as.texGroup) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.texGroup, null, editorKWrite());
		     addedFiles.add(as.texGroup);
		     BaseAction act = actionListSources();
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  // Shader Scenes
		  log(as.shdIncGroup + " : ");
		  if ( !doesNodeExists(w, as.shdIncGroup) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.shdIncGroup, "mi", editorSciTE());
		     addedFiles.add(as.shdIncGroup);
		     mod.addSecondarySequence(as.shdIncGroupSecSeq);
		     BaseAction act = actionMRayShaderInclude();
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.shdScene + " : ");
		  if ( !doesNodeExists(w, as.shdScene) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.shdScene, "ma", editorMaya());
		     addedFiles.add(as.shdScene);
		     BaseAction act = actionMayaReference();
		     referenceNode(w, as.shdScene, as.finalScene, act, REF, "final");
		     client.link(user, view, as.shdScene, as.shdIncGroup, REF, LINKALL,
			null);
		     client.link(user, view, as.shdScene, as.texGroup, REF, LINKALL, null);
		     client.link(user, view, as.shdScene, MEL_loadMRay, DEP, LINKALL, null);
		     act.setSingleParamValue("InitialMEL", MEL_loadMRay);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.shdExport + " : ");
		  if ( !doesNodeExists(w, as.shdExport) )
		  {
		     logLine("Building");
		     NodeMod mod = registerNode(w, as.shdExport, "ma", editorMaya());
		     addedFiles.add(as.shdExport);
		     BaseAction act = actionMayaShaderExport();
		     client.link(user, view, as.shdExport, as.shdScene, DEP, LINKALL, null);
		     act.setSingleParamValue("SelectionPrefix", as.assetName);
		     act.setSingleParamValue("MayaScene", as.shdScene);
		     mod.setAction(act);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  client.submitJobs(user, view, as.shdScene, null);
		  client.submitJobs(user, view, as.lr_finalScene, null);

	       } catch ( PipelineException ex )
	       {
		  for (String s : addedFiles)
		  {
		     try
		     {
			client.release(user, view, s, true);
		     } catch ( PipelineException e )
		     {
			e.printStackTrace();
		     }
		  }
		  ex.printStackTrace();
	       }
	    } else if ( stage == 2 )
	    {
	       {
		  NodeMod mod = client.getWorkingVersion(user, view, as.modScene);
		  mod.setAction(null);
		  client.unlink(user, view, as.modScene, MEL_charPlaceholder);
		  client.modifyProperties(user, view, mod);
	       }

	       {
		  NodeMod mod = client.getWorkingVersion(user, view, as.lr_modScene);
		  mod.setAction(null);
		  client.unlink(user, view, as.lr_modScene, MEL_charPlaceholder);
		  client.modifyProperties(user, view, mod);
	       }

	       disableAction(w, as.matScene);
	       disableAction(w, as.lr_matScene);
	       disableAction(w, as.shdScene);

	       client.submitJobs(user, view, as.shdScene, null);
	       client.submitJobs(user, view, as.lr_finalScene, null);
	    } else if ( stage == 3 )
	    {
	       {
		  NodeID nodeID = new NodeID(user, view, as.shdScene);
		  client.checkIn(nodeID,
		     "Inital model tree with placeholder geometry in model scene.",
		     VersionID.Level.Minor);
	       }

	       {
		  NodeID nodeID = new NodeID(user, view, as.lr_finalScene);
		  client.checkIn(nodeID,
		     "Inital model tree with placeholder geometry in model scene.",
		     VersionID.Level.Minor);
	       }
	    }
	 }
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
      }

   }

   private static void printHelp()
   {
      System.err.println("Use:  ./buildLairAssets <flags>");
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
      System.err.println("\t--asset=<value,value>");
      System.err.println("\t\tAn asset in the shot, followed by the type of the asset.");
      System.err.println("\t\t\tUse a separate flag for each asset.");
      System.err.println("\t\tValid asset types:");
      System.err.println("\t\t\tcharacter");
      System.err.println("\t\t\tprop");
      System.err.println("\t\t\tset");
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
      System.err.println("Lair Asset Creator version 1.0");
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
   private boolean verbose;
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

}

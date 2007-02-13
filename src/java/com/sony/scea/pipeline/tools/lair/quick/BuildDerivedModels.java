package com.sony.scea.pipeline.tools.lair.quick;

import java.util.LinkedList;
import java.util.TreeSet;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.*;
import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

public class BuildDerivedModels
{
   private static boolean verbose = true;

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      String user = "pipeline";
      String view = "build";
      String toolset = "csg_rev18";
      MasterMgrClient client = new MasterMgrClient();
      Wrapper w = null;
      String project = "lr";

      try
      {
	 w = new Wrapper(user, view, toolset, client);
	 PluginMgrClient.init();
      } catch ( PipelineException e1 )
      {
	 e1.printStackTrace();
      }

      String baseModel = "/projects/lr/assets/prop/asylianHelmet/asylianHelmet";
      String finalizeMel = "/projects/lr/assets/tools/mel/finalize-character";

      int pass = 3;

      LinkedList<String> toAdd = new LinkedList<String>();
      for (int i = 2; i <= 10; i++)
      {
	 toAdd.add("asylianHelmet" + i);
      }
      if ( pass == 1 )
      {
	 try
	 {
	    SonyAsset baseAsset = SonyConstants.stringToAsset(w, baseModel);
	    Globals.getNewest(w, baseAsset.finalScene, CheckOutMode.OverwriteAll,
	       CheckOutMethod.AllFrozen);
	    Globals.getNewest(w, baseAsset.lr_finalScene, CheckOutMode.OverwriteAll,
	       CheckOutMethod.AllFrozen);

	    AssetType baseType = baseAsset.assetType;

	    for (String name : toAdd)
	    {
	       TreeSet<String> addedNodes = new TreeSet<String>();
	       try
	       {
		  SonyAsset as = new SonyAsset(project, name, baseType);

		  log(as.texGroup + " : ");
		  if ( !Globals.doesNodeExists(w, as.texGroup) )
		  {
		     logLine("Building");
		     NodeMod mod = Globals.registerNode(w, as.texGroup, null, Plugins
			.editorKWrite(w));
		     addedNodes.add(as.texGroup);
		     BaseAction act = Plugins.actionListSources(w);
		     mod.setAction(act);
		     doReqs(mod);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.matScene + " : ");
		  if ( !Globals.doesNodeExists(w, as.matScene) )
		  {
		     logLine("Building");
		     NodeMod mod = Globals.registerNode(w, as.matScene, "ma", Plugins
			.editorMaya(w));
		     addedNodes.add(as.matScene);
		     BaseAction act = Plugins.actionMayaReference(w);
		     Globals.referenceNode(w, as.matScene, baseAsset.rigScene, act, REF,
			"rig");
		     client.link(user, view, as.matScene, as.texGroup, REF, LINKALL, null);
		     mod.setAction(act);
		     doReqs(mod);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.finalScene + " : ");
		  if ( !Globals.doesNodeExists(w, as.finalScene) )
		  {
		     logLine("Building");
		     NodeMod mod = Globals.registerNode(w, as.finalScene, "ma", Plugins
			.editorMaya(w));
		     addedNodes.add(as.finalScene);
		     BaseAction act = Plugins.actionMayaImport(w);
		     Globals.referenceNode(w, as.finalScene, as.matScene, act, DEP, "mat");
		     client
			.link(user, view, as.finalScene, finalizeMel, DEP, LINKALL, null);
		     act.setSingleParamValue("ModelMEL", finalizeMel);
		     mod.setAction(act);
		     doReqs(mod);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.lr_matScene + " : ");
		  if ( !Globals.doesNodeExists(w, as.lr_matScene) )
		  {
		     logLine("Building");
		     NodeMod mod = Globals.registerNode(w, as.lr_matScene, "ma", Plugins
			.editorMaya(w));
		     addedNodes.add(as.lr_matScene);
		     BaseAction act = Plugins.actionMayaReference(w);
		     Globals.referenceNode(w, as.lr_matScene, baseAsset.lr_rigScene, act,
			REF, "rig");
		     mod.setAction(act);
		     doReqs(mod);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  log(as.lr_finalScene + " : ");
		  if ( !Globals.doesNodeExists(w, as.lr_finalScene) )
		  {
		     logLine("Building");
		     NodeMod mod = Globals.registerNode(w, as.lr_finalScene, "ma", Plugins
			.editorMaya(w));
		     addedNodes.add(as.lr_finalScene);
		     BaseAction act = Plugins.actionMayaImport(w);
		     Globals.referenceNode(w, as.lr_finalScene, as.lr_matScene, act, DEP,
			"mat");
		     client.link(user, view, as.lr_finalScene, finalizeMel, DEP, LINKALL,
			null);
		     act.setSingleParamValue("ModelMEL", finalizeMel);
		     mod.setAction(act);
		     doReqs(mod);
		     client.modifyProperties(user, view, mod);
		  } else
		     logLine("Already Exists");

		  try
		  {
		     client.submitJobs(user, view, as.finalScene, null);
		     client.submitJobs(user, view, as.lr_finalScene, null);
		  } catch ( PipelineException ex )
		  {
		     ex.printStackTrace();
		  }

	       } catch ( PipelineException ex )
	       {
		  try
		  {
		     Globals.releaseNodes(w, addedNodes);
		  } catch ( PipelineException e )
		  {
		     e.printStackTrace();
		  }
		  ex.printStackTrace();
		  return;
	       }
	    }

	 } catch ( PipelineException e )
	 {
	    e.printStackTrace();
	 }
      } else if ( pass == 2 )
      {
	 for (String name : toAdd)
	 {
	    try
	    {
	       logLine(name);
	       SonyAsset baseAsset = SonyConstants.stringToAsset(w, baseModel);
	       AssetType baseType = baseAsset.assetType;
	       SonyAsset as = new SonyAsset(project, name, baseType);
	       Globals.disableAction(w, as.matScene);
	       Globals.disableAction(w, as.lr_matScene);
	    } catch ( PipelineException e )
	    {
	       e.printStackTrace();
	    }
	 }

      } else if ( pass == 3 )
      {
	 for (String name : toAdd)
	 {
	    try
	    {
	       logLine(name);
	       SonyAsset baseAsset = SonyConstants.stringToAsset(w, baseModel);
	       AssetType baseType = baseAsset.assetType;
	       SonyAsset as = new SonyAsset(project, name, baseType);
	       NodeID nodeID = new NodeID(user, view, as.finalScene);

	       client.checkIn(nodeID,
		  "Inital model tree built by the BuildDerivedModels tool.  Built off the "
			+ baseModel + " model", VersionID.Level.Minor);

	       nodeID = new NodeID(user, view, as.lr_finalScene);
	       client.checkIn(nodeID,
		  "Inital model tree built by the BuildDerivedModels tool.  Built off the "
			+ baseModel + " model", VersionID.Level.Minor);
	    } catch ( PipelineException e )
	    {
	       e.printStackTrace();
	    }
	 }

      }

   }

   private static void doReqs(NodeMod mod) throws PipelineException
   {
      JobReqs req = mod.getJobRequirements();
      req.addSelectionKey("Lair");
      mod.setJobRequirements(req);
   }

   private static void log(String s)
   {
      if ( verbose )
      {
	 System.err.print(s);
	 //log.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, s);
      }
   }

   private static void logLine(String s)
   {
      if ( verbose )
      {
	 System.err.println(s);
	 //log.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Fine, s);
      }
   }

   public static final LinkPolicy REF = LinkPolicy.Reference;
   public static final LinkRelationship LINKALL = LinkRelationship.All;
   public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
   public static final LinkPolicy DEP = LinkPolicy.Dependency;
}

package com.sony.scea.pipeline.tools.lair.quick;

import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.VersionID.Level;

import com.sony.scea.pipeline.tools.*;
import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

public class RerigAll
{

   static String project;
   static String toolset;

   /**
         * @param args
         */
   public static void main(String[] args)
   {
      String user = "pipeline";
      String view = "build";
      toolset = "csg_rev23";
      project = "lr";
      MasterMgrClient mclient = new MasterMgrClient();
      Wrapper w;
      try
      {
	 PluginMgrClient.init();
	 w = new Wrapper(user, view, toolset, mclient);
	 TreeMap<String, String> props = SonyConstants.getAssetList(w, project,
	    AssetType.PROP);
	 TreeMap<String, String> sets = SonyConstants.getAssetList(w, project,
	    AssetType.SET);
	 TreeMap<String, String> chars = SonyConstants.getAssetList(w, project,
	    AssetType.CHARACTER);
	 int pass = 3;
	 if ( pass == 1 )
	 {
	    
            for (String assetPrefix : props.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = props.get(assetPrefix);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.PROP);
	       doPassOne(w, as);
	    }

	    for (String assetPrefix : sets.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = sets.get(assetPrefix);
	       // System.out.println(nodeName);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.SET);
	       doPassOne(w, as);
	    }
                 

//	    for (String assetPrefix : chars.keySet())
//	    {
//	       System.out.println(assetPrefix);
//	       String nodeName = chars.get(assetPrefix);
//	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
//		  AssetType.CHARACTER);
//	       doPassOne(w, as);
//	    }


	 } else if ( pass == 2 )
	 {
	    for (String assetPrefix : props.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = props.get(assetPrefix);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.PROP);
	       doPassTwo(w, as);
	    }

	    for (String assetPrefix : sets.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = sets.get(assetPrefix);
	       // System.out.println(nodeName);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.SET);
	       doPassTwo(w, as);
	    }

//	    for (String assetPrefix : chars.keySet())
//	    {
//	       System.out.println(assetPrefix);
//	       String nodeName = chars.get(assetPrefix);
//	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
//		  AssetType.CHARACTER);
//	       doPassTwo(w, as);
//	    }
	 }

	 else if ( pass == 3 )
	 {
	    for (String assetPrefix : props.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = props.get(assetPrefix);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.PROP);
	       doPassThree(w, as);
	    }
	    for (String assetPrefix : sets.keySet())
	    {
	       System.out.println(assetPrefix);
	       String nodeName = sets.get(assetPrefix);
	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
		  AssetType.SET);
	       doPassThree(w, as);
	    }
//	    for (String assetPrefix : chars.keySet())
//	    {
//	       System.out.println(assetPrefix);
//	       String nodeName = chars.get(assetPrefix);
//	       SonyAsset as = new SonyAsset(project, new Path(nodeName).getName(),
//		  AssetType.CHARACTER);
//	       doPassThree(w, as);
//	    }
	 }
      } catch ( PipelineException e )
      {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
      }

   }

   private static void doPassOne(Wrapper w, SonyAsset as) throws PipelineException
   {
      MasterMgrClient mclient = w.mclient;
      String author = w.user;
      String view = w.view;
      if ( as.assetName.equals("plainsDragon5") )
	 return;
      if ( as.assetName.equals("testCamera1") )
	 return;
      
      try
      {
	 Globals.getNewest(w, as.texGroup, over, froz);
	 if (Globals.doesNodeExists(w, as.rigScene))
	    Globals.getLatest(w, as.rigScene, over, modi);
	 Globals.getLatest(w, as.matScene, keep, modi);
	 Globals.getLatest(w, as.matExpScene, keep, modi);
	 Globals.getLatest(w, as.finalScene, keep, modi);
	 Globals.getLatest(w, as.lr_finalScene, keep, modi);
	 {
	    NodeMod mod = mclient.getWorkingVersion(author, view, as.finalScene);
	    mod.setToolset(toolset);
	    mclient.modifyProperties(w.user, w.view, mod);
	 }
//	 {
//	    NodeMod mod = mclient.getWorkingVersion(author, view, as.rigScene);
//	    mod.setToolset(toolset);
//	    mclient.modifyProperties(w.user, w.view, mod);
//	 }


//	 {
//	    NodeMod mod = mclient.getWorkingVersion(author, view, as.finalScene);
//	    BaseAction act = mod.getAction();
//	    act.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
//	    mod.setAction(act);
//	    mclient.modifyProperties(w.user, w.view, mod);
//	 }
//	 {
//	    NodeMod mod = mclient.getWorkingVersion(author, view, as.lr_finalScene);
//	    BaseAction act = mod.getAction();
//	    act.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
//	    mod.setAction(act);
//	    mclient.modifyProperties(w.user, w.view, mod);
//	 }
      } catch (PipelineException ex)
      {
	 ex.printStackTrace();
      }
      // Globals.getLatest(w, as.lr_finalScene, keep, modi);
      //Globals.getLatest(w, as.matExpScene, keep, modi);
      //Globals.getLatest(w, as.matScene, keep, modi);
      //Globals.getLatest(w, as.rigScene, keep, modi);
      // Globals.getLatest(w, as.modScene, keep, modi);
      // Globals.getLatest(w, as.headModScene, keep, modi);
      // Globals.getLatest(w, as.blendShapeScene, keep, modi);
       //Globals.getLatest(w, as.lr_matScene, keep, modi);
      
//      if (Globals.doesNodeExists(w, as.rigScene))
//      {
//      if (!Globals.doesNodeExists(w, as.syflexScene))
//      {
//	 NodeMod mod = Globals.registerNode(w, as.syflexScene, "ma", Plugins.editorMaya(w));
//	 BaseAction act = Plugins.actionMayaBuild(w);
//	 mod.setAction(act);
//	 mclient.modifyProperties(w.user, w.view, mod);
//      }
//      }
      
//      {
//	 NodeMod mod = mclient.getWorkingVersion(w.user, w.view, as.matExpScene);
//	 BaseAction oldAct = mod.getAction();
//	 BaseAction act = Plugins.actionMayaShaderExport(w);
//	 act.setSingleParamValues(oldAct);
//	 mod.setAction(act);
//	 mclient.modifyProperties(w.user, w.view, mod);
//      }

/*      SonyAsset baseAsset = null;

      {
	 NodeMod mod = mclient.getWorkingVersion(author, view, as.matScene);
	 TreeSet<String> sources = new TreeSet<String>(mod.getSourceNames());
	 String rigName = null;
	 for (String source : sources)
	 {
	    System.out.println("Source: " + source);
	    if ( source.matches(".*_rig") )
	    {
	       rigName = source;
	       break;
	    }
	 }
	 if ( rigName == null )
	 {
	    System.err.println("ALL READY DONE??!?!?!?!?!?!?");
	    return;
	 }
	 String name = new Path(rigName).getName();
	 name = name.replace("_rig", "");
	 baseAsset = new SonyAsset(project, name, as.assetType);
      }

      System.out.println(baseAsset.finalScene);

      System.out.println("Creating the material Export scene");
      {
	 NodeMod mod = Globals.registerNode(w, as.matExpScene, "ma", Plugins.editorMaya(w));
	 mclient.link(w.user, w.view, as.matExpScene, as.matScene, DEP, LINKALL, null);
	 BaseAction act = Plugins.actionMayaShaderExport(w);
	 act.setSingleParamValue("SelectionPrefix", "");
	 act.setSingleParamValue("MayaScene", as.matScene);
	 mod.setAction(act);
	 mclient.modifyProperties(w.user, w.view, mod);
      }
      if ( Globals.doesNodeExists(w, as.rigScene) )
      {
	 System.out.println("Fixing the Rig scene");
	 NodeMod mod = mclient.getWorkingVersion(author, view, as.rigScene);
	 BaseAction act = Plugins.actionMayaBuild(w);
	 act.initSourceParams(as.modScene);
	 act.setSourceParamValue(as.modScene, "BuildType", "Import");
	 act.setSourceParamValue(as.modScene, "NameSpace", false);
	 if ( Globals.doesNodeExists(w, as.blendShapeScene) )
	 {
	    mclient.link(w.user, w.view, as.rigScene, as.blendShapeScene, DEP, LINKALL,
	       null);
	    act.initSourceParams(as.blendShapeScene);
	    act.setSourceParamValue(as.blendShapeScene, "BuildType", "Import");
	    act.setSourceParamValue(as.blendShapeScene, "NameSpace", false);
	 }
	 if ( Globals.doesNodeExists(w, as.headModScene) )
	 {
	    mclient.link(w.user, w.view, as.rigScene, as.headModScene, DEP, LINKALL, null);
	    act.initSourceParams(as.headModScene);
	    act.setSourceParamValue(as.headModScene, "BuildType", "Import");
	    act.setSourceParamValue(as.headModScene, "NameSpace", false);
	 }
	 mod.setAction(act);
	 if ( as.assetType == AssetType.CHARACTER )
	    mod.setActionEnabled(false);
	 mclient.modifyProperties(w.user, w.view, mod);
      }
      System.out.println("Fixing the Material scene");
      {
	 NodeMod mod = mclient.getWorkingVersion(author, view, as.matScene);
	 mclient.unlink(author, view, as.matScene, baseAsset.rigScene);
	 BaseAction act = Plugins.actionMayaBuild(w);
	 Globals.referenceNode(w, as.matScene, baseAsset.modScene, act, REF, "mod");
	 if ( Globals.doesNodeExists(w, as.headModScene) )
	 {
	    Globals.referenceNode(w, as.matScene, as.headModScene, act, REF, "head");
	 }
	 mod.setAction(act);
	 mod.setActionEnabled(true);
	 mclient.modifyProperties(w.user, w.view, mod);
	 mclient.removeFiles(author, view, as.matScene, null);
      }
      System.out.println("Fixing the low rez material scene");
      {
	 NodeMod mod = mclient.getWorkingVersion(author, view, as.lr_matScene);
	 for (String source : mod.getSourceNames())
	    mclient.unlink(author, view, as.lr_matScene, source);
	 BaseAction act = Plugins.actionMayaBuild(w);
	 Globals.referenceNode(w, as.lr_matScene, baseAsset.modScene, act, REF, "mod");
	 if ( Globals.doesNodeExists(w, baseAsset.headModScene) )
	 {
	    Globals.referenceNode(w, as.lr_matScene, baseAsset.headModScene, act, REF,
	       "head");
	 }
	 mclient.link(w.user, w.view, as.lr_matScene, as.texGroup, REF, LINKALL, null);
	 mod.setAction(act);
	 mod.setActionEnabled(true);
	 mclient.modifyProperties(w.user, w.view, mod);
	 mclient.removeFiles(author, view, as.lr_matScene, null);
      }
      System.out.println("Fixing the final scene");
      {
	 String melscript = null;
	 switch (as.assetType)
	 {
	    case CHARACTER:
	       melscript = "/projects/lr/assets/tools/mel/finalize-character";
	       break;
	    case PROP:
	       melscript = "/projects/lr/assets/tools/mel/finalize-prop";
	       break;
	    case SET:
	       melscript = "/projects/lr/assets/tools/mel/finalize-set";
	       break;

	 }

	 NodeMod mod = mclient.getWorkingVersion(author, view, as.finalScene);
	 BaseAction act = Plugins.actionMayaBuild(w);
	 if ( mod.getSourceNames().contains(
	    "/projects/lr/assets/tools/mel/finalize-character") )
	    mclient.unlink(author, view, as.finalScene,
	       "/projects/lr/assets/tools/mel/finalize-character");
	 mclient
	    .link(w.user, w.view, as.finalScene, baseAsset.rigScene, DEP, LINKALL, null);
	 mclient.link(w.user, w.view, as.finalScene, as.matExpScene, DEP, LINKALL, null);
	 act.initSourceParams(as.matScene);
	 act.setSourceParamValue(as.matScene, "BuildType", "Reference");
	 act.setSourceParamValue(as.matScene, "NameSpace", true);
	 act.setSourceParamValue(as.matScene, "PrefixName", "source");
	 act.initSourceParams(baseAsset.rigScene);
	 act.setSourceParamValue(baseAsset.rigScene, "BuildType", "Import");
	 act.setSourceParamValue(baseAsset.rigScene, "NameSpace", false);
	 act.initSourceParams(as.matExpScene);
	 act.setSourceParamValue(as.matExpScene, "BuildType", "Import");
	 act.setSourceParamValue(as.matExpScene, "NameSpace", false);
	 mclient.link(w.user, w.view, as.finalScene, melscript, DEP, LINKALL, null);
	 act.setSingleParamValue("ModelMEL", melscript);
	 mod.setAction(act);
	 mclient.modifyProperties(w.user, w.view, mod);
      }
      System.out.println("Fixing the low rez final scene");
      {
	 String melscript = null;
	 switch (as.assetType)
	 {
	    case CHARACTER:
	       melscript = "/projects/lr/assets/tools/mel/finalize-character_lr";
	       break;
	    case PROP:
	       melscript = "/projects/lr/assets/tools/mel/finalize-prop_lr";
	       break;
	    case SET:
	       melscript = "/projects/lr/assets/tools/mel/finalize-set_lr";
	       break;

	 }

	 NodeMod mod = mclient.getWorkingVersion(author, view, as.lr_finalScene);
	 for (String source : mod.getSourceNames())
	    mclient.unlink(author, view, as.lr_finalScene, source);
	 BaseAction act = Plugins.actionMayaBuild(w);
	 mclient.link(w.user, w.view, as.lr_finalScene, baseAsset.rigScene, DEP, LINKALL,
	    null);
	 act.initSourceParams(baseAsset.rigScene);
	 act.setSourceParamValue(baseAsset.rigScene, "BuildType", "Import");
	 act.setSourceParamValue(baseAsset.rigScene, "NameSpace", false);
	 mclient.link(w.user, w.view, as.lr_finalScene, melscript, DEP, LINKALL, null);
	 act.setSingleParamValue("ModelMEL", melscript);
	 mod.setAction(act);
	 mclient.modifyProperties(w.user, w.view, mod);
      }*/

   }

   private static void doPassTwo(Wrapper w, SonyAsset as) throws PipelineException
   {
      if ( as.assetName.equals("plainsDragon5") )
	 return;
      if ( as.assetName.equals("testCamera1") )
	 return;
      try
      {
//	      if (Globals.doesNodeExists(w, as.syflexScene))
//	      {
		 //mclient.submitJobs(author, view, as.syflexScene, null);
//		 Globals.removeAction(w, as.syflexScene);
//	      }

	 w.mclient.submitJobs(w.user, w.view, as.finalScene, null);
	 
	 // w.mclient.submitJobs(w.user, w.view, as.lr_matScene, null);
	 // Globals.disableAction(w, as.matScene);
	 // Globals.disableAction(w, as.lr_matScene);
      } catch ( PipelineException ex )
      {
	 ex.printStackTrace();
      }
//      try
//      {
//	 w.mclient.submitJobs(w.user, w.view, as.lr_finalScene, null);
//      } catch ( PipelineException ex )
//      {
//	 ex.printStackTrace();
//      }
     }

   private static void doPassThree(Wrapper w, SonyAsset as) throws PipelineException
   {
      if ( as.assetName.equals("plainsDragon5") )
	 return;
      if ( as.assetName.equals("testCamera1") )
	 return;
      try
      {
	 w.mclient.checkIn(w.user, w.view, as.finalScene,
	    "latest finalize scripts to make the rayUtil and colorMix setups work.", Level.Minor);
//	 if (Globals.doesNodeExists(w, as.syflexScene))
//	    w.mclient.checkIn(w.user, w.view, as.syflexScene,
//	       "Added the syflex scene.", Level.Minor);
//	 w.mclient.checkIn(w.user, w.view, as.lr_matScene,
//	    "Fixed the namespace issues.", Level.Minor);
//	 w.mclient.checkIn(w.user, w.view, as.finalScene,
//	    "Updated to a new version of MayaExportShaders.", Level.Minor);
//	 w.mclient.checkIn(w.user, w.view, as.finalScene,
//	    "Complete rebuild of the entire asset structure.  "
//		  + "No versions before this should be used in production.", Level.Major);
//	 w.mclient.checkIn(w.user, w.view, as.lr_finalScene,
//	    "Complete rebuild of the entire asset structure.  "
//		  + "No versions before this should be used in production.", Level.Major);
//	 w.mclient.checkIn(w.user, w.view, as.lr_matScene,
//	    "Complete rebuild of the entire asset structure.  "
//		  + "No versions before this should be used in production.", Level.Major);
      } catch ( PipelineException ex )
      {
	 ex.printStackTrace();
      }
//      try
//      {
//	 w.mclient.checkIn(w.user, w.view, as.lr_finalScene,
//	    "updated to Toolset 20 to allow for syflex.", Level.Minor);
//      } catch ( PipelineException ex )
//      {
//	 ex.printStackTrace();
//      }

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

}

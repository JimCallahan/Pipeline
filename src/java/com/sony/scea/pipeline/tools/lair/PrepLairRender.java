package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;
import static com.sony.scea.pipeline.tools.lair.LairConstants.getAssetValues;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;

public class PrepLairRender extends BootApp
{

   MasterMgrClient client;
   PluginMgrClient plug;

   public PrepLairRender()
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

   @Override
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

      Wrapper w = null;
      LairShot sh = null;
      try
      {

	 w = new Wrapper(user, view, toolset, client);
	 sh = LairShot.getShot(w, movie, seq, shot, null, null);
	 //int start = 1;
	 //int end = sh.length;
	 //int by = 1;
      } catch ( PipelineException ex )
      {
	 ex.printStackTrace();
	 return;
      }

      for (LairAsset as : sh.assets)
      {
	 try
	 {

	    logLine("Doing asset: " + as.assetName);

	    logLine("\tLocking the asset");
	    lockLatest(w, as.finalScene);

	    logLine("\tFreezing all the textures");
	    getLatest(w, as.texGroup, over, froz);

	    logLine("\tFreezing the shader includes");
	    getLatest(w, as.shdIncGroup, over, froz);

	    logLine("\tChecking out the shader scene.");
	    getNewest(w, as.shdScene, keep, pFroz);

	    logLine("\tChecking out the shader export scene.");
	    getNewest(w, as.shdExport, keep, pFroz);

	    logLine("\tChecking out the shader export scene.");
	    getNewest(w, as.shdExport, keep, pFroz);

	    logLine("\tLocking the Geo MIs.");
	    lockLatest(w, sh.assetGeoMI.get(as.assetName));

	    logLine("\tFreezing the Geo Instance MIs");
	    getLatest(w, sh.assetGeoInstMI.get(as.assetName), keep, froz);
	 } catch ( PipelineException e )
	 {
	    e.printStackTrace();
	 }
      }

      {
	 try
	 {
	    logLine("Doing shared nodes");

	    logLine("\tChecking out the lighting shader def scene.");
	    getNewest(w, sh.lightShaderDefsMI, keep, pFroz);

	    logLine("\tChecking out the pre-light scene.");
	    getNewest(w, sh.preLightScene, keep, pFroz);
	    doToolsetThingie(sh.preLightScene);

	    logLine("\tChecking out the light scene.");
	    getNewest(w, sh.lightScene, keep, pFroz);
	    doToolsetThingie(sh.lightScene);

	    logLine("\tLocking the camera MIs.");
	    lockLatest(w, sh.cameraMI);

	 } catch ( PipelineException e )
	 {
	    e.printStackTrace();
	 }
      }

      for (String passName : passes)
      {
	 if ( sh.passes.contains(passName) )
	 {
	    logLine("Doing pass: " + passName);
	    try
	    {

	       logLine("\tDoing the camera options node");
	       {
		  String name = sh.passCamOverMI.get(passName);
		  getNewest(w, name, keep, pFroz);
		  doToolsetThingie(name);
	       }

	       logLine("\tDoing the option MI");
	       {
		  String name = sh.passOptionMI.get(passName);
		  getNewest(w, name, keep, pFroz);
		  doToolsetThingie(name);
	       }

	       logLine("\tDoing the lights MIs");
	       {
		  String name = sh.passLightMI.get(passName);
		  getNewest(w, name, keep, pFroz);
		  doToolsetThingie(name);
	       }

	       ArrayList<LairAsset> updateAssets = null;
	       { //determine which assets to update.
		  if ( global )
		  {
		     updateAssets = new ArrayList<LairAsset>(sh.assets);
		  } else
		  {
		     TreeSet<LairAsset> temp = new TreeSet<LairAsset>();
		     for (LairAsset as : sh.assets)
			if ( assets.contains(as) )
			   temp.add(as);
		     temp.addAll(assets);
		     if ( assetGroupings != null )
		     {
			for (String s : sh.assetGroupings.keySet())
			   if ( assetGroupings.contains(s) )
			      temp.addAll(sh.assetGroupings.get(s));
		     }
		     updateAssets = new ArrayList<LairAsset>(temp);
		  }
	       }

	       for (LairAsset as : updateAssets)
	       {
		  logLine("\tDoing per-pass stuff for asset: " + as.assetName);

		  logLine("\t\tDoing the shade def MIs");
		  {
		     String name = sh.assetPassShaderDefsMI.get(as.assetName, passName);
		     getNewest(w, name, keep, pFroz);
		     doToolsetThingie(name);
		  }

		  logLine("\t\tDoing the shade MIs");
		  {
		     String name = sh.assetPassShadeMI.get(as.assetName, passName);
		     getNewest(w, name, keep, pFroz);
		     doToolsetThingie(name);
		  }
	       }

	       if ( global )
	       {
		  String name = sh.passImages.get(passName);
		  logLine("\tDoing global images: " + name);
		  getNewest(w, name, keep, pFroz);
		  doToolsetThingie(name);
	       }

	       for (LairAsset as : assets)
	       {
		  if ( sh.assets.contains(as) )
		  {
		     String name = sh.assetPassImages.get(as.assetName, passName);
		     logLine("\tDoing asset images: " + name);
		     getNewest(w, name, keep, pFroz);
		     doToolsetThingie(name);
		  }
	       }

	       if ( assetGroupings != null )
	       {
		  for (String s : assetGroupings)
		  {
		     if ( sh.assetGroupings.keySet().contains(s) )
		     {
			String name = sh.assetPassImages.get(s, passName);
			logLine("\tDoing assetGroup images: " + name);
			getNewest(w, name, keep, pFroz);
			doToolsetThingie(name);
		     }
		  }
	       }
	    } catch ( PipelineException e )
	    {
	       e.printStackTrace();
	    }
	 }
      }
   }

   private void doToolsetThingie(String name) throws PipelineException
   {
      if ( toolset != null )
      {
	 NodeMod mod = client.getWorkingVersion(user, view, name);
	 mod.setToolset(toolset);
	 client.modifyProperties(user, view, mod);
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
	 user = getStringValue("user", true, parsedArgs);
	 view = getStringValue("view", true, parsedArgs);
	 toolset = getStringValue("toolset", false, parsedArgs);
	 passes = getStringValues("pass", true, parsedArgs);
	 assets = getAssetValues("asset", false, parsedArgs);
	 assetGroupings = getStringValues("group", false, parsedArgs);
	 movie = getStringValue("movie", true, parsedArgs);
	 seq = getStringValue("seq", true, parsedArgs);
	 shot = getStringValue("shot", true, parsedArgs);
	 {
	    Boolean temp = getBooleanValue("verbose", false, parsedArgs);
	    if ( temp == null )
	       verbose = false;
	    else
	       verbose = temp;
	 }
	 {
	    Boolean temp = getBooleanValue("global", false, parsedArgs);
	    if ( temp == null )
	       global = false;
	    else
	       global = temp;
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

   private static void printHelp()
   {
      System.err.println("Use:  ./prepLairRender <flags>");
      System.err.println("Flag List:");
      System.err.println("\t--help");
      System.err.println("\t\tPrint this message.");
      System.err.println("\t--verbose=<boolean>");
      System.err.println("\t\tprint out lots of info.");
      System.err.println("\t\tIf you do not use this flag, it will default to false.");
      System.err.println("\t--user=<value>");
      System.err.println("\t\tThe user whose working area "
	    + "you want to prep the tree in.");
      System.err.println("\t--view=<value>");
      System.err.println("\t\tThe working area you want to prep the tree in.");
      System.err.println("\t--toolset=<value>");
      System.err
	 .println("\t\tOptional flag.  Setting this will cause "
	       + "Pipeline to set all nodes that are modifiable after the prep to the specified toolset.");
      System.err.println("\t\tThis provides an easy way to upgrade the toolsets"
	    + "on lots of nodes at once.");
      System.err.println("\t--pass=<value>");
      System.err.println("\t\tWhich render passes should be preped.");
      System.err.println("\t\t\tUse a separate flag for each pass.");
      System.err.println("\t--global=<boolean>");
      System.err.println("\t\tDo you want to prep the global image node that "
	    + "incorporates all the assets.");
      System.err.println("\t--asset=<value>");
      System.err.println("\t\tAn asset to prep images for.");
      System.err.println("\t\t\tUse a separate flag for each asset.");
      System.err.println("\t--group=<value>");
      System.err.println("\t\tAn asset group to prep images for");
      System.err.println("\t\t\tUse a separate flag for each group.");
      System.err.println("\t--movie=<value>");
      System.err.println("\t\tThe Movie the shot is in.");
      System.err.println("\t--seq=<value>");
      System.err.println("\t\tThe Sequence the shot is in.");
      System.err.println("\t--shot=<value>");
      System.err.println("\t\tThe name of the shot.");
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

   private static void printVersion()
   {
      System.err.println("Lair Render Prep Tool version 1.0");
   }

   private boolean verbose;
   private String user;
   private String view;
   private String toolset;
   private ArrayList<String> passes;
   private ArrayList<LairAsset> assets;
   private ArrayList<String> assetGroupings;
   private boolean global;
   private String movie;
   private String seq;
   private String shot;

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

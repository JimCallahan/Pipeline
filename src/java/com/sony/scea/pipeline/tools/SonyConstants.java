package com.sony.scea.pipeline.tools;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.Path;
import us.temerity.pipeline.PipelineException;

import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

public class SonyConstants
{
   public static final String charPattern(String proj)
   {
      return "/projects/" + proj + charPattern;
   }

   public static final String setPattern(String proj)
   {
      return "/projects/" + proj + setPattern;
   }

   public static final String propPattern(String proj)
   {
      return "/projects/" + proj + propPattern;
   }

   public static final String cameraStartPattern(String proj)
   {
      return "/projects/" + proj + "/assets/camera/";
   }

   public static final String miscStartPattern(String proj)
   {
      return "/projects/" + proj + "/assets/misc/";
   }

   public static final String lightStartPattern(String proj)
   {
      return "/projects/" + proj + "/assets/lights/";
   }

   public static final TreeMap<String, String> getAllMelWithPrefix(Wrapper w, String project)
      throws PipelineException
   {
      ArrayList<String> globalMel = Globals.getChildrenNodes(w, PATH_globalMelPath);

      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      for (String path : globalMel)
      {
	 Path p = new Path(path);
	 toReturn.put("global-" + p.getName(), PATH_globalMelPath + "/" + path);
      }
      if ( project != null )
      {
	 String searchPath = "/projects/" + project + "/" + PATH_projectMelEndPath;
	 ArrayList<String> projectMel = Globals.getChildrenNodes(w, searchPath);
	 for (String path : projectMel)
	 {
	    Path p = new Path(path);
	    toReturn.put("proj-" + p.getName(), searchPath + "/" + path);
	 }
      }
      return toReturn;
   }

   public static final ArrayList<String> getProjectList(Wrapper w) throws PipelineException
   {
      return Globals.getChildrenDirs(w, "/projects");
   }

   public static final ArrayList<String> getMovieList(Wrapper w, String proj)
      throws PipelineException
   {
      String start = "/projects/" + proj + "/production";
      return Globals.getChildrenDirs(w, start);
   }

   public static final ArrayList<String> getSequenceList(Wrapper w, String proj,
	 String movie) throws PipelineException
   {
      String start = "/projects/" + proj + "/production/" + movie;
      return Globals.getChildrenDirs(w, start);
   }

   public static final ArrayList<String> getShotList(Wrapper w, String proj, String movie,
	 String seq) throws PipelineException
   {
      String start = "/projects/" + proj + "/production/" + movie + "/" + seq;
      return Globals.getChildrenDirs(w, start);
   }

   public static ArrayList<String> getAllAssets(Wrapper w, String project)
      throws PipelineException
   {
      TreeMap<String, String> chars = getAssetList(w, project, AssetType.CHARACTER);
      TreeMap<String, String> props = getAssetList(w, project, AssetType.PROP);
      TreeMap<String, String> sets = getAssetList(w, project, AssetType.SET);

      ArrayList<String> toReturn = new ArrayList<String>(chars.size() + props.size()
	    + sets.size());
      toReturn.addAll(chars.keySet());
      toReturn.addAll(props.keySet());
      toReturn.addAll(sets.keySet());
      return toReturn;
   }

   public static TreeMap<String, String> getAllAssetsMap(Wrapper w, String project)
      throws PipelineException
   {
      TreeMap<String, String> chars = getAssetList(w, project, AssetType.CHARACTER);
      TreeMap<String, String> props = getAssetList(w, project, AssetType.PROP);
      TreeMap<String, String> sets = getAssetList(w, project, AssetType.SET);

      chars.putAll(props);
      chars.putAll(sets);

      return chars;
   }

   public static TreeMap<String, String> getMiscAssets(Wrapper w, String project)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = miscStartPattern(project);
      ArrayList<String> list = Globals.getAllNodes(w, start);
      for (String s : list)
      {
	 Path p = new Path(s);
	 String key = p.getName();
	 while ( toReturn.containsKey(key) )
	 {
	    key += "-";
	 }
	 toReturn.put(key, s);
      }
      return toReturn;
   }

   public static TreeMap<String, String> getAssetList(Wrapper w, String project,
	 AssetType type) throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = null;
      switch (type)
      {
	 case CHARACTER:
	    start = "/projects/" + project + charPattern;
	    break;
	 case SET:
	    start = "/projects/" + project + setPattern;
	    break;
	 case PROP:
	    start = "/projects/" + project + propPattern;
	    break;
      }
      ArrayList<String> list = Globals.getAllNodes(w, start);
      for (String s : list)
      {
	 Path p = new Path(s);
	 String name = p.getName();
	 String parent = p.getParentPath().getName();

	 if ( name.equals(parent) )
	 {
	    String key = name;
	    while ( toReturn.containsKey(key) )
	    {
	       key += "-";
	    }
	    toReturn.put(key, s);
	 }
      }
      return toReturn;
   }

   public static SonyAsset stringToAsset(Wrapper w, String name) throws PipelineException
   {
      SonyAsset as;
      if ( Globals.doesNodeExists(w, name) )
      {
	 Path p = new Path(name);
	 String assetName = p.getParentPath().getName();
	 AssetType assetType = AssetType.fromString(p.getParentPath().getParentPath()
	    .getName());
	 ArrayList<String> temp = p.getComponents();
	 String project = temp.get(1);
	 as = new SonyAsset(project, assetName, assetType);
      } else
	 throw new PipelineException("There is no node with the name (" + name + ")");
      return as;
   }

   public static TreeMap<String, String> getCameraList(Wrapper w, String project)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = cameraStartPattern(project);
      ArrayList<String> list = Globals.getAllNodes(w, start);
      for (String s : list)
      {
	 Path p = new Path(s);
	 String key = p.getName();
	 while ( toReturn.containsKey(key) )
	 {
	    key += "-";
	 }
	 toReturn.put(key, s);
      }
      return toReturn;
   }
 
   public static TreeMap<String, String> getLightsList(Wrapper w, String project)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = lightStartPattern(project);
      ArrayList<String> list = Globals.getAllNodes(w, start);
      for (String s : list)
      {
	 Path p = new Path(s);
	 String key = p.getName();
	 while ( toReturn.containsKey(key) )
	 {
	    key += "-";
	 }
	 toReturn.put(key, s);
      }
      return toReturn;
   }

   public static TreeMap<String, String> getCustomNamespaces(String project)
   {
      if ( project.equals("lr") )
	 return getLairNamespaces();
      return null;
   }

   private static TreeMap<String, String> getLairNamespaces()
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();

      toReturn.put("rohn", "rh");
      toReturn.put("hybridRohn", "hrh");
      toReturn.put("nakedRohn", "nrh");
      toReturn.put("loden", "ld");
      toReturn.put("jevon", "jv");
      toReturn.put("talan", "tl");
      toReturn.put("attakai", "at");
      toReturn.put("attakai2", "at2x");
      toReturn.put("kobakai", "kb");
      toReturn.put("kobakai2", "kb2x");
      toReturn.put("renkai", "rk");
      toReturn.put("renkai2", "rk2x");
      toReturn.put("guardian1", "gd1x");
      toReturn.put("guardian2", "gd2x");
      toReturn.put("guardian3", "gd3x");
      toReturn.put("prophet", "ph");
      toReturn.put("burntWoman", "bw");
      toReturn.put("burntBaby", "bb");
      toReturn.put("burntMan", "bm");
      toReturn.put("burntMan1", "bm1x");
      toReturn.put("burntMan2", "bm2x");
      toReturn.put("burntMan3", "bm3x");
      toReturn.put("burntMan4", "bm4x");
      toReturn.put("burntMan5", "bm5x");
      toReturn.put("burntMan6", "bm6x");
      toReturn.put("burntMan7", "bm7x");
      toReturn.put("burntMan8", "bm8x");
      toReturn.put("burntMan9", "bm9x");
      toReturn.put("burntMan10", "bm10x");
      toReturn.put("plainsDragon", "pd");
      toReturn.put("plainsDragon2", "pd2x");
      toReturn.put("plainsDragon3", "pd3x");
      toReturn.put("plainsDragon4", "pd4x");
      toReturn.put("plainsDragon5", "pd5x");
      toReturn.put("dragonX", "dx");
      toReturn.put("spear", "sr");
      toReturn.put("stryker", "sk");
      toReturn.put("strykerChain", "skc");
      toReturn.put("staff", "st");
      toReturn.put("asyTemple", "ast");
      toReturn.put("asyTempleRuins", "atr");
      toReturn.put("mokTemple", "mt");
      toReturn.put("mokTempleBurned", "mtb");
      toReturn.put("stables", "sb");
      toReturn.put("skyDome", "sd");
      toReturn.put("desert", "dd");
      toReturn.put("armorPieces", "ap");
      toReturn.put("asylianArrow", "aa");
      toReturn.put("asylianArrow2", "aa2x");
      toReturn.put("asylianBlade", "ab");
      toReturn.put("lodenHelmet", "lh");
      toReturn.put("asylianHelmet", "ah");
      toReturn.put("asylianHelmet2", "ah2x");
      toReturn.put("asylianHelmet3", "ah3x");
      toReturn.put("asylianHelmet4", "ah4x");
      toReturn.put("asylianHelmet5", "ah5x");
      toReturn.put("asylianHelmet6", "ah6x");
      toReturn.put("asylianHelmet7", "ah7x");
      toReturn.put("asylianHelmet8", "ah8x");
      toReturn.put("asylianHelmet9", "ah9x");
      toReturn.put("asylianHelmet10", "ah10x");
      toReturn.put("asylianSpear", "as");
      toReturn.put("asylianStryker", "ask");
      toReturn.put("chains", "ch");
      toReturn.put("chair", "cr");
      toReturn.put("mokaiHelmet", "mh");
      toReturn.put("mokaiSpear", "ms");
      toReturn.put("rohnCircleArt", "rca");
      toReturn.put("rohnStick", "rs");
      toReturn.put("saddleStraps", "ss");
      toReturn.put("table", "tb");
      toReturn.put("bridgeTent", "bt");
      toReturn.put("ramparts", "rm");
      toReturn.put("powCamp", "pow");
      toReturn.put("refugeeCamp", "rc");
      toReturn.put("slotCanyon", "sc");
      toReturn.put("smallTent", "smt");
      toReturn.put("mokaiStryker", "mk");
      toReturn.put("burner1", "bn1x");
      toReturn.put("burner2", "bn2x");
      toReturn.put("burner3", "bn3x");
      toReturn.put("burner4", "bn4x");
      toReturn.put("burner5", "bn5x");
      toReturn.put("burner6", "bn6x");
      toReturn.put("burner7", "bn7x");
      toReturn.put("burner8", "bn8x");
      toReturn.put("burner9", "bn9x");
      toReturn.put("burner10", "bn10x");
      toReturn.put("flyingDragon1", "fd1x");
      toReturn.put("flyingDragon2", "fd2x");
      toReturn.put("flyingDragon3", "fd3x");
      toReturn.put("flyingDragon4", "fd4x");
      toReturn.put("flyingDragon5", "fd5x");
      toReturn.put("flyingDragon6", "fd6x");
      toReturn.put("flyingDragon7", "fd7x");
      toReturn.put("flyingDragon8", "fd8x");
      toReturn.put("flyingDragon9", "fd9x");
      toReturn.put("flyingDragon10", "fd10x");
      toReturn.put("torch", "tch");
      toReturn.put("bone", "bn");
      toReturn.put("dragonCollar", "dc");
      toReturn.put("ashPile", "asp");
      toReturn.put("ashWomanSkull", "aws");
      toReturn.put("ashBabySkull", "abs");
      toReturn.put("skeleton", "skl");
      toReturn.put("brokenChain", "bch");
      toReturn.put("log", "lg");
      
      return toReturn;
   }

   private static final String PATH_globalMelPath = "/global/assets/tools/mel";
   private static final String PATH_projectMelEndPath = "assets/tools/mel";
   private static final String charPattern = "/assets/character/";
   private static final String setPattern = "/assets/set/";
   private static final String propPattern = "/assets/prop/";

}

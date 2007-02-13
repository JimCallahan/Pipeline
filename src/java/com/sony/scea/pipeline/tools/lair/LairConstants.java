package com.sony.scea.pipeline.tools.lair;

import java.util.*;

import com.sony.scea.pipeline.tools.Wrapper;
import com.sony.scea.pipeline.tools.Globals;
import com.sony.scea.pipeline.tools.Globals.NodeLocation;

import us.temerity.pipeline.*;


public class LairConstants
{
   public static final String MEL_finalizeCharacter = "/projects/lr/assets/tools/mel/finalize-character";
   public static final String MEL_charPlaceholder = "/projects/lr/assets/tools/mel/character-placeholder";
   public static final String MEL_importTurn = "/projects/lr/assets/tools/mel/turntable-import";
   public static final String MEL_loadMRay = "/projects/lr/assets/tools/mel/mr-init";
   public static final String MEL_crunch = "/projects/lr/assets/tools/mel/crunch";

   public static final String PATH_baseTurntables = "/projects/lr/assets/tt/setups/";
   public static final String PATH_baseTurntableOptions = "/projects/lr/assets/tt/opt/";

   private static final PluginMgrClient plug = PluginMgrClient.getInstance();

   public static BaseAction actionMayaMiExport() throws PipelineException
   {
      return plug.newAction("MayaMiExport", new VersionID("2.0.11"), "Temerity");
   }

   public static BaseAction actionMayaMiShader() throws PipelineException
   {
      return plug.newAction("MayaMiShader", new VersionID("2.0.13"), "Temerity");
   }

   public static BaseAction actionTouch() throws PipelineException
   {
      return plug.newAction("Touch", new VersionID("2.0.0"), "Temerity");
   }

   public static BaseAction actionMayaReference() throws PipelineException
   {
      return plug.newAction("MayaReference", new VersionID("2.0.11"), "Temerity");
   }

   public static BaseAction actionMRayShaderInclude() throws PipelineException
   {
      return plug.newAction("MRayShaderInclude", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseAction actionMayaAnimExport() throws PipelineException
   {
      return plug.newAction("MayaAnimExport", new VersionID("2.0.12"), "Temerity");
   }

   public static BaseAction actionMRayRender() throws PipelineException
   {
      return plug.newAction("MRayRender", new VersionID("2.0.10"), "Temerity");
   }

   public static BaseAction actionCatFiles() throws PipelineException
   {
      return plug.newAction("CatFiles", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseAction actionMRayCamOverride() throws PipelineException
   {
      return plug.newAction("MRayCamOverride", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseAction actionMayaImport() throws PipelineException
   {
      return plug.newAction("MayaImport", new VersionID("2.0.11"), "Temerity");
   }

   public static BaseAction actionListSources() throws PipelineException
   {
      return plug.newAction("List Sources", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseAction actionMRayInstGroup() throws PipelineException
   {
      return plug.newAction("MRayInstGroup", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseAction actionMayaCollate() throws PipelineException
   {
      return plug.newAction("MayaCollate", new VersionID("2.0.12"), "Temerity");
   }

   public static BaseAction actionMayaShaderExport() throws PipelineException
   {
      return plug.newAction("MayaShaderExport", new VersionID("2.0.10"), "Temerity");
   }

   public static BaseEditor editorMaya() throws PipelineException
   {
      return plug.newEditor("MayaProject", new VersionID("2.0.10"), "Temerity");
   }

   public static BaseEditor editorSciTE() throws PipelineException
   {
      return plug.newEditor("SciTE", new VersionID("2.0.10"), "Temerity");
   }

   public static BaseEditor editorFCheck() throws PipelineException
   {
      return plug.newEditor("FCheck", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseEditor editorKWrite() throws PipelineException
   {
      return plug.newEditor("KWrite", new VersionID("2.0.9"), "Temerity");
   }

   public static BaseEditor editorEmacs() throws PipelineException
   {
      return plug.newEditor("Emacs", new VersionID("2.0.10"), "Temerity");
   }

   public static ArrayList<String> getShotPasses(Wrapper w, LairShot sh)
      throws PipelineException
   {
      ArrayList<String> toReturn = new ArrayList<String>();
      String start = sh.shotStart + "mi/com/";
      toReturn = Globals.getChildrenDirs(w, start);
      return toReturn;
   }

   public static ArrayList<String> getAllPasses(Wrapper w)
      throws PipelineException
   {
      ArrayList<String> toReturn = new ArrayList<String>();
      toReturn = Globals.getChildrenNodes(w, PATH_baseTurntableOptions);
      return toReturn;
   }

   public static TreeMap<String, LinkedList<LairAsset>> getAssetGroupings(Wrapper w,
	 LairShot sh) throws PipelineException
   {
      TreeMap<String, LinkedList<LairAsset>> toReturn = new TreeMap<String, LinkedList<LairAsset>>();
      String imgStart = sh.shotStart + "img/";
      ArrayList<String> children = Globals.getChildrenNodes(w, imgStart);
      for (LairAsset as : sh.assets)
      {
	 if ( children.contains(as.assetName) )
	    children.remove(as.assetName);
      }
      if ( children.contains("com") )
	 children.remove("com");
      if ( children.size() > 0 )
      {
	 String shotPrefix = sh.seqName + "_" + sh.shotName + "_";
	 for (String group : children)
	 {
	    for (String pass : sh.passes)
	    {
	       String dImageMI = imgStart + group + "/" + pass + "/" + shotPrefix + group
		     + "_" + pass;
	       if ( Globals.doesNodeExists(w, dImageMI) )
	       {
		  NodeCommon com = null;
		  NodeLocation loc = Globals.getNodeLocation(w, sh.animScene);
		  switch (loc)
		  {
		     case LOCAL:
			com = w.mclient.getWorkingVersion(w.user, w.view, dImageMI);
			break;
		     case REP:
			TreeMap<VersionID, NodeVersion> versions = w.mclient
			   .getAllCheckedInVersions(sh.animScene);
			com = versions.get(versions.lastKey());
			break;
		     default:
			throw new PipelineException("The animation node doesn't exist "
			      + "anywhere you can get at it.");
		  }

		  LinkedList<LairAsset> assets = new LinkedList<LairAsset>();
		  for (String source : com.getSourceNames())
		  {
		     Path p = new Path(source);
		     String name = p.getName();
		     if ( name.equals("modInst") )
		     {
			ArrayList<String> temp = p.getComponents();
			String asset = temp.get(temp.size() - 3);
			AssetType type = getAssetType(w, asset);
			assets.add(new LairAsset(asset, type));
		     }
		  }
		  toReturn.put(group, assets);
		  break;
	       }
	    }
	 }
      }
      return toReturn;
   }

   public static AssetType getAssetType(Wrapper w, String assetName)
      throws PipelineException
   {
      if ( getLairList(w, AssetType.CHARACTER).keySet().contains(assetName) )
	 return AssetType.CHARACTER;
      if ( getLairList(w, AssetType.SET).keySet().contains(assetName) )
	 return AssetType.SET;
      if ( getLairList(w, AssetType.PROP).keySet().contains(assetName) )
	 return AssetType.PROP;
      return null;
   }

   public static LairAsset stringToAsset(Wrapper w, String name) throws PipelineException
   {
      LairAsset as;
      if ( Globals.doesNodeExists(w, name) )
      {
	 Path p = new Path(name);
	 String assetName = p.getParentPath().getName();
	 AssetType assetType = AssetType.fromString(p.getParentPath().getParentPath()
	    .getName());
	 as = new LairAsset(assetName, assetType);
      } else
	 throw new PipelineException("There is no node with the name (" + name + ")");
      return as;
   }

   public static ArrayList<LairAsset> getAssetValues(String arg, boolean required,
	 TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
   {
      LinkedList<String> values = parsedArgs.remove(arg);
      if ( values == null )
	 if ( required )
	    throw new PipelineException("ERROR: You must have at least one instance "
		  + "of the --" + arg + " flag.");

      ArrayList<LairAsset> toReturn = new ArrayList<LairAsset>();
      if ( values != null )
      {
	 for (String each : values)
	 {
	    String buffer[] = each.split(",");
	    if ( buffer.length != 2 )
	       throw new PipelineException("Improper use of the --" + arg
		     + " flag with a value of " + each);
	    String name = buffer[0];
	    AssetType type = AssetType.fromString(buffer[1]);
	    toReturn.add(new LairAsset(name, type));
	 }
      }
      return toReturn;
   }

   public static TreeMap<String, LinkedList<LairAsset>> getAssetGroupingValues(String arg,
	 boolean required, TreeMap<String, LinkedList<String>> parsedArgs)
      throws PipelineException
   {
      LinkedList<String> values = parsedArgs.remove(arg);
      if ( values == null )
	 if ( required )
	    throw new PipelineException("ERROR: You must have at least one instance "
		  + "of the --" + arg + " flag.");

      TreeMap<String, LinkedList<LairAsset>> toReturn = new TreeMap<String, LinkedList<LairAsset>>();
      if ( values != null )
      {
	 for (String each : values)
	 {
	    String buffer[] = each.split(":");
	    if ( buffer.length != 2 )
	       throw new PipelineException("Improper use of the --" + arg
		     + " flag with a value of " + each);
	    String groupName = buffer[0];
	    LinkedList<LairAsset> list = new LinkedList<LairAsset>();
	    buffer = buffer[1].split("-");
	    for (String each2 : buffer)
	    {
	       String buffer2[] = each2.split(",");
	       if ( buffer.length != 2 )
		  throw new PipelineException("Improper use of the --" + arg
			+ " flag with a value of " + each2);
	       String name = buffer2[0];
	       AssetType type = AssetType.fromString(buffer2[1]);
	       list.add(new LairAsset(name, type));
	    }
	    toReturn.put(groupName, list);
	 }
      }
      return toReturn;
   }

   public static final String cameraPatternStart = "/projects/lr/assets/camera/";
   public static final String lightsPatternStart = "/projects/lr/assets/lights/";
   public static final String charPatternStart = "/projects/lr/assets/character/";
   public static final String setPatternStart = "/projects/lr/assets/set/";
   public static final String propPatternStart = "/projects/lr/assets/prop/";

   public static TreeMap<String, String> getLairList(Wrapper w, AssetType type)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = null;
      switch (type)
      {
	 case CHARACTER:
	    start = charPatternStart;
	    break;
	 case SET:
	    start = setPatternStart;
	    break;
	 case PROP:
	    start = propPatternStart;
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

   public static TreeMap<String, String> getLairCameraList(Wrapper w)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = cameraPatternStart;
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

   public static TreeMap<String, String> getLairLightsList(Wrapper w)
      throws PipelineException
   {
      TreeMap<String, String> toReturn = new TreeMap<String, String>();
      String start = lightsPatternStart;
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

   public static enum AssetType
   {
      CHARACTER, PROP, SET;

      @Override
      public String toString()
      {
	 String toReturn = null;
	 switch (this)
	 {
	    case CHARACTER:
	       toReturn = "character";
	       break;
	    case PROP:
	       toReturn = "prop";
	       break;
	    case SET:
	       toReturn = "set";
	       break;
	 }
	 return toReturn;
      }
      
      public static String[] stringValues()
      {
	 AssetType v[] = values();
	 String toReturn[] = new String[v.length];
	 for (int i = 0; i < v.length; i++)
	    toReturn[i] = v[i].toString();
	 return toReturn;
      }

      /**
       * A method for converting a String into an AssetType. Throws a
       * {@link PipelineException PipelineException} when an invalid string is
       * passed in.
       * 
       * <P>
       * 
       * @param s
       *           The String to convert to an AssetType
       * @return The AssetType that corresponds to <code>s</code>.
       * @throws PipelineException
       * 
       */
      public static AssetType fromString(String s) throws PipelineException
      {
	 AssetType toReturn = null;
	 s = s.toLowerCase();
	 if ( s.equals("character") )
	    toReturn = AssetType.CHARACTER;
	 else if ( s.equals("prop") )
	    toReturn = AssetType.PROP;
	 else if ( s.equals("set") )
	    toReturn = AssetType.SET;
	 else
	    throw new PipelineException("The given type (" + s + ") does "
		  + "not match any of the known asset types");
	 return toReturn;
      }

   }
}

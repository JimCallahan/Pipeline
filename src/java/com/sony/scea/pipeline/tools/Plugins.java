package com.sony.scea.pipeline.tools;

import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Just a bunch of utility methods to return the latest version of an action
 * contained in a toolset.
 * <p>
 * Most of the methods in here are utility methods that exist to support old code.
 * All new code using these methods is written using the getAction and getEditor
 * code.  All the older methods here were retrofitted to use them as well.
 * @author Jesse Clemens
 *
 */
public class Plugins
{
   private static final PluginMgrClient plug = PluginMgrClient.getInstance();

   public static BaseAction getAction(Wrapper w, String vendor, String name)
      throws PipelineException
   {
      DoubleMap<String, String, TreeSet<VersionID>> plugs = w.mclient
	 .getToolsetActionPlugins(w.toolset);
      VersionID ver = plugs.get(vendor, name).last();

      return plug.newAction(name, ver, vendor);
   }

   public static BaseEditor getEditor(Wrapper w, String vendor, String name)
      throws PipelineException
   {
      DoubleMap<String, String, TreeSet<VersionID>> plugs = w.mclient
	 .getToolsetEditorPlugins(w.toolset);
      VersionID ver = plugs.get(vendor, name).last();

      return plug.newEditor(name, ver, vendor);
   }

   public static BaseAction actionMayaMiExport(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaMiExport");
   }

   public static BaseAction actionMayaMiShader(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaMiShader");
   }
   
   public static BaseAction actionModelReplace(Wrapper w) throws PipelineException
   {
      return getAction(w, "SCEA", "ModelReplace");
   }

   public static BaseAction actionTouch(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "Touch");
   }

   public static BaseAction actionMayaReference(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaReference");
   }
   
   public static BaseAction actionMayaBuild(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaBuild");
   }
   
   public static BaseAction actionMRayShaderInclude(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MRayShaderInclude");
   }

   public static BaseAction actionMayaAnimExport(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaAnimExport");
   }

   public static BaseAction actionMRayRender(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MRayRender");
   }

   public static BaseAction actionCatFiles(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "CatFiles");
   }

   public static BaseAction actionMRayCamOverride(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MRayCamOverride");
   }

   public static BaseAction actionMayaImport(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaImport");
   }

   public static BaseAction actionListSources(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "List Sources");
   }

   public static BaseAction actionMRayInstGroup(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MRayInstGroup");
   }

   public static BaseAction actionMayaCollate(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaCollate");
   }

   public static BaseAction actionMayaShaderExport(Wrapper w) throws PipelineException
   {
      return getAction(w, "Temerity", "MayaShaderExport");
   }

   public static BaseEditor editorMaya(Wrapper w) throws PipelineException
   {
      return getEditor(w, "Temerity", "MayaProject");
   }

   public static BaseEditor editorSciTE(Wrapper w) throws PipelineException
   {
      return getEditor(w, "Temerity", "SciTE");
   }

   public static BaseEditor editorFCheck(Wrapper w) throws PipelineException
   {
      return getEditor(w, "Temerity", "FCheck");
   }

   public static BaseEditor editorKWrite(Wrapper w) throws PipelineException
   {
      return getEditor(w, "Temerity", "KWrite");
   }

   public static BaseEditor editorEmacs(Wrapper w) throws PipelineException
   {
      return getEditor(w, "Temerity", "Emacs");
   }
}

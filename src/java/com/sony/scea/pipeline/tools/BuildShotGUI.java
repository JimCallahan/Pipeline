package com.sony.scea.pipeline.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;
import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.ui.*;

import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

public class BuildShotGUI extends BootApp implements ActionListener
{

   MasterMgrClient client;
   PluginMgrClient plug;
   QueueMgrClient queue;
   LogMgr log;

   public BuildShotGUI()
   {
      try
      {
	 PluginMgrClient.init();
	 client = new MasterMgrClient();
	 queue = new QueueMgrClient();
	 plug = PluginMgrClient.getInstance();
	 log = LogMgr.getInstance();
	 verbose = true;
	 selectionKeyFields = new TreeMap<String, JBooleanField>();
	 sequenceFields = new ArrayList<JCollectionField>();
	 sequenceBooleanFields = new ArrayList<JBooleanField>();
	 sequenceNameFields = new ArrayList<JTextField>();
	 shotNameFields = new ArrayList<JTextField>();
	 shotStartFields = new ArrayList<JIntegerField>();
	 shotEndFields = new ArrayList<JIntegerField>();
	 shotAssetMappings = new DoubleMap<Integer, String, JBooleanField>();
	 shotCameraMappings = new DoubleMap<Integer, String, JBooleanField>();
	 advancedRenderFields = new ArrayList<JBooleanField>();
	 validatedShots = new ArrayList<Integer>();
	 shotMiscMappings = new DoubleMap<Integer, String, JBooleanField>();
	 shotMiscTypeMappings = new DoubleMap<Integer, String, JCollectionField>();

	 usedKeys = new TreeSet<String>();

	 /* load the look-and-feel */
	 {
	    try
	    {
	       SynthLookAndFeel synth = new SynthLookAndFeel();
	       synth.load(LookAndFeelLoader.class.getResourceAsStream("synth.xml"),
		  LookAndFeelLoader.class);
	       UIManager.setLookAndFeel(synth);
	    } catch ( java.text.ParseException ex )
	    {
	       log.log(LogMgr.Kind.Ops, LogMgr.Level.Severe,
		  "Unable to parse the look-and-feel XML file (synth.xml):\n" + "  "
			+ ex.getMessage());
	       System.exit(1);
	    } catch ( UnsupportedLookAndFeelException ex )
	    {
	       log.log(LogMgr.Kind.Ops, LogMgr.Level.Severe,
		  "Unable to load the Pipeline look-and-feel:\n" + "  " + ex.getMessage());
	       System.exit(1);
	    }
	 }

	 /* application wide UI settings */
	 {
	    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
	 }
      } catch ( PipelineException ex )
      {
	 ex.printStackTrace();
      }
   }

   public static void main(String[] args)
   {
      BuildShotGUI gui = new BuildShotGUI();
      try
      {
	 boolean here = gui.buildGUI();
	 if ( here == false )
	    System.exit(0);
	 else
	 {

	    boolean here2 = gui.validateEntries();
	    if ( here2 )
	       gui.processShots();
	 }
	 System.exit(0);
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
	 System.exit(1);
      }
   }

   private void processShots() throws PipelineException
   {
      int count = shotNameFields.size();

      for (int i = 0; i < count; i++)
      {
	 if ( validatedShots.contains(i) )
	 {
	    try
	    {
	       buildShot(i);
	    } catch ( PipelineException e )
	    {
	       e.printStackTrace();
	    }
	 } else
	 {
	    JTextField shotNameField = shotNameFields.get(i);
	    JBooleanField sequenceBooleanField = sequenceBooleanFields.get(i);
	    String seq;
	    if ( sequenceBooleanField.getValue() )
	       seq = sequenceNameFields.get(i).getText();
	    else
	       seq = sequenceFields.get(i).getSelected();

	    String shotName = shotNameField.getText();
	    log.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Warning, "Could not create "
		  + seq + "_" + shotName);
	 }
      }
   }

   private boolean validateEntries() throws PipelineException
   {
      user = userField.getSelected();
      view = viewField.getSelected();
      project = projectField.getSelected();
      movie = getMovie();

      int count = shotNameFields.size();
      for (int i = 0; i < count; i++)
      {
	 String seq = getSequence(i);
	 String shot = shotNameFields.get(i).getText();
	 SonyShot sh = new SonyShot(project, movie, seq, shot, 1, null, null, null);
	 if ( !Globals.doesNodeExists(w, sh.animScene) && !shot.equals("") )
	    validatedShots.add(i);
      }

      for (int num : validatedShots)
      {
	 boolean temp = assetSelection(num);
	 if ( !temp )
	    return temp;
      }

      for (String key : selectionKeyFields.keySet())
      {
	 if ( selectionKeyFields.get(key).getValue() )
	    usedKeys.add(key);
      }
      return true;
   }

   private boolean assetSelection(int num) throws PipelineException
   {
      TreeMap<String, String> chars = SonyConstants.getAssetList(w, project,
	 AssetType.CHARACTER);
      TreeMap<String, String> sets = SonyConstants.getAssetList(w, project, AssetType.SET);
      TreeMap<String, String> props = SonyConstants
	 .getAssetList(w, project, AssetType.PROP);
      TreeMap<String, String> cameras = SonyConstants.getCameraList(w, project);
      TreeMap<String, String> misc = SonyConstants.getMiscAssets(w, project);

      String seq = getSequence(num);
      String shot = shotNameFields.get(num).getText();

      {
	 JPanel masterPanel = new JPanel();
	 masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
	 masterPanel.add(UIFactory.createPanelLabel(movie + "_" + seq + "_" + shot));

	 JPanel topPanel = new JPanel();
	 topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
	 topPanel.add(Box.createHorizontalStrut(4));

	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Characters:"));
	    inside.add(buildInsidePane(num, chars, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Sets:"));
	    inside.add(buildInsidePane(num, sets, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Props:"));
	    inside.add(buildInsidePane(num, props, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Cameras:"));
	    inside.add(buildInsidePane(num, cameras, shotCameraMappings));
	    topPanel.add(inside);
	 }
	 masterPanel.add(topPanel);
	 JToolDialog diag1 = new JToolDialog("Select Assets", masterPanel, "Continue");
	 diag1.pack();

	 diag1.setVisible(true);
	 if ( !diag1.wasConfirmed() )
	    return false;
      }
      {
	 JPanel masterPanel = new JPanel();
	 masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
	 masterPanel.add(UIFactory.createPanelLabel(movie + "_" + seq + "_" + shot));

	 JPanel topPanel = new JPanel();
	 topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
	 topPanel.add(Box.createHorizontalStrut(4));

	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Misc:"));
	    inside.add(buildInsideMiscPane(num, misc));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 masterPanel.add(topPanel);
	 JToolDialog diag1 = new JToolDialog("Select Misc Assets", masterPanel, "Continue");
	 diag1.pack();

	 diag1.setVisible(true);
	 if ( diag1.wasConfirmed() )
	    return true;
	 return false;

      }
   }

   private JScrollPane buildInsidePane(int num, TreeMap<String, String> map,
	 DoubleMap<Integer, String, JBooleanField> store)
   {
      Box finalBox = new Box(BoxLayout.Y_AXIS);

      JScrollPane scroll;

      {
	 scroll = new JScrollPane(finalBox);

	 scroll
	    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	 scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	 Dimension size = new Dimension(sTSize + sVSize + 52, 500);
	 scroll.setMinimumSize(size);
	 scroll.setPreferredSize(size);

	 scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
      {
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 for (String prefix : map.keySet())
	 {
	    String longName = map.get(prefix);
	    JBooleanField field = UIFactory.createTitledBooleanField(tpanel, prefix + ":",
	       sTSize, vpanel, sVSize, longName);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    field.setValue(false);
	    store.put(num, prefix, field);
	 }
	 finalBox.add(comps[2]);
      }
      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

	 finalBox.add(spanel);
      }

      return scroll;
   }

   private JScrollPane buildInsideMiscPane(int num, TreeMap<String, String> map)
   {
      Box finalBox = new Box(BoxLayout.Y_AXIS);

      JScrollPane scroll;

      {
	 scroll = new JScrollPane(finalBox);

	 scroll
	    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	 scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	 Dimension size = new Dimension(sTSize + sVSize + 52, 500);
	 scroll.setMinimumSize(size);
	 scroll.setPreferredSize(size);

	 scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }
      {
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 for (String prefix : map.keySet())
	 {
	    String longName = map.get(prefix);
	    {
	       JBooleanField field = UIFactory.createTitledBooleanField(tpanel, prefix
		     + ":", sTSize, vpanel, sVSize, longName);
	       UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	       field.setValue(false);
	       shotMiscMappings.put(num, prefix, field);
	    }
	    {
	       TreeSet<String> list = new TreeSet<String>();
	       list.add("Reference");
	       list.add("Import");
	       JCollectionField field = UIFactory.createTitledCollectionField(tpanel,
		  "Build Type:", sTSize, vpanel, list, diag, sVSize,
		  "Are you importing or referencing the misc model.");
	       field.setSelected("Reference");
	       shotMiscTypeMappings.put(num, prefix, field);
	    }
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 8);

	 }
	 finalBox.add(comps[2]);
      }
      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

	 finalBox.add(spanel);
      }

      return scroll;
   }

   @SuppressWarnings("unused")
   public void run(String[] args)
   {
      try
      {
	 boolean here = buildGUI();
	 if ( !here )
	    System.exit(0);
	 else
	 {
	    boolean here2 = validateEntries();
	    if ( here2 )
	       processShots();
	 }
	 System.exit(0);
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
      }
   }

   private void buildShot(int num) throws PipelineException
   {
      String seq = getSequence(num);
      String shot = shotNameFields.get(num).getText();

      int start = shotStartFields.get(num).getValue();
      int end = shotEndFields.get(num).getValue();

      boolean buildPreLight = buildPrelightField.getValue();
      boolean buildSwitch = buildModelReplaceField.getValue();
      boolean buildLight = buildLightField.getValue();
      boolean buildTestImages = buildTestImagesField.getValue();
      MissingModel missingModelSetting = MissingModel.fromString(missingModelField
	 .getSelected());
      boolean advancedRender = advancedRenderFields.get(num).getValue();
      String modelType = modelTypeField.getSelected();
      boolean hirez = false;
      if ( modelType.equals("Hi-Rez") )
	 hirez = true;
      String cameraType = cameraTypeField.getSelected();

      ArrayList<SonyAsset> assets = new ArrayList<SonyAsset>();
      ArrayList<String> cameras = new ArrayList<String>();
      ArrayList<String> misc = new ArrayList<String>();
      TreeMap<String, String> miscType = new TreeMap<String, String>();
      TreeMap<String, JBooleanField> map = shotAssetMappings.get(num);
      TreeMap<String, JBooleanField> camMap = shotCameraMappings.get(num);
      TreeMap<String, JBooleanField> miscMap = shotMiscMappings.get(num);
      TreeMap<String, JCollectionField> miscTypeMap = shotMiscTypeMappings.get(num);
      TreeMap<String, String> assetLookup = SonyConstants.getAllAssetsMap(w, project);
      TreeMap<String, String> cameraLookup = SonyConstants.getCameraList(w, project);
      TreeMap<String, String> miscLookup = SonyConstants.getMiscAssets(w, project);
      TreeMap<String, Boolean> altModelType = new TreeMap<String, Boolean>();
      for (String name : map.keySet())
      {
	 if ( map.get(name).getValue() )
	 {
	    SonyAsset as = SonyConstants.stringToAsset(w, assetLookup.get(name));
	    String check = as.lr_finalScene;
	    String other = as.finalScene;
	    if ( hirez )
	    {
	       check = as.finalScene;
	       other = as.lr_finalScene;
	    }
	    if ( !Globals.doesNodeExists(w, check) )
	    {
	       switch (missingModelSetting)
	       {
		  case Abort:
		     logLine("The Model " + check + " does not exist. Aborting");
		     return;
		  case Ignore:
		     break;
		  case SubstituteOrAbort:
		     if ( !Globals.doesNodeExists(w, other) )
		     {
			logLine("The Model " + check + " does not exist. Aborting");
			return;
		     }
		     assets.add(as);
		     altModelType.put(as.assetName, true);
		     break;
		  case SubstituteOrIgnore:
		     if ( Globals.doesNodeExists(w, other) )
		     {
			assets.add(as);
			altModelType.put(as.assetName, true);
		     }
		     break;
	       }
	    } else
	       assets.add(as);
	 }
      } // for (String name : map.keySet())

      for (String name : camMap.keySet())
      {
	 if ( camMap.get(name).getValue() )
	 {
	    cameras.add(cameraLookup.get(name));
	 }
      }

      for (String name : miscMap.keySet())
      {
	 if ( miscMap.get(name).getValue() )
	 {
	    misc.add(miscLookup.get(name));
	    miscType.put(miscLookup.get(name), miscTypeMap.get(name).getSelected());
	 }
      }

      SonyShot sh = new SonyShot(project, movie, seq, shot, end - start + 1, assets, null,
	 null);

      TreeSet<String> addedNodes = new TreeSet<String>();
      try
      {
	 for (String cameraName : cameras)
	 {
	    logLine("Getting the camera: " + cameraName);
	    Globals.getNewest(w, cameraName, keep, froz);
	 }

	 for (String miscName : misc)
	 {
	    logLine("Getting misc node: " + miscName);
	    Globals.getNewest(w, miscName, keep, froz);
	 }

	 TreeMap<String, String> customNamespaces = null;
	 {
	    customNamespaces = SonyConstants.getCustomNamespaces(project);
	    if ( customNamespaces == null )
	    {
	       customNamespaces = new TreeMap<String, String>();
	       for (SonyAsset as : assets)
		  customNamespaces.put(as.assetName, as.assetName);
	    } else
	    {
	       for (SonyAsset as : assets)
		  if ( !customNamespaces.containsKey(as.assetName) )
		     customNamespaces.put(as.assetName, as.assetName);
	    }
	 }

	 log(sh.animScene + " : ");
	 if ( !Globals.doesNodeExists(w, sh.animScene) )
	 { // AnimShot
	    logLine("Building");
	    NodeMod mod = Globals
	       .registerNode(w, sh.animScene, "ma", Plugins.editorMaya(w));
	    addedNodes.add(sh.animScene);
	    BaseAction act = Plugins.actionMayaBuild(w);

	    for (SonyAsset as : assets)
	    {
	       logLine("\tDoing asset: " + as.assetName);
	       boolean alt = false;
	       if ( altModelType.containsKey(as.assetName) )
		  alt = true;
	       if ( ( hirez && !alt ) || ( !hirez && alt ) )
	       {
		  Globals.getNewest(w, as.finalScene, over, frozU);
		  Globals.referenceNode(w, sh.animScene, as.finalScene, act, REF,
		     customNamespaces.get(as.assetName));
		  act.setSourceParamValue(as.finalScene, "BuildType", "Reference");
	       } else
	       {
		  Globals.getNewest(w, as.lr_finalScene, over, frozU);
		  Globals.referenceNode(w, sh.animScene, as.lr_finalScene, act, REF,
		     customNamespaces.get(as.assetName));
		  act.setSourceParamValue(as.lr_finalScene, "BuildType", "Reference");
	       }
	    }
	    for (String cameraName : cameras)
	    {
	       String space = new Path(cameraName).getName();
	       if ( cameraType.equals("Reference") )
		  Globals.referenceNode(w, sh.animScene, cameraName, act, REF, space);
	       else
		  Globals.referenceNode(w, sh.animScene, cameraName, act, DEP, space);
	       act.setSourceParamValue(cameraName, "BuildType", cameraType);
	    }
	    for (String miscName : misc)
	    {
	       String space = new Path(miscName).getName();
	       String type = miscType.get(miscName);
	       if ( type.equals("Reference") )
	       {
		  Globals.referenceNode(w, sh.animScene, miscName, act, REF, space);
	       } else
	       {
		  Globals.referenceNode(w, sh.animScene, miscName, act, DEP, space);
	       }
	       act.setSourceParamValue(miscName, "BuildType", type);
	    }

	    act.setSingleParamValue("StartFrame", start);
	    act.setSingleParamValue("EndFrame", start);
	    act.setSingleParamValue("TimeUnits", "NTSC (30 fps)");

	    mod.setAction(act);
	    doReqs(mod);
	    client.modifyProperties(user, view, mod);
	 } else
	    logLine("Already Exists");

	 if ( advancedRender && ( buildLight || buildPreLight ) )
	 {
	    log(sh.lightShaderDefsMI + " : ");
	    if ( !Globals.doesNodeExists(w, sh.lightShaderDefsMI) )
	    { // Shader Defs
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, sh.lightShaderDefsMI, "mi", Plugins
		  .editorEmacs(w));
	       addedNodes.add(sh.lightShaderDefsMI);
	       BaseAction act = Plugins.actionCatFiles(w);
	       for (SonyAsset as : assets)
	       {
		  client.link(user, view, sh.lightShaderDefsMI, as.shdIncGroup, DEP,
		     LINKALL, null);
		  act.initSecondarySourceParams(as.shdIncGroup, as.shdIncGroupSecSeq
		     .getFilePattern());
		  act.setSecondarySourceParamValue(as.shdIncGroup, as.shdIncGroupSecSeq
		     .getFilePattern(), "Order", 100);
	       }
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	 }

	 TreeMap<String, FileSeq> actualSeqs = new TreeMap<String, FileSeq>();
	 if ( buildPreLight || advancedRender )
	 {
	    log(sh.animExportGroup + " : ");
	    if ( !Globals.doesNodeExists(w, sh.animExportGroup) )
	    { // AnimExport Shot
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, sh.animExportGroup, null, Plugins
		  .editorKWrite(w));
	       addedNodes.add(sh.animExportGroup);
	       for (SonyAsset as : assets)
	       {
		  String prefix = customNamespaces.get(as.assetName);
		  FileSeq fSeq = new FileSeq(prefix, "anim");
		  mod.addSecondarySequence(fSeq);
		  actualSeqs.put(as.assetName, fSeq);
	       }
	       for (String cameraName : cameras)
	       {
		  String prefix = new Path(cameraName).getName();
		  FileSeq fSeq = new FileSeq(prefix, "anim");
		  mod.addSecondarySequence(fSeq);
	       }
	       client
		  .link(user, view, sh.animExportGroup, sh.animScene, DEP, LINKALL, null);
	       BaseAction act = Plugins.actionMayaAnimExport(w);
	       act.setSingleParamValue("Method", "Simulate");
	       act.setSingleParamValue("ExportSet", "SELECT");
	       act.setSingleParamValue("FirstFrame", start);
	       act.setSingleParamValue("LastFrame", end);
	       act.setSingleParamValue("MayaScene", sh.animScene);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	 }

	 if ( buildSwitch )
	 {
	    log(sh.switchLightScene + " : ");
	    if ( !Globals.doesNodeExists(w, sh.switchLightScene) )
	    { // switch lighting scene
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, sh.switchLightScene, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(sh.switchLightScene);
	       client.link(user, view, sh.switchLightScene, sh.animScene, DEP, LINKALL,
		  null);
	       if ( advancedRender )
		  client.link(user, view, sh.switchLightScene, sh.lightShaderDefsMI, REF,
		     LINKALL, null);
	       for (SonyAsset as : assets)
	       {
		  if ( Globals.doesNodeExists(w, as.finalScene) )
		  {
		     Globals.getNewest(w, as.finalScene, keep, froz);
		     client.link(user, view, sh.switchLightScene, as.finalScene, DEP,
			LINKALL, null);
		  }
	       }
	       BaseAction act = Plugins.actionModelReplace(w);
	       act.setSingleParamValue("Source", sh.animScene);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	 }

	 if ( buildPreLight )
	 {
	    log(sh.preLightScene + " : ");
	    if ( !Globals.doesNodeExists(w, sh.preLightScene) )
	    { // prelighting scene
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, sh.preLightScene, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(sh.preLightScene);
	       client.link(user, view, sh.preLightScene, sh.animExportGroup, DEP, LINKALL,
		  null);
	       if ( advancedRender )
		  client.link(user, view, sh.preLightScene, sh.lightShaderDefsMI, REF,
		     LINKALL, null);
	       if ( cameraType.equals("Reference") )
	       {
		  for (String cameraName : cameras)
		     client.link(user, view, sh.preLightScene, cameraName, DEP, LINKALL,
			null);
	       }
	       BaseAction act = Plugins.actionMayaCollate(w);
	       act.setSingleParamValue("RootDAGNode", "Reference");
	       act.setSingleParamValue("ImportSet", "SELECT");
	       act.setSingleParamValue("BeginFrame", 1);

	       if ( cameraType.equals("Reference") )
	       {
		  for (String cameraName : cameras)
		  {
		     String prefix = new Path(cameraName).getName();
		     FileSeq fSeq = new FileSeq(prefix, "anim");
		     act.initSourceParams(cameraName);
		     act.setSourceParamValue(cameraName, "Order", null);
		     act.setSourceParamValue(cameraName, "PrefixName", prefix);
		     act.initSecondarySourceParams(sh.animExportGroup, fSeq
			.getFilePattern());
		     act.setSecondarySourceParamValue(sh.animExportGroup, fSeq
			.getFilePattern(), "Order", 100);
		  }
	       }

	       for (SonyAsset as : assets)
	       {
		  client.link(user, view, sh.preLightScene, as.finalScene, DEP, LINKALL,
		     null);
		  act.initSourceParams(as.finalScene);
		  act.setSourceParamValue(as.finalScene, "Order", null);
		  act.setSourceParamValue(as.finalScene, "PrefixName", customNamespaces
		     .get(as.assetName));
	       }
	       for (FileSeq animSeq : actualSeqs.values())
	       {
		  act.initSecondarySourceParams(sh.animExportGroup, animSeq
		     .getFilePattern());
		  act.setSecondarySourceParamValue(sh.animExportGroup, animSeq
		     .getFilePattern(), "Order", 100);
	       }
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	 }

	 if ( advancedRender )
	 { // asset collates
	    for (SonyAsset as : assets)
	    {
	       String collateName = sh.assetCollateScenes.get(as.assetName);
	       log(collateName + " : ");
	       if ( !Globals.doesNodeExists(w, collateName) )
	       {
		  logLine("Building");
		  NodeMod mod = Globals.registerNode(w, collateName, "ma", Plugins
		     .editorMaya(w));
		  addedNodes.add(collateName);
		  BaseAction act = Plugins.actionMayaCollate(w);
		  act.setSingleParamValue("RootDAGNode", "Reference");
		  act.setSingleParamValue("ImportSet", "SELECT");
		  act.setSingleParamValue("BeginFrame", 1);
		  client.link(user, view, collateName, sh.animExportGroup, DEP, LINKALL,
		     null);
		  client.link(user, view, collateName, as.finalScene, DEP, LINKALL, null);
		  FileSeq animSeq = actualSeqs.get(as.assetName);
		  act.initSecondarySourceParams(sh.animExportGroup, animSeq
		     .getFilePattern());
		  act.setSecondarySourceParamValue(sh.animExportGroup, animSeq
		     .getFilePattern(), "Order", 100);
		  act.initSourceParams(as.finalScene);
		  act.setSourceParamValue(as.finalScene, "Order", null);
		  act.setSourceParamValue(as.finalScene, "PrefixName", customNamespaces
		     .get(as.assetName));
		  mod.setAction(act);
		  doReqs(mod);
		  client.modifyProperties(user, view, mod);
	       } else
		  logLine("Already Exists");
	    }

	    for (SonyAsset as : assets)
	    {
	       String miName = sh.assetGeoMI.get(as.assetName);
	       log(miName + " : ");
	       if ( !Globals.doesNodeExists(w, miName) )
	       {
		  logLine("Building");
		  String collateName = sh.assetCollateScenes.get(as.assetName);
		  NodeMod mod = Globals.registerSequence(w, miName, 4, "mi", Plugins
		     .editorEmacs(w), 1, end - start + 1, 1);
		  addedNodes.add(miName);
		  client.link(user, view, miName, collateName, DEP, LINKALL, null);
		  BaseAction act = Plugins.actionMayaMiExport(w);
		  SortedMap<String, Comparable> preset = act.getPresetValues(
		     "EntityPresets", preset_GEOALL);
		  Globals.setPresets(act, preset);
		  act.setSingleParamValue("MayaScene", collateName);
		  act.setSingleParamValue("ExportSet", customNamespaces.get(as.assetName)
			+ ":GEOMETRY");
		  mod.setAction(act);
		  doAnimationReqs(mod);
		  mod.setExecutionMethod(ExecutionMethod.Parallel);
		  mod.setBatchSize(20);
		  client.modifyProperties(user, view, mod);
	       } else
		  logLine("Already Exists");
	    }

	    for (SonyAsset as : assets)
	    {
	       String miName = sh.assetGeoInstMI.get(as.assetName);
	       log(miName + " : ");
	       if ( !Globals.doesNodeExists(w, miName) )
	       {
		  logLine("Building");
		  String geoMI = sh.assetGeoMI.get(as.assetName);
		  NodeMod mod = Globals.registerSequence(w, miName, 4, "mi", Plugins
		     .editorEmacs(w), 1, end - start + 1, 1);
		  addedNodes.add(miName);
		  client.link(user, view, miName, geoMI, DEP, LINKONE, null);
		  BaseAction act = Plugins.actionMRayInstGroup(w);
		  act.setSingleParamValue("GenerateIncludes", true);
		  mod.setAction(act);
		  doAnimationReqs(mod);
		  mod.setExecutionMethod(ExecutionMethod.Parallel);
		  mod.setBatchSize(20);
		  client.modifyProperties(user, view, mod);
	       } else
		  logLine("Already Exists");
	    }

	    log(sh.cameraMI + " : ");
	    if ( !Globals.doesNodeExists(w, sh.cameraMI) )
	    { // Camera MIs
	       logLine("Building");
	       NodeMod mod = Globals.registerSequence(w, sh.cameraMI, 4, "mi", Plugins
		  .editorEmacs(w), 1, end - start + 1, 1);
	       addedNodes.add(sh.cameraMI);
	       client.link(user, view, sh.cameraMI, sh.animScene, DEP, LINKALL, null);
	       BaseAction act = Plugins.actionMayaMiExport(w);
	       SortedMap<String, Comparable> preset = act.getPresetValues("EntityPresets",
		  preset_CAMERAS);
	       Globals.setPresets(act, preset);
	       act.setSingleParamValue("MayaScene", sh.animScene);
	       act.setSingleParamValue("ExportSet", "camera:CAMERA");
	       mod.setAction(act);
	       doAnimationReqs(mod);
	       mod.setExecutionMethod(ExecutionMethod.Parallel);
	       mod.setBatchSize(20);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already exists");

	    log(sh.geoTopGroup + " : ");
	    if ( !Globals.doesNodeExists(w, sh.geoTopGroup) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, sh.geoTopGroup, null, Plugins
		  .editorEmacs(w));
	       for (SonyAsset as : assets)
	       {
		  String miName = sh.assetGeoInstMI.get(as.assetName);
		  client.link(user, view, sh.geoTopGroup, miName, DEP, LINKALL, null);
	       }
	       if ( buildPreLight )
		  client.link(user, view, sh.geoTopGroup, sh.preLightScene, DEP, LINKALL,
		     null);
	       if ( buildSwitch )
		  client.link(user, view, sh.geoTopGroup, sh.switchLightScene, DEP,
		     LINKALL, null);
	       client.link(user, view, sh.geoTopGroup, sh.cameraMI, DEP, LINKALL, null);
	       BaseAction act = Plugins.actionTouch(w);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already exists");
	 } //if ( advancedRender )

	 if ( buildLight )
	 {
	    String lightsName = SonyConstants.getLightsList(w, project).get(
	       lightRigField.getSelected());
	    {
	       logLine("Getting the lights: " + lightsName);
	       Globals.getNewest(w, lightsName, keep, froz);
	    }

	    if ( buildSwitch && buildPreLight )
	    {
	       log(sh.testLightScene + " : ");
	       if ( !Globals.doesNodeExists(w, sh.testLightScene) )
	       { // lighting scene
		  logLine("Building");
		  NodeMod mod = Globals.registerNode(w, sh.testLightScene, "ma", Plugins
		     .editorMaya(w));
		  addedNodes.add(sh.testLightScene);
		  client.link(user, view, sh.testLightScene, sh.switchLightScene, REF,
		     LINKALL, null);
		  client.link(user, view, sh.testLightScene, lightsName, REF, LINKALL, null);
		  BaseAction act = Plugins.actionMayaReference(w);
		  act.initSourceParams(sh.switchLightScene);
		  act.setSourceParamValue(sh.switchLightScene, "PrefixName", "replaced");
		  act.initSourceParams(lightsName);
		  act.setSourceParamValue(lightsName, "PrefixName", "lights");
		  mod.setAction(act);
		  doReqs(mod);
		  client.modifyProperties(user, view, mod);
	       }
	    }
	    {
	       String sourceName = null;
	       String nameSpace = null;
	       if ( buildPreLight  )
	       {
		  sourceName = sh.preLightScene;
		  nameSpace = "prelight";
	       } else if ( buildSwitch )
	       {
		  sourceName = sh.switchLightScene;
		  nameSpace = "switch";
	       } else
	       {
		  sourceName = sh.animScene;
		  nameSpace = "anim";
	       }

	       log(sh.lightScene + " : ");
	       if ( !Globals.doesNodeExists(w, sh.lightScene) )
	       { // lighting scene
		  logLine("Building");
		  NodeMod mod = Globals.registerNode(w, sh.lightScene, "ma", Plugins
		     .editorMaya(w));
		  addedNodes.add(sh.lightScene);
		  client.link(user, view, sh.lightScene, sourceName, REF, LINKALL, null);
		  client.link(user, view, sh.lightScene, lightsName, REF, LINKALL, null);

		  if ( advancedRender && sourceName.equals(sh.animScene) )
		     client.link(user, view, sh.lightScene, sh.lightShaderDefsMI, REF,
			LINKALL, null);

		  BaseAction act = Plugins.actionMayaReference(w);

		  act.initSourceParams(sourceName);
		  act.setSourceParamValue(sourceName, "PrefixName", nameSpace);

		  act.initSourceParams(lightsName);
		  act.setSourceParamValue(lightsName, "PrefixName", "lights");

		  if ( advancedRender )
		  {
		     for (SonyAsset as : assets)
		     {
			Globals.referenceNode(w, sh.lightScene, as.shdExport, act, REF,
			   customNamespaces.get(as.assetName));
		     }
		  }
		  mod.setAction(act);
		  doReqs(mod);
		  client.modifyProperties(user, view, mod);
	       } else
		  logLine("Already exists");
	    }
	 }

	 if ( buildTestImages )
	 {
	    log(sh.initialImages + " : ");
	    if ( !Globals.doesNodeExists(w, sh.initialImages) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerSequence(w, sh.initialImages, 4, "iff",
		  Plugins.editorFCheck(w), start, end, 1);
	       addedNodes.add(sh.initialImages);
	       if ( buildLight )
	       {
		  if ( buildPreLight && buildSwitch )
		     client.link(user, view, sh.initialImages, sh.testLightScene, DEP,
			LINKALL, null);
		  else
		     client.link(user, view, sh.initialImages, sh.lightScene, DEP, LINKALL,
			null);
	       } else
		  client.link(user, view, sh.initialImages, sh.animScene, DEP, LINKALL,
		     null);
	       BaseAction act = Plugins.actionTouch(w);
	       mod.setAction(act);
	       doLightingReqs(mod);
	       mod.setExecutionMethod(ExecutionMethod.Parallel);
	       mod.setBatchSize(3);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already exists");
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

      queueAndWait(sh, advancedRender, buildSwitch, buildPreLight, buildLight,
	 buildTestImages);
      OverallQueueState state = checkStatuses(sh, advancedRender, buildSwitch,
	 buildPreLight, buildLight, buildTestImages);
      logLine("Queue state returned of " + state);
      JToolDialog tool = new JToolDialog("BuildShot", new JPanel(), "Continue");
      JConfirmDialog checkDialog;
      if ( !state.equals(OverallQueueState.Finished) )
      {
	 checkDialog = new JConfirmDialog(tool, "One or more of the jobs associated with ("
	       + sh.seqName + "_" + sh.shotName + ") did not successfully "
	       + "generate its nodes.Do you wish to continue?");
	 checkDialog.setVisible(true);
	 if ( !checkDialog.wasConfirmed() )
	    return;
      }

      if ( buildLight )
      {
	 Globals.disableAction(w, sh.lightScene);
	 if ( buildPreLight && buildSwitch )
	    Globals.disableAction(w, sh.testLightScene);
      }
      Globals.disableAction(w, sh.animScene);

      checkDialog = new JConfirmDialog(tool, "Everything appears to have finished fine.  \n"
	    + "Do you want to check the nodes in?");
      checkDialog.setVisible(true);
      if ( checkDialog.wasConfirmed() )
	 checkinNodes(sh, advancedRender, buildSwitch, buildPreLight, buildLight,
	    buildTestImages);
   }

   private void checkinNodes(SonyShot sh, boolean advancedRender, boolean buildSwitch,
	 boolean buildPreLight, boolean buildLight, boolean buildTestImages)
      throws PipelineException
   {
      NodeID nodeID;

      if ( advancedRender )
      {
	 nodeID = new NodeID(user, view, sh.geoTopGroup);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( buildTestImages )
      {
	 nodeID = new NodeID(user, view, sh.initialImages);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( buildLight && ( ( buildSwitch && buildPreLight ) || !buildTestImages ) )
      {
	 nodeID = new NodeID(user, view, sh.lightScene);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( !buildTestImages && buildLight && buildSwitch && buildPreLight )
      {
	 nodeID = new NodeID(user, view, sh.testLightScene);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( !advancedRender && !buildLight && buildPreLight )
      {
	 nodeID = new NodeID(user, view, sh.preLightScene);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( !advancedRender && !buildLight && buildSwitch )
      {
	 nodeID = new NodeID(user, view, sh.switchLightScene);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
      if ( !buildTestImages && !advancedRender && !buildLight && !buildPreLight
	    && !buildSwitch )
      {
	 nodeID = new NodeID(user, view, sh.animScene);
	 client.checkIn(nodeID, "Inital shot tree built by the Shot Builder GUI.",
	    VersionID.Level.Minor);
      }
   }

   private OverallQueueState checkStatuses(SonyShot sh, boolean advancedRender,
	 boolean buildSwitch, boolean buildPreLight, boolean buildLight,
	 boolean buildTestImages) throws PipelineException
   {
      NodeStatus status = null;
      if ( advancedRender )
      {
	 status = client.status(user, view, sh.geoTopGroup);
	 OverallQueueState one = Globals.getTreeState(status);
	 if ( buildTestImages )
	 {
	    if ( one.equals(OverallQueueState.Finished) )
	    {
	       status = client.status(user, view, sh.initialImages);
	       OverallQueueState two = Globals.getTreeState(status);
	       if ( buildSwitch && buildPreLight )
	       {
		  if ( two.equals(OverallQueueState.Finished) )
		  {
		     status = client.status(user, view, sh.lightScene);
		     OverallQueueState three = Globals.getTreeState(status);
		     return three;
		  }
	       }
	       return two;
	    }
	 } else if ( buildLight )
	    if ( one.equals(OverallQueueState.Finished) )
	    {
	       status = client.status(user, view, sh.lightScene);
	       OverallQueueState two = Globals.getTreeState(status);
	       if ( buildSwitch && buildPreLight )
	       {
		  if ( two.equals(OverallQueueState.Finished) )
		  {
		     status = client.status(user, view, sh.testLightScene);
		     OverallQueueState three = Globals.getTreeState(status);
		     return three;
		  }
	       }
	       return two;
	    }
	 return one;
      } else if ( buildLight )
      {
	 status = client.status(user, view, sh.lightScene);
	 OverallQueueState one = Globals.getTreeState(status);
	 if ( buildSwitch && buildPreLight )
	 {
	    if ( one.equals(OverallQueueState.Finished) )
	    {
	       status = client.status(user, view, sh.testLightScene);
	       OverallQueueState two = Globals.getTreeState(status);
	       return two;
	    }
	 }
	 return one;
      } else if ( buildPreLight || buildSwitch )
      {
	 OverallQueueState one = null;
	 if ( buildPreLight )
	 {
	    status = client.status(user, view, sh.preLightScene);
	    one = Globals.getTreeState(status);
	 }
	 if ( one != null && !one.equals(OverallQueueState.Finished) )
	    return one;
	 if ( buildSwitch )
	 {
	    status = client.status(user, view, sh.preLightScene);
	    one = Globals.getTreeState(status);
	    return one;
	 }
	 return one;
      } else
      {
	 status = client.status(user, view, sh.animScene);
	 OverallQueueState one = Globals.getTreeState(status);
	 return one;
      }
   }

   private void queueAndWait(SonyShot sh, boolean advancedRender, boolean buildSwitch,
	 boolean buildPreLight, boolean buildLight, boolean buildTestImages)
      throws PipelineException
   {
      LinkedList<QueueJobGroup> queueJobs = new LinkedList<QueueJobGroup>();

      logLine("Queuing the jobs now.");
      if ( advancedRender )
	 queueJobs.addAll(client.submitJobs(user, view, sh.geoTopGroup, null));
      if ( buildTestImages )
	 queueJobs.addAll(client.submitJobs(user, view, sh.initialImages, null));
      if ( buildLight && ( ( buildSwitch && buildPreLight ) || !buildTestImages ) )
	 queueJobs.addAll(client.submitJobs(user, view, sh.lightScene, null));
      if ( !buildTestImages && buildLight && buildSwitch && buildPreLight )
	 queueJobs.addAll(client.submitJobs(user, view, sh.testLightScene, null));
      if ( !advancedRender && !buildLight && buildPreLight )
	 queueJobs.addAll(client.submitJobs(user, view, sh.preLightScene, null));
      if ( !advancedRender && !buildLight && buildSwitch )
	 queueJobs.addAll(client.submitJobs(user, view, sh.switchLightScene, null));
      if ( !buildTestImages && !advancedRender && !buildLight && !buildPreLight
	    && !buildSwitch )
	 queueJobs.addAll(client.submitJobs(user, view, sh.animScene, null));

      boolean done = false;
      boolean error = false;

      log("Waiting for jobs to complete.");
      while ( !done )
      {
	 for (QueueJobGroup job : queueJobs)
	 {
	    TreeSet<Long> stuff = new TreeSet<Long>();
	    stuff.add(job.getGroupID());
	    TreeMap<Long, JobStatus> statuses = queue.getJobStatus(stuff);
	    for (JobStatus status : statuses.values())
	    {
	       JobState state = status.getState();
	       if ( state.equals(JobState.Failed) || state.equals(JobState.Aborted) )
	       {
		  error = true;
		  break;
	       }
	       if ( !state.equals(JobState.Finished) )
	       {
		  done = false;
		  break;
	       }
	       done = true;
	    }
	    if ( error )
	       throw new PipelineException("The job for the (" + sh.seqName + "_"
		     + sh.shotName + ") did not complete correctly.");
	    if ( !done )
	       break;
	 }
	 log("...");
	 if ( !done )
	 {
	    try
	    {
	       Thread.sleep(10000);
	    } catch ( InterruptedException e )
	    {
	       e.printStackTrace();
	    }
	 }
      }
      logLine("");
   }

   private boolean buildGUI() throws PipelineException
   {
      Box finalBox = new Box(BoxLayout.Y_AXIS);
      top = new Box(BoxLayout.Y_AXIS);

      JScrollPane scroll;

      {
	 scroll = new JScrollPane(finalBox);

	 scroll
	    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	 scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	 Dimension size = new Dimension(sTSize + sVSize + 52, 500);
	 scroll.setMinimumSize(size);
	 scroll.setPreferredSize(size);

	 scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }

      /* query the user */
      diag = new JToolDialog("Build Shot", scroll, "Continue");

      areas = client.getWorkingAreas();
      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];
	 {
	    userField = UIFactory.createTitledCollectionField(tpanel, "User:", sTSize,
	       vpanel, areas.keySet(), diag, sVSize,
	       "The user whose area the node is being created in.");
	    userField.setActionCommand("user");
	    userField.setSelected(PackageInfo.sUser);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    viewField = UIFactory.createTitledCollectionField(tpanel, "View:", sTSize,
	       vpanel, areas.get(PackageInfo.sUser), diag, sVSize,
	       "The working area to create the nodes in.");
	    viewField.setActionCommand("wrap");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    toolsetField = UIFactory.createTitledCollectionField(tpanel, "Toolset:",
	       sTSize, vpanel, client.getActiveToolsetNames(), diag, sVSize,
	       "The toolset to set on all the nodes.");
	    toolsetField.setSelected(client.getDefaultToolsetName());
	    toolsetField.setActionCommand("wrap");
	 }
	 w = new Wrapper(userField.getSelected(), viewField.getSelected(), toolsetField
	    .getSelected(), client);
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    projectField = UIFactory.createTitledCollectionField(tpanel, "Project:",
	       sTSize, vpanel, SonyConstants.getProjectList(w), diag, sVSize,
	       "All the projects in pipeline.");
	    projectField.setActionCommand("proj");
	    project = projectField.getSelected();
	 }
	 hbox.add(comps[2]);
	 top.add(hbox);
      }

      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];
	 {
	    movieField = UIFactory.createTitledCollectionField(tpanel, "Movie:", sTSize,
	       vpanel, SonyConstants.getMovieList(w, project), sVSize,
	       "The movie to add the shots to.");
	    movieField.setActionCommand("movie");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    movieBooleanField = UIFactory.createTitledBooleanField(tpanel, "New Movie:",
	       sTSize, vpanel, sVSize, "Do you want to create a new movie.");
	    movieBooleanField.setValue(false);
	    movieBooleanField.setActionCommand("movie");
	 }
	 movie = movieField.getSelected();
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    movieNameField = UIFactory.createTitledEditableTextField(tpanel, "Movie Name:",
	       sTSize, vpanel, null, sVSize, "The movie to create");
	    movieNameField.setEnabled(false);
	 }
	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Movie Settings", hbox, true);
	 top.add(draw);
      }
      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];
	 {
	    TreeSet<String> list = new TreeSet<String>();
	    list.add("Reference");
	    list.add("Import");
	    cameraTypeField = UIFactory.createTitledCollectionField(tpanel, "Camera Use:",
	       sTSize, vpanel, list, diag, sVSize,
	       "Are you importing or referencing cameras.");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    TreeSet<String> list = new TreeSet<String>();
	    list.add("Hi-Rez");
	    list.add("Lo-Rez");
	    modelTypeField = UIFactory.createTitledCollectionField(tpanel, "Model Type:",
	       sTSize, vpanel, list, diag, sVSize,
	       "What sort of models should be used in the animation scenes.");
	    modelTypeField.setSelected("Lo-Rez");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    TreeSet<String> list = new TreeSet<String>(Arrays.asList(MissingModel
	       .stringValues()));

	    missingModelField = UIFactory.createTitledCollectionField(tpanel,
	       "Missing Model Action:", sTSize, vpanel, list, diag, sVSize,
	       "What to do if the specified version of a model is not found.\n"
		     + "Abort will immediately halt processing.\n"
		     + "Ignore will just skip the model and proceed.\n"
		     + "Substitute will attempt to use a different version of the "
		     + "model and will fall back on the other option if no "
		     + "different version exists.");
	    missingModelField.setSelected("SubstituteOrIgnore");
	 }
	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Global Shot Settings", hbox, true);
	 top.add(draw);
      }
      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 {
	    buildModelReplaceField = UIFactory.createTitledBooleanField(tpanel,
	       "Build Switch Scene:", sTSize, vpanel, sVSize,
	       "Build a prelight scene that just switches from low rez to hi rez models.");
	    buildModelReplaceField.setValue(false);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    buildPrelightField = UIFactory.createTitledBooleanField(tpanel,
	       "Build Prelight:", sTSize, vpanel, sVSize,
	       "Build a prelight scene from exported animation and hi-rez models.");
	    buildPrelightField.setValue(false);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    buildLightField = UIFactory.createTitledBooleanField(tpanel,
	       "Build Light Scene:", sTSize, vpanel, sVSize,
	       "Build a lighting scene.  If you've selected creating a prelight scene, "
		     + "then this node will be hooked up to that scene. "
		     + "If you haven't, this will be hooked up to the animation scene.");
	    buildLightField.setValue(false);
	    buildLightField.setActionCommand("light");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    TreeMap<String, String> map = SonyConstants.getLightsList(w, project);
	    lightRigField = UIFactory.createTitledCollectionField(tpanel, "Light Rig:",
	       sTSize, vpanel, map.keySet(), diag, sVSize,
	       "The light rig to use with the created shots.");
	    lightRigField.setEnabled(false);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    buildTestImagesField = UIFactory.createTitledBooleanField(tpanel,
	       "Build Test Images:", sTSize, vpanel, sVSize, "Build a test images nodes.");
	    buildTestImagesField.setValue(false);
	 }

	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Lighting Settings", hbox, true);
	 top.add(draw);
      }

      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 TreeSet<String> keys = queue.getSelectionKeyNames(true);
	 for (String key : keys)
	 {
	    JBooleanField field = UIFactory.createTitledBooleanField(tpanel, key + ":",
	       sTSize, vpanel, sVSize,
	       "Should this selection key be assigned to all the generated nodes.");
	    field.setValue(false);
	    selectionKeyFields.put(key, field);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 }
	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Node Selection Keys", hbox, true);
	 top.add(draw);
      }

      {
	 Box vbox = new Box(BoxLayout.Y_AXIS);
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 JButton button = new JButton("Add Another Shot");
	 button.setName("ValuePanelButton");
	 button.setRolloverEnabled(false);
	 button.setFocusable(false);
	 Dimension d = new Dimension(sVSize, 25);
	 button.setPreferredSize(d);
	 button.setMinimumSize(d);
	 button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

	 //button.setAlignmentY(JButton.CENTER_ALIGNMENT);
	 vbox.add(Box.createRigidArea(new Dimension(0, 5)));
	 hbox.add(button);
	 hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	 vbox.add(hbox);
	 vbox.add(Box.createRigidArea(new Dimension(0, 5)));

	 button.setActionCommand("add");
	 button.addActionListener(this);

	 top.add(vbox);
      }

      top.add(shotBox());

      finalBox.add(top);

      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

	 finalBox.add(spanel);
      }

      {
	 userField.addActionListener(this);
	 viewField.addActionListener(this);
	 toolsetField.addActionListener(this);
	 projectField.addActionListener(this);
	 movieField.addActionListener(this);
	 movieBooleanField.addActionListener(this);
	 buildLightField.addActionListener(this);
      }

      diag.setVisible(true);
      if ( diag.wasConfirmed() )
	 return true;
      return false;
   }

   private JDrawer shotBox() throws PipelineException
   {
      JDrawer toReturn;
      Box stuff = new Box(BoxLayout.X_AXIS);
      int num = shotNameFields.size();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      {
	 JCollectionField field = UIFactory.createTitledCollectionField(tpanel,
	    "Sequence:", sTSize, vpanel, SonyConstants.getSequenceList(w, project, movie), diag, 
	    sVSize, "The sequence in the shot.");
	 sequenceFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JBooleanField field = UIFactory.createTitledBooleanField(tpanel, "New Sequence:",
	    sTSize, vpanel, sVSize, "Do you want to create a new sequence.");
	 field.setValue(false);
	 field.setActionCommand("sequence" + num);
	 sequenceBooleanFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JTextField field = UIFactory.createTitledEditableTextField(tpanel,
	    "Sequence Name:", sTSize, vpanel, null, sVSize, "A sequence to create");
	 field.setEnabled(false);
	 sequenceNameFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JTextField field = UIFactory.createTitledEditableTextField(tpanel, "Shot Name:",
	    sTSize, vpanel, null, sVSize, "The name of the shot being created.");
	 shotNameFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JIntegerField field = UIFactory.createTitledIntegerField(tpanel, "Start Frame:",
	    sTSize, vpanel, 1, sVSize, "Start Frame of the animation");
	 shotStartFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JIntegerField field = UIFactory.createTitledIntegerField(tpanel, "End Frame:",
	    sTSize, vpanel, 1, sVSize, "End Frame of the animation");
	 shotEndFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JBooleanField field = UIFactory.createTitledBooleanField(tpanel,
	    "Advanced MR Render:", sTSize, vpanel, sVSize,
	    "Should things be setup for a standalone mental ray render.");
	 field.setValue(false);
	 advancedRenderFields.add(num, field);
      }
      {
	 sequenceBooleanFields.get(num).addActionListener(this);
      }
      stuff.add(comps[2]);
      toReturn = new JDrawer("Add Shot", stuff, false);
      return toReturn;
   }

   public void actionPerformed(ActionEvent e)
   {
      String com = e.getActionCommand();
      if ( com.equals("user") )
      {
	 String user1 = userField.getSelected();
	 String view1 = viewField.getSelected();
	 viewField.setValues(areas.get(user1));
	 if ( areas.get(user1).contains(view1) )
	    viewField.setSelected(view1);
	 try
	 {
	    w = new Wrapper(user1, viewField.getSelected(), toolsetField.getSelected(),
	       client);
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
      } else if ( com.equals("wrap") )
      {
	 try
	 {
	    w = new Wrapper(userField.getSelected(), viewField.getSelected(), toolsetField
	       .getSelected(), client);
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
      } else if ( com.equals("proj") )
      {
	 ArrayList<String> movies = null;
	 try
	 {
	    movies = SonyConstants.getMovieList(w, project);
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
	 String name = movieField.getName();
	 movieField.setValues(movies);
	 if ( movies.contains(name) )
	    movieField.setSelected(name);
      } else if ( com.equals("add") )
      {
	 try
	 {
	    top.add(shotBox());
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
	 diag.validate();
      } else if ( com.equals("movie") )
      {
	 boolean newMovie = movieBooleanField.getValue();
	 if ( newMovie )
	 {
	    movieNameField.setEnabled(true);
	    movieField.setEnabled(false);
	    movie = movieNameField.getText();
	 } else
	 {
	    movieNameField.setEnabled(false);
	    movieField.setEnabled(true);
	    movie = movieField.getSelected();
	 }
	 System.err.println("Movie:" + movie);
	 ArrayList<String> sequences = null;
	 try
	 {
	    if ( !movie.equals("") )
	       sequences = SonyConstants.getSequenceList(w, project, movie);
	    else
	    {
	       sequences = new ArrayList<String>();
	    }
	    if ( sequences.size() == 0 )
	       sequences.add("none exist");
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
	 for (int i = 0; i < sequenceFields.size(); i++)
	 {
	    JCollectionField field = sequenceFields.get(i);
	    String sequence = sequenceFields.get(i).getName();
	    field.setValues(sequences);
	    if ( sequences.contains(sequence) )
	       field.setSelected(sequence);
	 }
      } else if ( com.startsWith("sequence") )
      {
	 String tempInt = com.replace("sequence", "");

	 int i = Integer.valueOf(tempInt);
	 JBooleanField field = (JBooleanField) e.getSource();
	 if ( field.getValue() )
	 {
	    sequenceNameFields.get(i).setEnabled(true);
	    sequenceFields.get(i).setEnabled(false);
	 } else
	 {
	    sequenceNameFields.get(i).setEnabled(false);
	    sequenceFields.get(i).setEnabled(true);
	 }
      } else if ( com.equals("light") )
      {
	 boolean value = buildLightField.getValue();
	 lightRigField.setEnabled(value);
      }
   }

   private void log(String s)
   {
      if ( verbose )
      {
	 System.err.print(s);
	 //log.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, s);
      }
   }

   private void logLine(String s)
   {
      if ( verbose )
      {
	 System.err.println(s);
	 //log.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Fine, s);
      }
   }

   private String getMovie()
   {
      if ( movieBooleanField.getValue() )
	 return movieNameField.getText();
      return movieField.getSelected();
   }

   private String getSequence(int num)
   {
      if ( sequenceBooleanFields.get(num).getValue() )
	 return sequenceNameFields.get(num).getText();
      return sequenceFields.get(num).getSelected();
   }

   private void doReqs(NodeMod mod) throws PipelineException
   {
      JobReqs req = mod.getJobRequirements();
      req.addSelectionKeys(usedKeys);
      mod.setJobRequirements(req);
   }

   private void doAnimationReqs(NodeMod mod) throws PipelineException
   {
      JobReqs req = mod.getJobRequirements();
      req.addSelectionKeys(usedKeys);
      req.addSelectionKey("Animation");
      mod.setJobRequirements(req);
   }

   private void doLightingReqs(NodeMod mod) throws PipelineException
   {
      JobReqs req = mod.getJobRequirements();
      req.addSelectionKeys(usedKeys);
      req.addSelectionKey("Lighting");
      mod.setJobRequirements(req);
   }

   private String user;
   private String view;
   private String project;
   private String movie;
   private boolean verbose;
   private JToolDialog diag;
   private Wrapper w;

   private TreeMap<String, TreeSet<String>> areas;

   private TreeSet<String> usedKeys;

   private JCollectionField userField;
   private JCollectionField viewField;
   private JCollectionField toolsetField;
   private JCollectionField projectField;
   private JCollectionField lightRigField;
   private JCollectionField modelTypeField;
   private JCollectionField missingModelField;
   private JCollectionField movieField;
   private JCollectionField cameraTypeField;

   private TreeMap<String, JBooleanField> selectionKeyFields;

   private ArrayList<JCollectionField> sequenceFields;
   private ArrayList<JBooleanField> sequenceBooleanFields;
   private ArrayList<JTextField> sequenceNameFields;
   private ArrayList<JTextField> shotNameFields;
   private ArrayList<JIntegerField> shotStartFields;
   private ArrayList<JIntegerField> shotEndFields;

   private ArrayList<JBooleanField> advancedRenderFields;

   private DoubleMap<Integer, String, JBooleanField> shotAssetMappings;
   private DoubleMap<Integer, String, JBooleanField> shotCameraMappings;

   private DoubleMap<Integer, String, JBooleanField> shotMiscMappings;
   private DoubleMap<Integer, String, JCollectionField> shotMiscTypeMappings;

   private JBooleanField movieBooleanField;
   private JBooleanField buildPrelightField;
   private JBooleanField buildModelReplaceField;
   private JBooleanField buildLightField;
   private JBooleanField buildTestImagesField;

   private JTextField movieNameField;

   private Box top;

   private ArrayList<Integer> validatedShots;

   public static final LinkRelationship LINKALL = LinkRelationship.All;
   public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
   public static final LinkPolicy DEP = LinkPolicy.Dependency;
   public static final LinkPolicy REF = LinkPolicy.Reference;
   public static final CheckOutMode over = CheckOutMode.OverwriteAll;
   public static final CheckOutMode keep = CheckOutMode.KeepModified;
   public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
   public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
   public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
   public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

   private static final int sTSize = 175;
   private static final int sVSize = 150;

   private static final String preset_GEOALL = "Geometry (Including Instances/Stub Materials)";
   private static final String preset_CAMERAS = "Camera Declarations and Instances";

   enum MissingModel
   {
      Abort, Ignore, SubstituteOrAbort, SubstituteOrIgnore;

      public static String[] stringValues()
      {
	 MissingModel v[] = values();
	 String toReturn[] = new String[v.length];
	 for (int i = 0; i < v.length; i++)
	    toReturn[i] = v[i].toString();
	 return toReturn;
      }

      public static MissingModel fromString(String s) throws PipelineException
      {
	 MissingModel toReturn = null;
	 if ( s.equals("Abort") )
	    toReturn = MissingModel.Abort;
	 else if ( s.equals("Ignore") )
	    toReturn = MissingModel.Ignore;
	 else if ( s.equals("SubstituteOrAbort") )
	    toReturn = MissingModel.SubstituteOrAbort;
	 else if ( s.equals("SubstituteOrIgnore") )
	    toReturn = MissingModel.SubstituteOrIgnore;
	 else
	    throw new PipelineException("The given type (" + s + ") does "
		  + "not match any of the known types");
	 return toReturn;
      }
   }

}

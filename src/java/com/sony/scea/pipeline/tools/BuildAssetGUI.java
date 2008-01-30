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

public class BuildAssetGUI extends BootApp implements ActionListener
{

   MasterMgrClient client;
   PluginMgrClient plug;
   QueueMgrClient queue;
   LogMgr log;

   public BuildAssetGUI()
   {
      try
      {
	 PluginMgrClient.init();
	 client = new MasterMgrClient();
	 queue = new QueueMgrClient();
	 plug = PluginMgrClient.getInstance();
	 log = LogMgr.getInstance();
	 assetNameFields = new ArrayList<JTextField>();
	 advancedShadeFields = new ArrayList<JBooleanField>();
	 textureStubFields = new ArrayList<JBooleanField>();
	 sepHeadFields = new ArrayList<JBooleanField>();
	 selectionKeyFields = new TreeMap<String, JBooleanField>();
	 usedKeys = new TreeSet<String>();
	 verbose = true;
	 validatedAssets = new ArrayList<Integer>();

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
      BuildAssetGUI gui = new BuildAssetGUI();
      try
      {
	 boolean here = gui.buildGUI();
	 if ( here == false )
	    System.exit(0);
	 else
	 {
	    boolean here2 = gui.validateEntries();
	    if ( here2 )
	       gui.processAssets();
	 }
	 System.exit(0);
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
	 System.exit(1);
      }

   }

   private void processAssets() throws PipelineException
   {
      int count = assetNameFields.size();

      if ( projectBooleanField.getValue() )
      {
	 mels = SonyConstants.getAllMelWithPrefix(w, null);
      } else
      {
	 String name = projectField.getSelected();
	 mels = SonyConstants.getAllMelWithPrefix(w, name);
      }

      for (int i = 0; i < count; i++)
      {
	 if ( validatedAssets.contains(i) )
	 {
	    try
	    {
	       buildAsset(i);
	    } catch ( PipelineException e )
	    {
	       e.printStackTrace();
	    }
	 } else
	 {
	    JTextField assetNameField = assetNameFields.get(i);
	    String assetName = assetNameField.getText();
	    log.logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Warning, "Could not create "
		  + assetName);
	 }
      }
   }

   private boolean validateEntries() throws PipelineException
   {
      user = userField.getSelected();
      view = viewField.getSelected();
      if ( projectBooleanField.getValue() )
	 project = projectNameField.getText();
      else
	 project = projectField.getSelected();

      int count = assetNameFields.size();

      //ArrayList<String> allAssets = SonyConstants.getAllAssets(w, project);

      LinkedList<String> allAddedAssets = new LinkedList<String>();

      for (int i = 0; i < count; i++)
      {
	 JTextField assetNameField = assetNameFields.get(i);
	 String assetName = assetNameField.getText();
	 allAddedAssets.add(assetName);
      }

      for (int i = 0; i < count; i++)
      {
	 JTextField assetNameField = assetNameFields.get(i);
	 String assetName = assetNameField.getText();
	 //if ( allAssets.contains(assetName) )
	   // continue;
	 ArrayList<String> tempList = new ArrayList<String>(allAddedAssets);
	 tempList.remove(i);
	 if ( tempList.contains(assetName) )
	    continue;
	 validatedAssets.add(i);
      }

      for (String key : selectionKeyFields.keySet())
      {
	 if ( selectionKeyFields.get(key).getValue() )
	    usedKeys.add(key);
      }

      return true;
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
	       processAssets();
	 }
	 System.exit(0);
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
      }
   }

   private void buildAsset(int num) throws PipelineException
   {
      String assetName = assetNameFields.get(num).getText();
      AssetType assetType = AssetType.fromString(assetTypeField.getSelected());

      boolean shade = advancedShadeFields.get(num).getValue();
      boolean texture = textureStubFields.get(num).getValue();
      boolean sepHead = sepHeadFields.get(num).getValue();

      String placeholderMel = placeholderMelField.getSelected();
      String finalizeMel = finalizeMelField.getSelected();
      String lrFinalizeMel = lrFinalizeMelField.getSelected();
      String mrInitMel = mrInitMelField.getSelected();
      boolean hasPlaceholderMel = true;
      boolean hasFinalizeMel = true;
      boolean hasLRFinalizeMel = true;
      boolean hasMRInitMel = true;

      if ( placeholderMel.equals("NONE") )
	 hasPlaceholderMel = false;
      else
      {
	 placeholderMel = mels.get(placeholderMel);
	 Globals.getNewest(w, placeholderMel, over, froz);
      }
      if ( finalizeMel.equals("NONE") )
	 hasFinalizeMel = false;
      else
      {
	 finalizeMel = mels.get(finalizeMel);
	 Globals.getNewest(w, finalizeMel, over, froz);
      }
      if ( lrFinalizeMel.equals("NONE") )
	 hasLRFinalizeMel = false;
      else
      {
	 lrFinalizeMel = mels.get(lrFinalizeMel);
	 Globals.getNewest(w, lrFinalizeMel, over, froz);
      }
      if ( mrInitMel.equals("NONE") )
	 hasMRInitMel = false;
      else
      {
	 mrInitMel = mels.get(mrInitMel);
	 Globals.getNewest(w, mrInitMel, over, froz);
      }

      TreeSet<String> addedNodes = new TreeSet<String>();

      SonyAsset as = new SonyAsset(project, assetName, assetType);

      try
      {
	 if ( texture )
	 {
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
	 }

	 log(as.modScene + " : ");
	 if ( !Globals.doesNodeExists(w, as.modScene) )
	 {
	    logLine("Building");
	    NodeMod mod = Globals.registerNode(w, as.modScene, "ma", Plugins.editorMaya(w));
	    addedNodes.add(as.modScene);
	    BaseAction act = Plugins.actionMayaBuild(w);
	    if ( hasPlaceholderMel )
	    {
	       client.link(user, view, as.modScene, placeholderMel, DEP, LINKALL, null);
	       act.setSingleParamValue("ModelMEL", placeholderMel);
	    }
	    mod.setAction(act);
	    doReqs(mod);
	    client.modifyProperties(user, view, mod);
	 } else
	    logLine("Already Exists");
	 
	 if (sepHead)
	 {
	    log(as.headModScene + " : ");
	    if ( !Globals.doesNodeExists(w, as.headModScene) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, as.headModScene, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(as.headModScene);
	       BaseAction act = Plugins.actionMayaBuild(w);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	    
	    log(as.blendShapeScene+ " : ");
	    if ( !Globals.doesNodeExists(w, as.blendShapeScene) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, as.blendShapeScene, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(as.blendShapeScene);
	       BaseAction act = Plugins.actionMayaBuild(w);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
	 }

	 log(as.rigScene + " : ");
	 if ( !Globals.doesNodeExists(w, as.rigScene) )
	 {
	    logLine("Building");
	    NodeMod mod = Globals.registerNode(w, as.rigScene, "ma", Plugins.editorMaya(w));
	    addedNodes.add(as.rigScene);
	    client.link(w.user, w.view, as.rigScene, as.modScene, DEP, LINKALL,
	       null);
	    if (sepHead)
	    {
		    client.link(w.user, w.view, as.rigScene, as.blendShapeScene, DEP, LINKALL,
		       null);
		    client.link(w.user, w.view, as.rigScene, as.headModScene, DEP, LINKALL, null);
	    }
	    BaseAction act = Plugins.actionMayaBuild(w);
	    act.initSourceParams(as.modScene);
	    act.setSourceParamValue(as.modScene, "BuildType", "Import");
	    act.setSourceParamValue(as.modScene, "NameSpace", false);
	    if (sepHead)
	    {
	       act.initSourceParams(as.blendShapeScene);
	       act.setSourceParamValue(as.blendShapeScene, "BuildType", "Import");
	       act.setSourceParamValue(as.blendShapeScene, "NameSpace", false);

	       act.initSourceParams(as.headModScene);
	       act.setSourceParamValue(as.headModScene, "BuildType", "Import");
	       act.setSourceParamValue(as.headModScene, "NameSpace", false);
	    }
	    mod.setAction(act);
	    doReqs(mod);
	    client.modifyProperties(user, view, mod);
	 } else
	    logLine("Already Exists");

	 log(as.matScene + " : ");
	 if ( !Globals.doesNodeExists(w, as.matScene) )
	 {
	    logLine("Building");
	    NodeMod mod = Globals.registerNode(w, as.matScene, "ma", Plugins.editorMaya(w));
	    addedNodes.add(as.matScene);
	    BaseAction act = Plugins.actionMayaBuild(w);
	    Globals.referenceNode(w, as.matScene, as.modScene, act, REF, "mod");
	    if ( sepHead )
	    {
	       Globals.referenceNode(w, as.matScene, as.headModScene, act, REF, "head");
	    }
	    if ( texture && !shade )
	       client.link(user, view, as.matScene, as.texGroup, REF, LINKALL, null);
	    mod.setAction(act);
	    doReqs(mod);
	    client.modifyProperties(user, view, mod);
	 } else
	    logLine("Already Exists");
	 
	 
	 log(as.matExpScene + " : ");
	 if ( !Globals.doesNodeExists(w, as.matExpScene) )
	 {
	    logLine("Building");
	    NodeMod mod = Globals.registerNode(w, as.matExpScene, "ma", Plugins.editorMaya(w));
	    client.link(w.user, w.view, as.matExpScene, as.matScene, DEP, LINKALL, null);
	    BaseAction act = Plugins.actionMayaShaderExport(w);
	    act.setSingleParamValue("SelectionPrefix", "");
	    act.setSingleParamValue("MayaScene", as.matScene);
	    mod.setAction(act);
	    client.modifyProperties(w.user, w.view, mod);
	 } else
	    logLine("Already Exists");

	 log(as.finalScene + " : ");
	 if ( !Globals.doesNodeExists(w, as.finalScene) )
	 {
	    logLine("Building");
	    NodeMod mod = Globals.registerNode(w, as.finalScene, "ma", Plugins
	       .editorMaya(w));
	    addedNodes.add(as.finalScene);
	    BaseAction act = Plugins.actionMayaBuild(w);
	    client.link(w.user, w.view, as.finalScene, as.matScene, DEP, LINKALL, null);
	    client.link(w.user, w.view, as.finalScene, as.rigScene, DEP, LINKALL, null);
	    client.link(w.user, w.view, as.finalScene, as.matExpScene, DEP, LINKALL, null);   
	    act.initSourceParams(as.matScene);
	    act.setSourceParamValue(as.matScene, "BuildType", "Reference");
	    act.setSourceParamValue(as.matScene, "NameSpace", true);
	    act.setSourceParamValue(as.matScene, "PrefixName", "source");
	    act.initSourceParams(as.rigScene);
	    act.setSourceParamValue(as.rigScene, "BuildType", "Import");
	    act.setSourceParamValue(as.rigScene, "NameSpace", false);
	    act.initSourceParams(as.matExpScene);
	    act.setSourceParamValue(as.matExpScene, "BuildType", "Import");
	    act.setSourceParamValue(as.matExpScene, "NameSpace", false);	    
	    if ( hasFinalizeMel )
	    {
	       client.link(user, view, as.finalScene, finalizeMel, DEP, LINKALL, null);
	       act.setSingleParamValue("ModelMEL", finalizeMel);
	    }
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
	    BaseAction act = Plugins.actionMayaBuild(w);
	    Globals.referenceNode(w, as.lr_matScene, as.modScene, act, REF, "mod");
	    if ( sepHead )
	    {
	       Globals.referenceNode(w, as.lr_matScene, as.headModScene, act, REF, "head");
	    }
	    client.link(user, view, as.lr_matScene, as.texGroup, REF, LINKALL, null);
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
	    BaseAction act = Plugins.actionMayaBuild(w);
	    client.link(user, view, as.lr_finalScene, as.rigScene, DEP, LINKALL,
	       null);
	    act.initSourceParams(as.rigScene);
	    act.setSourceParamValue(as.rigScene, "BuildType", "Import");
	    act.setSourceParamValue(as.rigScene, "NameSpace", false);
	    if ( hasLRFinalizeMel )
	    {
	       client.link(user, view, as.lr_finalScene, lrFinalizeMel, DEP, LINKALL, null);
	       act.setSingleParamValue("ModelMEL", lrFinalizeMel);
	    }
	    mod.setAction(act);
	    doReqs(mod);
	    client.modifyProperties(user, view, mod);
	 } else
	    logLine("Already Exists");
	 

	 if ( shade )
	 {
	    log(as.shdIncGroup + " : ");
	    if ( !Globals.doesNodeExists(w, as.shdIncGroup) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, as.shdIncGroup, "mi", Plugins
		  .editorEmacs(w));
	       addedNodes.add(as.shdIncGroup);
	       mod.addSecondarySequence(as.shdIncGroupSecSeq);
	       BaseAction act = Plugins.actionMRayShaderInclude(w);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");

	    log(as.shdScene + " : ");
	    if ( !Globals.doesNodeExists(w, as.shdScene) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, as.shdScene, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(as.shdScene);
	       BaseAction act = Plugins.actionMayaReference(w);
	       Globals.referenceNode(w, as.shdScene, as.finalScene, act, REF, "final");
	       client.link(user, view, as.shdScene, as.shdIncGroup, REF, LINKALL, null);
	       if ( texture )
		  client.link(user, view, as.shdScene, as.texGroup, REF, LINKALL, null);
	       if ( hasMRInitMel )
	       {
		  client.link(user, view, as.shdScene, mrInitMel, DEP, LINKALL, null);
		  act.setSingleParamValue("InitialMEL", mrInitMel);
	       }
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");

	    log(as.shdExport + " : ");
	    if ( !Globals.doesNodeExists(w, as.shdExport) )
	    {
	       logLine("Building");
	       NodeMod mod = Globals.registerNode(w, as.shdExport, "ma", Plugins
		  .editorMaya(w));
	       addedNodes.add(as.shdExport);
	       BaseAction act = Plugins.actionMayaShaderExport(w);
	       client.link(user, view, as.shdExport, as.shdScene, DEP, LINKALL, null);
	       act.setSingleParamValue("SelectionPrefix", as.assetName);
	       act.setSingleParamValue("MayaScene", as.shdScene);
	       mod.setAction(act);
	       doReqs(mod);
	       client.modifyProperties(user, view, mod);
	    } else
	       logLine("Already Exists");
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

      queueAndWait(as, shade);
      OverallQueueState state = checkStatuses(as, shade);
      logLine("Queue state returned of " + state);
      JConfirmDialog checkDialog;
      if ( !state.equals(OverallQueueState.Finished) )
      {
	JToolDialog tool = new JToolDialog("BuildAsset", new JPanel(), "Continue");
	 checkDialog = new JConfirmDialog(tool, "One or more of the jobs associated with ("
	       + assetName + ") did not successfully generate its nodes.\n"
	       + "  Do you wish to continue?");
	 checkDialog.setVisible(true);
	 if ( !checkDialog.wasConfirmed() )
	    return;
      }

      {
	 if ( hasPlaceholderMel )
	 {
	    NodeMod mod = client.getWorkingVersion(user, view, as.modScene);
	    mod.setAction(null);
	    client.unlink(user, view, as.modScene, placeholderMel);
	    client.modifyProperties(user, view, mod);
	 }

	 Globals.disableAction(w, as.matScene);
	 Globals.disableAction(w, as.lr_matScene);
	 if ( shade )
	    Globals.disableAction(w, as.shdScene);

	 //if ( hasPlaceholderMel )
	    queueAndWait(as, shade);
	 state = checkStatuses(as, shade);
	 logLine("Queue state returned of " + state);
//	 if ( state.equals(OverallQueueState.Finished) )
//	 {
//	    checkDialog = new JConfirmDialog("All jobs finished successfully.  \n"
//		  + "Do you want to check the nodes in?");
//	    checkDialog.setVisible(true);
//	    if ( checkDialog.wasConfirmed() )
//	       checkinNodes(as, shade);
//	 } else
//	    logLine("Jobs did not complete successfully.  \n"
//		  + "Unable to check-in the asset.  \n"
//		  + "Please check and see what happened");
//
      }
   }

   private void doReqs(NodeMod mod) throws PipelineException
   {
      JobReqs req = mod.getJobRequirements();
      req.addSelectionKeys(usedKeys);
      mod.setJobRequirements(req);
   }

   private void checkinNodes(SonyAsset as, boolean shade)
      throws PipelineException
   {
      NodeID nodeID;
      if ( shade )
      {
	 nodeID = new NodeID(user, view, as.shdExport);
      } else
      {
	 nodeID = new NodeID(user, view, as.finalScene);
      }
      client.checkIn(nodeID, "Inital model tree built by the Asset Builder GUI.",
	 VersionID.Level.Minor);

      nodeID = new NodeID(user, view, as.lr_finalScene);
      client.checkIn(nodeID, "Inital model tree built by the Asset Builder GUI.",
	 VersionID.Level.Minor);
      nodeID = new NodeID(user, view, as.lr_matScene);
      client.checkIn(nodeID, "Inital model tree built by the Asset Builder GUI.",
	 VersionID.Level.Minor);  
   }

   private OverallQueueState checkStatuses(SonyAsset as, boolean shade)
      throws PipelineException
   {
      NodeStatus status = null;
      if ( shade )
	 status = client.status(user, view, as.shdExport);
      else
	 status = client.status(user, view, as.finalScene);

      OverallQueueState one = Globals.getTreeState(status);

      if ( one.equals(OverallQueueState.Finished) )
      {
	 status = client.status(user, view, as.lr_finalScene);
	 OverallQueueState two = Globals.getTreeState(status);
	 if ( two.equals(OverallQueueState.Finished) )
	 {
	    status = client.status(user, view, as.lr_matScene);
	    OverallQueueState three= Globals.getTreeState(status);
	    return three;
	 } else
	    return two;
      } else
	 return one;
   }

   private void queueAndWait(SonyAsset as, boolean shade)
      throws PipelineException
   {
      LinkedList<QueueJobGroup> queueJobs = new LinkedList<QueueJobGroup>();

      logLine("Queuing the jobs now.");
      if ( shade )
      {
	 try
	 {
	    queueJobs.add(client.submitJobs(user, view, as.shdExport, null));
	 } catch ( PipelineException ex )
	 {
	    System.err.println("No New Shade Jobs");
	 }
      }
      else
      {
	 try
	 {
	    queueJobs.add(client.submitJobs(user, view, as.finalScene, null));
	 } catch ( PipelineException ex )
	 {
	    System.err.println("No New Final Jobs");
	 }
      }
      try
      {
	 queueJobs.add(client.submitJobs(user, view, as.lr_finalScene, null));
      } catch ( PipelineException ex )
      {
	 System.err.println("No New Low Rez Final Jobs");
      }
      try
      {
	 queueJobs.add(client.submitJobs(user, view, as.lr_matScene, null));
      } catch ( PipelineException ex )
      {
	 System.err.println("No New Low Rez Material Jobs");
      }


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
	       throw new PipelineException("The job for the (" + as.assetName
		     + ") did not complete correctly.");
	    if ( !done )
	       break;
	 }
	 log("...");
	 if ( !done )
	 {
	    try
	    {
	       Thread.sleep(5000);
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
      diag = new JToolDialog("Build Asset", scroll, "Continue");

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
	 {
	    userField.addActionListener(this);
	    viewField.addActionListener(this);
	    toolsetField.addActionListener(this);
	 }
	 hbox.add(comps[2]);
	 top.add(hbox);
      }
      w = new Wrapper(userField.getSelected(), viewField.getSelected(), toolsetField
	 .getSelected(), client);

      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];
	 {
	    projectField = UIFactory.createTitledCollectionField(tpanel, "Project:",
	       sTSize, vpanel, SonyConstants.getProjectList(w), diag, sVSize,
	       "All the projects in pipeline.");
	    projectField.setActionCommand("proj");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    projectBooleanField = UIFactory.createTitledBooleanField(tpanel,
	       "Make New Project:", sTSize, vpanel, sVSize,
	       "Should these assets be in a new project");
	    projectBooleanField.setValue(false);
	    projectBooleanField.setActionCommand("proj");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    projectNameField = UIFactory.createTitledEditableTextField(tpanel,
	       "New Project Name:", sTSize, vpanel, null, sVSize,
	       "The name of the new project");
	    projectNameField.setEnabled(false);
	    projectNameField.setActionCommand("proj");
	 }
	 {
	    projectField.addActionListener(this);
	    projectBooleanField.addActionListener(this);
	    projectNameField.addActionListener(this);
	 }
	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Project Settings", hbox, true);
	 top.add(draw);
      }
      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 {
	    List<String> values = Arrays.asList(AssetType.stringValues());
	    assetTypeField = UIFactory.createTitledCollectionField(tpanel,
	       "Asset Type:", sTSize, vpanel, values, sVSize, "The type of this asset");
	    assetTypeField.setSelected(AssetType.CHARACTER.toString());
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    placeholderMelField = UIFactory.createTitledCollectionField(tpanel,
	       "Model Placeholder:", sTSize, vpanel, getRightMel(), diag, sVSize,
	       "A mel script to generate a place holder model.  "
		     + "Select NONE if you don't want to use one.");

	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    finalizeMelField = UIFactory.createTitledCollectionField(tpanel,
	       "Finalize Script:", sTSize, vpanel, getRightMel(), diag, sVSize,
	       "A mel script to finalize the models. "
		     + "Select NONE if don't want to use one.");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 {
	    lrFinalizeMelField = UIFactory.createTitledCollectionField(tpanel,
	       "Low-Rez Finalize Script:", sTSize, vpanel, getRightMel(), diag, sVSize,
	       "A mel script to finalize the low-rez models. "
		     + "Select NONE if don't want to use one.");
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	 {
	    mrInitMelField = UIFactory.createTitledCollectionField(tpanel,
	       "MR Init for Shade Scene:", sTSize, vpanel, getRightMel(), diag, sVSize,
	       "A mel script to initialize mental ray in the shader scene "
		     + "if it is not loaded by default.  This is not used if you aren't"
		     + "using the advanced shader setup.  "
		     + "Select NONE is you don't want to use one.");
	 }
	 hbox.add(comps[2]);
	 JDrawer draw = new JDrawer("Global Asset Settings", hbox, true);
	 top.add(draw);
      }

      {
	 Box hbox = new Box(BoxLayout.X_AXIS);
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 TreeSet<String> keys = queue.getSelectionKeyNames();
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
	 JButton button = new JButton("Add Another Asset");
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

      top.add(assetBox());

      finalBox.add(top);

      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

	 finalBox.add(spanel);
      }

      diag.setVisible(true);
      if ( diag.wasConfirmed() )
	 return true;
      return false;
   }

   private ArrayList<String> getRightMel() throws PipelineException
   {
      ArrayList<String> toReturn = new ArrayList<String>();
      if ( projectBooleanField.getValue() )
      {
	 toReturn = new ArrayList<String>(SonyConstants.getAllMelWithPrefix(w, null)
	    .keySet());
	 toReturn.add(0, "NONE");
      } else
      {
	 String name = projectField.getSelected();
	 toReturn = new ArrayList<String>(SonyConstants.getAllMelWithPrefix(w, name)
	    .keySet());
	 toReturn.add(0, "NONE");
      }
      return toReturn;
   }

   private JDrawer assetBox()
   {
      JDrawer toReturn;
      Box stuff = new Box(BoxLayout.X_AXIS);
      int num = assetNameFields.size();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      {
	 JTextField field = UIFactory.createTitledEditableTextField(tpanel, "Asset Name:",
	    sTSize, vpanel, null, sVSize, "The name of the asset");
	 assetNameFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JBooleanField field = UIFactory.createTitledBooleanField(tpanel, "Has Separate Head:",
	    sTSize, vpanel, sVSize, "Does this model need a separate head for blend-shape work");
	 field.setValue(true);
	 sepHeadFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JBooleanField field = UIFactory.createTitledBooleanField(tpanel,
	    "Advanced Shade:", sTSize, vpanel, sVSize,
	    "Should an advanced shading network be setup.");
	 field.setValue(false);
	 advancedShadeFields.add(num, field);
      }
      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      {
	 JBooleanField field = UIFactory.createTitledBooleanField(tpanel, "Texture Stub:",
	    sTSize, vpanel, sVSize, "Should a texture stub node be created.");
	 field.setValue(true);
	 textureStubFields.add(num, field);
      }
      stuff.add(comps[2]);
      toReturn = new JDrawer("Add Asset", stuff, false);
      return toReturn;
   }

   TreeMap<String, TreeSet<String>> areas;
   TreeMap<String, String> mels;

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
	 boolean projName = projectBooleanField.getValue();
	 projectNameField.setEnabled(projName);
	 projectField.setEnabled(!projName);

	 String placeholderMel = placeholderMelField.getSelected();
	 String finalizeMel = finalizeMelField.getSelected();
	 String lrFinalizeMel = lrFinalizeMelField.getSelected();
	 String mrInitMel = mrInitMelField.getSelected();

	 ArrayList<String> melList = null;
	 try
	 {
	    melList = getRightMel();
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
	 placeholderMelField.setValues(melList);
	 if ( melList.contains(placeholderMel) )
	    placeholderMelField.setSelected(placeholderMel);
	 finalizeMelField.setValues(melList);
	 if ( melList.contains(finalizeMel) )
	    finalizeMelField.setSelected(finalizeMel);
	 lrFinalizeMelField.setValues(melList);
	 if ( melList.contains(lrFinalizeMel) )
	    lrFinalizeMelField.setSelected(lrFinalizeMel);
	 mrInitMelField.setValues(melList);
	 if ( melList.contains(mrInitMel) )
	    mrInitMelField.setSelected(mrInitMel);
      } else if ( com.equals("add") )
      {
	 top.add(assetBox());
	 diag.validate();
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

   private String user;
   private String view;
   private String project;
   private boolean verbose;
   private JToolDialog diag;
   private Wrapper w;

   private Box top;

   private ArrayList<Integer> validatedAssets;

   private ArrayList<JTextField> assetNameFields;

   private ArrayList<JBooleanField> advancedShadeFields;
   private ArrayList<JBooleanField> textureStubFields;
   private ArrayList<JBooleanField> sepHeadFields;

   private TreeMap<String, JBooleanField> selectionKeyFields;

   private TreeSet<String> usedKeys;

   private JCollectionField userField;
   private JCollectionField viewField;
   private JCollectionField toolsetField;
   private JCollectionField projectField;
   private JCollectionField placeholderMelField;
   private JCollectionField finalizeMelField;
   private JCollectionField lrFinalizeMelField;
   private JCollectionField mrInitMelField;
   private JCollectionField assetTypeField;

   private JTextField projectNameField;

   private JBooleanField projectBooleanField;

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

   private static final int sTSize = 150;
   private static final int sVSize = 200;

}

package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.ui.*;
/**
 * Unused class that was orignally intended to build the asset tree for Lair. <P>
 * 
 * This approach became much too complicated and suffered greatly from being in the
 * Tool format.  The inherent limitations of the tool setup (which could now be 
 * overcome using the new JAR format for tools) created way too much bloat in this class,
 * making it a development nightmare.  There is some stuff in here which might be useful
 * as reference, but pretty much everything here has been done better somewhere else.
 * 
 * 
 * @author Jesse Clemens
 */
@SuppressWarnings("unused")
public class BuildAssetTreeTool extends BaseTool
{
   private static final long serialVersionUID = 1323864046482371048L;

   /**
    * Builds the asset tree for a selected leaf model node.
    */
   public BuildAssetTreeTool()
   {
      super("BuildAssetTree", new VersionID("1.0.0"), "SCEA",
	 "Builds the asset tree for a selected leaf model node.");

      underDevelopment();

      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);

      // plug = PluginMgrClient.getInstance();

      /*
       * assetTypes = new ArrayList<String>(); assetTypes.add(CHAR,
       * "Character"); assetTypes.add(PROP, "Prop"); assetTypes.add(SET, "Set");
       */

      pPhase = 1;
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      switch (pPhase)
      {
	 case 1:
	    return collectFirstPhaseInput();
	 case 2:
	    return collectSecondPhaseInput();
	 default:
	    throw new PipelineException("This Should never happen.  pPhase is off");
      }
   }

   public synchronized String collectFirstPhaseInput() throws PipelineException
   {
      if ( pSelected.size() == 0 )
	 return null;

      Box vbox = new Box(BoxLayout.Y_AXIS);

      toolsetMap = new TreeMap<String, String>();
      rigNameMap = new TreeMap<String, String>();
      matNameMap = new TreeMap<String, String>();
      assetStartMap = new TreeMap<String, String>();
      finalNameMap = new TreeMap<String, String>();
      finalizeFieldMap = new TreeMap<String, JTextField>();
      projectStartMap = new TreeMap<String, String>();

      for (String name : pSelected.keySet())
      {
	 int switcher = 0;
	 if ( name.matches(hirezModelPattern) )
	 {
	    switcher = HIREZ;
	 } else if ( name.matches(lowrezModelPattern) )
	 {
	    switcher = LOREZ;
	 } else
	 {
	    throw new PipelineException("The selected node (" + name + ") "
		  + "does not match the naming conventions for SCEA projects and"
		  + "an asset tree can therefore not be generated for it.");
	 }

	 NodeStatus status = pSelected.get(name);
	 NodeID nodeID = status.getNodeID();
	 user = nodeID.getAuthor();
	 view = nodeID.getView();
	 toolsetMap.put(name, status.getDetails().getWorkingVersion().getToolset());

	 String projectPrefix;
	 String assetType;
	 {
	    String buffer[];
	    buffer = name.split("/");
	    int current = 0;
	    StringBuffer prefix = new StringBuffer();
	    while ( !buffer[current].equals("assets") )
	    {
	       prefix.append(buffer[current] + "/");
	       current++;
	    }
	    current++;
	    assetType = buffer[current];
	    projectPrefix = prefix.toString();
	 }
	 projectStartMap.put(name, projectPrefix);

	 String finalizeMelName = projectPrefix + finalizeMelEnd;

	 Path p = new Path(name);

	 String assetName = null;
	 switch (switcher)
	 {
	    case HIREZ:
	       assetName = p.getName().replace("_mod", "");
	       break;
	    case LOREZ:
	       assetName = p.getName().replace("_mod_lr", "");
	       break;
	 }
	 String assetStart = projectPrefix + "assets/" + assetType + "/" + assetName;
	 String rigScene = assetStart + "/rig/" + assetName + "_rig";
	 String matScene = assetStart + "/material/" + assetName + "_mat";
	 String finalScene = assetStart + "/" + assetName;

	 if ( switcher == LOREZ )
	 {
	    rigScene += "_lr";
	    matScene += "_lr";
	    finalScene += "_lr";
	 }
	 assetStartMap.put(name, assetStart);
	 rigNameMap.put(name, rigScene);
	 matNameMap.put(name, matScene);
	 finalNameMap.put(name, finalScene);

	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 {
	    JTextField textF = UIFactory.createTitledEditableTextField(tpanel,
	       "FinalizeMel", sTSize, vpanel, finalizeMelName, sVSize2,
	       "The finalize mel to use on the script. "
		     + "Set to the default project mel initially.");
	    finalizeFieldMap.put(name, textF);
	 }
	 JDrawer drawer = new JDrawer(name, (JComponent) comps[2], true);
	 vbox.add(drawer);
      }

      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize2, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize2, 7));

	 vbox.add(spanel);
      }

      JScrollPane scroll = new JScrollPane(vbox);
      Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
      scroll.setMinimumSize(size);
      scroll.setPreferredSize(size);

      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      JToolDialog diag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");

      diag.setVisible(true);

      if ( diag.wasConfirmed() )
      {
	 return ": gathering information.";
      }
      return null;
   }

   private String collectSecondPhaseInput()
   {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      rigActionFieldMap = new TreeMap<String, JCollectionField>();
      matActionFieldMap = new TreeMap<String, JCollectionField>();
      finalActionFieldMap = new TreeMap<String, JCollectionField>();
      melActionFieldMap = new TreeMap<String, JCollectionField>();

      for (String name : assetStartMap.keySet())
      {
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 {
	    int state = finalizeMelStateMap.get(name);
	    String text = getExplanation(state);
	    ArrayList<String> options = getMelOptions(state);

	    UIFactory.createTitledTextArea(tpanel, "MelInfo", sTSize, vpanel, text,
	       sVSize2, 6, true);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    JCollectionField collF = UIFactory.createTitledCollectionField(tpanel,
	       "MelAction", sTSize, vpanel, options, sVSize2,
	       "What Action do you wish to take?");
	    melActionFieldMap.put(name, collF);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	 {
	    int state = rigStateMap.get(name);
	    String text = getExplanation(state);
	    ArrayList<String> options = getOptions(state);

	    UIFactory.createTitledTextArea(tpanel, "RigInfo", sTSize, vpanel, text,
	       sVSize2, 6, true);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    JCollectionField collF = UIFactory.createTitledCollectionField(tpanel,
	       "RigAction", sTSize, vpanel, options, sVSize2,
	       "What Action do you wish to take?");
	    rigActionFieldMap.put(name, collF);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	 {
	    int state = matStateMap.get(name);
	    String text = getExplanation(state);
	    ArrayList<String> options = getOptions(state);

	    UIFactory.createTitledTextArea(tpanel, "MatInfo", sTSize, vpanel, text,
	       sVSize2, 6, true);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    JCollectionField collF = UIFactory.createTitledCollectionField(tpanel,
	       "MatAction", sTSize, vpanel, options, sVSize2,
	       "What Action do you wish to take?");
	    matActionFieldMap.put(name, collF);
	 }
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	 {
	    int state = matStateMap.get(name);
	    String text = getExplanation(state);
	    ArrayList<String> options = getOptions(state);

	    UIFactory.createTitledTextArea(tpanel, "FinalInfo", sTSize, vpanel, text,
	       sVSize2, 6, true);
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    JCollectionField collF = UIFactory.createTitledCollectionField(tpanel,
	       "FinalAction", sTSize, vpanel, options, sVSize2,
	       "What Action do you wish to take?");
	    finalActionFieldMap.put(name, collF);
	 }
	 UIFactory.addVerticalGlue(tpanel, vpanel);

	 JDrawer drawer = new JDrawer(name, (JComponent) comps[2], true);
	 vbox.add(drawer);
      }
      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize + sVSize2, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize + sVSize2, 7));

	 vbox.add(spanel);
      }

      JScrollPane scroll = new JScrollPane(vbox);
      Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
      scroll.setMinimumSize(size);
      scroll.setPreferredSize(size);

      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      JToolDialog diag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");

      diag.setVisible(true);

      if ( diag.wasConfirmed() )
      {
	 return ": Building the Scenes now";
      }
      return null;

   }

   // private static final int BADPATH = -1;
   // private static final int DOESNTEXIST = 0;
   // private static final int NEVERCHECKEDIN = 1;
   // private static final int EXISTS = 2;
   // private static final int CONNECTED = 3;
   // private static final int EXISTSCURRENT = 4;
   // private static final int CONNECTEDCURRENT = 5;
   // private static final int EXISTSCURRENT_CONNECTED = 6;

   // private static final String COM_CheckOut = "1. Check Out";
   // private static final String COM_CheckOutLink = "2. Check Out and Link";
   // private static final String COM_Create = "3. Create and Link";
   // private static final String COM_Link = "4. Link";
   // private static final String COM_Nothing = "5. Do Nothing";
   // private static final String COM_Cancel = "6. Halt Operation on this
   // Asset";

   private ArrayList<String> getOptions(int state)
   {
      ArrayList<String> toReturn = new ArrayList<String>();
      switch (state)
      {
	 case BADPATH:
	    toReturn.add(COM_Cancel);
	    break;
	 case DOESNTEXIST:
	    toReturn.add(COM_Create);
	    toReturn.add(COM_Cancel);
	    break;
	 case NEVERCHECKEDIN:
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTS:
	    toReturn.add(COM_CheckOutLink);
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Cancel);
	    break;
	 case CONNECTED:
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTSCURRENT:
	    toReturn.add(COM_Link);
	    toReturn.add(COM_Cancel);
	    break;
	 case CONNECTEDCURRENT:
	    toReturn.add(COM_Nothing);
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTSCURRENT_CONNECTED:
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Nothing);
	    toReturn.add(COM_Cancel);
      }
      return toReturn;
   }

   private ArrayList<String> getMelOptions(int state)
   {
      ArrayList<String> toReturn = new ArrayList<String>();
      switch (state)
      {
	 case BADPATH:
	    toReturn.add(COM_Cancel);
	    break;
	 case DOESNTEXIST:
	    toReturn.add(COM_Cancel);
	    break;
	 case NEVERCHECKEDIN:
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTS:
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Cancel);
	    break;
	 case CONNECTED:
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTSCURRENT:
	    toReturn.add(COM_Link);
	    toReturn.add(COM_Cancel);
	    break;
	 case CONNECTEDCURRENT:
	    toReturn.add(COM_Nothing);
	    toReturn.add(COM_Cancel);
	    break;
	 case EXISTSCURRENT_CONNECTED:
	    toReturn.add(COM_CheckOut);
	    toReturn.add(COM_Nothing);
	    toReturn.add(COM_Cancel);
      }
      return toReturn;
   }

   private String getExplanation(Integer state)
   {
      switch (state)
      {
	 case BADPATH:
	    return "The path that was specified was not a valid pipeline path.";
	 case DOESNTEXIST:
	    return "There is no node that exists with that name.";
	 case NEVERCHECKEDIN:
	    return "This node exists, but is in someone else's working area and has never been checked-in";
	 case EXISTS:
	    return "The node exists in the repository, but is not connected to the node that should be its source node.";
	 case CONNECTED:
	    return "The node exists in the respository and is connected to its source node.";
	 case EXISTSCURRENT:
	    return "The node exists in your working area, but is not connected correctly.  Linking could cause files to be overwriten by a Pipeline Action";
	 case CONNECTEDCURRENT:
	    return "The node exists in your working area and is connected to its source node.";
	 case EXISTSCURRENT_CONNECTED:
	    return "The node exists in your working area, but is not connected to the node that should be its source node.  However, a newer version of the node exists in the respository which is connected to the source node.";
      }
      return null;
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      switch (pPhase)
      {
	 case 1:
	    return executeFirstPhase(mclient, qclient);
	 case 2:
	    return executeSecondPhase(mclient, qclient);
	 default:
	    throw new PipelineException("This Should never happen.  pPhase is off");
      }
   }

   private boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
      for (String name : assetStartMap.keySet())
      {
	 comps.put(assetStartMap.get(name), true);
	 String s = finalizeFieldMap.get(name).getText();
	 comps.put(s, false);
      }
      NodeTreeComp treeComps = mclient.updatePaths(user, view, comps);

      rigStateMap = new TreeMap<String, Integer>();
      matStateMap = new TreeMap<String, Integer>();
      finalStateMap = new TreeMap<String, Integer>();
      finalizeMelStateMap = new TreeMap<String, Integer>();

      for (String name : toolsetMap.keySet())
      {
	 String rigScene = rigNameMap.get(name);
	 String matScene = matNameMap.get(name);
	 String finalScene = finalNameMap.get(name);
	 String finalizeMel = finalizeFieldMap.get(name).getText();

	 State rigState = getState(treeComps, rigScene);
	 State matState = getState(treeComps, matScene);
	 State finalState = getState(treeComps, finalScene);
	 State melState = getState(treeComps, finalizeMel);

	 // rigStateMap.put(name, rigState);
	 // matStateMap.put(name, matState);
	 // finalStateMap.put(name, finalState);
	 // finalizeMelStateMap.put(name, melState);

	 rigStateMap.put(name, calculateState(rigScene, name, rigState, mclient));
	 matStateMap.put(name, calculateState(matScene, rigScene, matState, mclient));
	 finalStateMap.put(name, calculateState(finalScene, matScene, finalState, mclient));
	 finalizeMelStateMap.put(name, calculateMelState(finalizeMel, finalScene, melState,
	    finalState, mclient));
      }
      pPhase++;
      return true;
   }

   private boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      for (String name : toolsetMap.keySet())
      {
	 String toolset = toolsetMap.get(name);
	 PluginMenuLayout editorLayout = mclient.getEditorMenuLayout(toolset);
	 VersionID mayaID = findID(editorLayout, "Temerity", "Maya");
	 if ( mayaID == null )
	    throw new PipelineException("There is no version of the Maya editor"
		  + "supported by the toolset that (" + name + ") uses");
	 // BaseEditor mayaEditor = plug.newEditor("Maya", mayaID, "Temerity");
      }
      return false;
   }

   private State getState(NodeTreeComp treeComps, String scene)
   {
      State toReturn = null;
      Path p = new Path(scene);
      NodeTreeComp dest = null;
      for (String s : p.getComponents())
      {
	 if ( dest == null )
	    dest = treeComps.get(s);
	 else
	    dest = dest.get(s);

	 if ( dest == null )
	    break;
      }
      if ( dest != null )
	 toReturn = dest.getState();

      return toReturn;
   }

   private VersionID findID(PluginMenuLayout editorLayout, String vendor, String name)
   {
      if ( !editorLayout.isSubmenu() )
      {
	 if ( editorLayout.getVendor().equals(vendor)
	       && editorLayout.getName().equals(name) )
	    return editorLayout.getVersionID();

	 return null;
      }
      for (PluginMenuLayout layout : editorLayout)
      {
	 VersionID id = findID(layout, vendor, name);
	 if ( id != null )
	    return id;
      }
      return null;
   }

   private Integer calculateMelState(String source, String target, State sourceState,
	 State targetState, MasterMgrClient mclient) throws PipelineException
   {
      if ( sourceState == null )
	 return DOESNTEXIST;

      boolean targetCheckedIn = false;
      boolean targetCurrent = false;
      if ( targetState != null )
      {
	 switch (targetState)
	 {
	    case WorkingCurrentCheckedInSome:
	    case WorkingNoneCheckedInSome:
	    case WorkingOtherCheckedInSome:
	       targetCheckedIn = true;
	 }
	 switch (targetState)
	 {
	    case WorkingCurrentCheckedInNone:
	    case WorkingCurrentCheckedInSome:
	       targetCurrent = true;
	 }
      }

      switch (sourceState)
      {
	 case Branch:
	    return BADPATH;
	 case WorkingOtherCheckedInNone:
	    return NEVERCHECKEDIN;
	 case WorkingNoneCheckedInSome:
	 case WorkingOtherCheckedInSome: {
	    if ( targetCheckedIn )
	    {
	       TreeMap<VersionID, NodeVersion> versions = mclient
		  .getAllCheckedInVersions(target);
	       VersionID last = new TreeSet<VersionID>(versions.keySet()).last();
	       NodeVersion ver = versions.get(last);
	       LinkVersion link = ver.getSource(source);
	       if ( link == null )
		  return EXISTS;
	       else
		  return CONNECTED;
	    }
	    return EXISTS;
	 }
	 case WorkingCurrentCheckedInNone: {
	    if ( targetCurrent )
	    {
	       NodeMod mod = mclient.getWorkingVersion(user, view, target);
	       LinkMod link = mod.getSource(source);
	       if ( link == null )
		  return EXISTSCURRENT;
	       else
		  return CONNECTEDCURRENT;
	    }
	    return EXISTSCURRENT;
	 }
	 case WorkingCurrentCheckedInSome: {
	    if ( targetCheckedIn )
	    {
	       TreeMap<VersionID, NodeVersion> versions = mclient
		  .getAllCheckedInVersions(target);
	       VersionID last = new TreeSet<VersionID>(versions.keySet()).last();
	       if ( targetCurrent )
	       {
		  NodeMod mod = mclient.getWorkingVersion(user, view, target);
		  VersionID cur = mod.getWorkingID();
		  int compared = cur.compareTo(last);
		  if ( compared < 0 )
		  {
		     NodeVersion ver = versions.get(last);
		     LinkVersion link = ver.getSource(source);
		     if ( link != null )
			return EXISTSCURRENT_CONNECTED;
		  }
	       } else
	       {
		  NodeVersion ver = versions.get(last);
		  LinkVersion link = ver.getSource(source);
		  if ( link == null )
		     return EXISTS;
		  else
		     return CONNECTED;
	       }
	    }
	    if ( targetCurrent )
	    {
	       NodeMod mod = mclient.getWorkingVersion(user, view, target);
	       LinkMod link = mod.getSource(source);
	       if ( link == null )
		  return EXISTSCURRENT;
	       else
		  return CONNECTEDCURRENT;
	    }
	    return EXISTSCURRENT;
	 }
      }
      return BADPATH;
   }

   private int calculateState(String target, String source, State targetState,
	 MasterMgrClient mclient) throws PipelineException
   {
      if ( targetState == null )
	 return DOESNTEXIST;
      switch (targetState)
      {
	 case Branch:
	    return BADPATH;
	 case WorkingOtherCheckedInNone:
	    return NEVERCHECKEDIN;
	 case WorkingNoneCheckedInSome:
	 case WorkingOtherCheckedInSome: {
	    TreeMap<VersionID, NodeVersion> versions = mclient
	       .getAllCheckedInVersions(target);
	    VersionID last = new TreeSet<VersionID>(versions.keySet()).last();
	    NodeVersion ver = versions.get(last);
	    LinkVersion link = ver.getSource(source);
	    if ( link == null )
	       return EXISTS;
	    else
	       return CONNECTED;
	 }
	 case WorkingCurrentCheckedInNone: {
	    NodeMod mod = mclient.getWorkingVersion(user, view, target);
	    LinkMod link = mod.getSource(source);
	    if ( link == null )
	       return EXISTSCURRENT;
	    else
	       return CONNECTEDCURRENT;
	 }
	 case WorkingCurrentCheckedInSome: {
	    TreeMap<VersionID, NodeVersion> versions = mclient
	       .getAllCheckedInVersions(target);
	    VersionID last = new TreeSet<VersionID>(versions.keySet()).last();
	    NodeMod mod = mclient.getWorkingVersion(user, view, target);
	    VersionID cur = mod.getWorkingID();
	    int compared = cur.compareTo(last);
	    if ( compared < 0 )
	    {
	       NodeVersion ver = versions.get(last);
	       LinkVersion link = ver.getSource(source);
	       if ( link != null )
		  return EXISTSCURRENT_CONNECTED;
	    }
	    LinkMod link = mod.getSource(source);
	    if ( link == null )
	       return EXISTSCURRENT;
	    else
	       return CONNECTEDCURRENT;
	 }
      }
      return BADPATH;
   }

   private PluginMgrClient plug;
   private String user;
   private String view;

   public static final LinkPolicy REF = LinkPolicy.Reference;
   public static final LinkPolicy DEP = LinkPolicy.Dependency;
   public static final LinkRelationship LINKALL = LinkRelationship.All;

   // ArrayList<String> assetTypes;

   TreeMap<String, String> projectStartMap;
   TreeMap<String, String> toolsetMap;
   TreeMap<String, String> assetStartMap;
   TreeMap<String, String> rigNameMap;
   TreeMap<String, String> matNameMap;
   TreeMap<String, String> finalNameMap;
   TreeMap<String, JTextField> finalizeFieldMap;
   TreeMap<String, Integer> rigStateMap;
   TreeMap<String, Integer> matStateMap;
   TreeMap<String, Integer> finalStateMap;
   TreeMap<String, Integer> finalizeMelStateMap;
   TreeMap<String, JCollectionField> rigActionFieldMap;
   TreeMap<String, JCollectionField> matActionFieldMap;
   TreeMap<String, JCollectionField> finalActionFieldMap;
   TreeMap<String, JCollectionField> melActionFieldMap;

   String finalizeMelEnd = "assets/tools/mel/finalize-character";

   String hirezModelPattern = ".*/assets/(character|set|prop)/.*/model/.*_mod";
   String lowrezModelPattern = ".*/assets/(character|set|prop)/.*/model/.*_mod_lr";

   private static final int HIREZ = 1;
   private static final int LOREZ = 2;

   // private static final int CHAR = 1;
   // private static final int PROP = 2;
   // private static final int SET = 3;

   private static final int BADPATH = -1;
   private static final int DOESNTEXIST = 0;
   private static final int NEVERCHECKEDIN = 1;
   private static final int EXISTS = 2;
   private static final int CONNECTED = 3;
   private static final int EXISTSCURRENT = 4;
   private static final int CONNECTEDCURRENT = 5;
   private static final int EXISTSCURRENT_CONNECTED = 6;

   private static final String COM_CheckOut = "1. Check Out";
   private static final int COMn_CheckOut = 1;
   private static final String COM_CheckOutLink = "2. Check Out and Link";
   private static final int COMn_CheckOutLink = 2;
   private static final String COM_Create = "3. Create and Link";
   private static final int COMn_Create = 3;
   private static final String COM_Link = "4. Link";
   private static final int COMn_Link = 4;
   private static final String COM_Nothing = "5. Do Nothing";
   private static final int COMn_Nothing = 5;
   private static final String COM_Cancel = "6. Halt Operation on this Asset";
   private static final int COMn_Cancel = 6;
   private int pPhase;

   private static final int sTSize = 120;
   // private static final int sVSize = 400;
   private static final int sVSize2 = 400;

   /* Utility Methods */
   private NodeMod registerNode(MasterMgrClient mclient, String name, String extention,
	 String toolset, BaseEditor editor) throws PipelineException
   {
      Path f = new Path(name);
      FileSeq animSeq = new FileSeq(f.getName(), extention);
      NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
      mclient.register(user, view, animNode);
      return animNode;
   }

   private void referenceNode(MasterMgrClient mclient, String target, String source,
	 BaseAction action, LinkPolicy policy, String namespace) throws PipelineException
   {
      boolean reference = false;

      String actionName = action.getName();
      if ( actionName.equals("MayaReference") )
	 reference = true;

      mclient.link(user, view, target, source, policy, LINKALL, null);
      if ( reference )
      {
	 action.initSourceParams(source);
	 action.setSourceParamValue(source, "PrefixName", namespace);
      }
   }

}

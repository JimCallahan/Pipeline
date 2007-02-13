package com.sony.scea.pipeline.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import com.sony.scea.pipeline.tools.SonyAsset.AssetType;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;
import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.ui.*;

public class AddModelsGUI extends BootApp implements ActionListener
{
   MasterMgrClient client;
   PluginMgrClient plug;
   QueueMgrClient queue;
   LogMgr log;

   public AddModelsGUI()
   {
      try
      {
	 PluginMgrClient.init();
	 client = new MasterMgrClient();
	 queue = new QueueMgrClient();
	 plug = PluginMgrClient.getInstance();
	 log = LogMgr.getInstance();

	 shotAssetMappings = new TreeMap<String, JBooleanField>();
	 shotCameraMappings = new TreeMap<String, JBooleanField>();
	 shotMapping = new TreeMap<String, JBooleanField>();
	 verbose = true;

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

   @Override
   public void run(String[] arg0)
   {

   }

   public static void main(String[] args)
   {
      AddModelsGUI gui = new AddModelsGUI();
      try
      {
	 boolean here = gui.buildGUI();
	 if ( !here )
	    System.exit(0);
	 else
	 {
	    boolean here2 = gui.assetSelection();
	    if ( !here2 )
	       System.exit(0);
	    else
	    {
	       gui.doStuff();
	    }
	 }
	 System.exit(0);
      } catch ( PipelineException e )
      {
	 e.printStackTrace();
	 System.exit(1);
      }
   }

   private boolean buildGUI() throws PipelineException
   {
      Box finalBox = new Box(BoxLayout.Y_AXIS);
      top = new Box(BoxLayout.Y_AXIS);
      projectBox = new Box(BoxLayout.Y_AXIS);

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
      finalBox.add(top);
      doProjectBox();
      finalBox.add(projectBox);

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
      }

      diag.setVisible(true);
      if ( diag.wasConfirmed() )
	 return true;
      return false;
   }

   private void doProjectBox() throws PipelineException
   {
      projectBox.removeAll();
      shotMapping.clear();
      boolean start = true;

      ArrayList<String> movies = SonyConstants.getMovieList(w, project);
      for (String mov : movies)
      {
	 Box mbox = new Box(BoxLayout.Y_AXIS);
	 if ( start )
	    start = false;
	 else
	    projectBox.add(Box.createVerticalStrut(15));
	 mbox.add(Box.createHorizontalStrut(100));
	 mbox.add(Box.createRigidArea(new Dimension(30, 0)));
	 ArrayList<String> seqs = SonyConstants.getSequenceList(w, project, mov);
	 for (String seq : seqs)
	 {
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    //hbox.add(Box.createHorizontalStrut(100));
	    //hbox.add(Box.createRigidArea(new Dimension(30, 30)));
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    ArrayList<String> shots = SonyConstants.getShotList(w, project, mov, seq);
	    for (String shot : shots)
	    {
	       String longShotName = "/projects/" + project + "/production/" + mov + "/"
		     + seq + "/" + shot;
	       JBooleanField field = UIFactory.createTitledBooleanField(tpanel, shot + ":",
		  sTSize, vpanel, sVSize, longShotName);
	       UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	       field.setValue(false);
	       shotMapping.put(longShotName, field);
	    }
	    hbox.add(comps[2]);
	    JDrawer draw = new JDrawer(seq, hbox, false);
	    mbox.add(draw);
	 }
	 JDrawer draw = new JDrawer(mov, mbox, true);
	 projectBox.add(draw);
      }
   }

   private boolean assetSelection() throws PipelineException
   {
      TreeMap<String, String> chars = SonyConstants.getAssetList(w, project,
	 AssetType.CHARACTER);
      TreeMap<String, String> sets = SonyConstants.getAssetList(w, project, AssetType.SET);
      TreeMap<String, String> props = SonyConstants
	 .getAssetList(w, project, AssetType.PROP);
      TreeMap<String, String> cameras = SonyConstants.getCameraList(w, project);
      {
	 JPanel masterPanel = new JPanel();
	 masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
	 masterPanel.add(UIFactory.createPanelLabel(project));

	 JPanel topPanel = new JPanel();
	 topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Characters:"));
	    inside.add(buildInsidePane(chars, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Sets:"));
	    inside.add(buildInsidePane(sets, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Props:"));
	    inside.add(buildInsidePane(props, shotAssetMappings));
	    topPanel.add(inside);
	 }
	 topPanel.add(Box.createHorizontalStrut(4));
	 {
	    JPanel inside = new JPanel();
	    inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
	    inside.add(UIFactory.createPanelLabel("Cameras:"));
	    inside.add(buildInsidePane(cameras, shotCameraMappings));
	    topPanel.add(inside);
	 }
	 masterPanel.add(topPanel);
	 JToolDialog diag1 = new JToolDialog("Select Assets", masterPanel, "Continue");
	 diag1.pack();

	 diag1.setVisible(true);
	 if ( !diag1.wasConfirmed() )
	    return false;
      }
      return true;
   }

   private JScrollPane buildInsidePane(TreeMap<String, String> map,
	 TreeMap<String, JBooleanField> store)
   {
      Box finalBox = new Box(BoxLayout.Y_AXIS);

      JScrollPane scroll;

      {
	 scroll = new JScrollPane(finalBox);

	 scroll
	    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
	    store.put(prefix, field);
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

   private void doStuff() throws PipelineException
   {
      user = userField.getSelected();
      view = viewField.getSelected();

      TreeSet<SonyAsset> assetList = new TreeSet<SonyAsset>();
      TreeMap<String, String> assetMap = SonyConstants.getAllAssetsMap(w, project);
      for (String asset : shotAssetMappings.keySet())
      {
	 if ( shotAssetMappings.get(asset).getValue() )
	 {
	    SonyAsset as = SonyConstants.stringToAsset(w, assetMap.get(asset));
	    assetList.add(as);
	 }
      }

      for (String shot : shotMapping.keySet())
      {
	 boolean test = shotMapping.get(shot).getValue();
	 if ( !test )
	    continue;
	 Path p = new Path(shot);
	 ArrayList<String> pathParts = p.getComponents();
	 int size = pathParts.size();
	 String shotName = pathParts.get(size - 1);
	 String seqName = pathParts.get(size - 2);
	 String movie = pathParts.get(size - 3);
	 SonyShot sh = SonyShot.getShot(w, project, movie, seqName, shotName, null, null);
	 Globals.getNewest(w, sh.animScene, Globals.over, Globals.frozU);
	 sh = SonyShot.getShot(w, project, movie, seqName, shotName, null, null);
	 logLine("Doing Shot: " +  sh.animScene);
	 ArrayList<SonyAsset> particularAssets = new ArrayList<SonyAsset>(assetList);
	 for (SonyAsset as : sh.assets)
	 {
	    logLine("Getting newest Asset: " +  as.assetName);
	    Globals.getNewest(w, as.lr_finalScene, Globals.over, Globals.froz);
	    if ( particularAssets.contains(as) )
	       particularAssets.remove(as);
	 }
	 File script = null;
	 try
	 {
	    script = File.createTempFile("AddModelsGUI.", ".mel", PackageInfo.sTempPath
	       .toFile());
	    FileCleaner.add(script);
	 } catch ( IOException ex )
	 {
	    throw new PipelineException(
	       "Unable to create the temporary MEL script used to collect "
		     + "texture information from the Maya scene!");
	 }
	 try
	 {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
	    TreeMap<String, String> spaces = SonyConstants.getCustomNamespaces(project);

	    for (SonyAsset as : particularAssets)
	    {
	       Globals.getNewest(w, as.lr_finalScene, Globals.over, Globals.froz);
	       logLine("Getting newest Asset: " +  as.assetName);
	       NodeMod mod = client.getWorkingVersion(user, view, as.lr_finalScene);
	       FileSeq fseq = mod.getPrimarySequence();
	       String suffix = fseq.getFilePattern().getSuffix();
	       if ( !fseq.isSingle() || ( suffix == null )
		     || !( suffix.equals("ma") || suffix.equals("mb") ) )
		  continue;
	       Path p1 = new Path(as.lr_finalScene);
	       Path sourcePath = new Path(p1.getParent() + "/" + fseq.getPath(0));

	       String nameSpace = spaces.get(as.assetName);

	       out.println("// MODEL: \" + sourceName");
	       out.println("print (\"referencing file: " + sourcePath + "\");");
	       out.println("file -reference -namespace \"" + nameSpace + "\" \"$WORKING"
		     + sourcePath.toOsString(OsType.Unix) + "\";");
	    }
	    out.println("// SAVE");
	    out.println("file -save;");

	    out.close();
	 } catch ( IOException ex )
	 {
	    throw new PipelineException("Unable to write the temporary MEL script ("
		  + script + ") used add the references!");
	 }

	 NodeID id = new NodeID(user, view, sh.animScene);
	 NodeMod mod = client.getWorkingVersion(id);
	 FileSeq fseq = mod.getPrimarySequence();
	 String suffix = fseq.getFilePattern().getSuffix();
	 if ( !fseq.isSingle() || ( suffix == null )
	       || ( !suffix.equals("ma") && !suffix.equals("mb") ) )
	    throw new PipelineException("The target node (" + sh.animScene
		  + ") must be a maya scene!");
	 Path targetPath = new Path(PackageInfo.sProdPath, id.getWorkingParent() + "/"
	       + fseq.getFile(0));

	 logLine("Running Maya");
	 try
	 {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("-batch");
	    args.add("-script");
	    args.add(script.getPath());
	    args.add("-file");
	    args.add(targetPath.toOsString());
	    
	    logLine(args.toString());

	    Path wdir = new Path(PackageInfo.sProdPath.toOsString() + id.getWorkingParent());
	    TreeMap<String, String> env = client.getToolsetEnvironment(user, view, mod
	       .getToolset(), PackageInfo.sOsType);

	    Map<String, String> nenv = env;
	    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
	    if ( midefs != null )
	    {
	       nenv = new TreeMap<String, String>(env);
	       Path dpath = new Path(new Path(wdir, midefs));
	       nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
	    }

	    String command = "maya";
	    if ( PackageInfo.sOsType.equals(OsType.Windows) )
	       command += ".exe";

	    SubProcessLight proc = new SubProcessLight("AddReferenceTool", command, args,
	       env, wdir.toFile());
	    try
	    {
	       proc.start();
	       proc.join();
	       if ( !proc.wasSuccessful() )
	       {
		  throw new PipelineException(
		     "Did not correctly add the reference due to a maya error.!\n\n"
			   + proc.getStdOut() + "\n\n" + proc.getStdErr());
	       }
	    } catch ( InterruptedException ex )
	    {
	       throw new PipelineException(ex);
	    }
	    logLine(proc.getStdErr());
	    logLine(proc.getCommand());
	 } catch ( Exception ex )
	 {
	    throw new PipelineException(ex);
	 }

	 logLine("Maya Done");
	 for (SonyAsset as : particularAssets)
	 {
	    client.link(user, view, sh.animScene, as.lr_finalScene, LinkPolicy.Reference,
	       LinkRelationship.All, null);
	    
	 }
	 {
	    BaseAction act = mod.getAction();
	    TreeMap<String, String> pNameSpaceMap = SonyConstants.getCustomNamespaces("lr");
	    if ( act.getName().equals("MayaBuild") )
	    {
	       for (SonyAsset as : particularAssets)
	       {
		  String nameSpace = pNameSpaceMap.get(as.assetName);
		  act.initSourceParams(as.lr_finalScene);
		  act.setSourceParamValue(as.lr_finalScene, "PrefixName", nameSpace);
	       }
	       mod.setAction(act);
	       mod.setActionEnabled(false);
	       client.modifyProperties(user, view, mod);
	    }
	 }
	 logLine("");
	 logLine("");
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

	 try
	 {
	    doProjectBox();
	 } catch ( PipelineException e1 )
	 {
	    e1.printStackTrace();
	    System.exit(1);
	 }
      }
   }

   private static final int sTSize = 175;
   private static final int sVSize = 150;

   private TreeMap<String, JBooleanField> shotAssetMappings;
   private TreeMap<String, JBooleanField> shotCameraMappings;
   private TreeMap<String, TreeSet<String>> areas;
   private TreeMap<String, JBooleanField> shotMapping;

   private String user;
   private String view;
   private String project;
   private boolean verbose;
   private JToolDialog diag;
   private Box top;
   private Box projectBox;
   private Wrapper w;

   private JCollectionField userField;
   private JCollectionField viewField;
   private JCollectionField toolsetField;
   private JCollectionField projectField;

}

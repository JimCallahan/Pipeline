// $Id: JArchiveVolumesDialog.java,v 1.4 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   V O L U M E S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for browsing the contents of existing archive volumes.
 */ 
public 
class JArchiveVolumesDialog
  extends JTopLevelDialog
  implements ActionListener, ComponentListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JArchiveVolumesDialog() 
  {
    super("Archive Volumes");

    /* initialize fields */ 
    {
      pArchiveVolumes = new TreeMap<String,ArchiveVolume>();
      pRestoredOn     = new TreeMap<String,TreeSet<Date>>();

      pUpdateLock = new Object();
    }

    /* create dialog body components */ 
    {
      JPanel apanel = new JPanel();
      {
	apanel.setName("MainDialogPanel");
	apanel.setLayout(new BoxLayout(apanel, BoxLayout.X_AXIS));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  vbox.add(UIFactory.createPanelLabel("Archive Volumes:"));

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  {
	    ArchiveVolumeSimpleTableModel model = new ArchiveVolumeSimpleTableModel();
	    pVolumeTableModel = model;
	    
	    JTablePanel tpanel = new JTablePanel(model);
	    pVolumeTablePanel = tpanel;

	    ListSelectionModel smodel = tpanel.getTable().getSelectionModel();
	    smodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    smodel.addListSelectionListener(this);

	    vbox.add(tpanel);
	  }

	  apanel.add(vbox);
	}

	apanel.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  {
	    Box lbox = new Box(BoxLayout.X_AXIS);
	    
	    lbox.add(Box.createRigidArea(new Dimension(4, 0)));
	    
	    {
	      JLabel label = new JLabel("X");
	      pVolumeLabel = label;
	      label.setName("PanelLabel");
	      
	      lbox.add(label);
	    }
	    
	    lbox.add(Box.createHorizontalGlue());

	    vbox.add(lbox);
	  }

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  /* archive details scroll panel */ 
	  {
	    Box sbox = new Box(BoxLayout.Y_AXIS);

	    /* archiver plugin */ 
	    {
	      Box rbox = new Box(BoxLayout.Y_AXIS);

	      {
		Component comps[] = UIFactory.createTitledPanels();
		JPanel tpanel = (JPanel) comps[0];
		JPanel vpanel = (JPanel) comps[1];
		
		pToolsetField = 
		  UIFactory.createTitledTextField
		  (tpanel, "Toolset:", sTSize, 
		   vpanel, "-", sVSize, 
		   "The name of the shell environment under which the Archiver plugin is run.");

		UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

		pArchiverField = 
		  UIFactory.createTitledTextField
		  (tpanel, "Archiver:", sTSize, 
		   vpanel, "", sVSize, 
		   "The name of the Archiver plugin associated with the archive volume.");
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		pArchiverVersionField = 
		  UIFactory.createTitledTextField
		  (tpanel, "Version:", sTSize, 
		   vpanel, "", sVSize, 
		   "The revision number of the Archiver plugin associated with the " + 
		   "archive volume.");
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		pArchiverVendorField = 
		  UIFactory.createTitledTextField
		  (tpanel, "Vendor:", sTSize, 
		   vpanel, "", sVSize, 
		   "The name of the vendor of the Archiver plugin associated with the " + 
		   "archive volume.");
		
		rbox.add(comps[2]);
	      }
	    
	      {
		Box apbox = new Box(BoxLayout.X_AXIS);
		apbox.addComponentListener(this);
		
		{
		  JPanel spanel = new JPanel();
		  spanel.setName("Spacer");
		  
		  spanel.setMinimumSize(new Dimension(7, 0));
		  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
		  spanel.setPreferredSize(new Dimension(7, 0));
		  
		  apbox.add(spanel);
		}
		
		/* archiver parameters */ 
		{
		  Component comps[] = UIFactory.createTitledPanels();
		  JPanel tpanel = (JPanel) comps[0];
		  JPanel vpanel = (JPanel) comps[1];
		  
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
		  JDrawer drawer = 
		    new JDrawer("Archiver Parameters:", (JComponent) comps[2], true);
		  drawer.setToolTipText(UIFactory.formatToolTip
					("Archiver plugin parameters."));
		  pArchiverParamsDrawer = drawer;
		  
		  apbox.add(drawer);
		}

		rbox.add(apbox);
	      }
	      
	      JDrawer drawer = new JDrawer("Archiver Plugin:", rbox, true);
	      sbox.add(drawer);
	    }

	    /* history */ 
	    {
	      Component comps[] = UIFactory.createTitledPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	      JDrawer drawer = new JDrawer("History:", (JComponent) comps[2], true);	      
	      pHistoryDrawer = drawer;
	      
	      sbox.add(drawer);
	    }

	    {
	      JPanel spanel = new JPanel();
	      spanel.setName("Spacer");
	      
	      spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	      spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	      spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	      
	      sbox.add(spanel);
	    }

	    sbox.add(Box.createVerticalGlue());
      
	    {
	      JScrollPane scroll = new JScrollPane(sbox);
	      
	      scroll.setHorizontalScrollBarPolicy
		(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	      scroll.setVerticalScrollBarPolicy
		(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	      
	      Dimension size = new Dimension(sTSize+sVSize+52, 150);
	      scroll.setMinimumSize(size);
	      scroll.setPreferredSize(size);
	      
	      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	      vbox.add(scroll);
	    }
	  }

	  apanel.add(vbox);
	}
      }
      
      /* node versions panel */ 
      JPanel cpanel = new JPanel();
      {
	cpanel.setName("MainDialogPanel");
	cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));

	{
	  Box box = new Box(BoxLayout.X_AXIS);
	  
	  box.add(Box.createRigidArea(new Dimension(4, 0)));

	  {    
	    JLabel label = new JLabel("Archived Versions:");
	    label.setName("PanelLabel");
	    box.add(label);
	  }	    
	  
	  box.add(Box.createHorizontalGlue());
	  
	  {    
	    JLabel label = new JLabel("Total Size: ???");
	    pTotalSizeLabel = label;
	    label.setName("PanelLabel");
	    box.add(label);
	  }
	  
	  box.add(Box.createRigidArea(new Dimension(23, 0)));
	  
	  cpanel.add(box);
	}

	cpanel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  ArchiveVolumeVersionsTableModel model = new ArchiveVolumeVersionsTableModel();
	  pVersionTableModel = model;
	  
	  JTablePanel tpanel = new JTablePanel(model);
	  pVersionTablePanel = tpanel;
	  
	  cpanel.add(tpanel);
	}
      }

      JSplitPane body = new JVertSplitPanel(apanel, cpanel);
      body.setAlignmentX(0.5f);

      String extra[][] = {
	{ "Request Restore", "submit" },
	{ "Update", "update" }
      };

      JButton btns[] = 
	super.initUI("Archive Volume Browser:", body, null, null, extra, "Close");

      btns[0].setToolTipText(UIFactory.formatToolTip
        ("Submit a request to restore the selected checked-in versions."));

      updateDetails(null);
      pack();
    }
  }

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R    I N T E R F A C E                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the archive detailes panel components.
   */
  private void 
  updateDetails
  (
   String aname
  ) 
  {
    ArchiveVolume volume = null;
    if(aname != null) {
      volume = pArchiveVolumes.get(aname);
      pVolumeLabel.setText(aname + ":");
    }
    else {
      pVolumeLabel.setText("None Selected:");
    }

    /* toolset environment */ 
    if(volume != null) 
      pToolsetField.setText(volume.getToolset());
    else 
      pToolsetField.setText("-");

    /* archiver fields */ 
    BaseArchiver archiver = null;
    if(volume != null) 
      archiver = volume.getArchiver();

    if(archiver != null) {
      pArchiverField.setText(archiver.getName());
      pArchiverVersionField.setText("v" + archiver.getVersionID());
      pArchiverVendorField.setText(archiver.getVendor());
    }
    else {
      pArchiverField.setText("-");
      pArchiverVersionField.setText("-");
      pArchiverVendorField.setText("-");
    }
    
    /* archiver parameters */ 
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      boolean first = true;
      if((archiver != null) && archiver.hasParams()) {
	for(String pname : archiver.getLayout()) {
	  if(pname == null) {
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	  }
	  else {
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    ArchiverParam aparam = archiver.getParam(pname);
	    if(aparam != null) {
	      String text = "-";

	      if(aparam instanceof BooleanArchiverParam) {
		Boolean value = (Boolean) aparam.getValue();
		if(value != null) 
		  text = (value ? "YES" : "no");
	      }
	      else if(aparam instanceof ByteSizeArchiverParam) {
		Long value = (Long) aparam.getValue();
		if(value != null) 
		  text = formatLong(value);
	      }
	      else if((aparam instanceof DoubleArchiverParam) ||
		      (aparam instanceof IntegerArchiverParam)) {
		Comparable value = aparam.getValue();
		if(value != null) 
		  text = value.toString();
	      }
	      else if((aparam instanceof DirectoryArchiverParam) ||
		      (aparam instanceof StringArchiverParam)) {
		Comparable value = aparam.getValue();
		text = "";
		if(value != null) 
		  text = value.toString();
	      }
	      else if(aparam instanceof EnumArchiverParam) {
		EnumArchiverParam eparam = (EnumArchiverParam) aparam;
		text = eparam.getValueOfIndex(eparam.getIndex());
	      }
	      else {
		assert(false) : "Unknown archiver parameter type!";
	      }
	      
	      UIFactory.createTitledTextField 
		(tpanel, aparam.getNameUI() + ":", sTSize-7, 
		 vpanel, text, sVSize, 
		 aparam.getDescription());
	    }
	  }
	  
	  first = false;
	}
      }
      else {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      
      UIFactory.addVerticalGlue(tpanel, vpanel);
      
      pArchiverParamsDrawer.setContents((JComponent) comps[2]);
    }

    /* history */ 
    {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      if(volume != null) {
	{
	  UIFactory.createTitledTextField 
	    (tpanel, "Created On:", sTSize, 
	     vpanel, Dates.format(volume.getTimeStamp()), sVSize, 
	     "The timestamp of when the archive volume was created.");
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  {
	    JLabel label = UIFactory.createFixedLabel
	      ("Archiver Log:", sTSize, JLabel.RIGHT, 
	       "The STDOUT output from the Archiver plugin.");
	    tpanel.add(label);
	  }
	  
	  {
	    JButton btn = new JButton("Output...");
	    
	    btn.setAlignmentX(0.5f);

	    btn.setName("ValuePanelButton");
	    btn.setRolloverEnabled(false);
	    btn.setFocusable(false);
	    
	    Dimension size = new Dimension(sVSize, 19);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	    
	    btn.addActionListener(this);
	    btn.setActionCommand("show-archive-output:" + aname);
	    
	    vpanel.add(btn);
	  }
	}
	
	synchronized(pRestoredOn) {
	  TreeSet<Date> stamps = pRestoredOn.get(aname);
	  if((stamps != null) && !stamps.isEmpty()) {
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 6);
	    
	    for(Date stamp : stamps) {
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	      {
		UIFactory.createTitledTextField 
		  (tpanel, "Restored On:", sTSize, 
		   vpanel, Dates.format(stamp), sVSize, 
		   "The timestamp of when the archive volume was restored.");
		
		UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Archiver Log:", sTSize, JLabel.RIGHT, 
		     "The STDOUT output from the Archiver plugin.");
		  tpanel.add(label);
		}
		
		{
		  JButton btn = new JButton("Output...");
		  
		  btn.setAlignmentX(0.5f);

		  btn.setName("ValuePanelButton");
		  btn.setRolloverEnabled(false);
		  btn.setFocusable(false);
		  
		  Dimension size = new Dimension(sVSize, 19);
		  btn.setMinimumSize(size);
		  btn.setPreferredSize(size);
		  btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
		  
		  btn.addActionListener(this);
		  btn.setActionCommand("show-restore-output:" + 
				       aname + ":" + stamp.getTime());
		  
		  vpanel.add(btn);
		}
	      }
	    }
	  }
	}
      }
      else {
	tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	vpanel.add(Box.createHorizontalGlue());
      }
      
      UIFactory.addVerticalGlue(tpanel, vpanel);
      
      pHistoryDrawer.setContents((JComponent) comps[2]);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean isVisible
  )
  {
    if(isVisible) 
      doUpdate();

    super.setVisible(isVisible);
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden(ComponentEvent e) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved(ComponentEvent e) {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   ComponentEvent e
  )
  {
    Box box = (Box) e.getComponent();
    
    Dimension size = box.getComponent(1).getSize();

    JPanel spacer = (JPanel) box.getComponent(0);
    spacer.setMaximumSize(new Dimension(7, size.height));
    spacer.revalidate();
    spacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}


  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    if(e.getValueIsAdjusting())
      return;

    pVolumeTablePanel.getTable().setEnabled(false);

    String aname = null;
    {
      int rows[] = pVolumeTablePanel.getTable().getSelectedRows();
      if(rows.length == 1) 
	aname = pVolumeTableModel.getName(rows[0]);

      pLastSelectedArchiveName = aname;
    }
    
    updateDetails(aname);

    GetVersionsTask task = new GetVersionsTask(aname);
    task.start();
  }


  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("submit")) 
      doSubmitRequests();
    else if(cmd.equals("update")) 
      doUpdate();
    else if(cmd.startsWith("show-archive-output:")) 
      doShowArchiveOutput(cmd.substring(20));
    else if(cmd.startsWith("show-restore-output:")) {
      String parts[] = cmd.substring(20).split(":");
      if(parts.length == 2) {
	try {
	  doShowRestoreOutput(parts[0], new Date(Long.parseLong(parts[1])));
	}
	catch(NumberFormatException ex) {
	}
      }
    }
    else 
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Submit restore requests for all selected node versions.
   */ 
  private void 
  doSubmitRequests()
  {
    TreeMap<String,TreeSet<VersionID>> versions = new TreeMap<String,TreeSet<VersionID>>();

    int rows[] = pVersionTablePanel.getTable().getSelectedRows();
    int wk;
    for(wk=0; wk<rows.length; wk++) {
      if(!pVersionTableModel.getIsOnline(rows[wk])) {
	String name = pVersionTableModel.getName(rows[wk]);
	TreeSet<VersionID> vids = versions.get(name);
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(name, vids);
	}
	vids.add(pVersionTableModel.getVersionID(rows[wk]));
      }
    }
    
    pVersionTablePanel.getTable().clearSelection();

    if(!versions.isEmpty()) {
      RestoreTask task = new RestoreTask(versions);
      task.start();
    }
  }

  /*
   * Update the table with the current archive volume information.
   */ 
  private void 
  doUpdate() 
  {
    GetArchivesTask task = new GetArchivesTask(this);
    task.start();
  }

  /**
   * Show a dialog containing the STDOUT output of the archiver plugin during archive 
   * volume creation.
   */ 
  private void 
  doShowArchiveOutput
  (
   String aname
  )
  {
    GetArchivedOutputTask task = new GetArchivedOutputTask(aname);
    task.start();
  }

  /**
   * Show a dialog containing the STDOUT output of the archiver plugin during archive 
   * volume restoration.
   */ 
  private void 
  doShowRestoreOutput
  (
   String aname, 
   Date stamp
  )
  {
    GetRestoredOutputTask task = new GetRestoredOutputTask(aname, stamp);
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value == null) 
      return "-";
    
    if(value < 1024) {
      return value.toString();
    }
    else if(value < 1048576) {
      double k = ((double) value) / 1024.0;
      return String.format("%1$.1fK", k);
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.1fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.1fG", g);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Get the up-to-date archive volume information. 
   */ 
  private
  class GetArchivesTask
    extends Thread
  {
    public 
    GetArchivesTask
    (
     JArchiveVolumesDialog parent
    )
    {
      super("JArchiveVolumeDialog:GetArchivesTask");
      pParent = parent;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      synchronized(pUpdateLock) {
	if(master.beginPanelOp()) {
	  try {
	    synchronized(pArchiveVolumes) {
	      TreeMap<String,Date> archives = client.getArchivedOn();
	      for(String aname : archives.keySet()) {
		ArchiveVolume vol = pArchiveVolumes.get(aname);
		if(vol == null) {
		  master.updatePanelOp("Loading Archive Volume: " + aname);
		  vol = client.getArchive(aname);
		  pArchiveVolumes.put(aname, vol);
		}
	      }
	    }
	    
	    synchronized(pRestoredOn) {
	      pRestoredOn.clear();
	      pRestoredOn.putAll(client.getRestoredOn());
	    }
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }

      UpdateTask task = new UpdateTask(pParent);
      SwingUtilities.invokeLater(task);
    }

    private JArchiveVolumesDialog  pParent; 
  }

  /** 
   * Update the UI components.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     JArchiveVolumesDialog parent
    )
    {
      super("JArchiveVolumeDialog:UpdateTask");
      pParent = parent;
    }

    public void 
    run() 
    {
      synchronized(pArchiveVolumes) {
	ListSelectionModel smodel = pVolumeTablePanel.getTable().getSelectionModel();
	smodel.removeListSelectionListener(pParent);
	{
	  pVolumeTableModel.setData(pArchiveVolumes.values());
	}
	smodel.addListSelectionListener(pParent);

	JTable table = pVolumeTablePanel.getTable();
	Integer srow = null;
	if(pLastSelectedArchiveName != null) {
	  int row;
	  for(row=0; row<table.getRowCount(); row++) {
	    if(pVolumeTableModel.getName(row).equals(pLastSelectedArchiveName)) {
	      srow = row; 
	      break;
	    }
	  }
	}
	  
	if(srow != null) 
	  table.setRowSelectionInterval(srow, srow);
	else 
	  table.clearSelection();
      }
    }

    private JArchiveVolumesDialog  pParent; 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the 
   */ 
  private
  class GetVersionsTask
    extends Thread
  {
    public 
    GetVersionsTask
    (
     String aname
    ) 
    {
      super("JArchiveVolumeDialog:GetVersionsTask");
      pArchiveName = aname;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.getMasterMgrClient();
      ArchiveVolume volume = null;
      TreeMap<String,TreeSet<VersionID>> offline = new TreeMap<String,TreeSet<VersionID>>();
      if(pArchiveName != null) {
	synchronized(pUpdateLock) {
	  if(master.beginPanelOp("Loading Offlined Versions...")) {
	    try {
	      synchronized(pArchiveVolumes) {
		volume = pArchiveVolumes.get(pArchiveName);
		for(String aname : volume.getNames()) {
		  TreeSet<VersionID> ovids = client.getOfflineVersionIDs(aname);
		  for(VersionID vid : volume.getVersionIDs(aname)) {
		    if(ovids.contains(vid)) {
		      TreeSet<VersionID> vids = offline.get(aname);
		      if(vids == null) {
			vids = new TreeSet<VersionID>();
			offline.put(aname, vids);
		      }
		      vids.add(vid);
		    }		
		  }
		}
	      }
	    }
	    catch(PipelineException ex) {
	      showErrorDialog(ex);
	    }
	    finally {
	    master.endPanelOp("Done.");
	    }
	  }
	}
      }

      UpdateVersionsTask task = new UpdateVersionsTask(volume, offline);
      SwingUtilities.invokeLater(task);
    }

    private String  pArchiveName;
  }
  
  /** 
   * Update the UI components.
   */ 
  private
  class UpdateVersionsTask
    extends Thread
  {
    public 
    UpdateVersionsTask
    (
     ArchiveVolume volume, 
     TreeMap<String,TreeSet<VersionID>> offline
    )
    {
      super("JArchiveVolumeDialog:UpdateVersionsTask");

      pVolume  = volume;
      pOffline = offline;
    }

    public void 
    run() 
    {
      if(pVolume != null) 
	pTotalSizeLabel.setText("Total Size: " + formatLong(pVolume.getTotalSize()));
      else 
	pTotalSizeLabel.setText("Total Size: ???");

      pVersionTableModel.setData(pVolume, pOffline);
      pVolumeTablePanel.getTable().setEnabled(true);
    }

    private ArchiveVolume                       pVolume;
    private TreeMap<String,TreeSet<VersionID>>  pOffline;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Restore the given versions of the node.
   */ 
  private
  class RestoreTask
    extends Thread
  {
    public 
    RestoreTask
    (
     TreeMap<String,TreeSet<VersionID>> versions   
    ) 
    {
      super("JArchiveVolumeDialog:RestoreTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Requesting Restore...")) {
	  try {
	    master.getMasterMgrClient().requestRestore(pVersions);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }
    }

    private TreeMap<String,TreeSet<VersionID>>   pVersions;
  }
 

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the output from the archiver plugin during archive volume creation.
   */ 
  private
  class GetArchivedOutputTask
    extends Thread
  {
    public 
    GetArchivedOutputTask
    (
     String aname
    ) 
    {
      super("JArchiveVolumeDialog:GetArchivedOutputTask");
      pName = aname;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      String output = null;
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Loading Archiver Output: " + pName)) {
	  try {
	    output = master.getMasterMgrClient().getArchivedOutput(pName);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }

      ShowOutputTask task = 
	new ShowOutputTask("Archive Volume:  " + pName, 
			   "Output from Archive Creation:", 
			   output);
      SwingUtilities.invokeLater(task);
    }

    private String  pName; 
  }

  /** 
   * Get the output from the archiver plugin during archive volume restoration.
   */ 
  private
  class GetRestoredOutputTask
    extends Thread
  {
    public 
    GetRestoredOutputTask
    (
     String aname, 
     Date stamp
    ) 
    {
      super("JArchiveVolumeDialog:GetRestoredOutputTask");
      pName  = aname;
      pStamp = stamp; 
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      String output = null;
      synchronized(pUpdateLock) {
	if(master.beginPanelOp("Loading Archiver Output: " + pName)) {
	  try {
	    output = master.getMasterMgrClient().getRestoredOutput(pName, pStamp);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	    return;
	  }
	  finally {
	    master.endPanelOp("Done.");
	  }
	}
      }

      ShowOutputTask task = 
	new ShowOutputTask("Archive Volume:  " + pName, 
			   "Output from Archive Restoration:  " + Dates.format(pStamp), 
			   output);
      SwingUtilities.invokeLater(task);
    }

    private String  pName; 
    private Date    pStamp; 
  }

  /** 
   * Show output dialog.
   */ 
  private
  class ShowOutputTask
    extends Thread
  {
    public 
    ShowOutputTask
    (
     String aname, 
     String title, 
     String msg
    ) 
    {
      super("JArchiveVolumeDialog:ShowOutputTask");

      pName    = aname; 
      pTitle   = title; 

      if((msg == null) || (msg.length() == 0)) 
	pMessage = "(Nothing Output)";
      else 
	pMessage = msg; 
    }

    public void 
    run() 
    {
      JOutputDialog diag = new JOutputDialog();
      diag.setMessage(pName, pTitle, pMessage);
      diag.setVisible(true);
    }

    private String  pName;
    private String  pTitle;
    private String  pMessage;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8172712609587023845L;
  
  private static final int sTSize = 180;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive volumes indexed by archive volume name.
   */
  private TreeMap<String,ArchiveVolume>  pArchiveVolumes; 

  /**
   * The timestamps of when each archive volume was restored indexed by archive volume name.
   */
  private TreeMap<String,TreeSet<Date>>  pRestoredOn; 

  /**
   * A lock used to synchronize communication with the plmaster(1).
   */ 
  private Object  pUpdateLock; 

    
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archive volumes table model.
   */ 
  private ArchiveVolumeSimpleTableModel pVolumeTableModel;

  /**
   * The archive volumes table.
   */ 
  private JTablePanel  pVolumeTablePanel;

  /**
   * The name of the last selected archive volume.
   */ 
  private String  pLastSelectedArchiveName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The title of the archive volume details panel.
   */ 
  private JLabel  pVolumeLabel; 


  /**
   * The name of the toolset environment. 
   */ 
  private JTextField  pToolsetField; 


  /**
   * The name of the archiver plugin.
   */ 
  private JTextField  pArchiverField;

  /**
   * The revision number of the archiver plugin.
   */ 
  private JTextField  pArchiverVersionField;

  /**
   * The name of the vendor of the archiver plugin.
   */ 
  private JTextField  pArchiverVendorField;


  /**
   * The archiver plugin parameters drawer.
   */ 
  private JDrawer  pArchiverParamsDrawer;

  /**
   * The archive volume history drawer.
   */ 
  private JDrawer  pHistoryDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The total size of all files contained in the archive volume.
   */ 
  private JLabel  pTotalSizeLabel;

  /**
   * The node versions table model.
   */ 
  private ArchiveVolumeVersionsTableModel  pVersionTableModel; 

  /**
   * The node versions table.
   */ 
  private JTablePanel  pVersionTablePanel;

}

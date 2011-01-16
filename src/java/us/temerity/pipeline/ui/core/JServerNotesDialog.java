// $Id: JConfigDialog.java,v 1.12 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   S E R V E R   N O T E S   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the operational notes for selected job manager servers.
 */ 
public 
class JServerNotesDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JServerNotesDialog()
  {
    super("Server Notes");

    /* init fields */
    {
      pPrivilegeDetails = new PrivilegeDetails();
      pTextAreas = new ArrayList<JTextArea>();
      pHosts = new TreeMap<String,String>();
    }

    /* create dialog body components */ 
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      
      vbox.add(Box.createRigidArea(new Dimension(0, 4)));

      {
        Box hbox = new Box(BoxLayout.X_AXIS);
 
        hbox.add(Box.createRigidArea(new Dimension(4, 0)));
        
        {
          JButton btn = new JButton();
          pPrevButton = btn;
          btn.setName("LeftArrowButton");
	  
          Dimension size = new Dimension(16, 16);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
	  
          btn.setActionCommand("prev-host");
          btn.addActionListener(this);
	  
          hbox.add(btn);
        } 

        hbox.add(Box.createRigidArea(new Dimension(4, 0)));
        
        {
          ArrayList<String> values = new ArrayList();
          values.add("-"); 
        
          JCollectionField field = UIFactory.createCollectionField(values, this, sSize-60);
          pHostsField = field;

          field.setActionCommand("host-changed");
          field.addActionListener(this);
        
          hbox.add(pHostsField);
        }

        hbox.add(Box.createRigidArea(new Dimension(4, 0)));
        
        {
          JButton btn = new JButton();
          pNextButton = btn;
          btn.setName("RightArrowButton");
	  
          Dimension size = new Dimension(16, 16);
          btn.setMinimumSize(size);
          btn.setMaximumSize(size);
          btn.setPreferredSize(size);
	  
          btn.setActionCommand("next-host");
          btn.addActionListener(this);
	  
          hbox.add(btn);
        } 

        hbox.add(Box.createRigidArea(new Dimension(16, 0)));

        vbox.add(hbox);
      }
      
      vbox.add(Box.createRigidArea(new Dimension(0, 4)));

      {
        pMessageBox = new Box(BoxLayout.Y_AXIS);
      
        JScrollPane scroll = UIFactory.createVertScrollPane(pMessageBox);
        pScroll = scroll;

        vbox.add(scroll);
      }

      String[][] extra = {
        { "New Note", "new-note" }, 
        { "Update",  "update" }
      };

      JButton[] btns = 
        super.initUI("Queue Server Notes:", vbox, null, null, extra, "Close", null);

      pNewNoteButton = btns[0];

      setSize(sSize, sSize);
    }

    pCreateNoteDialog = new JCreateNoteDialog(this);  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  update
  (
   TreeSet<String> hosts, 
   String selected
  )
  {
    /* update privileges */ 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      pPrivilegeDetails = client.getPrivilegeDetails();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }
    finally {
      master.releaseMasterMgrClient(client);
    }  
    pNewNoteButton.setEnabled(pPrivilegeDetails.isQueueAdmin());

    /* update selected hosts */ 
    {
      if(hosts != null) {
        pHosts = new TreeMap<String,String>();
        
        for(String hname : hosts) {
          Matcher n = sNumericHostPattern.matcher(hname);
          Matcher s = sShortHostPattern.matcher(hname);
          String sname = hname; 
          if(!n.matches() && s.find() && (s.group().length() > 0))
            sname = s.group();
          
          pHosts.put(sname, hname);
        }
      }
      
      ArrayList<String> values = new ArrayList<String>();
      if(pHosts.isEmpty()) 
        values.add("-"); 
      else 
        values.addAll(pHosts.keySet()); 
      
      String current = pHostsField.getSelected(); 
      if(selected != null) 
        current = selected;
      if(!values.contains(current)) 
        current = null;
      
      pHostsField.setValues(values);
      
      if(current == null) 
        current = !pHosts.isEmpty() ? pHosts.firstKey() : "-";
      
      pHostsField.setSelected(current); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    if(cmd.equals("host-changed"))
      doHostChanged();
    else if(cmd.equals("prev-host"))
      doPrevHost();
    else if(cmd.equals("next-host"))
      doNextHost();
    else if(cmd.equals("new-note"))
      doNewNote();
    else if(cmd.equals("update"))
      doUpdate();
    else if(cmd.startsWith("remove:")) 
      doRemoveNote(cmd.substring(7)); 
    else 
      super.actionPerformed(e);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the notes area whenever the host selection changes.
   */ 
  private void
  doHostChanged()
  { 
    UIMaster master = UIMaster.getInstance();
    QueueMgrClient qclient = master.acquireQueueMgrClient();
    try {
      pMessageBox.removeAll();
      pTextAreas.clear();  

      String lname = null;
      TreeMap<Long,SimpleLogMessage> notes = null;
      {
        String hname = pHostsField.getSelected(); 
        if((hname != null) && !hname.equals("-")) {
          lname = pHosts.get(hname);
          if(lname != null) 
            notes = qclient.getHostNotes(lname); 
        }
      }

      if(notes != null) {
	ArrayList<Long> stamps = new ArrayList<Long>(notes.keySet());
        Long oldest = stamps.isEmpty() ? null : stamps.get(0);
	Collections.reverse(stamps);

        for(Long stamp : stamps) {
          SimpleLogMessage note = notes.get(stamp);

	  {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	  
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		Box hbox2 = new Box(BoxLayout.X_AXIS);
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));

                {
                  JButton btn = new JButton();
                  btn.setName("CloseButton");
		
                  Dimension size = new Dimension(15, 19);
                  btn.setMinimumSize(size);
                  btn.setMaximumSize(size);
                  btn.setPreferredSize(size);

                  btn.setActionCommand("remove:" + lname + ":" + stamp);
                  btn.addActionListener(this);
                  btn.setToolTipText(UIFactory.formatToolTip("Remove the host note."));

                  btn.setEnabled(pPrivilegeDetails.isQueueAdmin());
                  
                  hbox2.add(btn);
                }

		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));

		{
		  JLabel label = new JLabel(note.getAuthor());
		  label.setToolTipText(UIFactory.formatToolTip
                    ("The name of the user who wrote the note."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		hbox2.add(Box.createHorizontalGlue());

		{
		  JLabel label = new JLabel(TimeStamps.format(note.getTimeStamp()));
		  label.setToolTipText(UIFactory.formatToolTip
                    ("When the note was written."));
		  hbox2.add(label);
		}
		
		hbox2.add(Box.createRigidArea(new Dimension(10, 0)));
		
		Dimension size = new Dimension(sSize-18, 19);
		hbox2.setMinimumSize(size);
		hbox2.setMaximumSize(size);
		hbox2.setPreferredSize(size);
		
		hbox.add(hbox2);
	      }
	      
	      hbox.add(Box.createHorizontalGlue());

	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
	    {
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextArea area = new JTextArea(note.getMessage(), 0, 0);
		pTextAreas.add(area);
		
		area.setName("HistoryTextArea");
		
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
                area.setEditable(false);
		
		hbox.add(area);
	      }
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      hbox.add(Box.createHorizontalGlue());
	      
	      panel.add(hbox);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(0, 3)));
	    
	    pMessageBox.add(panel);
	  }
	
	  if(stamp != oldest) {
	    JPanel spanel = new JPanel();
	    spanel.setName("Spacer");
	    
	    spanel.setMinimumSize(new Dimension(sSize-18, 7));
	    spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	    spanel.setPreferredSize(new Dimension(sSize-18, 7));
	    
	    pMessageBox.add(spanel);
	  }
	}
      }

      pMessageBox.add(UIFactory.createFiller(sSize-18));
      pMessageBox.revalidate();
      
      SwingUtilities.invokeLater(new ScrollTask());
    }  
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseQueueMgrClient(qclient);
    }    
  }

  /**
   * Select the previous host in the list.
   */ 
  private void
  doPrevHost()
  { 
    int size = pHostsField.getValues().size();
    if(size < 2) 
      return;

    int idx = pHostsField.getSelectedIndex(); 
    if(idx == 0) 
      idx = size-1;
    else 
      idx--;
    
    pHostsField.setSelectedIndex(idx);
  }

  /**
   * Select the next host in the list.
   */ 
  private void
  doNextHost()
  { 
    int size = pHostsField.getValues().size();
    if(size < 2) 
      return;

    int idx = pHostsField.getSelectedIndex(); 
    if(idx == size-1) 
      idx = 0;
    else 
      idx++;
    
    pHostsField.setSelectedIndex(idx);
  }

  /**
   * Create a new note for the currently selected host.
   */ 
  private void 
  doNewNote()
  { 
    String hname = pHostsField.getSelected(); 
    if(hname == null) 
      return;

    String lname = pHosts.get(hname);
    if(lname == null) 
      return;

    pCreateNoteDialog.update();
    pCreateNoteDialog.setVisible(true);
    if(pCreateNoteDialog.wasConfirmed()) {
      String msg = pCreateNoteDialog.getMessage();
      if(msg != null) {
        UIMaster master = UIMaster.getInstance();
        QueueMgrClient qclient = master.acquireQueueMgrClient();
        try {
          qclient.addHostNote(lname, msg);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);
        }
        finally {
          master.releaseQueueMgrClient(qclient);
        }    
        
        doUpdate();
      }
    }
  }

  /**
   * Update all notes.
   */
  private void 
  doUpdate()
  {
    update(null, null);
  }
  
  /**
   * Remove a host note.
   */ 
  private void
  doRemoveNote
  (
   String value
  )
  {
    String parts[] = value.split(":");
    if(parts.length != 2) 
      return;
    
    String hname = parts[0];
    Long stamp = new Long(parts[1]);

    JConfirmDialog diag = new JConfirmDialog(this, "Delete Host Note?");
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient qclient = master.acquireQueueMgrClient();
      try {
        qclient.removeHostNote(hname, stamp); 
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.releaseQueueMgrClient(qclient);
      }    
      
      doUpdate();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Scrolls to the newest note.
   */ 
  private
  class ScrollTask
    extends Thread
  {
    public 
    ScrollTask()
    {
      super("JServerNotesDialog:ScrollTask");
    }

    @Override
    public void 
    run() 
    {    
      for(JTextArea area : pTextAreas) {
	area.setRows(area.getLineCount());		
	
	Dimension size = area.getPreferredSize();
	area.setMinimumSize(size);
	area.setMaximumSize(size);
      }

      pScroll.getViewport().setViewPosition(new Point());
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5938743920919867990L; 

  private static final int  sSize = 500;

  /**
   * A regular expressions used to determine if a hostname is numeric and to match the 
   * first component (short name) of a non-numeric hostname.
   */ 
  private static final Pattern sNumericHostPattern = 
    Pattern.compile("([0-9])+\\.([0-9])+\\.([0-9])+\\.([0-9])+");

  private static final Pattern sShortHostPattern = 
    Pattern.compile("([^\\.])+");



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The fully resolved names of the selected hosts indexed by the short hostnames.
   */
  private TreeMap<String,String>  pHosts;

  /**
   * The notes for the currently selected host.
   */ 
  private TreeMap<Long,SimpleLogMessage>  pNotes;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The host navigation buttons.
   */ 
  private JButton  pPrevButton;
  private JButton  pNextButton;

  /**
   * The host selection field.
   */ 
  private JCollectionField  pHostsField;

  /**
   * The notes container.
   */
  private Box  pMessageBox;

  /**
   * The log message text areas.
   */
  private ArrayList<JTextArea>  pTextAreas; 

  /**
   * The scroll panel containing the messages.
   */ 
  private JScrollPane  pScroll; 

  /**
   * The displays the note creation dialog.
   */ 
  private JButton  pNewNoteButton; 

  /**
   * Dialog for writing new note text.
   */ 
  private JCreateNoteDialog  pCreateNoteDialog; 
}


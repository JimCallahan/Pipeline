// $Id: JFileSeqSelectDialog.java,v 1.3 2004/10/22 19:41:20 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E Q   S E L E C T   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog which allows the user to select file sequences.
 */ 
public 
class JFileSeqSelectDialog
  extends JBaseFileSelectDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog for selecting file sequences.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JFileSeqSelectDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Select File Sequence");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  protected void
  initUI() 
  {
    pRenderer = new JFileSeqListCellRenderer();
    JTextField field = UIMaster.createTextField(null, 60, JLabel.LEFT);
    super.initUI("Select File Sequence:", pRenderer, 
		 "File Sequence:", 90, field, "Select");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the selected file sequence. <P> 
   * 
   * The file sequence prefix will contain the full canonical path to the root 
   * directory of the current working area (see {@link #setRootDir setRootDir}).
   * 
   * @return 
   *   The file sequence or <CODE>null</CODE> if none is selected.
   */ 
  public FileSeq
  getSelectedFileSeq() 
  {
    return pFileSeq;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the enabled status of the confirm button.
   */ 
  protected void 
  updateConfirmButton()
  {
    pConfirmButton.setEnabled(true);
  }

  /**
   * Update the header label for the current working area.
   */ 
  public void 
  updateHeader
  (
   String author, 
   String view
  ) 
  {
    pHeaderLabel.setText("Select File Sequence:  " + author + "|" + view);
  }

  /**
   * Update the target directory and clear the file sequence field.
   */ 
  public void 
  updateTarget
  (
   File target
  ) 
  {
    updateTargetDir(target);
    pFileField.setText(null);
    pFileSeq = null;
  }

  /**
   * Update the target directory.
   */ 
  public void 
  updateTargetDir
  (
   File target
  )
  { 
    /* determine the canonical path to the target */ 
    File canon = null;
    {
      if(target != null) {
	try {
	  canon = target.getCanonicalFile();
	}
	catch(IOException ex) {
	}
      }
      
      if(canon == null) 
	canon = pRootDir;
    }

    /* determine the target directory */ 
    File dir = null;
    {
      if(canon.isDirectory()) 
	dir = canon;
      else {
	Toolkit.getDefaultToolkit().beep();
	
	File file = canon;
	while(file.getPath().startsWith(pRootDir.getPath())) {
	  if(file.isDirectory()) {
	    dir = file;
	    break;
	  }

	  file = file.getParentFile();
	}

	if(dir == null) 
	  return;
      }
      assert(dir != null);
    }

    /* initialize the UI components */ 
    {
      File fs[] = dir.listFiles();
      if(fs != null) {
	DefaultListModel model = (DefaultListModel) pFileList.getModel();
	model.clear();
	
	TreeSet<File> dirs  = new TreeSet<File>();
	TreeSet<File> files = new TreeSet<File>();
	
	int wk;
	for(wk=0; wk<fs.length; wk++) {
	  if((pFileField != null) && fs[wk].isFile())
	    files.add(fs[wk]);
	  else if(fs[wk].isDirectory()) 
	    dirs.add(fs[wk]);
	}

	if(!dir.equals(pRootDir)) 
	  model.addElement(new File(dir, ".."));
	
	for(File file : dirs) 
	  model.addElement(file);


	/* collate the files into file sequences */ 
	TreeMap<String,FileSeq> fseqs = new TreeMap<String,FileSeq>();
	{
	  /* collect all files who's names are valid file sequences fragments */ 
	  TreeMap<String,Fragment> ufrags = new TreeMap<String,Fragment>();
	  TreeMap<String,Fragment> pfrags = new TreeMap<String,Fragment>();
	  for(File file : files) {
	    String comps[] = file.getName().split("\\.", -1);
	    int num = comps.length;
	    if(((num == 2) || (num == 3)) &&
	       (comps[0].length() > 0) && 
	       (comps[1].length() > 0) && 
	       ((num == 2) || (comps[2].length() > 0))) {

	      String prefix = comps[0];
	      Integer frame = null;		
	      String suffix = null;
	      try {
		frame = new Integer(comps[1]);
		if(num == 3) 
		  suffix = comps[2];
	      }
	      catch(NumberFormatException ex) {
		if(num == 2) 
		  suffix = comps[1];
		else if(num == 3) 
		  prefix = null;
	      }
	      
	      if(prefix != null) {
		if(frame != null) {
		  boolean isPadded = comps[1].startsWith("0");
		  int digits = comps[1].length();
		  String key = (prefix + "|" + suffix + "|" + digits);
		  
		  Fragment frag = null;
		  if(isPadded) {
		    frag = pfrags.get(key);
		    if(frag == null) {
		      frag = new Fragment(prefix, suffix, digits);
		      pfrags.put(key, frag);
		    }
		  }
		  else {
		    frag = ufrags.get(key);
		    if(frag == null) {
		      frag = new Fragment(prefix, suffix, digits);
		      ufrags.put(key, frag);
		    }		    
		  }
		  
		  frag.uFrames.add(frame);
		}
		else {
		  FileSeq fseq = new FileSeq(prefix, suffix);
		  fseqs.put(fseq.toString(), fseq);
		}
	      }
	    }
	  }
	  
	  /* merge any unpadded fragment which coincides with a padded fragment */ 
	  for(String key : pfrags.keySet()) {
	    Fragment pfrag = pfrags.get(key);
	    Fragment ufrag = ufrags.get(key);

	    if(ufrag != null) {
	      pfrag.uFrames.addAll(ufrag.uFrames);
	      ufrags.remove(key);
	    }
	  }

	  /* build file sequences from the fragments */ 
	  for(Fragment frag : ufrags.values()) {
	    FileSeq fseq = frag.toFileSeq();
	    fseqs.put(fseq.toString(), fseq);
	  }

	  for(Fragment frag : pfrags.values()) {
	    FileSeq fseq = frag.toFileSeq();
	    fseqs.put(fseq.toString(), fseq);
	  }
	}

	for(FileSeq fseq : fseqs.values())
	  model.addElement(fseq);

	{
	  pRenderer.setDirectory(dir);

	  assert(dir.getPath().startsWith(pRootDir.getPath()));
	  String dstr = dir.getPath();
	  String text = dstr.substring(pRootDir.getPath().length(), dstr.length());
	  if(text.length() > 0) 
	    pDirField.setText(text);
	  else 
	    pDirField.setText("/");
	}
      }
      else {
	Toolkit.getDefaultToolkit().beep();
	return;
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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

    Object obj = pFileList.getSelectedValue();
    if((obj != null) && (obj instanceof FileSeq)) {
      pFileSeq = (FileSeq) obj;
      pFileField.setText(pFileSeq.toString());
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle double-click on the given list element.
   */ 
  protected void 
  doHandleDoubleClick
  (
   Object elem
  )
  { 
    if((elem != null) && (elem instanceof File)) 
      updateTargetDir((File) elem);
  }

  /**
   * Jump to the directory named by the directory field.
   */ 
  protected void 
  doJumpDir()
  {
    File dir = new File(pRootDir + pDirField.getText());
    if(dir.isDirectory()) 
      updateTargetDir(dir);
    else 
      Toolkit.getDefaultToolkit().beep();
  }

  /**
   * Jump to the home directory.
   */ 
  protected void 
  doJumpHome()
  { 
    updateTargetDir(pRootDir);
  }

  /**
   * Create a new directory under the current working directory and jump to it.
   */ 
  protected void 
  doNewFolder()
  {
    File dir = new File(pRootDir + pDirField.getText());
    if(dir.isDirectory()) {
      JNewFolderDialog diag = new JNewFolderDialog(this);
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) {
	File ndir = new File(dir, diag.getName());
	if(!ndir.mkdirs()) {
	  UIMaster.getInstance().showErrorDialog
	    ("I/O Error:", 
	     "Unable to create directory (" + ndir + ")!");
	}
	
	updateTargetDir(ndir);
      }
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class Fragment
  {
    public 
    Fragment
    (
     String prefix, 
     String suffix, 
     int digits
    ) 
    {
      uPrefix = prefix; 
      uSuffix = suffix;
      uDigits = digits; 

      uFrames = new TreeSet<Integer>();
    }

    public FileSeq
    toFileSeq() 
    {
      FilePattern fpat = new FilePattern(uPrefix, uDigits, uSuffix);

      FrameRange frange = null;
      {
	int startFrame = uFrames.first();
	int endFrame   = uFrames.last();
	if(endFrame == startFrame) {
	  frange = new FrameRange(startFrame);
	}
	else {
	  int byFrame = (endFrame - startFrame) + 1;
	  {
	    int prev = startFrame;
	    for(Integer frame : uFrames) {
	      int inc = (frame - prev);
	      if(inc > 0) 
		byFrame = Math.min(byFrame, inc);
	      prev = frame;
	    }
	  }
	  
	  frange = new FrameRange(startFrame, endFrame, byFrame);
	}
      }
      
      return new FileSeq(fpat, frange);
    }


    public String  uPrefix; 
    public String  uSuffix; 
    public int     uDigits; 

    public TreeSet<Integer>  uFrames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3224045468633955025L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selected file sequence or <CODE>null</CODE> if none is selected.
   */ 
  private FileSeq  pFileSeq;

  /**
   * The renderer of list cells.
   */ 
  private JFileSeqListCellRenderer pRenderer;
}

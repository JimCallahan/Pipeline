package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;


/**
 * Unused tool.<p>
 * The tool allows a user to switch back and forth between having low-rez and 
 * hi-rez models in a scene.
 * <p>
 * It displays a list of models in a scene and lets the user select which ones
 * are to be hi-rez and which are to be lo-rez.  It then text-edits the maya
 * scene and fixes all the links in pipeline.
 * 
 * @author 	Ifedayo 0. Ojomo
 * @version 1.0.0
 */
public class SubstituteModelTool 
	extends BaseTool {
	

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/

	/** 
	 * The phase number.
	 */	
	private int pPhase;
	
	/**
	 * The current working area user|view.
	 */
	private String pUser;
	private String pView;
	
	/**
	 * The patterns for character|set|prop 
	 */
	private String replacePattern = ".*/assets/(character|set|prop)/";
	private String modelPattern = replacePattern +".*";
	
	/**
	 * Nodes which could potentially be sustituted.
	 */
	private TreeSet<String> pPotentialNames;
	
	/**
	 * The names of references in the actual file corresponding to the target node.
	 */
	private TreeSet<String> pActualNames;
	
	/**
	 * The per-node substitution fields.
	 */
	private TreeMap<String, JBooleanField> pSubstituteFields;
	
	/**
	 * The nodes that will be substituted.
	 */
	private TreeSet<String> pSubstituteNames;
	
	public final LinkPolicy REF = LinkPolicy.Reference;
	public final LinkPolicy DEP = LinkPolicy.Dependency;
	public final LinkRelationship LINKALL = LinkRelationship.All;
	public final LinkRelationship LINKONE = LinkRelationship.OneToOne;
		
	
	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = -8037966517164201248L;
	private static final int sTSize  = 250;
	private static final int sVSize  = 150;	  
	 
	
	/*-----------------------------------------------*/
	/*                 CONSTRUCTOR                   */
	/*-----------------------------------------------*/
	/**
	 * Allows a user to substitute low-res models with hi-res models 
	 * and vice versa for a selected leaf node.
	 */
	public SubstituteModelTool(){
	   	super("Substitute Model", new VersionID("1.0.0"), "SCEA",
			"Substitutes low-res models with hi-res models and vice versa for a selected leaf node.");
	   	underDevelopment();

	   	addSupport(OsType.MacOS);
	   	addSupport(OsType.Windows);
	   	pPhase = 1;
	    	
		pSubstituteFields = new TreeMap<String, JBooleanField>();
	    pPotentialNames = new TreeSet<String>(); 
	    pActualNames = new TreeSet<String>();
	    pSubstituteNames = new TreeSet<String>();
	}
		 
	
	/*-------------------------------------------------------*/
	/*                          OPS                          */
	/*-------------------------------------------------------*/

	/**
	  * Create and show graphical user interface components to collect information
	  * from the user to use as input in the next phase of execution for the tool. <P>
	  * 
	  * @return 
	  *   The phase progress message or <CODE>null</CODE> to abort early.
	  * 
	  * @throws PipelineException
	  *   If unable to validate the given user input.
	  */
	public synchronized String collectPhaseInput() throws PipelineException
	{
	    switch (pPhase) {
	    	case 1:
	    	return collectFirstPhaseInput();
	      
	    	case 2:
	    	return collectSecondPhaseInput();
	      
	    	default:
	    	assert (false);
    
	    	return null;	    
	    }
	}//end collectPhaseInput
	

	/**
	  * Error checking.
	  * Get the names of all source models. <P>
	  * 
	  * @return 
	  *   The phase progress message or <CODE>null</CODE> to abort early.
	  * 
	  * @throws PipelineException
	  *   If unable to validate the given user input.
	  */
	private synchronized String collectFirstPhaseInput() throws PipelineException {
	    if(pPrimary == null)
	    	throw new PipelineException("The primary selection must be the Target Node!");
	      
		if(pSelected.size() != 1)
	    	throw new PipelineException("Only one Target Node may be selected.");
	   
		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		OverallNodeState state = status.getDetails().getOverallNodeState();
			
		if(!state.equals(OverallNodeState.Identical)){
		   	JToolDialog tool = new JToolDialog("SubModel", new JPanel(), "Continue");
			JConfirmDialog dialog = new JConfirmDialog
				(tool, "This node is not identical to the checked in node.\n"+
				"Using this tool could be a bad idea.\n Do you want to continue?");
			dialog.setVisible(true);
			if(!dialog.wasConfirmed()){
				return null;
			}
		}
		
		return ": No errors so far.";
	}//end collectFirstPhaseInput()
	  
	  
	/**
	  * Compare references in the file with those in pipeline. 
	  * If the node network contains the same references as the file, then 
	  * show the GUI to allow the user to choose which references to subsitute. <P>
	  * 
	  * @return 
	  *   The phase progress message or <CODE>null</CODE> to abort early.
	  * 
	  * @throws PipelineException
	  *   If unable to validate the given user input.
	  */
	private synchronized String collectSecondPhaseInput() throws PipelineException{
		pSubstituteNames.clear();
		pSubstituteNames.addAll(pPotentialNames);
		pSubstituteFields.clear();
		
		//System.err.println(pPotentialNames);
		//System.err.println(pActualNames);
		  	  	
		if(!pSubstituteNames.containsAll(pActualNames)) {
		  	/*JConfirmDialog dialog = new JConfirmDialog(
		  		"The scene file does not contain the same references as the node network");
		  	dialog.setVisible(true);*/
		  		
		  	throw new PipelineException(
		  		"The scene file does not contain the same references as the node network");
		}
		  	
		  	
	  	/* DO GUI DRAWING STUFF*/
		JScrollPane scroll = null;
		{
		     Box ibox = new Box(BoxLayout.Y_AXIS);
		     if(pPotentialNames.isEmpty()) {
		    	 Component comps[] = UIFactory.createTitledPanels();
		    	 JPanel tpanel = (JPanel) comps[0];
		    	 JPanel vpanel = (JPanel) comps[1];
			
		    	 tpanel.add(Box.createRigidArea(new Dimension(sTSize - 7, 0)));
		    	 vpanel.add(Box.createHorizontalGlue());
			
		    	 ibox.add(comps[2]);
		     } else {
	    		 Component comps[] = UIFactory.createTitledPanels();
	    		 JPanel tpanel = (JPanel) comps[0];
	    		 JPanel vpanel = (JPanel) comps[1];
	    		 JDrawer drawer = new JDrawer("Substitute Models: " , (JComponent) comps[2], true);
	    		 ibox.add(drawer);
		    	 for (String name : pPotentialNames) {
		    		 String shortName = name.replaceAll(replacePattern,"");
			   		 JBooleanField field = 
		    		 UIFactory.createTitledBooleanField(tpanel, shortName, sTSize - 7, 
		    			 vpanel, sVSize,"Whether to substitute the sources of the node.");
		    		 field.setValue(true);
		    		 pSubstituteFields.put(name, field);
			  
		    		 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);		    		  
		    	 }
		     }		      
		     
		     {
		    	 JPanel spanel = new JPanel();
		    	 spanel.setName("Spacer");

		    	 spanel.setMinimumSize(new Dimension(sTSize + sVSize, 7));
		    	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		    	 spanel.setPreferredSize(new Dimension(sTSize + sVSize, 7));

		    	 ibox.add(spanel);
		     }

		     {
		    	 scroll = new JScrollPane(ibox);
		    	 scroll
		    	 	.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		    	 scroll
		    	 	.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		    	 Dimension size = new Dimension(sTSize + sVSize + 52, 500);
		    	 scroll.setMinimumSize(size);
		    	 scroll.setPreferredSize(size);

		    	 scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		     }
		}

		/* query the user */
		JToolDialog diag = new JToolDialog("Substitute Model Tool", scroll,
								"Confirm");
	    diag.setVisible(true);
	    
	    /* Process User Input */
	    if(diag.wasConfirmed()) {
	    	for (String name : pSubstituteFields.keySet()) {
	    		JBooleanField field = pSubstituteFields.get(name);
	    		Boolean value = field.getValue();
	    		if((value == null) || !value)
	    			pSubstituteNames.remove(name);
	    	}
	    	//System.err.println("Modifying "+pSubstituteNames);
	    	return ": Modifying Nodes...";
	    } else {
	    	return null;
	    }
	}//end collectFirstPhaseInput()
	  
	/**
	   * Perform execution of the tool.<P>
	   * 
	   * @param mclient
	   *   The network connection to the plmaster(1) daemon.
	   * 
	   * @param qclient
	   *   The network connection to the plqueuemgr(1) daemon.
	   * 
	   * @return 
	   *   Whether to continue and collect user input for the next phase of the tool.
	   * 
	   * @throws PipelineException
	   *   If unable to sucessfully execute this phase of the tool.
	   */
	public synchronized boolean executePhase (MasterMgrClient mclient,QueueMgrClient qclient) 
		throws PipelineException
	{
	    switch (pPhase) {
	    	case 1:
	    	return executeFirstPhase(mclient, qclient);
	      
	    	case 2:
	    	return executeSecondPhase(mclient, qclient);
		
	    	default:
	    	assert (false);
	    	return false;
	    }
	}//end executePhase
	  
	
	/**
	   * get the names of all references in the Maya scene file<P>
	   * 
	   * @param mclient
	   *   The network connection to the plmaster(1) daemon.
	   * 
	   * @param qclient
	   *   The network connection to the plqueuemgr(1) daemon.
	   * 
	   * @return 
	   *   Whether to continue and collect user input for the next phase of the tool.
	   * 
	   * @throws PipelineException
	   *   If unable to sucessfully execute this phase of the tool.
	   */
	private boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
		throws PipelineException
	{
		NodeStatus stat = pSelected.get(pPrimary);
		findNodesToSwitch(stat, mclient);
	 	findActualNames(getFullFileName(stat));
	  	
		pPhase++;
		return true;
	}//end executeFirstPhase
	  
	
	/**
	   * Execute the second phase of the tool.<P>
	   * 
	   * @param mclient
	   *   The network connection to the plmaster(1) daemon.
	   * 
	   * @param qclient
	   *   The network connection to the plqueuemgr(1) daemon.
	   * 
	   * @return 
	   *   Whether to continue and collect user input for the next phase of the tool.
	   * 
	   * @throws PipelineException
	   *   If unable to sucessfully execute this phase of the tool.
	   */
	private boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
		throws PipelineException
	{
		NodeStatus stat = pSelected.get(pPrimary);	  		
		String node = stat.getName();
		String fileName = getFullFileName(stat);
		//System.err.println("Going to modify "+fileName);
	
		TreeMap<String,String> substitutions = new TreeMap<String, String>();
		
		for(String src: pSubstituteNames) {
			String newSrc = null;
				
			if(src.endsWith("_lr")){
				newSrc = src.substring(0, (src.length()-3));			
			} else {
				newSrc = src.concat("_lr");	
			}
			
			mclient.unlink(pUser, pView, node, src);
			mclient.link(pUser, pView, node, newSrc, REF, LINKALL, null);
			substitutions.put(src, newSrc);	
			//substituteInFile(fileName, src, newSrc);
		}
		substituteInFile(fileName, substitutions);
		return false;
	}//end executeSecondPhase
	
	
	
	/*---------------------------------------------*/
	/*                  HELPERS                    */    
	/*---------------------------------------------*/
	  
	/**
	 * 
	 * 
	 * @param mclient
	 * @param user
	 * @param view
	 * @param name
	 * @return The suffix of the node.
	 * @throws PipelineException
	 */
	private String getFileSuffix(MasterMgrClient mclient, String name) throws PipelineException {
		NodeMod mod = mclient.getWorkingVersion(pUser, pView, name);
		FileSeq seq = mod.getPrimarySequence();
		FilePattern pat = seq.getFilePattern();
		return pat.getSuffix();
	}
	
	
	/**
	 * Helper utility for substituting the reference strings in the Maya scene file.
	 *
	 * @param fName 	The file to be edited.
	 * @param subMap	The collection of old references and the new substitutions.
	 * @return
	 */
	private boolean substituteInFile(String fName, TreeMap<String, String> subMap)
	{		  
		/*Xiangyang Liu.***/
		try
		{
			for(String strOld: subMap.keySet()){
				String strNew = subMap.get(strOld);
				// make 32k buffer for output
				StringBuffer strOutput = new StringBuffer(32768);
				// read input file into a byte array
				byte[] pInput = ReadFile(fName);
				// make a backup copy
				//WriteFile(fName+".backup.copy",pInput);
				String strInput = new String(pInput);
				// check if words are empty
				if(strOld.equals("")||strNew.equals(""))
				{
					//System.out.println("Cannot process empty words");
					return false;
				}
				int nPos = 0;
				while(true)
				{
					int nIndex = strInput.indexOf(strOld,nPos);
					// if strOld can no longer be found, then copy the rest of the input
					if(nIndex<0){
						strOutput.append(strInput.substring(nPos));
						break;
					}
					// otherwise, replace it with strNew and continue
					else {
						strOutput.append(strInput.substring(nPos,nIndex));
						strOutput.append(strNew);
						nPos = nIndex+strOld.length();
					}
				}
				strInput = strOutput.toString();
				// write the output string to file
				WriteFile(fName,strInput.getBytes());
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		return true;
	}//end substituteInFile

	  
	// helper function to read a file into a byte array
	private final byte[] ReadFile(String strFile) throws IOException
	{
		int nSize = 32768;
		// open the input file stream
		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(strFile),nSize);
		byte[] pBuffer = new byte[nSize];
		int nPos = 0;
		// read bytes into a buffer
		nPos += inStream.read(pBuffer,nPos,nSize-nPos);
		// while the buffer is filled, double the buffer size and read more
		while(nPos==nSize)
		{
			byte[] pTemp = pBuffer;
			nSize *= 2;
			pBuffer = new byte[nSize];
			System.arraycopy(pTemp,0,pBuffer,0,nPos);
			nPos += inStream.read(pBuffer,nPos,nSize-nPos);
		}
		// close the input stream
		inStream.close();
		if(nPos==0)
		{
			return "".getBytes();
		}
		// return data read into the buffer as a byte array
		byte[] pData = new byte[nPos];
		System.arraycopy(pBuffer,0,pData,0,nPos);
		return pData;
	}//end ReadFile
	

	// helper function to write a byte array into a file
	private final void WriteFile(String strFile, byte[] pData) throws IOException
	{
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(strFile),32768);		
		if(pData.length>0) outStream.write(pData,0,pData.length);
		outStream.close();
	}
	  
	/*
	 * returns the absolute file name of the scene file for the node to be changed.
	 */  
	private String getFullFileName(NodeStatus stat) {
		NodeMod mod = stat.getDetails().getWorkingVersion();
		NodeID snodeID = stat.getNodeID();
		FileSeq fseq = mod.getPrimarySequence();
		Path script = new Path(PackageInfo.sProdPath,
						snodeID.getWorkingParent() + "/" + fseq.getPath(0));
		String fileName = script.toOsString();
		return fileName;		
	}
	  
	  
	/**
	 * Look for the actual references that exist in the file. 
	 * 
	 * @param strFile The Maya file to check for references to models.
	 */
	private void findActualNames (String strFile) throws PipelineException 
	{
	  	try 
	  	{
	  		int nSize = 32768;
			FileReader fr = new FileReader(strFile);
			BufferedReader br = new BufferedReader(fr,nSize);
			
			String curLine;
			while((curLine = br.readLine())!=null) {
				//look for references
				if(curLine.startsWith("file ")) {
					StringTokenizer strTok = new StringTokenizer(curLine," ");
					
					String token;
					while(strTok.hasMoreTokens()){
						token = strTok.nextToken();
						if(token.equals("-rfn")){
							//discard namespace
							strTok.nextToken();

							//get model reference names
							String ref = strTok.nextToken();						
							if(ref.matches(modelPattern)){
								//remove ""; and $WORKING
								int beg = ref.indexOf("/projects");		
								int end = ref.length()-2;
								ref = ref.substring(beg,end);									
									
								if(ref.endsWith(".ma")){
									String toAdd = ref.substring(0,ref.indexOf(".ma"));
									pActualNames.add(toAdd);
								}
							}//end if
						}
					}//end while
				}//end if
			}//end while

			fr.close();
			br.close();
	  	} catch (IOException ioe) {
			ioe.printStackTrace();
		}//end catch
	}//end findActualNames
	  
	  
	/**
	 * Search the sources for all model ma files that can be replaced.  
	 */
	private void findNodesToSwitch (NodeStatus status, MasterMgrClient mclient) throws PipelineException
	{		
		NodeDetails details = status.getDetails();
		if(details == null)
			throw new PipelineException("The target node must have an existing status!");

		NodeMod mod = details.getWorkingVersion();
		if(mod == null)
			throw new PipelineException("The target node must be checked-out!");
		 	
		Set<String> sources = status.getSourceNames();
		for(String src : sources) {
		   	if(src.matches(modelPattern)){
		   		String suffix = getFileSuffix(mclient, src);
		   		if(!suffix.equals("ma")){
		   			throw new PipelineException("This tool will only work with .ma files.");
		   		}
		   		pPotentialNames.add(src);
		   	}
		}//end for
	}//end findNodesToSwitch()
	 
	
	/*
	 * Test Main
	 */  
	public static void main(String[] args)
	{
		/*try {
			MasterMgrClient mclient = new MasterMgrClient();
			QueueMgrClient qclient = new QueueMgrClient();
			PluginMgrClient.init();
			pSelected = new TreeMap<String, NodeStatus>();
			SubstituteModelTool test = new SubstituteModelTool();
			test.mclient = mclient;
			
			String author = "iojomo";
			String view = "default";
			
			pPrimary = "/projects/lr/production/lair/unihead/markham/anim/unihead_markham_anim";
			NodeStatus value = mclient.status(author,view, pPrimary);
			pSelected.put(pPrimary, value);			

			while(test.collectPhaseInput()!=null){
				test.executePhase(mclient,qclient);
			}
			
			//System.err.println("DONE");
	
		} catch(PipelineException e){
			e.printStackTrace();
		}
		System.exit(0);*/
	}
	

}

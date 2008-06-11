//$Id: ShakeQuickTimeThumbnailAction.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ShakeQuickTimeThumbnailAction.v1_0_0;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   C O M P   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Executes a Shake script evaluating all FileOut nodes. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shake Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shake script to execute.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class ShakeQuickTimeThumbnailAction
extends CommonActionUtils
{  

	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public
	ShakeQuickTimeThumbnailAction() 
	{
		super("ShakeQuickTimeThumbnail", new VersionID("1.0.0"), "SCEA",
		"Executes a Shake script evaluating all FileOut nodes.");
		{
			ActionParam param = 
				new LinkActionParam
				(aMovieSource,
						"The source which contains the images used to create the movie.", 
						null);
			addSingleParam(param);
		} 
		{
			ActionParam param = 
				new IntegerActionParam
				(aXRes,
						"The x resolution of the movie.", 640
				);
			addSingleParam(param);
		} 
		{
			ActionParam param = 
				new IntegerActionParam
				(aYRes,
						"The y resolution of the movie.", 480
				);
			addSingleParam(param);
		} 

		{
			ActionParam param = 
				new IntegerActionParam
				(aFrame, 
						"Specifies the frame number of the move to process.", 
						1);
			addSingleParam(param);
		}

		addExtraOptionsParam(); 

		{  
			LayoutGroup layout = new LayoutGroup(true);
			layout.addEntry(aMovieSource); 
			layout.addSeparator();
			layout.addEntry(aXRes); 
			layout.addEntry(aYRes);
			layout.addSeparator();
			layout.addEntry(aFrame);
			addExtraOptionsParamToLayout(layout); 

			setSingleLayout(layout);  
		}

		addSupport(OsType.MacOS);
		//underDevelopment();
	}



	/*----------------------------------------------------------------------------------------*/
	/*   A C T I O N                                                                          */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
	 * fulfill the given action agenda. <P> 
	 * 
	 * @param agenda
	 *   The agenda to be accomplished by the action.
	 * 
	 * @param outFile 
	 *   The file to which all STDOUT output is redirected.
	 * 
	 * @param errFile 
	 *   The file to which all STDERR output is redirected.
	 * 
	 * @return 
	 *   The SubProcess which will fulfill the agenda.
	 * 
	 * @throws PipelineException 
	 *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
	 *   information in the action agenda or a general failure of the prep method code.
	 */
	public SubProcessHeavy
	prep
	(
			ActionAgenda agenda,
			File outFile, 
			File errFile 
	)
	throws PipelineException
	{ 
		Integer frame = getSingleIntegerParamValue(aFrame);
		if (frame == null)
			throw new PipelineException
			("The frame number was not set!!!");
		/* write the temp Shake script to evaluate */
		File script = createTemp(agenda, "shk");
		try {      

			Integer x = getSingleIntegerParamValue(aXRes); 
			Integer y = getSingleIntegerParamValue(aYRes);


			FileSeq sourceSeq = null;

			String sname = getSingleStringParamValue(aMovieSource); 
			if(sname == null) 
				throw new PipelineException
				("The Movie Source was not set!");

			FileSeq fseq = agenda.getPrimarySource(sname);
			if(fseq == null) 
				throw new PipelineException
				("Somehow the " + aMovieSource + " (" + sname + ") was not one of the " + 
				"source nodes!");

			sourceSeq = fseq;

			NodeID sNodeID = new NodeID(agenda.getNodeID(), sname);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));

			out.println("// Shake v4.10.0606 - (c) Apple Computer, Inc. 1998-2006.  All Rights Reserved.");
			out.println("// Apple, the Apple logo and Shake are trademarks of Apple Computer, Inc., registered in the U.S. and other countries.");
			out.println("");
			out.println("");
			out.println("SetTimeRange(\"1-90\");");
			out.println("SetFieldRendering(0);");
			out.println("SetFps(30);");
			out.println("SetMotionBlur(1, 1, 0);");
			out.println("SetQuality(1);");
			out.println("SetUseProxy(\"Base\");");
			out.println("SetProxyFilter(\"default\");");
			out.println("SetPixelScale(1, 1);");
			out.println("SetUseProxyOnMissing(1);");
			out.println("SetDefaultWidth(" + x + ");");
			out.println("SetDefaultHeight(" + y + ");");
			out.println("SetDefaultBytes(2);");
			out.println("SetDefaultAspect(1);");
			out.println("SetDefaultViewerAspect(1);");
			out.println("SetMacroCheck(1);");
			out.println("SetTimecodeMode(\"30 FPS\");");
			out.println("");
			out.println("DefineProxyPath(\"No_Precomputed_Proxy\", 1, 1, -1, \"Auto\", -1, 0, 0, \"\",1);");
			out.println("DefineProxyPath(\"No_Precomputed_Proxy\", 0.5, 1, 1, \"Auto\", 0, 0, 1, \"\");");
			out.println("DefineProxyPath(\"No_Precomputed_Proxy\", 0.25, 1, 1, \"Auto\", 0, 0, 2, \"\");");
			out.println("DefineProxyPath(\"No_Precomputed_Proxy\", 0.1, 1, 1, \"Auto\", 0, 0, 3, \"\");");
			out.println("SetAudio(\"100W@E0000qFdsuHW962Dl9BOW0mWa06w7mCJ000000000008\");");
			out.println("");
			out.println("// Input nodes");
			out.println("");
			out.println("seq001_004_lgt_test_render = SFileIn(\"" + new Path(PackageInfo.sProdPath, sNodeID.getWorkingParent()).toString() + "/" + sourceSeq.getPath(0) + "\", ");
			out.println("    \"Auto\", 0, 0, \"v1.1\", \"0\", \"\");");
			out.println("");
			out.println("// Processing nodes");
			out.println(""); 
			out.println("Resize1 = Resize(seq001_004_lgt_test_render, " + x + ", " + y + ", \"sinc\", ");
			out.println("    1);");
			fseq = agenda.getPrimaryTarget();
			out.println("random = FileOut(Resize1, \"" + new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0)) + "\", \"Auto\");");
			out.println("");
			out.println("");
			out.println("// User Interface settings");
			out.println("");
			out.println("SetKey(");
			out.println("    \"colorPicker.hex\", \"0\",");
			out.println("    \"colorPicker.range\", \"32\",");
			out.println("    \"globals.broadcastHighQuality\", \"1\",");
			out.println("    \"globals.broadcastMonitorNumber\", \"2\",");
			out.println("    \"globals.broadcastViewerAspectRatio\", \"script.defaultViewerAspectRatio\",");
			out.println("    \"globals.cacheMode\", \"2\",");
			out.println("    \"globals.consoleLineLength\", \"120\",");
			out.println("    \"globals.displayThumbnails\", \"1\",");
			out.println("    \"globals.enhancedNodeView\", \"0\",");
			out.println("    \"globals.fileBrowser.favorites\", \"/;$HOME;/Users/bmcconnell//nreal/;/Applications/Shake/;/Applications/Shake/doc/pix/;/Volumes/Media_CSG;/shakesan/projects/;\",");
			out.println("    \"globals.fileBrowserExactList\", \"0\",");
			out.println("    \"globals.fileBrowserFilterList\", \"1\",");
			out.println("    \"globals.fileBrowserFullList\", \"0\",");
			out.println("    \"globals.fileBrowserHeight\", \"540\",");
			out.println("    \"globals.fileBrowserImageList\", \"0\",");
			out.println("    \"globals.fileBrowserLC1\", \"230\",");
			out.println("    \"globals.fileBrowserLC2\", \"70\",");
			out.println("    \"globals.fileBrowserLC3\", \"110\",");
			out.println("    \"globals.fileBrowserLC4\", \"245\",");
			out.println("    \"globals.fileBrowserLC5\", \"175\",");
			out.println("    \"globals.fileBrowserLC6\", \"65\",");
			out.println("    \"globals.fileBrowserLC7\", \"111\",");
			out.println("    \"globals.fileBrowserRIntSet\", \"0\",");
			out.println("    \"globals.fileBrowserSC1\", \"211\",");
			out.println("    \"globals.fileBrowserSC2\", \"211\",");
			out.println("    \"globals.fileBrowserSC3\", \"211\",");
			out.println("    \"globals.fileBrowserSeqList\", \"0\",");
			out.println("    \"globals.fileBrowserShortList\", \"0\",");
			out.println("    \"globals.fileBrowserWIntSet\", \"1\",");
			out.println("    \"globals.fileBrowserWidth\", \"790\",");
			out.println("    \"globals.fileBrowserfullPath\", \"0\",");
			out.println("    \"globals.fontBlue\", \"1\",");
			out.println("    \"globals.fontGreen\", \"1\",");
			out.println("    \"globals.fontRed\", \"1\",");
			out.println("    \"globals.gridBlue\", \"0.15\",");
			out.println("    \"globals.gridEnabled\", \"0\",");
			out.println("    \"globals.gridGreen\", \"0.15\",");
			out.println("    \"globals.gridHeight\", \"40\",");
			out.println("    \"globals.gridRed\", \"0.15\",");
			out.println("    \"globals.gridVisible\", \"0\",");
			out.println("    \"globals.gridWidth\", \"40\",");
			out.println("    \"globals.layoutTightness\", \"40\",");
			out.println("    \"globals.multiPlaneLocatorScale\", \"1\",");
			out.println("    \"globals.noodleABlue\", \"1\",");
			out.println("    \"globals.noodleAGreen\", \"1\",");
			out.println("    \"globals.noodleARed\", \"1\",");
			out.println("    \"globals.noodleAZBlue\", \"0.4\",");
			out.println("    \"globals.noodleAZGreen\", \"0\",");
			out.println("    \"globals.noodleAZRed\", \"0\",");
			out.println("    \"globals.noodleBWABlue\", \"0.4\",");
			out.println("    \"globals.noodleBWAGreen\", \"0.4\",");
			out.println("    \"globals.noodleBWARed\", \"0.4\",");
			out.println("    \"globals.noodleBWAZBlue\", \"0.4\",");
			out.println("    \"globals.noodleBWAZGreen\", \"0\",");
			out.println("    \"globals.noodleBWAZRed\", \"0\",");
			out.println("    \"globals.noodleBWBlue\", \"0.15\",");
			out.println("    \"globals.noodleBWGreen\", \"0.15\",");
			out.println("    \"globals.noodleBWRed\", \"0.15\",");
			out.println("    \"globals.noodleBWZBlue\", \"0.4\",");
			out.println("    \"globals.noodleBWZGreen\", \"0\",");
			out.println("    \"globals.noodleBWZRed\", \"0\",");
			out.println("    \"globals.noodleBlue\", \"1\",");
			out.println("    \"globals.noodleColorCoding\", \"2\",");
			out.println("    \"globals.noodleGreen\", \"1\",");
			out.println("    \"globals.noodleRGBABlue\", \"0.7\",");
			out.println("    \"globals.noodleRGBAGreen\", \"0.7\",");
			out.println("    \"globals.noodleRGBARed\", \"0.7\",");
			out.println("    \"globals.noodleRGBAZBlue\", \"1\",");
			out.println("    \"globals.noodleRGBAZGreen\", \"0.4\",");
			out.println("    \"globals.noodleRGBAZRed\", \"0.4\",");
			out.println("    \"globals.noodleRGBBlue\", \"0\",");
			out.println("    \"globals.noodleRGBGreen\", \"0.7\",");
			out.println("    \"globals.noodleRGBRed\", \"0.7\",");
			out.println("    \"globals.noodleRGBZBlue\", \"0.8\",");
			out.println("    \"globals.noodleRGBZGreen\", \"0.2\",");
			out.println("    \"globals.noodleRGBZRed\", \"0.2\",");
			out.println("    \"globals.noodleRed\", \"1\",");
			out.println("    \"globals.noodleStipple16\", \"1061109567\",");
			out.println("    \"globals.noodleStipple32\", \"-1\",");
			out.println("    \"globals.noodleStipple8\", \"-1431655766\",");
			out.println("    \"globals.noodleTension\", \"0.25\",");
			out.println("    \"globals.noodleZBlue\", \"0.4\",");
			out.println("    \"globals.noodleZGreen\", \"0\",");
			out.println("    \"globals.noodleZRed\", \"0\",");
			out.println("    \"globals.paintFrameMode\", \"1\",");
			out.println("    \"globals.pdfBrowserPath\", \"\",");
			out.println("    \"globals.proxyTog.cycle\", \"-1,0,0,0,0,-1\",");
			out.println("    \"globals.renderModeTog.cycle\", \"2,0,0,2\",");
			out.println("    \"globals.rotoAutoControlScale\", \"1\",");
			out.println("    \"globals.rotoBuildColorBlue\", \"0.75\",");
			out.println("    \"globals.rotoBuildColorGreen\", \"0.75\",");
			out.println("    \"globals.rotoBuildColorRed\", \"0.375\",");
			out.println("    \"globals.rotoControlScale\", \"1\",");
			out.println("    \"globals.rotoFocusColorBlue\", \"0.9375\",");
			out.println("    \"globals.rotoFocusColorGreen\", \"0.375\",");
			out.println("    \"globals.rotoFocusColorRed\", \"0.5\",");
			out.println("    \"globals.rotoFocusSelectColorBlue\", \"0.5\",");
			out.println("    \"globals.rotoFocusSelectColorGreen\", \"1\",");
			out.println("    \"globals.rotoFocusSelectColorRed\", \"0.5\",");
			out.println("    \"globals.rotoKeyedColorBlue\", \"1\",");
			out.println("    \"globals.rotoKeyedColorGreen\", \"0.65\",");
			out.println("    \"globals.rotoKeyedColorRed\", \"0.45\",");
			out.println("    \"globals.rotoNormalColorBlue\", \"0.125\",");
			out.println("    \"globals.rotoNormalColorGreen\", \"0.75\",");
			out.println("    \"globals.rotoNormalColorRed\", \"0.75\",");
			out.println("    \"globals.rotoNormalSelectColorBlue\", \"0.0625\",");
			out.println("    \"globals.rotoNormalSelectColorGreen\", \"1\",");
			out.println("    \"globals.rotoNormalSelectColorRed\", \"0.0625\",");
			out.println("    \"globals.rotoPickRadius\", \"15\",");
			out.println("    \"globals.rotoTangentColorBlue\", \"0.125\",");
			out.println("    \"globals.rotoTangentColorGreen\", \"0.5625\",");
			out.println("    \"globals.rotoTangentColorRed\", \"0.5625\",");
			out.println("    \"globals.rotoTangentCreationRadius\", \"10\",");
			out.println("    \"globals.rotoTempKeyColorBlue\", \"0\",");
			out.println("    \"globals.rotoTempKeyColorGreen\", \"0.5\",");
			out.println("    \"globals.rotoTempKeyColorRed\", \"1\",");
			out.println("    \"globals.rotoTransformIncrement\", \"5\",");
			out.println("    \"globals.showActiveGlows\", \"2\",");
			out.println("    \"globals.showConcatenationLinks\", \"2\",");
			out.println("    \"globals.showExpressionLinks\", \"2\",");
			out.println("    \"globals.showTimeDependency\", \"2\",");
			out.println("    \"globals.textureProxy\", \"1\",");
			out.println("    \"globals.thumbAlphaBlend\", \"1\",");
			out.println("    \"globals.thumbSize\", \"15\",");
			out.println("    \"globals.thumbSizeRelative\", \"0\",");
			out.println("    \"globals.viewerAspectRatio\", \"script.defaultViewerAspectRatio\",");
			out.println("    \"globals.viewerZoom\", \"1.0/proxyScale\",");
			out.println("    \"globals.virtualSliderMode\", \"0\",");
			out.println("    \"globals.virtualSliderSpeed\", \"0.25\",");
			out.println("    \"globals.warpBoundaryNormalColorBlue\", \"0\",");
			out.println("    \"globals.warpBoundaryNormalColorGreen\", \"0.5\",");
			out.println("    \"globals.warpBoundaryNormalColorRed\", \"1\",");
			out.println("    \"globals.warpConnectionNormalColorBlue\", \"0.8\",");
			out.println("    \"globals.warpConnectionNormalColorGreen\", \"0\",");
			out.println("    \"globals.warpConnectionNormalColorRed\", \"0.4\",");
			out.println("    \"globals.warpDisplacedNormalColorBlue\", \"1\",");
			out.println("    \"globals.warpDisplacedNormalColorGreen\", \"0\",");
			out.println("    \"globals.warpDisplacedNormalColorRed\", \"1\",");
			out.println("    \"globals.warpLockedColorBlue\", \"0.6\",");
			out.println("    \"globals.warpLockedColorGreen\", \"0.6\",");
			out.println("    \"globals.warpLockedColorRed\", \"0.6\",");
			out.println("    \"globals.warpSourceNormalColorBlue\", \"1\",");
			out.println("    \"globals.warpSourceNormalColorGreen\", \"0.8\",");
			out.println("    \"globals.warpSourceNormalColorRed\", \"0\",");
			out.println("    \"globals.warpTargetNormalColorBlue\", \"0.7\",");
			out.println("    \"globals.warpTargetNormalColorGreen\", \"0.2\",");
			out.println("    \"globals.warpTargetNormalColorRed\", \"0.2\",");
			out.println("    \"globals.webBrowserPath\", \"\",");
			out.println("    \"mainQuad.bot\", \"0.6\",");
			out.println("    \"mainQuad.left\", \"0.29\",");
			out.println("    \"mainQuad.right\", \"0.35\",");
			out.println("    \"mainQuad.top\", \"0.6\",");
			out.println("    \"mainWin.height\", \"996\",");
			out.println("    \"mainWin.tabChild1\", \"0.Image\",");
			out.println("    \"mainWin.tabChild10\", \"0.CG\",");
			out.println("    \"mainWin.tabChild11\", \"0.Socom\",");
			out.println("    \"mainWin.tabChild12\", \"0.User\",");
			out.println("    \"mainWin.tabChild13\", \"0.PixelFarm\",");
			out.println("    \"mainWin.tabChild14\", \"0.GenArts\",");
			out.println("    \"mainWin.tabChild15\", \"0.Curve_Editor_2\",");
			out.println("    \"mainWin.tabChild16\", \"0.Node_View_2\",");
			out.println("    \"mainWin.tabChild17\", \"0.Time_View\",");
			out.println("    \"mainWin.tabChild18\", \"1.Parameters1\",");
			out.println("    \"mainWin.tabChild19\", \"1.Parameters2\",");
			out.println("    \"mainWin.tabChild2\", \"0.Color\",");
			out.println("    \"mainWin.tabChild20\", \"1.Globals\",");
			out.println("    \"mainWin.tabChild21\", \"2.Viewers\",");
			out.println("    \"mainWin.tabChild3\", \"0.Filter\",");
			out.println("    \"mainWin.tabChild4\", \"0.Key\",");
			out.println("    \"mainWin.tabChild5\", \"0.Layer\",");
			out.println("    \"mainWin.tabChild6\", \"0.Transform\",");
			out.println("    \"mainWin.tabChild7\", \"0.Warp\",");
			out.println("    \"mainWin.tabChild8\", \"0.Other\",");
			out.println("    \"mainWin.tabChild9\", \"0.REVision\",");
			out.println("    \"mainWin.width\", \"1674\",");
			out.println("    \"mainWin.xPos\", \"3\",");
			out.println("    \"mainWin.yPos\", \"7\",");
			out.println("    \"nodeView.Resize1.t\", \"0\",");
			out.println("    \"nodeView.Resize1.x\", \"348.25\",");
			out.println("    \"nodeView.Resize1.y\", \"226.375\",");
			out.println("    \"nodeView.random.t\", \"0\",");
			out.println("    \"nodeView.random.x\", \"344.75\",");
			out.println("    \"nodeView.random.y\", \"155\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.t\", \"1\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.tnChannel\", \"0\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.tnTime\", \"1\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.tnVisible\", \"1\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.x\", \"279.5\",");
			out.println("    \"nodeView.seq001_004_lgt_test_render.y\", \"278.875\",");
			out.println("    \"nodeView.xPan\", \"0\",");
			out.println("    \"nodeView.yPan\", \"0\",");
			out.println("    \"nodeView.zoom\", \"0.8\",");
			out.println("    \"pixelAnalyzer1.aStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.accumulate\", \"0\",");
			out.println("    \"pixelAnalyzer1.bStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.bit16ToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.bit32ToggleState\", \"1\",");
			out.println("    \"pixelAnalyzer1.bit8ToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.gStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.hStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.hex\", \"0\",");
			out.println("    \"pixelAnalyzer1.imgToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.lStatToggleState\", \"1\",");
			out.println("    \"pixelAnalyzer1.offToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.pxlToggleState\", \"1\",");
			out.println("    \"pixelAnalyzer1.rStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.sStatToggleState\", \"0\",");
			out.println("    \"pixelAnalyzer1.vStatToggleState\", \"0\",");
			out.println("    \"spawnedViewerCount\", \"0\",");
			out.println("    \"timeBar.current\", \"1\",");
			out.println("    \"timeBar.high\", \"100\",");
			out.println("    \"timeBar.incr\", \"1\",");
			out.println("    \"timeBar.low\", \"1\",");
			out.println("    \"timeView.ctrls.selGroup\", \"0\",");
			out.println("    \"timeView.list.open.seq001_004_lgt_test_render\", \"1\",");
			out.println("    \"timeView.list.open.seq001_004_lgt_test_render.cycle\", \"-2,-2,1\",");
			out.println("    \"timeView.wSpace.constDisp\", \"0\",");
			out.println("    \"timeView.wSpace.dispInOut\", \"1\",");
			out.println("    \"timeView.wSpace.endTime\", \"308.89\",");
			out.println("    \"timeView.wSpace.startTime\", \"1\",");
			out.println("    \"timeView.wSpace.trim\", \"0\",");
			out.println("    \"updater.mode\", \"2\",");
			out.println("    \"vDesk.0.chanTog.0.cycle\", \"4,0,0,0,0,0\",");
			out.println("    \"vDesk.0.chanTog.1.cycle\", \"4,0,0,0,0,0\",");
			out.println("    \"vDesk.0.compareMode\", \"0\",");
			out.println("    \"vDesk.0.compareTog.cycle\", \"-1,0,0,0,-1\",");
			out.println("    \"vDesk.0.displayModeA\", \"0\",");
			out.println("    \"vDesk.0.displayModeB\", \"0\",");
			out.println("    \"vDesk.0.dodNodeSerial\", \"SetDOD(Select1, 0, " + x + ", 0, " + y + ");\",");
			out.println("    \"vDesk.0.dodToggle\", \"0\",");
			out.println("    \"vDesk.0.g\", \"1\",");
			out.println("    \"vDesk.0.h\", \"661\",");
			out.println("    \"vDesk.0.i\", \"0\",");
			out.println("    \"vDesk.0.ih\", \"0\",");
			out.println("    \"vDesk.0.isActive\", \"1\",");
			out.println("    \"vDesk.0.iw\", \"0\",");
			out.println("    \"vDesk.0.lookupNodeSerial0\", \"Truelight_VLUT_(ViewerDodSelect, \\\"none\\\", \\\"monitor\\\", \\\"log\\\", 0, 0, 0, 0, 0, 0, 1, 0.01, 16, 0.1978, 0.4683);\",");
			out.println("    \"vDesk.0.lookupNodeSerial1\", \"ViewerLookup1_(ViewerDodSelect, 1, 0, 2, 0, 0, 0, 95, rBlack, rBlack, 685, rWhite, rWhite, 0.6, rNGamma, rNGamma, 1.7, rDGamma, rDGamma, 0, rSoftClip, rSoftClip);\",");
			out.println("    \"vDesk.0.lookupTog.cycle\", \"1,0,0,0\",");
			out.println("    \"vDesk.0.lookupToggle\", \"0\",");
			out.println("    \"vDesk.0.monitoredNodeA\", \"NRiScript1.random\",");
			out.println("    \"vDesk.0.numViewerLookups\", \"2\",");
			out.println("    \"vDesk.0.numViewerScripts\", \"6\",");
			out.println("    \"vDesk.0.oscAutoKeyOnOff\", \"0\",");
			out.println("    \"vDesk.0.oscLockTog.cycle\", \"1,0,0,0\",");
			out.println("    \"vDesk.0.oscOnOff\", \"1\",");
			out.println("    \"vDesk.0.oscTog.cycle\", \"1,0,0,1\",");
			out.println("    \"vDesk.0.roiOnOff\", \"1\",");
			out.println("    \"vDesk.0.scriptNodeSerial0\", \"ApertureMarking(riNode, \\\"Auto\\\", 0, 0, 1, 1, 1.0, 1.0, 1.0, 1.0, 3, 1, 0.2, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 1, 0.4, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 1, 0.6, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 0, 0.8, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 0, 0.5, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 0, 0.25, academyDefaultRed, academyDefaultGreen, academyDefaultBlue, academyDefaultAlpha, academyDefaultLineWidth, 1.0, 1.0, 1.0, 1.0, 3, 0, 0.2, fullDefaultRed, fullDefaultGreen, fullDefaultBlue, fullDefaultAlpha, fullDefaultLineWidth, 1, 0.4, fullDefaultRed, fullDefaultGreen, fullDefaultBlue, fullDefaultAlpha, fullDefaultLineWidth, 1, 0.6, fullDefaultRed, fullDefaultGreen, fullDefaultBlue, fullDefaultAlpha, fullDefaultLineWidth, 1.0, 1.0, 1.0, 1.0, 1, 1, 20, 0.25, tvDefaultRed, tvDefaultGreen, tvDefaultBlue, tvDefaultAlpha, tvDefaultLineWidth, 1, 10, 0.5, tvDefaultRed, tvDefaultGreen, tvDefaultBlue, tvDefaultAlpha, tvDefaultLineWidth);\",");
			out.println("    \"vDesk.0.scriptNodeSerial1\", \"ViewerScript_2_(riNode, 1, 1, 1, Input.width/2, Input.height/2);\",");
			out.println("    \"vDesk.0.scriptNodeSerial2\", \"ViewerScript_3_(riNode, 3, 0, .5);\",");
			out.println("    \"vDesk.0.scriptNodeSerial3\", \"ViewZ(riNode, 0, 0, 0, 5000000, 1, 100);\",");
			out.println("    \"vDesk.0.scriptNodeSerial4\", \"FloatView_(riNode, 2, 0, 0, 1, 1-red1, 1-green1, 1-blue1);\",");
			out.println("    \"vDesk.0.scriptNodeSerial5\", \"TimeCode_(riNode, \\\"Timecode\\\", 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, .5, 20, 20);\",");
			out.println("    \"vDesk.0.scriptTog.cycle\", \"-1,0,0,0,0,0,0,-1\",");
			out.println("    \"vDesk.0.scriptToggle\", \"0\",");
			out.println("    \"vDesk.0.updateModeA\", \"1\",");
			out.println("    \"vDesk.0.updateModeB\", \"1\",");
			out.println("    \"vDesk.0.updateTog.0.cycle\", \"-1,0,0,1\",");
			out.println("    \"vDesk.0.updateTog.1.cycle\", \"-1,0,0,1\",");
			out.println("    \"vDesk.0.w\", \"998\",");
			out.println("    \"vDesk.0.x\", \"0\",");
			out.println("    \"vDesk.0.xPan\", \"-144\",");
			out.println("    \"vDesk.0.xliderValue\", \"0\",");
			out.println("    \"vDesk.0.y\", \"0\",");
			out.println("    \"vDesk.0.yPan\", \"-56\",");
			out.println("    \"vDesk.0.zoom\", \"1\",");
			out.println("    \"vDesk.viewers\", \"1\"");
			out.println(");");

			out.close();
		}
		catch(IOException ex) {
			new PipelineException
			("Unable to write temporary MEL script file (" + script + ") for Job " + 
					"(" + agenda.getJobID() + ")!\n" +
					ex.getMessage());
		}

		/* the target frame range */ 
		FrameRange range = agenda.getPrimaryTarget().getFrameRange();

		/* create the process to run the action */ 
		{
			ArrayList<String> args = new ArrayList<String>();
			args.add("-exec");
			args.add(script.getPath());
			args.add("-t");
			args.add(frame.toString()); 

			args.addAll(getExtraOptionsArgs());

			return createSubProcess(agenda, "shake", args, outFile, errFile);
		}
	}



	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/


	public static final String aMovieSource = "MovieSource";
	public static final String aXRes = "XRes";
	public static final String aYRes = "YRes";
	public static final String aFrame = "Frame";
	//public static final String aShakeScript = "ShakeScript";
	private static final long serialVersionUID = -1016125057351138873L;
}


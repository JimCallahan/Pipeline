// $Id: MRayOptionsAction.java,v 1.4 2007/08/08 03:25:48 jesse Exp $

package us.temerity.pipeline.plugin.MRayOptionsAction.v2_3_5;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   O P T I O N S   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class MRayOptionsAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayOptionsAction() 
  {
    super("MRayOptions", new VersionID("2.3.5"), "Temerity",
	  "Creates a mi file which contains a mental ray options block.");
    
    {
      ActionParam param = 
	new StringActionParam
	(aOptionsName,
	 "The Name of the option block to create.", 
	 "defaultOptions");
      addSingleParam(param);
    }
    
    
    ArrayList<String> threeChoices = new ArrayList<String>();
    threeChoices.add("none");
    threeChoices.add("on");
    threeChoices.add("off");
    
    LayoutGroup sampleGroup = 
      new LayoutGroup("Sampling", "Settings related to sampling", true);
    {
      add(new IntegerActionParam(aSamplesMin, dSamples, -2), sampleGroup);
      add(new IntegerActionParam(aSamplesMax, dSamples, 0), sampleGroup);
      add(new IntegerActionParam(aSamplesDefMin, dSamplesDef, -128), sampleGroup);
      add(new IntegerActionParam(aSamplesDefMax, dSamplesDef, 127), sampleGroup);
      sampleGroup.addSeparator();
      add(new IntegerActionParam(aSamplesCollect, dSamplesCollect, 4), sampleGroup);
      add(new DoubleActionParam(aShadingSamples, dShadingSamples, 1.0), sampleGroup);
      add(new IntegerActionParam(aSamplesMotion, dSamplesMotion, 1), sampleGroup);
      
      LayoutGroup contrastGroup = 
	new LayoutGroup("Contrast", "Settings related to contrast", true);
      add(new DoubleActionParam(aContrastR, dContrast, .1), contrastGroup);
      add(new DoubleActionParam(aContrastG, dContrast, .1), contrastGroup);
      add(new DoubleActionParam(aContrastB, dContrast, .1), contrastGroup);
      add(new DoubleActionParam(aContrastA, dContrast, .1), contrastGroup);
      contrastGroup.addSeparator();
      add(new DoubleActionParam(aTimeContrastR, dTimeContrast, .2), contrastGroup);
      add(new DoubleActionParam(aTimeContrastG, dTimeContrast, .2), contrastGroup);
      add(new DoubleActionParam(aTimeContrastB, dTimeContrast, .2), contrastGroup);
      add(new DoubleActionParam(aTimeContrastA, dTimeContrast, .2), contrastGroup);
      sampleGroup.addSubGroup(contrastGroup);
      
      LayoutGroup filterGroup = 
	new LayoutGroup("Filtering", "Settings related to filtering", true);
      filterPresets(filterGroup);
      {
	String array[] = {"box", "triangle", "gauss", "mitchell", "lanczos"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aFilter, dFilter, "box", options), filterGroup);
      }
      add(new DoubleActionParam(aFilterHeight, dFilter, 1.0), filterGroup);
      add(new DoubleActionParam(aFilterWidth, dFilter, 1.0), filterGroup);
      add(new BooleanActionParam(aFilterClip, dFilterClip, false), filterGroup);
      add(new BooleanActionParam(aJitter, dJitter, true), filterGroup);
      sampleGroup.addSubGroup(filterGroup);
    }
    LayoutGroup hardwareGroup = 
      new LayoutGroup("Hardware", "Settings related hardware rendering", false);
    {
      {
	String array[] = {"off", "on", "all"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aHardwareMode, dHardwareMode, "off", options), hardwareGroup);
      }
      add(new BooleanActionParam(aHardwareCG, dHardwareCG, true), hardwareGroup);
      add(new BooleanActionParam(aHardwareNative, dHardwareNative, false), hardwareGroup);
      add(new BooleanActionParam(aHardwareFast, dHardwareFast, false), hardwareGroup);
      add(new BooleanActionParam(aHardwareForce, dHardwareForce, false), hardwareGroup);
    }
    
    LayoutGroup motionGroup = 
      new LayoutGroup("MotionBlur", "Settings related to motion blur", false);
    {
      add(new DoubleActionParam(aShutterSpeed, dShutter, 0.0), motionGroup);
      add(new DoubleActionParam(aShutterDelay, dShutter, 0.0), motionGroup);
      add(new EnumActionParam(aMotion, dMotion, "none", threeChoices), motionGroup);
      add(new IntegerActionParam(aMotionSteps, dMotionSteps, 1), motionGroup);
    }
    
    LayoutGroup shadowGroup = 
      new LayoutGroup("Shadow Settings", "Settings related to shadows", false);
    {
      {
	String array[] = {"off", "on", "sort", "segments"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aShadows, dShadows, "on", options), shadowGroup);
      }
      {
	String array[] = {"off", "on", "opengl", "detail"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aShadowMap, dShadowMap, "off", options), shadowGroup);
      }
      add(new BooleanActionParam(aShadowMapOnly, dShadowMapOnly, false), shadowGroup);
      add(new BooleanActionParam(aShdwMapRebuild, dShdwMapRebuild, false), shadowGroup);
      add(new BooleanActionParam(aShdwMapMerge, dShdwMapMerge, false), shadowGroup);
      add(new BooleanActionParam(aShdwMapMotion, dShdwMapMotion, true), shadowGroup);
      add(new DoubleActionParam(aShadowMapBias, dShadowMapBias, -1.0), shadowGroup);
    }
    
    LayoutGroup renderingGroup = 
      new LayoutGroup("Rendering Algorithms", 
	"Settings related to the different rendering algorithms", true);
    {
      add(new BooleanActionParam(aTrace, dTrace, true), renderingGroup);
      add(new IntegerActionParam(aReflectDepth, dReflectDepth, 2), renderingGroup);
      add(new IntegerActionParam(aRefractDepth, dRefractDepth, 2), renderingGroup);
      add(new IntegerActionParam(aTotalDepth, dTotalDepth, 4), renderingGroup);
      renderingGroup.addSeparator();
      {
	String array[] = {"on", "off", "rapid", "opengl"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aScanline, dScanline, "on", options), renderingGroup);
      }
      renderingGroup.addSeparator();
      {
	String array[] = {"bsp", "grid", "large bsp"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aAcceleration, dAcceleration, "bsp", options), renderingGroup);
      }
      renderingGroup.addSeparator();
      add(new IntegerActionParam(aBSPSize, dBSPSize, 10), renderingGroup);
      add(new IntegerActionParam(aBSPDepth, dBSPDepth, 40), renderingGroup);
      add(new BooleanActionParam(aBSPShadow, dBSPShadow, false), renderingGroup);
      renderingGroup.addSeparator();
      add(new IntegerActionParam(aGridResX, dGridRes, 0), renderingGroup);
      add(new IntegerActionParam(aGridResY, dGridRes, 0), renderingGroup);
      add(new IntegerActionParam(aGridResZ, dGridRes, 0), renderingGroup);
      add(new IntegerActionParam(aGridDepth, dGridDepth, 2), renderingGroup);
      add(new IntegerActionParam(aGridSize, dGridSize, 128), renderingGroup);
    }
    
    LayoutGroup featureGroup = 
      new LayoutGroup("Feature Settings", "Settings related to disable features", false);
    {
      add(new BooleanActionParam(aLens, dLens, true), featureGroup);
      add(new BooleanActionParam(aVolume, dVolume, true), featureGroup);
      add(new BooleanActionParam(aGeometry, dGeometry, true), featureGroup);
      add(new BooleanActionParam(aDisplace, dDisplace, true), featureGroup);
      add(new BooleanActionParam(aDisplacePre, dDisplacePre, true), featureGroup);
      add(new BooleanActionParam(aOutput, dOutput, true), featureGroup);
      add(new BooleanActionParam(aAutovolume, dAutovolume, true), featureGroup);
      add(new BooleanActionParam(aPhotonAutovol, dPhotonAutovol, true), featureGroup);
      add(new BooleanActionParam(aPass, dPass, true), featureGroup);
      {
	String array[] = {"on", "off", "only"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aLightmap, dLightmap, "on", options), featureGroup);
      }
    }
    
    LayoutGroup photonGroup = 
      new LayoutGroup("Photon Effects", "Settings related to caustics and globalillum", false);
    {
      add(new IntegerActionParam(aPhotonReflect, dPhotonReflect, 5), photonGroup);
      add(new IntegerActionParam(aPhotonRefract, dPhotonRefract, 5), photonGroup);
      add(new IntegerActionParam(aPhotonTotal, dPhotonTotal, 5), photonGroup);
      photonGroup.addSeparator();
      add(new StringActionParam(aPMapFile, dPMapFile, ""), photonGroup);
      add(new BooleanActionParam(aPMapRebuild, dPMapRebuild, false), photonGroup);
      add(new BooleanActionParam(aPMapOnly, dPMapOnly, false), photonGroup);
      
      LayoutGroup causticsGroup =
	new LayoutGroup("Caustics", "Settings related to caustics", false);
      {
	add(new BooleanActionParam(aCaustics, dCaustics, false), causticsGroup);
	add(new EnumActionParam(aCausticsGen, dCausticsGen, "on", threeChoices), causticsGroup);
	add(new EnumActionParam(aCausticsRec, dCausticsRec, "on", threeChoices), causticsGroup);
	add(new IntegerActionParam(aCausticsNum, dCausticsNum, 100), causticsGroup);
	add(new DoubleActionParam(aCausticsRadius, dCausticsRadius, 0.0), causticsGroup);
	causticsGroup.addSeparator();
	{
	  String array[] = {"box", "cone", "gauss"}; 
	  ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	  add(new EnumActionParam(aCausticsFilter, dCausticsFilter, "box", options), causticsGroup);
	}
	add(new DoubleActionParam(aCausticsConst, dCausticsConst, 1.1), causticsGroup);
	causticsGroup.addSeparator();
	add(new DoubleActionParam(aCausticsMerge, dCausticsMerge, -1.0), causticsGroup);
	add(new DoubleActionParam(aCausticsScaleR, dCausticsScale, 1.0), causticsGroup);
	add(new DoubleActionParam(aCausticsScaleG, dCausticsScale, 1.0), causticsGroup);
	add(new DoubleActionParam(aCausticsScaleB, dCausticsScale, 1.0), causticsGroup);
	add(new DoubleActionParam(aCausticsScaleA, dCausticsScale, 1.0), causticsGroup);
      }
      photonGroup.addSubGroup(causticsGroup);
      
      LayoutGroup giGroup =
	new LayoutGroup("GlobalIllum", "Settings related to Global Illumination", false);
      {
	add(new BooleanActionParam(aGlobIllm, dGlobIllm, false), giGroup);
	add(new EnumActionParam(aGlobIllmGen, dGlobIllmGen, "on", threeChoices), giGroup);
	add(new EnumActionParam(aGlobIllmRec, dGlobIllmRec, "on", threeChoices), giGroup);
	add(new IntegerActionParam(aGlobIllmNum, dGlobIllmNum, 500), giGroup);
	add(new DoubleActionParam(aGlobIllmRadius, dGlobIllmRadius, 0.0), giGroup);
	giGroup.addSeparator();
	add(new DoubleActionParam(aGlobIllmMerge, dGlobIllmMerge, -1.0), giGroup);
	add(new DoubleActionParam(aGlobIllmScaleR, dGlobIllmScale, 1.0), giGroup);
	add(new DoubleActionParam(aGlobIllmScaleG, dGlobIllmScale, 1.0), giGroup);
	add(new DoubleActionParam(aGlobIllmScaleB, dGlobIllmScale, 1.0), giGroup);
	add(new DoubleActionParam(aGlobIllmScaleA, dGlobIllmScale, 1.0), giGroup);
      }
      photonGroup.addSubGroup(giGroup);
      
      LayoutGroup pvGroup =
	new LayoutGroup("PhotonVolume", "Settings related to Photon Volumes", false);
      {
	add(new IntegerActionParam(aPhotVolNum, dPhotVolNum, 30), pvGroup);
	add(new DoubleActionParam(aPhotVolRadius, dPhotVolRadius, 0.0), pvGroup);
	pvGroup.addSeparator();
	add(new DoubleActionParam(aPhotVolMerge, dPhotVolMerge, -1.0), pvGroup);
	add(new DoubleActionParam(aPhotVolScaleR, dPhotVolScale, 1.0), pvGroup);
	add(new DoubleActionParam(aPhotVolScaleG, dPhotVolScale, 1.0), pvGroup);
	add(new DoubleActionParam(aPhotVolScaleB, dPhotVolScale, 1.0), pvGroup);
	add(new DoubleActionParam(aPhotVolScaleA, dPhotVolScale, 1.0), pvGroup);
      }
      photonGroup.addSubGroup(pvGroup);
    }
    
    LayoutGroup fgGroup = 
      new LayoutGroup("FinalGather", "Settings related to Final Gathering", false);
    {
      {
	String array[] = {"on", "off", "only", "fastlookup"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aFinalGather, dFinalGather, "off", options), fgGroup);
      }
      {
	String array[] = {"None", "\"3.4\"", "\"strict 3.4\"", "\"automatic\"", "\"multiframe\""}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aFGMode, dFGMode, "\"3.4\"", options), fgGroup);
      }
      add(new IntegerActionParam(aFGPoints, dFGPoints, 10), fgGroup);	
      add(new IntegerActionParam(aFGNum, dFGNum, 1000), fgGroup);
      add(new DoubleActionParam(aFGMax, dFGMax, -1.0), fgGroup);
      add(new DoubleActionParam(aFGMin, dFGMin, -1.0), fgGroup);
      add(new BooleanActionParam(aFGViewRadius, dFGViewRadius, false), fgGroup);
      
      LayoutGroup fgOptions = 
	new LayoutGroup("FinalGatherOptions", "Additional Settings related to Final Gathering", false);
      {
	add(new DoubleActionParam(aFGFalloffStop, dFGFalloffStop, 0.0), fgOptions);
	add(new DoubleActionParam(aFGFalloffStart, dFGFalloffStart, 0.0), fgOptions);
	fgOptions.addSeparator();
	add(new StringActionParam(aFGFile, dFGFile, null), fgOptions);
	{
	  String array[] = {"off", "on", "rebuild"}; 
	  ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	  add(new EnumActionParam(aFGRebuild, dFGRebuild, "on", options), fgOptions);
	}
	fgOptions.addSeparator();
	add(new IntegerActionParam(aFGReflect, dFGReflect, 0), fgOptions);
	add(new IntegerActionParam(aFGRefract, dFGRefract, 0), fgOptions);
	add(new IntegerActionParam(aFGDiffuse, dFGDiffuse, 0), fgOptions);
	add(new IntegerActionParam(aFGTotal, dFGTotal, 0), fgOptions);
	fgOptions.addSeparator();
	add(new IntegerActionParam(aFGFilter, dFGFilter, 1), fgOptions);
	add(new DoubleActionParam(aFGPresample, dFGPresample, 1.0), fgOptions);
      }
      fgGroup.addSubGroup(fgOptions);
      
      LayoutGroup fgScale = 
	new LayoutGroup("FinalGatherScale", "Settings related to Final Gathering Scale", false);
      {
	add(new DoubleActionParam(aFGScaleR, dFGScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGScaleG, dFGScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGScaleB, dFGScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGScaleA, dFGScale, 1.0), fgScale);
	fgScale.addSeparator();
	add(new DoubleActionParam(aFGSecScaleR, dFGSecScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGSecScaleG, dFGSecScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGSecScaleB, dFGSecScale, 1.0), fgScale);
	add(new DoubleActionParam(aFGSecScaleA, dFGSecScale, 1.0), fgScale);
      }
      fgGroup.addSubGroup(fgScale);
    }
    
    LayoutGroup bufferGroup = 
      new LayoutGroup("FrameBuffer", "Settings related to the Frame Buffer", true);
    {
      {
	String array[] = {"rgb", "alpha", "raw"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aColorClip, dColorClip, "rgb", options), bufferGroup);
      }
      add(new BooleanActionParam(aDesaturate, dDesaturate, false), bufferGroup);
      add(new BooleanActionParam(aPreMultiply, dPreMultiply, true), bufferGroup);
      add(new BooleanActionParam(aDither, dDither, false), bufferGroup);
      add(new DoubleActionParam(aGamma, dGamma, 1.0), bufferGroup);
    }
    
    LayoutGroup contourGroup = 
      new LayoutGroup("Contours", "Settings related to Contour Rendering", false);
    {
      add(new StringActionParam(aContourStore, dContourStore, null), contourGroup);
      add(new StringActionParam(aContourContrast, dContourContrast, null), contourGroup);
    }
    
    LayoutGroup diagGroup = 
      new LayoutGroup("Diagnotics", "Settings related to Diagnostic Rendering", false);
    {
      {
	String array[] = {"off", "object", "world", "camera"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aDiagGrid, dDiagGrid, "off", options), diagGroup);
      }
      add(new DoubleActionParam(aDiagGridSize, dDiagGridSize, 1.0), diagGroup);
      diagGroup.addSeparator();
      {
	String array[] = {"off", "depth", "size"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aDiagBSP, dDiagBSP, "off", options), diagGroup);
      }
      diagGroup.addSeparator();
      {
	String array[] = {"off", "density", "irradiance"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aDiagPhoton, dDiagPhoton, "off", options), diagGroup);
      }
      add(new DoubleActionParam(aDiagPhotonNum, dDiagPhotonNum, 0.0), diagGroup);
      diagGroup.addSeparator();
      add(new BooleanActionParam(aDiagSample, dDiagSample, false), diagGroup);
      add(new BooleanActionParam(aDiagFinalGather, dDiagFinalGather, false), diagGroup);
    }
    
    LayoutGroup approxGroup = 
      new LayoutGroup("Approximations", "Settings that related to geometry approximations", false);
    {
      {
	String array[] = {"None", "Parametric", "Reg Parametric", "L/D/A"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aSubdMethod, dSubdMethod, "None", options), approxGroup);
      }
      {
	String array[] = {"None", "grid", "tree", "delauny", "fine"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aSurfaceStyle, dSurfaceStyle, "None", options), approxGroup);
      }
      add(new IntegerActionParam(aSubdMin, dSubdMin, 1), approxGroup);
      add(new IntegerActionParam(aSubdMax, dSubdMax, 5), approxGroup);
      add(new BooleanActionParam(aView, dView, false), approxGroup);
      LayoutGroup paramGroup =
	new LayoutGroup("Parametic", "Parametric Settings", false);
      {
	add(new DoubleActionParam(aSubdU, dSubdU, 1.0), paramGroup);
	add(new DoubleActionParam(aSubdV, dSubdV, 1.0), paramGroup);
      }
      approxGroup.addSubGroup(paramGroup);
      
      LayoutGroup ldaGroup =
	new LayoutGroup("L/D/A", "Length/Distance/Angle settings", false);
      {
	add(new DoubleActionParam(aEdgeLength, dEdgeLength, 0.0), ldaGroup);
	add(new DoubleActionParam(aDistance, dDistance, 0.0), ldaGroup);
	add(new DoubleActionParam(aAngle, dAngle, 0.0), ldaGroup);
	add(new BooleanActionParam(aSatisfyAny, dSatisfyAny, true), ldaGroup);
      }
      approxGroup.addSubGroup(ldaGroup);
    }
    
    LayoutGroup displaceGroup = 
      new LayoutGroup("Displacement Approximations", "Settings that related to displacement approximations", false);
    {
      {
	String array[] = {"None", "Parametric", "Reg Parametric", "L/D/A"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aDispMethod, dSubdMethod, "None", options), displaceGroup);
      }
      {
	String array[] = {"None", "grid", "tree", "delauny", "fine"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aDispStyle, dSurfaceStyle, "None", options), displaceGroup);
      }
      add(new IntegerActionParam(aDispSubdMin, dSubdMin, 1), displaceGroup);
      add(new IntegerActionParam(aDispSubdMax, dSubdMax, 5), displaceGroup);
      add(new BooleanActionParam(aDispView, dView, false), displaceGroup);
      LayoutGroup paramGroup =
	new LayoutGroup("Parametic", "Parametric Settings", false);
      {
	add(new DoubleActionParam(aDispSubdU, dSubdU, 1.0), paramGroup);
	add(new DoubleActionParam(aDispSubdV, dSubdV, 1.0), paramGroup);
      }
      displaceGroup.addSubGroup(paramGroup);
      
      LayoutGroup ldaGroup =
	new LayoutGroup("L/D/A", "Length/Distance/Angle settings", false);
      {
	add(new DoubleActionParam(aDispEdgeLength, dEdgeLength, 0.0), ldaGroup);
	add(new DoubleActionParam(aDispDistance, dDistance, 0.0), ldaGroup);
	add(new DoubleActionParam(aDispAngle, dAngle, 0.0), ldaGroup);
	add(new BooleanActionParam(aDispSatisfyAny, dSatisfyAny, true), ldaGroup);
      }
      displaceGroup.addSubGroup(ldaGroup);
    }
    approxGroup.addSubGroup(displaceGroup);
    
    LayoutGroup miscGroup = 
      new LayoutGroup("Misc", "Settings that don't fit elsewhere", false);
    {
      {
	String array[] = {"camera", "object"}; 
	ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	add(new EnumActionParam(aSceneGeometry, dSceneGeometry, "object", options), miscGroup);
      }
      add(new IntegerActionParam(aTaskSize, dTaskSize, -1), miscGroup);
    }
    
    LayoutGroup mayaGroup =
      new LayoutGroup("Maya", "Settings that Autodesk added to mentalray", false);
    {
      add(new BooleanActionParam(aEnableMaya, dEnableMaya, true), mayaGroup);
      add(new BooleanActionParam(aPassAlpha, dPassAlpha, true), mayaGroup);
      add(new BooleanActionParam(aPassDepth, dPassDepth, false), mayaGroup);
      add(new BooleanActionParam(aPassLabel, dPassLabel, false), mayaGroup);
      add(new BooleanActionParam(aEnableGlow, dEnableGlow, false), mayaGroup);
      add(new IntegerActionParam(aGlowBuffer, dGlowBuffer, 1), mayaGroup);
      LayoutGroup dataGroup = 
	new LayoutGroup("Maya Data", "Settings that Autodesk added to mentalray in a data block", false);
      {
	add(new IntegerActionParam(aShadowLimit, dShadowLimit, 2), dataGroup);
	{
	  String array[] = {"off", "obey", "on"}; 
	  ArrayList<String> options = new ArrayList<String>(Arrays.asList(array));
	  add(new EnumActionParam(aShadowLinking, dShadowLinking, "on", options), dataGroup);
	}
	dataGroup.addSeparator();
	add(new BooleanActionParam(aComputeFilter, dComputeFilter, true), dataGroup);
	add(new DoubleActionParam(aFilterSize, dFilterSize, 1.0), dataGroup);
	dataGroup.addSeparator();
	add(new DoubleActionParam(aASQMin, dASQMin, 0.0), dataGroup);
	add(new DoubleActionParam(aASQMax, dASQMax, 0.0), dataGroup);
	dataGroup.addSeparator();
	add(new IntegerActionParam(aReflectBlur, dReflectBlur, 1), dataGroup);
	add(new IntegerActionParam(aRefractBlur, dRefractBlur, 1), dataGroup);
      }
      mayaGroup.addSubGroup(dataGroup);
    }

    {
      LayoutGroup finalLayout = new LayoutGroup(true);
      finalLayout.addEntry(aOptionsName);
      finalLayout.addSubGroup(renderingGroup);
      finalLayout.addSubGroup(sampleGroup);
      finalLayout.addSubGroup(bufferGroup);
      finalLayout.addSubGroup(shadowGroup);
      finalLayout.addSubGroup(featureGroup);
      finalLayout.addSubGroup(mayaGroup);
      finalLayout.addSubGroup(photonGroup);
      finalLayout.addSubGroup(fgGroup);
      finalLayout.addSubGroup(motionGroup);
      finalLayout.addSubGroup(approxGroup);
      finalLayout.addSubGroup(hardwareGroup);
      finalLayout.addSubGroup(contourGroup);
      finalLayout.addSubGroup(miscGroup);
      finalLayout.addSubGroup(diagGroup);
      setSingleLayout(finalLayout);
    }

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }
  
  @SuppressWarnings("unchecked")
  private void filterPresets
  (
    LayoutGroup sampleGroup  
  )
  {
    String array[] = {"Box", "Triangle", "Gauss", "Mitchell", "Lanczos"}; 
    ArrayList<String> choices = new ArrayList<String>(Arrays.asList(array));
    addPreset(aFilterPresets, choices);
    {
      TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
      values.put(aFilter      , "box");
      values.put(aFilterHeight, 1.0);
      values.put(aFilterWidth , 1.0);
      addPresetValues(aFilterPresets, "Box", values);
    }
    {
      TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
      values.put(aFilter      , "triangle");
      values.put(aFilterHeight, 2.0);
      values.put(aFilterWidth , 2.0);
      addPresetValues(aFilterPresets, "Triangle", values);
    }
    {
      TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
      values.put(aFilter      , "gauss");
      values.put(aFilterHeight, 3.0);
      values.put(aFilterWidth , 3.0);
      addPresetValues(aFilterPresets, "Gauss", values);
    }
    {
      TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
      values.put(aFilter      , "mitchell");
      values.put(aFilterHeight, 4.0);
      values.put(aFilterWidth , 4.0);
      addPresetValues(aFilterPresets, "Mitchell", values);
    }
    {
      TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
      values.put(aFilter      , "lanczos");
      values.put(aFilterHeight, 4.0);
      values.put(aFilterWidth , 4.0);
      addPresetValues(aFilterPresets, "Lanczos", values);
    }
    sampleGroup.addEntry(aFilterPresets);
  }

  private void
  add
  (
    ActionParam param,
    LayoutGroup group
  )
  {
    addSingleParam(param);
    group.addEntry(param.getName());
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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mi", "mi Options file");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "mi");
    try {      
      FileWriter out = new FileWriter(temp);
      
      String name = getSingleStringParamValue(aOptionsName, false);
      
      boolean maya = getSingleBooleanParamValue(aEnableMaya);
      if (maya)
	writeMayaData(out, name);
      
      out.write
        ("options \""+ name +"\"\n");
      
      writeMisc(out);
      writeSampling(out);
      writeHardware(out);
      writeShadows(out);
      writeMotion(out);
      writeRendering(out);
      writeFeatures(out);
      writePhotons(out);
      writeFG(out);
      writeContour(out);
      writeDiag(out);
      writeApprox(out);
      writeDisplace(out);
      if (maya)
	writeMaya(out);
      
      if (maya)
	out.write
	  ("  data \"" + name + ":data\"\n");
      
      out.write
        ("end options\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + temp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }
  
  private String 
  pad
  (
    double d  
  )
  {
    return " " + d + " ";
  }
  
  private String 
  pad
  (
    int i
  )
  {
    return " " + i + " ";
  }
  
  private String 
  pad
  (
    String s
  )
  {
    if (s == null)
      return "";
    return " " + s + " ";
  }

  private void 
  writeSampling
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    double cr = getSingleDoubleParamValue(aContrastR, new Range<Double>(0d, 1d, true));
    double cg = getSingleDoubleParamValue(aContrastG, new Range<Double>(0d, 1d, true));
    double cb = getSingleDoubleParamValue(aContrastB, new Range<Double>(0d, 1d, true));
    double ca = getSingleDoubleParamValue(aContrastA, new Range<Double>(0d, 1d, true));
    double tcr = getSingleDoubleParamValue(aTimeContrastR, new Range<Double>(0d, 1d, true));
    double tcg = getSingleDoubleParamValue(aTimeContrastG, new Range<Double>(0d, 1d, true));
    double tcb = getSingleDoubleParamValue(aTimeContrastB, new Range<Double>(0d, 1d, true));
    double tca = getSingleDoubleParamValue(aTimeContrastA, new Range<Double>(0d, 1d, true));
    int smin = getSingleIntegerParamValue(aSamplesMin, new Range<Integer>(-128, 127, true));
    int smax = getSingleIntegerParamValue(aSamplesMax, new Range<Integer>(-128, 127, true));
    int dsmin = getSingleIntegerParamValue(aSamplesDefMin, new Range<Integer>(-128, 127, true));
    int dsmax = getSingleIntegerParamValue(aSamplesDefMax, new Range<Integer>(-128, 127, true));
    int scol = getSingleIntegerParamValue(aSamplesCollect);
    double shdsamp = getSingleDoubleParamValue(aShadingSamples);
    int sampmot = getSingleIntegerParamValue(aSamplesMotion);
    String filter = getSingleStringParamValue(aFilter, false);
    boolean clip = getSingleBooleanParamValue(aFilterClip);
    if (clip && (filter.equals("mitchell") || filter.equals("lanczos")))
      filter += " clip";
    double fw = getSingleDoubleParamValue(aFilterWidth, new Range<Double>(0d, null, true));
    double fh = getSingleDoubleParamValue(aFilterHeight, new Range<Double>(0d, null, true));
    double jit = getSingleBooleanParamValue(aJitter) ? 1.0 : 0.0;
    
    out.write
      ("  #Sampling Settings\n" +
       "  contrast" + pad(cr) + pad(cg) + pad(cb) + pad(ca) + "\n" +
       "  time contrast" + pad(tcr) + pad(tcg) + pad(tcb) + pad(tca) + "\n" +
       "  samples" + pad(smin) + pad(smax) + pad(dsmin) + pad(dsmax) + "\n" +
       "  samples collect " + scol + "\n" +
       "  shading samples " + shdsamp + "\n" +
       "  samples motion " + sampmot + "\n" +
       "  filter" + pad(filter) + pad(fw) + pad(fh)+ "\n" +
       "  jitter " + jit + "\n\n\n");
  }
  
  private void 
  writeHardware
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String hw = getSingleStringParamValue(aHardwareMode, false);
    String cg = getSingleBooleanParamValue(aHardwareCG) ? "cg" : null;
    String ntv = getSingleBooleanParamValue(aHardwareNative) ? "native" : null;
    String fst = getSingleBooleanParamValue(aHardwareFast) ? "fast" : null;
    String frc = getSingleBooleanParamValue(aHardwareForce) ? "force" : null;
    
    out.write
      ("  #Hardware Settings\n" +
       "  hardware " + hw + "\n" +
       "  hardware" + pad(cg) + pad(ntv) + pad(fst) + pad(frc) + "\n\n\n");
  }
  
  private void 
  writeMotion
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    double spd = getSingleDoubleParamValue(aShutterSpeed, new Range<Double>(0.0, 1.0, true));
    double dly = getSingleDoubleParamValue(aShutterDelay, new Range<Double>(0.0, 1.0, true));
    String mtn = getSingleStringParamValue(aMotion, false);
    mtn = (mtn.equals("none")) ? null : mtn;
    int stp = getSingleIntegerParamValue(aMotionSteps, new Range<Integer>(1, 15, true));
    
    out.write
      ("  #Motion Blur Settings\n" +
       "  shutter" + pad(dly) + pad(spd) + "\n");
    if (mtn != null)
      out.write
        ("  motion " + mtn + "\n");
    out.write
      ("  motion steps " + stp + "\n\n\n");
  }
  
  private void 
  writeShadows
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String shd = getSingleStringParamValue(aShadows, false);
    String smap = getSingleStringParamValue(aShadowMap, false);
    boolean only = getSingleBooleanParamValue(aShadowMapOnly);
    String rbld = getSingleBooleanParamValue(aShdwMapRebuild) ? "on" : "off";
    boolean mrg = getSingleBooleanParamValue(aShdwMapMerge);
    String mtn = getSingleBooleanParamValue(aShdwMapMotion) ? "on" : "off";
    double bias = getSingleDoubleParamValue(aShadowMapBias);
    
    out.write
      ("  #Shadow Settings\n" +
       "  shadow " + shd + "\n" +
       "  shadowmap " + smap + "\n" +
       "  shadowmap motion " + mtn + "\n");
    if (only)
      out.write
        ("  shadowmap only\n");
    out.write
      ("  shadowmap rebuild " + rbld + "\n");
    if (mrg)
      out.write
        ("  shadowmap rebuild merge\n");
    if (bias <= 0)
      out.write
        ("  shadowmap bias " + bias + "\n");
    out.write("\n\n");
  }
  
  private void 
  writeRendering
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String trc = getSingleBooleanParamValue(aTrace) ? "on" : "off";
    int rld = getSingleIntegerParamValue(aReflectDepth, new Range<Integer>(0, null, true));
    int rrd = getSingleIntegerParamValue(aRefractDepth, new Range<Integer>(0, null, true));
    int td = getSingleIntegerParamValue(aTotalDepth, new Range<Integer>(0, null, true));
    String scn = getSingleStringParamValue(aScanline, false);
    String acc = getSingleStringParamValue(aAcceleration, false);
    int bsps = getSingleIntegerParamValue(aBSPSize, new Range<Integer>(0, null, true));
    int bspd = getSingleIntegerParamValue(aBSPDepth, new Range<Integer>(0, null, true));
    String bspshd = getSingleBooleanParamValue(aBSPShadow) ? "on" : "off";
    int grdX = getSingleIntegerParamValue(aGridResX, new Range<Integer>(0, null, true));
    int grdY = getSingleIntegerParamValue(aGridResY, new Range<Integer>(0, null, true));
    int grdZ = getSingleIntegerParamValue(aGridResZ, new Range<Integer>(0, null, true));
    int grdd = getSingleIntegerParamValue(aGridDepth, new Range<Integer>(0, null, true));
    int grds = getSingleIntegerParamValue(aGridSize, new Range<Integer>(0, null, true));
    
    out.write
      ("  #Render Algorithm Settings\n" +
       "  trace " + trc + "\n" +
       "  trace depth" + pad(rld) + pad(rrd) + pad(td) + "\n" +
       "  scanline " + scn + "\n" +
       "  acceleration " + acc + "\n");
    if (acc.equals("grid"))
      out.write
        ("  grid resolution " + pad(grdX) + pad(grdY) + pad(grdZ) + "\n" +
         "  grid depth " + grdd + "\n" +
         "  grid size " + grds + "\n");
    else
      out.write
        ("  bsp size " + bsps + "\n" +
         "  bsp depth " + bspd + "\n" +
         "  bsp shadow " + bspshd + "\n");
    out.write("\n\n");
  }
  
  private void 
  writeFeatures
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String lens = getSingleBooleanParamValue(aLens) ? "on" : "off";
    String vol = getSingleBooleanParamValue(aVolume) ? "on" : "off";
    String geo = getSingleBooleanParamValue(aGeometry) ? "on" : "off";
    String disp = getSingleBooleanParamValue(aDisplace) ? "on" : "off";
    String dispp = getSingleBooleanParamValue(aDisplacePre) ? "on" : "off";
    String outp = getSingleBooleanParamValue(aOutput) ? "on" : "off";
    String auto = getSingleBooleanParamValue(aAutovolume) ? "on" : "off";
    String pauto = getSingleBooleanParamValue(aPhotonAutovol) ? "on" : "off";
    String pass = getSingleBooleanParamValue(aPass) ? "on" : "off";
    String lgt = getSingleStringParamValue(aLightmap, false);
    
    out.write
    ("  #Feature Settings\n" +
     "  lens " + lens + "\n" +
     "  volume " + vol + "\n" +
     "  geometry " + geo + "\n" +
     "  displace " + disp + "\n" +
     "  displace presample " + dispp + "\n" +
     "  output " + outp + "\n" +
     "  autovolume " + auto + "\n" +
     "  photon autovolume " + pauto + "\n" +
     "  pass " + pass + "\n" +
     "  lightmap " + lgt + "\n\n\n"); 
  }
  
  private void 
  writePhotons
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    out.write
      ("  #Photon Settings\n");
    boolean caus = getSingleBooleanParamValue(aGlobIllm);
    boolean gi = getSingleBooleanParamValue(aCaustics);
    if (!gi && !caus) {
      out.write
      ("  caustic off\n" +
       "  globillum off\n\n\n");
      return;
    }
    int pdrl = getSingleIntegerParamValue(aPhotonReflect, new Range<Integer>(0, null, true));
    int pdrr = getSingleIntegerParamValue(aPhotonRefract, new Range<Integer>(0, null, true));
    int pdt = getSingleIntegerParamValue(aPhotonTotal, new Range<Integer>(0, null, true));
    String file = getSingleStringParamValue(aPMapFile);
    String rbld = getSingleBooleanParamValue(aPMapRebuild) ? "on" : "off";
    String only = getSingleBooleanParamValue(aPMapOnly) ? "on" : "off";
    out.write
      ("  photon trace depth" + pad(pdrl) + pad(pdrr) + pad(pdt) + "\n" +
       "  photonmap rebuild " + rbld + "\n" +
       "  photonmap only " + only + "\n");
    if (file != null)
      out.write
        ("photonmap file \"" + file + "\"\n");
    out.write("\n");
    if (caus) {
      int mode = extractMode(aCausticsGen, aCausticsRec);
      int num = getSingleIntegerParamValue(aCausticsNum, new Range<Integer>(0, null, true));
      double rad = getSingleDoubleParamValue(aCausticsRadius, new Range<Double>(0.0, null, true));
      String filt = getSingleStringParamValue(aCausticsFilter, false);
      double con = getSingleDoubleParamValue(aCausticsConst, new Range<Double>(1.0, null, false));
      double merge = getSingleDoubleParamValue(aCausticsMerge);
      double sr = getSingleDoubleParamValue(aCausticsScaleR);
      double sg = getSingleDoubleParamValue(aCausticsScaleG);
      double sb = getSingleDoubleParamValue(aCausticsScaleB);
      double sa = getSingleDoubleParamValue(aCausticsScaleA);
      
      out.write
        ("  #Caustics\n" +
         "  caustic on\n" +
         "  caustic mode " + mode + "\n" +
         "  caustic accuracy" + pad(num) + pad(rad) + "\n" +
         "  caustic filter" + pad(filt) + pad(con) + "\n" +
         "  caustic scale" + pad(sr) + pad(sg) + pad(sb) + pad(sa) + "\n");
      if (merge > 0)
	out.write
	  ("  \"caustic merge\" " + merge + "\n");
      out.write("\n\n");
    }
    if (gi) {
      int mode = extractMode(aGlobIllmGen, aGlobIllmRec);
      int num = getSingleIntegerParamValue(aGlobIllmNum, new Range<Integer>(0, null, true));
      double rad = getSingleDoubleParamValue(aGlobIllmRadius, new Range<Double>(0.0, null, true));
      double merge = getSingleDoubleParamValue(aGlobIllmMerge);
      double sr = getSingleDoubleParamValue(aGlobIllmScaleR);
      double sg = getSingleDoubleParamValue(aGlobIllmScaleG);
      double sb = getSingleDoubleParamValue(aGlobIllmScaleB);
      double sa = getSingleDoubleParamValue(aGlobIllmScaleA);
      
      out.write
        ("  #GlobIllum\n" +
         "  globillum on\n" +
         "  globillum mode " + mode + "\n" +
         "  globillum accuracy" + pad(num) + pad(rad) + "\n" +
         "  globillum scale" + pad(sr) + pad(sg) + pad(sb) + pad(sa) + "\n");
      if (merge > 0)
	out.write
	  ("  \"globillum merge\" " + merge + "\n");
      out.write("\n\n");
    }
    {
      int num = getSingleIntegerParamValue(aPhotVolNum, new Range<Integer>(0, null, true));
      double rad = getSingleDoubleParamValue(aPhotVolRadius, new Range<Double>(0.0, null, true));
      double merge = getSingleDoubleParamValue(aPhotVolMerge);
      double sr = getSingleDoubleParamValue(aPhotVolScaleR);
      double sg = getSingleDoubleParamValue(aPhotVolScaleG);
      double sb = getSingleDoubleParamValue(aPhotVolScaleB);
      double sa = getSingleDoubleParamValue(aPhotVolScaleA);
      
      out.write
        ("  #PhotVol\n" +
         "  photonvol accuracy" + pad(num) + pad(rad) + "\n" +
         "  photonvol scale" + pad(sr) + pad(sg) + pad(sb) + pad(sa) + "\n");
      if (merge > 0)
	out.write
	  ("  \"photonvol merge\" " + merge + "\n");
      out.write("\n\n");
    }
  }
  
  private void 
  writeMisc
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String spc = getSingleStringParamValue(aSceneGeometry, false);
    int task = getSingleIntegerParamValue(aTaskSize);
    
    out.write
    ("  #Other Settings\n " +
     "  " + spc + " space\n");
    if (task >= 0)
      out.write
      ("  task size " + task + "\n");
    out.write("\n\n");
  }
  
  private void 
  writeApprox
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String write = "approximate ";
    String meth = getSingleStringParamValue(aSubdMethod, false);
    if (meth.equals("None"))
      return;
    String mode = getSingleStringParamValue(aSurfaceStyle, false);
    int min = getSingleIntegerParamValue(aSubdMin, new Range<Integer>(0, 7, true));
    int max = getSingleIntegerParamValue(aSubdMax, new Range<Integer>(min, 7, true));
    double u = getSingleDoubleParamValue(aSubdU, new Range<Double>(0.0, null, true));
    double v = getSingleDoubleParamValue(aSubdV, new Range<Double>(0.0, null, true));
    double len = getSingleDoubleParamValue(aEdgeLength, new Range<Double>(0.0, null, true));
    double dist = getSingleDoubleParamValue(aDistance, new Range<Double>(0.0, null, true));
    double angle = getSingleDoubleParamValue(aAngle, new Range<Double>(0.0, 180d, true));
    boolean any = getSingleBooleanParamValue(aSatisfyAny);
    boolean view = getSingleBooleanParamValue(aView);
    
    if (meth.equals("L/D/A")) {
      if (mode.equals("fine")) {
	write += "fine ";
	if (view)
	  write += "view ";
	if (len > 0)
	  write += "length " + len;
      }
      else if (!mode.equals("None")) {
	write += mode + " ";
	if (view)
	  write += "view ";
	if (any)
	  write += "any ";
	if (len > 0)
	  write += "length " + len;
	if (dist >  0)
	  write += "distance " + dist;
	if (angle >  0)
	  write += "angle " + angle;
      }
    }
    else if (meth.equals("Parametric")) {
      if (mode.equals("fine") || mode.equals("grid"))
	write += mode + " ";
      write += "parametric" + pad(u) + pad(v);
    }
    else if (meth.equals("Reg Parametric")) {
      if (mode.equals("fine") || mode.equals("grid"))
	write += mode + " ";
      write += "regular parametric" + pad(u) + pad(v);
    }
    write += pad(min) + pad(max) + "all";
    out.write
      ("  #Approximation Settings\n " +
       "  " + write + "\n\n\n");
  }
  
  private void 
  writeDisplace
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String write = "approximate displace ";
    String meth = getSingleStringParamValue(aDispMethod, false);
    if (meth.equals("None"))
      return;
    String mode = getSingleStringParamValue(aDispStyle, false);
    int min = getSingleIntegerParamValue(aDispSubdMin, new Range<Integer>(0, 7, true));
    int max = getSingleIntegerParamValue(aDispSubdMax, new Range<Integer>(min, 7, true));
    double u = getSingleDoubleParamValue(aDispSubdU, new Range<Double>(0.0, null, true));
    double v = getSingleDoubleParamValue(aDispSubdV, new Range<Double>(0.0, null, true));
    double len = getSingleDoubleParamValue(aDispEdgeLength, new Range<Double>(0.0, null, true));
    double dist = getSingleDoubleParamValue(aDispDistance, new Range<Double>(0.0, null, true));
    double angle = getSingleDoubleParamValue(aDispAngle, new Range<Double>(0.0, 180d, true));
    boolean any = getSingleBooleanParamValue(aDispSatisfyAny);
    boolean view = getSingleBooleanParamValue(aDispView);
    
    if (meth.equals("L/D/A")) {
      if (mode.equals("fine")) {
	write += "fine ";
	if (view)
	  write += "view ";
	if (len > 0)
	  write += "length " + len;
      }
      else if (!mode.equals("None")) {
	write += mode + " ";
	if (view)
	  write += "view ";
	if (any)
	  write += "any ";
	if (len > 0)
	  write += "length " + len;
	if (dist >  0)
	  write += "distance " + dist;
	if (angle >  0)
	  write += "angle " + angle;
      }
    }
    else if (meth.equals("Parametric")) {
      if (mode.equals("fine") || mode.equals("grid"))
	write += mode + " ";
      write += "parametric" + pad(u) + pad(v);
    }
    else if (meth.equals("Reg Parametric")) {
      if (mode.equals("fine") || mode.equals("grid"))
	write += mode + " ";
      write += "regular parametric" + pad(u) + pad(v);
    }
    write += pad(min) + pad(max) + "all";
    out.write
      ("  #Displacement Approximation Settings\n " +
       "  " + write + "\n\n\n");
  }
  
  private void 
  writeFG
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    out.write
      ("  #Final Gathering Settings\n ");

    String fg = getSingleStringParamValue(aFinalGather, false);
    if (fg.equals("off")) {
      out.write
        ("  finalgather off\n\n\n");
      return;
    }
    String mode = getSingleStringParamValue(aFGMode, false);
    int pnt = getSingleIntegerParamValue(aFGPoints, new Range<Integer>(0, null, true));
    int num = getSingleIntegerParamValue(aFGNum, new Range<Integer>(0, null, true));
    double max = getSingleDoubleParamValue(aFGMax);
    double min = getSingleDoubleParamValue(aFGMin);
    boolean view = getSingleBooleanParamValue(aFGViewRadius);
    double start = getSingleDoubleParamValue(aFGFalloffStart);
    double stop = getSingleDoubleParamValue(aFGFalloffStop);
    String file = getSingleStringParamValue(aFGFile);
    int filt = getSingleIntegerParamValue(aFGFilter, new Range<Integer>(0, 10, true));
    String rebld = getSingleStringParamValue(aFGRebuild, false);
    int rfl = getSingleIntegerParamValue(aFGReflect, new Range<Integer>(0, null, true));
    int rfr = getSingleIntegerParamValue(aFGRefract, new Range<Integer>(0, null, true));
    int dif = getSingleIntegerParamValue(aFGDiffuse, new Range<Integer>(0, null, true));
    int tot = getSingleIntegerParamValue(aFGTotal, new Range<Integer>(0, null, true));
    double pre = getSingleDoubleParamValue(aFGPresample, new Range<Double>(0.0, null, true));
    double sr = getSingleDoubleParamValue(aFGScaleR, new Range<Double>(0.0, null, true));
    double sg = getSingleDoubleParamValue(aFGScaleG, new Range<Double>(0.0, null, true));
    double sb = getSingleDoubleParamValue(aFGScaleB, new Range<Double>(0.0, null, true));
    double sa = getSingleDoubleParamValue(aFGScaleA, new Range<Double>(0.0, null, true));
    double ssr = getSingleDoubleParamValue(aFGSecScaleR, new Range<Double>(0.0, null, true));
    double ssg = getSingleDoubleParamValue(aFGSecScaleG, new Range<Double>(0.0, null, true));
    double ssb = getSingleDoubleParamValue(aFGSecScaleB, new Range<Double>(0.0, null, true));
    double ssa = getSingleDoubleParamValue(aFGSecScaleA, new Range<Double>(0.0, null, true));
    
    out.write
      ("  finalgather " + fg + "\n");
    if (!mode.equals("None"))
      out.write
        ("  \"finalgather mode\" " + mode + "\n");
    if (mode.equals("\"automatic\"") || mode.equals("\"multiframe\"") )
      out.write
        ("  \"finalgather points\" " + pnt + "\n");
    String acc = "  finalgather accuracy";
    if (view)
      acc += (" view ");
    acc += pad(num);
    if (max >= 0) {
      acc += pad(max);
      if (min >= 0)
	acc += pad(min);
    }
    out.write
      (acc + "\n");
    out.write
      ("  finalgather falloff" + pad(start) + pad(stop) + "\n" );
    if (file != null)
      out.write("  finalgather file \"" + file +"\"\n");
    out.write
      ("  finalgather filter " + filt + "\n" +
       "  finalgather rebuild " + rebld + "\n" +
       "  finalgather trace depth" + pad(rfl) + pad(rfr) + pad(dif) + pad(tot) + "\n" +
       "  finalgather presample density " + pre + "\n" +
       "  finalgather scale" + pad(sr) + pad(sg) + pad(sb) + pad(sa) + "\n" + 
       "  finalgather secondary scale" + pad(ssr) + pad(ssg) + pad(ssb) + pad(ssa) + "\n\n\n");
  }
  
  private void 
  writeDiag
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String grd = getSingleStringParamValue(aDiagGrid, false);
    double grdN = getSingleDoubleParamValue(aDiagGridSize, new Range<Double>(0d, null, true));
    String bsp = getSingleStringParamValue(aDiagBSP, false);
    String phot = getSingleStringParamValue(aDiagPhoton, false);
    double photN = getSingleDoubleParamValue(aDiagPhotonNum, new Range<Double>(0d, null, true));
    String samp = getSingleBooleanParamValue(aDiagSample) ? "on" : "off";
    String fg = getSingleBooleanParamValue(aDiagFinalGather) ? "on" : "off";
    
    out.write
      ("  #Diagnostic Settings\n" +
       "  diagnostic grid" + pad(grd) + pad(grdN) + "\n" +
       "  diagnostic bsp " + bsp + "\n" +
       "  diagnostic photon" + pad(phot) + pad(photN) + "\n" +
       "  diagnostic samples " + samp + "\n" +
       "  diagnostic finalgather " + fg +  "\n\n\n");
  }
  
  private void 
  writeContour
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    String str = getSingleStringParamValue(aContourStore);
    String con = getSingleStringParamValue(aContourContrast);
    
    out.write
      ("  #Contour Settings\n ");
    if (str != null)
      out.write
        ("  contour store " + str + "\n");
    if (con != null)
      out.write
        ("  contour contrast" + con + "\n");
    out.write("\n\n");
  }
  
  private void 
  writeMayaData
  (
    FileWriter out,
    String optionName
  )
    throws PipelineException, IOException
  {
    String dataName = optionName + ":data";
    
    String glw = getSingleBooleanParamValue(aEnableGlow) ? "on" : "off";
    int lmt = getSingleIntegerParamValue(aShadowLimit, new Range<Integer>(0, null, true));
    int link = getSingleEnumParamIndex(aShadowLinking);
    String comp = getSingleBooleanParamValue(aComputeFilter) ? "on" : "off";
    double size = getSingleDoubleParamValue(aFilterSize, new Range<Double>(0.0, null, true));
    double amin = getSingleDoubleParamValue(aASQMin, new Range<Double>(0.0, null, true));
    double amax = getSingleDoubleParamValue(aASQMax, new Range<Double>(0.0, null, true));
    int rfl = getSingleIntegerParamValue(aReflectBlur, new Range<Integer>(0, null, true));
    int rfr = getSingleIntegerParamValue(aRefractBlur, new Range<Integer>(0, null, true));
    
    out.write
      ("data \"" + dataName + "\"\n" + 
       "\"maya_options\" (\n" +
       "  \"magic\" 1298233697,\n" +
       "  \"shadowLimit\" " + lmt + ",\n" +
       "  \"shadowLinking\" " + link + ",\n" +
       "  \"computeFilterSize\" " + comp + ",\n" +
       "  \"defaultFilterSize\" " + size + ",\n" + 
       "  \"disableShaderGlow\" " + glw + ",\n" +
       "  \"asqMinThreshold\" " + amin + ",\n" +
       "  \"asqMaxThreshold\" " + amax + ",\n" +
       "  \"reflectionBlurLimit\" " + rfl + ",\n" +
       "  \"refractionBlurLimit\" " + rfr + "\n" +
       ")\n\n");
  }
  
  private void 
  writeMaya
  (
    FileWriter out
  )
    throws PipelineException, IOException
  {
    boolean glw = getSingleBooleanParamValue(aEnableGlow);
    String alp = getSingleBooleanParamValue(aPassAlpha) ? "on" : "off";
    String dep = getSingleBooleanParamValue(aPassDepth) ? "on" : "off";
    String lbl = getSingleBooleanParamValue(aPassLabel) ? "on" : "off";
    int buf = getSingleIntegerParamValue(aGlowBuffer, new Range<Integer>(1, 7, true));
    
    out.write
    ("  #Maya Settings\n ");
    
    if (glw)
      out.write
        ("frame buffer " + (buf - 1) + " \"+rgb\"\n");
      
    out.write
      ("  state \"maya_state\" (\n" +
       "    \"passAlphaThrough\" " + alp + "\n" + 
       "    \"passDepthThrough\" " + dep + "\n" +
       "    \"passLabelThrough\" " + lbl + "\n");
    if (glw)
      out.write
        ("    \"glowColorBuffer\" " + buf + "\n");
    out.write
      ("  )\n\n");
  }
  
  private int
  extractMode
  (
    String genName,
    String recName
  )
    throws PipelineException
  {
    int gen = 0;
    int rec = 0;
    
    String genV = getSingleStringParamValue(genName);
    String recV = getSingleStringParamValue(recName);
    if (genV.equals("on"))
      gen = 1;
    else if (genV.equals("off"))
      gen = 4;
    if (recV.equals("on"))
      rec = 2;
    else if (recV.equals("off"))
      rec = 8;
    return rec + gen;
  }
  
  public static final String aOptionsName   = "OptionsName";
  private static final long serialVersionUID = -8860352635127568020L;

  
  /*---Sampling------------------------------------------------------------------------------*/
  public static final String aContrastR      = "ContrastR";
  public static final String aContrastG      = "ContrastG";
  public static final String aContrastB      = "ContrastB";
  public static final String aContrastA      = "ContrastA";
  public static final String aTimeContrastR  = "TimeContrastR";
  public static final String aTimeContrastG  = "TimeContrastG";
  public static final String aTimeContrastB  = "TimeContrastB";
  public static final String aTimeContrastA  = "TimeContrastA";
  public static final String aSamplesMin     = "SamplesMin";
  public static final String aSamplesMax     = "SamplesMax";
  public static final String aSamplesDefMin  = "SamplesDefMin";
  public static final String aSamplesDefMax  = "SamplesDefMax";
  public static final String aSamplesCollect = "SamplesCollect";
  public static final String aShadingSamples = "ShadingSamples";
  public static final String aSamplesMotion  = "SamplesMotion";
  public static final String aFilterPresets  = "FilterPresets";
  public static final String aFilter         = "Filter";
  public static final String aFilterWidth    = "FilterWidth";
  public static final String aFilterHeight   = "FilterHeight";
  public static final String aFilterClip     = "FilterClip";
  public static final String aJitter         = "Jitter";
  
  /*---Hardware Rendering--------------------------------------------------------------------*/
  public static final String aHardwareMode   = "HardwareMode";
  public static final String aHardwareCG     = "HardwareCG";
  public static final String aHardwareNative = "HardwareNative";
  public static final String aHardwareFast   = "HardwareFast";
  public static final String aHardwareForce  = "HardwareForce";
  
  /*---Tesselation---------------------------------------------------------------------------*/
  public static final String aSubdMethod     = "SubdMethod";
  public static final String aSurfaceStyle   = "SurfaceStyle";
  public static final String aSubdU          = "SubdU";
  public static final String aSubdV          = "SubdV";
  public static final String aSubdMin        = "SubdMin";
  public static final String aSubdMax        = "SubdMax";
  public static final String aEdgeLength     = "EdgeLength";
  public static final String aDistance       = "Distance";
  public static final String aAngle          = "Angle";
  public static final String aSatisfyAny     = "SatisfyAny";
  public static final String aView           = "View";
  
  public static final String aDispMethod     = "DispMethod";
  public static final String aDispStyle      = "DispStyle";
  public static final String aDispSubdU      = "DispSubdU";
  public static final String aDispSubdV      = "DispSubdV";
  public static final String aDispSubdMin    = "DispSubdMin";
  public static final String aDispSubdMax    = "DispSubdMax";
  public static final String aDispEdgeLength = "DispEdgeLength";
  public static final String aDispDistance   = "DispDistance";
  public static final String aDispAngle      = "DispAngle";
  public static final String aDispSatisfyAny = "DispSatisfyAny";
  public static final String aDispView       = "DispView";
  
  /*---Motion Blur---------------------------------------------------------------------------*/
  public static final String aShutterSpeed   = "ShutterSpeed";
  public static final String aShutterDelay   = "ShutterDelay";
  public static final String aMotion         = "Motion";
  public static final String aMotionSteps    = "MotionSteps";
  
  /*---Shadows-------------------------------------------------------------------------------*/
  public static final String aShadows        = "Shadows";
  public static final String aShadowMap      = "ShadowMap";
  public static final String aShadowMapOnly  = "ShadowMapOnly";
  public static final String aShdwMapRebuild = "ShdwMapRebuild";
  public static final String aShdwMapMerge   = "ShdwMapMerge";
  public static final String aShdwMapMotion  = "ShdwMapMotion";
  public static final String aShadowMapBias  = "ShadowMapBias";
  
  /*---Rendering Algorithms------------------------------------------------------------------*/
  public static final String aTrace          = "Trace";
  public static final String aReflectDepth   = "ReflectDepth";
  public static final String aRefractDepth   = "RefractDepth";
  public static final String aTotalDepth     = "TotalDepth";
  public static final String aScanline       = "Scanline";
  public static final String aAcceleration   = "Acceleration";
  public static final String aBSPSize        = "BSPSize";
  public static final String aBSPDepth       = "BSPDepth";
  public static final String aBSPShadow      = "BSPShadow";
  public static final String aGridResX       = "GridResX";
  public static final String aGridResY       = "GridResY";
  public static final String aGridResZ       = "GridResZ";
  public static final String aGridSize       = "GridSize";
  public static final String aGridDepth      = "GridDepth";
  
  /*---Features------------------------------------------------------------------------------*/
  public static final String aLens           = "Lens";
  public static final String aVolume         = "Volume";
  public static final String aGeometry       = "Geometry";
  public static final String aDisplace       = "Displace";
  public static final String aDisplacePre    = "DisplacePre";
  public static final String aOutput         = "Output";
  public static final String aAutovolume     = "Autovolume";
  public static final String aPhotonAutovol  = "PhotonAutovol";
  public static final String aPass           = "Pass";
  public static final String aLightmap       = "Lightmap";
  
  /*---Photons-------------------------------------------------------------------------------*/
  public static final String aPhotonReflect  = "PhotonReflect";
  public static final String aPhotonRefract  = "PhotonRefract";
  public static final String aPhotonTotal    = "PhotonTotal";
  public static final String aPMapFile       = "PMapFile";
  public static final String aPMapRebuild    = "PMapRebuild";
  public static final String aPMapOnly       = "PMapOnly";
  
  /*---Caustics------------------------------------------------------------------------------*/
  public static final String aCaustics       = "Caustics";
  public static final String aCausticsGen    = "CausticsGen";
  public static final String aCausticsRec    = "CausticsRec";
  public static final String aCausticsNum    = "CausticsNum";
  public static final String aCausticsRadius = "CausticsRadius";
  public static final String aCausticsFilter = "CausticsFilter";
  public static final String aCausticsConst  = "CausticsConst";
  public static final String aCausticsMerge  = "CausticsMerge";
  public static final String aCausticsScaleR = "CausticsScaleR";
  public static final String aCausticsScaleG = "CausticsScaleG";
  public static final String aCausticsScaleB = "CausticsScaleB";
  public static final String aCausticsScaleA = "CausticsScaleA";
  
  /*---Global Illumination-------------------------------------------------------------------*/
  public static final String aGlobIllm       = "GlobIllm";
  public static final String aGlobIllmGen    = "GlobIllmGen";
  public static final String aGlobIllmRec    = "GlobIllmRec";
  public static final String aGlobIllmNum    = "GlobIllmNum";
  public static final String aGlobIllmRadius = "GlobIllmRadius";
  public static final String aGlobIllmMerge  = "GlobIllmMerge";
  public static final String aGlobIllmScaleR = "GlobIllmScaleR";
  public static final String aGlobIllmScaleG = "GlobIllmScaleG";
  public static final String aGlobIllmScaleB = "GlobIllmScaleB";
  public static final String aGlobIllmScaleA = "GlobIllmScaleA";
  
  public static final String aPhotVolNum     = "PhotVolNum";
  public static final String aPhotVolRadius  = "PhotVolRadius";
  public static final String aPhotVolMerge   = "PhotVolMerge";
  public static final String aPhotVolScaleR  = "PhotVolScaleR";
  public static final String aPhotVolScaleG  = "PhotVolScaleG";
  public static final String aPhotVolScaleB  = "PhotVolScaleB";
  public static final String aPhotVolScaleA  = "PhotVolScaleA";
  
  /*---Final Gathering-----------------------------------------------------------------------*/
  public static final String aFinalGather    = "FinalGather";
  public static final String aFGMode         = "FGMode";
  public static final String aFGPoints       = "FGPoints";
  public static final String aFGNum          = "FGNum";
  public static final String aFGMax          = "FGMax";
  public static final String aFGMin          = "FGMin";
  public static final String aFGViewRadius   = "FGViewRadius";
  public static final String aFGFalloffStart = "FGFalloffStart";
  public static final String aFGFalloffStop  = "FGFalloffStop";
  public static final String aFGFile         = "FGFile";
  public static final String aFGFilter       = "FGFilter";
  public static final String aFGRebuild      = "FGRebuild";
  public static final String aFGReflect      = "FGReflect";
  public static final String aFGRefract      = "FGRefract";
  public static final String aFGDiffuse      = "FGDiffuse";
  public static final String aFGTotal        = "FGTotal";
  public static final String aFGPresample    = "FGPresample";
  public static final String aFGScaleR       = "FGScaleR";
  public static final String aFGScaleG       = "FGScaleG";
  public static final String aFGScaleB       = "FGScaleB";
  public static final String aFGScaleA       = "FGScaleA";
  public static final String aFGSecScaleR    = "FGSecScaleR";
  public static final String aFGSecScaleG    = "FGSecScaleG";
  public static final String aFGSecScaleB    = "FGSecScaleB";
  public static final String aFGSecScaleA    = "FGSecScaleA";
  
  /*---Frame Buffer--------------------------------------------------------------------------*/
  public static final String aColorClip      = "ColorClip";
  public static final String aDesaturate     = "Desaturate";
  public static final String aPreMultiply    = "PreMultiply";
  public static final String aDither         = "Dither";
  public static final String aGamma          = "Gamma";
  
  /*---Contour-------------------------------------------------------------------------------*/
  public static final String aContourStore   = "ContourStore";
  public static final String aContourContrast= "ContourContrast";
  
  /*---Diagnostic----------------------------------------------------------------------------*/
  public static final String aDiagGrid       = "DiagGrid";
  public static final String aDiagGridSize   = "DiagGridSize";
  public static final String aDiagBSP        = "DiagBSP";
  public static final String aDiagPhoton     = "DiagPhoton";
  public static final String aDiagPhotonNum  = "DiagPhotonNum";
  public static final String aDiagSample     = "DiagSample";
  public static final String aDiagFinalGather= "DiagFinalGather";
  
  /*---Other---------------------------------------------------------------------------------*/
  public static final String aSceneGeometry  = "SceneGeometry";
  public static final String aFace           = "Face";
  public static final String aTaskSize       = "TaskSize";

  /*---Maya----------------------------------------------------------------------------------*/
  public static final String aEnableMaya     = "EnableMaya";
  public static final String aPassAlpha      = "PassAlpha";
  public static final String aPassDepth      = "PassDepth";
  public static final String aPassLabel      = "PassLabel";
  public static final String aEnableGlow     = "EnableGlow";
  public static final String aGlowBuffer     = "GlowBuffer";
  public static final String aShadowLimit    = "ShadowLimit";
  public static final String aShadowLinking  = "ShadowLinking";
  public static final String aComputeFilter  = "ComputeFilter";
  public static final String aFilterSize     = "FilterSize";
  public static final String aASQMin         = "ASQMin";
  public static final String aASQMax         = "ASQMax";
  public static final String aReflectBlur    = "ReflectBlur";
  public static final String aRefractBlur    = "RefractBlur";
  
  
  
  
  /*---Sampling------------------------------------------------------------------------------*/
  private static final String dContrast = 
    "Controls oversampling. If neighboring samples differ by more than " +
    "the color r, g, b, a, supersampling kicks in.";

  private static final String dTimeContrast = 
    "Controls temporal supersampling for motion blurred scenes. " +
    "Calues for time contrast that are higher than contrast can speed up " +
    "motion blur rendering at the price of more grainy images.";

  private static final String dSamples = 
    "Each pixel is sampled at least 2^2min times and at most 2^2max times in each direction.";

  private static final String dSamplesDef = 
    "mental ray will never take fewer than 2min and more than 2max samples, and in areas " +
    "with no object sample settings it will further reduce that range to 2defmin " +
    "through 2defmax";

  private static final String dSamplesCollect = 
    "Increasing the collect rate improves motion blurring at little performance cost. ";

  private static final String dShadingSamples = 
    "Controls the number of shading calls per pixel";

  private static final String dSamplesMotion = 
    "How many points in time a moving object is sampled in rasterizer mode";

  private static final String dFilter = 
    "Specifies how multiple samples are to be combined into a single pixel value." +
    "Larger filter sizes result in softer images and may reduce rendering speed slightly";

  private static final String dFilterClip =
    "Variants of the regular Mitchell and Lanczos filters that clip the filter " +
    "result to prevent ringing";

  private static final String dJitter = 
    "Introduces systematic variations into sample locations";

  /*---Hardware Rendering--------------------------------------------------------------------*/
  public static final String dHardwareMode = 
    "(off) disables hardware rendering, (on) uses hardware rendering for all materials with a " +
    "hardware shader, (all) tries to find hardware substitutes for materials that do not " +
    "specify an explicit hardware shader.";
  
  public static final String dHardwareCG = 
    "look for shaders implemented in NVIDIA's Cg 1.2";
  
  public static final String dHardwareNative = 
    "looks for shaders implemented in the OpenGL 2.0";
  
  public static final String dHardwareFast = 
    "uses hardcoded OpenGL materials";
  
  public static final String dHardwareForce = 
    "objects only use hardware.  If no hardware shader exists, objects will render grey";
  
  /*---Tesselation---------------------------------------------------------------------------*/
  public static final String dSubdMethod = 
    "The different methods of dividing geometry. L/D/A stands for Length/Distance Angle.";
  public static final String dSurfaceStyle = 
    "The different methods of tesselating surfaces.";
  public static final String dSubdMin = 
    "The min parameter is a means to enforce a minimal triangulation fineness without any tests";
  public static final String dSubdMax = 
    "Specifies a max cutoff point for subdivisions even if criteria are not met.";
  public static final String dSubdU = 
    "parametric subdivisions in U";
  public static final String dSubdV = 
    "parametric subdivisions in V";
  public static final String dEdgeLength = 
    "subdivides the surface such that no edge length of the tessellation exceeds this value";
  public static final String dDistance = 
    "the maximum distance dist between the tessellation and the actual surface";
  public static final String dAngle = 
    "he maximum angle angle in degrees between normals of adjacent tiles of a displaced polygon";
  public static final String dSatisfyAny = 
    "the approximation stops as soon as any of the criteria is satisfied";
  public static final String dView = 
    "ontrols whether the edge argument of the length and spatial statements, and the dist " +
    "argument of the distance and curvature statements, are in the space the object is " +
    "defined in or in raster space";

  /*---Motion Blur---------------------------------------------------------------------------*/
  public static final String dShutter = 
    "The camera shutter opens at time delay and closes at time shutter.  if shutter is " +
    "equal to delay, motion blurring is disabled.";
  
  public static final String dMotion = 
    "Set to YES to force state->motion to be defined even if the shutter control " +
    "turns off blur.";
  
  public static final String dMotionSteps = 
    "how many motion path segments should be created for all motion transforms";
  
  /*---Shadows-------------------------------------------------------------------------------*/
  public static final String dShadows = 
    "Which shadowing mode to enable.  Consult docs for differences between them." +
    "Listed in order from fastest to slowest.";
  
  public static final String dShadowMap = 
    "Turns shadow maps on or off for the entire render.  OpenGL shadowmaps contain slightly " +
    "different information from those generated with the regular algorithm, and the " +
    "resulting shadows may look different.  Detail shadowmaps behave like a combination of " +
    "standard shadowmaps and raytraced shadows";
  
  public static final String dShadowMapOnly = 
    "render only shadow maps but not the color image";
  
  public static final String dShdwMapRebuild = 
    "Whether all shadow maps should be recomputed.";
  
  public static final String dShdwMapMerge = 
    "useful for building up shadowmaps for multipass rendering";
  
  public static final String dShdwMapMotion = 
    "whether shadow maps should be motion blurred.";
  
  public static final String dShadowMapBias = 
    "Applies the specified shadowmap bias to all light sources. " +
    "Specify a negative value to turn this off.";
  
  /*---Rendering Algorithms------------------------------------------------------------------*/
  public static final String dTrace = 
    "If trace off is specified, ray tracing is disabled.";
  
  public static final String dReflectDepth = 
    "limits the number of recursive reflection rays.";
  
  public static final String dRefractDepth = 
    "limits the number of recursive refraction rays.";
  
  public static final String dTotalDepth = 
    "limits the number of total rays.";
  
  public static final String dScanline = 
    "Turning scanline off forces mental ray to rely entirely on ray tracing. Rapidmode uses " +
    "a different scanline algorithm based on sample caching.  Faster motion blur, but artifacts " +
    "are possible.  opengl uses OpenGL hardware if present to further accelerate rendering.";
  
  public static final String dAcceleration = 
    "bsp is often, but not always, faster.  grid is a hierarchical voxel grid algorithm. " +
    "Grids provide faster preprocessing especially on multiprocessor systems.  large bsp is" +
    "slower but allows heavy scenes to render";
  
  public static final String dBSPSize = 
    "maximum number of primitives in a leaf of the BSP tree.  Larger leaf sizes reduce memory " +
    "consumption but increase rendering time";
  
  public static final String dBSPDepth = 
    "maximum number of levels in the BSP tree. Larger tree depths reduce rendering time but " +
    "increase memory consumption";
  
  public static final String dBSPShadow = 
    "separate shadow BSP tree that accelerates raytraced shadows";
  
  public static final String dGridRes = 
    "sets the number of grid voxels in the X, Y, and Z dimensions.  0 0 0 means mental ray " +
    "will calculate values at render time";

  public static final String dGridSize = 
    "sets the maximum number of triangles in a grid voxel";
  
  public static final String dGridDepth = 
    "sets the number of recursion levels";
  
  /*---Features------------------------------------------------------------------------------*/
  public static final String dLens = 
    "Ignore all lens shaders if set to off";
  
  public static final String dVolume = 
    "Ignore all volume shaders if set to off";
  
  public static final String dGeometry = 
    "Ignore all geometry shaders if set to off";
  
  public static final String dDisplace = 
    "Ignore all displacement shaders if set to off";
  
  public static final String dDisplacePre = 
    "disables displacement presampling";
  
  public static final String dOutput = 
    "Ignore all output shaders if set to off";
  
  public static final String dAutovolume = 
    "Autovolume mode enables a set of shader API functions that keep track of " +
    "which volumes the current point is in.";
  
  public static final String dPhotonAutovol = 
    "enables autovolume computations for light sources that are photon emitters";
  
  public static final String dPass = 
    "allows disabling all pass statements in the camera";
  
  public static final String dLightmap = 
    "If this option is set to only, only the lightmaps but not the camera images are rendered";
  
  /*---Photons-------------------------------------------------------------------------------*/
  public static final String dPhotonReflect = 
    "limits the number of recursive reflection photons";
  
  public static final String dPhotonRefract = 
    "limits the number of recursive refraction photons";
  
  public static final String dPhotonTotal = 
    "limits the number of total recursive photons";
  
  public static final String dPMapFile = 
    "use the file filename for the photon map.  created if it doesn't exist";
  
  public static final String dPMapRebuild = 
    "any existing file will be ignored, and the photon map will be recomputed and an existing " +
    "file will be overwritten";
  
  public static final String dPMapOnly = 
    "only the photon maps but not the camera images are rendered";

  /*---Caustics------------------------------------------------------------------------------*/
  public static final String dCaustics = 
    "Caustics are turned on or off";
  
  public static final String dCausticsGen = 
    "Override caustic generation flags on objects";
  
  public static final String dCausticsRec = 
    "Override caustic receiving flags on objects";
  
  public static final String dCausticsNum = 
    "the maximum number of photons that should be examined";
  
  public static final String dCausticsRadius = 
    "the maximum radius that is searched for photons.  If 0, a scene-size dependent radius " +
    "is used instead";
  
  public static final String dCausticsFilter = 
    "controls the sharpness of the caustics";
  
  public static final String dCausticsConst = 
    "Increasing the filter_const makes the caustics more blurry and decreasing makes it " +
    "even sharper but also slightly more noisy";
  
  public static final String dCausticsMerge = 
    "the caustic photons within the specified distance in world space are merged.  " +
    "Set to a negative value to disable. Mental Ray 3.5 only";
  
  public static final String dCausticsScale = 
    "Caustics are multiplied by the specified color";

  /*---Global Illumination-------------------------------------------------------------------*/
  public static final String dGlobIllm = 
    "Global Illumination is turned on or off";
  
  public static final String dGlobIllmGen = 
    "Override global illumination generation flags on objects";
  
  public static final String dGlobIllmRec = 
    "Override global illumination receiving flags on objects";
  
  public static final String dGlobIllmNum = 
    "the maximum number of photons that should be examined";
  
  public static final String dGlobIllmRadius = 
    "the maximum radius that is searched for photons.  If 0, a scene-size dependent radius " +
    "is used instead";
  
  public static final String dGlobIllmMerge = 
    "the globalillum photons within the specified distance in world space are merged.  " +
    "Set to a negative value to disable. Mental Ray 3.5 only";
  
  public static final String dGlobIllmScale = 
    "Global Illumination are multiplied by the specified color";

  public static final String dPhotVolNum = 
    "the maximum number of photons that should be examined";
  
  public static final String dPhotVolRadius = 
    "the maximum radius that is searched for photons.  If 0, a scene-size dependent radius " +
    "is used instead";
  
  public static final String dPhotVolMerge = 
    "the photon volume photons within the specified distance in world space are merged.  " +
    "Set to a negative value to disable. Mental Ray 3.5 only";
  
  public static final String dPhotVolScale = 
    "Volume Photons are multiplied by the specified color";
  
  
  /*---Final Gathering-----------------------------------------------------------------------*/
  public static final String dFinalGather = 
    "Final gathering for global illumination is turned on or off.  fastlookup mode also " +
    "turns final gathering on, but also alters the global illumination photon tracing stage " +
    "by computing the irradiance at every photon location.  only renders only the finalgather " +
    "map";
  
  public static final String dFGMode = 
   "automatic mode primarily targets rendering of single still images. The multiframe mode " +
   "targets rendering of camera fly-through animations. 3.4 mimics old behavior but " +
   "with improvements.  Mental Ray 3.5 only.  Set to null for other versions.";
  
  public static final String dFGPoints = 
    "In the automatic and multiframe finalgather modes, the number of finalgather points used.";
  
  public static final String dFGNum = 
    "controls how many rays should be used in each final gathering step to " +
    "compute the indirect illumination";
  
  public static final String dFGMax = 
    "maximum radius in which a final gather result can be interpolated or extrapolated." +
    "set to negative value to have mental ray compute a default value";
  
  public static final String dFGMin = 
    "distance within a final gather result must be used for interpolation or extrapolation. " +
    "The default is 10% of the maximum radius.  If Max is negative, this will be default as well.";
  
  public static final String dFGViewRadius = 
    "the min and max values are in pixels";
  
  public static final String dFGFalloffStop = 
    "Objects farther away than stop from the illuminated point will not cast light.";
  
  public static final String dFGFalloffStart = 
    "the beginning of a linear falloff range.  Only used when Stop is active.";
  
  public static final String dFGFile = 
    "use the file filename for loading and saving final gather points";
  
  public static final String dFGFilter = 
    "speckle elimination filter that prevents samples with extreme brightness from skewing " +
    "the overall energy.  Larger values eliminate more speckles and soften sample contrasts";
  
  public static final String dFGRebuild = 
    "all final gather points will be recomputed and an existing file will be overwritten." +
    "If freeze is set the finalgather file on disk will not be modified";
  
  public static final String dFGReflect = 
    "sets the number of recursive final gather reflection rays";
  
  public static final String dFGRefract = 
    "sets the number of recursive final gather refraction rays";
  
  public static final String dFGDiffuse = 
    "sets the number of recursive final gather diffuse rays";
  
  public static final String dFGTotal = 
    "sets the number of recursive total final gather rays";
  
  public static final String dFGPresample = 
    "controls the density of initial finalgather points";
  
  public static final String dFGScale = 
    "irradiance obtained from first bounce finalgather is multiplied " +
    "by the specified color";
  
  public static final String dFGSecScale = 
    "irradiance obtained from secondary bounce finalgather is multiplied " +
    "by the specified color";
  
  /*---Frame Buffer--------------------------------------------------------------------------*/
  public static final String dColorClip = 
    "controls how colors are clipped into a valid range [0,1] before being written to a " +
    "non-floating point frame buffer or file. raw mode should only be used if no layering " +
    "based on alpha is going to take place ";
  
  public static final String dDesaturate = 
    "If desaturation is turned off, the individual components are simply clipped into " +
    "the appropriate color range";
  
  public static final String dPreMultiply = 
    "Premultiplication means that colors are stored with alpha multiplied to R, G, and B." +
    "This option is ignored if the colorclip raw mode is in effect.";
  
  public static final String dDither = 
    "Dithering mitigates banding by introducing noise into the pixel";
  
  public static final String dGamma = 
    "Compensates for output devices with a nonlinear color response. " +
    "Gamma of 1.0 turns gamma correction off.";
  
  /*---Contour-------------------------------------------------------------------------------*/
  public static final String dContourStore = 
    "contour store shader name for contour rendering";
  public static final String dContourContrast = 
    "contour contrast shader name for contour rendering";
  
  /*---Diagnostic----------------------------------------------------------------------------*/
  public static final String dDiagGrid = 
    "Draws a colored grid on all objects in the scene visualizing the coordinate space given";
  
  public static final String dDiagGridSize =
    "distance between grid lines.";
  
  public static final String dDiagBSP = 
    "visualizes the depth and leaf size of the BSP tree used for ray tracing acceleration.";
  
  public static final String dDiagPhoton = 
    "produces a false-color rendering of photon density";
  
  public static final String dDiagPhotonNum = 
    "the density (or irradiance) that is assigned to 100%";
  
  public static final String dDiagSample = 
    "grayscale image showing the number of image samples made for each pixel.";
  
  public static final String dDiagFinalGather = 
    "shows final gathering points, as green dots for initial raster-space " +
    "final gathering points, blue dots3.4 for final gathering points from " +
    "per-object finalgather map files and red dots for render-time final " +
    "gathering points";
  
  /*---Other---------------------------------------------------------------------------------*/
 
  public static final String dSceneGeometry =
    "Camera Space exists for backwards compatibility only dnd is not recommended";
  
  public static final String dFace = 
    "Which side of geometric objects to render";
  
  public static final String dTaskSize = 
    "Smaller task sizes are convenient for previewing, but also increase " +
    "the overall rendering time. Set negative to disable.";
 
  /*---Other---------------------------------------------------------------------------------*/
  public static final String dEnableMaya = 
    "Should the maya render globals be enabled.";
  
  public static final String dPassAlpha = 
    "This option passes the mental ray alpha component of the final color as the alpha channel, " +
    "ignoring the Maya alpha component. This is useful when a custom shader is producing " +
    "an alpha value.";
  
  public static final String dPassDepth = 
    "This option overrides the Maya depth channel calculation with the default mental ray " +
    "depth channel calculation. This option is useful when you want to revert to using the " +
    "mental ray depth calculation";
  
  public static final String dPassLabel = 
    "This option passes label data untouched, rather than allowing adjustment for Maya shaders.";
  
  public static final String dEnableGlow = 
    "Should the shader glow related items be enabled.";
  
  public static final String dGlowBuffer = 
    "Which framebuffer the maya glow is rendering to.";
  
  public static final String dShadowLimit = 
    "The maximum number of times a shadow ray will penetrate a transparent or refracting object.";
  
  public static final String dShadowLinking = 
    "Controls how maya lightlinking works with shadows.  If this is set to (obey), then normal" +
    "lightlinking is used for shadow calculations.  If this is set to (on), then separate shadow" +
    "linking is used.  If it is set to off, then no shadow light linking is done.  The use" +
    "of this option pre-supposes that maya light linking is being used in the scene.";
  
  public static final String dComputeFilter = 
    "computeFilterSize in the maya_options shader";
  
  public static final String dFilterSize = 
    "defaultFilterSize in the maya_options shader";
   
  public static final String dASQMin = 
    "asqMinThreshold in the maya_options shader";
  
  public static final String dASQMax = 
    "asqMaxThreshold in the maya_options shader";
  
  public static final String dReflectBlur = 
    "Determines the blurriness of secondary reflections";
  
  public static final String dRefractBlur = 
    "Determines the blurriness of secondary refractions";
}



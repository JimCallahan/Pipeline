package us.temerity.pipeline.plugin.MayaOccMELAction.v2_3_4;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.CommonActionUtils;

/**
 * Creates a mel script that can be run on a scene to have it generate an ambient
 * occlusion pass.
 * <p>
 * Consult the mental ray documentation on its ambient occlusion shader for an
 * explanation of what the parameters control.
 * <DIV style="margin-left: 40px;">
 *   RenderType<BR>
 *   <DIV style="margin-left: 40px;">
 *    Is this a character or environment occlusion pass.
 *   </DIV> <BR>
 *   Samples<BR>
 *   <DIV style="margin-left: 40px;">
 *    The samples setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 *   Spread<BR>
 *   <DIV style="margin-left: 40px;">
 *    The spread setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 *   MaxDistance<BR>
 *   <DIV style="margin-left: 40px;">
 *    The max distance setting on the mental ray ambient occlusion shader.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class MayaOccMELAction 
  extends CommonActionUtils
{
  public
  MayaOccMELAction()
  {
    super("MayaOccMEL", new VersionID("2.3.4"), "Temerity",
      "Creates a mel script for an ambient occlusion render pass.");
    
    underDevelopment();

    {
      ActionParam param = 
	new StringActionParam
        (aShaderSuffix,
         "The Suffix to append to the shader", 
         "chOcc");
      addSingleParam(param);
    }
    {
      ActionParam param = 
	new BooleanActionParam
        (aConnectShaders,
         "Should be new shaders be connected to their shading groups?", 
         true);
      addSingleParam(param);
    }
    {
      ActionParam param = 
	new IntegerActionParam
        (aSamples,
         "The samples setting on the mental ray ambient occlusion shader.", 
         128);
      addSingleParam(param);
    }
    {
      ActionParam param = 
	new DoubleActionParam
        (aSpread,
         "The spread setting on the mental ray ambient occlusion shader.", 
         0.7);
      addSingleParam(param);
    }
    {
      ActionParam param = 
	new DoubleActionParam
        (aMaxDistance,
         "The max distance setting on the mental ray ambient occlusion shader.", 
         4000.0);
      addSingleParam(param);
    }
  }
  

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
   * @return nearDist
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
    /* sanity checks */ 
    NodeID nodeID = agenda.getNodeID();
    FileSeq fseq = agenda.getPrimaryTarget();
    if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("mel"))
      throw new PipelineException
      ("The MayaOccPass Action requires that primary target file sequence must " + 
      "be a single MEL script!"); 

    /* create a temporary shell script */ 
    File melScript = createTemp(agenda, 0644, "mel");
    Path target = new Path(PackageInfo.sProdPath, 
	nodeID.getWorkingParent() + "/" + fseq.getFile(0));
    
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(melScript)));

      String suffix = getSingleStringParamValue(aShaderSuffix);
      
      Integer samples = getSingleIntegerParamValue(aSamples, new Range<Integer>(1, null)); 

      Double spread = getSingleDoubleParamValue(aSpread, new Range<Double>(0.001, null));

      Double maxDist = getSingleDoubleParamValue(aMaxDistance, new Range<Double>(.0001, null));
      
      boolean connect = getSingleBooleanParamValue(aConnectShaders);
      
      out.write
        ("global proc breakConn(string $name)\n" + 
         "{\n" + 
         "  string $conns[] = `listConnections -p true -s true -d false $name`;\n" +
         "  if (size($conns) > 0)\n" + 
         "    catch(`disconnectAttr $conns[0] $name`);\n" + 
         "}\n\n");
      
      out.write
        ("{\n" + 
      	 "string $sg;\n" + 
      	 "for ($sg in `ls -type shadingEngine`)\n" + 
      	 "{\n" +
         "  breakConn($sg + \".miMaterialShader\");\n" + 
         "  breakConn($sg + \".miDisplacementShader\");\n" + 
         "  breakConn($sg + \".miShadowShader\");\n" + 
         "  breakConn($sg + \".miVolumeShader\");\n" + 
         "  breakConn($sg + \".miPhotonShader\");\n" + 
         "  breakConn($sg + \".miPhotonVolumeShader\");\n" + 
         "  breakConn($sg + \".miEnvironmentShader\");\n" + 
         "  breakConn($sg + \".miLightMapShader\");\n" + 
         "  breakConn($sg + \".miContourShader\");\n" + 
         "  breakConn($sg + \".miMaterialShader\");\n" + 
         "  // Maya stuff\n" + 
         "  breakConn($sg + \".surfaceShader\");\n" + 
         "  breakConn($sg + \".displacementShader\");\n" + 
         "  breakConn($sg + \".volumeShader\");\n" +
      	 "  $mrTex = `mrCreateCustomNode -asTexture \"\" mib_amb_occlusion`;\n" +
      	 "  setAttr ($mrTex+\".samples\") " + samples + ";\n" +
      	 "  setAttr ($mrTex+\".spread\") " + spread + ";\n" +
      	 "  setAttr ($mrTex+\".max_distance\") " + maxDist + ";\n" +
      	 "  string $new = `rename $mrTex ($sg + \"_" + suffix + "\")`;\n");
      	 if (connect)
      	 out.write
      	   ("  connectAttr -f ($new + \".outValue\") ($sg + \".miMaterialShader\"); \n");
      	 out.write
      	 ("}\n" +
      	  "}\n");
      
      out.close();
    } 
    catch(IOException ex) {
      throw new PipelineException
      ("Unable to write the target MEL script file (" + melScript + ") for Job " + 
       "(" + agenda.getJobID() + ")!\n" + ex.getMessage());
    }

    return createTempCopySubProcess(agenda, melScript, target, outFile, errFile);
  }

  private static final long serialVersionUID = 7899419781457153464L;

  public static final String aMaxDistance = "MaxDistance";
  public static final String aSpread = "Spread";
  public static final String aSamples = "Samples";
  public static final String aShaderSuffix = "ShaderSuffix";
  public static final String aConnectShaders = "ConnectShaders";
}

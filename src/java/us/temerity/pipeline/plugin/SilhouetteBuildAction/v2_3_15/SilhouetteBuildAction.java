// $Id: SilhouetteBuildAction.java,v 1.3 2008/05/19 11:10:17 jim Exp $

package us.temerity.pipeline.plugin.SilhouetteBuildAction.v2_3_15;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

//import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

public 
class SilhouetteBuildAction 
  extends CommonActionUtils 
{

  public 
  SilhouetteBuildAction()
  {
    super("SilhouetteBuild", new VersionID("2.3.15"), "Temerity",
      "Generates a new Silhouette scene from component image sources.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
    
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("Film (4K: 4096x3112)");
      args.add("Film (4K: 4096x3072)");
      args.add("Film (2K: 2048x1556)");
      args.add("Film (2K: 2048x1536)");
      args.add("Film Cinema (3656x2664)");
      args.add("Film Cinema (1828x1556)");
      args.add("Film Academy (1828x1332)");
      args.add("HDTV 24p (1920x1080)");
      args.add("HDTV 1080i (1920x1080)");
      args.add("HDTV 720p (1280x720)");
      args.add("NTSC (640x480)");
      args.add("NTSC (648x486)");
      args.add("NTSC DV (720x480)");
      args.add("NTSC DV Widescreen (720x480)");
      args.add("NTSC D1 (720x486)");
      args.add("NTSC D1 Square Pixels (720x540)");
      args.add("PAL D1/DV (720x576)");
      args.add("PAL D1/DV Square Pixels (768x576)");
      args.add("PAL D1/DV Widescreen (720x576)");
      
      ActionParam param = new EnumActionParam(aSession, "The type of roto session to create", "HDTV 24p (1920x1080)", args);
      addSingleParam(param);
    }
    {
      pSettings = new TreeMap<String, SessionSettings>();
      pSettings.put("Film (4K: 4096x3112)", new SessionSettings(4096, 3112, 1.0, 24));
      pSettings.put("Film (4K: 4096x3072)", new SessionSettings(4096, 3072, 1.0, 24));
      pSettings.put("Film (2K: 2048x1556)", new SessionSettings(2048, 1556, 1.0, 24));
      pSettings.put("Film (2K: 2048x1536)", new SessionSettings(2048, 1536, 1.0, 24));
      pSettings.put("Film Cinema (3656x2664)", new SessionSettings(3656, 2664, 2.0, 24) );
      pSettings.put("Film Cinema (1828x1556)", new SessionSettings(1828, 1556, 2.0, 24));
      pSettings.put("Film Academy (1828x1332)", new SessionSettings(1828, 1332, 1.0, 24));
      pSettings.put("HDTV 24p (1920x1080)", new SessionSettings(1920, 1080, 1.0, 24));
      pSettings.put("HDTV 1080i (1920x1080)", new SessionSettings(1920, 1080, 1.0, 30));
      pSettings.put("HDTV 720p (1280x720)", new SessionSettings(1280, 720, 1.0, 60) );
      pSettings.put("NTSC (640x480)", new SessionSettings(640, 480, 1.0, 29.97));
      pSettings.put("NTSC (648x486)", new SessionSettings(640, 486, 1.0, 29.97));
      pSettings.put("NTSC DV (720x480)", new SessionSettings(720, 480, 0.9, 29.97));
      pSettings.put("NTSC DV Widescreen (720x480)", new SessionSettings(720, 480, 1.2, 29.97));
      pSettings.put("NTSC D1 (720x486)", new SessionSettings(720, 486, 0.9, 29.97));
      pSettings.put("NTSC D1 Square Pixels (720x540)", new SessionSettings(720, 540, 1.0, 29.97));
      pSettings.put("PAL D1/DV (720x576)", new SessionSettings(720, 576, 1.07, 25));
      pSettings.put("PAL D1/DV Square Pixels (768x576)", new SessionSettings(768, 576, 1.0, 25));
      pSettings.put("PAL D1/DV Widescreen (720x576)", new SessionSettings(720, 576, 1.42, 25));
    }
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
   * 
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
    Path targetPath = getPrimaryTargetPath(agenda, "sfx", "Silhouette File");
    
    Integer startFrame = Integer.MAX_VALUE;
    Integer endFrame = Integer.MIN_VALUE;
    
    TreeMap<String, Path> seqs = new TreeMap<String, Path>();
    
    for (String sourceName : agenda.getSourceNames()) {
      FileSeq sseq = agenda.getPrimarySource(sourceName);
      if (sseq.hasFrameNumbers()) {
        FrameRange range = sseq.getFrameRange();
        FilePattern pat = sseq.getFilePattern();
        
        Integer start = range.getStart();
        Integer end = range.getEnd();
        
        if (start < startFrame)
          startFrame = start;
        if (end > endFrame)
          endFrame = end;
        
        int padding = pat.getPadding();
        
        String prefix = pat.getPrefix();
        String suffix = pat.getSuffix();
        String sfxName = prefix + ".[" + pad(start, padding) + "-" + pad(end, padding) + "]." + suffix;
        Path dir = getWorkingNodeFilePath(agenda, sourceName, sseq).getParentPath();
        Path finalPath = new Path(dir, sfxName);
        
        seqs.put(sfxName, finalPath);
      }
    }
    
    String targetString = targetPath.toOsString().replaceAll("\\\\", "/");
    
    String session = getSingleStringParamValue(aSession);
    SessionSettings settings = pSettings.get(session);
    int width = settings.pWidth;
    int height = settings.pHeight;
    double aspect = settings.pAspect;
    double rate = settings.pRate;
    
    TreeMap<String, Integer> pSeqIDs = new TreeMap<String, Integer>();
    File sfxFile =  createTemp(agenda, "sfx");
    int id = 0;
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(sfxFile));
      out.write("<!-- Silhouette Project File -->\n" +
      		"<Project version=\"2\" type=\"Project\" id=\"" + id++ + "\" label=\"" + targetString + "\" expanded=\"True\">\n");
      for (String each : seqs.keySet()) {
        String sourceString = seqs.get(each).toOsString().replaceAll("\\\\", "/");
        out.write("\t<Item type=\"SourceItem\" id=\"" + id++ + "\" label=\"" + each + "\" expanded=\"True\">\n");
        pSeqIDs.put(each, id);
        out.write("\t\t<Source type=\"FileSource\" id=\"" + id++ + "\" label=\"" + each + "\" expanded=\"True\" width=\""+width+"\" height=\""+height+"\">\n");
        out.write("\t\t\t<Path>"+ sourceString+"</Path>\n");
        out.write("                     <Properties>\n" + 
                  "                                <Property id=\"interlace\" constant=\"True\">\n" + 
                  "                                        <Value>None</Value>\n" + 
                  "                                </Property>\n" + 
                  "                                <Property id=\"pulldown\" constant=\"True\">\n" + 
                  "                                        <Value>None</Value>\n" + 
                  "                                </Property>\n" + 
                  "                        </Properties>\n" + 
                  "                </Source>\n" + 
                  "        </Item>\n");
      }
      out.write("\t<Item type=\"SessionItem\" id=\""+ id++ + "\" label=\"Roto\" expanded=\"True\">\n");
      out.write("\t\t<Session type=\"Session\" id=\""+ id++ + "\" label=\"Roto\" expanded=\"True\">\n");
      out.write("\t\t\t<Properties>\n");
      out.write("\t\t\t\t<Property id=\"duration\" constant=\"True\">\n");
      out.write("\t\t\t\t\t<Value>" + (endFrame - startFrame +1) + "</Value>\n");
      out.write("\t\t\t\t</Property>\n");
      out.write("                               <Property id=\"fieldMode\" constant=\"True\">\n" + 
      		"                                        <Value>false</Value>\n" + 
      		"                               </Property>\n");
      out.write("\t\t\t\t<Property id=\"frameRate\" constant=\"True\">\n");
      out.write("\t\t\t\t\t<Value>"+ rate + "</Value>\n");
      out.write("\t\t\t\t</Property>\n");
      int rotoNodeID = id;
      out.write("                               <Property id=\"mask\" constant=\"True\">\n" + 
      		"                                        <Value></Value>\n" + 
      		"                                </Property>\n" + 
      		"                                <Property id=\"nodes\">\n" + 
      		"                                        <Node type=\"RotoNode\" id=\""+ id++ +"\" label=\"Roto\" expanded=\"True\">\n" + 
      		"                                                <Properties>\n" + 
      		"                                                        <Property id=\"blur\" constant=\"True\">\n" + 
      		"                                                                <Value>0</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"invertAlpha\" constant=\"True\">\n" + 
      		"                                                                <Value>false</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"motionBlur\" constant=\"True\">\n" + 
      		"                                                                <Value>false</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"motionSamples\" constant=\"True\">\n" + 
      		"                                                                <Value>16</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"shutterAngle\" constant=\"True\">\n" + 
      		"                                                                <Value>180</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"shutterPhase\" constant=\"True\">\n" + 
      		"                                                                <Value>-90</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                        <Property id=\"useInputAlpha\" constant=\"True\">\n" + 
      		"                                                                <Value>false</Value>\n" + 
      		"                                                        </Property>\n" + 
      		"                                                </Properties>\n");
      
      String firstSource = pSeqIDs.firstKey();
      int firstID = pSeqIDs.get(firstSource);
      Path renderPath = getWorkingNodeFilePath(agenda.getNodeID(), agenda.getPrimaryTarget().getPath(0));
      out.write("                                               <Pipes>\n" + 
      		"                                                        <Pipe>\n" + 
      		"                                                                <Source ref=\"" + id + "\">output</Source>\n" + 
      		"                                                                <Target ref=\"" + rotoNodeID + "\">foreground</Target>\n" + 
      		"                                                        </Pipe>\n" + 
      		"                                                </Pipes>\n" + 
      		"                                        </Node>\n" + 
      		"\n");
      out.write("                                       <Node type=\"SourceNode\" id=\"" + id++ + "\" label=\"" + firstSource + "\" expanded=\"True\">\n" + 
      		"                                                <Properties>\n" + 
      		"                                                        <Property id=\"paint\">\n" + 
      		"                                                        </Property>\n" + 
      		"                                                </Properties>\n" + 
      		"                                                <Source ref=\"" + firstID + "\"/>\n" + 
      		"                                        </Node>\n" + 
      		"                                </Property>\n");
      out.write("                               <Property id=\"pixelAspect\" constant=\"True\">\n" + 
      		"                                        <Value>" + aspect + "</Value>\n" + 
      		"                                </Property>\n" + 
      		"                                <Property id=\"pixelFormat\" constant=\"True\">\n" + 
      		"                                        <Value>RGBFloat</Value>\n" + 
      		"                                </Property>\n" + 
      		"                                <Property id=\"size\" constant=\"True\">\n" + 
      		"                                        <Value>("+ width+".000000,"+height+".000000)</Value>\n" + 
      		"                                </Property>\n" + 
      		"                                <Property id=\"startFrame\" constant=\"True\">\n" + 
      		"                                        <Value>"+startFrame+"</Value>\n" + 
      		"                                </Property>\n");
      out.write("                    </Properties>\n" +
      		"                       <WorkRange>0,"+(endFrame-startFrame)+"</WorkRange>\n" + 
      		"                        <MultiFrameRange>0,"+(endFrame-startFrame)+"</MultiFrameRange>\n" + 
      		"                        <RenderOptions>\n" + 
      		"                                <Directory>"+renderPath.getParentPath().toOsString().replaceAll("\\\\", "/")+"</Directory>\n" + 
      		"                                <Filename>Roto.</Filename>\n" + 
      		"                                <Format>OpenEXR</Format>\n" + 
      		"                                <FormatOptions></FormatOptions>\n" + 
      		"                                <Range>All</Range>\n" + 
      		"                                <Save>Alpha</Save>\n" + 
      		"                                <Premultiply>False</Premultiply>\n" + 
      		"                                <ExternalAlpha>False</ExternalAlpha>\n" + 
      		"                                <Composite>False</Composite>\n" + 
      		"                                <MotionBlur>False</MotionBlur>\n" + 
      		"                                <Scale>1</Scale>\n" + 
      		"                                <StartFrame>-1</StartFrame>\n" + 
      		"                                <Padding>4</Padding>\n" + 
      		"                                <FieldRender>False</FieldRender>\n" + 
      		"                                <FieldDominance>Even</FieldDominance>\n" + 
      		"                                <Pulldown>None</Pulldown>\n" + 
      		"                        </RenderOptions>\n" + 
      		"                </Session>\n" + 
      		"        </Item>\n" + 
      		"</Project>");
      
      out.close();
    } 
    catch (IOException e) {
      throw new PipelineException(e); 
    }
    
    return createTempCopySubProcess(agenda, sfxFile, targetPath, outFile, errFile);
  }
  
  private String 
  pad
  (
    int value,
    int pad
  )
  {
    String toReturn = String.valueOf(value);
    while (toReturn.length() < pad)
      toReturn = "0" + toReturn;
    return toReturn;
  }
  
  private class
  SessionSettings
  {
    private SessionSettings
    (
      int width,
      int height,
      double aspect,
      double rate
    )
    {
      pWidth = width;
      pHeight = height;
      pAspect = aspect;
      pRate = rate;
    }
    
    private int pWidth;
    private int pHeight;
    private double pAspect;
    private double pRate;
  }
  
  public static final String aSession = "Session";
  
  private static final long serialVersionUID = -9125099345129472451L;

  private TreeMap<String, SessionSettings> pSettings;
}

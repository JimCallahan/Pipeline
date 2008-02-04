package com.intelligentcreatures.pipeline.plugin.PFTrackBuildAction.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*;

public class PFTrackBuildAction extends PythonActionUtils {

    public static final long serialVersionUID = -7153409256826940587L; 

    public static final String aImageSource = "ImageSource";
    public static final String aFPS = "FPS";

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////
    public PFTrackBuildAction() {
		
        super("PFTrackBuild", new VersionID("1.0.0"), "ICVFX", "Create a PFTrack shot from given image sequence");

	{
            ActionParam param = new LinkActionParam(aImageSource, "The source of images used to create shot scene", null);
            addSingleParam(param);
        }

        {
            ActionParam param = new DoubleActionParam(aFPS, "The number of image frames per second", null);
            addSingleParam(param);
        }

        {
            LayoutGroup layout = new LayoutGroup(true);

            layout.addEntry(aImageSource);
            layout.addEntry(aFPS);

            setSingleLayout(layout);
        }

        underDevelopment();
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTION
    ///////////////////////////////////////////////////////////////////////////
    public SubProcessHeavy prep(ActionAgenda agenda, File outFile, File errFile) throws PipelineException {
        // first get the file sequence
        FileSeq fileSequence = agenda.getPrimarySource(getSingleStringParamValue(aImageSource));

        // get the number of frames per second
        String framesPerSecond = String.valueOf(getSingleParamValue(aFPS));

        // get number of frames
        String frameCount = String.valueOf(fileSequence.numFrames());
        
        if (fileSequence == null) {
            throw new PipelineException("For some odd reason the Filesequence couldn't be created!");
        }

        // now first create the IMS file that will be loaded in the PSC script
        // IMS TEMPLATE
        // #IMS
        // 4
        // 25.0
        // "/path/to/frame1.jpg"
        // "path/to/frame2.jpg"

        File imsScript = createTemp(agenda, "ims");
        try {
            FileWriter imsWriter = new FileWriter(imsScript);

            imsWriter.write("#IMS\n");
            imsWriter.write(frameCount + "\n");
            imsWriter.write(framesPerSecond + "\n");

	    ArrayList<Path> fileSequencePaths = fileSequence.getPaths();

            for (Path fileSequencePath : fileSequencePaths) {
                imsWriter.write("\"" + fileSequencePath.toOsString() + "\"\n");
            }

            imsWriter.close();
        } catch(IOException ex) {
            throw new PipelineException("Unable to write temporary .ims file\n" + ex.getMessage());
        }

        // now that we have the ims file, create the .psc script file and load the ims file
        // using <importMovie> "/path/to/footage.ims
        // PSC TEMPLATE TO IMPORT MOVIE
        // #PFTrackScript v1.0
        // <importMovie> "/path/to/footage.ims"
        // <exit>
    
        File pscScript = createTemp(agenda, "psc");
        try {
            FileWriter pscWriter = new FileWriter(pscScript);

            pscWriter.write("#PFTrackScript v1.0\n");
            pscWriter.write("<importMovie> " +  "\"" + new Path(imsScript).toOsString() + "\"\n");
            pscWriter.write("<exit>\n");

            pscWriter.close();
        } catch(IOException ex) {
            throw new PipelineException("Unable to write temporary .psc file\n" + ex.getMessage());
        }

        // now that we have the psc file, create a python script that runs the psc file
        // pftrack temp.psc
        // and then return 

        File pythonScript = createTemp(agenda, "py");
        try {
            FileWriter pythonWriter = new FileWriter(pythonScript);

            pythonWriter.write("import shutil\n");
            pythonWriter.write(getPythonLaunchHeader());

            pythonWriter.write("launch('pftrack', '" + new Path(pscScript).toOsString() + "')");

            pythonWriter.close();

        } catch(IOException ex) {
            throw new PipelineException("Unable to write temporary Python script\n" + ex.getMessage());
        }
        
        return createPythonSubProcess(agenda, pythonScript, outFile, errFile); 
    }
}

    

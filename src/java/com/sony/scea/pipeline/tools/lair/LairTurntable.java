package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

public class LairTurntable
{
   LairTurntable(LairAsset as, String turntableName, String passName)
   {
      String assetName = as.assetName;
      String assetType = as.assetType.toString();
      turntableStart = "/projects/lr/production/tt/" + assetType + "/" + assetName + "/"
	    + turntableName;

      baseTurntableScene = PATH_baseTurntables + turntableName;
      baseTurntableOptions = PATH_baseTurntableOptions + passName;

      String namePrefix = assetName + "_" + turntableName + "_";

      turntableScene = turntableStart + "/lgt/" + namePrefix + "lgt";

      ttShadeMI = turntableStart + "/mi/" + assetName + "/" + passName + "/shd";
      ttLightMI = turntableStart + "/mi/com/" + passName + "/lgt";
      ttGeoMI = turntableStart + "/mi/" + assetName + "/com/mod";
      ttCamMI = turntableStart + "/mi/com/cam";
      ttCamOverMI = turntableStart + "/mi/com/" + passName + "/camOpt";
      ttOptionsMI = turntableStart + "/mi/com/" + passName + "/opt";
      ttImages = turntableStart + "/img/" + assetName + "/" + passName + "/" + namePrefix
	    + passName;

      ttShaderDefsMI = turntableStart + "/mi/" + assetName + "/" + passName
	    + "/shaders/mi_shader_defs";

   }

   // Base scene in assets
   public final String baseTurntableScene;
   public final String baseTurntableOptions;

   public final String turntableStart;

   // actual scene
   public final String turntableScene;

   // MiFiles
   public final String ttShadeMI;
   public final String ttLightMI;
   public final String ttGeoMI;
   public final String ttCamMI;
   public final String ttCamOverMI;
   public final String ttOptionsMI;

   // Extras
   public final String ttShaderDefsMI;

   // Final Images
   public final String ttImages;

}

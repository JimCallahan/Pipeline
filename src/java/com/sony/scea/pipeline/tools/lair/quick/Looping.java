package com.sony.scea.pipeline.tools.lair.quick;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.LinkVersion;
import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.NodeVersion;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.VersionID;

public class Looping {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MasterMgrClient mclient = new MasterMgrClient();
		try {
			TreeMap<VersionID, NodeVersion> hiRezV = mclient.getAllCheckedInVersions("hirez");
			TreeMap<VersionID, NodeVersion> loRezV = mclient.getAllCheckedInVersions("lowrez");
			
			//VersionID[] hiRezA = new VersionID[1]; 
			//hiRezA = hirezV.keySet().toArray(hiRezA);
			
//			VersionID[] loRezA = new VersionID[1];
//			loRezA = lowrezV.keySet().toArray(hiRezA);
			
			
//			Arrays.sort(hiRezA, Collections.reverseOrder());
//			Arrays.sort(loRezA, Collections.reverseOrder());
			
			TreeSet<VersionID> hiRezSorted = new TreeSet<VersionID>(Collections.reverseOrder());
			TreeSet<VersionID> loRezSorted = new TreeSet<VersionID>(Collections.reverseOrder());
			hiRezSorted.addAll(hiRezV.keySet());
			loRezSorted.addAll(loRezV.keySet());
			
			Iterator<VersionID> hiRezI = hiRezSorted.iterator();
			Iterator<VersionID> loRezI = loRezSorted.iterator();
			
			
			
			VersionID hiRezVersion = hiRezI.next();
			VersionID loRezVersion = loRezI.next();
			
			while (true)
			{
				LinkVersion hiLink = hiRezV.get(hiRezVersion).getSource("rig");
				VersionID hiRigVer = hiLink.getVersionID(); 
				
				LinkVersion loLink = loRezV.get(loRezVersion).getSource("rig");
				VersionID loRigVer = loLink.getVersionID();
				
				int compare = hiRigVer.compareTo(loRigVer);
				if (compare == 0)
					break;
				else if (compare < 0)
					if (loRezI.hasNext())
						loRezVersion = loRezI.next();
					else
						break;
				else
					if (hiRezI.hasNext())
						hiRezVersion = hiRezI.next();
					else
						break;
			}
			
			// if they're not equal, then there is no match
			
		} catch (PipelineException e) {
			e.printStackTrace();
		}


	}

}

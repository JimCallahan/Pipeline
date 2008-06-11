package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.text.DecimalFormat;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.ComplexUtilityParam;
import us.temerity.pipeline.IntegerUtilityParam;

public class IntegerListUtilityParam extends ComplexUtilityParam {

	private static final long serialVersionUID = -937791395924772004L;

	public IntegerListUtilityParam(String name, String desc, TreeMap<String, Integer> data) {
		super(name, desc);
		
		//DecimalFormat df = new DecimalFormat("0000");
		//int i = 0;
		
		for (String each : data.keySet())
		{
			IntegerUtilityParam ip = new IntegerUtilityParam(each, "Enter an integer value", data.get(each));
			addParam(ip);
			//i++;
		}
	}
	
	public IntegerListUtilityParam(String name, String desc, TreeSet<String> data, Integer defaultValue) {
		super(name, desc);
		
		//DecimalFormat df = new DecimalFormat("0000");
		//int i = 0;
		
		for (String each : data)
		{
			IntegerUtilityParam ip = new IntegerUtilityParam(each, "Enter an integer value", defaultValue);
			addParam(ip);
			//i++;
		}
	}

	@Override
	protected boolean needsUpdating() {
		// TODO Auto-generated method stub
		return false;
	}

}

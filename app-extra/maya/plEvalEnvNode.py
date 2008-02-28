# import maya
# maya.cmds.loadPlugin("plEvalEnvNode.py")
# maya.cmds.createNode("plEvalEnvNode")

import math, sys, os

import maya
import maya.OpenMaya as OpenMaya
import maya.OpenMayaMPx as OpenMayaMPx

kPluginNodeTypeName = "plEvalEnvNode"
kPluginNodeVendor   = "TemeritySoftware"
kPluginNodeVersion  = "1.0.0"

plEvalEnvNodeId = OpenMaya.MTypeId(0xffff0)  # FIX THIS!!!!

# Node definition
class plEvalEnvNode(OpenMayaMPx.MPxNode):
	# class variables
	input = OpenMaya.MObject()
	output = OpenMaya.MObject()
	def __init__(self):
		OpenMayaMPx.MPxNode.__init__(self)
	def compute(self,plug,dataBlock):
		if ( plug == plEvalEnvNode.output ):
			inputHandle = dataBlock.inputValue( plEvalEnvNode.input )

                        inType = inputHandle.type()
                        print('inputHandle.type = ' + str(inType))
                        if inType == 4:
                                inData = inputHandle.data()
                                inStringData = OpenMaya.MFnStringData(inData)

                                comps = inStringData.string().split('/')
                                ncomps = list()
                                for comp in comps:
                                        if comp.startswith('$'):
                                                try:
                                                        ncomps.append(os.environ[comp[1:]])
                                                except:
                                                        ncomps.append(comp)
                                        else:
                                                ncomps.append(comp)
                                expanded = '/'.join(ncomps)

                        
                        outputHandle = dataBlock.outputValue( plEvalEnvNode.output )

                        outType = outputHandle.type()
                        print('outputHandle.type = ' + str(outType))
                        if outType == 4:
                                outData = outputHandle.data()
                                outStringData = OpenMaya.MFnStringData(outData)
                                outStringData.set(expanded)

			dataBlock.setClean( plug )

		return OpenMaya.kUnknownParameter

# creator
def nodeCreator():
        node = plEvalEnvNode()
	return OpenMayaMPx.asMPxPtr( plEvalEnvNode() ) 

# initializer
def nodeInitializer():
	# input
	nAttr = OpenMaya.MFnTypedAttribute()
	plEvalEnvNode.input = nAttr.create( "input", "in", OpenMaya.MFnStringData.kString)
	nAttr.setStorable(1)
	nAttr.setWritable(1)
	# output
	nAttr = OpenMaya.MFnTypedAttribute()
        plEvalEnvNode.output = nAttr.create( "output", "out", OpenMaya.MFnStringData.kString)
	nAttr.setStorable(0)
        nAttr.setWritable(1)
	# add attributes
	plEvalEnvNode.addAttribute( plEvalEnvNode.input )
	plEvalEnvNode.addAttribute( plEvalEnvNode.output )
	plEvalEnvNode.attributeAffects( plEvalEnvNode.input, plEvalEnvNode.output )
        
	
# initialize the script plug-in
def initializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject, kPluginNodeVendor, kPluginNodeVersion)
	try:
		mplugin.registerNode( kPluginNodeTypeName, plEvalEnvNodeId, nodeCreator, nodeInitializer )
	except:
		sys.stderr.write( "Failed to register node: %s" % kPluginNodeTypeName )
		raise

# uninitialize the script plug-in
def uninitializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject, kPluginNodeVendor, kPluginNodeVersion)
	try:
		mplugin.deregisterNode( plEvalEnvNodeId )
	except:
		sys.stderr.write( "Failed to deregister node: %s" % kPluginNodeTypeName )
		raise
	

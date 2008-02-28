# import maya
# maya.cmds.loadPlugin("evalEnvNode.py")
# maya.cmds.createNode("spEvalEnvNode")

import math, sys, os

import maya.OpenMaya as OpenMaya
import maya.OpenMayaMPx as OpenMayaMPx

kPluginNodeTypeName = "spEvalEnvNode"

evalEnvNodeId = OpenMaya.MTypeId(0x87000)

# Node definition
class evalEnvNode(OpenMayaMPx.MPxNode):
	# class variables
	input = OpenMaya.MObject()
	output = OpenMaya.MObject()
	def __init__(self):
		OpenMayaMPx.MPxNode.__init__(self)
	def compute(self,plug,dataBlock):
		if ( plug == evalEnvNode.output ):
			inputHandle = dataBlock.inputValue( evalEnvNode.input )

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

                        
                        outputHandle = dataBlock.outputValue( evalEnvNode.output )

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
	return OpenMayaMPx.asMPxPtr( evalEnvNode() )

# initializer
def nodeInitializer():
	# input
	nAttr = OpenMaya.MFnTypedAttribute()
	evalEnvNode.input = nAttr.create( "input", "in", OpenMaya.MFnStringData.kString)
	nAttr.setStorable(1)
	# output
	nAttr = OpenMaya.MFnTypedAttribute()
        evalEnvNode.output = nAttr.create( "output", "out", OpenMaya.MFnStringData.kString)
	nAttr.setStorable(1)
	nAttr.setWritable(1)
	# add attributes
	evalEnvNode.addAttribute( evalEnvNode.input )
	evalEnvNode.addAttribute( evalEnvNode.output )
	evalEnvNode.attributeAffects( evalEnvNode.input, evalEnvNode.output )
	
# initialize the script plug-in
def initializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject)
	try:
		mplugin.registerNode( kPluginNodeTypeName, evalEnvNodeId, nodeCreator, nodeInitializer )
	except:
		sys.stderr.write( "Failed to register node: %s" % kPluginNodeTypeName )
		raise

# uninitialize the script plug-in
def uninitializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject)
	try:
		mplugin.deregisterNode( evalEnvNodeId )
	except:
		sys.stderr.write( "Failed to deregister node: %s" % kPluginNodeTypeName )
		raise
	

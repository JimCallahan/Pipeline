/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.*;
import org.apache.xmlrpc.parser.*;
import org.apache.xmlrpc.serializer.*;
import org.apache.xmlrpc.util.XmlRpcDateTimeDateFormat;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/** 
 * Default implementation of a type factory.<P> 
 * 
 * Modified by Jesse Clemens for Temerity Software from the original Apache sources to 
 * work with the non-standard responses that Shotgun provides.
 */
public class TemerityTypeFactory implements TypeFactory {
	private static final TypeSerializer NULL_SERIALIZER = new TemerityNullSerializer();
	private static final TypeSerializer STRING_SERIALIZER = new StringSerializer();
	private static final TypeSerializer I4_SERIALIZER = new I4Serializer();
	private static final TypeSerializer BOOLEAN_SERIALIZER = new BooleanSerializer();
	private static final TypeSerializer DOUBLE_SERIALIZER = new DoubleSerializer();
	private static final TypeSerializer BYTE_SERIALIZER = new I1Serializer();
	private static final TypeSerializer SHORT_SERIALIZER = new I2Serializer();
	private static final TypeSerializer LONG_SERIALIZER = new I8Serializer();
	private static final TypeSerializer FLOAT_SERIALIZER = new FloatSerializer();
	private static final TypeSerializer NODE_SERIALIZER = new NodeSerializer();
    private static final TypeSerializer SERIALIZABLE_SERIALIZER = new SerializableSerializer();
    private static final TypeSerializer BIGDECIMAL_SERIALIZER = new BigDecimalSerializer();
    private static final TypeSerializer BIGINTEGER_SERIALIZER = new BigIntegerSerializer();
    private static final TypeSerializer CALENDAR_SERIALIZER = new CalendarSerializer();

	private final XmlRpcController controller;
    private DateSerializer dateSerializer;

	/** Creates a new instance.
	 * @param pController The controller, which operates the type factory.
	 */
	public TemerityTypeFactory(XmlRpcController pController) {
		controller = pController;
	}

	/** Returns the controller, which operates the type factory.
	 * @return The controller, an instance of
	 * {@link org.apache.xmlrpc.client.XmlRpcClient},
	 * or {@link org.apache.xmlrpc.server.XmlRpcServer}.
	 */
	public XmlRpcController getController() {
		return controller;
	}

	public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
		if (pObject == null) {
			if (pConfig.isEnabledForExtensions()) {
				return NULL_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Null values aren't supported, if isEnabledForExtensions() == false"));
			}
		} else if (pObject instanceof String) {
			return STRING_SERIALIZER;
		} else if (pObject instanceof Byte) {
			if (pConfig.isEnabledForExtensions()) {
				return BYTE_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Byte values aren't supported, if isEnabledForExtensions() == false"));
			}
		} else if (pObject instanceof Short) {
			if (pConfig.isEnabledForExtensions()) {
				return SHORT_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Short values aren't supported, if isEnabledForExtensions() == false"));
			}
		} else if (pObject instanceof Integer) {
			return I4_SERIALIZER;
		} else if (pObject instanceof Long) {
			if (pConfig.isEnabledForExtensions()) {
				return LONG_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Long values aren't supported, if isEnabledForExtensions() == false"));
			}
		} else if (pObject instanceof Boolean) {
			return BOOLEAN_SERIALIZER;
		} else if (pObject instanceof Float) {
			if (pConfig.isEnabledForExtensions()) {
				return FLOAT_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Float values aren't supported, if isEnabledForExtensions() == false"));
			}
		} else if (pObject instanceof Double) {
			return DOUBLE_SERIALIZER;
		} else if (pObject instanceof Calendar) {
            if (pConfig.isEnabledForExtensions()) {
                return CALENDAR_SERIALIZER;
            } else {
                throw new SAXException(new XmlRpcExtensionException("Calendar values aren't supported, if isEnabledForExtensions() == false"));
            }
        } else if (pObject instanceof Date) {
            if (dateSerializer == null) {
                dateSerializer = new DateSerializer(new XmlRpcDateTimeDateFormat(){
                    private static final long serialVersionUID = 24345909123324234L;
                    protected TimeZone getTimeZone() {
                        return controller.getConfig().getTimeZone();
                    }
                });
            }
            return dateSerializer;
    	} else if (pObject instanceof byte[]) {
			return new ByteArraySerializer();
		} else if (pObject instanceof Object[]) {
			return new ObjectArraySerializer(this, pConfig);
		} else if (pObject instanceof List) {
			return new ListSerializer(this, pConfig);
		} else if (pObject instanceof Map) {
			return new MapSerializer(this, pConfig);
		} else if (pObject instanceof Node) {
			if (pConfig.isEnabledForExtensions()) {
				return NODE_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("DOM nodes aren't supported, if isEnabledForExtensions() == false"));
			}
        } else if (pObject instanceof BigInteger) {
            if (pConfig.isEnabledForExtensions()) {
                return BIGINTEGER_SERIALIZER;
            } else {
                throw new SAXException(new XmlRpcExtensionException("BigInteger values aren't supported, if isEnabledForExtensions() == false"));
            }
        } else if (pObject instanceof BigDecimal) {
            if (pConfig.isEnabledForExtensions()) {
                return BIGDECIMAL_SERIALIZER;
            } else {
                throw new SAXException(new XmlRpcExtensionException("BigDecimal values aren't supported, if isEnabledForExtensions() == false"));
            }
		} else if (pObject instanceof Serializable) {
			if (pConfig.isEnabledForExtensions()) {
				return SERIALIZABLE_SERIALIZER;
			} else {
				throw new SAXException(new XmlRpcExtensionException("Serializable objects aren't supported, if isEnabledForExtensions() == false"));
			}
		} else {
			return null;
		}
	}

	public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName) {
		if (XmlRpcWriter.EXTENSIONS_URI.equals(pURI)) {
			if (!pConfig.isEnabledForExtensions()) {
				return null;
			}
			if (TemerityNullSerializer.NIL_TAG.equals(pLocalName)) {
				return new NullParser();
			} else if (I1Serializer.I1_TAG.equals(pLocalName)) {
				return new I1Parser();
			} else if (I2Serializer.I2_TAG.equals(pLocalName)) {
				return new I2Parser();
			} else if (I8Serializer.I8_TAG.equals(pLocalName)) {
				return new I8Parser();
			} else if (FloatSerializer.FLOAT_TAG.equals(pLocalName)) {
				return new FloatParser();
            } else if (NodeSerializer.DOM_TAG.equals(pLocalName)) {
                return new NodeParser();
            } else if (BigDecimalSerializer.BIGDECIMAL_TAG.equals(pLocalName)) {
                return new BigDecimalParser();
            } else if (BigIntegerSerializer.BIGINTEGER_TAG.equals(pLocalName)) {
                return new BigIntegerParser();
			} else if (SerializableSerializer.SERIALIZABLE_TAG.equals(pLocalName)) {
				return new SerializableParser();
			} else if (CalendarSerializer.CALENDAR_TAG.equals(pLocalName)) {
			    return new CalendarParser();
            }
		} else if ("".equals(pURI)) {
			if (I4Serializer.INT_TAG.equals(pLocalName)  ||  I4Serializer.I4_TAG.equals(pLocalName)) {
				return new I4Parser();
			} else if (BooleanSerializer.BOOLEAN_TAG.equals(pLocalName)) {
				return new BooleanParser();
			} else if (DoubleSerializer.DOUBLE_TAG.equals(pLocalName)) {
				return new DoubleParser();
			} else if (DateSerializer.DATE_TAG.equals(pLocalName)) {
				return new DateParser(new XmlRpcDateTimeDateFormat(){
                    private static final long serialVersionUID = 7585237706442299067L;
                    protected TimeZone getTimeZone() {
                        return controller.getConfig().getTimeZone();
                    }
                });
			} else if (ObjectArraySerializer.ARRAY_TAG.equals(pLocalName)) {
				return new ObjectArrayParser(pConfig, pContext, this);
			} else if (MapSerializer.STRUCT_TAG.equals(pLocalName)) {
				return new MapParser(pConfig, pContext, this);
			} else if (ByteArraySerializer.BASE_64_TAG.equals(pLocalName)) {
				return new ByteArrayParser();
			} else if (StringSerializer.STRING_TAG.equals(pLocalName)) {
				return new StringParser();
			} else if (TemerityNullSerializer.NIL_TAG.equals(pLocalName)) {
				return new NullParser();
			}
			
		}
		return null;
	}
}

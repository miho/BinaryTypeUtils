/*
 * Copyright 2019-2021 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */
package eu.mihosoft.binarytypeutils;

import eu.mihosoft.streamutils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializes and deserializes Java number objects to and from specified binary representation
 * (binary-compatible with C/C++ on many devices, can be used for communicating with MCUs and Boards such as Arduinos etc.).
 */
class BinarySerializerImpl implements BinarySerializer, BinaryDeserializer {

    private final static String TAG = "eu.mihosoft.binarytypeutils:serializer";
    private final StreamUtils.ReadingInputStream inputStream;
    private final OutputStream outputStream;
    private final ByteOrder byteOrder;

    private final Map<BinaryType, BinaryConverter> converterByBinaryTypeName = new HashMap<>();

    /**
     * Creates a new serializer instance.
     * @param inputStream input stream to be used by this communication object
     * @param outputStream output stream to be used by this communication object
     * @param byteOrder byte order to be used
     */
    private BinarySerializerImpl(InputStream inputStream, OutputStream outputStream, ByteOrder byteOrder) {

        org.tinylog.Logger.tag(TAG).trace("creating BinaryCommunication with  byte-order:='{}'.",
                byteOrder);

        this.outputStream = outputStream;
        this.inputStream = new StreamUtils.ReadingInputStream(inputStream);
        this.byteOrder = byteOrder;

        init();
    }

    /**
     * Creates a new serialization instance.
     *
     * @param inputStream input stream to use for reading data
     * @param outputStream output stream to use for writing data
     * @param byteOrder byte order for value conversion
     * @return new instance
     */
    /*pkg private*/ static BinarySerializerImpl newInstance(InputStream inputStream, OutputStream outputStream, ByteOrder byteOrder) {
        return new BinarySerializerImpl(inputStream, outputStream, byteOrder);
    }

    @Override
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Initializes this communication object (creates converter instances).
     */
    private void init() {
        converterByBinaryTypeName.put(BinaryType.BYTE,  new BinaryConverter.SByteConverter());
        converterByBinaryTypeName.put(BinaryType.INT8,  new BinaryConverter.SByteConverter());
        converterByBinaryTypeName.put(BinaryType.UBYTE, new BinaryConverter.UByteConverter());
        converterByBinaryTypeName.put(BinaryType.UINT8, new BinaryConverter.UByteConverter());
        converterByBinaryTypeName.put(BinaryType.INT64, new BinaryConverter.SINT8Converter());
        converterByBinaryTypeName.put(BinaryType.INT32, new BinaryConverter.SINT4Converter());
        converterByBinaryTypeName.put(BinaryType.UINT32, new BinaryConverter.UINT4Converter());
        converterByBinaryTypeName.put(BinaryType.INT16, new BinaryConverter.SINT2Converter());
        converterByBinaryTypeName.put(BinaryType.UINT16, new BinaryConverter.UINT2Converter());
        converterByBinaryTypeName.put(BinaryType.FLOAT32, new BinaryConverter.SFloat4Converter());
        converterByBinaryTypeName.put(BinaryType.FLOAT64, new BinaryConverter.SFloat8Converter());
    }


    @Override
    public byte[] getReadBytes() {
        return this.inputStream.getReadBytes();
    }

    @Override
    public void clearReadBuffer() {
        this.inputStream.clearBuffer();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public Object readValue(BinaryType binaryType) {

        org.tinylog.Logger.tag(TAG).trace("receiving value binaryType='{}'.", binaryType);

        BinaryConverter converter = converterByBinaryTypeName.get(binaryType);

        if(converter==null) {
            RuntimeException ex = new RuntimeException("No converter found for binary type: " + binaryType + ".");
            throw ex;
        }

        try {
            Object o = converter.read(inputStream, byteOrder).getObject();
            return o;
        } catch (IOException e) {
            RuntimeException ex = new RuntimeException("Cannot receive value of type " + binaryType+".", e);
            throw ex;
        }
    }

    @Override
    public void writeValue(BinaryType binaryType, Object value) {

        org.tinylog.Logger.tag(TAG).trace("sending value binaryType='{}'.", binaryType);

        BinaryConverter converter = converterByBinaryTypeName.get(binaryType);

        if(converter==null) {
            RuntimeException ex = new RuntimeException("No converter found for binary type: " + binaryType + ".");
            throw ex;
        }

        try {
            converter.write(value, outputStream, byteOrder);
        } catch (IOException e) {
            RuntimeException ex = new RuntimeException("Cannot write value of type " + binaryType+".", e);
            throw ex;
        }
    }
}

/*
 * Copyright 2019-2022 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Deserializes Java number objects to and from specified binary representation
 * (binary-compatible with C/C++ on many devices, can be used for communicating with MCUs and Boards such as Arduinos etc.).
 */
public interface BinaryDeserializer {

    /**
     * Returns the byte order used by this serializer.
     * @return byte order
     */
    ByteOrder getByteOrder();

    /**
     * Returns the bytes that have been read by this stream after instantiation/clearBuffer() calls.
     * @return the bytes that have been read by this stream
     */
    byte[] getReadBytes();

    /**
     * Clears the internal byte buffer.
     */
    public void clearReadBuffer();

    /**
     * Returns the input stream used by this serializer.
     * @return input stream
     */
    public InputStream getInputStream();

    /**
     * Reads a value from the associated input stream.
     * @param binaryType binary type of the object to deserialize
     * @return the deserialized object
     * @throws RuntimeException if the requested value cannot be read from the specified stream
     */
    Object readValue(BinaryType binaryType);

    /**
     * Creates a new deserializer instance.
     *
     * @param inputStream input stream to use for reading data
     * @param byteOrder byte order for value conversion
     * @return new instance
     */
    static BinaryDeserializer newInstance(InputStream inputStream, ByteOrder byteOrder) {
        return BinarySerializerImpl.newInstance(inputStream, null, byteOrder);
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*pkg private*/interface BinaryConverter {

    int write(Object o, OutputStream outStream, ByteOrder byteOrder) throws IOException;

    ConversionResult read(InputStream inStream, ByteOrder byteOrder) throws IOException;

    static final class ConversionResult {
        private final Object o;
        private byte[] data;

        private ConversionResult(Object o, byte[] data) {
            this.o = o;
            this.data = data;
        }

        static ConversionResult newInstance(Object o, byte[] data) {
            return new ConversionResult(o, data);
        }

        public byte[] getBinary() {
            return data;
        }

        public Object getObject() {
            return o;
        }
    }

    // see http://www.darksleep.com/player/JavaAndUnsignedTypes.html for details on signed vs unsigned

    static final double MAX_SFLOAT8 = Double.MAX_VALUE;
    static final double MIN_SFLOAT8 = -Double.MAX_VALUE;

    static final double MAX_SFLOAT4 = Float.MAX_VALUE;
    static final double MIN_SFLOAT4 = -Float.MAX_VALUE;

    static final long MAX_SINT8 = Long.MAX_VALUE;
    static final long MIN_SINT8 = Long.MIN_VALUE;

    static final long MAX_SINT4 = Integer.MAX_VALUE;
    static final long MIN_SINT4 = Integer.MIN_VALUE;
    static final long MAX_UINT4 = Integer.MAX_VALUE*2L+1L; // (+1 because we get the sign bit)
    static final long MIN_UINT4 = 0;

    static final long MAX_SINT2 = Short.MAX_VALUE;
    static final long MIN_SINT2 = Short.MIN_VALUE;
    static final long MAX_UINT2 = Short.MAX_VALUE*2+1; // (+1 because we get the sign bit)
    static final long MIN_UINT2 = 0;

    static final long MAX_SBYTE = Byte.MAX_VALUE;
    static final long MIN_SBYTE = Byte.MIN_VALUE;
    static final long MAX_UBYTE = Byte.MAX_VALUE*2+1; // (+1 because we get the sign bit)
    static final long MIN_UBYTE = 0;


    static long toPrimitive(Object o, String msgTypeName, long minValue, long maxValue) {

        try {
            long value = ((Number)o).longValue();

            if(value < minValue || value > maxValue) {
                RuntimeException ex = new RuntimeException(
                        "Value out of range: got 'v = "+value+"', expected 'v >= " + minValue + "&& v <= "+maxValue+"'"
                );
                throw ex;
            }

            return value;
        } catch(Exception ex) {
            RuntimeException exP = new RuntimeException(
                    "Invalid value specified. Got '"
                            + (o==null?"null":o.getClass().getName())
                            + "', expected '" + msgTypeName + "'", ex
            );
            throw exP;
        }
    }

    static double toPrimitive(Object o, String msgTypeName, double minValue, double maxValue) {
        try {
            double value = ((Number)o).doubleValue();

            if(value < minValue || value > maxValue) {
                RuntimeException ex = new RuntimeException(
                        "Value out of range: got 'v = "+value+"', expected 'v >= " + minValue + "&& v <= "+maxValue+"'"
                );
                throw ex;
            }

            return value;
        } catch(Exception ex) {
            RuntimeException exP = new RuntimeException(
                    "Invalid value specified. Got '"
                            + (o==null?"null":o.getClass().getName())
                            + "', expected '" + msgTypeName + "'",ex
            );
            throw exP;
        }
    }

    static boolean toBoolean(Object o, String msgTypeName) {
        try {
            return (boolean) o;
        } catch(Exception ex) {
            RuntimeException exP = new RuntimeException(
                    "Invalid value specified. Got '"
                            + (o==null?"null":o.getClass().getName())
                            + "', expected '" + msgTypeName + "'"
            );
            throw exP;
        }
    }

    static byte[] readBytes(InputStream inStream, byte[] data) throws IOException {
        int readBytes = inStream.readNBytes(data, 0, data.length);

        if(readBytes!=data.length) {
            IOException ex = new IOException(
                    "Reading value incomplete: expected "
                            + data.length
                            + " number of bytes, got "
                            + readBytes + ""
            );
            throw ex;
        }

        return data;
    }

    static byte[] readBytes(int numBytesToRead, InputStream inStream) throws IOException {

        byte[] data = new byte[numBytesToRead];

        int readBytes = inStream.readNBytes(data, 0, data.length);

        if(readBytes!=data.length) {
            IOException ex = new IOException(
                    "Reading value incomplete: expected "
                            + data.length
                            + " number of bytes, got "
                            + readBytes + ""
            );
            throw ex;
        }

        return data;
    }

    static class SFloat8Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {

            byte[] data = ByteBuffer.allocate(8).order(byteOrder).putDouble(
                    (double) BinaryConverter.toPrimitive(o,
                            "signed float (8byte)", MIN_SFLOAT8, MAX_SFLOAT8
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 8;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[8]);
            Double value = ByteBuffer.wrap(data).order(byteOrder).getDouble();
            return ConversionResult.newInstance(value, data);
        }
    }

    static class SFloat4Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(4).order(byteOrder).putFloat(
                    (float) BinaryConverter.toPrimitive(o,
                            "signed float (4byte)", MIN_SFLOAT4, MAX_SFLOAT4
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 4;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[4]);
            Float value = ByteBuffer.wrap(data).order(byteOrder).getFloat();
            return ConversionResult.newInstance(value, data);
        }
    }

    static class SINT8Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(8).order(byteOrder).putLong(
                    (long) BinaryConverter.toPrimitive(o,
                            "signed int (8byte)", MIN_SINT8, MAX_SINT8
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 8;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[8]);
            Long value = ByteBuffer.wrap(data).order(byteOrder).getLong();
            return ConversionResult.newInstance(value, data);
        }
    }

    static class UINT4Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(4).order(byteOrder).putInt(
                    (int) BinaryConverter.toPrimitive(o,
                            "unsigned int (4byte)", MIN_UINT4, MAX_UINT4
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 4;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[4]);
            long value = Integer.toUnsignedLong(ByteBuffer.wrap(data).order(byteOrder).getInt());
            return ConversionResult.newInstance(value, data);
        }
    }

    static class SINT4Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(4).order(byteOrder).putInt(
                    (int) BinaryConverter.toPrimitive(o,
                            "signed int (4byte)", MIN_SINT4, MAX_SINT4
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 4;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[4]);
            Integer value = ByteBuffer.wrap(data).order(byteOrder).getInt();
            return ConversionResult.newInstance(value, data);
        }
    }

    static class UINT2Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(2).order(byteOrder).putShort(
                    (short) BinaryConverter.toPrimitive(o,
                            "unsigned int (2byte)", MIN_UINT2, MAX_UINT2
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 2;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[2]);
            Integer value = Short.toUnsignedInt(ByteBuffer.wrap(data).order(byteOrder).getShort());
            return ConversionResult.newInstance(value, data);
        }
    }

    static class SINT2Converter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(2).order(byteOrder).putShort(
                    (short) BinaryConverter.toPrimitive(o,
                            "signed int (2byte)", MIN_SINT2, MAX_SINT2
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 2;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[2]);
            Short value = ByteBuffer.wrap(data).order(byteOrder).getShort();
            return ConversionResult.newInstance(value, data);
        }
    }

    static class SByteConverter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = {(byte) BinaryConverter.toPrimitive(o,
                    "signed byte (1byte)", MIN_SBYTE, MAX_SBYTE
            )};
            outputStream.write(data);

            return 1;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream,new byte[1]);
            return ConversionResult.newInstance(data[0], data);
        }
    }

    static class UByteConverter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = ByteBuffer.allocate(1).order(byteOrder).put(
                    (byte) BinaryConverter.toPrimitive(o,
                            "unsigned byte (1byte)", MIN_UBYTE, MAX_UBYTE
                    )
            ).array();

            outputStream.write(data);
            outputStream.flush();

            return 1;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream, new byte[1]);
            Short value = (short)Byte.toUnsignedInt(data[0]);
            return ConversionResult.newInstance(value, data);
        }
    }

    static class BOOLConverter implements BinaryConverter {
        @Override
        public int write(Object o, OutputStream outputStream, ByteOrder byteOrder) throws IOException {
            byte value = (byte)(BinaryConverter.toBoolean(o,"boolean (1byte)")?1:0);
            outputStream.write(new byte[]{value});
            outputStream.flush();
            return 1;
        }

        @Override
        public ConversionResult read(InputStream inputStream, ByteOrder byteOrder) throws IOException {
            byte[] data = BinaryConverter.readBytes(inputStream, new byte[1]);
            Boolean value = data[0]!=0;
            return ConversionResult.newInstance(value, data);
        }
    }
}
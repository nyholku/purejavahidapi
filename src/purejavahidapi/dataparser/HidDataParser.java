/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * Copyright (c) 2018, Nicholas Saney / Chairosoft
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notices, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notices, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its
 * contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package purejavahidapi.dataparser;

/**
 * @author nsaney
 */
public class HidDataParser {
	/**
	 * Extracts data from the given buffer using bit (NOT byte)
	 * offset and length values.
	 *
	 * This method considers the least significant bit of a byte
	 * to be the "first" bit in that byte. Conversely, the most
	 * significant bit is treated as the "last" bit of a byte.
	 *
	 * @param buffer the buffer from which to extract data
	 * @param bitOffset the offset in bits (NOT bytes) from which to start extraction
	 * @param bitLength the length in bits (NOT bytes) to extract
	 * @return the extracted data, shifted if necessary from the original buffer
	 */
	public static byte[] extractDataAtBitOffset(byte[] buffer, int bitOffset, int bitLength) {
		int courseByteOffset = bitOffset / Byte.SIZE;
		int fineByteOffsetAdjustment = bitOffset % Byte.SIZE;
		int fineByteOffsetReverseAdjustment = Byte.SIZE - fineByteOffsetAdjustment;
		int courseByteLength = bitLength / Byte.SIZE;
		int fineByteLengthAdjustment = bitLength % Byte.SIZE;
		int extractedByteLength = courseByteLength + (fineByteLengthAdjustment == 0 ? 0 : 1);
		byte[] extractedBytes = new byte[extractedByteLength];
		int maskLow = (1 << fineByteOffsetAdjustment) - 1;
		int maskHigh = ((1 << Byte.SIZE) - 1) - maskLow;
		for (int i = 0, off = courseByteOffset; i < extractedByteLength; ++i, ++off) {
			byte result = 0;
			byte bCurrent = buffer[off];
			byte lo = (byte)((bCurrent & maskHigh) >>> fineByteOffsetAdjustment);
			result |= lo;
			if (maskLow != 0) {
				byte bNext = buffer[off + 1];
				byte hi = (byte)((bNext * maskLow) << fineByteOffsetReverseAdjustment);
				result |= hi;
			}
			extractedBytes[i] = result;
		}
		return extractedBytes;
	}
 
	// adapted from: https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
	public static long bytesToLong(byte[] buffer, int offset) {
		int max = offset + Long.BYTES;
		if (max > buffer.length) { max = buffer.length; }
		long result = 0;
		for (int i = offset; i < max; i++) {
			result <<= Byte.SIZE;
			result |= (buffer[i] & 0xff);
		}
		return result;
	}
}

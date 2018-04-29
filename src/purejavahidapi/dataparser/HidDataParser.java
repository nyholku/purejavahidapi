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

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author nsaney
 */
public class HidDataParser {
	/**
	 * On systems that support reading of report descriptors
	 * (currently only Windows, as of v0.0.11), this method can
	 * be used in implementations of this class for parsing report
	 * data from the descriptors that match the given report ID.
	 *
	 * If the calling system does not support reading of report
	 * descriptors, then this method will return null.
	 */
	public static ParsedReportDataItem[] getParsedReportData(
		HidDevice source,
		final byte reportID,
		byte[] reportData,
		int reportLength
	) {
		if (source == null) { return null; }
		HidDeviceInfo deviceInfo = source.getHidDeviceInfo();
		if (deviceInfo == null) { return null; }
		Capability[] deviceCapabilities = deviceInfo.getCapabilities();
		if (deviceCapabilities == null) { return null; }
		Capability[] reportCapabilities = Stream.of(deviceCapabilities)
			.filter(cap -> cap.getType() == Capability.Type.INPUT && cap.getReportId() == reportID)
			.sorted(Comparator.comparingInt(Capability::getDataIndexMin))
			.toArray(Capability[]::new);
		ParsedReportDataItem[] results = new ParsedReportDataItem[reportCapabilities.length];
		if (reportData.length != reportLength) { reportData = Arrays.copyOf(reportData, reportLength); }
		for (int i = 0; i < reportCapabilities.length; ++i) {
			Capability cap = reportCapabilities[i];
			ParsedReportDataItem parsedReportDataItem;
			int reportBitOffset = cap.getReportBitOffset();
			int reportBitLength = cap.getReportBitLength();
			byte[] extractedData = extractDataAtBitOffset(reportData, reportBitOffset, reportBitLength);
			if (cap instanceof Capability.ButtonRange) {
				Capability.ButtonRange buttonRange = (Capability.ButtonRange)cap;
				boolean[] parsedButtonRange = new boolean[reportBitLength];
				int p = 0;
				for (byte b : extractedData) {
					for (int mask = 1; mask < 0x100 && p < reportBitLength; mask <<= 1, ++p) {
						parsedButtonRange[p] = (b & mask) != 0;
					}
				}
				parsedReportDataItem = new ParsedReportDataItem(buttonRange, parsedButtonRange);
			}
			else if (cap instanceof Capability.Value) {
				Capability.Value value = (Capability.Value)cap;
				long[] parsedValues = new long[value.getReportCount()];
				int p = 0;
				for (int offset = 0; offset < extractedData.length; offset += Long.BYTES, ++p) {
					parsedValues[p] = bytesToLong(extractedData, offset);
				}
				parsedReportDataItem = new ParsedReportDataItem(value, parsedValues);
			}
			else {
				parsedReportDataItem = new ParsedReportDataItem(cap);
			}
			results[i] = parsedReportDataItem;
		}
		return results;
	}
	
	/**
	 * Extracts data from the given buffer using bit (NOT byte)
	 * offset and length values.
	 *
	 * This method considers the least significant bit of a byte
	 * to be the "first" bit in that byte. Conversely, the most
	 * significant bit is treated as the "last" bit of a byte.
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

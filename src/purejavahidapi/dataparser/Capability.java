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
public abstract class Capability {
	Type m_Type;
	byte m_ReportId;
	short m_LinkCollection;
	int m_ReportBitOffset;
	int m_ReportBitLength;
	short m_ReportByteLengthWithPadding;
	int m_DataIndexMin;
	int m_DataIndexMax;
	short m_UsagePage;
	
	protected Capability(
		Type type,
		byte reportId,
		short linkCollection,
		int reportBitOffset,
		int reportBitLength,
		int dataIndexMin,
		int dataIndexMax,
		short usagePage
	) {
		m_Type = type;
		m_ReportId = reportId;
		m_LinkCollection = linkCollection;
		m_ReportBitOffset = reportBitOffset;
		m_ReportBitLength = reportBitLength;
		m_ReportByteLengthWithPadding = (short)((m_ReportBitLength / Byte.SIZE) + ((m_ReportBitLength % Byte.SIZE == 0) ? 0 : 1));
		m_DataIndexMin = dataIndexMin;
		m_DataIndexMax = dataIndexMax;
		m_UsagePage = usagePage;
	}
	
	public Type getType() { return m_Type; }
	public byte getReportId() { return  m_ReportId; }
	public short getLinkCollection() { return m_LinkCollection; }
	public int getReportBitOffset() { return m_ReportBitOffset; }
	public int getReportBitLength() { return m_ReportBitLength; }
	public short getReportByteLengthWithPadding() { return m_ReportByteLengthWithPadding; }
	public int getDataIndexMin() { return m_DataIndexMin; }
	public int getDataIndexMax() { return m_DataIndexMax; }
	public short getUsagePage() { return m_UsagePage; }
	
	public enum Type { INPUT, OUTPUT, FEATURE }
	
	public static class ButtonRange extends Capability {
		final short m_UsageMin;
		final short m_UsageMax;
		
		public ButtonRange(
			Type type,
			byte reportId,
			short linkCollection,
			int reportBitOffset,
			int dataIndexMin,
			int dataIndexMax,
			short usagePage,
			short usageMin,
			short usageMax
		) {
			super(type, reportId, linkCollection, reportBitOffset, usageMax - usageMin + 1, dataIndexMin, dataIndexMax, usagePage);
			m_UsageMin = usageMin;
			m_UsageMax = usageMax;
		}
		
		public short getUsageMin() { return m_UsageMin; }
		public short getUsageMax() { return m_UsageMax; }
	}
	
	public static class Value extends Capability {
		short m_Usage;
		long m_LogicalMin;
		long m_LogicalMax;
		int m_BitSize;
		int m_ReportCount;
		
		public Value(
			Type type,
			byte reportId,
			short linkCollection,
			int reportBitOffset,
			int dataIndexMin,
			int dataIndexMax,
			short usagePage,
			short usage,
			long logicalMin,
			long logicalMax,
			int bitSize,
			int reportCount
		) {
			super(type, reportId, linkCollection, reportBitOffset, bitSize * reportCount, dataIndexMin, dataIndexMax, usagePage);
			m_Usage = usage;
			m_LogicalMin = logicalMin;
			m_LogicalMax = logicalMax;
			m_BitSize = bitSize;
			m_ReportCount = reportCount;
		}
		
		public short getUsage() { return m_Usage; }
		public long getLogicalMin() { return m_LogicalMin; }
		public long getLogicalMax() { return m_LogicalMax; }
		public int getBitSize() { return m_BitSize; }
		public int getReportCount() { return m_ReportCount; }
	}
}

/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
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
package purejavahidapi.hidparser;


public final class Field {
	Report m_Report;
	Collection m_Collection;
	int m_Physical;
	int m_Logical;
	int m_Application;
	int m_Usage;
	int m_Flags;
	int m_ReportOffset;
	int m_ReportSize;
	int m_ReportType;
	int m_LogicalMinimum;
	int m_LogicalMaximum;
	int m_PhysicalMinimum;
	int m_PhysicalMaximum;
	int m_UnitExponent;
	int m_Unit;

	Field(Collection collection) {
		m_Collection = collection;
		m_Collection.add(this);
	}

	//	int m_Index;
	public void dump(String tab) {
		HidParser.printf(tab + "-FIELD-------------------------\n");
		HidParser.printf(tab + "        usage: 0x%04X:0x%04X\n", (0xFFFF & m_Usage >> 16) & 0xFFFF, m_Usage & 0xFFFF);
		HidParser.printf(tab + "        flags: 0x%08X\n", m_Flags);
		HidParser.printf(tab + "    report id: 0x%02X\n", m_Report.m_Id);
		HidParser.printf(tab + "         type: %s\n", new String[] { "input", "output", "feature" }[m_Report.m_Type]);
		HidParser.printf(tab + "       offset: %d\n", m_ReportOffset);
		HidParser.printf(tab + "         size: %d\n", m_ReportSize);
		HidParser.printf(tab + "  logical min: %d\n", m_LogicalMinimum);
		HidParser.printf(tab + "  logical max: %d\n", m_LogicalMaximum);
		HidParser.printf(tab + " physical min: %d\n", m_PhysicalMinimum);
		HidParser.printf(tab + " physical max: %d\n", m_PhysicalMaximum);
		HidParser.printf(tab + "         unit: %d\n", m_Unit);
		HidParser.printf(tab + "     unit exp: %d\n", m_UnitExponent);
	}
}
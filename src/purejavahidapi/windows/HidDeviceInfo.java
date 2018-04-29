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
package purejavahidapi.windows;

import static purejavahidapi.windows.HidLibrary.*;
import static purejavahidapi.windows.SetupApiLibrary.HIDP_STATUS_SUCCESS;

import com.sun.jna.Memory;
import purejavahidapi.dataparser.Capability;
import purejavahidapi.windows.WinDef.HANDLE;

import java.util.*;

/* package*/class HidDeviceInfo extends purejavahidapi.HidDeviceInfo {

	public HidDeviceInfo(String path, String deviceId, HANDLE handle, HIDD_ATTRIBUTES attrib) {
		try {
			m_Path = path;
			m_DeviceId = deviceId;
			m_VendorId = attrib.VendorID;
			m_ProductId = attrib.ProductID;

			HIDP_CAPS caps = new HIDP_CAPS();

			// Get the report descriptors for this device.
			HIDP_PREPARSED_DATA[] ppd = new HIDP_PREPARSED_DATA[1];
			if (HidD_GetPreparsedData(handle, ppd)) {
				if (HidP_GetCaps(ppd[0], caps) == HIDP_STATUS_SUCCESS) {
					m_UsagePage = caps.UsagePage;
					m_UsageId = caps.Usage;
					
					HIDP_REPORT_TYPE[] reportTypes = HIDP_REPORT_TYPE.values();
					Map<HIDP_REPORT_TYPE, ArrayList<HidPDataIndexedCaps>> dataIndexedCapNodesByReportType = new HashMap<>();
					for (HIDP_REPORT_TYPE reportType : reportTypes) {
						ArrayList<HidPDataIndexedCaps> dataIndexedCapNodes = new ArrayList<>();
						short numberButtonCapNodes = reportType.getNumberButtonCaps(caps);
						if (numberButtonCapNodes > 0) {
							HIDP_BUTTON_CAPS[] buttonCapNodes = new HIDP_BUTTON_CAPS[numberButtonCapNodes];
							short[] buttonCapNodesLength = {numberButtonCapNodes};
							HidP_GetButtonCaps(reportType, buttonCapNodes, buttonCapNodesLength, ppd[0]);
							Collections.addAll(dataIndexedCapNodes, buttonCapNodes);
						}
						short numberValueCapNodes = reportType.getNumberValueCaps(caps);
						if (numberValueCapNodes > 0) {
							HIDP_VALUE_CAPS[] valueCapNodes = new HIDP_VALUE_CAPS[numberValueCapNodes];
							short[] valueCapNodesLength = {numberValueCapNodes};
							HidP_GetValueCaps(reportType, valueCapNodes, valueCapNodesLength, ppd[0]);
							Collections.addAll(dataIndexedCapNodes, valueCapNodes);
						}
						dataIndexedCapNodesByReportType.put(reportType, dataIndexedCapNodes);
					}
					
					ArrayList<Capability> capabilities = new ArrayList<>();
					for (HIDP_REPORT_TYPE reportType : reportTypes) {
						Map<Byte, Integer> reportBitOffsetByReportId = new HashMap<>();
						Capability.Type capabilityType = null;
						switch (reportType) {
							case HidP_Input: capabilityType = Capability.Type.INPUT; break;
							case HidP_Output: capabilityType = Capability.Type.OUTPUT; break;
							case HidP_Feature: capabilityType = Capability.Type.FEATURE; break;
						}
						ArrayList<HidPDataIndexedCaps> dataIndexedCapNodes = dataIndexedCapNodesByReportType.get(reportType);
						dataIndexedCapNodes.sort(Comparator.comparingInt(HidPDataIndexedCaps::getDataIndexMin));
						for (HidPDataIndexedCaps indexedCapNode : dataIndexedCapNodes) {
							boolean isRange = indexedCapNode.getIsRange() != 0;
							byte reportId = indexedCapNode.getReportID();
							int reportBitOffset = reportBitOffsetByReportId.getOrDefault(reportId, 0);
							Capability capability;
							if (indexedCapNode instanceof HIDP_BUTTON_CAPS) {
								HIDP_BUTTON_CAPS buttonCapNode = (HIDP_BUTTON_CAPS)indexedCapNode;
								short usageMin = isRange ? buttonCapNode.u.Range.UsageMin : 0;
								short usageMax = isRange ? buttonCapNode.u.Range.UsageMax : 0;
								capability = new Capability.ButtonRange(
									capabilityType,
									reportId,
									reportBitOffset,
									buttonCapNode.getDataIndexMin(),
									buttonCapNode.getDataIndexMax(),
									buttonCapNode.UsagePage,
									usageMin,
									usageMax
								);
							}
							else if (indexedCapNode instanceof HIDP_VALUE_CAPS) {
								HIDP_VALUE_CAPS valueCapNode = (HIDP_VALUE_CAPS)indexedCapNode;
								capability = new Capability.Value(
									capabilityType,
									reportId,
									reportBitOffset,
									valueCapNode.getDataIndexMin(),
									valueCapNode.getDataIndexMax(),
									valueCapNode.UsagePage,
									isRange ? 0 : valueCapNode.u.NotRange.Usage,
									valueCapNode.LogicalMin.longValue(),
									valueCapNode.LogicalMax.longValue(),
									valueCapNode.BitSize,
									valueCapNode.ReportCount
								);
							}
							else {
								throw new UnsupportedOperationException("Native capability type not currently supported: " + indexedCapNode.getClass());
							}
							int reportBitLength = capability.getReportBitLength();
							reportBitOffsetByReportId.put(reportId, reportBitOffset + reportBitLength);
							capabilities.add(capability);
						}
					}
					m_Capabilities = new Capability[capabilities.size()];
					capabilities.toArray(m_Capabilities);
				}

				HidD_FreePreparsedData(ppd[0]);
			}

			Memory wstr = new Memory(256);
			int sizeofWstr = (int)(wstr.size());

			if (HidD_GetSerialNumberString(handle, wstr, sizeofWstr))
				m_SerialNumberString = wstr.getWideString(0);
			if (HidD_GetManufacturerString(handle, wstr, sizeofWstr))
				m_ManufactureString = wstr.getWideString(0);
			if (HidD_GetProductString(handle, wstr, sizeofWstr))
				m_ProductString = wstr.getWideString(0);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
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
package purejavahidapi.windows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.win32.StdCallLibrary;
import purejavahidapi.dataparser.Capability;

import static purejavahidapi.windows.WinDef.HANDLE;

public class HidLibrary {
	static HidLibraryInterface INSTANCE = (HidLibraryInterface) Native.loadLibrary("hid", HidLibraryInterface.class);

	public enum HIDP_REPORT_TYPE {
		HidP_Input(0, Capability.Type.INPUT),
		HidP_Output(1, Capability.Type.OUTPUT),
		HidP_Feature(2, Capability.Type.FEATURE);
		
		public final int value;
		public final Capability.Type capabilityType;
		HIDP_REPORT_TYPE(int value, Capability.Type capabilityType) {
			this.value = value;
			this.capabilityType = capabilityType;
		}
		
		public static HIDP_REPORT_TYPE fromCapabilityType(Capability.Type capabilityType) {
			for (HIDP_REPORT_TYPE reportType : values()) {
				if (reportType.capabilityType == capabilityType) {
					return reportType;
				}
			}
			throw new IllegalArgumentException("Unable to find HIDP_REPORT_TYPE for Capability.Type = " + capabilityType);
		}
		
		public short getNumberButtonCaps(HIDP_CAPS caps) {
			if (caps == null) { return 0; }
			switch (this) {
				case HidP_Input: return caps.NumberInputButtonCaps;
				case HidP_Output: return caps.NumberOutputButtonCaps;
				case HidP_Feature: return caps.NumberFeatureButtonCaps;
			}
			return 0;
		}
		public short getNumberValueCaps(HIDP_CAPS caps) {
			if (caps == null) { return 0; }
			switch (this) {
				case HidP_Input: return caps.NumberInputValueCaps;
				case HidP_Output: return caps.NumberOutputValueCaps;
				case HidP_Feature: return caps.NumberFeatureValueCaps;
			}
			return 0;
		}
		public short getNumberDataIndices(HIDP_CAPS caps) {
			if (caps == null) { return 0; }
			switch (this) {
				case HidP_Input: return caps.NumberInputDataIndices;
				case HidP_Output: return caps.NumberOutputDataIndices;
				case HidP_Feature: return caps.NumberFeatureDataIndices;
			}
			return 0;
		}
		
	}
	
	/**
	 * The set of NTSTATUS codes returned by functions in this library.
	 * Based on the hidpi.h source code.
	 * Search for "#define FACILITY_HID_ERROR_CODE".
	 */
	public enum HIDP_STATUS {
		SUCCESS                 (0x0, 0),
		NULL                    (0x8, 1),
		INVALID_PREPARSED_DATA  (0xC, 1),
		INVALID_REPORT_TYPE     (0xC, 2),
		INVALID_REPORT_LENGTH   (0xC, 3),
		USAGE_NOT_FOUND         (0xC, 4),
		VALUE_OUT_OF_RANGE      (0xC, 5),
		BAD_LOG_PHY_VALUES      (0xC, 6),
		BUFFER_TOO_SMALL        (0xC, 7),
		INTERNAL_ERROR          (0xC, 8),
		I8042_TRANS_UNKNOWN     (0xC, 9),
		INCOMPATIBLE_REPORT_ID  (0xC, 0xA),
		NOT_VALUE_ARRAY         (0xC, 0xB),
		IS_VALUE_ARRAY          (0xC, 0xC),
		DATA_INDEX_NOT_FOUND    (0xC, 0xD),
		DATA_INDEX_OUT_OF_RANGE (0xC, 0xE),
		BUTTON_NOT_PRESSED      (0xC, 0xF),
		REPORT_DOES_NOT_EXIST   (0xC, 0x10),
		NOT_IMPLEMENTED         (0xC, 0x20);
		
		public static final long FACILITY_HID_ERROR_CODE = 0x11;
		public final long value;
		HIDP_STATUS(long severity, long code) {
			this.value = (severity << 28) | (FACILITY_HID_ERROR_CODE << 16) | code;
		}
		
		@Override
		public String toString() { return "HIDP_STATUS_" + super.toString(); }
		
		// inverse lookup
		private static final HashMap<Long, HIDP_STATUS> VALUES_LOOKUP = new HashMap<>(values().length);
		static {
			for (HIDP_STATUS hidpStatus : values()) {
				VALUES_LOOKUP.put(hidpStatus.value, hidpStatus);
			}
		}
		public static HIDP_STATUS fromValue(long value) {
			return VALUES_LOOKUP.get(value);
		}
	}
	
	static public class HIDP_PREPARSED_DATA extends PointerType {
	}

	static public class HIDP_LINK_COLLECTION_NODE extends Structure {
		public short LinkUsage;
		public short LinkUsagePage;
		public short Parent;
		public short NumberOfChildren;
		public short NextSibling;
		public short FirstChild;
		public int CollectionType; // :8 bitfield !!
		// ULONG IsAlias :1;
		// ULONG Reserved :23;
		public Pointer UserContext;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(
					"LinkUsage", //
					"LinkUsagePage",//
					"Parent", //
					"NumberOfChildren",//
					"NextSibling", //
					"FirstChild",//
					"CollectionType",//
					"UserContext"//
					);
		}
	}

	static public class HIDD_ATTRIBUTES extends Structure {
		public NativeLong Size = new NativeLong();
		public short VendorID;
		public short ProductID;
		public short VersionNumber;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("Size", "VendorID", "ProductID", "VersionNumber");
		}
	};

	public static class HIDP_VALUE_CAPS_Range extends Structure {
		public short UsageMin;
		public short UsageMax;
		public short StringMin;
		public short StringMax;
		public short DesignatorMin;
		public short DesignatorMax;
		public short DataIndexMin;
		public short DataIndexMax;

		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"UsageMin",//
					"UsageMax",//
					"StringMin",//
					"StringMax",//
					"DesignatorMin",//
					"DesignatorMax",//
					"DataIndexMin",//
					"DataIndexMax"//
			);
		}
	}

	public static class HIDP_VALUE_CAPS_NotRange extends Structure {
		public short Usage;
		public short Reserved1;
		public short StringIndex;
		public short Reserved2;
		public short DesignatorIndex;
		public short Reserved3;
		public short DataIndex;
		public short Reserved4;

		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"Usage",//
					"Reserved1",//
					"StringIndex",//
					"Reserved2",//
					"DesignatorIndex",//
					"Reserved3",//
					"DataIndex",//
					"Reserved4"//
			);
		}
	}

	public static class HIDP_CAPS_union extends Union {
		public HIDP_VALUE_CAPS_Range Range;
		public HIDP_VALUE_CAPS_NotRange NotRange;
	}
	
	public interface HidPDataIndexedCaps {
		byte getReportID();
		byte getIsRange();
		HIDP_CAPS_union getRangeUnion();
		
		default short getDataIndexMin() {
			return getIsRange() == 0 ? getRangeUnion().NotRange.DataIndex : getRangeUnion().Range.DataIndexMin;
		}
		
		default short getDataIndexMax() {
			return getIsRange() == 0 ? getRangeUnion().NotRange.DataIndex : getRangeUnion().Range.DataIndexMax;
		}
	}

	public static class HIDP_VALUE_CAPS extends Structure implements HidPDataIndexedCaps {
		public short UsagePage;
		public byte ReportID;
		public byte IsAlias;
		public short BitField;
		public short LinkCollection;
		public short LinkUsage;
		public short LinkUsagePage;
		public byte IsRange;
		public byte IsStringRange;
		public byte IsDesignatorRange;
		public byte IsAbsolute;
		public byte HasNull;
		public byte Reserved;
		public short BitSize;
		public short ReportCount;
		public short[] Reserved2=new short[5];
//		public short Reserved2_1;
//		public short Reserved2_2;
//		public short Reserved2_3;
//		public short Reserved2_4;
//		public short Reserved2_5;
		public NativeLong UnitsExp;
		public NativeLong Units;
		public NativeLong LogicalMin;
		public NativeLong LogicalMax;
		public NativeLong PhysicalMin;
		public NativeLong PhysicalMax;

		// public HIDP_VALUE_CAPS_union u=new HIDP_VALUE_CAPS_union();
		//		public short Usage;
		//		public short Reserved1;
		//		public short StringIndex;
		//		public short Reserved2;
		//		public short DesignatorIndex;
		//		public short Reserved3;
		//		public short DataIndex;
		//		public short Reserved4;

		// USAGE UsageMin;
		// USAGE UsageMax;
		// USHORT StringMin;
		// USHORT StringMax;
		// USHORT DesignatorMin;
		// USHORT DesignatorMax;
		// USHORT DataIndexMin;
		// USHORT DataIndexMax;
		// } Range;
		// struct {

		public HIDP_CAPS_union u;

		@Override
		public void read() {
			super.read();
			if (IsRange != 0)
				u.setType(HIDP_VALUE_CAPS_Range.class);
			else
				u.setType(HIDP_VALUE_CAPS_NotRange.class);
			u.read();
		}

		public void dump() {
			for (String name : getFieldOrder())
				System.out.println(name + " " + fieldOffset(name));
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"UsagePage",//
					"ReportID",//
					"IsAlias",//
					"BitField",//
					"LinkCollection",//
					"LinkUsage",//
					"LinkUsagePage",//
					"IsRange",//
					"IsStringRange",//
					"IsDesignatorRange",//
					"IsAbsolute",//
					"HasNull",//
					"Reserved",//
					"BitSize",//
					"ReportCount",//
					"Reserved2",//
//					"Reserved2_1",//
//					"Reserved2_2",//
//					"Reserved2_3",//
//					"Reserved2_4",//
//					"Reserved2_5",//
					"UnitsExp",//
					"Units",//
					"LogicalMin",//
					"LogicalMax",//
					"PhysicalMin",//
					"PhysicalMax",//
					"u"//
			);

		}
		
		@Override
		public byte getReportID() { return ReportID; }
		
		@Override
		public byte getIsRange() { return IsRange; }
		
		@Override
		public HIDP_CAPS_union getRangeUnion() { return u; }

	}

	public static class HIDP_BUTTON_CAPS extends Structure implements HidPDataIndexedCaps {
		public short UsagePage;
		public byte ReportID;
		public byte IsAlias;
		public short BitField;
		public short LinkCollection;
		public short LinkUsage;
		public short LinkUsagePage;
		public byte IsRange;
		public byte IsStringRange;
		public byte IsDesignatorRange;
		public byte IsAbsolute;
		public int[] Reserved=new int[10];
//		public int Reserved_1;
//		public int Reserved_2;
//		public int Reserved_3;
//		public int Reserved_4;
//		public int Reserved_5;
//		public int Reserved_6;
//		public int Reserved_7;
//		public int Reserved_8;
//		public int Reserved_9;
//		public int Reserved_10;

		public HIDP_CAPS_union u;

		@Override
		public void read() {
			super.read();
			if (IsRange != 0)
				u.setType(HIDP_VALUE_CAPS_Range.class);
			else
				u.setType(HIDP_VALUE_CAPS_NotRange.class);
			u.read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"UsagePage",//
					"ReportID",//
					"IsAlias",//
					"BitField",//
					"LinkCollection",//
					"LinkUsage",//
					"LinkUsagePage",//
					"IsRange",//
					"IsStringRange",//
					"IsDesignatorRange",//
					"IsAbsolute",//
					"Reserved",//
//					"Reserved_2",//
//					"Reserved_3",//
//					"Reserved_4",//
//					"Reserved_5",//
//					"Reserved_6",//
//					"Reserved_7",//
//					"Reserved_8",//
//					"Reserved_9",//
//					"Reserved_10",//
					"u"//
			);
		}
		
		@Override
		public byte getReportID() { return ReportID; }
		
		@Override
		public byte getIsRange() { return IsRange; }
		
		@Override
		public HIDP_CAPS_union getRangeUnion() { return u; }

	}

	public static class HIDP_CAPS extends Structure {
		public short Usage;
		public short UsagePage;
		public short InputReportByteLength;
		public short OutputReportByteLength;
		public short FeatureReportByteLength;
		public short[] Reserved = new short[17];
		public short NumberLinkCollectionNodes;
		public short NumberInputButtonCaps;
		public short NumberInputValueCaps;
		public short NumberInputDataIndices;
		public short NumberOutputButtonCaps;
		public short NumberOutputValueCaps;
		public short NumberOutputDataIndices;
		public short NumberFeatureButtonCaps;
		public short NumberFeatureValueCaps;
		public short NumberFeatureDataIndices;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(
					"Usage", 
					"UsagePage",
					"InputReportByteLength",
					"OutputReportByteLength",
					"FeatureReportByteLength",
					"Reserved",
					"NumberLinkCollectionNodes",
					"NumberInputButtonCaps",
					"NumberInputValueCaps",
					"NumberInputDataIndices",
					"NumberOutputButtonCaps",
					"NumberOutputValueCaps",
					"NumberOutputDataIndices",
					"NumberFeatureButtonCaps",
					"NumberFeatureValueCaps",
					"NumberFeatureDataIndices"
			);
		}
	};

	public interface HidLibraryInterface extends StdCallLibrary {
		boolean HidD_GetAttributes(HANDLE HidDeviceObject, HIDD_ATTRIBUTES Attributes);

		boolean HidD_GetPreparsedData(HANDLE HidDeviceObject, HIDP_PREPARSED_DATA[] PreparsedData);

		boolean HidD_FreePreparsedData(HIDP_PREPARSED_DATA PreparsedData);

		int HidP_GetCaps(HIDP_PREPARSED_DATA PreparsedData, HIDP_CAPS Capabilities);

		boolean HidD_GetSerialNumberString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength);

		boolean HidD_GetManufacturerString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength);
		
		boolean HidD_GetProductString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength);

		boolean HidD_SetFeature(HANDLE HidDeviceObject, byte[] ReportBuffer, int ReportBufferLength);

		boolean HidD_GetFeature(HANDLE HidDeviceObject,  byte[] ReportBuffer, int ReportBufferLength);

		boolean HidP_GetLinkCollectionNodes(HIDP_LINK_COLLECTION_NODE[] LinkCollectionNodes, int[] LinkCollectionNodesLength, HIDP_PREPARSED_DATA PreparsedData);

		boolean HidP_GetSpecificValueCaps(int ReportType, short UsagePage, short LinkCollection, short Usage, HIDP_VALUE_CAPS[] ValueCaps, short[] ValueCapsLength, HIDP_PREPARSED_DATA PreparsedData);

		boolean HidP_GetValueCaps(int ReportType, HIDP_VALUE_CAPS[] ValueCaps, short[] ValueCapsLength, HIDP_PREPARSED_DATA PreparsedData);

		boolean HidP_GetButtonCaps(int ReportType, HIDP_BUTTON_CAPS[] ButtonCaps, short[] ButtonCapsLength, HIDP_PREPARSED_DATA PreparsedData);

		boolean HidD_GetPhysicalDescriptor(HANDLE HidDeviceObject,Pointer Buffer,int BufferLength);
		
		boolean HidD_SetOutputReport(HANDLE HidDeviceObject, byte[] ReportBuffer, int ReportBufferLength);
		
		long HidP_MaxUsageListLength(int ReportType, short UsagePage, HIDP_PREPARSED_DATA PreparsedData);
		
		long HidP_GetUsages(int ReportType, short UsagePage, short LinkCollection, short[] UsageList, long[] UsageLength, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength);
		
		long HidP_GetUsageValue(int ReportType, short UsagePage, short LinkCollection, short Usage, long[] UsageValue, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength);
		
		long HidP_GetUsageValueArray(int ReportType, short UsagePage, short LinkCollection, short Usage, byte[] UsageValue, short UsageValueByteLength, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength);
	}

	static public boolean HidD_GetAttributes(HANDLE HidDeviceObject, HIDD_ATTRIBUTES Attributes) {
		return INSTANCE.HidD_GetAttributes(HidDeviceObject, Attributes);
	}

	static public boolean HidD_GetPreparsedData(HANDLE HidDeviceObject, HIDP_PREPARSED_DATA[] PreparsedData) {
		return INSTANCE.HidD_GetPreparsedData(HidDeviceObject, PreparsedData);
	}

	static public boolean HidD_FreePreparsedData(HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidD_FreePreparsedData(PreparsedData);
	}

	static public int HidP_GetCaps(HIDP_PREPARSED_DATA PreparsedData, HIDP_CAPS Capabilities) {
		return INSTANCE.HidP_GetCaps(PreparsedData, Capabilities);
	}

	static public boolean HidD_GetSerialNumberString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength) {
		return INSTANCE.HidD_GetSerialNumberString(HidDeviceObject, Buffer, BufferLength);
	}

	static public boolean HidD_GetManufacturerString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength) {
		return INSTANCE.HidD_GetManufacturerString(HidDeviceObject, Buffer, BufferLength);
	}
	
	static public boolean HidD_GetProductString(HANDLE HidDeviceObject, Pointer Buffer, int BufferLength) {
		return INSTANCE.HidD_GetProductString(HidDeviceObject, Buffer, BufferLength);
	}

	static public boolean HidD_SetFeature(HANDLE HidDeviceObject, byte[] ReportBuffer, int ReportBufferLength) {
		return INSTANCE.HidD_SetFeature(HidDeviceObject, ReportBuffer, ReportBufferLength);
	}

	static public boolean HidD_GetFeature(HANDLE HidDeviceObject,  byte[] ReportBuffer, int ReportBufferLength) {
		return INSTANCE.HidD_GetFeature(HidDeviceObject, ReportBuffer, ReportBufferLength);
	}

	static public boolean HidP_GetLinkCollectionNodes(HIDP_LINK_COLLECTION_NODE[] LinkCollectionNodes, int[] LinkCollectionNodesLength, HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidP_GetLinkCollectionNodes(LinkCollectionNodes, LinkCollectionNodesLength, PreparsedData);
	}

	static public boolean HidP_GetSpecificValueCaps(HIDP_REPORT_TYPE ReportType, short UsagePage, short LinkCollection, short Usage, HIDP_VALUE_CAPS[] ValueCaps, short[] ValueCapsLength, HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidP_GetSpecificValueCaps(ReportType.value, UsagePage, LinkCollection, Usage, ValueCaps, ValueCapsLength, PreparsedData);
	}

	static public boolean HidP_GetValueCaps(HIDP_REPORT_TYPE ReportType, HIDP_VALUE_CAPS[] ValueCaps, short[] ValueCapsLength, HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidP_GetValueCaps(ReportType.value, ValueCaps, ValueCapsLength, PreparsedData);
	}

	static public boolean HidP_GetButtonCaps(HIDP_REPORT_TYPE ReportType, HIDP_BUTTON_CAPS[] ButtonCaps, short[] ButtonCapsLength, HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidP_GetButtonCaps(ReportType.value, ButtonCaps, ButtonCapsLength, PreparsedData);
	}
	
	static public boolean HidD_GetPhysicalDescriptor(HANDLE HidDeviceObject,Pointer Buffer,int BufferLength) {
		return INSTANCE.HidD_GetPhysicalDescriptor(HidDeviceObject, Buffer, BufferLength);
	}

	static public boolean HidD_SetOutputReport(HANDLE HidDeviceObject, byte[] ReportBuffer, int ReportBufferLength){
		return INSTANCE.HidD_SetOutputReport(HidDeviceObject, ReportBuffer, ReportBufferLength);
	}
	
	static public long HidP_MaxUsageListLength(HIDP_REPORT_TYPE ReportType, short UsagePage, HIDP_PREPARSED_DATA PreparsedData) {
		return INSTANCE.HidP_MaxUsageListLength(ReportType.value, UsagePage, PreparsedData);
	}
	
	static public HIDP_STATUS HidP_GetButtons(HIDP_REPORT_TYPE ReportType, short UsagePage, short LinkCollection, short[] UsageList, long[] UsageLength, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength) {
		long statusValue = INSTANCE.HidP_GetUsages(ReportType.value, UsagePage, LinkCollection, UsageList, UsageLength, PreparsedData, Report, ReportLength);
		return HIDP_STATUS.fromValue(statusValue);
	}
	
	static public HIDP_STATUS HidP_GetUsageValue(HIDP_REPORT_TYPE ReportType, short UsagePage, short LinkCollection, short Usage, long[] UsageValue, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength) {
		long statusValue = INSTANCE.HidP_GetUsageValue(ReportType.value, UsagePage, LinkCollection, Usage, UsageValue, PreparsedData, Report, ReportLength);
		return HIDP_STATUS.fromValue(statusValue);
	}
	
	static public HIDP_STATUS HidP_GetUsageValueArray(HIDP_REPORT_TYPE ReportType, short UsagePage, short LinkCollection, short Usage, byte[] UsageValue, short UsageValueByteLength, HIDP_PREPARSED_DATA PreparsedData, byte[] Report, long ReportLength) {
		long statusValue = INSTANCE.HidP_GetUsageValueArray(ReportType.value, UsagePage, LinkCollection, Usage, UsageValue, UsageValueByteLength, PreparsedData, Report, ReportLength);
		return HIDP_STATUS.fromValue(statusValue);
	}
}

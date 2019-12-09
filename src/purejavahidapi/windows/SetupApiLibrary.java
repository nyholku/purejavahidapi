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

// TODO check what is the correct way to handle windows A/W calls with JNA . How to get Strings back, check for unicode string
// W32APIOptions.UNICODE_OPTIONS
// TODO should we add get listener functions

// HID descritor parsing 
// http://msdn.microsoft.com/en-us/library/windows/hardware/ff538731(v=vs.85).aspx

// getting top level collections
//NTSTATUS __stdcall HidP_GetLinkCollectionNodes(
//		  _Out_    PHIDP_LINK_COLLECTION_NODE LinkCollectionNodes,
//		  _Inout_  PULONG LinkCollectionNodesLength,
//		  _In_     PHIDP_PREPARSED_DATA PreparsedData
//		);

//typedef struct _HIDP_LINK_COLLECTION_NODE {
//	  USAGE  LinkUsage;
//	  USAGE  LinkUsagePage;
//	  USHORT Parent;
//	  USHORT NumberOfChildren;
//	  USHORT NextSibling;
//	  USHORT FirstChild;
//	  ULONG  CollectionType  :8;
//	  ULONG  IsAlias  :1;
//	  ULONG  Reserved  :23;
//	  PVOID  UserContext;
//	} HIDP_LINK_COLLECTION_NODE, *PHIDP_LINK_COLLECTION_NODE;

//NTSTATUS __stdcall HidP_GetValueCaps(
//		  _In_     HIDP_REPORT_TYPE ReportType,
//		  _Out_    PHIDP_VALUE_CAPS ValueCaps,
//		  _Inout_  PUSHORT ValueCapsLength,
//		  _In_     PHIDP_PREPARSED_DATA PreparsedData
//		);

//BOOLEAN __stdcall HidD_GetPreparsedData(
//		  _In_   HANDLE HidDeviceObject,
//		  _Out_  PHIDP_PREPARSED_DATA *PreparsedData
//		);

//typedef struct _HIDP_CAPS {
//	  USAGE  Usage;
//	  USAGE  UsagePage;
//	  USHORT InputReportByteLength;
//	  USHORT OutputReportByteLength;
//	  USHORT FeatureReportByteLength;
//	  USHORT Reserved[17];
//	  USHORT NumberLinkCollectionNodes;
//	  USHORT NumberInputButtonCaps;
//	  USHORT NumberInputValueCaps;
//	  USHORT NumberInputDataIndices;
//	  USHORT NumberOutputButtonCaps;
//	  USHORT NumberOutputValueCaps;
//	  USHORT NumberOutputDataIndices;
//	  USHORT NumberFeatureButtonCaps;
//	  USHORT NumberFeatureValueCaps;
//	  USHORT NumberFeatureDataIndices;
//	} HIDP_CAPS, *PHIDP_CAPS;

//NTSTATUS __stdcall HidP_GetUsageValue(
//		  _In_   HIDP_REPORT_TYPE ReportType,
//		  _In_   USAGE UsagePage,
//		  _In_   USHORT LinkCollection,
//		  _In_   USAGE Usage,
//		  _Out_  PULONG UsageValue,
//		  _In_   PHIDP_PREPARSED_DATA PreparsedData,
//		  _In_   PCHAR Report,
//		  _In_   ULONG ReportLength
//		);

//NTSTATUS __stdcall HidP_GetSpecificValueCaps(
//		  _In_     HIDP_REPORT_TYPE ReportType,
//		  _In_     USAGE UsagePage,
//		  _In_     USHORT LinkCollection,
//		  _In_     USAGE Usage,
//		  _Out_    PHIDP_VALUE_CAPS ValueCaps,
//		  _Inout_  PUSHORT ValueCapsLength,
//		  _In_     PHIDP_PREPARSED_DATA PreparsedData
//		);

// Walking through the collection:
// HidP_GetLinkCollectionNodes -> NA
// walk(NA,0);

// walk(NA,n);
// for i=NA[n].firstChild , NA[f].NumberOfChildren
//    walk(NA,i);
// while n=NA[n].nextSibling
//     HidP_GetSpecificValueCaps(0,n.0
//
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.W32APIOptions;

import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinDef.HWND;

public class SetupApiLibrary {
	static SetupApiInterface INSTANCE = (SetupApiInterface) Native.load("setupapi", SetupApiInterface.class,
			W32APIOptions.UNICODE_OPTIONS);

	public static final int DIGCF_PRESENT = 2;
	public static final int DIGCF_ALLCLASSES = 4;
	public static final int DIGCF_DEVICEINTERFACE = 16;

	public static final int SPDRP_DEVICEDESC = 0x00000000;
	public static final int SPDRP_HARDWAREID = 0x00000001;
	public static final int SPDRP_COMPATIBLEIDS = 0x00000002;
	public static final int SPDRP_UNUSED0 = 0x00000003;
	public static final int SPDRP_SERVICE = 0x00000004;
	public static final int SPDRP_UNUSED1 = 0x00000005;
	public static final int SPDRP_UNUSED2 = 0x00000006;
	public static final int SPDRP_CLASS = 0x00000007;
	public static final int SPDRP_CLASSGUID = 0x00000008;
	public static final int SPDRP_DRIVER = 0x00000009;
	public static final int SPDRP_CONFIGFLAGS = 0x0000000A;
	public static final int SPDRP_MFG = 0x0000000B;
	public static final int SPDRP_FRIENDLYNAME = 0x0000000C;
	public static final int SPDRP_LOCATION_INFORMATION = 0x0000000D;
	public static final int SPDRP_PHYSICAL_DEVICE_OBJECT_NAME = 0x0000000E;
	public static final int SPDRP_CAPABILITIES = 0x0000000F;
	public static final int SPDRP_UI_NUMBER = 0x00000010;
	public static final int SPDRP_UPPERFILTERS = 0x00000011;
	public static final int SPDRP_LOWERFILTERS = 0x00000012;
	public static final int SPDRP_BUSTYPEGUID = 0x00000013;
	public static final int SPDRP_LEGACYBUSTYPE = 0x00000014;
	public static final int SPDRP_BUSNUMBER = 0x00000015;
	public static final int SPDRP_ENUMERATOR_NAME = 0x00000016;
	public static final int SPDRP_SECURITY = 0x00000017;
	public static final int SPDRP_SECURITY_SDS = 0x00000018;
	public static final int SPDRP_DEVTYPE = 0x00000019;
	public static final int SPDRP_EXCLUSIVE = 0x0000001A;
	public static final int SPDRP_CHARACTERISTICS = 0x0000001B;
	public static final int SPDRP_ADDRESS = 0x0000001C;
	public static final int SPDRP_UI_NUMBER_DESC_FORMAT = 0X0000001D;
	public static final int SPDRP_DEVICE_POWER_DATA = 0x0000001E;
	public static final int SPDRP_REMOVAL_POLICY = 0x0000001F;
	public static final int SPDRP_REMOVAL_POLICY_HW_DEFAULT = 0x00000020;
	public static final int SPDRP_REMOVAL_POLICY_OVERRIDE = 0x00000021;
	public static final int SPDRP_INSTALL_STATE = 0x00000022;
	public static final int SPDRP_LOCATION_PATHS = 0x00000023;

	public static class GUID extends Structure {
		public int Data1;
		public short Data2;
		public short Data3;
		public byte[] Data4 = new byte[8];

		public GUID() {
		}

		public GUID(int... data) {
			Data1 = data[0];
			Data2 = (short) data[1];
			Data3 = (short) data[2];
			for (int i = 0; i < Data4.length; i++)
				Data4[i] = (byte) data[3 + i];
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("Data1", "Data2", "Data3", "Data4");

		}

	};

	public static class SP_DEVINFO_DATA extends Structure {
		public int cbSize;
		public GUID ClassGuid;
		public int DevInst;
		public Pointer Reserved;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("cbSize", "ClassGuid", "DevInst", "Reserved");
		}
	};

	public static class SP_DEVICE_INTERFACE_DATA extends Structure {
		public int cbSize;
		public GUID InterfaceClassGuid;
		public int Flags;
		public Pointer Reserved;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("cbSize", "InterfaceClassGuid", "Flags", "Reserved");
		}
	};

	final static int ANYSIZE_ARRAY = 1;

	static public class SP_DEVICE_INTERFACE_DETAIL_DATA_A extends Structure {
		public int cbSize = Native.POINTER_SIZE == 8 ? 8 : 5; // Note 1
		// Note 1, I believe this structure is packed in Windows API and as this field
		// is initialized with sizeof(SP_DEVICE_INTERFACE_DETAIL_DATA) it gets the size
		// 5 in 32 bit process but 8 in 64 bit process...i think! Nasty little detail
		// when accessing C API from Java with JNA
		public char[] DevicePath;

		public SP_DEVICE_INTERFACE_DETAIL_DATA_A(int cbsize, int size) {
			cbSize = cbsize;
			DevicePath = new char[size - 4];
			allocateMemory();
		}

		public SP_DEVICE_INTERFACE_DETAIL_DATA_A(int size) {
			DevicePath = new char[size - 4];
			allocateMemory();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("cbSize", "DevicePath");
		}
	};

	public static class HDEVINFO extends HANDLE {

		public HDEVINFO() {

		}

		public HDEVINFO(Pointer ptr) {
			super(ptr);
		}
	};

	final static int HIDP_STATUS_SUCCESS = 0x110000;

	public interface SetupApiInterface extends Library {
		HDEVINFO SetupDiCreateDeviceInfoList(GUID ClassGuid, HWND hwndParent);

		HDEVINFO SetupDiGetClassDevs(GUID ClassGuid, String Enumerator, HWND hwndParent, int Flags);

		boolean SetupDiEnumDeviceInterfaces(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
				GUID InterfaceClassGuid, int MemberIndex, SP_DEVICE_INTERFACE_DATA DeviceInterfaceData);

		boolean SetupDiGetDeviceInterfaceDetail(HDEVINFO DeviceInfoSet, SP_DEVICE_INTERFACE_DATA DeviceInterfaceData,
				SP_DEVICE_INTERFACE_DETAIL_DATA_A DeviceInterfaceDetailData, int DeviceInterfaceDetailDataSize,
				int[] RequiredSize, SP_DEVINFO_DATA DeviceInfoData);

		boolean SetupDiEnumDeviceInfo(HDEVINFO DeviceInfoSet, int MemberIndex, SP_DEVINFO_DATA DeviceInfoData);

		boolean SetupDiGetDeviceRegistryProperty(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData, int Property,
				int[] PropertyRegDataType, char[] PropertyBuffer, int PropertyBufferSize, int[] RequiredSize);

		boolean SetupDiDestroyDeviceInfoList(HDEVINFO DeviceInfoSet);

		boolean SetupDiGetDeviceInstanceId(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
				char[] DeviceInstanceId, int DeviceInstanceIdSize, int[] RequiredSize);

		boolean SetupDiOpenDeviceInterface(HDEVINFO DeviceInfoSet, String DevicePath, int OpenFlags,
				SP_DEVICE_INTERFACE_DATA DeviceInterfaceData);

		boolean SetupDiDeleteDeviceInterfaceData(HDEVINFO DeviceInfoSet, SP_DEVICE_INTERFACE_DATA DeviceInterfaceData);

		boolean SetupDiOpenDeviceInfo(HDEVINFO DeviceInfoSet, String DeviceInstanceId, HWND hwndParent, int OpenFlags,
									  SP_DEVINFO_DATA DeviceInfoData);
	}

	public static HDEVINFO SetupDiCreateDeviceInfoList(GUID ClassGuid, HWND hwndParent) {
		return INSTANCE.SetupDiCreateDeviceInfoList(ClassGuid, hwndParent);
	}

	public static HDEVINFO SetupDiGetClassDevs(GUID ClassGuid, String Enumerator, HWND hwndParent, int Flags) {
		return INSTANCE.SetupDiGetClassDevs(ClassGuid, Enumerator, hwndParent, Flags);
	}

	public static boolean SetupDiEnumDeviceInterfaces(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
			GUID InterfaceClassGuid, int MemberIndex, SP_DEVICE_INTERFACE_DATA DeviceInterfaceData) {
		return INSTANCE.SetupDiEnumDeviceInterfaces(DeviceInfoSet, DeviceInfoData, InterfaceClassGuid, MemberIndex,
				DeviceInterfaceData);
	}

	public static boolean SetupDiEnumDeviceInfo(HDEVINFO DeviceInfoSet, int MemberIndex,
			SP_DEVINFO_DATA DeviceInfoData) {
		return INSTANCE.SetupDiEnumDeviceInfo(DeviceInfoSet, MemberIndex, DeviceInfoData);
	}

	public static boolean SetupDiGetDeviceInterfaceDetail(HDEVINFO DeviceInfoSet,
			SP_DEVICE_INTERFACE_DATA DeviceInterfaceData, SP_DEVICE_INTERFACE_DETAIL_DATA_A DeviceInterfaceDetailData,
			int DeviceInterfaceDetailDataSize, int[] RequiredSize, SP_DEVINFO_DATA DeviceInfoData) {
		return INSTANCE.SetupDiGetDeviceInterfaceDetail(DeviceInfoSet, DeviceInterfaceData, DeviceInterfaceDetailData,
				DeviceInterfaceDetailDataSize, RequiredSize, DeviceInfoData);
	}

	public static boolean SetupDiGetDeviceRegistryProperty(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
			int Property, int[] PropertyRegDataType, char[] PropertyBuffer, int PropertyBufferSize,
			int[] RequiredSize) {
		return INSTANCE.SetupDiGetDeviceRegistryProperty(DeviceInfoSet, DeviceInfoData, Property, PropertyRegDataType,
				PropertyBuffer, PropertyBufferSize, RequiredSize);
	}

	static public boolean SetupDiDestroyDeviceInfoList(HDEVINFO DeviceInfoSet) {
		return INSTANCE.SetupDiDestroyDeviceInfoList(DeviceInfoSet);
	}

	static public boolean SetupDiGetDeviceInstanceId(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
			char[] DeviceInstanceId, int DeviceInstanceIdSize, int[] RequiredSize) {
		return INSTANCE.SetupDiGetDeviceInstanceId(DeviceInfoSet, DeviceInfoData, DeviceInstanceId,
				DeviceInstanceIdSize, RequiredSize);
	}

	static public boolean SetupDiOpenDeviceInterface(HDEVINFO DeviceInfoSet, String DevicePath, int OpenFlags,
			SP_DEVICE_INTERFACE_DATA DeviceInterfaceData) {
		return INSTANCE.SetupDiOpenDeviceInterface(DeviceInfoSet, DevicePath, OpenFlags, DeviceInterfaceData);
	}

	static public boolean SetupDiDeleteDeviceInterfaceData(HDEVINFO DeviceInfoSet,
			SP_DEVICE_INTERFACE_DATA DeviceInterfaceData) {
		return INSTANCE.SetupDiDeleteDeviceInterfaceData(DeviceInfoSet, DeviceInterfaceData);
	}

	static public boolean SetupDiOpenDeviceInfo(HDEVINFO DeviceInfoSet, String DeviceInstanceId, HWND hwndParent,
			int OpenFlags, SP_DEVINFO_DATA DeviceInfoData) {
		return INSTANCE.SetupDiOpenDeviceInfo(DeviceInfoSet, DeviceInstanceId, hwndParent, OpenFlags, DeviceInfoData);
	}

}

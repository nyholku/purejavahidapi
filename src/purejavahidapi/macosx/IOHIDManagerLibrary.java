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
package purejavahidapi.macosx;

import static purejavahidapi.macosx.CoreFoundationLibrary.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import purejavahidapi.macosx.CoreFoundationLibrary.CFArrayRef;

import com.sun.jna.*;

public class IOHIDManagerLibrary {
	private static IOHIDManagerLib INSTANCE = (IOHIDManagerLib) Native.load("IOKit", IOHIDManagerLib.class);

	public static final int kIOHIDOptionsTypeNone = 0;
	public static final int kIOHIDOptionsTypeSeizeDevice = 1;

	public static final String kIOHIDTransportKey = "Transport";
	public static final String kIOHIDVendorIDKey = "VendorID";
	public static final String kIOHIDVendorIDSourceKey = "VendorIDSource";
	public static final String kIOHIDProductIDKey = "ProductID";
	public static final String kIOHIDVersionNumberKey = "VersionNumber";
	public static final String kIOHIDManufacturerKey = "Manufacturer";
	public static final String kIOHIDProductKey = "Product";
	public static final String kIOHIDSerialNumberKey = "SerialNumber";
	public static final String kIOHIDCountryCodeKey = "CountryCode";
	public static final String kIOHIDStandardTypeKey = "StandardType";
	public static final String kIOHIDDeviceKeyboardStandardTypeKey = "DeviceKeyboardStandardType";
	public static final String kIOHIDLocationIDKey = "LocationID";
	public static final String kIOHIDDeviceUsageKey = "DeviceUsage";
	public static final String kIOHIDDeviceUsagePageKey = "DeviceUsagePage";
	public static final String kIOHIDDeviceUsagePairsKey = "DeviceUsagePairs";
	public static final String kIOHIDPrimaryUsageKey = "PrimaryUsage";
	public static final String kIOHIDPrimaryUsagePageKey = "PrimaryUsagePage";
	public static final String kIOHIDMaxInputReportSizeKey = "MaxInputReportSize";
	public static final String kIOHIDMaxOutputReportSizeKey = "MaxOutputReportSize";
	public static final String kIOHIDMaxFeatureReportSizeKey = "MaxFeatureReportSize";
	public static final String kIOHIDReportIntervalKey = "ReportInterval";
	public static final String kIOHIDReportDescriptorKey = "ReportDescriptor";
	public static final String kIOHIDResetKey = "Reset";
	public static final String kIOHIDDeviceKeyboardLanguageKey = "DeviceKeyboardLanguage";

	public static final String kIOHIDDeviceKey = "IOHIDDevice";

	public static final int kIOReturnSuccess = 0;

	public static final int kIOHIDReportTypeInput = 0;
	public static final int kIOHIDReportTypeOutput = 1;
	public static final int kIOHIDReportTypeFeature = 2;
	public static final int kIOHIDReportTypeCount = 3;

	public static final int kIOHIDElementTypeInput_Misc = 1;
	public static final int kIOHIDElementTypeInput_Button = 2;
	public static final int kIOHIDElementTypeInput_Axis = 3;
	public static final int kIOHIDElementTypeInput_ScanCodes = 4;
	public static final int kIOHIDElementTypeOutput = 129;
	public static final int kIOHIDElementTypeFeature = 257;
	public static final int kIOHIDElementTypeCollection = 513;

	public static final int kIOHIDElementCollectionTypePhysical = 0;
	public static final int kIOHIDElementCollectionTypeApplication = 1;
	public static final int kIOHIDElementCollectionTypeLogical = 2;
	public static final int kIOHIDElementCollectionTypeReport = 3;
	public static final int kIOHIDElementCollectionTypeNamedArray = 4;
	public static final int kIOHIDElementCollectionTypeUsageSwitch = 5;
	public static final int kIOHIDElementCollectionTypeUsageModifier = 6;

	// --------------------------------------------------------------------------------
	//	struct IOUSBDeviceStruct182 {
	//	    IUNKNOWN_C_GUTS;
	//	    IOReturn (*CreateDeviceAsyncEventSource)(void *self, CFRunLoopSourceRef *source);
	//	    CFRunLoopSourceRef (*GetDeviceAsyncEventSource)(void *self);
	//	    IOReturn (*CreateDeviceAsyncPort)(void *self, mach_port_t *port);
	//	    mach_port_t (*GetDeviceAsyncPort)(void *self);
	//	    IOReturn (*USBDeviceOpen)(void *self);
	//	    IOReturn (*USBDeviceClose)(void *self);
	//	    IOReturn (*GetDeviceClass)(void *self, UInt8 *devClass);
	//	    IOReturn (*GetDeviceSubClass)(void *self, UInt8 *devSubClass);
	//	    IOReturn (*GetDeviceProtocol)(void *self, UInt8 *devProtocol);
	//	    IOReturn (*GetDeviceVendor)(void *self, UInt16 *devVendor);
	//	    IOReturn (*GetDeviceProduct)(void *self, UInt16 *devProduct);
	//	    IOReturn (*GetDeviceReleaseNumber)(void *self, UInt16 *devRelNum);
	//	    IOReturn (*GetDeviceAddress)(void *self, USBDeviceAddress *addr);
	//	    IOReturn (*GetDeviceBusPowerAvailable)(void *self, UInt32 *powerAvailable);
	//	    IOReturn (*GetDeviceSpeed)(void *self, UInt8 *devSpeed);
	//	    IOReturn (*GetNumberOfConfigurations)(void *self, UInt8 *numConfig);
	//	    IOReturn (*GetLocationID)(void *self, UInt32 *locationID);
	//	    IOReturn (*GetConfigurationDescriptorPtr)(void *self, UInt8 configIndex, IOUSBConfigurationDescriptorPtr *desc);
	//	    IOReturn (*GetConfiguration)(void *self, UInt8 *configNum);
	//	    IOReturn (*SetConfiguration)(void *self, UInt8 configNum);
	//	    IOReturn (*GetBusFrameNumber)(void *self, UInt64 *frame, AbsoluteTime *atTime);
	//	    IOReturn (*ResetDevice)(void *self);
	//	    IOReturn (*DeviceRequest)(void *self, IOUSBDevRequest *req);
	//	    IOReturn (*DeviceRequestAsync)(void *self, IOUSBDevRequest *req, IOAsyncCallback1 callback, void *refCon);
	//	    IOReturn (*CreateInterfaceIterator)(void *self, IOUSBFindInterfaceRequest *req, io_iterator_t *iter);

	// --------------------------------------------------------------------------------
	public static class IOMemoryDescriptor extends PointerType {
		public IOMemoryDescriptor(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOMemoryDescriptor() {
			super();
		}

	}

	// --------------------------------------------------------------------------------
	public static class IOHIDManagerRef extends PointerType {
		final static int kIOHIDOptionsTypeNone = 0;

		public IOHIDManagerRef(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOHIDManagerRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class io_service_t extends com.sun.jna.PointerType {
		public io_service_t(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public io_service_t() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class io_cf_plugin_ref_t extends com.sun.jna.PointerType {
		public io_cf_plugin_ref_t(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public io_cf_plugin_ref_t() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class io_object_t extends com.sun.jna.PointerType {
		public io_object_t(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public io_object_t() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class io_iterator_t extends com.sun.jna.PointerType {
		public io_iterator_t(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public io_iterator_t() {
			super();
		}
	}

	// --------------------------------------------------------------------------------

	public static class mach_port_t extends com.sun.jna.PointerType {
		public mach_port_t(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public mach_port_t() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	// FIXME shoud this be XXXref ?
	public static class CFDictionaryRef extends com.sun.jna.PointerType {
		public CFDictionaryRef(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public CFDictionaryRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class IOHIDDeviceRef extends com.sun.jna.PointerType {
		public IOHIDDeviceRef(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOHIDDeviceRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class IOHIDElementRef extends com.sun.jna.PointerType {
		public IOHIDElementRef(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOHIDElementRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class IOUSBDeviceRef extends com.sun.jna.PointerType {
		public IOUSBDeviceRef(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOUSBDeviceRef() {
			super();
		}
	}

	public static class IOUSBDevRequest extends Structure {
		public byte bmRequestType;
		public byte bRequest;
		public short wValue;
		public short wIndex;
		public short wLength;
		public com.sun.jna.Pointer pData;
		public int wLenDone;

		public IOUSBDevRequest() {
			super();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(//
					"bmRequestType", //
					"bRequest", //
					"wValue", //
					"wIndex", //
					"wLength", //
					"pData",//
					"wLenDone" //
			);
		}
	}

	// --------------------------------------------------------------------------------
	public static class IOCFPlugInInterface extends Structure {
		public Pointer reserved;
		public QueryInterfaceCallback QueryInterface;
		public Pointer AddRef;
		public Pointer Release;
		public short version;
		public short revision;

		public static interface QueryInterfaceCallback extends Callback {
			int f(Pointer thisPointer, CFUUID.ByValue refid, Pointer ppv);
		}

		public static interface AddRefCallback extends Callback {
			void f(Pointer thisPointer);
		}

		public static interface ReleaseCallback extends Callback {
			void f(Pointer thisPointer);
		}

		public static interface DeviceRequestCallback extends Callback {
			int f(Pointer thisPointer, IOUSBDevRequest ppv);
		}

		public IOCFPlugInInterface(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOCFPlugInInterface() {
			super();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("reserved", "QueryInterface", "AddRef", "Release", "version", "revision");
		}
	}

	// --------------------------------------------------------------------------------
	public static class IOUSBDeviceInterface320 extends Structure {
		public Pointer reserved;
		public Pointer QueryInterface;
		public Pointer AddRef;
		public Pointer Release;

		public DummyCallback CreateDeviceAsyncEventSource; //	    IOReturn (*CreateDeviceAsyncEventSource)(void *self, CFRunLoopSourceRef *source);
		public DummyCallback GetDeviceAsyncEventSource; //	    CFRunLoopSourceRef (*GetDeviceAsyncEventSource)(void *self);
		public DummyCallback CreateDeviceAsyncPort; //	    IOReturn (*CreateDeviceAsyncPort)(void *self, mach_port_t *port);
		public DummyCallback GetDeviceAsyncPort;//	    mach_port_t (*GetDeviceAsyncPort)(void *self);
		public DummyCallback USBDeviceOpen;//	    IOReturn (*USBDeviceOpen)(void *self);
		public DummyCallback USBDeviceClose;//	    IOReturn (*USBDeviceClose)(void *self);
		public DummyCallback GetDeviceClass;//	    IOReturn (*GetDeviceClass)(void *self, UInt8 *devClass);
		public DummyCallback GetDeviceSubClass;//	    IOReturn (*GetDeviceSubClass)(void *self, UInt8 *devSubClass);
		public DummyCallback GetDeviceProtocol;//	    IOReturn (*GetDeviceProtocol)(void *self, UInt8 *devProtocol);
		public DummyCallback GetDeviceVendor;//	    IOReturn (*GetDeviceVendor)(void *self, UInt16 *devVendor);
		public DummyCallback GetDeviceProduct;//	    IOReturn (*GetDeviceProduct)(void *self, UInt16 *devProduct);
		public DummyCallback GetDeviceReleaseNumber;//	    IOReturn (*GetDeviceReleaseNumber)(void *self, UInt16 *devRelNum);
		public DummyCallback GetDeviceAddress;//	    IOReturn (*GetDeviceAddress)(void *self, USBDeviceAddress *addr);
		public DummyCallback GetDeviceBusPowerAvailable;//	    IOReturn (*GetDeviceBusPowerAvailable)(void *self, UInt32 *powerAvailable);
		public DummyCallback GetDeviceSpeed; //	    IOReturn (*GetDeviceSpeed)(void *self, UInt8 *devSpeed);
		public DummyCallback GetNumberOfConfigurations; //	    IOReturn (*GetNumberOfConfigurations)(void *self, UInt8 *numConfig);
		public DummyCallback GetLocationID; //	    IOReturn (*GetLocationID)(void *self, UInt32 *locationID);
		public DummyCallback GetConfigurationDescriptorPtr; //	    IOReturn (*GetConfigurationDescriptorPtr)(void *self, UInt8 configIndex, IOUSBConfigurationDescriptorPtr *desc);
		public DummyCallback GetConfiguration; //	    IOReturn (*GetConfiguration)(void *self, UInt8 *configNum);
		public DummyCallback SetConfiguration;//	    IOReturn (*SetConfiguration)(void *self, UInt8 configNum);
		public DummyCallback GetBusFrameNumber;//	    IOReturn (*GetBusFrameNumber)(void *self, UInt64 *frame, AbsoluteTime *atTime);
		public DummyCallback ResetDevice;//	    IOReturn (*ResetDevice)(void *self);
		public DeviceRequestCallback DeviceRequest;//	    IOReturn (*DeviceRequest)(void *self, IOUSBDevRequest *req);
		public DummyCallback DeviceRequestAsync;//	    IOReturn (*DeviceRequestAsync)(void *self, IOUSBDevRequest *req, IOAsyncCallback1 callback, void *refCon);
		public DummyCallback CreateInterfaceIterator;//	    IOReturn (*CreateInterfaceIterator)(void *self, IOUSBFindInterfaceRequest *req, io_iterator_t *iter);

		public static interface DummyCallback extends Callback {
			void f();
		}

		public static interface QueryInterfaceCallback extends Callback {
			int f(Pointer thisPointer, CFUUID.ByValue refid, Pointer ppv);
		}

		public static interface AddRefCallback extends Callback {
			void f(Pointer thisPointer);
		}

		public static interface ReleaseCallback extends Callback {
			void f(Pointer thisPointer);
		}

		public static interface DeviceRequestCallback extends Callback {
			int f(Pointer thisPointer, IOUSBDevRequest ppv);
		}

		public IOUSBDeviceInterface320(com.sun.jna.Pointer pointer) {
			super(pointer);
		}

		public IOUSBDeviceInterface320() {
			super();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(
					"reserved", //
					"QueryInterface", 
					"AddRef",  //
					"Release",  //
					"CreateDeviceAsyncEventSource", //	    IOReturn (*CreateDeviceAsyncEventSource)(void *self, CFRunLoopSourceRef *source);
					"GetDeviceAsyncEventSource", //	    CFRunLoopSourceRef (*GetDeviceAsyncEventSource)(void *self);
					"CreateDeviceAsyncPort", //	    IOReturn (*CreateDeviceAsyncPort)(void *self, mach_port_t *port);
					"GetDeviceAsyncPort",//	    mach_port_t (*GetDeviceAsyncPort)(void *self);
					"USBDeviceOpen",//	    IOReturn (*USBDeviceOpen)(void *self);
					"USBDeviceClose",//	    IOReturn (*USBDeviceClose)(void *self);
					"GetDeviceClass",//	    IOReturn (*GetDeviceClass)(void *self, UInt8 *devClass);
					"GetDeviceSubClass",//	    IOReturn (*GetDeviceSubClass)(void *self, UInt8 *devSubClass);
					"GetDeviceProtocol",//	    IOReturn (*GetDeviceProtocol)(void *self, UInt8 *devProtocol);
					"GetDeviceVendor",//	    IOReturn (*GetDeviceVendor)(void *self, UInt16 *devVendor);
					"GetDeviceProduct",//	    IOReturn (*GetDeviceProduct)(void *self, UInt16 *devProduct);
					"GetDeviceReleaseNumber",//	    IOReturn (*GetDeviceReleaseNumber)(void *self, UInt16 *devRelNum);
					"GetDeviceAddress",//	    IOReturn (*GetDeviceAddress)(void *self, USBDeviceAddress *addr);
					"GetDeviceBusPowerAvailable",//	    IOReturn (*GetDeviceBusPowerAvailable)(void *self, UInt32 *powerAvailable);
					"GetDeviceSpeed", //	    IOReturn (*GetDeviceSpeed)(void *self, UInt8 *devSpeed);
					"GetNumberOfConfigurations", //	    IOReturn (*GetNumberOfConfigurations)(void *self, UInt8 *numConfig);
					"GetLocationID",//	    IOReturn (*GetLocationID)(void *self, UInt32 *locationID);
					"GetConfigurationDescriptorPtr", //	    IOReturn (*GetConfigurationDescriptorPtr)(void *self, UInt8 configIndex, IOUSBConfigurationDescriptorPtr *desc);
					"GetConfiguration", //	    IOReturn (*GetConfiguration)(void *self, UInt8 *configNum);
					"SetConfiguration",//	    IOReturn (*SetConfiguration)(void *self, UInt8 configNum);
					"GetBusFrameNumber",//	    IOReturn (*GetBusFrameNumber)(void *self, UInt64 *frame, AbsoluteTime *atTime);
					"ResetDevice",//	    IOReturn (*ResetDevice)(void *self);
					"DeviceRequest",//	    IOReturn (*DeviceRequest)(void *self, IOUSBDevRequest *req);
					"DeviceRequestAsync",//	    IOReturn (*DeviceRequestAsync)(void *self, IOUSBDevRequest *req, IOAsyncCallback1 callback, void *refCon);
					"CreateInterfaceIterator"//				

			);
		}
	}

	// --------------------------------------------------------------------------------
	public interface IOHIDReportCallback extends com.sun.jna.Callback {
		void callback(Pointer context, int result, Pointer sender, int report_type, int report_id, Pointer report, NativeLong report_length);
	}

	// --------------------------------------------------------------------------------
	public interface IOHIDDeviceCallback extends com.sun.jna.Callback {
		void hid_device_removal_callback(Pointer context, int result, Pointer sender, IOHIDDeviceRef dev_ref);
	}

	// --------------------------------------------------------------------------------
	public interface IOHIDManagerLib extends Library {
		IOHIDManagerRef IOHIDManagerCreate(CFAllocatorRef allocator, int options);

		void IOHIDManagerSetDeviceMatching(IOHIDManagerRef manager, CFDictionaryRef matching);

		void IOHIDManagerScheduleWithRunLoop(IOHIDManagerRef manager, CFRunLoopRef runLoop, CFStringRef runLoopMode);

		CFSetRef IOHIDManagerCopyDevices(IOHIDManagerRef manager);

		CFTypeRef IOHIDDeviceGetProperty(IOHIDDeviceRef device, CFStringRef key);

		int IOHIDDeviceOpen(IOHIDDeviceRef device, int options);

		void IOHIDDeviceRegisterInputReportCallback(IOHIDDeviceRef device, Pointer report, NativeLong reportLength, IOHIDReportCallback callback, Pointer context);

		void IOHIDManagerRegisterDeviceRemovalCallback(IOHIDManagerRef manager, IOHIDDeviceCallback callback, Pointer context);

		void IOHIDDeviceScheduleWithRunLoop(IOHIDDeviceRef device, CFRunLoopRef runLoop, CFStringRef runLoopMode);

		void IOHIDDeviceUnscheduleFromRunLoop(IOHIDDeviceRef device, CFRunLoopRef runLoop, CFStringRef runLoopMode);

		CFRunLoopRef CFRunLoopGetMain();

		int IOHIDDeviceClose(IOHIDDeviceRef device, int options);

		int IOHIDDeviceGetReport(IOHIDDeviceRef device, int reportType, NativeLong reportID, ByteBuffer report, NativeLong[] pReportLength);

		int IOHIDDeviceSetReport(IOHIDDeviceRef device, int reportType, NativeLong reportID, ByteBuffer report, NativeLong pReportLength);

		int IOHIDManagerClose(IOHIDManagerRef manager, int options);

		CFArrayRef IOHIDDeviceCopyMatchingElements(IOHIDDeviceRef inIOHIDDeviceRef, CFDictionaryRef inMatchingCFDictRef, int inOptions);

		int IOHIDElementGetType(IOHIDElementRef element);

		int IOHIDElementGetUsagePage(IOHIDElementRef element);

		int IOHIDElementGetUsage(IOHIDElementRef element);

		CFStringRef IOHIDElementGetName(IOHIDElementRef element);

		int IOHIDElementGetReportID(IOHIDElementRef element);

		int IOHIDElementGetReportSize(IOHIDElementRef element);

		int IOHIDElementGetReportCount(IOHIDElementRef element);

		int IOHIDElementGetUnit(IOHIDElementRef element);

		int IOHIDElementGetUnitExponent(IOHIDElementRef element);

		int IOHIDElementGetLogicalMin(IOHIDElementRef element);

		int IOHIDElementGetLogicalMax(IOHIDElementRef element);

		int IOHIDElementGetPhysicalMin(IOHIDElementRef element);

		int IOHIDElementGetPhysicalMax(IOHIDElementRef element);

		boolean IOHIDElementIsVirtual(IOHIDElementRef element);

		boolean IOHIDElementIsRelative(IOHIDElementRef element);

		boolean IOHIDElementIsWrapping(IOHIDElementRef element);

		boolean IOHIDElementIsArray(IOHIDElementRef element);

		boolean IOHIDElementIsNonLinear(IOHIDElementRef element);

		boolean IOHIDElementHasPreferredState(IOHIDElementRef element);

		boolean IOHIDElementHasNullState(IOHIDElementRef element);

		IOHIDDeviceRef IOHIDElementGetDevice(IOHIDElementRef element);

		IOHIDElementRef IOHIDElementGetParent(IOHIDElementRef element);

		CFArrayRef IOHIDElementGetChildren(IOHIDElementRef element);

		int IOHIDElementGetCookie(IOHIDElementRef element);

		int IOHIDElementGetCollectionType(IOHIDElementRef element);

		CFTypeRef IOHIDElementGetProperty(IOHIDElementRef element, CFStringRef key);

		CFMutableDictionaryRef IOServiceMatching(String name);

		int IOServiceGetMatchingService(mach_port_t masterPort, CFMutableDictionaryRef matching, io_iterator_t[] iterator);

		int IOServiceGetMatchingServices(mach_port_t masterPort, CFMutableDictionaryRef matching, io_iterator_t[] iterator);

		io_object_t IOIteratorNext(io_iterator_t iterator);

		//int IOCreatePlugInInterfaceForService(io_object_t z, CFUUID u1, CFUUID u2, IOCFPlugInInterface[] x, int[] y);
		int IOCreatePlugInInterfaceForService(io_object_t ioDeviceObj, CFUUIDRef kIOUSBDeviceUserClientTypeID2, CFUUIDRef kIOCFPlugInInterfaceID2, Memory ioPlugin, int[] score);

		//		0x108d63ca0
		//		0x107ee5988
		//		0x107ee5990

		int IOMasterPort(Pointer p, mach_port_t[] t);

		//int GetDescriptor(IOUSBDeviceRef deviceIntf, byte descType, byte descIndex, byte[] buf, short len, int[] actError);
	}

	// --------------------------------------------------------------------------------
	public static IOHIDManagerRef IOHIDManagerCreate(CFAllocatorRef allocator, int options) {
		return INSTANCE.IOHIDManagerCreate(allocator, options);
	}

	public static void IOHIDManagerSetDeviceMatching(IOHIDManagerRef manager, CFDictionaryRef matching) {
		INSTANCE.IOHIDManagerSetDeviceMatching(manager, matching);
	}

	public static void IOHIDManagerScheduleWithRunLoop(IOHIDManagerRef manager, CFRunLoopRef runLoop, CFStringRef runLoopMode) {
		INSTANCE.IOHIDManagerScheduleWithRunLoop(manager, runLoop, runLoopMode);
	}

	public static CFSetRef IOHIDManagerCopyDevices(IOHIDManagerRef manager) {
		return INSTANCE.IOHIDManagerCopyDevices(manager);
	}

	public static CFTypeRef IOHIDDeviceGetProperty(IOHIDDeviceRef device, CFStringRef key) {
		return INSTANCE.IOHIDDeviceGetProperty(device, key);
	}

	public static int IOHIDDeviceOpen(IOHIDDeviceRef device, int options) {
		return INSTANCE.IOHIDDeviceOpen(device, options);
	}

	public static void IOHIDDeviceRegisterInputReportCallback(IOHIDDeviceRef device, Pointer report, int reportLength, IOHIDReportCallback callback, Pointer context) {
		INSTANCE.IOHIDDeviceRegisterInputReportCallback(device, report, new NativeLong(reportLength), callback, context);
	}

	public static void IOHIDManagerRegisterDeviceRemovalCallback(IOHIDManagerRef manager, IOHIDDeviceCallback callback, Pointer context) {
		INSTANCE.IOHIDManagerRegisterDeviceRemovalCallback(manager, callback, context);
	}

	public static void IOHIDDeviceScheduleWithRunLoop(IOHIDDeviceRef device, CFRunLoopRef runLoop, CFStringRef runLoopMode) {
		INSTANCE.IOHIDDeviceScheduleWithRunLoop(device, runLoop, runLoopMode);
	}

	public static void IOHIDDeviceUnscheduleFromRunLoop(IOHIDDeviceRef device, CFRunLoopRef runLoop, CFStringRef runLoopMode) {
		INSTANCE.IOHIDDeviceUnscheduleFromRunLoop(device, runLoop, runLoopMode);
	}

	public static int IOHIDDeviceClose(IOHIDDeviceRef device, int options) {
		return INSTANCE.IOHIDDeviceClose(device, options);
	}

	public static int IOHIDDeviceGetReport(IOHIDDeviceRef device, int reportType, int reportID, ByteBuffer report, int[] pReportLength) {
		NativeLong[] t = { new NativeLong(pReportLength[0]) };
		int res = INSTANCE.IOHIDDeviceGetReport(device, reportType, new NativeLong(reportID), report, t);
		pReportLength[0] = t[0].intValue();
		return res;
	}

	public static int IOHIDDeviceSetReport(IOHIDDeviceRef device, int reportType, int reportID, ByteBuffer report, int pReportLength) {
		return INSTANCE.IOHIDDeviceSetReport(device, reportType, new NativeLong(reportID), report, new NativeLong(pReportLength));
	}

	public static int IOHIDManagerClose(IOHIDManagerRef manager, int options) {
		return INSTANCE.IOHIDManagerClose(manager, options);
	}

	public static CFArrayRef IOHIDDeviceCopyMatchingElements(IOHIDDeviceRef inIOHIDDeviceRef, CFDictionaryRef inMatchingCFDictRef, int inOptions) {
		return INSTANCE.IOHIDDeviceCopyMatchingElements(inIOHIDDeviceRef, inMatchingCFDictRef, inOptions);
	}

	public static int IOHIDElementGetType(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetType(element);
	}

	public static int IOHIDElementGetUsagePage(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetUsagePage(element);
	}

	public static int IOHIDElementGetUsage(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetUsage(element);
	}

	public static CFStringRef IOHIDElementGetName(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetName(element);
	}

	public static int IOHIDElementGetReportID(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetReportID(element);
	}

	public static int IOHIDElementGetReportSize(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetReportSize(element);
	}

	public static int IOHIDElementGetReportCount(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetReportCount(element);
	}

	public static int IOHIDElementGetUnit(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetUnit(element);
	}

	public static int IOHIDElementGetUnitExponent(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetUnitExponent(element);
	}

	public static int IOHIDElementGetLogicalMin(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetLogicalMin(element);
	}

	public static int IOHIDElementGetLogicalMax(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetLogicalMax(element);
	}

	public static int IOHIDElementGetPhysicalMin(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetPhysicalMin(element);
	}

	public static int IOHIDElementGetPhysicalMax(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetPhysicalMax(element);
	}

	public static boolean IOHIDElementIsVirtual(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementIsVirtual(element);
	}

	public static boolean IOHIDElementIsRelative(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementIsRelative(element);
	}

	public static boolean IOHIDElementIsWrapping(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementIsWrapping(element);
	}

	public static boolean IOHIDElementIsArray(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementIsArray(element);
	}

	public static boolean IOHIDElementIsNonLinear(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementIsNonLinear(element);
	}

	public static boolean IOHIDElementHasPreferredState(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementHasPreferredState(element);
	}

	public static boolean IOHIDElementHasNullState(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementHasNullState(element);
	}

	public static IOHIDDeviceRef IOHIDElementGetDevice(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetDevice(element);
	}

	public static IOHIDElementRef IOHIDElementGetParent(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetParent(element);
	}

	public static CFArrayRef IOHIDElementGetChildren(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetChildren(element);
	}

	public static int IOHIDElementGetCookie(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetCookie(element);
	}

	public static int IOHIDElementGetCollectionType(IOHIDElementRef element) {
		return INSTANCE.IOHIDElementGetCollectionType(element);
	}

	public static CFTypeRef IOHIDElementGetProperty(IOHIDElementRef element, CFStringRef key) {
		return INSTANCE.IOHIDElementGetProperty(element, key);
	}

	public static CFMutableDictionaryRef IOServiceMatching(String name) {
		return INSTANCE.IOServiceMatching(name);
	}

	public static int IOServiceGetMatchingService(mach_port_t masterPort, CFMutableDictionaryRef matching, io_iterator_t[] iterator) {
		return INSTANCE.IOServiceGetMatchingService(masterPort, matching, iterator);
	}

	public static int IOServiceGetMatchingServices(mach_port_t masterPort, CFMutableDictionaryRef matching, io_iterator_t[] iterator) {
		return INSTANCE.IOServiceGetMatchingServices(masterPort, matching, iterator);
	}

	public static io_object_t IOIteratorNext(io_iterator_t iterator) {
		return INSTANCE.IOIteratorNext(iterator);

	}

	public static int IOCreatePlugInInterfaceForService(io_object_t ioDeviceObj, CFUUIDRef kIOUSBDeviceUserClientTypeID2, CFUUIDRef kIOCFPlugInInterfaceID2, Memory ioPlugin, int[] score) {
		return INSTANCE.IOCreatePlugInInterfaceForService(ioDeviceObj, kIOUSBDeviceUserClientTypeID2, kIOCFPlugInInterfaceID2, ioPlugin, score);
	}

	public static int IOMasterPort(Pointer p, mach_port_t[] t) {
		return INSTANCE.IOMasterPort(p, t);
	}

}

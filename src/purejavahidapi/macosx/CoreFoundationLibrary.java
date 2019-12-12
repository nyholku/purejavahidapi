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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.*;

//http://www.philipp.haussleiter.de/2012/09/building-native-macos-apps-with-java/

public class CoreFoundationLibrary {
	private static String m_NativeLibName = "CoreFoundation";
	private static CoreFoundationLibraryInterface INSTANCE = (CoreFoundationLibraryInterface) Native.load(m_NativeLibName, CoreFoundationLibraryInterface.class);
	private static NativeLibrary NINSTANCE = NativeLibrary.getInstance(m_NativeLibName);

	// --------------------------------------------------------------------------------
	public static CFAllocatorRef kCFAllocatorDefault = new CFAllocator_global(NINSTANCE.getGlobalVariableAddress("kCFAllocatorDefault")).value;
	public static CFStringRef kCFRunLoopDefaultMode = new CFStringRef_global(NINSTANCE.getGlobalVariableAddress("kCFRunLoopDefaultMode")).value;

	public static final int kCFRunLoopRunFinished = 1;
	public static final int kCFRunLoopRunStopped = 2;
	public static final int kCFRunLoopRunTimedOut = 3;
	public static final int kCFRunLoopRunHandledSource = 4;

	public static final int kCFNumberSInt32Type = 3;

	public static final int kCFStringEncodingASCII = 1536;
	public static final int kCFStringEncodingUTF8 = 0x08000100;
	public static final int kCFStringEncodingUTF32LE = 469762304;

	public static final class CFMutableDictionaryRef extends PointerType {
		public CFMutableDictionaryRef(Pointer pointer) {
			super(pointer);
		}

		public CFMutableDictionaryRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static final class CFAllocator_global extends Structure {
		public CFAllocatorRef value;

		CFAllocator_global(Pointer pointer) {
			super();
			useMemory(pointer, 0);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("value");
		}
	}

	// --------------------------------------------------------------------------------
	public static final class CFStringRef_global extends Structure {
		public CFStringRef value;

		CFStringRef_global(Pointer pointer) {
			super();
			useMemory(pointer, 0);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("value");
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFAllocatorRef extends PointerType {
		public CFAllocatorRef(Pointer pointer) {
			super(pointer);
		}

		public CFAllocatorRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFStringRef extends PointerType {
		public CFStringRef(Pointer pointer) {
			super(pointer);
		}

		public CFStringRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFSetRef extends PointerType {
		public CFSetRef(Pointer pointer) {
			super(pointer);
		}

		public CFSetRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFRunLoopRef extends PointerType {
		public CFRunLoopRef(Pointer pointer) {
			super(pointer);
		}

		public CFRunLoopRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFTypeRef extends PointerType {
		public CFTypeRef(Pointer pointer) {
			super(pointer);
		}

		public CFTypeRef() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFNumber extends PointerType {
		public CFNumber(Pointer pointer) {
			super(pointer);
		}

		public CFNumber() {
			super();
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFRange extends Structure {
		public NativeLong location;
		public NativeLong length;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("location", "length");
		}

		public CFRange() {
			super();
		}

		public CFRange(long location, long length) {
			super();
			this.location = new NativeLong(location);
			this.length = new NativeLong(length);
		}

		public CFRange(NativeLong location, NativeLong length) {
			super();
			this.location = location;
			this.length = length;
		}

		public ByValue byValue() {
			return new ByValue(location, length);
		}

		public static class ByReference extends CFRange implements com.sun.jna.Structure.ByReference {
		}

		public static class ByValue extends CFRange implements com.sun.jna.Structure.ByValue {
			ByValue(long location, long length) {
				super(location, length);
			}

			ByValue(NativeLong location, NativeLong length) {
				super(location, length);
			}
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFRunLoopSourceRef extends PointerType {
		public CFRunLoopSourceRef(Pointer pointer) {
			super(pointer);
		}

		public CFRunLoopSourceRef() {
			super();
		}

	}

	// --------------------------------------------------------------------------------
	public interface CFRunLoopPerformCallBack extends com.sun.jna.Callback {
		void callback(Pointer context);
	}

	// --------------------------------------------------------------------------------
	public static class CFArrayRef extends PointerType {
		public CFArrayRef(Pointer pointer) {
			super(pointer);
		}

		public CFArrayRef() {
			super();
		}

	}

	// --------------------------------------------------------------------------------
	public static class CFUUID extends Structure {
		public byte[] bytes = new byte[16];

		protected List<String> getFieldOrder() {
			return Arrays.asList("bytes");
		}

		public static class ByValue extends CFUUID implements Structure.ByValue {
		}
	}

	// --------------------------------------------------------------------------------
	public static class CFUUIDRef extends PointerType {
		public CFUUIDRef(Pointer pointer) {
			super(pointer);
		}

		public CFUUIDRef() {
			super();
		}

	}

	// --------------------------------------------------------------------------------
	//	public static class NSMapTableValueCallBacks extends Structure {
	//		public Pointer retain;
	//		public Pointer release;
	//		public Pointer describe;
	//
	//		@Override
	//		protected List getFieldOrder() {
	//			return Arrays.asList("retain", "release", "describe");
	//		}
	//	}
	//		public NSMapTableValueCallBacks() {
	//			super();
	//		}
	//		public NSMapTableValueCallBacks(org.rococoa.cocoa.foundation.NSMapTableValueCallBacks.retain_callback retain, org.rococoa.cocoa.foundation.NSMapTableValueCallBacks.release_callback release, org.rococoa.cocoa.foundation.NSMapTableValueCallBacks.describe_callback describe) {
	//			super();
	//			this.retain = retain;
	//			this.release = release;
	//			this.describe = describe;
	//		}
	//		public ByReference byReference() { return setupClone(new ByReference()); }
	//		public ByValue byValue() { return setupClone(new ByValue()); }
	//		public NSMapTableValueCallBacks clone() { return setupClone(new NSMapTableValueCallBacks()); }
	//		public static class ByReference extends NSMapTableValueCallBacks implements com.sun.jna.Structure.ByReference {}
	//		public static class ByValue extends NSMapTableValueCallBacks implements com.sun.jna.Structure.ByValue {}

	// --------------------------------------------------------------------------------
	CFRunLoopPerformCallBack perform;

	public static class CFRunLoopSourceContext extends Structure {
		public NativeLong version;
		public Pointer info;
		public Pointer retain;
		public Pointer release;
		public Pointer copyDescription;
		public Pointer equal;
		public Pointer hash;
		public Pointer schedule;
		public Pointer cancel;
		public CFRunLoopPerformCallBack perform;

		protected List<String> getFieldOrder() {
			return Arrays.asList("version", "info", "retain", "release", "copyDescription", "equal", "hash", "schedule", "cancel", "perform");
		}
	}

	// --------------------------------------------------------------------------------
	public interface CoreFoundationLibraryInterface extends Library {
		CFRunLoopRef CFRunLoopGetCurrent();

		int CFRunLoopRunInMode(CFStringRef mode, double seconds, boolean returnAfterSourceHandled);

		NativeLong CFSetGetCount(CFSetRef theSet);

		void CFSetGetValues(CFSetRef theSet, Pointer[] values);

		NativeLong CFNumberGetTypeID();

		NativeLong CFGetTypeID(Pointer cf);

		boolean CFNumberGetValue(CFNumber number, int theType, int[] value);

		CFStringRef CFStringCreateWithCString(CFAllocatorRef alloc, String cStr, int encoding);

		NativeLong CFStringGetLength(CFStringRef theString);

		//	CFIndex CFStringGetBytes(CFStringRef theString, CFRange range, CFStringEncoding encoding, UInt8 lossByte, Boolean isExternalRepresentation, UInt8 *buffer, CFIndex maxBufLen, CFIndex *usedBufLen);

		NativeLong CFStringGetBytes(CFStringRef theString, CFRange.ByValue range, int encoding, byte lossByte, boolean isExternalRepresentation, byte[] buffer, long maxBufLen, NativeLong[] usedBufLen);

		void CFRelease(PointerType obj);

		CFTypeRef CFRetain(PointerType obj);

		CFRunLoopSourceRef CFRunLoopSourceCreate(CFAllocatorRef allocator, NativeLong order, CFRunLoopSourceContext context);

		void CFRunLoopAddSource(CFRunLoopRef rl, CFRunLoopSourceRef source, CFStringRef mode);

		CFRunLoopRef CFRunLoopGetMain();

		void CFRunLoopSourceSignal(CFRunLoopSourceRef source);

		void CFRunLoopWakeUp(CFRunLoopRef rl);

		void CFRunLoopStop(CFRunLoopRef rl);

		int CFArrayGetCount(CFArrayRef theArray);

		Pointer CFArrayGetValueAtIndex(CFArrayRef theArray, int index);

		void CFDictionaryAddValue(CFMutableDictionaryRef theDict, CFStringRef key, CFTypeRef value);

		CFUUIDRef CFUUIDCreateFromString(CFAllocatorRef alloc, CFStringRef uuidStr);

		CFUUID.ByValue CFUUIDGetUUIDBytes(CFUUIDRef uuid);

	}

	// --------------------------------------------------------------------------------

	public static CFRunLoopRef CFRunLoopGetCurrent() {
		return INSTANCE.CFRunLoopGetCurrent();
	}

	public static int CFRunLoopRunInMode(CFStringRef mode, double seconds, boolean returnAfterSourceHandled) {
		return INSTANCE.CFRunLoopRunInMode(mode, seconds, returnAfterSourceHandled);
	}

	public static long CFSetGetCount(CFSetRef theSet) {
		return INSTANCE.CFSetGetCount(theSet).longValue();
	}

	public static void CFSetGetValues(CFSetRef theSet, Pointer[] values) {
		INSTANCE.CFSetGetValues(theSet, values);
	}

	public static long CFNumberGetTypeID() {
		return INSTANCE.CFNumberGetTypeID().longValue();
	}

	public static long CFGetTypeID(Pointer cf) {
		return INSTANCE.CFGetTypeID(cf).longValue();
	}

	public static boolean CFNumberGetValue(CFNumber number, int theType, int[] value) {
		return INSTANCE.CFNumberGetValue(number, theType, value);
	}

	public static CFStringRef CFStringCreateWithCString(CFAllocatorRef alloc, String cStr, int encoding) {
		return INSTANCE.CFStringCreateWithCString(alloc, cStr, encoding);
	}

	public static CFStringRef CFSTR(String string) {
		return INSTANCE.CFStringCreateWithCString(null, string, kCFStringEncodingASCII);
	}

	public static long CFStringGetLength(CFStringRef theString) {
		return INSTANCE.CFStringGetLength(theString).longValue();
	}

	public static long CFStringGetBytes(CFStringRef theString, CFRange range, int encoding, byte lossByte, boolean isExternalRepresentation, byte[] buffer, long maxBufLen, long[] usedBufLen) {
		NativeLong[] u = new NativeLong[1];
		long n = INSTANCE.CFStringGetBytes(theString, range.byValue(), encoding, lossByte, isExternalRepresentation, buffer, maxBufLen, u).longValue();
		usedBufLen[0] = u[0].longValue();
		return n;
	}

	public static void CFRelease(PointerType obj) {
		INSTANCE.CFRelease(obj);
	}

	public static CFTypeRef CFRetain(PointerType obj) {
		return INSTANCE.CFRetain(obj);
	}

	public static CFRunLoopSourceRef CFRunLoopSourceCreate(CFAllocatorRef allocator, long order, CFRunLoopSourceContext context) {
		return INSTANCE.CFRunLoopSourceCreate(allocator, new NativeLong(order), context);
	}

	public static void CFRunLoopAddSource(CFRunLoopRef rl, CFRunLoopSourceRef source, CFStringRef mode) {
		INSTANCE.CFRunLoopAddSource(rl, source, mode);
	}

	public static CFRunLoopRef CFRunLoopGetMain() {
		return INSTANCE.CFRunLoopGetMain();
	}

	public static void CFRunLoopSourceSignal(CFRunLoopSourceRef source) {
		INSTANCE.CFRunLoopSourceSignal(source);
	}

	public static void CFRunLoopWakeUp(CFRunLoopRef rl) {
		INSTANCE.CFRunLoopWakeUp(rl);
	}

	public static void CFRunLoopStop(CFRunLoopRef rl) {
		INSTANCE.CFRunLoopStop(rl);
	}

	public static int CFArrayGetCount(CFArrayRef theArray) {
		return INSTANCE.CFArrayGetCount(theArray);
	}

	public static Pointer CFArrayGetValueAtIndex(CFArrayRef theArray, int index) {
		return INSTANCE.CFArrayGetValueAtIndex(theArray, index);
	}

	public static void CFDictionaryAddValue(CFMutableDictionaryRef theDict, CFStringRef key, CFTypeRef value) {
		INSTANCE.CFDictionaryAddValue(theDict, key, value);
	}

	public static CFUUIDRef CFUUIDCreateFromString(CFAllocatorRef alloc, CFStringRef uuidStr) {
		return INSTANCE.CFUUIDCreateFromString(alloc, uuidStr);
	}

	public static CFUUID.ByValue CFUUIDGetUUIDBytes(CFUUIDRef uuid) {
		return INSTANCE.CFUUIDGetUUIDBytes(uuid);
	}

}

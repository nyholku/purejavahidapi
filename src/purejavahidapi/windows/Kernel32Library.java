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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import purejavahidapi.windows.WinDef.HANDLE;
import purejavahidapi.windows.WinDef.HMODULE;
import purejavahidapi.windows.WinDef.OVERLAPPED;
import purejavahidapi.windows.WinDef.SECURITY_ATTRIBUTES;

public class Kernel32Library {
	static Kernel32Interface INSTANCE = (Kernel32Interface) Native.loadLibrary("kernel32", Kernel32Interface.class, W32APIOptions.UNICODE_OPTIONS);

	public static final int ERROR_INSUFFICIENT_BUFFER = 122;
	public static final int ERROR_INVALID_USER_BUFFER = 1784;
	public static final int ERROR_NO_MORE_ITEMS = 259;
	public static final int ERROR_INVALID_DATA = 13;
	public static final int MAXDWORD = 0xFFFFFFFF;
	public static final int STATUS_WAIT_0 = 0x00000000;
	public static final int STATUS_ABANDONED_WAIT_0 = 0x00000080;
	public static final int WAIT_ABANDONED = (STATUS_ABANDONED_WAIT_0) + 0;
	public static final int WAIT_ABANDONED_0 = (STATUS_ABANDONED_WAIT_0) + 0;
	public static final int WAIT_OBJECT_0 = ((STATUS_WAIT_0) + 0);
	public static final int WAIT_FAILED = 0xFFFFFFFF;
	public static final int INFINITE = 0xFFFFFFFF;
	public static final int WAIT_TIMEOUT = 258; //
	public static final int GENERIC_READ = 0x80000000;
	public static final int GENERIC_WRITE = 0x40000000;
	public static final int GENERIC_EXECUTE = 0x20000000;
	public static final int GENERIC_ALL = 0x10000000;
	public static final int CREATE_NEW = 1;
	public static final int CREATE_ALWAYS = 2;
	public static final int OPEN_EXISTING = 3;
	public static final int OPEN_ALWAYS = 4;
	public static final int TRUNCATE_EXISTING = 5;

	public static final int FILE_SHARE_READ = 1;
	public static final int FILE_SHARE_WRITE = 2;
	public static final int FILE_FLAG_OVERLAPPED = 0x40000000;

	public static final int ERROR_OPERATION_ABORTED = 995;

	public static final int ERROR_IO_INCOMPLETE = 996;

	public static final int ERROR_IO_PENDING = 997;

	public static final int ERROR_BROKEN_PIPE = 109;

	public static final int ERROR_MORE_DATA = 234;

	public static final int ERROR_FILE_NOT_FOUND = 2;

	public static final int ERROR_DEVICE_NOT_CONNECTED = 1167;

	public static final int IOCTL_HID_GET_FEATURE = 0xb0192;

	public static final int IOCTL_HID_SET_FEATURE = 0xb0191;

	public interface Kernel32Interface extends StdCallLibrary {

		HANDLE CreateFile(String name, int access, int sharing, SECURITY_ATTRIBUTES security, int create, int attribs, Pointer template);

		boolean CloseHandle(HANDLE hFile);

		boolean CancelIo(HANDLE hFile);

		boolean CancelIoEx(HANDLE hFile, OVERLAPPED lpOverlapped);

		int WaitForSingleObject(HANDLE hHandle, int dwMilliseconds);

		boolean ResetEvent(HANDLE hEvent);

		boolean WriteFile(HANDLE hFile, Pointer buf, int wrn, int[] nwrtn, OVERLAPPED lpOverlapped);

		boolean ReadFile(HANDLE hFile, Pointer buf, int rdn, int[] nrd, OVERLAPPED lpOverlapped);

		boolean GetOverlappedResult(HANDLE hFile, OVERLAPPED lpOverlapped, int[] lpNumberOfBytesTransferred, boolean bWait);

		HMODULE GetModuleHandle(String name);

		boolean DeviceIoControl(HANDLE hDevice, int dwIoControlCode, Pointer lpInBuffer, int nInBufferSize, Pointer lpOutBuffer, int nOutBufferSize, int[] lpBytesReturned, OVERLAPPED lpOverlapped);
		
		HANDLE CreateEvent(SECURITY_ATTRIBUTES lpEventAttributes, boolean bManualReset, boolean bInitialState, String lpName);
	}

	public static HANDLE CreateFile(String name, int access, int sharing, SECURITY_ATTRIBUTES security, int create, int attribs, Pointer template) {
		HANDLE h = INSTANCE.CreateFile(name, access, sharing, security, create, attribs, template);
		return h;
	}

	public static boolean CloseHandle(HANDLE hFile) {
		return INSTANCE.CloseHandle(hFile);
	}

	public static int GetLastError() {
		return Native.getLastError();
	}

	public static boolean CancelIo(HANDLE hFile) {
		return INSTANCE.CancelIo(hFile);
	}

	public static boolean CancelIoEx(HANDLE hFile, OVERLAPPED overlapped) {
		return INSTANCE.CancelIoEx(hFile, overlapped);
	}

	public static int WaitForSingleObject(HANDLE hHandle, int dwMilliseconds) {
		return INSTANCE.WaitForSingleObject(hHandle, dwMilliseconds);
	}

	public static boolean ResetEvent(HANDLE hEvent) {
		return INSTANCE.ResetEvent(hEvent);
	}

	// This can be used with synchronous as well as overlapped writes

	public static boolean WriteFile(HANDLE hFile, Pointer buf, int wrn, int[] nwrtn, OVERLAPPED overlapped) {
		return INSTANCE.WriteFile(hFile, buf, wrn, nwrtn, overlapped);
	}

	public static boolean ReadFile(HANDLE hFile, Pointer buf, int rdn, int[] nrd, OVERLAPPED overlapped) {
		return INSTANCE.ReadFile(hFile, buf, rdn, nrd, overlapped);
	}

	public static boolean GetOverlappedResult(HANDLE hFile, OVERLAPPED ovl, int[] ntfrd, boolean wait) {
		return INSTANCE.GetOverlappedResult(hFile, ovl, ntfrd, wait);
	}

	public static HMODULE GetModuleHandle(String name) {
		return INSTANCE.GetModuleHandle(name);
	}

	public static boolean DeviceIoControl(HANDLE hDevice, int dwIoControlCode, Pointer lpInBuffer, int nInBufferSize, Pointer lpOutBuffer, int nOutBufferSize, int[] lpBytesReturned, OVERLAPPED lpOverlapped) {
		return INSTANCE.DeviceIoControl(hDevice, dwIoControlCode, lpInBuffer, nInBufferSize, lpOutBuffer, nOutBufferSize, lpBytesReturned, lpOverlapped);
	}
	
	public static HANDLE CreateEvent(SECURITY_ATTRIBUTES lpEventAttributes, boolean bManualReset, boolean bInitialState, String lpName) {
		HANDLE h = INSTANCE.CreateEvent(lpEventAttributes, bManualReset, bInitialState, lpName);
		return h;
	}
}

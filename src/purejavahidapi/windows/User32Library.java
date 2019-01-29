/*
 * Copyright (c) 2016, Kustaa Nyholm / SpareTimeLabs
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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import purejavahidapi.windows.WinDef.ATOM;
import purejavahidapi.windows.WinDef.HANDLE;
import purejavahidapi.windows.WinDef.HBRUSH;
import purejavahidapi.windows.WinDef.HCURSOR;
import purejavahidapi.windows.WinDef.HICON;
import purejavahidapi.windows.WinDef.HINSTANCE;
import purejavahidapi.windows.WinDef.HMENU;
import purejavahidapi.windows.WinDef.HWND;
import purejavahidapi.windows.WinDef.LPARAM;
import purejavahidapi.windows.WinDef.LPVOID;
import purejavahidapi.windows.WinDef.LRESULT;
import purejavahidapi.windows.WinDef.POINT;
import purejavahidapi.windows.WinDef.PVOID;
import purejavahidapi.windows.WinDef.WPARAM;

@SuppressWarnings("serial")
public class User32Library {
	static User32Interface INSTANCE = (User32Interface) Native.loadLibrary("user32", User32Interface.class, W32APIOptions.UNICODE_OPTIONS);

    public static final int WS_EX_TOPMOST = 0x00000008;
    public static final int DEVICE_NOTIFY_WINDOW_HANDLE = 0x00000000;

    public static final int WM_CREATE = 0x0001;
    public static final  int WM_SIZE = 0x0005;
    public static final  int WM_DESTROY = 0x0002;
    public static final int WM_DEVICECHANGE = 0x0219;

	
	public static class HDEVNOTIFY extends PVOID {
		public HDEVNOTIFY() {

		}

		public HDEVNOTIFY(Pointer p) {
			super(p);
		}
	}

	public static class WNDCLASSEX extends Structure {

		public static class ByReference extends WNDCLASSEX implements Structure.ByReference {
		}

		public WNDCLASSEX() {
		}

		public WNDCLASSEX(Pointer memory) {
			super(memory);
			read();
		}

		public int cbSize = this.size();

		public int style;

		public Callback lpfnWndProc;

		public int cbClsExtra;

		public int cbWndExtra;

		/** The h instance. */
		public HINSTANCE hInstance;

		public HICON hIcon;

		public HCURSOR hCursor;

		public HBRUSH hbrBackground;

		public String lpszMenuName;

		public WString lpszClassName;

		public HICON hIconSm;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "cbSize", "style", "lpfnWndProc", "cbClsExtra", "cbWndExtra", "hInstance", "hIcon", "hCursor", "hbrBackground", "lpszMenuName", "lpszClassName", "hIconSm" });
		}
	}

	public static class MSG extends Structure {
		public HWND hWnd;
		public int message;
		public WPARAM wParam;
		public LPARAM lParam;
		public int time;
		public POINT pt;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "hWnd", "message", "wParam", "lParam", "time", "pt" });
		}
	}

	interface User32Interface extends StdCallLibrary {
		ATOM RegisterClassEx(WNDCLASSEX lpwcx);

		HWND CreateWindowEx(int dwExStyle, String lpClassName, String lpWindowName, int dwStyle, int x, int y, int nWidth, int nHeight, HWND hWndParent, HMENU hMenu, HINSTANCE hInstance, LPVOID lpParam);

		HDEVNOTIFY RegisterDeviceNotification(HANDLE hRecipient, Structure notificationFilter, int Flags);

		int GetMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin, int wMsgFilterMax);

		boolean TranslateMessage(MSG msg);

		LRESULT DispatchMessage(MSG msg);

		boolean UnregisterDeviceNotification(HDEVNOTIFY arg0);

		boolean UnregisterClass(String arg0, HINSTANCE arg1);
		
		boolean UnregisterClass(WString arg0, HINSTANCE arg1);
		
		boolean UnregisterClass(int arg0, HINSTANCE arg1);

		boolean DestroyWindow(HWND hWnd);
		
	    void PostQuitMessage(int nExitCode);
	    
	    LRESULT DefWindowProc(HWND hWnd, int Msg, WPARAM wParam, LPARAM lParam);

	}

	public static ATOM RegisterClassEx(WNDCLASSEX lpwcx) {
		return INSTANCE.RegisterClassEx(lpwcx);
	}

	public static HWND CreateWindowEx(int dwExStyle, String lpClassName, String lpWindowName, int dwStyle, int x, int y, int nWidth, int nHeight, HWND hWndParent, HMENU hMenu, HINSTANCE hInstance, LPVOID lpParam) {
		return INSTANCE.CreateWindowEx(dwExStyle, lpClassName, lpWindowName, dwStyle, x, y, nWidth, nHeight, hWndParent, hMenu, hInstance, lpParam);
	}

	public static HDEVNOTIFY RegisterDeviceNotification(HANDLE hRecipient, Structure notificationFilter, int Flags){
		return INSTANCE.RegisterDeviceNotification( hRecipient,  notificationFilter,  Flags);
	}

	public static int GetMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin, int wMsgFilterMax){
		return INSTANCE.GetMessage( lpMsg,  hWnd,  wMsgFilterMin,  wMsgFilterMax);
	}

	public static boolean TranslateMessage(MSG msg){
		return INSTANCE.TranslateMessage( msg);
	}

	public static LRESULT DispatchMessage(MSG msg){
		return INSTANCE.DispatchMessage( msg);
	}

	public static boolean UnregisterDeviceNotification(HDEVNOTIFY arg0){
		return INSTANCE.UnregisterDeviceNotification(arg0);
	}

	public static boolean UnregisterClass(String arg0, HINSTANCE arg1){
		return INSTANCE.UnregisterClass( arg0,  arg1);
	}
	
	public static boolean UnregisterClass(WString arg0, HINSTANCE arg1){
		return INSTANCE.UnregisterClass( arg0,  arg1);
	}
	
	public static boolean UnregisterClass(ATOM arg0, HINSTANCE arg1){
		return INSTANCE.UnregisterClass( arg0.intValue(),  arg1);
	}

	public static boolean DestroyWindow(HWND hWnd){
		return INSTANCE.DestroyWindow( hWnd);
	}

	public static void PostQuitMessage(int nExitCode) {
		INSTANCE.PostQuitMessage(nExitCode);
	}

	public static LRESULT DefWindowProc(HWND hWnd, int Msg, WPARAM wParam, LPARAM lParam) {
		return INSTANCE.DefWindowProc( hWnd,  Msg,  wParam,  lParam);
	}

}
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

import purejavahidapi.windows.SetupApiLibrary.GUID;

import com.sun.jna.FromNativeContext;
import com.sun.jna.IntegerType;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public class WinDef {
	public static final int DBT_DEVICEREMOVECOMPLETE = 0x8004;

	public static final int DBT_DEVTYP_DEVICEINTERFACE = 0x00000005;
	public static final GUID GUID_DEVINTERFACE_USB_DEVICE = new GUID(0xA5DCBF10, 0x6530, 0x11D2, 0x90,0x1F, 0x00, 0xC0, 0x4F, 0xB9, 0x51, 0xED);

	public static HANDLE INVALID_HANDLE_VALUE = new HANDLE(invalidPointerValue());
	public static HANDLE NULL = new HANDLE(Pointer.createConstant(0));

	private static Pointer invalidPointerValue() {
		return Pointer.createConstant(Pointer.SIZE == 8 ? -1 : 0xFFFFFFFFL);
	}

	public static class PVOID extends PointerType {

		public PVOID() {
			super();
		}

		public PVOID(Pointer pointer) {
			super(pointer);
		}
	}

	public static class LPVOID extends PointerType {
		public LPVOID() {
			super();
		}

		public LPVOID(Pointer p) {
			super(p);
		}
	}

	public static class HMENU extends HANDLE {
		public HMENU() {

		}

		public HMENU(Pointer p) {
			super(p);
		}
	}

	public static class HINSTANCE extends HANDLE {

	}

	public static class HMODULE extends HINSTANCE {

	}

	public static class HWND extends HANDLE {
		public HWND() {

		}

		public HWND(Pointer p) {
			super(p);
		}
	}

	public static class LONG_PTR extends IntegerType {
        public LONG_PTR() {
            this(0);
        }

        public LONG_PTR(long value) {
            super(Pointer.SIZE, value);
        }

        public Pointer toPointer() {
            return Pointer.createConstant(longValue());
        }
    }
	
	public static class LPARAM extends LONG_PTR {
		public LPARAM() {
			this(0);
		}

		public LPARAM(long value) {
			super(value);
		}
	}

	public static class LRESULT extends LONG_PTR {
		public LRESULT() {
			this(0);
		}

		public LRESULT(long value) {
			super(value);
		}
	}

	public static class UINT_PTR extends IntegerType {

		public UINT_PTR() {
			super(Pointer.SIZE);
		}

		public UINT_PTR(long value) {
			super(Pointer.SIZE, value, true);
		}

		public Pointer toPointer() {
			return Pointer.createConstant(longValue());
		}
	}

	public static class WPARAM extends UINT_PTR {
		public WPARAM() {
			this(0);
		}

		public WPARAM(long value) {
			super(value);
		}
	}

	public static class WORD extends IntegerType {
		public static final int SIZE = 2;

		public WORD() {
			this(0);
		}

		public WORD(long value) {
			super(SIZE, value, true);
		}
	}

	public static class ATOM extends WORD {
		public ATOM() {
			this(0);
		}

		public ATOM(long value) {
			super(value);
		}
	}

	public static class HICON extends HANDLE {
		public HICON() {

		}

		public HICON(HANDLE handle) {
			this(handle.getPointer());
		}

		public HICON(Pointer p) {
			super(p);
		}
	}

	public static class HCURSOR extends HICON {
		public HCURSOR() {

		}

		public HCURSOR(Pointer p) {
			super(p);
		}
	}

	public static class HBRUSH extends HANDLE {
		public HBRUSH() {

		}

		public HBRUSH(Pointer p) {
			super(p);
		}
	}

	public static class POINT extends Structure {
		public int x;
		public int y;

		public POINT() {
			super();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("x", "y");

		}
	}

	public static class DEV_BROADCAST_HDR extends Structure {
		public int dbch_size = size();

		public int dbch_devicetype;

		public int dbch_reserved;

		public DEV_BROADCAST_HDR() {
			super();
		}

		public DEV_BROADCAST_HDR(long pointer) {
			this(new Pointer(pointer));
		}

		public DEV_BROADCAST_HDR(Pointer memory) {
			super(memory);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dbch_size", "dbch_devicetype", "dbch_reserved");

		}
	}

	public static class DEV_BROADCAST_DEVICEINTERFACE extends Structure {
		public int dbcc_size;
		public int dbcc_devicetype;
		public int dbcc_reserved;
		public GUID dbcc_classguid;
		public char[] dbcc_name = new char[1];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dbcc_size", "dbcc_devicetype", "dbcc_reserved", "dbcc_classguid", "dbcc_name");

		}

		public DEV_BROADCAST_DEVICEINTERFACE() {
			super();
		}

		public String get_dbcc_name() {
			return new String(dbcc_name);
		}
		
		public DEV_BROADCAST_DEVICEINTERFACE(Pointer memory) {
            super(memory);
            this.dbcc_size = (Integer) this.readField("dbcc_size");
            int len = 1 + this.dbcc_size - size();
            this.dbcc_name = new char[len];
            read();
        }
	}

	public static class HANDLE extends PointerType {
        private boolean immutable;

        public HANDLE() {
        }

        public HANDLE(Pointer p) {
            setPointer(p);
            immutable = true;
        }

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (INVALID_HANDLE_VALUE.equals(o)) {
                return INVALID_HANDLE_VALUE;
            }
            return o;
        }

        @Override
        public void setPointer(Pointer p) {
            if (immutable) {
                throw new UnsupportedOperationException("immutable reference");
            }

            super.setPointer(p);
        }

        @Override
        public String toString() {
            return String.valueOf(getPointer());
        }
    }

	public static class OVERLAPPED extends Structure {

		private static boolean TRACE;

		public Pointer Internal;

		public Pointer InternalHigh;

		public int Offset;

		public int OffsetHigh;

		public HANDLE hEvent;

		@Override
		protected List getFieldOrder() {

			return Arrays.asList("Internal",//

					"InternalHigh",//

					"Offset",//

					"OffsetHigh",//

					"hEvent"//

			);

		}

		public OVERLAPPED() {

			setAutoSynch(false);

		}

		public String toString() {

			return String.format(//

					"[Offset %d OffsetHigh %d hEvent %s]",//

					Offset, OffsetHigh, hEvent.toString());

		}

	}

	public static class SECURITY_ATTRIBUTES extends Structure {
		public int nLength;
		public Pointer lpSecurityDescriptor;
		public boolean bInheritHandle;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("nLength",//
					"lpSecurityDescriptor",//
					"bInheritHandle"//
			);
		}
	}

	public interface WindowProc extends StdCallCallback {
		LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam);
	}
}

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
package purejavahidapi.linux;

public class IOCtl {
	public static final int _IOC_NONE = 0;
	public static final int _IOC_WRITE = 1;
	public static final int _IOC_READ = 2;

	static final int IOCPARM_MASK = 0x1fff;
	static final int IOC_VOID = 0x20000000;
	static final int IOC_OUT = 0x40000000;
	static final int IOC_IN = 0x80000000;
	static final int IOC_INOUT = (IOC_IN | IOC_OUT);
	static final int IOC_DIRMASK = 0xe0000000;

	public static int IOCPARM_LEN(int x) {
		return (((x) >> 16) & IOCPARM_MASK);
	}

	public static int IOCBASECMD(int x) {
		return ((x) & ~IOCPARM_MASK);
	}

	public static int IOCGROUP(int x) {
		return (((x) >> 8) & 0xff);
	}

	static public int sizeof(int x) { 
		return 8; // does this work for 32 bit Linux too?
	}

	public static int _IOC(int inout, int group, int num, int len) {
		return ((inout << 30)  | ((group) << 8) | (num) | ((len & IOCPARM_MASK) << 16));
	}

	public static int _IO(int g, int n) {
		return _IOC(IOC_VOID, (g), (n), 0);
	}

	public static int _IOR(int g, int n, int t) {
		return _IOC(IOC_OUT, (g), (n), sizeof(t));
	}

	public static int _IOW(int g, int n, int t) {
		return _IOC(IOC_IN, (g), (n), sizeof(t));
	}

	public static int _IOWR(int g, int n, int t) {
		return _IOC(IOC_INOUT, (g), (n), sizeof(t));
	}

}

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
package purejavahidapi.demo;

import java.util.List;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

public class PureJavaHidApiDemo {
	// To run this code type at command prompt the following:
	// java -classpath purejavahidapi.jar:../lib/jna-4.0.0.jar purejavahidapi.demo.PureJavaHidApiDemo
	public static void main(String[] args) {
		try {
			System.out.println("PureJavaHidApi Demo!");
			System.out.println();
			System.out.println("This program opens one of the HID devices it knowns about");
			System.out.println("and attaches an input listener that outputs the report data ");
			System.out.println("to the console if it differs from previous report.");
			System.out.println();
			System.out.println("This program automatically exits in ten seconds.");
			System.out.println();

			List<HidDeviceInfo> list = PureJavaHidApi.enumerateDevices(0x0000,0x0000);
			String path = null;
			for (HidDeviceInfo info : list) {
				System.out.printf("VID = 0x%04X PID = 0x%04X Manufacturer = %s Product = %s Path = %s\n", //
						info.getVendorId(), //
						info.getProductId(), //
						info.getManufacturerString(), //
						info.getProductString(), //
						info.getPath());
				if (info.getVendorId() == 0x0810 && info.getProductId() == 0x0005)
					path = info.getPath();
				if (info.getVendorId() == 0x0461 && info.getProductId() == 0x4D22)
					path = info.getPath();
				if (info.getVendorId() == 0x0461 && info.getProductId() == 0x4D22)
					path = info.getPath();
				if (info.getVendorId() == 0x1D50 && info.getProductId() == 0x6020)
					path = info.getPath();
			}
			System.out.println();

			if (path != null) {
				System.out.println("Opening HID devie " + path);
				final long[] t0 = { System.nanoTime() };
				final long t00 = t0[0];
				//				final int[] index = { 0 };
				//				final double[] trace = new double[100 * 2];
				HidDevice device = PureJavaHidApi.openDevice(path);
				if (true)
					device.setInputReportListener(new InputReportListener() {
						byte[] prevBytes = new byte[64];

						@Override
						public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {
							loop: for (int i = 0; i < reportLength; i++) {
								if (reportData[i] != prevBytes[i]) {
									for (int j = 0; j < reportLength; j++) {
										System.out.printf("%02X ", reportData[j]);
										prevBytes[j] = reportData[j];
									}
									System.out.println();
									break loop;
								}
							}

							long t1 = System.nanoTime();
							double dt = (t1 - t0[0]) / 100000 / 10.0;
							double t = (t1 - t00) / 100000 / 10.0;
							t0[0] = t1;
						}
					});
				System.out.println();
				if (false)
					while (true) {
						//System.out.println("setOutputReport");
						byte[] bytes = new byte[64];
						for (int i = 0; i < 64; i++)
							bytes[i] = (byte) i;
						device.setOutputReport((byte) 0, bytes, 64);
						Thread.sleep(10);
					}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				device.close();
			}
			System.out.println("Exiting!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

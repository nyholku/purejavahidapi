package purejavahidapi.examples;

import java.util.List;

import purejavahidapi.*;

public class Example2 {
	volatile static boolean deviceOpen = false;

	public static void main(String[] args) {
		try {

			while (true) {
				// System.exit(0);
				HidDeviceInfo devInfo = null;
				if (!deviceOpen) {
					System.out.println("scanning");
					List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
					for (HidDeviceInfo info : devList) {
						// if (info.getVendorId() == (short) 0x16C0 &&
						// info.getProductId() == (short) 0x05DF) {
						//if (info.getVendorId() == (short) 0x16C0 && info.getProductId() == (short) 0x0a99) {
						if (info.getVendorId() == (short) 0x1D50 && info.getProductId() == (short) 0x6020) {
							devInfo = info;
							break;
						}
					}
					if (devInfo == null) {
						System.out.println("device not found");
						Thread.sleep(1000);
					} else {
						System.out.println("device found '" + devInfo.getProductString() + "'");
						if (true) {
							deviceOpen = true;
							final HidDevice dev = PureJavaHidApi.openDevice(devInfo);

							dev.setDeviceRemovalListener(new DeviceRemovalListener() {
								@Override
								public void onDeviceRemoval(HidDevice source) {
									System.out.println("device removed");
									deviceOpen = false;

								}
							});
							dev.setInputReportListener(new InputReportListener() {
								@Override
								public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
									if (2 == 2) {
										System.out.print(".");
										return;
									}
									System.out.printf("onInputReport: id %d len %d data ", Id, len);
									for (int i = 0; i < len; i++)
										System.out.printf("%02X ", data[i]);
									System.out.println();
								}
							});

							new Thread(new Runnable() {
								@Override
								public void run() {
									while (true) {
										try {
											Thread.currentThread().sleep(1000);
											System.out.println();
											System.out.println("Sending reset");
											for (int i = 0; i < 10; i++) {
												byte[] cmd = new byte[64];
												cmd[0] = (byte) 0xFE;
												cmd[1] = (byte) 0xED;
												cmd[2] = (byte) 0xC0;
												cmd[3] = (byte) 0xDE;
												System.out.println("SEND");
												dev.setOutputReport((byte) 0, cmd, cmd.length);
											}
											Thread.currentThread().sleep(1000);

											deviceOpen = false;
											dev.close();
											break;
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										/*
										while (false) {
											byte[] data = new byte[132];
											data[0] = 1;
											int len = 0;
											if (((len = dev.getFeatureReport(data, data.length)) >= 0) && true) {
												int Id = data[0];
												System.out.printf("getFeatureReport: id %d len %d data ", Id, len);
												for (int i = 0; i < data.length; i++)
													System.out.printf("%02X ", data[i]);
												System.out.println();
											}
										
										}
										*/
									}
								}
							}).start();

							Thread.sleep(2000);
							//dev.close();
							//deviceOpen = false;
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}

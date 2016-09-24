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
							if (info.getVendorId() == (short) 0x16D0 && info.getProductId() == (short) 0x044B) {
							devInfo = info;
							break;
						}
					}
					if (devInfo == null) {
						System.out.println("device not found");
						Thread.sleep(1000);
					} else {
						System.out.println("device found");
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

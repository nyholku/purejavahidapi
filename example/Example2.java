import java.util.List;

import purejavahidapi.*;

public class Example2 {

	public static void main(String[] args) {
		try {

			//System.exit(0);

			List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
			HidDeviceInfo devInfo = null;
			for (HidDeviceInfo info : devList) {
				if (info.getVendorId() == (short) 0x16C0 && info.getProductId() == (short) 0x05DF) {
					devInfo = info;
					break;
				}
			}
			if (devInfo == null)
				System.err.println("device not found");
			else {
				final HidDevice dev = PureJavaHidApi.openDevice(devInfo.getPath());
				dev.setDeviceRemovalListener(new DeviceRemovalListener() {
					@Override
					public void onDeviceRemoval(HidDevice source) {
						System.out.println("onDeviceRemoval");
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
							if (((len = dev.getFeatureReport(data, data.length)) >= 0)) {
								int Id=data[0];
								System.out.printf("getFeatureReport: id %d len %d data ", Id, len);
								for (int i = 0; i < data.length; i++)
									System.out.printf("%02X ", data[i]);
								System.out.println();
							}
						}

					}
				}).run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

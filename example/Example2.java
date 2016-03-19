import java.util.List;

import purejavahidapi.*;

public class Example2 {

	public static void main(String[] args) {
		try {
			List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
			HidDeviceInfo devInfo = null;
			for (HidDeviceInfo info : devList) {
				if (info.getVendorId() == (short) 0x0810 && info.getProductId() == (short) 0x0005) {
					devInfo = info;
					break;
				}
			}
			if (devInfo == null)
				System.err.println("device not found");
			else {
				HidDevice dev = PureJavaHidApi.openDevice(devInfo.getPath());
				dev.setInputReportListener(new InputReportListener() {
					@Override
					public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
						System.out.printf("onInputReport: id %d len %d data ", Id, len);
						for (int i = 0; i < len; i++)
							System.out.printf("%02X ", data[i]);
						System.out.println();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

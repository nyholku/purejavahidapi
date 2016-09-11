package purejavahidapi.shared;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;

abstract public class Backend {
	private HashMap<String, HidDevice> m_OpenDevices = new HashMap<String, HidDevice>();

	public abstract void init();

	public abstract void cleanup();

	public abstract List<HidDeviceInfo> enumerateDevices();

	public abstract HidDevice openDevice(HidDeviceInfo path) throws IOException;

	public void removeDevice(String deviceId) {
		m_OpenDevices.remove(deviceId);
	}

	public void addDevice(String deviceId, HidDevice device) {
		m_OpenDevices.put(deviceId, device);
	}

	public HidDevice getDevice(String deviceId) {
		return m_OpenDevices.get(deviceId);
	}

}
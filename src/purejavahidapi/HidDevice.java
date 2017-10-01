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
package purejavahidapi;

/**
 * Instances of HidDevice represent a single physical USB HID device that has
 * been opened for communication.
 * <p>
 * If the {@link HidDevice#close()} has been called for an object then no more
 * call should be made to any of the methods of that object and attempts to do
 * that will result in IllegalState exception being thrown.
 * <p>
 * Each HidDevice instance creates an internal background thread to perform some
 * work in the background. This thread may keep an application that uses
 * PureJavaHidApi from exiting if the device is not closed before the
 * application tries to exit.
 * <p>
 * 
 * @author nyholku
 *
 */

abstract public class HidDevice {
	protected boolean m_Open;
	protected InputReportListener m_InputReportListener;
	protected DeviceRemovalListener m_DeviceRemovalListener;
	protected HidDeviceInfo m_HidDeviceInfo;

	/**
	 * This method sets the input report listener for this device.
	 * <p>
	 * There can be only one input report listener at a time but it is possible
	 * to re-set the listener.
	 * <p>
	 * Setting the input report listener is the only way to receive input
	 * reports from the device.
	 * <p>
	 * The {@link InputReportListener#onInputReport} method is called from an
	 * internal (to the device instance) background thread and it is expected
	 * that the listener will process the data as quickly as possible and not do
	 * any length process in the callback.
	 * <p>
	 * 
	 * @param listener
	 *            the listener object or null to un-set
	 */
	public void setInputReportListener(InputReportListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_InputReportListener = listener;
	}

	/**
	 * This method returns the input report listener for this device
	 * 
	 * @return the device removal listener
	 */
	public InputReportListener getInputReportListener() {
		return m_InputReportListener;
	}

	/**
	 * This method sends an output report to the device.
	 * <p>
	 * If numbered reports are used (see USB HID specification for explanation
	 * about numbered reports) the reportID needs to be specified otherwise pass
	 * zero there.
	 * <p>
	 * This method may or may not block.
	 * <p>
	 * The method returning is no guarantee that the data has been physically
	 * transmitted from the host to the device.
	 * <p>
	 * The method returns the actual number of bytes successfully scheduled to
	 * be sent to the device.
	 * <p>
	 * As of this writing it is unclear under what circumstances something else
	 * than 'length' number of bytes could be returned as well as what happens
	 * if the report length does not match what the device expects.
	 * <p>
	 * 
	 * @param reportID
	 *            the report number if numbered reports are used else pass 0
	 * @param data
	 *            a byte array containing the data to be sent
	 * @param length
	 *            the number of bytes to send from the data array
	 * @return number bytes actually sent or -1 if the send failed
	 */

	abstract public int setOutputReport(byte reportID, byte[] data, int length);

	/**
	 * This method sends a feature report to the device.
	 * <p>
	 * See USB HID specification to learn more about feature reports.
	 * <p>
	 * This method may or may not block.
	 * <p>
	 * The method returning is no guarantee that the data has been physically
	 * transmitted from the host to the device.
	 * <p>
	 * The method returns the actual number of bytes successfully scheduled to
	 * be sent to the device.
	 * <p>
	 * As of this writing it is unclear under what circumstances something else
	 * than 'length' number of bytes could be returned as well as what happens
	 * if the report length does not match what the device expects.
	 * <p>
	 * 
	 * @param data
	 *            a byte array containing the data to be sent
         * @param reportId
         *            a byte specifying the report ID to send
	 * @param length
	 *            the number of bytes to send from the data array
	 * @return number bytes actually sent or -1 if the call failed
	 * 
	 */
        abstract public int setFeatureReport(byte reportId, byte[] data, int length);

	/**
	 * This method sends a feature report to the device.
	 * <p>
	 * See USB HID specification to learn more about feature reports.
	 * <p>
	 * This method may or may not block.
	 * <p>
	 * The method returning is no guarantee that the data has been physically
	 * transmitted from the host to the device.
	 * <p>
	 * The method returns the actual number of bytes successfully scheduled to
	 * be sent to the device.
	 * <p>
	 * As of this writing it is unclear under what circumstances something else
	 * than 'length' number of bytes could be returned as well as what happens
	 * if the report length does not match what the device expects.
	 * <p>
	 * 
	 * @param data
	 *            a byte array containing the data to be sent
	 * @param length
	 *            the number of bytes to send from the data array
	 * @return number bytes actually sent or -1 if the call failed
	 * 
	 */
    @Deprecated
	abstract public int setFeatureReport(byte[] data, int length);

	/**
	 * This method reads a feature report from the device.
	 * <p>
	 * See USB HID specification to learn more about feature reports.
	 * <p>
	 * This method blocks until the 'length' number of bytes has been received
	 * to the 'data' buffer or the device is closed.
	 * <p>
	 * This method returns the number of bytes actually received or -1 in case
	 * there was error in communicating with the device.
	 * <p>
	 * As of this writing it is unclear under what circumstances something else
	 * than 'length' number of bytes could be returned as well as what happens
	 * if the report length does not match what the device expects.
	 * <p>
	 * 
	 * @param data
	 *            the byte array to receive the data
	 * @param length
	 *            the size of the feature report
	 * @return the number of bytes actually received or -1 if the call failed
	 */
	abstract public int getFeatureReport(byte[] data, int length);

	/**
	 * This method sets the device removal listener.
	 * <p>
	 * The {@link DeviceRemovalListener#onDeviceRemoval} method of the device
	 * listener object will be called if and when the device is unexpectedly
	 * un-plugged.
	 * 
	 * There can be only one device removal listener at a time but it is
	 * possible to re-set the listener.
	 * <p>
	 * 
	 * @param listener
	 *            the device removal listener object or null to un-set
	 */
	public void setDeviceRemovalListener(DeviceRemovalListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_DeviceRemovalListener = listener;
	}

	/**
	 * This method returns the device removal listener for this device
	 * 
	 * @return the device removal listener
	 */
	public DeviceRemovalListener getDeviceRemovalListener() {
		return m_DeviceRemovalListener;
	}

    /**
	 * This method returns the same info that the {@link PureJavaHidApi#enumerateDevices()}
	 * would return for this device.
	 * 
	 * @return the device info object
	 */
	public HidDeviceInfo getHidDeviceInfo() {
		return m_HidDeviceInfo;
	}

	/**
	 * This method closes the access to the device.
	 * 
	 * After calling close no further method calls to this object must be done,
	 * otherwise an IllegalStateException will be generated.
	 * 
	 * This call may block indefinitely if the device stops sending reports.
	 * (Hopefully in the future the close method will not block under any
	 * circumstances).
	 * 
	 */
	abstract public void close();

}

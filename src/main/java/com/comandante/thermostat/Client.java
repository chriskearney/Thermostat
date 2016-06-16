package com.comandante.thermostat;

import javax.usb.*;
import java.nio.ByteBuffer;
import java.util.List;


public class Client {

    private static final short VENDOR_ID = 0x0c45;
    private static final short PRODUCT_ID = 0x7401;

    private static final int REQ_INT_LEN = 8;
    private static final int ENDPOINT = 0x82;
    private static final int INTERFACE = 1;
    private static final int CONFIG_NO = 1;
    private static final int TIMEOUT = 5000;

    private static final byte[] TEMPERATURE_COMMAND = new byte[]{(byte) 0x01, (byte) 0x80, (byte) 0x33, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    private final UsbDevice usbDevice;

    public Client() throws UsbException {
        this.usbDevice = findDevice(UsbHostManager.getUsbServices().getRootUsbHub(), VENDOR_ID, PRODUCT_ID);
    }

    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        throw new RuntimeException("Unable to find USB Temperature Device.");
    }

    public int getTemperature() throws UsbException {
        UsbConfiguration configuration = usbDevice.getUsbConfiguration((byte) 1);
        UsbInterface iface = configuration.getUsbInterface((byte) 1);
        iface.claim(usbInterface -> true);
        sendMessage(usbDevice, TEMPERATURE_COMMAND);
        return readTempSensor(iface);
    }

    private static int readTempSensor(UsbInterface iface) throws UsbException {
        UsbEndpoint inEndpoint = iface.getUsbEndpoint((byte) ENDPOINT);
        UsbPipe inPipe = inEndpoint.getUsbPipe();
        inPipe.open();
        try {
            byte[] sensorBytes = new byte[REQ_INT_LEN];
            int received = inPipe.syncSubmit(sensorBytes);
            if (received == 0) {
                throw new RuntimeException("Unable to retrieve data from usb temperature sensor.");
            }
            return (int) sensorBytes[2];
        } finally {
            inPipe.close();
        }
    }

    private static void sendMessage(UsbDevice device, byte[] message) throws UsbException {
        UsbControlIrp irp = device.createUsbControlIrp((byte) 0x21, (byte) 0x09, (short) 0x0200, (short) 0x01);
        irp.setData(message);
        device.syncSubmit(irp);
    }
}

package com.comandante.thermostat;

import javax.usb.*;
import java.util.List;


public class Client  {

    private static final short VENDOR_ID = 0x0c45;
    private static final short PRODUCT_ID = 0x7401;

    private static final int REQ_INT_LEN = 8;
    private static final int ENDPOINT = 0x82;
    private static final int INTERFACE = 1;
    private static final int CONFIG_NO = 1;
    private static final int TIMEOUT = 5000;

    //    'temp': b'\x01\x80\x33\x01\x00\x00\x00\x00',
    private static final byte[] TEMPERATURE_COMMAND = new byte[] { 0x01, (byte) 0x80, 0x33, 0x01, 0x00, 0x00, 0x00, 0x00 };
    // 'ini1': b'\x01\x82\x77\x01\x00\x00\x00\x00',
    private static final byte[] INIT_ONE_COMMAND = new byte[] { 0x01, (byte) 0x82, 0x77, 0x01, 0x00, 0x00, 0x00, 0x00 };
    //    'ini2': b'\x01\x86\xff\x01\x00\x00\x00\x00',
    private static final byte[] INIT_TWO_COMMAND = new byte[] { 0x01, (byte) 0x86, (byte) 0xff, 0x01, 0x00, 0x00, 0x00, 0x00 };

    public Client() throws UsbException {
        UsbDevice device = findDevice(UsbHostManager.getUsbServices().getRootUsbHub(), VENDOR_ID, PRODUCT_ID);
        UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
        UsbInterface iface = configuration.getUsbInterface((byte) 1);
        iface.claim(usbInterface -> true);
        sendMessage(device, TEMPERATURE_COMMAND);

    }

    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

    private static void read(UsbDevice device, UsbInterface iface) throws UsbException {
        UsbEndpoint inEndpoint =
                iface.getUsbEndpoint((byte) ENDPOINT);
        UsbPipe inPipe = inEndpoint.getUsbPipe();
        inPipe.open();
        try {
            byte[] headerBytes = new byte[REQ_INT_LEN];
            int received = inPipe.syncSubmit(headerBytes);
            System.out.println("Response received:" + received);
        } finally {
            inPipe.close();
        }
    }


    public static void sendMessage(UsbDevice device, byte[] message)
            throws UsbException
    {
        UsbControlIrp irp = device.createUsbControlIrp(
                (byte) (UsbConst.REQUESTTYPE_TYPE_CLASS |
                        UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE), (byte) 0x09,
                (short) 2, (short) 1);
        irp.setData(message);
        device.syncSubmit(irp);
    }


}

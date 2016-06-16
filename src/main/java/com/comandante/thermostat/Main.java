package com.comandante.thermostat;

import javax.usb.UsbException;

public class Main {

    public static void main(String[] args) throws UsbException {
        Client client = new Client();
        double farenheit = client.getTemperature() * 1.8 + 32;
        System.out.println("Temperature is " + farenheit);
    }
}

package com.example.seriaportcomm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import android_serialport_api.SerialPortFinder;
import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public class MainActivity extends AppCompatActivity {

    SerialPortFinder serialPortFinder;
    SerialHelper serialHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newSerialPort();
        setSerialPort();
        //}
    }


    public void newNewPortSetup(){
        
    }

    public void newSerialPort(){
        //SerialPo
        serialPortFinder = new SerialPortFinder();
        String[] x = serialPortFinder.getAllDevicesPath();
        Log.e("X : ",x.toString());
        serialHelper = new SerialHelper("/dev/ttyS0",9600) {
            @Override
            protected void onDataReceived(ComBean paramComBean) {
                if(paramComBean != null){
                    String byteStr2 = paramComBean.bRec.toString();
                    String byteStr = new String(paramComBean.bRec, StandardCharsets.US_ASCII);
                    //String resultString = byteStr.replaceAll("[^\\x00-\\x7F]", "");
                    String fixed = byteStr.replaceAll("[^\\x20-\\x7e]", "");
                    Log.e("SerialData",fixed);
                    //Log.e("SerialData",toAscii(byteStr));
                }
            }
        };

        try {
            serialHelper.open();
            serialHelper.sendTxt("Shop On Meesho");
            serialHelper.sendTxt("Shop On Amazon");
            serialHelper.sendTxt("Shop On Flipkart");
            serialHelper.sendTxt("Shop On Mantra");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {

        }
//        serialHelper.setPort("/dev/ttyS0");      //set the serial port
//        serialHelper.setBaudRate("9600");     //set the baud rate
//        serialHelper.setStopBits(0);  //set the stop bit
//        serialHelper.setDataBits(8);  //set the data bit
//        serialHelper.setParity(int parity);      //set the check bit
//        serialHelper.setFlowCon(int flowcon);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setSerialPort(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> devices = manager.getDeviceList();

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x1234, 0x0001, CdcAcmSerialDriver.class);
        customTable.addProduct(0x1234, 0x0002, CdcAcmSerialDriver.class);

        UsbSerialProber prober = new UsbSerialProber(customTable);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.e("SerialPort","No drivers found");
            return;
        }

        // Open a connection to the first available driver.

        for (UsbSerialDriver usbSerialDriver:availableDrivers) {
            UsbDevice usbDevice = usbSerialDriver.getDevice();
            List<UsbSerialPort> usbSerialPorts = usbSerialDriver.getPorts();
            String deviceName = "";
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deviceName  = usbDevice.getProductName();
                    try {
                        UsbConfiguration usbConfiguration = usbDevice.getConfiguration(0);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Log.e("[UsbDevice]",String.format("DeviceName :: %s , deviceName :: %s, DeviceId :: %s , VendorId :: %s , ProductId :: %s",
                    usbDevice.getDeviceName(),deviceName,usbDevice.getDeviceId(),usbDevice.getVendorId(),usbDevice.getProductId()));
        }
        UsbDevice usbDevice = devices.get("/dev/bus/usb/001/002");

        UsbDeviceConnection connection = manager.openDevice(usbDevice);
        if (connection == null) {
            PendingIntent pendingIntent = null;
            Log.e("SerialPort","connection == null");
            return;
        }
        ///dev/bus/usb/001/002 -> {UsbDevice@831697427656} "UsbDevice[mName=/dev/bus/usb/001/002,mVendorId=32903,mProductId=2730,mClass=224,mSubclass=1,mProtocol=1,mInterfaces=[Landroid.os.Parcelable;@5283c4a4]"


        UsbEndpoint endpoint = usbDevice.getInterface(0).getEndpoint(0);

        connection.claimInterface(usbDevice.getInterface(0), true);
        connection.bulkTransfer(endpoint, "DATA".getBytes(), 4, 10000);
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(19200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            port.write("A1001".getBytes(),10*1000);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SerialPort",e.getLocalizedMessage());
        }
        finally {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
        SerialInputOutputManager.Listener aListener= new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
                Log.e("SerialPort ",data.toString());
            }

            @Override
            public void onRunError(Exception e) {

            }
        };
        SerialInputOutputManager serialInputOutputManager  = new SerialInputOutputManager(port,aListener);
        serialInputOutputManager.start();
         */
    }

    @Override
    protected void onDestroy() {
        if(serialHelper != null && serialHelper.isOpen())
            serialHelper.close();
        super.onDestroy();
    }

    public String toAscii(String hex){
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        System.out.println(output);
        return output.toString();
    }
}
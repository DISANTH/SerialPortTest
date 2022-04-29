package com.example.seriaportcomm;

public interface UsbListener {
    public void onNewData(byte[] data);
}

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.esaj.wheelemetrics.bluetooth;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 *
 * @author esaj
 */
public interface Constants
{
    //SPP-protocol/profile UUID
    public static final String SPP = "00001101-0000-1000-8000-00805F9B34FB";

    // Message types sent from the BluetoothService Handler and others
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_BINARYDATA_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final int MESSAGE_DATA_READ = 10;


    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Speed data, extra data for intent (double)
    public static final String SPEED_DATA = "wheelemetrics_spd";

    //Loggable data
    public static final String MESSAGE_STRING_LOGGABLEDATA = "logdata";
}

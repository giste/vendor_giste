package org.giste.profiles.lib;

import static android.telephony.TelephonyManager.DATA_ENABLED_REASON_USER;

import static java.lang.Math.exp;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfilesLib {
    private static final Logger logger = Logger.getLogger("ProfilesLib");

    public static void setVolume(Context context, int stream, int value) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        logger.log(Level.INFO, "Setting volume(" + stream + ") = " + value);
        audioManager.setStreamVolume(stream, value, 0);
    }

    public static void setRingerMode(Context context, int mode) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getRingerMode() != mode) {
            logger.log(Level.INFO, "Setting ringer mode = " + mode);
            audioManager.setRingerMode(mode);
        }
    }

    public static void setWiFi(Context context, boolean value) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled() != value) {
            logger.log(Level.INFO, "Setting wifi = " + value);
            wifiManager.setWifiEnabled(value);
        }
    }

    public static void setData(Context context, boolean value) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager.isDataEnabled() != value) {
            logger.log(Level.INFO, "Setting data = " + value);
            telephonyManager.setDataEnabledForReason(DATA_ENABLED_REASON_USER, value);
        }
    }

    public static void setBluetooth(Context context, boolean value) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter.isEnabled() != value) {
            logger.log(Level.INFO, "Setting bluetooth = " + value);
            if (value) {
                bluetoothAdapter.enable();
            } else {
                bluetoothAdapter.disable();
            }
        }
    }

    public static void setNfc(Context context, boolean value) {
        NfcAdapter nfcAdapter;

        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        } catch (UnsupportedOperationException e) {
            nfcAdapter = null;
        }

        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled() != value) {
                logger.log(Level.INFO, "Setting NFC = " + value);
                if (value) {
                    nfcAdapter.enable();
                } else {
                    nfcAdapter.disable();
                }
            }
        }
    }

    public static void setAirplaneMode(Context context, boolean value) {
        int newValue = value ? 1 : 0;
        int current = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);

        if (current != newValue) {
            logger.log(Level.INFO, "Setting airplane mode = " + value);
            Settings.Global.putInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON,
                    newValue
            );
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", newValue == 1);
            context.sendBroadcast(intent);
        }
    }

    public static void setLocation(Context context, boolean value) {
        LocationManager locationManager;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isLocationEnabled() != value) {
            logger.log(Level.INFO, "Setting location = " + value);
            locationManager.setLocationEnabledForUser(value, new UserHandle(UserHandle.USER_CURRENT));
        }
    }

    public static void setBrightnessAuto(Context context, boolean value) {
        int newValue = value ? 1 : 0;
        int current = Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        );

        if (current != newValue) {
            logger.log(Level.INFO, "Setting brightness auto = " + value);
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    newValue
            );
        }
    }

    public static void setBrightness(Context context, int value) {
        boolean manual = (Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        int newValue = mapBrightness(value);

        if (manual) {
            int current = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 0
            );

            if (current != newValue) {
                logger.log(Level.INFO, "Setting brightness = " + value + " mapped to " + newValue);
                Settings.System.putInt(
                        context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        newValue
                );
            }
        } else {
            logger.log(Level.INFO,"Not setting brightness, it's auto");
        }
    }

    private static int mapBrightness(int percentage) {
        if (percentage <= 1) {
            return 1;
        } else if (percentage >= 100) {
            return 255;
        } else {
            return toIntExact(round(exp((percentage + 9.411) / 19.811)));
        }
    }

}

package com.example.nfcproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Placeholder;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "";
    TextView nfcShow;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if(savedInstanceState == null){
//            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
//        }

        nfcShow = (TextView)findViewById(R.id.nfcShow);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "NFC Not available!", Toast.LENGTH_LONG).show();
        }
    }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic tag = MifareClassic.get(tagFromIntent);
            try {
                //Variables
                int sectorCount = tag.getSectorCount();
                int tagSize = tag.getSize();
                int bCount;
                int bIndex;
                boolean auth;
                byte[] data;
                //Keys
                byte[] defaultKeys = new byte[]{};
                defaultKeys = MifareClassic.KEY_DEFAULT;
                //Connecting to tag
                tag.connect();
                //auth = true
                auth = tag.authenticateSectorWithKeyA(2, defaultKeys);


                if (auth) {
                    // Read the block
                    data = tag.readBlock(9);
                    String xNumberString = new String(data, "UTF-8");
                    char[] letters = new char[12];
                    char[] output = new char[10];
                    letters[0] = 'X';
                    letters[1] = 'x';
                    letters[2] = '0';
                    letters[3] = '1';
                    letters[4] = '2';
                    letters[5] = '3';
                    letters[6] = '4';
                    letters[7] = '5';
                    letters[8] = '6';
                    letters[9] = '7';
                    letters[10] = '8';
                    letters[11] = '9';
                    Log.i("length", " of string: " + xNumberString.length());
                    for(int i=0; i<xNumberString.length(); i++){
                        for(int j=0;j<letters.length; j++) {
                            if (xNumberString.charAt(i) == letters[j]) {
                                Log.i("Xnumber", "at position " + i + " is:" + xNumberString.charAt(i));
                                output[i] = xNumberString.charAt(i);
                            }
                        }
                    }
                    String xNumber = new String(output);
                    Log.i("Before Processing", " XnumberROW: " + xNumberString);
                    Log.i("After Processing", " XnumberROW: " + xNumber);
                    nfcShow.setText(" XNUMBER:  " + xNumber);

                } else {
                    Log.e("a", "Auth Failed");
                }
//                tag.connect();
//                auth = false;
//                String cardData = null;
//                for(int j = 0; j < sectorCount; j++){
//                   // Log.i("a", "a"+ j);
//                    // 6.1) authenticate the sector
//                    auth = tag.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
//
//                    if(auth){
//                        // 6.2) In each sector - get the block count
//                        bCount = tag.getBlockCountInSector(j);
//                        bIndex = 0;
//                        for(int i = 0; i < bCount; i++){
//                            bIndex = tag.sectorToBlock(j);
//                            // 6.3) Read the block
//                            data = tag.readBlock(bIndex);
//                            // 7) Convert the data into a string from Hex format.
//                            Log.i("DATA", Arrays.toString(data));
//                            bIndex++;
//                        }
//                    }else{ // Authentication failed - Handle it
//
//                    }
//                }
//            }catch (IOException e) {
//                Log.e(TAG, e.getLocalizedMessage());
//            }


                if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                    Toast.makeText(this, "NFC intent available", Toast.LENGTH_SHORT).show();
                    Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                    if (parcelables != null && parcelables.length > 0) {
                        readTextFromTag((NdefMessage) parcelables[0]);
                    } else {
                        Toast.makeText(this, "No Message found", Toast.LENGTH_SHORT).show();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            protected void readTextFromTag (NdefMessage message){
                NdefRecord[] ndefRecords = message.getRecords();
                if (ndefRecords != null && ndefRecords.length > 0) {
                    NdefRecord ndefRecord = ndefRecords[0];
                    String tagContent = getTextFromNdefRecord(ndefRecord);

                } else {
                    Toast.makeText(this, "No NDEF messages found", Toast.LENGTH_SHORT).show();
                }
            }

    public String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try{
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    @Override
        protected void onResume() {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            IntentFilter[] intentFilter = new IntentFilter[]{};
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);

            super.onResume();
        }

        @Override
        protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }
}

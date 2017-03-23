package com.krislarson.datarandomaccess;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MULTIPLE = 10;

    private long[] mLines;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLines = new long[500];

        long start = SystemClock.elapsedRealtime();

        int lineCount = 0;
        try (InputStream in = getResources().openRawResource(R.raw.fl_insurance_sample)) {

            int index = 0;
            int charCount = 0;
            int cIn;
            while ((cIn = in.read()) != -1) {
                charCount++;

                char ch = (char) cIn;
                if (ch == '\n' || ch == '\r') {
                    lineCount++;
                    if (lineCount % MULTIPLE == 0) {
                        index = lineCount / MULTIPLE;
                        if (index == mLines.length) {
                            mLines = Arrays.copyOf(mLines, mLines.length + 100);
                        }
                        mLines[index] = charCount;
                    }
                }

            }

            mLines = Arrays.copyOf(mLines, index+1);

        } catch (IOException e) {
            Log.e(TAG, "error reading raw resource", e);
        }

        long elapsed = SystemClock.elapsedRealtime() - start;

        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("time to index data: " + elapsed + "ms");
        Log.i(TAG, "time to index data: " + elapsed + "ms");

        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setHint("Enter a line number from 1 to " + (lineCount + 1));

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try (InputStream in = getResources().openRawResource(R.raw.fl_insurance_sample)) {

                    long start = SystemClock.elapsedRealtime();

                    int ch;
                    int line = Integer.parseInt(editText.getText().toString().trim());
                    if (line < 1 || line >= mLines.length ) {
                        mTextView.setText("invalid line: " + line + 1);
                    }
                    line--;
                    int index = (line / MULTIPLE);
                    in.skip(mLines[index]);
                    int rem = line % MULTIPLE;
                    while (rem > 0) {
                        ch = in.read();
                        if (ch == -1) {
                            return; // readLine will fail
                        } else if (ch == '\n' || ch == '\r') {
                            rem--;
                        }
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String text = reader.readLine();

                    long elapsed = SystemClock.elapsedRealtime() - start;

                    mTextView.setText(text + " --- access time: " + elapsed + "ms");

                } catch (NumberFormatException e) {
                    Log.e(TAG, "error parsing line num", e);
                    mTextView.setText(e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "error reading raw resource", e);
                    mTextView.setText(e.getMessage());
                }
            }
        });
    }
}

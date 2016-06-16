package rosis.pedometer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;

public class Pedometer extends Activity implements SensorEventListener {

    EditText inputPath;
    TextView textView;
    TextView textPath;
    TextView textStatus;
    Button startButton;
    Button stopButton;

    File file;
    FileOutputStream fs;
    PrintWriter writer;

    private SensorManager sensorManager;
    private long startTime;
    private long tick;
    private long seconds;

    boolean running = false;
    boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        // file name to be entered
        inputPath = (EditText) findViewById(R.id.inputPath);
        textView = (TextView) findViewById(R.id.textEvents);
        textPath = (TextView) findViewById(R.id.textPath);

        textStatus = (TextView) findViewById(R.id.textStatus);

        inputPath.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {  }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textPath.setText("/storage/sdcard0/ROSIS/" + System.currentTimeMillis() + "-" + inputPath.getText() + ".csv");
            }
        });

        // start button
        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                start();
            }
        });

        // stop button
        stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                stop();
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        startTime = System.nanoTime();
        textStatus.setText("Ready.");
    }

    void start() {
        if (running) return;
        try {
            file = new File("/storage/sdcard0/ROSIS/" + System.currentTimeMillis() + "-" + inputPath.getText() + ".csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            textPath.setText(file.getPath());
            fs = new FileOutputStream(file);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fs)));

            seconds = -3;

            Toast.makeText(getBaseContext(), "Started!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        running = true;
    }

    void stop() {
        if (!running) return;
        running = false;
        recording = false;

        try {
            writer.close();
            fs.close();
            Toast.makeText(getBaseContext(), "Done.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        textStatus.setText("Done.");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        tick += 1;
        long currentTime = System.nanoTime();
        if (currentTime - startTime > 1000000000) { // cca every second
            textView.setText("" + tick);
            startTime = System.nanoTime();
            tick = 0;

            if (running) {
                seconds ++;
                if (seconds < 0) {
                    textStatus.setText("ETA: " + seconds + "s" );
                } else if (seconds < 10) {
                    textStatus.setText("Recording: " + seconds + "s" );
                    recording = true;
                } else {
                    stop();
                }
            }
        }

        if (recording) {
            writer.println(
                    currentTime + "\t"+ event.sensor.getName() +"\t" + event.values[0] + "\t" + event.values[1] + " \t" + event.values[2]
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the sensors
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }
}
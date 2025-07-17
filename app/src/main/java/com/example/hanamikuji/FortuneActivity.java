package com.example.hanamikuji;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import android.view.View;
import android.os.Vibrator;
import android.content.Context;

public class FortuneActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private float lastX, lastY, lastZ;
    private long lastTime;
    private static final int SHAKE_THRESHOLD = 1200; // 振る感度

    private TextView txtResult;
    private boolean isShaken = false;

    private final String[] fortunes = {
            "ツバキ🌺 – 今日中に黒猫をなでるといいことがあるでしょう",
            "ウメ🌸 – 猫を見かけたら写真を撮ると幸運UP",
            "モモ🌸 – 猫の動画をシェアすると笑顔が増える",
            "サクラ🌸 – 猫の小物を身につけると恋愛運UP",
            "フジ💜 – 長いもの（猫のしっぽでもOK）に触れると吉",
            "アジサイ💠 – 雨の日、猫の足跡を探すと運気上昇",
            "ヒマワリ🌻 – 大きく伸びをすると気分爽快、金運UP",
            "ハス🌷 – △を眺めると心が整う（猫の耳でもOK）",
            "キク🌼 – 黄色い花を見ると友人と猫の話で関係が深まる",
            "コスモス🌸 – 夜空を見上げると猫の夢が見られる",
            "サザンカ🌺 – 猫と目が合ったら小さな幸せが訪れる",
            "シクラメン💖 – 赤いものを身につけると猫が寄ってくる"
    };

    private TextView txtGuide;
    private TextView txtFlower;
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortune);

        txtResult = findViewById(R.id.txtResult);
        Button btnTop = findViewById(R.id.btnTop);

        // トップに戻る
        btnTop.setOnClickListener(v -> {
            Intent intent = new Intent(FortuneActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        txtGuide = findViewById(R.id.txtGuide);
        txtFlower = findViewById(R.id.txtFlower);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !isShaken) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) > 100) {
                long diffTime = (currentTime - lastTime);
                lastTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (vibrator != null) {
                        vibrator.vibrate(200); // ← 200ミリ秒ブルっとする
                    }
                    showRandomFortune();
                    isShaken = true; // 一回だけ
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void showRandomFortune() {
        int randomIndex = new Random().nextInt(fortunes.length);
        String[] parts = fortunes[randomIndex].split("–", 2);

        // 花の名前を先に表示
        txtFlower.setText(parts[0].trim());
        txtResult.setText(""); // 占い内容は一旦空
        txtGuide.setVisibility(View.GONE);

        // 占い内容を遅れてフェードイン
        new android.os.Handler().postDelayed(() -> {
            txtResult.setAlpha(0f);
            txtResult.setText(parts.length > 1 ? parts[1].trim() : "");
            txtResult.animate().alpha(1f).setDuration(1500); // 0.5秒かけてふわっと
        }, 3000); // 0.7秒遅らせる
    }



}


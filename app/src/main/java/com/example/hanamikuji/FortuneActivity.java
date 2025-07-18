package com.example.hanamikuji;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // ★積もった花びらを管理するリスト
    private final List<ImageView> petals = new ArrayList<>();

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
                SensorManager.SENSOR_DELAY_GAME); // ★更新頻度はGAMEに
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float tiltX = event.values[0]; // 左右
            float tiltY = event.values[1]; // 前後（上下方向に使う）

            float moveX = -tiltX * 1.5f;
            float moveY = tiltY * 1.0f; // 傾けると少し上下する（調整可）

            ViewGroup root = findViewById(R.id.rootLayout);
            int maxX = root.getWidth();
            int maxY = root.getHeight();

            for (ImageView petal : petals) {
                // ★左右の端で止める
                float newX = petal.getX() + moveX;
                if (newX < 0) newX = 0;
                else if (newX > maxX - petal.getWidth()) newX = maxX - petal.getWidth();

                // ★上下も範囲内に
                float newY = petal.getY() - moveY; // 上に傾けると上がるようにマイナス
                if (newY < 0) newY = 0;
                else if (newY > maxY - petal.getHeight()) newY = maxY - petal.getHeight();

                petal.setX(newX);
                petal.setY(newY);
            }

            // ★振る判定は変えない
            if (!isShaken) {
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
                            vibrator.vibrate(200);
                        }
                        showRandomFortune();
                        showFlowerAnimation();
                        isShaken = true;
                    }

                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void showRandomFortune() {
        int randomIndex = new Random().nextInt(fortunes.length);
        String[] parts = fortunes[randomIndex].split("–", 2);

        txtFlower.setText(parts[0].trim());
        txtResult.setText("");
        txtGuide.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            txtResult.setAlpha(0f);
            txtResult.setText(parts.length > 1 ? parts[1].trim() : "");
            txtResult.animate().alpha(1f).setDuration(1500);
        }, 2000);
    }

    private void showFlowerAnimation() {
        ViewGroup root = findViewById(R.id.rootLayout);

        for (int i = 0; i < 45; i++) {
            ImageView flowerView = new ImageView(this);
            flowerView.setImageResource(R.drawable.sakura);

            int size = 64;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
            flowerView.setLayoutParams(params);

            float startX = (float) (Math.random() * (root.getWidth() - size));
            flowerView.setX(startX);
            flowerView.setY(-100);

            root.addView(flowerView);
            petals.add(flowerView);

            flowerView.setAlpha(0f);
            flowerView.postDelayed(() -> {
                flowerView.setAlpha(1f);

                long duration = (long) (Math.random() * 5000 + 5000);
                float driftX = startX + (float) (Math.random() * 400 - 200);

                // ★端に収める
                if (driftX < 0) {
                    driftX = 0;
                } else if (driftX > root.getWidth() - size) {
                    driftX = root.getWidth() - size;
                }

                flowerView.animate()
                        .translationY(root.getHeight() - size)
                        .translationX(driftX)
                        .rotationBy((float) (Math.random() * 180 - 90))
                        .setDuration(duration)
                        .start();
            }, (long) (Math.random() * 2000));
        }
    }

}

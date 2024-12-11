package com.example.pista.aut;

import android.app.AlertDialog;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pista.R;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // Variables OpenGL
    private GLSurfaceView glSurfaceView;
    private GameRenderer renderer;
    private TextView coordsText;
    private boolean isRotating = false;
    private float previousX, previousY;
    private static final float TOUCH_SCALE_FACTOR = 0.5f;

    // Variables ROS
    private ROSWebSocketClient webSocketClient;
    private TextView txtDecision, tvSignalStatus;
    private Switch switchMode;
    private ImageView imgSignal;
    private boolean flag;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isConnecting = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private AlertDialog connectionDialog;
    private TextView connectionStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización OpenGL
        initializeOpenGL();
        setupGLSurfaceViewTouchListener();

        // Inicialización ROS
        initializeROSViews();
        setupConnectionStatusView();
        initializeWebSocket();
        setupListeners();
    }

    private void initializeOpenGL() {
        String mode = getIntent().getStringExtra("mode");
        ArrayList<Float> pathPoints = null;
        if ("manual".equals(mode)) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("pathPoints")) {
                pathPoints = (ArrayList<Float>) extras.getSerializable("pathPoints");
            }
        }

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(3);
        renderer = new GameRenderer(this, pathPoints);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initializeROSViews() {
        txtDecision = findViewById(R.id.txtDecision);
        switchMode = findViewById(R.id.switchMode);  // Añadir esta línea

        // Iniciar en modo automático
        if (switchMode != null) {
            switchMode.setChecked(true);
            flag = true;
        }
    }

    private void setupConnectionStatusView() {
        connectionStatusView = findViewById(R.id.connectionStatusView);
        if (connectionStatusView == null) {
            Log.e("MainActivity", "ConnectionStatusView not found in layout");
            TextView statusView = new TextView(this);
            statusView.setId(View.generateViewId());
            statusView.setTextColor(getResources().getColor(android.R.color.white));
            statusView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            statusView.setPadding(16, 8, 16, 8);
            statusView.setText("Sin conexión al servidor");
            statusView.setVisibility(View.GONE);
            ((android.view.ViewGroup) findViewById(android.R.id.content)).addView(statusView);
            connectionStatusView = statusView;
        }
    }

    private void initializeWebSocket() {
        try {
            webSocketClient = new ROSWebSocketClient(
                    "ws://192.168.1.72:9090",
                    this,
                    findViewById(R.id.txtDecision),
                    renderer
            );
            webSocketClient.setConnectionCallback(new ROSWebSocketClient.ConnectionCallback() {
                @Override
                public void onConnectionSuccess() {
                    runOnUiThread(() -> {
                        hideConnectionProgress();
                        hideConnectionError();
                        setControlsEnabled(true);
                        isConnecting = false;
                        Toast.makeText(MainActivity.this, "Conectado al servidor", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onConnectionError(Exception e) {
                    runOnUiThread(() -> {
                        hideConnectionProgress();
                        isConnecting = false;
                        showConnectionError();
                        setControlsEnabled(false);
                    });
                }

                @Override
                public void onConnectionClosed() {
                    runOnUiThread(() -> {
                        showConnectionError();
                        setControlsEnabled(false);
                    });
                }
            });

            connectToWebSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            showConnectionError();
        }
    }

    private void setupListeners() {
        // ROS listeners
        txtDecision.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s.toString();
                if (currentText.contains("Decisión: ")) {
                    // Extraer solo la decisión del texto
                    String decision = currentText.replace("Decisión: ", "");
                    renderer.updateDecision(decision);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        ImageButton btnCar = findViewById(R.id.btnCar);
        btnCar.setOnClickListener(v -> {
            renderer.toggleCarPlacementMode();
            btnCar.setSelected(renderer.isCarPlacementMode());
            Toast.makeText(this,
                    renderer.isCarPlacementMode() ? "Toca en la pista para colocar el carro" : "Modo colocación desactivado",
                    Toast.LENGTH_SHORT).show();
        });


        // Control mode switch
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (webSocketClient != null && webSocketClient.isConnected()) {
                flag = isChecked;
                String estado = flag ? "aut" : "man";
                String message = "{ \"op\": \"publish\", \"topic\": \"/control\", \"msg\": { \"data\": \"" + estado + "\" } }";
                webSocketClient.send(message);

                // Si se cambia a modo manual, detener el movimiento
                if (!isChecked) {
                    renderer.updateDecision("Detenerse");
                }
            } else {
                switchMode.setChecked(!isChecked);
                Toast.makeText(this, "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });


    }




    // Métodos de conexión ROS
    private void connectToWebSocket() {
        if (!isConnecting && (webSocketClient == null || !webSocketClient.isConnected())) {
            isConnecting = true;
            showConnectionProgress();
            webSocketClient.connect();

            handler.postDelayed(() -> {
                if (isConnecting) {
                    hideConnectionProgress();
                    isConnecting = false;
                    showConnectionError();
                }
            }, 10000);
        }
    }

    private void showConnectionProgress() {
        if (connectionDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Conectando");
            builder.setMessage("Intentando conectar al servidor...");
            builder.setCancelable(false);
            connectionDialog = builder.create();
        }
        connectionDialog.show();
    }

    private void hideConnectionProgress() {
        if (connectionDialog != null && connectionDialog.isShowing()) {
            connectionDialog.dismiss();
        }
        isConnecting = false;
    }

    private void showConnectionError() {
        if (connectionStatusView != null) {
            connectionStatusView.setVisibility(View.VISIBLE);
        }
    }

    private void hideConnectionError() {
        if (connectionStatusView != null) {
            connectionStatusView.setVisibility(View.GONE);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        findViewById(R.id.btnStart).setEnabled(enabled);
        findViewById(R.id.btnCar).setEnabled(enabled);
        switchMode.setEnabled(enabled);
    }

    // Lifecycle methods
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        hideConnectionProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        if (webSocketClient != null && !webSocketClient.isConnected()) {
            reconnectAttempts = 0;
            connectToWebSocket();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    // Touch handling for OpenGL
    private void setupGLSurfaceViewTouchListener() {
        glSurfaceView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (renderer.isCarPlacementMode()) {
                        float[] worldCoords = renderer.screenToWorldCoordinates(x, y);
                        if (worldCoords != null) {
                            renderer.placeCarAt(worldCoords[0], worldCoords[1], worldCoords[2]);
                            Toast.makeText(this, "Colocando carro en: " + worldCoords[0] + ", " + worldCoords[2], Toast.LENGTH_SHORT).show();
                        }
                    }
                    isRotating = true;
                    previousX = x;
                    previousY = y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!renderer.isCarPlacementMode() && isRotating) {
                        float dx = x - previousX;
                        float dy = y - previousY;
                        renderer.updateCameraRotation(dx * TOUCH_SCALE_FACTOR, dy * TOUCH_SCALE_FACTOR);
                    }
                    previousX = x;
                    previousY = y;
                    break;

                case MotionEvent.ACTION_UP:
                    isRotating = false;
                    break;
            }
            return true;
        });
    }


}
package com.example.alavergas;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SceneManager sceneManager;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar GLSurfaceView para OpenGL
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new TrackRenderer());

        // Configurar SceneManager para el modelo del carro
        sceneManager = new SceneManager(this, findViewById(R.id.sceneView));
        sceneManager.loadModel("file:///android_asset/carro.glb");
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        try {
            sceneManager.resume();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        sceneManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sceneManager.destroy();
    }
}

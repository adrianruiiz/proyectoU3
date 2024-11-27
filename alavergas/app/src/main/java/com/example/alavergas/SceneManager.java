package com.example.alavergas;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

public class SceneManager {
    private final SceneView sceneView;
    private Node modelNode;
    private final Gesture gestureHandler;
    private float scaleFactor = 0.09f; // Escala inicial del modelo

    public SceneManager(Context context, SceneView sceneView) {
        this.sceneView = sceneView;
        this.gestureHandler = new Gesture(context, this);
        this.sceneView.setOnTouchListener((view, event) -> {
            gestureHandler.handleGesture(event);
            return true;
        });
    }

    public void loadModel(String modelUri) {
        ModelRenderable.builder()
                .setSource(sceneView.getContext(), Uri.parse(modelUri))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(this::addModelToScene)
                .exceptionally(throwable -> {
                    Toast.makeText(sceneView.getContext(), "Error al cargar el modelo 3D", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void addModelToScene(ModelRenderable modelRenderable) {
        modelNode = new Node();
        modelNode.setRenderable(modelRenderable);
        sceneView.getScene().addChild(modelNode);

        // Configurar posición y escala inicial del modelo
        modelNode.setLocalPosition(new Vector3(0.0f, 0.0f, -2.0f));
        modelNode.setLocalScale(new Vector3(scaleFactor, scaleFactor, scaleFactor));
    }

    public Node getModelNode() {
        return modelNode;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        if (modelNode != null) {
            modelNode.setLocalScale(new Vector3(scaleFactor, scaleFactor, scaleFactor));
        }
    }

    public void resume() throws Exception {
        sceneView.resume();
    }

    public void pause() {
        sceneView.pause();
    }

    public void destroy() {
        sceneView.destroy();
    }
}

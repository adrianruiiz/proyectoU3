package com.example.alavergas;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class Gesture {
    private final ScaleGestureDetector scaleGestureDetector;
    private final SceneManager sceneManager;
    private float initialX, initialY;
    private final float rotationSpeed = 0.5f;

    public Gesture(Context context, SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleChange = detector.getScaleFactor();
                float newScaleFactor = sceneManager.getModelNode() != null ? sceneManager.getModelNode().getLocalScale().x * scaleChange : 0.09f;

                // Restringir escala para evitar tamaños extremos
                newScaleFactor = Math.max(0.05f, Math.min(newScaleFactor, 0.5f));
                sceneManager.setScaleFactor(newScaleFactor);
                return true;
            }
        });
    }

    public void handleGesture(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        handleRotation(event);
    }

    private void handleRotation(MotionEvent event) {
        Node modelNode = sceneManager.getModelNode();
        if (modelNode == null) return;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - initialX;
                float deltaY = event.getY() - initialY;

                Quaternion currentRotation = modelNode.getLocalRotation();
                Quaternion rotationY = Quaternion.axisAngle(new Vector3(0, 1, 0), -deltaX * rotationSpeed);
                Quaternion rotationX = Quaternion.axisAngle(new Vector3(1, 0, 0), deltaY * rotationSpeed);

                modelNode.setLocalRotation(Quaternion.multiply(currentRotation, Quaternion.multiply(rotationY, rotationX)));

                initialX = event.getX();
                initialY = event.getY();
                break;
        }
    }
}

package com.z_iti_271304_u3_e01.aut;

import android.content.Context;
import android.util.Log;

import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.Scene;
import com.google.android.filament.TransformManager;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.ResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CarObject {
    private static final String TAG = "CarObject";
    private float posX, posY, posZ;
    private boolean isPlaced = false;

    private FilamentAsset asset;
    private int renderable;
    private Engine engine;
    private Scene scene;

    public CarObject(Context context, Engine engine, Scene scene) {
        this.engine = engine;
        this.scene = scene;

        try {
            // Cargar archivo GLB desde los assets
            ByteBuffer buffer = readAsset(context, "carro.glb");

            // Crear un AssetLoader con materiales predeterminados
            AssetLoader assetLoader = new AssetLoader(engine, null, EntityManager.get());

            // Cargar el modelo GLB
            asset = assetLoader.createAsset(buffer);
            if (asset != null) {
                // Obtener la entidad raíz del modelo
                renderable = asset.getRoot();

                // Añadir la entidad a la escena
                scene.addEntity(renderable);

                // Cargar los recursos del modelo
                ResourceLoader resourceLoader = new ResourceLoader(engine);
                resourceLoader.loadResources(asset);
                resourceLoader.destroy();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargando modelo GLB", e);
        }
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPosition(float x, float y, float z) {
        if (renderable != 0) {
            float[] transformMatrix = {
                    1, 0, 0, x,
                    0, 1, 0, y,
                    0, 0, 1, z,
                    0, 0, 0, 1
            };

            TransformManager transformManager = engine.getTransformManager();
            int transformInstance = transformManager.getInstance(renderable);

            if (transformInstance != 0) {
                transformManager.setTransform(transformInstance, transformMatrix);
            }
        }
    }

    public void destroy() {
        if (scene != null && renderable != 0) {
            scene.removeEntity(renderable);
        }
    }

    private ByteBuffer readAsset(Context context, String assetName) throws Exception {
        try (InputStream inputStream = context.getAssets().open(assetName);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return ByteBuffer.wrap(outputStream.toByteArray());
        }
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }
}

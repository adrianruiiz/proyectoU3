package com.example.pista.aut;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CarObject {
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram = -1;
    private float posX, posY, posZ;
    private float size = 0.5f;
    private boolean isPlaced = false;
    private static final int COORDS_PER_VERTEX = 3;
    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getPosZ() { return posZ; }
    public CarObject() {
        // Crear vértices para el carro (un cubo simple)
        float[] vertices = {
                // Frente
                -size, -size, size,  // 0
                size, -size, size,   // 1
                size, size, size,    // 2
                -size, size, size,   // 3
                // Atrás
                -size, -size, -size, // 4
                size, -size, -size,  // 5
                size, size, -size,   // 6
                -size, size, -size   // 7
        };

        short[] indices = {
                0, 1, 2, 2, 3, 0,  // Frente
                1, 5, 6, 6, 2, 1,  // Derecha
                5, 4, 7, 7, 6, 5,  // Atrás
                4, 0, 3, 3, 7, 4,  // Izquierda
                3, 2, 6, 6, 7, 3,  // Arriba
                4, 5, 1, 1, 0, 4   // Abajo
        };

        // Preparar buffers
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(indices).position(0);
    }

    public void setPosition(float x, float y, float z) {
        posX = x;
        posY = 0.5f; // Valor fijo en lugar de y + 0.5f
        posZ = z;
        isPlaced = true;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void draw(float[] projectionMatrix, float[] viewMatrix) {
        if (!isPlaced) return;

        if (mProgram == -1) {
            initializeShaders();
        }

        // Usar el programa shader
        GLES30.glUseProgram(mProgram);

        // Obtener handles de los atributos
        int positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        int mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Preparar la matriz de transformación
        float[] modelMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        float[] tempMatrix = new float[16];

        // Configurar la matriz modelo
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, posX, posY, posZ);

        // Calcular la matriz MVP
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0);

        // Pasar la matriz MVP al shader
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Habilitar el atributo de vértices
        GLES30.glEnableVertexAttribArray(positionHandle);

        // Preparar los datos de los vértices
        GLES30.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                0, vertexBuffer);

        // Dibujar el carro
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 36, // 12 triángulos * 3 vértices
                GLES30.GL_UNSIGNED_SHORT, indexBuffer);

        // Deshabilitar el arreglo de vértices
        GLES30.glDisableVertexAttribArray(positionHandle);
    }

    private void initializeShaders() {
        // Código del Vertex shader
        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        // Código del Fragment shader
        String fragmentShaderCode =
                "precision mediump float;" +
                        "void main() {" +
                        "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" + // Color rojo para el carro
                        "}";

        // Cargar shaders
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Crear el programa
        mProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgram, vertexShader);
        GLES30.glAttachShader(mProgram, fragmentShader);
        GLES30.glLinkProgram(mProgram);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        // Verificar la compilación
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("CarObject", "Error compilando shader: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
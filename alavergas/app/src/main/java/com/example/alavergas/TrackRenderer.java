package com.example.alavergas;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TrackRenderer implements GLSurfaceView.Renderer {
    private FloatBuffer vertexBuffer;
    private final int program;
    private final int vertexCount = 4;
    private final int vertexStride = 3 * 4; // Cada vértice tiene 3 componentes (x, y, z), cada uno de 4 bytes.

    // Definimos los vértices del piso (rectángulo)
    private final float[] vertices = {
            -5.0f, 0.0f, -5.0f,  // Esquina inferior izquierda
            5.0f, 0.0f, -5.0f,  // Esquina inferior derecha
            5.0f, 0.0f,  5.0f,  // Esquina superior derecha
            -5.0f, 0.0f,  5.0f   // Esquina superior izquierda
    };

    // Código del shader de vértices
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    // Código del shader de fragmentos
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // Color del piso (verde)
    private final float[] color = {0.0f, 0.8f, 0.0f, 1.0f}; // Verde

    public TrackRenderer() {
        // Convertir los vértices en un buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // Compilar shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Crear un programa OpenGL
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Fondo negro
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Usar el programa de OpenGL
        GLES20.glUseProgram(program);

        // Pasar los vértices al shader
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Pasar el color al shader
        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Dibujar el piso
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        // Desactivar el manejo de vértices
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}

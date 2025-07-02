package ru.ivansuper.jasmin.animate_tools;

import android.graphics.Camera;
import android.graphics.Matrix;

/**
 * Утилитный класс для применения трансформаций (3D и перспективных)
 * к матрицам, используемым в анимациях Android UI.
 */
public class Transform {

    private static Camera mCamera;

    /**
     * Применяет 3D флип (поворот) по оси Y вокруг центра.
     */
    public static void applyTransformationFlip2(float degree, float centerX, float centerY, Matrix m) {
        if (mCamera == null) {
            mCamera = new Camera();
        }
        mCamera.save();
        mCamera.rotateY(degree);
        mCamera.getMatrix(m);
        mCamera.restore();
        m.preTranslate(-centerX, -centerY);
        m.postTranslate(centerX, centerY);
    }

    /**
     * Применяет искажение в виде "змеи" (влево или вправо).
     */
    public static void applyPolySnake(Matrix m, int width, int height, float angle, int offset) {
        int shift = Math.abs(offset);
        if (angle > 0.0f) {
            // сдвиг влево
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{shift, 0, width, 0, width, height, shift, height}, 0,
                    4
            );
        } else {
            // сдвиг вправо
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{0, 0, width - shift, 0, width - shift, height, 0, height}, 0,
                    4
            );
        }
    }

    /**
     * Применяет трансформацию куба с перспективой.
     */
    public static void applyPolyCube(Matrix m, int width, int height, float angle, int offset) {
        int shift = Math.abs(offset);
        int factor = (int) Math.abs(Math.sin(Math.toRadians(angle / 2.0)) * (height * 24 / 100.0));
        if (angle > 0.0f) {
            // Правый поворот
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{shift, factor, width, 0, width, height, shift, height - factor}, 0,
                    4
            );
        } else {
            // Левый поворот
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{0, 0, width - shift, factor, width - shift, height - factor, 0, height}, 0,
                    4
            );
        }
    }

    /**
     * Обратная кубическая трансформация, "раскручивающая" искажение.
     */
    public static void applyPolyCubeInv(Matrix m, int width, int height, float angle, int offset) {
        int shift = Math.abs(offset);
        int factor = (int) Math.abs(Math.sin(Math.toRadians(angle / 2.0)) * (height * 24 / 100.0));
        if (angle > 0.0f) {
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{shift, 0, width, factor, width, height - factor, shift, height}, 0,
                    4
            );
        } else {
            m.setPolyToPoly(
                    new float[]{0, 0, width, 0, width, height, 0, height}, 0,
                    new float[]{0, factor, width - shift, 0, width - shift, height, 0, height - factor}, 0,
                    4
            );
        }
    }
}

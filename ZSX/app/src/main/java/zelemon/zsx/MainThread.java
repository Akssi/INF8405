package zelemon.zsx;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

// FROM https://www.youtube.com/watch?v=-XOMJYZmfkw

public class MainThread extends Thread {
    private static Canvas canvas;
    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;

    MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long startTime;
                long timeMillis;
                long waitTime;
                long totalTime = 0;
                int frameCount = 0;
                long targetTime = 1000 / FPS;

                while (running) {
                    startTime = System.nanoTime();
                    canvas = null;
                    threadUpdate();

                    timeMillis = (System.nanoTime() - startTime) / 1000000;
                    waitTime = targetTime - timeMillis;

                    sleep(waitTime);

                    totalTime += System.nanoTime() - startTime;
                    frameCount++;
                    if (frameCount == FPS) {
                        averageFPS = 1000 / ((totalTime / frameCount) / 1000000);
                        frameCount = 0;
                        totalTime = 0;
                        System.out.println(averageFPS);
                    }
                }
            } catch (InterruptedException ex) {
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
            }
        }
    }

    void setRunning(boolean b) {
        running = b;
    }

    void runOnce() {
        if (!threadUpdate()) {
            threadUpdate();
        }
    }

    private boolean threadUpdate() {
        boolean updated = false;
        //try locking the canvas for pixel editing
        try {
            canvas = this.surfaceHolder.lockCanvas();
            if (canvas != null) {
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            }
            updated = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }
}
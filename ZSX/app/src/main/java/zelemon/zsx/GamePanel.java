package zelemon.zsx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// FROM https://www.youtube.com/watch?v=-XOMJYZmfkw


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Player player;
    private Game game;

    @SuppressLint("ClickableViewAccessibility")
    public GamePanel(Context context) {
        super(context);
        game = (Game) context;


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);
        this.player = new Player(new Rect(0, 100, 100, 0), game.playerColor, new Float2(0.5f, 0.5f));
        this.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                System.out.println("Swipe left in game panel");
                StringBuilder sb = new StringBuilder();
                sb.append("Enemy number: ");
                sb.append(game.mParticipantEnemy.size());
                Log.v("ZSX", sb.toString());
                player.updateDirection(new Point(-1, 0));
            }

            @Override
            public void onSwipeRight() {
                System.out.println("Swipe right in game panel");
                player.updateDirection(new Point(1, 0));
            }

            @Override
            public void onSwipeUp() {
                System.out.println("Swipe up in game panel");
                player.updateDirection(new Point(0, -1));
            }

            @Override
            public void onSwipeDown() {
                System.out.println("Swipe down in game panel");
                player.updateDirection(new Point(0, 1));
            }
        });

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        player.updateScreenDim(new Point(width, height));
        for (Enemy e : game.mParticipantEnemy.values()) {
            e.updateScreenDim(new Point(width, height));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Width: ");
        sb.append(width);
        sb.append("Height: ");
        sb.append(height);
        Log.i("ZSX", sb.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    public void update() {
        player.update();
        for (Enemy enemy : game.mParticipantEnemy.values()) {
            enemy.update();
        }
        game.broadcastPosition(player);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        player.draw(canvas);
        for (Enemy enemy : game.mParticipantEnemy.values()) {
            enemy.draw(canvas);
        }
    }
}
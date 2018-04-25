package zelemon.zsx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.renderscript.Int2;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// FROM https://www.youtube.com/watch?v=-XOMJYZmfkw


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Player player;
    private Game game;
    private boolean isRunning;


    @SuppressLint("ClickableViewAccessibility")
    public GamePanel(Context context, Int2 gridSize, Int2 playerPosition) {
        super(context);
        game = (Game) context;
        setId(R.id.game_panel);


        //add the callback to the surface holder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);
        this.player = new Player(game.playerColor, playerPosition, gridSize);
        this.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
//                System.out.println("Swipe left in game panel");
                player.updateDirection(new Point(-1, 0));
                //game.broadcastTurnReliable(player);
            }

            @Override
            public void onSwipeRight() {
//                System.out.println("Swipe right in game panel");
                player.updateDirection(new Point(1, 0));
                //game.broadcastTurnReliable(player);
            }

            @Override
            public void onSwipeUp() {
//                System.out.println("Swipe up in game panel");
                player.updateDirection(new Point(0, -1));
                //game.broadcastTurnReliable(player);
            }

            @Override
            public void onSwipeDown() {
//                System.out.println("Swipe down in game panel");
                player.updateDirection(new Point(0, 1));
                //game.broadcastTurnReliable(player);
            }
        });

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    public void stopGameUpdate() {
        if (isRunning) {
            stopThread();
            isRunning = false;
//            thread.runOnce();
            Log.i("ZSX", "Game loop stopped");
        }
    }

    private void stopThread() {
        thread.setRunning(false);
        thread.interrupt();
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
        sb.append(" Height: ");
        sb.append(height);
        Log.i("ZSX", sb.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            stopThread();
            retry = false;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();
        StringBuilder sb = new StringBuilder();
        sb.append("Surface created: Width: ");
        sb.append(width);
        sb.append(" Height: ");
        sb.append(height);
        Log.i("ZSX", sb.toString());
        player.updateScreenDim(new Point(width, height));
        player.resetPlayer();
        for (Enemy e : game.mParticipantEnemy.values()) {
            e.updateScreenDim(new Point(width, height));
            e.resetEnemy();
        }
        thread.runOnce();
    }

    public void update() {

        player.update();
        for (Enemy enemy : game.mParticipantEnemy.values()) {
            enemy.update();
            for (int i = 0; i < enemy.getTrailPos().size(); i++) {
                Int2 pos = enemy.getTrailPos().get(i);

                if(player.getPlayerPosition().x == pos.x && player.getPlayerPosition().y == pos.y)
                {
                    // Collision
                    Log.i("ZSX", "COLLISION with enemy");
                    game.broadcastCollision();
                    stopGameUpdate();
                    break;
                }
            }

            if (player.getPlayerPosition().x == enemy.getEnemyPosition().x && player.getPlayerPosition().y == enemy.getEnemyPosition().y) {
                // Collision
                game.broadcastCollision();
                stopGameUpdate();
            }

        }

        for (int i = 0; i < player.getTrailPos().size(); i++) {
            Int2 pos = player.getTrailPos().get(i);
            if (player.getPlayerPosition().x == pos.x && player.getPlayerPosition().y == pos.y) {
                // Collision
                Log.i("ZSX", "COLLISION with self");
                game.broadcastCollision();
                stopGameUpdate();
                break;
            }
        }

        game.broadcastPosition(player);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawColor(Color.BLACK);
        player.draw(canvas);
        for (Enemy enemy : game.mParticipantEnemy.values()) {
            enemy.draw(canvas);
        }
    }

    public void startGameUpdate() {
        if (!isRunning) {

            player.resetPlayer();
            for (Enemy e : game.mParticipantEnemy.values()) {
                e.resetEnemy();
            }
            //we can safely start the game loop
            thread.setRunning(true);
            thread.start();
            isRunning = true;
        }
    }
}
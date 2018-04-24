package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;
import android.renderscript.Int2;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.math.MathUtils.clamp;

/**
 * Created by youri on 2018-03-08.
 */

public class Player implements GameObject {

    private Rect playerSprite;
    private int playerColor;
    private float speed = 0.005f;
    private Point screenSize = new Point(1440, 2560);
    private List<Rect> trail;
    private List<Int2> trailPos;
    private Int2 playerPosition;
    private Point direction = new Point(1, 0);
    private Int2 gridSize;
    private Float2 pixelPerSquare;
    private boolean inCollision = false;

    public Player(int playerColor, Int2 playerPosition, Int2 gridSize) {
        this.playerSprite = new Rect(0, 1, 0, 1);
        this.playerColor = playerColor;
        this.playerPosition = playerPosition;
        this.trail = new ArrayList<>();
        this.trailPos = new ArrayList<>();
        this.gridSize = gridSize;

        if (playerPosition.x == gridSize.x/2) {
            this.direction.x = 0;
        }
        else if (playerPosition.x < gridSize.x/2) {
            this.direction.x = 1;
        }
        else {
            this.direction.x = -1;
        }
        if (playerPosition.y == gridSize.y/2) {
            this.direction.y = 0;
        }
        else if (playerPosition.y < gridSize.y/2) {
            this.direction.y = 1;
        }
        else {
            this.direction.y = -1;
        }
    }

    public Int2 getPlayerPosition() {
        return playerPosition;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(playerColor);
        canvas.drawRect(playerSprite, paint);

        paint.setAlpha(50);
        for(Rect rect : trail)
            canvas.drawRect(rect, paint);
    }

    @Override
    public void update() {
        if (direction.x != 0 || direction.y != 0) {
            // Add old position to trail
            if (!trail.isEmpty()) {
                Int2 lastTrailPos = trailPos.get(trailPos.size() - 1);
                if (lastTrailPos.x != playerPosition.x || lastTrailPos.y != playerPosition.y) {
                    trail.add(new Rect(playerSprite));
                    trailPos.add(new Int2(playerPosition.x, playerPosition.y));
                }
            } else {
                trail.add(new Rect(playerSprite));
                trailPos.add(new Int2(playerPosition.x, playerPosition.y));
            }
        }

        // Update player pos
        playerPosition.x += direction.x;
        playerPosition.y += direction.y;
        Int2 newPlayerPosition = new Int2(clamp(playerPosition.x, 0, gridSize.x - 1),
                clamp(playerPosition.y, 0, gridSize.y - 1));
        if (newPlayerPosition != playerPosition) {
            playerPosition = newPlayerPosition;
        }

        playerSprite = new Rect((int) (playerPosition.x * pixelPerSquare.x),
                (int) (playerPosition.y * pixelPerSquare.y),
                (int) ((playerPosition.x + 1) * pixelPerSquare.x),
                (int) ((playerPosition.y + 1) * pixelPerSquare.y));
    }

    public void updateDirection(Point direction) {
        if (this.direction.x == -direction.x && this.direction.y == -direction.y) {
            return;
        }
        this.direction = direction;
    }

    public void updateScreenDim(Point newScreenDim) {
        pixelPerSquare = new Float2(newScreenDim.x / (float) (gridSize.x), (newScreenDim.y / (float) (gridSize.y)));
//        Log.i("ZSX", "Screen dim update");
    }

    public void resetPlayer() {
        this.trail = new ArrayList<>();
        this.trailPos = new ArrayList<>();
    }

    public List<Int2> getTrailPos(){
        return this.trailPos;
    }

    public boolean IsInCollision() {
        return this.inCollision;
    }

    public void SetInCollision(boolean value) {
        this.inCollision = value;
    }
}

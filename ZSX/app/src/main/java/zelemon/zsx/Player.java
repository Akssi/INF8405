package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;

import static android.support.v4.math.MathUtils.clamp;

/**
 * Created by youri on 2018-03-08.
 */

public class Player implements GameObject {

    private Rect playerSprite;
    private int playerColor;
    private Float2 playerPosition;
    private Point direction = new Point(0, -1);
    private float speed = 0.005f;
    private Point screenSize = new Point(1440, 2560);

    public Player(Rect playerSprite, int playerColor, Float2 playerPosition) {
        this.playerSprite = playerSprite;
        this.playerColor = playerColor;
        this.playerPosition = playerPosition;
    }

    public Float2 getPlayerPosition() {
        return playerPosition;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(playerColor);
        canvas.drawRect(playerSprite, paint);
    }

    @Override
    public void update() {
        playerPosition.x += direction.x * speed;
        playerPosition.y += direction.y * speed;
        Float2 newPlayerPosition = new Float2(clamp(playerPosition.x, (playerSprite.width() / 2 + 1) / (float) screenSize.x, (screenSize.x - playerSprite.width() / 2 - 1) / (float) screenSize.x),
                clamp(playerPosition.y, (playerSprite.height() / 2 + 1) / (float) screenSize.y, (screenSize.y - playerSprite.height() / 2 - 1) / (float) screenSize.y));
        if (newPlayerPosition != playerPosition) {
            playerPosition = newPlayerPosition;
        }
        playerSprite = new Rect((int) (playerPosition.x * screenSize.x) - playerSprite.width() / 2,
                (int) (playerPosition.y * screenSize.y) + playerSprite.height() / 2,
                (int) (playerPosition.x * screenSize.x) + playerSprite.width() / 2,
                (int) (playerPosition.y * screenSize.y) - playerSprite.height() / 2);
    }

    public void updateDirection(Point direction) {
        this.direction = direction;
    }

    public void updateScreenDim(Point newScreenDim) {
//        screenSize = newScreenDim;
    }
}

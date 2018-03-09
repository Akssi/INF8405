package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import static android.support.v4.math.MathUtils.clamp;

/**
 * Created by youri on 2018-03-08.
 */

public class Player implements GameObject {

    private Rect playerSprite;
    private int playerColor;
    private Point playerPosition;
    private Point direction = new Point(0, -1);
    private int speed = 1;
    private Point screenSize = new Point(1440, 2560);

    public Player(Rect playerSprite, int playerColor, Point playerPosition, Point screenSize) {
        this.playerSprite = playerSprite;
        this.playerColor = playerColor;
        this.playerPosition = playerPosition;
        this.screenSize = screenSize;
    }

    public Point getPlayerPosition() {
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
        Point newPlayerPosition = new Point(clamp(playerPosition.x, playerSprite.width() / 2 + 1, screenSize.x - playerSprite.width() / 2 - 1),
                clamp(playerPosition.y, playerSprite.height() / 2 + 1, screenSize.y - playerSprite.height() / 2 - 1));
        if (newPlayerPosition != playerPosition) {

//            int xDelta = newPlayerPosition.x-playerPosition.x;
//            int yDelta = newPlayerPosition.y-playerPosition.y;
//            if(playerPosition.x < )
//            Point stuckDelta = new Point(xDelta, yDelta);
            playerPosition = newPlayerPosition;
        }
        playerSprite = new Rect(playerPosition.x - playerSprite.width() / 2, playerPosition.y + playerSprite.height() / 2, playerPosition.x + playerSprite.width() / 2, playerPosition.y - playerSprite.height() / 2);
    }

    public void updateDirection(Point direction) {
        this.direction = direction;
    }

    public void updateScreenDim(Point newScreenDim) {
//        screenSize = newScreenDim;
    }
}

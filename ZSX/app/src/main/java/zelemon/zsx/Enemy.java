package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by youri on 2018-03-08.
 */

public class Enemy implements GameObject {

    private Rect enemySprite;
    private int enemyColor;
    private Point enemyPosition;
    private Point screenSize = new Point(1440, 2560);


    public Enemy(Rect enemySprite, int enemyColor, Point enemyPosition, Point screenSize) {
        this.enemySprite = enemySprite;
        this.enemyColor = enemyColor;
        this.enemyPosition = enemyPosition;
        this.screenSize = screenSize;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(enemyColor);
        canvas.drawRect(enemySprite, paint);
    }

    @Override
    public void update() {
        enemySprite = new Rect(enemyPosition.x - enemySprite.width() / 2, enemyPosition.y + enemySprite.height() / 2, enemyPosition.x + enemySprite.width() / 2, enemyPosition.y - enemySprite.height() / 2);
    }

    public void setEnemyPosition(Point enemyPosition) {
        this.enemyPosition = enemyPosition;
    }

    public void updateScreenDim(Point newScreenDim) {
//        screenSize = newScreenDim;
    }
}

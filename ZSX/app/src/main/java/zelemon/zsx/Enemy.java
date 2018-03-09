package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;

/**
 * Created by youri on 2018-03-08.
 */

public class Enemy implements GameObject {

    private Rect enemySprite;
    private int enemyColor;
    private Float2 enemyPosition;
    private Point screenSize = new Point(1440, 2560);


    public Enemy(Rect enemySprite, int enemyColor, Float2 enemyPosition) {
        this.enemySprite = enemySprite;
        this.enemyColor = enemyColor;
        this.enemyPosition = enemyPosition;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(enemyColor);
        canvas.drawRect(enemySprite, paint);
    }

    @Override
    public void update() {
        enemySprite = new Rect((int) (enemyPosition.x * screenSize.x) - enemySprite.width() / 2,
                (int) (enemyPosition.y * screenSize.y) + enemySprite.height() / 2,
                (int) (enemyPosition.x * screenSize.x) + enemySprite.width() / 2,
                (int) (enemyPosition.y * screenSize.y) - enemySprite.height() / 2);
    }

    public void setEnemyPosition(Float2 enemyPosition) {

        this.enemyPosition = enemyPosition;
    }

    public void updateScreenDim(Point newScreenDim) {
//        screenSize = newScreenDim;
    }
}

package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;
import android.renderscript.Int2;
import android.util.Log;

import static android.support.v4.math.MathUtils.clamp;

/**
 * Created by youri on 2018-03-08.
 */

public class Enemy implements GameObject {

    private Rect enemySprite;
    private int enemyColor;
    private Int2 enemyPosition;
    private Int2 gridSize;
    private Float2 pixelPerSquare;


    public Enemy(int enemyColor, Int2 enemyPosition, Int2 gridSize) {
        this.enemySprite = new Rect(0,1,0,1);
        this.enemyColor = enemyColor;
        this.enemyPosition = enemyPosition;
        this.gridSize = gridSize;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(enemyColor);
        canvas.drawRect(enemySprite, paint);
    }

    @Override
    public void update() {
        Int2 newPlayerPosition = new Int2(clamp(enemyPosition.x, 0, gridSize.x-1),
                clamp(enemyPosition.y, 0, gridSize.y-1));
        if (newPlayerPosition != enemyPosition)
            enemyPosition = newPlayerPosition;

        enemySprite = new Rect((int)(enemyPosition.x * pixelPerSquare.x),
                (int)(enemyPosition.y * pixelPerSquare.y),
                (int)((enemyPosition.x + 1) * pixelPerSquare.x),
                (int)((enemyPosition.y + 1) * pixelPerSquare.y));
    }

    public void setEnemyPosition(Int2 enemyPosition) {

        this.enemyPosition = enemyPosition;
    }

    public Int2 getEnemyPosition() {
        return enemyPosition;
    }

    public void updateScreenDim(Point newScreenDim) {
        pixelPerSquare = new Float2(newScreenDim.x/(float)(gridSize.x),(newScreenDim.y/(float)(gridSize.y)));
//        Log.i("ZSX", "Screen dim update");
    }
}

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
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by youri on 2018-03-08.
 */

public class Enemy implements GameObject {

    private Rect enemySprite;
    private int enemyColor;
    private Point screenSize = new Point(1440, 2560);
    private List<Rect> trail;
    private List<Int2> trailPos;
    private Int2 enemyPosition;
    private Int2 gridSize;
    private Float2 pixelPerSquare;


    public Enemy(int enemyColor, Int2 enemyPosition, Int2 gridSize) {
        this.enemySprite = new Rect(0,1,0,1);
        this.enemyColor = enemyColor;
        this.enemyPosition = enemyPosition;
        this.gridSize = gridSize;
        this.trail = new ArrayList<>();
        this.trailPos = new ArrayList<>();
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(enemyColor);
        canvas.drawRect(enemySprite, paint);

        paint.setAlpha(50);
        for(Rect rect : trail)
            canvas.drawRect(rect, paint);
    }

    @Override
    public void update() {
        // Add old position to trail
        if (!trail.isEmpty()) {
            Int2 lastTrailPos = trailPos.get(trailPos.size() - 1);
            if (lastTrailPos.x != enemyPosition.x || lastTrailPos.y != enemyPosition.y) {
                trail.add(new Rect(enemySprite));
                trailPos.add(new Int2(enemyPosition.x, enemyPosition.y));
            }
        } else {
            trail.add(new Rect(enemySprite));
            trailPos.add(new Int2(enemyPosition.x, enemyPosition.y));
        }

        Int2 newPlayerPosition = new Int2(clamp(enemyPosition.x, 0, gridSize.x-1),
                clamp(enemyPosition.y, 0, gridSize.y-1));
        if (newPlayerPosition != enemyPosition)
            enemyPosition = newPlayerPosition;

        enemySprite = new Rect((int)(enemyPosition.x * pixelPerSquare.x),
                (int)(enemyPosition.y * pixelPerSquare.y),
                (int)((enemyPosition.x + 1) * pixelPerSquare.x),
                (int)((enemyPosition.y + 1) * pixelPerSquare.y));
    }

    public Int2 getEnemyPosition() {
        return enemyPosition;
    }

    public void setEnemyPosition(Int2 enemyPosition) {

        this.enemyPosition = enemyPosition;
        if (!trailPos.isEmpty()) {
            // Fill trail with missing info
            Int2 lastTrailPos = trailPos.get(trailPos.size() - 1);
            if (lastTrailPos.x == enemyPosition.x && lastTrailPos.y != enemyPosition.y) {
                int minY = min(lastTrailPos.y, enemyPosition.y);
                int maxY = max(lastTrailPos.y, enemyPosition.y);
                for (int i = minY; i < maxY; i++) {
                    trailPos.add(new Int2(enemyPosition.x, i));
                }
                trail.add(new Rect((int) (enemyPosition.x * pixelPerSquare.x),
                        (int) (minY * pixelPerSquare.y),
                        (int) ((enemyPosition.x + 1) * pixelPerSquare.x),
                        (int) ((maxY) * pixelPerSquare.y)));
            }
            if (lastTrailPos.y == enemyPosition.y && lastTrailPos.x != enemyPosition.x) {
                int minX = min(lastTrailPos.x, enemyPosition.x);
                int maxX = max(lastTrailPos.x, enemyPosition.x);
                for (int i = minX; i < maxX; i++) {
                    trailPos.add(new Int2(i, enemyPosition.y));
                }
                trail.add(new Rect((int) (minX * pixelPerSquare.x),
                        (int) (enemyPosition.y * pixelPerSquare.y),
                        (int) ((maxX) * pixelPerSquare.x),
                        (int) ((enemyPosition.y + 1) * pixelPerSquare.y)));
            }
        }
    }

    public void updateScreenDim(Point newScreenDim) {
        pixelPerSquare = new Float2(newScreenDim.x/(float)(gridSize.x),(newScreenDim.y/(float)(gridSize.y)));
//        Log.i("ZSX", "Screen dim update");
    }

    public List<Int2> getTrailPos(){
        return this.trailPos;
    }

    public void resetEnemy() {
        this.trail = new ArrayList<>();
        this.trailPos = new ArrayList<>();
    }
}

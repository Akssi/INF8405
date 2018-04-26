package zelemon.zsx;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.renderscript.Float2;
import android.renderscript.Int2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        this.enemyColor = enemyColor;
        this.enemyPosition = enemyPosition;
        this.enemySprite = new Rect(0, 1, 0, 1);
        this.gridSize = gridSize;
        this.trail = new CopyOnWriteArrayList<>();
        this.trailPos = new CopyOnWriteArrayList<>();
        this.pixelPerSquare = new Float2(1, 1);

        enemySprite = new Rect((int) (enemyPosition.x * pixelPerSquare.x),
                (int) (enemyPosition.y * pixelPerSquare.y),
                (int) ((enemyPosition.x + 1) * pixelPerSquare.x),
                (int) ((enemyPosition.y + 1) * pixelPerSquare.y));

    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(enemyColor);
        canvas.drawRect(enemySprite, paint);

        paint.setAlpha(150);

        //for(Rect rect : trail)
        for (int i = 0; i < trail.size(); i++)
            canvas.drawRect(trail.get(i), paint);
    }

    @Override
    public void update() {
        enemySprite = new Rect((int) (enemyPosition.x * pixelPerSquare.x),
                (int) (enemyPosition.y * pixelPerSquare.y),
                (int) ((enemyPosition.x + 1) * pixelPerSquare.x),
                (int) ((enemyPosition.y + 1) * pixelPerSquare.y));

    }

    public Int2 getEnemyPosition() {
        return enemyPosition;
    }

    public void setEnemyPosition(Int2 newEnemyPosition) {
        // Add old position to trail
        trail.add(new Rect(new Rect((int) (enemyPosition.x * pixelPerSquare.x),
                (int)(enemyPosition.y * pixelPerSquare.y),
                (int)((enemyPosition.x + 1) * pixelPerSquare.x),
                (int) ((enemyPosition.y + 1) * pixelPerSquare.y))));
        trailPos.add(new Int2(this.enemyPosition.x, this.enemyPosition.y));

        this.enemyPosition = new Int2(clamp(newEnemyPosition.x, 0, gridSize.x - 1),
                clamp(newEnemyPosition.y, 0, gridSize.y - 1));




        if (!trailPos.isEmpty()) {
            // Fill trail with missing info
            Int2 lastTrailPos = trailPos.get(trailPos.size() - 1);

            if (lastTrailPos.x - newEnemyPosition.x == 1 && lastTrailPos.y - newEnemyPosition.y > 1) {
                int minY = min(lastTrailPos.y, newEnemyPosition.y) + 1;
                int maxY = max(lastTrailPos.y, newEnemyPosition.y);

                for (int i = minY; i < maxY; i++) {
                    trailPos.add(new Int2(newEnemyPosition.x, i));
                    trail.add(new Rect((int) (newEnemyPosition.x * pixelPerSquare.x),
                            (int) (i * pixelPerSquare.y),
                            (int) ((newEnemyPosition.x + 1) * pixelPerSquare.x),
                            (int) ((i + 1) * pixelPerSquare.y)));
                }
            }
            if (lastTrailPos.y - newEnemyPosition.y == 1 && lastTrailPos.x - newEnemyPosition.x > 1) {
                int minX = min(lastTrailPos.x, newEnemyPosition.x) + 1;
                int maxX = max(lastTrailPos.x, newEnemyPosition.x);
                for (int i = minX; i < maxX; i++) {
                    trailPos.add(new Int2(i, newEnemyPosition.y));
                    trail.add(new Rect((int) (i * pixelPerSquare.x),
                            (int) (enemyPosition.y * pixelPerSquare.y),
                            (int) ((i + 1) * pixelPerSquare.x),
                            (int) ((enemyPosition.y + 1) * pixelPerSquare.y)));
                }
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

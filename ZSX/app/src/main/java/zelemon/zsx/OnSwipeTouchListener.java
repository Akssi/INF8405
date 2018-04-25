package zelemon.zsx;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private static final float MIN_DISTANCE = 100;
    public Context context;
    private GestureDetector gestureDetector;
    private float y1;
    private float x1;
    private float x2;
    private float y2;
    private boolean moved;

    public OnSwipeTouchListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = motionEvent.getX();
                y1 = motionEvent.getY();
                moved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (moved) {
                    return true;
                }
                x2 = motionEvent.getX();
                y2 = motionEvent.getY();
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;
                float max = Math.max(Math.abs(deltaX), Math.abs(deltaY));
                if (Math.abs(deltaX) == max && Math.abs(deltaX) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        onSwipeRight();
                    }

                    // Right to left swipe action
                    else {
                        onSwipeLeft();
                    }

                } else if (Math.abs(deltaY) == max && Math.abs(deltaY) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (y2 > y1) {
                        onSwipeDown();
                    }

                    // Right to left swipe action
                    else {
                        onSwipeUp();
                    }

                }
                break;
        }
        return true;
    }

    public void onSwipeRight() {
        System.out.println("Swipe right in detector");
    }

    public void onSwipeLeft() {
        System.out.println("Swipe left in detector");
    }

    public void onSwipeUp() {
        System.out.println("Swipe up in detector");
    }

    public void onSwipeDown() {
        System.out.println("Swipe down in detector");
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 10;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
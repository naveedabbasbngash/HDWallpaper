package zs.wallpapers.site.utils;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior {

    public boolean onStartNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull View child, @NotNull View directTargetChild, @NotNull View target, int axes, int type) {
        return axes == 2;
    }

    public void onNestedPreScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull View child, @NotNull View target, int dx, int dy, @NotNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        child.setTranslationY(Math.max(0.0F, Math.min((float)child.getHeight(), child.getTranslationY() + (float)dy)));
    }

    public BottomNavigationBehavior(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
    }
}

package xyz.tanwb.airship.view.adapter.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class AlphaInAnimation implements BaseAnimation {

    private static final float DEFAULT_ALPHA_FROM = 0f;

    private final float myFrom;

    public AlphaInAnimation() {
        this(DEFAULT_ALPHA_FROM);
    }

    public AlphaInAnimation(float from) {
        myFrom = from;
    }

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[]{ObjectAnimator.ofFloat(view, "alpha", myFrom, 1f)};
    }
}

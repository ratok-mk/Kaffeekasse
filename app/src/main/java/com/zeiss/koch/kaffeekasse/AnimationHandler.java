package com.zeiss.koch.kaffeekasse;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;

public class AnimationHandler {
    static void highlight(Context ctx, View view) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.highlight);
        set.setTarget(view);
        set.start();
    }
}

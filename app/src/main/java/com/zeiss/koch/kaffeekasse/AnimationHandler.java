package com.zeiss.koch.kaffeekasse;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AnimationHandler {
    static void highlight(Context ctx, View view) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, R.animator.highlight);
        set.setTarget(view);
        set.start();
    }

    static void setIconRotation(Context context, ImageView iconImage, RelativeLayout layout, boolean animationState) {
        if (animationState) {
            RotateAnimation animation = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(2000);
            iconImage.startAnimation(animation);
            layout.setBackgroundColor(context.getColor(R.color.nfc_aware));
        } else {
            iconImage.clearAnimation();
            layout.setBackgroundColor(context.getColor(R.color.button_grey));
        }
    }
}

package com.jlyx.app.controllers

import android.view.View
import android.view.animation.*

class AnimationController {
    companion object {
        //按钮缩放动画
        private val mScaleAnimation =
            ScaleAnimation(
                1f,
                0.5f,
                1f,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 200L
            }
        //按钮重复缩放
        private val mRepeatScaleAnimationSet =
            AnimationSet(true).apply {
                addAnimation(ScaleAnimation(
                    1f,
                    0.5f,
                    1f,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                ).apply {
                    duration = 1000L
                    repeatMode = ScaleAnimation.REVERSE
                    repeatCount = ScaleAnimation.INFINITE
                })
                addAnimation(
                    ScaleAnimation(
                        1f,
                        1.2f,
                        1f,
                        1.2f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    ).apply {
                        startOffset = 1000L
                        duration = 1000L
                        repeatMode = ScaleAnimation.REVERSE
                        repeatCount = ScaleAnimation.INFINITE
                    }
                )
                interpolator = AccelerateInterpolator()
            }


        /**
         *@function: 播放缩放动画
         *@param:
         *@return:
         */
        fun startScaleAnimation(view: View?) {
            view?.apply {
                startAnimation(mScaleAnimation)
            }
        }

        fun startRepeatScaleAnimation(view: View?) {
            view?.apply {
                startAnimation(mRepeatScaleAnimationSet)
            }
        }

        fun cancelRepeatScaleAnimation(view: View?) {
            view?.apply {
                mRepeatScaleAnimationSet.apply {
                    cancel()
                    reset()
                }
            }
        }

        //重复平移动画
        private val mTranlationYForUpRepeatAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -0.5f
        ).apply {
            duration = 800
            repeatCount = TranslateAnimation.INFINITE
            interpolator = AccelerateInterpolator()
        }

        fun startRepeatTranslateYUpAnimation(view: View?) {
            view?.apply {
                startAnimation(mTranlationYForUpRepeatAnimation)
            }
        }
        fun cancelRepeatTranslateYUpAnimation(view: View?) {
            view?.apply {
                mTranlationYForUpRepeatAnimation.apply {
                    cancel()
                    reset()
                }
            }
        }
    }

}
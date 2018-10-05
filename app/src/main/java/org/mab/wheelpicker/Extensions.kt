import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

fun View.zoomIn(onAnimationStart: () -> Unit = {}, onAnimationEnd: () -> Unit = {}, duration: Long = 500, offset: Long = 0, fromScale: Float = 1f, toScale: Float = 1.5f) {
    val anim = ScaleAnimation(fromScale, toScale, fromScale, toScale, width / 2.0f, height / 2.0f)
    ScaleAnimation(fromScale, toScale, fromScale, toScale)
    anim.duration = duration
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {
        }

        override fun onAnimationStart(p0: Animation?) {
            onAnimationStart.invoke()
            this@zoomIn.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation) {
            onAnimationEnd.invoke()
        }
    })
    anim.startOffset = offset
    anim.fillAfter = true
    this.startAnimation(anim)
}

fun View.zoomOut(onAnimationStart: () -> Unit = {}, onAnimationEnd: () -> Unit = {}, duration: Long = 500, offset: Long = 0, fromScale: Float = 1.5f, toScale: Float = 1f) {
    val anim = ScaleAnimation(fromScale, toScale, fromScale, toScale, width / 2.0f, height / 2.0f)
    anim.duration = duration
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {
        }

        override fun onAnimationStart(p0: Animation?) {
            onAnimationStart.invoke()
            this@zoomOut.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation) {
            onAnimationEnd.invoke()
        }
    })
    anim.startOffset = offset
    anim.fillAfter = true
    this.startAnimation(anim)
}
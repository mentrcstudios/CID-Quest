package com.mentricstudios.cidquest.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.mentricstudios.cidquest.R
import java.io.File
import java.io.FileOutputStream

/**
 * Lets the player swap the default "Me" photo used for their in-maze
 * character for one of their own, picked from the gallery. The picked image
 * is immediately copied into the app's private storage (not just a
 * remembered content:// Uri) — gallery-picker Uris aren't guaranteed to
 * stay readable across app restarts/reboots, but a file we own always is.
 *
 * The player/guard [ImageBitmap]s used every gameplay frame are decoded,
 * cropped, and circle-masked exactly once per process and cached here —
 * without this, re-entering [screens.MazeGameScreen] for every new level
 * (a fresh composable each time) would re-decode and re-process all 4
 * images from scratch on every single level transition.
 */
object CharacterPhoto {

    private const val FILE_NAME = "player_photo.jpg"
    private const val MAX_DIMENSION = 512

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    fun hasCustomPhoto(context: Context): Boolean = file(context).exists()

    /** Copies the picked image into private storage, downscaled defensively. Returns success. */
    fun saveFromUri(context: Context, uri: Uri): Boolean {
        val saved = try {
            val input = context.contentResolver.openInputStream(uri) ?: return false
            val original = input.use { BitmapFactory.decodeStream(it) } ?: return false
            val largestSide = maxOf(original.width, original.height)
            val scaled = if (largestSide > MAX_DIMENSION) {
                val scale = MAX_DIMENSION.toFloat() / largestSide
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt().coerceAtLeast(1),
                    (original.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                original
            }
            FileOutputStream(file(context)).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            true
        } catch (e: Exception) {
            false
        }
        if (saved) cachedPlayerPhoto = null // invalidate — next access re-decodes the new photo
        return saved
    }

    fun clearCustomPhoto(context: Context) {
        file(context).delete()
        cachedPlayerPhoto = null
    }

    // --- Cached, ready-to-draw circular avatars -----------------------

    private var cachedPlayerPhoto: ImageBitmap? = null
    private var cachedEnemyPhotos: List<ImageBitmap>? = null

    /** The player's in-maze avatar: their custom photo if set, else the bundled default. Cached after first call. */
    fun playerAvatar(context: Context): ImageBitmap? {
        cachedPlayerPhoto?.let { return it }
        val f = file(context)
        val bitmap = if (f.exists()) {
            try { BitmapFactory.decodeFile(f.path) } catch (e: Exception) { null }
        } else null
            ?: BitmapFactory.decodeResource(context.resources, R.drawable.player_default)
        val avatar = bitmap?.let { toCircularAvatar(it) }
        cachedPlayerPhoto = avatar
        return avatar
    }

    /** The three fixed guard avatars, in order. Cached after first call — these never change at runtime. */
    fun enemyAvatars(context: Context): List<ImageBitmap> {
        cachedEnemyPhotos?.let { return it }
        val avatars = listOf(R.drawable.enemy_1, R.drawable.enemy_2, R.drawable.enemy_3).mapNotNull { resId ->
            try {
                BitmapFactory.decodeResource(context.resources, resId)?.let { toCircularAvatar(it) }
            } catch (e: Exception) {
                null
            }
        }
        cachedEnemyPhotos = avatars
        return avatars
    }

    /**
     * Center-crops to a square, then bakes a circular alpha mask into the
     * bitmap itself. Doing this once here — instead of clipping to a circle
     * every draw call — means the actual per-frame gameplay draw is a plain
     * [androidx.compose.ui.graphics.drawscope.DrawScope.drawImage] with no
     * per-frame Path allocation, which matters with up to 6 guards + the
     * player on screen at once at 60fps.
     */
    private fun toCircularAvatar(source: Bitmap): ImageBitmap {
        val size = minOf(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, -x.toFloat(), -y.toFloat(), paint)
        return output.asImageBitmap()
    }
}

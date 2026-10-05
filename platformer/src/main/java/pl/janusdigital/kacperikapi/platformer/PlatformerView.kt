package pl.janusdigital.kacperikapi.platformer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val GAME_H = 720f
private const val PLAYER_W = 52f
private const val PLAYER_H = 78f

private enum class GameState { TITLE, PLAYING, LEVEL_COMPLETE, GAME_OVER, WON }

private data class Platform(val x: Float, val y: Float, val w: Float, val h: Float)
private data class EnemySpawn(val x: Float, val y: Float, val minX: Float, val maxX: Float)
private data class Level(
    val name: String,
    val subtitle: String,
    val worldWidth: Float,
    val startX: Float,
    val startY: Float,
    val platforms: List<Platform>,
    val coins: List<Pair<Float, Float>>,
    val enemies: List<EnemySpawn>,
    val goalX: Float,
    val goalY: Float,
    val theme: Int
)

private class Coin(var x: Float, var y: Float, var collected: Boolean = false)
private class Enemy(
    var x: Float,
    var y: Float,
    val minX: Float,
    val maxX: Float,
    var dir: Float = 1f,
    var alive: Boolean = true
)

class PlatformerView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val prefs = context.getSharedPreferences("platformer_progress", Context.MODE_PRIVATE)
    private val levels = createLevels()

    private var state = GameState.TITLE
    private var levelIndex = 0
    private var highestUnlocked = prefs.getInt("highest_unlocked", 0).coerceIn(0, levels.lastIndex)
    private var lives = 3
    private var score = 0
    private var levelCoins = 0

    private var playerX = 120f
    private var playerY = 430f
    private var vx = 0f
    private var vy = 0f
    private var onGround = false
    private var invulnerable = 0f
    private var cameraX = 0f
    private var kapiX = 55f
    private var kapiY = 500f
    private var runPhase = 0f

    private var leftPressed = false
    private var rightPressed = false
    private var jumpPressed = false

    private var coins = mutableListOf<Coin>()
    private var enemies = mutableListOf<Enemy>()

    private var lastFrameNanos = 0L

    init {
        isFocusable = true
        isClickable = true
        loadLevel(0, keepLives = false)
        state = GameState.TITLE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = System.nanoTime()
        var dt = if (lastFrameNanos == 0L) 0f else (now - lastFrameNanos) / 1_000_000_000f
        lastFrameNanos = now
        dt = dt.coerceIn(0f, 0.033f)

        if (state == GameState.PLAYING) update(dt)

        val scale = height.coerceAtLeast(1) / GAME_H
        val screenW = width.coerceAtLeast(1) / scale

        canvas.save()
        canvas.scale(scale, scale)

        drawSky(canvas, screenW)

        canvas.save()
        canvas.translate(-cameraX, 0f)
        drawWorld(canvas)
        canvas.restore()

        drawHud(canvas, screenW)
        drawControls(canvas, screenW)
        drawOverlay(canvas, screenW)

        canvas.restore()

        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        val level = levels[levelIndex]
        invulnerable = max(0f, invulnerable - dt)

        val targetVx = when {
            leftPressed && !rightPressed -> -330f
            rightPressed && !leftPressed -> 330f
            else -> 0f
        }
        vx += (targetVx - vx) * min(1f, dt * 12f)
        if (abs(vx) > 5f) runPhase += dt * 11f

        val previousBottom = playerY + PLAYER_H
        playerX += vx * dt
        playerX = playerX.coerceIn(0f, level.worldWidth - PLAYER_W)

        vy += 1650f * dt
        playerY += vy * dt
        onGround = false

        val newBottom = playerY + PLAYER_H
        if (vy >= 0f) {
            var bestY = Float.MAX_VALUE
            for (p in level.platforms) {
                val horizontal = playerX + PLAYER_W - 9f > p.x && playerX + 9f < p.x + p.w
                val crossedTop = previousBottom <= p.y + 10f && newBottom >= p.y
                if (horizontal && crossedTop && p.y < bestY) bestY = p.y
            }
            if (bestY != Float.MAX_VALUE) {
                playerY = bestY - PLAYER_H
                vy = 0f
                onGround = true
            }
        }

        if (playerY > GAME_H + 130f) {
            loseLife()
            return
        }

        for (coin in coins) {
            if (!coin.collected && intersects(
                    playerX, playerY, PLAYER_W, PLAYER_H,
                    coin.x - 15f, coin.y - 15f, 30f, 30f
                )
            ) {
                coin.collected = true
                levelCoins++
                score += 100
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }

        for (enemy in enemies) {
            if (!enemy.alive) continue
            enemy.x += enemy.dir * 82f * dt
            if (enemy.x <= enemy.minX) {
                enemy.x = enemy.minX
                enemy.dir = 1f
            } else if (enemy.x >= enemy.maxX) {
                enemy.x = enemy.maxX
                enemy.dir = -1f
            }

            if (intersects(playerX, playerY, PLAYER_W, PLAYER_H, enemy.x, enemy.y, 48f, 42f)) {
                val playerBottom = playerY + PLAYER_H
                if (vy > 120f && playerBottom < enemy.y + 30f) {
                    enemy.alive = false
                    vy = -460f
                    score += 250
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                } else if (invulnerable <= 0f) {
                    loseLife()
                    return
                }
            }
        }

        if (intersects(
                playerX, playerY, PLAYER_W, PLAYER_H,
                level.goalX, level.goalY, 50f, 120f
            )
        ) {
            score += 500 + levelCoins * 25
            if (levelIndex < levels.lastIndex) {
                highestUnlocked = max(highestUnlocked, levelIndex + 1)
                prefs.edit().putInt("highest_unlocked", highestUnlocked).apply()
                state = GameState.LEVEL_COMPLETE
            } else {
                state = GameState.WON
            }
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        val desiredKapiX = playerX - if (vx >= 0f) 75f else -75f
        kapiX += (desiredKapiX - kapiX) * min(1f, dt * 5.5f)
        val desiredKapiY = playerY + 35f
        kapiY += (desiredKapiY - kapiY) * min(1f, dt * 8f)

        val scale = height.coerceAtLeast(1) / GAME_H
        val screenW = width.coerceAtLeast(1) / scale
        val desiredCamera = playerX - screenW * 0.38f
        cameraX += (desiredCamera - cameraX) * min(1f, dt * 4.5f)
        cameraX = cameraX.coerceIn(0f, max(0f, level.worldWidth - screenW))
    }

    private fun loseLife() {
        lives--
        performHapticFeedback(HapticFeedbackConstants.REJECT)
        if (lives <= 0) {
            state = GameState.GAME_OVER
            leftPressed = false
            rightPressed = false
            jumpPressed = false
        } else {
            respawn()
            invulnerable = 1.4f
        }
    }

    private fun respawn() {
        val level = levels[levelIndex]
        playerX = level.startX
        playerY = level.startY
        vx = 0f
        vy = 0f
        cameraX = max(0f, playerX - 220f)
        kapiX = max(0f, playerX - 70f)
        kapiY = playerY + 35f
    }

    private fun loadLevel(index: Int, keepLives: Boolean = true) {
        levelIndex = index.coerceIn(0, levels.lastIndex)
        if (!keepLives) lives = 3
        levelCoins = 0
        val level = levels[levelIndex]
        coins = level.coins.map { Coin(it.first, it.second) }.toMutableList()
        enemies = level.enemies.map { Enemy(it.x, it.y, it.minX, it.maxX) }.toMutableList()
        respawn()
        state = GameState.PLAYING
    }

    private fun nextLevel() {
        if (levelIndex < levels.lastIndex) {
            loadLevel(levelIndex + 1, keepLives = true)
        } else {
            state = GameState.WON
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            when (state) {
                GameState.TITLE -> {
                    score = 0
                    loadLevel(highestUnlocked, keepLives = false)
                    return true
                }
                GameState.LEVEL_COMPLETE -> {
                    nextLevel()
                    return true
                }
                GameState.GAME_OVER -> {
                    score = max(0, score - 500)
                    loadLevel(levelIndex, keepLives = false)
                    return true
                }
                GameState.WON -> {
                    score = 0
                    loadLevel(0, keepLives = false)
                    return true
                }
                GameState.PLAYING -> Unit
            }
        }

        if (state != GameState.PLAYING) return true

        val scale = height.coerceAtLeast(1) / GAME_H
        val screenW = width.coerceAtLeast(1) / scale
        val skipIndex = if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) event.actionIndex else -1

        var newLeft = false
        var newRight = false
        var newJump = false

        if (event.actionMasked != MotionEvent.ACTION_UP && event.actionMasked != MotionEvent.ACTION_CANCEL) {
            for (i in 0 until event.pointerCount) {
                if (i == skipIndex) continue
                val x = event.getX(i) / scale
                val y = event.getY(i) / scale
                if (y > 545f) {
                    when {
                        x < 145f -> newLeft = true
                        x < 300f -> newRight = true
                        x > screenW - 205f -> newJump = true
                    }
                }
            }
        }

        if (newJump && !jumpPressed && onGround) {
            vy = -715f
            onGround = false
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }

        leftPressed = newLeft
        rightPressed = newRight
        jumpPressed = newJump
        return true
    }

    private fun drawSky(canvas: Canvas, screenW: Float) {
        val theme = levels[levelIndex].theme
        val colors = when (theme) {
            1 -> intArrayOf(Color.rgb(87, 188, 255), Color.rgb(210, 239, 255))
            2 -> intArrayOf(Color.rgb(112, 181, 235), Color.rgb(243, 224, 195))
            3 -> intArrayOf(Color.rgb(87, 160, 220), Color.rgb(220, 240, 248))
            4 -> intArrayOf(Color.rgb(105, 156, 210), Color.rgb(239, 206, 182))
            else -> intArrayOf(Color.rgb(71, 144, 211), Color.rgb(225, 236, 250))
        }
        paint.shader = LinearGradient(0f, 0f, 0f, 560f, colors[0], colors[1], Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, screenW, 580f, paint)
        paint.shader = null

        paint.color = Color.argb(70, 255, 255, 255)
        for (i in 0..5) {
            val x = ((i * 260f - cameraX * 0.16f) % (screenW + 320f)) - 80f
            val y = 70f + (i % 3) * 55f
            canvas.drawOval(RectF(x, y, x + 150f, y + 45f), paint)
            canvas.drawOval(RectF(x + 45f, y - 20f, x + 115f, y + 42f), paint)
        }

        paint.color = Color.rgb(111, 174, 119)
        val shift = -(cameraX * 0.18f) % 420f
        for (i in -1..6) {
            val left = shift + i * 420f
            canvas.drawOval(RectF(left, 360f, left + 520f, 640f), paint)
        }
        paint.color = Color.rgb(80, 145, 91)
        val shift2 = -(cameraX * 0.3f) % 340f
        for (i in -1..7) {
            val left = shift2 + i * 340f
            canvas.drawOval(RectF(left, 420f, left + 430f, 650f), paint)
        }
    }

    private fun drawWorld(canvas: Canvas) {
        val level = levels[levelIndex]

        for (p in level.platforms) {
            paint.color = Color.rgb(119, 78, 46)
            canvas.drawRoundRect(RectF(p.x, p.y, p.x + p.w, p.y + p.h), 12f, 12f, paint)
            paint.color = Color.rgb(76, 174, 72)
            canvas.drawRoundRect(RectF(p.x, p.y, p.x + p.w, min(p.y + 18f, p.y + p.h)), 10f, 10f, paint)

            paint.color = Color.argb(45, 255, 255, 255)
            var stoneX = p.x + 38f
            while (stoneX < p.x + p.w - 20f) {
                canvas.drawCircle(stoneX, p.y + 47f, 5f, paint)
                stoneX += 76f
            }
        }

        for (coin in coins) {
            if (!coin.collected) drawCoin(canvas, coin.x, coin.y)
        }

        for (enemy in enemies) {
            if (enemy.alive) drawEnemy(canvas, enemy)
        }

        drawGoal(canvas, level.goalX, level.goalY)
        drawKapi(canvas, kapiX, kapiY)
        drawKacper(canvas, playerX, playerY)
    }

    private fun drawCoin(canvas: Canvas, x: Float, y: Float) {
        val bob = sin((runPhase + x * .01f).toDouble()).toFloat() * 4f
        paint.color = Color.rgb(255, 193, 7)
        canvas.drawCircle(x, y + bob, 15f, paint)
        paint.color = Color.rgb(255, 233, 130)
        canvas.drawCircle(x - 4f, y - 4f + bob, 5f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(225, 143, 0)
        canvas.drawCircle(x, y + bob, 13f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawEnemy(canvas: Canvas, enemy: Enemy) {
        paint.color = Color.rgb(83, 63, 138)
        canvas.drawRoundRect(RectF(enemy.x, enemy.y + 8f, enemy.x + 48f, enemy.y + 42f), 15f, 15f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(enemy.x + 14f, enemy.y + 20f, 6f, paint)
        canvas.drawCircle(enemy.x + 34f, enemy.y + 20f, 6f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(enemy.x + 15f, enemy.y + 21f, 2.6f, paint)
        canvas.drawCircle(enemy.x + 35f, enemy.y + 21f, 2.6f, paint)
        paint.strokeWidth = 3f
        canvas.drawLine(enemy.x + 14f, enemy.y + 34f, enemy.x + 34f, enemy.y + 34f, paint)
    }

    private fun drawGoal(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(96, 65, 45)
        canvas.drawRect(x + 6f, y, x + 13f, y + 120f, paint)
        paint.color = Color.rgb(239, 68, 68)
        canvas.drawRoundRect(RectF(x + 13f, y + 8f, x + 68f, y + 48f), 5f, 5f, paint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("META", x + 40f, y + 34f, textPaint)
    }

    private fun drawKacper(canvas: Canvas, x: Float, y: Float) {
        if (invulnerable > 0f && ((invulnerable * 10).toInt() % 2 == 0)) return

        val legSwing = if (abs(vx) > 30f && onGround) sin(runPhase.toDouble()).toFloat() * 7f else 0f

        paint.color = Color.rgb(41, 78, 127)
        canvas.drawRoundRect(RectF(x + 10f, y + 45f, x + 42f, y + 68f), 8f, 8f, paint)

        paint.color = Color.rgb(32, 50, 70)
        canvas.drawRoundRect(RectF(x + 13f + legSwing, y + 64f, x + 25f + legSwing, y + 78f), 5f, 5f, paint)
        canvas.drawRoundRect(RectF(x + 29f - legSwing, y + 64f, x + 41f - legSwing, y + 78f), 5f, 5f, paint)

        paint.color = Color.rgb(244, 194, 147)
        canvas.drawCircle(x + 26f, y + 28f, 19f, paint)

        paint.color = Color.rgb(244, 212, 73)
        canvas.drawArc(RectF(x + 7f, y + 4f, x + 45f, y + 36f), 180f, 180f, true, paint)
        canvas.drawOval(RectF(x + 7f, y + 11f, x + 18f, y + 27f), paint)
        canvas.drawOval(RectF(x + 35f, y + 10f, x + 45f, y + 27f), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(40, 48, 58)
        canvas.drawRoundRect(RectF(x + 10f, y + 20f, x + 24f, y + 31f), 4f, 4f, paint)
        canvas.drawRoundRect(RectF(x + 28f, y + 20f, x + 42f, y + 31f), 4f, 4f, paint)
        canvas.drawLine(x + 24f, y + 25f, x + 28f, y + 25f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(42, 97, 187)
        canvas.drawRoundRect(RectF(x + 7f, y + 42f, x + 45f, y + 64f), 9f, 9f, paint)
        paint.color = Color.rgb(230, 61, 58)
        canvas.drawRoundRect(RectF(x + 1f, y + 45f, x + 11f, y + 63f), 5f, 5f, paint)
    }

    private fun drawKapi(canvas: Canvas, x: Float, y: Float) {
        val bodyY = y + 22f
        paint.color = Color.rgb(247, 242, 226)
        canvas.drawOval(RectF(x + 5f, bodyY, x + 55f, bodyY + 28f), paint)

        paint.color = Color.rgb(158, 85, 47)
        canvas.drawOval(RectF(x + 33f, y + 6f, x + 61f, y + 34f), paint)
        canvas.drawOval(RectF(x + 28f, y + 5f, x + 41f, y + 32f), paint)
        canvas.drawOval(RectF(x + 52f, y + 5f, x + 65f, y + 32f), paint)

        paint.color = Color.rgb(250, 244, 224)
        canvas.drawOval(RectF(x + 39f, y + 9f, x + 57f, y + 31f), paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x + 55f, y + 22f, 3.8f, paint)
        canvas.drawCircle(x + 50f, y + 16f, 2.4f, paint)

        paint.color = Color.rgb(158, 85, 47)
        canvas.drawRoundRect(RectF(x + 11f, bodyY + 22f, x + 18f, bodyY + 38f), 3f, 3f, paint)
        canvas.drawRoundRect(RectF(x + 42f, bodyY + 22f, x + 49f, bodyY + 38f), 3f, 3f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawArc(RectF(x - 12f, bodyY - 5f, x + 16f, bodyY + 22f), 155f, 120f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawHud(canvas: Canvas, screenW: Float) {
        paint.color = Color.argb(150, 12, 26, 42)
        canvas.drawRoundRect(RectF(18f, 16f, min(screenW - 18f, 610f), 84f), 20f, 20f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.WHITE
        textPaint.textSize = 23f
        canvas.drawText("Poziom " + (levelIndex + 1) + "/5  •  " + levels[levelIndex].name, 36f, 45f, textPaint)

        textPaint.textSize = 18f
        textPaint.color = Color.rgb(255, 221, 87)
        canvas.drawText("● " + levelCoins + "/" + coins.size + "   Wynik: " + score, 36f, 72f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 27f
        textPaint.color = Color.rgb(245, 84, 84)
        canvas.drawText("♥".repeat(lives.coerceAtLeast(0)), screenW - 28f, 55f, textPaint)
    }

    private fun drawControls(canvas: Canvas, screenW: Float) {
        if (state != GameState.PLAYING) return

        drawControlButton(canvas, 78f, 642f, 58f, leftPressed, "◀")
        drawControlButton(canvas, 220f, 642f, 58f, rightPressed, "▶")
        drawControlButton(canvas, screenW - 102f, 628f, 72f, jumpPressed, "↑")

        textPaint.color = Color.argb(205, 255, 255, 255)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 15f
        canvas.drawText("SKOK", screenW - 102f, 704f, textPaint)
    }

    private fun drawControlButton(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        pressed: Boolean,
        label: String
    ) {
        paint.color = if (pressed) Color.argb(215, 255, 255, 255) else Color.argb(125, 18, 35, 55)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(210, 255, 255, 255)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.FILL
        textPaint.color = if (pressed) Color.rgb(20, 52, 86) else Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 42f
        canvas.drawText(label, cx, cy + 14f, textPaint)
    }

    private fun drawOverlay(canvas: Canvas, screenW: Float) {
        when (state) {
            GameState.PLAYING -> return
            GameState.TITLE -> {
                paint.color = Color.argb(205, 10, 26, 48)
                canvas.drawRoundRect(
                    RectF(screenW * .16f, 120f, screenW * .84f, 510f),
                    30f, 30f, paint
                )
                centeredText(canvas, "KACPER & KAPI", screenW / 2f, 205f, 48f, Color.WHITE)
                centeredText(canvas, "PLATFORMOWA PRZYGODA", screenW / 2f, 255f, 30f, Color.rgb(255, 215, 70))
                centeredText(canvas, "5 poziomów • monety • przeciwnicy • Kapi pomaga!", screenW / 2f, 310f, 19f, Color.WHITE)
                centeredText(canvas, "◀ ▶  poruszanie       ↑  skok", screenW / 2f, 355f, 22f, Color.rgb(188, 225, 255))
                if (highestUnlocked > 0) {
                    centeredText(canvas, "Odblokowany poziom: " + (highestUnlocked + 1) + "/5", screenW / 2f, 405f, 18f, Color.rgb(166, 240, 180))
                }
                centeredText(canvas, "DOTKNIJ, ABY ZACZĄĆ", screenW / 2f, 466f, 27f, Color.WHITE)
            }
            GameState.LEVEL_COMPLETE -> {
                overlayPanel(canvas, screenW)
                centeredText(canvas, "POZIOM UKOŃCZONY!", screenW / 2f, 260f, 40f, Color.rgb(255, 221, 79))
                centeredText(canvas, levels[levelIndex].subtitle, screenW / 2f, 310f, 22f, Color.WHITE)
                centeredText(canvas, "Monety: " + levelCoins + "/" + coins.size + "   •   Wynik: " + score, screenW / 2f, 360f, 22f, Color.WHITE)
                centeredText(canvas, "Dotknij, aby przejść dalej", screenW / 2f, 430f, 25f, Color.rgb(184, 226, 255))
            }
            GameState.GAME_OVER -> {
                overlayPanel(canvas, screenW)
                centeredText(canvas, "OJEJ! KACPER POTRZEBUJE POMOCY", screenW / 2f, 270f, 34f, Color.rgb(255, 114, 102))
                centeredText(canvas, "Kapi już czeka przy starcie.", screenW / 2f, 330f, 23f, Color.WHITE)
                centeredText(canvas, "Dotknij, aby spróbować ponownie", screenW / 2f, 410f, 25f, Color.rgb(184, 226, 255))
            }
            GameState.WON -> {
                overlayPanel(canvas, screenW)
                centeredText(canvas, "WIELKA WYPRAWA UKOŃCZONA!", screenW / 2f, 245f, 38f, Color.rgb(255, 221, 79))
                centeredText(canvas, "Kacper i Kapi zdobyli wszystkie 5 tras.", screenW / 2f, 305f, 23f, Color.WHITE)
                centeredText(canvas, "Wynik końcowy: " + score, screenW / 2f, 355f, 26f, Color.rgb(166, 240, 180))
                centeredText(canvas, "Dotknij, aby zagrać od początku", screenW / 2f, 430f, 24f, Color.rgb(184, 226, 255))
            }
        }
    }

    private fun overlayPanel(canvas: Canvas, screenW: Float) {
        paint.color = Color.argb(218, 9, 24, 43)
        canvas.drawRoundRect(
            RectF(screenW * .17f, 175f, screenW * .83f, 500f),
            30f, 30f, paint
        )
    }

    private fun centeredText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int
    ) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = size
        textPaint.color = color
        canvas.drawText(text, x, y, textPaint)
    }

    private fun intersects(
        ax: Float, ay: Float, aw: Float, ah: Float,
        bx: Float, by: Float, bw: Float, bh: Float
    ): Boolean = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by

    private fun createLevels(): List<Level> {
        fun p(x: Int, y: Int, w: Int, h: Int = 160) =
            Platform(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())
        fun e(x: Int, y: Int, min: Int, max: Int) =
            EnemySpawn(x.toFloat(), y.toFloat(), min.toFloat(), max.toFloat())
        fun c(x: Int, y: Int) = x.toFloat() to y.toFloat()

        return listOf(
            Level(
                name = "Wieliczka – Start",
                subtitle = "Pierwsza trasa zaliczona!",
                worldWidth = 3300f,
                startX = 110f,
                startY = 420f,
                platforms = listOf(
                    p(0, 560, 760), p(900, 560, 640), p(1710, 560, 690), p(2570, 560, 730),
                    p(430, 455, 190, 28), p(1070, 420, 230, 28), p(1900, 450, 210, 28),
                    p(2260, 365, 190, 28), p(2840, 430, 220, 28)
                ),
                coins = listOf(
                    c(260, 515), c(500, 410), c(690, 515), c(980, 515), c(1180, 375),
                    c(1440, 515), c(1810, 515), c(2000, 405), c(2310, 320), c(2670, 515),
                    c(2930, 385), c(3190, 515)
                ),
                enemies = listOf(e(1120, 518, 970, 1420), e(2020, 518, 1790, 2290), e(2790, 518, 2660, 3160)),
                goalX = 3210f,
                goalY = 440f,
                theme = 1
            ),
            Level(
                name = "Kraków – Dachy",
                subtitle = "Smok został za plecami!",
                worldWidth = 3650f,
                startX = 90f,
                startY = 400f,
                platforms = listOf(
                    p(0, 560, 520), p(690, 520, 530), p(1390, 560, 430), p(1990, 505, 610), p(2780, 560, 850),
                    p(340, 430, 150, 26), p(780, 390, 170, 26), p(1120, 335, 170, 26),
                    p(1500, 435, 180, 26), p(2140, 385, 190, 26), p(2500, 320, 180, 26), p(3050, 425, 230, 26)
                ),
                coins = listOf(
                    c(180, 515), c(405, 385), c(745, 475), c(860, 345), c(1185, 290),
                    c(1490, 515), c(1585, 390), c(2080, 460), c(2235, 340), c(2585, 275),
                    c(2860, 515), c(3150, 380), c(3470, 515)
                ),
                enemies = listOf(e(820, 478, 730, 1110), e(2100, 463, 2050, 2460), e(3080, 518, 2860, 3440)),
                goalX = 3540f,
                goalY = 440f,
                theme = 2
            ),
            Level(
                name = "Tatry – Szlak",
                subtitle = "Górski szlak pokonany!",
                worldWidth = 3900f,
                startX = 100f,
                startY = 410f,
                platforms = listOf(
                    p(0, 560, 600), p(790, 540, 470), p(1460, 500, 460), p(2120, 560, 520), p(2840, 510, 420), p(3450, 560, 440),
                    p(430, 415, 150, 26), p(920, 370, 180, 26), p(1300, 310, 160, 26),
                    p(1600, 370, 180, 26), p(2220, 400, 180, 26), p(2550, 335, 170, 26),
                    p(2960, 355, 180, 26), p(3290, 300, 150, 26)
                ),
                coins = listOf(
                    c(200, 515), c(495, 370), c(870, 495), c(1010, 325), c(1370, 265),
                    c(1580, 455), c(1690, 325), c(2190, 515), c(2310, 355), c(2630, 290),
                    c(2920, 465), c(3050, 310), c(3355, 255), c(3660, 515)
                ),
                enemies = listOf(e(900, 498, 850, 1160), e(1580, 458, 1530, 1820), e(2280, 518, 2200, 2520), e(2920, 468, 2900, 3150)),
                goalX = 3790f,
                goalY = 440f,
                theme = 3
            ),
            Level(
                name = "Praga – Mosty",
                subtitle = "Mosty Pragi zdobyte!",
                worldWidth = 4150f,
                startX = 80f,
                startY = 420f,
                platforms = listOf(
                    p(0, 560, 460), p(650, 560, 480), p(1320, 520, 420), p(1930, 560, 470),
                    p(2580, 520, 520), p(3300, 560, 830),
                    p(270, 410, 150, 26), p(740, 390, 170, 26), p(1080, 330, 160, 26),
                    p(1450, 380, 190, 26), p(1820, 315, 170, 26), p(2070, 420, 180, 26),
                    p(2690, 365, 170, 26), p(2990, 300, 170, 26), p(3450, 405, 210, 26)
                ),
                coins = listOf(
                    c(170, 515), c(335, 365), c(710, 515), c(825, 345), c(1160, 285),
                    c(1400, 475), c(1540, 335), c(1905, 270), c(2050, 515), c(2160, 375),
                    c(2660, 475), c(2770, 320), c(3070, 255), c(3380, 515), c(3550, 360), c(3970, 515)
                ),
                enemies = listOf(e(760, 518, 700, 1040), e(1410, 478, 1370, 1640), e(2050, 518, 1990, 2300), e(2710, 478, 2650, 2980), e(3510, 518, 3380, 3900)),
                goalX = 4040f,
                goalY = 440f,
                theme = 4
            ),
            Level(
                name = "Wielka wyprawa",
                subtitle = "Finał!",
                worldWidth = 4550f,
                startX = 90f,
                startY = 410f,
                platforms = listOf(
                    p(0, 560, 500), p(690, 520, 420), p(1290, 560, 390), p(1870, 500, 450),
                    p(2510, 560, 390), p(3090, 500, 460), p(3740, 560, 790),
                    p(330, 410, 150, 26), p(760, 360, 170, 26), p(1130, 300, 160, 26),
                    p(1420, 410, 170, 26), p(1980, 345, 190, 26), p(2330, 285, 170, 26),
                    p(2620, 400, 170, 26), p(3190, 340, 180, 26), p(3570, 285, 160, 26),
                    p(3930, 405, 220, 26)
                ),
                coins = listOf(
                    c(180, 515), c(395, 365), c(740, 475), c(845, 315), c(1205, 255),
                    c(1350, 515), c(1500, 365), c(1930, 455), c(2070, 300), c(2410, 240),
                    c(2570, 515), c(2705, 355), c(3150, 455), c(3280, 295), c(3650, 240),
                    c(3810, 515), c(4040, 360), c(4400, 515)
                ),
                enemies = listOf(
                    e(760, 478, 730, 1020), e(1370, 518, 1340, 1580), e(1960, 458, 1930, 2200),
                    e(2590, 518, 2560, 2820), e(3180, 458, 3150, 3420), e(3870, 518, 3810, 4300)
                ),
                goalX = 4430f,
                goalY = 440f,
                theme = 5
            )
        )
    }
}

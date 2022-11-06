import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.abs

class Renderer(
    dimension: Dimension
) : JPanel() {

    private val frame = JFrame()
    private var image = BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB)

    init {
        frame.setSize(dimension.width, dimension.height)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        frame.background = BACKGROUND_COLOR
        frame.add(this)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        g?.drawImage(image, 0, 0, width, height, this)
    }

    fun setPixel(point: Point, color: Color) {
        image.setRGB(point.x, point.y, color.rgb)
    }

    fun getPixelColor(point: Point): Color = Color(image.getRGB(point.x, point.y))

    fun setLineAlgA(startPoint: Point, endPoint: Point, color: Color = BLACK) {
        var dx = 0f
        var dy = 0f
        var x = 0f
        var y = 0f

        when {
            //1 и 2
            endPoint.x - startPoint.x >= 0 && endPoint.y - startPoint.y >= 0 ||
                    endPoint.x - startPoint.x >= 0 && endPoint.y - startPoint.y <= 0 -> {
                dx = 1f
                dy = (endPoint.y - startPoint.y).toFloat() / (endPoint.x - startPoint.x)
                x = startPoint.x.toFloat()
                y = startPoint.y.toFloat()
            }
            //3 и 4
            endPoint.x - startPoint.x <= 0 && endPoint.y - startPoint.y <= 0 ||
                    endPoint.x - startPoint.x <= 0 && endPoint.y - startPoint.y >= 0 -> {
                dx = -1f
                dy = (endPoint.y - startPoint.y).toFloat() / (startPoint.x - endPoint.x)
                x = startPoint.x.toFloat()
                y = startPoint.y.toFloat()
            }
        }

        var l = startPoint.x - endPoint.x
        if (abs(startPoint.x - endPoint.x) < abs(startPoint.y - endPoint.y)) {
            val t = dy
            when {
                dy < 0 && dx < 0 ||
                        dy > 0 && dx > 0 -> {
                    dy =
                        dx
                    dx = 1 / t
                }

                dy < 0 && dx > 0 ||
                        dy > 0 && dx < 0 -> {
                    dy = -dx
                    dx = 1 / -t
                }
            }
            l = startPoint.y - endPoint.y
        }

        for (i in 0 until abs(l)) {
            setPixel(Point(x.toInt(), y.toInt()), color)
            x += dx
            y += dy
        }
    }

    fun setLineAlgB(startPoint: Point, endPoint: Point, color: Color = BLACK) {
        val dx = abs(startPoint.x - endPoint.x)
        val dy = abs(startPoint.y - endPoint.y)

        var length = if (dx > dy) dx else dy

        if (length == 0) {
            setPixel(Point(startPoint.x, startPoint.y), color)
            return
        }

        val deltaX = (endPoint.x - startPoint.x).toFloat() / length
        val deltaY = (endPoint.y - startPoint.y).toFloat() / length

        var x = startPoint.x.toFloat()
        var y = startPoint.y.toFloat()

        while (length != 0) {
            x += deltaX
            y += deltaY
            setPixel(Point(x.toInt(), y.toInt()), color)
            length--
        }
    }

    fun setAreaAlgA(
        startPoint: Point,
        areaColor: Color,
        borderColor: Color = BLACK
    ) {
        val stack = Stack<Point>()
        stack.add(startPoint)
        while (stack.isNotEmpty()) {
            val pixel = stack.pop()
            if (pixel.x >= image.width - 1 || pixel.y >= image.height - 1 || pixel.x <= 0 || pixel.y <= 0) continue

            setPixel(pixel, color = areaColor)

            var newPixel = pixel.copy(x = pixel.x + 1)
            getPixelColor(newPixel).also { pixelColor ->
                if (pixelColor != borderColor && pixelColor != areaColor) {
                    stack.add(newPixel)
                }
            }

            newPixel = pixel.copy(y = pixel.y + 1)
            getPixelColor(newPixel).also { pixelColor ->
                if (pixelColor != borderColor && pixelColor != areaColor) {
                    stack.add(newPixel)
                }
            }

            newPixel = pixel.copy(x = pixel.x - 1)
            getPixelColor(newPixel).also { pixelColor ->
                if (pixelColor != borderColor && pixelColor != areaColor) {
                    stack.add(newPixel)
                }
            }

            newPixel = pixel.copy(y = pixel.y - 1)
            getPixelColor(newPixel).also { pixelColor ->
                if (pixelColor != borderColor && pixelColor != areaColor) {
                    stack.add(newPixel)
                }
            }
        }
    }

    fun setAreaAlgB(
        startPoint: Point,
        areaColor: Color,
        borderColor: Color = BLACK
    ) {
        val stack = Stack<Point>()
        stack.add(startPoint)
        while (stack.isNotEmpty()) {
            val pixel = stack.pop()

        }

    }

    companion object {
        val BACKGROUND_COLOR: Color = Color.WHITE
    }
}
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.sqrt

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
//        Thread.sleep(1)
        repaint()
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
            if (pixel.x >= image.width - 1 || pixel.y >= image.height - 1 || pixel.x < 0 || pixel.y < 0) continue

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
            if (pixel.x >= image.width - 1 || pixel.y >= image.height - 1 || pixel.x < 0 || pixel.y < 0) continue
            setPixel(pixel, areaColor)

            val keyPoint = pixel.x

            var x = keyPoint
            x++
            while (x > 0 && x < image.width - 1 && getPixelColor(Point(x, pixel.y)) != borderColor) {
                setPixel(Point(x, pixel.y), areaColor)
                x++
            }

            val xRight = x - 1
            x = keyPoint
            x--
            while (x > 0 && x < image.width - 1 && getPixelColor(Point(x, pixel.y)) != borderColor) {
                setPixel(Point(x, pixel.y), areaColor)
                x--
            }

            val xLeft = x + 1
            x = xLeft
            var y = pixel.y + 1
            while (x <= xRight) {
                var flag = false
                while (getPixelColor(Point(x, y)) != borderColor && getPixelColor(
                        Point(x, y)
                    ) != areaColor && x < xRight
                ) {
                    if (!flag) {
                        flag = true
                    }
                    x++
                }

                if (flag) {
                    if (x == xRight && getPixelColor(Point(x, y)) != borderColor && getPixelColor(
                            Point(x, y)
                        ) != areaColor
                    ) {
                        stack.add(Point(x, y))
                    } else {
                        stack.add(Point(x - 1, y))
                    }
                }

                val xEnter = x
                while (getPixelColor(Point(x, y)) == borderColor || getPixelColor(
                        Point(x, y)
                    ) == areaColor && x < xRight
                ) {
                    x++
                }

                if (x == xEnter) {
                    x++
                }
            }

            x = xLeft
            y = pixel.y - 1
            while (x <= xRight) {
                var flag = false
                while (x > 0 && x < image.width - 1 && y > 0 && y < image.height - 1 && getPixelColor(
                        Point(x, y)
                    ) != borderColor && getPixelColor(
                        Point(x, y)
                    ) != areaColor && x < xRight
                ) {
                    if (!flag) {
                        flag = true
                    }
                    x++
                }

                if (flag) {
                    if (x > 0 && x < image.width - 1 && y < image.height - 1 && x == xRight && getPixelColor(
                            Point(x, y)
                        ) != borderColor && getPixelColor(
                            Point(x, y)
                        ) != areaColor
                    ) {
                        stack.add(Point(x, y))
                    } else {
                        stack.add(Point(x - 1, y))
                    }
                }

                val xEnter = x
                while (getPixelColor(Point(x, y)) == borderColor || getPixelColor(
                        Point(x, y)
                    ) == areaColor && x < xRight
                ) {
                    x++
                }

                if (x == xEnter) {
                    x++
                }
            }
        }
    }

    val temp = 50000

    fun testAlgA(pointB: Point, pointA: Point, pointC: Point) {
        val millisStart = System.currentTimeMillis()
        val pointD = findMiddlePoint(pointA, pointC)
        for (i in 0..temp) {
            setLineAlgA(pointB, pointD)
        }
        val millisEnd = System.currentTimeMillis()
        println("a " + (millisEnd - millisStart))
    }

    fun testAlgB(pointB: Point, pointA: Point, pointC: Point) {
        val millisStart = System.currentTimeMillis()
        val pointD = findMiddlePoint(pointA, pointC)
        for (i in 0..temp) {
            setLineAlgB(pointB, pointD)
        }
        val millisEnd = System.currentTimeMillis()
        println("b " + (millisEnd - millisStart))
    }

    fun testAreaAlgA(middleAOE: Point) {
        val millisStart = System.currentTimeMillis()
        for (i in 0..temp) {
            setAreaAlgA(middleAOE, Color.RED)
        }
        val millisEnd = System.currentTimeMillis()
        println("a " + (millisEnd - millisStart))
    }

    fun testAreaAlgB(middleAOE: Point) {
        val millisStart = System.currentTimeMillis()
        for (i in 0..temp) {
            setAreaAlgB(middleAOE, Color.RED)
        }
        val millisEnd = System.currentTimeMillis()
        println("b " + (millisEnd - millisStart))
    }

    fun createTriangleAlgA(baseLength: Int, sideLength: Int, startPoint: Point) {
        //каркас
        val h = sqrt((sideLength * sideLength - baseLength.toFloat() / 2 * baseLength / 2)).toInt()
        val pointA = startPoint
        val pointC = pointA.copy(x = pointA.x + baseLength)
        val pointB = Point(pointA.x + baseLength / 2, pointA.y + h)
        setLineAlgA(pointA, pointC)
        setLineAlgA(pointA, pointB)
        setLineAlgA(pointB, pointC)

//        //тестирование на быстроту
//        testAlgA(pointB, pointA, pointC)

        //медианы
        val pointD = findMiddlePoint(pointA, pointC)
        val pointF = findMiddlePoint(pointC, pointB)
        val pointE = findMiddlePoint(pointB, pointA)
        setLineAlgA(pointA, pointF)
        setLineAlgA(pointC, pointE)
        setLineAlgA(pointB, pointD)

        //точка пересечения медиан
        val pointO = Point(pointD.x, pointD.y + h * 1 / 3)

        //Находим middlePoints для подтреугольников
        //для первого
        val pointAE = findMiddlePoint(pointA, pointE)
        val pointAO = findMiddlePoint(pointA, pointO)
        val pointEO = findMiddlePoint(pointE, pointO)
        //для второго
        val pointEB = findMiddlePoint(pointE, pointB)
        val pointBO = findMiddlePoint(pointB, pointO)
        //pointEO
        //для третьего
        val pointBF = findMiddlePoint(pointB, pointF)
        val pointFO = findMiddlePoint(pointF, pointO)
        //pointOB
        //для четвертого
        val pointFC = findMiddlePoint(pointF, pointC)
        val pointCO = findMiddlePoint(pointC, pointO)
        //pointOF
        //для пятого
        val pointOD = findMiddlePoint(pointO, pointD)
        //OC
        val pointDC = findMiddlePoint(pointD, pointC)
        //для шестого
        //DO
        val pointAD = findMiddlePoint(pointA, pointD)
        //AO

        val middleAOE = Point((pointA.x + pointO.x + pointE.x) / 3, (pointA.y + pointO.y + pointE.y) / 3)
        val middleEBO = Point((pointE.x + pointB.x + pointO.x) / 3, (pointE.y + pointB.y + pointO.y) / 3)
        val middleBOF = Point((pointB.x + pointO.x + pointF.x) / 3, (pointB.y + pointO.y + pointF.y) / 3)
        val middleFOC = Point((pointF.x + pointO.x + pointC.x) / 3, (pointF.y + pointO.y + pointC.y) / 3)
        val middleCOD = Point((pointC.x + pointO.x + pointD.x) / 3, (pointC.y + pointO.y + pointD.y) / 3)
        val middleDOA = Point((pointD.x + pointO.x + pointA.x) / 3, (pointD.y + pointO.y + pointA.y) / 3)

//        testAreaAlgA(middleAOE)
        setAreaAlgA(middleAOE, Color.RED)
        setAreaAlgA(middleEBO, Color.YELLOW)
        setAreaAlgA(middleBOF, Color.GREEN)
        setAreaAlgA(middleFOC, Color.CYAN)
        setAreaAlgA(middleCOD, Color.BLUE)
        setAreaAlgA(middleDOA, Color(13631963))

        //рисование медиан
        setLineAlgA(pointE, pointAO)
        setLineAlgA(pointO, pointAE)
        setLineAlgA(pointA, pointEO)

        setLineAlgA(pointE, pointBO)
        setLineAlgA(pointB, pointEO)
        setLineAlgA(pointO, pointEB)

        setLineAlgA(pointB, pointFO)
        setLineAlgA(pointF, pointBO)
        setLineAlgA(pointO, pointBF)

        setLineAlgA(pointO, pointFC)
        setLineAlgA(pointF, pointCO)
        setLineAlgA(pointC, pointFO)

        setLineAlgA(pointO, pointDC)
        setLineAlgA(pointC, pointOD)
        setLineAlgA(pointD, pointCO)

        setLineAlgA(pointA, pointOD)
        setLineAlgA(pointO, pointAD)
        setLineAlgA(pointD, pointAO)

        //линия по диагоналям
        setLineAlgA(middleAOE, middleEBO, Color.WHITE)
        setLineAlgA(middleEBO, middleBOF, Color.WHITE)
        setLineAlgA(middleBOF, middleFOC, Color.WHITE)
        setLineAlgA(middleFOC, middleCOD, Color.WHITE)
        setLineAlgA(middleCOD, middleDOA, Color.WHITE)
        setLineAlgA(middleDOA, middleAOE, Color.WHITE)
    }

    fun createTriangleAlgB(baseLength: Int, sideLength: Int, startPoint: Point) {
        //каркас
        val h = sqrt((sideLength * sideLength - baseLength.toFloat() / 2 * baseLength / 2)).toInt()
        val pointA = startPoint
        val pointC = pointA.copy(x = pointA.x + baseLength)
        val pointB = Point(pointA.x + baseLength / 2, pointA.y + h)
        setLineAlgB(pointA, pointC)
        setLineAlgB(pointA, pointB)
        setLineAlgB(pointB, pointC)

//        //тестирование на быстроту
//        testAlgB(pointB, pointA, pointC)

        //медианы
        val pointD = findMiddlePoint(pointA, pointC)
        val pointF = findMiddlePoint(pointC, pointB)
        val pointE = findMiddlePoint(pointB, pointA)
        setLineAlgB(pointA, pointF)
        setLineAlgB(pointC, pointE)
        setLineAlgB(pointB, pointD)

        //точка пересечения медиан
        val pointO = Point(pointD.x, pointD.y + h * 1 / 3)

        //Находим middlePoints для подтреугольников
        //для первого
        val pointAE = findMiddlePoint(pointA, pointE)
        val pointAO = findMiddlePoint(pointA, pointO)
        val pointEO = findMiddlePoint(pointE, pointO)
        //для второго
        val pointEB = findMiddlePoint(pointE, pointB)
        val pointBO = findMiddlePoint(pointB, pointO)
        //pointEO
        //для третьего
        val pointBF = findMiddlePoint(pointB, pointF)
        val pointFO = findMiddlePoint(pointF, pointO)
        //pointOB
        //для четвертого
        val pointFC = findMiddlePoint(pointF, pointC)
        val pointCO = findMiddlePoint(pointC, pointO)
        //pointOF
        //для пятого
        val pointOD = findMiddlePoint(pointO, pointD)
        //OC
        val pointDC = findMiddlePoint(pointD, pointC)
        //для шестого
        //DO
        val pointAD = findMiddlePoint(pointA, pointD)
        //AO

        val middleAOE = Point((pointA.x + pointO.x + pointE.x) / 3, (pointA.y + pointO.y + pointE.y) / 3)
        val middleEBO = Point((pointE.x + pointB.x + pointO.x) / 3, (pointE.y + pointB.y + pointO.y) / 3)
        val middleBOF = Point((pointB.x + pointO.x + pointF.x) / 3, (pointB.y + pointO.y + pointF.y) / 3)
        val middleFOC = Point((pointF.x + pointO.x + pointC.x) / 3, (pointF.y + pointO.y + pointC.y) / 3)
        val middleCOD = Point((pointC.x + pointO.x + pointD.x) / 3, (pointC.y + pointO.y + pointD.y) / 3)
        val middleDOA = Point((pointD.x + pointO.x + pointA.x) / 3, (pointD.y + pointO.y + pointA.y) / 3)

//        testAreaAlgB(middleAOE)
        setAreaAlgB(middleAOE, Color.RED)
        setAreaAlgB(middleEBO, Color.YELLOW)
        setAreaAlgB(middleBOF, Color.GREEN)
        setAreaAlgB(middleFOC, Color.CYAN)
        setAreaAlgB(middleCOD, Color.BLUE)
        setAreaAlgB(middleDOA, Color(13631963))

        //рисование медиан
        setLineAlgB(pointE, pointAO)
        setLineAlgB(pointO, pointAE)
        setLineAlgB(pointA, pointEO)

        setLineAlgB(pointE, pointBO)
        setLineAlgB(pointB, pointEO)
        setLineAlgB(pointO, pointEB)

        setLineAlgB(pointB, pointFO)
        setLineAlgB(pointF, pointBO)
        setLineAlgB(pointO, pointBF)

        setLineAlgB(pointO, pointFC)
        setLineAlgB(pointF, pointCO)
        setLineAlgB(pointC, pointFO)

        setLineAlgB(pointO, pointDC)
        setLineAlgB(pointC, pointOD)
        setLineAlgB(pointD, pointCO)

        setLineAlgB(pointA, pointOD)
        setLineAlgB(pointO, pointAD)
        setLineAlgB(pointD, pointAO)

        //линия по диагоналям
        setLineAlgB(middleAOE, middleEBO, Color.WHITE)
        setLineAlgB(middleEBO, middleBOF, Color.WHITE)
        setLineAlgB(middleBOF, middleFOC, Color.WHITE)
        setLineAlgB(middleFOC, middleCOD, Color.WHITE)
        setLineAlgB(middleCOD, middleDOA, Color.WHITE)
        setLineAlgB(middleDOA, middleAOE, Color.WHITE)
    }

    private fun findMiddlePoint(firstPoint: Point, lastPoint: Point): Point =
        Point((firstPoint.x + lastPoint.x) / 2, (firstPoint.y + lastPoint.y) / 2)

    companion object {
        val BACKGROUND_COLOR: Color = Color.WHITE
    }
}
import java.awt.Color
import java.awt.Dimension

fun main() {
    val rendererA = Renderer(Dimension(1200, 1200))
    rendererA.createTriangleAlgA(1000, 1000, startPoint = Point(10, 10))

    val rendererB = Renderer(Dimension(1200, 1200))
    rendererB.createTriangleAlgB(1000, 1000, startPoint = Point(10, 10))
}

val BLACK = Color(0, 0, 1)
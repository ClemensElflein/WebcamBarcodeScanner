
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamResolution
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.Color
import java.awt.Paint
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import javax.swing.JFrame


val webcam: Webcam = Webcam.getDefault()

class ScannerSupplier : WebcamPanel.ImageSupplier {
    val reader = MultiFormatReader()
    val robot = Robot()
    override fun get(): BufferedImage {
        val image = webcam.image


        val roi = Rectangle(0,image.height/2-50,image.width, 100)

        val graphics = image.createGraphics()
        graphics.paint = Color(0.0f,0.0f,0.0f,0.6f)
        graphics.fillRect(0, 0,image.width, roi.y)
        graphics.fillRect(0, roi.y+roi.height,image.width, image.height - roi.y+roi.height)


        val source = BufferedImageLuminanceSource(image.getSubimage(roi.x, roi.y, roi.width, roi.height))
        val bitmap = BinaryBitmap(HybridBinarizer(source))


        val result = try {
            reader.decode(bitmap)
        } catch (e:Exception) {
            null
        }

        if(result != null) {
            println(result.barcodeFormat.name)
            if (result.barcodeFormat !in listOf(BarcodeFormat.DATA_MATRIX)) {
                println(result.text)
                val resultString = result.text
                for (c in resultString) {
                    val key = c.uppercaseChar().toInt()
                    robot.keyPress(key)
                    robot.keyRelease(key)
                }
                robot.keyPress(KeyEvent.VK_ENTER)
                robot.keyRelease(KeyEvent.VK_ENTER)
            }
        }

        return image
    }

}

fun main() {


    webcam.viewSize = WebcamResolution.VGA.size


    val panel = WebcamPanel(webcam, webcam.viewSize, true, ScannerSupplier())

    panel.isFPSDisplayed = true
    panel.isDisplayDebugInfo = true
    panel.isImageSizeDisplayed = true
    panel.isMirrored = false



    val window = JFrame("Test webcam panel")
    window.add(panel)
    window.isResizable = true
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    window.pack()
    window.isVisible = true


}
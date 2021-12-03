import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamResolution
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.AWTKeyStroke
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.swing.JFrame


val webcam: Webcam = Webcam.getDefault()

data class CreatePartInfo(val title: String, val footprint: String, val description: String, val mpn: String)

val robot = Robot()
val c: Clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

private fun queryLCSC(json: String): CreatePartInfo {

    val product_code = json.substringAfter("pc:").substringBefore(",")
    val product = json.substringAfter("pm:").substringBefore(",")
    val qty = json.substringAfter("qty:").substringBefore(",")

    val url = "https://lcsc.com/product-detail/$product_code.html"

    println("Fetching $url...")

    val doc: Document = Jsoup.connect(url).get()
    println("got the document")

    val title = doc.select(".desc").select("h2").first()?.text() ?: "NO INFO"


    val footprint = doc.select(":containsOwn(Package)").next().text()
    val description = "LCSC-$product_code; " + doc.select(":containsOwn(Description)").next().text()
    val manufacturer = doc.select(":containsOwn(Manufacturer)").next().text()
    val mpn = product

    println("$title, $footprint, $description, $mpn")

    paste(title)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)
    paste(footprint)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)

    paste(description)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)
    paste(manufacturer)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)
    robot.keyPress(KeyEvent.VK_TAB)
    robot.keyRelease(KeyEvent.VK_TAB)
    paste(mpn)
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.keyRelease(KeyEvent.VK_ENTER)
    paste(qty)


    return CreatePartInfo(title, footprint, description, mpn)
}

class ScannerSupplier : WebcamPanel.ImageSupplier {
    val reader = MultiFormatReader()
    var lastScanTime = 0L
    override fun get(): BufferedImage {
        var image = webcam.image

        val source = BufferedImageLuminanceSource(image)

        val bitmap = BinaryBitmap(HybridBinarizer(source))


        val result = try {
            reader.decode(bitmap)
        } catch (e: Exception) {
            null
        }

        if (result != null) {
            val time = System.currentTimeMillis()
            if (time - lastScanTime > 2000) {
                if (result.barcodeFormat == BarcodeFormat.QR_CODE) {
                    val json = result.text

                    println(json)


                    println("querying LCSC")
                    val createPartInfo = queryLCSC(json)
                }
            }
            lastScanTime = time
        }


        return image
    }


}

fun paste(text: String) {

    Thread.sleep(100)
    val stringSelection = StringSelection(text)
    c.setContents(stringSelection, stringSelection)

    Thread.sleep(100)
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_V)
    robot.keyRelease(KeyEvent.VK_V)
    robot.keyRelease(KeyEvent.VK_CONTROL)
}

fun type(text: String) {
    for (c in text) {
        println(c)
        val stroke = AWTKeyStroke.getAWTKeyStroke(c.toString())
        println(stroke.keyCode)
        robot.keyPress(stroke.keyCode)
        robot.keyRelease(stroke.keyCode)
        /*
        if(c.isUpperCase())
        {
            println("$c is upper caseasdf-")
        }
        val key = c.uppercaseChar().toInt()
        robot.keyPress(key)
        robot.keyRelease(key)*/
    }
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.keyRelease(KeyEvent.VK_ENTER)
}

fun copyImage(source: BufferedImage): BufferedImage {
    val b = BufferedImage(source.width, source.height, source.type)
    val g = b.graphics
    g.drawImage(source, 0, 0, null)
    g.dispose()
    return b
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
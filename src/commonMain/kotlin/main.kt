import com.soywiz.korge.Korge
import com.soywiz.korge.annotations.KorgeExperimental
import com.soywiz.korge.resources.resources
import com.soywiz.korge.ui.uiTextInput
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.*
import com.soywiz.korim.vector.*
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.json.toJson
import com.soywiz.korio.serialization.xml.Xml


data class Symbol(
    var name:String = "",
    var  viewBox:String = "0, 0, 10, 21.5",
    var description:String = "",
    var path:String = "M0,0"
)

@OptIn(KorgeExperimental::class)
suspend fun main() = Korge(width = 1800, height = 900, bgcolor = Colors["#2b2b2b"]) {


    val symDir = resourcesVfs["symbols"].listNames()
    val symbolsList = extractSymInfo(symDir)

    val outMapJSON = mutableMapOf<String, Map<String, Any>>()

    symbolsList.forEach{ symbol ->
        val jsonSymbol = mapOf(
            "viewBox" to symbol.viewBox,
            "description" to symbol.description,
            "path" to symbol.path
        )
        outMapJSON[symbol.name] = jsonSymbol
    }

    val outputJSON= outMapJSON.toJson(true)
 
    localVfs("C:/FUNuage/Symbols/symbols.json").writeString(outputJSON)

}

private suspend fun extractSymInfo(symDir: List<String>):List<Symbol> {
    val symList = mutableListOf<Symbol>()

    for (item in symDir) {
        val svg = resourcesVfs["symbols/$item"].readString()
        val xml = Xml(svg)

        val dwidth = xml.double("width", 6.0)
        val dheight = xml.double("height", 21.0)
        val viewBox = xml.getString("viewBox") ?: "0 0 $dwidth $dheight"
        val pathList = xml.allChildren.filter { it.toString().contains("path") }

        var path = ""
        for (pL in pathList) {
            path += pL.str("d").replace('\n', ' ')
        }
        val desc = xml.text.split('\n')[1].split('.')[1].trim()
        val sName = item.split('.')[0]

        val newSymbol = Symbol(sName, viewBox, desc, path)
        symList.add(newSymbol)
    }

    return symList
}
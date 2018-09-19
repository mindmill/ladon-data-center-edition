package de.mc.ladon.server.boot.tables

/**
 * @author Ralf Ulrich
 * 16.09.16
 */
data class TableObject(val title: String, val headers: List<String>, val rows: List<TableRow>)

data class TableRow(val cells : List<TableCell>,  val color: Color )

data class TableCell(val text: String?, val link: String? = null)

enum class Color(val css : String){
    GREEN("bg-color-green"),
    BLUE("bg-color-blue"),
    RED("bg-color-red"),
    NONE("")

}
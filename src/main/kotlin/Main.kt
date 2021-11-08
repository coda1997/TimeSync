// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

@Composable
@Preview
fun App() {
    var output by remember { mutableStateOf("结果") }
    var filePath by remember { mutableStateOf("./offsets.csv") }
    val (showDialog, setShowDialog) =  remember { mutableStateOf(false) }
    var message:String = ""
    DesktopMaterialTheme {

        Column {
            Row {
                Text("保存路径：")
                TextField(filePath, {
                    filePath = it
                })
            }

            Button(onClick = {
                try {
                    val res = refreshDevices(filePath)
                    output += res
                } catch (e:Exception) {
                    setShowDialog(true)
                    message = e.localizedMessage
                }
            }) {
                Text("计算offset")
            }

            Text(output)
            DialogDemo(showDialog, setShowDialog, message)
        }
    }
}

fun refreshDevices(path: String):String{
    val process = Runtime.getRuntime().exec("adb shell date +%s%N")
    val input = process.inputStream
    val bufferBytes = input.readAllBytes()
    val remoteTime = String(bufferBytes).trimEnd().toLong().div(1000000)

    val localTime = System.currentTimeMillis()
    val header = LocalDateTime.now()
    val temp =
        "${header}: 本地计算机时间: ${localTime}, 手机时间: ${remoteTime}, offset(remote-local): ${remoteTime - localTime}\n"
    println(temp)
    //save to local files
    val res = "${header}, ${localTime}, ${remoteTime}, ${remoteTime - localTime}\n"
    saveToFile(res, path)
    return temp
}

fun saveToFile(content:String, path:String){
    val f = File(path)
    if (!f.exists()) {
        f.createNewFile()
    }
    f.appendText(content)
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialogDemo(showDialog: Boolean, setShowDialog: (Boolean) -> Unit, message:String) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                Text("错误")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Change the state to close the dialog
                        setShowDialog(false)
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Change the state to close the dialog
                        setShowDialog(false)
                    },
                ) {
                    Text("Dismiss")
                }
            },
            text = {
                Text(message)
            },
        )
    }
}

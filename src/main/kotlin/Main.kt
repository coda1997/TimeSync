// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.time.LocalDateTime
import kotlin.math.max

@Composable
@Preview
fun App() {
    var output by remember { mutableStateOf("结果\n") }
    var filePath by remember { mutableStateOf("./offsets.csv") }
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    var num by remember { mutableStateOf(10) }
    var message = ""
    val scroll = rememberScrollState(0)

    DesktopMaterialTheme {

        Column(modifier = Modifier.padding(5.dp)) {
            Row {
                Text("保存路径：")
                TextField(filePath, {
                    filePath = it
                })
            }
            Row {
                Text("计算${num}次")
                Button(onClick = {
                    num = max(num - 1, 1)
                }) {
                    Text("-1")
                }
                Button(onClick = {
                    num++
                }) {
                    Text("+1")
                }
            }
            Button(onClick = {
                try {
                    val res = refreshDevices(filePath, num)
                    output += res
                } catch (e: Exception) {
                    setShowDialog(true)
                    message = e.localizedMessage
                }
            }) {
                Text("计算offset")
            }

            Text(output, modifier = Modifier.verticalScroll(scroll))
            DialogDemo(showDialog, setShowDialog, message)
        }
    }
}

fun refreshDevices(path: String, count: Int): String {
    val localTimes = LongArray(count)
    val remoteTimes = LongArray(count)
    for (i in 0 until count) {
        val process = Runtime.getRuntime().exec("adb shell date +%s%N")
        val localTime = System.currentTimeMillis()
        val input = process.inputStream
        val bufferBytes = input.readAllBytes()
        val remoteTime = String(bufferBytes).trimEnd().toLong().div(1000000)
        localTimes[i] = localTime
        remoteTimes[i] = remoteTime
    }
    val localTime = localTimes.average()
    val remoteTime = remoteTimes.average()
    val header = LocalDateTime.now()
    val temp =
        "${header}: 本地计算机时间: ${localTime}, 手机时间: ${remoteTime}, offset(remote-local): ${"%.2f".format(remoteTime - localTime)}\n"
    println(temp)
    //save to local files
    val res = "${header}, ${localTime}, ${remoteTime}, ${remoteTime - localTime}\n"
    saveToFile(res, path)
    return temp
}

fun saveToFile(content: String, path: String) {
    val f = File(path)
    if (!f.exists()) {
        f.createNewFile()
    }
    f.appendText(content)
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "TimeSync") {
        App()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialogDemo(showDialog: Boolean, setShowDialog: (Boolean) -> Unit, message: String) {
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

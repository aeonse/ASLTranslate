package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.myapplication.ml.Model
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.checkerframework.checker.nullness.qual.Nullable
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : ComponentActivity() {

    lateinit var camera: Button
    lateinit var gallery: Button
    lateinit var imageView: ImageView
    lateinit var result: TextView
    private val imageSize = 32

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            setContentView(R.layout.layout)

            camera = findViewById(R.id.button)
            gallery = findViewById(R.id.button2)
            result = findViewById(R.id.result)
            imageView = findViewById(R.id.imageView)

            /*camera.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission) == PackageManager.PERMISSION_GRANTED) {
                }
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, 3)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
                }
            }*/

            gallery.setOnClickListener {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 1)
            }
        }



        fun classifyImage(image: Bitmap) {
            try {
                val model = Model.newInstance(applicationContext)

                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
                val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(imageSize * imageSize)
                image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
                var pixel = 0
                for (i in 0 until imageSize) {
                    for (j in 0 until imageSize) {
                        val value = intValues[pixel++]
                        byteBuffer.putFloat(((value shr 16) and 0xFF) * (1.0f / 1))
                        byteBuffer.putFloat(((value shr 8) and 0xFF) * (1.0f / 1))
                        byteBuffer.putFloat((value and 0xFF) * (1.0f / 1))
                    }
                }

                inputFeature0.loadBuffer(byteBuffer)

                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                val confidences = outputFeature0.floatArray
                var maxPos = 0
                var maxConfidence = 0.0f
                for (i in confidences.indices) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i]
                        maxPos = i
                    }
                }
                val classes = arrayOf("Apple", "Banana", "Orange")
                result.text = classes[maxPos]

                model.close()
            } catch (e: IOException) {
                // TODO Handle the exception
            }
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == RESULT_OK) {
                when (requestCode) {
                    3 -> {
                        val image = data?.extras?.get("data") as Bitmap
                        val dimension = Math.min(image.width, image.height)
                        val thumbnail = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                        imageView.setImageBitmap(thumbnail)

                        val scaledImage =
                            Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)
                        classifyImage(scaledImage)
                    }

                    else -> {
                        val uri = data?.data
                        var image: Bitmap? = null
                        try {
                            image = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        imageView.setImageBitmap(image)

                        image?.let {
                            val scaledImage =
                                Bitmap.createScaledBitmap(it, imageSize, imageSize, false)
                            classifyImage(scaledImage)
                        }
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            Greeting("Android")
        }
    }
}
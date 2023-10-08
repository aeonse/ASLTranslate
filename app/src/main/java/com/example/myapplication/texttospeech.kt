package com.example.texttospeech

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.texttospeech.ui.theme.TextToSpeechTheme
class MainActivity : AppCombatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var buttonSpeak : Button? = null
    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSpeak = this.button_speak
        editText = this.etditext_input

        buttonSpeak!!.isEnabled = false
        tts = TextToSpeech(context:this, listener:this)

        buttonSpeak!!.setOnClickListener {     it:View!
            speak()
        }
    }
    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeTest(context:this, text:"The Language specified is not supported!", Toast.LENGTH_SHORT).show()
            } else {
                buttonSpeak!!.isEnabled = true
            }
        }else {
            Toast.makeText(context:this, text:"Initialization Failed!", Toast.LENGTH_SHORT).show()
        }
    }
}
private fun speak() {
    val text = editText!!.text.toString()
    tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, params:null, utteranceld:"")
}
override fun onDestroy() {
    if (tts != null) {
        tts!!.stop()
        tts!!.shutdown()
    }
    super.OnDestroy()
}
}




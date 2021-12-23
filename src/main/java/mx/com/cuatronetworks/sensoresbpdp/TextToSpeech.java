/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import java.io.InputStream;
 
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;
 
import javazoom.jl.decoder.JavaLayerException;
import mx.com.cuatronetworks.sensoresbpdp.model.AudioPlayer;

/**
 *
 * @author david
 */
public class TextToSpeech {
    
    private final AmazonPolly polly;
    
    
    public TextToSpeech(Region region){
      // create an Amazon Polly client in a specific region
       // Use your access key id and access secret key
        // Obtain it from AWS console
        //
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("AKIAURYO3RK56DA2WC3I","X2moynxt6jDnJW/t9fkcRGZtso37BHh83dNqIR0J");
        //
        // Create an Amazon Polly client in a specific region
        //
        this.polly = AmazonPollyClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(region.getName()).build();
   

    }
    public void play(String text) throws IOException, JavaLayerException {
        //
        // Get the audio stream created using the text
        //
        InputStream speechStream = this.synthesize(text, OutputFormat.Mp3);
        //
        // Play the audio
        //
        AudioPlayer.play(speechStream);
    }
 
    @SuppressWarnings("empty-statement")
    public InputStream synthesize(String text, OutputFormat format) throws IOException {
        //
        // Get the default voice
        //
        Voice voice = this.getVoice();
        voice.setId("Lupe");
       /* SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId()).withOutputFormat(format).withEngine("neural");
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);*/
        //
        // Create speech synthesis request comprising of information such as following:
        // Text
        // Voice
        // The detail will be used to create the speech
        //
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId()).withOutputFormat(format);
        //
        // Create the speech
        //
        SynthesizeSpeechResult synthRes = this.polly.synthesizeSpeech(synthReq);
        //
        // Returns the audio stream
        //
        return synthRes.getAudioStream();
    }
 
    public Voice getVoice() {
        //
        // Create describe voices request.
        //
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        // Synchronously ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = this.polly.describeVoices(describeVoicesRequest);
        return describeVoicesResult.getVoices().get(0);
    }
}

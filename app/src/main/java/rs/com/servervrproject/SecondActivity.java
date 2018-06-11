package rs.com.servervrproject;

import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    private VrVideoView videoWidgetView;
    private VideoLoaderTask backgroundVideoLoaderTask;
    private Uri fileUri;
    private VrVideoView.Options videoOptions = new VrVideoView.Options();
    private boolean isMuted;
    private boolean isPaused = false;

    private TextView ipport;

    ServerSocket serverSocket;

    private LinearLayout ll;

    private String videoname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ipport = (TextView) findViewById(R.id.ipport);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Log.e(TAG, "onCreate: set " + ip);

        videoname = "congo";//getIntent().getStringExtra("videoname");

        videoOptions.inputFormat = getIntent().getIntExtra("inputFormat", VrVideoView.Options.FORMAT_DEFAULT);
        videoOptions.inputType = getIntent().getIntExtra("inputType", VrVideoView.Options.TYPE_MONO);
        fileUri = getIntent().getData();
        if (fileUri == null) {
            Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
        } else {
            Log.i(TAG, "Using file " + fileUri.toString());
        }

//        videoWidgetView = new VrVideoView(this);
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());

//        ll = (LinearLayout) findViewById(R.id.buttonlayout);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll.addView(videoWidgetView, lp);
//
//        ll.setVisibility(View.GONE);
//        ll.setVisibility(View.VISIBLE);
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();

//        play();

        ipport.setText("server ip address : " + getIpAddress() + "  port : 8080 ");

        Log.e(TAG, "onCreate: ip address " + getIpAddress());
    }
//192.168.2.108


    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());

        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {

        }

        @Override
        public void onClick() {
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {

        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }

    private void play(String videoname) {

        this.videoname = videoname;
        if (isPaused) {
            videoWidgetView.playVideo();
            isPaused = false;
        } else {
            if (backgroundVideoLoaderTask != null) {
                // Cancel any task from a previous intent sent to this activity.
                backgroundVideoLoaderTask.cancel(true);
            }
            backgroundVideoLoaderTask = new VideoLoaderTask();
            backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
//            isPaused = true;
        }
    }

    private void pause() {
        if (videoWidgetView != null) {
            videoWidgetView.pauseVideo();
            isPaused = true;
        }
    }

    private void stopVideo() {
        if (videoWidgetView != null) {
            videoWidgetView.pauseRendering();
            videoWidgetView.shutdown();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().

    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //192.168.0.120
    class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    VrVideoView.Options options = new VrVideoView.Options();
                    options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
//                    videoWidgetView.loadVideoFromAsset("congo.mp4", options);
//                    videoWidgetView.setDisplayMode(VrVideoView.DisplayMode.FULLSCREEN_MONO);
                    videoWidgetView.loadVideoFromAsset("congo.mp4", options);
                } else {
//                    videoWidgetView.setDisplayMode(VrVideoView.DisplayMode.FULLSCREEN_MONO);
                    videoWidgetView.loadVideo(fileInformation[0].first, fileInformation[0].second);
                }
            } catch (IOException e) {
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                videoWidgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(SecondActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                Log.e(TAG, "Could not open video: " + e);
            }

            return true;
        }
    }


    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                SecondActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        info.setText("I'm waiting here: "
//                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient = "";

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();
                    Log.e(TAG, "run: " + messageFromClient);
                    if (messageFromClient.equalsIgnoreCase("play")) {
                        play(messageFromClient);
                    } else if (messageFromClient.equalsIgnoreCase("pause")) {
                        pause();
                    } else {
                        play(messageFromClient);
                    }
//
//                    else if (messageFromClient.equalsIgnoreCase("stop")) {
//                        stopVideo();
//                    }


//                    count++;
//                    message += "#" + count + " from " + socket.getInetAddress()
//                            + ":" + socket.getPort() + "\n"
//                            + "Msg from client: " + messageFromClient + "\n";
//
//                    MainActivity.this.runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            msg.setText(message);
//                        }
//                    });
//
                    String msgReply = "Hello from Server, you are " + messageFromClient;
                    dataOutputStream.writeUTF(msgReply);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                final String errMsg = e.toString();
                SecondActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        msg.setText(errMsg);
                    }
                });

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}

package com.example.nexchat.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.nexchat.R
import com.example.nexchat.databinding.ActivityCallBinding
import com.example.nexchat.models.CallLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.nexchat.utils.Constants
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import java.util.UUID

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private var agoraEngine: RtcEngine? = null
    
    private val appId = Constants.AGORA_APP_ID
    private var channelName = ""
    private var token: String? = null
    
    private var isVideoCall = true
    private var isMuted = false
    private var remoteUid = 0
    private var isIncoming = false

    private val PERMISSION_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                remoteUid = uid
                binding.tvCallStatus.text = "Connected"
                binding.llCallInfo.visibility = if (isVideoCall) View.GONE else View.VISIBLE
                if (isVideoCall) {
                    setupRemoteVideo(uid)
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@CallActivity, "Call Ended", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                binding.tvCallStatus.text = if (isIncoming) "Connected" else "Calling..."
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {
                android.util.Log.e("CallActivity", "Agora Error: $err")
                if (err == 101) { // ERR_INVALID_APP_ID
                    Toast.makeText(this@CallActivity, "Invalid Agora App ID", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")
        val receiverId = intent.getStringExtra("uid")
        channelName = intent.getStringExtra("channel") ?: "test_channel"
        isVideoCall = intent.getBooleanExtra("isVideo", true)
        isIncoming = intent.getBooleanExtra("isIncoming", false)

        binding.tvCallUserName.text = name ?: "User"
        Glide.with(this).load(image).placeholder(R.drawable.ic_user_placeholder).into(binding.ivCallUser)

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_ID)
        } else {
            initCall()
        }

        binding.fabEndCall.setOnClickListener {
            endCall()
        }

        binding.fabMute.setOnClickListener {
            isMuted = !isMuted
            agoraEngine?.muteLocalAudioStream(isMuted)
            binding.fabMute.setImageResource(if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic)
        }

        binding.fabSwitchCamera.setOnClickListener {
            agoraEngine?.switchCamera()
        }
        
        if (!isVideoCall) {
            binding.localVideoViewContainer.visibility = View.GONE
            binding.remoteVideoViewContainer.visibility = View.GONE
            binding.fabSwitchCamera.visibility = View.GONE
        }
    }

    private fun initCall() {
        setupAgoraEngine()
        joinChannel()
        if (!isIncoming) {
            saveCallToHistory(intent.getStringExtra("name"), intent.getStringExtra("image"), intent.getStringExtra("uid"))
        }
    }

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                (!isVideoCall || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCall()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupAgoraEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            
            if (isVideoCall) {
                agoraEngine?.enableVideo()
                setupLocalVideo()
            } else {
                agoraEngine?.disableVideo()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Agora init failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLocalVideo() {
        val surfaceView = SurfaceView(baseContext)
        binding.localVideoViewContainer.removeAllViews()
        binding.localVideoViewContainer.addView(surfaceView)
        agoraEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = SurfaceView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.removeAllViews()
        binding.remoteVideoViewContainer.addView(surfaceView)
        agoraEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions()
        options.channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
        agoraEngine?.joinChannel(token, channelName, 0, options)
    }

    private fun saveCallToHistory(receiverName: String?, receiverImage: String?, receiverUid: String?) {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseFirestore.getInstance()
        val currentUid = auth.currentUser?.uid ?: return

        database.collection("users").document(currentUid).get().addOnSuccessListener { snapshot ->
            val callerName = snapshot.getString("name") ?: "User"
            val callerImage = snapshot.getString("profileImage") ?: ""

            val callLog = CallLog(
                callId = channelName,
                callerUid = currentUid,
                receiverUid = receiverUid ?: "",
                callerName = callerName,
                receiverName = receiverName ?: "User",
                callerImage = callerImage,
                receiverImage = receiverImage ?: "",
                timestamp = System.currentTimeMillis(),
                type = if (isVideoCall) "video" else "audio",
                isMissed = false,
                participants = listOf(currentUid, receiverUid ?: "")
            )

            database.collection("calls").document(callLog.callId).set(callLog)
            
            if (receiverUid != null) {
                database.collection("users").document(receiverUid).update("currentCallId", callLog.callId)
            }
        }
    }

    private fun endCall() {
        leaveChannel()
        finish()
    }

    private fun leaveChannel() {
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
    }
}

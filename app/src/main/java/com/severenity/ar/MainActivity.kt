package com.severenity.ar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "AR"
    private val CAMERA_REQUEST_CODE = 1

    private lateinit var arFragment: ArFragment

    private val pointer = PointerDrawable()
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        setupPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    buildScene()
                }
            }
        }
    }

    /**
     * Setup permissions if needed and were not setup before
     */
    private fun setupPermissions() {
        val permission = checkSelfPermission(Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to use camera denied.")
            makeRequest()
        } else {
            buildScene()
        }
    }

    /**
     * Request appropriate permissions
     */
    private fun makeRequest() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    /**
     * Build AR scene
     */
    private fun buildScene() {
        fabAdd.setOnClickListener {
            addObject(R.raw.model)
        }

//        arFragment.arSceneView.scene.setOnUpdateListener { frameTime ->
//            arFragment.onUpdate(frameTime)
//            onUpdate()
//        }
    }

    /**
     * Add object by URI
     */
    private fun addObject(resourceId: Int) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame == null) {
            return
        }

        val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
        for (hit in hits) {
            val trackable = hit.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                placeObject(arFragment, hit.createAnchor(), resourceId)
                break
            }
        }
    }

    private fun placeObject(fragment: ArFragment, createAnchor: Anchor, resourceId: Int) {
        ModelRenderable.builder()
                .setSource(fragment.context, resourceId)
                .build()
                .thenAccept {
                    addNodeToScene(fragment, createAnchor, it)
                }
                .exceptionally {
                    AlertDialog.Builder(this)
                        .setMessage(it.message).setTitle("Error!")
                        .create()
                        .show()
                    return@exceptionally null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, createAnchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(createAnchor)
        val rotatingNode = RotatingNode()
        val transformableNode = TransformableNode(fragment.transformationSystem)

        rotatingNode.renderable = renderable

        transformableNode.addChild(rotatingNode)
        transformableNode.setParent(anchorNode)

        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    private fun onUpdate() {
        val trackingChanged = updateTracking()
        val contentView = findViewById<View>(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }

        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                pointer.enabled = isHitting
                contentView.invalidate()
            }
        }
    }

    /**
     *
     */
    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame.camera.trackingState === TrackingState.TRACKING
        return isTracking != wasTracking
    }

    /**
     *
     */
    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(pt.x * 1.0f, pt.y * 1.0f)
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return android.graphics.Point(vw.width / 2, vw.height / 2)
    }
}
package com.severenity.ar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Sample code to display object within AR fragment
 *
 * @author Oleg Novosad
 * @date 07/05/2018
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "AR"
    private val CAMERA_REQUEST_CODE = 1

    private lateinit var arFragment: ArFragment

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
    }

    /**
     * Add object by URI
     *
     * @param resourceId id of the object to be added
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

    /**
     * Display object on the scene
     *
     * @param fragment {@link ArFragment} containing AR scene
     * @param createAnchor {@link Anchor} where object will be attached
     * @param resourceId id of the resource for object to display
     */
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

    /**
     * Wraps object into nodes and displays it on fragment
     *
     * @param fragment {@link ArFragment} containing AR scene
     * @param createAnchor {@link Anchor} where object will be attached
     * @param renderable {@link ModelRenderable} object to be wrapped within node
     */
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

    /**
     * Find screen center where object should be placed
     *
     * @return center of the screen
     */
    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return android.graphics.Point(vw.width / 2, vw.height / 2)
    }
}
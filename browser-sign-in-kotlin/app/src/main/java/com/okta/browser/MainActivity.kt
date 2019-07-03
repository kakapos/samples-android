package com.okta.browser

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.okta.oidc.*
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.clients.web.WebAuthClient
import com.okta.oidc.util.AuthorizationException

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.simpleName
    private lateinit var webAuthClient: WebAuthClient
    private lateinit var sessionClient: SessionClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = OIDCConfig.Builder()
            .withJsonFile(this, R.raw.config)
            .create()

        webAuthClient = Okta.WebAuthBuilder()
            .withConfig(config)
            .withContext(this)
            .setRequireHardwareBackedKeyStore(false)
            .create()

        sessionClient = webAuthClient.sessionClient

        webAuthClient.registerCallback(object : ResultCallback<AuthorizationStatus, AuthorizationException> {
            override fun onCancel() {
                status.text = "Operation cancelled"
            }

            override fun onError(msg: String?, exception: AuthorizationException?) {
                status.text = msg + exception?.toString()
                Log.d(tag, "", exception)
            }

            override fun onSuccess(result: AuthorizationStatus) {
                if (result == AuthorizationStatus.AUTHORIZED) {
                    val token = OktaIdToken.parseIdToken(webAuthClient.sessionClient.tokens.idToken!!)
                    status.text = token.claims.name + " signed in"
                } else if (result == AuthorizationStatus.SIGNED_OUT) {
                    status.text = "Signed out of browser"
                }
            }
        }, this)
    }

    fun signIn(view: View) {
        webAuthClient.signIn(this, null)
    }

    fun signOut(view: View) {
        webAuthClient.signOutOfOkta(this)
    }
}

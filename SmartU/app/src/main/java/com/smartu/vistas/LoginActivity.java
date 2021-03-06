package com.smartu.vistas;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.smartu.R;
import com.smartu.modelos.Usuario;
import com.smartu.utilidades.Constantes;
import com.smartu.utilidades.ConsultasBBDD;
import com.smartu.utilidades.ControladorPreferencias;
import com.smartu.utilidades.Encripta;
import com.smartu.utilidades.Sesion;
import com.smartu.utilidades.SliderMenu;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Para iniciar sesión también en la base de datos de Firebase
    //y poder enviar mensajes
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Hebra que va a comprobar el usuario y contraseña
    private HLogin hLogin = null;

    // Referencias a la UI.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Cargo el menú lateral
        SliderMenu sliderMenu = new SliderMenu(getApplicationContext(),this);
        sliderMenu.inicializateToolbar(getTitle().toString());
        //Inicializo los elementos de la interfaz
        inicializaUI();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    iniciarSesion();
                    return true;
                }
                return false;
            }
        });
        Button mRegistro = (Button) findViewById(R.id.btn_registro);
        Button mEmailSignInButton = (Button) findViewById(R.id.btn_iniciar_sesion);

        mRegistro.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(LoginActivity.this,RegistroActivity.class));
            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarSesion();
            }
        });

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        //Si el usuario está ya autentificado lo mando al Main
        if (Sesion.getUsuario(getApplicationContext()) != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }


    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializaUI(){
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Intenta iniciar sesión
     * Si hay errores (email no válido, campos no rellenos, etc.),
     * se muestran los errores y no se permite el inicio de sesion
     */
    private void iniciarSesion() {
        if (hLogin != null) {
            return;
        }

        // Reseteo los errores.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Guardo los valores
        String email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Compruebo que el password es válido
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_campo_obligatorio));
            focusView = mPasswordView;
            cancel = true;
        }else if(!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_password_incorrecto));
            focusView = mPasswordView;
            cancel = true;
        }

        // Compruebo que el email es correcto
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_campo_obligatorio));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_email_incorrecto));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            //Hay un error así que devuelvo el foco al campo concreto
            focusView.requestFocus();
        } else {
            //Muestro el progreso
            muestraProgreso(true);
            //Creo un usuario para el inicio de sesión
            Usuario usuario = new Usuario(email, Encripta.encriptar(password));
            //Creo la hebra para hacer el inicio de sesión
            hLogin = new HLogin(usuario);
            hLogin.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
    /**
     * Muestra el progreso de la hebra y esconde el formulario
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void muestraProgreso(final boolean mostrar) {
        // En Honeycomb MR2 tenemos ViewPropertyAnimator APIs, que nos permiten
        // animaciones de forma facil. Si esta disponible, uso esas APIs para hacer fade-in
        // del contador de progreso.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            //Cojo de los recursos de android un tiempo predefinido corto para la animación
            int tiempoAnimacion = getResources().getInteger(android.R.integer.config_shortAnimTime);
            //Si tengo que mostrar el progreso oculto el Formulario de Login sino lo mantengo visible
            mLoginFormView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
            //Establezco el tiempo y dependiendo de si tengo que mostrarla pongo el alpha o no y le agrego
            //un escuchador para cuando termine la animacion deje de mostrarse
            mLoginFormView.animate().setDuration(tiempoAnimacion).alpha(
                    mostrar ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
                }
            });
            //Muestro el progreso dependiendo de mostrar
            mProgressView.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            //Hago lo mismo para el progrewso que para el formulario de login
            mProgressView.animate().setDuration(tiempoAnimacion).alpha(
                    mostrar ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(mostrar ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // Si ViewPropertyAnimator APIs no esta disponible, muestro y escondo
            // el progreso y el login respectivamente
            mProgressView.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }*/
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    /**
     * HERBA LOGIN
     * Esta hebra va a quedarse en esta clase porque es intrínseca a ella y sólo se llama aquí
     */
    //////////////////////////////////////////////////////////////////////////////////////////
    private class HLogin  extends AsyncTask<Void, Void, Boolean> {

        private Usuario usuario;
        private SweetAlertDialog pDialog;

        HLogin(Usuario usuario) {
            this.usuario =usuario;
            pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Cargando...");
            pDialog.setCancelable(false);
        }
        @Override
        protected void onPreExecute() {
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: COMPLETAR LA TAREA DE LOGIN CUANDO ESTE EL SERVER.
            //Me creo el mapeador de JSON
            ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            String usuarioJSON = "";
            try {
                //Mapeo el usuario que me han pasado
                usuarioJSON = mapper.writeValueAsString(usuario);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            // Hacer llamada al servidor
             String resultado = ConsultasBBDD.hacerConsulta(ConsultasBBDD.consultaLogin,usuarioJSON,"POST");

            JSONObject res=null;
            if(resultado!=null) {
                try {
                    //Convierto el resultado a JSON
                    res = new JSONObject(resultado);
                    //Si el resultado es null signfica que no coincide usuario y contraseña
                    if (res != null) {
                        if (res.isNull("usuario"))
                            return false;
                        else {
                            JSONObject usuarioJSONServer = res.getJSONObject("usuario");
                            String password = usuario.getPassword();
                            //Mapeo el usuario que me han pasado para mantener la sesión abierta
                            usuario = mapper.readValue(usuarioJSONServer.toString(), Usuario.class);
                            usuario.setPassword(password);
                            return true;
                        }
                    } else
                        return false;

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }else
                return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            pDialog.dismissWithAnimation();
            //Libero memoria quitando la instancia de la hebra
            hLogin=null;
            //Dejo de mostrar el progreso
            muestraProgreso(false);
            //He conseguido hacer login en mi server
            if (success) {
                //Si he conseguido iniciar sesion voy a iniciar sesión en Firebase
                mFirebaseAuth.signInWithEmailAndPassword(usuario.getEmail(),password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Sino inicio sesion con firebase
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                } else {//Si he conseguido iniciar sesión
                                   Toast toast= Toast.makeText(LoginActivity.this, "Has Iniciado Sesión", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER,0,0);
                                    toast.show();
                                    //vamos al Main
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    //Una vez iniciado sesión actualizo el token en FCM
                                    updateFirebaseToken(task.getResult().getUser().getUid(), ControladorPreferencias.cargarToken(getApplicationContext()));
                                    //Guardo el FCM token en el usuario y el UID para que el usuario se quede con ellos para serializar
                                    usuario.setFirebaseToken(ControladorPreferencias.cargarToken(getApplicationContext()));
                                    usuario.setUid(task.getResult().getUser().getUid());
                                    //Serializo al usuario para que cuando se requiera esté inicializado
                                    Sesion.serializaUsuario(LoginActivity.this,usuario);

                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            } else {
                mPasswordView.setError(getString(R.string.error_password_incorrecto));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            pDialog.dismissWithAnimation();
            //Libero memoria quitando la instancia de la hebra
            hLogin=null;
            //Dejo de mostrar el progreso
            muestraProgreso(false);
        }
        private void updateFirebaseToken(String uid, String token) {
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Constantes.ARG_USERS)
                    .child(uid)
                    .child(Constantes.ARG_FIREBASE_TOKEN)
                    .setValue(token);
        }

    }



}


package jp.techacademy.hongou.yuka.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    ProgressDialog mProgress;

    FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> mCreateAccountListener;
    OnCompleteListener<AuthResult> mLoginListener;
    DatabaseReference mDatabaseReference;

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    boolean mIsCreateAccount = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //データベースへのリファレンスを取得
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //FirebaseAuthクラスのインスタンスを取得
        mAuth = FirebaseAuth.getInstance();

        //アカウント作成処理のリスナーを作成
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //成功した場合、ログインを行う
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                } else {
                    //失敗した場合、エラー表示を行う
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();

                    //プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }

            }
        };

        //ログイン処理のリスナー
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //成功した場合
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference useRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid());

                    if (mIsCreateAccount) {
                        //アカウント作成時はFirebaseに表示名を保存する
                        String name = mNameEditText.getText().toString();

                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        useRef.setValue(data);

                        // 表示名をPrefarenceに保存する
                        saveName(name);
                    } else {
                        useRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Map data = snapshot.getValue(Map.class);
                                saveName((String) data.get("name"));
                            }

                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });
                    }

                    //プログレスを非表示にする
                    mProgress.dismiss();

                    //アクティビティを閉じる
                    finish();

                } else {
                    //失敗した場合、エラー表示をする
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    //プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };

        //UIの準備
        setTitle("ログイン");

        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);
        mNameEditText = (EditText) findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if (email.length() != 0 && password.length() != 0 && name.length() >= 0) {
                    //ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;

                    createAccount(email, password);
                } else {
                    //エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    //フラグを落としておく
                    mIsCreateAccount = false;

                    login(email, password);
                } else {
                    //エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createAccount(String email, String password) {
        //プログレスダイアログを表示する
        mProgress.show();

        //アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
        }

    private void login(String email, String password) {
        //プログレスダイアログを表示する
        mProgress.show();

        //ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
    }

    private void saveName(String name) {
        //Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKey, name);
        editor.commit();
    }
}
